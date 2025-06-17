package com.mozu.mailchimp.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.mozu.base.controllers.AdminControllerHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.productadmin.Discount;
import com.mozu.api.contracts.productadmin.DiscountCollection;
import com.mozu.api.contracts.productadmin.DiscountLocalizedContent;
import com.mozu.api.contracts.tenant.Site;
import com.mozu.api.contracts.tenant.Tenant;
import com.mozu.api.resources.commerce.catalog.admin.DiscountResource;
import com.mozu.api.resources.platform.TenantResource;
import com.mozu.mailchimp.model.DiscountCampaignModel;
import com.mozu.mailchimp.model.DiscountModel;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.Constants;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

/**
 * This controller is used for rendering campaign and discount screen.
 * 
 * @author Amit
 * 
 */
@Controller
public class CampaignController {
	private static final Logger log = LoggerFactory
			.getLogger(CampaignController.class);

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * This method will get all the configured campaigns in mailchimp.
	 * 
	 * @param tenantId
	 * @return : Returns a status message.
	 */
	@RequestMapping(value = "/getCampaigns", method = RequestMethod.GET)
	public @ResponseBody
	ObjectNode getCampaigns(@CookieValue (AdminControllerHelper.TENANT_ID_COOKIE) int tenantId) {
		List<DiscountModel> discountList = null;
		discountList = getDiscount(tenantId);
		ObjectNode discountNode = mapper.createObjectNode();
		discountNode = getDiscountJson(discountList);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode returnObj = mapper.createObjectNode();
		try {
			MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
			List<DiscountCampaignModel> discCampMap = setting
					.getDiscountCampaignMap();
			if(setting.getStatusFlag() && setting.getMcListId()!=null){
				List<DiscountCampaignModel> campaignList = mailChimpUtil.getCampaignList(setting.getApiKey());
				List<DiscountCampaignModel> filteredCampaignList=filterCampaignList(campaignList, discCampMap);
				String campaignNode = getJsonMapModel(filteredCampaignList);
				returnObj.put("campaignDD", campaignNode);
				returnObj.put("discountDD", discountNode);
				returnObj.put("status", "SUCCESS");
			}
			else{
				returnObj.put("status", "FAIL");
			}
			return returnObj;
		} catch (Exception e) {
			returnObj.put("status", "FAIL");
			return returnObj;
		}

	}
	
	private List<DiscountCampaignModel> filterCampaignList(List<DiscountCampaignModel> campaignList, List<DiscountCampaignModel> discCampMap){
		List<DiscountCampaignModel> filteredCampaignList = new ArrayList<DiscountCampaignModel>();
		boolean present;
		for (DiscountCampaignModel campaign : campaignList) {
			present=false;
			if(discCampMap!=null){
				for (DiscountCampaignModel discountCampaign : discCampMap) {
					if(campaign.getKeyStr().equals(discountCampaign.getValStr())){
						present=true;
					}
				}
			}
			if(!present)
				filteredCampaignList.add(campaign);
		}
		return filteredCampaignList;
		
	}

	/**
	 * Converts List object to Json string
	 * 
	 * @param inputList
	 * @return
	 * @throws JsonProcessingException
	 */
	private String getJsonMapModel(List<DiscountCampaignModel> inputList)
			throws JsonProcessingException {
		String value = (new StringBuilder()).append("'")
				.append(mapper.writeValueAsString(inputList)).append("'")
				.toString();
		return value;
	}

	/**
	 * This method will pull all the configured discount from Mozu sysytem.
	 * Currently it is hard coded.
	 * 
	 * @param tenantId
	 * @return
	 */
	private List<DiscountModel> getDiscount(Integer tenantId) {
		try {
			

			ApiContext apiContext=new MozuApiContext(tenantId);
			List<DiscountModel> disocuntList = new ArrayList<DiscountModel>();
			TenantResource tenantResource = new TenantResource();
	        Tenant tenant = null;
	        try {
	            tenant = tenantResource.getTenant(tenantId);
	        } catch (Exception e) {
	        	log.warn("Unable to find tenant: " + tenantId);
	        }
	       
	        if (tenant != null) {
	            for (Site site : tenant.getSites()) {
	            	apiContext.setSiteId(site.getId());
	            	DiscountResource discountResource = new DiscountResource(
							apiContext);
					 DiscountCollection collection  = discountResource.getDiscounts();
					for (Discount discount : collection.getItems()) {
						log.debug("amount " + discount.getAmount());
						DiscountModel discountModel = new DiscountModel();
						if (discount.getStatus().equals("Active")) {
							DiscountLocalizedContent discountContent= 
									discountResource.getDiscountContent(discount.getId());
							discountModel.setAmount(discount.getAmount() != null?discount.getAmount():0);
							discountModel.setDiscountCode(discount.getId());
							discountModel.setDiscountType(discount.getAmountType());
							discountModel.setName(discountContent.getName());
							
							disocuntList.add(discountModel);
						}
					}
	            }

	        }
	   

			return disocuntList;

		} catch (Exception e) {
			log.error("Exception in getDiscount(), reason: " + e.getMessage());
			return null;

		}
	}

