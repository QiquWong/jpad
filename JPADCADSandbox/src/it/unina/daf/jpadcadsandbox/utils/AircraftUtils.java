package it.unina.daf.jpadcadsandbox.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
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
		
		CmdLineUtils.va = new ArgumentsJPADCADSandbox();
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
//					MyConfiguration.databaseDirectory,  // Used only for default location.
					MyConfiguration.inputDirectory,
					MyConfiguration.outputDirectory
					);
			
			// Overriding default database directory path
			MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, CmdLineUtils.va.getDatabaseDirectory().getAbsolutePath());
			
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
	 * @see getFuselageCAD(Fuselage fuselage, double noseFirstCapSectionFactor1, double noseFirstCapSectionFactor2, int numberNoseCapSections, int numberNosePatch2Sections, boolean exportSupportShapes)
	 * 
	 * noseFirstCapSectionFactor1 = 0.15, noseFirstCapSectionFactor2 = 1.00, numberNoseCapSections = 3, numberNosePatch2Sections = 9, numberTailPatchSections = 5, numberTailCapSections = 3
	 * 
	 * @param fuselage				the fuselage object, extracted from a Aircraft object
	 * @param exportLofts			include fuselage loft in the output shape list 
	 * @param exportSupportShapes	include supporting sections, outline curves, etc in the output shape list 
	 * @return
	 */
	public static List<OCCShape> getFuselageCAD(Fuselage fuselage, boolean exportLofts, boolean exportSupportShapes) {
		return getFuselageCAD(fuselage, 0.15, 1.0, 3, 9, 7, 1.0, 0.10, 3, exportLofts, exportSupportShapes);
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
	 * @param exportLofts					include fuselage loft in the output shape list 
	 * @param exportSupportShapes			include supporting sections, outline curves, etc in the output shape list 
	 * @return
	 */
	public static List<OCCShape> getFuselageCAD(Fuselage fuselage,
			double noseCapSectionFactor1, double noseCapSectionFactor2, int numberNoseCapSections, 
			int numberNosePatch2Sections, int numberTailPatchSections, double tailCapSectionFactor1, double tailCapSectionFactor2, int numberTailCapSections,
			boolean exportLofts,
			boolean exportSupportShapes) {
		if (fuselage == null)
			return null;
		
		System.out.println("========== [AircraftUtils::getFuselageCAD] ");

		if (OCCUtils.theFactory == null) {
			System.out.println("========== [AircraftUtils::getFuselageCAD] Initialize CAD shape factory");
			OCCUtils.initCADShapeFactory(); // theFactory now non-null
		}
		
		OCCShape patch1 = null, // nose cap 
				patch2 = null, // nose trunk
				patch3 = null, // cylindrical trunk 
				patch4 = null, // tail trunk 
				patch5 = null; // tail cap
		
		List<OCCShape> result = new ArrayList<>();
		List<OCCShape> extraShapes = new ArrayList<>();
		
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
				MyArrayUtils
					// .linspaceDouble(
					.halfCosine2SpaceDouble(
					// .cosineSpaceDouble(
					noseCapSectionFactor1*xbarNoseCap, noseCapSectionFactor2*xbarNoseCap, 
					numberNoseCapSections) // n. points
				);
		
		List<List<PVector>> sections1 = new ArrayList<List<PVector>>();
		xbars1.stream()
			  .forEach(x -> sections1.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		// x stations defining cap outlines
		List<Double> xmtPatch1 = new ArrayList<>();
		xmtPatch1.add(0.0); // nose tip
		xbars1.stream()
			  .forEach(x -> xmtPatch1.add(x*noseLength.doubleValue(SI.METER)));
		
		System.out.println("Nose-cap trunk selected x-stations (m), Patch-1: " + xmtPatch1.toString());
		
		if (exportLofts) {
			// <<<<<<<<<<<<<<<<<<<<<<<< Patch-1, loft: nose cap
			System.out.println("Constructing the nose-cap patch, Patch-1");
			patch1 = 
					OCCUtils.makePatchThruSectionsP(
							new PVector(0.0f, 0.0f, (float) zNoseTip.doubleValue(SI.METER)), // Nose tip vertex
							sections1
							);

		}
		
		System.out.println("========== [AircraftUtils::getFuselageCAD] Nose trunk (no cap): x=" + noseCapStation + " to x=" + noseLength);
		
		System.out.println("Getting selected sections ...");

		// all xbar's are normalized with noseLength
		List<Double> xbars2 = Arrays.asList(
				MyArrayUtils
				// .linspaceDouble(
				// .halfCosine1SpaceDouble(
				.cosineSpaceDouble(
					noseCapSectionFactor2*xbarNoseCap, 1.0, 
					numberNosePatch2Sections) // n. points
				);

		// x stations defining nose outlines
		List<Double> xmtPatch2 = new ArrayList<>();
		xbars2.stream()
			  .forEach(x -> xmtPatch2.add(x*noseLength.doubleValue(SI.METER)));
		
		System.out.println("Nose trunk selected x-stations (m), Patch-2: " + xmtPatch2.toString());

//		List<List<PVector>> sections2 = new ArrayList<List<PVector>>();
//		xbars2.stream()
//			  .forEach(x -> sections2.add(
//					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
//			  );

		List<CADGeomCurve3D> cadCurvesNoseTrunk = new ArrayList<>();
		xmtPatch2.stream()
				 .map(x -> Amount.valueOf(x, SI.METER))
				 .forEach(x -> cadCurvesNoseTrunk.add(
						 OCCUtils.theFactory
							.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(x), false)
						 	)
						 );
		
		if (exportLofts) {
			// <<<<<<<<<<<<<<<<<<<<<<<< Patch-2, loft: nose patch
			System.out.println("Constructing the nose patch, Patch-2");
			// patch2 = OCCUtils.makePatchThruSectionsP(sections2);
			patch2 = OCCUtils.makePatchThruSections(cadCurvesNoseTrunk);
		}
		
		// nose Patch-2 terminal section
		CADGeomCurve3D cadCrvCylinderInitialSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength), false);

		Amount<Length> cylinderLength = fuselage.getFuselageCreator().getLengthCylindricalTrunk();
		
		System.out.println("========== [AircraftUtils::getFuselageCAD] Fuselage cylindrical trunk: x=" + noseLength + " to x=" + noseLength.plus(cylinderLength));

		// x stations defining cylinder outlines
		List<Double> xmtPatch3 = Arrays.asList(
				MyArrayUtils.linspaceDouble(
						noseLength.doubleValue(SI.METER), noseLength.plus(cylinderLength).doubleValue(SI.METER), 
						3) // n. points
				);
		
		System.out.println("Cylinder trunk selected x-stations (m), Patch-3: " + xmtPatch3.toString());
		
		// Cylindrical trunk mid section
		CADGeomCurve3D cadCrvCylinderMidSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
						noseLength.plus(cylinderLength.times(0.5))),false);

		// Cylindrical trunk terminal section
		CADGeomCurve3D cadCrvCylinderTerminalSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
						noseLength.plus(cylinderLength)), false);

		if (exportLofts) {
			// <<<<<<<<<<<<<<<<<<<<<<<< Patch-3, loft: cylinder
			patch3 = OCCUtils.makePatchThruSections(
					cadCrvCylinderInitialSection, cadCrvCylinderMidSection, cadCrvCylinderTerminalSection);
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
		
		System.out.println("Tail trunk selected x-stations (m), Patch-4: " + xmtPatch4.toString());
		
		List<CADGeomCurve3D> cadCurvesTailTrunk = new ArrayList<>();
		xmtPatch4.stream()
				 .map(x -> Amount.valueOf(x, SI.METER))
				 .forEach(x -> cadCurvesTailTrunk.add(
						 OCCUtils.theFactory
							.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(x), false)
						 	)
						 );
		if(exportLofts) {
			// <<<<<<<<<<<<<<<<<<<<<<<< Patch-4, loft: tail
			patch4 = OCCUtils.makePatchThruSections(
					cadCurvesTailTrunk);
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

		System.out.println("Tail cap trunk selected x-stations (m), Patch-5: " + xmtPatch5.toString());
		
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

		if (exportLofts) {
			// <<<<<<<<<<<<<<<<<<<<<<<< Patch-5, loft: tail cap
			patch5 = OCCUtils.makePatchThruSections(cadCurvesTailCapTrunk, vertexTailTip);
		}
		
		// TODO: make this as a parameter
		boolean exporSolid = true;
		
		BRepBuilderAPI_Sewing sewMaker = new BRepBuilderAPI_Sewing();
		
		if (exportLofts) {
			// Sewing the lofts
			sewMaker.Init();
			sewMaker.Add(patch1.getShape());
			sewMaker.Add(patch2.getShape());
			sewMaker.Add(patch3.getShape());
			sewMaker.Add(patch4.getShape());
			sewMaker.Add(patch5.getShape());
			sewMaker.Perform(); // actually compute sewing. Never forget this step!
			
			System.out.println("========== [AircraftUtils::getFuselageCAD] Sewing step successful? " + !sewMaker.IsNull());

			if (!sewMaker.IsNull()) {
				TopoDS_Shape tds_shape = sewMaker.SewedShape();
				// The resulting shape may consist of multiple shapes!
				// Use TopExp_Explorer to iterate through shells
				System.out.println(OCCUtils.reportOnShape(tds_shape, "Fuselage sewed surface (Right side)"));
				
				List<OCCShape> sewedShapes = new ArrayList<>();
				TopExp_Explorer exp = new TopExp_Explorer(tds_shape, TopAbs_ShapeEnum.TopAbs_SHELL);
				while (exp.More() > 0) {
					sewedShapes.add((OCCShape)OCCUtils.theFactory.newShape(exp.Current()));
					exp.Next();
				}
				System.out.println("========== [AircraftUtils::getFuselageCAD] Exporting sewed loft.");
				result.addAll(sewedShapes);
				
				// >>>>>>>>>>>>>>>>>>>>>>>>>>>> MIRRORING

				System.out.println("========== [AircraftUtils::getFuselageCAD] Mirroring sewed lofts.");
			 	gp_Trsf mirrorTransform = new gp_Trsf();
			 	gp_Ax2 mirrorPointPlane = new gp_Ax2(
			 			new gp_Pnt(0.0, 0.0, 0.0),
			 			new gp_Dir(0.0, 1.0, 0.0), // Y dir normal to reflection plane XZ
			 			new gp_Dir(1.0, 0.0, 0.0)
			 			);
			 	mirrorTransform.SetMirror(mirrorPointPlane);
				BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
			 	
				List<OCCShape> mirroredShapes = new ArrayList<>();
				sewedShapes.stream()
					.map(occshape -> occshape.getShape())
					.forEach(s -> {
						mirrorBuilder.Perform(s, 1);
						TopoDS_Shape sMirrored = mirrorBuilder.Shape();
						mirroredShapes.add(
								(OCCShape)OCCUtils.theFactory.newShape(sMirrored)
								);
					});
				System.out.println("Mirrored shapes: " + mirroredShapes.size());
				System.out.println("========== [AircraftUtils::getFuselageCAD] Exporting mirrored sewed loft.");
				result.addAll(mirroredShapes);
				
				// TODO: make a solid from the two halves (right/left)
				exporSolid = true;
				if (exporSolid) {
					System.out.println("========== [AircraftUtils::getFuselageCAD] Experimental: build a solid ...");
					CADSolid solidFuselage = null;
					BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
					sewedShapes.stream()
						.forEach( sh -> {
							TopoDS_Shape tds_shape1 = sh.getShape();
							TopExp_Explorer exp1 = new TopExp_Explorer(tds_shape1, TopAbs_ShapeEnum.TopAbs_SHELL);
							solidMaker.Add(TopoDS.ToShell(exp1.Current()));					
						});
					mirroredShapes.stream()
					.forEach( sh -> {
						TopoDS_Shape tds_shape2 = sh.getShape();
						TopExp_Explorer exp2 = new TopExp_Explorer(tds_shape2, TopAbs_ShapeEnum.TopAbs_SHELL);
						solidMaker.Add(TopoDS.ToShell(exp2.Current()));					
					});
					solidMaker.Build();
					System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
					if (solidMaker.IsDone() == 1) {
						solidFuselage = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
						result.add((OCCShape) solidFuselage);
						
						System.out.println(OCCUtils.reportOnShape(((OCCShape) solidFuselage).getShape(), "Fuselage solid (Right + Left)"));

					}
				}				
				
			} else {
				// add patches one by one
				result.add(patch1); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-1, loft: nose cap
				result.add(patch2); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-2, loft: nose patch
				result.add(patch3); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-3, loft: cylinder
				result.add(patch4); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-4, loft: tail
				result.add(patch5); // <<<<<<<<<<<<<<<<<<<<<<<< Patch-5, loft: tail cap				
				System.out.println("========== [AircraftUtils::getFuselageCAD] Exporting un-sewed lofts.");
				if (exporSolid)
					System.out.println("========== [AircraftUtils::getFuselageCAD] Sewing failed, solid not created.");
			}
		}
		
		// other nose cap entities (outline curves, vertices)
		CADVertex vertexNoseTip = OCCUtils.theFactory.newVertex(0, 0, zNoseTip.doubleValue(SI.METER));
		// nose cap terminal section
		List<PVector> sectionCapTerminal = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseCapStation);
		CADGeomCurve3D cadCrvNoseCapTerminalSection = OCCUtils.theFactory
				.newCurve3D(
						sectionCapTerminal.stream()
						.map(p -> new double[]{p.x, p.y, p.z})
						.collect(Collectors.toList()),
						false);
		// points z's on nose outline curve, XZ, upper
		List<double[]> pointsNoseCapXZUpper = xmtPatch1.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
				})
				.collect(Collectors.toList());		
		// points z's on nose outline curve, XZ, lower
		List<double[]> pointsNoseCapXZLower = xmtPatch1.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
				})
				.collect(Collectors.toList());
		// points y's on nose outline curve, XY, right
		List<double[]> pointsNoseCapSideRight = xmtPatch1.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
				})
				.collect(Collectors.toList());
		
		CADGeomCurve3D cadCrvNoseCapXZUpper = OCCUtils.theFactory
				.newCurve3D(pointsNoseCapXZUpper, false);
		CADGeomCurve3D cadCrvNoseCapXZLower = OCCUtils.theFactory
				.newCurve3D(pointsNoseCapXZLower, false);
		CADGeomCurve3D cadCrvNoseCapXYRight = OCCUtils.theFactory
				.newCurve3D(pointsNoseCapSideRight, false);
		
		extraShapes.add((OCCVertex)vertexNoseTip);
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseCapXZUpper).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseCapXZLower).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseCapXYRight).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseCapTerminalSection).edge());
		
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
			.forEach(e -> extraShapes.add(e));
		
		// nose outline curves
		// points z's on nose outline curve, XZ, upper
