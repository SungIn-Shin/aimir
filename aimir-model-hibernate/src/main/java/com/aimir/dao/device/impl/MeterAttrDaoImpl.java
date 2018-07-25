package com.aimir.dao.device.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aimir.dao.AbstractHibernateGenericDao;
import com.aimir.dao.device.MeterAttrDao;
import com.aimir.dao.device.MeterDao;
import com.aimir.model.device.Meter;
import com.aimir.model.device.MeterAttr;


/**
 * SP-898
 * @author
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@Repository(value = "meterAttrDao")
public class MeterAttrDaoImpl extends AbstractHibernateGenericDao<MeterAttr, Long> implements MeterAttrDao {

	protected static Log logger = LogFactory.getLog(MeterAttrDaoImpl.class);

	@Autowired
	MeterDao meterDao;

	@Autowired
	protected MeterAttrDaoImpl(SessionFactory sessionFactory) {
		super(MeterAttr.class);
		super.setSessionFactory(sessionFactory);
	}

	public MeterAttr getByMeterId(Integer meter_id)
	{
		return findByCondition("meterId", meter_id);
	}

	public MeterAttr getByMdsId(String mdsId)
	{
		MeterAttr attr = null;
		Meter meter = meterDao.get(mdsId);
		if ( meter != null ){
			attr =  getByMeterId(meter.getId());
		}
		return attr;
	}
}
