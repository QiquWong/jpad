package aircraft.components.powerplant;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.engine.TurbofanEngineDatabaseReader;
import database.databasefunctions.engine.TurbopropEngineDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.customdata.CenterOfGravity;

/** 
 * The Propulsion System includes engines, engine exhaust, 
 * reverser, starting, controls, lubricating, and fuel systems.
 * The output of this class is the entire propulsion system (that
 * is, all engines are included) 
 */

public class PowerPlant {

	//--------------------------------------------------------------------------------------------------
	// VARIABLES DECLARATION
	private Integer _engineNumber;
	private List<Engine> _engineList;
	private EngineTypeEnum _engineType;
	private EngineMountingPositionEnum _mountingPosition;
	private TurbofanEngineDatabaseReader _turbofanEngineDatabaseReader;
	private TurbopropEngineDatabaseReader _turbopropEngineDatabaseReader;
	private Amount<Force> _t0Total;
	private Amount<Power> _p0Total;
	
	// TODO: move these ??
	private Amount<Mass> _totalMass, _dryMassPublicDomainTotal;
	private List<CenterOfGravity> _cgList;
	private CenterOfGravity _totalCG;
	
	//--------------------------------------------------------------------------------------------------
	// BUILDER
	public PowerPlant (List<Engine> engineList) {
		
		this._engineList = engineList;
		this._engineNumber = engineList.size();
		this._mountingPosition = engineList.get(0).getMountingPosition(); //TODO: this could be changed in future to account for different architectures (mixed engines)
		this._engineType = engineList.get(0).getEngineType(); //TODO: this could be changed in future to account for different architectures (mixed engines)
		this._t0Total = Amount.valueOf(
				engineList.stream().mapToDouble(eng -> eng.getT0().doubleValue(SI.NEWTON)).sum(),
				SI.NEWTON
				);
		
		this._t0Total = Amount.valueOf(
				engineList.stream().mapToDouble(eng -> eng.getT0().doubleValue(SI.NEWTON)).sum(),
				SI.NEWTON
				);
		this._p0Total = Amount.valueOf(
				engineList.stream().mapToDouble(eng -> eng.getP0().doubleValue(SI.WATT)).sum(),
				SI.WATT
				);
		this._dryMassPublicDomainTotal = Amount.valueOf(
				engineList.stream().mapToDouble(eng -> eng.getDryMassPublicDomain().doubleValue(SI.KILOGRAM)).sum(),
				SI.KILOGRAM
				); 

		_cgList = new ArrayList<>();

		//TODO: the two database reader should be a list of readers since each engine can be different
		//      (to be changed in future to account for different architectures (mixed engines))
		if((this._engineList.get(0).getEngineType().equals(EngineTypeEnum.TURBOPROP))
				|| (this._engineList.get(0).getEngineType().equals(EngineTypeEnum.PISTON)))
			try {
				_turbopropEngineDatabaseReader = DatabaseManager.initializeTurbopropDatabase(
						new TurbopropEngineDatabaseReader(
								MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), 
								_engineList.get(0).getEngineDatabaseName()
								), 
						MyConfiguration.getDir(FoldersEnum.DATABASE_DIR),
						_engineList.get(0).getEngineDatabaseName()
						);
			} catch (HDF5LibraryException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		else
			_turbofanEngineDatabaseReader = DatabaseManager.initializeTurbofanDatabase(
					new TurbofanEngineDatabaseReader(
							MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), 
							_engineList.get(0).getEngineDatabaseName()
							), 
					MyConfiguration.getDir(FoldersEnum.DATABASE_DIR),
					_engineList.get(0).getEngineDatabaseName()
					);
	}
	
	//--------------------------------------------------------------------------------------------------
	// METHODS
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();
		
		sb.append("\t-------------------------------------\n")
		  .append("\tThe Power Plant\n")
		  .append("\t-------------------------------------\n")
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
	
	public void calculateMass(Aircraft theAircraft) {

		_totalMass = Amount.valueOf(0., SI.KILOGRAM);
		_dryMassPublicDomainTotal = Amount.valueOf(0., SI.KILOGRAM);

		for(int i=0; i < _engineNumber; i++) {
			_engineList.get(i).getTheWeights().calculateTotalMass(theAircraft);
			_totalMass = _totalMass.plus(_engineList.get(i).getTheWeights().getTotalMass());
			_dryMassPublicDomainTotal = _dryMassPublicDomainTotal.plus(_engineList.get(i).getTheWeights().getDryMassPublicDomain());
		}
	}

	public CenterOfGravity calculateCG() {

		_totalCG = new CenterOfGravity();
		
		
		for(int i=0; i < _engineNumber; i++) {
			_engineList.get(i).getTheBalance().getCG().setX0(
					_engineList.get(i).getXApexConstructionAxes()
					);
			_engineList.get(i).getTheBalance().getCG().setY0(
					_engineList.get(i).getYApexConstructionAxes()
					);
			_engineList.get(i).getTheBalance().getCG().setZ0(
					_engineList.get(i).getZApexConstructionAxes()
					);
			_engineList.get(i).getTheBalance().calculateAll();
			_cgList.add(_engineList.get(i).getTheBalance().getCG());
			_totalCG = _totalCG.plus(_engineList.get(i).getTheBalance().getCG()
					.times(_engineList.get(i).getTotalMass().doubleValue(SI.KILOGRAM)));
		}
		
		_totalCG = _totalCG.divide(_totalMass.doubleValue(SI.KILOGRAM));
		return _totalCG;
	}

	//--------------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public Integer getEngineNumber() {
		return _engineNumber;
	}
	
	public void setEngineNumber(Integer _engineNumber) {
		this._engineNumber = _engineNumber;
	}
	
	public List<Engine> getEngineList() {
		return _engineList;
	}
	
	public void setEngineList(List<Engine> _engineList) {
		this._engineList = _engineList;
	}
	
	public Amount<Force> getT0Total() {
		return _t0Total;
	}
	
	public Amount<Power> getP0Total() {
		return _p0Total;
	}
	
	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}
	
	public Amount<Mass> getTotalMass() {
		return _totalMass;
	}
	
	public void setTotalMass(Amount<Mass> totalMass) {
		this._totalMass = totalMass;
	}
	
	public Amount<Mass> getDryMassPublicDomainTotal() {
		return _dryMassPublicDomainTotal;
	}
	
	public void setDryMassPublicDomainTotal(Amount<Mass> dryMassTotal) {
		this._dryMassPublicDomainTotal = dryMassTotal;
	}
	
	public CenterOfGravity getTotalCG() {
		return _totalCG;
	}
	
	public EngineTypeEnum getEngineType() {
		return _engineType;
	}

	public void setEngineType(EngineTypeEnum _engineType) {
		this._engineType = _engineType;
	}

	public EngineMountingPositionEnum getMountingPosition() {
		return _mountingPosition;
	}

	public void setMountingPosition(EngineMountingPositionEnum _mountingPosition) {
		this._mountingPosition = _mountingPosition;
	}

	public TurbofanEngineDatabaseReader getTurbofanEngineDatabaseReader() {
		return _turbofanEngineDatabaseReader;
	}

	public void setTurbofanEngineDatabaseReader(TurbofanEngineDatabaseReader _turbofanEngineDatabaseReader) {
		this._turbofanEngineDatabaseReader = _turbofanEngineDatabaseReader;
	}

	public TurbopropEngineDatabaseReader getTurbopropEngineDatabaseReader() {
		return _turbopropEngineDatabaseReader;
	}

	public void setTurbopropEngineDatabaseReader(TurbopropEngineDatabaseReader _turbopropEngineDatabaseReader) {
		this._turbopropEngineDatabaseReader = _turbopropEngineDatabaseReader;
	}

}