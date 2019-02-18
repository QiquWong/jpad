package analyses;

import java.util.List;
import java.util.Map;

import org.inferred.freebuilder.FreeBuilder;

import aircraft.Aircraft;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.CostsEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PerformanceEnum;

@FreeBuilder
public interface IACAnalysisManager {

	String getId();
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	boolean isIterativeLoop();
	List<AnalysisTypeEnum> getAnalysisList();
	double getPositiveLimitLoadFactor();
	double getNegativeLimitLoadFactor();
	Map<ComponentEnum, List<MethodEnum>> getMethodsMapWeights();
	Map<ComponentEnum, MethodEnum> getMethodsMapBalance();
	List<PerformanceEnum> getTaskListPerfromance();
	List<ConditionEnum> getTaskListAerodynamicAndStability();
	List<ConditionEnum> getTaskListDynamicStability();
	Map<CostsEnum, MethodEnum> getTaskListCosts();
	boolean isPlotWeights();
	boolean isPlotBalance();
	boolean isPlotAerodynamicAndStability();
	boolean isPlotDynamicStability();
	boolean isPlotPerformance();
	boolean isPlotCosts();
	boolean isCreateCSVWeights();
	boolean isCreateCSVBalance();
	boolean isCreateCSVAerodynamicAndStability();
	boolean isCreateCSVDynamicStability();
	boolean isCreateCSVPerformance();
	boolean isCreateCSVCosts();
	
	class Builder extends IACAnalysisManager_Builder {
		public Builder () {
			
		}
	}
}
