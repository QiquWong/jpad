package sandbox.vc;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.MomentCalc;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;

public class Test_VC_FusDes_01 {


	public static void main(String[] args) {

		// Assign all default folders
		MyConfiguration.initWorkingDirectoryTree();
		
		// Set database directory	
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String databaseFileName = "FusDes_database.h5";

		// Default operating conditions
		OperatingConditions theOperatingConditions = new OperatingConditions();

		theOperatingConditions.set_machCurrent(0.53);
		//theOperatingConditions.set_altitude(Amount.valueOf(2000, SI.METER));
		//theOperatingConditions.set_alphaCurrent(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));

		System.out.println("Operating condition");
		System.out.println("\tMach: " + theOperatingConditions.get_machCurrent());
		System.out.println("----------------------");


		Aircraft theAircraft = Aircraft.createDefaultAircraft();
		theAircraft.set_name("DefaultAircraft");

		System.out.println("\nAircraft");
		System.out.println("\tName of fuselage: " + theAircraft.get_fuselage().get_name());
		System.out.println("----------------------\n");

		// To create the aircraft it's necessary create an analysis and updating the geometry.
		ACAnalysisManager theAnalysis = new ACAnalysisManager(theOperatingConditions);
		theAnalysis.updateGeometry(theAircraft);

		double finenessRatio       = theAircraft.get_fuselage().get_lambda_F().doubleValue();
		double noseFinenessRatio   = theAircraft.get_fuselage().get_lambda_N().doubleValue();
		double tailFinenessRatio   = theAircraft.get_fuselage().get_lambda_T().doubleValue();
		double upsweepAngle 	   = theAircraft.get_fuselage().get_upsweepAngle().doubleValue(NonSI.DEGREE_ANGLE);
		double windshieldAngle     = theAircraft.get_fuselage().get_windshieldAngle().doubleValue(NonSI.DEGREE_ANGLE);
		double xPositionPole	   = 0.5;

		FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, databaseFileName);

		fusDesDatabaseReader.runAnalysis(noseFinenessRatio, windshieldAngle, finenessRatio, tailFinenessRatio, upsweepAngle, xPositionPole);

		double cDFlatPlate = AerodynamicCalc.calculateCfTurb(
								AerodynamicCalc.calculateReynolds(theOperatingConditions.get_altitude().doubleValue(SI.METER),
																	theOperatingConditions.get_machCurrent(),
																	theAircraft.get_fuselage().get_len_F().doubleValue(SI.METER)),
								theOperatingConditions.get_machCurrent());

		double fusSurfRatio = theAircraft.get_fuselage().get_area_C().doubleValue(SI.SQUARE_METRE)/
							  theAircraft.get_wing().get_surface().doubleValue(SI.SQUARE_METRE);

		double cDFuselage = DragCalc.dragFusDesCalc(
				fusDesDatabaseReader.getKn(),
				fusDesDatabaseReader.getKc(),
				fusDesDatabaseReader.getKt(),
				theAircraft.get_fuselage().get_sWet().doubleValue(SI.SQUARE_METRE),
				theAircraft.get_fuselage().get_sWetNose().doubleValue(SI.SQUARE_METRE),
				theAircraft.get_fuselage().get_sWetC().doubleValue(SI.SQUARE_METRE),
				theAircraft.get_fuselage().get_sWetTail().doubleValue(SI.SQUARE_METRE),
//				theAircraft.get_fuselage().calculateSfront(theAircraft.get_fuselage().get__diam_C().doubleValue(SI.METER)),
				theAircraft.get_fuselage().get_area_C().doubleValue(SI.SQUARE_METRE),
				cDFlatPlate) * fusSurfRatio;

		double cM0Fuselage = MomentCalc.calcCM0Fuselage(
				fusDesDatabaseReader.getCM0FR(),
				fusDesDatabaseReader.getdCMn(),
				fusDesDatabaseReader.getdCMt())* fusSurfRatio*theAircraft.get_fuselage().get__diam_C().doubleValue(SI.METER)/
														      theAircraft.get_wing().get_meanAerodChordActual().doubleValue(SI.METRE);

		double cMaFuselage = MomentCalc.calcCMAlphaFuselage(
				fusDesDatabaseReader.getCMaFR(),
				fusDesDatabaseReader.getdCMan(),
				fusDesDatabaseReader.getdCMat())* fusSurfRatio*theAircraft.get_fuselage().get__diam_C().doubleValue(SI.METER)/
															   theAircraft.get_wing().get_meanAerodChordActual().doubleValue(SI.METRE);

