package com.mozu.mailchimp.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mozu.api.ApiContext;
import com.mozu.api.ApiException;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.mzdb.EntityCollection;
import com.mozu.api.contracts.mzdb.EntityList;
import com.mozu.api.contracts.mzdb.IndexedProperty;
import com.mozu.api.resources.platform.EntityListResource;
import com.mozu.api.resources.platform.entitylists.EntityResource;
import com.mozu.api.security.AppAuthenticator;
import com.mozu.base.handlers.EntitySchemaHandler;
import com.mozu.base.models.EntityDataTypes;
import com.mozu.base.models.EntityScope;
import com.mozu.mailchimp.exception.MailchimpDuplicateMappingException;
import com.mozu.mailchimp.exception.MailchimpMappingReservedException;

@Service
public class IdMappingDaoImpl implements IdMappingDao {
    private static final Logger logger = LoggerFactory.getLogger(IdMappingDaoImpl.class);
    
    @Autowired
    private EntitySchemaHandler  entitySchemaHandler;
    
    private static final String ID_MAPPINGS_LIST = "mailchimpIdMappings";

    private static final String MAP_ENTRY_RESERVED = "reserved";
    
    protected static String mozuNamespace = null;
    protected static String idMapName = null;
   
  @Override
  public void installSchema(Integer tenantId) throws Exception {
        
        EntityList entityList = new EntityList();
        entityList.setName(ID_MAPPINGS_LIST);
        entityList.setIsVisibleInStorefront(false);
        entityList.setIsLocaleSpecific(false);
        entityList.setIsSandboxDataCloningSupported(true);
        entityList.setIsShopperSpecific(false);
        IndexedProperty idProperty = entitySchemaHandler.getIndexedProperty(TYPE_MOZU_ID_FIELD, EntityDataTypes.String);
		List<IndexedProperty> indexedProperties = new ArrayList<IndexedProperty>();
		indexedProperties.add(entitySchemaHandler.getIndexedProperty(TYPE_MAILCHIMP_ID_FIELD, EntityDataTypes.String));
		
		ApiContext apiContext = new MozuApiContext(tenantId);
		try {
			entitySchemaHandler.installSchema(apiContext, entityList, EntityScope.Tenant, idProperty, indexedProperties);
			
		} catch (Exception e) {
			 logger.debug("Exception occured while installing schema "+ ID_MAPPINGS_LIST + e.getMessage());
		}
    }
   @Override 
   public void installEventSchema(ApiContext apiContext) throws Exception {
        
        EntityList entityList = new EntityList();
        entityList.setName(EVENT_MAPPING_LIST);
        entityList.setIsVisibleInStorefront(false);
        entityList.setIsLocaleSpecific(false);
        entityList.setIsSandboxDataCloningSupported(true);
        entityList.setIsShopperSpecific(false);
        IndexedProperty idProperty = entitySchemaHandler.getIndexedProperty(TYPE_MOZU_EVENT_FIELD, EntityDataTypes.String);
        try {
        entitySchemaHandler.installSchema(apiContext, entityList, EntityScope.Tenant,idProperty, null);
        }catch (Exception e) {
			 logger.debug("Exception occured while installing schema "+ EVENT_MAPPING_LIST + e.getMessage());
		}
    }
   @Override
   public void installEcommerce360Schema(ApiContext apiContext) throws Exception {
        
	   EntityList entityList = new EntityList();
       entityList.setName(ECOMMERCE_MAPPING_LIST);
       entityList.setIsVisibleInStorefront(true);
       entityList.setIsLocaleSpecific(false);
       entityList.setIsSandboxDataCloningSupported(true);
       entityList.setIsShopperSpecific(true);
       entityList.setUseSystemAssignedId(false);
       IndexedProperty idProperty = entitySchemaHandler.getIndexedProperty(TYPE_MOZU_ORDER_FIELD, EntityDataTypes.String);
	   try {
			entitySchemaHandler.installSchema(apiContext, entityList, EntityScope.Tenant, idProperty, null);
			
		} catch (Exception e) {
			 logger.debug("Exception occured while installing schema "+ ECOMMERCE_MAPPING_LIST + e.getMessage());
		}
    }

    @Override
    public String getMailChimpId(Integer tenantId, String mozuId, String type) throws Exception {
        setMappingName();
        String mailChimpIdString = null;
        try {
            EntityResource entityResource = new EntityResource(new MozuApiContext(tenantId));
            JsonNode entity = entityResource.getEntity(idMapName, generateEntityId(type, mozuId));
            if(entity!=null){
	            JsonNode mcIdNode = entity.findValue(TYPE_MAILCHIMP_ID_FIELD);
	            if (mcIdNode!=null) {
	               	mailChimpIdString = mcIdNode.asText();
	            }
            }
        } catch (ApiException e) {
            logger.debug("Exception getting MailChimp Id " + e.getApiError().getMessage());
        }
        return mailChimpIdString;
    }
    
    
    
