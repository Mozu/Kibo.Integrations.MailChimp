package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpObject;

public class StaticSegment extends MailChimpObject{

	private static final long serialVersionUID = 1L;
	
	@Field
	public Integer id;
	
	@Field
	public String name;
	
	@Field
	public String added;
	

}
