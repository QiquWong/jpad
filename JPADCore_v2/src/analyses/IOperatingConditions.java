package analyses;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.DynamicViscosity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.quantity.VolumetricDensity;

import org.jscience.physics.amount.Amount;

import jahuwaldt.aero.StdAtmos1976;
import standaloneutils.MyInterpolatingFunction;

public interface IOperatingConditions {

	public void calculate();
	
	public Double getMachCruise();
	public void setMachCurrent(Double _mach);

	public Double getMachClimb();
	public void setMachClimb(Double _mach);
	
	public Double getMachTransonicThreshold();
	public void setMachTransonicThreshold(Double _machTransonicThreshold);

	public Amount<Length> getAltitudeCruise();
	public void setAltitude(Amount<Length> _altitude);

	public Amount<Angle> getAlphaCurrentClimb();
	public void setAlphaCurrentClimb(Amount<Angle> _alphaCurrentClimb);
	
	public Amount<Angle> getAlphaCurrentCruise();
	public void setAlphaCurrentCruise(Amount<Angle> _alphaCurrentCruise);
	
	public Amount<Angle> getAlphaCurrentTakeOff();
	public void setAlphaCurrentTakeOff(Amount<Angle> _alphaCurrentTakeOff);
	
	public Amount<Angle> getAlphaCurrentLanding();
	public void setAlphaCurrentLanding(Amount<Angle> _alphaCurrentLanding);
	
	public Amount<Angle> getBetaCurrentClimb();
	public void setBetaCurrentClimb(Amount<Angle> _betaCurrentClimb);
	
	public Amount<Angle> getBetaCurrentCruise();
	public void setBetaCurrentCruise(Amount<Angle> _betaCurrentCruise);
	
	public Amount<Angle> getBetaCurrentTakeOff();
	public void setBetaCurrentTakeOff(Amount<Angle> _betaCurrentTakeOff);
	
	public Amount<Angle> getBetaCurrentLanding();
	public void setBetaCurrentLanding(Amount<Angle> _betaCurrentLanding);
	
	public String getId();
	public void setId(String id);
	
	public Double getThrottleCruise();
	public void setThrottleCruise(Double _throttleCruise);

	public Amount<Length> getAltitudeTakeOff();
	public void setAltitudeTakeOff(Amount<Length> _altitudeTakeOff);

	public Double getMachTakeOff();
	public void setMachTakeOff(Double _machTakeOff);
	
	public Double getThrottleTakeOff();
	public void setThrottleTakeOff(Double _throttleTakeOff);

	public MyInterpolatingFunction getThrottleGroundIdleTakeOff();
	public void setThrottleGroundIdleTakeOff(MyInterpolatingFunction _throttleGroundIdleTakeOff);
	
	public List<Amount<Angle>> getFlapDeflectionTakeOff();
	public void setFlapDeflectionTakeOff(List<Amount<Angle>> _flapDeflectionTakeOff);

	public List<Amount<Angle>> getSlatDeflectionTakeOff();
	public void setSlatDeflectionTakeOff(List<Amount<Angle>> _slatDeflectionTakeOff);
	
	public Amount<Length> getAltitudeLanding();
	public void setAltitudeLanding(Amount<Length> _altitudeLanding);

	public Double getMachLanding();
	public void setMachLanding(Double _machLanding);

	public MyInterpolatingFunction getThrottleGroundIdleLanding();
	public void setThrottleGroundIdleLanding(MyInterpolatingFunction _throttleGroundIdleLanding);
	
	public List<Amount<Angle>> getFlapDeflectionLanding();
	public void setFlapDeflectionLanding(List<Amount<Angle>> _flapDeflectionLanding);

	public List<Amount<Angle>> getSlatDeflectionLanding();
	public void setSlatDeflectionLanding(List<Amount<Angle>> _slatDeflectionLanding);
	
	public Double getPpressureCoefficientTakeOff();
	public void setPressureCoefficientTakeOff(Double _pressureCoefficientTakeOff);

	public Amount<Velocity> getTASTakeOff();
	public void setTASTakeOff(Amount<Velocity> _tasTakeOff);

	public Amount<Velocity> getCASTakeOff();
	public void setCASTakeOff(Amount<Velocity> _casTakeOff);

	public Amount<Velocity> getEASTakeOff();
	public void setEASTakeOff(Amount<Velocity> _easTakeOff);

	public Amount<VolumetricDensity> getDensityTakeOff();
	public void setDensityTakeOff(Amount<VolumetricDensity> _densityTakeOff);

	public Amount<Pressure> getStaticPressureTakeOff();
	public void setStaticPressureTakeOff(Amount<Pressure> _staticPressureTakeOff);

	public Amount<Pressure> getDynamicPressureTakeOff();
	public void setDynamicPressureTakeOff(Amount<Pressure> _dynamicPressureTakeOff);

	public Amount<Pressure> getStagnationPressureTakeOff();
	public void setStagnationPressureTakeOff(Amount<Pressure> _stagnationPressureTakeOff);
	
