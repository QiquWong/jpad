package standaloneutils;

import java.util.ArrayList;
import java.util.List;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;

public class JPADWorkSession {

	private String _name = null;
	private Aircraft _aircraft = null;
	private List<OperatingConditions> _operatingPoints = new ArrayList<OperatingConditions>();
	private List<ACAnalysisManager> _analyses = new ArrayList<ACAnalysisManager>();
	
	public JPADWorkSession() {
	}
	public JPADWorkSession(Aircraft ac) {
		this._aircraft = ac;
	}
	public JPADWorkSession(Aircraft ac, OperatingConditions op) {
		this._aircraft = ac;
		this._operatingPoints.add(op);
	}

	public void initSession(Aircraft ac) {
		_aircraft = ac;
	}
	public void initSession(Aircraft ac, OperatingConditions op) {
		_aircraft = ac;
		_operatingPoints.add(op);
	}
	
	public void addOperatingPoint(OperatingConditions op)
	{
		_operatingPoints.add(op);
	}
	public void removeOperatingPoint(OperatingConditions op)
	{
		_operatingPoints.remove(op);
	}
	public void resetOperatingPoints()
	{
		_operatingPoints.clear();
	}
	public List<OperatingConditions> getOperatingPoints(){
		return _operatingPoints;
	}	
	public int getNOperatingPoints(){
		return _operatingPoints.size();
	}
	
	public void addAnalysis(ACAnalysisManager a)
	{
		_analyses.add(a);
	}
	public void removeAnalysis(ACAnalysisManager a)
	{
		_analyses.remove(a);
	}
	public void resetAnalyses()
	{
		_analyses.clear();
	}
	public List<ACAnalysisManager> getAnalises(){
		return _analyses;
	}	
	public int getNAnalysess(){
		return _analyses.size();
	}

	public void setName(String name) {
		this._name = name;
	}
	public String getName() {
		return this._name;
	}

	public void setAircraft(Aircraft ac) {
		this._aircraft = ac;
	}
	public Aircraft getAircraft() {
		return this._aircraft;
	}
		

}
