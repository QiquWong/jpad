package sandbox.vt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.MyChartToFileUtils;
import writers.JPADStaticWriteUtils;

/**
 * This test has the purpose of plotting a series of double value in order to give more 
 * flexibility to the plotting feature; in particular this could allow to plot vectors 
 * of different size and other useful functions. 
 * (see \\ADOPT_PROJECT\jpad\DOCS\jfreechart_tutorial at p.3 )
 * (see the method plotJFreeChart in MyChartToFileUtils for see how it works)
 * 
 * @author Vittorio Trifari
 *
 */

public class Test_JFreeChart {
	
	//--------------------------------------------------------------------------------------
	// MAIN (testing)
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		
		System.out.println("-------------------------------------------------------------");
		System.out.println("JFreeChart TEST");
		System.out.println("-------------------------------------------------------------");
		
		//----------------------------------------------------------------------------------
		// Assign default folders and paths
		MyConfiguration.initWorkingDirectoryTree();
		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Test_Plot_List" + File.separator);
		
		// Generating List to be plotted
		List<Double[]> xList = new ArrayList<Double[]>();
		xList.add(new Double[] {1.,2.,3.,4.,5.});
		xList.add(new Double[] {1.,2.,3.,4.});
		xList.add(new Double[] {1.,2.,3.,});
		
		List<Double[]> yList = new ArrayList<Double[]>();
		yList.add(new Double[] {3.,10.,1.,5.,2.});
		yList.add(new Double[] {1.,-10.,0.,-5.});
		yList.add(new Double[] {0.,5.,4.});
		
		// Generating legend list
		List<String> legend = new ArrayList<String>();
		legend.add("Series1");
		legend.add("Series2");
		legend.add("Series3");
		
		System.out.println("\n------------------GENERATING CHART-------------------------");
		MyChartToFileUtils.plotJFreeChart(
				xList, yList,						// List to be plotted
				"Test Chart", "x", "y",				// title and labels
				null, null, null, null,				// axis
				"","",								// units
				true, legend, 						// legend visibility and values 
				subfolderPath, "JFreeChart_Test"	// output information
				);
	}
	//---------------------------------------------------------------------------------------
	// END OF TEST
}