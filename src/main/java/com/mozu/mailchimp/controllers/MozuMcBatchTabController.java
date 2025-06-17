package com.mozu.mailchimp.controllers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mozu.mailchimp.model.JobInfoUI;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.model.MozuLookup;
import com.mozu.mailchimp.service.MozuMcClientSetupService;
import com.mozu.mailchimp.util.ApplicationUtils;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;
import com.mozu.base.controllers.AdminControllerHelper;
/**
 * This controller is used for Sync Setup Tab. Which creates a job schedule
 * entry in DB.
 * 
 * @author Amit
 * 
 */
@Controller
@RequestMapping("/api/job")
public class MozuMcBatchTabController {

	private static final Logger log = LoggerFactory
			.getLogger(MozuMcBatchTabController.class);

	@Value("${MzToMcVal}")
	private String mzToMcVal;
	
	@Value("${MzToMcOrderVal}")
	private String mzToMcOrderVal;

	@Value("${McToMzVal}")
	private String mcToMzVal;

	@Value("${MzToMcTxt}")
	private String mzToMcTxt;
	
	@Value("${MzToMcOrderTxt}")
	private String mzToMcOrderTxt;

	@Value("${McToMzTxt}")
	private String mcToMzTxt;
	
	@Autowired
	private MozuMcClientSetupService mozuMcClientSetupService;
	
	@Autowired
	private MozuUtil mozuUtil;
	
	@Autowired
	private MailchimpUtil mailChimpUtil;

	public MozuMcBatchTabController() {
	}

	@ModelAttribute("syncDirectionList")
	public List<MozuLookup> populateSyncList() {

		List<MozuLookup> mozuLookupList = new ArrayList<MozuLookup>();
		MozuLookup mozuLookup = new MozuLookup();
		mozuLookup.setKey(mzToMcVal);
		mozuLookup.setValue(mzToMcTxt);
		MozuLookup mozuLookup1 = new MozuLookup();
		mozuLookup1.setKey(mzToMcOrderVal);
		mozuLookup1.setValue(mzToMcOrderTxt);
		MozuLookup mozuLookup2 = new MozuLookup();
		mozuLookup2.setKey(mcToMzVal);
		mozuLookup2.setValue(mcToMzTxt);
		mozuLookupList.add(mozuLookup);
		mozuLookupList.add(mozuLookup1);
		mozuLookupList.add(mozuLookup2);
		return mozuLookupList;
	}

	/**
	 * Saves the batch tab entry in Database.
	 * 
	 * @param batchEntryParam
	 * @return
	 */
	@RequestMapping(value = "/saveBatchEntry", method = RequestMethod.POST)
	public @ResponseBody
	String createCustomerSync(
			@RequestParam(value = "batchEntryParam") String batchEntryParam,@CookieValue (AdminControllerHelper.TENANT_ID_COOKIE) int tenantId)throws Exception{
		log.debug("CreateCustomerSync method ");
	    if (!ApplicationUtils.isAppEnabled(tenantId)) {
		    log.debug("Application is disabled, batch not scheduled");
		    return "DISABLED";
		}
		
		try {
			Timestamp importFromTimestamp = null;
			String[] tenantParamArr = batchEntryParam.split(":");
			String direction = tenantParamArr[0];
			if(tenantParamArr.length>1){
				String fromDate = tenantParamArr[1];
				 if (StringUtils.isNotBlank(fromDate)) {
		          importFromTimestamp = Timestamp.valueOf(fromDate + " 00:00:00");
		        }
			}	
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(tenantId);
			if(!mailChimpUtil.isMailchimpListValid(mailchimpSetting)){
				log.debug("Please select a valid list, batch not scheduled");
			    return "INVALIDLIST";
			}
			mailchimpSetting.setSyncDirection(direction);
			mozuUtil.saveSettings(tenantId, mailchimpSetting);
			if ("0".equals(direction)) {
			    // mozu to mailchimp bulk sync
				mozuMcClientSetupService.triggerBatch(mailchimpSetting, JobInfoUI.CUSTOMER_EXPORT_JOB, importFromTimestamp);
		        log.debug("CustomerSync to MailChimp ");
			} else if ("1".equals(direction)) {
			    // mozu to mailchimp bulk sync
				mozuMcClientSetupService.triggerBatch(mailchimpSetting, JobInfoUI.ORDER_EXPORT_JOB, importFromTimestamp);
		        log.debug("Order Sync to MailChimp ");
			}else { 
			    // mailchimp to mozu bulk sync
                mozuMcClientSetupService.triggerBatch(mailchimpSetting, JobInfoUI.CUSTOMER_IMPORT_JOB, importFromTimestamp);
                log.debug("CustomerSync from MailChimp");
			}
		} catch (Exception e) {
			log.debug("Exception occured while syncing the data to mailchimp "+e);
			return "FAIL";
		}
		return "SUCCESS";
	}

