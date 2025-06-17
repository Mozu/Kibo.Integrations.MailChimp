package com.mozu.mailchimp.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ecwid.mailchimp.MailChimpException;
import com.ecwid.mailchimp.MailChimpObject;
import com.ecwid.mailchimp.method.v2_0.ecomm.OrderItemInfo;
import com.ecwid.mailchimp.method.v2_0.lists.BatchUnsubscribeMethod;
import com.ecwid.mailchimp.method.v2_0.lists.Email;
import com.ecwid.mailchimp.method.v2_0.lists.ListMethod;
import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult;
import com.ecwid.mailchimp.method.v2_0.lists.SubscribeMethod;
import com.ecwid.mailchimp.method.v2_0.lists.UpdateMemberMethod;
import com.mozu.api.ApiContext;
import com.mozu.api.ApiException;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.commerceruntime.orders.OrderItem;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerAttribute;
import com.mozu.api.contracts.productruntime.Category;
import com.mozu.mailchimp.exception.ErrorCode;
import com.mozu.mailchimp.exception.MailchimpDuplicateMappingException;
import com.mozu.mailchimp.exception.MailchimpException;
import com.mozu.mailchimp.mailchimpextends.CampaignSendNowMethod;
import com.mozu.mailchimp.mailchimpextends.HasListIdMethod;
import com.mozu.mailchimp.mailchimpextends.ListMemberInfoMethod;
import com.mozu.mailchimp.mailchimpextends.ListMemberInfoResult;
import com.mozu.mailchimp.mailchimpextends.ListMergeVarAddMethod;
import com.mozu.mailchimp.mailchimpextends.ListMergeVarsMethod;
import com.mozu.mailchimp.mailchimpextends.ListMergeVarsResult;
import com.mozu.mailchimp.mailchimpextends.ListMergeVarsResult.MergeTags;
import com.mozu.mailchimp.mailchimpextends.ListMethodResult.Data;
import com.mozu.mailchimp.mailchimpextends.ListSegmentAddMethod;
import com.mozu.mailchimp.mailchimpextends.ListSegmentResult;
import com.mozu.mailchimp.mailchimpextends.ListSegmentUpdateMethod;
import com.mozu.mailchimp.mailchimpextends.ListSingleMemberInfoMethod;
import com.mozu.mailchimp.mailchimpextends.MemberInfo;
import com.mozu.mailchimp.mailchimpextends.MemberInfoResult;
import com.mozu.mailchimp.mailchimpextends.MemberStatus;
import com.mozu.mailchimp.mailchimpextends.MergeVarInfo;
import com.mozu.mailchimp.mailchimpextends.OrderAddMethod;
import com.mozu.mailchimp.mailchimpextends.OrderInfo;
import com.mozu.mailchimp.mailchimpextends.SegmentInfo;
import com.mozu.mailchimp.mailchimpextends.StaticSegmentDeleteMethod;
import com.mozu.mailchimp.mailchimpextends.StaticSegmentMemberAddMethod;
import com.mozu.mailchimp.mailchimpextends.StaticSegmentMemberDeleteMethod;
import com.mozu.mailchimp.mailchimpextends.WebhookCreateMethod;
import com.mozu.mailchimp.mailchimpextends.WebhookListMethod;
import com.mozu.mailchimp.model.DiscountCampaignModel;
import com.mozu.mailchimp.model.Ecommerce360Map;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.service.IdMappingDaoImpl;
import com.mozu.mailchimp.service.MozuMailChimpClient;
import com.mozu.mailchimp.service.MozuMcClientSetupService;

/**
 * Mailchimp Utility class
 * 
 * @author Amit
 * 
 */
