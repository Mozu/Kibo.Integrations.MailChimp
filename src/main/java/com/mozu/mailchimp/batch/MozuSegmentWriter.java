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
import com.mozu.api.contracts.customer.CustomerSegment;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.service.CustomerSegmentSyncService;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

@Service("segmentExportWriter")
@Scope("step")
public class MozuSegmentWriter implements
		ItemWriter<CustomerSegment> {
	private static final Logger logger = LoggerFactory
			.getLogger(MozuSegmentWriter.class);
	 private Integer tenantId;
	
	 @Autowired
	 private MailchimpUtil mailchimpUtil;
		
	 @Autowired
	 private MozuUtil mozuUtil;
	 
	 @Autowired
	 private CustomerSegmentSyncService customerSegmentSyncService;
		
	@Override
	public void write(List<? extends CustomerSegment> customerSegments) throws Exception {
		logger.debug("Start wrtiting the segments to Mailchimp : Segments :"+customerSegments.toString());
		
		StringBuilder errorMsgs = new StringBuilder();
		ApiContext apiContext = new MozuApiContext(tenantId);
	    MailchimpSyncModel mailchimpSettings = mozuUtil.getSettings(tenantId);
	    if (mailchimpSettings.getApiKey() != null) {
	        for (CustomerSegment customerSegment : customerSegments) {
				try {
				
					customerSegmentSyncService.sendSegment(apiContext, customerSegment);
				
            }catch (Exception e) {
				errorMsgs = new StringBuilder(
						"Exception occured while syncing the customerSegment "+customerSegment.getName() +" for tenant id: ")
						.append(tenantId);
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
