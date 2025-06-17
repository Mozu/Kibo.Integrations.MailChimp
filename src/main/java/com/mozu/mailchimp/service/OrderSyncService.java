package com.mozu.mailchimp.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecwid.mailchimp.method.v2_0.lists.Email;
import com.mozu.api.ApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerAttributeCollection;
import com.mozu.api.resources.commerce.customer.accounts.CustomerAttributeResource;
import com.mozu.base.handlers.EntityHandler;
import com.mozu.mailchimp.mailchimpextends.MemberInfoResult;
import com.mozu.mailchimp.model.Ecommerce360Map;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

import freemarker.template.utility.StringUtil;


@Service
public class OrderSyncService {
	private static final Logger logger = LoggerFactory.getLogger(OrderSyncService.class);
	@Autowired
	private MailchimpUtil mailChimpUtil;
	
	@Autowired
    IdMappingDao idMappingDao;
	
	@Autowired
    private MozuUtil mozuUtil;
	
	
	public OrderSyncService() {
		// TODO Auto-generated constructor stub
	}

	public void sendOrderToMailchimp(ApiContext apiContext,Order order) throws Exception{
		Email subscribedCustomer=null;
		String mailchimpEmailId=null;
		Integer tenantId = apiContext.getTenantId();
		MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
		Integer customerId = order.getCustomerAccountId();
		if(customerId!=null){
				CustomerAccount customerAccount = mozuUtil.getMozuCustomer(
						apiContext, customerId);
				if(!StringUtils.isEmpty(customerAccount.getEmailAddress())){
				if(customerAccount.getAttributes()==null){
					CustomerAttributeResource attrResource = new CustomerAttributeResource(
							apiContext);
					CustomerAttributeCollection collection = attrResource
							.getAccountAttributes(customerAccount.getId());
					customerAccount.setAttributes(collection.getItems());
				}
				MemberInfoResult memberInfoResult=mailChimpUtil.getMemberInfo(setting.getApiKey(),
						setting.getMcListId(), customerAccount.getEmailAddress());
				if (customerAccount.getAcceptsMarketing() != null
							&& customerAccount.getAcceptsMarketing()) {
						if(memberInfoResult.data.isEmpty() ||(!memberInfoResult.data.get(0).status.equals("subscribed") && !memberInfoResult.data.get(0).status.equals("unsubscribed"))){
							subscribedCustomer=mailChimpUtil.subscribeCustomers(customerAccount,
									setting.getApiKey(),
									setting.getMcListId());
							mailchimpEmailId=subscribedCustomer.euid;
						}else{
							mailchimpEmailId=memberInfoResult.data.get(0).id;
						}
						
						logger.debug("Start wrtiting orders to Mailchimp : Orders :"+order.getOrderNumber());
						
						Ecommerce360Map ecommerce360Map=getEcommerceData(apiContext,order);
						mailChimpUtil.createEcommOrder(setting.getApiKey(),apiContext, order,customerAccount.getEmailAddress(),ecommerce360Map,mailchimpEmailId);
						
					} 
				}	else{
					logger.debug("Email address of customer "+customerId+" of tenant "+ tenantId +" is empty or null. So skipping the record");
				}
		}		
	}
	/* Get the EcommerceMap from MZDB*/
	public Ecommerce360Map getEcommerceData(ApiContext apiContext,Order order){
		EntityHandler<Ecommerce360Map> entityHandler = new EntityHandler<Ecommerce360Map>(Ecommerce360Map.class);
		Ecommerce360Map ecommerce360Map=null;
		try {
			ecommerce360Map= entityHandler.getEntity(apiContext.getTenantId(),IdMappingDao.ECOMMERCE_MAPPING_LIST, order.getOrderNumber().toString());
		} catch (Exception e) {
			logger.debug("Exception occured while getting the Entity List map "+IdMappingDao.ECOMMERCE_MAPPING_LIST+ e );
		}
		return ecommerce360Map;
	}
}
