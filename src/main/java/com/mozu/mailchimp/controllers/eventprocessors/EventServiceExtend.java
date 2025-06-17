package com.mozu.mailchimp.controllers.eventprocessors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.model.EventHandlerStatus;
import com.mozu.api.events.service.EventService;

public class EventServiceExtend extends EventService{

	private static final Logger logger = LoggerFactory.getLogger(EventService.class);
	private static ObjectMapper mapper = new ObjectMapper();
    
	public EventHandlerStatus dispatchEvent(HttpServletRequest httpRequest) {
        ApiContext apiContext = new MozuApiContext(httpRequest);
        Event event = null;

        // get the event from the request and validate
        try {
            String body = IOUtils.toString(httpRequest.getInputStream());
            logger.debug("Event body: " + body);
            event = mapper.readValue(body, Event.class);
            logger.info("Dispatching Event.  Correlation ID: " + event.getCorrelationId());
         
        } catch (IOException exception) {
            StringBuilder msg = new StringBuilder ("Unable to read product update event: ").append(exception.getMessage());
            logger.error(msg.toString());
            return( new EventHandlerStatus(msg.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR));
        }

        try {
            invokeHandler(event, apiContext);
        } catch (Exception exception) {
            StringBuilder msg = new StringBuilder ("Unable to process product update event: ").append(exception.getMessage());
            logger.error(msg.toString());
            return( new EventHandlerStatus(msg.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR));
        }
        return( new EventHandlerStatus(null, HttpStatus.SC_OK));
    }
}
