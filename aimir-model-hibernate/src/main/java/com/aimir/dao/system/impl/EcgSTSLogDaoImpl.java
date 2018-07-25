package com.aimir.dao.system.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aimir.dao.AbstractHibernateGenericDao;
import com.aimir.dao.system.EcgSTSLogDao;
import com.aimir.model.system.EcgSTSLog;
import com.aimir.model.system.EcgSTSLogPk;

@Repository(value = "ecgSTSLogDao")
public class EcgSTSLogDaoImpl extends AbstractHibernateGenericDao<EcgSTSLog, EcgSTSLogPk> implements EcgSTSLogDao {
    private static Log logger = LogFactory.getLog(EcgSTSLogDaoImpl.class);
    
	@Autowired
	protected EcgSTSLogDaoImpl(SessionFactory sessionFactory) {
		super(EcgSTSLog.class);
		super.setSessionFactory(sessionFactory);
	}
	
	public List<Map<String,Object>> getSTSHistory(Map<String,Object> condition) {
		Query query = null;
		String meterNumber = condition.get("meterNumber") == null ? null : (String) condition.get("meterNumber");
		String cmd = condition.get("cmd") == null ? null : (String) condition.get("cmd");
		String startDate = condition.get("startDate") == null ? null : (String) condition.get("startDate");
		String endDate = condition.get("endDate") == null ? null : (String) condition.get("endDate");
		Integer result = condition.get("result") == null ? null : (Integer)condition.get("result");
		Integer page = condition.get("page") == null ? null : (Integer)condition.get("page");
		Integer limit = condition.get("limit") == null ? null : (Integer)condition.get("limit");
		Boolean isTotalCnt = condition.get("isTotalCnt") == null ? false : (Boolean)condition.get("isTotalCnt");
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		
		try {
			StringBuffer sb = new StringBuffer();
			if(isTotalCnt) {
				sb.append("SELECT count(*) as CNT ");
			} else {
				sb.append("SELECT ecgLog.id.meterNumber AS METERNUMBER, ");
				sb.append("\n     ecgLog.id.cmd AS CMD, ");
				sb.append("\n	  ecgLog.id.createDate AS CREATEDATE, ");
				sb.append("\n	  ecgLog.id.seq AS SEQ, ");
				sb.append("\n 	  ecgLog.id.asyncTrId AS ASYNCTRID, ");

//				sb.append("\n 	  ecgLog.payMode AS PAYMODE, ");
				sb.append("\n 	  CASE WHEN ecgLog.payMode=4 THEN 'prepay' ");
				sb.append("\n 	       WHEN ecgLog.payMode=8 THEN 'postpay' ELSE '' END AS PAYMODE, ");
				
				
//				sb.append("\n 	  CASE WHEN ecgLog.payMode=1 THEN 'Postpay(Manual OFF)' ");
//				sb.append("\n 	       WHEN ecgLog.payMode=3 THEN 'Prepaid' ");
//				sb.append("\n 	       WHEN ecgLog.payMode=2 THEN 'Manual ON' ");
//				sb.append("\n 	       WHEN ecgLog.payMode=0 THEN 'Disable STS modlue control' ELSE '' END AS PAYMODE, ");
				
				sb.append("\n 	  ecgLog.result AS RESULT, ");
				sb.append("\n 	  ecgLog.failReason AS FAILREASON, ");
				sb.append("\n 	  ecgLog.resultDate AS RESULTDATE, ");
				sb.append("\n 	  ecgLog.tokenDate AS TOKENDATE, ");
				sb.append("\n	  ecgLog.token AS TOKEN, ");
				sb.append("\n 	  ecgLog.chargedCredit AS CHARGEDCREDIT, ");
				sb.append("\n 	  ecgLog.getDate AS GETDATE, ");
//				sb.append("\n 	  ecgLog.emergencyCreditMode AS ECMODE, ");
				sb.append("\n 	  CASE WHEN ecgLog.emergencyCreditMode=1 THEN 'Enable(1)' ");
				sb.append("\n 	       WHEN ecgLog.emergencyCreditMode=0 THEN 'Disalbe(0)' ELSE '' END AS ECMODE, ");
				sb.append("\n 	  ecgLog.emergencyCreditDay AS EMERGENCYCREDITDAY, ");
//				sb.append("\n 	  ecgLog.tariffMode AS TARIFFMODE, ");
				sb.append("\n 	  CASE WHEN ecgLog.tariffMode=1 THEN 'Future tariff(1)' ");
				sb.append("\n 	       WHEN ecgLog.tariffMode=0 THEN 'Current tariff(0)' ELSE '' END AS TARIFFMODE, ");
				sb.append("\n 	  ecgLog.tariffKind AS TARIFFKIND, ");
				sb.append("\n 	  ecgLog.tariffCount AS TARIFFCOUNT, ");
				sb.append("\n 	  ecgLog.condLimit1 AS CONDLIMIT1, ");
				sb.append("\n 	  ecgLog.condLimit2 AS CONDLIMIT2, ");
				sb.append("\n 	  ecgLog.consumption AS CONSUMPTION, ");
				sb.append("\n 	  ecgLog.fixedRate AS FIXEDRATE, ");
				sb.append("\n 	  ecgLog.varRate AS VARRATE, ");
				sb.append("\n 	  ecgLog.condRate1 AS CONDRATE1, ");
				sb.append("\n 	  ecgLog.condRate2 AS CONDRATE2, ");
				sb.append("\n 	  ecgLog.tariffDate AS TARIFFDATE, ");
				sb.append("\n 	  ecgLog.remainingCreditDate AS REMAININGCREDITDATE, ");
				sb.append("\n 	  ecgLog.remainingCredit AS REMAININGCREDIT, ");
				sb.append("\n 	  ecgLog.netChargeYyyymm AS NETCHARGEYYYYMM, ");
				sb.append("\n 	  ecgLog.netChargeMonthConsumption AS NETCHARGEMONTHCONSUMPTION, ");
				sb.append("\n 	  ecgLog.netChargeMonthCost AS NETCHARGEMONTHCOST, ");
				sb.append("\n 	  ecgLog.netChargeYyyymmdd AS NETCHARGEYYYYMMDD, ");
				sb.append("\n 	  ecgLog.netChargeDayConsumption AS NETCHARGEDAYCONSUMPTION, ");
				sb.append("\n 	  ecgLog.netChargeDayCost AS NETCHARGEDAYCOST, ");
//				sb.append("\n 	  ecgLog.fcMode AS FCMODE, ");
				sb.append("\n 	  CASE WHEN ecgLog.fcMode=1 THEN 'Pending schedule' ");
				sb.append("\n 	       WHEN ecgLog.fcMode=0 THEN 'Current schedule' ELSE '' END AS FCMODE, ");
				sb.append("\n 	  ecgLog.stsNumber AS STSNUMBER, ");
				sb.append("\n 	  ecgLog.kct1 AS KCT1, ");
				sb.append("\n 	  ecgLog.kct2 AS KCT2, ");
				sb.append("\n 	  ecgLog.channel AS CHANNEL, ");
				sb.append("\n 	  ecgLog.panId AS PANID, ");
				sb.append("\n 	  ecgLog.friendlyDate AS FRIENDLYDATE, ");
				sb.append("\n 	  ecgLog.friendlyDayType AS FRIENDLYDAYTYPE, ");
				sb.append("\n 	  ecgLog.friendlyFromHHMM AS FRIENDLYFROMHHMM, ");
				sb.append("\n 	  ecgLog.friendlyEndHHMM AS FRIENDLYENDHHMM, ");
				
				sb.append("\n 	  ecgLog.activeEnergyCharge AS ACTIVEENERGYCHARGE, ");
				sb.append("\n 	  ecgLog.govLey AS GOVLEVY, ");
				sb.append("\n 	  ecgLog.streetLightLevy AS STREETLIGHTLEVY, ");
				sb.append("\n 	  ecgLog.vat AS VAT, ");
				sb.append("\n 	  ecgLog.lifeLineSubsidy AS LIFELINESUBSIDY, ");
				sb.append("\n 	  ecgLog.friendlyCreditAmount AS FRIENDLYCREDITAMOUNT, ");
				sb.append("\n 	  ecgLog.emergencyCreditAmount AS EMERGENCYCREDITAMOUNT, ");
				sb.append("\n 	  ecgLog.switchTime AS SWITCHTIME ");
			}
			
			sb.append("\nFROM EcgSTSLog ecgLog ");
			sb.append("\nWHERE 1=1 ");
			if(meterNumber != null && !meterNumber.isEmpty()) {
				sb.append("\nAND ecgLog.id.meterNumber=:meterNumber ");
			}
			if(cmd != null && !cmd.isEmpty()) {
				sb.append("\nAND ecgLog.id.cmd=:cmd ");
			}
			if(result != null) {
				if(result == 100) {  //Unknow
					sb.append("\nAND ((ecgLog.result <> 0 and ecgLog.result <> 1) or ecgLog.result is null) ");
				} else {
					sb.append("\nAND ecgLog.result = :result ");
				}
			}
			if(startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
				sb.append("\nAND (ecgLog.id.createDate >= :startDate AND ecgLog.id.createDate <= :endDate) ");
			}
			if(!isTotalCnt) {
				sb.append("\nORDER BY ecgLog.id.createDate desc ");
			}
			
			query = getSession().createQuery(sb.toString());
			if(meterNumber != null && !meterNumber.isEmpty()) {
				query.setString("meterNumber",meterNumber);
			}
			if(cmd != null && !cmd.isEmpty()) {
				query.setString("cmd", cmd);
			}
			if(result != null) {
				if(result != 100) {  //!Unknow
					query.setInteger("result", result);
				}
			}
			if(startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
				query.setString("startDate", startDate);
				query.setString("endDate", endDate);
			}
			
	        if (!isTotalCnt) {
	        	query.setFirstResult((page - 1) * limit);
	            query.setMaxResults(limit);
	        }
	        returnList = query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
		} catch (Exception e) {
			logger.error(e,e);
		}
		
		return returnList;
	}
	
