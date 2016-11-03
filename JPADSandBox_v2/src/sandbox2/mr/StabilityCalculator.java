package sandbox2.mr;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.ConditionEnum;
import standaloneutils.MyArrayUtils;

/*
 * ***********************************************************************************************************************
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
	
/*
 * ****************************************************************************************************************************************
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
	private List<Amount<Angle>> _alphasBody;
	
	private ConditionEnum _theCondition;
	
	//Wing -------------------------------------------
	//----------------------------------------------------------------
	
	//High lift devices -------------------------------------------
	//----------------------------------------------------------------
	
	//Fuselage -------------------------------------------
	//----------------------------------------------------------------
	
	//Horizontal Tail -------------------------------------------
	//----------------------------------------------------------------
	
	//Engines -------------------------------------------
	//----------------------------------------------------------------

	/*
	 * *****************************************************************************************************************************************
	* In this section the arrays are initialized. These initialization will be made also in final version                                    *
	* 												*																						 *
	*****************************************************************************************************************************************/
	//----------------------------------
	//INITIALIZE DATA           	   ||
	//----------------------------------
	
	// alpha body array
	double[] alphaBodyTemp = new double[this._numberOfAlphasBody];
	
}
