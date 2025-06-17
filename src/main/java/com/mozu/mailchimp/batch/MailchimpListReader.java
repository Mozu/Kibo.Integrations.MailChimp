package com.mozu.mailchimp.batch;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult;
import com.ecwid.mailchimp.method.v2_0.lists.ListMethodResult.Data;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.util.MailchimpUtil;
import com.mozu.mailchimp.util.MozuUtil;

/**
 * Import customers from MailChimp 
 */
@Service("customerImportReader")
@Scope("step")
public class MailchimpListReader extends AbstractPagingItemReader<Data> {

	private static final Logger logger = LoggerFactory.getLogger(MailchimpListReader.class);

    private Integer tenantId;

    @Autowired
    private MailchimpUtil mailChimpUtil;
    
    @Autowired
    protected MozuUtil mozuUtil;
    
    @Override
    protected void doReadPage() {
        logger.debug("Import read page");
        if (results == null) {
            results = new CopyOnWriteArrayList<Data>();
        } else {
            results.clear();
        }
        
        MailchimpSyncModel setting;
        try {
            setting = mozuUtil.getSettings(tenantId);
            String apiKey = "";
            if (setting != null) {
                apiKey = setting.getApiKey();
            }
            ListMethodResult result = mailChimpUtil.getAllMcList(apiKey, getPage() * getPageSize(), getPageSize());
            results.addAll(result.data);
        } catch (Exception e) {
            logger.error("In exception while processing bulk sync from mailchimp to mozu for tenantID "
                    + tenantId);
        }
    }

    @Override
    protected void doJumpToPage(int itemIndex) {
        logger.debug("Import jump to page");
    }

    @Value("#{jobParameters['tenantId']}")
    public void setTenantId(final Long tenantId) {
        this.tenantId = tenantId !=null ? tenantId.intValue() : null;
    }
}
