package com.aimir.dao.system.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aimir.dao.AbstractHibernateGenericDao;
import com.aimir.dao.system.StsContractDao;
import com.aimir.model.system.StsContract;

@Repository(value="stsContractDao")
public class StsContractDaoImpl extends AbstractHibernateGenericDao<StsContract, Integer> implements StsContractDao {

	Log logger = LogFactory.getLog(StsContractDaoImpl.class);
	
	/*@Autowired
	protected StsContractDaoImpl(Class<StsContract> persistentClass) {
		super(persistentClass);
		// TODO Auto-generated constructor stub
	}	*/
	
	@Autowired
	protected StsContractDaoImpl(SessionFactory sessionFactory) {
		super(StsContract.class);
		super.setSessionFactory(sessionFactory);
		// TODO Auto-generated constructor stub
	}	
	

	//
	@Override
	public StsContract getRowByContractId(int contractId) {
		StsContract sc = findByCondition("contractId", contractId);
        if (sc != null) {
            Hibernate.initialize(sc.getStsNumber());
            Hibernate.initialize(sc.getMdevId());
        }
        return sc;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getTargetStsMeterList (Map<String, Object> condition) {
		/**
		 * row : StsContract 테이블에서 미터리스트, STS 넘버 조회
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT       con.mdev_id as mdev_id, ");
		sb.append("\n       con.sts_number as sts_number, ");
		sb.append("\n       con.contract_id as contract_id ");
		sb.append("\n FROM      stscontract con");		
		
		Query query = getSession().createSQLQuery(sb.toString());
		
		return query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getTargetStsContractDateList (Map<String, Object> condition) {
		/**
		 * row : StsContract 테이블에서 stscontract_date 조회
		 */
		Integer contractId = (Integer)condition.get("contractId");
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT     stscontract_date as stscontract_date ");
		sb.append("\n FROM      stscontract  ");		
		sb.append("\n WHERE   contract_id = " + contractId );		
		
		Query query = getSession().createSQLQuery(sb.toString());
		
		return query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
	}

}
