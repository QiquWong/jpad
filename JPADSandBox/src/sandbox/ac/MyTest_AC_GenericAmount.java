package sandbox.ac;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Mass;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import standaloneutils.MyUnits;


public class MyTest_AC_GenericAmount {

	public static void main(String[] args) {

		Amount<Mass> mass = Amount.valueOf(10, SI.KILOGRAM);
		Amount<Duration> duration = Amount.valueOf(10, SI.SECOND);
		
		
//		test(mass);
		test(duration);
	}

	public static void test(Amount amount){
//		double densityEng, densitySI = 800, densitySI2;
//		densityEng = MyConversionUtils.kgPerCubM2LbPerUsGal(densitySI);
//		densitySI2 = MyConversionUtils.lbPerUsGal2kgPerCubM(densityEng);
	
		Amount<VolumetricDensity> densitySI = Amount.valueOf(800, MyUnits.KILOGRAM_PER_CUBIC_METER),
				densityEng, densitySI2, densityNonSI;
		double doubAmount, price_lt = 0.20, price_USGal, price3,price4;
		
		densityEng = Amount.valueOf(densitySI.doubleValue(MyUnits.POUND_PER_USGALLON), MyUnits.POUND_PER_USGALLON);
		densitySI2 = Amount.valueOf(densityEng.doubleValue(MyUnits.KILOGRAM_PER_CUBIC_METER), MyUnits.KILOGRAM_PER_CUBIC_METER);
		densityNonSI = Amount.valueOf(densityEng.doubleValue(MyUnits.KILOGRAM_LITER), MyUnits.KILOGRAM_LITER);
		
		doubAmount = densityEng.doubleValue(MyUnits.POUND_PER_USGALLON);
		
		price_USGal = MyUnits.usDolPerLt2USDolPerUSGal(price_lt);
		
		if (amount.getUnit().equals(SI.KILOGRAM)) System.out.println("It's kilogram!!!");
		if (amount.getUnit().equals(SI.SECOND)) System.out.println("It's second!!!");
		
		double statMile = Amount.valueOf(1, NonSI.MILE).doubleValue(SI.KILOMETER); 
		
	}
}
