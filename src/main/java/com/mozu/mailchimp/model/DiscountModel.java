package com.mozu.mailchimp.model;

import java.io.Serializable;

/**
 * This model is used to populate discount dropdown
 * 
 * @author Amit
 * 
 */
public class DiscountModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	/**
	 * @return the discountCode
	 */

	private Integer discountCode;
	private String discountType;
	private Double amount;
	private String name;

	public Integer getDiscountCode() {
		return discountCode;
	}

	/**
	 * @param discountCode
	 *            the discountCode to set
	 */
	public void setDiscountCode(Integer discountCode) {
		this.discountCode = discountCode;
	}

	/**
	 * @return the discountType
	 */
	public String getDiscountType() {
		return discountType;
	}

	/**
	 * @param discountType
	 *            the discountType to set
	 */
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}

	/**
	 * @return the amount
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