	/**
	 * This method is used for saving the discount and campign mapping data.
	 * 
	 * @param discountParam
	 * @return
	 */
	@RequestMapping(value = "/saveDiscountMapping", method = RequestMethod.POST)
	public @ResponseBody
	String saveDiscountMapping(
			@RequestBody String discountParam) {
		log.debug("START : saveDiscountMapping method ");
		String[] paramArr = discountParam.split(":");
		String tenantId = paramArr[0];
		String campaignCode = paramArr[1];
		String discountCode = paramArr[2];
		String discountType = paramArr[3];
		String discountAmount = paramArr[4];
		String discountName = paramArr[5];
				
		try {
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(Integer
					.parseInt(tenantId));
			List<DiscountCampaignModel> discCampMap = mailchimpSetting
					.getDiscountCampaignMap();

			Map<String, String> campaignStatusMap = mailchimpSetting
					.getCampaignCodeStatusMap();
			if (campaignStatusMap == null) {
				campaignStatusMap = new HashMap<String, String>();
			}
			if (discCampMap == null) {
				discCampMap = new ArrayList<DiscountCampaignModel>();
			}
			DiscountCampaignModel modelDisCampaign = new DiscountCampaignModel();
			modelDisCampaign.setKeyStr(discountCode);
			modelDisCampaign.setValStr(campaignCode);
			modelDisCampaign.setDiscountName(discountName);
			discCampMap.add(modelDisCampaign);
			campaignStatusMap.put(campaignCode, "NS");// NS stands for Not Send
			// to create merge tag for customers
			mailChimpUtil.createCustomerMergeTag(campaignCode, tenantId,
					mailchimpSetting.getApiKey());
			// updates value in merge tag for discount code, type, amount
			boolean updateMergeFlag = mailChimpUtil.updateCustomerMergeTag(
					tenantId, discountName, discountType, discountAmount,
					mailchimpSetting.getApiKey());

			if (updateMergeFlag) {
				// saving mapping campaign and discount mapping in MZDB
				mailchimpSetting.setDiscountCampaignMap(discCampMap);
				mailchimpSetting.setCampaignCodeStatusMap(campaignStatusMap);
				mozuUtil.saveSettings(Integer.parseInt(tenantId),
						mailchimpSetting);
			}

		} catch (Exception e) {
			log.error("Exception while saveDiscountMapping " + e.getMessage());
			return Constants.FAILED;
		}
		return Constants.SUCCESS;
	}

	@RequestMapping(value = "/getDiscountMapping", method = RequestMethod.GET)
	public @ResponseBody
	ObjectNode getDiscountMapping(@CookieValue (AdminControllerHelper.TENANT_ID_COOKIE) int tenantId) {
		ObjectNode returnNode = mapper.createObjectNode();
		returnNode.put("status", Constants.SUCCESS);
		try {
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(tenantId);
			List<DiscountCampaignModel> mappingDiscountCampaign = mailchimpSetting
					.getDiscountCampaignMap();
			Map<String, String> mapCampaignStatus = mailchimpSetting
					.getCampaignCodeStatusMap();
		
			List<DiscountCampaignModel> campaignMapList = mailChimpUtil.getAllCampignMap(mailchimpSetting.getApiKey());
			Map<String, String> campNameIdMap = new HashMap<String, String>();
			for (DiscountCampaignModel model : campaignMapList) {
				campNameIdMap.put(model.getKeyStr(), model.getValStr());
			}

			ArrayNode disCapArrayNode = mapper.createArrayNode();
			if (mappingDiscountCampaign != null && mailchimpSetting.getStatusFlag() && mailchimpSetting.getMcListId()!=null) {
				for (DiscountCampaignModel entry : mappingDiscountCampaign) {
					if(entry.getDiscountName() !=null && campNameIdMap.get(entry.getValStr())!=null){
					ObjectNode disCampNode = mapper.createObjectNode();
					disCampNode.put("discountCode", entry.getKeyStr());
					disCampNode.put("discountName", entry.getDiscountName());
					disCampNode.put("campaignCode",entry.getValStr()+":"+
							campNameIdMap.get(entry.getValStr()));
					disCampNode.put("campaignStatus",
							mapCampaignStatus.get(entry.getValStr()));
					disCapArrayNode.add(disCampNode);
					}
				}
			}

			returnNode.put("discCampMapping", disCapArrayNode);

		} catch (Exception e) {
			log.error("Exception while getDiscountMapping " + e.getMessage());
			returnNode.put("status", Constants.FAILED);
		}

		return returnNode;
	}

