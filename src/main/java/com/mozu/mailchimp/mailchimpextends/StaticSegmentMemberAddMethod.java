package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.method.v2_0.lists.Email;

@MailChimpMethod.Method(name = "lists/static-segment-members-add", version = MailChimpAPIVersion.v2_0)
public class StaticSegmentMemberAddMethod extends HasListIdMethod<ListSegmentResult>  {
	private static final long serialVersionUID = 1L;
	
	@Field
	public Integer seg_id;
	
	@Field
	public List<Email> batch;
}
