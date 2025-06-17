package com.mozu.mailchimp.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.mozu.base.controllers.AdminControllerHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mozu.jobs.dao.JobExecutionDao;
import com.mozu.jobs.dao.SkipItemsDao;
import com.mozu.jobs.models.SkipItems;
import com.mozu.mailchimp.model.BatchModel;
import com.mozu.mailchimp.model.JobInfoUI;
import com.mozu.mailchimp.service.MozuMcClientSetupService;
import com.mozu.mailchimp.util.CommonUtil;

/**
 * This controller is used for rendering the job history screen.
 * 
 * @author Amit
 * 
 */
@Controller
@RequestMapping("/api/jobHistory")
public class JobHistoryController {

	private static final Logger log = LoggerFactory
			.getLogger(JobHistoryController.class);

	@Value("${MzToMcVal}")
	private String mzToMcVal;

	@Value("${McToMzVal}")
	private String mcToMzVal;

	@Value("${MzToMcTxt}")
	private String mzToMcTxt;

	@Value("${McToMzTxt}")
	private String mcToMzTxt;

	@Autowired
	JobExecutionDao jobExecutionDao;

	@Autowired
	SkipItemsDao skipItemDao;
	
	@Autowired
    JobExplorer jobExplorer;

	/**
	 * This method gets all the job history data from DB.
	 * 
	 * @param tenantId
	 * @return : It returns a Json object with all job history data.
	 */

	@RequestMapping(value = "/getJobHistory", method = RequestMethod.GET)
	public @ResponseBody
	ObjectNode getHistory(@CookieValue (AdminControllerHelper.TENANT_ID_COOKIE) int tenantId) {
		
	    ResourceBundle messages = ResourceBundle.getBundle("Messages");
	    ObjectNode returnObj = null;
	    try {
		List<Long> jobExecutionIds = jobExecutionDao.getRecentJobExecutionIds(Long.valueOf(tenantId), 
		        JobInfoUI.CUSTOMER_IMPORT_JOB);
		List<BatchModel> modelList = addToModelList(jobExecutionIds, messages.getString("history.customer_import_sync"));
		 
	     jobExecutionIds = jobExecutionDao.getRecentJobExecutionIds(Long.valueOf(tenantId), 
	    		 JobInfoUI.CUSTOMER_EXPORT_JOB);
	     modelList.addAll(addToModelList(jobExecutionIds, messages.getString("history.customer_export_sync")));
	     
	     jobExecutionIds = jobExecutionDao.getRecentJobExecutionIds(Long.valueOf(tenantId), 
	    		 JobInfoUI.ORDER_EXPORT_JOB);
	     modelList.addAll(addToModelList(jobExecutionIds, messages.getString("history.order_export_sync")));
	     
	    Collections.sort(modelList, new JobHistoryComparator());

		returnObj = getHistoryJsonReturn(modelList);
		} catch (JsonProcessingException e) {
			log.error("Error processing history json response: " + e);
		}catch(Exception e){
			log.error("Error getting the job details: " + e);
			throw e;
		}
		return returnObj;
	}
	
	private List<BatchModel> addToModelList(List<Long> jobExecutionIds,
            String direction) {
        List<BatchModel> modelList = new ArrayList<BatchModel>();

        for (Long jobExecutionId : jobExecutionIds) {
            JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
            BatchModel model = new BatchModel();
            model.setBatchStatus(jobExecution.getStatus().toString());
            model.setCreatedDate(CommonUtil.getDDMonYYYY(jobExecution.getCreateTime()));
            model.setSyncDirection(direction);
            List<SkipItems> skipItemList = skipItemDao
                    .getByJobExecutionId(jobExecutionId);

            model.setErrorCount(skipItemList.size());
            model.setJobExecutionId(jobExecutionId);
            int readCount = 0;
            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                readCount += stepExecution.getReadCount();
            }
            model.setRecordCount(readCount);
            modelList.add(model);
        }
        return modelList;
    }

	private ObjectNode getHistoryJsonReturn(List<BatchModel> model)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode returnObj = mapper.createObjectNode();

		String value = (new StringBuilder()).append("'")
				.append(mapper.writeValueAsString(model)).append("'")
				.toString();
		returnObj.put("historyData", value);

		return returnObj;
	}

	@RequestMapping(value = "/getErrorDataForBatch", method = RequestMethod.GET)
	public @ResponseBody
	ObjectNode getErrorDataForBatch(
			@RequestParam(value = "jobExecutionId") String jobExecutionId) {
		
		List<SkipItems> skippedItems = skipItemDao.getByJobExecutionId(Long.parseLong(jobExecutionId));
		
		ObjectNode returnObj = null;
		try {
			returnObj = getErrorDataJson(skippedItems);
		} catch (JsonProcessingException e) {
			returnObj = null;
		}
		return returnObj;
	}

	private ObjectNode getErrorDataJson(List<SkipItems> model)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode returnObj = mapper.createObjectNode();

		String value = (new StringBuilder()).append("'")
				.append(mapper.writeValueAsString(model)).append("'")
				.toString();
		returnObj.put("errorData", value);

		return returnObj;
	}

}
