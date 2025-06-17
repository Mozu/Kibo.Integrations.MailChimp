package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.method.v2_0.lists.Email;
import com.ecwid.mailchimp.method.v2_0.lists.ListsRelatedMethod;


@MailChimpMethod.Method(name = "/lists/member-info", version = MailChimpAPIVersion.v2_0)
public class ListSingleMemberInfoMethod extends
		ListsRelatedMethod<MemberInfoResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Field
	public List<Email> emails;

}
