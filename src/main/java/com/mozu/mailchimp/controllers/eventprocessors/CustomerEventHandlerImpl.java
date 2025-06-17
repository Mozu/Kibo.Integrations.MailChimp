package com.mozu.mailchimp.controllers.eventprocessors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.EventManager;
import com.mozu.api.events.handlers.CustomerAccountEventHandler;
import com.mozu.api.events.model.EventHandlerStatus;
import com.mozu.base.handlers.EntityHandler;
import com.mozu.mailchimp.service.CustomerSyncService;
import com.mozu.mailchimp.service.IdMappingDao;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

/**
 * This controller handles the customer related events emitted by Mozu eg
 * customer update, create.
 * 
 * @author Amit
 * 
 */
@Component
public class CustomerEventHandlerImpl implements CustomerAccountEventHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(CustomerEventHandlerImpl.class);

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Autowired
	private CustomerSyncService customerSyncService;
    
    /**
	 * This method registers the controller with Eventmanager.
	 */
	@PostConstruct
	public void initialize() {
			EventManager.getInstance().registerHandler(this);
			logger.info("Customer event handler initialized");
	}

	/**
	 * This method handles the customer created event.
	 */
	@Override
	public EventHandlerStatus created(ApiContext apiContext, Event event) {
		logger.info("Inside customer create event");
		try {
			String customerId = event.getEntityId();
			CustomerAccount customer = mozuUtil.getMozuCustomer(apiContext,
					Integer.parseInt(customerId));
			customerSyncService.sendCustomerToMailchimp(apiContext, customer);
		} 
		catch (Exception e) {
			String msg= String.format("Unable to create customer in mailchimp from event for customer %s, correlationid %s and tenant id %s - Exception %s", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId(),e.getMessage());
		    logger.warn(msg);
		    return new EventHandlerStatus(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		return new EventHandlerStatus(HttpStatus.SC_OK);
	}

	@Override
	public EventHandlerStatus deleted(ApiContext apiContext, Event event) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method handles the customer update event.
	 */
	@Override
	public EventHandlerStatus updated(ApiContext apiContext, Event event) {
		
		EntityHandler<Event> entityHandler = new EntityHandler<Event>(Event.class);
		String customerId = event.getEntityId();
		Event customerUpdateEvent = null;
		boolean processing = false;
		try {
			customerUpdateEvent= entityHandler.getEntity(apiContext.getTenantId(),IdMappingDao.EVENT_MAPPING_LIST, customerId);
			if(customerUpdateEvent==null){
				entityHandler.upsertEntity(apiContext.getTenantId(), IdMappingDao.EVENT_MAPPING_LIST, customerId, event);
				processing = true;
				logger.info("Inside customer update event "+apiContext.getSiteId() +"     "+event.getCorrelationId());
				CustomerAccount customer = mozuUtil.getMozuCustomer(apiContext,
						Integer.parseInt(customerId));
				customerSyncService.sendCustomerToMailchimp(apiContext, customer);
			}
		}
			catch (Exception e) {
				String msg= String.format("Unable to update customer in mailchimp from event for customer %s, correlationid %s and tenant id %s - Exception %s", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId(),e.getMessage());
			    logger.warn(msg);
			    return new EventHandlerStatus(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
		finally{
			try {
				
				if (processing){
					entityHandler.deleteEntity(apiContext.getTenantId(), IdMappingDao.EVENT_MAPPING_LIST, customerId);
				}
			} catch (Exception e) {
				logger.error("Exception occured while deleting the customerUpdateEvent mapping id for the customer "+customerId+" from mzdb "+e);

			}
		}


		return new EventHandlerStatus(HttpStatus.SC_OK);
	}
	
	@PreDestroy
	public void cleanup() {
		EventManager.getInstance().unregisterHandler(this.getClass());
		logger.debug("Customer event handler unregistered");
	}

}
