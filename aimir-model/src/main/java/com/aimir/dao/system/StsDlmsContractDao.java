package com.aimir.dao.system;

import java.util.List;
import java.util.Map;

import com.aimir.dao.GenericDao;
import com.aimir.model.system.StsDlmsContract;

public interface StsDlmsContractDao extends GenericDao<StsDlmsContract, Integer>{

	/**
	 * 주어진 contract.id에 해당하는 항목을 조회한다.
	 * @param contractId
	 * @return
	 */
	public StsDlmsContract getRowByContractId(int contractId);
	
	// STS_DLMS_CONTRACT 테이블에서 미터리스트 획득
	public List<Object> getTargetStsDlmsMeterList(Map<String, Object> condition);
	
	// STS_DLMS_CONTRACT 테이블에서 업데이트 된 날짜 획득
	public List<Object> getTargetStsDlmsContractDateList(Map<String, Object> condition);
}
