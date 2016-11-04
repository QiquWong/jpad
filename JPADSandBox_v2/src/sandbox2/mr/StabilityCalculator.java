package sandbox2.mr;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ConditionEnum;
import standaloneutils.MyArrayUtils;

/** ***********************************************************************************************************************
 * THIS CLASS IS A PROTOTYPE OF THE NEW ACStabilityManager																*
 * After it will be necessary to change the name of the class and to read the data from other analysis, not from file.  *
 * Moreover the Test_Stability class will be the new test class of the executable										*
 * 																														*
 * @author Manuela Ruocco																								*
 ***********************************************************************************************************************/

public class StabilityCalculator {

	//----------------------------------
	// VARIABLE DECLARATION			   ||
	//----------------------------------

	/** ****************************************************************************************************************************************
	 * When this class will begin the ACStabilityManager, these values will be read from the Aircraft that has to pass to the builder pattern *
	 * 												*																						 *
	 *****************************************************************************************************************************************/

	//Operating Conditions -------------------------------------------
	//----------------------------------------------------------------

	private Amount<Length> _altitude;
	private Double _machCurrent;
	private Double _ReynoldsCurrent;

	private Amount<Angle> _alphaBodyInitial;
	private Amount<Angle> _alphaBodyFinal;
	private int _numberOfAlphasBody;
	private List<Amount<Angle>> _alphasBody;  // not from input

	private ConditionEnum _theCondition;

	//Wing -------------------------------------------
	//----------------------------------------------------------------

	private Amount<Length> _xApexWing;
	private Amount<Length> _yApexWing;
	private Amount<Length> _zApexWing;

	private Amount<Area> _wingSurface;
	private Double _aspectRatio;
	private Amount<Length> _wingSpan;  //not from input
	private Amount<Length> _wingSemiSpan;  // not from input
	private int _numberOfPointSemiSpanWise;
	private Double _adimentionalKinkStation;
	private int _numberOfGivenSections;

	private AirfoilFamilyEnum _meanAirfoilFamily;
	private Double _maxThicknessMeanAirfoil;

	// input distributions

	private List<Double> _yAdimensionalBreakPoints;
	private List<Amount<Length>> _yBreakPoints;
	private List<Double> _yAdimensionalDistribution;
	private List<Amount<Length>> _yDistribution;





	//High lift devices -------------------------------------------
	//----------------------------------------------------------------

	//Fuselage -------------------------------------------
	//----------------------------------------------------------------

	//Horizontal Tail -------------------------------------------
	//----------------------------------------------------------------

	//Engines -------------------------------------------
	//----------------------------------------------------------------

	/*****************************************************************************************************************************************
	 * In this section the arrays are initialized. These initialization will be made also in final version                                    *
	 * 												*																						 *
	 *****************************************************************************************************************************************/

	//----------------------------------
	//INITIALIZE DATA           	   ||
	//----------------------------------

	public void initializeData(){

		// dependent variables

		this._wingSpan = Amount.valueOf(
				Math.sqrt(
						this._wingSurface.doubleValue(SI.SQUARE_METRE) *
						(this._aspectRatio)),
				SI.METER
				);

		this._wingSemiSpan = Amount.valueOf(
				this._wingSpan.doubleValue(SI.METER)/2,
				SI.METER
				);


		//---------------
		// Arrays        |
		//---------------

		// alpha body array
		double[] alphaBodyTemp = new double[this._numberOfAlphasBody];
		alphaBodyTemp = MyArrayUtils.linspace(
				_alphaBodyInitial.doubleValue(NonSI.DEGREE_ANGLE),
				_alphaBodyFinal.doubleValue(NonSI.DEGREE_ANGLE),
				_numberOfAlphasBody);

		// fill the array of alpha Body

		for (int i=0; i<_numberOfAlphasBody; i++){
			this._alphasBody.add(
					Amount.valueOf(alphaBodyTemp[i], NonSI.DEGREE_ANGLE)
					);
		}

		// alpha wing array

		// downwash calculator

		// alpha tail array

		//---------------
		// Distributions |
		//---------------

		// y adimensional distribution
		double[] yAdimentionalDistributionTemp = new double[this._numberOfPointSemiSpanWise];
		yAdimentionalDistributionTemp = MyArrayUtils.linspace(
				0.0,
				1.0,
				this._numberOfPointSemiSpanWise);

		for (int i=0; i<_numberOfPointSemiSpanWise; i++){
			this._yAdimensionalDistribution.add(
					yAdimentionalDistributionTemp[i]
					);
		}

		// y dimensional distribution
		double[] yDistributionTemp = new double[this._numberOfPointSemiSpanWise];
		yDistributionTemp = MyArrayUtils.linspace(
				0.0,
				this._wingSemiSpan.doubleValue(SI.METER),
				this._numberOfPointSemiSpanWise);

		for (int i=0; i<_numberOfPointSemiSpanWise; i++){
			this._yDistribution.add(
					Amount.valueOf(yDistributionTemp[i], SI.METER)
					);
		}

	}

	/*****************************************************************************************************************************************
	 * When this class will begin the ACStabilityManager, this method will be simply eliminated            									 *
	 * 												*																						 *
	 *****************************************************************************************************************************************/
	public void printAllData(){
		System.out.println("LONGITUDINAL STATIC STABILITY TEST");
		System.out.println("------------------------------------------------------------------------------");

		System.out.println("\n\n*------------------------------------------*");
		System.out.println("*              Aircraft Data               *");
		System.out.println("*------------------------------------------*\n");

		System.out.println("Operating Conditions ----------");
		System.out.println("");

	}

}
