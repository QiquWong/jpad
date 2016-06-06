package standaloneutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public final class MyXLSWriteUtils {

	public static CellStyle 
	styleTitlesFirstCol = null, styleTitlesOtherCols = null, 
	styleDefault = null, styleSubtitles = null,
	styleFirstColumn = null, styleMethodComparison = null,
	styleFirstColumnTemp = null;

	public static Font
	fontTitle = null, fontDefault = null, fontSubtitles = null;

	private MyXLSWriteUtils() {}
	
	/** 
	 * Set each style used in the xls files
	 * 
	 * @author Lorenzo Attanasio
	 */
	public static void setXLSstyle(Workbook workbook) {

		styleTitlesFirstCol = workbook.createCellStyle();
		fontTitle = workbook.createFont();
		fontTitle.setFontName("Times New Roman");
		fontTitle.setBoldweight(Font.BOLDWEIGHT_BOLD);
		fontTitle.setFontHeightInPoints((short) 13);
		styleTitlesFirstCol.setFont(fontTitle);
		styleTitlesFirstCol.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.index);
		styleTitlesFirstCol.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleTitlesFirstCol.setAlignment(CellStyle.ALIGN_LEFT);
		
		styleTitlesOtherCols = workbook.createCellStyle();
		styleTitlesOtherCols.cloneStyleFrom(styleTitlesFirstCol);
		styleTitlesOtherCols.setAlignment(CellStyle.ALIGN_CENTER);
		
		styleSubtitles = workbook.createCellStyle();		
		fontSubtitles = workbook.createFont();
		fontSubtitles.setFontName("Times New Roman");
		fontSubtitles.setBoldweight(Font.BOLDWEIGHT_BOLD);
		fontSubtitles.setFontHeightInPoints((short) 12);
		styleSubtitles.setFont(fontSubtitles);
		styleSubtitles.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		styleSubtitles.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleSubtitles.setAlignment(CellStyle.ALIGN_LEFT);

		styleDefault = workbook.createCellStyle();		
		fontDefault = workbook.createFont();
		fontDefault.setFontName("Times New Roman");
		fontDefault.setFontHeightInPoints((short) 11);
		styleDefault.setFont(fontDefault);
		styleDefault.setAlignment(CellStyle.ALIGN_CENTER);

		styleFirstColumn = workbook.createCellStyle();
		styleFirstColumn.cloneStyleFrom(styleDefault);
		styleFirstColumn.setAlignment(CellStyle.ALIGN_LEFT);

		styleFirstColumnTemp = workbook.createCellStyle();
		styleFirstColumnTemp.cloneStyleFrom(styleFirstColumn);

		styleMethodComparison = workbook.createCellStyle();
		styleMethodComparison.cloneStyleFrom(styleDefault);
		styleMethodComparison.setAlignment(CellStyle.ALIGN_CENTER);
	}

	/** 
	 * Compare aircrafts copying each xls file to a single file.
	 * Each aircraft takes one column.
	 * The comparison file must be initialized with the first 
	 * aircraft xls file before calling this function
	 * 
	 * @param FILE_TO_READ
	 * @param FILE_TO_WRITE
	 * @param index
	 */
	public static void compareXLSs(
			String FILE_TO_READ, 
			String FILE_TO_WRITE, 
			int index) {

		FileInputStream fileRead, fileWrite;
		Workbook workbookRead, workbookWrite;
		CellStyle styleDefaultLocal;

		try {

			fileRead = new FileInputStream(new File(FILE_TO_READ  + ".xls"));
			fileWrite = new FileInputStream(new File(FILE_TO_WRITE  + ".xls"));

			//Get the workbook instance for XLS file to read
			workbookRead = new HSSFWorkbook(fileRead);

			//Get the workbook instance for XLS file to write
			workbookWrite = new HSSFWorkbook(fileWrite);

			styleDefaultLocal = workbookWrite.createCellStyle();		
			styleDefaultLocal.cloneStyleFrom(styleDefault);

			Sheet sheetRead, sheetWrite;

			Cell cellRead, cellWrite;
			CellStyle newCellStyle = workbookWrite.createCellStyle();

			for (int i = 0; i < workbookRead.getNumberOfSheets(); i++) {

				//Get first sheet from the workbook to read
				sheetRead = workbookRead.getSheetAt(i);

				//Get first sheet from the workbook to write
				sheetWrite = workbookWrite.getSheetAt(i);

				//Iterate through each rows from first sheet of xls file to read
				Iterator<Row> rowIteratorRead = sheetRead.iterator();
				Iterator<Row> rowIteratorWrite = sheetWrite.iterator();

				while(rowIteratorRead.hasNext() && rowIteratorWrite.hasNext()) {
					Row rowRead = rowIteratorRead.next();
					Row rowWrite = rowIteratorWrite.next();

					////////////////////////////////////////////////////////////////
					// Read third column (index is 2) and copy it to column #index 
					////////////////////////////////////////////////////////////////
					cellRead = rowRead.getCell(2);
					cellWrite = rowWrite.createCell(index);

					if (cellRead != null) {

						sheetWrite.setColumnWidth(index, 5000);

						switch(cellRead.getCellType()) {
						case Cell.CELL_TYPE_NUMERIC:
							cellWrite.setCellValue(cellRead.getNumericCellValue());
							break;
						case Cell.CELL_TYPE_STRING:
							cellWrite.setCellValue(cellRead.getStringCellValue());
							break;
						}

						if (rowWrite.getRowNum() == 1) {
							cellWrite.setCellStyle(newCellStyle);
						} else {
							cellWrite.setCellStyle(styleDefaultLocal);
						}
					}
				}

			}

			fileRead.close();
			//			FileOutputStream outRead =
			//					new FileOutputStream(new File(FILE_TO_READ + ".xls"));
			//			workbookRead.write(outRead);
			//			outRead.close();

			fileWrite.close();
			FileOutputStream outWrite =
					new FileOutputStream(new File(FILE_TO_WRITE + ".xls"));
			workbookWrite.write(outWrite);
			outWrite.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/** 
	 * Create a new sheet in xls file
	 * 
	 * @author Lorenzo Attanasio
	 * @param name
	 * @param workbook TODO
	 * @return 
	 */
	public static Sheet createNewSheet(
			String name, 
			String aircraftName, 
			Workbook workbook) {

		Sheet sheet = workbook.createSheet(name);
		sheet.setColumnWidth(0, 10000);
		sheet.setColumnWidth(1, 2500);
		for (int i = 2; i < 15; i++) {
			sheet.setColumnWidth(i, 5000);
		}

		sheet.setDefaultRowHeightInPoints(18);

		Row row0 = sheet.createRow(0);
		Cell cell = row0.createCell((short) 2);
		cell.setCellValue(aircraftName);

		Row row1 = sheet.createRow(1);

		// Aqua background
		//	    style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		//	    style.setFillPattern(CellStyle.FINE_DOTS);

		Cell cell0 = row1.createCell((short) 0);
		cell0.setCellValue("Description");
		cell0.setCellStyle(styleTitlesFirstCol);

		Cell cell1 = row1.createCell((short) 1);
		cell1.setCellValue("Unit");
		cell1.setCellStyle(styleTitlesOtherCols);

		Cell cell2 = row1.createCell((short) 2);
		cell2.setCellValue("Value");
		cell2.setCellStyle(styleTitlesOtherCols);

		return sheet;

	}

}
