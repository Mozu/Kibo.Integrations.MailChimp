package com.mozu.mailchimp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecwid.mailchimp.MailChimpException;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerSegment;
import com.mozu.api.contracts.customer.CustomerSegmentCollection;
import com.mozu.api.resources.commerce.customer.CustomerSegmentResource;
import com.mozu.mailchimp.exception.MailchimpDuplicateMappingException;
import com.mozu.mailchimp.exception.MailchimpMappingReservedException;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;


@Service
public class CustomerSegmentSyncService {
	private static final Logger logger = LoggerFactory.getLogger(CustomerSegmentSyncService.class);
	@Autowired
	private MailchimpUtil mailChimpUtil;
	
	@Autowired
    IdMappingDao idMappingDao;
	
	@Autowired
    private MozuUtil mozuUtil;
	
	public CustomerSegmentSyncService() {
		// TODO Auto-generated constructor stub
	}

	public List<CustomerSegment> getCustomerSegments(ApiContext apiContext,int offset, int pageSize){
		CustomerSegmentResource customerSegmentResource= new CustomerSegmentResource(apiContext);
		CustomerSegmentCollection customerSegmentCollection=null;
		try {
			customerSegmentCollection=customerSegmentResource.getSegments(offset, pageSize, null, null, null);
			
		} catch (Exception e) {
			StringBuilder msg=new StringBuilder("Exception occured while getting the customer segemnts for tenant ")
			.append(apiContext.getTenantId())
			.append(": ")
			.append(e);
			logger.error(msg.toString(), e);
			throw new RuntimeException(msg.toString());
		}
		return customerSegmentCollection.getItems();
	}
	
	public CustomerSegment getCustomerSegment(ApiContext apiContext, Integer segmentId){
		CustomerSegmentResource customerSegmentResource= new CustomerSegmentResource(apiContext);
		CustomerSegment customerSegment=null;
		try {
			customerSegment=customerSegmentResource.getSegment(segmentId);
			
		} catch (Exception e) {
			StringBuilder msg=new StringBuilder("Exception occured while getting the customer segment for tenant ")
			.append(apiContext.getTenantId())
			.append(": ")
			.append(e);
			logger.error(msg.toString(), e);
			throw new RuntimeException(msg.toString());
		}
		return customerSegment;
	}
	
	public Integer sendSegmentToMailchimp(ApiContext apiContext, String mzSegmentId) throws Exception{
		CustomerSegment customerSegment=getCustomerSegment(apiContext, Integer.valueOf(mzSegmentId));
		return sendSegment(apiContext, customerSegment);
	}
	
