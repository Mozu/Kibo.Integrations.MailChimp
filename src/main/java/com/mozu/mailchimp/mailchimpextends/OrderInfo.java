package com.mozu.mailchimp.mailchimpextends;

import java.util.Date;
import java.util.List;

import com.ecwid.mailchimp.MailChimpObject;
import com.ecwid.mailchimp.method.v2_0.ecomm.OrderItemInfo;

public class OrderInfo extends MailChimpObject {
	private static final long serialVersionUID = 1L;

	@Field
	public String id;

	@Field
	public String campaign_id;

	@Field
	public String email_id;

	@Field
	public String email;

	@Field
	public Double total;

	@Field
	public Date order_date;

	@Field
	public Double shipping;

	@Field
	public Double tax;

	@Field
	public String store_id;

	@Field
	public String store_name;

	@Field
	public List<OrderItemInfo> items;

}
