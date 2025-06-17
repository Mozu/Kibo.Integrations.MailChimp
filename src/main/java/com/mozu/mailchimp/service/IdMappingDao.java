package com.mozu.mailchimp.service;

import com.mozu.api.ApiContext;

/**
 * @author lakshmi_nair
 *
 */
/**
 * @author lakshmi_nair
 *
 */
public interface IdMappingDao {
 
    public static final String PRODUCT_TYPE = "product";
    public static final String SEGMENT_TYPE = "segment";
  
    public static final String TYPE_MOZU_ID_FIELD = "typeMozuId";
    public static final String TYPE_MAILCHIMP_ID_FIELD = "typeMailchimpId";
       
    public  String EVENT_MAPPING_LIST = "mailchimpEventIdMapping";
    public  String TYPE_MOZU_EVENT_FIELD = "entityId";
    
    public  String ECOMMERCE_MAPPING_LIST = "mailchimpEcommerceIdMapping";
    public  String TYPE_MOZU_ORDER_FIELD = "orderId";



    /**
     * Create or update the ID mapping schema in MZDB for the tenant
     * 
     * @param tenantId
     * @throws Exception
     */
    public void installSchema(Integer tenantId) throws Exception;
    
    /**
     * Create or update the Event mapping schema in MZDB for the tenant
     * 
     * @param tenantId
     * @throws Exception
     */
    public void installEventSchema(ApiContext apiContext) throws Exception;
    
    /**
     * Create or update the Ecommerce360 mapping schema in MZDB for the tenant
     * 
     * @param tenantId
     * @throws Exception
     */
    
    public void installEcommerce360Schema(ApiContext apiContext) throws Exception;
    
    /**
     * Get the Mozu id given the MailChimp id
     * 
     * @param tenantId tenant 
     * @param mailChimpId id of the MailChimp object
     * @return id of the Mozu object if known, null otherwise
     * @param type id type, CATEGORY_TYPE | DISCOUNT_TYPE | PRODUCT_TYPE
     * @throws Exception exception thrown for data access errors
     */
    public String getMozuId( Integer tenantId, Integer mailChimpId, String type) throws Exception;
    
    /**
     * Create an entry in the id map for the Mozu id/MailChimp id pair
     * 
     * @param tenantId tenant
     * @param mozuId id of the Mozu object
     * @param mailChimpId id of the MailChimp object
     * @param type id type, CATEGORY_TYPE | DISCOUNT_TYPE | PRODUCT_TYPE
     * @return The typeId of the entry created in the form [type]-[mozuId]
     * @throws Exception thrown if an entry already exists with this MozuId or this
     *   MailChimp Id
     */
    public String putMozuId( Integer tenantId, String mozuId, Integer mailChimpId, String type) throws Exception;
    
    
    /**
     * Get the MailChimp id given the Mozu id and the type
     * 
     * @param tenantId tenant
     * @param mozuId id of the Mozu object
     * @param type id type, CATEGORY_TYPE | DISCOUNT_TYPE | PRODUCT_TYPE
     * @return id of the MailChimp if known, null otherwise
     * @throws Exception exception thrown for data access errors
     */
    public String getMailChimpId(Integer tenantId, String mozuProdCode, String type) throws Exception;
    
     /**
     * Get the MailChimp product id given the Mozu product id
     * (Convenience function)
     * 
     * @param tenantId tenant for the product
     * @param mozuId id of the Mozu product
     * @return id of the MailChimp product if known, null otherwise
     * @throws Exception exception thrown for data access errors
     */
    public Integer getMailChimpProductId(Integer tenantId, String mozuId) throws Exception;

    /**
    * Get the MailChimp Segment id given the Mozu segment id
    * (Convenience function)
    * 
    * @param tenantId tenant for the segment
    * @param mozuId id of the Mozu segment
    * @return id of the MailChimp segment if known, null otherwise
    * @throws Exception exception thrown for data access errors
    */
    public Integer getMailChimpSegmentId(Integer tenantId, String mozuId) throws Exception ;
     /**
     * Get the Mozu product id given the MailChimp product id
     * 
     * @param tenantId tenant for the product
     * @param mailchimpId id of the MailChimp product
     * @return id of the Mozu product if known, null otherwise
     * @throws Exception exception thrown for data access errors
     */
    public String getMozuProductCode( Integer tenantId, Integer mailchimpId) throws Exception;

     
    /**
     * Get the Mozu segment id given the MailChimp segment id
     * 
     * @param tenantId tenant for the segment
     * @param mailchimpId id of the MailChimp segment
     * @return id of the Mozu segment if known, null otherwise
     * @throws Exception exception thrown for data access errors
     */
    public String getMozuSegmentId( Integer tenantId, Integer mailchimpId) throws Exception;
    
    /**
     * Create an entry in the product id map for the Mozu id/MailChimp id pair
     * 
     * @param tenantId tenant for the product
     * @param mozuId id of the Mozu product
     * @param mailchimpId id of the MailChimp product
     * @return The typeId of the entry created in the form product-<mozuId>.
     * @throws Exception thrown if an entry already exists with this MozuId or this
     *   MailChimp Id 
     */
    public String putMozuProductCode( Integer tenantId, String mozuId, Integer mailchimpId) throws Exception;

    
    /**
     * Create an entry in the segment id map for the Mozu id/MailChimp id pair
     * 
     * @param tenantId tenant for the segment
     * @param mozuId id of the Mozu segment
     * @param mailchimpId id of the MailChimp segment
     * @return The typeId of the entry created in the form segment-<mozuId>.
     * @throws Exception thrown if an entry already exists with this MozuId or this
     *   MailChimp Id 
     */
    public String putMozuSegmentId( Integer tenantId, String mozuId, Integer mailchimpId) throws Exception;
    
    /**
     * Delete an entry in the id map
     * 
     * @param tenantId tenant
     * @param mozuId id of the Mozu object
     * @param type id type, CATEGORY_TYPE | DISCOUNT_TYPE | PRODUCT_TYPE
     * @throws Exception
     */
    public void deleteEntry(Integer tenantId, String mozuId, String type) throws Exception;
    
    /**
     * Update a previously created entry with a LightSpeedId
     * 
     * @param tenantId tenant
     * @param indexKey entity key returned from a previously run put operation
     * @param lsId new LightSpeed id
     * @throws Exception
     */
    public void updateEntry(Integer tenantId, String indexKey, Integer mcId) throws Exception;

    /**
     * Get the Mozu IndexKey
     * @param tenantId
     * @param mozuId
     * @param type
     * @return
     * @throws Exception
     */
    public String getMozuIndexKey(Integer tenantId, String mozuId, String type) throws Exception;
    
    /**
     * Get the Mozu segment IndexKey
     * @param tenantId
     * @param mozuId
     * @return
     * @throws Exception
     */
    
   public String getMozuSegmentIndexKey(Integer tenantId, Integer mozuId) throws Exception;
    
    /**
     * Delete segment entry in the id map
     * 
     * @param tenantId tenant
     * @param mozuId id of the Mozu object
    * @throws Exception
     */

    public void deleteSegmentEntry(Integer tenantId, String mozuId)throws Exception ;

   

}
