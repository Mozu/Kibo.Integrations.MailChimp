/**
 * 
 */
package com.mozu.mailchimp.batch;

import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerSegment;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.service.CustomerSegmentSyncService;
import com.mozu.mailchimp.service.CustomerSyncService;
import com.mozu.mailchimp.service.MozuMcClientSetupService;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

/**
 * @author Akshay
 * 
 */
@Service("customerExportWriter")
@Scope("step")

public class MozuCustomerWriter implements ItemWriter<CustomerAccount> {
	private static final Logger logger = LoggerFactory
			.getLogger(MozuCustomerWriter.class);

	@Autowired
	MozuMcClientSetupService mozuMcClientSetupService;
	
	@Autowired
	private MailchimpUtil mailchimpUtil;
	
	@Autowired
	private MozuUtil mozuUtil;
	
	@Autowired
	private CustomerSyncService customerSyncService;
	
	private Integer tenantId;
 

	@Override
	public void write(List<? extends CustomerAccount> items) throws Exception {
	    StringBuilder errorMsgs = new StringBuilder();

	    MailchimpSyncModel mailchimpSettings = mozuUtil.getSettings(tenantId);
	    ApiContext apiContext = new MozuApiContext(tenantId);
	    if (mailchimpSettings.getApiKey() != null) {
	        for (CustomerAccount customer : items) {
	        	try{
	        		if(StringUtils.isNotEmpty(customer.getEmailAddress())){
		        		logger.info("Start writing customer "+customer.getEmailAddress() +" of tenant "+tenantId+" to mailchimp");
		        		customerSyncService.sendCustomerToMailchimp(apiContext, customer);
	        		}
	        		
	        	}
	        	catch (Exception e) {
	                    errorMsgs.append("Error exporting customer with Id ").append(customer.getId()).append(": ").append(e.getMessage()).append(" | ");
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

