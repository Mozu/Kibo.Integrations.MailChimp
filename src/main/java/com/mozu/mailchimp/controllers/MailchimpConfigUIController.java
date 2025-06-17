package com.mozu.mailchimp.controllers;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult;
import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.tenant.Site;
import com.mozu.api.contracts.tenant.Tenant;
import com.mozu.api.utils.JsonUtils;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.model.MozuLookup;
import com.mozu.mailchimp.service.MozuMcClientSetupService;
import com.mozu.mailchimp.util.ApplicationUtils;
import com.mozu.mailchimp.util.Constants;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;
import com.mozu.base.controllers.AdminControllerHelper;

/**
 * this controller is used for saving mailchimp configuration.
 * 
 * @author Amit
 * 
 */
@Controller
@RequestMapping("/api/config")
public class MailchimpConfigUIController {

	private static final Logger logger = LoggerFactory
			.getLogger(MailchimpConfigUIController.class);

	@Autowired
	private MozuMcClientSetupService mozuMcClientSetupservice;

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Autowired
    protected ApplicationUtils applicationUtils;
    
	@Value("${webhookUrl}")
	private String URL;

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

	public MailchimpConfigUIController() {
		JsonUtils.initObjectMapper();
	}

	@ModelAttribute("mailchimpSyncModel")
	public MailchimpSyncModel populateMaillchimpSyc() {
		MailchimpSyncModel mailchimpSyncModel = new MailchimpSyncModel();
		return mailchimpSyncModel;
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
	 * This method is used for saving the Mailchimp configuration along with
	 * site and list mapping.
	 * 
	 * @param tenantParam
	 * @return : returns Status
	 * @throws NumberFormatException
	 * @throws Exception
	 */

	@RequestMapping(value = "/saveMailchimpKey", method = RequestMethod.GET)
	public @ResponseBody
	String saveApiKey(@CookieValue (AdminControllerHelper.TENANT_ID_COOKIE) int tenantId,@RequestParam(value = "tenantParam") String tenantParam)
			throws NumberFormatException, Exception {
		logger.debug("Inside MailchimpConfigUIController controller saveApiKey method");
		String[] tenantParamArr = tenantParam.split(":");
		String apiKey = tenantParamArr[0];
		String listIdPrm = tenantParamArr[2];
		String status;
		if (StringUtils.isEmpty(apiKey)) { 
		    return "FAIL";
		}
		
		MozuApiContext apiContext = new MozuApiContext(
				Integer.valueOf(tenantId));
		MailchimpSyncModel mailchimpSettingMzdb = mozuUtil.getSettings(tenantId);
		if (mailChimpUtil.validateMailchimpKey(apiKey)) {
			
				if (mailchimpSettingMzdb == null
						|| mailchimpSettingMzdb.getApiKey() == null) {
				    mailchimpSettingMzdb = new MailchimpSyncModel();
				    mailchimpSettingMzdb.setTenantId(String.valueOf(tenantId));
				    mailchimpSettingMzdb.setApiKey(apiKey);
				    mailchimpSettingMzdb.setStatusFlag(true);
					
				} else {
					// To remove the mapping data if key have been updated
					if (!apiKey.equals(mailchimpSettingMzdb.getApiKey())) {
						resetSettings(mailchimpSettingMzdb,String.valueOf(tenantId), apiKey,true);
					} else {
						
						if (!(Constants.PLEASE_SELECT_VAL.equals(listIdPrm)
								)) { // to check leist and
							// site dropdown
							// values are not
							// please select
											
						mailchimpSettingMzdb.setApiKey(apiKey);
						mailchimpSettingMzdb.setStatusFlag(true);
						mailchimpSettingMzdb.setMcListId(listIdPrm);
					}
					}
				}
			
			ListMethodResult resultList = mailChimpUtil.getAllMcList(apiKey);
			List<String> listIdList = new ArrayList<String>();
			for (Data data : resultList.data) {
				listIdList.add(data.id);
			}
			String url = URL + tenantId;
			mailChimpUtil.createWebhook(listIdList, url, apiKey);
			status= "SUCCESS";
		} else {
			resetSettings(mailchimpSettingMzdb,String.valueOf(tenantId), apiKey, false);
			status= "FAIL";
		}
		if(mailchimpSettingMzdb!=null) {
			mozuUtil.saveSettings(tenantId,mailchimpSettingMzdb);
		}
		applicationUtils.setAppInitializeStatus(apiContext);
		
		return status;

	}
	
	public void resetSettings(MailchimpSyncModel mailchimpSettingMzdb,String tenantId, String apiKey, Boolean status){
		mailchimpSettingMzdb.setApiKey(apiKey);
		mailchimpSettingMzdb.setStatusFlag(status);
		mailchimpSettingMzdb.setListName(null);
		mailchimpSettingMzdb.setMcListId(null);
		mailchimpSettingMzdb.setSyncDirection(null);
		
	}

	/**
	 * This method is called by ajax call to populate the site and list drop
	 * downs
	 * 
	 * @param tenantId
	 * @return
	 */
	@RequestMapping(value = "/getSiteListDD", method = RequestMethod.GET)
	public @ResponseBody
	ObjectNode getSiteListDD(@CookieValue (AdminControllerHelper.TENANT_ID_COOKIE) int tenantId) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode returnObj = mapper.createObjectNode();
		ArrayNode ddListArrayNode = mapper.createArrayNode();
		ArrayNode ddSiteArrayNode = mapper.createArrayNode();
		try {
			returnObj.put("status", "SUCCESS");
			MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
			String apikey = null;
			// adding please select
			ObjectNode plsSelectListNode = mapper.createObjectNode();
			ddListArrayNode.add(plsSelectListNode);
			plsSelectListNode.put("id", 0);
			plsSelectListNode.put("value", Constants.PLEASE_SELECT);
			if (setting != null) {
				apikey = setting.getApiKey();
				ListMethodResult resultList = mailChimpUtil.getAllMcList(apikey);

				for (Data data : resultList.data) {
					ObjectNode ddObjNode = mapper.createObjectNode();
					ddObjNode.put("id", data.id);
					ddObjNode.put("value", data.name);
					ddListArrayNode.add(ddObjNode);
	
				}			
				
			}	
			String selectedList=setting.getMcListId()!=null?setting.getMcListId():"0";
			returnObj.put("selectedList", selectedList);
			returnObj.put("listDD", ddListArrayNode);
			
			return returnObj;
		} catch (Exception e) {
			returnObj.put("status", "FAIL");
			return returnObj;
		}
	}

}
