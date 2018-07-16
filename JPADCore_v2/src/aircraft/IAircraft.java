package aircraft;

import java.util.List;

import javax.annotation.Nullable;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.FuelTank;
import aircraft.components.LandingGears;
import aircraft.components.Systems;
import aircraft.components.cabinconfiguration.CabinConfiguration;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.PowerPlant;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.PrimaryElectricSystemsEnum;
import configuration.enumerations.RegulationsEnum;

@FreeBuilder
public interface IAircraft {

	String getId();
	AircraftTypeEnum getTypeVehicle(); 
	RegulationsEnum getRegulations();
	PrimaryElectricSystemsEnum getPrimaryElectricSystemsType();
	
	Fuselage getFuselage();
	Amount<Length> getXApexFuselage();
	Amount<Length> getYApexFuselage();
	Amount<Length> getZApexFuselage();
	
	LiftingSurface getWing();
	Amount<Length> getXApexWing();
	Amount<Length> getYApexWing();
	Amount<Length> getZApexWing();
	Amount<Angle> getRiggingAngleWing();
	
	LiftingSurface getHTail();
	Amount<Length> getXApexHTail();
	Amount<Length> getYApexHTail();
	Amount<Length> getZApexHTail();
	Amount<Angle> getRiggingAngleHTail();
	
	LiftingSurface getVTail();
	Amount<Length> getXApexVTail();
	Amount<Length> getYApexVTail();
	Amount<Length> getZApexVTail();
	Amount<Angle> getRiggingAngleVTail();
	
	@Nullable
	LiftingSurface getCanard();
	@Nullable
	Amount<Length> getXApexCanard();
	@Nullable
	Amount<Length> getYApexCanard();
	@Nullable
	Amount<Length> getZApexCanard();
	@Nullable
	Amount<Angle> getRiggingAngleCanard();
	
	FuelTank getFuelTank();
	Amount<Length> getXApexFuelTank();
	Amount<Length> getYApexFuelTank();
	Amount<Length> getZApexFuelTank();
	
	PowerPlant getPowerPlant();
	Nacelles getNacelles();
	
	LandingGears getLandingGears();
	Amount<Length> getXApexNoseGear();
	Amount<Length> getYApexNoseGear();
	Amount<Length> getZApexNoseGear();
	Amount<Length> getXApexMainGear();
	Amount<Length> getYApexMainGear();
	Amount<Length> getZApexMainGear();
	LandingGearsMountingPositionEnum getLandingGearsMountingPositionEnum();
	
	Systems getSystems();
	CabinConfiguration getCabinConfiguration();
	
	List<Object> getComponentList();
	
	class Builder extends IAircraft_Builder  { 
		 public Builder() {
			 
		 }
	}
	
}
