package standaloneutils.launchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.MatrixUtils;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple4;
import javaslang.Tuple6;

// see: http://www.uavs.us/2011/12/02/matlab-avl-control/

public class AVLExternalJob implements IAVLExternalJob {

	protected File executableFile;
	protected File runFile;
	protected File inputFile;
	protected File massFile;
	protected File binDirectory;
	protected File cacheDirectory;
	protected File outputFile;
	protected File outputStabilityDerivativesFile;
	
	Map<String, String> additionalEnvironment = new HashMap<String, String>();
	
	protected List<String> commandInformation = new ArrayList<String>();
	protected SystemCommandExecutor systemCommandExecutor;
	protected StringBuilder stdOut, stdErr;
	
	private AVLOutputStabilityDerivativesFileReader outputStabilityDerivativesFileReader;

	public static void main(String[] args) throws IOException, InterruptedException {
		// Instantiate the job executor object
		AVLExternalJob job = new AVLExternalJob();

		System.out.println("--------------------------------------------- Launch AVL job in a separate process.");

		// Set the AVLROOT environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "AVL" + File.separator 
				+ "bin" 				
				;
		job.setEnvironmentVariable("AVLROOT", binDirPath);

		// Establish the path to dir where the executable file resides 
		job.setBinDirectory(new File(binDirPath));
		System.out.println("Binary directory: " + job.getBinDirectory());

		// Establish the path to executable file
		job.setExecutableFile(new File(binDirPath + File.separator + "avl.exe"));
		System.out.println("Executable file: " + job.getExecutableFile());
		
		// Establish the path to the cache directory - TODO: for now the same as bin dir
		job.setCacheDirectory(new File(binDirPath));
		System.out.println("Cache directory: " + job.getCacheDirectory());
		
		//-----------------------------------------------------------------------------------------------------
		// Handle file names according to a given base-name
		String baseName = "newData2";
		
		// gather files and clean up before execution
		List<String> fileNames = new ArrayList<>();
		Stream<String> fileExtensions = Stream.of(".mass", ".st", ".sb", ".eig");
		fileExtensions.forEach(ext -> fileNames.add(baseName+ext));

		fileNames.stream().forEach(name -> {
			Path path = FileSystems.getDefault().getPath(
					binDirPath + File.separator + name);
			try {
				System.out.println("Deleting file: " + path);
				Files.delete(path);
			} catch (NoSuchFileException e) {
				System.err.format("%s: no such" + " file or directory: %1$s\n", path);
			} catch (DirectoryNotEmptyException e) {
				System.err.format("%1$s not empty\n", path);
			} catch (IOException e) {
				System.err.println(e);
			}
		});
		
		// Assign the main .avl input file
		job.setInputAVLFile(new File(binDirPath + File.separator + baseName+".avl"));

		// Assign the .mass file
		job.setInputMassFile(new File(binDirPath + File.separator + baseName+".mass"));
		
		// Assign .run file with commands
		// TODO: produce .run file on the fly
		job.setInputRunFile(new File(binDirPath + File.separator + baseName+".run"));

		//-------------------------------------------------------------------------
		// Generate data

		AVLMainInputData inputData = job.importToMainInputData(); // TODO: pass JPADAircraft structure
		
		AVLAircraft aircraft = job.importToAVLAircraft(); // TODO: pass JPADAircraft structure
		
		AVLMassInputData massData = job.importToMassInputData(); // TODO: pass JPADAircraft structure
		
		//-------------------------------------------------------------------------
		// Form the final command to launch the external process
		String commandLine = job.formCommand(inputData, aircraft, massData);

		// Print out the command line
		System.out.println("Command line: " + commandLine);

		System.out.println("---------------------------------------------");
		System.out.println("EXECUTE JOB:\n");
		int status = job.execute();

		// print the stdout and stderr
		System.out.println("The numeric result of the command was: " + status);
		System.out.println("---------------------------------------------");
		System.out.println("STDOUT:");
		System.out.println(job.getStdOut());
		System.out.println("---------------------------------------------");
		System.out.println("STDERR:");
		System.out.println(job.getStdErr());
		System.out.println("---------------------------------------------");
		System.out.println("Environment variables:");
		Map<String, String> env = job.getEnvironment();
		// env.forEach((k,v)->System.out.println(k + "=" + v));
		System.out.println("AVLROOT=" + env.get("AVLROOT"));
		System.out.println("windir=" + env.get("windir"));
		System.out.println("---------------------------------------------");

/*

		// Parse the AVL output file
		job.parseOutputFile();

		// print the map of variables
		Map<String, List<Number>> variables = job.getAVLOutputReader().getVariables();
		// Print the map of variables
		variables.forEach((key, value) -> {
			System.out.println(key + " = " + value);
		});
		System.out.println("Number of alpha's = " + job.getAVLOutputReader().getNAlphas());
		System.out.println("---------------------------------------------");

*/

		System.out.println("Job terminated.");

	}


