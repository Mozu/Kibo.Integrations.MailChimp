package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.method.v1_3.campaign.CampaingRelatedMethod;

@MailChimpMethod.Method(name = "/campaigns/send", version = MailChimpAPIVersion.v2_0)
public class CampaignSendNowMethod extends CampaingRelatedMethod<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}