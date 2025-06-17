package com.mozu.mailchimp.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mozu.api.ApiContext;
import com.mozu.api.ApiError;
import com.mozu.api.ApiException;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.commerceruntime.orders.OrderCollection;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerAccountCollection;
import com.mozu.api.contracts.productruntime.Category;
import com.mozu.api.contracts.sitesettings.application.Application;
import com.mozu.api.contracts.tenant.Tenant;
import com.mozu.api.resources.commerce.OrderResource;
import com.mozu.api.resources.commerce.catalog.storefront.CategoryResource;
import com.mozu.api.resources.commerce.customer.CustomerAccountResource;
import com.mozu.api.resources.platform.TenantDataResource;
import com.mozu.api.resources.platform.TenantResource;
import com.mozu.mailchimp.exception.ErrorCode;
import com.mozu.mailchimp.exception.MozuException;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.model.MozuBatchPojo;

/**
 * Mozu Utility Class to make Mozu SDK and Mailchimp API connections
 * 
 * @author Amit
 * 
 */
@Component
public class MozuUtil {

	@Value("${ApplicationId}")
	private String ApplicationId;

	private static final Logger logger = LoggerFactory
			.getLogger(MailchimpUtil.class);

	/**
	 * This method gets the order object.
	 * 
	 * @param orderId
	 * @param apiContext
	 * @return
	 * @throws Exception
	 */
	public Order getOrder(String orderId, ApiContext apiContext)
			throws Exception {
		OrderResource orderResource = new OrderResource(apiContext);
		return orderResource.getOrder(orderId, true,null, null);
	}
	
	/**
	 * This method gets the order object.
	 * 
	 * @param orderId
	 * @param apiContext
	 * @return
	 * @throws Exception
	 */
	public List<Order> getOrders(ApiContext apiContext,Integer startIndex, Integer pageSize, String filter)
			throws Exception {
		OrderResource orderResource = new OrderResource(apiContext);
		OrderCollection orderCollection=null;
		try{
		  orderCollection =orderResource.getOrders(startIndex, pageSize, null, filter, null, null, null, null);
		}catch (Exception e) {
			StringBuilder msg = new StringBuilder(
					"Exception occurred while getting order for tenant ")
					.append(apiContext.getTenantId()).append(": ")
					.append(e.getMessage());
			logger.error(msg.toString(), e);
			throw new RuntimeException(msg.toString());
		}
		return orderCollection.getItems();
	}

	/**
	 * This method gets the CustomerAccount object using ID.
	 * 
	 * @param apiContext
	 * @param customerId
	 * @return
	 */
	public CustomerAccount getMozuCustomer(ApiContext apiContext,
			int customerId) {
		CustomerAccountResource customerResource = new CustomerAccountResource(
				apiContext);
		CustomerAccount account = null;
		try {
			account = customerResource.getAccount(Integer.valueOf(customerId));
		} catch (Exception e) {
			logger.error("Exception while getting customer in getMozuCustomer() for customer id "
					+ customerId);
		}
		return account;
	}

	/**
	 * This method updates the CustomerAccount Object in mozu system.
	 * 
	 * @param apiContext
	 * @param customerAccount
	 * @return
	 */
	public CustomerAccount updateMozuCustomer(ApiContext apiContext,
			CustomerAccount customerAccount) {
		CustomerAccountResource customerResource = new CustomerAccountResource(
				apiContext);
		CustomerAccount updatedAccount = null;
		try {
			updatedAccount = customerResource.updateAccount(customerAccount,
					customerAccount.getId());

		} catch (Exception e) {
			logger.error("Exception while updating customer in updateMozuCustomer() for customer id "
					+ customerAccount.getId());
		}
		return updatedAccount;
	}

	/**
	 * This method gets mozu customers with the page size and starting index.
	 * 
	 * @param apiContext
	 * @param startIndex
	 *            defines the starting index from where the customer needs to be
	 *            fetched.
	 * @param pagesize
	 *            defines how many customer needs to be fetched.
	 * @return
	 * @throws MozuException
	 */
	public List<CustomerAccount> getMozuCustomers(ApiContext apiContext,
			int startIndex, int pagesize) {
		CustomerAccountResource customerResource = new CustomerAccountResource(
				apiContext);
        CustomerAccountCollection customerAccountCollection = null;

		try {
			customerAccountCollection = customerResource.getAccounts(
					Integer.valueOf(startIndex), Integer.valueOf(pagesize),
					null, null, null, null, null, null,null);
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder("Exception occurred getting customers for tenant ")
            .append(apiContext.getTenantId())
            .append(": ")
            .append(e.getMessage()); 
        logger.error(msg.toString(),e);
        throw new RuntimeException(msg.toString());

		}
		return customerAccountCollection.getItems();
	}
	
