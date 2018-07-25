package com.aimir.dao.device;

import java.util.List;

import com.aimir.dao.GenericDao;
import com.aimir.model.device.OperationList;

public interface OperationListDao extends GenericDao<OperationList, Integer> {
	
	List<OperationList> getOperationListByOperationCodeId(int operationCodeId);

    /**
     * method name : getOperationListByModelId<b/>
     * method Desc : MDIS - Meter Management 맥스가젯에서 선택한 Meter 의 Model ID 에 해당하는 OperationList 를 조회한다.
     *
     * @param modelId aimir.model.system.DeviceModel.id
     * @param operationCodeList List of aimir.model.device.OperationList.operationCode.code
     * @return List of aimir.model.device.OperationList
     */
    public List<OperationList> getOperationListByModelId(Integer modelId, List<String> operationCodeList);
}