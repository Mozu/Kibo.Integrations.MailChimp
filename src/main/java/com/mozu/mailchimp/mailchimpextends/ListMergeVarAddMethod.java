package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;

@MailChimpMethod.Method(name = "/lists/merge-var-add", version = MailChimpAPIVersion.v2_0)
public class ListMergeVarAddMethod extends HasListIdMethod<Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Field
	public String name;

	@Field
	public MergeVarInfo options;

	@Field
	public String tag;
}
