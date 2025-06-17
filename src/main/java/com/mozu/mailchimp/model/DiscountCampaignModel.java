package com.mozu.mailchimp.model;

import java.io.Serializable;

public class DiscountCampaignModel extends MapModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String discountName;

	/**
	 * @return the discountName
	 */
	public String getDiscountName() {
		return discountName;
	}

	/**
	 * @param discountName
	 *            the discountName to set
	 */
	public void setDiscountName(String discountName) {
		this.discountName = discountName;
	}

}
