package aircraft.calculators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.apache.commons.lang3.text.WordUtils;
import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
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

		_xCoGMeanAtOEM = (aircraft.get_wing().get_X0().getEstimatedValue() + 
				0.25*aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue());
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

		aircraft.get_fuselage().calculateCG(aircraft, conditions);

		aircraft.get_wing().calculateCG(aircraft, conditions);
		aircraft.get_HTail().calculateCG(aircraft, conditions);
		aircraft.get_VTail().calculateCG(aircraft, conditions);

		aircraft.get_theNacelles().calculateCG();

		aircraft.get_theFuelTank().calculateCG(aircraft, conditions);

		aircraft.get_landingGear().calculateCG(aircraft, conditions);

		// --- END OF STRUCTURE MASS-----------------------------------

		aircraft.get_powerPlant().calculateCG();

		calculateTotalCG(aircraft);

		set_xCoGMeanAtOEM(aircraft.get_wing().get_X0().getEstimatedValue() + 
				0.25*aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue());
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

		_cgList.add(aircraft.get_fuselage().get_cg());
		_cgList.add(aircraft.get_wing().get_cg());
		_cgList.add(aircraft.get_HTail().get_cg());
		_cgList.add(aircraft.get_VTail().get_cg());
		_cgList.addAll(aircraft.get_theNacelles().get_cgList());
		_cgList.add(aircraft.get_landingGear().get_cg());

		double prod = 0., sum = 0.;
		for (int i=0; i < _cgList.size(); i++) {

			prod += _cgList.get(i).get_xBRF().getEstimatedValue()
					*aircraft.get_weights().get_massStructureList().get(i).getEstimatedValue();
			sum += aircraft.get_weights().get_massStructureList().get(i).getEstimatedValue();

		}

		_cgStructure.set_xBRF(
				Amount.valueOf(prod/sum, SI.METER));

		_cgStructure.calculateCGinMAC(
				aircraft.get_wing().get_xLEMacActualBRF(), 
				aircraft.get_wing().get_xLEMacActualBRF(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.get_wing().get_meanAerodChordActual());

		// Structure + engines CG
		_cgStructureAndPower = new CenterOfGravity();

		_cgStructureAndPower.set_xBRF(
				Amount.valueOf(
						(aircraft.get_powerPlant().get_totalCG().get_xBRF().getEstimatedValue()*
								aircraft.get_powerPlant().get_totalMass().getEstimatedValue() + 
								aircraft.get_weights().get_structuralMass().getEstimatedValue()*
								get_cgStructure().get_xBRF().getEstimatedValue())/
								(aircraft.get_weights().get_structuralMass().getEstimatedValue() + 
										aircraft.get_powerPlant().get_totalMass().getEstimatedValue())
										, SI.METER));

		get_cgStructureAndPower().calculateCGinMAC(
				aircraft.get_wing().get_xLEMacActualBRF(), 
				aircraft.get_wing().get_xLEMacActualBRF(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.get_wing().get_meanAerodChordActual());

		// MZFW CG location
		_cgMZFM = new CenterOfGravity();

		get_cgMZFM().set_xBRF(Amount.valueOf(
				(get_cgStructureAndPower().get_xBRF().getEstimatedValue()*aircraft.get_weights().get_OEM().getEstimatedValue() + 
						aircraft.get_configuration().get_seatsCoG().getEstimatedValue()*
						aircraft.get_weights().get_paxMassMax().getEstimatedValue()) /
						(aircraft.get_weights().get_paxMassMax().getEstimatedValue() + aircraft.get_weights().get_OEM().getEstimatedValue())
						, SI.METER));

		get_cgMZFM().calculateCGinMAC(
				aircraft.get_wing().get_xLEMacActualBRF(), 
				aircraft.get_wing().get_xLEMacActualBRF(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.get_wing().get_meanAerodChordActual());

		// MTOM CG location
		_cgMTOM = new CenterOfGravity();

		_cgMTOM.set_xBRF(Amount.valueOf(
				(_cgMZFM.get_xBRF().getEstimatedValue() 
						* aircraft.get_weights().get_MZFM().getEstimatedValue()
						+ aircraft.get_theFuelTank().get_fuelMass().getEstimatedValue()
						* aircraft.get_theFuelTank().get_cg().get_xBRF().getEstimatedValue())
						/ aircraft.get_weights().get_MTOM().getEstimatedValue(),
						SI.METER));

		_cgMTOM.calculateCGinMAC(
				aircraft.get_wing().get_xLEMacActualBRF(), 
				aircraft.get_wing().get_xLEMacActualBRF(), 
				Amount.valueOf(0., SI.METER), 
				aircraft.get_wing().get_meanAerodChordActual());

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