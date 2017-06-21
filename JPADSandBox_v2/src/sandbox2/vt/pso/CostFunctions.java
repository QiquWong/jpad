package sandbox2.vt.pso;

import java.util.List;
import java.util.stream.Collectors;

import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class CostFunctions {

	public static double sphere(List<Double> x) {
		
		return MyMathUtils.summation(
				0,
				x.size(),
				MyArrayUtils.convertToDoublePrimitive(
						x.stream()
						.map(xe -> Math.pow(xe, 2))
						.collect(Collectors.toList())
						)
				);
		
	}
	
	// TODO : ADD OTHER COSTS FUNCTIIONS WHEN AVAILABLE
	
}
