package com.mozu.mailchimp.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mozu.api.contracts.customer.CustomerAttributeCollection;
import com.mozu.api.contracts.customer.CustomerSegment;
import com.mozu.api.ApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.resources.commerce.customer.accounts.CustomerAttributeResource;
import com.mozu.mailchimp.exception.MozuException;
import com.mozu.mailchimp.mailchimpextends.MemberInfoResult;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;


@Service
public class CustomerSyncService {
	private static final Logger logger = LoggerFactory.getLogger(CustomerSyncService.class);
	@Autowired
	private MailchimpUtil mailChimpUtil;
	
	@Autowired
    IdMappingDao idMappingDao;
	
	@Autowired
    private MozuUtil mozuUtil;
	
	@Autowired
	private CustomerSegmentSyncService customerSegmentSyncService;
	       
	
	public CustomerSyncService() {
		// TODO Auto-generated constructor stub
	}

	public void sendCustomerToMailchimp(ApiContext apiContext,CustomerAccount customerAccount) throws Exception{
		Integer tenantId = apiContext.getTenantId();
		MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
		try {
				logger.info("Start processing customer "+customerAccount.getEmailAddress()+ " of tenant "+apiContext.getTenantId()+ " to mailchimp");
				if ( setting.getApiKey() != null && setting.getMcListId() != null) {
					MemberInfoResult memberInfoResult=mailChimpUtil.getMemberInfo(setting.getApiKey(),
							setting.getMcListId(), customerAccount.getEmailAddress());
					if (customerAccount.getAcceptsMarketing() != null
							&& customerAccount.getAcceptsMarketing()) {
						if(customerAccount.getAttributes()==null){
							CustomerAttributeResource attrResource = new CustomerAttributeResource(
									apiContext);
							CustomerAttributeCollection collection = attrResource
									.getAccountAttributes(customerAccount.getId());
							customerAccount.setAttributes(collection.getItems());
						}
						// Send only new customers or existing customers that are not in "unsubscribed" status to Mailchimp. Mailchimp doesn't allow to resubscribe an already unsubscribed customer through API
						if(memberInfoResult.data.isEmpty() ||
								(!memberInfoResult.data.isEmpty() && !memberInfoResult.data.get(0).status.equals("unsubscribed"))){
							logger.info("Subscribe customer "+customerAccount.getId() +" of tenant "+tenantId.toString()+" to mailchimp");
						    mailChimpUtil.subscribeCustomers(customerAccount,
									setting.getApiKey(),
									setting.getMcListId());
						    logger.debug((new StringBuilder())
									.append("Customer subscribed successfully. Customer ID: ")
									.append(customerAccount.getId())
									.append("of teanant")
									.append(tenantId)
									.toString());
						    for (CustomerSegment customerSegment : customerAccount.getSegments()) {
						    	customerSegmentSyncService.addCustomersToMailchimpSegment(apiContext.getTenantId(),customerSegment.getId().toString(),customerAccount.getEmailAddress());
							}
						}else{
							logger.info("Unable to subscribe customer "+customerAccount.getEmailAddress()+ " of tenant "+apiContext.getTenantId());
						}
					
					} else {
						//Unsubscribe a customer only if there are no customers with the same email address in Mozu that has opt-in enabled.
						if(!memberInfoResult.data.isEmpty() && memberInfoResult.data.get(0).status.equals("subscribed") && !existEmailAddressWithOptInEnabled(apiContext, customerAccount.getEmailAddress())){
							logger.info("Unsubscribe customer "+customerAccount.getId() +" of tenant "+tenantId+" from mailchimp");
						    mailChimpUtil.unsubscribeCustomers(customerAccount,
									setting.getApiKey(),
									setting.getMcListId());
						    for (CustomerSegment customerSegment : customerAccount.getSegments()) {
						        customerSegmentSyncService.removeCustomersFromMailchimpSegment(apiContext.getTenantId(), customerSegment.getId().toString(),customerAccount.getEmailAddress());
						    }
						}else{
							logger.info("Cannot unsubscribe customer "+customerAccount.getEmailAddress()+ " of tenant "+apiContext.getTenantId()+
									". Either opt-in is not enabled for the customer or other customers exist with the same emailid with opt-in enabled");
						}
						
					}
					
				}
	
			} catch (Exception e) {
				logger.error(
						"Exception while processing customer update, tenantID: "
								+ tenantId + " exception:"
								+ e);
				throw e;
				
			}
	
	}
	
	private Boolean existEmailAddressWithOptInEnabled(ApiContext apiContext,String emailAddress) throws MozuException{
		Boolean subscribed= false;
		List<CustomerAccount> customerAccountList = mozuUtil
				.getMozuCustomersByEmail(apiContext, emailAddress);
		for (CustomerAccount customerAccount : customerAccountList) {
			if(customerAccount.getAcceptsMarketing()!=null && customerAccount.getAcceptsMarketing()){
				subscribed = true;
				break;
			}
		}
		
		return subscribed;
		
	}
}
