package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpObject;


/**
 *
 * @author Vasily Karyaev <v.karyaev@gmail.com>
 */
public class MergeVarInfo extends MailChimpObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Not used in {@link ListMergeVarAddMethod} requests.
	 */
	@Field
	public String tag;

	/**
	 * Not used in {@link ListMergeVarAddMethod} requests.
	 */
	@Field
	public String name;

	@Field
	public Boolean req;

	@Field
	public MergeVarType field_type;

	@Field(name="public")
	public Boolean public_;

	@Field
	public Boolean show;

	@Field
	public Integer order;

	@Field(name="default")
	public String default_;

	@Field
	public Integer size;

	@Field
	public List<String> choices;

	/**
	 * Not set in responses.
	 */
	@Field
	public String dateformat;

	/**
	 * Not set in responses.
	 */
	@Field
	public String phoneformat;

	/**
	 * Not set in responses.
	 */
	@Field
	public String defaultcountry;
}
