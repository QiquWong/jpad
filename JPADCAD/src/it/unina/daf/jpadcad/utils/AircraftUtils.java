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
import cad.occ.OCCShapeFactory;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import opencascade.Geom_Curve;
import opencascade.Geom_SurfaceOfLinearExtrusion;
import opencascade.gp_Dir;
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

	public static List<OCCShape> getFuselageCAD(Fuselage fuselage, boolean supportShapes) {
		if (fuselage == null)
			return null;
		if (OCCUtils.theFactory == null)
			return null;
			
		System.out.println("========== [AircraftUtils::getFuselageCAD] ");

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

		System.out.println("========== [AircraftUtils::getFuselageCAD] Patch 1: nose cap, from nose tip to x=" + noseCapStation);
		
		System.out.println("Getting selected sections ...");
		// all xbar's are normalized with noseLength
		List<Double> xbars1 = Arrays.asList(
//				new Double[] {
//						0.2*xbarNoseCap, 0.4*xbarNoseCap, 0.8*xbarNoseCap, 1.0*xbarNoseCap}
				MyArrayUtils
					// .linspaceDouble(
					.halfCosine2SpaceDouble(
					// .cosineSpaceDouble(
						0.2*xbarNoseCap, xbarNoseCap, 
						4) // n. points
				);
		
		
		System.out.println("Nose-cap trunk selected sections, Patch-1, normalized x-stations: " + xbars1.toString());

		List<List<PVector>> sections1 = new ArrayList<List<PVector>>();
		xbars1.stream()
			  .forEach(x -> sections1.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		System.out.println("Constructing the nose-cap patch, Patch-1");
		OCCShape patch1 = 
				OCCUtils.makePatchThruSections(
						new PVector(0.0f, 0.0f, (float) zNoseTip.doubleValue(SI.METER)), // Nose tip vertex
						sections1
				);
		
		System.out.println("========== [AircraftUtils::getFuselageCAD] Patch 2: x=" + noseCapStation + "to x=" + noseLength);
		
		System.out.println("Getting selected sections ...");

		// all xbar's are normalized with noseLength
		List<Double> xbars2 = Arrays.asList(
//				new Double[] {
//						xbarNoseCap, 0.2, 0.4, 0.6, 0.8, 1.0}
				MyArrayUtils
				// .linspaceDouble(
				// .halfCosine1SpaceDouble(
				.cosineSpaceDouble(
					xbarNoseCap, 1.0, 
					13) // n. points
				);

		System.out.println("Nose trunk selected sections, Patch-2, normalized x-stations: " + xbars2.toString());

		List<List<PVector>> sections2 = new ArrayList<List<PVector>>();
		xbars2.stream()
			  .forEach(x -> sections2.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		System.out.println("Constructing the nose patch, Patch-2");
		OCCShape patch2 = OCCUtils.makePatchThruSections(sections2);

		System.out.println("========== [AircraftUtils::getFuselageCAD] Construct the entire fuselage nose patch (Sewing Patch-1/Patch-2) - TODO");
				
		List<OCCShape> ret = new ArrayList<>();
		ret.add(patch1);
		ret.add(patch2);

		System.out.println("========== [AircraftUtils::getFuselageCAD] Construct fuselage cylindrical patch [>>> Experimental <<<]");
		
		// nose Patch-2 terminal section
		List<PVector> sectionNoseTerminal = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength);
		CADGeomCurve3D cadCrvNoseTerminalSection = OCCUtils.theFactory
				.newCurve3D(
					sectionNoseTerminal.stream()
						.map(p -> new double[]{p.x, p.y, p.z})
						.collect(Collectors.toList()),
					false);

		Amount<Length> cylinderLength = fuselage.getFuselageCreator().getLengthCylindricalTrunk();
		
		gp_Dir extrusionDir = new gp_Dir(1.0,0.0,0.0);
		OCCEdge edge = (OCCEdge) cadCrvNoseTerminalSection.edge();
//		Geom_SurfaceOfLinearExtrusion cylinder = new Geom_SurfaceOfLinearExtrusion( 
//				(Geom_Curve) (edge.getShape()),
//				extrusionDir);
//		System.out.println("------->>>> Extrusion null? " + cylinder.IsNull());
		
		// TODO: add new shape creation feature in OCCShell
//		OCCShape cylinderShape = OCCUtils.theFactory.newShape(cylinder. ???);
		
		if (supportShapes) {
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
							15) // n. points
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
			
			// x stations defining cap outlines
			List<Double> xxPatch2 = Arrays.asList(
					MyArrayUtils.halfCosine1SpaceDouble(
							noseCapStation.doubleValue(SI.METER), noseLength.doubleValue(SI.METER),
							15) // n. points
					);
			// points y's on nose outline curve, XY, right
			List<double[]> pointsNoseSideRight = xxPatch2.stream()
					.map(x -> new double[]{
							x,
							fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
							fuselage.getFuselageCreator().getCamberZAtX(x)
					})
					.collect(Collectors.toList());
			CADGeomCurve3D cadCrvNoseXYRight = OCCUtils.theFactory
					.newCurve3D(pointsNoseSideRight, false);
			
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
			
			ret.addAll(extraShapesCap);
		}
		
		return ret;
	}
}
