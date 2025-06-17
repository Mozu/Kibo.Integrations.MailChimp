package com.mozu.mailchimp.model;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

public class JobInfoUI {
    public static final String TENANT_ID_PARAM = "tenantId";
    public static final String SITE_ID_PARAM = "siteId";
    public static final String TIMESTAMP_PARAM = "timestamp";
    public static final String LAST_RUN_TIME_PARAM = "lastRunTime";
    
    public static final String CUSTOMER_EXPORT_JOB = "mcCustomerExportJob";
    public static final String CUSTOMER_IMPORT_JOB = "mcCustomerImportJob";
    public static final String ORDER_EXPORT_JOB = "mcOrderExportJob";

    
    private Long id;
    private Long tenantId;
    private Long siteId;
    private int readCount;
    private int processCount;
    private int writeCount;
    private int errorCount;
    private String batchStatus;
    private String exitStatus;
    private List<String> errorMessage;
    private String jobName;

    private Long startDateTime = null;
    
    public JobInfoUI () {
        
    }
    
    public JobInfoUI (JobExecution jobExecution) {
        this.setJobName(jobExecution.getJobInstance().getJobName());
        this.setId(jobExecution.getId());
        this.setSiteId(jobExecution.getJobParameters().getLong(SITE_ID_PARAM));
        this.setTenantId(jobExecution.getJobParameters().getLong(TENANT_ID_PARAM));
        this.setBatchStatus(jobExecution.getStatus().toString());
        this.setExitStatus(jobExecution.getExitStatus().getExitCode().toString());
        int errorCount = 0;
        int readCount = 0;
        int writeCount = 0;
        
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            errorCount += stepExecution.getProcessSkipCount() + stepExecution.getReadSkipCount() + stepExecution.getWriteSkipCount();
            readCount += stepExecution.getReadCount();
            writeCount +=stepExecution.getWriteCount();
        }

        if (jobExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode())) {
            errorCount++;
        }
        
        this.setErrorCount(errorCount); 
        this.setWriteCount(writeCount);
        this.setReadCount(readCount);
        
        // The others can be localized
        if (jobExecution.getStartTime() != null) {
            this.startDateTime = jobExecution.getStartTime().getTime();
        }

    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTenantId() {
        return tenantId;
    }
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    public Long getSiteId() {
        return siteId;
    }
    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public int getReadCount() {
        return readCount;
    }
    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }
    public int getProcessCount() {
        return processCount;
    }
    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }
    public int getWriteCount() {
        return writeCount;
    }
    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }
    public int getErrorCount() {
        return errorCount;
    }
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
    public List<String> getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(List<String> errorMessage) {
        this.errorMessage = errorMessage;
    }
    public String getBatchStatus() {
        return batchStatus;
    }
    public void setBatchStatus(String batchStatus) {
        this.batchStatus = batchStatus;
    }
    public String getExitStatus() {
        return exitStatus;
    }
    public void setExitStatus(String exitStatus) {
        this.exitStatus = exitStatus;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

}