//		List<double[]> pointsNoseXZUpper = xmtPatch2.stream()
//				.map(x -> new double[]{
//						x,
//						0.0,
//						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
//				})
//				.collect(Collectors.toList());
		// TODO: 
		List<double[]> pointsNoseXZUpper = cadCurvesNoseTrunk.stream()
				.map(crv -> crv.edge().vertices()[0])
				.map(v -> new double[]{v.pnt()[0], v.pnt()[1], v.pnt()[2]})
				.collect(Collectors.toList());
		
//		System.out.println("========== [AircraftUtils::getFuselageCAD] Experimental...................................");		
//		patch2 = OCCUtils.makePatchThruSections(cadCurvesNoseTrunk);
//		CADShape cadShape = CADShapeFactory.getFactory().newShape(patch2.getShape());
//		CADExplorer expE = CADShapeFactory.getFactory().newExplorer();
//		int edges = 0;
//		for (expE.init(cadShape, CADShapeTypes.EDGE); expE.more(); expE.next()) {
//			edges++;
//			System.out.println(">> edge " + edges);
//			CADEdge cadEdge = (CADEdge) expE.current();
//			// extraShapesCap.add((OCCShape) cadEdge);
//		}		
		
		// points z's on nose outline curve, XZ, lower
		List<double[]> pointsNoseXZLower = xmtPatch2.stream()
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
		
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseXZUpper).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseXZLower).edge());
		
		List<double[]> pointsNoseSideRight = xmtPatch2.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
				})
				.collect(Collectors.toList());
		
		CADGeomCurve3D cadCrvNoseXYRight = OCCUtils.theFactory
				.newCurve3D(pointsNoseSideRight, false);

		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvNoseXYRight).edge());			

		// support sections of nose patch-2
