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
import com.aimir.dao.system.StsDlmsContractDao;
import com.aimir.model.system.StsDlmsContract;

@Repository(value="stsDlmsContractDao")
public class StsDlmsContractDaoImpl extends AbstractHibernateGenericDao<StsDlmsContract, Integer> implements StsDlmsContractDao {

	Log logger = LogFactory.getLog(StsDlmsContractDaoImpl.class);
	
	@Autowired
	protected StsDlmsContractDaoImpl(SessionFactory sessionFactory) {
		super(StsDlmsContract.class);
		super.setSessionFactory(sessionFactory);
	}	
	

	@Override
	public StsDlmsContract getRowByContractId(int contractId) {
		StsDlmsContract sc = findByCondition("contractId", contractId);
        if (sc != null) {
            //Hibernate.initialize(sc.getStsNumber());
            Hibernate.initialize(sc.getMdevId());
        }
        return sc;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getTargetStsDlmsMeterList (Map<String, Object> condition) {
		/**
		 * row : STS_DLMS_CONTRACT 테이블에서 미터리스트  조회
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT       con.mdev_id as mdev_id, ");
		sb.append("\n       con.contract_id as contract_id ");
		sb.append("\n FROM      sts_dlms_contract con");		
		
		Query query = getSession().createSQLQuery(sb.toString());
		return query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
	}

	// 임승한 점검
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getTargetStsDlmsContractDateList (Map<String, Object> condition) {
		/**
		 * row : STS_DLMS_CONTRACT 테이블에서 sts_dlms_contract_date 조회
		 */
		Integer contractId = (Integer)condition.get("contractId");
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT     stscontract_date as stscontract_date ");
		sb.append("\n FROM      sts_dlms_contract  ");		
		sb.append("\n WHERE   contract_id = " + contractId );		
		
		Query query = getSession().createSQLQuery(sb.toString());
		
		return query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
	}

}
