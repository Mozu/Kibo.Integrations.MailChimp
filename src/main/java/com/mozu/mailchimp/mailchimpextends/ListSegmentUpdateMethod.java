package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;

@MailChimpMethod.Method(name = "lists/segment-update", version = MailChimpAPIVersion.v2_0)
public class ListSegmentUpdateMethod extends HasListIdMethod<ListSegmentResult>  {
	private static final long serialVersionUID = 1L;
	
	@Field
	public Integer seg_id ;
	
	@Field
	public SegmentInfo opts;
	
}
