package com.mozu.mailchimp.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecwid.mailchimp.MailChimpException;
import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult;
import com.mozu.jobs.dao.JobExecutionDao;
import com.mozu.jobs.handlers.JobHandler;
import com.mozu.base.utils.ApplicationUtils;
import com.mozu.mailchimp.model.JobInfoUI;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.MailchimpUtil;

/**
 * 
 * @author Amit
 * 
 */
@Service
public class MozuMcClientSetupServiceImpl implements MozuMcClientSetupService {
	private static final Logger logger = LoggerFactory
			.getLogger(MozuMcClientSetupServiceImpl.class);

	private JobParameters jobParameters;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private MailchimpUtil mailChimpUtil;
	
    @Autowired
    JobExecutionDao jobExecutionDao;
	
	@Override
	public Map<String, String> getIdList(MailchimpSyncModel mailchimpSetting) {

		ListMethodResult listMethodResult = null;
		try {
			listMethodResult = mailChimpUtil.getAllMcList(mailchimpSetting
					.getApiKey());
		} catch (IOException | MailChimpException e) {
			logger.error("Error while getting list from mailchimp");
		}
		Map<String, String> mcListMap = new HashMap<String, String>();
		if (null != listMethodResult) {

			if (!listMethodResult.isEmpty()) {
				for (ListMethodResult.Data data : listMethodResult.data) {
					mcListMap.put(data.id, data.name);
				}
			} else {
				logger.debug("no mailchimp lists have been associated with this mailchimp API key. Key: "
						+ mailchimpSetting.getApiKey());
			}
		}

		return mcListMap;
	}

	@Override
	public JobExecution triggerBatch(MailchimpSyncModel mailchimpSetting, String jobName, Timestamp fromRunDate) {
		Integer tenantId=Integer.parseInt(mailchimpSetting.getTenantId());
		  JobExecution jobExecution = null;
		 if (ApplicationUtils.isAppEnabled(tenantId)) {
				try {
					this.jobParameters = buildJobParams(tenantId,jobName, fromRunDate);
					jobExecution=jobHandler.executeJob(tenantId,
							0,
							this.jobParameters,
							jobName);
				}  catch (JobExecutionException e) {
		            throw new RuntimeException("Unable to run job: " + e.getMessage());
		        }
		 }else{
			 	throw new RuntimeException(
                "Unable to run job.  Application is disabled. Enable application and try again.");
	}
		 return jobExecution;
	}

	public JobParameters buildJobParams(Integer tenantId,String jobName, Timestamp fromRunDate)
			throws JobExecutionException {
		Timestamp lastSuccessfulRunDate = null;
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong("tenantId", tenantId.longValue());
		builder.addLong("timestamp", new Date().getTime());
		if(jobName.equals(JobInfoUI.ORDER_EXPORT_JOB)){
			if (fromRunDate == null) {
				lastSuccessfulRunDate = jobExecutionDao.getLastExecutionDate(tenantId.longValue(), JobInfoUI.ORDER_EXPORT_JOB);
            } else {
                lastSuccessfulRunDate  = fromRunDate;
            }
			if (lastSuccessfulRunDate != null) {
				builder.addDate(JobInfoUI.LAST_RUN_TIME_PARAM, new Date(lastSuccessfulRunDate.getTime()));
			}
		}
		return builder.toJobParameters();
	}
}
