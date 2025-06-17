package com.mozu.mailchimp.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecwid.mailchimp.MailChimpClient;
import com.ecwid.mailchimp.MailChimpException;
import com.ecwid.mailchimp.MailChimpMethod;

@Service
public class MozuMailChimpClient {
    private static final Logger logger = LoggerFactory.getLogger(MozuMailChimpClient.class);

    private MailChimpClient mailChimpClient = null;

    @Autowired
    MozuConnectionManager connectionManager;
    
    @PostConstruct
    public void intialize() {
        logger.debug("Initialize MailChimpClient");
        mailChimpClient = new MailChimpClient(connectionManager);
    }
    
    public <T> T execute(MailChimpMethod<T> method ) throws IOException, MailChimpException {
        return mailChimpClient.execute(method);
    }
}
