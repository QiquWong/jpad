package calculators.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.jscience.physics.amount.Amount;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import aircraft.components.fuselage.FuselageCurvesSection;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

/**
 * In this class there are some methods which computes fuselage geometry parameters.
 * In the /JPADCore/src/aircraft/components/fuselage/Fuselage.java there are methods 
 * which computes the same paramaters.
 * 
 * TODO: delete the methods in this class and use that into Fusalege.java
 * 
 * @author Vincenzo Cusati
 */

public class FusNacGeometryCalc {

	/**
	 * @author Vincenzo Cusati 
	 * 
	 * @param fuselageDiameter
	 * @param noseFinenessRatio
	 * @return the length of the fuselage nose.
	 */
	public static double calculateFuselageNoseLength(double fuselageDiameter, double noseFinenessRatio){
		return	fuselageDiameter*noseFinenessRatio;
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageLength
	 * @param fuselageNoseLength
	 * @param fuselageTailLength
	 * @return the length of the fuselage cabin.
	 */

	public static double calculateFuselageCabinLength(double fuselageLength, double fuselageNoseLength, double fuselageTailLength){
		return	fuselageLength-fuselageNoseLength-fuselageTailLength;
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageDiameter
	 * @param tailFinenessRatio
	 * @return the length of the fuselage tailcone.
	 */

	public static double calculateFuselageTailLength(double fuselageDiameter, double tailFinenessRatio){
		return	fuselageDiameter*tailFinenessRatio;
	}

	/**
	 * @author Vincenzo Cusati 
	 * 
	 * @param fuselageDiameter
	 * @param fuselageNoseLength
	 * @return the estimation of the nose wet surface of the fuselage
	 * 
	 * (see  I. Kroo et al., Aircraft Design: Synthesis and Analysis, Stanford 
	 * University, January 2001.
	 * URL http://adg.stanford.edu/aa241/AircraftDesign.html)
	 */
	public static double calcFuselageNoseWetSurface(double fuselageDiameter, double fuselageNoseLength){
		double sWetNose = 0.75*Math.PI*fuselageDiameter*fuselageNoseLength;
		return sWetNose;
	}

	/**
	 * @author Vincenzo Cusati
	 * @param fuselageDiameter
	 * @param fuselageNoseLength
	 * @return the estimation of the cabin wet surface of the fuselage
	 */
	public static double calcFuselageCabinWetSurface(
			double fuselageDiameter, double fuselageCabinLength){
		double sWetCabin = Math.PI*fuselageDiameter*fuselageCabinLength;

		return sWetCabin;
	}

	/**
	 * @author Vincenzo Cusati
	 *
	 * @param fuselageDiameter
	 * @param fuselageTailLength
	 * @return the estimation of the tail wet surface of the fuselage
	 * 
	 * (see  I. Kroo et al., Aircraft Design: Synthesis and Analysis, Stanford 
	 * University, January 2001.
	 * URL http://adg.stanford.edu/aa241/AircraftDesign.html)
	 */
	public static double calcFuselageTailWetSurface(double fuselageDiameter, double fuselageTailLength){
		double sWetTail = 0.72*Math.PI*fuselageDiameter*fuselageTailLength;

		return sWetTail;
	}

//	public static double calcFuselageFrontalSurface(double fuselageDiameter){
//		return Math.PI*Math.pow(fuselageDiameter, 2)/4;
//	}

	/**
	 * @author Vincenzo Cusati
	 *
	 * @param fuselageDiameter
	 * @param fuselageLength
	 * @param fuselageNoseLength
	 * @param fuselageCabinLength
	 * @param fuselageTailLength
	 * @return the estimation of the fuselage wet surface
	 */
	public static double calcFuselageWetSurface(double fuselageDiameter, double fuselageLength,
			double fuselageNoseLength, double fuselageCabinLength, double fuselageTailLength){

		double sWetNose = calcFuselageNoseWetSurface(fuselageDiameter,fuselageNoseLength);
		double sWetTail = calcFuselageTailWetSurface(fuselageDiameter,fuselageTailLength);
		double sWetCabin= calcFuselageCabinWetSurface(fuselageDiameter, fuselageCabinLength);

		double fuselageWetSurface = sWetNose + sWetCabin + sWetTail;

		return fuselageWetSurface;

	}
	
	public static double calculateSfront(Amount<Length> fuselageDiameter){
		return Math.PI*Math.pow(fuselageDiameter.doubleValue(SI.METER), 2)/4;
	}
	
	public static List<PVector> getUniqueValuesXZUpperCurve(
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ
			) {
		
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( outlineXZUpperCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXZUpperCurveX.get(0).doubleValue(),
							(float)0.0,
							(float)outlineXZUpperCurveZ.get(0).doubleValue()
							)
					);

		for(int i = 1; i <= outlineXZUpperCurveX.size()-1; i++)
		{
			if ( !outlineXZUpperCurveX.get(i-1).equals( outlineXZUpperCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXZUpperCurveX.get(i).doubleValue(),
								(float)0.0,
								(float)outlineXZUpperCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}


	public static List<PVector> getUniqueValuesXZLowerCurve(
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ
			) {
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( outlineXZLowerCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXZLowerCurveX.get(0).doubleValue(),
							(float)0.0,
							(float)outlineXZLowerCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= outlineXZLowerCurveX.size()-1; i++)
		{
			if ( !outlineXZLowerCurveX.get(i-1).equals( outlineXZLowerCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXZLowerCurveX.get(i).doubleValue(),
								(float)0.0,
								(float)outlineXZLowerCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}
	
	public static List<PVector> getUniqueValuesXYSideRCurve(
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			)
	{
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( outlineXYSideRCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXYSideRCurveX.get(0).doubleValue(),
							(float)outlineXYSideRCurveY.get(0).doubleValue(),
							(float)0.0 // _outlineXYSideRCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= outlineXYSideRCurveX.size()-1; i++)
		{
			if ( ! outlineXYSideRCurveX.get(i-1).equals( outlineXYSideRCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXYSideRCurveX.get(i).doubleValue(),
								(float)outlineXYSideRCurveY.get(i).doubleValue(),
								(float)0.0 // _outlineXYSideRCurveZ.get(i).doubleValue()
								)
						);
			}
		}
		return p;
	}

	public static List<PVector> getUniqueValuesXYSideLCurve(
			List<Double> outlineXYSideLCurveX,
			List<Double> outlineXYSideLCurveY
			)
	{
		List<PVector> p  = new ArrayList<PVector>();
		// add the first element
		if ( outlineXYSideLCurveX.size() != 0 )
			p.add(
					new PVector(
							(float)outlineXYSideLCurveX.get(0).doubleValue(),
							(float)outlineXYSideLCurveY.get(0).doubleValue(),
							(float)0.0 // _outlineXYSideLCurveZ.get(0).doubleValue()
							)
					);
		for(int i = 1; i <= outlineXYSideLCurveX.size()-1; i++)
		{
			if ( !outlineXYSideLCurveX.get(i-1).equals( outlineXYSideLCurveX.get(i) ) )
			{
				p.add(
						new PVector(
								(float)outlineXYSideLCurveX.get(i).doubleValue(),
								(float)outlineXYSideLCurveY.get(i).doubleValue(),
								(float)0.0 // _outlineXYSideLCurveZ.get(0).doubleValue()
								)
						);
			}
		}
		return p;
	}
	
	public static Double getZOutlineXZUpperAtX(
			double x,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ
			) {
		// base vectors - upper
		// unique values
		double vxu[] = new double[getUniqueValuesXZUpperCurve(outlineXZUpperCurveX, outlineXZUpperCurveZ).size()];
		double vzu[] = new double[getUniqueValuesXZUpperCurve(outlineXZUpperCurveX, outlineXZUpperCurveZ).size()];
		for (int i = 0; i < vxu.length; i++)
		{
			vxu[i] = getUniqueValuesXZUpperCurve(outlineXZUpperCurveX, outlineXZUpperCurveZ).get(i).x;
			vzu[i] = getUniqueValuesXZUpperCurve(outlineXZUpperCurveX, outlineXZUpperCurveZ).get(i).z;
		}
		// interpolation - lower
		UnivariateInterpolator interpolatorUpper = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionUpper =
				interpolatorUpper.interpolate(vxu, vzu);

		// section z-coordinates at x
		Double z_F_u = 0.0;
		if (x < vxu[0]) {
			z_F_u = vzu[0];
		}
		if (x > vxu[vxu.length-1]) {
			z_F_u = vzu[vzu.length-1];
		}
		if ((x >= vxu[0]) && (x <= vxu[vxu.length-1])){
			z_F_u = myInterpolationFunctionUpper.value(x);
		}
		return z_F_u;
	}


	public static Double getZOutlineXZLowerAtX(
			double x,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ
			) {
		// base vectors - lower
		// unique values
		double vxl[] = new double[getUniqueValuesXZLowerCurve(outlineXZLowerCurveX, outlineXZLowerCurveZ).size()];
		double vzl[] = new double[getUniqueValuesXZLowerCurve(outlineXZLowerCurveX, outlineXZLowerCurveZ).size()];
		for (int i = 0; i < vxl.length; i++)
		{
			vxl[i] = getUniqueValuesXZLowerCurve(outlineXZLowerCurveX, outlineXZLowerCurveZ).get(i).x;
			vzl[i] = getUniqueValuesXZLowerCurve(outlineXZLowerCurveX, outlineXZLowerCurveZ).get(i).z;
		}
		// Interpolation - lower
		UnivariateInterpolator interpolatorLower = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionLower =
				interpolatorLower.interpolate(vxl, vzl);

		// section z-coordinates at x
		Double z_F_l = 0.0;
		if (x < vxl[0]) {
			z_F_l = vzl[0];
		}
		if (x > vxl[vxl.length-1]) {
			z_F_l = vzl[vzl.length-1];
		}
		if ((x >= vxl[0]) && (x <= vxl[vxl.length-1])){
			z_F_l = myInterpolationFunctionLower.value(x);
		}
		return z_F_l;
	}
	
	/** Return Camber z-coordinate at x-coordinate */
	public static Double getCamberZAtX(
			double x,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ
			) {
		double zUp = getZOutlineXZUpperAtX(x, outlineXZUpperCurveX, outlineXZUpperCurveZ);
		double zDown = getZOutlineXZLowerAtX(x, outlineXZLowerCurveX, outlineXZLowerCurveZ);
		return zUp/2 + zDown/2;
	}
	
	public static Double getCamberAngleAtXFuselage(
			double x,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ,
			Amount<Length> noseLength,
			Amount<Length> cabinLength
			) {
		if (x<= noseLength.doubleValue(SI.METER) || x<= cabinLength.doubleValue(SI.METER)) {
//			return Math.atan(
//					getCamberZAtX(
//							x,
//							outlineXZUpperCurveX,
//							outlineXZUpperCurveZ,
//							outlineXZLowerCurveX,
//							outlineXZLowerCurveZ
//							)
//					/x
//					);
			
			List<Double> camberList = outlineXZUpperCurveX.stream()
					.map(xc -> getCamberZAtX(xc, outlineXZUpperCurveX, outlineXZUpperCurveZ, outlineXZLowerCurveX, outlineXZLowerCurveZ))
					.collect(Collectors.toList());

			List<Double> camberAngleList = new ArrayList<>();
			camberAngleList.add((camberList.get(1)-camberList.get(0))/(outlineXZUpperCurveX.get(1)-outlineXZUpperCurveX.get(0)));
			for (int i = 1; i < camberList.size(); i++)
				camberAngleList.add((camberList.get(i)-camberList.get(i-1))/(outlineXZUpperCurveX.get(i)-outlineXZUpperCurveX.get(i-1)));

			return MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertToDoublePrimitive(outlineXZUpperCurveX),
					MyArrayUtils.convertToDoublePrimitive(camberAngleList),
					x
					);
		}
//		if (x>= cabinLength.doubleValue(SI.METER)) 
//			return Math.atan(
//					-getCamberZAtX(
//							x,
//							outlineXZUpperCurveX,
//							outlineXZUpperCurveZ,
//							outlineXZLowerCurveX,
//							outlineXZLowerCurveZ
//							)
//					/x
//					);
		return 0.;
	}
	
	public static Double getCamberAngleAtXNacelle(
			double x,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ
			) {

		List<Double> camberList = outlineXZUpperCurveX.stream()
				.map(xc -> getCamberZAtX(xc, outlineXZUpperCurveX, outlineXZUpperCurveZ, outlineXZLowerCurveX, outlineXZLowerCurveZ))
				.collect(Collectors.toList());
		
		List<Double> camberAngleList = new ArrayList<>();
		camberAngleList.add((camberList.get(1)-camberList.get(0))/(outlineXZUpperCurveX.get(1)-outlineXZUpperCurveX.get(0)));
		for (int i = 1; i < camberList.size(); i++)
			camberAngleList.add((camberList.get(i)-camberList.get(i-1))/(outlineXZUpperCurveX.get(i)-outlineXZUpperCurveX.get(i-1)));
		
		return MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(outlineXZUpperCurveX),
				MyArrayUtils.convertToDoublePrimitive(camberAngleList),
				x
				);
				
	}
	
	public static Double getYOutlineXYSideRAtX(
			double x,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {
		// base vectors - side (right)
		// unique values
		double vxs[] = new double[getUniqueValuesXYSideRCurve(outlineXYSideRCurveX, outlineXYSideRCurveY).size()];
		double vys[] = new double[getUniqueValuesXYSideRCurve(outlineXYSideRCurveX, outlineXYSideRCurveY).size()];
		for (int i = 0; i < vxs.length; i++)
		{
			vxs[i] = getUniqueValuesXYSideRCurve(outlineXYSideRCurveX, outlineXYSideRCurveY).get(i).x;
			vys[i] = getUniqueValuesXYSideRCurve(outlineXYSideRCurveX, outlineXYSideRCurveY).get(i).y;
		}
		// Interpolation - side (right)
		UnivariateInterpolator interpolatorSide = new LinearInterpolator(); // SplineInterpolator();
		UnivariateFunction myInterpolationFunctionSide =
				interpolatorSide.interpolate(vxs, vys);

		Double y_F_r = 0.0;
		if (x < vxs[0]) {
			y_F_r = vys[0];
		}
		if (x > vxs[vxs.length-1]) {
			y_F_r = vys[vxs.length-1];
		}
		if ((x >= vxs[0]) && (x <= vxs[vxs.length-1])){
			y_F_r = myInterpolationFunctionSide.value(x);
		}
		return y_F_r;
	}
	
	//  Return width at x-coordinate
	public static Double getWidthAtX(
			double x,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {
		return 2*getYOutlineXYSideRAtX(x, outlineXYSideRCurveX, outlineXYSideRCurveY);
	}
	
	/**
	 * @param fuselageLength in m
	 * @param equivalentDiameters in m
	 * @return the fuselage volume in m^3
	 */
	public static Amount<Volume> calculateFuselageVolume(
			Amount<Length> fuselageLength,
			Double[] equivalentDiameters
			) {
		
		return Amount.valueOf(
				MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.linspace(0, fuselageLength.doubleValue(SI.METER), equivalentDiameters.length),
						MyArrayUtils.convertToDoublePrimitive(
								Arrays.stream(equivalentDiameters).map(diam -> Math.PI*Math.pow(diam, 2)/4).collect(Collectors.toList())
								)
						),
				SI.CUBIC_METRE);
		
	}
	
	public static Amount<Length> calculateEquivalentDiameterAtX (
			Amount<Length> x,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {

		Double zUp = getZOutlineXZUpperAtX(x.doubleValue(SI.METER), outlineXZUpperCurveX, outlineXZUpperCurveZ);
		Double zDown = getZOutlineXZLowerAtX(x.doubleValue(SI.METER), outlineXZLowerCurveX, outlineXZLowerCurveZ);
		Double height = zUp - zDown;
		Double width = 2*getYOutlineXYSideRAtX(x.doubleValue(SI.METER), outlineXYSideRCurveX, outlineXYSideRCurveY);
		
		return Amount.valueOf(
				Math.sqrt(height*width),
				SI.METER
				);
		
	}
	
	public static Amount<Length> calculateMaxDiameter(
			List<Amount<Length>> xStations,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {
		
		return Amount.valueOf(
				xStations.stream()
				.mapToDouble(x -> getWidthAtX(x.doubleValue(SI.METER), outlineXYSideRCurveX, outlineXYSideRCurveY))
				.max()
				.getAsDouble(),
				SI.METER
				);
		
	}
	
}
