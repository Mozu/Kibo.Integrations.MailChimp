package com.mozu.mailchimp.util;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.sitesettings.application.Application;
import com.mozu.api.events.model.EventHandlerStatus;
import com.mozu.api.resources.commerce.settings.ApplicationResource;
import com.mozu.mailchimp.model.MailchimpSyncModel;

@Component
public class ApplicationUtils extends com.mozu.base.utils.ApplicationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationUtils.class);
      
    @Autowired
    protected MozuUtil mozuUtil;
    
    public EventHandlerStatus setAppInitializeStatus(ApiContext apiContext) {
        EventHandlerStatus status = null;
        
        logger.debug("Set application initialization state for tenant " + apiContext.getTenantId());
        
        // Only set initialized if there are valid values in the settings
        try {
        	MailchimpSyncModel settings = mozuUtil.getSettings(apiContext.getTenantId());
            if (settings!=null) {
                logger.debug("tenant settings retrieved");

                // Only enable if configuration data is valid
                if (settings.getStatusFlag() && settings.getMcListId()!=null) {

                    try {
                        ApplicationUtils.setApplicationToInitialized(apiContext);
                        status = new EventHandlerStatus(HttpStatus.SC_OK);
                    } catch (Exception e) {
                        logger.warn("Exception intializing application: " + e.getMessage());
                        status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                } else {
                    try {
                        setApplicationToUninitialized(apiContext);
                        status = new EventHandlerStatus(HttpStatus.SC_OK);
                    } catch (Exception e) {
                        logger.warn("Exception unintializing application: " + e.getMessage());
                        status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                }
            }
        } catch (Exception e) {
            status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return status;
    }

    public static final Application setApplicationToUninitialized(ApiContext apiContext) throws Exception {
        ApplicationResource appResource = new ApplicationResource(apiContext);
        Application application = getApplication(appResource);

        // Set the app to uninitialized
        application.setInitialized(false);
        
        try {
            appResource.thirdPartyUpdateApplication(application);
            logger.debug("Application updated");
        } catch (Exception e) {
            logger.warn("Exception updating application: " + e.getMessage());
            throw new Exception("Exception updating application: " + e.getMessage());
        }
        return application;
    }
    
}