	/**
	 * This method gets mozu customers with the page size and starting index.
	 * 
	 * @param apiContext
	 * @param startIndex
	 *            defines the starting index from where the customer needs to be
	 *            fetched.
	 * @param pagesize
	 *            defines how many customer needs to be fetched.
	 * @return
	 * @throws MozuException
	 */
	public List<CustomerAccount> getMozuCustomersInSegment(ApiContext apiContext,
			String segmentId) {
		CustomerAccountResource customerResource = new CustomerAccountResource(
				apiContext);
        CustomerAccountCollection customerAccountCollection = null;
        String filter= "segments.id eq "+segmentId;
		try {
			customerAccountCollection = customerResource.getAccounts(
					null, null,
					null, filter , null, null, null, null,null);
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder("Exception occurred getting customers for tenant ")
            .append(apiContext.getTenantId())
            .append(": ")
            .append(e.getMessage()); 
        logger.error(msg.toString(),e);
        throw new RuntimeException(msg.toString());

		}
		return customerAccountCollection.getItems();
	}

	/**
	 * This method gets the customer using its email address.
	 * 
	 * @param apiContext
	 * @param searchString
	 * @return
	 * @throws MozuException
	 */
	public List<CustomerAccount> getMozuCustomersByEmail(
			ApiContext apiContext, String searchString) throws MozuException {
		CustomerAccountResource customerResource = new CustomerAccountResource(
				apiContext);

		List<CustomerAccount> customerList = new ArrayList<CustomerAccount>();
		try {
			CustomerAccountCollection accounts = customerResource.getAccounts(
					null, null, null, "emailAddress eq " + searchString, null,
					null, null, null,null);

			for (CustomerAccount customer : accounts.getItems()) {
				logger.debug(customer.getFirstName());
				customerList.add(customer);
			}
		} catch (Exception e) {
			logger.error("Exception while getting custoemr by email in getMozuCustomersByEmail() for email id "
					+ searchString);
			MozuException ex = new MozuException();
			ex.setErrorCode(ErrorCode.EX_CD_200);
			ex.setErrorMessage(ErrorCode.EX_MSG_200);
			throw ex;
		}
		return customerList;
	}

	/**
	 * This method returns the mailchimp model saved in MZDB.
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public MailchimpSyncModel getSettings(Integer tenantId)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		MozuApiContext apiContext = new MozuApiContext(
				Integer.valueOf(tenantId));
		TenantDataResource tenantData = new TenantDataResource(apiContext);
		MailchimpSyncModel setting = null;
		String settingStr = "";
		try {
			settingStr = tenantData.getDBValue(tenantId.toString());

		} catch (Exception exc) {
			return setting;
		}
		if (!StringUtils.isBlank(settingStr)) {
			setting = (MailchimpSyncModel) mapper.readValue(settingStr,
					MailchimpSyncModel.class);
		}
		if (setting == null) {
			setting = new MailchimpSyncModel();
		}
		return setting;
	}

	/**
	 * This method saves the mailchimp model in MZDB database.
	 * 
	 * @param tenantId
	 * @param setting
	 * @throws Exception
	 */
	public void saveSettings(Integer tenantId, MailchimpSyncModel setting)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		MozuApiContext apiContext = new MozuApiContext(tenantId);
		TenantDataResource tenantData = new TenantDataResource(apiContext);

		String value = (new StringBuilder()).append("'")
				.append(mapper.writeValueAsString(setting)).append("'")
				.toString();
		logger.debug(value);

