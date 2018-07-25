package com.aimir.service.mvm;

import java.util.List;
import java.util.Map;


public interface NetMonitoringManager {
	public List<Map<String, Object>> getNetMonitoringGridData(Map<String, Object> conditions);
	public Integer getNetMonitoringTotal(Map<String, Object> conditions);

	public List<List<Map<String, Object>>> getNetMonitoringChartData(Map<String, Object> conditions);

	public List<Map<String, Object>> getNetMonitoringExcelData(Map<String, Object> conditions);
	
	
	public List<Map<String, Object>> getNetMonitoringGridData_EVN(Map<String, Object> conditions);
	public Integer getNetMonitoringTotal_EVN(Map<String, Object> conditions);
	
}
