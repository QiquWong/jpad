package aircraft.componentmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;

public abstract class Component {

	private ComponentEnum _type;

	// Name
	protected String _name = "";
	protected String _description = "";
	public final String id = null;

	// Origin in aircraft construction frame (ACF)
	private Amount<Length> _X0=Amount.valueOf(0.0, SI.METER);
	private Amount<Length> _Y0=Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _Z0=Amount.valueOf(0.0, SI.METER);
	protected CenterOfGravity _cg;

	Amount<Length> _span = null, _semispan = null;
	Amount<Length> _roughness;

	Amount<Area> _surface = null;
	Amount<Area> _surfaceExposed = null;
	Amount<Area> _surfaceWetted = null;
	Amount<Area> _surfaceWettedExposed = null;

	Amount<Volume> _volume = null;
	Amount<Volume> _volumeExposed = null;


	private Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();

	public Component(String name) {
		_name = name;
	}

	public Component(String name, String description) {
		_name = name;
		_description = description;
	}

	public Component(
			String id, String name, 
			String description, double x, double y, double z) {

		this(name, description);
		_X0 = Amount.valueOf(x, SI.METER);
		_Y0 = Amount.valueOf(y, SI.METER);
		_Z0 = Amount.valueOf(z, SI.METER);
		_cg = new CenterOfGravity();
	}

	/** This method MUST be overridden by MyComponent subclasses
	 * 
	 * @return
	 */
	public Map<AnalysisTypeEnum, List<MethodEnum>> get_methodsMap() {
		return _methodsMap;
	}


	/** This method MUST be overridden by MyComponent subclasses
	 * 
	 * @param aircraft
	 * @param conditions
	 * @param method
	 */
	public void calculateMass(Aircraft aircraft,
			OperatingConditions conditions, MethodEnum method) {
		System.out.println("WARNING: this method MUST be overridden by MyComponent subclasses;"
				+ "it should not be used by MyComponent instances");
	}

	/** 
	 * This method MUST be overridden by MyComponent subclasses
	 * 
	 * @param aircraft
	 * @param method
	 */
	public void calculateMass(Aircraft aircraft, MethodEnum method) {

	}

	public void calculateMass(Aircraft aircraft,
			OperatingConditions conditions, MethodEnum ... method) {
	}

	/** 
	 * This method MUST be overridden by MyComponent subclasses.
	 * The CG is always evaluated in the LRF
	 * 
	 * @param aircraft
	 * @param conditions
	 * @param myMethodEnum
	 */
	public void calculateCG(
			Aircraft aircraft, 
			OperatingConditions conditions,
			MethodEnum myMethodEnum) {
		System.out.println("WARNING: this method MUST be overridden by MyComponent subclasses;"
				+ "it should not be used by MyComponent instances");
	}

	public void calculateCLalpha(Aircraft aircraft, OperatingConditions conditions,
			MethodEnum method) {
		System.out.println("WARNING: this method MUST be overridden by MyComponent subclasses;"
				+ "it should not be used by MyComponent instances");		
	}

	public ComponentEnum getType() {
		System.out.println("WARNING: this method MUST be overridden by MyComponent subclasses;"
				+ "it should not be used by MyComponent instances");
		return _type;
	}

	public void setType(ComponentEnum _type) {
		System.out.println("WARNING: this method MUST be overridden by MyComponent subclasses;"
				+ "it should not be used by MyComponent instances");
		this._type = _type;
	}

	public static String getId() {
		return null;
	}

	public void calculateMass() {
		// TODO Auto-generated method stub

	}

	public String getName() { 
		return _name; 
	}

	public void setName(String name) {
		_name = name; 
	}

	public String getDescription() { 
		return _description; 
	}

	void setDescription(String descr) {
		_description = descr; 
	}

	public Amount<Length> get_X0() { 
		return _X0; 
	}

	public void setX0(double x) { 
		_X0 = Amount.valueOf(x, SI.METER); 
	}

	public void set_X0(Amount<Length> x) {
		_X0 = x;
	}

	public Amount<Length> get_Y0() {
		return _Y0; 
	}

	public void setY0(double y) {
		_Y0 = Amount.valueOf(y, SI.METER); 
	}

	public void set_Y0(Amount<Length> y) {
		_Y0 = y; 
	}

	public Amount<Length> get_Z0() {
		return _Z0;
	}

	public void setZ0(double z) {
		_Z0 = Amount.valueOf(z, SI.METER); 
	}

	public void set_Z0(Amount<Length> z) {
		_Z0 = z; 
	}

	public CenterOfGravity get_cg() {
		return _cg;
	}

	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}

}
