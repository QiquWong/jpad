package sandbox.vt;

import static org.kohsuke.args4j.ExampleMode.ALL;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import configuration.MyConfiguration;

public class MySandbox_VT {

	@Option(name="-n",usage="repeat <n> times\nusage can have new lines in it and also it can be verrrrrrrrrrrrrrrrrry long")
	private int _num = -1;

	@Option(name = "-i", aliases = { "--input" }, required = false,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-f", aliases = { "--inputName" }, required = false,
			usage = "my input file name")
	private String _inputFileName;

	public CmdLineParser theCmdLineParser;

	// receives other command line parameters than options
	@Argument
	private List<String> arguments = new ArrayList<String>();


	// The class constructor
	public MySandbox_VT() {

		theCmdLineParser = new CmdLineParser(this);

	}


	public static void main(String[] args) throws IOException {
		System.out.println("---------------------------");
		System.out.println("MySandbox_VT :: main ");
		System.out.println("---------------------------");


		MySandbox_VT theObj = new MySandbox_VT();
		theObj.theCmdLineParser.printUsage(System.out);
		theObj.manageCmdLineArguments(args);

		System.out.println("user.dir: " + System.getProperty("user.dir"));

		String inputFileFullPath = MyConfiguration.currentDirectoryString + File.separator + theObj.get_inputFile().getName();
		System.out.println("File name, full path: " + inputFileFullPath);
		System.out.println("File name, full path: " + theObj.get_inputFile().getAbsolutePath());

		System.out.println("The input file name: " + theObj.get_inputFileName());
		System.out.println("The input file: " + theObj.get_inputFile());
		System.out.println("The num: " + theObj.get_num());
		theObj.readInputFile();
		System.out.println("---------------------------");
	} // end of main 


	public void readInputFile() throws IOException {
		System.out.println("MySandbox_VT : readInputFile");
		String line;
		try 
		{
			FileReader fstream = new FileReader(_inputFile);
			BufferedReader reader = new BufferedReader(fstream);
			while ((line = reader.readLine()) != null)
			{
				System.out.println(line);
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("WARNING!!! File not found");
		}
	}


	public void manageCmdLineArguments(String[] args) {

		try {
			// parse the arguments.
			theCmdLineParser.parseArgument(args);

			// you can parse additional arguments if you want.
			// parser.parseArgument("more","args");

			// after parsing arguments, you should check
			// if enough arguments are given.
			if( arguments.isEmpty() )
				throw new CmdLineException(theCmdLineParser,"No additional argument are given!!!");
			else
				System.out.println("Additional arguments: " + arguments);
		}

		catch( CmdLineException e ) 
		{
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.out.println();
			System.out.println(e.getMessage());
			System.out.println("java MySandbox_VT [options...] arguments...");
			// print option sample. This is useful some time
			System.out.println("  Example: java SampleMain"+theCmdLineParser.printExample(ALL));
			System.out.println();
			return;        	
		}
	}

	public int get_num() 
	{
		return _num;
	}

	public void set_num(int n) 
	{
		this._num = n;
	}

	public File get_inputFile() 
	{
		return _inputFile;
	}

	public void set_inputFile(File f) 
	{
		this._inputFile = f;
	}

	public String get_inputFileName() 
	{
		return _inputFileName;
	}
}
