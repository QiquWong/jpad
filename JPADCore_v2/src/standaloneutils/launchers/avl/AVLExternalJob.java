package standaloneutils.launchers.avl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import aircraft.Aircraft;
import analyses.OperatingConditions;
import configuration.enumerations.AnalysisTypeEnum;
import javaslang.Tuple;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.launchers.SystemCommandExecutor;

// see: http://www.uavs.us/2011/12/02/matlab-avl-control/

public class AVLExternalJob implements IAVLExternalJob {

	String baseName;
	protected File executableFile;
	protected File runFile;
	protected File inputFile;
	protected File massFile;
	protected File binDirectory;
	protected File cacheDirectory;
	protected File outputStabilityDerivativesFile;
	protected File outputStabilityDerivativesBodyAxesFile;
	
	Map<String, String> additionalEnvironment = new HashMap<String, String>();
	
	protected List<String> commandInformation = new ArrayList<String>();
	protected SystemCommandExecutor systemCommandExecutor;
	protected StringBuilder stdOut, stdErr;
	
	private boolean enabledStabilityAnalysis = false;
	private AVLOutputStabilityDerivativesFileReader outputStabilityDerivativesFileReader;

//	public static void main(String[] args) throws IOException, InterruptedException {
//		// Instantiate the job executor object
//		AVLExternalJob job = new AVLExternalJob();
//
//		System.out.println("--------------------------------------------- Launch AVL job in a separate process.");
//
//		// Set the AVLROOT environment variable
//		String binDirPath = System.getProperty("user.dir") + File.separator  
//				+ "src" + File.separator 
//				+ "standaloneutils" + File.separator 
//				+ "launchers" + File.separator 
//				+ "apps" + File.separator 
//				+ "AVL" + File.separator 
//				+ "bin" 				
//				;
//		job.setEnvironmentVariable("AVLROOT", binDirPath);
//
//		// Establish the path to dir where the executable file resides 
//		job.setBinDirectory(new File(binDirPath));
//		System.out.println("Binary directory: " + job.getBinDirectory());
//
//		// Establish the path to executable file
//		job.setExecutableFile(new File(binDirPath + File.separator + "avl.exe"));
//		System.out.println("Executable file: " + job.getExecutableFile());
//		
//		// Establish the path to the cache directory - TODO: for now the same as bin dir
//		job.setCacheDirectory(new File(binDirPath));
//		System.out.println("Cache directory: " + job.getCacheDirectory());
//		
//		//-----------------------------------------------------------------------------------------------------
//		// Handle file names according to a given base-name
//		// Must assign this to avoid NullPointerException
//		job.setBaseName("newData2");
//		
//		// gather files and clean up before execution
//		List<String> fileNames = new ArrayList<>();
//		Stream<String> fileExtensions = Stream.of(".run", ".avl", ".mass", ".st", ".sb", ".eig");
//		fileExtensions.forEach(ext -> fileNames.add(job.getBaseName()+ext));
//
//		fileNames.stream().forEach(name -> {
//			Path path = FileSystems.getDefault().getPath(
//					binDirPath + File.separator + name);
//			try {
//				System.out.println("Deleting file: " + path);
//				Files.delete(path);
//			} catch (NoSuchFileException e) {
//				System.err.format("%s: no such" + " file or directory: %1$s\n", path);
//			} catch (DirectoryNotEmptyException e) {
//				System.err.format("%1$s not empty\n", path);
//			} catch (IOException e) {
//				System.err.println(e);
//			}
//		});
//		
//		// Assign the main .avl input file
//		job.setInputAVLFile(new File(binDirPath + File.separator + job.getBaseName()+".avl"));
//
//		// Assign the .mass file
//		job.setInputMassFile(new File(binDirPath + File.separator + job.getBaseName()+".mass"));
//		
//		// Assign the output stability derivatives file
//		job.setOutputStabilityDerivativesFile(new File(binDirPath + File.separator + job.getBaseName()+".st"));
//
//		// Assign the output stability derivatives file
//		job.setOutputStabilityDerivativesBodyAxesFile(new File(binDirPath + File.separator + job.getBaseName()+".sb"));
//
//		// Assign .run file with commands
//		job.setInputRunFile(new File(binDirPath + File.separator + job.getBaseName()+".run"));
//		
//		//-------------------------------------------------------------------------
//		// Generate data
//
//		AVLMainInputData inputData = job.importToMainInputData(null, null);
//		
//		AVLAircraft aircraft = job.importToAVLAircraft(null); // pass a null Aircraft object
//		
//		AVLMassInputData massData = job.importToMassInputData(null); // TODO: pass JPADAircraft/Analysis structure
//		
//		AVLMacro avlMacro = job.formRunMacro(); // TODO: modify this as appropriate
//		
//		/*
//		 * ================================================================
//		 * Form the final command to launch the external process
//		 *
//		 * Example, in Win32 shell:
//		 *
//		 * >$ cd <avl-executable-dir>
//		 * >$ avl.exe < <base-name>.run
//		 * 
//		 * ================================================================
//		 */
//		String commandLine = job.formCommand(inputData, aircraft, massData, avlMacro);
//
//		// Print out the command line
//		System.out.println("Command line: " + commandLine);
//
//		System.out.println("---------------------------------------------");
//		System.out.println("EXECUTE JOB:\n");
//		int status = job.execute();
//
//		// print the stdout and stderr
//		System.out.println("The numeric result of the command was: " + status);
//		System.out.println("---------------------------------------------");
//		System.out.println("STDOUT:");
//		System.out.println(job.getStdOut());
//		System.out.println("---------------------------------------------");
//		System.out.println("STDERR:");
//		System.out.println(job.getStdErr());
//		System.out.println("---------------------------------------------");
//		System.out.println("Environment variables:");
//		Map<String, String> env = job.getEnvironment();
//		// env.forEach((k,v)->System.out.println(k + "=" + v));
//		System.out.println("AVLROOT=" + env.get("AVLROOT"));
//		System.out.println("windir=" + env.get("windir"));
//		System.out.println("---------------------------------------------");
//
//		// Parse the AVL output file
//		
//		System.out.println("Output file full path: " + job.getOutputStabilityDerivativesFile());
//		
//		// Use AVLOutputStabilityDerivativesFileReader object
//		AVLOutputStabilityDerivativesFileReader reader = new AVLOutputStabilityDerivativesFileReader(job.getOutputStabilityDerivativesFile());
//		
//		System.out.println("The Datcom output file is available? " + reader.isFileAvailable());
//		System.out.println("The Datcom output file to read: " + reader.getTheFile());
//		
//		// parse the file and build map of variables & values
//		reader.parse();
//		
//		// print the map
//		System.out.println("------ Map of variables ------");
//		Map<String, List<Number>> variables = reader.getVariables();
//		// Print the map of variables
//		variables.forEach((key, value) -> {
//		    System.out.println(key + " = " + value);
//		});		
//		
//		System.out.println("---------------------------------------------");
//
//		System.out.println("Job terminated.");
//
//	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String name) {
		this.baseName = name;
	}	

	public AVLMainInputData importToMainInputData(OperatingConditions theOperatingConditions, Aircraft theAircraft) { // TODO: pass JPADAircraft / JPAD-OperatingConditions structures
		if ((theOperatingConditions != null) && (theAircraft != null)) {
			
			System.out.println("\n\n\n Aircraft NOT NULL");
			System.out.println("Span: " + theAircraft.getWing().getSpan().doubleValue(SI.METER));
			System.out.println("\n\n\n");

			return new AVLMainInputData
				.Builder()
				.setDescription("JPAD Aircraft: " + theAircraft.getId())
				/*
				 *    Mach number
				 */
				.setMach(theOperatingConditions.getMachCruise())
				/*
				 *    Span (m)
				 */
				.setBref(theAircraft.getWing().getSpan().doubleValue(SI.METER))
				/*
				 *    Reference chord (m)
				 */
				.setCref(theAircraft.getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
				/*
				 *    Reference surface (m^2)
				 */
				.setSref(theAircraft.getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE))
				/*
				 *   CG
				 */
				.setXref(theAircraft.getTheAnalysisManager().getTheBalance().getCGMaximumTakeOffMass().getXBRF().doubleValue(SI.METER))
				.setYref(theAircraft.getTheAnalysisManager().getTheBalance().getCGMaximumTakeOffMass().getYBRF().doubleValue(SI.METER))
				.setZref(theAircraft.getTheAnalysisManager().getTheBalance().getCGMaximumTakeOffMass().getZBRF().doubleValue(SI.METER))
				/*
				 *   Build object, finally 
				 *   Validate for all fields to be set, Optional fields are empty	
				 *   
				 */
				.build();
		}
		return
			new AVLMainInputData
					.Builder()
					.setDescription("Agodemar dummy aircraft")
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

	public AVLAircraft importToAVLAircraft(Aircraft theAircraft) {
		if (this.binDirectory == null) {
			System.err.println("AVLExternalJob error: binDirectory unassigned");
			return null;
		}
		if (theAircraft != null) {
			return  // assign the aircraft getting data from theAircraft object
				new AVLAircraft
					.Builder()
					.setDescription(theAircraft.getId() + " (AVL_Test)")
					.appendWing( //----------------------------------------------- wing 1
						new AVLWing
							.Builder()
							.setDescription(theAircraft.getWing().getId())
							.setIncidence(theAircraft.getWing().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE))
							.setOrigin( // wing apex coordinates in BRF 
									new Double[]{
											theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER), 
											theAircraft.getWing().getYApexConstructionAxes().doubleValue(SI.METER), 
											theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER)})
							.addSections( //-------------------------------------- wing 1 - section 1
								new AVLWingSection
									.Builder()
									.setDescription("Wing root section")
									.setAirfoilObject(theAircraft.getWing().getAirfoilList().get(0)) // 1. set source first
									.setAirfoilCoordFile( // 2. set airfoil name
										new File(this.binDirectory.getAbsolutePath() + File.separator 
											+ getBaseName() + "_airfoil_wing_root.dat"
										)
									)
									.setOrigin(new Double[]{
											theAircraft.getWing().getXLEBreakPoints().get(0).doubleValue(SI.METER), // x l.e. root in LRF 
											theAircraft.getWing().getYBreakPoints().get(0).doubleValue(SI.METER), 
											theAircraft.getWing().getZLEBreakPoints().get(0).doubleValue(SI.METER)})
									.setChord(
											theAircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
											)
									.setTwist(
											- theAircraft.getWing().getPanels().get(0).getAirfoilRoot().getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE)
											)
									.build()
								)
							.addSections( //-------------------------------------- wing 1 - section 2
								new AVLWingSection
									.Builder()
									.setDescription("Wing kink section")
									.setAirfoilObject(theAircraft.getWing().getAirfoilList().get(1)) // 2. set source first
									.setAirfoilCoordFile( // 2. set airfoil name
											new File(this.binDirectory.getAbsolutePath() + File.separator 
												+ getBaseName() + "_airfoil_wing_kink.dat"
											)
										)
									.setOrigin(new Double[]{
											theAircraft.getWing().getXLEBreakPoints().get(1).doubleValue(SI.METER), // x l.e. of kink section in LRF 
											theAircraft.getWing().getYBreakPoints().get(1).doubleValue(SI.METER), 
											theAircraft.getWing().getZLEBreakPoints().get(1).doubleValue(SI.METER)})
									.setChord(
											theAircraft.getWing().getPanels().get(0).getChordTip().doubleValue(SI.METER)
											)
									.setTwist(
											theAircraft.getWing().getPanels().get(0).getTwistGeometricAtTip().doubleValue(NonSI.DEGREE_ANGLE)
											- theAircraft.getWing().getPanels().get(0).getAirfoilTip().getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE)
											)
									.build()
								)
							.addSections( //-------------------------------------- wing 1 - section 3
									new AVLWingSection
										.Builder()
										.setDescription("Wing tip section")
										.setAirfoilObject(theAircraft.getWing().getAirfoilList().get(2)) // 2. set source first
										.setAirfoilCoordFile( // 2. set airfoil name
												new File(this.binDirectory.getAbsolutePath() + File.separator 
													+ getBaseName() + "_airfoil_wing_tip.dat"
												)
											)
										.setOrigin(new Double[]{
												theAircraft.getWing().getXLEBreakPoints().get(2).doubleValue(SI.METER), // x l.e. of tip section in LRF 
												theAircraft.getWing().getYBreakPoints().get(2).doubleValue(SI.METER), 
												theAircraft.getWing().getZLEBreakPoints().get(2).doubleValue(SI.METER)})
										.setChord(
												theAircraft.getWing().getPanels().get(1).getChordTip().doubleValue(SI.METER)
												)
										.setTwist(
												theAircraft.getWing().getPanels().get(1).getTwistGeometricAtTip().doubleValue(NonSI.DEGREE_ANGLE)
												- theAircraft.getWing().getPanels().get(1).getAirfoilTip().getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE)
												)
										.build()
									)
							.build()
						)
					.appendWing( //----------------------------------------------- wing 2
						new AVLWing
							.Builder()
							.setDescription(theAircraft.getHTail().getId())
							.setOrigin( // htail apex coordinates in BRF 
									new Double[]{
											theAircraft.getHTail().getXApexConstructionAxes().doubleValue(SI.METER), 
											theAircraft.getHTail().getYApexConstructionAxes().doubleValue(SI.METER), 
											theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER)})
							.setIncidence(theAircraft.getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE))
							.addSections( //-------------------------------------- wing 2 - section 1
								new AVLWingSection
									.Builder()
									.setDescription("HTail root section")
									.setAirfoilObject(theAircraft.getHTail().getAirfoilList().get(0)) // 2. set source first
									.setAirfoilCoordFile( // 2. set airfoil name
											new File(this.binDirectory.getAbsolutePath() + File.separator 
												+ getBaseName() + "_airfoil_htail_root.dat"
											)
										)
									.setOrigin(new Double[]{
											theAircraft.getHTail().getXLEBreakPoints().get(0).doubleValue(SI.METER), // x l.e. root in LRF 
											theAircraft.getHTail().getYBreakPoints().get(0).doubleValue(SI.METER), 
											theAircraft.getHTail().getZLEBreakPoints().get(0).doubleValue(SI.METER)})
									.setChord(
											theAircraft.getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
											)
									.setTwist(
											- theAircraft.getHTail().getPanels().get(0).getAirfoilRoot().getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE)
											)
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
							.addSections( //-------------------------------------- wing 2 - section 2
								new AVLWingSection
									.Builder()
									.setDescription("HTail tip section")
									.setAirfoilObject(theAircraft.getHTail().getAirfoilList().get(1)) // 2. set source first
									.setAirfoilCoordFile( // 2. set airfoil name
											new File(this.binDirectory.getAbsolutePath() + File.separator 
												+ getBaseName() + "_airfoil_htail_tip.dat"
											)
										)
									.setOrigin(new Double[]{
											theAircraft.getHTail().getXLEBreakPoints().get(1).doubleValue(SI.METER), // x l.e. tip in LRF 
											theAircraft.getHTail().getYBreakPoints().get(1).doubleValue(SI.METER), 
											theAircraft.getHTail().getZLEBreakPoints().get(1).doubleValue(SI.METER)})
									.setChord(
											theAircraft.getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
											)
									.setTwist(
											theAircraft.getHTail().getPanels().get(0).getTwistGeometricAtTip().doubleValue(NonSI.DEGREE_ANGLE)
											- theAircraft.getHTail().getPanels().get(0).getAirfoilRoot().getAlphaZeroLift().doubleValue(NonSI.DEGREE_ANGLE)
											)
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
							.setOrigin( // body apex coordinates in BRF 
									new Double[]{
											theAircraft.getFuselage().getXApexConstructionAxes().doubleValue(SI.METER), 
											theAircraft.getFuselage().getYApexConstructionAxes().doubleValue(SI.METER), 
											theAircraft.getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)})
							.setFuselageObject(theAircraft.getFuselage()) // 2. set source first
							.setBodyCoordFile( // 2. set airfoil name
									new File(this.binDirectory.getAbsolutePath() + File.separator 
										+ getBaseName() + "_body.dat"
									)
								)
							.build()
						)
					// -------------------------------------- build the aircraft, finally
					.build();
			}
		else // theAircraft == null
			return null;
	}
	
	public AVLMassInputData importToMassInputData(Aircraft theAircraft) {
		// Get masses and inertias from weight+balance analyses
		double massWing = 0.0;
//		double ixx = 0.0;
//		double iyy = 0.0;
//		double izz = 0.0;
		double ixxWing = 0.0;
		double iyyWing = 0.0;
		double izzWing = 0.0;
		double ixyWing = 0.0;
		double ixzWing = 0.0;
		double iyzWing = 0.0;
		if (theAircraft != null) {
			if (theAircraft.getTheAnalysisManager() != null) {
				// TODO: @masc adjust the following code accordingly
				System.out.println("---------------------------------------------");
				System.out.println("Data obtained from get methods of the aircraft.\n");

				if (theAircraft.getTheAnalysisManager().getAnalysisList().contains(AnalysisTypeEnum.WEIGHTS)) {
					// the weights analysis is done at this time					
					massWing = theAircraft
							.getTheAnalysisManager().getTheWeights().getWingMass()
							.doubleValue(SI.KILOGRAM);
					System.out.println("massWing = " + massWing + " kg\n");

				}
				if (theAircraft.getTheAnalysisManager().getAnalysisList().contains(AnalysisTypeEnum.BALANCE)) {
					
					//===========================================
					// ENABLE STABILITY DERIVATIVE CALCULATIONS
					//===========================================
					enabledStabilityAnalysis = true;
					
					// the balance analysis is done at this time
					ixxWing = theAircraft
							.getTheAnalysisManager().getTheBalance().getAircraftInertiaMomentIxx()
							.doubleValue(MyUnits.KILOGRAM_METER_SQUARED);
					// TODO: get inertia of the wing
					System.out.println("IxxWing = " + ixxWing + " kg m^2\n");
					
					iyyWing = theAircraft
							.getTheAnalysisManager().getTheBalance().getAircraftInertiaMomentIyy()
							.doubleValue(MyUnits.KILOGRAM_METER_SQUARED);
					// TODO: get inertia of the wing
					System.out.println("IyyWing = " + iyyWing + " kg m^2\n");
					
					izzWing = theAircraft
							.getTheAnalysisManager().getTheBalance().getAircraftInertiaMomentIzz()
							.doubleValue(MyUnits.KILOGRAM_METER_SQUARED);
					// TODO: get inertia of the wing
					System.out.println("IzzWing = " + izzWing + " kg m^2\n");
					
					ixyWing = theAircraft
							.getTheAnalysisManager().getTheBalance().getAircraftInertiaProductIxy()
							.doubleValue(MyUnits.KILOGRAM_METER_SQUARED);
					// TODO: get inertia of the wing
					System.out.println("IxyWing = " + ixyWing + " kg m^2\n");
					
					ixzWing = theAircraft
							.getTheAnalysisManager().getTheBalance().getAircraftInertiaProductIxz()
							.doubleValue(MyUnits.KILOGRAM_METER_SQUARED);
					// TODO: get inertia of the wing
					System.out.println("IxzWing = " + ixzWing + " kg m^2\n");
					
					iyzWing = theAircraft
							.getTheAnalysisManager().getTheBalance().getAircraftInertiaProductIyz()
							.doubleValue(MyUnits.KILOGRAM_METER_SQUARED);
					// TODO: get inertia of the wing
					System.out.println("IyzWing = " + iyzWing + " kg m^2\n");
					
				}
				System.out.println("---------------------------------------------\n");
			}
			// aircraft & aircraft->analysisManager both non null 
			return
					new AVLMassInputData
							.Builder()
							.setDescription("(C) Agostino De Marco, agodemar, mass properties")
							.setLUnit(1.0)
							.setMUnit(1.0)
							.addMassProperties( // wing center panel
								Tuple.of( // TODO
									Tuple.of(massWing, 14.0, 0.0, 0.0), // mass, x, y, z
									Tuple.of(ixxWing, iyyWing, izzWing, ixyWing, ixzWing, iyzWing) // Ixx, Iyy, Izz, Ixy, Ixz, Iyz
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
			
		} else { 
			// Aircraft mass & balance not calculated
			enabledStabilityAnalysis = false;
			return null;
		}
	}

	// TODO: this is the sequence of commands passed to AVL,
	//       modify them as appropriate
	//
	public AVLMacro formRunMacro(OperatingConditions theOperatingConditions) {
		
		String massFileName = null;
		String outputStabilityDerivativesFileName = null;
		String outputStabilityDerivativesBodyAxesFileName = null;
		if (this.enabledStabilityAnalysis) {
			if (this.massFile != null)
				massFileName = this.massFile.getName();
			if (this.outputStabilityDerivativesFile != null)
				outputStabilityDerivativesFileName = this.outputStabilityDerivativesFile.getName();
			if (this.outputStabilityDerivativesBodyAxesFile != null)
				outputStabilityDerivativesBodyAxesFileName = this.outputStabilityDerivativesBodyAxesFile.getName();
		} 
		return
			new AVLMacro()
				.load(this.getInputAVLFile().getName())
				.mass(massFileName) // might be null
				.mset(0)
				.plop("g, F")
				.back()
				.oper()
				.c1()
				.velocity(SpeedCalc.calculateTAS(
						theOperatingConditions.getMachCruise(), 
						theOperatingConditions.getAltitudeCruise(),
						theOperatingConditions.getDeltaTemperatureCruise()
						).doubleValue(SI.METERS_PER_SECOND)
						)
				.back()
				.runCase()
				.stabilityDerivatives(outputStabilityDerivativesFileName) // might be null
				.bodyAxisDerivatives(outputStabilityDerivativesBodyAxesFileName) // might be null
				.back()
				.mode()
				.newEigenmodeCalculation()
				.writeEigenvaluesToFile(this.getBaseName()+".eig")
				.back()
				.quit()
				;
	}
	
	/*
	 *  TODO modify this function as appropriate
	 */
	public String formCommand(
			AVLMainInputData inputData, AVLAircraft aircraft, AVLMassInputData massData,
			AVLMacro avlMacro) {

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
//		AVLInputGenerator.writeDataToAVLFile(inputData, aircraft, this.getInputAVLFile().getAbsolutePath());
		System.out.println("Input AVL file full path: " + this.getInputAVLFile());
		System.out.println("Input AVL file name: " + this.getInputAVLFile().getName());

		// Write out the mass file
		if (massData != null) {
//			AVLInputGenerator.writeDataToMassFile(massData, this.getInputMassFile().getAbsolutePath());
			System.out.println("Input Mass file full path: " + this.getInputMassFile());
			System.out.println("Input Mass file name: " + this.getInputMassFile().getName());
		} else {
			this.enabledStabilityAnalysis = false;
			System.out.println("Mass input not written.");
		}

		// Write out the run file
//		AVLInputGenerator.writeDataToRunFile(avlMacro, this.getInputRunFile().getAbsolutePath());
		
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
	public File getOutputStabilityDerivativesBodyAxesFile() {
		return this.outputStabilityDerivativesBodyAxesFile;
	}

	@Override
	public void setOutputStabilityDerivativesBodyAxesFile(File file) {
		this.outputStabilityDerivativesBodyAxesFile = file;
	}
	
	@Override
	public boolean parseOutputStabilityDerivativesFile() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCommandLineInformation(List<String> cli) {
		this.commandInformation = cli;
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

	public boolean isEnabledStabilityAnalysis() {
		return enabledStabilityAnalysis;
	}

	public void setEnabledStabilityAnalysis(boolean enabledStabilityAnalysis) {
		this.enabledStabilityAnalysis = enabledStabilityAnalysis;
	}

}
