package aircraft.components;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import database.databasefunctions.FuelFractionDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;

/** 
 * The fuel tank is supposed to be make up of a series of prismoids from the root station to the 85% of the
 * wing semispan. The separation of each prismoid from the other is make by the kink airfoil stations (more
 * than one on a multi-panel wing). 
 * 
 * Each prismoid is defined from the the inner spanwise section, the outer spanwise section and the 
 * distance between the two airfoil stations. Furthermore, each section of the prismoid is defined by:
 * 
 *    - the airfoil thickness related to the main spar x station
 *    - the airfoil thickness related to the second spar x station
 *    - the distance between the two spar stations. 
 * 
 * The spar stations can be set for a default aircraft (for example 25% - 55%) 
 * or can be read from the wing file.
 * 
 * The fuel tank is supposed to be contained in the wing; 
 * the class defines only half of the fuel tank (the whole tank is symmetric with respect
 * to xz plane).
 *  
 * @author Vittorio Trifari
 * 
 */
public class FuelTank implements IFuelTank {

	private String _id;
	
	private LiftingSurface _theWing;
	
	private Amount<Length> _xApexConstructionAxes;
	private Amount<Length> _yApexConstructionAxes;
	private Amount<Length> _zApexConstructionAxes;
	
	private FuelFractionDatabaseReader fuelFractionDatabase;
	
	private Amount<Mass> _massEstimated;

	private Double _mainSparNormalizedStation;
	private Double _secondarySparNormalizedStation;
	List<Amount<Length>> _thicknessAtMainSpar;
	List<Amount<Length>> _thicknessAtSecondarySpar;
	List<Amount<Length>> _distanceBetweenSpars;
	List<Amount<Length>> _prismoidsLength;
	List<Amount<Area>> _prismoidsSectionsAreas;
	List<Amount<Volume>> _prismoidsVolumes;
	List<Amount<Length>> _fuelTankStations;
	List<Amount<Length>> _wingChordsAtFuelTankStations;
	
	private Amount<Length> _xCG;
	private Amount<Length> _yCG;
	private Amount<Length> _zCG;
	
	// Jet A1 fuel density : the user can set this parameter when necessary
	private Amount<VolumetricDensity> _fuelDensity = Amount.valueOf(804.0, MyUnits.KILOGRAM_PER_CUBIC_METER);
	private Amount<Volume> _fuelVolume = Amount.valueOf(0.0, SI.CUBIC_METRE);
	private Amount<Mass> _fuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
	private Amount<Force> _fuelWeight = Amount.valueOf(0.0, SI.NEWTON);

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class FuelTankBuilder {
	
		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		private Double __mainSparNormalizedStation;
		private Double __secondarySparNormalizedStation;
		private LiftingSurface __theWing;
		
		List<Amount<Length>> __thicknessAtMainSpar = new ArrayList<Amount<Length>>();
		List<Amount<Length>> __thicknessAtSecondarySpar = new ArrayList<Amount<Length>>();
		List<Amount<Length>> __distanceBetweenSpars = new ArrayList<Amount<Length>>();
		List<Amount<Length>> __prismoidsLength = new ArrayList<Amount<Length>>();
		List<Amount<Area>> __prismoidsSectionsAreas = new ArrayList<Amount<Area>>();
		List<Amount<Volume>> __prismoidsVolumes = new ArrayList<Amount<Volume>>();
		List<Amount<Length>> __fuelTankStations = new ArrayList<Amount<Length>>();
		List<Amount<Length>> __wingChordsAtFuelTankStations = new ArrayList<Amount<Length>>();
		
		public FuelTankBuilder (String id, LiftingSurface theWing) {
			this.__id = id;
			this.__theWing = theWing;
			this.__mainSparNormalizedStation = theWing.getLiftingSurfaceCreator().getMainSparNonDimensionalPosition();
			this.__secondarySparNormalizedStation = theWing.getLiftingSurfaceCreator().getSecondarySparNonDimensionalPosition();
		}
		
		public FuelTankBuilder mainSparPosition (Double xMainSpar) {
			this.__mainSparNormalizedStation = xMainSpar;
			return this;
		}
		
		public FuelTankBuilder secondarySparPosition (Double xSecondarySpar) {
			this.__secondarySparNormalizedStation = xSecondarySpar;
			return this;
		}
		
		public FuelTank build() {
			return new FuelTank (this);
		}
		
	}
	
