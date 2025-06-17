package com.mozu.mailchimp.controllers.eventprocessors.application;

import javax.annotation.PostConstruct;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.EventManager;
import com.mozu.api.events.handlers.ApplicationEventHandler;
import com.mozu.api.events.model.EventHandlerStatus;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.service.IdMappingDao;
import com.mozu.mailchimp.service.MozuMcClientSetupService;
import com.mozu.mailchimp.util.ApplicationUtils;
import com.mozu.mailchimp.util.MozuUtil;
/**
 * This class handles app events.
 * 
 * @author Amit
 *
 */
@Controller
public class AppEnableController implements ApplicationEventHandler {
	private static final Logger log = LoggerFactory
			.getLogger(AppEnableController.class);

	@Autowired
	private MozuMcClientSetupService mozuMcClientSetupService;

    @Autowired
    private MozuUtil mozuUtil;
    
    
    @Autowired
    private IdMappingDao idMappingDao;
    
  
    @Autowired
    private ApplicationUtils applicationUtils;
    
   	@PostConstruct
	public void initialize() {
		try {
			EventManager.getInstance().registerHandler(this);
		} catch (Exception e) {
			e.getMessage();
		}
		log.info("AppEnableController event handler initialized");
	}

	@Override
	public EventHandlerStatus disabled(ApiContext apiContext, Event event) {

		log.debug("Recieved App Enable event ");
		EventHandlerStatus status = new EventHandlerStatus(HttpStatus.SC_OK);
		Integer tenantId = apiContext.getTenantId();

		try {
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(
					(tenantId));
			mailchimpSetting.setStatusFlag(false);
			mozuUtil.saveSettings(tenantId, mailchimpSetting);
		}  catch (Exception e) {
				log.debug("Exception in App enable event handler for tenant ID "+tenantId);
		}
		return status;
	
	}

	@Override
	public EventHandlerStatus enabled(ApiContext apiContext, Event event) {
		// TODO Auto-generated method stub
		log.debug("Recieved App Enable event ");
		EventHandlerStatus status = new EventHandlerStatus(HttpStatus.SC_OK);
		Integer tenantId = apiContext.getTenantId();

		try {
			MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(
					(tenantId));
			mailchimpSetting.setStatusFlag(true);
			mozuUtil.saveSettings(tenantId, mailchimpSetting);
		}  catch (Exception e) {
				log.debug("Exception in App enable event handler for tenant ID "+tenantId);
		}
		return status;	}

	@Override
	public EventHandlerStatus installed(ApiContext apiContext, Event event) {
		log.debug("Application installed event");
	        EventHandlerStatus status;
	        try {
	            idMappingDao.installSchema(apiContext.getTenantId());
	            idMappingDao.installEventSchema(apiContext);
	            idMappingDao.installEcommerce360Schema(apiContext);
	            status = new EventHandlerStatus(HttpStatus.SC_OK);
	        } catch (Exception e) {
	        	log.error("Exception during installation: " + e.getMessage());
	            status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
	        }
	        return status;
	}

	@Override
	public EventHandlerStatus uninstalled(ApiContext apiContext, Event event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventHandlerStatus upgraded(ApiContext apiContext, Event event) {
		log.debug("Application upgraded event");
        EventHandlerStatus status = applicationUtils.setAppInitializeStatus(apiContext);
        try {
            idMappingDao.installSchema(apiContext.getTenantId());
            idMappingDao.installEventSchema(apiContext);
            idMappingDao.installEcommerce360Schema(apiContext);
            } catch (Exception e) {
        	log.error("Exception during applicaiton upgrade: " + e.getMessage());
            status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return status;
	}
}