//		sections2.stream()
//		.map(sec -> OCCUtils.theFactory
//				.newCurve3D(
//						sec.stream()
//						.map(p -> new double[]{p.x, p.y, p.z})
//						.collect(Collectors.toList()),
//						false)
//				)
//		.map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
//		.forEach(e -> extraShapesCap.add(e));

		cadCurvesNoseTrunk.stream()
			.map(c -> (OCCGeomCurve3D)c)
			.map(crv -> (OCCEdge)(crv.edge()))
			.forEach(e -> extraShapes.add(e));
			
		
		// support sections of cylinder, patch-3
		
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderInitialSection).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderMidSection).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderTerminalSection).edge());


		// points z's on cylinder outline curve, XZ, upper
		List<double[]> pointsCylinderXZUpper = xmtPatch3.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
				})
				.collect(Collectors.toList());
		// points z's on cylinder outline curve, XZ, lower
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
		
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderXZUpper).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvCylinderXZLower).edge());
		
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

		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCylinderXYRight).edge());
		
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

		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvTailXZUpper).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCrvTailXZLower).edge());
		
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
		
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadTailXYRight).edge());
		
		// support sections of tail, patch-4
		cadCurvesTailTrunk.stream()
		         .map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
		         .forEach(e -> extraShapes.add(e));			
		
		// tail cap support entities (outline curves, vertices)

		cadCurvesTailCapTrunk.stream()
         .map(crv -> (OCCEdge)((OCCGeomCurve3D)crv).edge())
         .forEach(e -> extraShapes.add(e));			
		
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

		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadTailCapXZUpper).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadTailCapXZLower).edge());
		
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

		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadTailCapXYRight).edge());
		
		if (exportSupportShapes) {
			System.out.println("========== [AircraftUtils::getFuselageCAD] adding support cad entities");
			result.addAll(extraShapes);
		}
		
		return result;
	}

	/**
	 * Creates a list of shapes, mostly surfaces/shells, representing a lifting surface.
	 * 
	 * ...
	 * 
	 * @param liftingSurface 							the lifting-surface object, extracted from a Aircraft object
	 * @param typeLS						the type of lifting surface, Wing, HTail, VTail, etc
	 * @param exportLofts					include fuselage loft in the output shape list 
	 * @param exportSupportShapes			include supporting sections, outline curves, etc in the output shape list 
	 * @return
	 */

	public static List<OCCShape> getLiftingSurfaceCAD(LiftingSurface liftingSurface, ComponentEnum typeLS,
			boolean exportLofts,
			boolean exportSupportShapes) {
		
		if (liftingSurface == null)
			return null;
		
		List<OCCShape> result = new ArrayList<>();
		List<OCCShape> extraShapes = new ArrayList<>();
		
		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] ");

		if (OCCUtils.theFactory == null) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Initialize CAD shape factory");
			OCCUtils.initCADShapeFactory(); // theFactory now non-null
		}

		int nPanels = liftingSurface.getLiftingSurfaceCreator().getPanels().size();
		System.out.println(">>> n. panels: " + nPanels);

