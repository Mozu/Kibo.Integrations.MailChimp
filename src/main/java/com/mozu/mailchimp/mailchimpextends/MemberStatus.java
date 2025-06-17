package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;

@MailChimpMethod.Method(name = "lists/batch-subscribe", version = MailChimpAPIVersion.v2_0)
public enum MemberStatus {
	subscribed, unsubscribed, cleaned, pending, updated;
}