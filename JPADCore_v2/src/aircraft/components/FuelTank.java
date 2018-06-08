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

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import calculators.aerodynamics.AirfoilCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;

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
public class FuelTank {

	//--------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private String _id;
	private LiftingSurface _theWing;
	private Amount<Length> _xApexConstructionAxes;
	private Amount<Length> _yApexConstructionAxes;
	private Amount<Length> _zApexConstructionAxes;
	private List<Amount<Length>> _thicknessAtMainSpar;
	private List<Amount<Length>> _thicknessAtSecondarySpar;
	private List<Amount<Length>> _distanceBetweenSpars;
	private List<Amount<Length>> _prismoidsLength;
	private List<Amount<Area>> _prismoidsSectionsAreas;
	private List<Amount<Volume>> _prismoidsVolumes;
	private List<Amount<Length>> _fuelTankStations;
	private List<Amount<Length>> _wingChordsAtFuelTankStations;
	
	private Amount<Mass> _massEstimated, _massReference;

	private Amount<Length> _xCG;
	private Amount<Length> _yCG;
	private Amount<Length> _zCG;
	
	private Amount<Length> _xCGLRF;
	private Amount<Length> _yCGLRF;
	private Amount<Length> _zCGLRF;
	
	private CenterOfGravity _cG;
	
	// Jet A1 fuel density : the user can set this parameter when necessary
	private Amount<VolumetricDensity> _fuelDensity = Amount.valueOf(804.0, MyUnits.KILOGRAM_PER_CUBIC_METER);
	private Amount<Volume> _fuelVolume = Amount.valueOf(0.0, SI.CUBIC_METRE);
	private Amount<Mass> _fuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
	private Amount<Force> _fuelWeight = Amount.valueOf(0.0, SI.NEWTON);

	//--------------------------------------------------------------------------------------------
	// BUILDER
	
