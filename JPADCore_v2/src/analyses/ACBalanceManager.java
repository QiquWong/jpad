package analyses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

/**
 * Manage the calculations for estimating the aircraft balance.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class ACBalanceManager extends ACCalculatorManager {

	private String _id;
	private static Aircraft _theAircraft;

	//---------------------------------------------------------------------------------
	// INPUT DATA :
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _maximumZeroFuelMass;
	private Amount<Mass> _operatingEmptyMass;	
	private Amount<Mass> _passengersTotalMass;
	private Amount<Mass> _fuselageMass;
	private Amount<Mass> _wingMass;
	private Amount<Mass> _horizontalTailMass;
	private Amount<Mass> _verticalTailMass;
	private Amount<Mass> _canardMass;
	private List<Amount<Mass>> _nacellesMassList;
	private List<Amount<Mass>> _enginesMassList;
	private Amount<Mass> _fuelTankMass;
	private Amount<Mass> _landingGearsMass;
	private Amount<Mass> _systemsMass;
	
	//---------------------------------------------------------------------------------
	// OUTPUT DATA : 
	private Double _xCGMeanAtOEM;
	private Double _xCGMaxAftAtOEM;
	private Double _xCGMaxForAtOEM;
	private Double _xCGMeanOEMplusMaxPax;
	private CenterOfGravity _cgStructure;
	private CenterOfGravity _cgStructureAndPower;
	private CenterOfGravity _cgMZFM;
	private List<CenterOfGravity> _cgList;
	private CenterOfGravity _cgMTOM;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACBalanceManagerBuilder {

		// required parameters
		private String __id;
		private Aircraft __theAircraft;

		// optional parameters ... defaults
		// ...
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __maximumZeroFuelMass;
		private Amount<Mass> __operatingEmptyMass;	
		private Amount<Mass> __passengersTotalMass;
		private Amount<Mass> __fuselageMass;
		private Amount<Mass> __wingMass;
		private Amount<Mass> __horizontalTailMass;
		private Amount<Mass> __verticalTailMass;
		private Amount<Mass> __canardMass;
		private List<Amount<Mass>> __nacellesMassList = new ArrayList<Amount<Mass>>();
		private List<Amount<Mass>> __enginesMassList = new ArrayList<Amount<Mass>>();
		private Amount<Mass> __fuelTankMass;
		private Amount<Mass> __landingGearsMass;
		private Amount<Mass> __systemsMass;
	
		private List<CenterOfGravity> __cgList = new ArrayList<CenterOfGravity>();
		
		public ACBalanceManagerBuilder id (String id) {
			this.__id = id;
			return this;
		}

		public ACBalanceManagerBuilder aircraft (Aircraft theAircraft) {
			this.__theAircraft = theAircraft;
			return this;
		}
		
		public ACBalanceManagerBuilder maximumTakeOffMass (Amount<Mass> maximumTakeOffMass) {
			this.__maximumTakeOffMass = maximumTakeOffMass;
			return this;
		}
		
		public ACBalanceManagerBuilder maximumZeroFuelMass (Amount<Mass> maximumZeroFuelMass) {
			this.__maximumZeroFuelMass = maximumZeroFuelMass;
			return this;
		}
		
		public ACBalanceManagerBuilder operatingEmptyMass (Amount<Mass> operatingEmptyMass) {
			this.__operatingEmptyMass = operatingEmptyMass;
			return this;
		}
		
		public ACBalanceManagerBuilder passengersTotalMass (Amount<Mass> passengersTotalMass) {
			this.__passengersTotalMass = passengersTotalMass;
			return this;
		}
		
		public ACBalanceManagerBuilder fuselageMass (Amount<Mass> fuselageMass) {
			this.__fuselageMass = fuselageMass;
			return this;
		}
		
		public ACBalanceManagerBuilder wingMass (Amount<Mass> wingMass) {
			this.__wingMass = wingMass;
			return this;
		}
		
		public ACBalanceManagerBuilder horizontalTailMass (Amount<Mass> horizontalTailMass) {
			this.__horizontalTailMass = horizontalTailMass;
			return this;
		}
		
		public ACBalanceManagerBuilder verticalTailMass (Amount<Mass> verticalTailMass) {
			this.__verticalTailMass = verticalTailMass;
			return this;
		}
		
		public ACBalanceManagerBuilder canardMass (Amount<Mass> canardMass) {
			this.__canardMass = canardMass;
			return this;
		}
		
		public ACBalanceManagerBuilder nacellesMass (List<Amount<Mass>> nacellesMassList) {
			this.__nacellesMassList = nacellesMassList;
			return this;
		}
		
		public ACBalanceManagerBuilder enginesMass (List<Amount<Mass>> enginesMassList) {
			this.__enginesMassList = enginesMassList;
			return this;
		}
		
		public ACBalanceManagerBuilder fuelTankMass (Amount<Mass> fuelTankMass) {
			this.__fuelTankMass = fuelTankMass;
			return this;
		}
		
		public ACBalanceManagerBuilder landingGearsMass (Amount<Mass> landingGearsMass) {
			this.__landingGearsMass = landingGearsMass;
			return this;
		}
		
		public ACBalanceManagerBuilder systemsMass (Amount<Mass> systemsMass) {
			this.__systemsMass = systemsMass;
			return this;
		}
		
		public ACBalanceManagerBuilder (String id, Aircraft theAircraft) {
			this.__id = id;
			this.__theAircraft = theAircraft;
			// TODO : INITIALIZER ?? IT HAS TO INITIALIZE WHAT? (ZERO??)
		}
		
		public ACBalanceManager build() {
			return new ACBalanceManager(this);
		}
	}
	
	private ACBalanceManager(ACBalanceManagerBuilder builder) {
		
		this._id = builder.__id;
		_theAircraft = builder.__theAircraft;
		
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._maximumZeroFuelMass = builder.__maximumZeroFuelMass;
		this._operatingEmptyMass = builder.__operatingEmptyMass;
		this._passengersTotalMass = builder.__passengersTotalMass;
		this._fuselageMass = builder.__fuselageMass;
		this._wingMass = builder.__wingMass;
		this._horizontalTailMass = builder.__horizontalTailMass;
		this._verticalTailMass = builder.__verticalTailMass;
		this._canardMass = builder.__canardMass;
		this._nacellesMassList = builder.__nacellesMassList;
		this._enginesMassList = builder.__enginesMassList;
		this._fuelTankMass = builder.__fuelTankMass;
		this._landingGearsMass = builder.__landingGearsMass;
		this._systemsMass = builder.__systemsMass;
		
		this._cgList = builder.__cgList;
		
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// TODO : ADD ImportFromXml() (which reads the xls file and find the wanted variable inside it,  //
    //    	  or initializes the input data). 														 //
    //		  ADD toString().																		 //
	//		  ADD toXLSFile().																		 //
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 
	 * A first guess value of center of gravity location
	 * of the whole aircraft.
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 */
	public void calculateBalance() {

		_xCGMeanAtOEM = (_theAircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
		setXCGMaxAftAtOEM((_xCGMeanAtOEM*(1-0.1)));	
	}

	/** 
	 * Evaluate center of gravity location
	 * of each component.
	 * 
	 * @param aircraft
	 * @param conditions
	 * @param methodsMap
	 */
	public void calculateBalance(
			Map<ComponentEnum,
			List<MethodEnum>> methodsMap
			){

		if(_theAircraft.getFuselage() != null)
			_theAircraft.getFuselage().calculateCG(_theAircraft);

		if(_theAircraft.getWing() != null)
			_theAircraft.getWing().calculateCGAllMethods(methodsMap, ComponentEnum.WING);
		if(_theAircraft.getHTail() != null)
			_theAircraft.getHTail().calculateCGAllMethods(methodsMap, ComponentEnum.HORIZONTAL_TAIL);
		if(_theAircraft.getVTail() != null)
			_theAircraft.getVTail().calculateCGAllMethods(methodsMap, ComponentEnum.VERTICAL_TAIL);
		if(_theAircraft.getCanard() != null)
			_theAircraft.getCanard().calculateCGAllMethods(methodsMap, ComponentEnum.CANARD);
		
		if(_theAircraft.getNacelles() != null)
			_theAircraft.getNacelles().calculateCG();

		if(_theAircraft.getFuelTank() != null)
			_theAircraft.getFuelTank().calculateCG();

		if(_theAircraft.getLandingGears() != null)
			_theAircraft.getLandingGears().calculateCG(_theAircraft);


		// --- END OF STRUCTURE MASS-----------------------------------
		if(_theAircraft.getPowerPlant() != null)
			_theAircraft.getPowerPlant().calculateCG();

		calculateTotalCG();

		setXCGMeanAtOEM(_theAircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
		setXCGMaxAftAtOEM(getXCoGMeanAtOEM()*(1-0.1));
		setXCGMaxForAtOEM(getXCoGMeanAtOEM()*(1+0.1));
	}

	/**
	 * Evaluate overall CG
	 * 
	 * @param aircraft
	 */
	public void calculateTotalCG() {

		// Structural CG
		_cgStructure = new CenterOfGravity();

		_cgList.add(_theAircraft.getFuselage().getCG());
		_cgList.add(_theAircraft.getWing().getCG());
		_cgList.add(_theAircraft.getHTail().getCG());
		_cgList.add(_theAircraft.getVTail().getCG());
		_cgList.add(_theAircraft.getLandingGears().getCg());
		_cgList.addAll(_theAircraft.getNacelles().getCGList());
		
		System.out.println("\n \nCG COMPONENTS LOCATION IN BRF");
		System.out.println("fuselage --> " + _cgList.get(0).getXBRF());
		System.out.println("wing --> " + _cgList.get(1).getXBRF());
		System.out.println("HTail --> " + _cgList.get(2).getXBRF());
		System.out.println("VTail --> " + _cgList.get(3).getXBRF());
		System.out.println("Landing gear --> " + _cgList.get(4).getXBRF());
		for(int i=0 ;  i<_theAircraft.getNacelles().getCGList().size() ; i++){
		System.out.println("Nacelle  "+  i + " --> " + _cgList.get(i+5).getXBRF());
		}

		List<Amount<Mass>> massStructureList = new ArrayList<Amount<Mass>>();
		if(_fuselageMass != null)
			massStructureList.add(_fuselageMass);
		if(_wingMass != null)
			massStructureList.add(_wingMass);
		if(_horizontalTailMass != null)
			massStructureList.add(_horizontalTailMass);
		if(_verticalTailMass != null)
			massStructureList.add(_verticalTailMass);
		if(_canardMass != null)
			massStructureList.add(_canardMass);
		if(_nacellesMassList != null) 
			massStructureList.addAll(_nacellesMassList);
		if(_landingGearsMass != null)
			massStructureList.add(_landingGearsMass);
		
		double prod = 0., sum = 0.;
		for (int i=0; i < _cgList.size(); i++) {

			prod += _cgList.get(i).getXBRF().getEstimatedValue()*massStructureList.get(i).getEstimatedValue();
			sum += massStructureList.get(i).getEstimatedValue();

		}

		_cgStructure.setXBRF(
				Amount.valueOf(prod/sum, SI.METER));

		_cgStructure.calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// Structure + engines CG
		_cgStructureAndPower = new CenterOfGravity();

		System.out.println("fuel tank --> " + _theAircraft.getFuelTank().getXCG().getEstimatedValue());
		double cgPowerPlantContribute =0.0;
		Amount<Mass> powerPlantMass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		for(int i=0 ; i< _theAircraft.getPowerPlant().getEngineNumber(); i++){
			powerPlantMass = powerPlantMass.plus(_enginesMassList.get(i));
			cgPowerPlantContribute = cgPowerPlantContribute + (_theAircraft.getPowerPlant().getCGList().get(i).getXBRF().getEstimatedValue()*
					_enginesMassList.get(i).getEstimatedValue());
			System.out.println("Engine " + i + " --> " + _theAircraft.getPowerPlant().getCGList().get(i).getXBRF());
		}
		_cgStructureAndPower.setXBRF(
				Amount.valueOf(
						       (cgPowerPlantContribute+
						    		   sum*getCGStructure().getXBRF().getEstimatedValue())/
								(sum + powerPlantMass.getEstimatedValue())
										, SI.METER));

		getCGStructureAndPower().calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MZFW CG location
		_cgMZFM = new CenterOfGravity();

		getCGMZFM().setXBRF(Amount.valueOf(
				(getCGStructureAndPower().getXBRF().getEstimatedValue()*_operatingEmptyMass.getEstimatedValue() + 
						_theAircraft.getCabinConfiguration().getSeatsCoG().getEstimatedValue()*
						_passengersTotalMass.getEstimatedValue()) /
						(_passengersTotalMass.getEstimatedValue() 
								+ _operatingEmptyMass.getEstimatedValue())
						, SI.METER));

		getCGMZFM().calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MTOM CG location
		_cgMTOM = new CenterOfGravity();

		_cgMTOM.setXBRF(Amount.valueOf(
				(_cgMZFM.getXBRF().getEstimatedValue() 
						* _maximumZeroFuelMass.getEstimatedValue()
						+ _theAircraft.getFuelTank().getFuelMass().getEstimatedValue()
						* _theAircraft.getFuelTank().getXCG().getEstimatedValue())
						/ this._maximumTakeOffMass.getEstimatedValue(),
						SI.METER));

		_cgMTOM.calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

	}

	public Double getXCoGMeanAtOEM() {
		return _xCGMeanAtOEM;
	}

	public void setXCGMeanAtOEM(Double _xCGMeanAtOEM) {
		this._xCGMeanAtOEM = _xCGMeanAtOEM;
	}

	public Double getXCGMaxAftAtOEM() {
		return _xCGMaxAftAtOEM;
	}

	public void setXCGMaxAftAtOEM(Double _xCGMaxAftAtOEM) {
		this._xCGMaxAftAtOEM = _xCGMaxAftAtOEM;
	}

	public Double getXCGMaxForAtOEM() {
		return _xCGMaxForAtOEM;
	}

	public void setXCGMaxForAtOEM(Double _xCGMaxForAtOEM) {
		this._xCGMaxForAtOEM = _xCGMaxForAtOEM;
	}

	public Double getXCGMeanOEMplusMaxPax() {
		return _xCGMeanOEMplusMaxPax;
	}

	public void setXCGMeanOEMplusMaxPax(Double _xCGMeanOEMplusMaxPax) {
		this._xCGMeanOEMplusMaxPax = _xCGMeanOEMplusMaxPax;
	}

	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}

	public void setCGList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	public CenterOfGravity getCGStructure() {
		return _cgStructure;
	}

	public void setCGStructure(CenterOfGravity _cgStructure) {
		this._cgStructure = _cgStructure;
	}

	public CenterOfGravity getCGStructureAndPower() {
		return _cgStructureAndPower;
	}

	public void setCGStructureAndPower(CenterOfGravity _cgStructureAndPower) {
		this._cgStructureAndPower = _cgStructureAndPower;
	}

	public CenterOfGravity getCGMZFM() {
		return _cgMZFM;
	}

	public void setCGMZFM(CenterOfGravity _cgMZFM) {
		this._cgMZFM = _cgMZFM;
	}

	public CenterOfGravity getCGMTOM() {
		return _cgMTOM;
	}

	public void setCGMTOM(CenterOfGravity _cgMTOM) {
		this._cgMTOM = _cgMTOM;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}
	
	public static Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public static void setTheAircraft(Aircraft _theAircraft) {
		ACBalanceManager._theAircraft = _theAircraft;
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) {
		this._maximumTakeOffMass = _maximumTakeOffMass;
	}

	public Amount<Mass> getMaximumZeroFuelMass() {
		return _maximumZeroFuelMass;
	}

	public void setMaximumZeroFuelMass(Amount<Mass> _maximumZeroFuelMass) {
		this._maximumZeroFuelMass = _maximumZeroFuelMass;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass) {
		this._operatingEmptyMass = _operatingEmptyMass;
	}

	public Amount<Mass> getPassengersTotalMass() {
		return _passengersTotalMass;
	}

	public void setPassengersTotalMass(Amount<Mass> _passengerTotalMass) {
		this._passengersTotalMass = _passengerTotalMass;
	}

	public Amount<Mass> getFuselageMass() {
		return _fuselageMass;
	}

	public void setFuselageMass(Amount<Mass> _fuselageMass) {
		this._fuselageMass = _fuselageMass;
	}

	public Amount<Mass> getWingMass() {
		return _wingMass;
	}

	public void setWingMass(Amount<Mass> _wingMass) {
		this._wingMass = _wingMass;
	}

	public Amount<Mass> getHorizontalTailMass() {
		return _horizontalTailMass;
	}

	public void setHorizontalTailMass(Amount<Mass> _horizontalTailMass) {
		this._horizontalTailMass = _horizontalTailMass;
	}

	public Amount<Mass> getVerticalTailMass() {
		return _verticalTailMass;
	}

	public void setVerticalTailMass(Amount<Mass> _verticalTailMass) {
		this._verticalTailMass = _verticalTailMass;
	}

	public Amount<Mass> getCanardMass() {
		return _canardMass;
	}

	public void setCanardMass(Amount<Mass> _canardMass) {
		this._canardMass = _canardMass;
	}

	public List<Amount<Mass>> getNacellesMassList() {
		return _nacellesMassList;
	}

	public void setNacellesMassList(List<Amount<Mass>> _nacellesMassList) {
		this._nacellesMassList = _nacellesMassList;
	}

	public List<Amount<Mass>> getEnginesMassList() {
		return _enginesMassList;
	}

	public void setEnginesMassList(List<Amount<Mass>> _enginesMassList) {
		this._enginesMassList = _enginesMassList;
	}

	public Amount<Mass> getFuelTankMass() {
		return _fuelTankMass;
	}

	public void setFuelTankMass(Amount<Mass> _fuelTankMass) {
		this._fuelTankMass = _fuelTankMass;
	}

	public Amount<Mass> getLandingGearsMass() {
		return _landingGearsMass;
	}

	public void setLandingGearsMass(Amount<Mass> _landingGearsMass) {
		this._landingGearsMass = _landingGearsMass;
	}

	public Amount<Mass> getSystemsMass() {
		return _systemsMass;
	}

	public void setSystemsMass(Amount<Mass> _systemsMass) {
		this._systemsMass = _systemsMass;
	}
}