	public AVLMainInputData importToMainInputData() { // TODO: pass JPADAircraft / JPAD-OperatingConditions structures
		// NOTE: these data might come from the outside, generated by the 
		//       aircraft design process
		return
			new AVLMainInputData
					.Builder()
					.setDescription("(C) Agostino De Marco, agodemar")
					/*
					 *    Mach number
					 */
					.setMach(0.3) // only one Mach number at time permitted
					/*
					 *   Build object, finally 
					 *   Validate for all fields to be set, Optional fields are empty	
					 *   
					 */
					.build();
	}

	public AVLAircraft importToAVLAircraft() {
		if (this.binDirectory == null) {
			System.err.println("AVLExternalJob error: binDirectory unassigned");
			return null;
		}
		return // assign the aircraft as a collection of wings and bodies
			new AVLAircraft
				.Builder()
				.setDescription("The aircraft - agodemar")
				.appendWing( //----------------------------------------------- wing 1
					new AVLWing
						.Builder()
						.setDescription("Main wing")
						.addSections( //-------------------------------------- wing 1 - section 1
							new AVLWingSection
								.Builder()
								.setDescription("Root section")
								.setAirfoilCoordFile(
									new File(this.binDirectory.getAbsolutePath() + File.separator 
										+ "ag38.dat"
									) // TODO: produce .dat file on the fly
								)
								/*
								.setAirfoilSectionInline(
									// Inline section coordinates formatted as airfoil section: 
									//    This is useful when the real airfoil shape is known.
									//    Such a 2D array would be filled programmatically and 
									//    the AFIL/<airfoil-section>.dat couple would not be 
									//    required (no auxiliary file to write).
									AVLInputGenerator.getAG38AirfoilSection()
								)								
						        */
								.setOrigin(new Double[]{0.0, 0.0, 0.0})
								.setChord(3.0)
								.setTwist(0.0)
								.build()
							)
						.addSections( //-------------------------------------- wing 1 - section 2
							new AVLWingSection
								.Builder()
								.setDescription("Tip section")
								.setAirfoilCoordFile(
									new File(this.binDirectory.getAbsolutePath() + File.separator 
										+ "ag38.dat"
									) // TODO: produce .dat file on the fly
								)
								/*
								.setAirfoilSectionInline(
									// Inline section coordinates formatted as airfoil section: 
									//    This is useful when the real airfoil shape is known.
									//    Such a 2D array would be filled programmatically and 
									//    the AFIL/<airfoil-section>.dat couple would not be 
									//    required (no auxiliary file to write).
									AVLInputGenerator.getAG38AirfoilSection()
								)
								 */
								.setOrigin(new Double[]{0.0, 12.0, 0.0})
								.setChord(1.5)
								.setTwist(0.0)
								.build()
							)
						.build()
					)
				.appendWing( //----------------------------------------------- wing 2
					new AVLWing
						.Builder()
						.setDescription("Horizontal tail")
						.setOrigin(new Double[]{15.0, 0.0, 1.25})
						.addSections( //-------------------------------------- wing 2 - section 1
							new AVLWingSection
								.Builder()
								.setDescription("Root section")
								.setAirfoilCoordFile(
									new File(this.binDirectory.getAbsolutePath() + File.separator 
										+ "ag38.dat"
									)
								)
								.setOrigin(new Double[]{0.0, 0.0, 0.0})
								.setChord(1.2)
								.setTwist(0.0)
								.addControlSurfaces(
									new AVLWingSectionControlSurface
										.Builder()
										.setDescription("Elevator")
										.setGain(1.0)
										.setXHinge(0.6)
										.setHingeVector(new Double[]{0.0, 1.0, 0.0})
										.setSignDuplicate(1.0)
										.build()
								)
								.build()
							)
						.addSections(
							new AVLWingSection
								.Builder()
								.setDescription("Tip section")
								.setAirfoilCoordFile(
									new File(this.binDirectory.getAbsolutePath() + File.separator 
										+ "ag38.dat"
									) // TODO: produce .dat file on the fly
								)
								/*
								.setAirfoilSectionInline(
										// Inline section coordinates formatted as airfoil section: 
										//    This is useful when the real airfoil shape is known.
										//    Such a 2D array would be filled programmatically and 
										//    the AFIL/<airfoil-section>.dat couple would not be 
										//    required (no auxiliary file to write).
										AVLInputGenerator.getAG38AirfoilSection()
									)
								*/
								.setOrigin(new Double[]{0.0, 3.5, 0.0})
								.setChord(1.2)
								.setTwist(0.0)
								.addControlSurfaces(
										new AVLWingSectionControlSurface
											.Builder()
											.setDescription("Elevator")
											.setGain(1.0)
											.setXHinge(0.6)
											.setHingeVector(new Double[]{0.0, 1.0, 0.0})
											.setSignDuplicate(1.0)
											.build()
								)
								.build()
							)
						.build()
					)
				.appendBody( //----------------------------------------------- body 1
					new AVLBody
						.Builder()
						.setDescription("theFuselage")
						.setBodyCoordFile(
							new File(this.binDirectory.getAbsolutePath() + File.separator 
								+ "sub.dat"
							) // TODO: produce .dat file on the fly
						)
						/*
						.setBodySectionInline(
							// Inline body-section coordinates formatted as airfoil section: 
							//    x --> X-coordinate of the section parallel to YZ-plane
							//    y --> radius of the equivalent circular section, 
							//          i.e. a circle of the same area of body's real section 
							//          
							//    This is useful when the real fuselage shape is known and equivalent sections
							//    are calculated on the fly. Such a 2D array would be filled programmatically
							//    and the BFIL/<body-section>.dat couple would not be required (no auxiliary file 
							//    to write).
							MatrixUtils.createRealMatrix(
									new double[][]{
										{1.0, 0.000},
										{0.9, 0.010},
										{0.8, 0.015},
										{0.5, 0.020},
										{0.2, 0.015},
										{0.1, 0.010},
										{0.0, 0.000},
										{0.1,-0.010},
										{0.2,-0.015},
										{0.5,-0.020},
										{0.8,-0.015},
										{0.9,-0.010},
										{1.0, 0.000}
									}
							)
						)
						*/
						.build()
					)
				// -------------------------------------- build the aircraft, finally
				.build();
	}
	
