package aircraft.components.nacelles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.NacelleMountingPositionEnum;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * Manage all the nacelles of the aircraft
 * and the calculations associated with them
 * 
 * @author Lorenzo Attanasio
 *
 */
public class Nacelles {

	private String _id;
	private int _nacellesNumber;
	private List<NacelleCreator> _nacellesList;
	private Map<NacelleCreator, Engine> _nacelleEngineMap;
	
	private Amount<Mass> _totalMass;
	private List<Amount<Mass>> _massList;
	private CenterOfGravity _totalCG;
	private List<CenterOfGravity> _cgList;
	
	private Amount<Length> _distanceBetweenInboardNacellesY, _distanceBetweenOutboardNacellesY;
	private Double _percentTotalDifference;
	private Double _cD0Total, _cD0Parasite, _cD0Base;
	private Amount<Mass> _massReference;
	
	private Amount<Area> _surfaceWetted;
	private Double _kExcr = 0.0;
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class NacellesBuilder {
		
		// required parameters
		private String __id;
		private Integer __nacellesNumber;
		public List<NacelleCreator> __nacellesList = new ArrayList<NacelleCreator>();
		
		// optional parameters ... defaults
		// ...	
		private Map<NacelleCreator, Engine> __nacelleEngineMap = new HashMap<NacelleCreator, Engine>();
		private List<Amount<Mass>> __massList = new ArrayList<Amount<Mass>>();
		private List<CenterOfGravity> __cgList = new ArrayList<CenterOfGravity>();
		
		public NacellesBuilder (String id, List<NacelleCreator> nacellesList) {
			this.__id = id;
			this.__nacellesList = nacellesList;
			this.__nacellesNumber = nacellesList.size();
		}
		
		public NacellesBuilder (String id, AircraftEnum aircraftName) {
			this.__id = id;
			initializeDefaultVariables(aircraftName);
		}
		
		@SuppressWarnings("incomplete-switch")
		private void initializeDefaultVariables (AircraftEnum aircraftName) {
			switch(aircraftName) {
			
			case ATR72:
				__nacellesNumber = 2;
				for (int i=0; i<__nacellesNumber; i++)
					__nacellesList.add(
							new NacelleCreator
							.NacelleCreatorBuilder("ATR-72 Nacelle", aircraftName)
							.build()
							);
				__nacellesList.get(0).setXApexConstructionAxes(Amount.valueOf(8.56902, SI.METER));
				__nacellesList.get(0).setYApexConstructionAxes(Amount.valueOf(4.5738, SI.METER));
				__nacellesList.get(0).setZApexConstructionAxes(Amount.valueOf(1.02895, SI.METER));
				__nacellesList.get(0).setMountingPosition(NacelleMountingPositionEnum.WING);

				__nacellesList.get(1).setXApexConstructionAxes(Amount.valueOf(8.56902, SI.METER));
				__nacellesList.get(1).setYApexConstructionAxes(Amount.valueOf(-4.5738, SI.METER));
				__nacellesList.get(1).setZApexConstructionAxes(Amount.valueOf(1.02895, SI.METER));
				__nacellesList.get(1).setMountingPosition(NacelleMountingPositionEnum.WING);
				break;

			case B747_100B:
				__nacellesNumber = 4;
				for (int i=0; i<__nacellesNumber; i++)
					__nacellesList.add(
							new NacelleCreator
								.NacelleCreatorBuilder("B747-100B Nacelle", aircraftName)
									.build()
							);
				__nacellesList.get(0).setXApexConstructionAxes(Amount.valueOf(23.770, SI.METER));
				__nacellesList.get(0).setYApexConstructionAxes(Amount.valueOf(11.820, SI.METER));
				__nacellesList.get(0).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__nacellesList.get(0).setMountingPosition(NacelleMountingPositionEnum.WING);
				
				__nacellesList.get(1).setXApexConstructionAxes(Amount.valueOf(31.693, SI.METER));
				__nacellesList.get(1).setYApexConstructionAxes(Amount.valueOf(21.951, SI.METER));
				__nacellesList.get(1).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__nacellesList.get(1).setMountingPosition(NacelleMountingPositionEnum.WING);
				
				__nacellesList.get(2).setXApexConstructionAxes(Amount.valueOf(23.770, SI.METER));
				__nacellesList.get(2).setYApexConstructionAxes(Amount.valueOf(-11.820, SI.METER));
				__nacellesList.get(2).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__nacellesList.get(2).setMountingPosition(NacelleMountingPositionEnum.WING);
				
				__nacellesList.get(3).setXApexConstructionAxes(Amount.valueOf(31.693, SI.METER));
				__nacellesList.get(3).setYApexConstructionAxes(Amount.valueOf(-21.951, SI.METER));
				__nacellesList.get(3).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__nacellesList.get(3).setMountingPosition(NacelleMountingPositionEnum.WING);
				
				break;
				
			case AGILE_DC1:
				__nacellesNumber = 2;
				for (int i=0; i<__nacellesNumber; i++)
					__nacellesList.add(
							new NacelleCreator
								.NacelleCreatorBuilder("AGILE-DC1 Nacelle", aircraftName)
									.build()
							);
				__nacellesList.get(0).setXApexConstructionAxes(Amount.valueOf(11.84, SI.METER));
				__nacellesList.get(0).setYApexConstructionAxes(Amount.valueOf(4.91, SI.METER));
				__nacellesList.get(0).setZApexConstructionAxes(Amount.valueOf(-2.45, SI.METER));
				__nacellesList.get(0).setMountingPosition(NacelleMountingPositionEnum.WING);
				
				__nacellesList.get(1).setXApexConstructionAxes(Amount.valueOf(11.84, SI.METER));
				__nacellesList.get(1).setYApexConstructionAxes(Amount.valueOf(-4.91, SI.METER));
				__nacellesList.get(1).setZApexConstructionAxes(Amount.valueOf(-2.45, SI.METER));
				__nacellesList.get(1).setMountingPosition(NacelleMountingPositionEnum.WING);
				
				break;
			}
		}
		
		public Nacelles build() {
			return new Nacelles(this);
		}
	}
	
