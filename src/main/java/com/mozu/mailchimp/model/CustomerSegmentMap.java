package com.mozu.mailchimp.model;

import java.util.List;

/**
 * Maps a segment to all the customers that belongs to the segment
  * @author lnair
 */
public class CustomerSegmentMap {
	
	protected String segmentId;
    protected List<Integer> customerIds;
    /**
	 * @return the customerId
	 */
	public List<Integer> getCustomerIds() {
		return customerIds;
	}
	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerIds(List<Integer> customerIds) {
		this.customerIds = customerIds;
	}
	/**
	 * @return the segmentId
	 */
	public String getSegmentId() {
		return segmentId;
	}
	/**
	 * @param segmentId the segmentId to set
	 */
	public void setSegmentId(String segmentId) {
		this.segmentId = segmentId;
	}
	
	
    
    
}
