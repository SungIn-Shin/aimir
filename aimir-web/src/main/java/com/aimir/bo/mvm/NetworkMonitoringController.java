package com.aimir.bo.mvm;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.aimir.bo.common.CommandProperty;
import com.aimir.constants.CommonConstants.CommandType;
import com.aimir.constants.CommonConstants.ResultStatus;
import com.aimir.esapi.AimirAuthenticator;
import com.aimir.esapi.AimirUser;
import com.aimir.model.system.Code;
import com.aimir.model.system.Operator;
import com.aimir.model.system.Role;
import com.aimir.model.system.Supplier;
import com.aimir.service.mvm.NetMonitoringManager;
import com.aimir.service.system.OperatorManager;
import com.aimir.util.CalendarUtil;
import com.aimir.util.MonitoringGridMakeExcel;
import com.aimir.util.TimeUtil;
import com.aimir.util.ZipUtils;

@Controller
public class NetworkMonitoringController {
	private static Log log = LogFactory.getLog(NetworkMonitoringController.class);
	
	@Autowired
	OperatorManager operatorManager;

	@Autowired
	NetMonitoringManager netMonitoringManager;
	
	@RequestMapping(value = "/gadget/mvm/networkMonitoringMiniGadget")
	public ModelAndView getNetworkMonitoringMiniGadgetCondition() {
		ModelAndView mav = new ModelAndView("gadget/mvm/NetworkMonitoringMiniGadget");
		// ESAPI.setAuthenticator((Authenticator) new AimirAuthenticator());
		AimirAuthenticator instance = (AimirAuthenticator) ESAPI.authenticator();
		AimirUser user = (AimirUser) instance.getUserFromSession();

		Operator operator = null;
		if (user != null && !user.isAnonymous()) {
			try {
				operator = operatorManager.getOperator(user.getOperator(new Operator()).getId());

				if (operator.getUseLocation() != null && operator.getUseLocation()) {
					// set a permit location
					mav.addObject("permitLocationId",
							(operator.getLocationId() == null) ? 0 : operator.getLocationId());
				}
				mav.addObject("supplierId", user.getRoleData().getSupplier().getId());
			} catch (Exception e) {
				// e.printStackTrace();
				log.error("user is not operator type");
			}
		}

		return mav;
	}
	
