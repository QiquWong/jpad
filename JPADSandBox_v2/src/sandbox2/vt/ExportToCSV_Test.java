package sandbox2.vt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import writers.JPADStaticWriteUtils;

public class ExportToCSV_Test {

	public static void main(String[] args) {

		List<Double[]> xList = new ArrayList<>();
		List<Double[]> yList = new ArrayList<>();
		List<String> fileNameList = new ArrayList<>();
		List<String> xListName = new ArrayList<>();
		List<String> yListName = new ArrayList<>();
		
		//...............................................................
		xList.add(new Double[] {1.0, 2.0, 3.0, 4.0});
		xList.add(new Double[] {5.0, 6.0, 7.0, 8.0});
		xList.add(new Double[] {9.0, 10.0, 11.0, 12.0});
		//...............................................................
		yList.add(new Double[] {100.0, 200.0, 300.0, 400.0});
		yList.add(new Double[] {500.0, 600.0, 700.0, 800.0});
		yList.add(new Double[] {900.0, 1000.0, 1100.0, 1200.0});
		//...............................................................
		fileNameList.add("List 1");
		fileNameList.add("List 2");
		fileNameList.add("List 3");		
		//...............................................................
		xListName.add("X1");
		xListName.add("X2");
		xListName.add("X3");
		//...............................................................
		yListName.add("Y1");
		yListName.add("Y2");
		yListName.add("Y3");
		
		//...............................................................
		MyConfiguration.initWorkingDirectoryTree();
		
		//...............................................................
		JPADStaticWriteUtils.exportToCSV(
				xList, yList,
				fileNameList,
				xListName, yListName,
				MyConfiguration.createNewFolder(
						MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
						+ File.separator 
						+ "ExportToCSV_Test" 
						)
				);
		
	}

}
