package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.MailChimpObject;

@MailChimpMethod.Method(name = "/campaigns/create", version = MailChimpAPIVersion.v2_0)
public class CampaignCreateMethod extends CampaingRelatedMethod<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Field
	public CampaignType type;

	@Field
	public MailChimpObject options;

	@Field
	public MailChimpObject segment_opts;

	@Field
	public MailChimpObject content;

}