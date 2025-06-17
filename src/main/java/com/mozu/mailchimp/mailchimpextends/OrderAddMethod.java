package com.mozu.mailchimp.mailchimpextends;

import com.ecwid.mailchimp.MailChimpAPIVersion;
import com.ecwid.mailchimp.MailChimpMethod;
import com.ecwid.mailchimp.method.v2_0.lists.DummyResult;

@MailChimpMethod.Method(name = "ecomm/order-add", version = MailChimpAPIVersion.v2_0)
public class OrderAddMethod extends MailChimpMethod<DummyResult> {
	private static final long serialVersionUID = 1L;
	@Field
	public OrderInfo order;
}
