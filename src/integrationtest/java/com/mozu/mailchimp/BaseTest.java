package com.mozu.mailchimp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;

import com.mozu.mailchimp.model.JobInfoUI;



public class BaseTest {
	
    @Autowired
    JobExplorer jobExplorer;

    protected static final int TENANT_ID = 4449;
    protected static final int SITE_ID = 7280;

    
    public List<JobInfoUI> getSyncStatus(String executionIds)
            throws Exception {
        List<JobInfoUI> jobInfoList = new ArrayList<>();

        String[] jobIdStrs = executionIds.split(",");

        for (String execIdStr : jobIdStrs) {
            if (StringUtils.isNotBlank(execIdStr)) {
                Long executionId = Long.decode(execIdStr);
                jobInfoList.add(new JobInfoUI(jobExplorer.getJobExecution(executionId)));
            }
        }

        return jobInfoList;
    }
}
