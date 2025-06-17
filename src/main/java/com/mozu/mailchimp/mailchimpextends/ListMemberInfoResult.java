package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.MailChimpObject;

@MailChimpMethod.Method(name = "ListMemberInfoResult", version = MailChimpAPIVersion.v2_0)
public class ListMemberInfoResult extends MailChimpObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Field
	public List<MemberInfo> data;
	@Field
	public Integer errors;
	@Field
	public Integer success;

}