	public FuelTank(String id, LiftingSurface theWing) {
			
		this._theWing = theWing;
		
		_thicknessAtMainSpar = new ArrayList<>();
		_thicknessAtSecondarySpar = new ArrayList<>();
		_distanceBetweenSpars = new ArrayList<>();
		_prismoidsLength = new ArrayList<>();
		_prismoidsSectionsAreas = new ArrayList<>();
		_prismoidsVolumes = new ArrayList<>();
		_fuelTankStations = new ArrayList<>();
		_wingChordsAtFuelTankStations = new ArrayList<>();
		
		calculateGeometry(_theWing);
		calculateFuelMass();
		
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS
	
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
							AirfoilCalc.calculateThicknessRatioAtXNormalizedStation(
									theWing.getMainSparDimensionlessPosition(),
									theWing.getAirfoilList().get(i).getThicknessToChordRatio()
									)
							* theWing.getChordsBreakPoints().get(i).doubleValue(SI.METER),
							SI.METER)
					);
			this._thicknessAtSecondarySpar.add(
					Amount.valueOf(
							AirfoilCalc.calculateThicknessRatioAtXNormalizedStation(
									theWing.getSecondarySparDimensionlessPosition(),
									theWing.getAirfoilList().get(i).getThicknessToChordRatio()
									)
							* theWing.getChordsBreakPoints().get(i).doubleValue(SI.METER),
							SI.METER)
					);
			this._distanceBetweenSpars.add(
					Amount.valueOf(
							theWing.getSecondarySparDimensionlessPosition()
							* theWing.getChordsBreakPoints().get(i).doubleValue(SI.METER)
							- (theWing.getMainSparDimensionlessPosition()
							* theWing.getChordsBreakPoints().get(i).doubleValue(SI.METER)),
							SI.METER
							)
					);
			this._fuelTankStations.add(theWing.getYBreakPoints().get(i));
			this._wingChordsAtFuelTankStations.add(theWing.getChordsBreakPoints().get(i));
		}
		for(int i=1; i<theWing.getYBreakPoints().size()-1; i++)
			this._prismoidsLength.add(
					theWing.getYBreakPoints().get(i)
					.minus(theWing.getYBreakPoints().get(i-1))
					);
		
		Airfoil airfoilAt85Percent = LSGeometryCalc.calculateAirfoilAtY(
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
						AirfoilCalc.calculateThicknessRatioAtXNormalizedStation(
								theWing.getMainSparDimensionlessPosition(),
								airfoilAt85Percent.getThicknessToChordRatio()
								)
						* chordAt85Percent.doubleValue(SI.METER),
						SI.METER)
				);
		this._thicknessAtSecondarySpar.add(
				Amount.valueOf(
						AirfoilCalc.calculateThicknessRatioAtXNormalizedStation(
								theWing.getSecondarySparDimensionlessPosition(),
								airfoilAt85Percent.getThicknessToChordRatio()
								)
						* chordAt85Percent.doubleValue(SI.METER),
						SI.METER)
				);
		this._distanceBetweenSpars.add(
				Amount.valueOf(
						(theWing.getSecondarySparDimensionlessPosition()*chordAt85Percent.doubleValue(SI.METER))
						-(theWing.getMainSparDimensionlessPosition()*chordAt85Percent.doubleValue(SI.METER)),
						SI.METER
						)
				);
		this._fuelTankStations.add(theWing.getSemiSpan().times(0.85));
		
		this._wingChordsAtFuelTankStations.add(chordAt85Percent);
		
		this._prismoidsLength.add(
				theWing.getSemiSpan().times(0.85)
				.minus(theWing.getYBreakPoints().get(
						theWing.getYBreakPoints().size()-2)
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
	
	public void calculateGeometry(LiftingSurface theWing) {
		
		estimateDimensions(theWing);
		calculateAreas();
		calculateVolume();
	}
	
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
													
														.getXLEAtYActual(
																this._fuelTankStations.get(i).doubleValue(SI.METER)
																)
														.plus(this._wingChordsAtFuelTankStations.get(i)
																.times(_theWing.getMainSparDimensionlessPosition())
																)
														.doubleValue(SI.METER) + xCGLateralFacesLFRList.get(i)[0];
			xCGSegmentOppositeFaceSpanwiseY[1] = this._theWing
													
														.getXLEAtYActual(
																this._fuelTankStations.get(i+1).doubleValue(SI.METER)
																)
														.plus(this._wingChordsAtFuelTankStations.get(i+1)
																.times(_theWing.getMainSparDimensionlessPosition())
																)
														.doubleValue(SI.METER) + xCGLateralFacesLFRList.get(i)[2];

			xCGSegmentOppositeFaceChordwiseX[0] = this._theWing
																	.getYBreakPoints().get(i)
																		.doubleValue(SI.METER)
												  + xCGLateralFacesLFRList.get(i)[3]; 
			xCGSegmentOppositeFaceChordwiseX[1] = this._theWing
																	.getYBreakPoints().get(i)
																		.doubleValue(SI.METER)
												  + xCGLateralFacesLFRList.get(i)[1];
			
			xCGSegmentOppositeFaceChordwiseY[0] = this._theWing
													
														.getXLEAtYActual(
																this._theWing
																		.getYBreakPoints().get(i)
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[3]																
																).doubleValue(SI.METER)
														+ (this._theWing.getChordAtYActual(
																this._theWing
																	.getYBreakPoints().get(i)
																		.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[3])
																* _theWing.getMainSparDimensionlessPosition()
																);
			xCGSegmentOppositeFaceChordwiseY[1] = this._theWing
													
														.getXLEAtYActual(
																this._theWing
																		.getYBreakPoints().get(i)	
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[1]																
																).doubleValue(SI.METER)
														+ (this._theWing.getChordAtYActual(
																this._theWing
																		.getYBreakPoints().get(i)
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[1])
																* _theWing.getMainSparDimensionlessPosition()
																)
														+ ((this._theWing.getChordAtYActual(
																this._theWing
																		.getYBreakPoints().get(i)
																			.doubleValue(SI.METER)
																+ xCGLateralFacesLFRList.get(i)[1])
																* _theWing.getSecondarySparDimensionlessPosition())
															- (this._theWing.getChordAtYActual(
																	this._theWing
																			.getYBreakPoints().get(i)
																				.doubleValue(SI.METER)
																	+ xCGLateralFacesLFRList.get(i)[1])
																	* _theWing.getMainSparDimensionlessPosition()));

			// check if the chordwise X array is monotonic increasing
			if(xCGSegmentOppositeFaceChordwiseX[1] - xCGSegmentOppositeFaceChordwiseX[0] < 0.0001)
				xCGSegmentOppositeFaceChordwiseX[0] -= 0.0001; 
			
			// now that the segments coordinates are calculated, we have to intersect these latter.
			xCGPrismoidsList.add(
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									xCGSegmentOppositeFaceSpanwiseX,
									xCGSegmentOppositeFaceSpanwiseY,
									xCGSegmentOppositeFaceChordwiseX[0]),
							SI.METER
							)
					);
			
			
		}
		
		System.out.println("\n xCG list = " + xCGPrismoidsList);
		
		_xCGLRF = Amount.valueOf(0.0, SI.METER);
		
		for(int i=0; i<this._prismoidsVolumes.size(); i++)
			_xCGLRF = _xCGLRF.plus(
					Amount.valueOf(
							this._prismoidsVolumes.get(i).getEstimatedValue()
							*xCGPrismoidsList.get(i).getEstimatedValue()
							, SI.METER
							)
					);
		_xCGLRF = _xCGLRF.divide(this._fuelVolume.divide(2).getEstimatedValue());
		_xCG = _xCGLRF.plus(_theWing.getXApexConstructionAxes());
		
		_yCGLRF = Amount.valueOf(0.0, SI.METER);
		_yCG = Amount.valueOf(0.0, SI.METER);
		
		_zCGLRF = _theWing.getTheBalanceManager().getCG().getZLRF();
		_zCG = _theWing.getTheBalanceManager().getCG().getZBRF();
		
		_cG = new CenterOfGravity();
		_cG.setX0(_xApexConstructionAxes.to(SI.METER));
		_cG.setY0(_yApexConstructionAxes.to(SI.METER));
		_cG.setZ0(_zApexConstructionAxes.to(SI.METER));
		_cG.setXLRFref(_xCGLRF.to(SI.METER));
		_cG.setYLRFref(_yCGLRF.to(SI.METER));
		_cG.setZLRFref(_zCGLRF.to(SI.METER));
		
		_cG.calculateCGinBRF(ComponentEnum.FUEL_TANK);
		
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
				.append("\tMain spar position (% local chord): " + _theWing.getMainSparDimensionlessPosition() + "\n")
				.append("\tSecondary spar position (% local chord): " + _theWing.getSecondarySparDimensionlessPosition() + "\n")
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
	
	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public LiftingSurface getTheWing() {
		return _theWing;
	}
	
	public void setTheWing (LiftingSurface theWing) {
		this._theWing = theWing;
	}
	
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public List<Amount<Length>> getDistanceBetweenSpars() {
		return _distanceBetweenSpars;
	}

	public void setDistanceBetweenSpars(List<Amount<Length>> _distanceBetweenSpars) {
		this._distanceBetweenSpars = _distanceBetweenSpars;
	}

	public List<Amount<Length>> getPrismoidsLength() {
		return _prismoidsLength;
	}

	public void setPrismoidsLength(List<Amount<Length>> _prismoidsLength) {
		this._prismoidsLength = _prismoidsLength;
	}

	public Amount<Length> getXCG() {
		return _xCG;
	}

	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	public Amount<Length> getYCG() {
		return _yCG;
	}

	public void setYCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	public Amount<Length> getZCG() {
		return _zCG;
	}

	public void setZCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	public Amount<VolumetricDensity> getFuelDensity() {
		return _fuelDensity;
	}

	public void setFuelDensity(Amount<VolumetricDensity> _fuelDensity) {
		this._fuelDensity = _fuelDensity;
	}

	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public List<Amount<Length>> getThicknessAtMainSpar() {
		return _thicknessAtMainSpar;
	}

	public List<Amount<Length>> getThicknessAtSecondarySpar() {
		return _thicknessAtSecondarySpar;
	}

	public List<Amount<Area>> getPrismoidsSectionsAreas() {
		return _prismoidsSectionsAreas;
	}

	public List<Amount<Volume>> getPrismoidsVolumes() {
		return _prismoidsVolumes;
	}

	public Amount<Volume> getFuelVolume() {
		return _fuelVolume;
	}

	public Amount<Mass> getFuelMass() {
		return _fuelMass;
	}

	public void setFuelMass(Amount<Mass> fuelMass) {
		this._fuelMass = fuelMass;
	}
	
	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	public Amount<Force> getFuelWeight() {
		return _fuelWeight;
	}

	public Amount<Length> getXCGLRF() {
		return _xCGLRF;
	}

	public void setXCGLRF(Amount<Length> _xCGLRF) {
		this._xCGLRF = _xCGLRF;
	}

	public Amount<Length> getYCGLRF() {
		return _yCGLRF;
	}

	public void setYCGLRF(Amount<Length> _yCGLRF) {
		this._yCGLRF = _yCGLRF;
	}

	public Amount<Length> getZCGLRF() {
		return _zCGLRF;
	}

	public void setZCGLRF(Amount<Length> _zCGLRF) {
		this._zCGLRF = _zCGLRF;
	}

	public CenterOfGravity getCG() {
		return _cG;
	}

	public void setCG(CenterOfGravity _cG) {
		this._cG = _cG;
	}
}