	public void deleteStaticSegmentFromMailchimp(ApiContext apiContext, String mzSegmentId) throws Exception{
		MailchimpSyncModel setting = mozuUtil.getSettings(apiContext.getTenantId());
		Integer mcSegmentId=idMappingDao.getMailChimpSegmentId(apiContext.getTenantId(), mzSegmentId);
		if(mcSegmentId !=null){
			try{
			mailChimpUtil.deleteStaticSegement(setting.getApiKey(), setting.getMcListId(), mcSegmentId);
			
			}catch(Exception e){
				logger.debug("Error occured while deleting mailchimp segment "+mcSegmentId+" "+e);
				throw e;
			}
			
		}
		try{
		idMappingDao.deleteSegmentEntry(apiContext.getTenantId(), mzSegmentId);
		}catch(Exception e){
			logger.debug("Error occured while deleting mozu segment entry "+mzSegmentId +" in MZDB"+" "+e);
		}
	}

	
	public Integer sendSegment(ApiContext apiContext, CustomerSegment customerSegment) throws Exception{
		
		MailchimpSyncModel setting = mozuUtil.getSettings(apiContext.getTenantId());
		String mzSegmentId=customerSegment.getId().toString();
		Integer mcSegmentId=null;
		Integer mailchimpSegmentId=null;
		try{
			 mcSegmentId=idMappingDao.getMailChimpSegmentId(apiContext.getTenantId(), mzSegmentId);
		} catch (MailchimpMappingReservedException mre) {
            // do nothing, return null segmentId
			logger.debug("Mapping reserved for segement, Mozu ID " + mzSegmentId);
			throw mre;
		}

		if(mcSegmentId == null){
			String indexKey=null;
			try{
				indexKey=idMappingDao.putMozuSegmentId(apiContext.getTenantId(), mzSegmentId, null);
			} catch (MailchimpDuplicateMappingException dme) {
				logger.debug("Mozu Segment already mapped :"+mzSegmentId);
				throw dme;
			}

			try{
				logger.info("Start creating mozu segment "+customerSegment.getName()+ " in mailchimp list "+setting.getMcListId());
				mailchimpSegmentId=mailChimpUtil.createMailchimpSegment(setting.getApiKey(), setting.getMcListId(), customerSegment.getName());
				logger.info("Completed creating mozu segment "+customerSegment.getName()+ " in mailchimp list "+setting.getMcListId());
			}catch(Exception e){
				idMappingDao.deleteSegmentEntry(apiContext.getTenantId(), mzSegmentId);
				throw e;
			}
			
			idMappingDao.updateEntry(apiContext.getTenantId(), indexKey, mailchimpSegmentId);
			
		}else{
			 logger.debug("Update Segment " + customerSegment.getName());
			 try{
				 logger.info("Start updating mozu segment "+customerSegment.getName()+ " in mailchimp list "+setting.getMcListId());
				 mailChimpUtil.updateMailchimpSegment(setting.getApiKey(), setting.getMcListId(), customerSegment.getName(),mcSegmentId);
				 logger.info("Completed updating mozu segment "+customerSegment.getName()+ " in mailchimp list "+setting.getMcListId());
			 }catch(Exception e){
				 if(e.getMessage().startsWith("com.ecwid.mailchimp.MailChimpException: API Error (211): A segment with an id of")){
					 logger.debug("Exception occured while trying to update segment "+customerSegment.getName() +" Trying to add it the segment to mailchimp." );
					 try{
							mailchimpSegmentId=mailChimpUtil.createMailchimpSegment(setting.getApiKey(), setting.getMcListId(), customerSegment.getName());
							logger.debug("Added Segment "+customerSegment.getName() +" to mailchimp." );
						}catch(Exception e1){
							idMappingDao.deleteSegmentEntry(apiContext.getTenantId(), mzSegmentId);
							throw e1;
						}
					    String mzIndexKey = idMappingDao.getMozuSegmentIndexKey(apiContext.getTenantId(), Integer.parseInt(mzSegmentId));
						idMappingDao.updateEntry(apiContext.getTenantId(), mzIndexKey, mailchimpSegmentId);
				     }
			 }
		}
		return mailchimpSegmentId;
		
	}
		
	public void addCustomersToMailchimpSegment(Integer tenantId,String mzSegmentId, String email) throws Exception{
		MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
		Integer mcSegmentId=idMappingDao.getMailChimpSegmentId(tenantId, mzSegmentId);
		if(mcSegmentId ==null){
			ApiContext apiContext=new MozuApiContext(tenantId);
			try{
				logger.info("Mozu Segment "+mzSegmentId+" doesn't exist in mailchimp. So add the segment");
				mcSegmentId=sendSegmentToMailchimp(apiContext, mzSegmentId);
			}catch(Exception e){
				logger.debug("Exception occured while sending mozu segment: "+mzSegmentId+" to mailchimp");
				throw e;
			}
		   
		}
		logger.info("Adding customer with email id "+email+ " to mailchimp segment "+mcSegmentId);
		 mailChimpUtil.addStaticSegmentMember(setting.getApiKey(), setting.getMcListId(), mcSegmentId, email);
		
	}
	
	public void removeCustomersFromMailchimpSegment(Integer tenantId,String mzSegmentId, String email) throws Exception{
		MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
		Integer mcSegmentId=idMappingDao.getMailChimpSegmentId(tenantId, mzSegmentId);
		if(mcSegmentId !=null){
			logger.info("Delete customer with email id "+email+ " from mailchimp segment "+mcSegmentId);
		     mailChimpUtil.deleteStaticSegementMember(setting.getApiKey(), setting.getMcListId(), mcSegmentId, email);
		     
		}
	}
}
