/**
 * Copyright Nuri Telecom Corp.
 * 파일명: FirmwareDaoImpl
 * 작성일자/작성자 : 2010.12.06 최창희
 * @see 
 * 
 *
 * 펌웨어 관리자 페이지 DAO
 * 
 * ============================================================================
 * 수정 내역
 * NO  수정일자   수정자   수정내역
 * 
 * ============================================================================
 */
package com.aimir.dao.device.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aimir.dao.AbstractJpaDao;
import com.aimir.dao.device.FirmwareDao;
import com.aimir.model.device.Firmware;
import com.aimir.model.device.FirmwareCodi;
import com.aimir.model.device.FirmwareMCU;
import com.aimir.model.device.FirmwareModem;
import com.aimir.util.Condition;

@Repository(value = "firmwareDao")
public class FirmwareDaoImpl extends AbstractJpaDao<Firmware, Integer> implements FirmwareDao {
	private static Log logger = LogFactory.getLog(FirmwareDaoImpl.class);

	public FirmwareDaoImpl() {
		super(Firmware.class);
	}
	
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public Firmware get(int id){
		return findByCondition("id", id);
	}
	
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public Firmware getByFirmwareId(String firmwareId) {
		return findByCondition("firmwareId", firmwareId);
	}

    @Override
    public List<FirmwareMCU> getMCUFirmwareList(Map<String, Object> condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FirmwareCodi> getCodiFirmwareList(Map<String, Object> condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FirmwareModem> getModemFirmwareList(
            Map<String, Object> condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getStatisticsStr(Map<String, Object> condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> distributeFmStatusEqDetail(Map<String, Object> param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFirmwareFileMgmListCNT(Map<String, Object> condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getFirmwareFileMgmList(Map<String, Object> condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String checkExistFirmware(Map<String, Object> condition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTriggerListStep1CNT(Map<String, Object> condition,
            String locationStr) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getTriggerListStep1(Map<String, Object> condition,
            String locationStr) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getTriggerListStep2(Map<String, Object> condition)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<Firmware> getPersistentClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getSumFieldByCondition(Set<Condition> conditions,
            String field, String... groupBy) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public List<Object> getFirmwareFileList(Map<String, Object> condition) throws Exception {
		String supplier_Id = (condition.get("supplierId") == null) ? "" : String.valueOf(condition.get("supplierId"));
		String equip_kind = (condition.get("equip_kind") == null) ? "" : String.valueOf(condition.get("equip_kind"));
		String fileName = (condition.get("fileName") == null) ? "" : String.valueOf(condition.get("fileName"));
		String modelName = (condition.get("modelName") == null) ? "" : String.valueOf(condition.get("modelName"));
		String modelId = (condition.get("modelId") == null) ? "" : String.valueOf(condition.get("modelId"));
		String fwVer = (condition.get("fwVer") == null) ? "" : String.valueOf(condition.get("fwVer"));
		String niFwVer =  (condition.get("ni_fwVer") == null) ? "" : String.valueOf(condition.get("ni_fwVer"));
		
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append("SELECT        			                    \n");		
		sqlBuf.append("       frm.EQUIP_MODEL,                      \n");
		sqlBuf.append("       frm.FIRMWARE_ID,                      \n");
		sqlBuf.append("       frm.SUPPLIER_ID,                      \n");
		sqlBuf.append("       frm.EQUIP_KIND,                       \n");
		sqlBuf.append("       frm.EQUIP_TYPE,                       \n");
		sqlBuf.append("       frm.EQUIP_VENDOR,                     \n");
		sqlBuf.append("       frm.ARM,                              \n");
		sqlBuf.append("       frm.HW_VERSION,                       \n");
		sqlBuf.append("       frm.FW_VERSION,                       \n");
		sqlBuf.append("       frm.BUILD,                            \n");
		sqlBuf.append("       frm.RELEASED_DATE,                    \n");
		sqlBuf.append("       frm.BINARYFILENAME,                   \n");
		sqlBuf.append("       frm.DEVICEMODEL_ID,                   \n");
		sqlBuf.append("       frm.CHECK_SUM,		                \n");
		sqlBuf.append("       frm.CRC,		                        \n");
		sqlBuf.append("       frm.IMAGE_KEY,		                \n");
		sqlBuf.append("       frm.FILE_PATH,	                    \n");
		sqlBuf.append("       frm.FILE_URL_PATH,	                \n");
		sqlBuf.append("       frm.ID                                \n");
		sqlBuf.append("FROM   FIRMWARE frm                          \n");
		sqlBuf.append("WHERE 1 = 1                        			\n");
		
		if (!supplier_Id.isEmpty())
			sqlBuf.append("AND frm.SUPPLIER_ID = '" + Integer.parseInt(supplier_Id) + "' 	\n");		
		if (!equip_kind.isEmpty())
			sqlBuf.append("AND frm.EQUIP_KIND = '" + equip_kind + "' 						\n");
		if (!fileName.isEmpty())
			sqlBuf.append("AND frm.FILENAME = '" + fileName + "%' 							\n");
		if (!modelName.isEmpty())
			sqlBuf.append("AND frm.EQUIP_MODEL = '" + modelName + "%' 						\n");
		if (!modelId.isEmpty())
			sqlBuf.append("AND frm.DEVICEMODEL_ID = '" + Integer.parseInt(modelId) + "' 	\n");
		if (!fwVer.isEmpty())
			sqlBuf.append("AND frm.FW_VERSION = '" + fwVer + "%' 							\n");
		if (!niFwVer.isEmpty())
			sqlBuf.append("AND frm.NI_FW_VERSION = '" + niFwVer + "' 						\n");
		
		logger.debug("Firmware sqlBuf : " + sqlBuf.toString());
		
		Query query = em.createNativeQuery(sqlBuf.toString());
		List<Object> result = query.getResultList();
		
		return result;
	}
	
}
