package com.mozu.mailchimp.model;

import java.io.Serializable;

/**This class holds mapping between discount code and campaign code.
 * @author Amit
 *
 */
public class DiscountCampaignMapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer discountCode;
	private Integer campaignCode;
	private String campaignName;
	/**
	 * @return the discountCode
	 */
	public Integer getDiscountCode() {
		return discountCode;
	}
	/**
	 * @param discountCode the discountCode to set
	 */
	public void setDiscountCode(Integer discountCode) {
		this.discountCode = discountCode;
	}
	/**
	 * @return the campaignCode
	 */
	public Integer getCampaignCode() {
		return campaignCode;
	}
	/**
	 * @param campaignCode the campaignCode to set
	 */
	public void setCampaignCode(Integer campaignCode) {
		this.campaignCode = campaignCode;
	}
	/**
	 * @return the campaignName
	 */
	public String getCampaignName() {
		return campaignName;
	}
	/**
	 * @param campaignName the campaignName to set
	 */
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

}
