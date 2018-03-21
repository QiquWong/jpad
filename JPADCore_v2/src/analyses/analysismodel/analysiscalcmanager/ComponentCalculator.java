package analyses.analysismodel.analysiscalcmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aircraft.Aircraft;
import analyses.analysismodel.InnerCalculator;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;

public abstract class ComponentCalculator {

	protected Aircraft _theAircraft;
	protected Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	protected List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	protected InnerCalculator calculator;
	protected Double[] _percentDifference;
	
	public abstract void calculateAll(); 
	
	public abstract void initializeDependentData();
	
	public abstract void initializeInnerCalculators();
	
}
