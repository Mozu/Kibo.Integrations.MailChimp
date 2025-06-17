package com.mozu.mailchimp.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
import com.mozu.base.controllers.AdminControllerHelper;
import com.mozu.mailchimp.exception.UnauthorizedSourceException;
import com.mozu.mailchimp.model.ListSiteMapModel;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.model.MozuLookup;
import com.mozu.mailchimp.service.MozuMcClientSetupService;
import com.mozu.mailchimp.util.ApplicationUtils;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

/**
 * This controller is called when we launch application in Mozu.
 * 
 * @author Amit
 * 
 */
@Controller
@Scope("session")
public class AdminController {

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Autowired
    protected ApplicationUtils applicationUtils;
    
	private static final Logger log = LoggerFactory
			.getLogger(AdminController.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	@Value("${SharedSecret}")
    String sharedSecret;
    @Value("${spice}")
    String keySpice;

	@Autowired
	MozuMcClientSetupService mozuMcClientSetupService;

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
	 * This method fetches pre configured configuration for the requested tenant
	 * Id.If Configuration does not exist then it would allow user to set the
	 * configuration such as mailchimp api key, Site and list mapping etc.
	 * 
	 * @param tenantId
	 *            : Tenant Id of the Mozu
	 * @param model
	 *            : model attribute
	 * @param httpRequest
	 *            : Http Request
	 * @return : Returns next view template name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String index(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ModelMap model) {
		try {

	        // validate request
	        try {
	        	AdminControllerHelper adh = new AdminControllerHelper(keySpice, sharedSecret);
	            if (!adh.securityCheck(httpRequest, httpResponse)) {
	                log.warn("Not authorized");
	                return "unauthorized";
	            }
	        } catch (Exception e) {
	            log.warn("Validation exception: " + e.getMessage());
	            return "unauthorized";
	        }
	     
	        String tenantId = httpRequest.getParameter("tenantId");
	         		
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(Integer
					.parseInt(tenantId));
			Tenant tenant = mozuUtil.getTenantName(Integer.parseInt(tenantId));

			if (mailchimpSetting != null
					&& mailchimpSetting.getApiKey() != null && mailChimpUtil.validateMailchimpKey(mailchimpSetting.getApiKey())) {
				
				mailchimpSetting.setTenantId(tenantId);
				mailchimpSetting.setTenantName(tenant.getName());

				if (mailchimpSetting.getApiKey() != null) {
					model.addAttribute("welcomeMessage", (new StringBuilder())
							.append(tenant.getName()).toString());
					model.addAttribute("userExist", "true");
				}

				ListMethodResult resultList = mailChimpUtil
						.getAllMcList(mailchimpSetting.getApiKey());
				List<String> listId = new ArrayList<String>();
				for (Data data : resultList.data) {
					listId.add(data.name);
				}

				model.addAttribute("mailchimpSyncModel", mailchimpSetting);
				model.addAttribute("listIdList", listId);
				model.addAttribute("invalidApiKey", null);
			} else {
				MailchimpSyncModel firstMcModel = new MailchimpSyncModel();
				firstMcModel.setTenantId(tenantId);
				firstMcModel.setStatusFlag(false);
				model.addAttribute("mailchimpSyncModel", firstMcModel);
			}
			return "mailchimpSettings";
		} catch (UnauthorizedSourceException e) {
			log.error("Request received from a non mozu source. Blocked with message: "
					+ e.getMessage());
			return "unauthorized";
		} catch (Exception e) {
			log.error("Exception in AdminController: " + e.getMessage());
			return "mailchimpSettings";
		}

	}
	
}
