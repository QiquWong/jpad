package aircraft.components.powerplant;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * The Propulsion System includes engines, engine exhaust, 
 * reverser, starting, controls, lubricating, and fuel systems.
 * The output of this class is the entire propulsion system (that
 * is, all engines are included) 
 */

public class PowerPlant implements IPowerPlant {

	public  String _id ;
	private Integer _engineNumber;
	public List<Engine> _engineList;
	public EngineTypeEnum _engineType;
	public EngineMountingPositionEnum _mountingPosition;
	
	private Amount<Force> _t0Total;
	private Amount<Power> _p0Total;
	
	private List<CenterOfGravity> _cgList;
	
	private Amount<Mass> _totalMass,
						 _dryMassPublicDomainTotal;
	
	private Double _percentTotalDifference;
	private CenterOfGravity _totalCG;
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class PowerPlantBuilder {
	
		// required parameters
		private String __id;
		private Integer __engineNumber;
		public List<Engine> __engineList = new ArrayList<Engine>();
		
		// optional parameters ... defaults
		// ...	
		private List<CenterOfGravity> __cgList = new ArrayList<CenterOfGravity>();
		
		public PowerPlantBuilder (String id, List<Engine> engineList) {
			this.__id = id;
			this.__engineList = engineList;
			this.__engineNumber = engineList.size();
		}
		
		public PowerPlantBuilder (String id, AircraftEnum aircraftName) {
			this.__id = id;
			initializeDefaultVariables(aircraftName);
		}
		
		private void initializeDefaultVariables (AircraftEnum aircraftName) {
			switch(aircraftName) {
			
			case ATR72:
				__engineNumber = 2;
				for (int i=0; i<__engineNumber; i++)
					__engineList.add(
							new Engine
								.EngineBuilder("ATR-72 Engine", EngineTypeEnum.TURBOPROP, aircraftName)
									.build()
							);
				__engineList.get(0).setXApexConstructionAxes(Amount.valueOf(8.56902, SI.METER));
				__engineList.get(0).setYApexConstructionAxes(Amount.valueOf(4.5738, SI.METER));
				__engineList.get(0).setZApexConstructionAxes(Amount.valueOf(1.02895, SI.METER));
				__engineList.get(0).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(0).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				__engineList.get(1).setXApexConstructionAxes(Amount.valueOf(8.56902, SI.METER));
				__engineList.get(1).setYApexConstructionAxes(Amount.valueOf(-4.5738, SI.METER));
				__engineList.get(1).setZApexConstructionAxes(Amount.valueOf(1.02895, SI.METER));
				__engineList.get(1).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(1).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				break;
				
			case B747_100B:
				__engineNumber = 4;
				for (int i=0; i<__engineNumber; i++)
					__engineList.add(
							new Engine
								.EngineBuilder("B747-100B Engine", EngineTypeEnum.TURBOFAN, aircraftName)
									.build()
							);
				__engineList.get(0).setXApexConstructionAxes(Amount.valueOf(23.770, SI.METER));
				__engineList.get(0).setYApexConstructionAxes(Amount.valueOf(11.820, SI.METER));
				__engineList.get(0).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__engineList.get(0).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(0).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				__engineList.get(1).setXApexConstructionAxes(Amount.valueOf(31.693, SI.METER));
				__engineList.get(1).setYApexConstructionAxes(Amount.valueOf(21.951, SI.METER));
				__engineList.get(1).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__engineList.get(1).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(1).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				__engineList.get(2).setXApexConstructionAxes(Amount.valueOf(23.770, SI.METER));
				__engineList.get(2).setYApexConstructionAxes(Amount.valueOf(-11.820, SI.METER));
				__engineList.get(2).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__engineList.get(2).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(2).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				__engineList.get(3).setXApexConstructionAxes(Amount.valueOf(31.693, SI.METER));
				__engineList.get(3).setYApexConstructionAxes(Amount.valueOf(-21.951, SI.METER));
				__engineList.get(3).setZApexConstructionAxes(Amount.valueOf(-2.642, SI.METER));
				__engineList.get(3).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(3).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				break;
				
			case AGILE_DC1:
				__engineNumber = 2;
				for (int i=0; i<__engineNumber; i++)
					__engineList.add(
							new Engine
								.EngineBuilder("AGILE-DC1 Engine", EngineTypeEnum.TURBOFAN, aircraftName)
									.build()
							);
				__engineList.get(0).setXApexConstructionAxes(Amount.valueOf(11.84, SI.METER));
				__engineList.get(0).setYApexConstructionAxes(Amount.valueOf(4.91, SI.METER));
				__engineList.get(0).setZApexConstructionAxes(Amount.valueOf(-2.45, SI.METER));
				__engineList.get(0).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(0).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				__engineList.get(1).setXApexConstructionAxes(Amount.valueOf(11.84, SI.METER));
				__engineList.get(1).setYApexConstructionAxes(Amount.valueOf(-4.91, SI.METER));
				__engineList.get(1).setZApexConstructionAxes(Amount.valueOf(-2.45, SI.METER));
				__engineList.get(1).setMountingPosition(EngineMountingPositionEnum.WING);
				__engineList.get(1).setTiltingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				
				break;
			}
		}
		
		public PowerPlant build() {
			return new PowerPlant(this);
		}
	}
	
