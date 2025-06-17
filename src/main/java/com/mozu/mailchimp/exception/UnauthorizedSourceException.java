/**
 * 
 */
package com.mozu.mailchimp.exception;

/**
 * @author Akshay
 * 
 */
public class UnauthorizedSourceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4976808939161336851L;

	public UnauthorizedSourceException() {
	}

	public UnauthorizedSourceException(String message) {
		super(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return super.getMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	@Override
	public String getLocalizedMessage() {
		return super.getLocalizedMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}

}
