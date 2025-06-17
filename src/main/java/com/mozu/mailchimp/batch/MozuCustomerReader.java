package com.mozu.mailchimp.batch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerAttributeCollection;
import com.mozu.api.resources.commerce.customer.accounts.CustomerAttributeResource;
import com.mozu.mailchimp.util.MozuUtil;

@Service("customerExportReader")
@Scope("step")
public class MozuCustomerReader extends AbstractPagingItemReader<CustomerAccount> {

    private static final Logger logger = LoggerFactory.getLogger(MozuCustomerReader.class);

    private Integer tenantId;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new CopyOnWriteArrayList<CustomerAccount>();
        } else {
            results.clear();
        }
        if (tenantId == null) {
            String msg = "Tenant ID must be supplied as job parameters."; 
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        ApiContext apiContext = new MozuApiContext(tenantId);
        CustomerAttributeResource attrResource = new CustomerAttributeResource(
				apiContext);

        
        final List<CustomerAccount> result = mozuUtil.getMozuCustomers(apiContext,this.getPage() * getPageSize(), getPageSize());
        
        for (CustomerAccount account : result) {
			if (account.getAcceptsMarketing() != null
					&& account.getAcceptsMarketing()) {
				CustomerAttributeCollection collection = null;
				try {
					collection = attrResource
							.getAccountAttributes(account.getId());
				} catch (Exception e) {
		            StringBuilder msg = new StringBuilder("Exception occurred getting attribute for customer id ").append(account.getId()).append(": ").append(e.getMessage()); 
		            logger.error(msg.toString(),e);
		            throw new RuntimeException(msg.toString());
		        }
				account.setAttributes(collection.getItems());
			}
		}
        
        if ((getPage() == 0) && result.isEmpty()) {
            StringBuilder msg = new StringBuilder("No customers returned for tenantId: ").append(tenantId); 
            logger.error(msg.toString());
            throw new RuntimeException(msg.toString());
        }
        results.addAll(result);
    }
    
    @Override
    public CustomerAccount read() throws Exception ,org.springframework.batch.item.UnexpectedInputException ,org.springframework.batch.item.ParseException {
    	CustomerAccount CustomerAccount = super.read();
        if (CustomerAccount != null) {
            logger.debug("Read customer: " + CustomerAccount.getId());
        }
        return CustomerAccount;
    } 

    @Override
    protected void doJumpToPage(int itemIndex) {
        // TODO Auto-generated method stub
        
    }

    @Value("#{jobParameters['tenantId']}")
    public void setTenantId(final Long tenantId) {
        this.tenantId = tenantId !=null ? tenantId.intValue() : null;
    }

    
}
