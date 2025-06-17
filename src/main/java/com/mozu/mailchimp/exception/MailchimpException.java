/**
 * 
 */
package com.mozu.mailchimp.exception;

/**
 * A holder class to contain error related details.
 * 
 * @author Akshay
 * 
 */
public class MailchimpException extends Exception {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {	
		return "Exception while connecting to Mailchimp";
	}
}
