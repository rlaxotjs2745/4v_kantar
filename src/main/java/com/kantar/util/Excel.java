package com.kantar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

@Component
public class Excel {
	public List<String[]> getCsvListData(String file) throws Exception{
		List<String[]> allRows = null;
		try{
			CsvParserSettings settings = new CsvParserSettings();
			settings.getFormat().setLineSeparator("\n");
			settings.setMaxColumns(65535);
			settings.setMaxCharsPerColumn(65535);
			
			CsvParser parser = new CsvParser(settings);
			File _f = new File(file);
			if(!_f.exists()){
				return allRows;
			}
			Reader inputReader = new InputStreamReader(new FileInputStream(new File(file)), "UTF-8");
			if(inputReader!=null){
				allRows = parser.parseAll(inputReader);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return allRows;
	}

	public String getCellValue(XSSFCell cell) {
		String value = "";
		
		if(cell == null){
			return value;
		}

		switch (cell.getCellType()) {
			case STRING:
				value = cell.getStringCellValue();
				break;
			case NUMERIC:
				value = (int) cell.getNumericCellValue() + "";
				break;
			default:
				break;
		}
		return value;
	}

	// 엑셀파일의 데이터 목록 가져오기 (파라미터들은 위에서 설명함)
	public List<Map<String, Object>> getListData(MultipartFile file, int startRowNum, int columnLength) throws IOException, InvalidFormatException {
		
		OPCPackage opcPackage = OPCPackage.open(file.getInputStream());
		return getListDataProc(opcPackage, startRowNum, columnLength);
	}
	public List<Map<String, Object>> getListData(String file, int startRowNum, int columnLength) throws IOException, InvalidFormatException {
		OPCPackage opcPackage = OPCPackage.open(file);
		return getListDataProc(opcPackage, startRowNum, columnLength);
	}

	public List<Map<String, Object>> getListDataProc(OPCPackage opcPackage, int startRowNum, int columnLength) throws IOException, InvalidFormatException{
		List<Map<String, Object>> excelList = new ArrayList<Map<String,Object>>();
		
		try {
			@SuppressWarnings("resource")
			XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);

			// 첫번째 시트
			XSSFSheet sheet = workbook.getSheetAt(0);
			
			int rowIndex = 0;
			int columnIndex = 0;

			// 첫번째 행(0)은 컬럼 명이기 때문에 두번째 행(1) 부터 검색
			for (rowIndex = startRowNum; rowIndex < sheet.getLastRowNum() + 1; rowIndex++) {
				XSSFRow row = sheet.getRow(rowIndex);

				// 빈 행은 Skip
				if (row.getCell(0) != null && !row.getCell(0).toString().isBlank()) {
					Map<String, Object> map = new HashMap<String, Object>();

					int cells = columnLength;

					for (columnIndex = 0; columnIndex <= cells; columnIndex++) {
						XSSFCell cell = row.getCell(columnIndex);
						map.put(String.valueOf(columnIndex), getCellValue(cell));
					    System.out.println(rowIndex + " 행 : " + columnIndex+ " 열 = " + getCellValue(cell));
					}
					
					excelList.add(map);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return excelList;
	}
}