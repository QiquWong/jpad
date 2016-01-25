// This is the Test class for Longitudinal Stability. The object of analysis is an aircraft.
//
// The reference angle of attack is alphaBody, that is the angle between the direction of asimptotic 
// velocity and the reference line of fuselage. So, for each component is necessary to evaluate the
// aerodynamic characteristics, such as lift, drag and moment, having alphaBody as input.
// Moreover for each component are drawn the aerodynamic curves in function of local angle of attack.

// alphaWing = alphaBody + iWing
// The angle of incidence is defined as the angle between the chord line and a reference line of the fuselage
// alphaHorizontal = alphaBody - downwashAngle +iHorizontal

// So alphaBody is the input data, iWing and iHorizontal are geometry data and downwashAngle must be calculated


package sandbox.mr;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.FusAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javafx.util.Pair;
import writers.JPADStaticWriteUtils;

public class Test_MR_07_LongitudinalStability {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {

		System.out.println("------------------------------------");
		System.out.println("\n Longitudinal Stability Test ");
		System.out.println("\n------------------------------------");

		// -----------------------------------------------------------------------
		// INITIALIZE TEST CLASS
		// -----------------------------------------------------------------------


		System.out.println("\nInitializing test class...");
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Longitudinal_Static_Stability" + File.separator);

		//----------------------------------------------------------------------------------
		// Default folders creation:
		MyConfiguration.initWorkingDirectoryTree();


		//------------------------------------------------------------------------------------
		// Operating Condition 
		OperatingConditions theConditions = new OperatingConditions();
		theConditions.set_alphaCurrent(Amount.valueOf(toRadians(2.), SI.RADIAN));


		//------------------------------------------------------------------------------------
		// Default Aircraft 
		Aircraft aircraft = Aircraft.createDefaultAircraft("ATR-72");
		aircraft.set_name("ATR-72");
		System.out.println("\nDefault aircraft: " + aircraft.get_name() + "\n");

		//------------------------------------------------------------------------------------
		// Wing
		LiftingSurface theWing = aircraft.get_wing();


		//--------------------------------------------------------------------------------------
		// Aerodynamic managers
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theConditions);
		theAnalysis.updateGeometry(aircraft);

		LSAerodynamicsManager theLSAnalysis = new LSAerodynamicsManager(
				theConditions, 
				theWing,
				aircraft
				); 

		
		aircraft.get_wing().setAerodynamics(theLSAnalysis);

		//--------------------------------------------------------------------------------------
		// Set databases


		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC, 
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);		



		//--------------------------------------------------------------------------------------
		FusAerodynamicsManager theFuselageManager = new FusAerodynamicsManager(theConditions, aircraft);



		//--------------------------------------------------------------------------------------
		// Define airfoils

		System.out.println("\nAIRFOILS:");

		//AIRFOIL 1
		double yLocRoot = 0.0;
		MyAirfoil airfoilRoot = new MyAirfoil(theWing, yLocRoot, "23-018");
		airfoilRoot.getGeometry().update(yLocRoot);  // define chord
		airfoilRoot.getGeometry().set_maximumThicknessOverChord(0.18); //REPORT
		airfoilRoot.getGeometry().set_deltaYPercent(0.192 *airfoilRoot.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t ROOT \nAirfoil Type: " + airfoilRoot.get_family());
		System.out.println("Root Chord [m] = " + theWing.get_chordRoot().getEstimatedValue() );
		System.out.println("Root maximum thickness = " + airfoilRoot.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());		
		System.out.println("LE sharpness parameter Root = " + airfoilRoot.getGeometry().get_deltaYPercent());


		//AIRFOIL 2
		double yLocKink = theWing.get_spanStationKink() * theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilKink = new MyAirfoil(theWing, yLocKink, "23-015");
		airfoilKink.getGeometry().update(yLocKink);   // define chord
		airfoilKink.getGeometry().set_maximumThicknessOverChord(0.15); //REPORT
		airfoilKink.getGeometry().set_deltaYPercent(0.192 *airfoilKink.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t KINK \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("Kink Station [m] = " + yLocKink);
		System.out.println("Kink Chord [m] = " + theWing.get_chordKink().getEstimatedValue() );
		System.out.println("Kink maximum thickness = " + airfoilKink.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Kink = " + airfoilKink.getGeometry().get_deltaYPercent());


		//AIRFOIL 3
		double yLocTip = theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip, "23-012");
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT
		airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("tip Chord [m] = " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness = " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip = " + airfoilTip.getGeometry().get_deltaYPercent());


		//--------------------------------------------------------------------------------------
		// Assign airfoil


		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry(); 


		//--------------------------------------------------------------------------------------
		// Angle of attack

		Amount<Angle> alphaBody = theConditions.get_alphaCurrent();

		// -----------------------------------------------------------------------
		// LIFT CHARACTERISTICS 
		// -----------------------------------------------------------------------

		// Considering an angle of attack alphaBody, the aircraft components generates a lift. So 
		// it is necessary to evaluate these contributes. In particular, first of all, is wanted to 
		// evaluate the CL of isolated wing. This value will be correct with fuselage influence.
		// Afterwards it is necessary to evaluate the contribute of horizontal tail. 
		// It is important to note that each component works at a different angle of attack, meanwhile
		// for longitudinal stability the reference angle of attack is alphaBody.

		System.out.println("\n\n------------------------------------");
		System.out.println("\n LIFT CHARACTERISTICS  ");
		System.out.println("\n------------------------------------");


		// ------------------Wing---------------
		double cLIsolatedWing;
		double cLAlphaWingBody;

		System.out.println("\nAngle of attack alpha body = " + alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue());
		System.out.println("Angle of incidence of wing = " + theWing.get_iw().to(NonSI.DEGREE_ANGLE).getEstimatedValue());

		LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator = theLSAnalysis.new CalcCLAtAlpha();
		cLIsolatedWing = theCLWingCalculator.nasaBlackwellalphaBody(alphaBody);

		System.out.println("CL of Isolated wing at alpha body = " + cLIsolatedWing);
		theLSAnalysis.PlotCLvsAlphaCurve(subfolderPath);
		System.out.println("\t \t \tDONE PLOTTING CL VS ALPHA CURVE  ");




		// ------------------Fuselage---------------

		double cLAlphaWing = theLSAnalysis.getcLLinearSlopeNB();
		cLAlphaWingBody = theFuselageManager.calculateCLAlphaFuselage(cLAlphaWing);

		System.out.println(" cl alpha isolated wing = " + cLAlphaWing);
		System.out.println(" cl alpha wing body = " + cLAlphaWingBody);

		aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(alphaBody);
	}

}
