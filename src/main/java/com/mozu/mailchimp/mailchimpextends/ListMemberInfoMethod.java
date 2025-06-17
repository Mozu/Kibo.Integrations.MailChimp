package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;

@MailChimpMethod.Method(name = "/lists/members", version = MailChimpAPIVersion.v2_0)
public class ListMemberInfoMethod extends HasListIdMethod<ListMemberInfoResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Field
	public List<String> email_address;
	
	@Field
	public String status;

}
