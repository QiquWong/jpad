package it.unina.daf.jpadcad.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import analyses.OperatingConditions;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.occ.OCCSolid;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import writers.JPADStaticWriteUtils;

public final class AircraftUtils {

	public static Aircraft importAircraft(String[] args) {
		
		// redirect console output
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		CmdLineUtils.va = new ArgumentsCADTests();
		CmdLineUtils.theCmdLineParser = new CmdLineParser(CmdLineUtils.va);
		
		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			CmdLineUtils.theCmdLineParser.parseArgument(args);
			
			String pathToXML = CmdLineUtils.va.getInputFile().getAbsolutePath();
			System.out.println("AIRCRAFT INPUT ===> " + pathToXML);

			String pathToAnalysesXML = CmdLineUtils.va.getInputFileAnalyses().getAbsolutePath();
			System.out.println("ANALYSES INPUT ===> " + pathToAnalysesXML);
			
			String pathToOperatingConditionsXML = CmdLineUtils.va.getOperatingConditionsInputFile().getAbsolutePath();
			System.out.println("OPERATING CONDITIONS INPUT ===> " + pathToOperatingConditionsXML);
			
			String dirAirfoil = CmdLineUtils.va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			String dirFuselages = CmdLineUtils.va.getFuselagesDirectory().getCanonicalPath();
			System.out.println("FUSELAGES ===> " + dirFuselages);
			
			String dirLiftingSurfaces = CmdLineUtils.va.getLiftingSurfacesDirectory().getCanonicalPath();
			System.out.println("LIFTING SURFACES ===> " + dirLiftingSurfaces);
			
			String dirEngines = CmdLineUtils.va.getEnginesDirectory().getCanonicalPath();
			System.out.println("ENGINES ===> " + dirEngines);
			
			String dirNacelles = CmdLineUtils.va.getNacellesDirectory().getCanonicalPath();
			System.out.println("NACELLES ===> " + dirNacelles);
			
			String dirLandingGears = CmdLineUtils.va.getLandingGearsDirectory().getCanonicalPath();
			System.out.println("LANDING GEARS ===> " + dirLandingGears);
			
			String dirSystems = CmdLineUtils.va.getSystemsDirectory().getCanonicalPath();
			System.out.println("SYSTEMS ===> " + dirSystems);
			
			String dirCabinConfiguration = CmdLineUtils.va.getCabinConfigurationDirectory().getCanonicalPath();
			System.out.println("CABIN CONFIGURATIONS ===> " + dirCabinConfiguration);
			
			System.out.println("--------------");

			//------------------------------------------------------------------------------------
			// Setup database(s)
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.databaseDirectory,
					MyConfiguration.inputDirectory,
					MyConfiguration.outputDirectory
					);
			
			String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
			String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
			String highLiftDatabaseFileName = "HighLiftDatabase.h5";
			String fusDesDatabaseFilename = "FusDes_database.h5";
			String vedscDatabaseFilename = "VeDSC_database.h5";
			
			AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
					new AerodynamicDatabaseReader(
							databaseFolderPath,	aerodynamicDatabaseFileName
							),
					databaseFolderPath
					);
			HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
					new HighLiftDatabaseReader(
							databaseFolderPath,	highLiftDatabaseFileName
							),
					databaseFolderPath
					);
			FusDesDatabaseReader fusDesDatabaseReader = DatabaseManager.initializeFusDes(
					new FusDesDatabaseReader(
							databaseFolderPath,	fusDesDatabaseFilename
							),
					databaseFolderPath
					);
			VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
					new VeDSCDatabaseReader(
							databaseFolderPath,	vedscDatabaseFilename
							),
					databaseFolderPath
					);

			////////////////////////////////////////////////////////////////////////
			// Aircraft creation
			System.out.println("Creating the Aircraft ... ");
			
			// deactivating system.out
			System.setOut(filterStream);
			
			// default Aircraft ATR-72 ...
