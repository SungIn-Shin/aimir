package com.aimir.dao.device.impl;

import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.aimir.dao.AbstractJpaDao;
import com.aimir.dao.device.OperationListDao;
import com.aimir.model.device.OperationList;
import com.aimir.util.Condition;

@Repository(value = "operationlistDao")
public class OperationListDaoImpl extends AbstractJpaDao<OperationList, Integer> implements OperationListDao {

    public OperationListDaoImpl() {
		super(OperationList.class);
	}

	/*
	@SuppressWarnings("unchecked")
	public List<OperationList> getOperationListByConstraintId(int constraintId) {

		String sql = " FROM OperationList operationList WHERE operationList.constraintId = :constraintId ";
		
		Query query = getSession().createQuery(sql);
		query.setInteger("constraintId", constraintId);
		
		return query.list();
	}
	*/
	
	@SuppressWarnings("unchecked")
	public List<OperationList> getOperationListByOperationCodeId(int operationCodeId) {
		String sql = "select o from OperationList o where operationCode.id = :operationCodeId";
		Query query = em.createQuery(sql, OperationList.class);
		query.setParameter("operationCodeId", operationCodeId);
		return query.getResultList();
	}

    /**
     * method name : getOperationListByModelId<b/>
     * method Desc : MDIS - Meter Management 맥스가젯에서 선택한 Meter 의 Model ID 에 해당하는 OperationList 를 조회한다.
     *
     * @param modelId aimir.model.system.DeviceModel.id
     * @param operationCodeList List of aimir.model.device.OperationList.operationCode.code
     * @return List of aimir.model.device.OperationList
     */
    @SuppressWarnings("unchecked")
    public List<OperationList> getOperationListByModelId(Integer modelId, List<String> operationCodeList) {
        StringBuilder sb = new StringBuilder();
        sb.append("select op ");
        sb.append("\nFROM OperationList op ");
        sb.append("\nWHERE op.model.id = :modelId ");
        sb.append("\nAND   op.operationCode.code IN (:operationCodeList) ");

        Query query = em.createQuery(sb.toString(), OperationList.class);
        query.setParameter("modelId", modelId);
        query.setParameter("operationCodeList", operationCodeList);

        return query.getResultList();
    }

    @Override
    public Class<OperationList> getPersistentClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getSumFieldByCondition(Set<Condition> conditions,
            String field, String... groupBy) {
        // TODO Auto-generated method stub
        return null;
    }
}