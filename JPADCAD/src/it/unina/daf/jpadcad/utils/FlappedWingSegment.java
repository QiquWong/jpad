package it.unina.daf.jpadcad.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aircraft.components.liftingSurface.LiftingSurface;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADSolid;

/*
 * Usage
 * 
 * 
 
 	// Inner-flap wing segment
	FlappedWingSegment ws1 = new FlappedWingSegment(wing, 0.1, 0.4);
 	Map<SolidType, CADSolid> solidsMap = getSolids(true, FlapType.NON_SYMMETRIC, true);
 	CADSolid csWingCut = solidsMap.get()
	CADSolid csWingUncut = ws1.getSolidClean(SolidType.WING_CUT);
	CADSolid csFlap      = ws1.getSolidClean(SolidType.FLAP);
	CADSolid csSlat      = ws1.getSolidClean(SolidType.SLAT);
 
 	// Clean wing segment
	FlappedWingSegment ws2 = new FlappedWingSegment(wing, 0.4, 0.45);
	ws2.makeSolidClean();
	
 	// Inner-flap wing segment
	FlappedWingSegment ws3 = new FlappedWingSegment(wing, 0.45, 0.9);
 	Map<SolidType, CADSolid> solidsMap = getSolids(true, FlapType.NON_SYMMETRIC, true);
 	CADSolid csWingCut = solidsMap.get()
	CADSolid csWingUncut = ws1.getSolidClean(SolidType.WING_CUT);
	CADSolid csFlap      = ws1.getSolidClean(SolidType.FLAP);
	CADSolid csSlat      = ws1.getSolidClean(SolidType.SLAT);
 
	
	List<FlappedWingSegment> wingSegments = new ArrayList<>();
	wingSegments.add(w1);
	wingSegments.add(w2);
	wingSegments.add(w3);
	
	
 */


public class FlappedWingSegment {
	
	public enum FlapType {
		SYMMETRIC,
		NON_SYMMETRIC,
		FOWLER;
	}

	public enum SolidType {
		WING_CLEAN,
		WING_CUT,
		FLAP,
		SLAT;
	}
	
	LiftingSurface wing;
	
	Boolean isFlapped;
	Boolean isSlatted;
	double yInnerPct, yOuterPct;
	FlapType flapType;
	List<CADEdge> airfoilsClean = new ArrayList<>();
	List<CADEdge> airfoilsCut = new ArrayList<>();
	List<CADEdge> airfoilsFlap = new ArrayList<>();
	List<CADEdge> airfoilsSlat = new ArrayList<>();
	
	Map<SolidType, CADSolid> solidsMap = new HashMap<>();

	// Assume that flap and slat share the same pair (yInnerPct, yOuterPct)
	public FlappedWingSegment(LiftingSurface wing, double yInnerPct, double yOuterPct) {
		this.wing = wing;
		this.yInnerPct = yInnerPct;
		this.yOuterPct = yOuterPct;
		this.isFlapped = false;
		this.isSlatted = false;
		
		makeAirfoilsClean();
		
	}

	public FlappedWingSegment(double yInnerPct, double yOuterPct) {
		super();
		this.isFlapped = true;
		this.isSlatted = false;
		this.yInnerPct = yInnerPct;
		this.yOuterPct = yOuterPct;
		
		makeAirfoilsClean();
		
	}
	
	private void makeAirfoilsClean() {
		// TODO get the code from my tests
		
	}
	
	public Map<SolidType, CADSolid> getSolids(Boolean isFlapped, FlapType flapType, Boolean doMakeHorn, Boolean isSlatted) {
		this.isFlapped = isFlapped;
		this.isSlatted = isSlatted;
		this.flapType = flapType;
		
		if (this.isFlapped) {
			makeAirfoilsCutAndFlap(doMakeHorn);
		} 
		
		if (this.isSlatted) {
			makeAirfoilsSlat();
		} 
		
		// Populate lists: airfoilsCut and airfoilsFlap
		
		// TODO first set this.solid then return it
		return this.solidsMap;
		
	}

	public Map<SolidType, CADSolid> getSolids(){
		return this.solidsMap;
	}
	
	public void makeSolidClean() {
		// put in solidsMap the solid of a clean wing segment --> WING_CLEAN
		// solidsMap.put(SolidType.WING_CLEAN, value)
		// TODO
		
	}
	
	public CADSolid getCleanSolid() {
		return solidsMap.get(SolidType.WING_CLEAN);
	}

	private void makeAirfoilsCutAndFlap(Boolean doMakeHorn) {
		// Populate lists: airfoilsCut and airfoilsFlap
		
		switch (this.flapType) {
		case SYMMETRIC:
			// TODO
			break;
		case NON_SYMMETRIC:
			// TODO
			break;
		case FOWLER:
			// TODO
			break;
		default:
			break;
		}
		// TODO: make horn?

	}

	private void makeAirfoilsSlat() {
		// Populate list airfoilsSlat
		if (this.isFlapped) {
			// construct slat shape from the airfoilsCut list
			
			// TODO Auto-generated method stub
			
		} else {
			// construct slat shape from the airfoilsClean list
			
			// TODO Auto-generated method stub
			
		}
	}

	
}
