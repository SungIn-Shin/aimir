package com.aimir.dao.mvm.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aimir.dao.AbstractHibernateGenericDao;
import com.aimir.dao.mvm.NetMonitoringDao;
import com.aimir.model.mvm.NetMonitoring;
import com.aimir.util.StringUtil;

@Repository(value = "netMonitoringDao")
public class NetMonitoringDaoImpl extends AbstractHibernateGenericDao<NetMonitoring, Integer> implements NetMonitoringDao {

	private static Log log = LogFactory.getLog(NetMonitoringDaoImpl.class);
	
	@Autowired
	protected NetMonitoringDaoImpl(SessionFactory sessionFactory) {
		super(NetMonitoring.class);
		super.setSessionFactory(sessionFactory);
	}
	
	protected NetMonitoringDaoImpl(Class<NetMonitoring> persistentClass) {
		super(persistentClass);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getNetMonitoringGridData(Map<String, Object> conditions, boolean isCount) {

		Map<String, Object> result = new HashMap<String, Object>();
		
		String supplierId = StringUtil.nullToBlank(conditions.get("supplierId"));
		String searchStartDate = StringUtil.nullToBlank(conditions.get("searchStartDate"));
		String searchEndDate = StringUtil.nullToBlank(conditions.get("searchEndDate"));
		String groupId = StringUtil.nullToBlank(conditions.get("groupId"));
		Integer page = (Integer)conditions.get("page");
		Integer limit = (Integer)conditions.get("limit");
		
		Criteria criteria = getSession().createCriteria(NetMonitoring.class);

		if(!searchStartDate.isEmpty()) {
            criteria.add(Restrictions.ge("yyyymmddhhmmss", searchStartDate + "000000"));
		}
		if(!searchEndDate.isEmpty()) {
			criteria.add(Restrictions.le("yyyymmddhhmmss", searchEndDate + "235959"));
		}
		
		if(isCount) {
			result.put("count", criteria.list().size());
		}else {
			criteria.setFirstResult((page-1)*limit);
			criteria.setMaxResults(limit);
			
			List<NetMonitoring> list = criteria.addOrder(Order.desc("yyyymmddhhmmss")).list();
			
			result.put("list", list);
		}
		
        return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getNetMonitoringChartData(Map<String, Object> conditions) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		String supplierId = StringUtil.nullToBlank(conditions.get("supplierId"));
		String searchStartDate = StringUtil.nullToBlank(conditions.get("searchStartDate"));
		String searchEndDate = StringUtil.nullToBlank(conditions.get("searchEndDate"));
		String groupId = StringUtil.nullToBlank(conditions.get("groupId"));
		
		Criteria criteria = getSession().createCriteria(NetMonitoring.class);

		if(!searchStartDate.isEmpty()) {
            criteria.add(Restrictions.ge("yyyymmddhhmmss", searchStartDate + "000000"));
		}
		if(!searchEndDate.isEmpty()) {
			criteria.add(Restrictions.le("yyyymmddhhmmss", searchEndDate + "235959"));
		}
		
		List<NetMonitoring> list = criteria.addOrder(Order.asc("yyyymmddhhmmss")).list();
		
		result.put("list", list);
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getNetMonitoringGridData_EVN(Map<String, Object> conditions, boolean isCount) {

		Map<String, Object> result = new HashMap<String, Object>();
		
		String supplierId = StringUtil.nullToBlank(conditions.get("supplierId"));
		String searchStartDate = StringUtil.nullToBlank(conditions.get("searchStartDate"));
		String searchEndDate = StringUtil.nullToBlank(conditions.get("searchEndDate"));
		String groupId = StringUtil.nullToBlank(conditions.get("groupId"));
		Integer page = (Integer)conditions.get("page");
		Integer limit = (Integer)conditions.get("limit");
		
		Criteria criteria = getSession().createCriteria(NetMonitoring.class);

		if(!searchStartDate.isEmpty()) {
            criteria.add(Restrictions.ge("yyyymmddhhmmss", searchStartDate + "000000"));
		}
		if(!searchEndDate.isEmpty()) {
			criteria.add(Restrictions.le("yyyymmddhhmmss", searchEndDate + "235959"));
		}
		
		if(isCount) {
			result.put("count", criteria.list().size());
		}else {
			criteria.setFirstResult((page-1)*limit);
			criteria.setMaxResults(limit);
			
			List<NetMonitoring> list = criteria.addOrder(Order.desc("yyyymmddhhmmss")).list();
			
			result.put("list", list);
		}
		
        return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getNetMonitoringChartData_EVN(Map<String, Object> conditions) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		String supplierId = StringUtil.nullToBlank(conditions.get("supplierId"));
		String searchStartDate = StringUtil.nullToBlank(conditions.get("searchStartDate"));
		String searchEndDate = StringUtil.nullToBlank(conditions.get("searchEndDate"));
		String groupId = StringUtil.nullToBlank(conditions.get("groupId"));
		
		Criteria criteria = getSession().createCriteria(NetMonitoring.class);

		if(!searchStartDate.isEmpty()) {
            criteria.add(Restrictions.ge("yyyymmddhhmmss", searchStartDate + "000000"));
		}
		if(!searchEndDate.isEmpty()) {
			criteria.add(Restrictions.le("yyyymmddhhmmss", searchEndDate + "235959"));
		}
		
		List<NetMonitoring> list = criteria.addOrder(Order.asc("yyyymmddhhmmss")).list();
		
		result.put("list", list);
		
		return result;
	}
	
	
	
	
}
