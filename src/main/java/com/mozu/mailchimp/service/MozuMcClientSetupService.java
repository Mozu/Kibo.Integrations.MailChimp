package com.mozu.mailchimp.service;

import java.sql.Timestamp;
import java.util.Map;
/**
 * @author Amit
 */



import org.springframework.batch.core.JobExecution;

import com.mozu.mailchimp.model.MailchimpSyncModel;

public interface MozuMcClientSetupService {
    
	Map<String, String> getIdList(MailchimpSyncModel mailchimpSetting);

	JobExecution triggerBatch(MailchimpSyncModel mailchimpSetting, String jobName, Timestamp fromRunDate);
}
