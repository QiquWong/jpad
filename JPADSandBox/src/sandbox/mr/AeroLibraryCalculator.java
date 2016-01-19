package sandbox.mr;

public class AeroLibraryCalculator {
	

	
	//METHODS 
	
public static double calculateCLalfaPolhamus(double aspectRatio, double lambdaLE, double mach, double taperRatio){
		
		double k=calculateKFactorforCLalfa(aspectRatio, lambdaLE);
		
		double lambdaMean = calculateSweepAngle(lambdaLE, 0.50, taperRatio, aspectRatio);
		
		double a = aspectRatio*aspectRatio*(1-mach*mach)/(k*k);
		
		double tan = Math.tan(lambdaMean);
		
		double b = 1 + (tan*tan)/(1 - Math.pow(mach, 2)); 
		
		double argument = a*b + 4;
		
		double denominator = 2 + Math.sqrt(argument);
		
		double clalfa = ((2.)*Math.PI*aspectRatio)/denominator;
		
		return clalfa/1.17171134;
		}




public static double calculateKFactorforCLalfa(double aspectRatio, double lambdaLE)
{
	// NOTE: angle in radian
  double k=0;
	if (aspectRatio < 4){
		
		k = 1. + (aspectRatio*(1.87-0.000233*lambdaLE))/100;
	}
	else if (aspectRatio >=4) {
		k =  1. + (8.2-2.3*lambdaLE-aspectRatio*(0.22-0.153*lambdaLE))/100;
	}
	 //System.out.println("\n k =" + k );
	return k;
	
}
	
	public static double calculateSweepAngle(double lambdaLE, double chord, double TaperRatio, double AspectRatio){
		
		double b = ((4*chord*(1-TaperRatio))/(AspectRatio*(1+TaperRatio)));
		double c = Math.tan(lambdaLE);
		double a = c - b ;
		
		return Math.atan(a);
		
}



}
