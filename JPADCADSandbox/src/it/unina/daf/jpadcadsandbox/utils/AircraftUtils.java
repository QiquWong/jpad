package it.unina.daf.jpadcadsandbox.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import calculators.aerodynamics.AirfoilCalc;
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
import it.unina.daf.jpadcad.occ.CADGeomSurface;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCFace;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRep_Tool;
import opencascade.GeomAPI_ProjectPointOnCurve;
import opencascade.GeomAbs_Shape;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.TopoDS_Vertex;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
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
	 * @param liftingSurface 				the lifting-surface object, extracted from a Aircraft object
	 * @param typeLS						the type of lifting surface, Wing, HTail, VTail, etc
	 * @param exportLofts					include lifting surface loft in the output shape list 
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
			if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				ptsLE.add(new double[] {
						xbp,
						0,
						ybp + zApex.doubleValue(SI.METER)
				});
			} else {
				ptsLE.add(new double[] {xbp, ybp, zbp});
			}
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
		
		// Add chord segments & build the trailing edge
		List<double[]> ptsTE = new ArrayList<double[]>();
		List<Double> chords = new ArrayList<>();
		List<Double> twists = new ArrayList<>();
		for (int kPts = 0; kPts < ptsLE.size(); kPts++) {
			double ybp = liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(kPts).doubleValue(SI.METER); 
			double chord = liftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints().get(kPts).doubleValue(SI.METER);    
			chords.add(chord);
			double twist = liftingSurface.getLiftingSurfaceCreator().getTwistsBreakPoints().get(kPts).doubleValue(SI.RADIAN);
//			double twist = MyMathUtils.getInterpolatedValue1DLinear(
//					MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getLiftingSurfaceCreator().getDiscretizedYs()),
//					MyArrayUtils.convertToDoublePrimitive(
//							liftingSurface.getLiftingSurfaceCreator().getDiscretizedTwists().stream()
//							.map(t -> t.doubleValue(SI.RADIAN))
//							.collect(Collectors.toList())),							
//					ybp
//					);
			twists.add(twist);
			System.out.println(">>> ybp:   " + ybp);
			System.out.println(">>> chord: " + chord);
			System.out.println(">>> twist: " + twist);
			if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				ptsTE.add(new double[] {
						ptsLE.get(kPts)[0] + chord, 
						0, 
						ptsLE.get(kPts)[2]
				});
			} else {
				ptsTE.add(new double[] {
						ptsLE.get(kPts)[0] + chord*Math.cos(twist + riggingAngle.doubleValue(SI.RADIAN)), 
						ybp, 
						ptsLE.get(kPts)[2] - chord*Math.sin(twist + riggingAngle.doubleValue(SI.RADIAN)) 		
				});
			}
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
		List<List<CADGeomCurve3D>> cadCurveAirfoilList = new ArrayList<List<CADGeomCurve3D>>();
		
		// airfoils at breakpoints
		List<CADGeomCurve3D> cadCurveAirfoilBPList = new ArrayList<CADGeomCurve3D>();
		cadCurveAirfoilBPList = IntStream.range(0, liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().size())
				.mapToObj(i -> {
					AirfoilCreator airfoilCoords = liftingSurface.getAirfoilList().get(i).getAirfoilCreator();
					List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
							liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(i).doubleValue(SI.METER), 
							airfoilCoords, 
							liftingSurface
							);
					CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
					return cadCurveAirfoil;
				})
				.collect(Collectors.toList());

//		cadCurveAirfoilBPList.forEach(crv -> extraShapes.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));

		// airfoils between breakpoints
		List<CADGeomCurve3D> cadCurveAirfoilBetBPList = new ArrayList<CADGeomCurve3D>();
		int nSec = 1; // number of sections between two contiguous breakpoints
		for(int iP = 1; iP <= liftingSurface.getLiftingSurfaceCreator().getPanels().size(); iP++) {
			List<CADGeomCurve3D> cadCurveAirfoilPanelList = new ArrayList<CADGeomCurve3D>();
			double[] secVec = new double[nSec + 2];
			secVec = MyArrayUtils.linspace(
					liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(iP-1).doubleValue(SI.METER), 
					liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(iP).doubleValue(SI.METER), 
					nSec + 2
					);
			final double[] fSecVec = secVec; //TODO: this use of final probably needs some fixing
			final int fIP = iP;
			cadCurveAirfoilPanelList.add(cadCurveAirfoilBPList.get(iP-1));
			cadCurveAirfoilPanelList.addAll(IntStream.range(1, (nSec + 1))
					.mapToObj(i -> {
						AirfoilCreator airfoilCoords = liftingSurface.getAirfoilList().get(fIP-1).getAirfoilCreator();
						List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
								fSecVec[i], 
								airfoilCoords, 
								liftingSurface
								);
						CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
						cadCurveAirfoilBetBPList.add(cadCurveAirfoil);
						return cadCurveAirfoil;
					})
					.collect(Collectors.toList()));
			cadCurveAirfoilPanelList.add(cadCurveAirfoilBPList.get(iP));	
			cadCurveAirfoilList.add(cadCurveAirfoilPanelList);
		}

//		cadCurveAirfoilBetBPList.forEach(crv -> extraShapes.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));

		// Create patches through the sections defined above
		List<OCCShape> patchList = new ArrayList<>();
//		patchList = cadCurveAirfoilList.stream()
//									   .map(crvL -> OCCUtils.makePatchThruSections(crvL))
//									   .collect(Collectors.toList());
		
		for(int i = 0; i < cadCurveAirfoilList.size(); i++) {
			for(int j = 1; j < cadCurveAirfoilList.get(i).size(); j++) {
				patchList.add(OCCUtils.makePatchThruSections(
						cadCurveAirfoilList.get(i).get(j-1),
						cadCurveAirfoilList.get(i).get(j)
						));
			}
		}
				
//		System.out.println(cadCurveAirfoilList.get(0).get(0).edge().vertices()[0].equals(cadCurveAirfoilList.get(0).get(0).edge().vertices()[1]));
//		System.out.println(cadCurveAirfoilBPList.stream().anyMatch(crv -> (crv.value(crv.getRange()[0]).equals(crv.value(crv.getRange()[1])))));
//		System.out.println(Arrays.toString(cadCurveAirfoilBPList.get(0).value(cadCurveAirfoilBPList.get(0).getRange()[0])));
//		System.out.println(Arrays.toString(cadCurveAirfoilBPList.get(0).value(cadCurveAirfoilBPList.get(0).getRange()[1])));
		
		//Eventually closing the trailing edge
		List<List<OCCShape>> patchTE = new ArrayList<List<OCCShape>>();
		if(cadCurveAirfoilBPList.stream().anyMatch(crv -> !AircraftUtils.isAirfoilCADCurveClosed(crv))) {
			for(int i = 0; i < cadCurveAirfoilList.size(); i++) {
				List<OCCShape> patchTEPanel = new ArrayList<OCCShape>();
				if(!AircraftUtils.isAirfoilCADCurveClosed(cadCurveAirfoilList.get(i).get(0)) &&
				   !AircraftUtils.isAirfoilCADCurveClosed(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1))	
				   ) {
					for(int j = 1; j < cadCurveAirfoilList.get(i).size(); j++) {
						CADFace f1 = OCCUtils.theFactory.newFacePlanar(
								cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[0]), 
								cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[1]), 
								cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[0])
								);
						CADFace f2 = OCCUtils.theFactory.newFacePlanar(
								cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[0]), 
								cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[1]), 
								cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[1])
								);
						CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(f1, f2);
						patchTEPanel.add((OCCShape)shell);
					}
					patchTE.add(patchTEPanel);
				}
				if(!AircraftUtils.isAirfoilCADCurveClosed(cadCurveAirfoilList.get(i).get(0)) &&
				    AircraftUtils.isAirfoilCADCurveClosed(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1))	
				   ) {
					for(int j = 1; j < (cadCurveAirfoilList.get(i).size()-1); j++) {
						CADFace f1 = OCCUtils.theFactory.newFacePlanar(
								cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[0]), 
								cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[1]), 
								cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[0])
								);
						CADFace f2 = OCCUtils.theFactory.newFacePlanar(
								cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[0]), 
								cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[1]), 
								cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[1])
								);
						CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(f1, f2);
						patchTEPanel.add((OCCShape)shell);
					}
					CADFace f0 = OCCUtils.theFactory.newFacePlanar(
							cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-2).value(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-2).getRange()[0]), 
							cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-2).value(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-2).getRange()[1]), 
							cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).value(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).getRange()[1])
							);
					patchTEPanel.add((OCCShape)f0);
					patchTE.add(patchTEPanel);
				}
				if(AircraftUtils.isAirfoilCADCurveClosed(cadCurveAirfoilList.get(i).get(0)) &&
				  !AircraftUtils.isAirfoilCADCurveClosed(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1))	
				   ) {
					CADFace f0 = OCCUtils.theFactory.newFacePlanar(
							cadCurveAirfoilList.get(i).get(0).value(cadCurveAirfoilList.get(i).get(0).getRange()[0]), 
							cadCurveAirfoilList.get(i).get(1).value(cadCurveAirfoilList.get(i).get(1).getRange()[0]), 
							cadCurveAirfoilList.get(i).get(1).value(cadCurveAirfoilList.get(i).get(1).getRange()[1])
							);
					patchTEPanel.add((OCCShape)f0);
						for(int j = 2; j < cadCurveAirfoilList.get(i).size(); j++) {
							CADFace f1 = OCCUtils.theFactory.newFacePlanar(
									cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[0]), 
									cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[1]), 
									cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[0])
									);
							CADFace f2 = OCCUtils.theFactory.newFacePlanar(
									cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[0]), 
									cadCurveAirfoilList.get(i).get(j).value(cadCurveAirfoilList.get(i).get(j).getRange()[1]), 
									cadCurveAirfoilList.get(i).get(j-1).value(cadCurveAirfoilList.get(i).get(j-1).getRange()[1])
									);
							CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(f1, f2);
							patchTEPanel.add((OCCShape)shell);
						}
						patchTE.add(patchTEPanel);
					}	
			}	
		}
		
		// Closing tip (testing)