@Component
public class MailchimpUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(MailchimpUtil.class);

	@Autowired
	private MozuMcClientSetupService mozuMcClientSetupService;
	
	@Autowired
	private MozuMailChimpClient mmClient;
	
	@Autowired
    private MozuUtil mozuUtil;
	
	@Autowired
    IdMappingDaoImpl idMappingDao;
	
	@Value("${ThrottleRetries}")
    protected int throttleRetries;
	
	SecureRandom sc= new SecureRandom();

	/**
	 * This method gets the customer and subscribes them to mailchimp
	 * list. 
	 * @param customerList
	 * @param mcApiKey
	 * @param listId
	 * @throws IOException
	 * @throws MailChimpException
	 * @throws MailchimpException
	 */
	public Email subscribeCustomers(
			CustomerAccount customer, String mcApiKey, String listId)
			throws IOException, Exception {
		logger.info("Subscribe customer with email address "+customer.getEmailAddress() +" to mailchimp list "+listId);
		Email subscribedCustomer=null;
		try {
				if (customer.getEmailAddress() != null
						&& (customer.getAcceptsMarketing() != null)) {
					sendCustomerAttributesToMailchimp(mcApiKey, listId, customer.getAttributes());
					SubscribeMethod subscribeMethod = new SubscribeMethod();
					subscribeMethod.apikey = mcApiKey;
					subscribeMethod.id = listId;
					MailChimpObject mailChimpObj = new MailChimpObject();
					mailChimpObj.put("FNAME", customer.getFirstName());
					mailChimpObj.put("LNAME", customer.getLastName());
					List<CustomerAttribute> attrList = customer.getAttributes();
					
					for (CustomerAttribute attr : attrList) {
						String tagName = attr.getAttributeDefinitionId().toString();
						String tagValue = attr.getValues().get(0).toString();
						mailChimpObj.put(tagName, tagValue);
					}

					subscribeMethod.merge_vars = mailChimpObj;
					subscribeMethod.email = new Email();
					subscribeMethod.email.email = customer.getEmailAddress();
					subscribeMethod.update_existing = Boolean.valueOf(true);
					subscribeMethod.double_optin = Boolean.valueOf(false);
					subscribedCustomer=mmClient.execute(subscribeMethod);
				}

		} catch (UnknownHostException u) {
			logger.error("Exception while subscribing customer "+customer.getEmailAddress()+ " to mailchimp list " +listId+" " + u.getMessage());
			MailchimpException ex = new MailchimpException();
			ex.setErrorCode(ErrorCode.EX_CD_100);
			ex.setErrorMessage(ErrorCode.EX_MSG_100);
			throw ex;
		} catch (Exception e) {
			logger.error("Exception while subscribing customer "+customer.getEmailAddress()+ " to mailchimp list " +listId+" " + e.getMessage());
			throw e;
		}
		return subscribedCustomer;
	}
	
	public void sendCustomerAttributesToMailchimp( String apikey, String listId,List<CustomerAttribute> customerAtrributes){
		ListMergeVarsResult listMergeVarsResult=getMergeTag(apikey, listId);
		for (MergeTags data : listMergeVarsResult.data) {
			if(data.id.equals(listId)){
				List<MergeVarInfo> mcMergeVarInfoList=data.merge_vars;
				if (!CollectionUtils.isEmpty(customerAtrributes)) {
					for (CustomerAttribute customerAtrribute : customerAtrributes) {
						Boolean mergeVarPresent=false;
						if(!CollectionUtils.isEmpty(mcMergeVarInfoList) ){
							for (MergeVarInfo mergeVarInfo : mcMergeVarInfoList) {
								if(mergeVarInfo.tag.equals(customerAtrribute.getAttributeDefinitionId().toString())){
									mergeVarPresent=true;
									break;
								}
							}
							if(mergeVarPresent==false)
							  createMergeTag(apikey, listId, customerAtrribute);
						}
					}
			   }
				
			}
		}
		
		
	}

	
    /**
     * This method gets all the list associated with a mailchimp account.
     * 
     * @param apiKey
     * @return
     * @throws IOException
     * @throws MailChimpException
     */
    public ListMethodResult getAllMcList(String apiKey)
            throws IOException, MailChimpException {
        ListMethodResult listMethodResult = null;
        ListMethod listMethod = new ListMethod();
        listMethod.apikey = apiKey;
        listMethodResult = mmClient.execute(listMethod);

        return listMethodResult;
    }

    /**
     * This method gets all the list associated with a mailchimp account.
     * 
     * @param apiKey
     * @return
     * @throws IOException
     * @throws MailChimpException
     */
    public ListMethodResult getAllMcList(String apiKey, Integer start, Integer pageLimit)
            throws IOException, MailChimpException {
        ListMethodResult listMethodResult = null;
        ListMethod listMethod = new ListMethod();
        listMethod.start = start;
        listMethod.limit = pageLimit;
        listMethod.apikey = apiKey;
        listMethodResult = mmClient.execute(listMethod);

        return listMethodResult;
    }

	/**
	 * This method validates mailchimp Api key.
	 * 
	 * @param apiKeyMailchimp
	 * @return
	 */
	public boolean validateMailchimpKey(String apiKeyMailchimp) {
		boolean isSuccessFul = false;
		try {
			getAllMcList(apiKeyMailchimp);
			isSuccessFul = true;
		} catch (Exception e) {
			logger.error("Invalid mailchimp API key: " + apiKeyMailchimp+" Error : "+e);
		}
		return isSuccessFul;
	}
	
	/**
	 * This method validates mailchimp List.
	 * 
	 * @param apiKeyMailchimp
	 * @return
	 */
	public boolean isMailchimpListValid(MailchimpSyncModel mailchimpSetting) {
		boolean isSuccessFul = false;
		try {
			 
			Map<String, String> returnMap = mozuMcClientSetupService
					.getIdList(mailchimpSetting);
			if(!returnMap.isEmpty()){
				Set<String> listIds=returnMap.keySet();
				if(mailchimpSetting.getMcListId() !=null && listIds.contains(mailchimpSetting.getMcListId())){
					isSuccessFul=true;
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while validating the list "+e);
		}
		return isSuccessFul;
	}

	/**
	 * This method un-subscribes subscriber from mailchimp account. This method
	 * is used by live sync.
	 * 
	 * @param custAccounts
	 * @param apiKeyMailchimp
	 * @param listId
	 * @throws IOException
	 * @throws MailChimpException
	 */
	public void deleteCustomers(List<CustomerAccount> custAccounts,
			String apiKeyMailchimp, String listId) throws IOException,
			MailChimpException {

		List<Email> deleteCustomerList = new ArrayList<Email>();
		for (CustomerAccount customer : custAccounts)
			if (customer.getEmailAddress() != null) {
				Email emailDelete = new Email();
				emailDelete.email = customer.getEmailAddress();
				deleteCustomerList.add(emailDelete);
			}
		BatchUnsubscribeMethod batchUnsubscribeMethod = new BatchUnsubscribeMethod();
		batchUnsubscribeMethod.apikey = apiKeyMailchimp;
		batchUnsubscribeMethod.id = listId;
		batchUnsubscribeMethod.delete_member = true;
		batchUnsubscribeMethod.batch = deleteCustomerList;
		mmClient.execute(batchUnsubscribeMethod);
	}

	/**
	 * This method un-subscribes customer from mailchimp in bulk.
	 * 
	 * @param custAccounts
	 * @param apiKeyMailchimp
	 * @param listId
	 * @throws IOException
	 * @throws MailChimpException
	 * @throws MailchimpException
	 */
	public void unsubscribeCustomers(CustomerAccount customer,
			String apiKeyMailchimp, String listId) throws IOException,
			MailChimpException, MailchimpException {

		try {
			List<Email> deleteCustomerList = new ArrayList<Email>();
				if (customer.getEmailAddress() != null) {
					Email emailDelete = new Email();
					emailDelete.email = customer.getEmailAddress();
					deleteCustomerList.add(emailDelete);
				}
			BatchUnsubscribeMethod batchUnsubscribeMethod = new BatchUnsubscribeMethod();
			batchUnsubscribeMethod.apikey = apiKeyMailchimp;
			batchUnsubscribeMethod.id = listId;
			batchUnsubscribeMethod.batch = deleteCustomerList;
			mmClient.execute(batchUnsubscribeMethod);
		} catch (UnknownHostException u) {
			logger.error("Exception while unsubscribing customers in bulk in unsubscribeCustomers()"+" Error : "+u);
			MailchimpException ex = new MailchimpException();
			ex.setErrorCode(ErrorCode.EX_CD_100);
			ex.setErrorMessage(ErrorCode.EX_MSG_100);
			throw ex;
		} catch (Exception e) {
			logger.error("Exception while unsubscribing customers in bulk in unsubscribeCustomers()"+" Error : "+e);
			MailchimpException ex = new MailchimpException();
			ex.setErrorCode(ErrorCode.EX_CD_300);
			ex.setErrorMessage(ErrorCode.EX_MSG_300);
			throw ex;
		}
	}
	
	

	/**
	 * This method gets only active the campaign list from mailchimp account.
	 * 
	 * @param apikey
	 * @return
	 * @throws IOException
	 * @throws MailChimpException
	 */
	public List<DiscountCampaignModel> getCampaignList(String apikey)
			throws IOException, MailChimpException {
		List<DiscountCampaignModel> campaignList = new ArrayList<DiscountCampaignModel>();
		com.mozu.mailchimp.mailchimpextends.ListMethod campaignMethod = new com.mozu.mailchimp.mailchimpextends.ListMethod();
		campaignMethod.apikey = apikey;
		com.mozu.mailchimp.mailchimpextends.ListMethodResult result = mmClient.execute(campaignMethod);

		for (Data data : result.data) {
			if (data.list_id != null
					&& (Constants.CAMPAIGN_STATUS.equals(data.status) || data.send_time == null)) {

				DiscountCampaignModel mapModel = new DiscountCampaignModel();
				mapModel.setKeyStr(data.id);
				mapModel.setValStr(data.title);
				campaignList.add(mapModel);
			}
		}
		return campaignList;
	}

	
	/**
	 * This method gets all the campaign list from mailchimp account.
	 * 
	 * @param apikey
	 * @return
	 * @throws IOException
	 * @throws MailChimpException
	 */
	public List<DiscountCampaignModel> getAllCampignMap(String apikey)
			throws IOException, MailChimpException {
		List<DiscountCampaignModel> campaignList = new ArrayList<DiscountCampaignModel>();
		com.mozu.mailchimp.mailchimpextends.ListMethod campaignMethod = new com.mozu.mailchimp.mailchimpextends.ListMethod();
		campaignMethod.apikey = apikey;
		com.mozu.mailchimp.mailchimpextends.ListMethodResult result = mmClient
				.execute(campaignMethod);

		for (Data data : result.data) {
			if (data.list_id != null) {

				DiscountCampaignModel mapModel = new DiscountCampaignModel();
				mapModel.setKeyStr(data.id);
				mapModel.setValStr(data.title);
				campaignList.add(mapModel);
			}
		}
		return campaignList;
	}
	
	public Map<String, String> getMailchimpEventMap(String body)
			throws UnsupportedEncodingException {
		String decodedStr = java.net.URLDecoder.decode(body, "UTF-8");
		String[] decodeArr = decodedStr.split("&");
		Map<String, String> requestMap = new HashMap<String, String>();
		for (String str : decodeArr) {
			requestMap.put(str.split("=")[0], str.split("=")[1]);

		}
		return requestMap;
	}
	
	/**
	 * This method creates merge tag in mailchimp list.
	 * 
	 * @param apikey
	 * @param listId
	 * @param mergeVarList
	 */
	public void createMergeTag(String apikey, String listId,
			CustomerAttribute customerAttribute) {
		if (customerAttribute != null) {
			logger.info("Create customer attribute "+customerAttribute.getAttributeDefinitionId().toString() +" as merge tag in mailchimp list "+listId);
			ListMergeVarAddMethod mergeVar = new ListMergeVarAddMethod();
			String mergeTag = customerAttribute.getFullyQualifiedName().split("~")[1];
			mergeVar.apikey = apikey;
			mergeVar.id = listId;
			mergeVar.tag = customerAttribute.getAttributeDefinitionId().toString();
			mergeVar.name = mergeTag;
			MergeVarInfo options = new MergeVarInfo();
			options.req = false;
			options.public_ = true;
			options.show = true;
			mergeVar.options = options;
			try {
				mmClient.execute(mergeVar);
			} catch (Exception e) {
					logger.error("Error saving merge vars, apiKey: " + apikey
							+ " , listId: " + listId+" Error : "+e);
				}
			}
	}
	
	/**
	 * This method gets the merge tag in mailchimp list.
	 * 
	 * @param apikey
	 * @param listId
	 * @param mergeVarList
	 * @throws MailChimpException 
	 * @throws IOException 
	 */
	public ListMergeVarsResult getMergeTag(String apikey, String listId)  {
		List<String> mcList=new ArrayList<String>();
		mcList.add(listId);
		ListMergeVarsMethod mergeVar = new ListMergeVarsMethod();
		mergeVar.apikey = apikey;
		mergeVar.id= mcList;
		ListMergeVarsResult result=null;
		try {
			 result=mmClient.execute(mergeVar);
		} catch (Exception e) {
	           logger.error("Error getting merge vars, apiKey: " + apikey
							+ " , listId: " + listId+" Error : "+e);
		}
		return result;
	}

	/**
	 * This method creates ecommerce order against the customer in mailchimp.
	 *  
	 * @param apikey
	 * @param order
	 */
	public void createEcommOrder(String apikey,ApiContext apiContext, Order order,String emailAddress,Ecommerce360Map ecommerce360Map,String mailchimpEmailId) throws Exception{
		OrderAddMethod orderAdd = new OrderAddMethod();
		orderAdd.apikey = apikey;
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.email = emailAddress;
		orderInfo.id = order.getOrderNumber().toString();
		orderInfo.order_date = order.getSubmittedDate().toDate();
		orderInfo.store_id = order.getTenantId().toString(); 
		orderInfo.shipping = order.getShippingTotal();
		orderInfo.store_name = order.getSiteId().toString();
		orderInfo.tax = order.getItemTaxTotal();
		orderInfo.total = order.getTotal();
		//If EcommerceMap has data, get the campaign id and email id and send it to mailchimp
		if(ecommerce360Map!=null && StringUtils.isNotBlank(ecommerce360Map.getCampaignId())&&StringUtils.isNotBlank(mailchimpEmailId)){
			orderInfo.campaign_id=ecommerce360Map.getCampaignId();
			orderInfo.email_id=mailchimpEmailId;
		}
		
		List<OrderItemInfo> itemList = new ArrayList<OrderItemInfo>();
		for (OrderItem item : order.getItems()) {
			OrderItemInfo itemInfo = new OrderItemInfo();
			if(item.getProduct().getCategories().size() > 0 ){
				Integer categoryId =  item.getProduct().getCategories().get(0).getId(); 
				Category category = mozuUtil.getCategory(apiContext, categoryId);
				if(category!=null){
					itemInfo.category_id = categoryId; 
					itemInfo.category_name = category.getContent().getName();
				}else{
					itemInfo.category_id = -1;
					itemInfo.category_name = "unknown";
				}
			}else{
				itemInfo.category_id = -1;
				itemInfo.category_name = "unknown";
			}
			itemInfo.cost = item.getExtendedTotal();
			itemInfo.product_id = getMailChimpProductId(item.getProduct().getProductCode(), apiContext.getTenantId());
			itemInfo.product_name = item.getProduct().getName();
			itemInfo.qty = Double.parseDouble(item.getQuantity().toString());
			itemInfo.sku = item.getProduct().getProductCode();
			itemList.add(itemInfo);

		}
		orderInfo.items = itemList;

		orderAdd.order = orderInfo;
		try {
			mmClient.execute(orderAdd);
			logger.debug("Completed wrtiting orders to Mailchimp : Orders :"+order.getOrderNumber());
		} catch (Exception e) {
			 if (((MailChimpException)e).code== 330) { 
				 logger.error("Exception while adding ecommerce order to mailchimp, apiKey: "
					+ apikey + " , tenant id: " + order.getTenantId()+" Error : "+e );
			 }else{
				 logger.error("Exception while adding ecommerce order to mailchimp, apiKey: "
							+ apikey + " , tenant id: " + order.getTenantId()+" Error : "+e );
				 throw e;
			 }
		}

	}
	
	public Integer createMailchimpSegment(String apikey,String listId, String segmentName) throws Exception{
		ListSegmentAddMethod segmentAdd=new ListSegmentAddMethod();
		segmentAdd.apikey= apikey;
		segmentAdd.id= listId;
		SegmentInfo segmentInfo=new SegmentInfo();
		segmentInfo.type="static";
		segmentInfo.name= segmentName;
		segmentAdd.opts=segmentInfo;
		ListSegmentResult segmentResult=null; 
		Integer mailchimpSegmentId=null;
		try {
			segmentResult =  mmClient.execute(segmentAdd);
			if(segmentResult !=null)
				mailchimpSegmentId=segmentResult.id;
			
		} catch (IOException | MailChimpException e) {
			logger.error("Exception while adding segment "+segmentName+" to mailchimp, apiKey: "
					+ apikey + " , and list id: "+listId+" Error : "+e  );
			throw new RuntimeException(e);
		}
		return mailchimpSegmentId;
	}
	
	public void updateMailchimpSegment(String apikey,String listId, String segmentName, Integer mcSegmentId) throws Exception{
		ListSegmentUpdateMethod segmentUpdate=new ListSegmentUpdateMethod();
		segmentUpdate.apikey= apikey;
		segmentUpdate.id= listId;
		segmentUpdate.seg_id=mcSegmentId;
		SegmentInfo segmentInfo=new SegmentInfo();
		segmentInfo.name= segmentName;
		segmentUpdate.opts=segmentInfo;
		try {
			 mmClient.execute(segmentUpdate);
			
		} catch (IOException | MailChimpException e) {
			logger.error("Exception while updating segment "+segmentName+" to mailchimp, apiKey: "
					+ apikey + " , and list id: "+listId+" Error : "+e );
			throw new RuntimeException(e);
		}
	}
	
	public void addStaticSegmentMember(String apikey,String listId, Integer mcSegmentId, String mzEmail) throws Exception{
		StaticSegmentMemberAddMethod segmentMemberAdd=new StaticSegmentMemberAddMethod();
		segmentMemberAdd.apikey= apikey;
		segmentMemberAdd.id= listId;
		segmentMemberAdd.seg_id=mcSegmentId;
		Email email = new Email();
		email.email = mzEmail;
		List<Email> emailList = new ArrayList<Email>();
		emailList.add(email);
		segmentMemberAdd.batch=emailList;
		
		try {
			 mmClient.execute(segmentMemberAdd);
			
		} catch (IOException | MailChimpException e) {
			logger.error("Exception while adding members to static  segment "+mcSegmentId+" to mailchimp, apiKey: "
					+ apikey + " , and list id: "+listId+" Error : "+e );
			throw new RuntimeException(e);
		}
	}

	public void deleteStaticSegementMember(String apikey,String listId, Integer mcSegmentId, String mzEmail) throws Exception{
		StaticSegmentMemberDeleteMethod segmentMemberDel=new StaticSegmentMemberDeleteMethod();
		segmentMemberDel.apikey= apikey;
		segmentMemberDel.id= listId;
		segmentMemberDel.seg_id=mcSegmentId;
		Email email = new Email();
		email.email = mzEmail;
		List<Email> emailList = new ArrayList<Email>();
		emailList.add(email);
		segmentMemberDel.batch=emailList;
		
		try {
			 mmClient.execute(segmentMemberDel);
			
		} catch (IOException | MailChimpException e) {
			logger.error("Exception while deleting members from static  segment "+mcSegmentId+" from mailchimp, apiKey: "
					+ apikey + " , and list id: "+listId+" Error : "+e );
			throw new RuntimeException(e);
		}
	}
	
	public void deleteStaticSegement(String apikey,String listId, Integer mcSegmentId) throws Exception{
		StaticSegmentDeleteMethod segmentDel=new StaticSegmentDeleteMethod();
		segmentDel.apikey= apikey;
		segmentDel.id= listId;
		segmentDel.seg_id=mcSegmentId;
		try {
			 mmClient.execute(segmentDel);
			
		} catch (IOException | MailChimpException e) {
			logger.error("Exception while deleting static  segment "+mcSegmentId+" from mailchimp, apiKey: "
					+ apikey + " , and list id: "+listId+" Error : "+e );
			throw new RuntimeException(e);
		}
	}
	
	public Integer getMailChimpProductId(String mzProductCode,Integer tenantId) throws Exception{
		Integer mailChimpProdId=null;
  		mailChimpProdId=idMappingDao.getMailChimpProductId(tenantId, mzProductCode);
			
		if(mailChimpProdId!=null){
			return mailChimpProdId;
		}else{
			logger.debug("Get a random number");
			   mailChimpProdId=getRandomNumber(tenantId,mzProductCode);
	       try {
	         logger.debug("Reserve mapping for mozu id " + mzProductCode);
	     	idMappingDao.putMozuProductCode(tenantId, mzProductCode, mailChimpProdId);
		   } catch (MailchimpDuplicateMappingException dme) {
			   logger.error("MailchimpDuplicateMappingException "+dme);
			   mailChimpProdId=idMappingDao.getMailChimpProductId(tenantId, mzProductCode);
	       }

			
	     }
	  return mailChimpProdId;
		
	}

	/**
	 * This method is use for creating webhook in mailchimp account.
	 * 
	 * @param listId
	 * @param url
	 * @param apiKey
	 */
	public void createWebhook(List<String> listId, String url,
			String apiKey) {
		WebhookCreateMethod webhook = new WebhookCreateMethod();
		webhook.apikey = apiKey;
		webhook.url = url;
		for (String id : listId) {
			webhook.id = id;
			if (listWebhook(id, apiKey)) {
				try {
					mmClient.execute(webhook);
				} catch (IOException | MailChimpException e) {
					logger.error("Exception while creating webhook in mailchimp, listId: "
							+ listId + " , apiKey: " + apiKey + " , URL "+ url+" Error : "+e);
				}
			}

		}

	}

	/**
	 * This method lists webhook in mailchimp account.
	 * 
	 * @param listId
	 * @param apiKey
	 * @return
	 */
	public boolean listWebhook(String listId, String apiKey) {

		try {
			WebhookListMethod listWebhook = new WebhookListMethod();
			listWebhook.id = listId;
			listWebhook.apikey = apiKey;
			Object returnObj = mmClient.execute(listWebhook);
			if ("[]".equals(returnObj.toString())) {
				return true;
			}
		} catch (IOException | MailChimpException e) {
			logger.error("Exception while listing webhook in mailchimp, listId: "
					+ listId + " , apiKey: " + apiKey+" Error : "+e);
		}
		return false;
	}

	/**
	 * This method gets the member info based on email address.
	 * 
	 * @param apikey
	 * @param listId
	 * @param emailId
	 * @return
	 */
	public MemberInfoResult getMemberInfo(String apikey, String listId,
			String emailId) {
		MemberInfoResult memberInfoResult = null;
		ListSingleMemberInfoMethod member = new ListSingleMemberInfoMethod();
		member.apikey = apikey;
		member.id = listId;
		Email email = new Email();
		email.email = emailId;
		List<Email> listEmail = new ArrayList<Email>();
		listEmail.add(email);
		member.emails = listEmail;

		try {
			memberInfoResult = mmClient.execute(member);
		} catch (IOException | MailChimpException e) {
			logger.error("Exception while getting member info from, listId: "
					+ listId + ", apiKey: " + apikey + ", emailId: " + emailId+" Error : "+e);
		}
		return memberInfoResult;
	}

	/**
	 * This method get the subscribes members from mailchimp.
	 * 
	 * @param apikey
	 * @param listId
	 * @return
	 */
	public List<String> getSubscibedCustomer(String apikey, String listId) {
		ListMemberInfoMethod listMember = new ListMemberInfoMethod();
		listMember.apikey = apikey;
		listMember.id = listId;
		ListMemberInfoResult memberInfoResult = null;
		List<String> memberList = new ArrayList<String>();
		try {
			memberInfoResult = mmClient.execute(listMember);
			for (MemberInfo member : memberInfoResult.data) {
				memberList.add(member.email);
			}

		} catch (IOException | MailChimpException e) {
			logger.error("Exception while getting subscribed customer from mailchimp, listId: "
					+ listId + ", apiKey: " + apikey + ", listId: " + listId+" Error : "+e);
		}
		return memberList;
	}
	
	/**
	 * This method get the all customers from mailchimp.
	 * 
	 * @param apikey
	 * @param listId
	 * @return
	 */
	public List<MemberInfo> getAllCustomers(String apikey, String listId) {
		ListMemberInfoMethod listMember = new ListMemberInfoMethod();
		listMember.apikey = apikey;
		listMember.id = listId;
		String[] status=new String[]{MemberStatus.subscribed.toString(),MemberStatus.unsubscribed.toString(),MemberStatus.cleaned.toString()};
		ListMemberInfoResult memberInfoResult = null;
		List<MemberInfo> memberList = new ArrayList<MemberInfo>();
		try {
			for (String s : status) {
				listMember.status=s;
				memberInfoResult = mmClient.execute(listMember);
				for (MemberInfo member : memberInfoResult.data) {
					memberList.add(member);
				}
			}
			

		} catch (IOException | MailChimpException e) {
			logger.error("Exception while getting customers from mailchimp, listId: "
					+ listId + ", apiKey: " + apikey + ", listId: " + listId+" Error : "+e);
		}
		return memberList;
	}

	
	/**
	 * This method create merge tags campaign code and discount code for the
	 * list associated with campaign.
	 * 
	 * @param disocuntCode
	 * @param disocuntType
	 * @param campaignCode
	 * @param tenantId
	 */

	public void createCustomerMergeTag(String campaignCode,
			String tenantId, String apikey) {

		try {

			com.mozu.mailchimp.mailchimpextends.ListMethod campaignMethod = new com.mozu.mailchimp.mailchimpextends.ListMethod();
			campaignMethod.apikey = apikey;
			com.mozu.mailchimp.mailchimpextends.ListMethodResult result = mmClient
					.execute(campaignMethod);

			for (Data data : result.data) {
				if (data.list_id != null) {
					List<String> mergeList = new ArrayList<String>();
					mergeList.add(Constants.DISCOUNT_TYPE);
					mergeList.add(Constants.DISCOUNT_NAME);
					mergeList.add(Constants.DISCOUNT_VALUE);

					for (String mergeTag : mergeList) {
						ListMergeVarAddMethod mergeVar = new ListMergeVarAddMethod();
						mergeVar.apikey = apikey;
						mergeVar.id = data.list_id;
						mergeVar.tag = mergeTag;
						mergeVar.name = mergeTag;
						MergeVarInfo options = new MergeVarInfo();
						options.req = false;
						options.public_ = true;
						options.show = true;
						mergeVar.options = options;
						mmClient.execute(mergeVar);
					}

				}
			}
		} catch (Exception e) {
			logger.error("Exception while creating merge tag for tenantId "
					+ tenantId + " and campaign ID " + campaignCode+" Error : "+e);
		}

	}

	public boolean updateCustomerMergeTag(String tenantId,
			String discountName, String discountType, String discountValue,
			String apiKey) {
		try {

			com.mozu.mailchimp.mailchimpextends.ListMethod campaignMethod = new com.mozu.mailchimp.mailchimpextends.ListMethod();
			campaignMethod.apikey = apiKey;
			com.mozu.mailchimp.mailchimpextends.ListMethodResult result = mmClient.execute(campaignMethod);

			for (Data data : result.data) {
				if (data.list_id != null) {
					// get all subscriber of list
					List<String> emailList = getSubscibedCustomer(apiKey,
							data.list_id);
					for (String email : emailList) {
						UpdateMemberMethod updateMemberMethod = new UpdateMemberMethod();
						updateMemberMethod.apikey = apiKey;
						updateMemberMethod.id = data.list_id;
						Email emailObj = new Email();
						emailObj.email = email;
						updateMemberMethod.email = emailObj;
						MailChimpObject mailChimpObj = new MailChimpObject();
						mailChimpObj.put(Constants.DISCOUNT_TYPE, discountType);
						mailChimpObj.put(Constants.DISCOUNT_NAME, discountName);
						mailChimpObj.put(Constants.DISCOUNT_VALUE,
								discountValue);
						updateMemberMethod.merge_vars = mailChimpObj;
						mmClient.execute(updateMemberMethod);
						logger.debug("Updated tenantId ID " + tenantId
								+ " and discount  " + discountName);

					}
				}
			}
			return true;

		} catch (Exception e) {
			logger.error("Exception while updating merge tag for tenantId "
					+ tenantId + " and discount  " + discountName+" Error : "+e);
			return false;
		}
	}
	
	public boolean sendCampaign(String apiKey, String campaignCode,
			String tenantId) {

		try {
			CampaignSendNowMethod campaignSendNowMethod = new CampaignSendNowMethod();
			campaignSendNowMethod.apikey = apiKey;
			campaignSendNowMethod.cid = campaignCode;
			mmClient.execute(campaignSendNowMethod);
			logger.debug("Sent campign with code " + campaignCode);
			return true;
		} catch (Exception e) {
			logger.error("Exception while sending camapign for tenant ID"
					+ tenantId + " and campaign ID " + campaignCode+" Error : "+e);
			return false;
		}
	}

	public void resetMergeTag(String apiKey, String campaignCode,
			String tenantId) {

		try {
			com.mozu.mailchimp.mailchimpextends.ListMethod campaignMethod = new com.mozu.mailchimp.mailchimpextends.ListMethod();
			campaignMethod.apikey = apiKey;
			com.mozu.mailchimp.mailchimpextends.ListMethodResult result = mmClient.execute(campaignMethod);

			for (Data data : result.data) {
				if (data.list_id != null) {
					// get all subscriber of list
					List<String> emailList = getSubscibedCustomer(apiKey,
							data.list_id);
					for (String email : emailList) {
						UpdateMemberMethod updateMemberMethod = new UpdateMemberMethod();
						updateMemberMethod.apikey = apiKey;
						updateMemberMethod.id = data.list_id;
						Email emailObj = new Email();
						emailObj.email = email;
						updateMemberMethod.email = emailObj;
						MailChimpObject mailChimpObj = new MailChimpObject();
						mailChimpObj.put(Constants.DISCOUNT_TYPE, "");
						mailChimpObj.put(Constants.DISCOUNT_NAME, "");
						mailChimpObj.put(Constants.DISCOUNT_VALUE, "");
						updateMemberMethod.merge_vars = mailChimpObj;
						mmClient.execute(updateMemberMethod);
						logger.debug("resetMergeTag campign code "
								+ campaignCode);

					}
				}
			}

		} catch (Exception e) {
			logger.error("Exception while updating merge tag for tenantId "
					+ tenantId + " and campaign ID " + campaignCode+" Error : "+e);
		}
	}
	
	
	protected Integer getRandomNumber(Integer tenantId, String mzProductCode) throws Exception {
		
		Integer randomNumber=null;
		int counter=0;
		while(counter < throttleRetries){
			randomNumber=sc.nextInt(Integer.MAX_VALUE);
			String mozuProductCode=idMappingDao.getMozuProductCode(tenantId, randomNumber);
			if (mozuProductCode==null) {
				break;
			}else{
				StringBuilder errMsg = new StringBuilder("Cannot add entry for Mozu id")
   				.append(mzProductCode)
   				.append(" Mailchimp Id ")
   				.append(randomNumber)
   				.append(". The Mailchimp Id is already mapped.");
				logger.error(errMsg.toString());
				counter++;
		   }
		  
		}
		return randomNumber;
	}
}