	private Nacelles(NacellesBuilder builder) {
		
		this._id = builder.__id;
		this._nacellesNumber = builder.__nacellesNumber;
		this._nacellesList = builder.__nacellesList;
		this._nacelleEngineMap = builder.__nacelleEngineMap;
		this._massList = builder.__massList;
		this._cgList = builder.__cgList;
		
		this._distanceBetweenInboardNacellesY = _nacellesList.get(0).getYApexConstructionAxes().times(2);
		if (_nacellesNumber>2)
			this._distanceBetweenOutboardNacellesY = _nacellesList.get(2).getYApexConstructionAxes().times(2);
		
		populateEnginesMap();
		calculateSurfaceWetted();
		
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	private void populateEnginesMap() {
		for(int i=0; i < _nacellesNumber; i++) {
			_nacelleEngineMap.put(_nacellesList.get(i), _nacellesList.get(i).getTheEngine());
		}
	}

	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();
		
		sb.append("\t-------------------------------------\n")
		  .append("\tNacelles\n")
		  .append("\t-------------------------------------\n")
		  .append("\tId: '" + _id + "'\n")
		  .append("\tNumber of nacelles: " + _nacellesNumber + "\n")
		  ;
		for(int i=0; i<this._nacellesList.size(); i++)
			sb.append("\t-------------------------------------\n")
			  .append("\tNacelle n° " + (i+1) + "\n")
			  .append("\t-------------------------------------\n")
			  .append(this._nacellesList.get(i).toString())
			  ;
		
		
		return sb.toString();
		
	}
	
	
	public void initializeWeights(Aircraft theAircraft) {
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).initializeWeights(theAircraft);
		}
	}

	
	public void initializeBalance() {
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).initializeBalance();
		}
	}
	
	
	public void calculateSurfaceWetted() {
		_surfaceWetted = Amount.valueOf(0., SI.SQUARE_METRE);
		for(int i=0; i < _nacellesNumber; i++) {
			_surfaceWetted = _surfaceWetted.plus(_nacellesList.get(i).getSurfaceWetted()); 
		}
	}

	/**
	 * @author Lorenzo Attanasio
	 */
	
	public void calculateMass(Aircraft theAircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {

		_totalMass = Amount.valueOf(0., SI.KILOGRAM);
		_massReference = theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass();
		initializeWeights(theAircraft);

		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).getWeights().setMassReference(
					theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass()
						.divide(_nacellesNumber));
			_nacellesList.get(i).getWeights().calculateAll();
			if(!methodsMapWeights.get(ComponentEnum.NACELLE).equals(MethodEnum.AVERAGE))
				_nacellesList.get(i).getWeights().setMassEstimated(
						_nacellesList.get(i).getWeights().getMassMap().get(
								methodsMapWeights.get(ComponentEnum.NACELLE)
								)
						);
			_massList.add(_nacellesList.get(i).getWeights().getMassEstimated());
			_totalMass = _totalMass.plus(_nacellesList.get(i).getWeights().getMassEstimated());
			_massReference = _massReference.plus(theAircraft.getTheAnalysisManager().getTheWeights().getNacelleReferenceMass());
		}
		
		_percentTotalDifference = _totalMass.
				minus(_massReference).
				divide(_massReference).
				getEstimatedValue()*100.;
	}
	
	
	public CenterOfGravity calculateCG() {

		_totalCG = new CenterOfGravity();
		initializeBalance();
		
		for(int i=0; i < _nacellesNumber; i++) {
			_nacellesList.get(i).initializeBalance();
			_nacellesList.get(i).getBalance().calculateAll();
			_cgList.add(_nacellesList.get(i).getBalance().getCG());
			_totalCG = _totalCG.plus(_nacellesList.get(i).getBalance().getCG()
					.times(_nacellesList.get(i).getWeights().getMassEstimated().doubleValue(SI.KILOGRAM)));
		}

		_totalCG = _totalCG.divide(_totalMass.doubleValue(SI.KILOGRAM));
		return _totalCG;
	}

	
	public String getId() {
		return _id;
	}

	
	public void setId(String _id) {
		this._id = _id;
	}

	
	public int getNacellesNumber() {
		return _nacellesNumber;
	}

	
	public void setNacellesNumber(int _nacellesNumber) {
		this._nacellesNumber = _nacellesNumber;
	}

	
	public List<NacelleCreator> getNacellesList() {
		return _nacellesList;
	}

	
	public void setNacellesList(List<NacelleCreator> _nacellesList) {
		this._nacellesList = _nacellesList;
	}

	
	public Map<NacelleCreator, Engine> getNacelleEngineMap() {
		return _nacelleEngineMap;
	}

	
	public void setNacelleEngineMap(Map<NacelleCreator, Engine> _nacelleEngineMap) {
		this._nacelleEngineMap = _nacelleEngineMap;
	}

	
	public Amount<Mass> getTotalMass() {
		return _totalMass;
	}

	
	public void setTotalMass(Amount<Mass> _totalMass) {
		this._totalMass = _totalMass;
	}

	
	public List<Amount<Mass>> getMassList() {
		return _massList;
	}

	
	public void setMassList(List<Amount<Mass>> _massList) {
		this._massList = _massList;
	}

	
	public CenterOfGravity getTotalCG() {
		return _totalCG;
	}

	
	public void setTotalCG(CenterOfGravity _totalCG) {
		this._totalCG = _totalCG;
	}

	
	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}

	
	public void setCGList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	
	public Double getPercentTotalDifference() {
		return _percentTotalDifference;
	}

	
	public Double getCD0Total() {
		return _cD0Total;
	}

	
	public Double getCD0Parasite() {
		return _cD0Parasite;
	}

	
	public Double getCD0Base() {
		return _cD0Base;
	}

	
	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	
	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	
	public Amount<Area> getSurfaceWetted() {
		return _surfaceWetted;
	}

	
	public void setSurfaceWetted(Amount<Area> _surfaceWetted) {
		this._surfaceWetted = _surfaceWetted;
	}

	public Amount<Length> getDistanceBetweenInboardNacellesY() {
		return _distanceBetweenInboardNacellesY;
	}

	public void setDistanceBetweenInboardNacellesY(Amount<Length> _distanceBetweenInboardNacellesY) {
		this._distanceBetweenInboardNacellesY = _distanceBetweenInboardNacellesY;
	}

	public Amount<Length> getDistanceBetweenOutboardNacellesY() {
		return _distanceBetweenOutboardNacellesY;
	}

	public void setDistanceBetweenOutboardNacellesY(Amount<Length> _distanceBetweenOutboardNacellesY) {
		this._distanceBetweenOutboardNacellesY = _distanceBetweenOutboardNacellesY;
	}

	/**
	 * @return the _kExcr
	 */
	public Double getKExcr() {
		return _kExcr;
	}

	/**
	 * @param _kExcr the _kExcr to set
	 */
	public void setKExcr(Double _kExcr) {
		this._kExcr = _kExcr;
	}
}