package com.mozu.mailchimp.controllers.eventprocessors;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult;
import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult.Data;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.mailchimp.controllers.MailChimpAuthController;
import com.mozu.mailchimp.mailchimpextends.MemberInfoData;
import com.mozu.mailchimp.mailchimpextends.MemberInfoResult;
import com.mozu.mailchimp.mailchimpextends.StaticSegment;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.service.CustomerSegmentSyncService;
import com.mozu.mailchimp.service.IdMappingDao;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

/**
 * This controller handles the customer related events emitted by Mailchimp.
 * Only optIn/optOut information will be updated.
 * 
 * @author Amit
 * 
 */
@Controller
public class CustomerMailchimp2MozuSyncController extends
		MailChimpAuthController {

	private static final Logger logger = LoggerFactory
			.getLogger(CustomerMailchimp2MozuSyncController.class);

	@Value("${unsub}")
	private String unsubscribe;
	
	@Value("${ApplicationId}")
	private String applicationId;

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
	@Autowired
    private IdMappingDao idMappingDao;
    
    @Autowired
    private CustomerSegmentSyncService customerSegmentSyncService;
    
	@RequestMapping(value = "/mozu/sync", method = RequestMethod.GET)
	public ResponseEntity<String> processEventRequest(
			HttpServletRequest httpRequest,
			@RequestParam(value = "tenantId", required = false) Integer tenantId) {
		logger.debug("Customer update event received from mailchimp");
		return responseEntity("", HttpStatus.OK);
	}

	/**
	 * This method captures the subscriber update event in mailchimp and
	 * processes it.
	 * 
	 * @param httpRequest
	 * @param tenantId
	 *            : tenant id of mozu where customer needs to be updated.
	 * @return
	 */

	@RequestMapping(value = "/mozu/sync", method = RequestMethod.POST)
	public ResponseEntity<String> processMailchimpSubscriber(
			HttpServletRequest httpRequest,
			@RequestParam(value = "tenantId", required = false) Integer tenantId) {
		logger.debug("Customer sync event received from mailchimp");

		try {

			ApiContext apiContext = new MozuApiContext(tenantId, null, null, null);
			MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);

			if (setting.getStatusFlag()) {
				String body = IOUtils.toString(httpRequest.getInputStream());
				logger.debug((new StringBuilder()).append("Event body: ")
						.append(body).toString());
				String event = httpRequest.getParameter("type");
				String emailId = httpRequest
						.getParameter("data[merges][EMAIL]");
				List<CustomerAccount> customerAccountList = mozuUtil
						.getMozuCustomersByEmail(apiContext, emailId);
				for (CustomerAccount customerAccount : customerAccountList) {
					if (emailId.equals(customerAccount.getEmailAddress())) {
						if (unsubscribe.equals(event)) {
							customerAccount.setAcceptsMarketing(false);
						} else {
							customerAccount.setAcceptsMarketing(true);
						}

						mozuUtil.updateMozuCustomer(apiContext,	customerAccount);
					    logger.info("Successfully updated customer "+customerAccount.getEmailAddress()+" with subscription data "+event+" from mailchimp");

						responseEntity("", HttpStatus.OK);
					}
				}
			}

		} catch (Exception e) {
			logger.error("Exception while updating opt in/out information from mailchimp to mozu: "
					+ e);
			return responseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return responseEntity("", HttpStatus.OK);
	}
}
