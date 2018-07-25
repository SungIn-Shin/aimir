package com.aimir.util;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.LineChartData;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;

/**
 * MonitoringGridMakeExcel.java Description
 * Date 2017.11.03
 * @author TEN
 *
 */
public class MonitoringGridMakeExcel {
	private static Log log = LogFactory.getLog(MonitoringGridMakeExcel.class);
	public MonitoringGridMakeExcel() {}
	
	/**
     * @param resultList
     * @param filePath
     * @param fileName
     */
	public static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }
	
	
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public void writeMonitoringReportExcel(List<List<Map<String, Object>>>resultList, String filePath, String fileName) {
		
		try {
			 XSSFWorkbook workbook = new XSSFWorkbook();
			 	
	            XSSFFont fontTitle = workbook.createFont();
	            fontTitle.setFontHeightInPoints((short) 14);
	            fontTitle.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

	            XSSFFont fontHeader = workbook.createFont();
	            fontHeader.setFontHeightInPoints((short) 11);
	            fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

	            XSSFFont fontBody = workbook.createFont();
	            fontBody.setFontHeightInPoints((short) 11);
	            fontBody.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);
	            
	            XSSFRow row = null;
	            XSSFRow row2 = null;
	            XSSFCell cell = null;
	            												//workbook, font, top, bottom, left, right, grey, green, orange, align, border, format
	            XSSFCellStyle titleCellStyle = ExcelUtil.getStyle(workbook, fontTitle, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0);
	            XSSFCellStyle headerCellStyle = ExcelUtil.getStyle(workbook, fontHeader, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0);
	            XSSFCellStyle dateCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0);
	            XSSFCellStyle typeCellStyle = ExcelUtil.getStyle(workbook, fontHeader, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0);
	            
	            XSSFCellStyle a24CellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 3, 1, 0, 0);
	            XSSFCellStyle na24CellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 4, 1, 0, 0);
	            XSSFCellStyle na48CellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 2, 0, 1, 0, 0);
	            XSSFCellStyle unknownCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 2, 1, 0, 0);
	            XSSFCellStyle totalCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 5, 1, 0, 0);
	            
	            XSSFCellStyle dataCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 0, 2, 0, 1);
	            XSSFCellStyle dataCellStyle2 = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0);

	            String fileFullPath = new StringBuilder().append(filePath).append(File.separator).append(fileName).toString();
	            String [] reportTitle = {"Network Monitoring Statistics", "Meter Statistics", "DCU Statistics", "Repeater Statistics", "RF Statistics", "MMIU Statistics"};
	            String [] sheetName = {"Data","Meter","DCU","Repeater","RF","MMIU"};
	            int titleColumnCnt = 5;
	            int typeHeader = 4;
	            int statusRow = 3;
	            int dataStartRow = 4;

	            List<String> keyList = new ArrayList<String>();
	            keyList.add("A24");
	            keyList.add("NA24");
	            keyList.add("NA48");
	            keyList.add("Unknown");
	            
	            XSSFSheet sheet = workbook.createSheet(sheetName[0]);	//Data
	            XSSFSheet sheet1 = workbook.createSheet(sheetName[1]);	//Meter
	            XSSFSheet sheet2 = workbook.createSheet(sheetName[2]);	//DCU
	            XSSFSheet sheet3 = workbook.createSheet(sheetName[3]);	//Repeater
	            XSSFSheet sheet4 = workbook.createSheet(sheetName[4]);	//RF
	            XSSFSheet sheet5 = workbook.createSheet(sheetName[5]);	//MMIU
	            
	            //Data Sheet Start
	            int colIdx = 0;
	            /*for(colIdx =0; colIdx<29; colIdx++) {
	            	sheet.autoSizeColumn(colIdx++);     //No  
	            }*/
	            
	            sheet.setColumnWidth(colIdx++, 256 * 5);	//NO
	            
	            sheet.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet.setColumnWidth(colIdx++, 256 * 7);
	            sheet.setColumnWidth(colIdx++, 256 * 9);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//Meter
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//DCU
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//Repeater
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//RF
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//MMIU
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);

	            row = sheet.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[0]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet.createRow(2);
	            cell = row.createCell(0);
	            sheet.addMergedRegion(new CellRangeAddress(2,3,0,0));
	            cell.setCellValue("No");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,1,3));
	            cell.setCellValue("DATE");
	            cell.setCellStyle(headerCellStyle);
	            // 합병된 cell에도 style 적용
	            cell = row.createCell(2);
	            cell.setCellStyle(headerCellStyle);
	            cell = row.createCell(3);
	            cell.setCellStyle(headerCellStyle);

	            // Type Header - Meter
	            cell = row.createCell(typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader, typeHeader+4));
	            cell.setCellValue("Meter");
	            cell.setCellStyle(typeCellStyle);
	            
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);

	            // Type Header - DCU
	            cell = row.createCell(++typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader,typeHeader+4));
	            cell.setCellValue("DCU");
	            cell.setCellStyle(typeCellStyle);

	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            
	            // Type Header - Repeater
	            cell = row.createCell(++typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader,typeHeader+4));
	            cell.setCellValue("Repeater");
	            cell.setCellStyle(typeCellStyle);

	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            
	            // Type Header - RF(ZRU)
	            cell = row.createCell(++typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader,typeHeader+4));
	            cell.setCellValue("RF");
	            cell.setCellStyle(typeCellStyle);

	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            
	            // Type Header - MMIU
	            cell = row.createCell(++typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader,typeHeader+4));
	            cell.setCellValue("MMIU");
	            cell.setCellStyle(typeCellStyle);
	            
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            

	            // Header2 - DATE
	            row = sheet.createRow(statusRow);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("Date");
	            cell.setCellStyle(dateCellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("Week");
	            cell.setCellStyle(dateCellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("Time");
	            cell.setCellStyle(dateCellStyle);
	            
	            
	            // Header2 - A24, NA24, NA48, UNKNOWN
	            for(int i = 4; i<29; i+=5) {
	            	
	            	//sheet.autoSizeColumn(i);  
	                //sheet.setColumnWidth(i, 256 * 10);	//A24
	            	cell = row.createCell(i);
	            	cell.setCellValue("A24");
	            	cell.setCellStyle(a24CellStyle);
	            	
	            	//sheet.autoSizeColumn(i+1);  
	            	//sheet.setColumnWidth(i+1, 256 * 10);	//NA24
	            	cell = row.createCell(i+1);
	            	cell.setCellValue("NA24");
	            	cell.setCellStyle(na24CellStyle);
	            	
	            	//sheet.autoSizeColumn(i+2);  
	            	//sheet.setColumnWidth(i+2, 256 * 10);	//NA48
	            	cell = row.createCell(i+2);
	            	cell.setCellValue("NA48");
	            	cell.setCellStyle(na48CellStyle);
	            	
	            	//sheet.autoSizeColumn(i+3);  
	            	//sheet.setColumnWidth(i+3, 256 * 10);	//Unknown
	            	cell = row.createCell(i+3);
	            	cell.setCellValue("Unknown");
	            	cell.setCellStyle(unknownCellStyle);
	            	
	            	//sheet.autoSizeColumn(i+4);  
	            	//sheet.setColumnWidth(i+4, 256 * 10);	//Total
	            	cell = row.createCell(i+4);
	            	cell.setCellValue("Total");
	            	cell.setCellStyle(totalCellStyle);
	            }
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet.createRow(dataStartRow);
	            int a24=0, na24=0, na48=0, unknown=0, total=0, k=0;
	            String [] typeList = {"energymeter", "dcu", "zbrepeater", "zru", "mmiu"};
	            Map<String,Object> resultMap;
	            
	            for(int i = 0; i< resultList.get(0).size(); i++) {
	            	row2 = sheet.createRow(dataStartRow+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(0).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("No").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(resultMap.get("dateDate").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(resultMap.get("dateWeek").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(resultMap.get("dateTime").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(3);
		            
		            for(int j = 4; j<29; j+=5) {
		            	a24=0; na24=0; na48=0; unknown=0; total=0;
		            	k=(j-4)/5;
		            	cell = row2.createCell(j);
		            	a24 = Integer.parseInt(resultMap.get(typeList[k]+"A24").toString());
			            cell.setCellValue(a24);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j);
			            		
			            cell = row2.createCell(j+1);
			            na24 = Integer.parseInt(resultMap.get(typeList[k]+"NA24").toString());
			            cell.setCellValue(na24);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+1);
			            
			            cell = row2.createCell(j+2);
			            na48 = Integer.parseInt(resultMap.get(typeList[k]+"NA48").toString());
			            cell.setCellValue(na48);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+2);
			            
			            cell = row2.createCell(j+3);
			            unknown = Integer.parseInt(resultMap.get(typeList[k]+"Unknown").toString()); 
			            cell.setCellValue(unknown);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+3);
			            
			            cell = row2.createCell(j+4);
			            total = a24 + na24 + na48 + unknown;
			            cell.setCellValue(total);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+4);
		            }
		        }
	            // Monitoring Data End
	            //Data Sheet End
	            
	            //Meter Sheet1 Start
	            colIdx = 0;
	            sheet1.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet1.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet1.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet1.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet1.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet1.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet1.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet1.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[1]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet1.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet1.createRow(3);
	            
	            for(int i = 0; i< resultList.get(1).size(); i++) {
	            	row2 = sheet1.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(1).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet1.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet1, keyList, resultList.get(1).size());
	            //Meter Sheet1 End
	            
	            //DCU Sheet2 Start
	            colIdx = 0;
	            sheet2.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet2.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet2.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet2.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet2.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet2.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet2.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet2.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[2]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet2.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet2.createRow(3);
	            
	            for(int i = 0; i< resultList.get(2).size(); i++) {
	            	row2 = sheet2.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(2).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet2.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet2, keyList, resultList.get(2).size());
	            //DCU Sheet2 End
	            
	            //Repeater Sheet3 Start
	            colIdx = 0;
	            sheet3.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet3.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet3.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet3.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet3.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet3.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet3.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet3.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[3]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet3.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet3.createRow(3);
	            
	            for(int i = 0; i< resultList.get(3).size(); i++) {
	            	row2 = sheet3.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(3).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet3.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet3, keyList, resultList.get(3).size());
	            // Repeater Sheet3 End
	            
	            //RF Sheet4 Start
	            colIdx = 0;
	            sheet4.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet4.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet4.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet4.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet4.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet4.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet4.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet4.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[4]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet4.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet4.createRow(3);
	            
	            for(int i = 0; i< resultList.get(4).size(); i++) {
	            	row2 = sheet4.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(4).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet4.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet4, keyList, resultList.get(4).size());
	            //RF Sheet4 End
	            
	            //MMIU Sheet5 Start
	            colIdx = 0;
	            sheet5.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet5.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet5.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet5.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet5.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet5.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet5.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet5.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[5]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet5.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet5.createRow(3);
	            
	            for(int i = 0; i< resultList.get(5).size(); i++) {
	            	row2 = sheet5.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(5).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet5.autoSizeColumn(0);
		            
		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet5.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet5.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet5.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet5.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet5.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            drawingChart(sheet5, keyList, resultList.get(5).size());
	            //MMIU Sheet5 End
	            
	            // File Create
	            FileOutputStream fs = null;
	            try {
	                fs = new FileOutputStream(fileFullPath);
	                workbook.write(fs);
	                log.info("Excel Create Success ~ !");
	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
				}catch (Exception e) {
					log.error(e,e);
				}finally {
		            if (fs != null)
		                fs.close();
		        }
		
		} catch (Exception e) {
	    	log.error(e,e);
	    } // End Try
	}
	
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public void writeMonitoringReportExcel_EVN(List<List<Map<String, Object>>>resultList, String filePath, String fileName) {
		
		try {
			 XSSFWorkbook workbook = new XSSFWorkbook();
			 	
	            XSSFFont fontTitle = workbook.createFont();
	            fontTitle.setFontHeightInPoints((short) 14);
	            fontTitle.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

	            XSSFFont fontHeader = workbook.createFont();
	            fontHeader.setFontHeightInPoints((short) 11);
	            fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

	            XSSFFont fontBody = workbook.createFont();
	            fontBody.setFontHeightInPoints((short) 11);
	            fontBody.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);
	            
	            XSSFRow row = null;
	            XSSFRow row2 = null;
	            XSSFCell cell = null;
	            												//workbook, font, top, bottom, left, right, grey, green, orange, align, border, format
	            XSSFCellStyle titleCellStyle = ExcelUtil.getStyle(workbook, fontTitle, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0);
	            XSSFCellStyle headerCellStyle = ExcelUtil.getStyle(workbook, fontHeader, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0);
	            XSSFCellStyle dateCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0);
	            XSSFCellStyle typeCellStyle = ExcelUtil.getStyle(workbook, fontHeader, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0);
	            
	            XSSFCellStyle a24CellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 3, 1, 0, 0);
	            XSSFCellStyle na24CellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 4, 1, 0, 0);
	            XSSFCellStyle na48CellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 2, 0, 1, 0, 0);
	            XSSFCellStyle unknownCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 2, 1, 0, 0);
	            XSSFCellStyle totalCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 5, 1, 0, 0);
	            
	            XSSFCellStyle dataCellStyle = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 0, 2, 0, 1);
	            XSSFCellStyle dataCellStyle2 = ExcelUtil.getStyle(workbook, fontBody, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0);

	            String fileFullPath = new StringBuilder().append(filePath).append(File.separator).append(fileName).toString();
	            String [] reportTitle = {"Network Monitoring Statistics", "Meter Statistics", "DCU Statistics", "ZRU Statistics", "MMIU Statistics"};
	            String [] sheetName = {"Data","Meter","DCU","ZRU","MMIU"};
	            int titleColumnCnt = 5;
	            int typeHeader = 4;
	            int statusRow = 3;
	            int dataStartRow = 4;

	            List<String> keyList = new ArrayList<String>();
	            keyList.add("A24");
	            keyList.add("NA24");
	            keyList.add("NA48");
	            keyList.add("Unknown");
	            
	            XSSFSheet sheet = workbook.createSheet(sheetName[0]);	//Data
	            XSSFSheet sheet1 = workbook.createSheet(sheetName[1]);	//Meter
	            XSSFSheet sheet2 = workbook.createSheet(sheetName[2]);	//DCU	            
	            XSSFSheet sheet3 = workbook.createSheet(sheetName[3]);	//RF-ZRU
	            XSSFSheet sheet4 = workbook.createSheet(sheetName[4]);	//MMIU
	            
	            //Data Sheet Start
	            int colIdx = 0;
	            /*for(colIdx =0; colIdx<24; colIdx++) {
	            	sheet.autoSizeColumn(colIdx++);     //No  
	            }*/
	            
	            sheet.setColumnWidth(colIdx++, 256 * 5);	//NO
	            
	            sheet.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet.setColumnWidth(colIdx++, 256 * 7);
	            sheet.setColumnWidth(colIdx++, 256 * 9);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//Meter
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//DCU
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//ZRU
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            sheet.setColumnWidth(colIdx++, 256 * 8);	//MMIU
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 8);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            sheet.setColumnWidth(colIdx++, 256 * 10);
	            
	            row = sheet.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[0]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet.createRow(2);
	            cell = row.createCell(0);
	            sheet.addMergedRegion(new CellRangeAddress(2,3,0,0));
	            cell.setCellValue("No");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,1,3));
	            cell.setCellValue("DATE");
	            cell.setCellStyle(headerCellStyle);
	            // 합병된 cell에도 style 적용
	            cell = row.createCell(2);
	            cell.setCellStyle(headerCellStyle);
	            cell = row.createCell(3);
	            cell.setCellStyle(headerCellStyle);

	            // Type Header - Meter
	            cell = row.createCell(typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader, typeHeader+4));
	            cell.setCellValue("Meter");
	            cell.setCellStyle(typeCellStyle);
	            
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);

	            // Type Header - DCU
	            cell = row.createCell(++typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader,typeHeader+4));
	            cell.setCellValue("DCU");
	            cell.setCellStyle(typeCellStyle);

	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            
	            // Type Header - RF(ZRU)
	            cell = row.createCell(++typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader,typeHeader+4));
	            cell.setCellValue("ZRU");
	            cell.setCellStyle(typeCellStyle);

	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            
	            // Type Header - MMIU
	            cell = row.createCell(++typeHeader);
	            sheet.addMergedRegion(new CellRangeAddress(2,2,typeHeader,typeHeader+4));
	            cell.setCellValue("MMIU");
	            cell.setCellStyle(typeCellStyle);
	            
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            cell = row.createCell(++typeHeader);
	            cell.setCellStyle(typeCellStyle);
	            

	            // Header2 - DATE
	            row = sheet.createRow(statusRow);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("Date");
	            cell.setCellStyle(dateCellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("Week");
	            cell.setCellStyle(dateCellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("Time");
	            cell.setCellStyle(dateCellStyle);
	            
	            
	            // Header2 - A24, NA24, NA48, UNKNOWN
	            for(int i = 4; i<24; i+=5) {
	            	
	            	//sheet.autoSizeColumn(i);  
	                //sheet.setColumnWidth(i, 256 * 8);	//A24
	            	cell = row.createCell(i);
	            	cell.setCellValue("A24");
	            	cell.setCellStyle(a24CellStyle);
	            	
	            	//sheet.autoSizeColumn(i+1);  
	            	//sheet.setColumnWidth(i+1, 256 * 8);	//NA24
	            	cell = row.createCell(i+1);
	            	cell.setCellValue("NA24");
	            	cell.setCellStyle(na24CellStyle);
	            	
	            	//sheet.autoSizeColumn(i+2);  
	            	//sheet.setColumnWidth(i+2, 256 * 8);	//NA48
	            	cell = row.createCell(i+2);
	            	cell.setCellValue("NA48");
	            	cell.setCellStyle(na48CellStyle);
	            	
	            	//sheet.autoSizeColumn(i+3);  
	            	//sheet.setColumnWidth(i+3, 256 * 10);	//Unknown
	            	cell = row.createCell(i+3);
	            	cell.setCellValue("Unknown");
	            	cell.setCellStyle(unknownCellStyle);
	            	
	            	//sheet.autoSizeColumn(i+4);  
	            	//sheet.setColumnWidth(i+4, 256 * 10);	//Total
	            	cell = row.createCell(i+4);
	            	cell.setCellValue("Total");
	            	cell.setCellStyle(totalCellStyle);
	            }
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet.createRow(dataStartRow);
	            int a24=0, na24=0, na48=0, unknown=0, total=0, k=0;
	            String [] typeList = {"energymeter", "dcu", "zru", "mmiu"};
	            Map<String,Object> resultMap;
	            
	            for(int i = 0; i< resultList.get(0).size(); i++) {
	            	row2 = sheet.createRow(dataStartRow+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(0).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("No").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(resultMap.get("dateDate").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(resultMap.get("dateWeek").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(resultMap.get("dateTime").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet.autoSizeColumn(3);
		            
		            for(int j = 4; j<24; j+=5) {
		            	a24=0; na24=0; na48=0; unknown=0; total=0;
		            	k=(j-4)/5;
		            	cell = row2.createCell(j);
		            	a24 = Integer.parseInt(resultMap.get(typeList[k]+"A24").toString());
			            cell.setCellValue(a24);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j);
			            		
			            cell = row2.createCell(j+1);
			            na24 = Integer.parseInt(resultMap.get(typeList[k]+"NA24").toString());
			            cell.setCellValue(na24);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+1);
			            
			            cell = row2.createCell(j+2);
			            na48 = Integer.parseInt(resultMap.get(typeList[k]+"NA48").toString());
			            cell.setCellValue(na48);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+2);
			            
			            cell = row2.createCell(j+3);
			            unknown = Integer.parseInt(resultMap.get(typeList[k]+"Unknown").toString()); 
			            cell.setCellValue(unknown);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+3);
			            
			            cell = row2.createCell(j+4);
			            total = a24 + na24 + na48 + unknown;
			            cell.setCellValue(total);
			            cell.setCellStyle(dataCellStyle);
			            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			            //sheet.autoSizeColumn(j+4);
		            }
		        }
	            // Monitoring Data End
	            //Data Sheet End
	            
	            //Meter Sheet1 Start
	            colIdx = 0;
	            sheet1.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet1.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet1.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet1.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet1.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet1.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet1.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet1.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[1]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet1.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet1.createRow(3);
	            
	            for(int i = 0; i< resultList.get(1).size(); i++) {
	            	row2 = sheet1.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(1).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet1.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet1.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet1, keyList, resultList.get(1).size());
	            //Meter Sheet1 End
	            
	            //DCU Sheet2 Start
	            colIdx = 0;
	            sheet2.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet2.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet2.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet2.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet2.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet2.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet2.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet2.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[2]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet2.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet2.createRow(3);
	            
	            for(int i = 0; i< resultList.get(2).size(); i++) {
	            	row2 = sheet2.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(2).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet2.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet2.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet2, keyList, resultList.get(2).size());
	            //DCU Sheet2 End
	            
	            //ZRU Sheet3 Start
	            colIdx = 0;
	            sheet3.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet3.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet3.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet3.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet3.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet3.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet3.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet3.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[3]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet3.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet3.createRow(3);
	            
	            for(int i = 0; i< resultList.get(3).size(); i++) {
	            	row2 = sheet3.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(3).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet3.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet3.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet3, keyList, resultList.get(3).size());
	            // Repeater Sheet3 End
	            
	            //MMIU Sheet4 Start
	            colIdx = 0;
	            sheet4.setColumnWidth(colIdx++, 256 * 13);	//DATE
	            sheet4.setColumnWidth(colIdx++, 256 * 8);	//A24
	            sheet4.setColumnWidth(colIdx++, 256 * 8);	//NA24
	            sheet4.setColumnWidth(colIdx++, 256 * 8);	//NA48
	            sheet4.setColumnWidth(colIdx++, 256 * 10);	//Unknown
	            sheet4.setColumnWidth(colIdx++, 256 * 10);	//Total

	            row = sheet4.createRow(0);
	            cell = row.createCell(0);
	            //Title
	            sheet4.addMergedRegion(new CellRangeAddress(0, (short) 0, 0, (short) (titleColumnCnt)));
	            cell.setCellValue(reportTitle[4]);
	            cell.setCellStyle(titleCellStyle);

	            // Header Start
	            row = sheet4.createRow(2);
	            cell = row.createCell(0);
	            cell.setCellValue("Date");
	            cell.setCellStyle(headerCellStyle);
	            
	            cell = row.createCell(1);
	            cell.setCellValue("A24");
	            cell.setCellStyle(a24CellStyle);
	            
	            cell = row.createCell(2);
	            cell.setCellValue("NA24");
	            cell.setCellStyle(na24CellStyle);
	            
	            cell = row.createCell(3);
	            cell.setCellValue("NA48");
	            cell.setCellStyle(na48CellStyle);
	            
	            cell = row.createCell(4);
	            cell.setCellValue("Unknown");
	            cell.setCellStyle(unknownCellStyle);
	            
	            cell = row.createCell(5);
	            cell.setCellValue("Total");
	            cell.setCellStyle(totalCellStyle);
	            // Header End
	            
	            // Monitoring Data Start
	            row2 = sheet4.createRow(3);
	            
	            for(int i = 0; i< resultList.get(4).size(); i++) {
	            	row2 = sheet4.createRow(3+i);
	            	resultMap = new HashMap<String, Object>();
	            	resultMap = resultList.get(4).get(i);
	            	
		            cell = row2.createCell(0);
		            cell.setCellValue(resultMap.get("date").toString());
		            cell.setCellStyle(dataCellStyle2);
		            //sheet4.autoSizeColumn(0);

		            cell = row2.createCell(1);
		            cell.setCellValue(Integer.parseInt(resultMap.get("a24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(1);
		            
		            cell = row2.createCell(2);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na24").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(2);
		            
		            cell = row2.createCell(3);
		            cell.setCellValue(Integer.parseInt(resultMap.get("na48").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(3);

		            cell = row2.createCell(4);
		            cell.setCellValue(Integer.parseInt(resultMap.get("unknown").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(4);
		            
		            cell = row2.createCell(5);
		            cell.setCellValue(Integer.parseInt(resultMap.get("total").toString()));
		            cell.setCellStyle(dataCellStyle);
		            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		            //sheet4.autoSizeColumn(5);
		        }
	            // Monitoring Data End
	            
	            drawingChart(sheet4, keyList, resultList.get(4).size());
	            //RF Sheet4 End
	            
	            
	            // File Create
	            FileOutputStream fs = null;
	            try {
	                fs = new FileOutputStream(fileFullPath);
	                workbook.write(fs);
	                log.info("Excel Create Success ~ !");
	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
				}catch (Exception e) {
					log.error(e,e);
				}finally {
		            if (fs != null)
		                fs.close();
		        }
		
		} catch (Exception e) {
	    	log.error(e,e);
	    } // End Try
	}
	
	public void drawingChart(XSSFSheet sheet, List<String> keyList, int size) {
		try {
			
			Drawing dr = sheet.createDrawingPatriarch();
            ClientAnchor anchor = dr.createAnchor(0, 0, 0, 0, 8, 3, 20, 16);
            
            Chart chart = dr.createChart(anchor);
            ChartLegend legend = chart.getOrCreateLegend();
            legend.setPosition(LegendPosition.RIGHT);
            
            LineChartData data = chart.getChartDataFactory().createLineChartData();
       	 
    	    ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
    	    ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
    	    leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
    	    
    	    if(keyList.size() > 0) {
    	    	ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, new CellRangeAddress(3, 3+size-1, 0, 0));
    	    	for(int i = 1; i <= keyList.size(); i++){
    	    		ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(3, 3+size-1, i, i));
    	    		data.addSeries(xs, ys).setTitle(keyList.get(i-1));
    	    	}
    	    }else {
    	    	ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, new CellRangeAddress(3, 3+size-1, 0, 0));
    	    	for(int i = 1; i < keyList.size(); i++){
    	    		ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(3, 3+size-1, i, i));
    	    		data.addSeries(xs, ys).setTitle(keyList.get(i-1));
    	    	}
    	    }
    	    
    	    chart.plot(data, bottomAxis, leftAxis);
    	    
    	    String[] colorArr = {
    	    		"#0718D7","#A99903","#FC8F00","#F31523"
            };
    	    
    	    XSSFChart xssfChart = (XSSFChart) chart;
            CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
            CTMarker ctMarker = CTMarker.Factory.newInstance();
            CTBoolean ctBool = CTBoolean.Factory.newInstance();
            CTSolidColorFillProperties fillProp = CTSolidColorFillProperties.Factory.newInstance();
            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            CTLineProperties lineProp = CTLineProperties.Factory.newInstance();
            CTShapeProperties ctShapeProperties = CTShapeProperties.Factory.newInstance();
            
            ctMarker.setSymbol(CTMarkerStyle.Factory.newInstance());
            ctBool.setVal(false);
            
            int p = 0;
            for (CTLineSer ser : plotArea.getLineChartArray()[0].getSerArray()) {
            	// Color 제작
            	if(p >= colorArr.length) p = 0;
            	Color color = hex2Rgb(colorArr[p++]);
                rgb.setVal(new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()});
                fillProp.setSrgbClr(rgb);
                lineProp.setSolidFill(fillProp);
                ctShapeProperties.setLn(lineProp);
                ctShapeProperties.setSolidFill(fillProp);
                
                ctMarker.setSpPr(ctShapeProperties); // Marker 색
                ser.setSmooth(ctBool);	// 그래프가 꺽이도록
                ser.setMarker(ctMarker);	// 항목마다 마킹
                
                ser.setSpPr(ctShapeProperties); // Line 색
            }
			
		}catch (Exception e) {
			log.debug(e,e);
		}
		
	}
	
}
