package com.aimir.dao.system.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aimir.dao.AbstractJpaDao;
import com.aimir.dao.system.StsContractDao;
import com.aimir.model.system.StsContract;
import com.aimir.util.Condition;

public class StsContractDaoImpl  extends AbstractJpaDao<StsContract, Integer> implements StsContractDao {
	
	Log logger = LogFactory.getLog(StsContractDaoImpl.class);

	@Override
	public Class<StsContract> getPersistentClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getSumFieldByCondition(Set<Condition> conditions, String field, String... groupBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StsContract getRowByContractId(int contractId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getTargetStsMeterList(Map<String, Object> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getTargetStsContractDateList(Map<String, Object> condition) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
