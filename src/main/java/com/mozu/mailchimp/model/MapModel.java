package com.mozu.mailchimp.model;

import java.io.Serializable;

public class MapModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String keyStr;
	private String valStr;

	public String getKeyStr() {
		return keyStr;
	}

	public void setKeyStr(String keyStr) {
		this.keyStr = keyStr;
	}

	public String getValStr() {
		return valStr;
	}

	public void setValStr(String valStr) {
		this.valStr = valStr;
	}

}
