package com.mozu.mailchimp.batch.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mozu.jobs.dao.SkipItemsDao;
import com.mozu.jobs.models.SkipItems;

@Service("jobFailureListener")
public class JobFailureListener implements JobExecutionListener {

    @Autowired 
    SkipItemsDao skipItemsDao;
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        // nothing to do
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            ExitStatus exitStatus = ExitStatus.FAILED;
            for (Throwable e : jobExecution.getAllFailureExceptions()) {
                String msg;
                if (e.getMessage() == null) {
                    msg = String.format("Export Failed. Unexpected Exception Thrown: %s. Please Contact Support.", e.getClass().toString());
                } else {
                    msg = String.format("Export Failed: %s", e.getMessage());  
                };


                
                SkipItems skipItems = new SkipItems ("EXECUTION", "JobFailure", msg, 
                        jobExecution.getId(), null, jobExecution.getJobInstance().getJobName());
                skipItemsDao.save(skipItems);

                exitStatus = exitStatus.addExitDescription(e);
            }
            jobExecution.setExitStatus(exitStatus);
        }
    }

}