		try {
			if (setting != null && setting.getApiKey() != null) {
				tenantData.updateDBValue(value, tenantId.toString());
			} else {
				tenantData.createDBValue(value, tenantId.toString());
			}

		} catch (Exception e) {
			if (e instanceof ApiException) {
				ApiError apiError = ((ApiException) e).getApiError();
				logger.warn((new StringBuilder())
						.append("Exception updating application settings: App name: ")
						.append(apiError.getApplicationName())
						.append(" Error Code ").append(apiError.getErrorCode())
						.append(" Message: ").append(apiError.getMessage())
						.toString());
			}
			throw e;
		}
	}

	/**
	 * This method initializes the application.
	 * 
	 * @param apiContext
	 * @throws Exception
	 */
	public final void setApplicationToInitialized(ApiContext apiContext)
			throws Exception {
		String errorMsg = null;
		// get the application information
		com.mozu.api.resources.commerce.settings.ApplicationResource appResource = new com.mozu.api.resources.commerce.settings.ApplicationResource(
				apiContext);

		com.mozu.api.contracts.sitesettings.application.Application application;
		try {
			application = appResource.thirdPartyGetApplication();
			logger.debug("Application retrieved");
		} catch (Exception e) {
			errorMsg = "Exception getting application information: ";
			if (e instanceof ApiException) {
				ApiError apiError = ((ApiException) e).getApiError();
				if (apiError != null) {
					errorMsg = errorMsg + " App name: "
							+ apiError.getApplicationName()
							+ " Correlation ID: " + apiError.getCorrelationId()
							+ " Error Code " + apiError.getErrorCode()
							+ " Message: " + apiError.getMessage();
				} else {
					errorMsg = errorMsg.concat(" ").concat(e.getMessage());
				}
			} else {
				errorMsg = errorMsg.concat(" ").concat(e.getMessage());
			}
			logger.debug(errorMsg);
			throw new Exception(errorMsg);
		}

		// Set the app to initialized
		application.setInitialized(true);

		try {
			appResource.thirdPartyUpdateApplication(application);
			logger.debug("Application updated");
		} catch (Exception e) {
			logger.warn("Exception updating application: " + e.getMessage());
			throw new Exception("Exception updating application: "
					+ e.getMessage());
		}
	}

	/**
	 * Gets the application object from mozu API
	 * @param apiContext
	 * @return
	 */
	public Application getApplication(ApiContext apiContext) {

		String errorMsg = null;
		// get the application information
		com.mozu.api.resources.commerce.settings.ApplicationResource appResource = new com.mozu.api.resources.commerce.settings.ApplicationResource(
				apiContext);

		com.mozu.api.contracts.sitesettings.application.Application application = null;
		try {
			application = appResource.thirdPartyGetApplication();

			logger.debug("Application retrieved");
		} catch (Exception e) {
			errorMsg = "Exception getting application information: ";
			if (e instanceof ApiException) {
				ApiError apiError = ((ApiException) e).getApiError();
				if (apiError != null) {
					errorMsg = errorMsg + " App name: "
							+ apiError.getApplicationName()
							+ " Correlation ID: " + apiError.getCorrelationId()
							+ " Error Code " + apiError.getErrorCode()
							+ " Message: " + apiError.getMessage();
				} else {
					errorMsg = errorMsg.concat(" ").concat(e.getMessage());
				}
			} else {
				errorMsg = errorMsg.concat(" ").concat(e.getMessage());
			}
			logger.debug(errorMsg);
		}

		return application;

	}

	/**
	 * This method gets the Tenant object using tenant Id
	 * 
	 * @param tenantId
	 * @return
	 */
	public Tenant getTenantName(Integer tenantId) {
		Tenant tenant = null;
		MozuApiContext apiContext = new MozuApiContext(tenantId);
		TenantResource tenantResource = new TenantResource(apiContext);
		try {
			tenant = tenantResource.getTenant(tenantId);
		} catch (Exception e) {
			logger.error("Exception while getting tenant info for tenant ID "
					+ tenantId);
		}

		return tenant;
	}

	/**
	 * This method saves the batch object in MZDB.
	 * 
	 * @param tenantId
	 * @param batchPojo
	 * @throws Exception
	 */
	public void saveBatchPojo(Integer tenantId, MozuBatchPojo batchPojo)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		MozuApiContext apiContext = new MozuApiContext(tenantId);
		TenantDataResource tenantData = new TenantDataResource(apiContext);

		String value = (new StringBuilder()).append("'")
				.append(mapper.writeValueAsString(batchPojo)).append("'")
				.toString();
		logger.debug(value);

		try {
			if (batchPojo.getKey() != null) {
				tenantData.updateDBValue(value, tenantId.toString() + "_JOB");
			} else {
				tenantData.createDBValue(value, tenantId.toString() + "_JOB");
			}

		} catch (Exception e) {
			if (e instanceof ApiException) {
				ApiError apiError = ((ApiException) e).getApiError();
				logger.warn((new StringBuilder())
						.append("Exception updating application settings: App name: ")
						.append(apiError.getApplicationName())
						.append(" Error Code ").append(apiError.getErrorCode())
						.append(" Message: ").append(apiError.getMessage())
						.toString());
			}
			throw e;
		}
	}

	/**
	 * This method gets the batch object from MZDB.
	 * 
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public MozuBatchPojo getBatchPojo(Integer tenantId) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		MozuApiContext apiContext = new MozuApiContext(
				Integer.valueOf(tenantId));
		TenantDataResource tenantData = new TenantDataResource(apiContext);
		MozuBatchPojo mozuBatchPojo = null;
		String settingStr = "";
		try {
			settingStr = tenantData.getDBValue(tenantId.toString() + "_JOB");

		} catch (Exception exc) {
			return mozuBatchPojo;
		}
		if (!StringUtils.isBlank(settingStr)) {
			mozuBatchPojo = (MozuBatchPojo) mapper.readValue(settingStr,
					MozuBatchPojo.class);
		}
		if (mozuBatchPojo == null) {
			mozuBatchPojo = new MozuBatchPojo();
		}
		return mozuBatchPojo;
	}
	
	
	public Category getCategory(
			ApiContext apiContext, Integer categoryId) {

		CategoryResource categoryResource = new CategoryResource(apiContext);
		Category category = null;
		try {
			category = categoryResource.getCategory(categoryId);
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder(
					"Exception occurred getting categories for site ")
					.append(apiContext.getSiteId()).append(": ")
					.append(e.getMessage());
			logger.error(msg.toString(), e);
			throw new RuntimeException(msg.toString());
		}
		return category;
	}

}
