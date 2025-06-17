package com.mozu.mailchimp.mailchimpextends;

import java.util.List;

import com.ecwid.mailchimp.MailChimpObject;


public class ListMergeVarsResult extends MailChimpObject {

	private static final long serialVersionUID = 1L; 
	
	@Field
	public Integer success_count;
	
	@Field
	public Integer error_count;
	
	@Field
	public List<MergeTags> data;
	
	@Field
	public List<Error> errors;

	public static class MergeTags extends MailChimpObject {
		@Field
		public String id;
		
		@Field
		public String name;
		
		@Field
		public List<MergeVarInfo> merge_vars;
	}
	
	public static class Error extends MailChimpObject {
        @Field
        public String id;

        @Field
        public Integer code;

        @Field
        public String msg;
    }

}
