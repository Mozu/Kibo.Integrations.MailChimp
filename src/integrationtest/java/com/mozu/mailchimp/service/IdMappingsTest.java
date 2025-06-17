package com.mozu.mailchimp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.mzdb.EntityCollection;
import com.mozu.api.resources.platform.EntityListResource;
import com.mozu.api.resources.platform.entitylists.EntityResource;
import com.mozu.api.security.AppAuthenticator;
import com.mozu.mailchimp.BaseTest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/servlet-context.xml"})
public class IdMappingsTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(IdMappingsTest.class);
    
    @Autowired
    IdMappingDaoImpl idMappingDao;
    
    @Before
    public void setUp() throws Exception {
        idMappingDao.installSchema(TENANT_ID);
    }

    @After
    public void tearDown() throws Exception {
    }


    
    @Test
    public void dumpIdMap() throws Exception {
        ApiContext apiContext = new MozuApiContext(TENANT_ID);
        
        idMappingDao.installSchema(TENANT_ID);

        String ID_MAPPINGS_LIST = "mailchimpIdMappings";
        
        String mozuNamespace = null;
        String idMapName = null;
        String appId = AppAuthenticator.getInstance().getAppAuthInfo().getApplicationId();
        
        mozuNamespace = appId.substring(0, appId.indexOf('.'));
        idMapName = ID_MAPPINGS_LIST + "@" + mozuNamespace;

        EntityResource entityResource = new EntityResource(apiContext);
        EntityCollection entities = entityResource.getEntities(idMapName);
        logger.info("** Start id list **");
        for (JsonNode entity:entities.getItems()) {
            logger.info(entity.toString());
        }
        logger.info("** Finish id list **");
    }

    
    @Test
    public void deleteIdMap() throws Exception {
        ApiContext apiContext = new MozuApiContext(TENANT_ID);
        String ID_MAPPINGS_LIST = "mailchimpIdMappings";
        
        String mozuNamespace = null;
        String idMapName = null;
        String appId = AppAuthenticator.getInstance().getAppAuthInfo().getApplicationId();
        
        mozuNamespace = appId.substring(0, appId.indexOf('.'));
        idMapName = ID_MAPPINGS_LIST + "@" + mozuNamespace;
        
        EntityListResource entityListResource = new EntityListResource(apiContext);
        entityListResource.deleteEntityList(idMapName);
    }

   
}
