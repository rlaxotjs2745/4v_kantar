package com.kantar.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Component
public class Excel {
	public List<String[]> getCsvListData(String file) throws Exception{
		List<String[]> allRows = new ArrayList<>();
		File targetFile = new File(file);

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(targetFile))) {
			CSVParser parser = CSVFormat.EXCEL.withFirstRecordAsHeader().withQuote('"').parse(bufferedReader); //엑셀타입 & 쌍따옴표 escape처리
			for (CSVRecord record : parser.getRecords()) {
				String[] line = new String[record.size()];
				for (int i = 0; i < line.length; i++) {
				  line[i] = record.get(i);
				}
				allRows.add(line);
			}
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