//		wing.getLiftingSurfaceCreator().getXLEBreakPoints();
//		wing.getLiftingSurfaceCreator().getYBreakPoints();
		
		
		Amount<Length> xApex = liftingSurface.getXApexConstructionAxes();
		Amount<Length> zApex = liftingSurface.getZApexConstructionAxes();
		Amount<Angle> riggingAngle = liftingSurface.getRiggingAngle();

		// build the leading edge
		List<double[]> ptsLE = new ArrayList<double[]>();
		
		// calculate FIRST breakpoint coordinates
		ptsLE.add(new double[] {xApex.doubleValue(SI.METER), 0.0, zApex.doubleValue(SI.METER)});

		double zbp = zApex.doubleValue(SI.METER);
		// calculate breakpoints coordinates
		for (int kBP = 1; kBP < liftingSurface.getLiftingSurfaceCreator().getXLEBreakPoints().size(); kBP++) {
			double xbp = liftingSurface.getLiftingSurfaceCreator().getXLEBreakPoints().get(kBP).plus(xApex).doubleValue(SI.METER);
			double ybp = liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(kBP).doubleValue(SI.METER);
			double semiSpanPanel = liftingSurface.getLiftingSurfaceCreator().getPanels().get(kBP - 1).getSpan().times(0.5).doubleValue(SI.METER);
			double dihedralPanel = liftingSurface.getLiftingSurfaceCreator().getPanels().get(kBP - 1).getDihedral().doubleValue(SI.RADIAN);
			zbp = zbp + semiSpanPanel*Math.tan(dihedralPanel);
			ptsLE.add(new double[] {xbp, ybp, zbp});
		}

		// make a wire for the leading edge
		List<TopoDS_Edge> tdsEdgesLE = new ArrayList<>();
		for (int kPts = 1; kPts < ptsLE.size(); kPts++) {
			BRepBuilderAPI_MakeEdge em = new BRepBuilderAPI_MakeEdge(
					new gp_Pnt(ptsLE.get(kPts - 1)[0], ptsLE.get(kPts - 1)[1], ptsLE.get(kPts - 1)[2]),
					new gp_Pnt(ptsLE.get(kPts    )[0], ptsLE.get(kPts    )[1], ptsLE.get(kPts    )[2])
					);
			em.Build();
			if (em.IsDone() == 1)
				tdsEdgesLE.add(em.Edge());
		}
		
