package optimization;

import java.util.Arrays;
import java.util.stream.Collectors;

import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;

public class CostFunctions {

	public static double sphere(Double[] x) {
		
		return MyMathUtils.summation(
				0,
				x.length,
				MyArrayUtils.convertToDoublePrimitive(
						Arrays.stream(x)
						.map(xe -> Math.pow(xe, 2))
						.collect(Collectors.toList())
						)
				);
		
	}
	
	public static double costFunction3Vars(Double[] x, MyInterpolatingFunction costFunction) {
		
		if(x.length != 3 || (costFunction.getX() == null && costFunction.getY() == null && costFunction.getZ() == null)) {
			System.err.println("DESIGN VARIABLES AND COST FUNCTION MUST HAVE 3 DIMENSIONS !!");
			System.exit(1);
		}
		
		return costFunction.value(x[0], x[1], x[2]);
				
		
	}
	
	// TODO : ADD OTHER COSTS FUNCTIIONS WHEN AVAILABLE
	
}
