package sandbox.mr;


import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import calculators.aerodynamics.LiftCalc;
import configuration.MyConfiguration;

import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.FuelFractionDatabaseReader;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.customdata.CenterOfGravity;

// This class testing the class DownwashCalculator using a default aircraft.

public class Test_MR_03_DownwashAircraft {

	public static void main(String[] args) {
		
			System.out.println("Downwash gradient test calculator");
			System.out.println("--------------------------------------------------------");

			// Assign all default folders
			MyConfiguration.initWorkingDirectoryTree();

			// Setup database	
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			
			// Operating Condition / Aircraft / AnalysisManager (geometry calculations)
			OperatingConditions theCondition = new OperatingConditions();
			Aircraft aircraft = Aircraft.createDefaultAircraft();
			aircraft.get_theAerodynamics().set_aerodynamicDatabaseReader(aeroDatabaseReader);
			aircraft.set_name("ATR-72");

			ACAnalysisManager theAnalysis = new ACAnalysisManager(theCondition);
			theAnalysis.updateGeometry(aircraft);
			
		
//			theAnalysis.doAnalysis(aircraft, 
//					AnalysisTypeEnum.AERODYNAMIC, 
//					AnalysisTypeEnum.BALANCE,
//					AnalysisTypeEnum.WEIGHTS,
//					AnalysisTypeEnum.PERFORMANCES, 
//					AnalysisTypeEnum.COSTS
//					);

			aircraft.get_HTail().calculateGeometry();
			aircraft.get_HTail().calculateACwACdistance(aircraft);
			

			// --------------------------------------------------------
			//DATA
			
			//Distance between aerodynamic center of wing and c/4 point of the h tail root chord
			Amount<Length> xCoordinateHTail = aircraft.get_HTail().get_X0();
			System.out.println("x le tail " + xCoordinateHTail);
			Amount<Length> hTailRootChord = aircraft.get_HTail().get_chordRoot();
			System.out.println("h root chord " + hTailRootChord);
			Amount<Length> xCoordinateMAC = aircraft.get_wing().get_xLEMacActualBRF();
			System.out.println("x mac wing " + xCoordinateMAC);
			Amount<Length> meanAerodynamicChord = aircraft.get_wing().get_meanAerodChordActual();
			System.out.println("mac " +  meanAerodynamicChord);
			double distAerodynamicCenter = (xCoordinateHTail.getEstimatedValue()-(xCoordinateMAC.getEstimatedValue()+
					0.25*meanAerodynamicChord.getEstimatedValue())+0.25*hTailRootChord.getEstimatedValue() ); 		
			
			
			//Distance between the AC of the wing and the horizontal tail.
			Amount<Length> acDistanceHtailWing = aircraft.get_HTail().get_ACw_ACdistance();
			double acDistanceDouble = Double.valueOf(acDistanceHtailWing.getEstimatedValue());
			
			
			//Distance between the horizontal tail and the wing root chord
			Amount<Length> zCoordinateWing  = aircraft.get_wing().get_Z0();
			System.out.println("z wing " + zCoordinateWing);
			Amount<Length> ZCoordinateHTail = aircraft.get_HTail().get_Z0();
			System.out.println("z htail " + ZCoordinateHTail);
			double distWingToHTail= -zCoordinateWing.getEstimatedValue()+ZCoordinateHTail.getEstimatedValue();
			
			
			//CL_alfa
			double clAlfaIntegralDoubleCalc = LiftCalc.calcCLatAlphaLinearDLR( 
					theCondition.get_alphaCurrent().getEstimatedValue(),
					aircraft.get_wing().get_aspectRatio()
					);
			
			double clAlfaPhol = AeroLibraryCalculator.calculateCLalfaPolhamus(
					aircraft.get_wing().get_aspectRatio(),theCondition.get_machCurrent(), 
					Math.toRadians(Double.valueOf(aircraft.get_wing().get_sweepLEEquivalent().getEstimatedValue())),
					aircraft.get_wing().get_taperRatioEquivalent());
			
			double clAlfa=6.0; // report value
	
			
			
			
			//Print Data 
			
			System.out.println("Distance between aerodynamic center of wing and c/4 point of the h tail root chord [m] --> " + distAerodynamicCenter);
			System.out.println("Distance between aerodynamic center of wing and h tail [m] --> " + acDistanceDouble); 
			System.out.println("Distance among z axis between root chord of wing and H tail [m] --> "+ distWingToHTail);
			System.out.println("CL Alfa [1/rad] --> " + clAlfa); 
			System.out.println("sweep angle " +Double.valueOf(aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue()));
			System.out.println("Wing Span [m] --> " + Double.valueOf(aircraft.get_wing().get_span().getEstimatedValue()));
	
			
		
			// Creating the Calculator Object	
			System.out.println("\n Start calculating Downwash gradiendt ... \n ");
			
			DownwashCalculator test = new DownwashCalculator(aircraft.get_wing().get_aspectRatio(), 
					aircraft.get_wing().get_taperRatioActual());
	
			double downwashDatcom=test.calculateDownwashDatcom(distAerodynamicCenter, distWingToHTail,
																Double.valueOf(aircraft.get_wing().get_span().getEstimatedValue()),
															    Double.valueOf(aircraft.get_wing().get_sweepHalfChordEq().getEstimatedValue()));
			double downwashDelft=test.calculateDownwashDelft(acDistanceDouble, distWingToHTail,
																clAlfa,Double.valueOf(aircraft.get_wing().get_span().getEstimatedValue()),
																Double.valueOf(aircraft.get_wing().get_sweepHalfChordEq().getEstimatedValue()));
			
			
			System.out.println("The value of downwash gradient, calculate with Datcom Method is: --> " + downwashDatcom);
			System.out.println("The value of downwash gradient, calculate with Delft Method  is: --> " + downwashDelft);

	}

}