		double cNbFuselage = MomentCalc.calcCNBetaFuselage(
				fusDesDatabaseReader.getCNbFR(),
				fusDesDatabaseReader.getdCNbn(),
				fusDesDatabaseReader.getdCNbt())* fusSurfRatio * theAircraft.get_fuselage().get__diam_C().doubleValue(SI.METER)/
                											     theAircraft.get_wing().get_span().doubleValue(SI.METRE);


		// -------------------------------  Test ----------------------------------------------
		System.out.println("--------Geometry---------");
		System.out.println("diameter: " + theAircraft.get_fuselage().get__diam_C().doubleValue(SI.METER));
		System.out.println("length: " + theAircraft.get_fuselage().get_len_F().doubleValue(SI.METER));
		System.out.println("MAC : " + theAircraft.get_wing().get_meanAerodChordActual().doubleValue(SI.METRE));
		System.out.println("span : " + theAircraft.get_wing().get_span().doubleValue(SI.METRE));
		System.out.println("sFusWet: " + theAircraft.get_fuselage().get_sWet().doubleValue(SI.SQUARE_METRE));
		System.out.println("sFront: " + theAircraft.get_fuselage().get_area_C().doubleValue(SI.SQUARE_METRE));
		System.out.println("sWing: " + theAircraft.get_wing().get_surface().doubleValue(SI.SQUARE_METRE));
		System.out.println("--------Method parameters-----------");
		System.out.println("finenessRatio: " + finenessRatio);
		System.out.println("noseFinenessRatio: " + noseFinenessRatio);
		System.out.println("tailFinenessRatio: " + tailFinenessRatio);
		System.out.println("upsweepAngle: " + upsweepAngle);
		System.out.println("windshieldAngle: " + windshieldAngle);
		System.out.println("xPositionPole: " + xPositionPole);
		System.out.println("--------Aerodynamics-----------");
		System.out.println("Kn: " + fusDesDatabaseReader.getKn());
		System.out.println("Kc: " + fusDesDatabaseReader.getKc());
		System.out.println("Kt: " + fusDesDatabaseReader.getKt());
		System.out.println("CD0 fuselge: " + cDFuselage);		// 69 counts (75 cfd)
		System.out.println("CM0 FR: " + fusDesDatabaseReader.getCM0FR());
		System.out.println("CM0 FR Sw mac: " + fusDesDatabaseReader.getCM0FR()*fusSurfRatio*theAircraft.get_fuselage().get__diam_C().doubleValue(SI.METER)/
			      theAircraft.get_wing().get_meanAerodChordActual().doubleValue(SI.METRE));
		System.out.println("CMn: " + fusDesDatabaseReader.getdCMn());
		System.out.println("CMt: " + fusDesDatabaseReader.getdCMt());
		System.out.println("CM0 fuselge: " + cM0Fuselage);		// - 0.0361 (-0.2180)
		System.out.println("CMalfa fuselge: " + cMaFuselage); 	// 0.0222 (0.2243)
		System.out.println("CNb fuselge: " + cNbFuselage);    	// -.0022 (-.0021 cfd)
		System.out.println("-------------------");

		/* // Gotta create this object
				ACAnalysisManager theAnalysis = new ACAnalysisManager(theOperatingConditions);
				// Gotta call this method first
				theAnalysis.updateGeometry(theAircraft);
				theAnalysis.doAnalysis(theAircraft, 
						AnalysisTypeEnum.AERODYNAMIC, 
						AnalysisTypeEnum.BALANCE,
						AnalysisTypeEnum.WEIGHTS,
						AnalysisTypeEnum.PERFORMANCES,
						AnalysisTypeEnum.COSTS);
				// do pass these parameters in this sequence


				System.out.println("----------------------");

				// TODO: fix this ??
				JPADGlobalData.setTheCurrentAircraft(theAircraft);
				JPADGlobalData.setTheCurrentAnalysis(theAnalysis);
				JPADWriteUtils.buildXmlTree(theAircraft, theOperatingConditions);

				JPADDataWriter theWriter = new JPADDataWriter(
						theOperatingConditions, theAircraft, theAnalysis);

				String xmlFileFolderPath = 
						MyConfiguration.currentDirectoryString;		
				String xmlFilePath = xmlFileFolderPath + File.separator + "pippo.xml";

				System.out.println("Exporting to file: " + xmlFilePath);
				theWriter.exportToXMLfile(xmlFilePath);*/

	}// end-of-main

}// end-of-class
