package com.aimir.cms.service;

import java.util.List;
import java.util.Map;

import com.aimir.cms.model.DebtEnt;
import com.aimir.model.system.Contract;
import com.aimir.model.system.Operator;

public interface DebtEntManager {
	public List<Map<String, Object>> getPrepaymentChargeList(Map<String, Object> condition) throws Exception;
	public Integer getPrepaymentChargeListTotalCount(Map<String, Object> condition);
	public List<Map<String,Object>> getDebtInfoByCustomerNo(String customerNo, String debtType, String debtRef);
	public void modifyDebtInfo(Map<String, Object> condition);
	public Map<String, Object> vendorSavePrepaymentChargeECG(Map<String, Object> condition);
	public List<Map<String, Object>> getDebtArrearsLog(Long prepaymentLogId);
	public Map<String,Object> cancelTransaction(Long id, String operatorId, String reason);
		
	public Map<String,Object> getVendorCustomerReceiptDataWithDebt(Map<String,Object> condition);
	public Map<String, Object> getDepositHistoryList(Map<String,Object> condition);
	
	public Map<String,Object> partpayManagement(Contract contract, List<DebtEnt> preDebtList, List<Map<String,Object>> debtList, Operator operator, String customerNo) throws Exception;
}
