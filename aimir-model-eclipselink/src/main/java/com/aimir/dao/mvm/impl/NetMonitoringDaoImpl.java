package com.aimir.dao.mvm.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.aimir.dao.AbstractJpaDao;
import com.aimir.dao.mvm.NetMonitoringDao;
import com.aimir.model.mvm.NetMonitoring;
import com.aimir.util.Condition;

@Repository(value = "netMonitoringDao")
public class NetMonitoringDaoImpl extends AbstractJpaDao<NetMonitoring, Integer> implements NetMonitoringDao {

	private static Log log = LogFactory.getLog(NetMonitoringDaoImpl.class);

	@Override
	public Class<NetMonitoring> getPersistentClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring findById(Integer id, boolean lock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring findByCondition(String condition, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NetMonitoring> findByConditions(Set<Condition> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> findTotalCountByConditions(Set<Condition> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getSumFieldByCondition(Set<Condition> conditions, String field, String... groupBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NetMonitoring> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NetMonitoring> findByExample(NetMonitoring exampleInstance, String[] excludeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring saveOrUpdate(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring saveOrUpdate_requires_new(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring add(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring add_requires_new(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(Integer id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NetMonitoring codeAdd(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring codeParentAdd(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring codeUpdate(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void codeDelete(NetMonitoring entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NetMonitoring groupAdd(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring groupSaveOrUpdate(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring groupUpdate(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void groupDelete(NetMonitoring entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NetMonitoring mcuAdd(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring meterAdd(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring modemAdd(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring get(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring update(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring update(NetMonitoring entity, Properties addCriteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetMonitoring update_requires_new(NetMonitoring entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(NetMonitoring entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int deleteById(Integer id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteAll() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void evict(Object entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void merge(Object entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flushAndClear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(Object entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveOrUpdateAll(Collection<?> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getNetMonitoringGridData(Map<String, Object> conditions, boolean isCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getNetMonitoringChartData(Map<String, Object> conditions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getNetMonitoringGridData_EVN(Map<String, Object> conditions, boolean isCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getNetMonitoringChartData_EVN(Map<String, Object> conditions) {
		// TODO Auto-generated method stub
		return null;
	}


}
