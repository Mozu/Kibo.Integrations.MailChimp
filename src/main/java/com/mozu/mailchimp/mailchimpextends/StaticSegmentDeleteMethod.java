package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;


@MailChimpMethod.Method(name = "lists/static-segment-del", version = MailChimpAPIVersion.v2_0)
public class StaticSegmentDeleteMethod extends HasListIdMethod<ListSegmentResult>  {
	private static final long serialVersionUID = 1L;
	
	@Field
	public Integer seg_id;
	
}