	public AVLMassInputData importToMassInputData() { // TODO: pass JPADAircraft / JPAD structures
		// NOTE: these data might come from the outside, generated by the 
		//       aircraft design process
		return
			new AVLMassInputData
					.Builder()
					.setDescription("(C) Agostino De Marco, agodemar, mass properties")
					.setLUnit(0.0254)
					.setMUnit(0.001)
					.addMassProperties( // wing center panel
						Tuple.of(
							Tuple.of(156.0, 4.0, 0.0, 0.0), // mass, x, y, z
							Tuple.of(11700.0, 832.0, 12532.0, 0.0, 0.0, 0.0) // Ixx, Iyy, Izz, Ixy, Ixz, Iyz
						)
					)
					.addMassProperties( // wing R mid panel
							Tuple.of(
								Tuple.of(55.5, 4.2, 22.0, 1.0), // mass, x, y, z
								Tuple.of(1180.0, 210.0, 1390.0, 0.0, 0.0, 0.0) // Ixx, Iyy, Izz, Ixy, Ixz, Iyz
							)
						)
					.addMassProperties( // wing L mid panel
							Tuple.of(
								Tuple.of(55.5, 4.2, -22.0, 1.0), // mass, x, y, z
								Tuple.of(1180.0, 210.0, 1390.0, 0.0, 0.0, 0.0) // Ixx, Iyy, Izz, Ixy, Ixz, Iyz
							)
						)
					.addMassProperties( // horiz tail
							Tuple.of(
								Tuple.of(12.0, 29.0, 0.0, 1.0), // mass, x, y, z
								Tuple.of(270.0, 12.0, 282.0, 0.0, 0.0, 0.0) // Ixx, Iyy, Izz, Ixy, Ixz, Iyz
							)
						)
					.build();
	}

	
	/*
	 *  TODO modify this function as appropriate
	 */
	private String formCommand(AVLMainInputData inputData, AVLAircraft aircraft, AVLMassInputData massData) {

		// build the system command we want to run
		// TODO: handle Win32 and Win64 with separate tags,
		//       handle Linux and Mac iOS as well
		String binShellWin32 = System.getenv("WINDIR") + File.separator
				+ "syswow64"  + File.separator
				+ "cmd.exe"
				;
		System.out.println("Shell Win32 launcher: " + binShellWin32);

		// the Win32 shell cmd.exe
		commandInformation.add(binShellWin32);
		// option /C to cmd.exe
		commandInformation.add("/C");
		// command line to pass to the shell prompt

		//			commandInformation.add("dir"); // must be on Windows
		//			commandInformation.add(
		//					"." + File.separator
		//					+ "src" + File.separator
		//					+ "standaloneutils" + File.separator
		//					+ "launchers"
		//					);

		//			// The following writes a file similar to B-737.dcm
		//			DatcomPlusInputGenerator.writeTemplate(this.getInputFile().getAbsolutePath()); // Ok

		// Write out the input file
		AVLInputGenerator.writeDataToAVLFile(inputData, aircraft, this.getInputAVLFile().getAbsolutePath());

		System.out.println("Input AVL file full path: " + this.getInputAVLFile());
		System.out.println("Input AVL file name: " + this.getInputAVLFile().getName());

		// Write out the mass file
		AVLInputGenerator.writeDataToMassFile(massData, this.getInputMassFile().getAbsolutePath());

		System.out.println("Input Mass file full path: " + this.getInputMassFile());
		System.out.println("Input Mass file name: " + this.getInputMassFile().getName());
		
		// Assign the output file
		this.setOutputFile(
				new File(this.getBinDirectory() + File.separator 
						+ this.getInputAVLFile().getName().replaceFirst(".avl", ".st")
						)
				);

		System.out.println("Output file full path: " + this.getOutputFile());
		System.out.println("Output file name: " + this.getOutputFile().getName());

		commandInformation.add(
				"cd " + this.getBinDirectory()
				);
		commandInformation.add(
				"& "
						+ this.getExecutableFile().getName() + " < " + this.getInputRunFile().getAbsolutePath() // .getName()
				);

		return this.getCommandLine();
	}

