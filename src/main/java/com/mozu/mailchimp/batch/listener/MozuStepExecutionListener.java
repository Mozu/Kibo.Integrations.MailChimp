package com.mozu.mailchimp.batch.listener;

/**
 The stepExecutionListener retrieve some informations and save it into the StepExecutionContext.
 This stepExecutionContext can be used via the Spring Configuration when the scope is step.

 @Author : Amit
 **/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * This implements StepExecutionListener interface.
 * 
 * @author Amit
 * 
 */
@Service("mozuStepExecutionListener")
@Scope("step")
public class MozuStepExecutionListener implements StepExecutionListener {
	private static final Logger logger = LoggerFactory
			.getLogger(MozuStepExecutionListener.class);
	@Override
    public void beforeStep(StepExecution stepExecution){
        Long jobExecutionId =stepExecution.getJobExecutionId();
        String jobName=stepExecution.getJobExecution().getJobInstance().getJobName();
        stepExecution.getExecutionContext().put("jobExecutionId", jobExecutionId);
        stepExecution.getExecutionContext().put("stepExecutionId", stepExecution.getId());
        stepExecution.getExecutionContext().put("jobName", jobName);
    }
 

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

}