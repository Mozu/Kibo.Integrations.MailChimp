package com.mozu.mailchimp.controllers.eventprocessors;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.mozu.api.ApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.EventManager;
import com.mozu.api.events.handlers.OrderEventHandler;
import com.mozu.api.events.model.EventHandlerStatus;
import com.mozu.base.handlers.EntityHandler;
import com.mozu.mailchimp.service.IdMappingDao;
import com.mozu.mailchimp.service.OrderSyncService;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

@Component
public class OrderEventHandlerImpl implements OrderEventHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(OrderEventHandlerImpl.class);

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    private MozuUtil mozuUtil;
    
    @Autowired
    private OrderSyncService orderSyncService;
    
	@PostConstruct
	public void initialize() {
		try {
			EventManager.getInstance().registerHandler(this);
		} catch (Exception e) {
			e.getMessage();
		}
		logger.info("Application event handler initialized");
	}

	@Override
	public EventHandlerStatus cancelled(ApiContext apiContext, Event event) {
		return null;
	}

	@Override
	public EventHandlerStatus closed(ApiContext apiContext, Event event) {
		EventHandlerStatus status = new EventHandlerStatus(HttpStatus.SC_OK);
		EntityHandler<Event> entityHandler = new EntityHandler<Event>(Event.class);
		String orderId = event.getEntityId();
		Event orderClosedEvent = null;
		boolean processing = false;
		try {
			orderClosedEvent= entityHandler.getEntity(apiContext.getTenantId(),IdMappingDao.EVENT_MAPPING_LIST, orderId);
			if(orderClosedEvent==null){
				entityHandler.upsertEntity(apiContext.getTenantId(), IdMappingDao.EVENT_MAPPING_LIST, orderId, event);
				Order order = mozuUtil.getOrder(event.getEntityId(), apiContext);
				processing = true;
				logger.info("Start sending closed order to mailchimp");
				orderSyncService.sendOrderToMailchimp(apiContext, order);
			}
	   } catch (Exception e) {
			logger.error(
					"Exception while sending the closed order to mailchimp, tenantID: "
							+ apiContext.getTenantId() + " Site Id : " + apiContext.getSiteId().toString(), " exception:"
							+ e);
			status = new EventHandlerStatus(e.getMessage(),
					HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		finally{
			try {
				if (processing)
					entityHandler.deleteEntity(apiContext.getTenantId(), IdMappingDao.EVENT_MAPPING_LIST, orderId);
			} catch (Exception e) {
				logger.error("Exception occured while deleting the orderClosedEvent mapping id for the order "+orderId+" from mzdb");

			}
		}

		return status;
	}

	@Override
	public EventHandlerStatus fulfilled(ApiContext apiContext, Event event) {
		return null;
	}

	@Override
	public EventHandlerStatus opened(ApiContext apiContext, Event event) {
		return null;
		
	}

	@Override
	public EventHandlerStatus pendingreview(ApiContext apiContext, Event event) {
		return null;
	}

	@Override
	public EventHandlerStatus updated(ApiContext apiContext, Event event) {
		return null;
	}

	@Override
	public EventHandlerStatus abandoned(ApiContext apiContext, Event event) {
		return null;
	}
	
	@PreDestroy
	public void cleanup() {
		EventManager.getInstance().unregisterHandler(this.getClass());
		logger.debug("Order event handler unregistered");
	}

	@Override
	public EventHandlerStatus imported(ApiContext apiContext, Event event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventHandlerStatus saved(ApiContext apiContext, Event event) {
		// TODO Auto-generated method stub
		return null;
	}

}
