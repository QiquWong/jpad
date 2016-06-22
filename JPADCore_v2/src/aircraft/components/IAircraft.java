package aircraft.components;

import java.util.List;

import aircraft.calculators.ACAerodynamicsManager;
import aircraft.calculators.ACBalanceManager;
import aircraft.calculators.ACPerformanceManager;
import aircraft.calculators.ACStructuralCalculatorManager;
import aircraft.calculators.ACWeightsManager;
import aircraft.calculators.costs.Costs;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacellesManager;
import aircraft.components.powerPlant.PowerPlant;
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
	
	public NacellesManager getNacelles();
	public void setNacelles(NacellesManager nacelles);

	public FuelTanks getFuelTank();
	public void setFuelTank(FuelTanks fuelTank);
	
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
	
	public ACAerodynamicsManager getTheAerodynamics();
	public void setTheAerodynamics(ACAerodynamicsManager theAerodynamics);

	public ACStructuralCalculatorManager getTheStructures();
	public void setTheStructures(ACStructuralCalculatorManager theStructures);

	public ACPerformanceManager getThePerformance();
	public void setThePerformance(ACPerformanceManager thePerformance);
	
	public ACWeightsManager getTheWeights();
	public void setTheWieghts(ACWeightsManager theWeights);
	
	public ACBalanceManager getTheBalance();
	public void setTheBalance(ACBalanceManager theBalance);
	
	public Costs getTheCosts();
	public void setTheCosts(Costs theCosts);

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
	
	
}