	public Amount<Pressure> getMaxDeltaPressureTakeOff();
	public void setMaxDeltaPressureTakeOff(Amount<Pressure> _maxDeltaPressureTakeOff);

	public Amount<Pressure> getMaxDynamicPressureTakeOff();
	public void setMaxDynamicPressureTakeOff(Amount<Pressure> _maxDynamicPressureTakeOff);

	public Amount<DynamicViscosity> getMuTakeOff();
	public void setMuTakeOff(Amount<DynamicViscosity> _muTakeOff);
	
	public Amount<Temperature> getStaticTemperatureTakeOff();
	public void setStaticTemperatureTakeOff(Amount<Temperature> _staticTemperatureTakeOff);

	public Amount<Temperature> getStagnationTemperatureTakeOff();
	public void setStagnationTemperatureTakeOff(Amount<Temperature> _stagnationTemperatureTakeOff);

	public Double getPressureCoefficientLanding();
	public void setPressureCoefficientLanding(Double _pressureCoefficientLanding);

	public Amount<Velocity> getTASLanding();
	public void setTASLanding(Amount<Velocity> _tasLanding);

	public Amount<Velocity> getCASLanding();
	public void setCASLanding(Amount<Velocity> _casLanding);

	public Amount<Velocity> getEASLanding();
	public void setEASLanding(Amount<Velocity> _easLanding);

	public Amount<VolumetricDensity> getDensityLanding();
	public void setDensityLanding(Amount<VolumetricDensity> _densityLanding);

	public Amount<Pressure> getStaticPressureLanding();
	public void setStaticPressureLanding(Amount<Pressure> _staticPressureLanding);

	public Amount<Pressure> getDynamicPressureLanding();
	public void setDynamicPressureLanding(Amount<Pressure> _dynamicPressureLanding);

	public Amount<Pressure> getStagnationPressureLanding();
	public void setStagnationPressureLanding(Amount<Pressure> _stagnationPressureLanding);

	public Amount<Pressure> getMaxDeltaPressureLanding();
	public void setMaxDeltaPressureLanding(Amount<Pressure> _maxDeltaPressureLanding);

	public Amount<Pressure> getMaxDynamicPressureLanding();
	public void setMaxDynamicPressureLanding(Amount<Pressure> _maxDynamicPressureLanding);

	public Amount<DynamicViscosity> getMuLanding();
	public void setMuLanding(Amount<DynamicViscosity> _muLanding);

	public Amount<Temperature> getStaticTemperatureLanding();
	public void setStaticTemperatureLanding(Amount<Temperature> _staticTemperatureLanding);

	public Amount<Temperature> getStagnationTemperatureLanding();
	public void setStagnationTemperatureLanding(Amount<Temperature> _stagnationTemperatureLanding);

	public Double getPressureCoefficientCruise();
	public void setPressureCoefficientCruise(Double _pressureCoefficientCruise);

	public Amount<Velocity> getTASCruise();
	public void setTASCruise(Amount<Velocity> _tasCruise);

	public Amount<Velocity> getCASCruise();
	public void setCASCruise(Amount<Velocity> _casCruise);

	public Amount<Velocity> getEASCruise();
	public void setEASCruise(Amount<Velocity> _easCruise);

	public Amount<VolumetricDensity> getDensityCruise();
	public void setDensityCruise(Amount<VolumetricDensity> _densityCruise);

	public Amount<Pressure> getStaticPressureCruise();
	public void setStaticPressureCruise(Amount<Pressure> _staticPressureCruise);
	
	public Amount<Pressure> getDynamicPressureCruise();
	public void setDynamicPressureCruise(Amount<Pressure> _dynamicPressureCruise);

	public Amount<Pressure> getStagnationPressureCruise();
	public void setStagnationPressureCruise(Amount<Pressure> _stagnationPressureCruise);

	public Amount<Pressure> getMaxDeltaPressureCruise();
	public void setMaxDeltaPressureCruise(Amount<Pressure> _maxDeltaPressureCruise);
	
	public Amount<Pressure> getMaxDynamicPressureCruise();
	public void setMaxDynamicPressureCruise(Amount<Pressure> _maxDynamicPressureCruise);

	public Amount<DynamicViscosity> getMuCruise();
	public void setMuCruise(Amount<DynamicViscosity> _muCruise);

	public Amount<Temperature> getStaticTemperatureCruise();
	public void setStaticTemperatureCruise(Amount<Temperature> _staticTemperatureCruise);

	public Amount<Temperature> getStagnationTemperatureCruise();
	public void setStagnationTemperatureCruise(Amount<Temperature> _stagnationTemperatureCruise);

	public StdAtmos1976 getAtmosphereCruise();
	public void setAtmosphereCruise(StdAtmos1976 _atmoshpereCruise);

	public StdAtmos1976 getAtmosphereTakeOff();
	public void setAtmosphereTakeOff(StdAtmos1976 _atmoshpereTakeOff);

	public StdAtmos1976 getAtmosphereLanding();
	public void setAtmosphereLanding(StdAtmos1976 _atmoshpereLanding);
}
