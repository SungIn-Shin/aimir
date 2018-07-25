package com.aimir.dao.mvm;


import java.util.Map;

import com.aimir.dao.GenericDao;
import com.aimir.model.mvm.NetMonitoring;


public interface NetMonitoringDao extends GenericDao<NetMonitoring, Integer> {
	public Map<String, Object> getNetMonitoringGridData(Map<String, Object> conditions, boolean isCount);
	public Map<String, Object> getNetMonitoringChartData(Map<String, Object> conditions);
	
	public Map<String, Object> getNetMonitoringGridData_EVN(Map<String, Object> conditions, boolean isCount);
	public Map<String, Object> getNetMonitoringChartData_EVN(Map<String, Object> conditions);
}
