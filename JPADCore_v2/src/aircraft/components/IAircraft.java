package aircraft.components;

import java.util.List;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.PowerPlant;
import analyses.ACAnalysisManager;
import configuration.enumerations.AeroConfigurationTypeEnum;
import configuration.enumerations.AircraftTypeEnum;

public interface IAircraft {

	public Fuselage getFuselage();
	public void setFuselage(Fuselage fuselage);
	
	public LiftingSurface getWing();
	public void setWing(LiftingSurface wing);
	
	public LiftingSurface getExposedWing();
	public void setExposedWing(LiftingSurface exposedWing);
	
	public LiftingSurface getHTail();
	public void setHTail(LiftingSurface hTail);
	
	public LiftingSurface getVTail();
	public void setVTail(LiftingSurface vTail);
	
	public LiftingSurface getCanard();
	public void setCanard(LiftingSurface canard);
	
	public PowerPlant getPowerPlant();
	public void setPowerPlant(PowerPlant powerPlant);
	
	public Nacelles getNacelles();
	public void setNacelles(Nacelles nacelles);

	public FuelTank getFuelTank();
	public void setFuelTank(FuelTank fuelTank);
	
	public LandingGears getLandingGears();
	public void setLandingGears(LandingGears landingGears);
	
	public Systems getSystems();
	public void setSystems(Systems systems);
	
	public void deleteFuselage();
	public void deleteWing();
	public void deleteExposedWing();
	public void deleteHTail();
	public void deleteVTail();
	public void deleteCanard();	
	public void deletePowerPlant();	
	public void deleteNacelles();	
	public void deleteFuelTank();
	public void deleteLandingGears();
	public void deleteSystems();
	
	public ACAnalysisManager getTheAnalysisManager();
	public void setTheAnalysisManager(ACAnalysisManager theAnalysisManager);

	public CabinConfiguration getCabinConfiguration();
	public void setCabinConfiguration(CabinConfiguration theCabinConfiguration);
	
	public AircraftTypeEnum getTypeVehicle(); 
	public void setTypeVehicle(AircraftTypeEnum _typeVehicle); 

	public String getId();
	public void setId(String _name);

	public AeroConfigurationTypeEnum getType();

	public double getLifeSpan();
	public void setLifeSpan(double _lifeSpan);
	
	public List<Object> getComponentsList();
	
	public Amount<Length> getWingACToCGDistance();
	public void setWingACToCGDistance(Amount<Length> _wingACToCGDistance);
	
}
