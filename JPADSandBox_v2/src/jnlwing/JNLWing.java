package jnlwing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface.LiftingSurfaceBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;

class MyArgumentWing {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}
}

public class JNLWing {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	//-------------------------------------------------------------

	public static LiftingSurface theWing;

	//-------------------------------------------------------------
	
	public static void main(String[] args) {

		System.out.println("--------------");
		System.out.println("JNLWing");
		System.out.println("--------------");

		MyArgumentWing va = new MyArgumentWing();
		JNLWing.theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			JNLWing.theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			System.out.println("--------------");

			// This wing static object is available in the scope of
			// the Application.start method
			
			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree();
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
			HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
			
//			// read LiftingSurface from xml ...
			theWing = new LiftingSurfaceBuilder("MyWing", ComponentEnum.WING, aeroDatabaseReader, highLiftDatabaseReader)
					.liftingSurfaceCreator(
							LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, pathToXML, dirAirfoil)
							)
					.build();
			
			// default LiftingSurface from xml ...
//			theWing = new LiftingSurfaceBuilder("MyWing", ComponentEnum.WING, aeroDatabaseReader, highLiftDatabaseReader)
//					.liftingSurfaceCreator(
//							new LiftingSurfaceCreator
//							.LiftingSurfaceCreatorBuilder(
//									"MyWing",
//									Boolean.TRUE,
//									AircraftEnum.ATR72,
//									ComponentEnum.WING
//									)
//							.build()
//							)
//					.build();

			JNLWing.theWing.calculateGeometry(
					40,
					theWing.getType(),
					theWing.getLiftingSurfaceCreator().isMirrored());

			JNLWing.theWing.populateAirfoilList(aeroDatabaseReader, Boolean.FALSE);
			
			System.out.println("The wing ...");
			System.out.println(JNLWing.theWing.getLiftingSurfaceCreator().toString());
			System.out.println("Details on panel discretization ...");
			JNLWing.theWing.getLiftingSurfaceCreator().reportPanelsToSpanwiseDiscretizedVariables();

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			JNLWing.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

	}

}
