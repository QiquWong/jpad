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
import it.unina.daf.jpadcad.occ.CADEdge;
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
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GeomAPI_ProjectPointOnCurve;
import opencascade.GeomAbs_Shape;
import opencascade.IFSelect_ReturnStatus;
import opencascade.STEPControl_StepModelType;
import opencascade.STEPControl_Writer;
import opencascade.ShapeExtend_Explorer;
import opencascade.StlAPI_Writer;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_HSequenceOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
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
//		List<Double> xmtPatch4 = Arrays.asList(
//				MyArrayUtils.halfCosine1SpaceDouble( // cosineSpaceDouble( // 
//						noseLength.plus(cylinderLength).doubleValue(SI.METER), 
//						fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)).doubleValue(SI.METER),
//						numberTailPatchSections) // n. points
//				);
		List<Double> xmtPatch4 = Arrays.asList(
		MyArrayUtils.cosineSpaceDouble( // cosineSpaceDouble( // 
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
	 * @param tipTolerance					allowed tolerance for the tip construction 
	 * @param exportLofts					include lifting surface lofts in the output shape list 
	 * @param exportSolid					include lifting surface solids in the output shape list
	 * @param exportSupportShapes			include supporting sections, outline curves, etc in the output shape list 
	 * @return
	 */
	public static List<OCCShape> getLiftingSurfaceCAD(
			LiftingSurface liftingSurface, 
			ComponentEnum typeLS,
			double tipTolerance,
			boolean exportLofts,
			boolean exportSolids,
			boolean exportSupportShapes) {
		
		if (liftingSurface == null)
			return null;
		
		List<OCCShape> result = new ArrayList<>();
		List<OCCShape> lofts = new ArrayList<>();
		List<OCCShape> solids = new ArrayList<>();
		List<OCCShape> extraShapes = new ArrayList<>();
		
		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] ");

		if (OCCUtils.theFactory == null) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Initialize CAD shape factory");
			OCCUtils.initCADShapeFactory(); // theFactory now non-null
		}

		int nPanels = liftingSurface.getLiftingSurfaceCreator().getPanels().size();
		System.out.println(">>> n. panels: " + nPanels);
				
		Amount<Length> xApex = liftingSurface.getXApexConstructionAxes();
		Amount<Length> zApex = liftingSurface.getZApexConstructionAxes();
		Amount<Angle> riggingAngle = liftingSurface.getRiggingAngle();
		
		// Build the leading edge
		List<double[]> ptsLE = new ArrayList<double[]>();
		
		// calculate FIRST breakpoint coordinates
		ptsLE.add(new double[] {xApex.doubleValue(SI.METER), 0.0, zApex.doubleValue(SI.METER)});

		// calculate breakpoints coordinates
		for (int kBP = 1; kBP < liftingSurface.getLiftingSurfaceCreator().getXLEBreakPoints().size(); kBP++) {
			double xbp = liftingSurface.getLiftingSurfaceCreator().getXLEBreakPoints().get(kBP).plus(xApex).doubleValue(SI.METER);
			double ybp = liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(kBP).doubleValue(SI.METER);
			double zbp = zApex.doubleValue(SI.METER);
//			double semiSpanPanel = liftingSurface.getLiftingSurfaceCreator().getPanels().get(kBP - 1).getSpan().times(0.5).doubleValue(SI.METER);
			double spanPanel = liftingSurface.getLiftingSurfaceCreator().getPanels().get(kBP - 1).getSpan().doubleValue(SI.METER);
			double dihedralPanel = liftingSurface.getLiftingSurfaceCreator().getPanels().get(kBP - 1).getDihedral().doubleValue(SI.RADIAN);
//			zbp = zbp + semiSpanPanel*Math.tan(dihedralPanel);
//			zbp = zbp + spanPanel*Math.tan(dihedralPanel);
			zbp = zbp + ybp*Math.tan(dihedralPanel);
			if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				ptsLE.add(new double[] {
						xbp,
						0,
						ybp + zApex.doubleValue(SI.METER)
				});
			} else {
				ptsLE.add(new double[] {xbp, ybp, zbp});
			}
			System.out.println("span #" + kBP + ": " + spanPanel);
			System.out.println("dihedral #" + kBP + ": " + dihedralPanel);
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
//		tdsEdgesLE.forEach(e -> wm.Add(e));
//		wm.Build();

		// export
		tdsEdgesLE.forEach(e -> extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(e)));
		
		// Add chord segments & build the trailing edge
		List<double[]> ptsTE = new ArrayList<double[]>();
		List<Double> chords = new ArrayList<>();
		List<Double> twists = new ArrayList<>();
		for (int kPts = 0; kPts < ptsLE.size(); kPts++) {
			double ybp = liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(kPts).doubleValue(SI.METER); 
			double chord = liftingSurface.getLiftingSurfaceCreator().getChordsBreakPoints().get(kPts).doubleValue(SI.METER);    
			chords.add(chord);
			double twist = liftingSurface.getLiftingSurfaceCreator().getTwistsBreakPoints().get(kPts).doubleValue(SI.RADIAN);		
			twists.add(twist);
			System.out.println(">>> ybp:   " + ybp);
			System.out.println(">>> chord: " + chord);
			System.out.println(">>> twist: " + twist);
			if(typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
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
		tdsChords.forEach(e -> extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(e)));
		
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
		tdsEdgesTE.forEach(e -> extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(e)));

		// Create airfoils at specific y stations
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

		cadCurveAirfoilBPList.forEach(crv -> extraShapes.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));

		// airfoils between breakpoints
		List<CADGeomCurve3D> cadCurveAirfoilBetBPList = new ArrayList<CADGeomCurve3D>();
		int nSec = 5; // number of sections between two contiguous breakpoints
		for(int iP = 1; iP <= liftingSurface.getLiftingSurfaceCreator().getPanels().size(); iP++) {
			List<CADGeomCurve3D> cadCurveAirfoilPanelList = new ArrayList<CADGeomCurve3D>();
			double[] secVec = new double[nSec + 2];
			secVec = MyArrayUtils.linspace(
					liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(iP-1).doubleValue(SI.METER), 
					liftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(iP).doubleValue(SI.METER), 
					nSec + 2
					);
			double[] fSecVec = secVec;
			int fIP = iP;
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
		cadCurveAirfoilBetBPList.forEach(crv -> extraShapes.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));

		// Create patches through the sections defined above
		List<OCCShape> patchWing = new ArrayList<>();	
