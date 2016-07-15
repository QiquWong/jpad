package analyses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.apache.commons.lang3.text.WordUtils;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.AnalysisTypeEnum;
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

	private static final String id = "22";
	private AnalysisTypeEnum _type;
	private String _name;

	private Double _xCoGMeanAtOEM;
	private Double _xCoGMaxAftAtOEM;
	private Double _xCoGMaxForAtOEM;
	private Double _xCoGMeanOEMplusMaxPax;
	private Amount<VolumetricDensity> _materialDensity;
	private CenterOfGravity _cgStructure;
	private CenterOfGravity _cgStructureAndPower;
	private CenterOfGravity _cgMZFM;
	private List<CenterOfGravity> _cgList = new ArrayList<CenterOfGravity>();
	private CenterOfGravity _cgMTOM;


	public ACBalanceManager() {
		super();

		_type = AnalysisTypeEnum.BALANCE;
		_name = WordUtils.capitalizeFully(AnalysisTypeEnum.BALANCE.name());

	}

	/** 
	 * A first guess value of center of gravity location
	 * of the whole aircraft.
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 */
	public void calculateBalance(Aircraft aircraft) {

		_xCoGMeanAtOEM = (aircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
		set_xCoGMaxAftAtOEM((_xCoGMeanAtOEM*(1-0.1)));	
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
			Aircraft aircraft, OperatingConditions conditions, 
			Map<ComponentEnum, List<MethodEnum>> methodsMap){

		aircraft.getFuselage().calculateCG(aircraft);

		aircraft.getWing().calculateCGAllMethods(methodsMap, ComponentEnum.WING);
		aircraft.getHTail().calculateCGAllMethods(methodsMap, ComponentEnum.HORIZONTAL_TAIL);
		aircraft.getVTail().calculateCGAllMethods(methodsMap, ComponentEnum.VERTICAL_TAIL);

		aircraft.getNacelles().calculateCG();

		aircraft.getFuelTank().calculateCG();

		aircraft.getLandingGears().calculateCG(aircraft);


		// --- END OF STRUCTURE MASS-----------------------------------

		aircraft.getPowerPlant().calculateCG();

		calculateTotalCG(aircraft);

		set_xCoGMeanAtOEM(aircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
		set_xCoGMaxAftAtOEM(get_xCoGMeanAtOEM()*(1-0.1));
		set_xCoGMaxForAtOEM(get_xCoGMeanAtOEM()*(1+0.1));
	}

	/**
	 * Evaluate overall CG
	 * 
	 * @param aircraft
	 */
	public void calculateTotalCG(Aircraft aircraft) {

		// Structural CG
		_cgStructure = new CenterOfGravity();

		_cgList.add(aircraft.getFuselage().get_cg());
		_cgList.add(aircraft.getWing().getCG());
		_cgList.add(aircraft.getHTail().getCG());
		_cgList.add(aircraft.getVTail().getCG());
		_cgList.add(aircraft.getLandingGears().getCg());
		_cgList.addAll(aircraft.getNacelles().getCGList());
		
		System.out.println("\n \nCG COMPONENTS LOCATION IN BRF");
		System.out.println("fuselage --> " + _cgList.get(0).get_xBRF());
		System.out.println("wing --> " + _cgList.get(1).get_xBRF());
		System.out.println("HTail --> " + _cgList.get(2).get_xBRF());
		System.out.println("VTail --> " + _cgList.get(3).get_xBRF());
		System.out.println("Landing gear --> " + _cgList.get(4).get_xBRF());
		for(int i=0 ;  i<aircraft.getNacelles().getCGList().size() ; i++){
		System.out.println("Nacelle  "+  i + " --> " + _cgList.get(i+5).get_xBRF());
		}

		
		
//		 _cgList.forEach(p-> System.out.println(p.get_xBRF()));

		double prod = 0., sum = 0.;
		for (int i=0; i < _cgList.size(); i++) {

			prod += _cgList.get(i).get_xBRF().getEstimatedValue()
					*aircraft.getTheWeights().getMassStructureList().get(i).getEstimatedValue();
			sum += aircraft.getTheWeights().getMassStructureList().get(i).getEstimatedValue();

		}

		_cgStructure.set_xBRF(
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
			cgPowerPlantContribute = cgPowerPlantContribute + (aircraft.getPowerPlant().getCGList().get(i).get_xBRF().getEstimatedValue()*
					aircraft.getPowerPlant().getEngineList().get(i).getTotalMass().getEstimatedValue());
			System.out.println("Engine " + i + " --> " + aircraft.getPowerPlant().getCGList().get(i).get_xBRF());
		}
		_cgStructureAndPower.set_xBRF(
				Amount.valueOf(
						       (cgPowerPlantContribute+
								aircraft.getTheWeights().getStructuralMass().getEstimatedValue()*
								get_cgStructure().get_xBRF().getEstimatedValue())/
								(aircraft.getTheWeights().getStructuralMass().getEstimatedValue() + 
										aircraft.getPowerPlant().getTotalMass().getEstimatedValue())
										, SI.METER));

		get_cgStructureAndPower().calculateCGinMAC(
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MZFW CG location
		_cgMZFM = new CenterOfGravity();

		get_cgMZFM().set_xBRF(Amount.valueOf(
				(get_cgStructureAndPower().get_xBRF().getEstimatedValue()*aircraft.getTheWeights().getOperatingEmptyMass().getEstimatedValue() + 
						aircraft.getCabinConfiguration().getSeatsCoG().getEstimatedValue()*
						aircraft.getTheWeights().getPaxMassMax().getEstimatedValue()) /
						(aircraft.getTheWeights().getPaxMassMax().getEstimatedValue() + aircraft.getTheWeights().getOperatingEmptyMass().getEstimatedValue())
						, SI.METER));

		get_cgMZFM().calculateCGinMAC(
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MTOM CG location
		_cgMTOM = new CenterOfGravity();

		_cgMTOM.set_xBRF(Amount.valueOf(
				(_cgMZFM.get_xBRF().getEstimatedValue() 
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

	@Override    
	public AnalysisTypeEnum get_type() {
		return _type;
	}

	public void set_type(AnalysisTypeEnum _type) {
		this._type = _type;
	}

	@Override
	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public Double get_xCoGMeanAtOEM() {
		return _xCoGMeanAtOEM;
	}

	public void set_xCoGMeanAtOEM(Double _xCoGMeanAtOEM) {
		this._xCoGMeanAtOEM = _xCoGMeanAtOEM;
	}

	public Double get_xCoGMaxAftAtOEM() {
		return _xCoGMaxAftAtOEM;
	}

	public void set_xCoGMaxAftAtOEM(Double _xCoGMaxAftAtOEM) {
		this._xCoGMaxAftAtOEM = _xCoGMaxAftAtOEM;
	}

	public Double get_xCoGMaxForAtOEM() {
		return _xCoGMaxForAtOEM;
	}

	public void set_xCoGMaxForAtOEM(Double _xCoGMaxForAtOEM) {
		this._xCoGMaxForAtOEM = _xCoGMaxForAtOEM;
	}

	public Double get_xCoGMeanOEMplusMaxPax() {
		return _xCoGMeanOEMplusMaxPax;
	}

	public void set_xCoGMeanOEMplusMaxPax(Double _xCoGMeanOEMplusMaxPax) {
		this._xCoGMeanOEMplusMaxPax = _xCoGMeanOEMplusMaxPax;
	}

	public Amount<VolumetricDensity> get_materialDensity() {
		return _materialDensity;
	}

	public void set_materialDensity(Amount<VolumetricDensity> _materialDensity) {
		this._materialDensity = _materialDensity;
	}


	public List<CenterOfGravity> get_cgList() {
		return _cgList;
	}

	public void set_cgList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	public CenterOfGravity get_cgStructure() {
		return _cgStructure;
	}

	public void set_cgStructure(CenterOfGravity _cgStructure) {
		this._cgStructure = _cgStructure;
	}

	public CenterOfGravity get_cgStructureAndPower() {
		return _cgStructureAndPower;
	}

	public void set_cgStructureAndPower(CenterOfGravity _cgStructureAndPower) {
		this._cgStructureAndPower = _cgStructureAndPower;
	}

	public CenterOfGravity get_cgMZFM() {
		return _cgMZFM;
	}

	public void set_cgMZFM(CenterOfGravity _cgMZFM) {
		this._cgMZFM = _cgMZFM;
	}

	public CenterOfGravity get_cgMTOM() {
		return _cgMTOM;
	}

	public void set_cgMTOM(CenterOfGravity _cgMTOM) {
		this._cgMTOM = _cgMTOM;
	}

	public static String getId() {
		return id;
	}


}