//			theAircraft = new Aircraft.AircraftBuilder(
//					"ATR-72",
//					AircraftEnum.ATR72,
//					aeroDatabaseReader,
//					highLiftDatabaseReader,
//			        fusDesDatabaseReader,
//					veDSCDatabaseReader
//					)
//					.build();

//			AircraftSaveDirectives asd = new AircraftSaveDirectives
//					.Builder("_ATR72")
//					.addAllWingAirfoilFileNames(
//							theAircraft.getWing().getAirfoilList().stream()
//									.map(a -> a.getAirfoilCreator().getName() + ".xml")
//									.collect(Collectors.toList())
//						)
//					.addAllHTailAirfoilFileNames(
//							theAircraft.getHTail().getAirfoilList().stream()
//									.map(a -> a.getAirfoilCreator().getName() + ".xml")
//									.collect(Collectors.toList())
//						)
//					.addAllVTailAirfoilFileNames(
//							theAircraft.getVTail().getAirfoilList().stream()
//									.map(a -> a.getAirfoilCreator().getName() + ".xml")
//									.collect(Collectors.toList())
//						)
//					.build();
//			
//			JPADStaticWriteUtils.saveAircraftToXML(theAircraft, MyConfiguration.getDir(FoldersEnum.INPUT_DIR), "aircraft_ATR72", asd);
			
			// reading aircraft from xml ... 
			Aircraft aircraft = Aircraft.importFromXML(
					pathToXML,
					dirLiftingSurfaces,
					dirFuselages,
					dirEngines,
					dirNacelles,
					dirLandingGears,
					dirSystems,
					dirCabinConfiguration,
					dirAirfoil,
					aeroDatabaseReader,
					highLiftDatabaseReader,
					fusDesDatabaseReader,
					veDSCDatabaseReader);
			
			// activating system.out
//			System.setOut(originalOut);			
//			System.out.println(aircraft.toString());
//			System.setOut(filterStream);
			
			////////////////////////////////////////////////////////////////////////
			// Set the folders tree
			MyConfiguration.initWorkingDirectoryTree(
					MyConfiguration.currentDirectoryString,
					MyConfiguration.inputDirectory, 
					MyConfiguration.outputDirectory);
			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
			String aircraftFolder = JPADStaticWriteUtils.createNewFolder(folderPath + aircraft.getId() + File.separator);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(aircraftFolder);

			////////////////////////////////////////////////////////////////////////
			// Defining the operating conditions ...
			System.setOut(originalOut);
			System.out.println("Defining the operating conditions ... ");
			System.setOut(filterStream);
			OperatingConditions theOperatingConditions = OperatingConditions.importFromXML(pathToOperatingConditionsXML);
//			System.setOut(originalOut);
			System.out.println(theOperatingConditions.toString());
