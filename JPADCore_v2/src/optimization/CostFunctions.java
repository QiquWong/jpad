package optimization;

import java.util.Arrays;
import java.util.stream.Collectors;

import standaloneutils.MyArrayUtils;
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
	
	// TODO : ADD OTHER COSTS FUNCTIIONS WHEN AVAILABLE
	
}
