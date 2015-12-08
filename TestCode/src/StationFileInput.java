

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StationFileInput {
	private FileInputStream whereStation;
	private String wherePath;
	private XSSFWorkbook workbook;
	private int index;
	private int rowindex;
	private int columnindex;
	private Vector<String>stationName;
	private Vector<Double>GPSx;
	private Vector<Double>GPSy;
	//private HashMap<String, Double>hashGPSx;
	//private HashMap<String, Double>hashGPSy;
	

	public StationFileInput(File station){
		try {
			whereStation = new FileInputStream(station);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			workbook = new XSSFWorkbook(whereStation);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rowindex = 0;
		columnindex = 0;
		index = 0;
		
		stationName = new Vector<String>();
		GPSx = new Vector<Double>();
		GPSy = new Vector<Double>();
		
		XSSFSheet sheet = workbook.getSheetAt(0);
		
		int rows = sheet.getPhysicalNumberOfRows();
		for(rowindex = 2; rowindex < rows; rowindex++){
			XSSFRow row = sheet.getRow(rowindex);
			if(row != null){
				int cells = row.getPhysicalNumberOfCells();
				for(columnindex = 0; columnindex <= cells; columnindex++){
					if(columnindex == 1){
						XSSFCell cell = row.getCell(columnindex);
						stationName.add(cell.getStringCellValue());
					}
					else if(columnindex == 7){
						XSSFCell cell = row.getCell(columnindex);
						GPSx.add(Double.parseDouble(cell.getStringCellValue()));
						
					}
					else if(columnindex == 8){
						XSSFCell cell = row.getCell(columnindex);
						GPSy.add(Double.parseDouble(cell.getStringCellValue()));
						
					}
				}
			}
			index++;
		}
		
		
	}
	
	public Vector<String> getStationName(){
		return stationName;
		
	}
	
	public Vector<Double> getGPSx(){
		return GPSx;
	}
	
	public Vector<Double> getGPSy(){
		return GPSy;
	}
		
}
