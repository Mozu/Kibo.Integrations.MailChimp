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
import com.mozu.api.contracts.customer.CustomerSegment;
import com.mozu.api.contracts.productruntime.Product;
import com.mozu.mailchimp.service.CustomerSegmentSyncService;
import com.mozu.mailchimp.util.MozuUtil;

@Service("segmentExportReader")
@Scope("step")
public class MozuSegmentReader extends AbstractPagingItemReader<CustomerSegment> {

    private static final Logger logger = LoggerFactory.getLogger(MozuSegmentReader.class);

    private Integer tenantId;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Autowired
    private CustomerSegmentSyncService customerSegmentSyncService;
    
    @Override
    protected void doReadPage() {
    	logger.debug("Start reading segments from Mozu for tenant id "+tenantId );
        if (results == null) {
            results = new CopyOnWriteArrayList<CustomerSegment>();
        } else {
            results.clear();
        }
        if (tenantId == null) {
            String msg = "Tenant ID must be supplied as job parameters for searching segment."; 
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        ApiContext apiContext = new MozuApiContext(tenantId);
        final List<CustomerSegment> result = customerSegmentSyncService.getCustomerSegments(apiContext, this.getPage()*getPageSize(), getPageSize());
        if ((getPage() == 0) && result.isEmpty()) {
            StringBuilder msg = new StringBuilder("No segment returned for tenant: ").append(tenantId); 
            logger.info(msg.toString());
        }
        results.addAll(result);
    }
    
    @Override
    public CustomerSegment read() throws Exception ,org.springframework.batch.item.UnexpectedInputException ,org.springframework.batch.item.ParseException {
    	CustomerSegment customerSegment = super.read();
        if (customerSegment != null) {
            logger.debug("Read Segment: " + customerSegment.getCode());
        }
        return customerSegment;
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