    @Override
    public String getMozuId(Integer tenantId, Integer mailChimpId, String type) throws Exception {
        if (mailChimpId == null) {
            return null;
        }
        String mzId = null;
        
        setMappingName();

        EntityResource entityResource = new EntityResource(new MozuApiContext(tenantId));

        StringBuilder filter = new StringBuilder(TYPE_MAILCHIMP_ID_FIELD)
            .append(" eq ")
            .append(generateEntityId(type,mailChimpId.toString()));
        EntityCollection entityCollection = entityResource.getEntities(idMapName, 1, 0, filter.toString(), null, null);
        if (entityCollection.getTotalCount()==0) {
            return null;
        }
        
        List<JsonNode> mappings = entityCollection.getItems();
        if (mappings!=null && mappings.size()>0) {
            // there should only be one entry. Use the first on regardless
            JsonNode mapping = mappings.get(0);
            JsonNode mzIdNode = mapping.findValue(TYPE_MOZU_ID_FIELD);
            if (mzIdNode!=null) {
                // parse off the ID value
                String idStr = mzIdNode.asText();
                mzId = idStr.substring(type.length()+1);
            }
        }
        return mzId;
    }

    @Override
    public String putMozuId(Integer tenantId, String mozuId, Integer mailChimpId, String type) throws Exception {
        
        setMappingName();

        EntityResource entityResource = new EntityResource(new MozuApiContext(tenantId));

        // See if an entry exists for the Mailchimp id. We do not need to explicitly check
        // for a duplicate Mozu Id, the insert will throw a duplicate exception
        if (this.getMozuId(tenantId, mailChimpId, type)!=null) {
            StringBuilder errMsg = new StringBuilder("Cannot add entry for Mozu id")
                .append(mozuId)
                .append(" Mailchimp Id ")
                .append(mailChimpId)
                .append(". The Mailchimp Id is already mapped.");
            logger.error(errMsg.toString());
            throw new MailchimpDuplicateMappingException(errMsg.toString());
        }
        
        // Build the Json object for the mapping
        JsonNodeFactory nodeFactory = new JsonNodeFactory(false);
        
        ObjectNode idMap = nodeFactory.objectNode();
        idMap.put(TYPE_MOZU_ID_FIELD, generateEntityId(type, mozuId));
        String mcIdString = mailChimpId != null ? mailChimpId.toString() : null; 
        idMap.put(TYPE_MAILCHIMP_ID_FIELD, generateEntityId(type, mcIdString));

        // Add the mapping entry
        JsonNode rtnEntry = null;
        try {
            logger.debug("Insert mapping " + idMap.toString());
            rtnEntry = entityResource.insertEntity(idMap, idMapName);
        } catch (Exception e) {
            // An exception occurred. If it is a duplicate, throw a duplicate exception,
            // otherwise log the error and rethrow the exception
            StringBuilder errMsg = new StringBuilder("Cannot add entry for Mozu id")
            .append(mozuId)
            .append(" MailChimp Id ")
            .append(mailChimpId)
            .append(" ");
            if (e instanceof ApiException) {
                if (StringUtils.equals(((ApiException)e).getApiError().getErrorCode(), "ITEM_ALREADY_EXISTS")) {
                        errMsg.append("the Mozu Id is already mapped.");
                    logger.error(errMsg.toString());
                    throw new MailchimpDuplicateMappingException(errMsg.toString());
                } else {
                    errMsg.append(((ApiException)e).getApiError().getMessage());
                    logger.error(errMsg.toString());
                    throw e;
                }
            } else {
                errMsg.append(e.getMessage());
                logger.error(errMsg.toString());
                throw e;
            }
        }
        return rtnEntry.findValue(TYPE_MOZU_ID_FIELD).textValue();
    }

