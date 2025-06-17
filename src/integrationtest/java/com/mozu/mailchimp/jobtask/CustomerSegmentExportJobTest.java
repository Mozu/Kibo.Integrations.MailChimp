package com.mozu.mailchimp.jobtask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mozu.mailchimp.BaseTest;
import com.mozu.mailchimp.model.JobInfoUI;
import com.mozu.mailchimp.model.MailchimpSyncModel;
import com.mozu.mailchimp.service.MozuMcClientSetupService;
import com.mozu.mailchimp.util.MozuUtil;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/servlet-context.xml"})
public class CustomerSegmentExportJobTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(CustomerSegmentExportJobTest.class);

    @Autowired
	private MozuMcClientSetupService mozuMcClientSetupService;
    
	@Autowired
	private MozuUtil mozuUtil;
	
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
    	MailchimpSyncModel mailchimpSetting = mozuUtil.getSettings(TENANT_ID);
    	
    	JobExecution jobExecution=mozuMcClientSetupService.triggerBatch(mailchimpSetting, JobInfoUI.CUSTOMER_EXPORT_JOB, null);
      
        assertNotNull(jobExecution);

        JobInfoUI currentJobInfoUI = new JobInfoUI(jobExecution);
        List<JobInfoUI> jobInfoUIs = null;
        int count = 0;
        while (currentJobInfoUI.getExitStatus().equals(ExitStatus.UNKNOWN.getExitCode()) ||
                currentJobInfoUI.getExitStatus().equals(ExitStatus.EXECUTING.getExitCode())) {
            if (count++ > 60) {
                logger.error("We've  been going for 5 minutes...lets stop and fail!");
                break;
            }
            jobInfoUIs = getSyncStatus(jobExecution.getId().toString());
            currentJobInfoUI = jobInfoUIs.get(0);
            Thread.sleep(5000);
        } 
        System.out.println ("Exit Status: " + jobExecution.getExitStatus());
        
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }
}
