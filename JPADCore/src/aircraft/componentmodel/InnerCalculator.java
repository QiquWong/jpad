package aircraft.componentmodel;

import java.util.Map;
import java.util.TreeMap;

import configuration.enumerations.MethodEnum;

/**
 * An inner calculator is an inner class of a component calculator;
 * e.g.: the lifting surface has an LSAerodynamicsManager class.
 * 
 * This class has an inner class for each parameter that has to be estimated.
 * This means that the CMalpha is not a simple double property of the
 * LSAerodynamicsManager class; it is instead an object of CalcCMalpha type,
 * where CalcCMalpha is the inner calculator which contains all the methods
 * implemented to evaluate the CMalpha value.
 * 
 * If the user developer wants to estimate the CMalpha value, he/she can do so
 * directly from the object. Supposing he/she has an Aircraft aircraft object,
 * the CMalpha can be evaluated as:
 * 
 * aircraft.get_wing().getAerodynamics().getCalcCMalpha().integral3DwithTwist();
 * 
 * Otherwise, a call like the following
 * 
 * aircraft.get_wing().getAerodynamics().getCalcCMalpha().allMethods();
 * 
 * will execute all the available methods relative to that quantity (e.g. CMalpha)
 * and will then store all the results in a _methodsMap which will have a MethodEnum key
 * (the name of the method) and a value. A specific value can then be retrieved as:
 * 
 * aircraft.get_wing().getAerodynamics().getCalcCMalpha().get_methodsMap().get(MethodEnum.XXX);
 * 
 * 
 * @author Lorenzo Attanasio
 *
 */
public abstract class InnerCalculator {

	protected Map<MethodEnum, Double> _methodsMap = new TreeMap<MethodEnum, Double>();

	public abstract void allMethods();

	public Map<MethodEnum, Double> get_methodsMap() {
		return _methodsMap;
	}

}
