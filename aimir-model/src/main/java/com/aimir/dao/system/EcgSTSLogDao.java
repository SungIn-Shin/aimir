package com.aimir.dao.system;

import java.util.List;
import java.util.Map;

import com.aimir.dao.GenericDao;
import com.aimir.model.system.EcgSTSLog;
import com.aimir.model.system.EcgSTSLogPk;

public interface EcgSTSLogDao extends GenericDao<EcgSTSLog, EcgSTSLogPk> {
	
	public List<Map<String,Object>> getSTSHistory(Map<String,Object> condition);
	public List<EcgSTSLog> getEcgSTSLog(Map<String,Object> condition);
	public EcgSTSLog getLastSetTariff(String mdsId);
}
