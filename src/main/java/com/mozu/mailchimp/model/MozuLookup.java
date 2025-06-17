package com.mozu.mailchimp.model;

import java.io.Serializable;

/**
 * This is look up table entity.
 * 
 * @author Amit
 * 
 * 
 */
public class MozuLookup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer srNo;
	private String key;
	private String value;
	private String type;

	public MozuLookup() {
	}

	public MozuLookup(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the srNo
	 */
	public Integer getSrNo() {
		return srNo;
	}

	/**
	 * @param srNo
	 *            the srNo to set
	 */
	public void setSrNo(Integer srNo) {
		this.srNo = srNo;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
