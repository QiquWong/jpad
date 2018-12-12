package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.Aircraft;
import javafx.application.Application;
import javafx.stage.Stage;

class CADArguments {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
			usage = "airfoil directory path")
	private File _airfoilDirectory;

	@Option(name = "-df", aliases = { "--dir-fuselages" }, required = true,
			usage = "fuselages directory path")
	private File _fuselagesDirectory;
	
	@Option(name = "-dls", aliases = { "--dir-lifting-surfaces" }, required = true,
			usage = "lifting surfaces directory path")
	private File _liftingSurfacesDirectory;
	
	@Option(name = "-de", aliases = { "--dir-engines" }, required = true,
			usage = "engines directory path")
	private File _enginesDirectory;
	
	@Option(name = "-dn", aliases = { "--dir-nacelles" }, required = true,
			usage = "nacelles directory path")
	private File _nacellesDirectory;
	
	@Option(name = "-dlg", aliases = { "--dir-landing-gears" }, required = true,
			usage = "landing gears directory path")
	private File _landingGearsDirectory;
	
	@Option(name = "-dcc", aliases = { "--dir-cabin-configurations" }, required = true,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	
	public File getAirfoilDirectory() {
		return _airfoilDirectory;
	}

	public File getFuselagesDirectory() {
		return _fuselagesDirectory;
	}
	
	public File getLiftingSurfacesDirectory() {
		return _liftingSurfacesDirectory;
	}

	public File getEnginesDirectory() {
		return _enginesDirectory;
	}
	
	public File getNacellesDirectory() {
		return _nacellesDirectory;
	}
	
	public File getLandingGearsDirectory() {
		return _landingGearsDirectory;
	}

	public File getCabinConfigurationDirectory() {
		return _cabinConfigurationsDirectory;
	}	
}

public class CompleteCADTest extends Application {
	
	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		//--------------------------------------------------
		// get the aircraft object
		System.out.println("\n\n##################");
		System.out.println("function start :: getting the aircraft object ...");

		Aircraft aircraft = CompleteCADTest.theAircraft;
		if (aircraft == null) {
			System.out.println("aircraft object null, returning.");
			return;
		}
	}

	public static void main(String[] args) {
		
		

	}

}
