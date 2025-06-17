package com.mozu.mailchimp.batch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult.Data;
import com.mozu.mailchimp.mailchimpextends.MemberInfo;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

/**
 * Import customers from MailChimp 
 */
@Service("customerImportProcessor")
@Scope("step")
public class MailchimpCustomerReader implements ItemProcessor<Data, List<MemberInfo>> {

	private static final Logger logger = LoggerFactory.getLogger(MailchimpCustomerReader.class);

    private Integer tenantId;

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Override
    public List<MemberInfo> process(Data item) throws Exception {
        List<MemberInfo> memberInfo = null;
        
        logger.debug("Process list " + item.name);
        
        MailchimpSyncModel setting;
        try {
            setting = mozuUtil.getSettings(tenantId);
            String apiKey = "";
            if (setting != null) {
                apiKey = setting.getApiKey();
            }
            memberInfo = mailChimpUtil.getAllCustomers(apiKey, item.id);
        } catch (Exception e) {
            logger.error("In exception while processing bulk sync from mailchimp to mozu for tenantID "
                    + tenantId);
        }
        return memberInfo;
    }

    @Value("#{jobParameters['tenantId']}")
    public void setTenantId(final Long tenantId) {
        this.tenantId = tenantId !=null ? tenantId.intValue() : null;
    }
}