//		for(int i = 0; i < cadCurveAirfoilList.size(); i++) {
//			for(int j = 1; j < cadCurveAirfoilList.get(i).size(); j++) {
//				patchWing.add(OCCUtils.makePatchThruSections(
//						cadCurveAirfoilList.get(i).get(j-1),
//						cadCurveAirfoilList.get(i).get(j)
//						));
//			}
//		}
		patchWing.addAll(cadCurveAirfoilList.stream()
		                   .map(OCCUtils::makePatchThruSections)
		                   .collect(Collectors.toList()));
		
		// Closing the trailing edge
		List<List<OCCShape>> patchTE = new ArrayList<List<OCCShape>>();
		for(int i = 0; i < cadCurveAirfoilList.size(); i++) {
			List<OCCShape> patchTEPanel = new ArrayList<>();
			for(int j = 1; j < cadCurveAirfoilList.get(i).size(); j++) {
				double[] crvR1 = cadCurveAirfoilList.get(i).get(j-1).getRange();
				double[] crvR2 = cadCurveAirfoilList.get(i).get(j).getRange();
				CADFace face1 = OCCUtils.theFactory.newFacePlanar(
						cadCurveAirfoilList.get(i).get(j-1).value(crvR1[0]), 
						cadCurveAirfoilList.get(i).get(j-1).value(crvR1[1]), 
						cadCurveAirfoilList.get(i).get(j).value(crvR2[0])
						);
				CADFace face2 = OCCUtils.theFactory.newFacePlanar(
						cadCurveAirfoilList.get(i).get(j).value(crvR2[0]), 
						cadCurveAirfoilList.get(i).get(j).value(crvR2[1]), 
						cadCurveAirfoilList.get(i).get(j-1).value(crvR1[1])
						);	
				CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(face1, face2);
				patchTEPanel.add((OCCShape)shell);
			}
			patchTE.add(patchTEPanel);
		}
		
		// Closing the tip using a filler surface
		List<OCCShape> patchWingTip = new ArrayList<>();
		int iTip = cadCurveAirfoilBPList.size() - 1;                      // tip airfoil index 
		CADGeomCurve3D airfoilTip = cadCurveAirfoilBPList.get(iTip);      // airfoil CAD curve
		CADGeomCurve3D airfoilPreTip = cadCurveAirfoilBPList.get(iTip-1); // second to last airfoil CAD curve
				
		Double rTh = liftingSurface.getAirfoilList().get(iTip).getAirfoilCreator().getThicknessToChordRatio(); 
		Double eTh = rTh*chords.get(iTip); // effective airfoil thickness
		
		// creating the tip chord edge
		OCCEdge tipChord = (OCCEdge) OCCUtils.theFactory.newShape(tdsChords.get(iTip));	
		
		// creating the second to last chord edge
		OCCEdge preTipChord = (OCCEdge) OCCUtils.theFactory.newShape(tdsChords.get(iTip-1));	
		
		// splitting the tip airfoil curve using the first vertex of the tip chord
		List<OCCEdge> airfoilTipCrvs = OCCUtils.splitEdge(
				airfoilTip, 
				OCCUtils.getVertexFromEdge(tipChord, 0).pnt()
				);
		
		// adjusting points for the tip airfoil
		int nPnts = 200;
		CADGeomCurve3D airfoilUpp = OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(0));
		CADGeomCurve3D airfoilLow = OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(1));
		airfoilUpp.discretize(nPnts);
		airfoilLow.discretize(nPnts);
		List<gp_Pnt> gpPntAirfoilUpp = ((OCCGeomCurve3D)airfoilUpp).getDiscretizedCurve().getPoints();
		List<gp_Pnt> gpPntAirfoilLow = ((OCCGeomCurve3D)airfoilLow).getDiscretizedCurve().getPoints();
		gpPntAirfoilUpp.set(nPnts - 1, BRep_Tool.Pnt(OCCUtils.getVertexFromEdge(tipChord, 0).getShape()));
		gpPntAirfoilLow.set(0, BRep_Tool.Pnt(OCCUtils.getVertexFromEdge(tipChord, 0).getShape()));
		CADGeomCurve3D airfoilUppMod = OCCUtils.theFactory.newCurve3DGP(gpPntAirfoilUpp, false);
		CADGeomCurve3D airfoilLowMod = OCCUtils.theFactory.newCurve3DGP(gpPntAirfoilLow, false);
		airfoilTipCrvs.clear();
		airfoilTipCrvs.add((OCCEdge) airfoilUppMod.edge());
		airfoilTipCrvs.add((OCCEdge) airfoilLowMod.edge());
		
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
		if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
			teVector.z = leVector.z; // slightly modified in order to obtain a plane
		}	
		float teLength = teVector.mag();
		float bLength = eTh.floatValue()/leLength;		
		PVector bVector = PVector.mult(teVector, bLength);
		PVector bPnt = PVector.add(te2, bVector);
		
		// creating the edge a
		List<PVector> aPnts = new ArrayList<>();
		aPnts.add(le2);
		aPnts.add(aPnt);
		CADGeomCurve3D segmentA = OCCUtils.theFactory.newCurve3DP(aPnts, false);
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)segmentA).edge());
		
		// creating the edge b
		List<PVector> bPnts = new ArrayList<>();
		bPnts.add(te2);
		bPnts.add(bPnt);
		CADGeomCurve3D segmentB = OCCUtils.theFactory.newCurve3DP(bPnts, false);
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)segmentB).edge());
		
		// creating the edge c
		List<PVector> cPnts = new ArrayList<>();
		cPnts.add(aPnt);
		cPnts.add(bPnt);
		CADGeomCurve3D segmentC = OCCUtils.theFactory.newCurve3DP(cPnts, false);
		extraShapes.add((OCCEdge)((OCCGeomCurve3D)segmentC).edge());
		
		// creating vertical splitting vectors for the tip airfoil curve, orthogonal to the chord
		PVector chordTipVector = PVector.sub(te2, le2); // vector in the airfoil plane	
		PVector chordTipNVector = new PVector();
		PVector.cross(chordTipVector, aVector, chordTipNVector).normalize(); // vector in the airfoil plane, normal to the chord, normalized
		
		// creating vertical splitting vectors for the second to last airfoil curve
		PVector chordPreTipVector = PVector.sub(te1, le1); 	
		PVector chordPreTipNVector = new PVector();
		PVector.cross(chordPreTipVector, aVector, chordPreTipNVector).normalize(); 
		
		// creating points for the guide curves in the construction plane formed by the segments a, b and c
		double[] mainVSecVector = {0.25, 0.75};
		PVector cPnt = PVector.lerp(aPnt, bPnt, (float) mainVSecVector[0]);
		PVector dPnt = PVector.lerp(aPnt, bPnt, (float) mainVSecVector[1]);	
		PVector ePnt = PVector.lerp(le2, te2, (float) mainVSecVector[1]);
		PVector fPnt = PVector.lerp(dPnt, ePnt, 0.10f); // slightly modified D point	
		PVector gPnt = PVector.lerp(te2, bPnt, 0.75f);
		
		// creating the guide curves in the construction plane
		List<double[]> constrPlaneGuideCrv1Pnts = new ArrayList<>();
		constrPlaneGuideCrv1Pnts.add(new double[] {le2.x, le2.y, le2.z});
		constrPlaneGuideCrv1Pnts.add(new double[] {cPnt.x, cPnt.y, cPnt.z});
		
		List<double[]> constrPlaneGuideCrv2Pnts = new ArrayList<>();
		constrPlaneGuideCrv2Pnts.add(new double[] {cPnt.x, cPnt.y, cPnt.z});
		constrPlaneGuideCrv2Pnts.add(new double[] {fPnt.x, fPnt.y, fPnt.z});
		constrPlaneGuideCrv2Pnts.add(new double[] {gPnt.x, gPnt.y, gPnt.z});
		
		// creating the tangent vectors for the guide curves in the construction plane	
//		PVector cVector = PVector.sub(bPnt, aPnt);
		PVector cVector = PVector.sub(cPnt, aPnt);
		PVector gVector = PVector.sub(gPnt, fPnt);				
