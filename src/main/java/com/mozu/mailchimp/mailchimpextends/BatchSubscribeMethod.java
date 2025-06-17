package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.method.v2_0.lists.BatchSubscribeInfo;
import com.ecwid.mailchimp.method.v2_0.lists.BatchSubscribeResult;
import com.ecwid.mailchimp.method.v2_0.lists.ListsRelatedMethod;

/**
 * @author Vasily Karyaev <v.karyaev@gmail.com>
 */
@MailChimpMethod.Method(name = "lists/batch-subscribe", version = MailChimpAPIVersion.v2_0)
public class BatchSubscribeMethod extends
		ListsRelatedMethod<BatchSubscribeResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Field
	public List<BatchSubscribeInfo> batch;

	@Field
	public Boolean double_optin;

	@Field
	public Boolean update_existing;

	@Field
	public Boolean replace_interests;

}