package aircraft.components;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

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
	
	LiftingSurface getCanard();
	Amount<Length> getXApexCanard();
	Amount<Length> getYApexCanard();
	Amount<Length> getZApexCanard();
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
	
	class Builder extends IAircraft_Builder  { 
		 public Builder() {
			 
		 }
	}
	
}
