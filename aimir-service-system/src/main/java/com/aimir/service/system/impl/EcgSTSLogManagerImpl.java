package com.aimir.service.system.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aimir.dao.system.EcgSTSLogDao;
import com.aimir.dao.system.SupplierDao;
import com.aimir.model.system.Supplier;
import com.aimir.service.system.EcgSTSLogManager;
import com.aimir.util.StringUtil;
import com.aimir.util.TimeLocaleUtil;

@Service(value = "ecgSTSLogManager")
@Transactional(value = "transactionManager")
public class EcgSTSLogManagerImpl implements EcgSTSLogManager{

	@Autowired
	EcgSTSLogDao stsLogDao;

	@Autowired
	SupplierDao supplierDao;
	
	public List<Map<String,Object>> getSTSHistory(Map<String,Object> condition) {
		
		List<Map<String,Object>> returnList = stsLogDao.getSTSHistory(condition);
		
        String supplierId = StringUtil.nullToBlank( condition.get("supplierId"));
        if (supplierId.length() > 0) {
            Supplier supplier = supplierDao.get(Integer.parseInt(supplierId));
            
            for (Map<String,Object> data : returnList) {
                Map<String, Object> mapData = (Map<String, Object>) data;
                mapData.put("CREATEDATE", TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(mapData.get("CREATEDATE")) , supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter()));
                mapData.put("RESULTDATE", TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(mapData.get("RESULTDATE")) , supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter()));
                mapData.put("TOKENDATE", TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(mapData.get("TOKENDATE")) , supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter()));
                mapData.put("GETDATE", TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(mapData.get("GETDATE")) , supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter()));
                mapData.put("TARIFFDATE", TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(mapData.get("TARIFFDATE")) , supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter()));
                mapData.put("REMAININGCREDITDATE", TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(mapData.get("REMAININGCREDITDATE")) , supplier.getLang().getCode_2letter(), supplier.getCountry().getCode_2letter()));
            }
        }
		
		return returnList;
	}

}
