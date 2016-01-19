package sandbox.mr;

import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.customdata.CenterOfGravity;

public class Test_MR_Wing_06bis {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		// -----------------------------------------------------------------------
				// Generate default Wing
				// -----------------------------------------------------------------------

				// Fuselage
				Fuselage theFuselage = new Fuselage(
						"Fuselage", // name
						"Data from AC_ATR_72_REV05.pdf", // description
						0.0, 0.0, 0.0 // Fuselage apex (x,y,z)-coordinates in construction axes
						);

				// Wing
				double xAw = 11.0; //meter 
				double yAw = 0.0;
				double zAw = 1.6;
				double iw = 0.0;
				LiftingSurface theWing = new LiftingSurface(
						"Wing", // name
						"Data from AC_ATR_72_REV05.pdf", 
						xAw, yAw, zAw, iw, 
						ComponentEnum.WING,
						theFuselage // let her see the fuselage
						); 

				theWing.calculateGeometry();
				theWing.getGeometry().calculateAll();

				// Center of Gravity
				double xCgLocal= 1.5; // meter 
				double yCgLocal= 0;
				double zCgLocal= 0;

				CenterOfGravity cg = new CenterOfGravity(
						Amount.valueOf(xCgLocal, SI.METER), // coordinates in LRF
						Amount.valueOf(yCgLocal, SI.METER),
						Amount.valueOf(zCgLocal, SI.METER),
						Amount.valueOf(xAw, SI.METER), // origin of LRF in BRF 
						Amount.valueOf(yAw, SI.METER),
						Amount.valueOf(zAw, SI.METER),
						Amount.valueOf(0.0, SI.METER),// origin of BRF
						Amount.valueOf(0.0, SI.METER),
						Amount.valueOf(0.0, SI.METER)
						);

				cg.calculateCGinBRF();
				theWing.set_cg(cg);

				// Default operating conditions
				OperatingConditions theOperatingConditions = new OperatingConditions();				

				System.out.println("\n \n-----------------------------------------------------");
				System.out.println("Operating condition");
				System.out.println("-----------------------------------------------------");
				System.out.println("\tMach: " + theOperatingConditions.get_machCurrent());
				System.out.println("\tAltitude: " + theOperatingConditions.get_altitude());
				System.out.println("----------------------");


				// allocate manager
				LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager ( 
						theOperatingConditions,
						theWing
						);


				theWing.setAerodynamics(theLSAnalysis);

				// Assign all default folders
				MyConfiguration.initWorkingDirectoryTree();

