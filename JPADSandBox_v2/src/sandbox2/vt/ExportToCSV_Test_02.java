package sandbox2.vt;

import java.util.ArrayList;
import java.util.List;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.MyChartToFileUtils;

public class ExportToCSV_Test_02 {

	public static void main(String[] args) {

		
		/////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////
		double[][] xArrays1 = new double[][]{
			{1.0, 2.0, 3.0, 4.0},
			{5.0, 6.0, 7.0, 8.0},
			{9.0, 10.0, 11.0, 12.0}
		};
		double[][] yArrays1 = new double[][]{
			{100.0, 200.0, 300.0, 400.0},
			{500.0, 600.0, 700.0, 800.0},
			{900.0, 1000.0, 1100.0, 1200.0}
		};
		String legendName1 = "List";
		double[] legendValues1 = new double[]{1.0,2.0,3.0};
		
		//...............................................................
		MyConfiguration.initWorkingDirectoryTree();
		
		//...............................................................
		MyChartToFileUtils.plot(
				xArrays1, yArrays1,
				null, null, null, null,
				"Distance", "Height",
				"m", "ft",
				legendName1, legendValues1, "kg",
				MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR),
				"Plot_Test_01",
				true
				);
		
		
		/////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////
		List<double[]> xArrays2 = new ArrayList<>();
		xArrays2.add(new double[] {1.0, 2.0, 3.0, 4.0});
		xArrays2.add(new double[] {5.0, 6.0, 7.0, 8.0});
		xArrays2.add(new double[] {9.0, 10.0, 11.0, 12.0});
		
		List<double[]> yArrays2 = new ArrayList<>();
		yArrays2.add(new double[] {100.0, 200.0, 300.0, 400.0});
		yArrays2.add(new double[] {500.0, 600.0, 700.0, 800.0});
		yArrays2.add(new double[] {900.0, 1000.0, 1100.0, 1200.0});
			
		String legendName2 = "List";
		double[] legendValues2 = new double[]{1.0,2.0,3.0};
		
		//...............................................................
		MyConfiguration.initWorkingDirectoryTree();
		
		//...............................................................
		MyChartToFileUtils.plot(
				xArrays2, yArrays2,
				null, null, null, null,
				"Distance", "Height",
				"m", "ft",
				legendName2, legendValues2, true, "kg",
				MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR),
				"Plot_Test_02"
				);
		
		
		/////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////
		double[][] xArrays3 = new double[][]{
			{1.0, 2.0, 3.0, 4.0},
			{5.0, 6.0, 7.0, 8.0},
			{9.0, 10.0, 11.0, 12.0}
		};
		double[][] yArrays3 = new double[][]{
			{100.0, 200.0, 300.0, 400.0},
			{500.0, 600.0, 700.0, 800.0},
			{900.0, 1000.0, 1100.0, 1200.0}
		};

		String[] legendValues3 = new String[]{"a","b","c"};
		
		//...............................................................
		MyConfiguration.initWorkingDirectoryTree();
		
		//...............................................................
		MyChartToFileUtils.plot(
				xArrays3, yArrays3,
				null, null, null, null,
				"Distance", "Height",
				"m", "ft",
				legendValues3,
				MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR),
				"Plot_Test_03",
				true
				);
		
		
		/////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////
		double[][] xArrays4 = new double[][]{
			{1.0, 2.0, 3.0, 4.0},
			{5.0, 6.0, 7.0, 8.0},
			{9.0, 10.0, 11.0, 12.0}
		};
		double[][] yArrays4 = new double[][]{
			{100.0, 200.0, 300.0, 400.0},
			{500.0, 600.0, 700.0, 800.0},
			{900.0, 1000.0, 1100.0, 1200.0}
		};

		//...............................................................
		MyConfiguration.initWorkingDirectoryTree();
		
		//...............................................................
		MyChartToFileUtils.plotNoLegend(
				xArrays4, yArrays4,
				null, null, null, null,
				"Distance", "Height",
				"m", "ft",
				MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR),
				"Plot_Test_04",true
				);
		
		
		/////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////
		List<Double[]> xArrays5 = new ArrayList<>();
		xArrays5.add(new Double[] {1.0, 2.0, 3.0, 4.0});
		xArrays5.add(new Double[] {5.0, 6.0, 7.0, 8.0});
		xArrays5.add(new Double[] {9.0, 10.0, 11.0, 12.0});
		
		List<Double[]> yArrays5 = new ArrayList<>();
		yArrays5.add(new Double[] {100.0, 200.0, 300.0, 400.0});
		yArrays5.add(new Double[] {500.0, 600.0, 700.0, 800.0});
		yArrays5.add(new Double[] {900.0, 1000.0, 1100.0, 1200.0});
			
		List<String> legendValues5 = new ArrayList<>();
		legendValues5.add("A");
		legendValues5.add("B");
		legendValues5.add("C");
		
		//...............................................................
		MyConfiguration.initWorkingDirectoryTree();
		
		//...............................................................
		try {
			MyChartToFileUtils.plot(
					xArrays5, 
					yArrays5,
					"Plot Test 05", "Distance", "Height",
					null, null, null, null,
					"m", "ft",
					true, legendValues5,
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR),
					"Plot_Test_05",
					true
					);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}

}
