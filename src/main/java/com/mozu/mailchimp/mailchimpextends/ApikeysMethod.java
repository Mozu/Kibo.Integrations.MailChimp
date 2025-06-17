package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.method.v1_3.security.ApikeysResult;
import com.ecwid.mailchimp.method.v1_3.security.HasUsernameAndPasswordMethod;

@MailChimpMethod.Method(name = "apikeys", version = MailChimpAPIVersion.v2_0)
public class ApikeysMethod extends HasUsernameAndPasswordMethod<ApikeysResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Field
	public Boolean expired;
}
