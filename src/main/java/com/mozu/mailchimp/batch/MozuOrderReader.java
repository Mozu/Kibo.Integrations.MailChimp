package com.mozu.mailchimp.batch;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerAttributeCollection;
import com.mozu.api.resources.commerce.customer.accounts.CustomerAttributeResource;
import com.mozu.mailchimp.util.MozuUtil;

@Service("orderExportReader")
@Scope("step")
public class MozuOrderReader extends AbstractPagingItemReader<Order> {

    private static final Logger logger = LoggerFactory.getLogger(MozuOrderReader.class);

    private Integer tenantId;
    private Date lastRunDate;
    List<Order> result;
    private static final String status="Completed";
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new CopyOnWriteArrayList<Order>();
        } else {
            results.clear();
        }
        if (tenantId == null) {
            String msg = "Tenant ID must be supplied as job parameters."; 
            logger.error(msg);
            throw new RuntimeException(msg);
        }
  		try {
	  			ApiContext apiContext = new MozuApiContext(tenantId);
	  	        String filter = null;
 	  	        filter="status eq "+status;
	  	        if (lastRunDate != null) {
	  	            DateTime lastRunDateTime = new DateTime(lastRunDate);
	  	            filter += " and submittedDate gt " + lastRunDateTime.toDateTimeISO();
	  	        }
				result = mozuUtil.getOrders(apiContext,this.getPage() * getPageSize(), getPageSize(),filter);
		        if ((getPage() == 0) && result.isEmpty()) {
		            StringBuilder msg = new StringBuilder("No orders returned for tenantId: ").append(tenantId); 
		            logger.error(msg.toString());
		        }
		        results.addAll(result);
  			} catch (Exception e) {
  					StringBuilder msg = new StringBuilder("Exception occurred getting orders for tenant ").append(tenantId).append(": ").append(e.getMessage()); 
  					logger.error(msg.toString(),e);
  					throw new RuntimeException(msg.toString());
  				}
    }
    
    @Override
    public Order read() throws Exception ,org.springframework.batch.item.UnexpectedInputException ,org.springframework.batch.item.ParseException {
    	Order order = super.read();
        if (order != null) {
            logger.debug("Read Order: " + order.getId());
        }
        return order;
    } 

    @Override
    protected void doJumpToPage(int itemIndex) {
        // TODO Auto-generated method stub
        
    }

    @Value("#{jobParameters['tenantId']}")
    public void setTenantId(final Long tenantId) {
        this.tenantId = tenantId !=null ? tenantId.intValue() : null;
    }
    
    @Value("#{jobParameters['lastRunTime']}")
    public void setBeginDate (final Date lastRunDate) {
        this.lastRunDate = lastRunDate;
    }

    
}