	private FuelTank (FuelTankBuilder builder) {
	
		this._id = builder.__id;
		this._theWing = builder.__theWing;
		this._mainSparNormalizedStation = builder.__mainSparNormalizedStation;
		this._secondarySparNormalizedStation = builder.__secondarySparNormalizedStation;
		this._thicknessAtMainSpar = builder.__thicknessAtMainSpar;
		this._thicknessAtSecondarySpar = builder.__thicknessAtSecondarySpar;
		this._distanceBetweenSpars = builder.__distanceBetweenSpars;
		this._prismoidsLength = builder.__prismoidsLength;
		this._prismoidsSectionsAreas = builder.__prismoidsSectionsAreas;
		this._prismoidsVolumes = builder.__prismoidsVolumes;
		this._fuelTankStations = builder.__fuelTankStations;
		this._wingChordsAtFuelTankStations = builder.__wingChordsAtFuelTankStations;
		
		calculateGeometry(_theWing);
		calculateFuelMass();
		
	}
	
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	/************************************************************************************ 
	 * Estimates dimensions of the fuel tank.
	 * 
	 * The first section is at the root station while the other ones, except the last,
	 * are at the kink stations (may be more than one).
	 * 
	 * The last section is at 85% of the semispan so that it has to be defined separately.
	 * 
	 */
	private void estimateDimensions(LiftingSurface theWing) {

		for (int i=0; i<theWing.getAirfoilList().size()-1; i++) {
			this._thicknessAtMainSpar.add(
					Amount.valueOf(
							theWing.getAirfoilList().get(i)
								.getAirfoilCreator()
									.calculateThicknessRatioAtXNormalizedStation(
											_mainSparNormalizedStation,
											theWing.getAirfoilList().get(i).getAirfoilCreator().getThicknessToChordRatio()
											)
							* theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER),
							SI.METER)
					);
			this._thicknessAtSecondarySpar.add(
					Amount.valueOf(
							theWing.getAirfoilList().get(i)
								.getAirfoilCreator()
									.calculateThicknessRatioAtXNormalizedStation(
											_secondarySparNormalizedStation,
											theWing.getAirfoilList().get(i).getAirfoilCreator().getThicknessToChordRatio()
											)
							* theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER),
							SI.METER)
					);
			this._distanceBetweenSpars.add(
					Amount.valueOf(
							_secondarySparNormalizedStation*theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)
							-_mainSparNormalizedStation*theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER),
							SI.METER
							)
					);
			this._fuelTankStations.add(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i));
			this._wingChordsAtFuelTankStations.add(theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i));
		}
		for(int i=1; i<theWing.getLiftingSurfaceCreator().getYBreakPoints().size()-1; i++)
			this._prismoidsLength.add(
					theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i)
					.minus(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1))
					);
		
		AirfoilCreator airfoilAt85Percent = LiftingSurface.calculateAirfoilAtY(
				theWing,
				theWing.getSemiSpan().times(0.85).doubleValue(SI.METER)
				);
		Amount<Length> chordAt85Percent = Amount.valueOf(
				theWing.getChordAtYActual(
						theWing.getSemiSpan().times(0.85).doubleValue(SI.METER)
						),
				SI.METER
				);
		
		this._thicknessAtMainSpar.add(
				Amount.valueOf(
						airfoilAt85Percent
							.calculateThicknessRatioAtXNormalizedStation(
									_mainSparNormalizedStation,
									airfoilAt85Percent.getThicknessToChordRatio()
									)
						* chordAt85Percent.doubleValue(SI.METER),
						SI.METER)
				);
		this._thicknessAtSecondarySpar.add(
				Amount.valueOf(
						airfoilAt85Percent
							.calculateThicknessRatioAtXNormalizedStation(
									_secondarySparNormalizedStation,
									airfoilAt85Percent.getThicknessToChordRatio()
									)
						* chordAt85Percent.doubleValue(SI.METER),
						SI.METER)
				);
		this._distanceBetweenSpars.add(
				Amount.valueOf(
						(_secondarySparNormalizedStation*chordAt85Percent.doubleValue(SI.METER))
						-(_mainSparNormalizedStation*chordAt85Percent.doubleValue(SI.METER)),
						SI.METER
						)
				);
		this._fuelTankStations.add(theWing.getSemiSpan().times(0.85));
		
		this._wingChordsAtFuelTankStations.add(chordAt85Percent);
		
		this._prismoidsLength.add(
				theWing.getSemiSpan().times(0.85)
				.minus(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(
						theWing.getLiftingSurfaceCreator().getYBreakPoints().size()-2)
						)
				);
	}
	
	/***********************************************************************************
	 * Calculates areas of each prismoid section (spanwise) from base size
	 * 
	 * @param theAircraft
	 */
	private void calculateAreas() {

		/*
		 * Each section is a trapezoid, so that the area is given by:
		 * 
		 *  (thicknessAtMainSpar + thicknessAtSecondarySpar)*distanceBetweenSpars*0.5
		 *  
		 */
		int nSections = this._thicknessAtMainSpar.size();
		for(int i=0; i<nSections; i++)
			this._prismoidsSectionsAreas.add(
					Amount.valueOf(
							(this._thicknessAtMainSpar.get(i).plus(this._thicknessAtSecondarySpar.get(i)))
							.times(this._distanceBetweenSpars.get(i)).times(0.5).getEstimatedValue(),
							SI.SQUARE_METRE
							)
					);
	}
	
	/*********************************************************************************
	 * Calculates the fuel tank volume using the section areas. 
	 * Each prismoid has a volume given by:
	 * 
	 *  (prismoidLength/3)*
	 *  	((prismoidSectionAreas(inner)) + (prismoidSectionAreas(outer)) 
	 *  		+ sqrt((prismoidSectionAreas(inner)) * (prismoidSectionAreas(outer))))
	 *  
	 * The total volume is the double of the sum of all prismoid volumes 
	 */
	private void calculateVolume() {

		/*

		 */
		for(int i=0; i<this._prismoidsLength.size(); i++) 
			this._prismoidsVolumes.add(
					Amount.valueOf(
							this._prismoidsLength.get(i).divide(3)
							.times(
									this._prismoidsSectionsAreas.get(i).getEstimatedValue()
									+ this._prismoidsSectionsAreas.get(i+1).getEstimatedValue()
									+ Math.sqrt(
											this._prismoidsSectionsAreas.get(i)
											.times(this._prismoidsSectionsAreas.get(i+1))
											.getEstimatedValue()
											)
									).getEstimatedValue(),
							SI.CUBIC_METRE
							)
					);
		
		for(int i=0; i<this._prismoidsVolumes.size(); i++)
			this._fuelVolume = this._fuelVolume
										.plus(this._prismoidsVolumes.get(i));
		this._fuelVolume = this._fuelVolume.times(2);
		
	}
	
	@Override
	public void calculateGeometry(LiftingSurface theWing) {
		
		estimateDimensions(theWing);
		calculateAreas();
		calculateVolume();
	}
	
	@Override
	public void calculateFuelMass() {
		_fuelMass = Amount.valueOf(_fuelDensity.times(_fuelVolume).getEstimatedValue(), SI.KILOGRAM);
		_fuelWeight = _fuelMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
	}

	/**********************************************************************************
	 * The total X and Y coordinates of the center of gravity can be calculated for
	 * each prismoid starting from its lateral faces. These are trapezoids and their
	 * center of gravity can be calculated as:
	 * 
	 *  x_cg = ((thicknessMainSpar + (2*thicknessSecondarySpar))
	 *  			/(thicknessMainSpar + thicknessSecondarySpar))
	 *  				*(distanceBetweenSpars/3)
	 *  
	 * These coordinates are taken from the major base of each face.
	 * 
	 * N.B.: The lateral faces will be enumerated counter-clockwise 
	 * 	     starting from the root station 
	 * 
	 * Having these coordinates for each lateral face the total prismoid CG 
	 * coordinates (x,y) can be determined by the intersection of the segments built up
	 * using these coordinates.
	 * 
	 * Finally the total CG coordinates of each  semi-wing tank are calculated as
	 *    
	 *  X_cg = sum(prismoidsVolumes(i)*x_cg(i)
	 *  		/sum(prismoidsVolumes(i)
	 *   
	 *  Y_cg = sum(prismoidsVolumes(i)*y_cg(i)
	 *  		/sum(prismoidsVolumes(i)
	 *  
	 * The total tank Y_cg will be zero since its symmetry; while the X_cg will be the one
	 * calculated for each semi-wing tank
	 *  
	 *  @author Vittorio Trifari
	 */
	@Override
	public void calculateCG() {

		//-------------------------------------------------------------
		// Lateral faces xCG calculation (LFR = Local Reference Frame):
		List<Double[]> xCGLateralFacesLFRList = new ArrayList<Double[]>();
		
		for(int i=0; i<this._prismoidsLength.size(); i++) {
			
			Double[] xCGLateralFacesLFR = new Double[4];
			
			xCGLateralFacesLFR[0] = this._thicknessAtMainSpar.get(i)
										.plus(this._thicknessAtSecondarySpar.get(i).times(2))
											.divide(
												this._thicknessAtMainSpar.get(i)
													.plus(this._thicknessAtSecondarySpar.get(i))
													)
												.times(this._distanceBetweenSpars.get(i).divide(3))
													.getEstimatedValue();
			
			xCGLateralFacesLFR[1] = this._thicknessAtSecondarySpar.get(i)
										.plus(this._thicknessAtSecondarySpar.get(i+1).times(2))
											.divide(
												this._thicknessAtSecondarySpar.get(i)
													.plus(this._thicknessAtSecondarySpar.get(i+1))
													)
												.times(this._prismoidsLength.get(i).divide(3))
													.getEstimatedValue();
			
			xCGLateralFacesLFR[2] = this._thicknessAtMainSpar.get(i+1)
										.plus(this._thicknessAtSecondarySpar.get(i+1).times(2))
											.divide(
													this._thicknessAtMainSpar.get(i+1)
														.plus(this._thicknessAtSecondarySpar.get(i+1))
													)
												.times(this._distanceBetweenSpars.get(i+1).divide(3))
													.getEstimatedValue();
			
			xCGLateralFacesLFR[3] = this._thicknessAtMainSpar.get(i)
										.plus(this._thicknessAtMainSpar.get(i+1).times(2))
											.divide(
													this._thicknessAtMainSpar.get(i)
													.plus(this._thicknessAtMainSpar.get(i+1))
													)
												.times(this._prismoidsLength.get(i).divide(3))
													.getEstimatedValue();
			
			xCGLateralFacesLFRList.add(xCGLateralFacesLFR);
		}
		
		//-------------------------------------------------------------
		// Calculation of the Xcg coordinates of each prismoid in wing LRF.
		
		List<Amount<Length>> xCGPrismoidsList = new ArrayList<Amount<Length>>();
		
		for(int i=0; i<xCGLateralFacesLFRList.size(); i++) {
			
			double[] xCGSegmentOppositeFaceSpanwiseX = new double[2];
			double[] xCGSegmentOppositeFaceSpanwiseY = new double[2];
			
			double[] xCGSegmentOppositeFaceChordwiseX = new double[2];
			double[] xCGSegmentOppositeFaceChordwiseY = new double[2];
			
			xCGSegmentOppositeFaceSpanwiseX[0] = this._fuelTankStations.get(i).doubleValue(SI.METER);
			xCGSegmentOppositeFaceSpanwiseX[1] = this._fuelTankStations.get(i+1).doubleValue(SI.METER);
			xCGSegmentOppositeFaceSpanwiseY[0] = this._theWing
													.getLiftingSurfaceCreator()
														.getXLEAtYActual(
																this._fuelTankStations.get(i).doubleValue(SI.METER)
																)
														.plus(this._wingChordsAtFuelTankStations.get(i)
																.times(this._mainSparNormalizedStation)
																)
														.doubleValue(SI.METER) + xCGLateralFacesLFRList.get(i)[0];
			xCGSegmentOppositeFaceSpanwiseY[1] = this._theWing
													.getLiftingSurfaceCreator()
														.getXLEAtYActual(
																this._fuelTankStations.get(i+1).doubleValue(SI.METER)
																)
														.plus(this._wingChordsAtFuelTankStations.get(i+1)
																.times(this._mainSparNormalizedStation)
																)
														.doubleValue(SI.METER) + xCGLateralFacesLFRList.get(i)[2];

			xCGSegmentOppositeFaceChordwiseX[0] = this._theWing.getLiftingSurfaceCreator()
																	.getYBreakPoints().get(i)
																		.doubleValue(SI.METER)
												  + xCGLateralFacesLFRList.get(i)[3]; 
			xCGSegmentOppositeFaceChordwiseX[1] = this._theWing.getLiftingSurfaceCreator()
																	.getYBreakPoints().get(i)
																		.doubleValue(SI.METER)
												  + xCGLateralFacesLFRList.get(i)[1];
			
			xCGSegmentOppositeFaceChordwiseY[0] = this._theWing
													.getLiftingSurfaceCreator()
														.getXLEAtYActual(
																this._theWing.getLiftingSurfaceCreator()
																		.getYBreakPoints().get(i)
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[3]																
																).doubleValue(SI.METER)
														+ (this._theWing.getChordAtYActual(
																this._theWing.getLiftingSurfaceCreator()
																	.getYBreakPoints().get(i)
																		.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[3])
																* this._mainSparNormalizedStation
																);
			xCGSegmentOppositeFaceChordwiseY[1] = this._theWing
													.getLiftingSurfaceCreator()
														.getXLEAtYActual(
																this._theWing.getLiftingSurfaceCreator()
																		.getYBreakPoints().get(i)	
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[1]																
																).doubleValue(SI.METER)
														+ (this._theWing.getChordAtYActual(
																this._theWing.getLiftingSurfaceCreator()
																		.getYBreakPoints().get(i)
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[1])
																* this._mainSparNormalizedStation
																)
														+ ((this._theWing.getChordAtYActual(
																this._theWing.getLiftingSurfaceCreator()
																		.getYBreakPoints().get(i)
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[1])
																* this._secondarySparNormalizedStation)
															- (this._theWing.getChordAtYActual(
																	this._theWing.getLiftingSurfaceCreator()
																			.getYBreakPoints().get(i)
																				.doubleValue(SI.METER)
																	+ xCGLateralFacesLFRList.get(i)[1])
																	* this._mainSparNormalizedStation));

			// check if the chordwise X array is monotonic increasing
			if(xCGSegmentOppositeFaceChordwiseX[1] - xCGSegmentOppositeFaceChordwiseX[0] < 0.0001)
				xCGSegmentOppositeFaceChordwiseX[0] -= 0.0001; 
			
			// now that the segments coordinates are calculated, we have to intersect these latter.
			UnivariateFunction functionToIntersectSpanWise = MyMathUtils.interpolate1DLinear(
					xCGSegmentOppositeFaceSpanwiseX,
					xCGSegmentOppositeFaceSpanwiseY
					);
			UnivariateFunction functionToIntersectChordWise = MyMathUtils.interpolate1DLinear(
					xCGSegmentOppositeFaceChordwiseX,
					xCGSegmentOppositeFaceChordwiseY
					);
			
			double[] arrayToIntersectSpanWise = new double[50];
			double[] arrayToIntersectChordWise = new double[50];
			double[] xArrayFittedSpanwise = MyArrayUtils.linspace(
					xCGSegmentOppositeFaceSpanwiseX[0],
					xCGSegmentOppositeFaceSpanwiseX[xCGSegmentOppositeFaceSpanwiseX.length-1],
					50
					);
			double[] xArrayFittedChordwise = MyArrayUtils.linspace(
					xCGSegmentOppositeFaceChordwiseX[0],
					xCGSegmentOppositeFaceChordwiseX[xCGSegmentOppositeFaceChordwiseX.length-1],
					50
					);
			for(int j=0; j<arrayToIntersectSpanWise.length; j++) {
				arrayToIntersectSpanWise[j] = functionToIntersectSpanWise.value(xArrayFittedSpanwise[j]);
				arrayToIntersectChordWise[j] = functionToIntersectChordWise.value(xArrayFittedChordwise[j]);
			}
			
			double[] intersection = MyArrayUtils.intersectArraysSimple(arrayToIntersectSpanWise, arrayToIntersectChordWise);
			
			for(int j=0; j<intersection.length; j++)
				if(intersection[j] != 0.0)
					xCGPrismoidsList.add(
							Amount.valueOf(
									intersection[j],
									SI.METER
									)
							);
			
			
		}
		
		System.out.println("\n xCG list = " + xCGPrismoidsList);
		
		_xCG = Amount.valueOf(0.0, SI.METER);
		
		for(int i=0; i<this._prismoidsVolumes.size(); i++)
			_xCG = _xCG.plus(
					Amount.valueOf(
							this._prismoidsVolumes.get(i).getEstimatedValue()
							*xCGPrismoidsList.get(i).getEstimatedValue()
							, SI.METER
							)
					);
		_xCG = _xCG.divide(this._fuelVolume.divide(2).getEstimatedValue());
		_xCG = _xCG.plus(_theWing.getXApexConstructionAxes());
		
		_yCG = Amount.valueOf(0.0, SI.METER);
		_zCG = _theWing.getZCG();
		
	}

	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tFuel tank\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "'\n")
				.append("\t·····································\n")
				.append("\tMain spar position (% semispan): " + _mainSparNormalizedStation + "\n")
				.append("\tSecondary spar position (% semispan): " + _secondarySparNormalizedStation + "\n")
				.append("\t·····································\n")
				.append("\tAirfoils thickness at main spar stations: " + _thicknessAtMainSpar + "\n")
				.append("\tAirfoils thickness at secondary spar stations: " + _thicknessAtSecondarySpar + "\n")
				.append("\tSpar distance at each spanwise station: " + _distanceBetweenSpars + "\n")
				.append("\tPrismoids length: " + _prismoidsLength + "\n")
				.append("\t·····································\n")
				.append("\tPrismoids spanwise sections areas: " + _prismoidsSectionsAreas + "\n")
				.append("\tPrismoids volumes: " + _prismoidsVolumes + "\n")
				.append("\t·····································\n")
				.append("\tTotal tank volume: " + _fuelVolume + "\n")
				.append("\tFuel density: " + _fuelDensity + "\n")
				.append("\tTotal fuel mass: " + _fuelMass + "\n")
				.append("\tTotal fuel weight: " + _fuelWeight + "\n")
				.append("\t·····································\n")
				;
		
		return sb.toString();
		
	}
	
	@Override
	public FuelFractionDatabaseReader getFuelFractionDatabase() {
		return fuelFractionDatabase;
	}
	
	@Override
	public void setFuelFractionDatabase(FuelFractionDatabaseReader fuelFractionDatabase) {
		this.fuelFractionDatabase = fuelFractionDatabase;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public void setId(String _id) {
		this._id = _id;
	}

	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	@Override
	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	@Override
	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	@Override
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	@Override
	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	@Override
	public Double getMainSparNormalizedStation() {
		return _mainSparNormalizedStation;
	}

	@Override
	public void setMainSparNormalizedStation(Double _mainSparNormalizedStation) {
		this._mainSparNormalizedStation = _mainSparNormalizedStation;
	}

	@Override
	public Double getSecondarySparNormalizedStation() {
		return _secondarySparNormalizedStation;
	}

	@Override
	public void setSecondarySparNormalizedStation(Double _secondarySparNormalizedStation) {
		this._secondarySparNormalizedStation = _secondarySparNormalizedStation;
	}

	@Override
	public List<Amount<Length>> getDistanceBetweenSpars() {
		return _distanceBetweenSpars;
	}

	@Override
	public void setDistanceBetweenSpars(List<Amount<Length>> _distanceBetweenSpars) {
		this._distanceBetweenSpars = _distanceBetweenSpars;
	}

	@Override
	public List<Amount<Length>> getPrismoidsLength() {
		return _prismoidsLength;
	}

	@Override
	public void setPrismoidsLength(List<Amount<Length>> _prismoidsLength) {
		this._prismoidsLength = _prismoidsLength;
	}

	@Override
	public Amount<Length> getXCG() {
		return _xCG;
	}

	@Override
	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	@Override
	public Amount<Length> getYCG() {
		return _yCG;
	}

	@Override
	public void setYCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	@Override
	public Amount<Length> getZCG() {
		return _zCG;
	}

	@Override
	public void setZCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	@Override
	public Amount<VolumetricDensity> getFuelDensity() {
		return _fuelDensity;
	}

	@Override
	public void setFuelDensity(Amount<VolumetricDensity> _fuelDensity) {
		this._fuelDensity = _fuelDensity;
	}

	@Override
	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	@Override
	public List<Amount<Length>> getThicknessAtMainSpar() {
		return _thicknessAtMainSpar;
	}

	@Override
	public List<Amount<Length>> getThicknessAtSecondarySpar() {
		return _thicknessAtSecondarySpar;
	}

	@Override
	public List<Amount<Area>> getPrismoidsSectionsAreas() {
		return _prismoidsSectionsAreas;
	}

	@Override
	public List<Amount<Volume>> getPrismoidsVolumes() {
		return _prismoidsVolumes;
	}

	@Override
	public Amount<Volume> getFuelVolume() {
		return _fuelVolume;
	}

	@Override
	public Amount<Mass> getFuelMass() {
		return _fuelMass;
	}

	@Override
	public void setFuelMass(Amount<Mass> fuelMass) {
		this._fuelMass = fuelMass;
	}
	
	@Override
	public Amount<Force> getFuelWeight() {
		return _fuelWeight;
	}
}