//		CADShape faceTip = OCCUtils.makeFilledFace(cadCurveAirfoilList.get(cadCurveAirfoilList.size()-1).get(cadCurveAirfoilList.get(cadCurveAirfoilList.size()-1).size()-1));
//		CADShape faceRoot = OCCUtils.makeFilledFace(cadCurveAirfoilList.get(0).get(0));
//		result.add((OCCShape)faceTip);
//		result.add((OCCShape)faceRoot);	
		
		// Closing the tip using a filler surface
		int iTip = cadCurveAirfoilBPList.size() - 1;                      // tip airfoil index 
		CADGeomCurve3D airfoilTip = cadCurveAirfoilBPList.get(iTip);      // airfoil CAD curve
		CADGeomCurve3D airfoilPreTip = cadCurveAirfoilBPList.get(iTip-1); // second to last airfoil CAD curve
				
		Double rTh = liftingSurface.getAirfoilList().get(iTip).getAirfoilCreator().getThicknessToChordRatio(); 
		Double eTh = rTh*chords.get(iTip); // effective airfoil thickness	
		
		// creating the tip chord edge
		OCCEdge tipChord = (OCCEdge) OCCUtils.theFactory.newShape(tdsChords.get(iTip));	
		Double tipChordLength = liftingSurface.getChordTip().doubleValue(SI.METER);
		
		// creating the second to last chord edge
		OCCEdge preTipChord = (OCCEdge) OCCUtils.theFactory.newShape(tdsChords.get(iTip-1));	
		Double preTipChordLength = liftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints().get(iTip-1).doubleValue(SI.METER);
		
		// splitting the tip airfoil curve using the first vertex of the tip chord
		List<OCCEdge> airfoilTipCrvs = OCCUtils.splitEdge(
				airfoilTip, 
				OCCUtils.getVertexFromEdge(tipChord, 0).pnt()
				);
		
		// splitting the second to last airfoil curve using its chord first vertex
		List<OCCEdge> airfoilPreTipCrvs = OCCUtils.splitEdge(
				airfoilPreTip, 
				OCCUtils.getVertexFromEdge(preTipChord, 0).pnt()
				);
				
		// creating a drawing plane next to the tip airfoil
		PVector le1 = new PVector(
				(float) ptsLE.get(iTip-1)[0],
				(float) ptsLE.get(iTip-1)[1],
				(float) ptsLE.get(iTip-1)[2]  // coordinates of the second to last leading edge BP
				);
		
		PVector le2 = new PVector(
				(float) ptsLE.get(iTip)[0],
				(float) ptsLE.get(iTip)[1],
				(float) ptsLE.get(iTip)[2]    // coordinates of the last leading edge BP
				);
		
		PVector te1 = new PVector(
				(float) ptsTE.get(iTip-1)[0],
				(float) ptsTE.get(iTip-1)[1],
				(float) ptsTE.get(iTip-1)[2]  // coordinates of the second to last trailing edge BP
				);
		
		PVector te2 = new PVector(
				(float) ptsTE.get(iTip)[0],
				(float) ptsTE.get(iTip)[1],
				(float) ptsTE.get(iTip)[2]    // coordinates of the last trailing edge BP
				);
				
		PVector leVector = PVector.sub(le2, le1); // vector representation of the last panel leading edge 
	    float leLength = leVector.mag();
		float aLength = eTh.floatValue()/leLength;		
		PVector aVector = PVector.mult(leVector, aLength);
		PVector aPnt = PVector.add(le2, aVector);
		
		PVector teVector = PVector.sub(te2, te1); // vector representation of the last panel trailing edge 
		teVector.z = leVector.z; // slightly modified in order to obtain a plane
		float teLength = teVector.mag();
		float bLength = eTh.floatValue()/teLength;		
		PVector bVector = PVector.mult(teVector, bLength);
		PVector bPnt = PVector.add(te2, bVector);
		
		// creating the edge a
		List<PVector> aPnts = new ArrayList<>();
		aPnts.add(le2);
		aPnts.add(aPnt);
		CADGeomCurve3D segA = OCCUtils.theFactory.newCurve3DP(aPnts, false);
		
		// creating the edge b
		List<PVector> bPnts = new ArrayList<>();
		bPnts.add(te2);
		bPnts.add(bPnt);
		CADGeomCurve3D segB = OCCUtils.theFactory.newCurve3DP(bPnts, false);
		
		// creating the edge c
		List<PVector> cPnts = new ArrayList<>();
		cPnts.add(aPnt);
		cPnts.add(bPnt);
		CADGeomCurve3D segC = OCCUtils.theFactory.newCurve3DP(cPnts, false);
		
		// creating vertical splitting vectors for the tip airfoil curve, orthogonal to the chord
		PVector chordTipVector = PVector.sub(te2, le2); // vector in the airfoil plane	
		PVector chordTipNVector = new PVector();
		PVector.cross(chordTipVector, aVector, chordTipNVector).normalize(); // vector in the airfoil plane, normal to the chord, normalized
		
		// creating vertical splitting vectors for the second to last airfoil curve
		PVector chordPreTipVector = PVector.sub(te1, le1); 	
		PVector chordPreTipNVector = new PVector();
		PVector.cross(chordPreTipVector, aVector, chordPreTipNVector).normalize(); 
		
		// creating points for the guide curves in the construction plane formed by the segments a, b and c
		Double[] splits = {0.25, 0.75};
		PVector cPnt = PVector.lerp(aPnt, bPnt, splits[0].floatValue());
		PVector dPnt = PVector.lerp(aPnt, bPnt, splits[1].floatValue());
		
		PVector ePnt = PVector.lerp(le2, te2, splits[1].floatValue());
		dPnt.lerp(ePnt, 0.25f); // slightly modified D point
		
		// creating the guide curves in the construction plane
		List<double[]> constPlaneGuideCrv1Pnts = new ArrayList<>();
		constPlaneGuideCrv1Pnts.add(new double[] {le2.x, le2.y, le2.z});
		constPlaneGuideCrv1Pnts.add(new double[] {cPnt.x, cPnt.y, cPnt.z});
		
		List<double[]> constPlaneGuideCrv2Pnts = new ArrayList<>();
		constPlaneGuideCrv2Pnts.add(new double[] {cPnt.x, cPnt.y, cPnt.z});
		constPlaneGuideCrv2Pnts.add(new double[] {dPnt.x, dPnt.y, dPnt.z});
		constPlaneGuideCrv2Pnts.add(new double[] {te2.x, te2.y, te2.z});
				
		PVector cVector = PVector.sub(bPnt, aPnt);
				
		double tanAFac = 5*aVector.mag();
		double tanBFac = 3*bVector.mag()*(-1);
		double tanCFac = 5*aVector.mag();
		aVector.normalize();
		bVector.normalize();
		cVector.normalize();
		
		double[] tanAConstPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {aVector.x, aVector.y, aVector.z}, tanAFac);
		double[] tanBConstPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {bVector.x, bVector.y, bVector.z}, tanBFac);
		double[] tanCConstPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {cVector.x, cVector.y, cVector.z}, tanCFac);
		
		System.out.println(">>>> Tangent Vector a: " + Arrays.toString(aVector.array()));
		System.out.println(">>>> Tangent Vector b: " + Arrays.toString(bVector.array()));
		System.out.println(">>>> Tangent Vector c: " + Arrays.toString(cVector.array()));		

		CADGeomCurve3D constPlaneGuideCrv1 = OCCUtils.theFactory.newCurve3D(
				constPlaneGuideCrv1Pnts, 
				false, 
				tanAConstPlaneGuideCrv, 
				tanCConstPlaneGuideCrv, 
				false
				);	
		CADGeomCurve3D constPlaneGuideCrv2_0 = OCCUtils.theFactory.newCurve3D(
				constPlaneGuideCrv2Pnts, 
				false, 
				tanCConstPlaneGuideCrv, 
				tanBConstPlaneGuideCrv, 
				false
				);
		
		// splitting constPlaneGuideCrv2_0 for further manipulations
		List<OCCEdge> constPlaneGuideCrvs2 = OCCUtils.splitEdge(
				constPlaneGuideCrv2_0, 
				new double[] {dPnt.x, dPnt.y, dPnt.z}
				);		
		CADGeomCurve3D constPlaneGuideCrv2 = OCCUtils.theFactory.newCurve3D(constPlaneGuideCrvs2.get(0));
		CADGeomCurve3D constPlaneGuideCrv3 = OCCUtils.theFactory.newCurve3D(constPlaneGuideCrvs2.get(1));
		
		// first of all, splitting the tip airfoil in 6 parts, 3 for the upper and 3 for the lower, and getting tangent vectors
		List<OCCEdge> airfoilUpperCrvs = new ArrayList<>();
		List<OCCEdge> airfoilLowerCrvs = new ArrayList<>();
		airfoilUpperCrvs.add(airfoilTipCrvs.get(0));
		airfoilLowerCrvs.add(airfoilTipCrvs.get(1));
		
		List<PVector[]> tanList = new ArrayList<>();
		
		for(int i = 0; i < splits.length; i++) {
			
			int iUpp = 0;
			int iLow = 0;
			
			PVector pntOnTipChord = PVector.lerp(le2, te2, splits[i].floatValue()); // tip chord fraction point
			
			Double[] tipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip).getAirfoilCreator(), 
					splits[i]
					);
			
			PVector pntOnTipAirfoilUCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[0].floatValue())*tipChordLength.floatValue())
					);			
			PVector pntOnTipAirfoilLCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[1].floatValue())*tipChordLength.floatValue())
					);
						
			List<OCCEdge> arflUppCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilUpperCrvs.get(iUpp)), 
					new double[] {pntOnTipAirfoilUCrv.x, pntOnTipAirfoilUCrv.y, pntOnTipAirfoilUCrv.z}
					);			
			List<OCCEdge> arflLowCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilLowerCrvs.get(iLow)),
					new double[] {pntOnTipAirfoilLCrv.x, pntOnTipAirfoilLCrv.y, pntOnTipAirfoilLCrv.z}
					);
			
			airfoilUpperCrvs.remove(iUpp);		
			airfoilUpperCrvs.add(iUpp, arflUppCrvs.get(0));
			airfoilUpperCrvs.add(iUpp + 1, arflUppCrvs.get(1));
			
			airfoilLowerCrvs.remove(iLow);
			airfoilLowerCrvs.add(iLow, arflLowCrvs.get(1));
			airfoilLowerCrvs.add(iLow + 1, arflLowCrvs.get(0));
			
			iLow++;	
			
			// tangent vectors calculation
			PVector pntOnPreTipChord = PVector.lerp(le1, te1, splits[i].floatValue());
			
			Double[] preTipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip-1).getAirfoilCreator(), 
					splits[i]
					);
			
			PVector pntOnPreTipAirfoilUCrv = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[0].floatValue())*preTipChordLength.floatValue())
					);			
			PVector pntOnPreTipAirfoilLCrv = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[1].floatValue())*preTipChordLength.floatValue())
					);
			
			double[] pntPTU = OCCUtils.pointProjectionOnCurve(
					(OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(0)),
					new double[] {pntOnPreTipAirfoilUCrv.x, pntOnPreTipAirfoilUCrv.y, pntOnPreTipAirfoilUCrv.z}
					).pnt();			
			double[] pntPTL = OCCUtils.pointProjectionOnCurve(
					(OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(1)),
					new double[] {pntOnPreTipAirfoilLCrv.x, pntOnPreTipAirfoilLCrv.y, pntOnPreTipAirfoilLCrv.z}
					).pnt();
			
			PVector pntPreTipUpp = new PVector(
					(float) pntPTU[0], 
					(float) pntPTU[1],
					(float) pntPTU[2]
					);			
			PVector pntPreTipLow = new PVector(
					(float) pntPTL[0], 
					(float) pntPTL[1],
					(float) pntPTL[2]
					);
			
			PVector[] tanVecs = new PVector[2];
			
			tanVecs[0] = PVector.sub(
					pntOnTipAirfoilUCrv,
					pntPreTipUpp
					).normalize();
			
			tanVecs[1] = PVector.sub(
					pntOnTipAirfoilLCrv,
					pntPreTipLow
					).normalize();
			
			System.out.println(">>>> Upper Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[0].array()));
			System.out.println(">>>> Lower Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[1].array()));

			tanList.add(tanVecs);
		}
		
		// main vertical sections 
		List<double[]> mainVSec1Pnts = new ArrayList<>();
		List<double[]> mainVSec2Pnts = new ArrayList<>();
		
		mainVSec1Pnts.add(airfoilUpperCrvs.get(splits.length).vertices()[0].pnt());
		mainVSec1Pnts.add(new double[] {cPnt.x, cPnt.y, cPnt.z});
		mainVSec1Pnts.add(airfoilLowerCrvs.get(splits.length).vertices()[1].pnt());		
		
		mainVSec2Pnts.add(airfoilUpperCrvs.get(splits.length-1).vertices()[0].pnt());
		mainVSec2Pnts.add(new double[] {dPnt.x, dPnt.y, dPnt.z});
		mainVSec2Pnts.add(airfoilLowerCrvs.get(splits.length-1).vertices()[1].pnt());
		
		double tanUppMainVSec1Fac = tanAFac;
		double tanLowMainVSec1Fac = tanAFac*(-1);		
		double tanUppMainVSec2Fac = tanAFac;
		double tanLowMainVSec2Fac = tanAFac*(-1);
		
		double[] tanUppMainVSec1 = MyArrayUtils.scaleArray(
				new double[] {tanList.get(0)[0].x, tanList.get(0)[0].y, tanList.get(0)[0].z}, 
				tanUppMainVSec1Fac
				);		
		double[] tanLowMainVSec1 = MyArrayUtils.scaleArray(
				new double[] {tanList.get(0)[1].x, tanList.get(0)[1].y, tanList.get(0)[1].z},
				tanLowMainVSec1Fac
				);		
		double[] tanUppMainVSec2 = MyArrayUtils.scaleArray(
				new double[] {tanList.get(1)[0].x, tanList.get(1)[0].y, tanList.get(1)[0].z}, 
				tanUppMainVSec2Fac
				);		
		double[] tanLowMainVSec2 = MyArrayUtils.scaleArray(
				new double[] {tanList.get(1)[1].x, tanList.get(1)[1].y, tanList.get(1)[1].z},
				tanLowMainVSec2Fac
				);
		
		CADGeomCurve3D mainVSec1 = OCCUtils.theFactory.newCurve3D(
				mainVSec1Pnts, 
				false, 
				tanUppMainVSec1, 
				tanLowMainVSec1, 
				false
				);		
		CADGeomCurve3D mainVSec2 = OCCUtils.theFactory.newCurve3D(
				mainVSec2Pnts, 
				false, 
				tanUppMainVSec2, 
				tanLowMainVSec2, 
				false
				);
		
		// splitting the main sections curves, in order to obtain better accuracy
//		List<OCCEdge> mainVSec1Crvs = OCCUtils.splitEdge(
//				mainVSec1, 
//				new double[] {cPnt.x, cPnt.y, cPnt.z}
//				);		
//		CADGeomCurve3D mainVSec1Upp = OCCUtils.theFactory.newCurve3D(mainVSec1Crvs.get(0));
//		CADGeomCurve3D mainVSec1Low = OCCUtils.theFactory.newCurve3D(mainVSec1Crvs.get(1));
//		
//		List<OCCEdge> mainVSec2Crvs = OCCUtils.splitEdge(
//				mainVSec2, 
//				new double[] {dPnt.x, dPnt.y, dPnt.z}
//				);
//		CADGeomCurve3D mainVSec2Upp = OCCUtils.theFactory.newCurve3D(mainVSec2Crvs.get(0));
//		CADGeomCurve3D mainVSec2Low = OCCUtils.theFactory.newCurve3D(mainVSec2Crvs.get(1));
		
		// more splittings for better accuracy
		Double[] splitsVP1 = {0.10, 0.30, 0.50, 0.75};
		Double[] splitsVP2 = {0.25, 0.50, 0.75};
		Double[] splitsVP3 = {0.30, 0.55, 0.75, 0.90};
		
		Double[] splitsHP1 = {0.15, 0.35, 0.65, 0.85};
		Double[] splitsHP2 = {0.15, 0.35, 0.65, 0.85};
		Double[] splitsHP3 = {0.15, 0.35, 0.65, 0.85};
		
		// first patch, sub vertical curves creation
		List<double[]> pntsOnCPGC1 = new ArrayList<>();
		List<OCCEdge> airfoilUpperCrvsP1 = new ArrayList<>();
		List<OCCEdge> airfoilLowerCrvsP1 = new ArrayList<>();
		List<CADGeomCurve3D> subVSecP1 = new ArrayList<>();
		airfoilUpperCrvsP1.add(airfoilUpperCrvs.get(2));
		airfoilLowerCrvsP1.add(airfoilLowerCrvs.get(2));
		double[] rangeCPGC1 = constPlaneGuideCrv1.getRange();
		
		for(int i = 0; i < splitsVP1.length; i++) {			
			// projecting points on the construction curve onto the tip chord
			double[] pntOnCrv = constPlaneGuideCrv1.value(splitsVP1[i]*(rangeCPGC1[1] - rangeCPGC1[0]) + rangeCPGC1[0]);
			pntsOnCPGC1.add(pntOnCrv);
			double[] projOnChord = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(tipChord), 
					pntOnCrv
					).pnt();
			PVector projOnChordPV = new PVector(
					(float) projOnChord[0], 
					(float) projOnChord[1], 
					(float) projOnChord[2]
					);
			double chordFraction = (PVector.sub(projOnChordPV, le2).mag())/tipChordLength;
			
			// obtaining points on the tip airfoil curve	
			int iUpp = 0;
			int iLow = 0;
			
			PVector pntOnTipChord = PVector.lerp(le2, te2, (float) chordFraction);
			
			Double[] tipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip).getAirfoilCreator(), 
					chordFraction
					);
			
			PVector pntOnTipAirfoilUCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[0].floatValue())*tipChordLength.floatValue())
					);			
			PVector pntOnTipAirfoilLCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[1].floatValue())*tipChordLength.floatValue())
					);	
			
			List<OCCEdge> arflUppCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilUpperCrvsP1.get(iUpp)), 
					new double[] {pntOnTipAirfoilUCrv.x, pntOnTipAirfoilUCrv.y, pntOnTipAirfoilUCrv.z}
					);			
			List<OCCEdge> arflLowCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilLowerCrvsP1.get(iLow)), 
					new double[] {pntOnTipAirfoilLCrv.x, pntOnTipAirfoilLCrv.y, pntOnTipAirfoilLCrv.z}
					);
			
			airfoilUpperCrvsP1.remove(iUpp);		
			airfoilUpperCrvsP1.add(iUpp, arflUppCrvs.get(0));
			airfoilUpperCrvsP1.add(iUpp + 1, arflUppCrvs.get(1));
			
			airfoilLowerCrvsP1.remove(iLow);
			airfoilLowerCrvsP1.add(iLow, arflLowCrvs.get(1));
			airfoilLowerCrvsP1.add(iLow + 1, arflLowCrvs.get(0));
			
			// calculating tangent using the second to last airfoil curve
			PVector pntOnPreTipChord = PVector.lerp(le1, te1, (float) chordFraction); 

			Double[] preTipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip-1).getAirfoilCreator(), 
					chordFraction
					);		
			
			PVector pntOnPreTipAirfoilUpp = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[0].floatValue())*preTipChordLength.floatValue())
					);
			PVector pntOnPreTipAirfoilLow = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[1].floatValue())*preTipChordLength.floatValue())
					);			
			double[] pntPTU = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(0)), 
					new double[] {pntOnPreTipAirfoilUpp.x, pntOnPreTipAirfoilUpp.y, pntOnPreTipAirfoilUpp.z}
					).pnt();				
			double[] pntPTL = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(1)), 
					new double[] {pntOnPreTipAirfoilLow.x, pntOnPreTipAirfoilLow.y, pntOnPreTipAirfoilLow.z}
					).pnt();			
			PVector pntPreTipUpp = new PVector(
					(float) pntPTU[0], 
					(float) pntPTU[1],
					(float) pntPTU[2]
					);			
			PVector pntPreTipLow = new PVector(
					(float) pntPTL[0], 
					(float) pntPTL[1],
					(float) pntPTL[2]
					);

			PVector[] tanVecs = new PVector[2];
			tanVecs[0] = PVector.sub(
					pntOnTipAirfoilUCrv,
					pntPreTipUpp
					).normalize();
			tanVecs[1] = PVector.sub(
					pntOnTipAirfoilLCrv,
					pntPreTipLow
					).normalize();
			double tanSubVSecUppFac = tanAFac;
			double tanSubVSecLowFac = tanAFac*(-1);

			System.out.println(">>>> Upper Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[0].array()));
			System.out.println(">>>> Lower Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[1].array()));
			
			// creating new sub vertical curves for patch 1			
			List<double[]> pntsSubVSec = new ArrayList<>();
			pntsSubVSec.add(arflUppCrvs.get(0).vertices()[1].pnt());
			pntsSubVSec.add(pntOnCrv);
			pntsSubVSec.add(arflLowCrvs.get(0).vertices()[1].pnt());
			CADGeomCurve3D subVSec = OCCUtils.theFactory.newCurve3D(
					pntsSubVSec, 
					false,
					MyArrayUtils.scaleArray(new double[] {tanVecs[0].x, tanVecs[0].y, tanVecs[0].z}, tanSubVSecUppFac),  
					MyArrayUtils.scaleArray(new double[] {tanVecs[1].x, tanVecs[1].y, tanVecs[1].z}, tanSubVSecLowFac),
					false
					);
			subVSecP1.add(subVSec);				
		}
		
		// second patch, sub vertical curves creation
		List<double[]> pntsOnCPGC2 = new ArrayList<>();
		List<OCCEdge> airfoilUpperCrvsP2 = new ArrayList<>();
		List<OCCEdge> airfoilLowerCrvsP2 = new ArrayList<>();
		List<CADGeomCurve3D> subVSecP2 = new ArrayList<>();
		airfoilUpperCrvsP2.add(airfoilUpperCrvs.get(1));
		airfoilLowerCrvsP2.add(airfoilLowerCrvs.get(1));
		double[] rangeCPGC2 = constPlaneGuideCrv2.getRange();
		
		for(int i = 0; i < splitsVP2.length; i++) {			
			// projecting points on the construction curve onto the tip chord
			double[] pntOnCrv = constPlaneGuideCrv2.value(splitsVP2[i]*(rangeCPGC2[1] - rangeCPGC2[0]) + rangeCPGC2[0]);
			pntsOnCPGC2.add(pntOnCrv);
			double[] projOnChord = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(tipChord), 
					pntOnCrv
					).pnt();
			PVector projOnChordPV = new PVector(
					(float) projOnChord[0], 
					(float) projOnChord[1], 
					(float) projOnChord[2]
					);
			double chordFraction = (PVector.sub(projOnChordPV, le2).mag())/tipChordLength;
			
			// obtaining points on the tip airfoil curve	
			int iUpp = 0;
			int iLow = 0;
			
			PVector pntOnTipChord = PVector.lerp(le2, te2, (float) chordFraction);
			
			Double[] tipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip).getAirfoilCreator(), 
					chordFraction
					);
			
			PVector pntOnTipAirfoilUCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[0].floatValue())*tipChordLength.floatValue())
					);			
			PVector pntOnTipAirfoilLCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[1].floatValue())*tipChordLength.floatValue())
					);	
			
			List<OCCEdge> arflUppCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilUpperCrvsP2.get(iUpp)), 
					new double[] {pntOnTipAirfoilUCrv.x, pntOnTipAirfoilUCrv.y, pntOnTipAirfoilUCrv.z}
					);			
			List<OCCEdge> arflLowCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilLowerCrvsP2.get(iLow)),
					new double[] {pntOnTipAirfoilLCrv.x, pntOnTipAirfoilLCrv.y, pntOnTipAirfoilLCrv.z}
					);
			
			airfoilUpperCrvsP2.remove(iUpp);		
			airfoilUpperCrvsP2.add(iUpp, arflUppCrvs.get(0));
			airfoilUpperCrvsP2.add(iUpp + 1, arflUppCrvs.get(1));
			
			airfoilLowerCrvsP2.remove(iLow);
			airfoilLowerCrvsP2.add(iLow, arflLowCrvs.get(1));
			airfoilLowerCrvsP2.add(iLow + 1, arflLowCrvs.get(0));
			
			// calculating tangent using the second to last airfoil curve
			PVector pntOnPreTipChord = PVector.lerp(le1, te1, (float) chordFraction); 

			Double[] preTipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip-1).getAirfoilCreator(), 
					chordFraction
					);		
			
			PVector pntOnPreTipAirfoilUpp = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[0].floatValue())*preTipChordLength.floatValue())
					);
			PVector pntOnPreTipAirfoilLow = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[1].floatValue())*preTipChordLength.floatValue())
					);			
			double[] pntPTU = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(0)), 
					new double[] {pntOnPreTipAirfoilUpp.x, pntOnPreTipAirfoilUpp.y, pntOnPreTipAirfoilUpp.z}
					).pnt();				
			double[] pntPTL = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(1)), 
					new double[] {pntOnPreTipAirfoilLow.x, pntOnPreTipAirfoilLow.y, pntOnPreTipAirfoilLow.z}
					).pnt();			
			PVector pntPreTipUpp = new PVector(
					(float) pntPTU[0], 
					(float) pntPTU[1],
					(float) pntPTU[2]
					);			
			PVector pntPreTipLow = new PVector(
					(float) pntPTL[0], 
					(float) pntPTL[1],
					(float) pntPTL[2]
					);

			PVector[] tanVecs = new PVector[2];
			tanVecs[0] = PVector.sub(
					pntOnTipAirfoilUCrv,
					pntPreTipUpp
					).normalize();
			tanVecs[1] = PVector.sub(
					pntOnTipAirfoilLCrv,
					pntPreTipLow
					).normalize();
			double tanSubVSecUppFac = tanAFac;
			double tanSubVSecLowFac = tanAFac*(-1);

			System.out.println(">>>> Upper Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[0].array()));
			System.out.println(">>>> Lower Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[1].array()));
			
			// creating new sub vertical curves for patch 1			
			List<double[]> pntsSubVSec = new ArrayList<>();
			pntsSubVSec.add(arflUppCrvs.get(0).vertices()[1].pnt());
			pntsSubVSec.add(pntOnCrv);
			pntsSubVSec.add(arflLowCrvs.get(0).vertices()[1].pnt());
			CADGeomCurve3D subVSec = OCCUtils.theFactory.newCurve3D(
					pntsSubVSec, 
					false,
					MyArrayUtils.scaleArray(new double[] {tanVecs[0].x, tanVecs[0].y, tanVecs[0].z}, tanSubVSecUppFac),  
					MyArrayUtils.scaleArray(new double[] {tanVecs[1].x, tanVecs[1].y, tanVecs[1].z}, tanSubVSecLowFac),
					false
					);
			subVSecP2.add(subVSec);				
		}
		
		// third patch, sub vertical curves creation
		List<double[]> pntsOnCPGC3 = new ArrayList<>();
		List<OCCEdge> airfoilUpperCrvsP3 = new ArrayList<>();
		List<OCCEdge> airfoilLowerCrvsP3 = new ArrayList<>();
		List<CADGeomCurve3D> subVSecP3 = new ArrayList<>();
		airfoilUpperCrvsP3.add(airfoilUpperCrvs.get(0));
		airfoilLowerCrvsP3.add(airfoilLowerCrvs.get(0));
		double[] rangeCPGC3 = constPlaneGuideCrv3.getRange();

		for(int i = 0; i < splitsVP3.length; i++) {			
			// projecting points on the construction curve onto the tip chord
			double[] pntOnCrv = constPlaneGuideCrv3.value(splitsVP3[i]*(rangeCPGC3[1] - rangeCPGC3[0]) + rangeCPGC3[0]);
			pntsOnCPGC3.add(pntOnCrv);
			double[] projOnChord = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(tipChord), 
					pntOnCrv
					).pnt();
			PVector projOnChordPV = new PVector(
					(float) projOnChord[0], 
					(float) projOnChord[1], 
					(float) projOnChord[2]
					);
			double chordFraction = (PVector.sub(projOnChordPV, le2).mag())/tipChordLength;
			
			System.out.println(chordFraction);

			// obtaining points on the tip airfoil curve	
			int iUpp = 0;
			int iLow = 0;

			PVector pntOnTipChord = PVector.lerp(le2, te2, (float) chordFraction);

			Double[] tipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip).getAirfoilCreator(), 
					chordFraction
					);

			PVector pntOnTipAirfoilUCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[0].floatValue())*tipChordLength.floatValue())
					);			
			PVector pntOnTipAirfoilLCrv = PVector.add(
					pntOnTipChord, 
					PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[1].floatValue())*tipChordLength.floatValue())
					);	

			List<OCCEdge> arflUppCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilUpperCrvsP3.get(iUpp)),
					new double[] {pntOnTipAirfoilUCrv.x, pntOnTipAirfoilUCrv.y, pntOnTipAirfoilUCrv.z}
					);			
			List<OCCEdge> arflLowCrvs = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(airfoilLowerCrvsP3.get(iLow)), 
					new double[] {pntOnTipAirfoilLCrv.x, pntOnTipAirfoilLCrv.y, pntOnTipAirfoilLCrv.z}
					);

			airfoilUpperCrvsP3.remove(iUpp);		
			airfoilUpperCrvsP3.add(iUpp, arflUppCrvs.get(0));
			airfoilUpperCrvsP3.add(iUpp + 1, arflUppCrvs.get(1));

			airfoilLowerCrvsP3.remove(iLow);
			airfoilLowerCrvsP3.add(iLow, arflLowCrvs.get(1));
			airfoilLowerCrvsP3.add(iLow + 1, arflLowCrvs.get(0));

			// calculating tangent using the second to last airfoil curve
			PVector pntOnPreTipChord = PVector.lerp(le1, te1, (float) chordFraction); 

			Double[] preTipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
					liftingSurface.getAirfoilList().get(iTip-1).getAirfoilCreator(), 
					chordFraction
					);		

			PVector pntOnPreTipAirfoilUpp = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[0].floatValue())*preTipChordLength.floatValue())
					);
			PVector pntOnPreTipAirfoilLow = PVector.add(
					pntOnPreTipChord, 
					PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[1].floatValue())*preTipChordLength.floatValue())
					);			
			double[] pntPTU = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(0)), 
					new double[] {pntOnPreTipAirfoilUpp.x, pntOnPreTipAirfoilUpp.y, pntOnPreTipAirfoilUpp.z}
					).pnt();				
			double[] pntPTL = OCCUtils.pointProjectionOnCurve(
					OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(1)), 
					new double[] {pntOnPreTipAirfoilLow.x, pntOnPreTipAirfoilLow.y, pntOnPreTipAirfoilLow.z}
					).pnt();			
			PVector pntPreTipUpp = new PVector(
					(float) pntPTU[0], 
					(float) pntPTU[1],
					(float) pntPTU[2]
					);			
			PVector pntPreTipLow = new PVector(
					(float) pntPTL[0], 
					(float) pntPTL[1],
					(float) pntPTL[2]
					);

			PVector[] tanVecs = new PVector[2];
			tanVecs[0] = PVector.sub(
					pntOnTipAirfoilUCrv,
					pntPreTipUpp
					).normalize();
			tanVecs[1] = PVector.sub(
					pntOnTipAirfoilLCrv,
					pntPreTipLow
					).normalize();
			double tanSubVSecUppFac = tanAFac;
			double tanSubVSecLowFac = tanAFac*(-1);

			System.out.println(">>>> Upper Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[0].array()));
			System.out.println(">>>> Lower Tangent #" + (i + 1) + ": " + Arrays.toString(tanVecs[1].array()));

			// creating new sub vertical curves for patch 3			
			List<double[]> pntsSubVSec = new ArrayList<>();
			pntsSubVSec.add(arflUppCrvs.get(0).vertices()[1].pnt());
			pntsSubVSec.add(pntOnCrv);
			pntsSubVSec.add(arflLowCrvs.get(0).vertices()[1].pnt());
			CADGeomCurve3D subVSec = OCCUtils.theFactory.newCurve3D(
					pntsSubVSec, 
					false,
					MyArrayUtils.scaleArray(new double[] {tanVecs[0].x, tanVecs[0].y, tanVecs[0].z}, tanSubVSecUppFac),  
					MyArrayUtils.scaleArray(new double[] {tanVecs[1].x, tanVecs[1].y, tanVecs[1].z}, tanSubVSecLowFac),
					false
					);
			subVSecP3.add(subVSec);				
		}
		
		// first patch, sub horizontal curves creation
		List<CADGeomCurve3D> subHSecP1 = new ArrayList<>();
		for(int i = 0; i < splitsHP1.length; i++) {
			List<double[]> pntsSubH = new ArrayList<>();
			double[] mainVSec1R = mainVSec1.getRange();
			int index = i;
			pntsSubH.add(airfoilTipCrvs.get(0).vertices()[1].pnt());
			subVSecP1.forEach(crv -> {
				double[] crvRange = crv.getRange();
				pntsSubH.add(crv.value(splitsHP1[index]*(crvRange[1] - crvRange[0]) + crvRange[0]));
			});
			pntsSubH.add(mainVSec1.value(splitsHP1[i]*(mainVSec1R[1] - mainVSec1R[0]) + mainVSec1R[0]));
			CADGeomCurve3D subHSec = OCCUtils.theFactory.newCurve3D(pntsSubH, false);
			subHSecP1.add(subHSec);
		}
		
		// second patch, sub horizontal curves creation
		List<CADGeomCurve3D> subHSecP2 = new ArrayList<>();
		for(int i = 0; i < splitsHP2.length; i++) {
			List<double[]> pntsSubH = new ArrayList<>();
			double[] mainVSec1R = mainVSec1.getRange();
			double[] mainVSec2R = mainVSec2.getRange();
			int index = i;
			pntsSubH.add(mainVSec1.value(splitsHP2[i]*(mainVSec1R[1] - mainVSec1R[0]) + mainVSec1R[0]));
			subVSecP2.forEach(crv -> {
				double[] crvRange = crv.getRange();
				pntsSubH.add(crv.value(splitsHP2[index]*(crvRange[1] - crvRange[0]) + crvRange[0]));
			});
			pntsSubH.add(mainVSec2.value(splitsHP2[i]*(mainVSec2R[1] - mainVSec2R[0]) + mainVSec2R[0]));
			CADGeomCurve3D subHSec = OCCUtils.theFactory.newCurve3D(pntsSubH, false);
			subHSecP2.add(subHSec);
		}
		
		// third patch, sub horizontal curves creation
		List<CADGeomCurve3D> subHSecP3 = new ArrayList<>();
		for(int i = 0; i < splitsHP3.length; i++) {
			List<double[]> pntsSubH = new ArrayList<>();
			double[] mainVSec2R = mainVSec2.getRange();
			int index = i;
			pntsSubH.add(mainVSec2.value(splitsHP3[i]*(mainVSec2R[1] - mainVSec2R[0]) + mainVSec2R[0]));
			subVSecP3.forEach(crv -> {
				double[] crvRange = crv.getRange();
				pntsSubH.add(crv.value(splitsHP3[index]*(crvRange[1] - crvRange[0]) + crvRange[0]));
			});
			pntsSubH.add(airfoilTipCrvs.get(0).vertices()[0].pnt());
			CADGeomCurve3D subHSec = OCCUtils.theFactory.newCurve3D(pntsSubH, false);
			subHSecP3.add(subHSec);
		}
		
		// splitting main vertical guide curves
		List<OCCEdge> mainVSec1Split = new ArrayList<>();
		mainVSec1Split = OCCUtils.splitEdge(mainVSec1, Arrays.asList(splitsHP1));
		
		List<OCCEdge> mainVSec2Split = new ArrayList<>();
		mainVSec2Split = OCCUtils.splitEdge(mainVSec2, Arrays.asList(splitsHP2));
		
		// splitting construction plane guide curves
		List<OCCEdge> constPlaneGC1Split = new ArrayList<>();
		constPlaneGC1Split = OCCUtils.splitEdge(constPlaneGuideCrv1, Arrays.asList(splitsHP1));
		
		List<OCCEdge> constPlaneGC2Split = new ArrayList<>();
		constPlaneGC2Split = OCCUtils.splitEdge(constPlaneGuideCrv2, Arrays.asList(splitsHP2));
		
		List<OCCEdge> constPlaneGC3Split = new ArrayList<>();
		constPlaneGC3Split = OCCUtils.splitEdge(constPlaneGuideCrv3, Arrays.asList(splitsHP3));
		
		// splitting first patch sub vertical and horizontal curves
		List<List<OCCEdge>> subVSecP1Split = new ArrayList<List<OCCEdge>>();
		subVSecP1.stream().forEach(crv -> subVSecP1Split.add(OCCUtils.splitEdge(
				crv, 
				Arrays.asList(splitsHP1)
				)));
		
		List<List<OCCEdge>> subHSecP1Split = new ArrayList<List<OCCEdge>>();
		for(int i = 0; i < splitsHP1.length; i++) {
			List<double[]> pnts = new ArrayList<>();
			int index = i;
			subVSecP1Split.stream().forEach(crvList -> pnts.add(crvList.get(index).vertices()[1].pnt()));
			subHSecP1Split.add(OCCUtils.splitEdgeByPntsList(subHSecP1.get(i), pnts));
			}
		
		// splitting second patch sub vertical and horizontal curves
		List<List<OCCEdge>> subVSecP2Split = new ArrayList<List<OCCEdge>>();
		subVSecP2.stream().forEach(crv -> subVSecP2Split.add(OCCUtils.splitEdge(
				crv, 
				Arrays.asList(splitsHP2)
				)));
		
		List<List<OCCEdge>> subHSecP2Split = new ArrayList<List<OCCEdge>>();
		for(int i = 0; i < splitsHP2.length; i++) {
			List<double[]> pnts = new ArrayList<>();
			int index = i;
			subVSecP2Split.stream().forEach(crvList -> pnts.add(crvList.get(index).vertices()[1].pnt()));
			subHSecP2Split.add(OCCUtils.splitEdgeByPntsList(subHSecP2.get(i), pnts));
			}
		
		// splitting third patch sub vertical and horizontal curves
		List<List<OCCEdge>> subVSecP3Split = new ArrayList<List<OCCEdge>>();
		subVSecP3.stream().forEach(crv -> subVSecP3Split.add(OCCUtils.splitEdge(
				crv, 
				Arrays.asList(splitsHP3)
				)));
		
		List<List<OCCEdge>> subHSecP3Split = new ArrayList<List<OCCEdge>>();
		for(int i = 0; i < splitsHP3.length; i++) {
			List<double[]> pnts = new ArrayList<>();
			int index = i;
			subVSecP3Split.stream().forEach(crvList -> pnts.add(crvList.get(index).vertices()[1].pnt()));
			subHSecP3Split.add(OCCUtils.splitEdgeByPntsList(subHSecP3.get(i), pnts));
			}		
		
		// filling patch 1 
		// patch P1_11
		double[] pnt1_P1_11 = subHSecP1Split.get(0).get(0).vertices()[1].pnt();
		double[] pnt2_P1_11 = subHSecP1Split.get(0).get(1).vertices()[1].pnt();
		
		BRepOffsetAPI_MakeFilling fillerP1_11 = new BRepOffsetAPI_MakeFilling();
		
		fillerP1_11.Add(
				airfoilUpperCrvsP1.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_11.Add(
				airfoilUpperCrvsP1.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_11.Add(
				airfoilUpperCrvsP1.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_11.Add(
				subHSecP1Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_11.Add(
				subHSecP1Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_11.Add(
				subHSecP1Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_11.Add(
				subVSecP1Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_11.Add(
				subVSecP1Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		fillerP1_11.Add(new gp_Pnt(pnt1_P1_11[0], pnt1_P1_11[1], pnt1_P1_11[2]));
		fillerP1_11.Add(new gp_Pnt(pnt2_P1_11[0], pnt2_P1_11[1], pnt2_P1_11[2]));		
	
		fillerP1_11.Build();
		System.out.println("Deformed surface P1_11 is done? = " + fillerP1_11.IsDone());
		System.out.println("Deformed surface P1_11 shape type: " + fillerP1_11.Shape().ShapeType());
		
		// patch p1_12
		double[] pnt1_P1_12 = subHSecP1Split.get(0).get(3).vertices()[1].pnt();
		
		BRepOffsetAPI_MakeFilling fillerP1_12 = new BRepOffsetAPI_MakeFilling();
		
		fillerP1_12.Add(
				airfoilUpperCrvsP1.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_12.Add(
				airfoilUpperCrvsP1.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_12.Add(
				subVSecP1Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_12.Add(
				subVSecP1Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_12.Add(
				subHSecP1Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_12.Add(
				subHSecP1Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);		
		fillerP1_12.Add(
				mainVSec1Split.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_12.Add(
				mainVSec1Split.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		fillerP1_12.Add(new gp_Pnt(pnt1_P1_12[0], pnt1_P1_12[1], pnt1_P1_12[2]));	
	
		fillerP1_12.Build();
		System.out.println("Deformed surface P1_12 is done? = " + fillerP1_12.IsDone());
		System.out.println("Deformed surface P1_12 shape type: " + fillerP1_12.Shape().ShapeType());

		// patch P1_21
		CADGeomCurve3D cc1_P1_21 = OCCUtils.theFactory.newCurve3D(subVSecP1Split.get(0).get(2));
		CADGeomCurve3D cc2_P1_21 = OCCUtils.theFactory.newCurve3D(subVSecP1Split.get(1).get(2));
		double[] cc1_P1_21R = cc1_P1_21.getRange();
		double[] cc2_P1_21R = cc2_P1_21.getRange();
		double[] pnt1_P1_21 = constPlaneGC1Split.get(0).vertices()[1].pnt();
		double[] pnt2_P1_21 = constPlaneGC1Split.get(1).vertices()[1].pnt();
		double[] pnt3_P1_21 = cc1_P1_21.value(0.10*(cc1_P1_21R[1] - cc1_P1_21R[0]) + cc1_P1_21R[0]);
		double[] pnt4_P1_21 = cc1_P1_21.value(0.65*(cc1_P1_21R[1] - cc1_P1_21R[0]) + cc1_P1_21R[0]);
		double[] pnt5_P1_21 = cc2_P1_21.value(0.10*(cc2_P1_21R[1] - cc2_P1_21R[0]) + cc2_P1_21R[0]);
		double[] pnt6_P1_21 = cc2_P1_21.value(0.65*(cc2_P1_21R[1] - cc2_P1_21R[0]) + cc2_P1_21R[0]);
		
		BRepOffsetAPI_MakeFilling fillerP1_21 = new BRepOffsetAPI_MakeFilling();
		
		fillerP1_21.Add(
				subHSecP1Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_21.Add(
				subHSecP1Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_21.Add(
				subHSecP1Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_21.Add(
				subHSecP1Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_21.Add(
				subHSecP1Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_21.Add(
				subHSecP1Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_21.Add(
				subVSecP1Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		fillerP1_21.Add(new gp_Pnt(pnt1_P1_21[0], pnt1_P1_21[1], pnt1_P1_21[2]));
		fillerP1_21.Add(new gp_Pnt(pnt2_P1_21[0], pnt2_P1_21[1], pnt2_P1_21[2]));	
		fillerP1_21.Add(new gp_Pnt(pnt3_P1_21[0], pnt3_P1_21[1], pnt3_P1_21[2]));
		fillerP1_21.Add(new gp_Pnt(pnt4_P1_21[0], pnt4_P1_21[1], pnt4_P1_21[2]));
		fillerP1_21.Add(new gp_Pnt(pnt5_P1_21[0], pnt5_P1_21[1], pnt5_P1_21[2]));
		fillerP1_21.Add(new gp_Pnt(pnt6_P1_21[0], pnt6_P1_21[1], pnt6_P1_21[2]));
	
		fillerP1_21.Build();
		System.out.println("Deformed surface P1_21 is done? = " + fillerP1_21.IsDone());
		System.out.println("Deformed surface P1_21 shape type: " + fillerP1_21.Shape().ShapeType());
		
		// patch P1_22
		CADGeomCurve3D cc1_P1_22 = OCCUtils.theFactory.newCurve3D(subVSecP1Split.get(3).get(2));
		double[] cc1_P1_22R = cc1_P1_22.getRange();
		double[] pnt1_P1_22 = constPlaneGC1Split.get(3).vertices()[1].pnt();
		double[] pnt2_P1_22 = cc1_P1_22.value(0.10*(cc1_P1_22R[1] - cc1_P1_22R[0]) + cc1_P1_22R[0]);
		double[] pnt3_P1_22 = cc1_P1_22.value(0.65*(cc1_P1_22R[1] - cc1_P1_22R[0]) + cc1_P1_22R[0]);

		BRepOffsetAPI_MakeFilling fillerP1_22 = new BRepOffsetAPI_MakeFilling();

		fillerP1_22.Add(
				subHSecP1Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_22.Add(
				subHSecP1Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);		
		fillerP1_22.Add(
				subHSecP1Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_22.Add(
				subHSecP1Split.get(2).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_22.Add(
				subVSecP1Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_22.Add(
				mainVSec1Split.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP1_22.Add(new gp_Pnt(pnt1_P1_22[0], pnt1_P1_22[1], pnt1_P1_22[2]));
		fillerP1_22.Add(new gp_Pnt(pnt2_P1_22[0], pnt2_P1_22[1], pnt2_P1_22[2]));	
		fillerP1_22.Add(new gp_Pnt(pnt3_P1_22[0], pnt3_P1_22[1], pnt3_P1_22[2]));

		fillerP1_22.Build();
		System.out.println("Deformed surface P1_22 is done? = " + fillerP1_22.IsDone());
		System.out.println("Deformed surface P1_22 shape type: " + fillerP1_22.Shape().ShapeType());
		
		// patch P1_31
		double[] pnt1_P1_31 = subHSecP1Split.get(3).get(0).vertices()[1].pnt();
		double[] pnt2_P1_31 = subHSecP1Split.get(3).get(1).vertices()[1].pnt();
		
		BRepOffsetAPI_MakeFilling fillerP1_31 = new BRepOffsetAPI_MakeFilling();

		fillerP1_31.Add(
				subHSecP1Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_31.Add(
				subHSecP1Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_31.Add(
				subHSecP1Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_31.Add(
				airfoilLowerCrvsP1.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_31.Add(
				airfoilLowerCrvsP1.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_31.Add(
				airfoilLowerCrvsP1.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_31.Add(
				subVSecP1Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_31.Add(
				subVSecP1Split.get(2).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		fillerP1_31.Add(new gp_Pnt(pnt1_P1_31[0], pnt1_P1_31[1], pnt1_P1_31[2]));
		fillerP1_31.Add(new gp_Pnt(pnt2_P1_31[0], pnt2_P1_31[1], pnt2_P1_31[2]));	

		fillerP1_31.Build();
		System.out.println("Deformed surface P1_31 is done? = " + fillerP1_31.IsDone());
		System.out.println("Deformed surface P1_31 shape type: " + fillerP1_31.Shape().ShapeType());
		
		// patch P1_32
		double[] pnt1_P1_32 = subHSecP1Split.get(3).get(3).vertices()[1].pnt();

		BRepOffsetAPI_MakeFilling fillerP1_32 = new BRepOffsetAPI_MakeFilling();

		fillerP1_32.Add(
				subHSecP1Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_32.Add(
				subHSecP1Split.get(2).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);		
		fillerP1_32.Add(
				airfoilLowerCrvsP1.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP1_32.Add(
				airfoilLowerCrvsP1.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);		
		fillerP1_32.Add(
				subVSecP1Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_32.Add(
				subVSecP1Split.get(2).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_32.Add(
				mainVSec1Split.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1_32.Add(
				mainVSec1Split.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP1_32.Add(new gp_Pnt(pnt1_P1_32[0], pnt1_P1_32[1], pnt1_P1_32[2]));

		fillerP1_32.Build();
		System.out.println("Deformed surface P1_32 is done? = " + fillerP1_32.IsDone());
		System.out.println("Deformed surface P1_32 shape type: " + fillerP1_32.Shape().ShapeType());
		
		// patch P2_11
		double[] pnt1_P2_11 = subHSecP2Split.get(0).get(0).vertices()[1].pnt();
		
		BRepOffsetAPI_MakeFilling fillerP2_11 = new BRepOffsetAPI_MakeFilling();
		
		fillerP2_11.Add(
				airfoilUpperCrvsP2.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_11.Add(
				airfoilUpperCrvsP2.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_11.Add(
				mainVSec1Split.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_11.Add(
				mainVSec1Split.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_11.Add(
				subHSecP2Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_11.Add(
				subHSecP2Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_11.Add(
				subVSecP2Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_11.Add(
				subVSecP2Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		fillerP2_11.Add(new gp_Pnt(pnt1_P2_11[0], pnt1_P2_11[1], pnt1_P2_11[2]));
		
		fillerP2_11.Build();
		System.out.println("Deformed surface P2_11 is done? = " + fillerP2_11.IsDone());
		System.out.println("Deformed surface P2_11 shape type: " + fillerP2_11.Shape().ShapeType());
		
		// patch P2_12
		double[] pnt1_P2_12 = subHSecP2Split.get(0).get(2).vertices()[1].pnt();

		BRepOffsetAPI_MakeFilling fillerP2_12 = new BRepOffsetAPI_MakeFilling();

		fillerP2_12.Add(
				airfoilUpperCrvsP2.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_12.Add(
				airfoilUpperCrvsP2.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_12.Add(
				mainVSec2Split.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_12.Add(
				mainVSec2Split.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_12.Add(
				subHSecP2Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_12.Add(
				subHSecP2Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_12.Add(
				subVSecP2Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_12.Add(
				subVSecP2Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP2_12.Add(new gp_Pnt(pnt1_P2_12[0], pnt1_P2_12[1], pnt1_P2_12[2]));

		fillerP2_12.Build();
		System.out.println("Deformed surface P2_12 is done? = " + fillerP2_12.IsDone());
		System.out.println("Deformed surface P2_12 shape type: " + fillerP2_12.Shape().ShapeType());
		
		// patch P2_21
		CADGeomCurve3D cc1_P2_21 = OCCUtils.theFactory.newCurve3D(subVSecP2Split.get(0).get(2));
		double[] cc1_P2_21R = cc1_P2_21.getRange();
		double[] pnt1_P2_21 = constPlaneGC2Split.get(0).vertices()[1].pnt();
		double[] pnt2_P2_21 = cc1_P2_21.value(0.10*(cc1_P2_21R[1] - cc1_P2_21R[0]) + cc1_P2_21R[0]);
		double[] pnt3_P2_21 = cc1_P2_21.value(0.65*(cc1_P2_21R[1] - cc1_P2_21R[0]) + cc1_P2_21R[0]);
		
		BRepOffsetAPI_MakeFilling fillerP2_21 = new BRepOffsetAPI_MakeFilling();
		
		fillerP2_21.Add(
				subHSecP2Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_21.Add(
				subHSecP2Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);		
		fillerP2_21.Add(
				subHSecP2Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_21.Add(
				subHSecP2Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_21.Add(
				subVSecP2Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_21.Add(
				mainVSec1Split.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP2_21.Add(new gp_Pnt(pnt1_P2_21[0], pnt1_P2_21[1], pnt1_P2_21[2]));
		fillerP2_21.Add(new gp_Pnt(pnt2_P2_21[0], pnt2_P2_21[1], pnt2_P2_21[2]));	
		fillerP2_21.Add(new gp_Pnt(pnt3_P2_21[0], pnt3_P2_21[1], pnt3_P2_21[2]));

		fillerP2_21.Build();
		System.out.println("Deformed surface P2_21 is done? = " + fillerP2_21.IsDone());
		System.out.println("Deformed surface P2_21 shape type: " + fillerP2_21.Shape().ShapeType());
		
		// patch P2_22
		CADGeomCurve3D cc1_P2_22 = OCCUtils.theFactory.newCurve3D(subVSecP2Split.get(2).get(2));
		CADGeomCurve3D cc2_P2_22 = OCCUtils.theFactory.newCurve3D(constPlaneGC2Split.get(2));
		CADGeomCurve3D cc3_P2_22 = OCCUtils.theFactory.newCurve3D(constPlaneGC2Split.get(3));
		double[] cc1_P2_22R = cc1_P2_22.getRange();
		double[] cc2_P2_22R = cc2_P2_22.getRange();
		double[] cc3_P2_22R = cc3_P2_22.getRange();
		double[] pnt1_P2_22 = constPlaneGC2Split.get(2).vertices()[1].pnt();
		double[] pnt2_P2_22 = cc1_P2_22.value(0.10*(cc1_P2_22R[1] - cc1_P2_22R[0]) + cc1_P2_22R[0]);
		double[] pnt3_P2_22 = cc1_P2_22.value(0.65*(cc1_P2_22R[1] - cc1_P2_22R[0]) + cc1_P2_22R[0]);
		double[] pnt4_P2_22 = cc2_P2_22.value(0.10*(cc2_P2_22R[1] - cc2_P2_22R[0]) + cc2_P2_22R[0]);
		double[] pnt5_P2_22 = cc3_P2_22.value(0.10*(cc3_P2_22R[1] - cc3_P2_22R[0]) + cc3_P2_22R[0]);

		BRepOffsetAPI_MakeFilling fillerP2_22 = new BRepOffsetAPI_MakeFilling();

		fillerP2_22.Add(
				subHSecP2Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_22.Add(
				subHSecP2Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);		
		fillerP2_22.Add(
				subHSecP2Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_22.Add(
				subHSecP2Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_22.Add(
				subVSecP2Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_22.Add(
				mainVSec2Split.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP2_22.Add(new gp_Pnt(pnt1_P2_22[0], pnt1_P2_22[1], pnt1_P2_22[2]));
		fillerP2_22.Add(new gp_Pnt(pnt2_P2_22[0], pnt2_P2_22[1], pnt2_P2_22[2]));	
		fillerP2_22.Add(new gp_Pnt(pnt3_P2_22[0], pnt3_P2_22[1], pnt3_P2_22[2]));
		fillerP2_22.Add(new gp_Pnt(pnt4_P2_22[0], pnt4_P2_22[1], pnt4_P2_22[2]));
		fillerP2_22.Add(new gp_Pnt(pnt5_P2_22[0], pnt5_P2_22[1], pnt5_P2_22[2]));

		fillerP2_22.Build();
		System.out.println("Deformed surface P2_22 is done? = " + fillerP2_22.IsDone());
		System.out.println("Deformed surface P2_22 shape type: " + fillerP2_22.Shape().ShapeType());
		
		// patch P2_31
		double[] pnt1_P2_31 = subHSecP2Split.get(3).get(0).vertices()[1].pnt();

		BRepOffsetAPI_MakeFilling fillerP2_31 = new BRepOffsetAPI_MakeFilling();

		fillerP2_31.Add(
				airfoilLowerCrvsP2.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_31.Add(
				airfoilLowerCrvsP2.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_31.Add(
				mainVSec1Split.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_31.Add(
				mainVSec1Split.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_31.Add(
				subHSecP2Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_31.Add(
				subHSecP2Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_31.Add(
				subVSecP2Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_31.Add(
				subVSecP2Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP2_31.Add(new gp_Pnt(pnt1_P2_31[0], pnt1_P2_31[1], pnt1_P2_31[2]));

		fillerP2_31.Build();
		System.out.println("Deformed surface P2_31 is done? = " + fillerP2_31.IsDone());
		System.out.println("Deformed surface P2_31 shape type: " + fillerP2_31.Shape().ShapeType());
		
		// patch P2_32
		double[] pnt1_P2_32 = subHSecP2Split.get(3).get(2).vertices()[1].pnt();

		BRepOffsetAPI_MakeFilling fillerP2_32 = new BRepOffsetAPI_MakeFilling();

		fillerP2_32.Add(
				airfoilLowerCrvsP2.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP2_32.Add(
				airfoilLowerCrvsP2.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_32.Add(
				mainVSec2Split.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_32.Add(
				mainVSec2Split.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_32.Add(
				subHSecP2Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_32.Add(
				subHSecP2Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_32.Add(
				subVSecP2Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP2_32.Add(
				subVSecP2Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP2_32.Add(new gp_Pnt(pnt1_P2_32[0], pnt1_P2_32[1], pnt1_P2_32[2]));

		fillerP2_32.Build();
		System.out.println("Deformed surface P2_32 is done? = " + fillerP2_32.IsDone());
		System.out.println("Deformed surface P2_32 shape type: " + fillerP2_32.Shape().ShapeType());
		
		// patch P3_11
		double[] pnt1_P3_11 = subHSecP3Split.get(0).get(0).vertices()[1].pnt();

		BRepOffsetAPI_MakeFilling fillerP3_11 = new BRepOffsetAPI_MakeFilling();

		fillerP3_11.Add(
				airfoilUpperCrvsP3.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP3_11.Add(
				airfoilUpperCrvsP3.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_11.Add(
				mainVSec2Split.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_11.Add(
				mainVSec2Split.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_11.Add(
				subHSecP3Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_11.Add(
				subHSecP3Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_11.Add(
				subVSecP3Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_11.Add(
				subVSecP3Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP3_11.Add(new gp_Pnt(pnt1_P3_11[0], pnt1_P3_11[1], pnt1_P3_11[2]));

		fillerP3_11.Build();
		System.out.println("Deformed surface P3_11 is done? = " + fillerP3_11.IsDone());
		System.out.println("Deformed surface P3_11 shape type: " + fillerP3_11.Shape().ShapeType());
		
		// patch P3_12
		double[] pnt1_P3_12 = subHSecP3Split.get(0).get(2).vertices()[1].pnt();
		double[] pnt2_P3_12 = subHSecP3Split.get(0).get(3).vertices()[1].pnt();

		BRepOffsetAPI_MakeFilling fillerP3_12 = new BRepOffsetAPI_MakeFilling();

		fillerP3_12.Add(
				airfoilUpperCrvsP3.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP3_12.Add(
				airfoilUpperCrvsP3.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_12.Add(
				airfoilUpperCrvsP3.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_12.Add(
				subHSecP3Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_12.Add(
				subHSecP3Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_12.Add(
				subHSecP3Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_12.Add(
				subVSecP3Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_12.Add(
				subVSecP3Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP3_12.Add(new gp_Pnt(pnt1_P3_12[0], pnt1_P3_12[1], pnt1_P3_12[2]));
		fillerP3_12.Add(new gp_Pnt(pnt2_P3_12[0], pnt2_P3_12[1], pnt2_P3_12[2]));

		fillerP3_12.Build();
		System.out.println("Deformed surface P3_12 is done? = " + fillerP3_12.IsDone());
		System.out.println("Deformed surface P3_12 shape type: " + fillerP3_12.Shape().ShapeType());
		
		// patch P3_21
		CADGeomCurve3D cc1_P3_21 = OCCUtils.theFactory.newCurve3D(subVSecP3Split.get(0).get(2));
		CADGeomCurve3D cc2_P3_21 = OCCUtils.theFactory.newCurve3D(constPlaneGC3Split.get(0));
		CADGeomCurve3D cc3_P3_21 = OCCUtils.theFactory.newCurve3D(constPlaneGC3Split.get(1));
		double[] cc1_P3_21R = cc1_P3_21.getRange();
		double[] cc2_P3_21R = cc2_P3_21.getRange();
		double[] cc3_P3_21R = cc3_P3_21.getRange();
		double[] pnt1_P3_21 = constPlaneGC3Split.get(0).vertices()[1].pnt();		
		double[] pnt2_P3_21 = cc1_P3_21.value(0.10*(cc1_P3_21R[1] - cc1_P3_21R[0]) + cc1_P3_21R[0]);
		double[] pnt3_P3_21 = cc1_P3_21.value(0.65*(cc1_P3_21R[1] - cc1_P3_21R[0]) + cc1_P3_21R[0]);
		double[] pnt4_P3_21 = cc2_P3_21.value(0.10*(cc2_P3_21R[1] - cc2_P3_21R[0]) + cc2_P3_21R[0]);
		double[] pnt5_P3_21 = cc3_P3_21.value(0.99*(cc3_P3_21R[1] - cc3_P3_21R[0]) + cc3_P3_21R[0]);
		
		BRepOffsetAPI_MakeFilling fillerP3_21 = new BRepOffsetAPI_MakeFilling();
		
		fillerP3_21.Add(
				subHSecP3Split.get(1).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_21.Add(
				subHSecP3Split.get(1).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_21.Add(
				mainVSec2Split.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_21.Add(
				subHSecP3Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_21.Add(
				subHSecP3Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_21.Add(
				subVSecP3Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP3_21.Add(new gp_Pnt(pnt1_P3_21[0], pnt1_P3_21[1], pnt1_P3_21[2]));
		fillerP3_21.Add(new gp_Pnt(pnt2_P3_21[0], pnt2_P3_21[1], pnt2_P3_21[2]));
		fillerP3_21.Add(new gp_Pnt(pnt3_P3_21[0], pnt3_P3_21[1], pnt3_P3_21[2]));
		fillerP3_21.Add(new gp_Pnt(pnt4_P3_21[0], pnt4_P3_21[1], pnt4_P3_21[2]));
		fillerP3_21.Add(new gp_Pnt(pnt5_P3_21[0], pnt5_P3_21[1], pnt5_P3_21[2]));
		
		fillerP3_21.Build();
		System.out.println("Deformed surface P3_21 is done? = " + fillerP3_21.IsDone());
		System.out.println("Deformed surface P3_21 shape type: " + fillerP3_21.Shape().ShapeType());
		
		// patch P3_22
		double[] pnt1_P3_22 = constPlaneGC3Split.get(2).vertices()[1].pnt();
		double[] pnt2_P3_22 = constPlaneGC3Split.get(3).vertices()[1].pnt();
		
		BRepOffsetAPI_MakeFilling fillerP3_22 = new BRepOffsetAPI_MakeFilling();
		
		fillerP3_22.Add(
				subHSecP3Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_22.Add(
				subHSecP3Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_22.Add(
				subHSecP3Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_22.Add(
				subVSecP3Split.get(1).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_22.Add(
				subHSecP3Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_22.Add(
				subHSecP3Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_22.Add(
				subHSecP3Split.get(2).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		fillerP3_22.Add(new gp_Pnt(pnt1_P3_22[0], pnt1_P3_22[1], pnt1_P3_22[2]));
		fillerP3_22.Add(new gp_Pnt(pnt2_P3_22[0], pnt2_P3_22[1], pnt2_P3_22[2]));
		
		fillerP3_22.Build();
		System.out.println("Deformed surface P3_22 is done? = " + fillerP3_22.IsDone());
		System.out.println("Deformed surface P3_22 shape type: " + fillerP3_22.Shape().ShapeType());
		
		// patch P3_31
		double[] pnt1_P3_31 = subHSecP3Split.get(3).get(0).vertices()[1].pnt();	
		
		BRepOffsetAPI_MakeFilling fillerP3_31 = new BRepOffsetAPI_MakeFilling();
		
		fillerP3_31.Add(
				subHSecP3Split.get(2).get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_31.Add(
				subHSecP3Split.get(2).get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_31.Add(
				airfoilLowerCrvsP3.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP3_31.Add(
				airfoilLowerCrvsP3.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_31.Add(
				mainVSec2Split.get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_31.Add(
				mainVSec2Split.get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_31.Add(
				subVSecP3Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_31.Add(
				subVSecP3Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		fillerP3_31.Add(new gp_Pnt(pnt1_P3_31[0], pnt1_P3_31[1], pnt1_P3_31[2]));
		
		fillerP3_31.Build();
		System.out.println("Deformed surface P3_31 is done? = " + fillerP3_31.IsDone());
		System.out.println("Deformed surface P3_31 shape type: " + fillerP3_31.Shape().ShapeType());
		
		// patch P3_32
		double[] pnt1_P3_32 = subHSecP3Split.get(3).get(2).vertices()[1].pnt();
		double[] pnt2_P3_32 = subHSecP3Split.get(3).get(3).vertices()[1].pnt();

		BRepOffsetAPI_MakeFilling fillerP3_32 = new BRepOffsetAPI_MakeFilling();

		fillerP3_32.Add(
				airfoilLowerCrvsP3.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);	
		fillerP3_32.Add(
				airfoilLowerCrvsP3.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_32.Add(
				airfoilLowerCrvsP3.get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_32.Add(
				subHSecP3Split.get(2).get(2).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_32.Add(
				subHSecP3Split.get(2).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_32.Add(
				subHSecP3Split.get(2).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_32.Add(
				subVSecP3Split.get(1).get(3).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP3_32.Add(
				subVSecP3Split.get(1).get(4).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP3_32.Add(new gp_Pnt(pnt1_P3_32[0], pnt1_P3_32[1], pnt1_P3_32[2]));
		fillerP3_32.Add(new gp_Pnt(pnt2_P3_32[0], pnt2_P3_32[1], pnt2_P3_32[2]));

		fillerP3_32.Build();
		System.out.println("Deformed surface P3_32 is done? = " + fillerP3_32.IsDone());
		System.out.println("Deformed surface P3_32 shape type: " + fillerP3_32.Shape().ShapeType());		
		
		// exporting the shapes
		extraShapes.addAll(airfoilUpperCrvsP1);
		extraShapes.addAll(airfoilUpperCrvsP2);
		extraShapes.addAll(airfoilUpperCrvsP3);
		extraShapes.addAll(airfoilLowerCrvsP1);
		extraShapes.addAll(airfoilLowerCrvsP2);
		extraShapes.addAll(airfoilLowerCrvsP3);
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)segA).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)segB).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)segC).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)constPlaneGuideCrv1).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)constPlaneGuideCrv2).edge());
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)constPlaneGuideCrv3).edge());
		mainVSec1Split.forEach(crv -> extraShapes.add(crv));
		mainVSec2Split.forEach(crv -> extraShapes.add(crv));
		subVSecP1Split.forEach(crvList -> crvList.forEach(crv -> extraShapes.add(crv)));
		subVSecP2Split.forEach(crvList -> crvList.forEach(crv -> extraShapes.add(crv)));
		subVSecP3Split.forEach(crvList -> crvList.forEach(crv -> extraShapes.add(crv)));
		subHSecP1Split.forEach(crvList -> crvList.forEach(crv -> extraShapes.add(crv)));
		subHSecP2Split.forEach(crvList -> crvList.forEach(crv -> extraShapes.add(crv)));
		subHSecP3Split.forEach(crvList -> crvList.forEach(crv -> extraShapes.add(crv)));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP1_11.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP1_12.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP1_21.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP1_22.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP1_31.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP1_32.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP2_11.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP2_12.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP2_21.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP2_22.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP2_31.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP2_32.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP3_11.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP3_12.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP3_21.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP3_22.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP3_31.Shape()));
		extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(fillerP3_32.Shape()));
		
		// sewing shapes
		BRepBuilderAPI_Sewing sewMaker = new BRepBuilderAPI_Sewing();
		
		sewMaker.Init();
		sewMaker.Add(fillerP1_11.Shape());
		sewMaker.Add(fillerP1_12.Shape());
		sewMaker.Add(fillerP1_21.Shape());
		sewMaker.Add(fillerP1_22.Shape());
		sewMaker.Add(fillerP1_31.Shape());
		sewMaker.Add(fillerP1_32.Shape());
		sewMaker.Add(fillerP2_11.Shape());
		sewMaker.Add(fillerP2_12.Shape());
		sewMaker.Add(fillerP2_21.Shape());
		sewMaker.Add(fillerP2_22.Shape());
		sewMaker.Add(fillerP2_31.Shape());
		sewMaker.Add(fillerP2_32.Shape());
		sewMaker.Add(fillerP3_11.Shape());
		sewMaker.Add(fillerP3_12.Shape());
		sewMaker.Add(fillerP3_21.Shape());
		sewMaker.Add(fillerP3_22.Shape());
		sewMaker.Add(fillerP3_31.Shape());
		sewMaker.Add(fillerP3_32.Shape());
		sewMaker.Perform();
		
		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing step successful? " + !sewMaker.IsNull());	
		
		if (!sewMaker.IsNull()) {
			TopoDS_Shape tds_shape = sewMaker.SewedShape();
			// The resulting shape may consist of multiple shapes!
			// Use TopExp_Explorer to iterate through shells
			System.out.println(OCCUtils.reportOnShape(tds_shape, "Lifting Surface sewed surface (Right side)"));
			List<OCCShape> sewedShapes = new ArrayList<>();
			TopExp_Explorer exp = new TopExp_Explorer(tds_shape, TopAbs_ShapeEnum.TopAbs_SHELL);
			while (exp.More() > 0) {
				sewedShapes.add((OCCShape)OCCUtils.theFactory.newShape(exp.Current()));
				exp.Next();
			}
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Exporting sewed loft.");
			result.addAll(sewedShapes);
		}
		
		// Sewing adjacent patches in order to create one single shell
//		BRepBuilderAPI_Sewing sewMaker = new BRepBuilderAPI_Sewing();
//		
//		sewMaker.Init();
//		for(int i = 0; i < patchList.size(); i++) {
//			sewMaker.Add(patchList.get(i).getShape());
//		}
//		if(!patchTE.isEmpty()) {
//			for(int i = 0; i < patchTE.size(); i++) {
//				for(int j = 0; j < patchTE.get(i).size(); j++) {
//					sewMaker.Add(patchTE.get(i).get(j).getShape());
//				}
//			}	
//		}	
//		sewMaker.Add(((OCCShape)faceTip).getShape());
//		sewMaker.Add(((OCCShape)faceRoot).getShape());
//		sewMaker.Perform();
		
//		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing step successful? " + !sewMaker.IsNull());	
//		
//		if (!sewMaker.IsNull()) {
//			TopoDS_Shape tds_shape = sewMaker.SewedShape();
//			// The resulting shape may consist of multiple shapes!
//			// Use TopExp_Explorer to iterate through shells
//			System.out.println(OCCUtils.reportOnShape(tds_shape, "Lifting Surface sewed surface (Right side)"));
//			List<OCCShape> sewedShapes = new ArrayList<>();
//			TopExp_Explorer exp = new TopExp_Explorer(tds_shape, TopAbs_ShapeEnum.TopAbs_SHELL);
//			while (exp.More() > 0) {
//				sewedShapes.add((OCCShape)OCCUtils.theFactory.newShape(exp.Current()));
//				exp.Next();
//			}
//			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Exporting sewed loft.");
//			result.addAll(sewedShapes);
//			
//			// >>>>>>>>>>>>>>>>>>>>>>>>>>>> MIRRORING
//
//			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Mirroring sewed lofts.");
//		 	gp_Trsf mirrorTransform = new gp_Trsf();
//		 	gp_Ax2 mirrorPointPlane = new gp_Ax2(
//		 			new gp_Pnt(0.0, 0.0, 0.0),
//		 			new gp_Dir(0.0, 1.0, 0.0), // Y dir normal to reflection plane XZ
//		 			new gp_Dir(1.0, 0.0, 0.0)
//		 			);
//		 	mirrorTransform.SetMirror(mirrorPointPlane);
//			BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
//			
//			List<OCCShape> mirroredShapes = new ArrayList<>();
//			sewedShapes.stream()
//				.map(occshape -> occshape.getShape())
//				.forEach(s -> {
//					mirrorBuilder.Perform(s, 1);
//					TopoDS_Shape sMirrored = mirrorBuilder.Shape();
//					mirroredShapes.add(
//							(OCCShape)OCCUtils.theFactory.newShape(sMirrored)
//							);
//				});
//			System.out.println("Mirrored shapes: " + mirroredShapes.size());
//			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Exporting mirrored sewed loft.");
//			result.addAll(mirroredShapes);
			
			// Make a solid from the two halves
//			boolean exportSolid = true;
//			if (exportSolid) {
//				System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Experimental: build a solid ...");
//				CADSolid solidLiftingSurface = null;
//				BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
//				sewedShapes.stream()
//					.forEach( sh -> {
//						TopoDS_Shape tds_shape1 = sh.getShape();
//						TopExp_Explorer exp1 = new TopExp_Explorer(tds_shape1, TopAbs_ShapeEnum.TopAbs_SHELL);
//						solidMaker.Add(TopoDS.ToShell(exp1.Current()));					
//					});
//				mirroredShapes.stream()
//					.forEach( sh -> {
//						TopoDS_Shape tds_shape2 = sh.getShape();
//						TopExp_Explorer exp2 = new TopExp_Explorer(tds_shape2, TopAbs_ShapeEnum.TopAbs_SHELL);
//						solidMaker.Add(TopoDS.ToShell(exp2.Current()));					
//					});
//				solidMaker.Build();
//				System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
//				if (solidMaker.IsDone() == 1) {
//					solidLiftingSurface = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
//					result.add((OCCShape) solidLiftingSurface);
//					
//					System.out.println(OCCUtils.reportOnShape(((OCCShape) solidLiftingSurface).getShape(), "LS solid (Right + Left)"));
//				}
//			}				
//		}
						
				
//		if(!patchTE.isEmpty()) {
//			for(int i = 0; i < patchTE.size(); i++) {
//				patchTE.get(i).forEach(p -> result.add(p));
//			}
//		}
				
				
//				CADFace f0 = OCCUtils.theFactory.newFacePlanar(
//						cadCurveAirfoilList.get(i).get(0).value(cadCurveAirfoilList.get(i).get(0).getRange()[0]), 
//						cadCurveAirfoilList.get(i).get(0).value(cadCurveAirfoilList.get(i).get(0).getRange()[1]), 
//						cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).value(cadCurveAirfoilList.get(i).get(0).getRange()[0])
//						);		
//			}
//			if(!cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).value(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).getRange()[0]).equals(
//			    cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).value(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).getRange()[1]))) {
//				CADFace f1 = OCCUtils.theFactory.newFacePlanar(
//						cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).value(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).getRange()[0]), 
//						cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).value(cadCurveAirfoilList.get(i).get(cadCurveAirfoilList.get(i).size()-1).getRange()[1]), 
//						cadCurveAirfoilList.get(i).get(0).value(cadCurveAirfoilList.get(i).get(0).getRange()[1])					
//						);
//				CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(f1);
//				result.add((OCCShape)f1);
//			}
//		}
						
		// exporting lofts
//		if (exportLofts) {
//			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding loft surfaces");
//			result.addAll(patchList);
//		}
		
		// exporting extra shapes
		if (exportSupportShapes) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding support cad entities");
			result.addAll(extraShapes);
		}		

		// root
//		Double[] wingRootXCoordinates = liftingSurface.getAirfoilList().get(0).getAirfoilCreator().getXCoords();
//		Double[] wingRootZCoordinates = liftingSurface.getAirfoilList().get(0).getAirfoilCreator().getZCoords();
//		
//		List<double[]> ptsAirfoilRoot = new ArrayList<>();
//		
//		System.out.println(">>> wingRootXCoordinates.length " + wingRootXCoordinates.length);
//		IntStream.range(0, wingRootXCoordinates.length)
//			.forEach(i -> 
//				ptsAirfoilRoot.add(new double[] {
//						wingRootXCoordinates[i]*chords.get(0) + xApex.doubleValue(SI.METER),
//						ptsLE.get(0)[1],
//						wingRootZCoordinates[i]*chords.get(0) + zApex.doubleValue(SI.METER)
//				})
//			);
		
//		AirfoilCreator airfoilRoot = liftingSurface.getAirfoilList().get(0).getAirfoilCreator();
//		List<double[]> ptsAirfoilRoot = AircraftUtils.populateCoordinateList(ptsLE.get(0)[1], airfoilRoot, liftingSurface);
		
		// root cad curve
//		CADGeomCurve3D cadCurveRootAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilRoot, false);
		
//		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveRootAirfoil).edge());
		
		// mid section between root and kink
//		AirfoilCreator airfoilMidRK = liftingSurface.getAirfoilList().get(0).getAirfoilCreator();
//		double midAirfoilRKXCoord = ptsLE.get(0)[0] + (ptsLE.get(1)[0] - ptsLE.get(0)[0])/2;
//		double midAirfoilRKYCoord = ptsLE.get(0)[1] + (ptsLE.get(1)[1] - ptsLE.get(0)[1])/2;
//		double midAirfoilRKZCoord = ptsLE.get(0)[2] + (ptsLE.get(1)[2] - ptsLE.get(0)[2])/2;		
//		Double midAirfoilRKChord = MyMathUtils.getInterpolatedValue1DLinear(new double[] {ptsLE.get(0)[1], ptsLE.get(1)[1]}, new double[] {chords.get(0), chords.get(1)}, midAirfoilRKYCoord);
//		List<double[]> ptsAirfoilMidRK = AircraftUtils.populateCoordinateList(midAirfoilRKYCoord, airfoilMidRK, liftingSurface);		
//		Double[] wingMidRKXCoordinates = midAirfoilRK.getXCoords();
//		Double[] wingMidRKZCoordinates = midAirfoilRK.getZCoords();
					
//		System.out.println(">>> wingMidRKXCoordinates.length " + wingMidRKXCoordinates.length);
//		IntStream.range(0, wingMidRKXCoordinates.length)
//		         .forEach(i ->
//		           	 ptsAirfoilMidRK.add(new double[] {
//		           			 wingMidRKXCoordinates[i]*midAirfoilRKChord + midAirfoilRKXCoord,
//				           	 midAirfoilRKYCoord,
//				             wingMidRKZCoordinates[i]*midAirfoilRKChord + midAirfoilRKZCoord	   
//		           	 })
//		         );
		
		// mid(root-kink) section cad curve
//		CADGeomCurve3D cadCurveMidRKAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilMidRK, false);
		
//		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveMidRKAirfoil).edge());
		
		// kink
//		Double[] wingKinkXCoordinates = liftingSurface.getAirfoilList().get(1).getAirfoilCreator().getXCoords();
//		Double[] wingKinkZCoordinates = liftingSurface.getAirfoilList().get(1).getAirfoilCreator().getZCoords();
//		
//		List<double[]> ptsAirfoilKink = new ArrayList<>();
//		
//		System.out.println(">>> wingKinkXCoordinates.length " + wingKinkXCoordinates.length);
//		IntStream.range(0, wingKinkXCoordinates.length)
//			.forEach(i -> 
//				ptsAirfoilKink.add(new double[] {
//						wingKinkXCoordinates[i]*chords.get(1) + ptsLE.get(1)[0],
//						ptsLE.get(1)[1],
//						wingKinkZCoordinates[i]*chords.get(1) + ptsLE.get(1)[2],
//				})
//			);
		
//		AirfoilCreator airfoilKink = liftingSurface.getAirfoilList().get(1).getAirfoilCreator();
//		List<double[]> ptsAirfoilKink = AircraftUtils.populateCoordinateList(ptsLE.get(1)[1], airfoilKink, liftingSurface);
		
		// kink cad curve
//		CADGeomCurve3D cadCurveKinkAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilKink, false);
		
//		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveKinkAirfoil).edge());
				
		// mid sections between kink and tip
//		int nSec = 5;
//		List<CADGeomCurve3D> airfoilMidKTList = new ArrayList<>();
//		AirfoilCreator airfoilMidKT = liftingSurface.getAirfoilList().get(1).getAirfoilCreator();
//		Double[] secVec = new Double[nSec];
//		secVec = MyArrayUtils.halfCosine1SpaceDouble(
//				liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(1).doubleValue(SI.METER), 
//				liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(2).doubleValue(SI.METER), 
//				nSec
//				);
//		for(int i = 1; i < nSec-1; i++) {
//			CADGeomCurve3D cadCurveMidKTAirfoil = OCCUtils.theFactory.newCurve3D(
//					AircraftUtils.populateCoordinateList(secVec[i], airfoilMidKT, liftingSurface), 
//					false
//					);
//			airfoilMidKTList.add(cadCurveMidKTAirfoil);
//			extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveMidKTAirfoil).edge());
//		}
//		
//		airfoilMidKTList.add(0, cadCurveKinkAirfoil);
		
//		AirfoilCreator airfoilMidKT1 = liftingSurface.getAirfoilList().get(1).getAirfoilCreator();
//		AirfoilCreator airfoilMidKT2 = liftingSurface.getAirfoilList().get(2).getAirfoilCreator();
		
//		AirfoilCreator airfoilMidKT3 = liftingSurface.getAirfoilList().get(1).getAirfoilCreator();

		
//		double midAirfoilKTXCoord = ptsLE.get(1)[0] + (ptsLE.get(2)[0] - ptsLE.get(1)[0])/2;
		
//		double midAirfoilKT1YCoord = ptsLE.get(1)[1] + (ptsLE.get(2)[1] - ptsLE.get(1)[1])/5;
//		double midAirfoilKT2YCoord = ptsLE.get(1)[1] + (ptsLE.get(2)[1] - ptsLE.get(1)[1])/3;
//		double midAirfoilKT3YCoord = ptsLE.get(2)[1] - (ptsLE.get(2)[1] - ptsLE.get(1)[1])/10;
		
		
//		double midAirfoilKTZCoord = ptsLE.get(1)[2] + (ptsLE.get(2)[2] - ptsLE.get(1)[2])/2;
//		double midAirfoilKTTwist = MyMathUtils.getInterpolatedValue1DLinear(new double[] {ptsLE.get(1)[1], ptsLE.get(2)[1]}, new double[] {twists.get(1), twists.get(2)}, midAirfoilKTYCoord);
//		Double midAirfoilKTChord = MyMathUtils.getInterpolatedValue1DLinear(new double[] {ptsLE.get(1)[1], ptsLE.get(2)[1]}, new double[] {chords.get(1), chords.get(2)}, midAirfoilKTYCoord);
		
//		List<double[]> ptsAirfoilMidKT1 = AircraftUtils.populateCoordinateList(midAirfoilKT1YCoord, airfoilMidKT1, liftingSurface);
//		List<double[]> ptsAirfoilMidKT2 = AircraftUtils.populateCoordinateList(midAirfoilKT2YCoord, airfoilMidKT1, liftingSurface);
//		List<double[]> ptsAirfoilMidKT3 = AircraftUtils.populateCoordinateList(midAirfoilKT3YCoord, airfoilMidKT2, liftingSurface);
	
		
//		Double[] wingMidKTXCoordinates = midAirfoilKT.getXCoords();
//		Double[] wingMidKTZCoordinates = midAirfoilKT.getZCoords();
		
//		System.out.println(">>> wingMidKTXCoordinates.length " + wingMidKTXCoordinates.length);
//		IntStream.range(0, wingMidKTXCoordinates.length)
//				 .forEach(i ->
//		             ptsAirfoilMidKT.add(new double[] {
//				             (wingMidKTXCoordinates[i]*midAirfoilKTChord*Math.cos(midAirfoilKTTwist) + wingMidKTZCoordinates[i]*midAirfoilKTChord*Math.cos(midAirfoilKTTwist)) + midAirfoilKTXCoord,
//				              midAirfoilKTYCoord,
//				             (wingMidKTZCoordinates[i]*midAirfoilKTChord*Math.cos(midAirfoilKTTwist) - wingMidKTXCoordinates[i]*midAirfoilKTChord*Math.sin(midAirfoilKTTwist)) + midAirfoilKTZCoord	   
//		             })
//				 );
		
		// mid(kink-tip) section cad curve
//		CADGeomCurve3D cadCurveMidKT1Airfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilMidKT1, false);
//		CADGeomCurve3D cadCurveMidKT2Airfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilMidKT2, false);
//		CADGeomCurve3D cadCurveMidKT3Airfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilMidKT3, false);
//
//		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveMidKT1Airfoil).edge());
//		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveMidKT2Airfoil).edge());
//		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveMidKT3Airfoil).edge());
		
		//tip
//		Double[] wingTipXCoordinates = liftingSurface.getAirfoilList().get(2).getAirfoilCreator().getXCoords();
//		Double[] wingTipZCoordinates = liftingSurface.getAirfoilList().get(2).getAirfoilCreator().getZCoords();
//		
//		List<double[]> ptsAirfoilTip = new ArrayList<>();
//		
//		System.out.println(">>> wingTipXCoordinates.length " + wingTipXCoordinates.length);
//		IntStream.range(0, wingTipXCoordinates.length)
//		         .forEach(i ->
//		         	 ptsAirfoilTip.add(new double[] {
//		         			 (wingTipXCoordinates[i]*chords.get(2)*Math.cos(twists.get(2)) + wingTipZCoordinates[i]*chords.get(2)*Math.sin(twists.get(2))) + ptsLE.get(2)[0],
//		         			 ptsLE.get(2)[1],
//		         			 (wingTipZCoordinates[i]*chords.get(2)*Math.cos(twists.get(2)) - wingTipXCoordinates[i]*chords.get(2)*Math.sin(twists.get(2))) + ptsLE.get(2)[2]
//		         	 })
//		         );
		
//		AirfoilCreator airfoilTip = liftingSurface.getAirfoilList().get(2).getAirfoilCreator();
//		List<double[]> ptsAirfoilTip = AircraftUtils.populateCoordinateList(ptsLE.get(2)[1], airfoilTip, liftingSurface);
//		
//		//tip CAD curve
//		CADGeomCurve3D cadCurveTipAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilTip, false);
//		airfoilMidKTList.add(cadCurveTipAirfoil);
//		
//		extraShapes.add((OCCEdge)((OCCGeomCurve3D)cadCurveTipAirfoil).edge());
				
//		if (exportSupportShapes) {
//			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding support cad entities");
//			result.addAll(extraShapes);
//		}
//				
//		OCCShape patch1 = OCCUtils.makePatchThruSections(cadCurveRootAirfoil, cadCurveMidRKAirfoil, cadCurveKinkAirfoil);
//		OCCShape patch2 = OCCUtils.makePatchThruSections(airfoilMidKTList);
//		OCCShape patch3 = OCCUtils.makePatchThruSections(cadCurveMidKT1Airfoil, cadCurveMidKT2Airfoil);
//		OCCShape patch4 = OCCUtils.makePatchThruSections(cadCurveMidKT2Airfoil, cadCurveMidKT3Airfoil);
//		OCCShape patch5 = OCCUtils.makePatchThruSections(cadCurveMidKT3Airfoil, cadCurveTipAirfoil);


//		if (exportLofts) {
//			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding loft surfaces");
//            result.add(patch1);
//            result.add(patch2);
//            result.add(patch3);
//            result.add(patch4);
//            result.add(patch5);
//		}
		
		
		return result;
	}
	
	public static List<double[]> populateCoordinateList(
			double yStation,
			AirfoilCreator theCreator,
			LiftingSurface theLiftingSurface
			) {
		
		List<double[]> actualAirfoilCoordinates = new ArrayList<>();
		
//		double c = theLiftingSurface.getChordAtYActual(yStation);
		double c = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(theLiftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints()), 
				yStation
				);
		double twist = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()), 
				MyArrayUtils.convertToDoublePrimitive(
						theLiftingSurface.getLiftingSurfaceCreator().getTwistsBreakPoints().stream()
																						   .map(t -> t.doubleValue(SI.RADIAN))
																						   .collect(Collectors.toList())
						),
				yStation
				);
		double xLE = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(theLiftingSurface.getLiftingSurfaceCreator().getXLEBreakPoints()), 
				yStation
				);
		double x, y, z;

		for (int i=0; i<theCreator.getXCoords().length; i++) {

			// Scale to actual dimensions
			x = theCreator.getXCoords()[i].doubleValue()*c;
			y = 0.0;
			z = theCreator.getZCoords()[i].doubleValue()*c;

//			double twistAtY = MyMathUtils.getInterpolatedValue1DLinear(
//					MyArrayUtils.convertListOfAmountTodoubleArray(theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs()),
//					MyArrayUtils.convertToDoublePrimitive(
//							theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedTwists().stream()
//							.map(t -> t.doubleValue(SI.RADIAN))
//							.collect(Collectors.toList())),							
//					yStation
//					);

			// Rotation due to twist
			if (!theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				double r = Math.sqrt(x*x + z*z);
				x = (x - r*(1-Math.cos(-twist - theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN))));
				z = (z + r*Math.sin(-twist - theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));				
			}

			// Actual location
//			x = x + theLiftingSurface.getLiftingSurfaceCreator().getXLEAtYActual(yStation).doubleValue(SI.METER)
//				  + theLiftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
			x = x + xLE + theLiftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
			y = yStation;
			z = z + theLiftingSurface.getZApexConstructionAxes().doubleValue(SI.METER)
				  + (yStation
//						* Math.tan(theLiftingSurface.getLiftingSurfaceCreator().getDihedralAtYActual(yStation).doubleValue(SI.RADIAN)));
                        * Math.tan(AircraftUtils.getDihedralAtYActual(theLiftingSurface, yStation).doubleValue(SI.RADIAN)));	

			if (theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				actualAirfoilCoordinates.add(
						new double[] {
								x,
								theCreator.getZCoords()[i].doubleValue()*c,
								(yStation + theLiftingSurface.getZApexConstructionAxes().doubleValue(SI.METER))
						});
			} else {
				actualAirfoilCoordinates.add(new double[] {x, y, z});
			}
		}	
		
		return actualAirfoilCoordinates;
	}
	
	public static boolean isAirfoilCADCurveClosed(CADGeomCurve3D cadCurve) {
		boolean closed;
		
//		long lClosed;
//		lClosed = ((OCCEdge) cadCurve).getShape().Closed();
//		if(lClosed == 1) closed = true;
//		else closed = false;
		
		double norm1 = (MyArrayUtils.sumArrayElements(
				MyArrayUtils.subtractArrayEbE(
						MyArrayUtils.convertFromDoubleToPrimitive(cadCurve.edge().vertices()[0].pnt()),
						MyArrayUtils.convertFromDoubleToPrimitive(cadCurve.edge().vertices()[1].pnt())
				))).doubleValue();
		
		if(norm1 > 1e-5) closed = false;
		else closed = true;
		
		return closed;
	}
	
	public static Double[] getThicknessAtX(AirfoilCreator airfoil, Double xChord) {
		Double[] thickness = new Double[2];
		
		Double[] x = airfoil.getXCoords();
		Double[] z = airfoil.getZCoords();
		
		int nPnts = x.length;
		int iXMin = MyArrayUtils.getIndexOfMin(x);
		
		List<Double> xUpperPnts = new ArrayList<>();
		List<Double> zUpperPnts = new ArrayList<>();
		List<Double> xLowerPnts = new ArrayList<>();
		List<Double> zLowerPnts = new ArrayList<>();
		
		IntStream.range(0, iXMin + 1).forEach(i -> {
			xUpperPnts.add(x[i]);
			zUpperPnts.add(z[i]);
			});
		
		IntStream.range(iXMin, nPnts).forEach(i -> {
			xLowerPnts.add(x[i]);
			zLowerPnts.add(z[i]);
			});
		
		double[] xUpperArray = MyArrayUtils.convertToDoublePrimitive(xUpperPnts);
		double[] zUpperArray = MyArrayUtils.convertToDoublePrimitive(zUpperPnts);
		
		for (int i = 0; i < xUpperArray.length/2; i++) {
			double tempX = xUpperArray[i];
			double tempZ = zUpperArray[i];
			xUpperArray[i] = xUpperArray[xUpperArray.length-1-i];
			zUpperArray[i] = zUpperArray[zUpperArray.length-1-i];
			xUpperArray[xUpperArray.length-1-i] = tempX;
			zUpperArray[zUpperArray.length-1-i] = tempZ;
		}
		
		Double thU = MyMathUtils.getInterpolatedValue1DSpline(
				xUpperArray, 
				zUpperArray,
				xChord
				);
		
		Double thL = MyMathUtils.getInterpolatedValue1DSpline(
				MyArrayUtils.convertToDoublePrimitive(xLowerPnts), 
				MyArrayUtils.convertToDoublePrimitive(zLowerPnts), 
				xChord
				);
		
		// check on the direction (clockwise/anti-clockwise)
//		int iHW = Math.round(iXMin/2);
//		if(z[iHW] < 0) {
//			thickness[0] = thL;
//			thickness[1] = thU;
//		} else {
//			thickness[0] = thU;
//			thickness[1] = thL;
//		}	
		thickness[0] = thU;
		thickness[1] = thL;
		
		return thickness;
	}
	
	public static Amount<Angle> getDihedralAtYActual(LiftingSurface theLiftingSurface, Double yStation) {
		if (yStation >= 0) return getDihedralSemispanAtYActual(theLiftingSurface, yStation);
		else return getDihedralSemispanAtYActual(theLiftingSurface, -yStation);
	}
	
	public static Amount<Angle> getDihedralSemispanAtYActual(LiftingSurface theLiftingSurface, Double yStation) {

		Amount<Angle> dihedralAtY = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		if(yStation < theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(0).getEstimatedValue()) {
			System.err.println("INVALID Y STATION");
			dihedralAtY = null;
		}

		for(int i=1; i<theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().size(); i++) {

			if(yStation < theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(i).getEstimatedValue() && 
			   yStation >= theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).getEstimatedValue()
					)
				dihedralAtY = theLiftingSurface.getLiftingSurfaceCreator().getPanels().get(i-1).getDihedral();
		}
		return dihedralAtY;
	}
}
