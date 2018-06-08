package sandbox2.gt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class ArgumentsCPACSReaderSandbox {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "Cpacs file")
	private File _inputFile;
	
	@Option(name = "-o", aliases = { "--output" }, required = false,
			usage = "Jsbsim file")
	private File _outputFile;

	@Option(name = "-ns", aliases = { "--no-sim" }, required = false,
			usage = "Jsbsim file")
	private boolean _noSim = false;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	public File getOutputFile() {
		return _outputFile;
	}
	public boolean isNoSim() {
		return _noSim;
	}
}