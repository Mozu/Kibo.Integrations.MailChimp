package com.mozu.mailchimp.batch;

import java.util.List;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerSegment;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.service.CustomerSegmentSyncService;
import com.mozu.mailchimp.service.OrderSyncService;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

@Service("orderExportWriter")
@Scope("step")
public class MozuOrderWriter implements
		ItemWriter<Order> {
	private static final Logger logger = LoggerFactory
			.getLogger(MozuOrderWriter.class);
	 private Integer tenantId;
	
	 @Autowired
	 private MailchimpUtil mailchimpUtil;
		
	 @Autowired
	 private MozuUtil mozuUtil;
	 
	 @Autowired
	 private MailchimpUtil mailChimpUtil;
	 
	 @Autowired
	    private OrderSyncService orderSyncService;
		
	@Override
	public void write(List<? extends Order> orders) throws Exception {
		StringBuilder errorMsgs = new StringBuilder();
		ApiContext apiContext = new MozuApiContext(tenantId);
	    MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
	    if (setting.getApiKey() != null) {
	        for (Order order : orders) {
				try {
					apiContext.setSiteId(order.getSiteId());
					orderSyncService.sendOrderToMailchimp(apiContext, order);
		        }catch (Exception e) {
			        	 errorMsgs = new StringBuilder(
						"Exception occured while syncing the order "+order.getOrderNumber() +" for tenant id: ")
						.append(tenantId).append(e);
				logger.error(errorMsgs.toString(), e);
			}
		}
	    }

		if (errorMsgs.length() > 0) {
			throw new JobExecutionException(errorMsgs.toString());
		}
	}

	@Value("#{jobParameters['tenantId']}")
    public void setTenantId(final Long tenantId) {
        this.tenantId = tenantId !=null ? tenantId.intValue() : null;
    }

    
}
