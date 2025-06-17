package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpObject;

public class SegmentInfo extends MailChimpObject{

	private static final long serialVersionUID = 1L;
	
	@Field
	public String type;
	
	@Field
	public String name;
	
	@Field
	public SegmentOption segment_opts;
	

}
