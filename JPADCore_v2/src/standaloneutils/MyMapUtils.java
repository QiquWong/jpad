package standaloneutils;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Table;

import standaloneutils.customdata.MyArray;

public class MyMapUtils {

	//	public static Double getAbsoluteMax(Map<Object, Double> map) {
	//		return Collections.max(map.values());
	//	}
	//
	//	public static Double getAbsoluteMax(Map<Object, MyArray> map) {
	//		return Collections.max(Collections.max(map.values()));
	//	}

	public static double getAbsoluteMax(Table<Object, Object, Object> table) {

		double max = Double.MIN_VALUE;

		// Loop over rows
		for (Entry<Object, Map<Object, Object>> m : table.rowMap().entrySet()) {

			Map<Object, Object> innerMap = m.getValue();

			// Loop over elements
			for (Entry<Object, Object> mm : innerMap.entrySet()){
				if(mm.getValue() instanceof MyArray){
					if (((MyArray) mm.getValue()).getMax() > max) 
						max = ((MyArray) mm.getValue()).getMax(); 
				}
			}
		}

		return max;
	}

	public static double getAbsoluteMin(Table<Object, Object, Object> table) {

		double min = Double.MAX_VALUE;

		// Loop over rows
		for (Entry<Object, Map<Object, Object>> m : table.rowMap().entrySet()) {

			Map<Object, Object> innerMap = m.getValue();

			// Loop over elements
			for (Entry<Object, Object> mm : innerMap.entrySet()){
				if(mm.getValue() instanceof MyArray){
					if (((MyArray) mm.getValue()).getMin() < min) 
						min = ((MyArray) mm.getValue()).getMin(); 
				}
			}
		}

		return min;
	}

	public static double getAbsoluteMax(Map<Object, Object> innerMap) {

		double max = Double.MIN_VALUE;

		// Loop over elements
		for (Entry<Object, Object> mm : innerMap.entrySet()){
			if(mm.getValue() instanceof MyArray){
				if (((MyArray) mm.getValue()).getMax() > max) 
					max = ((MyArray) mm.getValue()).getMax(); 
			} else {
				if (MyArrayUtils.getMax((Double[]) mm.getValue()) > max) 
					max = MyArrayUtils.getMax((Double[]) mm.getValue());
			}
		}
		return max;
	}

	public static double getAbsoluteMin(Map<Object, Object> innerMap) {

		double min = Double.MAX_VALUE;

		for (Entry<Object, Object> mm : innerMap.entrySet()){
			if(mm.getValue() instanceof MyArray){
				if (((MyArray) mm.getValue()).getMin() < min) 
					min = ((MyArray) mm.getValue()).getMin(); 
			} else {
				if (MyArrayUtils.getMin((Double[]) mm.getValue()) < min) 
					min = MyArrayUtils.getMin((Double[]) mm.getValue());
			}
		}

		return min;
	}

}





