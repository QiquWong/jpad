package sandbox.mr;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;


import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ConditionEnum;
import standaloneutils.customdata.CenterOfGravity;

public class ACStabilityManager {

	// VARIABLE DECLARATION--------------------------------------
	
	OperatingConditions theOperatingConditions = new OperatingConditions();
	CenterOfGravity centerOfGravity = new CenterOfGravity();
	Amount<Length> maxXaftCenterOfGravityBRF;
	Amount<Length> maxXforwCenterOfGravityBRF;
	
	Aircraft aircraft;
	LiftingSurface theWing;
	Fuselage theFuselage;
	LiftingSurface theHTail;
	
	// BUILDER--------------------------------------
	
	public ACStabilityManager(Aircraft theAircraft,Amount<Angle> alphaBody, ConditionEnum theCondition, boolean plotCheck){

		this.aircraft = theAircraft;
		this.theWing = aircraft.get_wing();
		this.theFuselage = aircraft.get_fuselage();
		this.theHTail = aircraft.get_HTail();
		
		//Set Operating Conditions and CG position 

		switch (theCondition) {
		case TAKE_OFF:
			if (theAircraft.get_typeVehicle() == AircraftTypeEnum.TURBOPROP)
				theOperatingConditions.set_machCurrent(0.2);
			if (theAircraft.get_typeVehicle() == AircraftTypeEnum.JET)
				theOperatingConditions.set_machCurrent(0.3);
			break;

		case LANDING:	

			if (theAircraft.get_typeVehicle() == AircraftTypeEnum.TURBOPROP)
				theOperatingConditions.set_machCurrent(0.2);
			if (theAircraft.get_typeVehicle() == AircraftTypeEnum.JET)
				theOperatingConditions.set_machCurrent(0.3);
			break;

		case CRUISE:	

			if (theAircraft.get_typeVehicle() == AircraftTypeEnum.TURBOPROP)
				theOperatingConditions.set_machCurrent(0.45);
			if (theAircraft.get_typeVehicle() == AircraftTypeEnum.JET)
				theOperatingConditions.set_machCurrent(0.75);
			break;
		}
		
		theOperatingConditions.calculate();
		
		theWing.getAerodynamics().setTheOperatingConditions(theOperatingConditions);
		theWing.getAerodynamics().initializeDataFromOperatingConditions(theOperatingConditions);
		theWing.getAerodynamics().initializeDependentData();
		theWing.getAerodynamics().initializeInnerCalculators();
		
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theOperatingConditions);
		
		theFuselage.getAerodynamics().set_theOperatingConditions(theOperatingConditions);
		theFuselage.getAerodynamics().initializeDependentData();
		theFuselage.getAerodynamics().initializeInnerCalculators();
		
		theHTail.getAerodynamics().setTheOperatingConditions(theOperatingConditions);
		theHTail.getAerodynamics().initializeDataFromOperatingConditions(theOperatingConditions);
		theHTail.getAerodynamics().initializeDependentData();
		theHTail.getAerodynamics().initializeInnerCalculators();
		
		
		
		// do Analysis
		
		System.out.println("------------------------------------");
		System.out.println("\nANALYSIS \n\n ");
		System.out.println("\n------------------------------------");
		theAnalysis.doAnalysis(aircraft,
				AnalysisTypeEnum.WEIGHTS,
				AnalysisTypeEnum.BALANCE
				);
		
		switch (theCondition) {
		case TAKE_OFF:
			centerOfGravity = theAircraft.get_theBalance().get_cgMTOM();
			break;

		case LANDING:	
			centerOfGravity = theAircraft.get_theBalance().get_cgMZFM();
			break;

		case CRUISE:	
			CenterOfGravity centerOfGravityTempMTOM = theAircraft.get_theBalance().get_cgMTOM();
			CenterOfGravity centerOfGravityTempMZFM =  theAircraft.get_theBalance().get_cgMZFM();
			
			Amount<Length> x0 = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_x0().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_x0().getEstimatedValue())/2),centerOfGravityTempMTOM.get_x0().getUnit()) ;
			Amount<Length> y0 = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_y0().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_y0().getEstimatedValue())/2),centerOfGravityTempMTOM.get_y0().getUnit()) ;
			Amount<Length> z0 = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_z0().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_z0().getEstimatedValue())/2),centerOfGravityTempMTOM.get_z0().getUnit()) ;

			Amount<Length> xL = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_xLRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_xLRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_xLRF().getUnit()) ;
			Amount<Length> yL =  Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_yLRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_yLRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_yLRF().getUnit()) ;
			Amount<Length> zL = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_zLRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_zLRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_zLRF().getUnit()) ;

			Amount<Length> xB  = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_xBRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_xBRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_xBRF().getUnit()) ;
			Amount<Length> yB =  Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_yBRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_yBRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_yBRF().getUnit()) ;
			Amount<Length> zB = Amount.valueOf(Math.abs((centerOfGravityTempMTOM.get_zBRF().getEstimatedValue()
					+ centerOfGravityTempMZFM.get_zBRF().getEstimatedValue())/2),centerOfGravityTempMTOM.get_zBRF().getUnit()) ;
			
			centerOfGravity = new CenterOfGravity(x0, y0 , z0 , xL, yL, zL, xB, yB, zB);
			centerOfGravity.calculateCGinBRF();
			
			break;
		}
		
		maxXaftCenterOfGravityBRF = Amount.valueOf((centerOfGravity.get_xBRF().getEstimatedValue()*(1+0.1)), centerOfGravity.get_xBRF().getUnit());
		maxXforwCenterOfGravityBRF = Amount.valueOf((centerOfGravity.get_xBRF().getEstimatedValue()*(1-0.1)), centerOfGravity.get_xBRF().getUnit());
		
		
	}
	
	public void CalculateAll(){
		// CL --> need to consider flap contributes
		
		//CL, CD, CM... 
	}
}
