package sandbox.mr;

import java.io.File;

import configuration.MyConfiguration;
import standaloneutils.MyChartToFileUtils;
import writers.JPADStaticWriteUtils;

public class DownwashCalculator_03 {
	
	    // VARIABLE DECLARATION
		
		private double aspectRatioDouble;
		private double taperRatioDouble; 
		
		// TO EVALUATE:
		private double downwashDerivateMethodOne;
		private double downwashDerivateMethodTwo;
		


		// BUILDER
		public DownwashCalculator_03(double aspectRatioDouble,
				                  double taperRatioDouble){
			
			this.aspectRatioDouble = aspectRatioDouble;
			this.taperRatioDouble =taperRatioDouble;
		}

		
		// METHODS 
		
	    //TODO Complete javadoc with the name of the method.
		
		/**
		 * This method calculates the downwash gradient using Datcom formula.
		 *
		 * 
		 * @param distAerodynamicCenter Distance between the points at c/4 of the mean 
		 * aerodynamic chord of the wing and the horizontal tail.
		 * @param distWingToHTail Distance between the horizontal tail and the wing root chord
		 * @param wingSpanDouble Wing span
		 * @param sweepQuarterChordEq in radians
		 * 
		 * @author  Manuela Ruocco
		 */
	

	// Datcom Method	
	public double calculateDownwashDatcom(double distAerodynamicCenter,double distWingToHTail,double wingSpanDouble,double sweepQuarterChordEq){
		double ka, kLambda, kh;
		
	ka=(1/aspectRatioDouble)-(1/(1+Math.pow(aspectRatioDouble,1.7)));
	kLambda=(10-3*taperRatioDouble)/7;
	kh=(1-(distWingToHTail/wingSpanDouble))/Math.cbrt(2*distAerodynamicCenter/wingSpanDouble);
	
	downwashDerivateMethodOne= 4.44*Math.pow( ka * kLambda * kh * Math.sqrt(Math.cos(sweepQuarterChordEq)),1.19);
	return downwashDerivateMethodOne;
	}
	
	
	/**
	 * This method calculates the downwash gradient using Delft formula.
	 *
	 * 
	 * @param distAerodynamicCenter Distance between the points at c/4 of the mean 
	 * aerodynamic chord of the wing and the horizontal tail.
	 * @param distWingToHTail Distance between the horizontal tail and the wing root chord
	 * @param clAlfa 
	 * @param wingSpanDouble Wing span
	 * @param sweep quarter cord of equivalent wing in radians
	 * 
	 * @author  Manuela Ruocco
	 */
	
	// Delft Method
	public double calculateDownwashDelft(double distAerodynamicCenter,double distWingToHTail,
			double clAlfaAndersonDouble, double wingSpanDouble,double sweepQuarterChordEq ){
		double keGamma, keGammaZero;
		double semiWingSpan=wingSpanDouble/2;
		double r=distAerodynamicCenter/semiWingSpan;
		double rPow=Math.pow(r,2);
		double m=distWingToHTail/semiWingSpan;
		double mpow=Math.pow(m, 2);
		
		
		keGamma=(0.1124+0.1265*sweepQuarterChordEq+0.1766*Math.pow(sweepQuarterChordEq,2))
				/rPow+0.1024/r+2;
		keGammaZero=0.1124/rPow+0.1024/r+2;
		
		double kFraction=keGamma/keGammaZero;
		double first= (r/(rPow+ mpow))*(0.4876/Math.sqrt(rPow+0.6319+mpow));
		double second= 1+Math.pow(rPow/(rPow+0.7915+5.0734*mpow),0.3113);
		double third = 1-Math.sqrt(mpow/(1+mpow));
		
		downwashDerivateMethodTwo=kFraction*(first+second*third)*(clAlfaAndersonDouble/(Math.PI*aspectRatioDouble));
		
		return downwashDerivateMethodTwo;
		
	}	
	
	//TODO implement third method
	
	//Plot the sweep angle-downwash gradient chart
	
	public static void createSweepAngleDownwashGradientChart(
			int n, // number of elements of sweep angle array
			double []  sweepAngles,
			double [] downwashArrayDatcom,
			double [] downwashArrayDelft
			){
		
		System.out.println("\n ----------WRITING CHART TO FILE-----------");
		
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Downwash_Gradient" + File.separator);
		
		// built array 
		
		
		double[][] sweepAngle_double_Arrays = new double[2][n];
		for (int i=0; i<n; i++){
			sweepAngle_double_Arrays[0][i] =  sweepAngles[i];
			sweepAngle_double_Arrays[1][i] =  sweepAngles[i]; 
			}
		
		double[][] downwashGadient_double_Arrays = new double[2][4];
		for (int i=0; i<4; i++){
			downwashGadient_double_Arrays[0][i] = downwashArrayDatcom[i];
			downwashGadient_double_Arrays[1][i] = downwashArrayDelft[i];
		}
		
		double [] legendDouble = new double [2];
		legendDouble[0] = 1;
		legendDouble[1] = 2;
		
		String [] legend = new String [2];
		legend[0] = "Metodo Datcom";
		legend[1] = "Metodo Delft";
		
	//TODO Try to modify legend name
		
		// plot using a double array as legend
		MyChartToFileUtils.plot(
				sweepAngle_double_Arrays,	downwashGadient_double_Arrays, // array to plot
				10.0, 21.0, 0.25, 0.36,					    // axis with limits
				"Sweep Angle at c/4", "Downwash Gradient", "degree", "ad.",	    // label with unit
				"Method ", legendDouble, "",						// legend
				subfolderPath, "Downwash_Gradient_LegendDouble");			    // output informations
		
		// plot using a String array as legend
		MyChartToFileUtils.plot(
				sweepAngle_double_Arrays,	downwashGadient_double_Arrays, // array to plot
				10.0, 21.0,0.25, 0.36,					    // axis with limits
				"Sweep Angle at c/4", "Downwash Gradient", "degree", "ad.",	    // label with unit
				legend,					// legend
				subfolderPath, "Downwash_Gradient");			    // output informations
		

		// plot with no legend
		MyChartToFileUtils.plotNoLegend(
				sweepAngle_double_Arrays,	downwashGadient_double_Arrays, // array to plot
				10.0, 21.0,0.25, 0.36,					    // axis with limits
				"Sweep Angle at c/4", "Downwash Gradient", "degree", "ad.",	    // label with unit
				subfolderPath, "Downwash_Gradient_NoLegend");			    // output informations
		
		
		
		System.out.println("\n ----------DONE-----------");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