//		BRepBuilderAPI_MakeWire wm = new BRepBuilderAPI_MakeWire();
//		tdsEdgesLE.stream().forEach(e -> wm.Add(e));
//		wm.Build();

		// export
		tdsEdgesLE.stream().forEach(e -> result.add((OCCShape)OCCUtils.theFactory.newShape(e)));
		
		// Add chord segments & build the trealing edge
		List<double[]> ptsTE = new ArrayList<double[]>();
		List<Double> chords = new ArrayList<>();
		for (int kPts = 0; kPts < ptsLE.size(); kPts++) {
			double ybp = liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(kPts).doubleValue(SI.METER); 
			double chord = liftingSurface.getLiftingSurfaceCreator().getChordAtYActual(ybp);
			chords.add(chord);
			double twist = liftingSurface.getLiftingSurfaceCreator().getTwistsBreakPoints().get(kPts).doubleValue(SI.RADIAN);
			System.out.println(">>> ybp:   " + ybp);
			System.out.println(">>> chord: " + chord);
			System.out.println(">>> twist: " + twist);
			ptsTE.add(new double[] {
					ptsLE.get(kPts)[0] + chord*Math.cos(twist), ybp, ptsLE.get(kPts)[2] - chord*Math.sin(twist) 
			});
			System.out.println(">>> ptsLE: " + Arrays.toString(ptsLE.get(kPts)) );
			System.out.println(">>> ptsTE: " + Arrays.toString(ptsTE.get(kPts)) );
		}		
		
		List<TopoDS_Edge> tdsChords = new ArrayList<>();
		for (int kPts = 0; kPts < ptsLE.size(); kPts++) {
			BRepBuilderAPI_MakeEdge em = new BRepBuilderAPI_MakeEdge(
					new gp_Pnt(ptsLE.get(kPts)[0], ptsLE.get(kPts)[1], ptsLE.get(kPts)[2]),
					new gp_Pnt(ptsTE.get(kPts)[0], ptsTE.get(kPts)[1], ptsTE.get(kPts)[2])
					);
			em.Build();
			if (em.IsDone() == 1)
				tdsChords.add(em.Edge());
		}
		// export
		tdsChords.stream().forEach(e -> result.add((OCCShape)OCCUtils.theFactory.newShape(e)));
		
		List<TopoDS_Edge> tdsEdgesTE = new ArrayList<>();
		for (int kPts = 1; kPts < ptsTE.size(); kPts++) {
			BRepBuilderAPI_MakeEdge em = new BRepBuilderAPI_MakeEdge(
					new gp_Pnt(ptsTE.get(kPts - 1)[0], ptsTE.get(kPts - 1)[1], ptsTE.get(kPts - 1)[2]),
					new gp_Pnt(ptsTE.get(kPts    )[0], ptsTE.get(kPts    )[1], ptsTE.get(kPts    )[2])
					);
			em.Build();
			if (em.IsDone() == 1)
				tdsEdgesTE.add(em.Edge());
		}
		// export
		tdsEdgesTE.stream().forEach(e -> result.add((OCCShape)OCCUtils.theFactory.newShape(e)));

		// Airfoils
		
		// root
		Double[] wingRootXCoordinates = liftingSurface.getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] wingRootZCoordinates = liftingSurface.getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		
		List<double[]> ptsAirfoilRoot = new ArrayList<>();
		
		System.out.println(">>> wingRootXCoordinates.length " + wingRootXCoordinates.length);
		IntStream.range(0, wingRootXCoordinates.length)
			.forEach(i -> 
				ptsAirfoilRoot.add(new double[] {
						wingRootXCoordinates[i]*chords.get(0) + xApex.doubleValue(SI.METER),
						ptsLE.get(0)[1],
						wingRootZCoordinates[i]*chords.get(0) + zApex.doubleValue(SI.METER)
				})
			);
		// root cad curve
		CADGeomCurve3D cadCurveRootAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilRoot, false);
		
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveRootAirfoil).edge());
		

		// kink
		Double[] wingKinkXCoordinates = liftingSurface.getAirfoilList().get(1).getAirfoilCreator().getXCoords();
		Double[] wingKinkZCoordinates = liftingSurface.getAirfoilList().get(1).getAirfoilCreator().getZCoords();
		
		List<double[]> ptsAirfoilKink = new ArrayList<>();
		
		System.out.println(">>> wingKinkXCoordinates.length " + wingKinkXCoordinates.length);
		IntStream.range(0, wingKinkXCoordinates.length)
			.forEach(i -> 
				ptsAirfoilKink.add(new double[] {
						wingKinkXCoordinates[i]*chords.get(1) + ptsLE.get(1)[0],
						ptsLE.get(1)[1],
						wingKinkZCoordinates[i]*chords.get(1) + ptsLE.get(1)[2],
				})
			);
		// root cad curve
		CADGeomCurve3D cadCurveKinkAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilKink, false);
		
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveKinkAirfoil).edge());
		
		
		if (exportSupportShapes) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding support cad entities");
			result.addAll(extraShapes);
		}
		
		OCCShape patch1 = OCCUtils.makePatchThruSections(cadCurveRootAirfoil, cadCurveKinkAirfoil);

		if (exportLofts) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding loft surfaces");
			result.add(patch1);
		}
		
		
		return result;
	}
	
	
}