//		double tanAFac = 4*aVector.mag(); // wing and hTail
//		double tanCFac = 5*aVector.mag();
//		double tanGFac = 1*aVector.mag();	
//		double tanAFac = 7*aVector.mag(); // these parameters work fine for the IRON canard
//		double tanCFac = 7*aVector.mag();
//		double tanGFac = 1*aVector.mag();	
		double tanAFac = 1;
		double tanCFac = (cVector.mag()/aVector.mag())*0.75;
		double tanGFac = tanCFac;
		aVector.normalize();
		cVector.normalize();
		gVector.normalize();			
		double[] tanAConstrPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {aVector.x, aVector.y, aVector.z}, tanAFac);
		double[] tanCConstrPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {cVector.x, cVector.y, cVector.z}, tanCFac);
		double[] tanGConstrPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {gVector.x, gVector.y, gVector.z}, tanGFac);	
		
		System.out.println(">>>> Tangent Vector a: " + Arrays.toString(aVector.array()));
		System.out.println(">>>> Tangent Vector c: " + Arrays.toString(cVector.array()));	
		System.out.println(">>>> Tangent Vector g: " + Arrays.toString(gVector.array()));		

		CADGeomCurve3D constrPlaneGuideCrv1 = OCCUtils.theFactory.newCurve3D(
				constrPlaneGuideCrv1Pnts, 
				false, 
				tanAConstrPlaneGuideCrv, 
				tanCConstrPlaneGuideCrv, 
				false
				);	
		CADGeomCurve3D constrPlaneGuideCrv2_0 = OCCUtils.theFactory.newCurve3D(
				constrPlaneGuideCrv2Pnts, 
				false, 
				tanCConstrPlaneGuideCrv, 
				tanGConstrPlaneGuideCrv, 
				false
				);
		
		// splitting constrPlaneGuideCrv2_0 for further manipulations
		List<OCCEdge> constrPlaneGuideCrvs2 = OCCUtils.splitEdge(
				constrPlaneGuideCrv2_0, 
				new double[] {fPnt.x, fPnt.y, fPnt.z}
				);		
		CADGeomCurve3D constrPlaneGuideCrv2 = OCCUtils.theFactory.newCurve3D(constrPlaneGuideCrvs2.get(0));
		CADGeomCurve3D constrPlaneGuideCrv3 = OCCUtils.theFactory.newCurve3D(constrPlaneGuideCrvs2.get(1));
		
		List<CADGeomCurve3D> constrPlaneGuideCrvs = new ArrayList<CADGeomCurve3D>();
		constrPlaneGuideCrvs.add(constrPlaneGuideCrv1);
		constrPlaneGuideCrvs.add(constrPlaneGuideCrv2);
		constrPlaneGuideCrvs.add(constrPlaneGuideCrv3);		
		constrPlaneGuideCrvs.forEach(crv -> extraShapes.add((OCCEdge)((OCCGeomCurve3D)crv).edge())); // export
		
		// creating main vertical sections #1, #2 and #3
		PVector[] secPnts = {cPnt, fPnt};
		List<CADGeomCurve3D[]> mainVSections = new ArrayList<>();
		for(int i = 0; i < 2; i++) {
			CADGeomCurve3D[] mainVSec = createVerCrvsForTipClosure(
			liftingSurface, 
			airfoilTipCrvs,
			airfoilPreTipCrvs,
			new PVector[] {le1, le2},
			new PVector[] {te1, te2},
			mainVSecVector[i],
			new double[] {secPnts[i].x, secPnts[i].y, secPnts[i].z}
			);
			mainVSections.add(mainVSec);
		}
		
		CADGeomCurve3D[] mainVSec3 = createVerCrvsForTipClosure( 
				airfoilTipCrvs,
				airfoilPreTipCrvs,
				new PVector[] {le1, le2},
				new PVector[] {te1, te2},
				new double[] {gPnt.x, gPnt.y, gPnt.z}
				);
		mainVSections.add(mainVSec3); // trailing edge vertical section curve
		
		mainVSections.forEach(crvs -> { // export
			extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[0]).edge());
			extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[1]).edge());
		});
		
		// creating sub vertical sections		
		double[] subVSecP1Vector = {0.10, 0.15, 0.40, 0.55, 0.60, 0.75, 0.90}; 
		double[] subVSecP2Vector = {0.25, 0.50, 0.75};
		double[] subVSecP3Vector = {0.25, 0.50, 0.75};
		List<double[]> subVSecVector = new ArrayList<>();
		subVSecVector.add(subVSecP1Vector);
		subVSecVector.add(subVSecP2Vector);
		subVSecVector.add(subVSecP3Vector);
				
		List<CADGeomCurve3D[]> subVSecP1 = new ArrayList<>();
		List<CADGeomCurve3D[]> subVSecP2 = new ArrayList<>();
		List<CADGeomCurve3D[]> subVSecP3 = new ArrayList<>();	
		List<List<CADGeomCurve3D[]>> subVSec = new ArrayList<List<CADGeomCurve3D[]>>();
		
		for(int i = 0; i < 3; i++) {
			int idx = i;
			subVSec.add(Arrays
				  .stream(subVSecVector.get(i))
			      .mapToObj(f -> {
			    	  double[] crvRange = constrPlaneGuideCrvs.get(idx).getRange();
			    	  double[] pntOnGuideCurve = constrPlaneGuideCrvs.get(idx).value(f*(crvRange[1] - crvRange[0]) + crvRange[0]);
			    	  double interpCoord;
			    	  double chordFraction;
			    	  double x = pntOnGuideCurve[0];
			    	  if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
			    		  interpCoord = pntOnGuideCurve[1];
			    		  double xLE = MyMathUtils.getInterpolatedValue1DLinear(
			    				  new double[] {le2.y, aPnt.y}, 
			    				  new double[] {le2.x, aPnt.x}, 
			    				  interpCoord
			    				  );
			    		  double xTE = MyMathUtils.getInterpolatedValue1DLinear(
			    				  new double[] {te2.y, bPnt.y}, 
			    				  new double[] {te2.x, bPnt.x}, 
			    				  interpCoord
			    				  );
			    		  chordFraction = (x - xLE)/(xTE - xLE);
			    	  } else {
			    		  interpCoord = pntOnGuideCurve[2];
			    		  double xLE = MyMathUtils.getInterpolatedValue1DLinear(
			    				  new double[] {le2.z, aPnt.z}, 
			    				  new double[] {le2.x, aPnt.x}, 
			    				  interpCoord
			    				  );
			    		  double xTE = MyMathUtils.getInterpolatedValue1DLinear(
			    				  new double[] {te2.z, bPnt.z}, 
			    				  new double[] {te2.x, bPnt.x}, 
			    				  interpCoord
			    				  );
			    		  chordFraction = (x - xLE)/(xTE - xLE);
			    	  }
			    	  CADGeomCurve3D[] subVSecCrvs = createVerCrvsForTipClosure(
			    			  liftingSurface, 
			    			  airfoilTipCrvs,
			    			  airfoilPreTipCrvs,
			    			  new PVector[] {le1, le2},
			    			  new PVector[] {te1, te2},
			    			  chordFraction,
			    			  pntOnGuideCurve
			    			  );
			    	  return subVSecCrvs;
			      })
			      .collect(Collectors.toList())
				  );
		}
		subVSecP1.addAll(subVSec.get(0));
		subVSecP2.addAll(subVSec.get(1));
		subVSecP3.addAll(subVSec.get(2));
		
		subVSec.forEach(list -> list.forEach(crvs -> { // export
			extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[0]).edge());
			extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[1]).edge());
		}));
		
		// splitting the tip airfoil curves and the construction curve #1 in order to fill the wing tip LE correctly
		List<OCCEdge> airfoilUpperCrvs = new ArrayList<>();
		List<OCCEdge> airfoilLowerCrvs = new ArrayList<>();

		airfoilUpperCrvs.addAll(OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(0)), 
				subVSecP1.get(1)[0].edge().vertices()[0].pnt()
				));
		airfoilLowerCrvs.addAll(OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(1)), 
				subVSecP1.get(1)[1].edge().vertices()[1].pnt()
				));
		airfoilUpperCrvs.forEach(crv -> extraShapes.add(crv));
		airfoilLowerCrvs.forEach(crv -> extraShapes.add(crv));
		
		List<OCCEdge> constPlaneGuideCrvs1 = new ArrayList<>();
		constPlaneGuideCrvs1.addAll(OCCUtils.splitEdge(
				constrPlaneGuideCrv1, 
				subVSecP1.get(1)[0].edge().vertices()[1].pnt()
				));
		
		// creating a filler surface at the wing tip leading edge, upper		
		double[] contrCrvUppRng = subVSecP1.get(0)[0].getRange();
		double[] contrPntUpp1 = subVSecP1.get(0)[0].value(0.25*(contrCrvUppRng[1] - contrCrvUppRng[0]) + contrCrvUppRng[0]);
		double[] contrPntUpp2 = subVSecP1.get(0)[0].value(0.50*(contrCrvUppRng[1] - contrCrvUppRng[0]) + contrCrvUppRng[0]);
		double[] contrPntUpp3 = subVSecP1.get(0)[0].value(0.75*(contrCrvUppRng[1] - contrCrvUppRng[0]) + contrCrvUppRng[0]);

		BRepOffsetAPI_MakeFilling fillerP1Upp = new BRepOffsetAPI_MakeFilling();

		fillerP1Upp.Add(
				airfoilUpperCrvs.get(1).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1Upp.Add(
				((OCCEdge)((OCCGeomCurve3D)subVSecP1.get(1)[0]).edge()).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1Upp.Add(
				constPlaneGuideCrvs1.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);		

		fillerP1Upp.Add(new gp_Pnt(contrPntUpp1[0], contrPntUpp1[1], contrPntUpp1[2]));
		fillerP1Upp.Add(new gp_Pnt(contrPntUpp2[0], contrPntUpp2[1], contrPntUpp2[2]));
		fillerP1Upp.Add(new gp_Pnt(contrPntUpp3[0], contrPntUpp3[1], contrPntUpp3[2]));

		fillerP1Upp.Build();
		System.out.println("Deformed surface P1 Upp is done? = " + fillerP1Upp.IsDone());
		System.out.println("Deformed surface P1 Upp shape type: " + fillerP1Upp.Shape().ShapeType());
		
		patchWingTip.add((OCCShape)(OCCUtils.theFactory.newShape(fillerP1Upp.Shape())));
		
        // creating a filler surface at the wing tip leading edge, lower
		double[] contrCrvLowRng = subVSecP1.get(0)[1].getRange();
		double[] contrPntLow1 = subVSecP1.get(0)[1].value(0.25*(contrCrvLowRng[1] - contrCrvLowRng[0]) + contrCrvLowRng[0]);
		double[] contrPntLow2 = subVSecP1.get(0)[1].value(0.50*(contrCrvLowRng[1] - contrCrvLowRng[0]) + contrCrvLowRng[0]);
		double[] contrPntLow3 = subVSecP1.get(0)[1].value(0.75*(contrCrvLowRng[1] - contrCrvLowRng[0]) + contrCrvLowRng[0]);

		BRepOffsetAPI_MakeFilling fillerP1Low = new BRepOffsetAPI_MakeFilling();

		fillerP1Low.Add(
				constPlaneGuideCrvs1.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1Low.Add(
				((OCCEdge)((OCCGeomCurve3D)subVSecP1.get(1)[1]).edge()).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillerP1Low.Add(
				airfoilLowerCrvs.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		fillerP1Low.Add(new gp_Pnt(contrPntLow1[0], contrPntLow1[1], contrPntLow1[2]));
		fillerP1Low.Add(new gp_Pnt(contrPntLow2[0], contrPntLow2[1], contrPntLow2[2]));
		fillerP1Low.Add(new gp_Pnt(contrPntLow3[0], contrPntLow3[1], contrPntLow3[2]));

		fillerP1Low.Build();
		System.out.println("Deformed surface P1 Low is done? = " + fillerP1Low.IsDone());
		System.out.println("Deformed surface P1 Low shape type: " + fillerP1Low.Shape().ShapeType());
		
		patchWingTip.add((OCCShape)(OCCUtils.theFactory.newShape(fillerP1Low.Shape())));
		
		// patching through patch #1 vertical sections
		List<CADGeomCurve3D> sectionsListP1Upp = new ArrayList<>();
		List<CADGeomCurve3D> sectionsListP1Low = new ArrayList<>();
		
		sectionsListP1Upp.addAll(subVSecP1.stream()
										  .skip(1)	
										  .map(crvs -> crvs[0])
										  .collect(Collectors.toList())		
				);
		sectionsListP1Upp.add(mainVSections.get(0)[0]);
		
		sectionsListP1Low.addAll(subVSecP1.stream()	
										  .skip(1)
										  .map(crvs -> crvs[1])
										  .collect(Collectors.toList())		
				);
		sectionsListP1Low.add(mainVSections.get(0)[1]);
		
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP1Upp));
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP1Low));
		
		// patching through patch #2 vertical sections, first step
		List<CADGeomCurve3D> sectionsListP2_1Upp = new ArrayList<>();
		List<CADGeomCurve3D> sectionsListP2_1Low = new ArrayList<>();
		
		sectionsListP2_1Upp.add(mainVSections.get(0)[0]);
		sectionsListP2_1Upp.addAll(subVSecP2.stream()
											.limit(2)
											.map(crvs -> crvs[0])
											.collect(Collectors.toList())		
				);
		
		sectionsListP2_1Low.add(mainVSections.get(0)[1]);
		sectionsListP2_1Low.addAll(subVSecP2.stream()
											.limit(2)	
											.map(crvs -> crvs[1])
											.collect(Collectors.toList())		
				);
		
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP2_1Upp));
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP2_1Low));
		
		// patching through patch #2 vertical sections, second step
		List<CADGeomCurve3D> sectionsListP2_2Upp = new ArrayList<>();
		List<CADGeomCurve3D> sectionsListP2_2Low = new ArrayList<>();
		
		sectionsListP2_2Upp.addAll(subVSecP2.stream()
											.skip(1)
											.map(crvs -> crvs[0])
											.collect(Collectors.toList())		
				);
		sectionsListP2_2Upp.add(mainVSections.get(1)[0]);
		
		sectionsListP2_2Low.addAll(subVSecP2.stream()
											.skip(1)	
											.map(crvs -> crvs[1])
											.collect(Collectors.toList())		
				);
		sectionsListP2_2Low.add(mainVSections.get(1)[1]);
		
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP2_2Upp));
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP2_2Low));
		
		// patching through patch #3 vertical sections
		List<CADGeomCurve3D> sectionsListP3Upp = new ArrayList<>();
		List<CADGeomCurve3D> sectionsListP3Low = new ArrayList<>();
		
		sectionsListP3Upp.add(mainVSections.get(1)[0]);
		sectionsListP3Upp.addAll(subVSecP3.stream()
										  .map(crvs -> crvs[0])
										  .collect(Collectors.toList())		
				);
		sectionsListP3Upp.add(mainVSections.get(2)[0]);
		
		sectionsListP3Low.add(mainVSections.get(1)[1]);
		sectionsListP3Low.addAll(subVSecP3.stream()
										  .map(crvs -> crvs[1])
										  .collect(Collectors.toList())		
				);
		sectionsListP3Low.add(mainVSections.get(2)[1]);
		
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP3Upp));
		patchWingTip.add(OCCUtils.makePatchThruSections(sectionsListP3Low));
				
		// filling the wing tip trailing edge		
		CADShape wingTipTE = OCCUtils.makeFilledFace(
				mainVSections.get(2)[0],
				mainVSections.get(2)[1],
				OCCUtils.theFactory.newCurve3D(
						airfoilUpperCrvs.get(0).vertices()[0].pnt(), 
						airfoilLowerCrvs.get(1).vertices()[1].pnt()
						)
				);
		
		patchWingTip.add((OCCShape)wingTipTE);
		
		// exporting wing tip lofts
		lofts.addAll(patchWingTip);
		
		// sewing wing tip shapes
		BRepBuilderAPI_Sewing sewMakerTip = new BRepBuilderAPI_Sewing();
			
		sewMakerTip.Init();	
		sewMakerTip.SetTolerance(tipTolerance);
		patchWingTip.forEach(s -> sewMakerTip.Add(s.getShape()));
		sewMakerTip.Perform();
		
		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Tip sewing step successful? " + !sewMakerTip.IsNull());
		
		List<OCCShape> wingTip = new ArrayList<>();
		if(!sewMakerTip.IsNull()) {
			TopoDS_Shape tds_shape = sewMakerTip.SewedShape();
			System.out.println(OCCUtils.reportOnShape(tds_shape, "Tip sewed surface"));
			TopExp_Explorer exp = new TopExp_Explorer(tds_shape, TopAbs_ShapeEnum.TopAbs_SHELL);
			while(exp.More() > 0) {
				wingTip.add((OCCShape)OCCUtils.theFactory.newShape(exp.Current()));
				exp.Next();
			}
		} else {
			// closing wing tip in case the sewing has not been obtained
			CADShape faceTip = OCCUtils.makeFilledFace(
					cadCurveAirfoilBPList.get(iTip),
					OCCUtils.theFactory.newCurve3D(
							cadCurveAirfoilBPList.get(iTip).edge().vertices()[0].pnt(), 
							cadCurveAirfoilBPList.get(iTip).edge().vertices()[1].pnt()
							)
					);
			wingTip.add((OCCShape)faceTip);
		}
		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Exporting wing tip loft.");
		lofts.addAll(wingTip);
			
		// Sewing adjacent wing patches in order to create one single shell
		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		
		sewMakerWing.Init();	
		sewMakerWing.SetTolerance(tipTolerance);
		
		patchWing.forEach(p -> sewMakerWing.Add(p.getShape()));
		patchTE.forEach(pL -> pL.forEach(p -> sewMakerWing.Add(p.getShape())));
		sewMakerWing.Add(wingTip.get(0).getShape());
		
		// closing the wing root in case of Vertical Tail
		if(typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
			CADShape faceRoot = OCCUtils.makeFilledFace(
					cadCurveAirfoilList.get(0).get(0),
					OCCUtils.theFactory.newCurve3D(
							cadCurveAirfoilList.get(0).get(0).edge().vertices()[0].pnt(), 
							cadCurveAirfoilList.get(0).get(0).edge().vertices()[1].pnt()
							)
					);
			sewMakerWing.Add(((OCCShape)faceRoot).getShape());
		}		