    @Override
    public void updateEntry(Integer tenantId, String indexKey, Integer lsId) throws Exception {
        setMappingName();

        EntityResource entityResource = new EntityResource(new MozuApiContext(tenantId));

        // Build the Json object for the mapping
        JsonNodeFactory nodeFactory = new JsonNodeFactory(false);
        
        ObjectNode idMap = nodeFactory.objectNode();
        idMap.put(TYPE_MOZU_ID_FIELD, indexKey);
        idMap.put(TYPE_MAILCHIMP_ID_FIELD, generateEntityId(indexKey.substring(0, indexKey.indexOf('-')), lsId.toString()));

        // Add the mapping entry
        try {
            entityResource.updateEntity(idMap, idMapName, indexKey );
        } catch (Exception e) {
            // An exception occurred. If it is a duplicate, throw a duplicate exception,
            // otherwise log the error and rethrow the exception
            StringBuilder errMsg = new StringBuilder("Cannot add entry for Mozu id")
            .append(indexKey)
            .append(" MailChimp Id ")
            .append(lsId)
            .append(" ");
            if (e instanceof ApiException) {
                if (StringUtils.equals(((ApiException)e).getApiError().getErrorCode(), "ITEM_ALREADY_EXISTS")) {
                        errMsg.append("the Mozu Id is already mapped.");
                    logger.error(errMsg.toString());
                    throw new MailchimpDuplicateMappingException(errMsg.toString());
                } else {
                    errMsg.append(((ApiException)e).getApiError().getMessage());
                    logger.error(errMsg.toString());
                    throw e;
                }
            } else {
                errMsg.append(e.getMessage());
                logger.error(errMsg.toString());
                throw e;
            }
        }
    }


    @Override
    public Integer getMailChimpProductId(Integer tenantId, String mozuId) throws Exception {
    	Integer mailChimpId = null;
        String mailChimpIdString = getMailChimpId(tenantId, mozuId, PRODUCT_TYPE);
        if(mailChimpIdString!=null){
        	mailChimpId = Integer.valueOf(mailChimpIdString.substring(PRODUCT_TYPE.length()+1));
        }
        return mailChimpId;
    }

    @Override
    public Integer getMailChimpSegmentId(Integer tenantId, String mozuId) throws Exception {
    	Integer mailChimpId = null;
        String mailChimpIdString = getMailChimpId(tenantId, mozuId, SEGMENT_TYPE);
        if(mailChimpIdString!=null){
        	if (mailChimpIdString.equals(MAP_ENTRY_RESERVED)) {
                throw new MailchimpMappingReservedException();
            }
        	mailChimpId = Integer.valueOf(mailChimpIdString.substring(SEGMENT_TYPE.length()+1));
        }
        return mailChimpId;
    }
    
    @Override
	public String getMozuSegmentId(Integer tenantId, Integer mailchimpId)
			throws Exception {
    	return getMozuId(tenantId, mailchimpId, SEGMENT_TYPE);
	}

	@Override
	public String putMozuSegmentId(Integer tenantId, String mozuId,
			Integer mailchimpId) throws Exception {
		 return putMozuId(tenantId, mozuId, mailchimpId, SEGMENT_TYPE);
	}
    
    @Override
    public String getMozuIndexKey(Integer tenantId, String mozuId, String type) throws Exception {
        return generateEntityId(type, mozuId);
    }
    
       
    @Override
    public String getMozuSegmentIndexKey(Integer tenantId, Integer mozuId) throws Exception {
        return getMozuIndexKey(tenantId, mozuId.toString(), SEGMENT_TYPE);
    }
    
    
    @Override
    public String getMozuProductCode(Integer tenantId, Integer mailChimpId) throws Exception {
        return getMozuId(tenantId, mailChimpId, PRODUCT_TYPE);
    }

 
   
    @Override
    public String putMozuProductCode(Integer tenantId, String mozuId, Integer mailChimpId) throws Exception {
        return putMozuId(tenantId, mozuId, mailChimpId, PRODUCT_TYPE);
    }

    
   

    @Override
    public void deleteEntry(Integer tenantId, String mozuId, String type)
            throws Exception {
    	logger.info("Delete mzdb entry for mozuId "+mozuId);
        setMappingName();

        EntityResource entityResource = new EntityResource(new MozuApiContext(tenantId));
        entityResource.deleteEntity(idMapName, generateEntityId(type, mozuId));
    }
    
    @Override
    public void deleteSegmentEntry(Integer tenantId, String mozuId)
            throws Exception {
        deleteEntry(tenantId, mozuId, SEGMENT_TYPE);
    }

 
    private IndexedProperty getIndexedProperty(String name, String type) {
        IndexedProperty property = new IndexedProperty();
        property.setPropertyName(name);
        property.setDataType(type);
        return property;
    }
  
    private String generateEntityId(String mapType, String id) {
        if (id==null) {
            return MAP_ENTRY_RESERVED;
        }
        return new StringBuilder(mapType).append("-").append(id).toString();
    }
    
    private void setMappingName() {
        if (idMapName==null) {
            String appId = AppAuthenticator.getInstance().getAppAuthInfo().getApplicationId();
    
            mozuNamespace = appId.substring(0, appId.indexOf('.'));
            idMapName = ID_MAPPINGS_LIST + "@" + mozuNamespace;
        }
    }

	

}
