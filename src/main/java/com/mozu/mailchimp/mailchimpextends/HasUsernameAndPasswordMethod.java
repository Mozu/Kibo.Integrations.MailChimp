package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpMethod;

public abstract class HasUsernameAndPasswordMethod<R> extends
		MailChimpMethod<R> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Field
	public String username, password;
}