//		// rounded tip MUST be there ==> sewedShapesTip.get(0) non null
//		assert !sewMakerTip.IsNull();	
		
		sewMakerWing.Perform();
				
		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing step successful? " + !sewMakerWing.IsNull());	
 
		List<OCCShape> sewedWing = new ArrayList<>();
		if(!sewMakerWing.IsNull()) {
			TopoDS_Shape tds_shape = sewMakerWing.SewedShape();
			System.out.println(OCCUtils.reportOnShape(tds_shape, "Sewed lifting surface (Right side)"));
			List<OCCShape> rigthSideWing = new ArrayList<>();
			TopExp_Explorer exp = new TopExp_Explorer(tds_shape, TopAbs_ShapeEnum.TopAbs_SHELL);
			while(exp.More() > 0) {
				rigthSideWing.add((OCCShape)OCCUtils.theFactory.newShape(exp.Current()));
				exp.Next();
			}
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Exporting sewed loft.");
			lofts.addAll(rigthSideWing);

			// Mirroring
			if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
				System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Mirroring rigth lofts.");
				List<OCCShape> leftSideWing = new ArrayList<>();
				gp_Trsf mirrorTransform = new gp_Trsf();
				gp_Ax2 mirrorPointPlane = new gp_Ax2(
						new gp_Pnt(0.0, 0.0, 0.0),
						new gp_Dir(0.0, 1.0, 0.0), // Y direction normal to reflection plane XZ
						new gp_Dir(1.0, 0.0, 0.0)
						);
				mirrorTransform.SetMirror(mirrorPointPlane);				
				BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
				rigthSideWing.stream()
						     .map(occshape -> occshape.getShape())
						     .forEach(s -> {
						    	 mirrorBuilder.Perform(s, 1);
						    	 TopoDS_Shape sMirrored = mirrorBuilder.Shape();
						    	 leftSideWing.add(
						    			 (OCCShape)OCCUtils.theFactory.newShape(sMirrored)
						    			 );
						     });
				System.out.println("Mirrored shapes: " + leftSideWing.size());
				System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Exporting mirrored sewed loft.");
				lofts.addAll(leftSideWing);
				
				// sewing the two halves
				BRepBuilderAPI_Sewing sewMakerHalves = new BRepBuilderAPI_Sewing();
				
				sewMakerHalves.Init();				
				rigthSideWing.forEach(s -> {
					TopoDS_Shape tds_shape_0 = s.getShape();
					TopExp_Explorer exp_0 = new TopExp_Explorer(tds_shape_0, TopAbs_ShapeEnum.TopAbs_SHELL);
					sewMakerHalves.Add(TopoDS.ToShell(exp_0.Current()));		
				});			
				leftSideWing.forEach(s -> {
					TopoDS_Shape tds_shape_0 = s.getShape();
					TopExp_Explorer exp_0 = new TopExp_Explorer(tds_shape_0, TopAbs_ShapeEnum.TopAbs_SHELL);
					sewMakerHalves.Add(TopoDS.ToShell(exp_0.Current()));	
				});
				
				sewMakerHalves.Perform();
				
				System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing halves step successful? " + !sewMakerHalves.IsNull());
				
				if(!sewMakerHalves.IsNull()) {
					TopoDS_Shape tds_shape_0 = sewMakerHalves.SewedShape();
					System.out.println(OCCUtils.reportOnShape(tds_shape_0, "Lifting Surface sewed surface (Right + Left side)"));
					TopExp_Explorer exp_0 = new TopExp_Explorer(tds_shape_0, TopAbs_ShapeEnum.TopAbs_SHELL);
					while(exp_0.More() > 0) {
						sewedWing.add((OCCShape)OCCUtils.theFactory.newShape(exp_0.Current()));
						exp_0.Next();
					}
				}
			} else {
				sewedWing.addAll(rigthSideWing);
			}
			lofts.addAll(sewedWing);
			
			// Make a solid from the sewed halves
			boolean exportSolid = true;
			if(exportSolid) {
				System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Building the solid");
				CADSolid solidWing = null;
				BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
				sewedWing.forEach(s -> {
					TopoDS_Shape tds_shape_0 = s.getShape();
					TopExp_Explorer exp_0 = new TopExp_Explorer(tds_shape_0, TopAbs_ShapeEnum.TopAbs_SHELL);
					solidMaker.Add(TopoDS.ToShell(exp_0.Current()));					
				});
				solidMaker.Build();
				System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
				if(solidMaker.IsDone() == 1) {
					solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
					solids.add((OCCShape) solidWing);					
					System.out.println(OCCUtils.reportOnShape(((OCCShape) solidWing).getShape(), "LS solid")); 
				}
			}			
		}
						
		// exporting lofts
		if(exportLofts) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding loft surfaces");
			result.addAll(lofts);
		}
		
		// exporting solids
		if(exportSolids) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding solids");
			result.addAll(solids);
		}
		
		// exporting extra shapes
		if(exportSupportShapes) {
			System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] adding support cad entities");
			result.addAll(extraShapes);
		}				
		return result;
	}
	
	public static void getAircraftSolidFile(
			List<OCCShape> allShapes,
			String fileName,
			String fileExtension
			) {
		
		// filter the shapes in order to obtain just the solids
		List<TopoDS_Shape> tdsSolids = new ArrayList<>();
		System.out.println("========== [AircraftUtils::getAircraftSolidFile] Searching for solids");
		allShapes.forEach(s -> {
			TopoDS_Shape tdsShape = s.getShape();
			TopExp_Explorer exp = new TopExp_Explorer(tdsShape, TopAbs_ShapeEnum.TopAbs_SOLID);
			while(exp.More() > 0) {
				tdsSolids.add(exp.Current());
				exp.Next();
			}
		});
		System.out.println("Solids found: " + tdsSolids.size());
		
		// choosing the file extension
		switch(fileExtension) {
		
		case ".brep":
			System.out.println("========== [AircraftUtils::getAircraftSolidFile] .brep file extension selected");
			String fileNameBrep = fileName + fileExtension;
			
			BRep_Builder compoundBrep = new BRep_Builder();
			TopoDS_Compound solidsCompoundBrep = new TopoDS_Compound();
			compoundBrep.MakeCompound(solidsCompoundBrep);
			tdsSolids.forEach(s -> compoundBrep.Add(solidsCompoundBrep, s));
			
			System.out.println(".brep file writing ...");
			long result = BRepTools.Write(solidsCompoundBrep, fileNameBrep);
			System.out.println("========== [AircraftUtils::getAircraftSolidFile] file correctly written? " + (result == 1)); 
			
			break;
			
		case ".step":
			System.out.println("========== [AircraftUtils::getAircraftSolidFile] .step file extension selected");
			String fileNameStep = fileName + fileExtension;
			
			STEPControl_Writer stepWriter = new STEPControl_Writer();
			tdsSolids.forEach(s -> stepWriter.Transfer(s, STEPControl_StepModelType.STEPControl_AsIs));
			
			System.out.println(".step file writing ...");
			IFSelect_ReturnStatus statusStep = stepWriter.Write(fileNameStep);
			System.out.println("========== [AircraftUtils::getAircraftSolidFile] file status: " + statusStep);
			
			break;
			
		case ".stl":
			System.out.println("========== [AircraftUtils::getAircraftSolidFile] .stl file extension selected");
			String fileNameStl = fileName + fileExtension;
			
			BRep_Builder compoundStl = new BRep_Builder();
			TopoDS_Compound solidsCompoundStl = new TopoDS_Compound();
			compoundStl.MakeCompound(solidsCompoundStl);
			
			BRepMesh_IncrementalMesh solidMesh = new BRepMesh_IncrementalMesh();
			StlAPI_Writer stlWriter = new StlAPI_Writer();		
			
			// meshing each solid separately			
//			tdsSolids.forEach(s -> {
//				solidMesh.SetShape(s);
//				solidMesh.Perform();
//				TopoDS_Shape tdsSolidMeshed = solidMesh.Shape();
//				tdsSolidMeshed.Reverse();
//				compoundBuilder.Add(solidsCompoundStl, tdsSolidMeshed);		
//			});
			
			// meshing all the solids at the same time
			System.out.println("creating the mesh ...");
			tdsSolids.forEach(s -> compoundStl.Add(solidsCompoundStl, s));
			solidMesh.SetShape(solidsCompoundStl);
			solidMesh.Perform();
			TopoDS_Shape tdsSolidMeshed = solidMesh.Shape();
			tdsSolidMeshed.Reverse();
			
			System.out.println(".step file writing ...");
			//stlWriter.Write(solidsCompoundStl, fileNameSTL);
			stlWriter.Write(tdsSolidMeshed, fileNameStl);
			System.out.println("========== [AircraftUtils::getAircraftSolidFile] file done");
			
			break;

		default:
			break;
		}
	}
	
	private static List<double[]> populateCoordinateList(
			double yStation,
			AirfoilCreator theCreator,
			LiftingSurface theLiftingSurface
			) {
		
		List<double[]> actualAirfoilCoordinates = new ArrayList<>();
		
		int nPoints = theCreator.getXCoords().length;
		double[] xCoords = MyArrayUtils.convertToDoublePrimitive(theCreator.getXCoords());
		double[] zCoords = MyArrayUtils.convertToDoublePrimitive(theCreator.getZCoords());
		
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
		
		// checking the trailing edge
		if(Math.abs(zCoords[0] - zCoords[nPoints - 1]) < 1e-5) {
			zCoords[0] += 1*1e-3;
			zCoords[nPoints - 1] -= 1*1e-3; // IRON canard works better with 1 instead of .5
		}

		for (int i = 0; i < nPoints; i++) {

			// Scale to actual dimensions
			x = xCoords[i]*c;
			y = 0.0;
			z = zCoords[i]*c;

//			// Rotation due to twist
//			if(!theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
//				double r = Math.sqrt(x*x + z*z);
//				x = (x - r*(1-Math.cos(-twist - theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN))));
//				z = (z + r*Math.sin(-twist - theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));				
//			}
			
			// Rotation due to twist
			if(!theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				double r = Math.sqrt(x*x + z*z);
				x = x - r*(1-Math.cos(twist + theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));
				z = z - r*Math.sin(twist + theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN));				
			}

			// Actual location
			x = x + xLE + theLiftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
			y = yStation;
			z = z + theLiftingSurface.getZApexConstructionAxes().doubleValue(SI.METER)
				  + (yStation
//						* Math.tan(theLiftingSurface.getLiftingSurfaceCreator().getDihedralAtYActual(yStation).doubleValue(SI.RADIAN)));
                        * Math.tan(AircraftUtils.getDihedralAtYActual(theLiftingSurface, yStation).doubleValue(SI.RADIAN)));

			if(theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				actualAirfoilCoordinates.add(
						new double[] {
								x,
								-zCoords[i]*c,
								(yStation + theLiftingSurface.getZApexConstructionAxes().doubleValue(SI.METER))
						});
			} else {
				actualAirfoilCoordinates.add(new double[] {x, y, z});
			}
		}			
		return actualAirfoilCoordinates;
	}
	
	private static CADGeomCurve3D[] createVerCrvsForTipClosure(
			LiftingSurface theLiftingSurface, 
			List<OCCEdge> tipAirfoil,
			List<OCCEdge> preTipAirfoil,
			PVector[] leVec,
			PVector[] teVec,
			double chordFrac,
			double[] guideCrvPnt
			) {
		
		CADGeomCurve3D[] verSecCrvsList = new CADGeomCurve3D[2];
		
		int iTip = theLiftingSurface.getAirfoilList().size() - 1;
		float tipChordLength = (float) theLiftingSurface
				.getChordTip().doubleValue(SI.METER);
		float preTipChordLength = (float) theLiftingSurface
				.getLiftingSurfaceCreator().getChordsBreakPoints().get(iTip-1).doubleValue(SI.METER);
		
		PVector le1 = leVec[0];
		PVector le2 = leVec[1];
		PVector te1 = teVec[0];
		PVector te2 = teVec[1];
		
		// creating vertical splitting vectors for the tip airfoil curve, orthogonal to the chord
		PVector leVector = PVector.sub(le2, le1);
		PVector axisVector; 
		PVector chordTipVector = PVector.sub(te2, le2); // vector in the airfoil plane	
		PVector chordTipNVector = new PVector();
		PVector constrCrvApexTan = new PVector();
		PVector.cross(chordTipVector, leVector, constrCrvApexTan).normalize();
//		PVector.cross(chordTipVector, leVector, chordTipNVector).normalize(); // vector in the airfoil plane, normal to the chord, normalized
		if(!theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL))
			axisVector = new PVector(0, 1, 0);
		else
			axisVector = new PVector(0, 0, 1);
		PVector.cross(chordTipVector, axisVector, chordTipNVector).normalize(); // vector in the airfoil plane, normal to the chord, normalized
		
		// creating vertical splitting vectors for the second to last airfoil curve
		PVector chordPreTipVector = PVector.sub(te1, le1); 	
		PVector chordPreTipNVector = new PVector();
