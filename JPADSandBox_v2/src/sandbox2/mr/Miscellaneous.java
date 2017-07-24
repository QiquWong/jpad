package sandbox2.mr;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class Miscellaneous {

	public static void main(String[] args) throws IOException, RowsExceededException, WriteException {
		// TODO Auto-generated method stub

		Amount<?> clAlpha = Amount.valueOf(0.1047, NonSI.DEGREE_ANGLE.inverse());
		System.out.println("CL ALPHA " + clAlpha);
		System.out.println("CL ALPHA " + clAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue());
		System.out.println("CL ALPHA " + clAlpha.to(SI.RADIAN.inverse()));
		
		double [] prova = {1,7,3,5,9};
		System.out.println(" vettore prima " + Arrays.toString(prova));
		Arrays.sort(prova);
		System.out.println(" vettore dopo " + Arrays.toString(prova));
		
//		String outputChartPath = 	MyConfiguration.createNewFolder(
//				"C:\\Users\\manue\\Desktop"
//				+ File.separator 
//				+ "XlsFiles"
//				+ File.separator
//				);
//		
//		WritableWorkbook workbook = Workbook.createWorkbook(new File(outputChartPath + File.separator + "prova.xls"));
//
//		WritableSheet sheet = workbook.createSheet("First Sheet", 0);
//
//		Label label = new Label(0, 0, "A label record"); 
//		sheet.addCell(label); 
//		
//		workbook.write();
//		workbook.close();
	}

}
