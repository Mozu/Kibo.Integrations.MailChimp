package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.MailChimpObject;

public abstract class CampaingRelatedMethod<R> extends MailChimpMethod<R> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@MailChimpObject.Field
	public String cid;

}