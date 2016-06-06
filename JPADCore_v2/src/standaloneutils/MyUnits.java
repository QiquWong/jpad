package standaloneutils;

import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

/** 
 * Define custom units of measurement not included in SI or NonSI libraries
 * 
 * @author Lorenzo Attanasio
 *
 */
public class MyUnits {

	private static Amount<Mass> _massSIUnit = Amount.valueOf(1, SI.KILOGRAM);
	private static Amount<Volume> _volSIUnit = Amount.valueOf(1, SI.CUBIC_METRE);
	private static double _densConvSI2Eng = _massSIUnit.doubleValue(NonSI.POUND)/
											_volSIUnit.doubleValue(NonSI.GALLON_LIQUID_US);
	
	public static final Unit<Area> FOOT2 = SI.SQUARE_METRE.times(0.09290304);
	public static final Unit<Velocity> FOOT_PER_SECOND = SI.METERS_PER_SECOND.times(0.3048);
	public static final Unit<Pressure> LB_FT2 = SI.PASCAL.times(47.8802589804);
	public static final Unit<VolumetricDensity> KILOGRAM_LITER = VolumetricDensity.UNIT.times(0.001);
	public static final Unit<Frequency> RPM = SI.HERTZ.divide(60);
	public static final Unit<VolumetricDensity> KILOGRAM_PER_CUBIC_METER = VolumetricDensity.UNIT;
	public static final Unit<VolumetricDensity> POUND_PER_USGALLON = KILOGRAM_PER_CUBIC_METER.
																		divide(_densConvSI2Eng);
	public static final Unit<Dimensionless> NON_DIMENSIONAL = Unit.ONE;

	/**
	 * Method that converts a price per kilogram (US$/Kg) to a price per pound (US$/lb)
	 * 
	 * @param usDolPerKg Price per kilogram (US$/Kg)
	 * @return Price per pound (US$/lb)
	 * @author AC
	 */
	public static double usDolPerKg2USDolPerLb(double usDolPerKg){
		double conversionConst;
		conversionConst = 1/_massSIUnit.doubleValue(NonSI.POUND);
		
		return usDolPerKg/conversionConst;
	}

	/**
	 * Method that converts a price per cubic meter (US$/m^3) to a price per liter (US$/lt)
	 * 
	 * @param usDolPerCubM Price per cubic meter (US$/m^3)
	 * @return Price per liter (US$/lt)
	 * @author AC
	 */
	public static double usDolPerCubM2USDolPerLt(double usDolPerCubM){
		double conversionConst;
		conversionConst = 1/_volSIUnit.doubleValue(NonSI.LITER);
				
		return usDolPerCubM/conversionConst;
	}
	
	/**
	 * Method that converts a price per cubic meter (US$/m^3) to a price per US gallon (US$/USGal)
	 * 
	 * @param usDolPerCubM Price per cubic meter (US$/m^3)
	 * @return Price per liter (US$/USGal)
	 * @author AC
	 */
	public static double usDolPerCubM2USDolPerUSGal(double usDolPerCubM){
		double conversionConst;
		conversionConst = 1/_volSIUnit.doubleValue(NonSI.GALLON_LIQUID_US);
				
		return usDolPerCubM/conversionConst;
	}

	/**
	 * Method that converts a price per liter (US$/lt) to a price per US gallon (US$/USGal)
	 * 
	 * @param UsDolPerCubM Price per cubic meter (US$/lt)
	 * @return Price per liter (US$/USGal)
	 * @author AC
	 */
	public static double usDolPerLt2USDolPerUSGal(double usDolPerLt){
		double conversionConst;
		conversionConst = usDolPerCubM2USDolPerUSGal(1)/
						  usDolPerCubM2USDolPerLt(1);
				
		return usDolPerLt/conversionConst;
	}
	
	/**
	 * Method that converts a price per cubic meter (US$/m^3) to a price per kilograms (US$/kg)
	 * 
	 * @param usDolPerCubM Price per cubic meter (US$/m^3)
	 * @param materialDensityKgPerCubM Material density in (kg/m^3)
	 * @return Price per liter (US$/kg)
	 * @author AC
	 */
	public static double usDolPerCubM2USDolPerkg(double usDolPerCubM, double materialDensityKgPerCubM){
		//TODO: Create an if that accept all density units known in another method,
							   //      in this method, the density must be expressed in kg/m^3
				
		return usDolPerCubM/materialDensityKgPerCubM;
	}	
	
	public static double usDolPerCubM2USDolPerkg(double usDolPerCubM,
											Amount<VolumetricDensity> materialDensityKgPerCubM){
		
		return usDolPerCubM2USDolPerkg(usDolPerCubM,
				materialDensityKgPerCubM.doubleValue(KILOGRAM_PER_CUBIC_METER));
	}

	
}
