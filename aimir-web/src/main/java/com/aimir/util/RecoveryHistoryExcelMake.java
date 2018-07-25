package com.aimir.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.aimir.model.system.Supplier;

public class RecoveryHistoryExcelMake {

	private static Log logger = LogFactory.getLog(RecoveryHistoryExcelMake.class);

	public RecoveryHistoryExcelMake() {

	}

	/**
	 * @param result
	 * @param msgMap
	 * @param isLast
	 * @param filePath
	 * @param fileName
	 */
	public void writeReportExcel(List<Map<String, Object>> result, Map<String, String> msgMap, boolean isLast, String filePath, String fileName, Supplier supplier) {

		try {
			HSSFWorkbook workbook = new HSSFWorkbook();

			HSSFFont fontTitle = workbook.createFont();
			fontTitle.setFontHeightInPoints((short) 14);
			fontTitle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

			HSSFFont fontHeader = workbook.createFont();
			fontHeader.setFontHeightInPoints((short) 10);
			fontHeader.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

			HSSFFont fontBody = workbook.createFont();
			fontBody.setFontHeightInPoints((short) 10);
			fontBody.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);

			HSSFRow row = null;
			HSSFCell cell = null;
			HSSFCellStyle titleCellStyle = ExcelUtil.getStyle(workbook, fontHeader, 1, 1, 1, 1, 0, 1, 0, 1, 0);
			HSSFCellStyle dataCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 0, 0, 0);

			Map<String, Object> resultMap = new HashMap<String, Object>();
			String fileFullPath = new StringBuilder().append(filePath).append(File.separator).append(fileName).toString();
			final String reportTitle = msgMap.get("title");
			int dataGapsStartRow = 3;
			int totalColumnCnt = 9;
			int dataCount = 0;

			String lang = supplier.getLang().getCode_2letter();
			String country = supplier.getCountry().getCode_2letter();
			DecimalFormat cdf = DecimalUtil.getDecimalFormat(supplier.getCd());
			DecimalFormat mdf = DecimalUtil.getDecimalFormat(supplier.getMd());

			HSSFSheet sheet = workbook.createSheet(reportTitle);
			int colIdx = 0;
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);
			sheet.setColumnWidth(colIdx++, 256 * 25);

			row = sheet.createRow(0);
			cell = row.createCell(0);
			cell.setCellValue(reportTitle);
			cell.setCellStyle(ExcelUtil.getStyle(workbook, fontTitle, 0, 0, 0, 0, 0, 0, 0, 1, 0));
			sheet.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (totalColumnCnt - 1)));

			// Title
			row = sheet.createRow(dataGapsStartRow);

			cell = row.createCell(0);
			cell.setCellValue(msgMap.get("Contract No"));
			cell.setCellStyle(titleCellStyle);

			cell = row.createCell(1);
			cell.setCellValue(msgMap.get("Customer ID"));
			cell.setCellStyle(titleCellStyle);

			cell = row.createCell(2);
			cell.setCellValue(msgMap.get("Customer Name"));
			cell.setCellStyle(titleCellStyle);

			cell = row.createCell(3);
			cell.setCellValue(msgMap.get("Meter ID"));
			cell.setCellStyle(titleCellStyle);
			
			cell = row.createCell(4);
			cell.setCellValue(msgMap.get("Last Token Date"));
			cell.setCellStyle(titleCellStyle);
			
			cell = row.createCell(5);
			cell.setCellValue(msgMap.get("Charged Credit"));
			cell.setCellStyle(titleCellStyle);

			cell = row.createCell(6);
			cell.setCellValue(msgMap.get("Balance"));
			cell.setCellStyle(titleCellStyle);

			cell = row.createCell(7);
			cell.setCellValue(msgMap.get("Usage"));
			cell.setCellStyle(titleCellStyle);

			cell = row.createCell(8);
			cell.setCellValue(msgMap.get("Active Energy"));
			cell.setCellStyle(titleCellStyle);
			
			cell = row.createCell(9);
			cell.setCellValue(msgMap.get("Used Consumption"));
			cell.setCellStyle(titleCellStyle);
			
			cell = row.createCell(10);
			cell.setCellValue(msgMap.get("Is Deleted"));
			cell.setCellStyle(titleCellStyle);
			
			cell = row.createCell(11);
			cell.setCellValue(msgMap.get("Last Modify Date"));
			cell.setCellStyle(titleCellStyle);
			// Title End

			// Data
			dataCount = result.size();

			for (int i = 0; i < dataCount; i++) {
				resultMap = result.get(i);
				row = sheet.createRow(i + (dataGapsStartRow + 1));

				cell = row.createCell(0);
				cell.setCellValue(resultMap.get("CONTRACT_NUMBER") == null ? "" : (String) resultMap.get("CONTRACT_NUMBER"));
				cell.setCellStyle(dataCellStyle);

				cell = row.createCell(1);
				cell.setCellValue(resultMap.get("CUSTOMER_ID") == null ? "" : resultMap.get("CUSTOMER_ID").toString());
				cell.setCellStyle(dataCellStyle);

				cell = row.createCell(2);
				cell.setCellValue(resultMap.get("NAME") == null ? "" : resultMap.get("NAME").toString());
				cell.setCellStyle(dataCellStyle);

				cell = row.createCell(3); 
				cell.setCellValue(resultMap.get("METER_ID") == null ? "" : resultMap.get("METER_ID").toString());
				cell.setCellStyle(dataCellStyle);
				
				cell = row.createCell(4);
				cell.setCellValue(resultMap.get("LASTTOKENDATE") == null ? "" : resultMap.get("LASTTOKENDATE").toString());
				cell.setCellStyle(dataCellStyle);

				cell = row.createCell(5);
				cell.setCellValue(resultMap.get("CHARGEDCREDIT") == null ? "" : resultMap.get("CHARGEDCREDIT").toString());
				cell.setCellStyle(dataCellStyle);

				cell = row.createCell(6);
				cell.setCellValue(resultMap.get("BALANCE") == null ? "" : resultMap.get("BALANCE").toString());
				cell.setCellStyle(dataCellStyle);

				cell = row.createCell(7);
				cell.setCellValue(resultMap.get("USAGE") == null ? "" : resultMap.get("USAGE").toString());
				cell.setCellStyle(dataCellStyle);

				cell = row.createCell(8);
				cell.setCellValue(resultMap.get("ACTIVEENERGY") == null ? "" : resultMap.get("ACTIVEENERGY").toString());
				cell.setCellStyle(dataCellStyle);
				
				cell = row.createCell(9);
				cell.setCellValue(resultMap.get("USED_CONSUMPTION") == null ? "" : resultMap.get("USED_CONSUMPTION").toString());
				cell.setCellStyle(dataCellStyle);
				
				cell = row.createCell(10);
				cell.setCellValue(resultMap.get("IS_DELETE") == null ? "" : resultMap.get("IS_DELETE").toString());
				cell.setCellStyle(dataCellStyle);
				
				cell = row.createCell(11);
				cell.setCellValue(resultMap.get("LAST_MODIFY_DATE") == null ? "" : resultMap.get("LAST_MODIFY_DATE").toString());
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
			logger.error(e, e);
		} // End Try
	}

}
