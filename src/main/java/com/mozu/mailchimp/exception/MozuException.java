/**
 * 
 */
package com.mozu.mailchimp.exception;

/**
 * @author Akshay
 * 
 */
public class MozuException extends Exception {

	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = -1898202572481865096L;

	private Integer errorCode;

	private String errorMessage;

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String getMessage() {
		return "Exception while connecting to Mozu";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
