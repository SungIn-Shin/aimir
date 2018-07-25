package com.aimir.service.mvm.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aimir.dao.mvm.NetMonitoringDao;
import com.aimir.dao.system.SupplierDao;
import com.aimir.model.mvm.NetMonitoring;
import com.aimir.model.system.Supplier;
import com.aimir.service.mvm.NetMonitoringManager;
import com.aimir.util.CalendarUtil;
import com.aimir.util.DecimalUtil;
import com.aimir.util.StringUtil;
import com.aimir.util.TimeLocaleUtil;

@Service(value = "netMonitoringManager")
@Transactional(value = "transactionManager", readOnly=false)
public class NetMonitoringManagerImpl implements NetMonitoringManager{
	private static Log log = LogFactory.getLog(NetMonitoringManagerImpl.class);
	
	@Autowired
	NetMonitoringDao netMonitoringDao;
	
	@Autowired
	SupplierDao supplierDao;
	
	
	/*
	 * Network Monitoring Max Gadget
	 * Grid Panel - Data
	 * */
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getNetMonitoringGridData(Map<String, Object> conditions) {
		String supplierId = conditions.get("supplierId").toString();
		Integer page = Integer.parseInt(conditions.get("page").toString());
		Integer limit = Integer.parseInt(conditions.get("limit").toString());
		
		Supplier supplier = supplierDao.get(Integer.parseInt(supplierId));
		
		Map<String, Object> result = netMonitoringDao.getNetMonitoringGridData(conditions, false);
		List<NetMonitoring> list = (List<NetMonitoring>) result.get("list");
		
    	List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
    	Map<String, Object> changeMap = null;
    	String lang = supplier.getLang().getCode_2letter();
    	String country = supplier.getCountry().getCode_2letter();

    	int idx = 1;
        int count0, count1, count2, count3;
        
        DecimalFormat dfMd = DecimalUtil.getMDStyle(supplier.getMd());
        
        for(NetMonitoring nmg : list) {
        	changeMap = new HashMap<String, Object>();
        	count0=0; count1=0; count2=0; count3=0;
        	if (page != null && limit != null) {
                changeMap.put("No", dfMd.format((((page-1) * limit) + idx)));
                idx++;
            }
        	changeMap.put("id"					, StringUtil.nullToZero(nmg.getId()));
        	changeMap.put("date"				, TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(nmg.getYyyymmddhhmmss()), lang, country));
        	changeMap.put("energymeterA24"		, StringUtil.nullToBlank(nmg.getEnergymeterA24()));
        	changeMap.put("energymeterNA24"		, StringUtil.nullToBlank(nmg.getEnergymeterNA24()));
        	changeMap.put("energymeterNA48"		, StringUtil.nullToBlank(nmg.getEnergymeterNA48()));
        	changeMap.put("energymeterUnknown"	, StringUtil.nullToBlank(nmg.getEnergymeterUnknown()));
        	changeMap.put("zruA24"				, StringUtil.nullToBlank(nmg.getZruA24()));
        	changeMap.put("zruNA24"				, StringUtil.nullToBlank(nmg.getZruNA24()));
        	changeMap.put("zruNA48"				, StringUtil.nullToBlank(nmg.getZruNA48()));
        	changeMap.put("zruUnknown"			, StringUtil.nullToBlank(nmg.getZruUnknown()));
        	changeMap.put("mmiuA24"				, StringUtil.nullToBlank(nmg.getMmiuA24()));
        	changeMap.put("mmiuNA24"			, StringUtil.nullToBlank(nmg.getMmiuNA24()));
        	changeMap.put("mmiuNA48"			, StringUtil.nullToBlank(nmg.getMmiuNA48()));
        	changeMap.put("mmiuUnknown"			, StringUtil.nullToBlank(nmg.getMmiuUnknown()));
        	changeMap.put("zeuplsA24"			, StringUtil.nullToBlank(nmg.getZeuplsA24()));
        	changeMap.put("zeuplsNA24"			, StringUtil.nullToBlank(nmg.getZeuplsNA24()));
        	changeMap.put("zeuplsNA48"			, StringUtil.nullToBlank(nmg.getZeuplsNA48()));
        	changeMap.put("zeuplsUnknown"		, StringUtil.nullToBlank(nmg.getZeuplsUnknown()));
        	changeMap.put("meterTotal"			, StringUtil.nullToBlank(nmg.getMeterTotal()));
        	changeMap.put("meterSuccess"		, StringUtil.nullToBlank(nmg.getMeterSuccess()));
        	changeMap.put("contractTotal"		, StringUtil.nullToBlank(nmg.getContractTotal()));
        	changeMap.put("contractSuccess"		, StringUtil.nullToBlank(nmg.getContractSuccess()));
        	
        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuA24()));
        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA24()));
        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA48()));
        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuUnknown()));
        	count0 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorA24()));
        	count1 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA24()));
        	count2 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA48()));
        	count3 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorUnknown()));
        	//changeMap.put("indoorA24"			, StringUtil.nullToZero(nmg.getIndoorA24()));
        	//changeMap.put("indoorNA24"			, StringUtil.nullToZero(nmg.getIndoorNA24()));
        	//changeMap.put("indoorNA48"			, StringUtil.nullToZero(nmg.getIndoorNA48()));
        	//changeMap.put("indoorUnknown"		, StringUtil.nullToZero(nmg.getIndoorUnknown()));
        	changeMap.put("dcuA24"				, StringUtil.nullToZero(count0));
        	changeMap.put("dcuNA24"				, StringUtil.nullToZero(count1));
        	changeMap.put("dcuNA48"				, StringUtil.nullToZero(count2));
        	changeMap.put("dcuUnknown"			, StringUtil.nullToZero(count3));
        	
        	changeMap.put("plciuA24"			, StringUtil.nullToBlank(nmg.getPlciuA24()));
        	changeMap.put("plciuNA24"			, StringUtil.nullToBlank(nmg.getPlciuNA24()));
        	changeMap.put("plciuNA48"			, StringUtil.nullToBlank(nmg.getPlciuNA48()));
        	changeMap.put("plciuUnknown"		, StringUtil.nullToBlank(nmg.getPlciuUnknown()));
        	changeMap.put("ieiuA24"				, StringUtil.nullToBlank(nmg.getIeiuA24()));
        	changeMap.put("ieiuNA24"			, StringUtil.nullToBlank(nmg.getIeiuNA24()));
        	changeMap.put("ieiuNA48"			, StringUtil.nullToBlank(nmg.getIeiuNA48()));
        	changeMap.put("ieiuUnknown"			, StringUtil.nullToBlank(nmg.getIeiuUnknown()));
        	changeMap.put("zbrepeaterA24"		, StringUtil.nullToBlank(nmg.getZbrepeaterA24()));
        	changeMap.put("zbrepeaterNA24"		, StringUtil.nullToBlank(nmg.getZbrepeaterNA24()));
        	changeMap.put("zbrepeaterNA48"		, StringUtil.nullToBlank(nmg.getZbrepeaterNA48()));
        	changeMap.put("zbrepeaterUnknown"	, StringUtil.nullToBlank(nmg.getZbrepeaterUnknown()));
        	
        	returnList.add(changeMap);
        }
        
		return returnList;
	}
	
	/*
	 * Network Monitoring Max Gadget
	 * Grid Panel - Total
	 * */
	@Override
	public Integer getNetMonitoringTotal(Map<String, Object> conditions) {
		
		Map<String, Object> result = netMonitoringDao.getNetMonitoringGridData(conditions, true);
		Integer count = Integer.parseInt(result.get("count").toString());
		
		return count;
	}

	/*
	 * Network Monitoring Max Gadget
	 * Fusion Chart
	 * */
	@Override
	public List<List<Map<String, Object>>> getNetMonitoringChartData(Map<String, Object> conditions) {
		String supplierId = conditions.get("supplierId").toString();
		String selectMeter = conditions.get("selectMeter").toString();
		String searchStartDate = conditions.get("searchStartDate").toString();
		String searchEndDate = conditions.get("searchEndDate").toString();
		Supplier supplier = supplierDao.get(Integer.parseInt(supplierId));
		
		List<List<Map<String, Object>>> resultList = new ArrayList<List<Map<String,Object>>>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> seriesList = new ArrayList<Map<String,Object>>();

		Map<String, Object> result = netMonitoringDao.getNetMonitoringChartData(conditions);
		List<NetMonitoring> list = (List<NetMonitoring>) result.get("list");
		List<String> dateList = new ArrayList<String>();
		
		String resultDate="1";
		int i =0;
		while(!resultDate.equals(searchEndDate)) {
			resultDate = CalendarUtil.getDate(searchStartDate, Calendar.DAY_OF_MONTH, i);
			dateList.add(resultDate);
			i++;
		}
		
		Map<String, Object> seriesMap = null;
    	Map<String, Object> dataMap = null;

    	String lang = supplier.getLang().getCode_2letter();
    	String country = supplier.getCountry().getCode_2letter();
    	String dateCode = "", datePre = "", excelDate="" ;
        int count=0, count0=0, count1=0, count2=0, count3=0, tmp=0;
        i=0;
        for(NetMonitoring nmg : list) {
        	dataMap = new HashMap<String, Object>();
        	dateCode = StringUtil.nullToBlank(nmg.getYyyymmddhhmmss()).substring(0, 8);
        	seriesMap = new HashMap<String, Object>();
        	count=0; count0=0; count1=0; count2=0; count3=0;
        	
	        	if(datePre.isEmpty() || !datePre.equals(dateCode)) {
	        		
	        		for(int j=tmp; j<dateList.size(); j++) {
	        			if(dateCode.equals(dateList.get(j).toString())) {
	        				seriesMap = new HashMap<String, Object>();
	        				dataMap = new HashMap<String, Object>();
	        				tmp=j+1;
	        				break;
	        			}else {
	        				seriesMap = new HashMap<String, Object>();
	            			dataMap = new HashMap<String, Object>();
	    	        		
	            			excelDate = dateList.get(j).toString().substring(0,4)+"-"
	            					+dateList.get(j).toString().substring(4,6)+"-"
	            					+dateList.get(j).toString().substring(6,8);
	            			
	            			seriesMap.put("series", dateList.get(j).toString());
	    	        		dataMap.put("xCode", TimeLocaleUtil.getLocaleDate(dateList.get(j).toString(), lang, country));
	    	        		dataMap.put("date",excelDate);
	    	        		
	    	        		count=0; count0=0; count1=0; count2=0; count3=0;
	    	        		dataMap.put("a24", count0);
	    		        	dataMap.put("na24", count1);
	    		        	dataMap.put("na48", count2);
	    		        	dataMap.put("unknown", count3);
	    		        	dataMap.put("total", count);
	    		        	dataList.add(dataMap);
	    		        	seriesList.add(seriesMap);
	        			}
	        		}
	        	excelDate = dateCode.substring(0,4)+"-"
        					+dateCode.substring(4,6)+"-"
        					+dateCode.substring(6,8);
	        		
	        	seriesMap.put("series", dateCode);
	        	dataMap.put("date", excelDate);
	        	dataMap.put("xCode", TimeLocaleUtil.getLocaleDate(dateCode, lang, country));
	        	
	        	if(selectMeter.toUpperCase().equals("METER")) {
		        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getEnergymeterA24()));
		        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getEnergymeterNA24()));
		        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getEnergymeterNA48()));
		        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getEnergymeterUnknown()));
		        	count = count0+count1+count2+count3;
		        	log.debug("energymeter====>"+count0+count1+count2+count3);
	        	}else if(selectMeter.toUpperCase().equals("RF")) {
	        		count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getZruA24()));
	        		count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getZruNA24()));
	        		count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getZruNA48()));
	        		count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getZruUnknown()));
	        		count = count0+count1+count2+count3;
	        		log.debug("zru====>"+count0+count1+count2+count3);
	        	}else if(selectMeter.toUpperCase().equals("MMIU")) {
		        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getMmiuA24()));
		        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getMmiuNA24()));
		        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getMmiuNA48()));
		        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getMmiuUnknown()));
		        	count = count0+count1+count2+count3;
		        	log.debug("mmiu====>"+count0+count1+count2+count3);
	        	}else if(selectMeter.toUpperCase().equals("ZEUPLS")) {
		        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getZeuplsA24()));
		        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getZeuplsNA24()));
		        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getZeuplsNA48()));
		        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getZeuplsUnknown()));
		        	count = count0+count1+count2+count3;
		        	log.debug("zeupls====>"+count0+count1+count2+count3);
	        	}else if(selectMeter.toUpperCase().equals("DCU")) {
		        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuA24()));
		        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA24()));
		        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA48()));
		        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuUnknown()));
		        	count0 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorA24()));
		        	count1 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA24()));
		        	count2 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA48()));
		        	count3 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorUnknown()));
		        	count = count0+count1+count2+count3;
		        	log.debug("dcu====>"+count0+count1+count2+count3);
	        	}else if(selectMeter.toUpperCase().equals("PLCIU")) {
		        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getPlciuA24()));
		        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getPlciuNA24()));
		        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getPlciuNA48()));
		        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getPlciuUnknown()));
		        	count = count0+count1+count2+count3;
		        	log.debug("plciu====>"+count0+count1+count2+count3);
	        	}else if(selectMeter.toUpperCase().equals("IEIU")) {
		        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getIeiuA24()));
		        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getIeiuNA24()));
		        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getIeiuNA48()));
		        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getIeiuUnknown()));
		        	count = count0+count1+count2+count3;
		        	log.debug("ieiu====>"+count0+count1+count2+count3);
	        	}else if(selectMeter.toUpperCase().equals("REPEATER")) {
		        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getZbrepeaterA24()));
		        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getZbrepeaterNA24()));
		        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getZbrepeaterNA48()));
		        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getZbrepeaterUnknown()));
		        	count = count0+count1+count2+count3;
		        	log.debug("zbrepeater====>"+count0+count1+count2+count3);
	        	}
	
	        	dataMap.put("a24", count0);
	        	dataMap.put("na24", count1);
	        	dataMap.put("na48", count2);
	        	dataMap.put("unknown", count3);
	        	dataMap.put("total", count);
	        	datePre = dateCode;
	        	
	        	dataList.add(dataMap);
	        	seriesList.add(seriesMap);
        	}
        	
        }
        for(int k=tmp; k<dateList.size(); k++) {
        	seriesMap = new HashMap<String, Object>();
			dataMap = new HashMap<String, Object>();
    		
			excelDate = dateList.get(k).toString().substring(0,4)+"-"
					+dateList.get(k).toString().substring(4,6)+"-"
					+dateList.get(k).toString().substring(6,8);
			
			seriesMap.put("series", dateList.get(k).toString());
    		dataMap.put("xCode", TimeLocaleUtil.getLocaleDate(dateList.get(k).toString(), lang, country));
    		dataMap.put("date", excelDate);
    		
    		count=0; count0=0; count1=0; count2=0; count3=0;
    		dataMap.put("a24", count0);
        	dataMap.put("na24", count1);
        	dataMap.put("na48", count2);
        	dataMap.put("unknown", count3);
        	dataMap.put("total", count);
        	dataList.add(dataMap);
        	seriesList.add(seriesMap);
        }
        
        resultList.add(dataList);
        resultList.add(seriesList);
        
		return resultList;
	}

	@Override
	public List<Map<String, Object>> getNetMonitoringExcelData(Map<String, Object> conditions) {
		String supplierId = conditions.get("supplierId").toString();
		Map<String, Object> result = netMonitoringDao.getNetMonitoringChartData(conditions);
		
		Supplier supplier = supplierDao.get(Integer.parseInt(supplierId));
		String lang = supplier.getLang().getCode_2letter();
		String country = supplier.getCountry().getCode_2letter();
		
		List<NetMonitoring> list = (List<NetMonitoring>) result.get("list");
    	List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
    	Map<String, Object> changeMap = null;
    	
    	int count0, count1, count2, count3;
        int idx = 1;
        String preDate, date, week, time;
        DecimalFormat dfMd = DecimalUtil.getMDStyle(supplier.getMd());
        
        for(NetMonitoring nmg : list) {
        	changeMap = new HashMap<String, Object>();
        	count0=0; count1=0; count2=0; count3=0;

        	changeMap.put("No", dfMd.format(idx)); idx++;
        	changeMap.put("id"					, StringUtil.nullToZero(nmg.getId()));
        	
        	preDate = StringUtil.nullToBlank(nmg.getYyyymmddhhmmss());
        	week = TimeLocaleUtil.getLocaleWeekDayOnly(preDate.substring(0,8), lang, country);
        	time = TimeLocaleUtil.getLocaleHourMinute(preDate.substring(8,12), lang, country);
        	preDate = TimeLocaleUtil.getLocaleDateByMediumFormat(preDate.substring(0,8), lang, country);
        	
        	changeMap.put("dateDate", preDate);
        	changeMap.put("dateWeek", week);
        	changeMap.put("dateTime", time);
        	
        	changeMap.put("energymeterA24"		, StringUtil.nullToZero(nmg.getEnergymeterA24()));
        	changeMap.put("energymeterNA24"		, StringUtil.nullToZero(nmg.getEnergymeterNA24()));
        	changeMap.put("energymeterNA48"		, StringUtil.nullToZero(nmg.getEnergymeterNA48()));
        	changeMap.put("energymeterUnknown"	, StringUtil.nullToZero(nmg.getEnergymeterUnknown()));
        	
        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuA24()));
        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA24()));
        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA48()));
        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuUnknown()));
        	count0 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorA24()));
        	count1 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA24()));
        	count2 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA48()));
        	count3 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorUnknown()));
        	changeMap.put("dcuA24"				, count0);
        	changeMap.put("dcuNA24"				, count1);
        	changeMap.put("dcuNA48"				, count2);
        	changeMap.put("dcuUnknown"			, count3);
        	
        	changeMap.put("zbrepeaterA24"		, StringUtil.nullToZero(nmg.getZbrepeaterA24()));
        	changeMap.put("zbrepeaterNA24"		, StringUtil.nullToZero(nmg.getZbrepeaterNA24()));
        	changeMap.put("zbrepeaterNA48"		, StringUtil.nullToZero(nmg.getZbrepeaterNA48()));
        	changeMap.put("zbrepeaterUnknown"	, StringUtil.nullToZero(nmg.getZbrepeaterUnknown()));
        	
        	changeMap.put("zruA24"				, StringUtil.nullToZero(nmg.getZruA24()));
        	changeMap.put("zruNA24"				, StringUtil.nullToZero(nmg.getZruNA24()));
        	changeMap.put("zruNA48"				, StringUtil.nullToZero(nmg.getZruNA48()));
        	changeMap.put("zruUnknown"			, StringUtil.nullToZero(nmg.getZruUnknown()));
        	                                                       
        	changeMap.put("mmiuA24"				, StringUtil.nullToZero(nmg.getMmiuA24()));
        	changeMap.put("mmiuNA24"			, StringUtil.nullToZero(nmg.getMmiuNA24()));
        	changeMap.put("mmiuNA48"			, StringUtil.nullToZero(nmg.getMmiuNA48()));
        	changeMap.put("mmiuUnknown"			, StringUtil.nullToZero(nmg.getMmiuUnknown()));
        	
        	returnList.add(changeMap);
        }
        
		return returnList;
	}

	@Override
	public List<Map<String, Object>> getNetMonitoringGridData_EVN(Map<String, Object> conditions) {
		String supplierId = conditions.get("supplierId").toString();
		Integer page = Integer.parseInt(conditions.get("page").toString());
		Integer limit = Integer.parseInt(conditions.get("limit").toString());
		
		Supplier supplier = supplierDao.get(Integer.parseInt(supplierId));
		
		Map<String, Object> result = netMonitoringDao.getNetMonitoringGridData_EVN(conditions, false);
		List<NetMonitoring> list = (List<NetMonitoring>) result.get("list");
		
    	List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
    	Map<String, Object> changeMap = null;
    	String lang = supplier.getLang().getCode_2letter();
    	String country = supplier.getCountry().getCode_2letter();

    	int idx = 1;
        int count0, count1, count2, count3;
        
        DecimalFormat dfMd = DecimalUtil.getMDStyle(supplier.getMd());
        
        for(NetMonitoring nmg : list) {
        	changeMap = new HashMap<String, Object>();
        	count0=0; count1=0; count2=0; count3=0;
        	if (page != null && limit != null) {
                changeMap.put("No", dfMd.format((((page-1) * limit) + idx)));
                idx++;
            }
        	changeMap.put("id"					, StringUtil.nullToZero(nmg.getId()));
        	changeMap.put("date"				, TimeLocaleUtil.getLocaleDate(StringUtil.nullToBlank(nmg.getYyyymmddhhmmss()), lang, country));
        	changeMap.put("energymeterA24"		, StringUtil.nullToBlank(nmg.getEnergymeterA24()));
        	changeMap.put("energymeterNA24"		, StringUtil.nullToBlank(nmg.getEnergymeterNA24()));
        	changeMap.put("energymeterNA48"		, StringUtil.nullToBlank(nmg.getEnergymeterNA48()));
        	changeMap.put("energymeterUnknown"	, StringUtil.nullToBlank(nmg.getEnergymeterUnknown()));
        	changeMap.put("zruA24"				, StringUtil.nullToBlank(nmg.getZruA24()));
        	changeMap.put("zruNA24"				, StringUtil.nullToBlank(nmg.getZruNA24()));
        	changeMap.put("zruNA48"				, StringUtil.nullToBlank(nmg.getZruNA48()));
        	changeMap.put("zruUnknown"			, StringUtil.nullToBlank(nmg.getZruUnknown()));
        	changeMap.put("mmiuA24"				, StringUtil.nullToBlank(nmg.getMmiuA24()));
        	changeMap.put("mmiuNA24"			, StringUtil.nullToBlank(nmg.getMmiuNA24()));
        	changeMap.put("mmiuNA48"			, StringUtil.nullToBlank(nmg.getMmiuNA48()));
        	changeMap.put("mmiuUnknown"			, StringUtil.nullToBlank(nmg.getMmiuUnknown()));
        	changeMap.put("zeuplsA24"			, StringUtil.nullToBlank(nmg.getZeuplsA24()));
        	changeMap.put("zeuplsNA24"			, StringUtil.nullToBlank(nmg.getZeuplsNA24()));
        	changeMap.put("zeuplsNA48"			, StringUtil.nullToBlank(nmg.getZeuplsNA48()));
        	changeMap.put("zeuplsUnknown"		, StringUtil.nullToBlank(nmg.getZeuplsUnknown()));
        	changeMap.put("meterTotal"			, StringUtil.nullToBlank(nmg.getMeterTotal()));
        	changeMap.put("meterSuccess"		, StringUtil.nullToBlank(nmg.getMeterSuccess()));
        	changeMap.put("contractTotal"		, StringUtil.nullToBlank(nmg.getContractTotal()));
        	changeMap.put("contractSuccess"		, StringUtil.nullToBlank(nmg.getContractSuccess()));
        	
        	count0 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuA24()));
        	count1 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA24()));
        	count2 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuNA48()));
        	count3 = Integer.parseInt(StringUtil.nullToZero(nmg.getDcuUnknown()));
        	count0 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorA24()));
        	count1 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA24()));
        	count2 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorNA48()));
        	count3 += Integer.parseInt(StringUtil.nullToZero(nmg.getIndoorUnknown()));
        	//changeMap.put("indoorA24"			, StringUtil.nullToZero(nmg.getIndoorA24()));
        	//changeMap.put("indoorNA24"			, StringUtil.nullToZero(nmg.getIndoorNA24()));
        	//changeMap.put("indoorNA48"			, StringUtil.nullToZero(nmg.getIndoorNA48()));
        	//changeMap.put("indoorUnknown"		, StringUtil.nullToZero(nmg.getIndoorUnknown()));
        	changeMap.put("dcuA24"				, StringUtil.nullToZero(count0));
        	changeMap.put("dcuNA24"				, StringUtil.nullToZero(count1));
        	changeMap.put("dcuNA48"				, StringUtil.nullToZero(count2));
        	changeMap.put("dcuUnknown"			, StringUtil.nullToZero(count3));
        	
        	changeMap.put("plciuA24"			, StringUtil.nullToBlank(nmg.getPlciuA24()));
        	changeMap.put("plciuNA24"			, StringUtil.nullToBlank(nmg.getPlciuNA24()));
        	changeMap.put("plciuNA48"			, StringUtil.nullToBlank(nmg.getPlciuNA48()));
        	changeMap.put("plciuUnknown"		, StringUtil.nullToBlank(nmg.getPlciuUnknown()));
        	changeMap.put("ieiuA24"				, StringUtil.nullToBlank(nmg.getIeiuA24()));
        	changeMap.put("ieiuNA24"			, StringUtil.nullToBlank(nmg.getIeiuNA24()));
        	changeMap.put("ieiuNA48"			, StringUtil.nullToBlank(nmg.getIeiuNA48()));
        	changeMap.put("ieiuUnknown"			, StringUtil.nullToBlank(nmg.getIeiuUnknown()));
        	changeMap.put("zbrepeaterA24"		, StringUtil.nullToBlank(nmg.getZbrepeaterA24()));
        	changeMap.put("zbrepeaterNA24"		, StringUtil.nullToBlank(nmg.getZbrepeaterNA24()));
        	changeMap.put("zbrepeaterNA48"		, StringUtil.nullToBlank(nmg.getZbrepeaterNA48()));
        	changeMap.put("zbrepeaterUnknown"	, StringUtil.nullToBlank(nmg.getZbrepeaterUnknown()));
        	
        	returnList.add(changeMap);
        }
        
		return returnList;
	}

	@Override
	public Integer getNetMonitoringTotal_EVN(Map<String, Object> conditions) {
		Map<String, Object> result = netMonitoringDao.getNetMonitoringGridData_EVN(conditions, true);
		Integer count = Integer.parseInt(result.get("count").toString());
		
		return count;
	}
	
}
