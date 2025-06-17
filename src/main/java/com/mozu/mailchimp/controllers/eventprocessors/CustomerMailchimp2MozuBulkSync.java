package com.mozu.mailchimp.controllers.eventprocessors;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import com.mozu.mailchimp.model.MailchimpSyncModel;
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
public class CustomerMailchimp2MozuBulkSync extends MailChimpAuthController {

	private static final Logger logger = LoggerFactory
			.getLogger(CustomerMailchimp2MozuBulkSync.class);

	@Value("${unsub}")
	private String unsubscribe;

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
	@RequestMapping(value = "/mozu/bulk/sync", method = RequestMethod.GET)
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

	@RequestMapping(value = "/mozu/bulk/sync", method = RequestMethod.POST)
	public ResponseEntity<String> processMailchimpSubscriber(
			HttpServletRequest httpRequest,
			@RequestParam(value = "tenantId", required = false) Integer tenantId) {
		logger.debug("Customer bulk sync event received from mailchimp");

		try {
			ApiContext apiContext = new MozuApiContext(tenantId, null, null, null);
			MailchimpSyncModel setting = mozuUtil.getSettings(tenantId);
			String apiKey = "";
			if (setting != null) {
				apiKey = setting.getApiKey();
			}

			if(setting.getStatusFlag())	{
			ListMethodResult result = mailChimpUtil.getAllMcList(apiKey);
			List<String> listIdList = new ArrayList<String>();
			List<String> memberListFinal = new ArrayList<String>();
			for (Data data : result.data) {
				listIdList.add(data.id);
				List<String> meberInfo = mailChimpUtil.getSubscibedCustomer(
						apiKey, data.id);
				memberListFinal.addAll(meberInfo);
			}

			for (String member : memberListFinal) {
				List<CustomerAccount> listCustomerAccount = mozuUtil
						.getMozuCustomersByEmail(apiContext, member);

				for (CustomerAccount customer : listCustomerAccount) {
					mozuUtil.updateMozuCustomer(apiContext, customer);
				}
			}
			}
			responseEntity("", HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Exception while updating opt in/out information from mailchimp to mozu: "
					+ e);
		}

		return responseEntity("", HttpStatus.OK);
	}
}
