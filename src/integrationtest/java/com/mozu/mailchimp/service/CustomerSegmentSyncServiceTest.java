package com.mozu.mailchimp.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerSegment;
import com.mozu.mailchimp.BaseTest;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/servlet-context.xml"})
public class CustomerSegmentSyncServiceTest extends BaseTest {
    @Autowired
	CustomerSegmentSyncService customerSegmentSyncService;
    
    @Autowired
    private MozuUtil mozuUtil;
    
    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    public CustomerSegmentSyncServiceTest() {
        super();
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSyncSegmentToMailchimp () throws Exception {
    
	     ApiContext apiContext=new MozuApiContext(TENANT_ID,SITE_ID);
	     int offset=0;
	     int pageSize=10;
	     List<CustomerSegment> customerSegmentList=customerSegmentSyncService.getCustomerSegments(apiContext, offset, pageSize);
	     assertNotNull(customerSegmentList);
    
		 for (CustomerSegment customerSegment : customerSegmentList) {
			 customerSegmentSyncService.sendSegmentToMailchimp(apiContext, customerSegment.getId().toString());
	  	 }
   
    }
    
    @Test
    public void testDeleteSegmentFromMailchimp () throws Exception {
 	     ApiContext apiContext=new MozuApiContext(TENANT_ID,SITE_ID);
	     int offset=0;
	     int pageSize=20;
	     List<CustomerSegment> customerSegmentList=customerSegmentSyncService.getCustomerSegments(apiContext, offset, pageSize);
	     assertNotNull(customerSegmentList);
    
		 for (CustomerSegment customerSegment : customerSegmentList) {
			 customerSegmentSyncService.deleteStaticSegmentFromMailchimp(apiContext, customerSegment.getId().toString());
		 }
    }
  /*  
    @Test
    public void testAddCustomersToMailchimpSegment() throws Exception {
    
	     ApiContext apiContext=new MozuApiContext(TENANT_ID,SITE_ID);
	     int offset=0;
	     int pageSize=100;
	     Integer tenantId=apiContext.getTenantId();
	     List<CustomerAccount> customersList= mozuUtil.getRegisteredMozuCustomers(apiContext, offset, pageSize);
	     
	     assertNotNull(customersList);
		 for (CustomerAccount customer : customersList) {
			 if(customer.getEmailAddress().equals("mozuqa+ritu@gmail.com")){
			 for(CustomerSegment customerSegment:customer.getSegments())
				 customerSegmentSyncService.addCustomersToMailchimpSegment(tenantId,customerSegment.getId().toString(),customer.getEmailAddress());
			 }
	  	 }
   
    }
    
    @Test
    public void testRemoveCustomersFromMailchimpSegment() throws Exception {
    
	     ApiContext apiContext=new MozuApiContext(TENANT_ID,SITE_ID);
	     int offset=0;
	     int pageSize=30;
	     Integer tenantId=apiContext.getTenantId();
	     List<CustomerAccount> customersList= mozuUtil.getRegisteredMozuCustomers(apiContext, offset, pageSize);
	     
	     assertNotNull(customersList);
		 for (CustomerAccount customer : customersList) {
			 for(CustomerSegment customerSegment:customer.getSegments())
				 customerSegmentSyncService.removeCustomersFromMailchimpSegment(tenantId,customerSegment.getId().toString(),customer.getEmailAddress());
	  	 }
   
    }
    
    
    @Test
    public void testGetRegisteredCustomers() throws Exception {
    
	     ApiContext apiContext=new MozuApiContext(TENANT_ID,SITE_ID);
	     int offset=0;
	     int pageSize=100;
	     Integer tenantId=apiContext.getTenantId();
	     List<CustomerAccount> customersList= mozuUtil.getRegisteredMozuCustomers(apiContext, offset, pageSize);
	     
	     assertNotNull(customersList);
		 
    }*/
      
}
