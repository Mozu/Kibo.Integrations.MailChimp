package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;


@MailChimpMethod.Method(name = "lists/segments", version = MailChimpAPIVersion.v2_0)
public class SegmentListMethod extends HasListIdMethod<ListSegmentResult>  {
	private static final long serialVersionUID = 1L;
	
	@Field
	public String type;
}
