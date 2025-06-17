package com.mozu.mailchimp.exception;
/**
 * Custom Error code and message .
 * 
 * @author Amit
 *
 */

public final class ErrorCode {

	public static final String EX_MSG_100 = "Exception while connecting to Mailchimp";
	public static final String EX_MSG_200 = "Exception while connecting to Mozu";
	public static final String EX_MSG_300 = "Exception while updating data in Mailchimp";
	public static final String EX_MSG_400 = "Exception while processing data in Mozu";
	public static final String EX_MSG_500 = "Exception while processing data in spring batch";

	public static final Integer EX_CD_100 = 100;
	public static final Integer EX_CD_200 = 200;
	public static final Integer EX_CD_300 = 300;
	public static final Integer EX_CD_400 = 400;
	public static final Integer EX_CD_500 = 500;
}
