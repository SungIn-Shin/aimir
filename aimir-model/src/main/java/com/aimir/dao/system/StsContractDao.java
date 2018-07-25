package com.aimir.dao.system;

import java.util.List;
import java.util.Map;

import com.aimir.dao.GenericDao;
import com.aimir.model.system.StsContract;

public interface StsContractDao extends GenericDao<StsContract, Integer>{

	/**
	 * 주어진 contract.id에 해당하는 항목을 조회한다.
	 * @param contractId
	 * @return
	 */
	public StsContract getRowByContractId(int contractId);
	
	// StsContract 테이블에서 미터리스트 획득
	public List<Object> getTargetStsMeterList(Map<String, Object> condition);
	// StsContract 테이블에서 업데이트 된 날짜 획득
	public List<Object> getTargetStsContractDateList(Map<String, Object> condition);
}
