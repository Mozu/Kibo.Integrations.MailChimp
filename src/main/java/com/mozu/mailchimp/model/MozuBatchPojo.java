package com.mozu.mailchimp.model;

import java.io.Serializable;
import java.util.List;

/**
 * This class is used for saving batch entry data in MZDB
 * 
 * @author Amit
 * 
 */
public class MozuBatchPojo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	private List<BatchModel> batchModelList;

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
	 * @return the batchModelList
	 */
	public List<BatchModel> getBatchModelList() {
		return batchModelList;
	}

	/**
	 * @param batchModelList
	 *            the batchModelList to set
	 */
	public void setBatchModelList(List<BatchModel> batchModelList) {
		this.batchModelList = batchModelList;
	}

}
