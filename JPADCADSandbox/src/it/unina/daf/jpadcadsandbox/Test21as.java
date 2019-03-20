package it.unina.daf.jpadcadsandbox;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.FlappedWing;
import it.unina.daf.jpadcad.utils.FlappedWing.SolidType;

public class Test21as {
	
	public static void main(String[] args) {
		Instant before = Instant.now();

		System.out.println("Starting JPADCADSandbox Test21as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");

		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface liftingSurface = aircraft.getWing();
		Boolean exportWing = true;
		Boolean exportFlap = true;
		Boolean exportSlat = true;
//		Boolean exportCleanWing = true;
		
		Map<SolidType, List<OCCShape>> solidsMap = FlappedWing.getFlappedWingCAD(liftingSurface, exportWing, exportFlap, exportSlat);
		
		// ----------------------------------------------------------
		// Rotate Slats
		// ----------------------------------------------------------	

		List<OCCShape> slatsList = solidsMap.get(SolidType.SLAT);
		OCCShape slat = slatsList.get(0);

		double slatDeflection = -0.4;
		double innerHingePntxSlat = 0.25;
		double innerHingePntzSlat = -0.15;
		double outerHingePntxSlat = 0.25;
		double outerHingePntzSlat = -0.15;

		OCCShape rotatedSlat = FlappedWing.symFlapRotation(liftingSurface, slatDeflection, slat, innerHingePntxSlat, innerHingePntzSlat,
				outerHingePntxSlat, outerHingePntzSlat);

		// ----------------------------------------------------------
		// Rotate Aileron
		// ----------------------------------------------------------	

		List<OCCShape> flapList	 = solidsMap.get(SolidType.FLAP);
		OCCShape aileron = flapList.get(flapList.size() - 1);
		OCCShape innerFlap = flapList.get(0);
		OCCShape outerFlap = flapList.get(1);

		double aileronDeflection = -0.4;
		double innerHingePntxAileron = 0.75;
		double innerHingePntzAileron = -0.015;
		double outerHingePntxAileron = 0.75;
		double outerHingePntzAileron = -0.015;

		OCCShape rotatedAileron = FlappedWing.symFlapRotation(liftingSurface, aileronDeflection, aileron, innerHingePntxAileron, innerHingePntzAileron,
				outerHingePntxAileron, outerHingePntzAileron);

		// ----------------------------------------------------------
		// Rotate Flaps
		// ----------------------------------------------------------	
		List<double[]> nonSymFlapChordRatios = new ArrayList<>();
		nonSymFlapChordRatios.add(new double[] {
				0.30,
				0.30
		});
		nonSymFlapChordRatios.add(new double[] {
				0.30,
				0.30
		});
		
		double innerFlapDeflection = 0.575;
		double innerHingePntxInnerFlap = 0.7115;
		double innerHingePntzInnerFlap = -0.0761;

		double outerHingePntxInnerFlap = 0.7115;
		double outerHingePntzInnerFlap = -0.0761;

		OCCShape rotatedInnerFlap = FlappedWing.nonSymRotation(liftingSurface, innerFlapDeflection, solidsMap.get(SolidType.WING).get(0),
				innerFlap, nonSymFlapChordRatios.get(0)[0], nonSymFlapChordRatios.get(0)[1], innerHingePntxInnerFlap, innerHingePntzInnerFlap,
				outerHingePntxInnerFlap, outerHingePntzInnerFlap);
				
		double outerFlapDeflection = 0.575;
		double innerHingePntxOuterFlap = 0.7115;
		double innerHingePntzOuterFlap = -0.0761;
		double outerHingePntxOuterFlap =0.7115;
		double outerHingePntzOuterFlap = -0.0761;

		OCCShape rotatedOuterFlap = FlappedWing.nonSymRotation(liftingSurface, outerFlapDeflection, solidsMap.get(SolidType.WING).get(0),
				outerFlap, nonSymFlapChordRatios.get(1)[0], nonSymFlapChordRatios.get(1)[1], innerHingePntxOuterFlap, innerHingePntzOuterFlap,
				outerHingePntxOuterFlap, outerHingePntzOuterFlap);

		// ----------------------------------------------------------
		// Check aileron interference
		// ----------------------------------------------------------	
		double minDeflection = -0.523;
		double maxDeflection = 0.523;
		int numAng = 5;
		
		FlappedWing.checkInterferenceSym(liftingSurface, minDeflection, maxDeflection, 
				solidsMap.get(SolidType.WING).get(0), aileron, numAng);

		
		List<OCCShape> exportShapes = new ArrayList<>();
		exportShapes.addAll(solidsMap.get(SolidType.WING));
//		exportShapes.addAll(solidsMap.get(SolidType.FLAP));
//		exportShapes.addAll(solidsMap.get(SolidType.SLAT));
		exportShapes.add(rotatedInnerFlap);
		exportShapes.add(rotatedOuterFlap);
		exportShapes.add(rotatedAileron);
		exportShapes.add(rotatedSlat);
	
		String fileName = "Test21as";
		System.out.println("========== [main] Output written on file: " + fileName);
		OCCUtils.write(fileName, FileExtension.STEP, exportShapes);

		Instant after = Instant.now();
		long delta = Duration.between(before, after).toMillis(); 
		System.out.println("Elapsed time : "  + delta );
		
	}

}
