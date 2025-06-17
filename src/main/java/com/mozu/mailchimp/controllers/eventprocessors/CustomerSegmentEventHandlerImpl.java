package com.mozu.mailchimp.controllers.eventprocessors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.EventManager;
import com.mozu.api.events.handlers.CustomerSegmentEventHandler;
import com.mozu.api.events.model.EventHandlerStatus;
import com.mozu.base.handlers.EntityHandler;
import com.mozu.mailchimp.service.CustomerSegmentSyncService;
import com.mozu.mailchimp.service.IdMappingDao;
import com.mozu.mailchimp.util.MozuUtil;

@Component
public class CustomerSegmentEventHandlerImpl implements CustomerSegmentEventHandler{

	private static final Logger logger = LoggerFactory.getLogger(CustomerSegmentEventHandlerImpl.class);
	
	@Autowired
	CustomerSegmentSyncService customerSegmentSyncService;
	 
	@Autowired
	MozuUtil mozuUtil;
	
	
	@PostConstruct
    public void initialize() {
        EventManager.getInstance().registerHandler(this);
        logger.info("CustomerSegment event handler initialized");
    }

	@Override
	public EventHandlerStatus created(ApiContext apiContext, Event event) {
		logger.info("Start creating Customer Segment");
		try {
			customerSegmentSyncService.sendSegmentToMailchimp(apiContext, event.getEntityId());
		} catch (Exception e) {
			String msg= String.format("Unable to create segment in mailchimp from event for segment %s, correlationid %s and tenant id %s - Exception %s", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId(),e.getMessage());
		    logger.warn(msg);
		    return new EventHandlerStatus(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		return new EventHandlerStatus(HttpStatus.SC_OK);
	}

	@Override
	public EventHandlerStatus deleted(ApiContext apiContext, Event event) {
		logger.info("Start Customer Segment Delete");
		try {
			customerSegmentSyncService.deleteStaticSegmentFromMailchimp(apiContext, event.getEntityId());
		} catch (Exception e) {
			String msg= String.format("Unable to delete segment in mailchimp from event for segment %s, correlationid %s and tenant id %s - Exception %s", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId(),e.getMessage());
		    logger.warn(msg);
		    return new EventHandlerStatus(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		return new EventHandlerStatus(HttpStatus.SC_OK);
	}

	@Override
	public EventHandlerStatus updated(ApiContext apiContext, Event event) {
		
		EntityHandler<Event> entityHandler = new EntityHandler<Event>(Event.class);
		String segmentId = event.getEntityId();
		Event segmentUpdateEvent = null;
		boolean processing = false;
		try {
			segmentUpdateEvent= entityHandler.getEntity(apiContext.getTenantId(),IdMappingDao.EVENT_MAPPING_LIST, segmentId);
			if(segmentUpdateEvent==null){
				entityHandler.upsertEntity(apiContext.getTenantId(), IdMappingDao.EVENT_MAPPING_LIST, segmentId, event);
				processing = true;
				logger.info("Inside Customer Segment update event");
				customerSegmentSyncService.sendSegmentToMailchimp(apiContext, segmentId);
			}
			} catch (Exception e) {
				String msg= String.format("Unable to update segment in mailchimp from event for segment %s, correlationid %s and tenant id %s - Exception %s", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId(),e.getMessage());
				logger.warn(msg);
				return new EventHandlerStatus(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}finally{
			try {
				if (processing){
					entityHandler.deleteEntity(apiContext.getTenantId(), IdMappingDao.EVENT_MAPPING_LIST, segmentId);
				}
			} catch (Exception e) {
				logger.error("Exception occured while deleting the segmentUpdateEvent mapping id for the segment "+segmentId+" from mzdb "+e);

			}
		}
		return new EventHandlerStatus(HttpStatus.SC_OK);
	}
	
	 @PreDestroy
	 public void cleanup() {
	        EventManager.getInstance().unregisterHandler(this.getClass());
	        logger.debug("CustomerSegment event handler unregistered");
	    }

	@Override
	public EventHandlerStatus customeradded(ApiContext apiContext, Event event) {
		logger.info("Customer Segment:Inside  customeradded event");
		if(!CollectionUtils.isEmpty(event.getExtendedProperties())){
			Integer customerId=Integer.parseInt(event.getExtendedProperties().get(0).getValue().toString());
			// Add only registered and opted in customers to segment
			CustomerAccount customer=mozuUtil.getMozuCustomer(apiContext,customerId );
			if(customer.getAcceptsMarketing() != null
					&& customer.getAcceptsMarketing()){
				try {
					customerSegmentSyncService.addCustomersToMailchimpSegment(apiContext.getTenantId(),event.getEntityId(),customer.getEmailAddress());
				}
				catch (Exception e) {
					String msg= String.format("Unable to add customer to segment in mailchimp from event for segment %s, correlationid %s and tenant id %s - Exception %s", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId(),e.getMessage());
					logger.warn(msg);
					return new EventHandlerStatus(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}
			}
		}else{
			String msg= String.format("Insufficient data to add customer to segment in mailchimp from event for segment %s, correlationid %s and tenant id %s . Mozu Event is not sending the customerId data ", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId());
			logger.debug(msg);
		}
	return new EventHandlerStatus(HttpStatus.SC_OK);
		
	}

	@Override
	public EventHandlerStatus customerremoved(ApiContext apiContext, Event event) {
		logger.info("Customer Segment:Inside  customerremoved event");
		if(!CollectionUtils.isEmpty(event.getExtendedProperties())){
			Integer customerId=Integer.parseInt(event.getExtendedProperties().get(0).getValue().toString());
			CustomerAccount customer=mozuUtil.getMozuCustomer(apiContext,customerId );
			try {
				customerSegmentSyncService.removeCustomersFromMailchimpSegment(apiContext.getTenantId(),event.getEntityId(),customer.getEmailAddress());
				} catch (Exception e) {
					String msg= String.format("Unable to remove customer from segment in mailchimp from event for segment %s, correlationid %s and tenant id %s - Exception %s", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId(),e.getMessage());
				    logger.warn(msg);
				    return new EventHandlerStatus(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}
		}else{
			String msg= String.format("Insufficient data to remove customer from segment in mailchimp from event for segment %s, correlationid %s and tenant id %s . Mozu Event is not sending the customerId data ", event.getEntityId(), event.getCorrelationId(), apiContext.getTenantId());
			logger.debug(msg);
		}
		
			return new EventHandlerStatus(HttpStatus.SC_OK);
	}
	
}