	@Override
	public File getExecutableFile() {
		return executableFile;
	}

	@Override
	public void setExecutableFile(File file) {
		this.executableFile = file;
		
	}
	
	@Override
	public int execute() throws IOException, InterruptedException {
		
		System.out.println("AVLExternalJob::execute --> launching external process");
	    
		// allocate the executor
	    this.systemCommandExecutor = new SystemCommandExecutor(commandInformation);
	    
	    // fetch additional environment variables, before executing the process
	    additionalEnvironment.forEach(
	    		(k,v) -> systemCommandExecutor.setEnvironmentVariable(k, v)
	    );

	    // execute the process
	    int result = systemCommandExecutor.executeCommand();
	    
	    // get the stdout and stderr from the command that was run
	    stdOut = systemCommandExecutor.getStandardOutputFromCommand();
	    stdErr = systemCommandExecutor.getStandardErrorFromCommand();
		
	    return result;
	}

	@Override
	public Map<String, String> getEnvironment() {
		return systemCommandExecutor.getProcessBuilder().environment();
	}

	@Override
	public void setEnvironmentVariable(String varName, String value) {
		// systemCommandExecutor.setEnvironmentVariable(varName, value);
		additionalEnvironment.put(varName, value);
	}

	@Override
	public File getOutputStabilityDerivativesFile() {
		return this.outputStabilityDerivativesFile;
	}

	@Override
	public void setOutputStabilityDerivativesFile(File file) {
		this.outputStabilityDerivativesFile = file;
	}

	@Override
	public boolean parseOutputStabilityDerivativesFile() {
		// TODO Auto-generated method stub
		return false;
	}

	public List<String> getCommandLineInformation() {
		return commandInformation;
	} 

	@Override
	public String getCommandLine() {
		return String.join(" ", commandInformation);
	} 
	
	@Override
	public File getInputRunFile() {
		return runFile;
	}
	
	@Override
	public void setInputRunFile(File file) {
		this.runFile = file;
	}
	
	@Override
	public File getInputAVLFile() {
		return inputFile;
	}
	
	@Override
	public void setInputAVLFile(File inputFile) {
		this.inputFile = inputFile;
	}
	
	@Override
	public File getInputMassFile() {
		return this.massFile;
	}

	@Override
	public void setInputMassFile(File massFile) {
		this.massFile = massFile;
	}


	@Override
	public File getBinDirectory() {
		return binDirectory;
	}
	
	@Override
	public void setBinDirectory(File binDirectory) {
		this.binDirectory = binDirectory;
	}
	
	@Override
	public File getCacheDirectory() {
		return cacheDirectory;
	}
	
	@Override
	public void setCacheDirectory(File cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	@Override
	public SystemCommandExecutor getSystemCommandExecutor() {
		return systemCommandExecutor;
	}

	@Override
	public StringBuilder getStdOut() {
		return stdOut;
	}

	@Override
	public StringBuilder getStdErr() {
		return stdErr;
	}

}
