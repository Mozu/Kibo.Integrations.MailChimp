package com.mozu.mailchimp.mailchimpextends;


import com.ecwid.mailchimp.MailChimpObject;
import com.ecwid.mailchimp.method.v2_0.lists.MemberInfoError;

import java.util.List;


public class MemberInfoResult extends MailChimpObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Field
	public Integer success_count;
	
	@Field
	public Integer error_count;
	
	@Field
	public List<MemberInfoError> errors;
	
	@Field
	public List<MemberInfoData> data;
}