	@RequestMapping(value = "/gadget/mvm/networkMonitoringMaxGadget")
	public ModelAndView getNetworkMonitoringMaxGadgetCondition() {
		ModelAndView mav = new ModelAndView("gadget/mvm/NetworkMonitoringMaxGadget");
		// ESAPI.setAuthenticator((Authenticator) new AimirAuthenticator());
		AimirAuthenticator instance = (AimirAuthenticator) ESAPI.authenticator();
		AimirUser user = (AimirUser) instance.getUserFromSession();

		Operator operator = null;
		if (user != null && !user.isAnonymous()) {
			try {
				operator = operatorManager.getOperator(user.getOperator(new Operator()).getId());

				if (operator.getUseLocation() != null && operator.getUseLocation()) {
					// set a permit location
					mav.addObject("permitLocationId",
							(operator.getLocationId() == null) ? 0 : operator.getLocationId());
				}
				Supplier supplier = user.getRoleData().getSupplier();
				mav.addObject("supplierId", supplier.getId());
				mav.addObject("supplierName", supplier.getName());
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.toString(), e);
			}
		}
		return mav;
	}
	
	
	@RequestMapping(value = "/gadget/mvm/networkMonitoringMiniGadget_EVN")
	public ModelAndView getNetworkMonitoringMiniGadgetCondition_EVN() {
		ModelAndView mav = new ModelAndView("gadget/mvm/NetworkMonitoringMiniGadget_EVN");
		// ESAPI.setAuthenticator((Authenticator) new AimirAuthenticator());
		AimirAuthenticator instance = (AimirAuthenticator) ESAPI.authenticator();
		AimirUser user = (AimirUser) instance.getUserFromSession();

		Operator operator = null;
		if (user != null && !user.isAnonymous()) {
			try {
				operator = operatorManager.getOperator(user.getOperator(new Operator()).getId());

				if (operator.getUseLocation() != null && operator.getUseLocation()) {
					// set a permit location
					mav.addObject("permitLocationId",
							(operator.getLocationId() == null) ? 0 : operator.getLocationId());
				}
				mav.addObject("supplierId", user.getRoleData().getSupplier().getId());
			} catch (Exception e) {
				// e.printStackTrace();
				log.error("user is not operator type");
			}
		}

		return mav;
	}
	
	@RequestMapping(value = "/gadget/mvm/networkMonitoringMaxGadget_EVN")
	public ModelAndView getNetworkMonitoringMaxGadgetCondition_EVN() {
		ModelAndView mav = new ModelAndView("gadget/mvm/NetworkMonitoringMaxGadget_EVN");
		// ESAPI.setAuthenticator((Authenticator) new AimirAuthenticator());
		AimirAuthenticator instance = (AimirAuthenticator) ESAPI.authenticator();
		AimirUser user = (AimirUser) instance.getUserFromSession();

		Operator operator = null;
		if (user != null && !user.isAnonymous()) {
			try {
				operator = operatorManager.getOperator(user.getOperator(new Operator()).getId());

				if (operator.getUseLocation() != null && operator.getUseLocation()) {
					// set a permit location
					mav.addObject("permitLocationId",
							(operator.getLocationId() == null) ? 0 : operator.getLocationId());
				}
				Supplier supplier = user.getRoleData().getSupplier();
				mav.addObject("supplierId", supplier.getId());
				mav.addObject("supplierName", supplier.getName());
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.toString(), e);
			}
		}
		return mav;
	}
	
	/* Grid Panel */
	@RequestMapping(value = "/gadget/mvm/getMonitoringDataGrid")
	public ModelAndView getMonitoringDataGrid(
		@RequestParam(value="supplierId") String supplierId,
		@RequestParam(value="searchStartDate", required=false) String searchStartDate,
		@RequestParam(value="searchEndDate", required=false) String searchEndDate,
		@RequestParam(value="groupId") String groupId) {
		
		ModelAndView mav = new ModelAndView("jsonView");
		HttpServletRequest request = ESAPI.httpUtilities().getCurrentRequest();
		int page = Integer.parseInt(request.getParameter("page"));
		int limit = Integer.parseInt(request.getParameter("limit"));
        
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("supplierId", supplierId);
		conditions.put("searchStartDate", searchStartDate);
		conditions.put("searchEndDate", searchEndDate);
		conditions.put("groupId", groupId);
		conditions.put("page", page);
		conditions.put("limit", limit);
		
		mav.addObject("count", netMonitoringManager.getNetMonitoringTotal(conditions));
		mav.addObject("list", netMonitoringManager.getNetMonitoringGridData(conditions));
        
		return mav;
	}
	
	/* Fusion Chart */
	@RequestMapping(value = "/gadget/mvm/getMonitoringDataChart")
	public ModelAndView getMonitoringDataChart(
			@RequestParam(value="supplierId") String supplierId,
			@RequestParam(value="selectMeter") String selectMeter,
			@RequestParam(value="searchStartDate", required=false) String searchStartDate,
			@RequestParam(value="searchEndDate", required=false) String searchEndDate,
			@RequestParam(value="groupId") String groupId) {
		ModelAndView mav = new ModelAndView("jsonView");
		
		Map<String, Object> conditions = new HashMap<String, Object>();
	    conditions.put("supplierId", supplierId);
	    conditions.put("selectMeter", selectMeter);
	    conditions.put("searchStartDate", searchStartDate);
		conditions.put("searchEndDate", searchEndDate);
		conditions.put("groupId", groupId);
		
	    List<List<Map<String, Object>>> result = netMonitoringManager.getNetMonitoringChartData(conditions);
	    List<Map<String, Object>> chartData = (List<Map<String, Object>>) result.get(0);
        List<Map<String, Object>> chartSeries = (List<Map<String, Object>>) result.get(1);
	    
	    mav.addObject("chartSeries", chartSeries);
	    mav.addObject("chartData", chartData);
	    
		return mav;
	}
	
	
	@RequestMapping(value = "/gadget/mvm/NetworkExcelDownloadPopup")
    public ModelAndView NetworkExcelDownloadPopup() {
		ModelAndView mav = new ModelAndView("/gadget/mvm/NetworkExcelDownloadPopup");
		return mav;
	}

	/* Grid -> Excel */
	@RequestMapping(value = "/gadget/mvm/monitoringDataMakeExcel")
	public ModelAndView monitoringDataMakeExcel(
		@RequestParam(value="loginId") String loginId,
		@RequestParam(value="supplierId") String supplierId,
		@RequestParam(value="searchStartDate") String searchStartDate,
		@RequestParam(value="searchEndDate") String searchEndDate,
		@RequestParam(value="groupId", required=false) String groupId, 
		@RequestParam("filePath") String filePath) {
		
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("loginId", loginId);
		conditions.put("supplierId", supplierId);
		conditions.put("searchStartDate", searchStartDate);
		conditions.put("searchEndDate", searchEndDate);
		conditions.put("groupId", groupId);
		String [] selectMeter = {"METER","DCU","REPEATER","RF","MMIU"};
		
		List<List<Map<String, Object>>> excelDataList = new ArrayList<List<Map<String,Object>>>();
		List<List<Map<String, Object>>> edList = new ArrayList<List<Map<String,Object>>>();
		
		String rtnStr = "";
		ResultStatus status = ResultStatus.FAIL;
		final String monitoringListName = "Monitoring";
		
		StringBuilder sFileName = new StringBuilder();
		StringBuilder sbFileName = new StringBuilder();
		List<String> fileNameList = new ArrayList<String>();
		
		Long total = 0L;        // 데이터 조회건수
		// excel sheet 하나에 보여줄 수 있는 최대 데이터 row 수
  		Long maxRows = Long.parseLong(CommandProperty.getProperty("excel.download", "5000"));
		
		try {
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
			List<List<Map<String, Object>>> resultList;
			List<Map<String, Object>> chartData;
			
			if (!commandAuthCheck(loginId, CommandType.DeviceRead, "8.3.2")){
				mav.addObject("rtnStr", "No permission");
				return mav;
			}
			result = netMonitoringManager.getNetMonitoringExcelData(conditions);
			excelDataList.add(result);
			total = new Integer(result.size()).longValue();
			
			for(int i=0; i<selectMeter.length; i++) {
				conditions.put("selectMeter", selectMeter[i]);
				resultList =  new ArrayList<List<Map<String, Object>>>();
				chartData = new ArrayList<Map<String, Object>>();
				
				resultList = netMonitoringManager.getNetMonitoringChartData(conditions);
				chartData = (List<Map<String, Object>>) resultList.get(0);
				excelDataList.add(chartData);
			}

			// Check Download Dir
			File downDir = new File(filePath);
			
			sbFileName.append(monitoringListName);
			sbFileName.append(TimeUtil.getCurrentTimeMilli());
			
			if (downDir.exists()) {
                File[] files = downDir.listFiles();

                if (files != null) {
                    String filename = null;
                    String deleteDate;

                    deleteDate = CalendarUtil.getDate(TimeUtil.getCurrentDay(), Calendar.DAY_OF_MONTH, -10); // 10일 이전 일자
                    boolean isDel = false;

                    for (File file : files) {
                        filename = file.getName();
                        isDel = false;

                        // 파일길이 : 22이상, 확장자 : xls|zip
                        if (filename.length() > 22 && (filename.endsWith("xls") || filename.endsWith("zip"))) {
                            // 10일 지난 파일들 삭제
                            if (filename.startsWith(monitoringListName) && filename.substring(8, 16).compareTo(deleteDate) < 0) {
                                isDel = true;
                            }

                            if (isDel) {
                                file.delete();
                            }
                        }
                        filename = null;
                    }
                }
            } else {
                // directory 가 없으면 생성
                downDir.mkdir();
            }
			
			// Start Create Excel
			MonitoringGridMakeExcel wExcel = new MonitoringGridMakeExcel();
			
			if(total>0) {
				sbFileName.append(sFileName);
				sbFileName.append(".xls");
				wExcel.writeMonitoringReportExcel(excelDataList, filePath, sbFileName.toString());
				fileNameList.add(sbFileName.toString());
				
				// create zip file
				StringBuilder sbZipFile = new StringBuilder();
				sbZipFile.append(sbFileName).append(".zip");
				
				ZipUtils zutils = new ZipUtils();
				try {
					zutils.zipEntry(fileNameList, sbZipFile.toString(), filePath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				mav.addObject("filePath", filePath);
				mav.addObject("fileName", fileNameList.get(0));
				mav.addObject("zipFileName", sbZipFile.toString());
				mav.addObject("fileNames", fileNameList);
				
				status = ResultStatus.SUCCESS;
				
			}else {
				status = ResultStatus.FAIL;
			}
			
		}catch (Exception e) {
			status = ResultStatus.FAIL;
			rtnStr = e.getMessage();
			log.error(e, e);
			mav.addObject("error", rtnStr);
		}
			
		mav.addObject("status", status);
		return mav;
	}
	
	/* Grid -> Excel */
	@RequestMapping(value = "/gadget/mvm/monitoringDataMakeExcel_EVN")
	public ModelAndView monitoringDataMakeExcel_EVN(
		@RequestParam(value="loginId") String loginId,
		@RequestParam(value="supplierId") String supplierId,
		@RequestParam(value="searchStartDate") String searchStartDate,
		@RequestParam(value="searchEndDate") String searchEndDate,
		@RequestParam(value="groupId", required=false) String groupId, 
		@RequestParam("filePath") String filePath) {
		
		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("loginId", loginId);
		conditions.put("supplierId", supplierId);
		conditions.put("searchStartDate", searchStartDate);
		conditions.put("searchEndDate", searchEndDate);
		conditions.put("groupId", groupId);
		String [] selectMeter = {"METER","DCU","RF","MMIU"};
		
		List<List<Map<String, Object>>> excelDataList = new ArrayList<List<Map<String,Object>>>();
		List<List<Map<String, Object>>> edList = new ArrayList<List<Map<String,Object>>>();
		
		String rtnStr = "";
		ResultStatus status = ResultStatus.FAIL;
		final String monitoringListName = "Monitoring";
		
		StringBuilder sFileName = new StringBuilder();
		StringBuilder sbFileName = new StringBuilder();
		List<String> fileNameList = new ArrayList<String>();
		
		Long total = 0L;        // 데이터 조회건수
		// excel sheet 하나에 보여줄 수 있는 최대 데이터 row 수
  		Long maxRows = Long.parseLong(CommandProperty.getProperty("excel.download", "5000"));
		
		try {
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
			List<List<Map<String, Object>>> resultList;
			List<Map<String, Object>> chartData;
			
			if (!commandAuthCheck(loginId, CommandType.DeviceRead, "8.3.2")){
				mav.addObject("rtnStr", "No permission");
				return mav;
			}
			result = netMonitoringManager.getNetMonitoringExcelData(conditions);
			excelDataList.add(result);
			total = new Integer(result.size()).longValue();
			
			for(int i=0; i<selectMeter.length; i++) {
				conditions.put("selectMeter", selectMeter[i]);
				resultList =  new ArrayList<List<Map<String, Object>>>();
				chartData = new ArrayList<Map<String, Object>>();
				
				resultList = netMonitoringManager.getNetMonitoringChartData(conditions);
				chartData = (List<Map<String, Object>>) resultList.get(0);
				excelDataList.add(chartData);
			}

			// Check Download Dir
			File downDir = new File(filePath);
			
			sbFileName.append(monitoringListName);
			sbFileName.append(TimeUtil.getCurrentTimeMilli());
			
			if (downDir.exists()) {
                File[] files = downDir.listFiles();

                if (files != null) {
                    String filename = null;
                    String deleteDate;

                    deleteDate = CalendarUtil.getDate(TimeUtil.getCurrentDay(), Calendar.DAY_OF_MONTH, -10); // 10일 이전 일자
                    boolean isDel = false;

                    for (File file : files) {
                        filename = file.getName();
                        isDel = false;

                        // 파일길이 : 22이상, 확장자 : xls|zip
                        if (filename.length() > 22 && (filename.endsWith("xls") || filename.endsWith("zip"))) {
                            // 10일 지난 파일들 삭제
                            if (filename.startsWith(monitoringListName) && filename.substring(8, 16).compareTo(deleteDate) < 0) {
                                isDel = true;
                            }

                            if (isDel) {
                                file.delete();
                            }
                        }
                        filename = null;
                    }
                }
            } else {
                // directory 가 없으면 생성
                downDir.mkdir();
            }
			
			// Start Create Excel
			MonitoringGridMakeExcel wExcel = new MonitoringGridMakeExcel();
			
			if(total>0) {
				sbFileName.append(sFileName);
				sbFileName.append(".xls");
				wExcel.writeMonitoringReportExcel_EVN(excelDataList, filePath, sbFileName.toString());
				fileNameList.add(sbFileName.toString());
				
				// create zip file
				StringBuilder sbZipFile = new StringBuilder();
				sbZipFile.append(sbFileName).append(".zip");
				
				ZipUtils zutils = new ZipUtils();
				try {
					zutils.zipEntry(fileNameList, sbZipFile.toString(), filePath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				mav.addObject("filePath", filePath);
				mav.addObject("fileName", fileNameList.get(0));
				mav.addObject("zipFileName", sbZipFile.toString());
				mav.addObject("fileNames", fileNameList);
				
				status = ResultStatus.SUCCESS;
				
			}else {
				status = ResultStatus.FAIL;
			}
			
		}catch (Exception e) {
			status = ResultStatus.FAIL;
			rtnStr = e.getMessage();
			log.error(e, e);
			mav.addObject("error", rtnStr);
		}
			
		mav.addObject("status", status);
		return mav;
	}
	
	/* Grid Panel */
	@RequestMapping(value = "/gadget/mvm/getMonitoringDataGrid_EVN")
	public ModelAndView getMonitoringDataGrid_EVN(
		@RequestParam(value="supplierId") String supplierId,
		@RequestParam(value="searchStartDate", required=false) String searchStartDate,
		@RequestParam(value="searchEndDate", required=false) String searchEndDate,
		@RequestParam(value="groupId") String groupId) {
		
		ModelAndView mav = new ModelAndView("jsonView");
		HttpServletRequest request = ESAPI.httpUtilities().getCurrentRequest();
		int page = Integer.parseInt(request.getParameter("page"));
		int limit = Integer.parseInt(request.getParameter("limit"));
        
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("supplierId", supplierId);
		conditions.put("searchStartDate", searchStartDate);
		conditions.put("searchEndDate", searchEndDate);
		conditions.put("groupId", groupId);
		conditions.put("page", page);
		conditions.put("limit", limit);
		
		mav.addObject("count", netMonitoringManager.getNetMonitoringTotal_EVN(conditions));
		mav.addObject("list", netMonitoringManager.getNetMonitoringGridData_EVN(conditions));
        
		return mav;
	}
	
	/*권한체크*/
    protected boolean commandAuthCheck(String loginId, CommandType cmdType, String command) {

		Operator operator = operatorManager.getOperatorByLoginId(loginId);
		if(operator==null){
			return false; // wrong id
		}

		Role role = operator.getRole();
		Set<Code> commands = role.getCommands();
		Code codeCommand = null;
		if (role.getCustomerRole() != null && role.getCustomerRole()) {
			return false; // 고객 권한이면
		}

		for (Iterator<Code> i = commands.iterator(); i.hasNext();) {
			codeCommand = (Code) i.next();
			if (codeCommand.getCode().equals(command))
				return true; // 관리자가 아니라도 명령에 대한 권한이 있으면
		}
		return false;
	}
	    
	   
	    
}