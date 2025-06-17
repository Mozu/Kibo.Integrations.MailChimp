package com.mozu.mailchimp.batch;

import java.util.List;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.mailchimp.mailchimpextends.MemberInfo;
import com.mozu.mailchimp.util.MozuUtil;

@Service("customerImportWriter")
@Scope("step")
public class MailchimpCustomerWriter implements ItemWriter<List<MemberInfo>> {
    private static final Logger logger = LoggerFactory.getLogger(MailchimpCustomerWriter.class);

    private Integer tenantId;

    @Autowired
    protected MozuUtil mozuUtil;
    
    @Override
    public void write(List<? extends List<MemberInfo>> items) throws Exception {
        StringBuilder errorMsgs = new StringBuilder();
        ApiContext apiContext = new MozuApiContext(tenantId);
        for (List<MemberInfo> miList: items) {
            for (MemberInfo member : miList) {
                logger.debug("Write customer " + member.email);
    
                try {
                    List<CustomerAccount> listCustomerAccount = mozuUtil
                            .getMozuCustomersByEmail(apiContext, member.email);
                    
                    Boolean acceptsMarketing=member.status.toString().equals("subscribed")?true:false;
                    for (CustomerAccount customer : listCustomerAccount) {
                        customer.setAcceptsMarketing(acceptsMarketing);
                        mozuUtil.updateMozuCustomer(apiContext, customer);
                    }
                } catch (Exception e) {
                    errorMsgs.append("Error writing customer ").append(member.email_address).append(": ").append(e.getMessage()).append(" | ");
                }
            }
        }
        
        if (errorMsgs.length() > 0) {
            throw new JobExecutionException(errorMsgs.toString());
        }
    }

    @Value("#{jobParameters['tenantId']}")
   public void setTenantId(final Long tenantId) {
       this.tenantId = tenantId !=null ? tenantId.intValue() : null;
   }
}