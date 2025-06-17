package com.mozu.mailchimp.model;

import java.io.Serializable;

/**
 * This class will be used in MozuBatchPojo
 * 
 * @author Amit
 * 
 */
public class BatchModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String syncDirection;

	private String batchStatus;

	private String createdBy;

	private String createdDate;

	private String updatedBy;

	private String updatedDate;

	private Integer errorCd;

	private String errorMsg;

	private String listId;

	private String listName;

	private Integer errorCount;

	private Long jobExecutionId;
	
	private Integer recordCount;

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
	 * @return the batchStatus
	 */
	public String getBatchStatus() {
		return batchStatus;
	}

	/**
	 * @param batchStatus
	 *            the batchStatus to set
	 */
	public void setBatchStatus(String batchStatus) {
		this.batchStatus = batchStatus;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy
	 *            the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the createdDate
	 */
	public String getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy
	 *            the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * @return the updatedDate
	 */
	public String getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * @param updatedDate
	 *            the updatedDate to set
	 */
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	/**
	 * @return the errorCd
	 */
	public Integer getErrorCd() {
		return errorCd;
	}

	/**
	 * @param errorCd
	 *            the errorCd to set
	 */
	public void setErrorCd(Integer errorCd) {
		this.errorCd = errorCd;
	}

	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * @param errorMsg
	 *            the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * @return the listId
	 */
	public String getListId() {
		return listId;
	}

	/**
	 * @param listId
	 *            the listId to set
	 */
	public void setListId(String listId) {
		this.listId = listId;
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
	 * @return the errorCount
	 */
	public Integer getErrorCount() {
		return errorCount;
	}

	/**
	 * @param errorCount
	 *            the errorCount to set
	 */
	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}

	/**
	 * @return the jobExecutionId
	 */
	public Long getJobExecutionId() {
		return jobExecutionId;
	}

	/**
	 * @param jobExecutionId
	 *            the jobExecutionId to set
	 */
	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

	
}
