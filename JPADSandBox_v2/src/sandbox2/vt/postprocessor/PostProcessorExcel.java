package sandbox2.vt.postprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class PostProcessorExcel {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	List<String> _csvFilenameWithPathAndExtList;
	List<Boolean> _holdOnList;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:
	public PostProcessorExcel (
			List<String> csvFilenameWithPathAndExtList,
			List<Boolean> holdOnList
			) {
		
		this._csvFilenameWithPathAndExtList = csvFilenameWithPathAndExtList;
		this._holdOnList = holdOnList;
		
		_csvFilenameWithPathAndExtList
			.stream()
				.forEach(x -> importFromCSV(x));
		
	}
	
	//------------------------------------------------------------------------------------------
	// METHODS:
	private void importFromCSV(String csvFilenameWithPathAndExt) {

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFilenameWithPathAndExt));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] country = line.split(cvsSplitBy);

                System.out.println("Country [code= " + country[4] + " , name=" + country[5] + "]");

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	public void createXls(List<String> csvFilenameWithPathAndExtList) throws InvalidFormatException, IOException {		
		
		Workbook wb;
		File outputFile = new File("Output.xlsx");
		if (outputFile.exists()) { 
		   outputFile.delete();		
		   System.out.println("Deleting the old .xls file ...");
		} 
		
		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}
		
		//---------------------------------------------------------------------------------------
		// DATA SHEETS:
		Sheet sheetAcceleration = wb.createSheet("Acceleration");
		
		List<String> xlsAccelerationDescription = new ArrayList<String>();
		xlsAccelerationDescription.add("Time");
		xlsAccelerationDescription.add("Space");
		xlsAccelerationDescription.add("Acceleration");
		
		MyArray accelerationArray = new MyArray();
//		accelerationArray.setAmountList(output.getAcceleration());
		
		List<MyArray> xlsAccelerationList = new ArrayList<MyArray>();
//		xlsAccelerationList.add(timeArray);
//		xlsAccelerationList.add(groundDistenceArray);
		xlsAccelerationList.add(accelerationArray);
		
		List<String> xlsAccelerationUnit = new ArrayList<String>();
		xlsAccelerationUnit.add("s");
		xlsAccelerationUnit.add("m");
		xlsAccelerationUnit.add("m/(s^2)");
		
		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetAcceleration,
				xlsAccelerationDescription,
				xlsAccelerationList,
				xlsAccelerationUnit
				);
		
		//---------------------------------------------------------------------------------------
		// OUTPUT FILE CREATION:
		FileOutputStream fileOut = new FileOutputStream("Output.xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public List<String> getCSVFilenameWithPathAndExtList() {
		return _csvFilenameWithPathAndExtList;
	}

	public void setCSVFilenameWithPathAndExtList(List<String> _csvFilenameWithPathAndExtList) {
		this._csvFilenameWithPathAndExtList = _csvFilenameWithPathAndExtList;
	}

	public List<Boolean> getHoldOnList() {
		return _holdOnList;
	}

	public void setHoldOnList(List<Boolean> _holdOnList) {
		this._holdOnList = _holdOnList;
	}	
}
