package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;

@MailChimpMethod.Method(name = "lists/segment-add", version = MailChimpAPIVersion.v2_0)
public class ListSegmentAddMethod extends HasListIdMethod<ListSegmentResult>  {
	private static final long serialVersionUID = 1L;
	
	@Field
	public SegmentInfo opts;
	
}
