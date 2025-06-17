package com.mozu.mailchimp.exception;

public class MailchimpDuplicateMappingException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public MailchimpDuplicateMappingException(String msg) {
        super(msg);
    }

}