				// Setup database(s)	
				String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
				String databaseFileName = "Aerodynamic_Database_Ultimate.h5";
				String highLiftDatabaseFileName = "HighLiftDatabase.h5";
				AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath, databaseFileName);
				HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
				
				theLSAnalysis.set_AerodynamicDatabaseReader(aeroDatabaseReader);
				theLSAnalysis.set_highLiftDatabaseReader(highLiftDatabaseReader);
				
				theWing.getAerodynamics().set_AerodynamicDatabaseReader(aeroDatabaseReader);
				theWing.getAerodynamics().set_highLiftDatabaseReader(highLiftDatabaseReader);
				
				// -----------------------------------------------------------------------
				// Define airfoil
				// -----------------------------------------------------------------------

				System.out.println("\n \n-----------------------------------------------------");
				System.out.println("AIRFOIL");
				System.out.println("-----------------------------------------------------");



				//AIRFOIL 1
				double yLocRoot = 0.0;
				MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot, "23-018");
				airfoilRoot.getGeometry().update(yLocRoot);  // define chord
				airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
				airfoilRoot.getGeometry().set_deltaYPercent(0.192 *airfoilRoot.getGeometry().get_maximumThicknessOverChord()*100 );
				System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRoot.get_family());
				System.out.println("Root Chord " + theWing.get_chordRoot().getEstimatedValue() );
				System.out.println("Root maximum thickness " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
				System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());		
				System.out.println("LE sharpness parameter Root " + airfoilRoot.getGeometry().get_deltaYPercent());
				
				
				
				//AIRFOIL 2
				double yLocKink = theWing.get_spanStationKink() * theWing.get_semispan().getEstimatedValue();
				MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink, "23-015");
				airfoilKink.getGeometry().update(yLocKink);   // define chord
				airfoilKink.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
				airfoilKink.getGeometry().set_deltaYPercent(0.192 *airfoilKink.getGeometry().get_maximumThicknessOverChord()*100 );
				System.out.println("\n \n \t KINK \nAirfoil Type: " + airfoilKink.get_family());
				System.out.println("Kink Station " + yLocKink);
				System.out.println("Kink Chord " + theWing.get_chordKink().getEstimatedValue() );
				System.out.println("Kink maximum thickness " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
				System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
				System.out.println("LE sharpness parameter Kink " + airfoilKink.getGeometry().get_deltaYPercent());
				

				
				//AIRFOIL 3
				double yLocTip = theWing.get_semispan().getEstimatedValue();
				MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip, "23-012");
				airfoilTip.getGeometry().update(yLocRoot);  // define chord
				airfoilTip.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT

				airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
				System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
				System.out.println("tip Chord " +theWing.get_chordTip().getEstimatedValue() );
				System.out.println("Tip maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
				System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
				System.out.println("LE sharpness parameter Tip " + airfoilTip.getGeometry().get_deltaYPercent());



				// -----------------------------------------------------------------------
				// Assign airfoil
				// -----------------------------------------------------------------------


				List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
				myAirfoilList.add(0, airfoilRoot);
				myAirfoilList.add(1, airfoilKink);
				myAirfoilList.add(2, airfoilTip);
				theWing.set_theAirfoilsList(myAirfoilList);
				theWing.updateAirfoilsGeometry(); 
				
				
				
				
				// -----------------------------------------------------------------------
				// Calculate CL 
				// -----------------------------------------------------------------------

				System.out.println("---------------------------");
				System.out.println("\nEvaluating CL "); 
				System.out.println("\n---------------------------");
				
				Amount<Angle> alpha = Amount.valueOf(toRadians(14.), SI.RADIAN);
				LSAerodynamicsManager.CalcCLAtAlpha theCLCalculator = theLSAnalysis.new CalcCLAtAlpha();
				double CL = theCLCalculator.nasaBlackwellCompleteCurve(alpha);
				
				System.out.println(" At alpha " + alpha.to(NonSI.DEGREE_ANGLE) + " CL = " + CL);
				
				theLSAnalysis.PlotCLvsAlphaCurve();
				
				System.out.println("\nDONE "); 
				
				//----------------------------------------------------------------------------------
				// High Lift Devices Test
				List<Double[]> deltaFlap = new ArrayList<Double[]>();
				List<Double> flapType = new ArrayList<Double>();
				List<Double> eta_in_flap = new ArrayList<Double>();
				List<Double> eta_out_flap = new ArrayList<Double>();
				List<Double> cf_c = new ArrayList<Double>();

				// FLAP 1
				// flapType from 1.0 to 6.0 for the required flap type:
				//		  1 = Single Slotted Flap
				//		  2 = Double Slotted Flap
				//		  3 = Split Flap
				//		  4 = Plain Flap
				//		  5 = Fowler Flap
				//		  6 = Triple Slotted Flap
				flapType.add(1.0);
				cf_c.add(0.1711);
				deltaFlap.add(new Double[] {20.0});
				eta_in_flap.add(0.098);
				eta_out_flap.add(0.39072039072039072039072039072039);

				// FLAP 2 
				// flapType from 1.0 to 6.0 for the required flap type:
				//		  1 = Single Slotted Flap
				//		  2 = Double Slotted Flap
				//		  3 = Split Flap
				//		  4 = Plain Flap
				//		  5 = Fowler Flap
				//		  6 = Triple Slotted Flap
				flapType.add(1.0);
				cf_c.add(0.1711);
				deltaFlap.add(new Double[] {20.0});
				eta_in_flap.add(0.39072039072039072039072039072039);
				eta_out_flap.add(0.75);

				LSAerodynamicsManager.CalcHighLiftDevices highLiftCalculator = theLSAnalysis
						.new CalcHighLiftDevices(
								theWing,
								deltaFlap,
								flapType,
								null,
								eta_in_flap,
								eta_out_flap,
								null,
								null,
								cf_c,
								null,
								null,
								null
								);
//				CalcHighLiftDevices highLiftCalculator = new CalcHighLiftDevices(
//						aircraft,
//						deltaFlap,
//						flapType,
//						null,
//						eta_in_flap,
//						eta_out_flap,
//						null,
//						null,
//						cf_c,
//						null,
//						null,
//						null
//						);

				highLiftCalculator.calculateHighLiftDevicesEffects();

				//----------------------------------------------------------------------------------
				// Results print
				System.out.println("\ndeltaCl0_flap_list = ");
				for(int i=0; i<highLiftCalculator.getDeltaCl0_flap_list().size(); i++)
					System.out.print(highLiftCalculator.getDeltaCl0_flap_list().get(i) + " ");

				System.out.println("\n\ndeltaCl0_flap = \n" + highLiftCalculator.getDeltaCl0_flap());

				System.out.println("\n\ndeltaCL0_flap_list = ");
				for(int i=0; i<highLiftCalculator.getDeltaCL0_flap_list().size(); i++)
					System.out.print(highLiftCalculator.getDeltaCL0_flap_list().get(i) + " ");

				System.out.println("\n\ndeltaCL0_flap = \n" + highLiftCalculator.getDeltaCL0_flap());

				System.out.println("\n\ndeltaClmax_flap_list = ");
				for(int i=0; i<highLiftCalculator.getDeltaClmax_flap_list().size(); i++)
					System.out.print(highLiftCalculator.getDeltaClmax_flap_list().get(i) + " ");

				System.out.println("\n\ndeltaClmax_flap = \n" + highLiftCalculator.getDeltaClmax_flap());

				System.out.println("\n\ndeltaCLmax_flap_list = ");
				for(int i=0; i<highLiftCalculator.getDeltaCLmax_flap_list().size(); i++)
					System.out.print(highLiftCalculator.getDeltaCLmax_flap_list().get(i) + " ");

				System.out.println("\n\ndeltaCLmax_flap = \n" + highLiftCalculator.getDeltaCLmax_flap());

				System.out.println("\n\ncLalpha_new_list = ");
				for(int i=0; i<highLiftCalculator.getcLalpha_new_list().size(); i++)
					System.out.print(highLiftCalculator.getcLalpha_new_list().get(i) + " ");

				System.out.println("\n\ncLalpha_new = \n" + highLiftCalculator.getcLalpha_new());

				System.out.println("\n\ndeltaAlphaMax_list = ");
				for(int i=0; i<highLiftCalculator.getDeltaAlphaMax_list().size(); i++)
					System.out.print(highLiftCalculator.getDeltaAlphaMax_list().get(i) + " ");
				
				System.out.println("\n\ndeltaAlphaMax = \n" + highLiftCalculator.getDeltaAlphaMax());
				
				System.out.println("\n\ndeltaCD_list = ");
				for(int i=0; i<highLiftCalculator.getDeltaCD_list().size(); i++)
					System.out.print(highLiftCalculator.getDeltaCD_list().get(i) + " ");

				System.out.println("\n\ndeltaCD = \n" + highLiftCalculator.getDeltaCD());
				
				//--------------------------
				// New lift curve 
				
				
				//Amount<Angle> alpha = Amount.valueOf(toRadians(8.), SI.RADIAN);
				double cLHighLift = theCLCalculator.highLiftDevice(
						alpha,
						deltaFlap,
						flapType,
						eta_in_flap, 
						eta_out_flap, 
						cf_c,
						null,
						null, 
						null,
						null, 
						null, 
						null);
				
				System.out.println("\n\nCL flap = " + cLHighLift);
				
				theLSAnalysis.PlotHighLiftCurve(
						deltaFlap,
						flapType,
						eta_in_flap, 
						eta_out_flap, 
						cf_c,
						null,
						null, 
						null,
						null, 
						null, 
						null);
				System.out.println("DONE");
				
				
	}

}
