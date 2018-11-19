package aircraft.components.powerplant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Force;
import javax.measure.quantity.Power;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import analyses.powerplant.EngineBalanceManager;
import analyses.powerplant.EngineWeightManager;
import configuration.MyConfiguration;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.engine.EngineDatabaseManager;

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
	private List<EngineTypeEnum> _engineType;
	private List<EngineMountingPositionEnum> _mountingPosition;
	private List<EngineDatabaseManager> _engineDatabaseReaderList;
//	private TurbofanEngineDatabaseReader _turbofanEngineDatabaseReader;
//	private TurbopropEngineDatabaseReader _turbopropEngineDatabaseReader;
	private Amount<Force> _t0Total;
	private Amount<Power> _p0Total;
	
	private EngineWeightManager _theWeights;
	private EngineBalanceManager _theBalance;
	
	//--------------------------------------------------------------------------------------------------
	// BUILDER
	public PowerPlant (List<Engine> engineList) {
		
		this._engineList = engineList;
		this._engineNumber = engineList.size();
		this._mountingPosition = new ArrayList<>();
		this._engineType = new ArrayList<>();
		this._engineList.stream().forEach(engine -> {
			_mountingPosition.add(engine.getMountingPosition());
			_engineType.add(engine.getEngineType());
		});
//		this._mountingPosition = engineList.get(0).getMountingPosition(); //TODO: this could be changed in future to account for different architectures (mixed engines)
//		this._engineType = engineList.get(0).getEngineType(); //TODO: this could be changed in future to account for different architectures (mixed engines)
		
		this._t0Total = Amount.valueOf(
				engineList.stream().mapToDouble(eng -> eng.getT0().doubleValue(SI.NEWTON)).sum(),
				SI.NEWTON
				);
		this._p0Total = Amount.valueOf(
				engineList.stream().mapToDouble(eng -> eng.getP0().doubleValue(SI.WATT)).sum(),
				SI.WATT
				);

		this._theWeights = new EngineWeightManager();
		this._theBalance = new EngineBalanceManager();
		
		_engineDatabaseReaderList = new ArrayList<>();
		this._engineList.stream().forEach(engine -> {
			try {
				try {
					_engineDatabaseReaderList.add(
							DatabaseManager.initializeEngineDatabase(
									new EngineDatabaseManager(), 
									MyConfiguration.getDir(FoldersEnum.DATABASE_DIR),
									_engineList.get(0).getEngineDatabaseName()
									)
							);
				} catch (InvalidFormatException | IOException e) {
					e.printStackTrace();
				}
				
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		});
		
//		if((this._engineList.get(0).getEngineType().equals(EngineTypeEnum.TURBOPROP))
//				|| (this._engineList.get(0).getEngineType().equals(EngineTypeEnum.PISTON)))
//			try {
//				_turbopropEngineDatabaseReader = DatabaseManager.initializeTurbopropDatabase(
//						new TurbopropEngineDatabaseReader(
//								MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), 
//								_engineList.get(0).getEngineDatabaseName()
//								), 
//						MyConfiguration.getDir(FoldersEnum.DATABASE_DIR),
//						_engineList.get(0).getEngineDatabaseName()
//						);
//			} catch (HDF5LibraryException e) {
//				e.printStackTrace();
//			} catch (NullPointerException e) {
//				e.printStackTrace();
//			}
//		else
//			_turbofanEngineDatabaseReader = DatabaseManager.initializeTurbofanDatabase(
//					new TurbofanEngineDatabaseReader(
//							MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), 
//							_engineList.get(0).getEngineDatabaseName()
//							), 
//					MyConfiguration.getDir(FoldersEnum.DATABASE_DIR),
//					_engineList.get(0).getEngineDatabaseName()
//					);
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
	
	public List<EngineTypeEnum> getEngineType() {
		return _engineType;
	}

	public void setEngineType(List<EngineTypeEnum> _engineType) {
		this._engineType = _engineType;
	}

	public List<EngineMountingPositionEnum> getMountingPosition() {
		return _mountingPosition;
	}

	public void setMountingPosition(List<EngineMountingPositionEnum> _mountingPosition) {
		this._mountingPosition = _mountingPosition;
	}

//	public TurbofanEngineDatabaseReader getTurbofanEngineDatabaseReader() {
//		return _turbofanEngineDatabaseReader;
//	}
//
//	public void setTurbofanEngineDatabaseReader(TurbofanEngineDatabaseReader _turbofanEngineDatabaseReader) {
//		this._turbofanEngineDatabaseReader = _turbofanEngineDatabaseReader;
//	}
//
//	public TurbopropEngineDatabaseReader getTurbopropEngineDatabaseReader() {
//		return _turbopropEngineDatabaseReader;
//	}
//
//	public void setTurbopropEngineDatabaseReader(TurbopropEngineDatabaseReader _turbopropEngineDatabaseReader) {
//		this._turbopropEngineDatabaseReader = _turbopropEngineDatabaseReader;
//	}

	public EngineWeightManager getTheWeights() {
		return _theWeights;
	}

	public void setTheWeights(EngineWeightManager _theWeights) {
		this._theWeights = _theWeights;
	}

	public EngineBalanceManager getTheBalance() {
		return _theBalance;
	}

	public void setTheBalance(EngineBalanceManager _theBalance) {
		this._theBalance = _theBalance;
	}

	public List<EngineDatabaseManager> getEngineDatabaseReaderList() {
		return _engineDatabaseReaderList;
	}

	public void setEngineDatabaseReader(List<EngineDatabaseManager> _engineDatabaseReaderList) {
		this._engineDatabaseReaderList = _engineDatabaseReaderList;
	}

}