	/**
	 * This method returns discount Json.
	 * 
	 * @param listDiscount
	 * @return
	 */
	private ObjectNode getDiscountJson(List<DiscountModel> listDiscount) {
		ObjectNode returnObj = mapper.createObjectNode();
		String value = null;
		try {
			value = (new StringBuilder()).append("'")
					.append(mapper.writeValueAsString(listDiscount))
					.append("'").toString();
		} catch (JsonProcessingException e) {
			returnObj.put("discountData", value);
		}
		returnObj.put("discountData", value);

		return returnObj;
	}

	@RequestMapping(value = "/deleteDiscountMapping", method = RequestMethod.POST)
	public @ResponseBody
	ObjectNode deleteDiscountMapping(
			@RequestParam(value = "discountParam") String discountParam) {
		log.debug("START : deleteDiscountMapping method ");
		String[] paramArr = discountParam.split(":");
		String tenantId = paramArr[0];
		String discountCode = paramArr[1];
		String campaignCode=paramArr[2];
		MailchimpSyncModel setting;
		ObjectNode returnNode = mapper.createObjectNode();
		try {
			setting = mozuUtil.getSettings(Integer.parseInt(tenantId));
			List<DiscountCampaignModel> disCampMap = setting.getDiscountCampaignMap();
			List<DiscountCampaignModel> repopulatMapping = new ArrayList<DiscountCampaignModel>();
			if (disCampMap != null) {
				for (DiscountCampaignModel entry : disCampMap) {
					if (!(entry.getKeyStr().equals(discountCode) && entry.getValStr().equals(campaignCode))) {
						DiscountCampaignModel model = new DiscountCampaignModel();
						model.setKeyStr(entry.getKeyStr());
						model.setValStr(entry.getValStr());
						model.setDiscountName(entry.getDiscountName());
						repopulatMapping.add(model);
						
					}

				}
			}
			setting.setDiscountCampaignMap(repopulatMapping);
			mozuUtil.saveSettings(Integer.parseInt(tenantId), setting);
			returnNode.put("status", Constants.SUCCESS);

		} catch (Exception e) {
			returnNode.put("status", Constants.FAILED);
			log.error("Exception in deleteDiscountMapping " + e.getMessage());
		}
		log.debug("End deleteDiscountMapping");
		return returnNode;
	}

	/**
	 * This method is use to send the campaign.
	 * 
	 * @param sendCampaignPrm
	 * @return
	 */
	@RequestMapping(value = "/sendCampaign", method = RequestMethod.POST)
	public @ResponseBody
	String sendCampaign(
			@RequestParam(value = "sendCampaignPrm") String sendCampaignPrm) {
		log.debug("START sendCampaign");
		String tenantId = sendCampaignPrm.split(":")[0];
		String discountCode = sendCampaignPrm.split(":")[1];
		String campaignCode = sendCampaignPrm.split(":")[2];

		try {
			MailchimpSyncModel setting = mozuUtil.getSettings(Integer
					.parseInt(tenantId));

			List<DiscountCampaignModel> campaignList = mailChimpUtil.getCampaignList(setting
					.getApiKey());
			for (DiscountCampaignModel model : campaignList) {
				if (model.getValStr().equals(campaignCode)) {
					campaignCode = model.getKeyStr();
				}
			}

			boolean campaignSendFlag = false;
			campaignSendFlag = mailChimpUtil.sendCampaign(setting.getApiKey(),
					campaignCode, tenantId);
			Map<String, String> campStatusMap = setting
					.getCampaignCodeStatusMap();
			if (campaignSendFlag) {
				if (campStatusMap != null) {
					campStatusMap.put(campaignCode, "S");// update status flag
															// to sent
				}
			}
			setting.setCampaignCodeStatusMap(campStatusMap);
			mozuUtil.saveSettings(Integer.parseInt(tenantId), setting);
			log.debug("END sendCampaign");
			return Constants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while sending campaign in sendCampaign() for tenantID "
					+ tenantId
					+ " discount Code "
					+ discountCode
					+ " campaign code " + campaignCode);
			return Constants.FAILED;
		}

	}

	@RequestMapping(value = "/resetMergeTags", method = RequestMethod.POST)
	public @ResponseBody
	String resetMergeTags(
			@RequestParam(value = "resetMergeTags") String resetMergeTags) {
		log.debug("START resetMergeTags");
		String tenantId = resetMergeTags.split(":")[0];
		String discountCode = resetMergeTags.split(":")[1];
		String campaignCode = resetMergeTags.split(":")[2];

		try {
			MailchimpSyncModel setting = mozuUtil.getSettings(Integer
					.parseInt(tenantId));

			mailChimpUtil.resetMergeTag(setting.getApiKey(), campaignCode,
					tenantId);

			log.debug("END resetMergeTags");
			return Constants.SUCCESS;
		} catch (Exception e) {
			log.error("Error while sending campaign in sendCampaign() for tenantID "
					+ tenantId
					+ " discount Code "
					+ discountCode
					+ " campaign code " + campaignCode);
			return Constants.FAILED;
		}

	}

}