//		PVector.cross(chordPreTipVector, leVector, chordPreTipNVector).normalize(); 
		PVector.cross(chordPreTipVector, axisVector, chordPreTipNVector).normalize();
		
		// getting points onto the tip airfoil
		PVector pntOnTipChord = PVector.lerp(le2, te2, (float) chordFrac); // tip chord fraction point

		Double[] tipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
				theLiftingSurface.getAirfoilList().get(iTip).getAirfoilCreator(), 
				chordFrac
				);

		PVector pntOnTipAirfoilUCrv = PVector.add(
				pntOnTipChord, 
				PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[0].floatValue())*tipChordLength)
				);			
		PVector pntOnTipAirfoilLCrv = PVector.add(
				pntOnTipChord, 
				PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[1].floatValue())*tipChordLength)
				);

		double[] tipAirfoilUppVtx = OCCUtils.pointProjectionOnCurve(
				OCCUtils.theFactory.newCurve3D(tipAirfoil.get(0)), 
				new double[] {pntOnTipAirfoilUCrv.x, pntOnTipAirfoilUCrv.y, pntOnTipAirfoilUCrv.z}
				).pnt();			
		double[] tipAirfoilLowVtx = OCCUtils.pointProjectionOnCurve(
				OCCUtils.theFactory.newCurve3D(tipAirfoil.get(1)),
				new double[] {pntOnTipAirfoilLCrv.x, pntOnTipAirfoilLCrv.y, pntOnTipAirfoilLCrv.z}
				).pnt();

		// tangent vectors calculation
		PVector pntOnPreTipChord = PVector.lerp(le1, te1, (float) chordFrac);

		Double[] preTipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
				theLiftingSurface.getAirfoilList().get(iTip-1).getAirfoilCreator(), 
				chordFrac
				);

		PVector pntOnPreTipAirfoilUCrv = PVector.add(
				pntOnPreTipChord, 
				PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[0].floatValue())*preTipChordLength)
				);			
		PVector pntOnPreTipAirfoilLCrv = PVector.add(
				pntOnPreTipChord, 
				PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[1].floatValue())*preTipChordLength)
				);

		double[] preTipAirfoilUppVtx = OCCUtils.pointProjectionOnCurve(
				(OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(preTipAirfoil.get(0)),
				new double[] {pntOnPreTipAirfoilUCrv.x, pntOnPreTipAirfoilUCrv.y, pntOnPreTipAirfoilUCrv.z}
				).pnt();			
		double[] preTipAirfoilLowVtx = OCCUtils.pointProjectionOnCurve(
				(OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(preTipAirfoil.get(1)),
				new double[] {pntOnPreTipAirfoilLCrv.x, pntOnPreTipAirfoilLCrv.y, pntOnPreTipAirfoilLCrv.z}
				).pnt();

		PVector[] tanVecs = new PVector[2];
		tanVecs[0] = PVector.sub(
				new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2]),
				new PVector((float) preTipAirfoilUppVtx[0], (float) preTipAirfoilUppVtx[1], (float) preTipAirfoilUppVtx[2])
				).normalize();
		tanVecs[1] = PVector.sub(
				new PVector((float) tipAirfoilLowVtx[0], (float) tipAirfoilLowVtx[1], (float) tipAirfoilLowVtx[2]),
				new PVector((float) preTipAirfoilLowVtx[0], (float) preTipAirfoilLowVtx[1], (float) preTipAirfoilLowVtx[2])
				).normalize();
		
		// weights for the tangent constraints
		double thickUpp = PVector.sub(pntOnTipAirfoilUCrv, pntOnTipChord).mag();
		double thickLow = PVector.sub(pntOnTipAirfoilLCrv, pntOnTipChord).mag();
		double crvHeight = PVector.sub(
				new PVector(
						(float) guideCrvPnt[0], 
						(float) guideCrvPnt[1], 
						(float) guideCrvPnt[2]), 
				pntOnTipChord).mag(); 
		
		double tanUppVSecCrvFac = 1;
		double tanLowVSecCrvFac = 1*(-1);		
		double tanUppHalfVSecCrvFac = Math.pow(thickUpp/crvHeight, 0.60)*(-1); //TODO: eventually make this a parameter
		double tanLowHalfVSecCrvFac = Math.pow(thickLow/crvHeight, 0.60)*(-1);
		
		// vertical section curves creation
		List<double[]> verSecCrvUppPnts = new ArrayList<>();
		List<double[]> verSecCrvLowPnts = new ArrayList<>();
		
		verSecCrvUppPnts.add(tipAirfoilUppVtx);
		verSecCrvUppPnts.add(guideCrvPnt);
		verSecCrvLowPnts.add(guideCrvPnt);
		verSecCrvLowPnts.add(tipAirfoilLowVtx);
		
		double[] tanUppVSecCrv = MyArrayUtils.scaleArray(
				new double[] {tanVecs[0].x, tanVecs[0].y, tanVecs[0].z}, 
				tanUppVSecCrvFac
				);	
		double[] tanUppHalfVSecCrv = MyArrayUtils.scaleArray(
				new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
				tanUppHalfVSecCrvFac
				);
		double[] tanLowHalfVSecCrv = MyArrayUtils.scaleArray(
				new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
				tanLowHalfVSecCrvFac
				);
		double[] tanLowVSecCrv = MyArrayUtils.scaleArray(
				new double[] {tanVecs[1].x, tanVecs[1].y, tanVecs[1].z},
				tanLowVSecCrvFac
				);
		
		CADGeomCurve3D verSecCrvUpp = OCCUtils.theFactory.newCurve3D(
				verSecCrvUppPnts, 
				false, 
				tanUppVSecCrv, 
				tanUppHalfVSecCrv, 
				false
				);
		CADGeomCurve3D verSecCrvLow = OCCUtils.theFactory.newCurve3D(
				verSecCrvLowPnts, 
				false, 
				tanLowHalfVSecCrv, 
				tanLowVSecCrv, 
				false
				);
		
		verSecCrvsList[0] = verSecCrvUpp;
		verSecCrvsList[1] = verSecCrvLow;		
		
		return verSecCrvsList;
	}
	
	private static CADGeomCurve3D[] createVerCrvsForTipClosure( 
			List<OCCEdge> tipAirfoil,
			List<OCCEdge> preTipAirfoil,
			PVector[] leVec,
			PVector[] teVec,
			double[] guideCrvPnt
			) {		
		
		CADGeomCurve3D[] verSecCrvsList = new CADGeomCurve3D[2];
		
		PVector le1 = leVec[0];
		PVector le2 = leVec[1];
		PVector te1 = teVec[0];
		PVector te2 = teVec[1];
		
		// getting points on the tip airfoil	
		double[] tipAirfoilUppVtx = tipAirfoil.get(0).vertices()[0].pnt();			
		double[] tipAirfoilLowVtx = tipAirfoil.get(1).vertices()[1].pnt();
		
		// tangent vectors calculation
		double[] preTipAirfoilUppVtx = preTipAirfoil.get(0).vertices()[0].pnt();				
		double[] preTipAirfoilLowVtx = preTipAirfoil.get(1).vertices()[1].pnt();
		
		PVector[] tanVecs = new PVector[2];
		tanVecs[0] = PVector.sub(
				new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2]),
				new PVector((float) preTipAirfoilUppVtx[0], (float) preTipAirfoilUppVtx[1], (float) preTipAirfoilUppVtx[2])
				).normalize();
		tanVecs[1] = PVector.sub(
				new PVector((float) tipAirfoilLowVtx[0], (float) tipAirfoilLowVtx[1], (float) tipAirfoilLowVtx[2]),
				new PVector((float) preTipAirfoilLowVtx[0], (float) preTipAirfoilLowVtx[1], (float) preTipAirfoilLowVtx[2])
				).normalize();
		
		// weights for the tangent constraints
		PVector thickness = PVector.sub(
				new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2]),
				new PVector((float) tipAirfoilLowVtx[0], (float) tipAirfoilLowVtx[1], (float) tipAirfoilLowVtx[2])
				);
		
		double thickUpp = thickness.mag()/2;
		double thickLow = thickUpp;
		double crvHeight = PVector.sub(
				new PVector((float) guideCrvPnt[0], (float) guideCrvPnt[1], (float) guideCrvPnt[2]), 
				new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2])
				).mag();

		double tanUppVSecCrvFac = 1;
		double tanLowVSecCrvFac = 1*(-1);		
		double tanUppHalfVSecCrvFac = Math.pow(thickUpp/crvHeight, 0.60)*(-1); //TODO: eventually make this a parameter
		double tanLowHalfVSecCrvFac = Math.pow(thickLow/crvHeight, 0.60)*(-1);
		
		// section curves apex tangent vector
		PVector chordTipVector = PVector.sub(te2, le2);
		PVector leVector = PVector.sub(le2, le1);
		PVector constrCrvApexTan = new PVector();
		PVector.cross(chordTipVector, leVector, constrCrvApexTan).normalize();
		
		// vertical section curves creation
		List<double[]> verSecCrvUppPnts = new ArrayList<>();
		List<double[]> verSecCrvLowPnts = new ArrayList<>();
		
		thickness.normalize();

		verSecCrvUppPnts.add(tipAirfoilUppVtx);
		verSecCrvUppPnts.add(guideCrvPnt);
		verSecCrvLowPnts.add(guideCrvPnt);
		verSecCrvLowPnts.add(tipAirfoilLowVtx);

		double[] tanUppVSecCrv = MyArrayUtils.scaleArray(
				new double[] {tanVecs[0].x, tanVecs[0].y, tanVecs[0].z}, 
				tanUppVSecCrvFac
				);	
		double[] tanUppHalfVSecCrv = MyArrayUtils.scaleArray(
				new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
				tanUppHalfVSecCrvFac
				);
		double[] tanLowHalfVSecCrv = MyArrayUtils.scaleArray(
				new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
				tanLowHalfVSecCrvFac
				);
		double[] tanLowVSecCrv = MyArrayUtils.scaleArray(
				new double[] {tanVecs[1].x, tanVecs[1].y, tanVecs[1].z},
				tanLowVSecCrvFac
				);

		CADGeomCurve3D verSecCrvUpp = OCCUtils.theFactory.newCurve3D(
				verSecCrvUppPnts, 
				false, 
				tanUppVSecCrv, 
				tanUppHalfVSecCrv, 
				false
				);
		CADGeomCurve3D verSecCrvLow = OCCUtils.theFactory.newCurve3D(
				verSecCrvLowPnts, 
				false, 
				tanLowHalfVSecCrv, 
				tanLowVSecCrv, 
				false
				);

		verSecCrvsList[0] = verSecCrvUpp;
		verSecCrvsList[1] = verSecCrvLow;

		return verSecCrvsList;
	}
	
	private static Double[] getThicknessAtX(AirfoilCreator airfoil, Double xChord) {
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
		
		if(Math.abs(x[iXMin + 1] - x[iXMin]) > 1e-5) { // making lower list start from the last point of the upper one, if necessary
			xLowerPnts.add(x[iXMin]);
			zLowerPnts.add(z[iXMin]);
		}
		
		IntStream.range(iXMin + 1, nPnts).forEach(i -> {
			xLowerPnts.add(x[i]);
			zLowerPnts.add(z[i]);
			});
		
		double[] xUpperArray = MyArrayUtils.convertToDoublePrimitive(xUpperPnts);
		double[] zUpperArray = MyArrayUtils.convertToDoublePrimitive(zUpperPnts);
		
		for (int i = 0; i < xUpperArray.length/2; i++) {  // reverting the upper lists in order to pass 
			double tempX = xUpperArray[i];                // them to the spline interpolation method
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
		thickness[0] = thU;
		thickness[1] = thL;
		
		return thickness;
	}
	
	private static Amount<Angle> getDihedralAtYActual(LiftingSurface theLiftingSurface, Double yStation) {
		if (yStation >= 0) return getDihedralSemispanAtYActual(theLiftingSurface, yStation);
		else return getDihedralSemispanAtYActual(theLiftingSurface, -yStation);
	}
	
	private static Amount<Angle> getDihedralSemispanAtYActual(LiftingSurface theLiftingSurface, Double yStation) {
		Amount<Angle> dihedralAtY = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		if(yStation < theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(0).getEstimatedValue()) {
			System.err.println("INVALID Y STATION");
			dihedralAtY = null;
		}
		for(int i=1; i<theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().size(); i++) {
			if(yStation <= theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(i).getEstimatedValue() && 
			   yStation >= theLiftingSurface.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).getEstimatedValue()
					)
				dihedralAtY = theLiftingSurface.getLiftingSurfaceCreator().getPanels().get(i-1).getDihedral();
		}
		return dihedralAtY;
	}
}