//			System.setOut(filterStream);
			
			
			System.setOut(originalOut);
			return aircraft;
			
		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			CmdLineUtils.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return null;
		}	
		
	}

	/**
	 * Creates a list of shapes, mostly surfaces/shells, representing the fuselage.
	 * @see getFuselageCAD(Fuselage fuselage, double noseFirstCapSectionFactor1, double noseFirstCapSectionFactor2, int numberNoseCapSections, int numberNosePatch2Sections, boolean exportSupportShapes)
	 * 
	 * noseFirstCapSectionFactor1 = 0.15, noseFirstCapSectionFactor2 = 1.00, numberNoseCapSections = 3, numberNosePatch2Sections = 9, numberTailPatchSections = 5, numberTailCapSections = 3
	 * 
	 * @param fuselage				the fuselage object, extracted from a Aircraft object
	 * @param exportSupportShapes	include supporting sections, outline curves, etc in the output shape list 
	 * @return
	 */
	public static List<OCCShape> getFuselageCAD(Fuselage fuselage, boolean exportSupportShapes) {
		return getFuselageCAD(fuselage, 0.15, 1.0, 3, 9, 7, 1.0, 0.10, 3, true, exportSupportShapes);
	}
	
	/**
	 * Creates a list of shapes, mostly surfaces/shells, representing the fuselage.
	 * 
	 * Nose trunk patches. Patch-1, Patch-2:
	 * First, the nose patch is created, as the union of two patches: Patch-1, i.e. Nose Cap Patch, and Patch-2, i.e. from cap terminal section to
	 * the nose trunk terminal section. Patch-1 has numberNoseCapSections supporting section curves and is constrained to include the fuselage foremost tip vertex.
	 * Patch-1 passes thru: nose tip vertex, support-section-1, support-section-2, ... support-section-<numberNoseCapSections>. The last section of Patch-1 coincides
	 * with the first support section of Patch-2, which passes thru: support-section-<numberNoseCapSections>, support-section-<numberNoseCapSections + 1>, ... 
	 * support-section-<numberNoseCapSections + numberNosePatch2Sections>.
	 * 
	 * Cylindrical trunk patch:
	 * Patch-3
	 * 
	 * Tail cone trunk patch:
	 * Patch-4
	 * 
	 * Tail cap patch:
	 * Patch-5
	 * 
	 * @param fuselage 						the fuselage object, extracted from a Aircraft object
	 * @param noseCapSectionFactor1 		the factor multiplying xNoseCap/noseCapLength to obtain the first support section of Patch-1, e.g. 0.15
	 * @param noseCapSectionFactor2			the factor multiplying xNoseCap/noseCapLength to obtain the last support section of Patch-1, e.g. 1.0 (>1.0 means x > xNoseCap) 
	 * @param numberNoseCapSections			number of Patch-1 supporting sections, e.g. 3 
	 * @param numberNosePatch2Sections		number of Patch-2 supporting sections, e.g. 9
	 * @param numberTailPatchSections		number of Patch-4 supporting sections, e.g. 5
	 * @param tailCapSectionFactor1 		the factor multiplying (fuselageLength - xTailCap)/tailCapLength to obtain the first support section of Patch-5, e.g. 1.0 (>1.0 means x < xFusLength - tailCapLength)
	 * @param tailCapSectionFactor2 	    the factor multiplying (fuselageLength - xTailCap)/tailCapLength to obtain the last support section of Patch-5, e.g. 0.15
	 * @param numberTailCapSections			number of Patch-5 supporting sections, e.g. 3 
	 * @param exportLoft					include fuselage loft in the output shape list 
	 * @param exportSupportShapes			include supporting sections, outline curves, etc in the output shape list 
	 * @return
	 */
	public static List<OCCShape> getFuselageCAD(Fuselage fuselage,
			double noseCapSectionFactor1, double noseCapSectionFactor2, int numberNoseCapSections, 
			int numberNosePatch2Sections, int numberTailPatchSections, double tailCapSectionFactor1, double tailCapSectionFactor2, int numberTailCapSections,
			boolean exporLoft,
			boolean exportSupportShapes) {
		if (fuselage == null)
			return null;
		if (OCCUtils.theFactory == null)
			return null;
			
		System.out.println("========== [AircraftUtils::getFuselageCAD] ");
		List<OCCShape> ret = new ArrayList<>();

		Amount<Length> noseLength = fuselage.getFuselageCreator().getLengthNoseTrunk();
		System.out.println("Nose length: " + noseLength);
		Amount<Length> noseCapStation = fuselage.getFuselageCreator().getDxNoseCap();
		System.out.println("Nose cap x-station: " + noseCapStation);
		Double xbarNoseCap = fuselage.getNoseDxCapPercent(); // normalized with noseLength
		System.out.println("Nose cap x-station normalized: " + xbarNoseCap);
		Amount<Length> zNoseTip = Amount.valueOf( 
				fuselage.getFuselageCreator().getZOutlineXZLowerAtX(0.0),
				SI.METER);
		System.out.println("Nose tip z: " + zNoseTip);

		System.out.println("========== [AircraftUtils::getFuselageCAD] Nose cap, from nose tip: x = 0 m to x=" + noseCapStation);
		
		System.out.println("Getting selected sections ...");
		// all xbar's are normalized with noseLength
		List<Double> xbars1 = Arrays.asList(
//				new Double[] {
//						0.2*xbarNoseCap, 0.4*xbarNoseCap, 0.8*xbarNoseCap, 1.0*xbarNoseCap}
				MyArrayUtils
					// .linspaceDouble(
					.halfCosine2SpaceDouble(
					// .cosineSpaceDouble(
					noseCapSectionFactor1*xbarNoseCap, noseCapSectionFactor2*xbarNoseCap, 
					numberNoseCapSections) // n. points
				);
		
		System.out.println("Nose-cap trunk selected sections, Patch-1, normalized x-stations: " + xbars1.toString());

		List<List<PVector>> sections1 = new ArrayList<List<PVector>>();
		xbars1.stream()
			  .forEach(x -> sections1.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		if(exporLoft) {
			System.out.println("Constructing the nose-cap patch, Patch-1");
			OCCShape patch1 = 
					OCCUtils.makePatchThruSectionsP(
							new PVector(0.0f, 0.0f, (float) zNoseTip.doubleValue(SI.METER)), // Nose tip vertex
							sections1
							);

			ret.add(patch1); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-1, loft: nose cap
		}
		
		System.out.println("========== [AircraftUtils::getFuselageCAD] Nose trunk (no cap): x=" + noseCapStation + " to x=" + noseLength);
		
		System.out.println("Getting selected sections ...");

		// all xbar's are normalized with noseLength
		List<Double> xbars2 = Arrays.asList(
//				new Double[] {
//						xbarNoseCap, 0.2, 0.4, 0.6, 0.8, 1.0}
				MyArrayUtils
				// .linspaceDouble(
				// .halfCosine1SpaceDouble(
				.cosineSpaceDouble(
					noseCapSectionFactor2*xbarNoseCap, 1.0, 
					numberNosePatch2Sections) // n. points
				);

		System.out.println("Nose trunk selected sections, Patch-2, normalized x-stations: " + xbars2.toString());

		List<List<PVector>> sections2 = new ArrayList<List<PVector>>();
		xbars2.stream()
			  .forEach(x -> sections2.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		if(exporLoft) {
			System.out.println("Constructing the nose patch, Patch-2");
			OCCShape patch2 = OCCUtils.makePatchThruSectionsP(sections2);

			ret.add(patch2); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-2, loft: nose patch
		}
		
		// nose Patch-2 terminal section
		CADGeomCurve3D cadCrvCylinderInitialSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength), false);

		Amount<Length> cylinderLength = fuselage.getFuselageCreator().getLengthCylindricalTrunk();
		
		System.out.println("========== [AircraftUtils::getFuselageCAD] Fuselage cylindrical trunk: x=" + noseLength + " to x=" + noseLength.plus(cylinderLength));

		// Cylindrical trunk mid section
		CADGeomCurve3D cadCrvCylinderMidSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
						noseLength.plus(cylinderLength.times(0.5))),false);

		// Cylindrical trunk terminal section
		CADGeomCurve3D cadCrvCylinderTerminalSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
						noseLength.plus(cylinderLength)), false);

		if(exporLoft) {
			OCCShape patch3 = OCCUtils.makePatchThruSections(
					cadCrvCylinderInitialSection, cadCrvCylinderMidSection, cadCrvCylinderTerminalSection);

			ret.add(patch3); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-3, loft: cylinder
			
			// TODO: fixme and OCCSolid
//			OCCSolid solid3 = new OCCSolid(patch3);
//			System.out.println("Solid volume = " + solid3.getVolume());
		}
		
		// Tail trunk
		Amount<Length> tailLength = fuselage.getFuselageCreator().getLengthTailTrunk();
		Amount<Length> tailCapLength = fuselage.getFuselageCreator().getDxTailCap();
		Amount<Length> fuselageLength = fuselage.getLength();

		System.out.println("========== [AircraftUtils::getFuselageCAD] Tail trunk (no cap): x=" 
				+ noseLength.plus(cylinderLength) + " to x=" + fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)) + " (fus. length - tail cap length)"
				);

		// x stations defining cylinder outlines
		List<Double> xmtPatch4 = Arrays.asList(
				MyArrayUtils.halfCosine1SpaceDouble( // cosineSpaceDouble( // 
						noseLength.plus(cylinderLength).doubleValue(SI.METER), 
						fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)).doubleValue(SI.METER),
						numberTailPatchSections) // n. points
				);
		
		List<CADGeomCurve3D> cadCurvesTailTrunk = new ArrayList<>();
		xmtPatch4.stream()
				 .map(x -> Amount.valueOf(x, SI.METER))
				 .forEach(x -> cadCurvesTailTrunk.add(
						 OCCUtils.theFactory
							.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(x), false)
						 	)
						 );
		if(exporLoft) {
			OCCShape patch4 = OCCUtils.makePatchThruSections(
					cadCurvesTailTrunk);

			ret.add(patch4); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-4, loft: tail
		}
		
		// tail cap patch

		System.out.println("========== [AircraftUtils::getFuselageCAD] Fuselage tail cap trunk: x=" 
				+ fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)) + " to x=" + fuselageLength + " (fus. total length)"
				);

		// x stations in tail cap
		List<Double> xmtPatch5 = Arrays.asList(
				MyArrayUtils.halfCosine2SpaceDouble(
						fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)).doubleValue(SI.METER), 
						fuselageLength.minus(tailCapLength.times(tailCapSectionFactor2)).doubleValue(SI.METER), // tweak to avoid a degenerate section 
						numberTailCapSections) // n. points
				);

		Amount<Length> zTailTip = Amount.valueOf( 
				fuselage.getFuselageCreator().getZOutlineXZLowerAtX(fuselageLength.doubleValue(SI.METER)),
				SI.METER);
		
		CADVertex vertexTailTip = OCCUtils.theFactory.newVertex(
				fuselageLength.doubleValue(SI.METER), 0, zTailTip.doubleValue(SI.METER));
		
		List<CADGeomCurve3D> cadCurvesTailCapTrunk = new ArrayList<>();
		xmtPatch5.stream()
				 .map(x -> Amount.valueOf(x, SI.METER))
				 .forEach(x -> cadCurvesTailCapTrunk.add(
						 OCCUtils.theFactory
							.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(x), false)
						 	)
						 );

		if(exporLoft) {
			OCCShape patch5 = OCCUtils.makePatchThruSections(cadCurvesTailCapTrunk, vertexTailTip);
			ret.add(patch5); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-5, loft: tail cap
		}
		
		
		if (exportSupportShapes) {
			
			System.out.println("========== [AircraftUtils::getFuselageCAD] adding support cad entities");
			List<OCCShape> extraShapesCap = new ArrayList<>();

			// other nose cap entities (outline curves, vertices)
			CADVertex vertexNoseTip = OCCUtils.theFactory.newVertex(0, 0, zNoseTip.doubleValue(SI.METER));
			// nose cap terminal section
			List<PVector> sectionCapTerminal = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseCapStation);
			CADGeomCurve3D cadCrvCapTerminalSection = OCCUtils.theFactory
					.newCurve3D(
						sectionCapTerminal.stream()
							.map(p -> new double[]{p.x, p.y, p.z})
							.collect(Collectors.toList()),
						false);
			// x stations defining cap outlines
			List<Double> xxPatch1 = Arrays.asList(
					MyArrayUtils.halfCosine1SpaceDouble(
							0.0, noseCapStation.doubleValue(SI.METER), 
							numberNoseCapSections) // n. points
					);
			// points z's on nose outline curve, XZ, upper
			List<double[]> pointsCapXZUpper = xxPatch1.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
					})
					.collect(Collectors.toList());
			// points z's on nose outline curve, XZ, lower
			List<double[]> pointsCapXZLower = xxPatch1.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
					})
					.collect(Collectors.toList());
			// points y's on nose outline curve, XY, right
			List<double[]> pointsCapSideRight = xxPatch1.stream()
					.map(x -> new double[]{
							x,
							fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
							fuselage.getFuselageCreator().getCamberZAtX(x)
					})
					.collect(Collectors.toList());

			CADGeomCurve3D cadCrvCapXZUpper = OCCUtils.theFactory
					.newCurve3D(pointsCapXZUpper, false);
			CADGeomCurve3D cadCrvCapXZLower = OCCUtils.theFactory
					.newCurve3D(pointsCapXZLower, false);
			CADGeomCurve3D cadCrvCapXYRight = OCCUtils.theFactory
					.newCurve3D(pointsCapSideRight, false);
			
			extraShapesCap.add((OCCVertex)vertexNoseTip);
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapXZUpper).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapXZLower).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapXYRight).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCapTerminalSection).edge());
			
			// support sections of nose cap
			sections1.stream()
			         .map(sec -> OCCUtils.theFactory
								         .newCurve3D(
										   sec.stream()
										      .map(p -> new double[]{p.x, p.y, p.z})
										      .collect(Collectors.toList()),
										   false)
			         )
			         .map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
			         .forEach(e -> extraShapesCap.add(e));
			
			// nose outline curves
			// x stations defining nose Patch-2 outlines
			List<Double> xxPatch2 = Arrays.asList(
					MyArrayUtils.halfCosine1SpaceDouble(
							noseCapStation.doubleValue(SI.METER), noseLength.doubleValue(SI.METER), 
							numberNosePatch2Sections) // n. points
					);
			// points z's on nose outline curve, XZ, upper
			List<double[]> pointsNoseXZUpper = xxPatch2.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
					})
					.collect(Collectors.toList());
			// points z's on nose outline curve, XZ, lower
			List<double[]> pointsNoseXZLower = xxPatch2.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
					})
					.collect(Collectors.toList());
			
			CADGeomCurve3D cadCrvNoseXZUpper = OCCUtils.theFactory
					.newCurve3D(pointsNoseXZUpper, false);
			CADGeomCurve3D cadCrvNoseXZLower = OCCUtils.theFactory
					.newCurve3D(pointsNoseXZLower, false);
			
			List<double[]> pointsNoseSideRight = xxPatch2.stream()
					.map(x -> new double[]{
							x,
							fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
							fuselage.getFuselageCreator().getCamberZAtX(x)
					})
					.collect(Collectors.toList());
			
			CADGeomCurve3D cadCrvNoseXYRight = OCCUtils.theFactory
					.newCurve3D(pointsNoseSideRight, false);
			
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseXZUpper).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseXZLower).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseXYRight).edge());			
			
			// support sections of nose patch-2
			sections2.stream()
			         .map(sec -> OCCUtils.theFactory
								         .newCurve3D(
										   sec.stream()
										      .map(p -> new double[]{p.x, p.y, p.z})
										      .collect(Collectors.toList()),
										   false)
			         )
			         .map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
			         .forEach(e -> extraShapesCap.add(e));
			
			// support sections of cylinder, patch-3

			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderInitialSection).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderMidSection).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderTerminalSection).edge());

			// x stations defining cylinder outlines
			List<Double> xmtPatch3 = Arrays.asList(
					MyArrayUtils.halfCosine1SpaceDouble(
							noseLength.doubleValue(SI.METER), noseLength.plus(cylinderLength).doubleValue(SI.METER), 
							3) // n. points
					);

			// points z's on nose outline curve, XZ, upper
			List<double[]> pointsCylinderXZUpper = xmtPatch3.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
					})
					.collect(Collectors.toList());
			// points z's on nose outline curve, XZ, lower
			List<double[]> pointsCylinderXZLower = xmtPatch3.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
					})
					.collect(Collectors.toList());
			
			// cylinder outline curves
			CADGeomCurve3D cadCrvCylinderXZUpper = OCCUtils.theFactory
					.newCurve3D(pointsCylinderXZUpper, false);
			CADGeomCurve3D cadCrvCylinderXZLower = OCCUtils.theFactory
					.newCurve3D(pointsCylinderXZLower, false);
			
			// cylinder side curve
			List<double[]> pointsCylinderSideRight = xmtPatch3.stream()
					.map(x -> new double[]{
							x,
							fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
							fuselage.getFuselageCreator().getCamberZAtX(x)
					})
					.collect(Collectors.toList());
			
			CADGeomCurve3D cadCylinderXYRight = OCCUtils.theFactory
					.newCurve3D(pointsCylinderSideRight, false);

			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderXZUpper).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderXZLower).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCylinderXYRight).edge());
			
			// tail trunk
			// points z's on nose outline curve, XZ, upper
			List<double[]> pointsTailXZUpper = xmtPatch4.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
					})
					.collect(Collectors.toList());
			// points z's on nose outline curve, XZ, lower
			List<double[]> pointsTailXZLower = xmtPatch4.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
					})
					.collect(Collectors.toList());
			
			CADGeomCurve3D cadCrvTailXZUpper = OCCUtils.theFactory
					.newCurve3D(pointsTailXZUpper, false);
			CADGeomCurve3D cadCrvTailXZLower = OCCUtils.theFactory
					.newCurve3D(pointsTailXZLower, false);

			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvTailXZUpper).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadCrvTailXZLower).edge());
			
			// tail side curve
			List<double[]> pointsTailSideRight = xmtPatch4.stream()
					.map(x -> new double[]{
							x,
							fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
							fuselage.getFuselageCreator().getCamberZAtX(x)
					})
					.collect(Collectors.toList());
			
			CADGeomCurve3D cadTailXYRight = OCCUtils.theFactory
					.newCurve3D(pointsTailSideRight, false);

			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadTailXYRight).edge());
			
			// support sections of tail, patch-4
			cadCurvesTailTrunk.stream()
			         .map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
			         .forEach(e -> extraShapesCap.add(e));			
			
			// tail cap support entities (outline curves, vertices)

			cadCurvesTailCapTrunk.stream()
	         .map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
	         .forEach(e -> extraShapesCap.add(e));			
			
			// points z's on tail cap outline curve, XZ, upper
			List<double[]> pointsTailCapXZUpper = xmtPatch5.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
					})
					.collect(Collectors.toList());
			pointsTailCapXZUpper.add(vertexTailTip.pnt()); // add tail tip point
			// points z's on nose outline curve, XZ, lower
			List<double[]> pointsTailCapXZLower = xmtPatch5.stream()
					.map(x -> new double[]{
							x,
							0.0,
							fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
					})
					.collect(Collectors.toList());
			pointsTailCapXZLower.add(vertexTailTip.pnt()); // add tail tip point
			
			CADGeomCurve3D cadTailCapXZUpper = OCCUtils.theFactory
					.newCurve3D(pointsTailCapXZUpper, false);
			CADGeomCurve3D cadTailCapXZLower = OCCUtils.theFactory
					.newCurve3D(pointsTailCapXZLower, false);
			
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadTailCapXZUpper).edge());
			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadTailCapXZLower).edge());
			
			// tail side curve
			List<double[]> pointsTailCapSideRight = xmtPatch5.stream()
					.map(x -> new double[]{
							x,
							fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
							fuselage.getFuselageCreator().getCamberZAtX(x)
					})
					.collect(Collectors.toList());
			pointsTailCapSideRight.add(vertexTailTip.pnt()); // add tail tip point
			
			CADGeomCurve3D cadTailCapXYRight = OCCUtils.theFactory
					.newCurve3D(pointsTailCapSideRight, false);

			extraShapesCap.add((OCCEdge)((OCCGeomCurve3D)cadTailCapXYRight).edge());
			
			// finally add all to extra shapes
			ret.addAll(extraShapesCap);
		}
		
		return ret;
	}
}