	private PowerPlant (PowerPlantBuilder builder) {
		
		this._id = builder.__id;
		this._engineNumber = builder.__engineNumber;
		this._engineList = builder.__engineList;
		this._cgList = builder.__cgList;

		calculateDerivedVariables();
		
	}
	
	//============================================================================================
	// End of builder pattern 
	//============================================================================================

	private void calculateDerivedVariables() {

		_engineType = this._engineList.get(0).getEngineType();
		_mountingPosition = this._engineList.get(0).getMountingPosition();
		
		_t0Total = Amount.valueOf(0., SI.NEWTON);
		_p0Total = Amount.valueOf(0., SI.WATT);
		
		for(int i=0; i < _engineNumber; i++) {
			_t0Total = _t0Total.plus(_engineList.get(i).getT0());
			_p0Total = _p0Total.plus(_engineList.get(i).getP0());
		}
	}

	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();
		
		sb.append("\t-------------------------------------\n")
		  .append("\tThe Power Plant\n")
		  .append("\t-------------------------------------\n")
		  .append("\tId: '" + _id + "'\n")
		  .append("\tNumber of engines: " + _engineNumber + "\n")
		  ;
		for(int i=0; i<this._engineList.size(); i++)
			sb.append("\t-------------------------------------\n")
			  .append("\tEngine n° " + (i+1) + "\n")
			  .append("\t-------------------------------------\n")
			  .append(this._engineList.get(i).toString())
			  ;
		
		
		return sb.toString();
		
	}
	
	@Override
	public void calculateMass() {

		_totalMass = Amount.valueOf(0., SI.KILOGRAM);
		_dryMassPublicDomainTotal = Amount.valueOf(0., SI.KILOGRAM);

		for(int i=0; i < _engineNumber; i++) {
			_totalMass = _totalMass.plus(_engineList.get(i).getTotalMass());
			_dryMassPublicDomainTotal = _dryMassPublicDomainTotal.plus(_engineList.get(i).getTheWeights().getDryMassPublicDomain());
		}

		_percentTotalDifference = _totalMass.
				minus(_dryMassPublicDomainTotal).
				divide(_dryMassPublicDomainTotal).
				getEstimatedValue()*100.;
	}

	@Override
	public CenterOfGravity calculateCG() {

		_totalCG = new CenterOfGravity();		
		for(int i=0; i < _engineNumber; i++) {
			_engineList.get(i).getTheBalance().calculateAll();
			_cgList.add(_engineList.get(i).getTheBalance().get_cg());
			_totalCG = _totalCG.plus(_engineList.get(i).getTheBalance().get_cg()
					.times(_engineList.get(i).getTotalMass().doubleValue(SI.KILOGRAM)));
		}
		
		_totalCG = _totalCG.divide(_totalMass.doubleValue(SI.KILOGRAM));
		return _totalCG;
	}

	@Override
	public String getId() {
		return _id;
	}
	
	@Override
	public void setId(String _id) {
		this._id = _id;
	}

	@Override
	public Integer getEngineNumber() {
		return _engineNumber;
	}

	@Override
	public void setEngineNumber(Integer _engineNumber) {
		this._engineNumber = _engineNumber;
	}

	@Override
	public EngineTypeEnum getEngineType() {
		return _engineType;
	}
	
	@Override
	public void setEngineType(EngineTypeEnum engineType) {
		this._engineType = engineType;
	}
	
	@Override
	public EngineMountingPositionEnum getMountingPosition() {
		return _mountingPosition;
	}
	
	@Override
	public List<Engine> getEngineList() {
		return _engineList;
	}

	@Override
	public void setEngineList(List<Engine> _engineList) {
		this._engineList = _engineList;
	}

	@Override
	public Amount<Force> getT0Total() {
		return _t0Total;
	}

	@Override
	public Amount<Power> getP0Total() {
		return _p0Total;
	}

	@Override
	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}

	@Override
	public Amount<Mass> getTotalMass() {
		return _totalMass;
	}

	@Override
	public void setTotalMass(Amount<Mass> totalMass) {
		this._totalMass = totalMass;
	}
	
	@Override
	public Amount<Mass> getDryMassPublicDomainTotal() {
		return _dryMassPublicDomainTotal;
	}

	@Override
	public Double getPercentTotalDifference() {
		return _percentTotalDifference;
	}

	@Override
	public CenterOfGravity getTotalCG() {
		return _totalCG;
	}

}