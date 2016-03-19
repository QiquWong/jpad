package sandbox.mr;
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.google.common.collect.TreeBasedTable;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAerodynamics;
import aircraft.auxiliary.airfoil.MyGeometry;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.aerodynamics.NasaBlackwell;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLMaxClean;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcLiftDistribution;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.DatabaseReaderEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import igeo.IVec2R.Angle;
import javafx.util.Pair;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.CenterOfGravity;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class Test_MR_CLalpha {


	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		// -----------------------------------------------------------------------
		// INITIALIZE TEST CLASS
		// -----------------------------------------------------------------------

		System.out.println("\nInitializing test class...");
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL vs Alpha evaluation" + File.separator);


		// -----------------------------------------------------------------------
		// Generate default Wing
		// -----------------------------------------------------------------------

		
		// Wing
		double xAw = 11.0; //meter 
		double yAw = 0.0;
		double zAw = 1.6;
		double iw = 0.0;
		LiftingSurface theWing = new LiftingSurface(
				"Wing", // name
				"Data from AC_ATR_72_REV05.pdf", 
				xAw, yAw, zAw, iw, 
				ComponentEnum.WING
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


		// -----------------------------------------------------------------------
		// Operating Condition
		// -----------------------------------------------------------------------

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

		//--------------------------------------------------------------------------------------
		// Set databases
		
		theLSAnalysis.setDatabaseReaders(
				new Pair(DatabaseReaderEnum.AERODYNAMIC,
						"Aerodynamic_Database_Ultimate.h5"),
				new Pair(DatabaseReaderEnum.HIGHLIFT, "HighLiftDatabase.h5")
				);



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


		//		airfoilRoot.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN));
		//		airfoilRoot.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(10.0),SI.RADIAN));
		//		airfoilRoot.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
		//		airfoilRoot.getAerodynamics().set_clAlpha(6.07);
		airfoilRoot.getAerodynamics().set_clMax(1.8);
		//		airfoilRoot.getAerodynamics().set_clStar(1.06);



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

		//		airfoilKink.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN));
		//		airfoilKink.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(10.0),SI.RADIAN));
		//		airfoilKink.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
		//		airfoilKink.getAerodynamics().set_clAlpha(6.07);
		airfoilKink.getAerodynamics().set_clMax(1.8);
		//		airfoilKink.getAerodynamics().set_clStar(1.06);


		//AIRFOIL 3
		double yLocTip = theWing.get_semispan().getEstimatedValue();
		MyAirfoil airfoilTip = new MyAirfoil(theWing, yLocTip, "23-012");
		airfoilTip.getGeometry().update(yLocRoot);  // define chord
		airfoilTip.getGeometry().set_maximumThicknessOverChord(0.12); //REPORT
		//		airfoilTip.getAerodynamics().set_clMax(1.0);
		airfoilTip.getGeometry().set_deltaYPercent(0.192 *airfoilTip.getGeometry().get_maximumThicknessOverChord()*100 );
		System.out.println("\n \n \t TIP \nAirfoil Type: " + airfoilKink.get_family());
		System.out.println("tip Chord " +theWing.get_chordTip().getEstimatedValue() );
		System.out.println("Tip maximum thickness " + airfoilTip.getGeometry().get_maximumThicknessOverChord());
		System.out.println("CL max --> " + airfoilRoot.getAerodynamics().get_clMax());
		System.out.println("LE sharpness parameter Tip " + airfoilTip.getGeometry().get_deltaYPercent());

		//		airfoilTip.getAerodynamics().set_alphaZeroLift(Amount.valueOf(Math.toRadians(-1.6), SI.RADIAN));
		//		airfoilTip.getAerodynamics().set_alphaStar(Amount.valueOf(Math.toRadians(6.0),SI.RADIAN));
		//		airfoilTip.getAerodynamics().set_alphaStall(Amount.valueOf(Math.toRadians(12.0),SI.RADIAN));
		//		airfoilTip.getAerodynamics().set_clAlpha(6.01);
		airfoilTip.getAerodynamics().set_clMax(1.6);
		//		airfoilTip.getAerodynamics().set_clStar(0.63);

		// -----------------------------------------------------------------------
		// Assign airfoil
		// -----------------------------------------------------------------------


		List<MyAirfoil> myAirfoilList = new ArrayList<MyAirfoil>();
		myAirfoilList.add(0, airfoilRoot);
		myAirfoilList.add(1, airfoilKink);
		myAirfoilList.add(2, airfoilTip);
		theWing.set_theAirfoilsList(myAirfoilList);
		theWing.updateAirfoilsGeometry(); 



	}
}