	public List<EcgSTSLog> getEcgSTSLog(Map<String,Object> condition) {

		String meterNumber = (String)condition.get("meterNumber");
		String createDate = (String)condition.get("createDate");
		String cmd = (String)condition.get("cmd"); 
		Long trId = (Long)condition.get("trId");
		
		StringBuffer sb = new StringBuffer();
		sb.append("\nFROM EcgSTSLog log ");
		sb.append("\nWHERE 1=1");
		if(meterNumber != null) {
			sb.append("\nAND log.id.meterNumber=:meterNumber ");
		}
		if(createDate != null) {
			sb.append("\nAND log.id.createDate=:createDate");
		}
		if(trId != null) {
			sb.append("\nAND log.id.asyncTrId=:trId");
		}
		if(cmd != null) {
			sb.append("\nAND log.id.cmd=:cmd");
		}
		sb.append("\nORDER BY log.id.createDate");
		Query query = getSession().createQuery(sb.toString());
		if(meterNumber != null) {
			query.setString("meterNumber", meterNumber);
		}
		if(createDate != null) {
			query.setString("createDate", createDate);
		}
		if(cmd != null) {
			query.setString("cmd", cmd);
		}
		if(trId != null) {
			query.setLong("trId", trId);
		}
		
		return query.list();
	}
	
	public EcgSTSLog getLastSetTariff(String mdsId) {
		EcgSTSLog stslog = null;
		String cmd = "cmdSetTariff";
		
		StringBuffer sb = new StringBuffer();
		sb.append("");
		sb.append("\nFROM EcgSTSLog");
		sb.append("\nWHERE id.cmd=:cmd");
		sb.append("\nAND id.meterNumber=:meterNumber");
		sb.append("\nAND id.createDate=(SELECT max(id.createDate)");
		sb.append("\n   				FROM   EcgSTSLog");
		sb.append("\n	 				WHERE  id.cmd=:cmd");
		sb.append("\n	 				AND    id.meterNumber=:meterNumber");
		sb.append("\n	 				AND    result=:result)");
		sb.append("\nAND result=:result");
		
		Query query = getSession().createQuery(sb.toString());
		query.setString("cmd", cmd);
		query.setString("meterNumber", mdsId);
		query.setInteger("result", 0);
		
		List<Object> list = query.list();
		if(list.size() > 0) {
			stslog = (EcgSTSLog) query.list().get(0);
		}
		
		return stslog;
	}
}
