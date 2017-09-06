package standaloneutils;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.Volume;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.economics.money.Currency;
import org.jscience.physics.amount.Amount;

import com.sun.javafx.print.Units;

/** 
 * Define custom units of measurement not included in SI or NonSI libraries
 * 
 * @author Lorenzo Attanasio, Agostino De Marco
 *
 */
@SuppressWarnings("rawtypes")
public class MyUnits {

	private static Amount<Length> _lengthSIUnit = Amount.valueOf(1, SI.METER);
	private static Amount<Mass> _massSIUnit = Amount.valueOf(1, SI.KILOGRAM);
	private static Amount<Volume> _volSIUnit = Amount.valueOf(1, SI.CUBIC_METRE);
	private static double _densConvSI2Eng = _massSIUnit.doubleValue(NonSI.POUND)/
											_volSIUnit.doubleValue(NonSI.GALLON_LIQUID_US);
	
	public static final Unit<Area> FOOT2 = SI.SQUARE_METRE.times(0.09290304);
	public static final Unit<Volume> FOOT3 = SI.CUBIC_METRE.times(0.0283168);
	public static final Unit<Velocity> FOOT_PER_SECOND = SI.METERS_PER_SECOND.times(0.3048);
	public static final Unit<Pressure> LB_FT2 = SI.PASCAL.times(47.8802589804);
	public static final Unit<VolumetricDensity> KILOGRAM_LITER = VolumetricDensity.UNIT.times(0.001);
	public static final Unit<Frequency> RPM = SI.HERTZ.divide(60);
	public static final Unit<VolumetricDensity> KILOGRAM_PER_CUBIC_METER = VolumetricDensity.UNIT;
	public static final Unit<VolumetricDensity> POUND_PER_USGALLON = KILOGRAM_PER_CUBIC_METER.
																		divide(_densConvSI2Eng);
	public static final Unit<Dimensionless> NON_DIMENSIONAL = Unit.ONE;
	public static final Unit<Acceleration> FOOT_PER_SQUARE_MINUTE = SI.METERS_PER_SQUARE_SECOND.times(0.000084666666666667);
	public static final Unit<Velocity> FOOT_PER_MINUTE = SI.METERS_PER_SECOND.times(0.00508);
	public static final Unit DEG_PER_SECOND = NonSI.DEGREE_ANGLE.divide(SI.SECOND);
	
	// used in mission simulations
	
	public static final Unit ONE_PER_SECOND = Unit.ONE.divide(SI.SECOND);
	public static final Unit RADIAN_PER_SECOND = Unit.ONE.divide(SI.SECOND);
	public static final Unit SECOND_SQUARED = SI.SECOND.times(SI.SECOND);
	public static final Unit ONE_PER_SECOND_SQUARED = Unit.ONE.divide(MyUnits.SECOND_SQUARED);
	
	public static final Unit KILOGRAM_PER_METER = SI.KILOGRAM.divide(SI.METER);
	public static final Unit SLUG = SI.KILOGRAM.times(14.593903); // https://en.wikipedia.org/wiki/Slug_(mass)
	public static final Unit SLUG_PER_FT = MyUnits.SLUG.divide(NonSI.FOOT);
	public static final Unit METER_PER_KILOGRAM = SI.METER.divide(SI.KILOGRAM);
	public static final Unit FT_PER_SLUG = NonSI.FOOT.divide(MyUnits.SLUG);
	public static final Unit RAD_METER_PER_KILOGRAM = SI.RADIAN.times(MyUnits.METER_PER_KILOGRAM);
	public static final Unit DEG_METER_PER_KILOGRAM = NonSI.DEGREE_ANGLE.times(MyUnits.METER_PER_KILOGRAM);
	public static final Unit RAD_FT_PER_SLUG = SI.RADIAN.times(MyUnits.FT_PER_SLUG);
	public static final Unit DEG_FT_PER_SLUG = NonSI.DEGREE_ANGLE.times(MyUnits.FT_PER_SLUG);

	public static final Unit KILOGRAM_METER = SI.KILOGRAM.times(SI.METER);
	public static final Unit KILOGRAM_METER_PER_SECOND = SI.KILOGRAM.times(SI.METER).divide(SI.SECOND);

	public static final Unit KILOGRAM_PER_SECOND = SI.KILOGRAM.divide(SI.SECOND);
	public static final Unit KILOGRAM_PER_SECOND_PER_NEWTON = SI.KILOGRAM.divide(SI.SECOND).divide(SI.NEWTON);
	public static final Unit SLUG_PER_SECOND_PER_POUND = MyUnits.SLUG.divide(SI.SECOND).divide(NonSI.POUND_FORCE);
	
	public static final Unit HOURS_PER_YEAR = NonSI.HOUR.divide(NonSI.YEAR);
	
	public static final Unit USD_PER_HOUR = Currency.USD.divide(NonSI.HOUR);
	public static final Unit USD_PER_NAUTICAL_MILE = Currency.USD.divide(NonSI.NAUTICAL_MILE);
	public static final Unit USD_PER_FLIGHT = Currency.USD.divide(Unit.ONE);
	public static final Unit USD_PER_TON = Currency.USD.divide(NonSI.METRIC_TON);
	public static final Unit USD_PER_KM_SQRT_TON = Currency.USD.divide(SI.KILOGRAM.times(NonSI.METRIC_TON.root(2)));
	public static final Unit USD_PER_GALLON = Currency.USD.divide(NonSI.GALLON_LIQUID_US);
	public static final Unit BARREL = NonSI.LITER.times(158.98); 
	public static final Unit USD_PER_BARREL = Currency.USD.divide(MyUnits.BARREL);
	public static final Unit CENTS_PER_GALLON = (Currency.USD.divide(100)).divide(NonSI.GALLON_LIQUID_US);
	public static final Unit G_PER_KN = SI.GRAM.divide(SI.NEWTON.divide(1000.0));
	
	
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
	 * Method that converts a price per gallon (US$/USGal) to a price cubic meter (US$/m^3) 
	 * 
	 * @param usDolPerGal Price per gallon (US$/gal)
	 * @return Price per cubic meter (US$/m^3)
	 * @author AC
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> usDolPerUSGal2USDolPerBarrel(Amount<?> usDolPerCubM){
		double conversionConst;
		conversionConst = 1/_volSIUnit.doubleValue(SI.CUBIC_METRE);
				
		return Amount.valueOf(
				usDolPerCubM.getEstimatedValue()/conversionConst,
				MyUnits.USD_PER_BARREL
				);
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
