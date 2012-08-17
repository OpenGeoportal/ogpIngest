package org.OpenGeoPortal.Ingest.Mvc;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.OpenGeoPortal.Ingest.IngestStatus;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.springframework.web.servlet.view.document.AbstractExcelView;
import org.springframework.web.util.HtmlUtils;

public class IngestStatusSpreadsheetView extends AbstractExcelView {

	    protected void buildExcelDocument(
	        Map model,
	        HSSFWorkbook wb,
	        HttpServletRequest req,
	        HttpServletResponse resp)
	        throws Exception {

	        IngestStatus ingestStatus = (IngestStatus) model.get("ingestStatus");
	        
	        List<Map<String,String>> errors = ingestStatus.getErrors();
	        List<Map<String,String>> warnings = ingestStatus.getWarnings();
	        List<Map<String,String>> successes = ingestStatus.getSuccesses();
	        
	        writeSheet(successes, "Successes", wb);
	        writeSheet(warnings, "Warnings", wb);
	        writeSheet(errors, "Errors", wb);
	  
	    }
	    
	    private void writeSheet(List<Map<String,String>> valueMaps, String worksheetName, HSSFWorkbook wb){
	    	  // Set column widths
	    	HSSFSheet sheet = wb.createSheet(worksheetName);
	    	sheet.setColumnWidth(Short.parseShort("0"), Short.parseShort("15000"));
	    	sheet.setColumnWidth(Short.parseShort("1"), Short.parseShort("30000"));

	        //header style
	        HSSFCellStyle headerStyle;
	        HSSFFont headerFont = wb.createFont();
	        headerFont.setFontHeightInPoints((short)11);
	        headerStyle = wb.createCellStyle();
	        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	        headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	        headerStyle.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
	        headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	        headerStyle.setFont(headerFont);
	        headerStyle.setWrapText(true);
	    	
	        //header row
	        HSSFRow headerRow = sheet.createRow(0);
	        headerRow.setHeightInPoints(30);
	        HSSFCell headerCell0 = headerRow.createCell((short) 0);
	        HSSFCell headerCell1 = headerRow.createCell((short) 1);

	        headerCell0.setCellStyle(headerStyle);
	    	setText(headerCell0, "Layer Name");
	        headerCell1.setCellStyle(headerStyle);
	    	setText(headerCell1, "Message");
	    	
	    	int counter = 1;
	        for (Map<String,String>valueMap : valueMaps) {
		        HSSFRow dataRow = sheet.createRow(counter);
	        	String layer = valueMap.get("layer");
	        	String status = valueMap.get("status");
	        	status = HtmlUtils.htmlUnescape(status);
	        	HSSFCell currentCell0 = dataRow.createCell((short) 0);
	        	HSSFCell currentCell1 = dataRow.createCell((short) 1);
	            setText(currentCell0, layer);
	            setText(currentCell1, status);
	            counter++;
	        }
	    }
	
}
