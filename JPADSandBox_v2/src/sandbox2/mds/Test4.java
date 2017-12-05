package sandbox2.mds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import standaloneutils.MyArrayUtils;

public class Test4 {

	public static double[] getArrayOfBracketedDoubles(String inputString) {

		double[] outputVector = null;
		
		inputString = inputString.trim();
		int openParenthesisCheck = inputString.indexOf('[');
		int closeParenthesisCheck = inputString.indexOf(']');
		// strip out brackets
		if ( openParenthesisCheck != -1){
			inputString = inputString.substring(openParenthesisCheck+1, inputString.length());
		}
		if ( closeParenthesisCheck != -1){
			inputString = inputString.substring(0, closeParenthesisCheck-1);
		}

		inputString = inputString.trim();
		String [] arraysString = null ;
		inputString = inputString.replaceAll(";", ",");
		arraysString = inputString.split(",");

		outputVector = MyArrayUtils.convertToDoublePrimitive( 
				Arrays.asList(arraysString).stream()
										   .filter(s -> NumberUtils.isCreatable(s))
										   .map(s -> Double.valueOf(s.trim()))
										   .collect(Collectors.toList())
			);
		return outputVector;
	}
	
	public static void main(String[] args) {
		
		if (args.length > 1) {
			String s1 = args[0];
			String s2 = args[1];
			System.out.println("s2: " + s2);
			if (s1.equals("-vec")) {
				double[] vec = Test4.getArrayOfBracketedDoubles(s2);
				for (double d : vec) {
					System.out.println("double: " + d);					
				}
			} else {
				System.out.println("Unrecognized option.");
			}
		} else {
			System.out.println("Termination. You must give 2 arguments!");
		}

	}

}
