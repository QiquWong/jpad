package aircraft.auxiliary.airfoil;

import javax.measure.unit.NonSI;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import calculators.aerodynamics.AirfoilCalc;
import standaloneutils.MyArrayUtils;

public class Airfoil {

/**
 * @author Vittorio Trifari, Manuela Ruocco
 * 
 * This class is a manager of the aircraft generic airfoil. It handles the AirfoilCreator, which
 * embodies all the geometric and aerodynamic characteristics, and call for all the needed static
 * methods contained in the calculator class AirfoilCalc.
 */

	private AirfoilCreator _theAirfoilCreator;

	/**
	 * This constructor creates an Airfoil object from the AirfoilCreator class
	 */
	public Airfoil(AirfoilCreator airfoilCreator) {
		
		this._theAirfoilCreator = airfoilCreator;
		
		//........................................................................
		if(airfoilCreator.getClCurve().isEmpty()) {

			_theAirfoilCreator.setAlphaForClCurve(
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									-30,
									30,
									50
									),
							NonSI.DEGREE_ANGLE
							)
					);

			AirfoilCalc.calculateClCurve(
					_theAirfoilCreator.getAlphaForClCurve(), 
					_theAirfoilCreator
					);
		}
		else
			AirfoilCalc.extractLiftCharacteristicsfromCurve(
					MyArrayUtils.convertListOfDoubleToDoubleArray(_theAirfoilCreator.getClCurve()),
					_theAirfoilCreator.getAlphaForClCurve(),
					_theAirfoilCreator);
		//........................................................................
		if(airfoilCreator.getCmCurve().isEmpty()) {

			_theAirfoilCreator.setClForCmCurve(
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.linspaceDouble(
									-3,
									3,
									50
									)
							)
					);

			AirfoilCalc.calculateCmvsClCurve(
					_theAirfoilCreator.getClForCmCurve(), 
					_theAirfoilCreator
					);
		}
		else
			AirfoilCalc.extractMomentCharacteristicsfromCurve(
					MyArrayUtils.convertListOfDoubleToDoubleArray(_theAirfoilCreator.getCmCurve()),
					_theAirfoilCreator.getClForCmCurve(),
					_theAirfoilCreator);
		//........................................................................
		if(airfoilCreator.getCdCurve().isEmpty()) {

			_theAirfoilCreator.setClForCdCurve(
					MyArrayUtils.convertDoubleArrayToListDouble(
							MyArrayUtils.linspaceDouble(
									-3,
									3,
									50
									)
							)
					);

			AirfoilCalc.calculateCdvsClCurve(
					_theAirfoilCreator.getClForCdCurve(), 
					_theAirfoilCreator
					);
		}
		//........................................................................
	}
	
	public AirfoilCreator getAirfoilCreator() {
		return _theAirfoilCreator;
	}

	public void setAirfoilCreator(AirfoilCreator _theAirfoilCreator) {
		this._theAirfoilCreator = _theAirfoilCreator;
	}

} // end-of-class
