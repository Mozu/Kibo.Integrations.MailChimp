package com.mozu.mailchimp.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.jobs.dao.SkipItemsDao;
import com.mozu.jobs.models.SkipItems;

/**
 * 
 The stepExecutionListener retrieve some informations and save it into the
 * StepExecutionContext. This stepExecutionContext can be used via the Spring
 * Configuration when the scope is step.
 * 
 * @author Amit
 * 
 */
@Service("mozuSkipListener")
@Scope("step")
public class MozuSkipListener  implements SkipListener<Object, Object> {

	private static Logger log = LoggerFactory
			.getLogger(MozuSkipListener.class);

    @Autowired
    private SkipItemsDao skipItemsDao;
    protected Long jobExecutionId;
    protected Long stepExecutionId;
    protected String jobName;
 
    public String getJobName() {
        return jobName;
    }
    @Value("#{stepExecutionContext['jobName']}")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    public Long getStepExecutionId() {
        return stepExecutionId;
    }
    @Value("#{stepExecutionContext['stepExecutionId']}")
    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    @Value("#{stepExecutionContext['jobExecutionId']}")
    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }
    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        log.info("onSkipInProcess:" +item.getClass()+" "+t.getClass(), t);
        if(t instanceof Exception){
            String msg;
            if (t.getMessage() == null) {
                msg = String.format("Unexpected Exception Thrown: %s. Please Contact Admin.", t.getClass().toString());
            } else {
                msg = t.getMessage();  
            };
            skipItemsDao.save(createSkipElement("PROCESS", item.getClass().toString(), msg));
        }
    }
 
    public void onSkipInRead(Throwable t) {
        log.info("onSkipInRead:"+ t.getClass(), t);
        if(t instanceof Exception){
            String msg;
            if (t.getMessage() == null) {
                msg = String.format("Unexpected Exception Thrown: %s. Please Contact Support.", t.getClass().toString());
            } else {
                msg = t.getMessage();  
            };
            skipItemsDao.save(createSkipElement("READ", "", msg));
        }
    }
 
    public void onSkipInWrite(Object item, Throwable t) {
        log.info("onSkipInWrite:" +item.getClass()+" "+t.getClass());
        if(t instanceof Exception){
            Exception sampleSkipException=(Exception)t;
            String msg;
            if (sampleSkipException.getMessage() == null) {
                msg = String.format("Unexpected Exception Thrown: %s. Please Contact Support.", sampleSkipException.getClass().toString());
            } else {
                msg = sampleSkipException.getMessage();  
            };
          skipItemsDao.save(createSkipElement("WRITE", item.getClass().toString(), msg));
        }
    }
 
    private SkipItems createSkipElement(String type, String item, String msg){
        return new SkipItems(type, item, msg, getJobExecutionId(), getStepExecutionId(), getJobName());
    }
    public SkipItemsDao getSkipItemsDao() {
        return skipItemsDao;
    }
 
    public void setSkipItemsDao(SkipItemsDao skipItemsDao) {
        this.skipItemsDao = skipItemsDao;
    }

}