package sandbox2.vc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import standaloneutils.JPADProperty;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXLSUtils;


public class TestReadXlsx {
	
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-d", aliases = { "--database-path" }, required = true,
			usage = "path for database files")
	private File _databasePath;
	
	@Option(name = "-o", aliases = { "--output" }, required = true,
			usage = "my output file")
	private File _outputFile;
	
	// declaration necessary for Concrete Object usage
	public CmdLineParser theCmdLineParser;
	public JPADXmlReader reader;
	
	@Argument
	private List<String> arguments = new ArrayList<String>();

	public TestReadXlsx (){
		theCmdLineParser = new CmdLineParser(this);
	}
	
	public static void main(String[] args) throws CmdLineException, IOException {
		
		// Allocate the main object
		TestReadXlsx theTestObject = new TestReadXlsx();
		
		theTestObject.theCmdLineParser.parseArgument(args);
		String fileNameWithPathAndExt = theTestObject.get_inputFile().getAbsolutePath();
		//String databaseDirectoryAbsolutePath = theTestObject.get_databasePath().getAbsolutePath();
		
		System.out.println("File name: " + fileNameWithPathAndExt);
		
		File input = new File(fileNameWithPathAndExt);
		
		FileInputStream readerXLS = new FileInputStream(input);
		Workbook workbook;
		if (input.getAbsolutePath().endsWith(".xls")) {
			workbook = new HSSFWorkbook(readerXLS);
		}
		else if (input.getAbsolutePath().endsWith(".xlsx")) {
			workbook = new XSSFWorkbook(readerXLS);
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}

		//---------------------------------------------------------------
		// Sheet 1
//		Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "Sheet 1");
//		if(sheetGlobalData != null) {
//			Cell test1Cell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "test1").get(0)).getCell(2);
//			if(test1Cell != null)
//				test1 = Amount.valueOf(test1Cell.getNumericCellValue(), SI.KILOGRAM);
		}
	
	public File get_inputFile() {
		return _inputFile;
	}
	

}
