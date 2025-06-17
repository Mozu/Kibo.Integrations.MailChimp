package com.mozu.mailchimp.model;

import java.io.Serializable;

/**
 * this class hold the mapping between list and site.
 * 
 * @author Amit
 * 
 */
public class ListSiteMapModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String listId;
	private String listName;
	private String siteId;
	private String siteName;

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

}
