package com.mozu.mailchimp.mailchimpextends;

import java.util.Date;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.MailChimpObject;

@MailChimpMethod.Method(name = "MemberInfo", version = MailChimpAPIVersion.v2_0)
public class MemberInfo extends MailChimpObject {
	private static final long serialVersionUID = 1L;
	@Field
	public String campaign_id;
	@Field
	public String email;
	@Field
	public String email_address;
	@Field
	public EmailType email_type;
	@Field
	public String error;
	@Field
	public String id;
	@Field
	public Date info_changed;
	@Field
	public String ip_opt;
	@Field
	public String ip_signup;
	@Field
	public Boolean is_gmonkey;
	@Field
	public Integer member_rating;
	@Field
	public MailChimpObject merges;
	@Field
	public MemberStatus status;
	@Field
	public Date timestamp;
	@Field
	public Date timestamp_opt;
	@Field
	public Date timestamp_signup;
	@Field
	public Integer web_id;

}
