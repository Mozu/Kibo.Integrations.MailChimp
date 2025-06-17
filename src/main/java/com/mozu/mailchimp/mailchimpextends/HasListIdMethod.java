package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;

@MailChimpMethod.Method(name = "HasListIdMethod", version = MailChimpAPIVersion.v2_0)
public class HasListIdMethod<R> extends MailChimpMethod<R> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Field
	public String id;

}
