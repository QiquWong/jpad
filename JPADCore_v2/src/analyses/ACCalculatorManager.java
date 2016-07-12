package analyses;

import java.util.HashMap;
import java.util.Map;

import configuration.enumerations.AnalysisTypeEnum;

/**
 * Define a model for a generic calculator of the parameters relative to the whole aircraft.
 * Insert here properties/methods that you think should be common to all the aicraft calculators.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class ACCalculatorManager {

	protected static Map<AnalysisTypeEnum, Class> _theCalculatorsList = 
			new HashMap<AnalysisTypeEnum, Class>(); 
	
	public ACCalculatorManager() {
		
		// TODO: check this
		_theCalculatorsList.put(AnalysisTypeEnum.WEIGHTS, ACWeightsManager.class);
//		System.out.println("MyCalculator :: constructor");
		
	}
	
	public String get_name(){
		System.out.println("MyCalculator get_name function to override");
		return null;
	}
	
	public AnalysisTypeEnum get_type(){
		System.out.println("MyCalculator get_type function to override");
		return null;
	}
	
	public void calculate() {
		System.out.println("MyCalculator calculate function to override");
	}
	
}
