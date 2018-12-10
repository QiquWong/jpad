package it.unina.daf.jpadcadsandbox.cfdworkflows;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class ArgumentsForCFDSim {
	
	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;
	
	@Option(name = "-db", aliases = { "--database-dir" }, required = false,
			usage = "database directory")
	private File _databaseDirectory;
	
	@Option(name = "-da", aliases = { "--airfoils-dir" }, required = false,
			usage = "airfoils directory path")
	private File _airfoilsDirectory;
	
	@Option(name = "-df", aliases = { "--fuselages-dir" }, required = false,
			usage = "fuselages directory path")
	private File _fuselagesDirectory;

	@Option(name = "-dls", aliases = { "--lifting-surfaces-dir" }, required = false,
			usage = "lifting surfaces directory path")
	private File _liftingSurfacesDirectory;

	@Option(name = "-de", aliases = { "--engines-dir" }, required = false,
			usage = "engines directory path")
	private File _enginesDirectory;

	@Option(name = "-dn", aliases = { "--nacelles-dir" }, required = false,
			usage = "nacelles directory path")
	private File _nacellesDirectory;

	@Option(name = "-dlg", aliases = { "--landing-gears-dir" }, required = false,
			usage = "landing gears directory path")
	private File _landingGearsDirectory;

	@Option(name = "-dcc", aliases = { "--cabin-configurations-dir" }, required = false,
			usage = "cabin configurations directory path")
	private File _cabinConfigurationsDirectory;
	
	@Option(name = "-scf", aliases = { "--sim-config-file" }, required = false,
			usage = "simulation configuration file path")
	private File _simulationConfigurationFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();
	
	public File getInputFile() {
		return _inputFile;
	}
	
	public File getDatabaseDirectory() {
		return _databaseDirectory;
	}
	
	public File getAirfoilDirectory() {
		return _airfoilsDirectory;
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
	
	public File getSimulationConfigurationFile() {
		return _simulationConfigurationFile;
	}
	
}