	/**
	 * This is used for populating the dropdown for mailchimp list.
	 * 
	 * @param tenantId
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/getMailchimpSettings", method = RequestMethod.GET)
	public @ResponseBody
	ObjectNode getMailchimpSetting(
			@CookieValue (AdminControllerHelper.TENANT_ID_COOKIE) int tenantId) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode returnObj = mapper.createObjectNode();
		ArrayNode ddArrayNode = mapper.createArrayNode();
		try {
			returnObj.put("status", "SUCCESS");
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(tenantId);
			if (StringUtils.isNotBlank(mailchimpSetting.getApiKey()) && mailchimpSetting.getStatusFlag() && StringUtils.isNotBlank(mailchimpSetting.getMcListId())) {
				Map<String, String> returnMap = mozuMcClientSetupService
						.getIdList(mailchimpSetting);
				for (Map.Entry<String, String> entry : returnMap.entrySet()) {
					ObjectNode ddObjNode = mapper.createObjectNode();
					ddObjNode.put("text", entry.getValue());
					ddObjNode.put("value", entry.getKey());
					ddArrayNode.add(ddObjNode);
				}
				returnObj.put("ddList", ddArrayNode);

			} else {
				returnObj.put("status", "FAIL");
			}
		} catch (Exception e) {
			returnObj.put("status", "FAIL");
			return returnObj;
		}
		return returnObj;

	}

	/**
	 * This method is used for rescheduling the job which are failed. T
	 * 
	 * @param batchId
	 * @return
	 */
/*	@RequestMapping(value = "/reScheduleBatch", method = RequestMethod.POST)
	public @ResponseBody
	String rescheduleBatch(
			@RequestParam(value = "resschedulePrm") String resschedulePrm) {
		String tenantId = resschedulePrm.split(":")[0];
		String direction = resschedulePrm.split(":")[1];

		if (!ApplicationUtils.isAppEnabled(Integer.parseInt(tenantId))) {
            log.debug("Application is disabled, batch not scheduled");
            return "DISABLED";
        }
        
		try {
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(Integer
					.parseInt(tenantId));
			if(!mailChimpUtil.isMailchimpListValid(mailchimpSetting)){
				log.debug("Please select a valid list, batch not scheduled");
			    return "INVALIDLIST";
			}
			if ("0".equals(direction)) {
			    // mozu to mailchimp bulk sync
				mozuMcClientSetupService.triggerBatch(mailchimpSetting, JobInfoUI.CUSTOMER_EXPORT_JOB);
		        log.debug("CustomerSync to MailChimp ");
			} else if ("1".equals(direction)) {
			    // mozu to mailchimp bulk sync
				mozuMcClientSetupService.triggerBatch(mailchimpSetting, JobInfoUI.ORDER_EXPORT_JOB);
		        log.debug("Order Sync to MailChimp ");
			}else { 
			    // mailchimp to mozu bulk sync
                mozuMcClientSetupService.triggerBatch(mailchimpSetting, JobInfoUI.CUSTOMER_IMPORT_JOB);
                log.debug("CustomerSync from MailChimp");
			}
		} catch (Exception ex) {
			log.error("Exception while saving batch entry in resheduleBatch() method "
					+ ex.getMessage());
			return "FAIL";
		}
		return "SUCCESS";
	}*/
}
