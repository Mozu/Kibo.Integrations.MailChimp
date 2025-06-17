package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;

/**

 * 
 * @author Benjamin Warncke
 */
@MailChimpMethod.Method(name = "/lists/webhooks", version = MailChimpAPIVersion.v2_0)
public class WebhookListMethod extends MailChimpMethod<Object> {

	private static final long serialVersionUID = 1L;

	
	@Field
	public String id;

	
}