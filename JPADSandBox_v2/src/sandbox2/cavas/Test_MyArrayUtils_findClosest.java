package sandbox2.cavas;

import java.util.Arrays;

import javaslang.Tuple2;
import standaloneutils.MyArrayUtils;

public class Test_MyArrayUtils_findClosest {

	public static void main(String[] args) {
		
		double arr[] = { -1.5, -0.5, -0.002, 0.0002, 2.0, 3.0, 5.4, 6.0 };
		
		System.out.println("Array: " + Arrays.toString(arr));
		
        Double target = 2.1;
        Double tolerance = 1e-3;
        Tuple2<Integer, Double> res = MyArrayUtils.findClosest(
        		MyArrayUtils.convertArrayDoublePrimitiveToList(arr),target, tolerance);
        
        System.out.println("Target: " + target + " --> Index: " + res._1 + " Value: " + res._2);

        target = 0.0;
        tolerance = 1e-4;
        res = MyArrayUtils.findClosest(
        		MyArrayUtils.convertArrayDoublePrimitiveToList(arr),target, tolerance);
        
        System.out.println("Target: " + target + " --> Index: " + res._1 + " Value: " + res._2);
        
        target = 0.0;
        tolerance = 1e-2;
        res = MyArrayUtils.findClosest(
        		MyArrayUtils.convertArrayDoublePrimitiveToList(arr),target, tolerance);
        
        System.out.println("Target: " + target + " --> Index: " + res._1 + " Value: " + res._2);

        target = 7.5;
        tolerance = 1e-4;
        res = MyArrayUtils.findClosest(
        		MyArrayUtils.convertArrayDoublePrimitiveToList(arr),target, tolerance);
        
        System.out.println("Target: " + target + " --> Index: " + res._1 + " Value: " + res._2);

        target = -5.0;
        tolerance = 1e-4;
        res = MyArrayUtils.findClosest(
        		MyArrayUtils.convertArrayDoublePrimitiveToList(arr),target, tolerance);
        
        System.out.println("Target: " + target + " --> Index: " + res._1 + " Value: " + res._2);
        
	}

}
