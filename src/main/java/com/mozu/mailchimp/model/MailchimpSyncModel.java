package com.mozu.mailchimp.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This is the model used for saving data to MZDB.
 * 
 * @author Amit
 * 
 */
public class MailchimpSyncModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String apiKey;
	private String tenantId;
	private String syncDirection;
	private String mcListId;
	private String listName;
	private String tenantName;
	private Boolean statusFlag;
	private Map<String, String> siteListMap;
	private List<DiscountCampaignModel> discountCampaignMap; // discount would
																// be key and
	// campaign code will be value
	private Map<String, String> campaignCodeStatusMap;

	/**
	 * @return the campaignCodeStatusMap
	 */
	public Map<String, String> getCampaignCodeStatusMap() {
		return campaignCodeStatusMap;
	}

	/**
	 * @param campaignCodeStatusMap
	 *            the campaignCodeStatusMap to set
	 */
	public void setCampaignCodeStatusMap(
			Map<String, String> campaignCodeStatusMap) {
		this.campaignCodeStatusMap = campaignCodeStatusMap;
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey
	 *            the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * @param tenantId
	 *            the tenantId to set
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * @return the syncDirection
	 */
	public String getSyncDirection() {
		return syncDirection;
	}

	/**
	 * @param syncDirection
	 *            the syncDirection to set
	 */
	public void setSyncDirection(String syncDirection) {
		this.syncDirection = syncDirection;
	}

	/**
	 * @return the mcListId
	 */
	public String getMcListId() {
		return mcListId;
	}

	/**
	 * @param mcListId
	 *            the mcListId to set
	 */
	public void setMcListId(String mcListId) {
		this.mcListId = mcListId;
	}

	/**
	 * @return the listName
	 */
	public String getListName() {
		return listName;
	}

	/**
	 * @param listName
	 *            the listName to set
	 */
	public void setListName(String listName) {
		this.listName = listName;
	}

	/**
	 * @return the tenantName
	 */
	public String getTenantName() {
		return tenantName;
	}

	/**
	 * @param tenantName
	 *            the tenantName to set
	 */
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	/**
	 * @return the statusFlag
	 */
	public Boolean getStatusFlag() {
		return statusFlag;
	}

	/**
	 * @param statusFlag
	 *            the statusFlag to set
	 */
	public void setStatusFlag(Boolean statusFlag) {
		this.statusFlag = statusFlag;
	}

	/**
	 * @return the siteListMap
	 */
	public Map<String, String> getSiteListMap() {
		return siteListMap;
	}

	/**
	 * @param siteListMap
	 *            the siteListMap to set
	 */
	public void setSiteListMap(Map<String, String> siteListMap) {
		this.siteListMap = siteListMap;
	}

	/**
	 * @return the discountCampaignMap
	 */
	public List<DiscountCampaignModel> getDiscountCampaignMap() {
		return discountCampaignMap;
	}

	/**
	 * @param discountCampaignMap
	 *            the discountCampaignMap to set
	 */
	public void setDiscountCampaignMap(
			List<DiscountCampaignModel> discountCampaignMap) {
		this.discountCampaignMap = discountCampaignMap;
	}

}
