package com.aimir.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class STSHistoryMakeExcel {

	public STSHistoryMakeExcel() {

	}

	/**
	 * @param result
	 * @param msgMap
	 * @param isLast
	 * @param filePath
	 * @param fileName
	 */
	@SuppressWarnings("unchecked")
	public void writeReportExcel(List<Map<String,Object>> result,
			Map<String, String> msgMap, boolean isLast, String filePath,
			String fileName) {

		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
			
            HSSFFont fontTitle = workbook.createFont();
            fontTitle.setFontHeightInPoints((short)14);
            fontTitle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

            HSSFFont fontHeader = workbook.createFont();
            fontHeader.setFontHeightInPoints((short)10);
            fontHeader.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            
            HSSFFont fontBody = workbook.createFont();
            fontBody.setFontHeightInPoints((short)10);
            fontBody.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);

			HSSFRow row = null;
			HSSFCell cell = null;
			HSSFCellStyle titleCellStyle = ExcelUtil.getStyle(workbook, fontHeader, 0, 1, 0, 1, 0, 1, 0, 1, 0);
			HSSFCellStyle title2CellStyle = ExcelUtil.getStyle(workbook, fontHeader, 1, 1, 1, 1, 0, 1, 0, 1, 0);
			HSSFCellStyle dataCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 0, 0, 0);

			Map<String, Object> resultMap = new HashMap<String, Object>();
			String fileFullPath = new StringBuilder().append(filePath).append(
					File.separator).append(fileName).toString();
            final String reportTitle = msgMap.get("title");
            int voltagelevelListStartRow = 2;
            int totalColumnCnt = result.size();
			int dataCount = 0;

			HSSFSheet sheet = workbook.createSheet(reportTitle);
			
            int colIdx = 0;
  
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            sheet.setColumnWidth(colIdx++, 256 * 20);
            
   
            //Title
            row = sheet.createRow(0);
            cell = row.createCell(0);
            cell.setCellValue(reportTitle);
            cell.setCellStyle(ExcelUtil.getStyle(workbook, fontTitle, 0, 0, 0, 0, 0, 0, 0, 1, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (9)));

            // header2
            row = sheet.createRow(voltagelevelListStartRow);

			int cellCnt = 0;
	
			cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("contractNumber"));
            cell.setCellStyle(titleCellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("createDate"));
            cell.setCellStyle(titleCellStyle);
            
			cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("cmd"));
            cell.setCellStyle(titleCellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("seq"));
            cell.setCellStyle(titleCellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("asyncTrId"));
            cell.setCellStyle(titleCellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("payMode"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("resultDate"));
            cell.setCellStyle(title2CellStyle);
            
			cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("result"));
            cell.setCellStyle(title2CellStyle);

			cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("failReason"));
            cell.setCellStyle(title2CellStyle);

			cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("tokenDate"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("token"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("chargedCredit"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("getDate"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("ecMode"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("emergencyCreditDay"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("tariffDate"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("tariffMode"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("tariffKind"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("tariffCount"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("condLimit1"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("condLimit2"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("consumption"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("fixedRate"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("varRate"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("condRate1"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("condRate2"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("remainingCreditDate"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("remainingCredit"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("netChargeYyyymm"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("netChargeMonthConsumption"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("netChargeMonnthCost"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("netChargeYyyymmdd"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("netChargeDayConsumption"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("netChargeDayCost"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("fcMode"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("friendlyDate"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("friendlyDayType"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("friendlyFromHHMM"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("friendlyEndHHMM"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("stsNumber"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("kct1"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("kct2"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("channel"));
            cell.setCellStyle(title2CellStyle);
            
            cell = row.createCell(cellCnt++);
			cell.setCellValue(msgMap.get("panId"));
            cell.setCellStyle(title2CellStyle);

			// Title End

			// Data

			dataCount = result.size();

			for (int i = 0; i < dataCount; i++) {
				resultMap = (Map<String, Object>) result.get(i);
				row = sheet.createRow(i + (voltagelevelListStartRow + 1));  
				int cellCnt2 = 0;
			            	
				cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CONTRACTNUMBER")==null?"":resultMap.get("CONTRACTNUMBER").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CREATEDATE")==null?"":resultMap.get("CREATEDATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
				cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CMD")==null?"":String.valueOf(resultMap.get("CMD")));
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("SEQ")==null?"":resultMap.get("SEQ").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("ASYNCTRID")==null?"":resultMap.get("ASYNCTRID").toString());
            	cell.setCellStyle(dataCellStyle);
				
				cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("PAYMODE")==null?"":resultMap.get("PAYMODE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("RESULTDATE")==null?"":resultMap.get("RESULTDATE").toString());
            	cell.setCellStyle(dataCellStyle);
				
				cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("RESULT")==null?"":resultMap.get("RESULT").toString());
            	cell.setCellStyle(dataCellStyle);
            	
				cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("FAILREASON")==null?"":resultMap.get("FAILREASON").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("TOKENDATE")==null?"":resultMap.get("TOKENDATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("TOKEN")==null?"":resultMap.get("TOKEN").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CHARGEDCREDIT")==null?"":resultMap.get("CHARGEDCREDIT").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("GETDATE")==null?"":resultMap.get("GETDATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("ECMODE")==null?"":resultMap.get("ECMODE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("EMERGENCYCREDITDAY")==null?"":resultMap.get("EMERGENCYCREDITDAY").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("TARIFFDATE")==null?"":resultMap.get("TARIFFDATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("TARIFFMODE")==null?"":resultMap.get("TARIFFMODE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("TARIFFKIND")==null?"":resultMap.get("TARIFFKIND").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("TARIFFCOUNT")==null?"":resultMap.get("TARIFFCOUNT").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CONDLIMIT1")==null?"":resultMap.get("CONDLIMIT1").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CONDLIMIT2")==null?"":resultMap.get("CONDLIMIT2").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CONSUMPTION")==null?"":resultMap.get("CONSUMPTION").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("FIXEDRATE")==null?"":resultMap.get("FIXEDRATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("VARRATE")==null?"":resultMap.get("VARRATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CONDRATE1")==null?"":resultMap.get("CONDRATE1").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CONDRATE2")==null?"":resultMap.get("CONDRATE2").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("REMAININGCREDITDATE")==null?"":resultMap.get("REMAININGCREDITDATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("REMAININGCREDIT")==null?"":resultMap.get("REMAININGCREDIT").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("NETCHARGEYYYYMM")==null?"":resultMap.get("NETCHARGEYYYYMM").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("NETCHARGEMONTHCONSUMPTION")==null?"":resultMap.get("NETCHARGEMONTHCONSUMPTION").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("NETCHARGEMONTHCOST")==null?"":resultMap.get("NETCHARGEMONTHCOST").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("NETCHARGEYYYYMMDD")==null?"":resultMap.get("NETCHARGEYYYYMMDD").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("NETCHARGEDAYCONSUMPTION")==null?"":resultMap.get("NETCHARGEDAYCONSUMPTION").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("NETCHARGEDAYCOST")==null?"":resultMap.get("NETCHARGEDAYCOST").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("FCMODE")==null?"":resultMap.get("FCMODE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("FRIENDLYDATE")==null?"":resultMap.get("FRIENDLYDATE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("FRIENDLYDAYTYPE")==null?"":resultMap.get("FRIENDLYDAYTYPE").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("FRIENDLYFROMHHMM")==null?"":resultMap.get("FRIENDLYFROMHHMM").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("FRIENDLYENDHHMM")==null?"":resultMap.get("FRIENDLYENDHHMM").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("STSNUMBER")==null?"":resultMap.get("STSNUMBER").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("KCT1")==null?"":resultMap.get("KCT1").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("KCT2")==null?"":resultMap.get("KCT2").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("CHANNEL")==null?"":resultMap.get("CHANNEL").toString());
            	cell.setCellStyle(dataCellStyle);
            	
            	cell = row.createCell(cellCnt2++);
				cell.setCellValue(resultMap.get("PANID")==null?"":resultMap.get("PANID").toString());
            	cell.setCellStyle(dataCellStyle);
            	

			}
			// End Data

			// 파일 생성
			FileOutputStream fs = null;
			try {
				fs = new FileOutputStream(fileFullPath);
				workbook.write(fs);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fs != null)
					fs.close();
			}

		} catch (Exception e) {
			// TODO: handle exception
		} // End Try
	}

}
