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
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	/** 
	 * A first guess value of center of gravity location
	 * of the whole aircraft.
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 */
	public void calculateBalance(Aircraft aircraft) {

		_xCGMeanAtOEM = (aircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
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
			Aircraft aircraft,
			Map<ComponentEnum,
			List<MethodEnum>> methodsMap
			){

		if(aircraft.getFuselage() != null)
			aircraft.getFuselage().calculateCG(aircraft);

		if(aircraft.getWing() != null)
			aircraft.getWing().calculateCGAllMethods(methodsMap, ComponentEnum.WING);
		if(aircraft.getHTail() != null)
			aircraft.getHTail().calculateCGAllMethods(methodsMap, ComponentEnum.HORIZONTAL_TAIL);
		if(aircraft.getVTail() != null)
			aircraft.getVTail().calculateCGAllMethods(methodsMap, ComponentEnum.VERTICAL_TAIL);
		if(aircraft.getCanard() != null)
			aircraft.getCanard().calculateCGAllMethods(methodsMap, ComponentEnum.CANARD);
		
		if(aircraft.getNacelles() != null)
			aircraft.getNacelles().calculateCG();

		if(aircraft.getFuelTank() != null)
			aircraft.getFuelTank().calculateCG();

		if(aircraft.getLandingGears() != null)
			aircraft.getLandingGears().calculateCG(aircraft);


		// --- END OF STRUCTURE MASS-----------------------------------
		if(aircraft.getPowerPlant() != null)
			aircraft.getPowerPlant().calculateCG();

		calculateTotalCG(aircraft);

		setXCGMeanAtOEM(aircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
		setXCGMaxAftAtOEM(getXCoGMeanAtOEM()*(1-0.1));
		setXCGMaxForAtOEM(getXCoGMeanAtOEM()*(1+0.1));
	}

	/**
	 * Evaluate overall CG
	 * 
	 * @param aircraft
	 */
	public void calculateTotalCG(Aircraft aircraft) {

		// Structural CG
		_cgStructure = new CenterOfGravity();

		_cgList.add(aircraft.getFuselage().getCG());
		_cgList.add(aircraft.getWing().getCG());
		_cgList.add(aircraft.getHTail().getCG());
		_cgList.add(aircraft.getVTail().getCG());
		_cgList.add(aircraft.getLandingGears().getCg());
		_cgList.addAll(aircraft.getNacelles().getCGList());
		
		System.out.println("\n \nCG COMPONENTS LOCATION IN BRF");
		System.out.println("fuselage --> " + _cgList.get(0).getXBRF());
		System.out.println("wing --> " + _cgList.get(1).getXBRF());
		System.out.println("HTail --> " + _cgList.get(2).getXBRF());
		System.out.println("VTail --> " + _cgList.get(3).getXBRF());
		System.out.println("Landing gear --> " + _cgList.get(4).getXBRF());
		for(int i=0 ;  i<aircraft.getNacelles().getCGList().size() ; i++){
		System.out.println("Nacelle  "+  i + " --> " + _cgList.get(i+5).getXBRF());
		}

		double prod = 0., sum = 0.;
		for (int i=0; i < _cgList.size(); i++) {

			prod += _cgList.get(i).getXBRF().getEstimatedValue()
					*aircraft.getTheWeights().getMassStructureList().get(i).getEstimatedValue();
			sum += aircraft.getTheWeights().getMassStructureList().get(i).getEstimatedValue();

		}

		_cgStructure.setXBRF(
				Amount.valueOf(prod/sum, SI.METER));

		_cgStructure.calculateCGinMAC(
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// Structure + engines CG
		_cgStructureAndPower = new CenterOfGravity();

		System.out.println("fuel tank --> " + aircraft.getFuelTank().getXCG().getEstimatedValue());
		double cgPowerPlantContribute =0.0;
		
		for(int i=0 ; i< aircraft.getPowerPlant().getEngineNumber(); i++){
			cgPowerPlantContribute = cgPowerPlantContribute + (aircraft.getPowerPlant().getCGList().get(i).getXBRF().getEstimatedValue()*
					aircraft.getPowerPlant().getEngineList().get(i).getTotalMass().getEstimatedValue());
			System.out.println("Engine " + i + " --> " + aircraft.getPowerPlant().getCGList().get(i).getXBRF());
		}
		_cgStructureAndPower.setXBRF(
				Amount.valueOf(
						       (cgPowerPlantContribute+
								aircraft.getTheWeights().getStructuralMass().getEstimatedValue()*
								getCGStructure().getXBRF().getEstimatedValue())/
								(aircraft.getTheWeights().getStructuralMass().getEstimatedValue() + 
										aircraft.getPowerPlant().getTotalMass().getEstimatedValue())
										, SI.METER));

		getCGStructureAndPower().calculateCGinMAC(
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MZFW CG location
		_cgMZFM = new CenterOfGravity();

		getCGMZFM().setXBRF(Amount.valueOf(
				(getCGStructureAndPower().getXBRF().getEstimatedValue()*aircraft.getTheWeights().getOperatingEmptyMass().getEstimatedValue() + 
						aircraft.getCabinConfiguration().getSeatsCoG().getEstimatedValue()*
						aircraft.getTheWeights().getPaxMassMax().getEstimatedValue()) /
						(aircraft.getTheWeights().getPaxMassMax().getEstimatedValue() + aircraft.getTheWeights().getOperatingEmptyMass().getEstimatedValue())
						, SI.METER));

		getCGMZFM().calculateCGinMAC(
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MTOM CG location
		_cgMTOM = new CenterOfGravity();

		_cgMTOM.setXBRF(Amount.valueOf(
				(_cgMZFM.getXBRF().getEstimatedValue() 
						* aircraft.getTheWeights().getMaximumZeroFuelMass().getEstimatedValue()
						+ aircraft.getFuelTank().getFuelMass().getEstimatedValue()
						* aircraft.getFuelTank().getXCG().getEstimatedValue())
						/ aircraft.getTheWeights().getMaximumTakeOffMass().getEstimatedValue(),
						SI.METER));

		_cgMTOM.calculateCGinMAC(
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

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
}