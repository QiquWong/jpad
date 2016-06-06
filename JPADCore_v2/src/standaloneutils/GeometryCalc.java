package standaloneutils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.MathArrays;

public class GeometryCalc {

	public static double getXLEAtYActual(double[] yStationActual, double[] xLEvsYActual, Double y){
		if (y >= 0) 
			return MyMathUtils.getInterpolatedValue1DLinear(yStationActual, xLEvsYActual, y);
		else {
			double[] temp = MathArrays.scale(-1., yStationActual);
			double[] temp2 = xLEvsYActual.clone();
			ArrayUtils.reverse(temp);
			ArrayUtils.reverse(temp2);
			return MyMathUtils.getInterpolatedValue1DLinear(temp, temp2, y);
		}
	}

	public static double getChordAtYActual(double[] yStationActual, double[] chordsVsYActual, Double y) {
		if (y >= 0) 
			return MyMathUtils.getInterpolatedValue1DLinear(yStationActual, chordsVsYActual, y);
		else {
			double[] temp = MathArrays.scale(-1., yStationActual);
			double[] temp2 = chordsVsYActual.clone();
			ArrayUtils.reverse(temp);
			ArrayUtils.reverse(temp2);
			return MyMathUtils.getInterpolatedValue1DLinear(temp, temp2, y);
		}
	}

}
