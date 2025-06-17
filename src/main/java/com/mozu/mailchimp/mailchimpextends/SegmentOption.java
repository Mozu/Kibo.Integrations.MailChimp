package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpObject;

public class SegmentOption extends MailChimpObject{

	private static final long serialVersionUID = 1L;
	
	@Field
	public String match;
	
	@Field
	public List<String> conditions;
	
}
