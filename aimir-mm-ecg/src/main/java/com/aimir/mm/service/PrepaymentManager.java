package com.aimir.mm.service;

import java.util.Map;

import com.aimir.mm.model.RequestAuthToken;

public interface PrepaymentManager {
	
	public Map<String,Object> customerRecharge(RequestAuthToken requestAuthToken, String meter_number, Double recharge_amount);
	public Map<String,Object> vendorRecharge(RequestAuthToken requestAuthToken, String meter_number, String vendor_id, Double recharge_amount);
	public Map<String,Object> rechargeCancel(Long id, String operatorId, String reason);
	
}
