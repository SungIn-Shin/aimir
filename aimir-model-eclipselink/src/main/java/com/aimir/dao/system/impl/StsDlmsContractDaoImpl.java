package com.aimir.dao.system.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aimir.dao.AbstractJpaDao;
import com.aimir.dao.system.StsDlmsContractDao;
import com.aimir.model.system.StsDlmsContract;
import com.aimir.util.Condition;

public class StsDlmsContractDaoImpl  extends AbstractJpaDao<StsDlmsContract, Integer> implements StsDlmsContractDao {
	
	Log logger = LogFactory.getLog(StsDlmsContractDaoImpl.class);

	@Override
	public Class<StsDlmsContract> getPersistentClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getSumFieldByCondition(Set<Condition> conditions, String field, String... groupBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StsDlmsContract getRowByContractId(int contractId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getTargetStsDlmsMeterList(Map<String, Object> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getTargetStsDlmsContractDateList(Map<String, Object> condition) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
