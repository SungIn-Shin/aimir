package com.aimir.dao.system.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.aimir.dao.AbstractJpaDao;
import com.aimir.dao.system.EcgSTSLogDao;
import com.aimir.model.system.EcgSTSLog;
import com.aimir.model.system.EcgSTSLogPk;
import com.aimir.util.Condition;

@Repository(value = "ecgSTSLogDao")
public class EcgSTSLogDaoImpl extends AbstractJpaDao<EcgSTSLog, EcgSTSLogPk> implements EcgSTSLogDao {

	@Override
	public Class<EcgSTSLog> getPersistentClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getSumFieldByCondition(Set<Condition> conditions, String field, String... groupBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> getSTSHistory(Map<String, Object> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EcgSTSLog> getEcgSTSLog(Map<String, Object> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EcgSTSLog getLastSetTariff(String mdsId) {
		// TODO Auto-generated method stub
		return null;
	}

}
