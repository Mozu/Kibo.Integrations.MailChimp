package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;




@MailChimpMethod.Method(name = "/lists/merge-vars", version = MailChimpAPIVersion.v2_0)
public class ListMergeVarsMethod extends MailChimpMethod<ListMergeVarsResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Field
	public List<String> id;
	
}


