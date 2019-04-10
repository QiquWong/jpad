package it.unina.daf.jpadcad;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.enums.FairingPosition;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import javaslang.Tuple2;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class FairingDataCollection {

	private Fuselage _fuselage = null;
	private LiftingSurface _liftingSurface = null;
	
	private double _frontLengthFactor = 0.0;
	private double _backLengthFactor = 0.0;
	private double _widthFactor = 0.0;
	private double _heightFactor = 0.0;
	private double _heightBelowReferenceFactor = 0.0;
	private double _heightAboveReferenceFactor = 0.0;
	private double _filletRadiusFactor = 0.0;
	
	private double _rootChord = 0.0;
	private double _rootThickness = 0.0;
	
	private List<double[]> _rootAirfoilPts = new ArrayList<>();
	private List<double[]> _sideAirfoilPts = new ArrayList<>(); // LS airfoil points at FUSELAGE max width
	private List<double[]> _tipAirfoilPts = new ArrayList<>();  // LS airfoil points at FAIRING max width
	private double[] _rootAirfoilTop = new double[3];
	private double[] _rootAirfoilBottom = new double[3];
	private double[] _rootAirfoilLE = new double[3];
	private double[] _rootAirfoilTE = new double[3];
	private double[] _sideAirfoilTop = new double[3];
	private double[] _sideAirfoilBottom = new double[3];
	private double[] _tipAirfoilTop = new double[3];
	private double[] _tipAirfoilBottom = new double[3];

	private PVector _fusDeltaApex = null;
	private double[] _fuselageSCMiddleTopPnt = new double[3];
	private double[] _fuselageSCMiddleBottomPnt = new double[3];
	private double[] _fuselageSCFrontTopPnt = new double[3];
	private double[] _fuselageSCFrontBottomPnt = new double[3];
	private double[] _fuselageSCBackTopPnt = new double[3];
	private double[] _fuselageSCBackBottomPnt = new double[3];
	private Tuple2<List<Double>, List<Double>> _fuselageSCMiddleUpperYZCoords = null;
	private Tuple2<List<Double>, List<Double>> _fuselageSCMiddleLowerYZCoords = null;
	private double _fuselageMinimumZ = 0.0;
	private double _fuselageMaximumZ = 0.0;
	
	private double[] _fusLSContactPnt = new double[3];
	private double[] _fusFairingUppContactPnt = new double[3];
	private double[] _fusFairingLowContactPnt = new double[3];
	
	private double _fairingMinimumZ = 0.0;
	private double _fairingMaximumZ = 0.0;
	private double _fairingReferenceZ = 0.0;	
		
	private double _frontLength = 0.0;
	private double _backLength = 0.0;
	private double _width = 0.0;
	
	private FairingPosition _fairingPosition;	
	
	private boolean _populated = false;
	
	public FairingDataCollection (Fuselage fuselage, LiftingSurface liftingSurface, 
			double frontLengthFactor, double backLengthFactor, double widthFactor, double heightFactor,
			double heightBelowReferenceFactor, double heightAboveContactFactor,
			double filletRadiusFactor
			) {
		
		this._fuselage = fuselage;
		this._liftingSurface = liftingSurface;
		
		// -------------------------------
		// FAIRING parameters assignment
		// -------------------------------
		this._frontLengthFactor = frontLengthFactor;
		this._backLengthFactor = backLengthFactor;
		this._widthFactor = widthFactor;
		this._heightFactor = heightFactor;
		this._heightBelowReferenceFactor = heightBelowReferenceFactor;
		this._heightAboveReferenceFactor = heightAboveContactFactor;
		this._filletRadiusFactor = filletRadiusFactor;
		
		// -------------------------------
		// FAIRING reference lengths
		// -------------------------------
		this._rootChord = liftingSurface.getChordsBreakPoints().get(0).doubleValue(SI.METER);
		this._rootThickness = liftingSurface.getAirfoilList().get(0).getThicknessToChordRatio()*_rootChord;
		
		// -------------------------------
		// FUSELAGE delta position
		// -------------------------------
		this._fusDeltaApex = new PVector(
				(float) fuselage.getXApexConstructionAxes().doubleValue(SI.METER),
				(float) fuselage.getYApexConstructionAxes().doubleValue(SI.METER),
				(float) fuselage.getZApexConstructionAxes().doubleValue(SI.METER)
				);		
		
		// ------------------------
		// Root reference airfoil
		// ------------------------
		this._rootAirfoilPts = AircraftCADUtils.generateAirfoilAtY(0, liftingSurface);
		
		this._rootAirfoilTop = getAirfoilTop(_rootAirfoilPts);
		this._rootAirfoilBottom = getAirfoilBottom(_rootAirfoilPts);
		this._rootAirfoilLE = getAirfoilLE(_rootAirfoilPts);
		this._rootAirfoilTE = getAirfoilTE(_rootAirfoilPts);
		
		// --------------------------------------
		// FUSELAGE reference points and curves
		// --------------------------------------
		double fusWidthAtRootAirfoilTopX = fuselage.getWidthAtX(_rootAirfoilTop[0])*0.5;
		double fusCamberZAtRootAirfoilTopX = fuselage.getCamberZAtX(_rootAirfoilTop[0] - _fusDeltaApex.x);
		
		List<PVector> fuselageSCMiddle = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(_rootAirfoilTop[0] - _fusDeltaApex.x, SI.METER));	
		List<PVector> fuselageSCFront = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf((_rootAirfoilLE[0] - _frontLength) - _fusDeltaApex.x, SI.METER));
		List<PVector> fuselageSCBack = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf((_rootAirfoilTE[0] + _backLength) - _fusDeltaApex.x, SI.METER));

		fuselageSCMiddle.forEach(pv -> pv.add(_fusDeltaApex));

		this._fuselageSCMiddleTopPnt = new double[] {
				fuselageSCMiddle.get(0).x,
				fuselageSCMiddle.get(0).y,
				fuselageSCMiddle.get(0).z
		};
		this._fuselageSCMiddleBottomPnt = new double[] {
				fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).x,
				fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).y,
				fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).z
		};
		this._fuselageSCFrontTopPnt = new double[] {
				fuselageSCFront.get(0).x + _fusDeltaApex.x,
				fuselageSCFront.get(0).y + _fusDeltaApex.y,
				fuselageSCFront.get(0).z + _fusDeltaApex.z
		};
		this._fuselageSCFrontBottomPnt = new double[] {
				fuselageSCFront.get(fuselageSCFront.size() - 1).x + _fusDeltaApex.x,
				fuselageSCFront.get(fuselageSCFront.size() - 1).y + _fusDeltaApex.y,
				fuselageSCFront.get(fuselageSCFront.size() - 1).z + _fusDeltaApex.z
		};
		this._fuselageSCBackTopPnt = new double[] {
				fuselageSCBack.get(0).x + _fusDeltaApex.x,
				fuselageSCBack.get(0).y + _fusDeltaApex.y,
				fuselageSCBack.get(0).z + _fusDeltaApex.z
		};
		this._fuselageSCBackBottomPnt = new double[] {
				fuselageSCBack.get(fuselageSCBack.size() - 1).x + _fusDeltaApex.x,
				fuselageSCBack.get(fuselageSCBack.size() - 1).y + _fusDeltaApex.y,
				fuselageSCBack.get(fuselageSCBack.size() - 1).z + _fusDeltaApex.z
		};

		this._fuselageMaximumZ = Math.min(_fuselageSCFrontTopPnt[2], _fuselageSCBackTopPnt[2]);
		this._fuselageMinimumZ = Math.max(_fuselageSCFrontBottomPnt[2], _fuselageSCBackBottomPnt[2]);

		List<Double> fuselageSCMiddleUpperZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleUpperYCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerYCoords = new ArrayList<>();

		fuselageSCMiddleLowerZCoords.add(fusCamberZAtRootAirfoilTopX);
		fuselageSCMiddleLowerYCoords.add(fusWidthAtRootAirfoilTopX);

		for (int i = 0; i < fuselageSCMiddle.size() - 1; i++) {
			PVector pv = fuselageSCMiddle.get(i);

			if (pv.z > fusCamberZAtRootAirfoilTopX) {
				fuselageSCMiddleUpperZCoords.add((double) pv.z);
				fuselageSCMiddleUpperYCoords.add((double) pv.y);
			} else if (pv.z < fusCamberZAtRootAirfoilTopX) {
				fuselageSCMiddleLowerZCoords.add((double) pv.z);
				fuselageSCMiddleLowerYCoords.add((double) pv.y);
			}
		}

		fuselageSCMiddleUpperZCoords.add(fusCamberZAtRootAirfoilTopX);
		fuselageSCMiddleUpperYCoords.add(fusWidthAtRootAirfoilTopX);

		this._fuselageSCMiddleUpperYZCoords = obtainMonotonicSequence(
				fuselageSCMiddleUpperYCoords, fuselageSCMiddleUpperZCoords, true);

		this._fuselageSCMiddleLowerYZCoords = obtainMonotonicSequence(
				fuselageSCMiddleLowerYCoords, fuselageSCMiddleLowerZCoords, false);
		
		// ------------------------
		// Side reference airfoil
		// ------------------------
		this._sideAirfoilPts = AircraftCADUtils.generateAirfoilAtY(fusWidthAtRootAirfoilTopX, liftingSurface);
		
		this._sideAirfoilTop = getAirfoilTop(_sideAirfoilPts);
		this._sideAirfoilBottom = getAirfoilBottom(_sideAirfoilPts);
		
		// ------------------------------------------------------
		// Check FAIRING position
		// ------------------------------------------------------
		_fairingPosition = checkFairingPosition();
		
		// ------------------------------------------------------
		// LIFTING-SURFACE / FUSELAGE contact point calculation and 
		// ------------------------------------------------------
		switch (_fairingPosition) {

		case ATTACHED_UP:
			_fusLSContactPnt = new double[] {
					_sideAirfoilBottom[0],
					MyMathUtils.getInterpolatedValue1DSpline(
							MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleUpperYZCoords._2())), 
							MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleUpperYZCoords._1())), 
							_sideAirfoilBottom[2]),
					_sideAirfoilBottom[2]
			};

			break;

		case ATTACHED_DOWN:
			_fusLSContactPnt = new double[] {
					_sideAirfoilTop[0],
					MyMathUtils.getInterpolatedValue1DSpline(
							MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._2())), 
							MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._1())), 
							_sideAirfoilTop[2]),
					_sideAirfoilTop[2]
			};
			
			break;
			
		default:

			break;
		}
		
		// ----------------------------------------
		// Calculate fairing principal dimensions
		// ----------------------------------------
		this._frontLength = _frontLengthFactor*_rootChord;
		this._backLength = _backLengthFactor*_rootChord;
		
		if ((_fairingPosition.equals(FairingPosition.ATTACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) 
				&& _widthFactor < 1.0) {
			
			this._width = widthFactor*(fusWidthAtRootAirfoilTopX - (_fusLSContactPnt[1] - _fusDeltaApex.y)) + 
					(_fusLSContactPnt[1] - _fusDeltaApex.y);
			
		} else {
			
			this._width = _widthFactor*fusWidthAtRootAirfoilTopX;
		}
		
		// ------------------------
		// Tip reference airfoil
		// ------------------------
		this._tipAirfoilPts = AircraftCADUtils.generateAirfoilAtY(_width, liftingSurface);				
		
		this._tipAirfoilTop = getAirfoilTop(_tipAirfoilPts);
		this._tipAirfoilBottom = getAirfoilBottom(_tipAirfoilPts);

		// --------------------------------------------------------------------------------
		// FUSELAGE / FAIRING contact point and maximum/minimum z coordinates calculation
		// --------------------------------------------------------------------------------
		if (_widthFactor < 1.0) {
			
			_fusFairingUppContactPnt = new double[] {
					_tipAirfoilTop[0],
					_width,
					MyMathUtils.getInterpolatedValue1DSpline(
							MyArrayUtils.convertToDoublePrimitive(_fuselageSCMiddleUpperYZCoords._1()), 
							MyArrayUtils.convertToDoublePrimitive(_fuselageSCMiddleUpperYZCoords._2()), 
							_width
							)
			};
			
			_fusFairingLowContactPnt = new double[] {
					_tipAirfoilTop[0],
					_width,
					MyMathUtils.getInterpolatedValue1DSpline(
							MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._1())), 
							MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._2())), 
							_width
							)
			};
			
			if (_fairingPosition.equals(FairingPosition.DETACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
				
				_fairingReferenceZ = _fusFairingUppContactPnt[2];
				
				_fairingMinimumZ = MyArrayUtils.getMax(
						new double[] {
								_fuselageSCFrontBottomPnt[2],
								_fusFairingLowContactPnt[2],
								_fuselageSCBackBottomPnt[2]
						});
				
				_fuselageMaximumZ = Math.min(
						_fuselageSCFrontTopPnt[2], 
						_fuselageSCBackTopPnt[2]
						);
				
				if (_fairingPosition.equals(FairingPosition.DETACHED_UP)) {
					
					_fairingMaximumZ = Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]) + _rootThickness*_heightFactor;
					
				} else if (_fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
					
					_fairingMaximumZ = _rootAirfoilTop[2] + (_fuselageSCMiddleTopPnt[2] - _rootAirfoilTop[2])*_heightFactor;
					
				}			
				
			} else if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN) || _fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
				
				_fairingReferenceZ = _fusFairingLowContactPnt[2];
				
				_fairingMaximumZ = MyArrayUtils.getMin(
						new double[] {
								_fuselageSCFrontTopPnt[2],
								_fusFairingUppContactPnt[2],
								_fuselageSCBackTopPnt[2]
						});
				
				_fuselageMinimumZ = Math.max(
						_fuselageSCFrontBottomPnt[2], 
						_fuselageSCBackBottomPnt[2]
						);
				
				if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) {
					
					_fairingMinimumZ = _fuselageSCMiddleBottomPnt[2] - _rootThickness*_heightFactor;
					
				} else if (_fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
					
					_fairingMinimumZ = Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - _rootThickness*_heightFactor;
					
				}				
			}
			
		} else {
			
			if (_fairingPosition.equals(FairingPosition.DETACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
				
				_fairingReferenceZ = fusCamberZAtRootAirfoilTopX + 
						(Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - fusCamberZAtRootAirfoilTopX)*0.5;
				
				_fairingMinimumZ = MyArrayUtils.getMax(
						new double[] {
								_fuselageSCFrontBottomPnt[2],
								_fuselageSCBackBottomPnt[2]
						});
				
				_fuselageMaximumZ = Math.min(
						_fuselageSCFrontTopPnt[2], 
						_fuselageSCBackTopPnt[2]
						);
				
				if (_fairingPosition.equals(FairingPosition.DETACHED_UP)) {
					
					_fairingMaximumZ = Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]) + _rootThickness*_heightFactor;
					
				} else if (_fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
					
					_fairingMaximumZ = _rootAirfoilTop[2] + (_fuselageSCMiddleTopPnt[2] - _rootAirfoilTop[2])*_heightFactor;
					
				}	
				
			} else if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN) || _fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
				
				_fairingReferenceZ = fusCamberZAtRootAirfoilTopX - 
						(fusCamberZAtRootAirfoilTopX - Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]))*0.5;
				
				_fairingMaximumZ = MyArrayUtils.getMin(
						new double[] {
								_fuselageSCFrontTopPnt[2],
								_fuselageSCBackTopPnt[2]
						});
				
				_fuselageMinimumZ = Math.max(
						_fuselageSCFrontBottomPnt[2], 
						_fuselageSCBackBottomPnt[2]
						);
				
				if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) {
					
					_fairingMinimumZ = _fuselageSCMiddleBottomPnt[2] - _rootThickness*_heightFactor;
					
				} else if (_fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
					
					_fairingMinimumZ = Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - _rootThickness*_heightFactor;
					
				}				
			}
		}
		
		_populated = true;
	}
	
	@Override
	public String toString() {
		
		StringBuilder stringBuilder = new StringBuilder();
		
		if (_populated) {
			DecimalFormat df = new DecimalFormat("#.##");
					
			stringBuilder.append("\n\t\tFairingDataCollection class attributes summary:\n\n")
						 .append("\t\t\tInterested components: " + _liftingSurface.getType().name() + ", " + ComponentEnum.FUSELAGE.name() + "\n")
						 .append("\t\t\tFront length factor: " + df.format(_frontLengthFactor) + "\n")
						 .append("\t\t\tBack length factor: " + df.format(_backLengthFactor) + "\n")
						 .append("\t\t\tWidth factor: " + df.format(_widthFactor) + "\n")
						 .append("\t\t\tHeight factor: " + df.format(_heightFactor) + "\n")
						 .append("\t\t\tHeight below reference factor: " + df.format(_heightBelowReferenceFactor) + "\n")
						 .append("\t\t\theight above reference factor: " + df.format(_heightAboveReferenceFactor) + "\n")
						 .append("\t\t\tFillet radius factor: " + df.format(_filletRadiusFactor) + "\n")
						 .append("\t\t\tReference airfoil chord (root, m): " + df.format(_rootChord) + "\n")
						 .append("\t\t\tReference airfoil thickness (root, m): " + df.format(_rootThickness) + "\n")
						 .append("\t\t\tRoot airfoil xLE (m): " + df.format(_rootAirfoilLE[0]) + "\n")
						 .append("\t\t\tRoot airfoil xTE (m): " + df.format(_rootAirfoilTE[0]) + "\n")
						 .append("\t\t\tRoot airfoil zTop (m): " + df.format(_rootAirfoilTop[2]) + "\n")
						 .append("\t\t\tRoot airfoil zBot (m): " + df.format(_rootAirfoilBottom[2]) + "\n")
						 .append("\t\t\tSide airfoil zTop (m): " + df.format(_sideAirfoilTop[2]) + "\n")
						 .append("\t\t\tSide airfoil zBot (m): " + df.format(_sideAirfoilBottom[2]) + "\n")
						 .append("\t\t\tTip airfoil zTop (m): " + df.format(_tipAirfoilTop[2]) + "\n")
						 .append("\t\t\tTip airfoil zBot (m): " + df.format(_tipAirfoilBottom[2]) + "\n")
						 .append("\t\t\tFuselage middle curve zTop (m): " + df.format(_fuselageSCMiddleTopPnt[2]) + "\n")
						 .append("\t\t\tFuselage middle curve zBot (m): " + df.format(_fuselageSCMiddleBottomPnt[2]) + "\n")
						 .append("\t\t\tFuselage front curve zTop (m): " + df.format(_fuselageSCFrontTopPnt[2]) + "\n")
						 .append("\t\t\tFuselage front curve zBot (m): " + df.format(_fuselageSCFrontBottomPnt[2]) + "\n")
						 .append("\t\t\tFuselage back curve zTop (m): " + df.format(_fuselageSCBackTopPnt[2]) + "\n")
						 .append("\t\t\tFuselage back curve zBot (m): " + df.format(_fuselageSCBackBottomPnt[2]) + "\n")
						 .append("\t\t\tFuselage minimum z (m): " + df.format(_fuselageMinimumZ) + "\n")
						 .append("\t\t\tFuselage maximum z (m): " + df.format(_fuselageMaximumZ) + "\n")
						 .append("\t\t\tFuselage-Fairing lower contact point z (m): " + df.format(_fusFairingLowContactPnt[2]) + "\n")
						 .append("\t\t\tFuselage-Fairing upper contact point z (m): " + df.format(_fusFairingUppContactPnt[2]) + "\n")
						 .append("\t\t\tFairing minimum z (m): " + df.format(_fairingMinimumZ) + "\n")
						 .append("\t\t\tFairing reference z (m): " + df.format(_fairingReferenceZ) + "\n")
						 .append("\t\t\tFairing maximum z (m): " + df.format(_fairingMaximumZ));
		}
			
		return stringBuilder.toString();
	} 
	
	private FairingPosition checkFairingPosition() {

		List<PVector> fuselageSCAtTopX = _fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(_sideAirfoilTop[0] - _fusDeltaApex.x, SI.METER));
		List<PVector> fuselageSCAtBottomX = _fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(_sideAirfoilBottom[0] - _fusDeltaApex.x, SI.METER));

		double fuselageZTopAtTopX = fuselageSCAtTopX.get(0).add(_fusDeltaApex).z;
		double fuselageCamberZAtTopX = _fuselage.getCamberZAtX(_sideAirfoilTop[0] - _fusDeltaApex.x) + _fusDeltaApex.z;
		double fuselageCamberZAtBottomX = _fuselage.getCamberZAtX(_sideAirfoilBottom[0] - _fusDeltaApex.x) + _fusDeltaApex.z;
		double fuselageZBottomAtBottomX = fuselageSCAtBottomX.get(fuselageSCAtBottomX.size() - 1).add(_fusDeltaApex).z;

		if (_rootAirfoilTop[2] > fuselageZTopAtTopX) {
			return FairingPosition.DETACHED_UP;

		} else if (_sideAirfoilTop[2] < fuselageZTopAtTopX && _sideAirfoilBottom[2] > fuselageCamberZAtBottomX) {
			return FairingPosition.ATTACHED_UP;
		}

		if (_rootAirfoilBottom[2] < fuselageZBottomAtBottomX) {
			return FairingPosition.DETACHED_DOWN;

		} else if (_sideAirfoilTop[2] < fuselageCamberZAtTopX && _sideAirfoilBottom[2] > fuselageZBottomAtBottomX) {
			return FairingPosition.ATTACHED_DOWN;
		}		

		return FairingPosition.MIDDLE;
	}
	
	private double[] getAirfoilTop(List<double[]> airfoilPts) {
		return airfoilPts.stream().max(Comparator.comparing(pnt -> pnt[2])).get();
	}
	
	private double[] getAirfoilBottom(List<double[]> airfoilPts) {
		return airfoilPts.stream().min(Comparator.comparing(pnt -> pnt[2])).get();
	}
	
	private double[] getAirfoilLE(List<double[]> airfoilPts) {
		return airfoilPts.stream().min(Comparator.comparing(pnt -> pnt[0])).get();
	}
	
	private double[] getAirfoilTE(List<double[]> airfoilPts) {
		return airfoilPts.stream().max(Comparator.comparing(pnt -> pnt[0])).get();
	}
	
	private static <T> List<T> reverseList(List<T> list) {
		return IntStream.range(0, list.size())
				.mapToObj(i -> list.get(list.size() - 1 - i))
				.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private static Tuple2<List<Double>, List<Double>> obtainMonotonicSequence(
			List<Double> y, List<Double> z, boolean strictlyIncreasing) {		
		
		List<Double> ym = new ArrayList<>();
		List<Double> zm = new ArrayList<>();
		
		int n = y.size() - 1;
		ym.add(y.get(0));
		zm.add(z.get(0));
		
		int j = 0;
		for (int i = 1; i <= n; i++) {
			Double yt_p = y.get(i);
			Double zt_p = z.get(i);
			Double yt_m = ym.get(j);
			
			if (strictlyIncreasing) {
				if (yt_p > yt_m) {
					ym.add(yt_p);
					zm.add(zt_p);
					j++;
				}
			} else {
				if (yt_p < yt_m) {
					ym.add(yt_p);
					zm.add(zt_p);
					j++;
				}
			}				
		}
		
		return new Tuple2<List<Double>, List<Double>>(ym, zm);
	}
	
	public Fuselage getFuselage() {
		return _fuselage;
	}
	
	public LiftingSurface getLiftingSurface() {
		return _liftingSurface;
	}
	
	public double getFrontLengthFactor() {
		return _frontLengthFactor;
	}
	
	public double getBackLengthFactor() {
		return _backLengthFactor;
	}
	
	public double getWidthFactor() {
		return _widthFactor;
	}
	
	public double getHeightFactor() {
		return _heightFactor;
	}
	
	public double getHeightBelowReferenceFactor() {
		return _heightBelowReferenceFactor;
	}
	
	public double getHeightAboveReferenceFactor() {
		return _heightAboveReferenceFactor;
	}
	
	public double getFilletRadiusFactor() {
		return _filletRadiusFactor;
	}
	
	public double getRootChord() {
		return _rootChord;
	}
	
	public double getRootThickness() {
		return _rootThickness;
	}
	
	public List<double[]> getRootAirfoilPts() {
		return _rootAirfoilPts;
	}
	
	public List<double[]> getSideAirfoilPts() {
		return _sideAirfoilPts;
	}
	
	public List<double[]> getTipAirfoilPts() {
		return _tipAirfoilPts;
	}
	
	public double[] getRootAirfoilTop() {
		return _rootAirfoilTop;
	}
	
	public double[] getRootAirfoilBottom() {
		return _rootAirfoilBottom;
	}
	
	public double[] getRootAirfoilLE() {
		return _rootAirfoilLE;
	}
	
	public double[] getRootAirfoilTE() {
		return _rootAirfoilTE;
	}
	
	public double[] getSideAirfoilTop() {
		return _sideAirfoilTop;
	}
	
	public double[] getSideAirfoilBottom() {
		return _sideAirfoilBottom;
	}
	
	public double[] getTipAirfoilTop() {
		return _tipAirfoilTop;
	}
	
	public double[] getTipAirfoilBottom() {
		return _tipAirfoilBottom;
	}
	
	public PVector getFusDeltaApex() {
		return _fusDeltaApex;
	}
	
	public double[] getFuselageSCMiddleTop() {
		return _fuselageSCMiddleTopPnt;
	}
	
	public double[] getFuselageSCMiddleBottom() {
		return _fuselageSCMiddleBottomPnt;
	}
	
	public double[] getFuselageSCFrontTop() {
		return _fuselageSCFrontTopPnt;
	}
	
	public double[] getFuselageSCFrontBottom() {
		return _fuselageSCFrontBottomPnt;
	}
	
	public double[] getFuselageSCBackTop() {
		return _fuselageSCBackTopPnt;
	}
	
	public double[] getFuselageSCBackBottom() {
		return _fuselageSCBackBottomPnt;
	}
	
	public Tuple2<List<Double>, List<Double>> getFuselageSCMiddleUpperYZCoords() {
		return _fuselageSCMiddleUpperYZCoords;
	}
	
	public Tuple2<List<Double>, List<Double>> getFuselageSCMiddleLowerYZCoords() {
		return _fuselageSCMiddleLowerYZCoords;
	}
	
	public double getFuselageMinimumZ() {
		return _fuselageMinimumZ;
	}
	
	public double getFuselageMaximumZ() {
		return _fuselageMaximumZ;
	}

	public double[] getFusLSContactPnt() {
		return _fusLSContactPnt;
	}
	
	public double[] getFusFairingUppContactPnt() {
		return _fusFairingUppContactPnt;
	}
	
	public double[] getFusFairingLowContactPnt() {
		return _fusFairingLowContactPnt;
	} 
	
	public double getFairingMinimumZ() {
		return _fairingMinimumZ;
	}
	
	public double getFairingMaximumZ() {
		return _fairingMaximumZ;
	}
	
	public double getFairingReferenceZ() {
		return _fairingReferenceZ;
	}
	
	public double getFairingFrontLength() {
		return _frontLength;
	}
	
	public double getFairingBackLength() {
		return _backLength;
	}
	
	public double getFairingWidth() {
		return _width;
	}
	
	public FairingPosition getFairingPosition() {
		return _fairingPosition;
	}	
	
	public boolean isPopulated() {
		return _populated;
	}
}
