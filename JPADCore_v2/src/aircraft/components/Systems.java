package aircraft.components;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;


public class Systems implements ISystems {

	private String _id;
	
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	
	private Amount<Mass> _controlSurfaceMass;
	private Amount<Mass> _referenceMass;
	private Amount<Mass> _meanMass;
	private Amount<Mass> _overallMass;

	private Double[] _percentDifference;
	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class SystemsBuilder {

		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		private Amount<Mass> __referenceMass;
		private Map <MethodEnum, Amount<Mass>> __massMap = new TreeMap<MethodEnum, Amount<Mass>>();
		private List<MethodEnum> __methodsList = new ArrayList<MethodEnum>();
		private Map <AnalysisTypeEnum, List<MethodEnum>> __methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();

		public SystemsBuilder (String id) {
			this.__id = id;
			this.initializeDefaultVariables(AircraftEnum.ATR72);
		}
		
		public SystemsBuilder (String id, AircraftEnum aircraftName) {
			this.__id = id;
			this.initializeDefaultVariables(aircraftName);
		}

		public SystemsBuilder referenceMass (Amount<Mass> referenceMass) {
			this.__referenceMass = referenceMass;
			return this;
		}

		public Systems build() {
			return new Systems (this);
		}
		
		/************************************************************************
		 * method that recognize aircraft name and sets its 
		 * systems data.
		 * 
		 * @author Vittorio Trifari
		 */
		private void initializeDefaultVariables (AircraftEnum aircraftName) {

			switch(aircraftName) {

			case ATR72:
				__referenceMass = Amount.valueOf(2118, SI.KILOGRAM);
				break;

			case B747_100B:
				__referenceMass = Amount.valueOf(15949.0, SI.KILOGRAM);
				break;

			case AGILE_DC1:
				__referenceMass = Amount.valueOf(5087., SI.KILOGRAM);
				break;
			}
		}
	}
	
	private Systems (SystemsBuilder builder) { 
		
		this._id = builder.__id;
		this._referenceMass = builder.__referenceMass;
		
		this._methodsMap = builder.__methodsMap;
		this._massMap = builder.__massMap;
		this._methodsList = builder.__methodsList;
		
	}
	
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	public static Systems importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading systems data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		//REFERENCE MASS
		Amount<Mass> massReference = Amount
				.valueOf(
						Double.valueOf(
								reader.getXMLPropertyByPath(
										"//reference_masses/overall_reference_mass"
										)
								),
						SI.KILOGRAM
						);
		
		Systems aircraftSystems = new SystemsBuilder(id)
				.referenceMass(massReference)
				.build();
		
		return aircraftSystems;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tSystems\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "'\n")
				.append("\t·····································\n")
				.append("\tOverall reference mass: " + _referenceMass + "\n")
				.append("\t·····································\n")
				;
		
		return sb.toString();
		
	}
	
	@Override
	public void calculateMass(Aircraft aircraft, MethodEnum method) {

		// The user can estimate the overall systems mass (control surfaces systems, apu, de-icing ecc...)
		// or estimate each component mass separately
		calculateOverallMass(aircraft, method);
	}

	@Override
	public void calculateControlSurfaceMass(Aircraft aircraft, MethodEnum method) {

		switch (method) {
		case JENKINSON : {
			_methodsList.add(method);
			_controlSurfaceMass = Amount.valueOf(
					Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().times(0.04).getEstimatedValue(), 0.684),
					SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_controlSurfaceMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1982 : {
			_controlSurfaceMass = Amount.valueOf(
					0.4915*Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue(), 2/3), 
					SI.KILOGRAM);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_controlSurfaceMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		default : {} break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

	}
	
	/*****************************************************************************
	 * Evaluate mass of all the systems on board 
	 * (APU, air conditioning, hydraulic systems ...)
	 * 
	 * @param aircraft
	 * @param method
	 */
	@Override
	public void calculateOverallMass(Aircraft aircraft, MethodEnum method) {

		switch(method) {
		case TORENBEEK_2013 : {

			_overallMass = Amount.valueOf((
					250*aircraft.getFuselage().getFuselageCreator().getLenF().getEstimatedValue()*
					aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue() +
					150*aircraft.getFuselage().getFuselageCreator().getLenF().getEstimatedValue())/
					AtmosphereCalc.g0.getEstimatedValue(), 
					SI.KILOGRAM);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));

		} break;
		default : break;
		}	
		
		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_meanMass = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_referenceMass, 
				_massMap,
				_percentDifference,
				30.).getFilteredMean(), SI.KILOGRAM);
		
	}

	@Override
	public void calculateAPUMass() {
		// TODO
	}

	@Override
	public void calculateInstrumentationMass() {
		// TODO
	}

	@Override
	public void calculateElectricalMass() {
		// TODO
	}

	@Override
	public void calculateAntiIceAirCond() {
		// TODO
	}

	@Override
	public void calculateElectronicsMass() {
		// TODO
	}

	@Override
	public void calculateHydraulicPneumaticMass() {
		// TODO
	}
	
	@Override
	public void calculateAbsorbedPower() {
		// TODO
	}
	
	@Override
	public Amount<Mass> getControlSurfaceMass() {
		return _controlSurfaceMass;
	}

	@Override
	public void setControlSurfaceMass(Amount<Mass> csMass) {
		this._controlSurfaceMass = csMass;
	}

	@Override
	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	@Override
	public void setMassMap(Map<MethodEnum, Amount<Mass>> _massMap) {
		this._massMap = _massMap;
	}

	@Override
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	@Override
	public void setMethodsMap(
			Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap) {
		this._methodsMap = _methodsMap;
	}

	@Override
	public Double[] getPercentDifference() {
		return _percentDifference;
	}

	@Override
	public void setPercentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	@Override
	public Amount<Mass> getReferenceMass() {
		return _referenceMass;
	}

	@Override
	public void setReferenceMass(Amount<Mass> _massReference) {
		this._referenceMass = _massReference;
	}

	@Override
	public Amount<Mass> getMeanMass() {
		return _meanMass;
	}

	@Override
	public Amount<Mass> getOverallMass() {
		return _overallMass;
	}

	@Override
	public void setOverallMass(Amount<Mass> _mass) {
		this._overallMass = _mass;
	}

	@Override
	public String getId() {
		return _id;
	}
	
	@Override
	public void setId(String id) {
		this._id = id;
	}
	
	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	};
	
	@Override
	public void setXApexConstructionAxes (Amount<Length> xApexConstructionAxes) {
		this._xApexConstructionAxes = xApexConstructionAxes;
	};
	
	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	};
	
	@Override
	public void setYApexConstructionAxes (Amount<Length> yApexConstructionAxes) {
		this._yApexConstructionAxes = yApexConstructionAxes; 
	};
	
	@Override 
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	};
	
	@Override
	public void setZApexConstructionAxes (Amount<Length> zApexConstructionAxes) {
		this._zApexConstructionAxes = zApexConstructionAxes;
	};
}
