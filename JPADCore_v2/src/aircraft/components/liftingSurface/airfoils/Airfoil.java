package aircraft.components.liftingSurface.airfoils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import calculators.aerodynamics.AirfoilCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;
import processing.core.PVector;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

public class Airfoil {

	//----------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private IAirfoil _theAirfoilInterface;
	
	private List<PVector> _coordinatesRight;
	private List<PVector> _coordinatesLeft;
	
	//----------------------------------------------------------------------------------
	// BUILDER
	public Airfoil (IAirfoil theAirfoilInterface) {
		
		this._theAirfoilInterface = theAirfoilInterface;
		
		this._coordinatesLeft = new ArrayList<>();
		this._coordinatesRight = new ArrayList<>();
		
	}
	
	//----------------------------------------------------------------------------------
	// METHODS
	public static Airfoil importFromXML(String pathToXML) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading airfoil data ...");

		boolean externalClCurveFlag = false;
		String externalClCurveProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@external_cl_curve");
		if(externalClCurveProperty.equalsIgnoreCase("true"))
			externalClCurveFlag = true;
		else
			externalClCurveFlag = false;
		
		boolean externalCdCurveFlag = false;
		String externalCdCurveProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@external_cd_curve");
		if(externalCdCurveProperty.equalsIgnoreCase("true"))
			externalCdCurveFlag = true;
		else
			externalCdCurveFlag = false;
		
		boolean externalCmCurveFlag = false;
		String externalCmCurveProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@external_cm_curve");
		if(externalCmCurveProperty.equalsIgnoreCase("true"))
			externalCmCurveFlag = true;
		else
			externalCmCurveFlag = false;
		
		String name = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/@name");
		
		String familyProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/@family");

		String typeProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/@type");
		
		// check if the airfoil type given in file is a legal enumerated type
		AirfoilTypeEnum type = Arrays.stream(AirfoilTypeEnum.values())
	            .filter(e -> e.toString().equals(typeProperty))
	            .findFirst()
	            .orElseThrow(() -> new IllegalStateException(String.format("Unsupported airfoil type %s.", typeProperty)));
		
		AirfoilFamilyEnum family = Arrays.stream(AirfoilFamilyEnum.values())
	            .filter(e -> e.toString().equals(familyProperty))
	            .findFirst()
	            .orElseThrow(() -> new IllegalStateException(String.format("Unsupported airfoil family %s.", familyProperty)));
		
		double thicknessRatio = 0.0;
		double radiusLeadingEdgeNormalized = 0.0;
		double[] xCoords = null;
		double[] zCoords = null;
		Amount<Angle> alphaZeroLift = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaEndLinearTrait = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaStall = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<?> clAlphaLinearTrait = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse());
		double clAtAlphaZero = 0.0;
		double clEndLinearTrait = 0.0;
		double clMax = 0.0;
		double cDmin = 0.0;
		double clAtCdMin = 0.0;
		double kFactorDragPolar = 0.0;
		double laminarBucketSemiExtension = 0.0;
		double laminarBucketDepth = 0.0;
		Amount<?> cmAlphaQuarterChord = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse());
		double cmAC = 0.0;
		double cmACAtStall = 0.0;
		List<Double> clCurve = new ArrayList<>();
		List<Double> cdCurve = new ArrayList<>();
		List<Double> cmCurve = new ArrayList<>();
		List<Amount<Angle>> alphaForClCurve = new ArrayList<>();
		List<Double> clForCdCurve = new ArrayList<>();
		List<Double> clForCmCurve = new ArrayList<>();
		double xACNormalized = 0.0;
		double machCritical = 0.0;
		double xTransitionUpper = 0.0;
		double xTransitionLower = 0.0;
		
		String thicknessRatioProperty = reader.getXMLPropertyByPath("//geometry/thickness_to_chord_ratio_max");
		if(thicknessRatioProperty != null)
			thicknessRatio = Double.valueOf(reader.getXMLPropertyByPath("//geometry/thickness_to_chord_ratio_max"));
		
		String radiusLeadingEdgeNormalizedProperty = reader.getXMLPropertyByPath("//geometry/radius_leading_edge_normalized");
		if(radiusLeadingEdgeNormalizedProperty != null)
			radiusLeadingEdgeNormalized = Double.valueOf(radiusLeadingEdgeNormalizedProperty);
		
		List<String> xCoordsProperty = reader.getXMLPropertiesByPath("//geometry/x_coordinates");
		if(!xCoordsProperty.isEmpty()) 
			xCoords = MyArrayUtils.convertToDoublePrimitive(
					MyArrayUtils.convertListOfDoubleToDoubleArray(
							JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/x_coordinates").get(0)).stream()
							.map(x -> Double.valueOf(x))
							.collect(Collectors.toList())
							)
					);
			
		List<String> zCoordsProperty = reader.getXMLPropertiesByPath("//geometry/z_coordinates");
		if(!zCoordsProperty.isEmpty()) 
			zCoords = MyArrayUtils.convertToDoublePrimitive( 
					MyArrayUtils.convertListOfDoubleToDoubleArray(
							JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/z_coordinates").get(0)).stream()
							.map(x -> Double.valueOf(x))
							.collect(Collectors.toList())
							)
					);
		
		if(externalClCurveFlag == Boolean.FALSE) {
		
			String alphaZeroLiftProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/alpha_zero_lift");
			if (alphaZeroLiftProperty!= null)
				alphaZeroLift = reader.getXMLAmountAngleByPath("//airfoil/aerodynamics/alpha_zero_lift");

			String alphaEndLinearTraitProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/alpha_end_linear_trait");
			if (alphaEndLinearTraitProperty!= null)
				alphaEndLinearTrait = reader.getXMLAmountAngleByPath("//airfoil/aerodynamics/alpha_end_linear_trait");

			String alphaStallProperty =  reader.getXMLPropertyByPath("//airfoil/aerodynamics/alpha_stall");
			if (alphaStallProperty!= null)
				alphaStall = reader.getXMLAmountAngleByPath("//airfoil/aerodynamics/alpha_stall");


			String clAlphaLinearTraitProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_alpha_linear_trait");
			if (clAlphaLinearTraitProperty != null)
				clAlphaLinearTrait =  reader.getXMLAmountWithUnitByPath("//airfoil/aerodynamics/Cl_alpha_linear_trait");


			String clAtAlphaZeroProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_at_alpha_zero");
			if (clAtAlphaZeroProperty!= null)
				clAtAlphaZero = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_at_alpha_zero"));


			String clEndLinearTraitProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_end_linear_trait");
			if (clEndLinearTraitProperty!= null)
				clEndLinearTrait = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_end_linear_trait"));

			String clMaxProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_max");
			if (clMaxProperty!= null)
				clMax = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_max"));

		}
		if(externalCdCurveFlag == Boolean.FALSE) {
			
			String cDminProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cd_min");
			if (cDminProperty!= null)
				cDmin = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cd_min"));

			String clAtCdMinProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_at_Cdmin");
			if (clAtCdMinProperty!= null)
				clAtCdMin = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cl_at_Cdmin"));

			String kFactorDragPolarProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/K_factor_drag_polar");
			if (kFactorDragPolarProperty!= null)
				kFactorDragPolar = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/K_factor_drag_polar"));

			String laminarBucketSemiExtensionProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/laminar_bucket_semi_extension");
			if (laminarBucketSemiExtensionProperty!= null)
				laminarBucketSemiExtension = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/laminar_bucket_semi_extension"));
			
			String laminarBucketDepthProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/laminar_bucket_depth");
			if (laminarBucketDepthProperty!= null)
				laminarBucketDepth = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/laminar_bucket_depth"));
			
		}
		if(externalCmCurveFlag == Boolean.FALSE) {
			
			String cmAlphaQuarterChordProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cm_alpha_quarter_chord");
			if (cmAlphaQuarterChordProperty!= null)
				cmAlphaQuarterChord = reader.getXMLAmountWithUnitByPath("//airfoil/aerodynamics/Cm_alpha_quarter_chord");

			String cmACProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cm_ac");
			if (cmACProperty!= null)
				cmAC = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cm_ac"));

			String cmACAtStallProperty = reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cm_ac_at_stall");
			if (cmACAtStallProperty!= null)
				cmACAtStall = Double.valueOf(reader.getXMLPropertyByPath("//airfoil/aerodynamics/Cm_ac_at_stall"));

		}
		if(externalClCurveFlag == Boolean.TRUE) {
			
			List<String> clCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cl_curve");
			if(!clCurveProperty.isEmpty()) 
				clCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cl_curve"); 
			
			List<String> alphaForClCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/alpha_for_Cl_curve");
			if(!alphaForClCurveProperty.isEmpty()) 
				alphaForClCurve = reader.readArrayofAmountFromXML("//aerodynamics/airfoil_curves/alpha_for_Cl_curve");
			
		}
		else {
			alphaForClCurve.addAll(
						MyArrayUtils.convertDoubleArrayToListOfAmount(
								MyArrayUtils.linspace(
										-30,
										30,
										50
										),
								NonSI.DEGREE_ANGLE
								)
						);

			clCurve.addAll(
					AirfoilCalc.calculateClCurve(
							alphaForClCurve,
							clAtAlphaZero,
							clMax,
							alphaEndLinearTrait,
							alphaStall, 
							clAlphaLinearTrait
							)
					);
			}
		if(externalCdCurveFlag == Boolean.TRUE) {
			
			List<String> cdCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cd_curve");
			if(!cdCurveProperty.isEmpty()) 
				cdCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cd_curve");
			
			List<String> clForCdCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cl_for_Cd_curve");
			if(!clForCdCurveProperty.isEmpty()) 
				clForCdCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cl_for_Cd_curve");
		}
		else {
				clForCdCurve.addAll(
						MyArrayUtils.convertDoubleArrayToListDouble(
								MyArrayUtils.linspaceDouble(
										-3,
										3,
										50
										)
								)
						);

				cdCurve.addAll(
						AirfoilCalc.calculateCdvsClCurve(
								clForCdCurve, 
								cDmin, 
								clAtCdMin, 
								kFactorDragPolar,
								laminarBucketDepth,
								laminarBucketSemiExtension)
						);
		}
		if(externalCmCurveFlag == Boolean.TRUE) {
			
			List<String> cmCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cm_curve");
			if(!cmCurveProperty.isEmpty()) 
				cmCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cm_curve");
			
			List<String> clForCmCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cl_for_Cm_curve");
			if(!clForCmCurveProperty.isEmpty()) 
				clForCmCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cl_for_Cm_curve"); 
			
		}
		else {
				clForCmCurve.addAll(
						MyArrayUtils.convertDoubleArrayToListDouble(
								MyArrayUtils.linspaceDouble(
										-3,
										3,
										50
										)
								)
						);

				cmCurve.addAll(
						AirfoilCalc.calculateCmvsClCurve(
								clForCmCurve, 
								cmACAtStall,
								cmAlphaQuarterChord,
								clAlphaLinearTrait,
								cmACAtStall, 
								clEndLinearTrait, 
								clMax
								)
						);
			}
		
		String xACNormalizedProperty = reader.getXMLPropertyByPath("//aerodynamics/x_ac_normalized");
		if(xACNormalizedProperty != null)
			xACNormalized = Double.valueOf(reader.getXMLPropertyByPath("//aerodynamics/x_ac_normalized"));
		
		String machCriticalProperty = reader.getXMLPropertyByPath("//aerodynamics/mach_critical");
		if(machCriticalProperty != null)
			machCritical = Double.valueOf(reader.getXMLPropertyByPath("//aerodynamics/mach_critical"));
		
		String xTransitionUpperProperty = reader.getXMLPropertyByPath("//aerodynamics/x_transition_upper");
		if(xTransitionUpperProperty != null)
			xTransitionUpper = Double.valueOf(reader.getXMLPropertyByPath("//aerodynamics/x_transition_upper"));
		
		String xTransitionLowerProperty = reader.getXMLPropertyByPath("//aerodynamics/x_transition_lower");
		if(xTransitionLowerProperty != null)
			xTransitionLower = Double.valueOf(reader.getXMLPropertyByPath("//aerodynamics/x_transition_lower"));

		// create an Airfoil object with the Builder pattern
		Airfoil airfoil = new Airfoil(
				new IAirfoil.Builder()
				.setName(name)
				.setType(type)
				.setFamily(family)
				.setThicknessToChordRatio(thicknessRatio)
				.setRadiusLeadingEdgeNormalized(radiusLeadingEdgeNormalized)
				.setXCoordinates(xCoords)
				.setZCoordinates(zCoords)
				.setAlphaZeroLift(alphaZeroLift)
				.setAlphaEndLinearTrait(alphaEndLinearTrait)
				.setAlphaStall(alphaStall)
				.setClAlphaLinearTrait(clAlphaLinearTrait)
				.setClAtAlphaZero(clAtAlphaZero)
				.setClEndLinearTrait(clEndLinearTrait)
				.setClMax(clMax)
				.setClAtCdMin(clAtCdMin)
				.setCdMin(cDmin)
				.setKFactorDragPolar(kFactorDragPolar)
				.setLaminarBucketDepth(laminarBucketDepth)
				.setLaminarBucketSemiExtension(laminarBucketSemiExtension)
				.setCmAlphaQuarterChord(cmAlphaQuarterChord)
				.setCmAC(cmAC)
				.setCmACAtStall(cmACAtStall)
				.setXACNormalized(xACNormalized)
				.setCriticalMach(machCritical)
				.setXTransitionUpper(xTransitionUpper)
				.setXTransitionLower(xTransitionLower)
				.setClCurveFromFile(externalClCurveFlag)
				.setCdCurveFromFile(externalCdCurveFlag)
				.setCmCurveFromFile(externalCmCurveFlag)
				.addAllAlphaForClCurve(alphaForClCurve)
				.addAllClCurve(clCurve)
				.addAllClForCdCurve(clForCdCurve)
				.addAllCdCurve(cdCurve)
				.addAllClForCmCurve(clForCmCurve)
				.addAllCmCurve(cmCurve)
				.build()
				);
		
		if(airfoil.getClCurveFromFile() == true)
			AirfoilCalc.extractLiftCharacteristicsfromCurve(
					MyArrayUtils.convertListOfDoubleToDoubleArray(airfoil.getClCurve()),
					airfoil.getAlphaForClCurve(),
					airfoil);
		
		if(airfoil.getCdCurveFromFile() == true)
			AirfoilCalc.extractPolarCharacteristicsfromCurve(
					MyArrayUtils.convertListOfDoubleToDoubleArray(airfoil.getCdCurve()),
					airfoil.getClForCdCurve(),
					airfoil);

		if(airfoil.getCmCurveFromFile() == true)
			AirfoilCalc.extractMomentCharacteristicsfromCurve(
					MyArrayUtils.convertListOfDoubleToDoubleArray(airfoil.getCmCurve()),
					airfoil.getClForCmCurve(),
					airfoil);
		
		return airfoil;

	}

	
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tAirfoil\n")
				.append("\t-------------------------------------\n")
				.append("\tName: " + _theAirfoilInterface.getName() + "\n")
				.append("\tType: " + _theAirfoilInterface.getType() + "\n")
				.append("\tt/c = " + _theAirfoilInterface.getThicknessToChordRatio() + "\n")
				.append("\tr_le/c = " + _theAirfoilInterface.getRadiusLeadingEdgeNormalized() + "\n")
				.append("\tx coordinates = " + Arrays.toString(_theAirfoilInterface.getXCoordinates()) + "\n")
				.append("\tz coordinates = " + Arrays.toString(_theAirfoilInterface.getZCoordinates()) + "\n")
				.append("\talpha_0l = " + _theAirfoilInterface.getAlphaZeroLift().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\talpha_star = " + _theAirfoilInterface.getAlphaEndLinearTrait().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\talpha_stall = " + _theAirfoilInterface.getAlphaStall().to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tCl_alpha = " + _theAirfoilInterface.getClAlphaLinearTrait() + "\n")
				.append("\tCl0 = " + _theAirfoilInterface.getClAtAlphaZero() + "\n")
				.append("\tCl_star = " + _theAirfoilInterface.getClEndLinearTrait() + "\n")
				.append("\tCl_max = " + _theAirfoilInterface.getClMax() + "\n")
				.append("\tAlpha for Cl curve (deg) = " + _theAirfoilInterface.getAlphaForClCurve() + "\n")
				.append("\tCl curve (deg) = " + _theAirfoilInterface.getClCurve() + "\n")
				.append("\tCd_min = " + _theAirfoilInterface.getCdMin() + "\n")
				.append("\tCl @ Cd_min = " + _theAirfoilInterface.getClAtCdMin() + "\n")
				.append("\tk-factor (drag polar) = " + _theAirfoilInterface.getKFactorDragPolar() + "\n")
				.append("\tCl for Cd curve (deg) = " + _theAirfoilInterface.getClForCdCurve() + "\n")
				.append("\tCd curve (deg) = " + _theAirfoilInterface.getCdCurve() + "\n")
				.append("\tCm_alpha = " + _theAirfoilInterface.getCmAlphaQuarterChord() + "\n")
				.append("\tCm_ac = " + _theAirfoilInterface.getCmAC() + "\n")
				.append("\tCm_ac @ stall = " + _theAirfoilInterface.getCmACAtStall() + "\n")
				.append("\tCl for Cm curve (deg) = " + _theAirfoilInterface.getClForCmCurve() + "\n")
				.append("\tCm curve (deg) = " + _theAirfoilInterface.getCmCurve() + "\n")
				.append("\tx_ac/c = " + _theAirfoilInterface.getXACNormalized() + "\n")
				.append("\tM_cr = " + _theAirfoilInterface.getCriticalMach())
				.append("\tTransition point upper side = " + _theAirfoilInterface.getXTransitionUpper() + "\n")
				.append("\tTransition point lower side = " + _theAirfoilInterface.getXTransitionLower() + "\n")
				;
		return sb.toString();
	}

	//---------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public IAirfoil getTheAirfoilInterface() {
		return _theAirfoilInterface;
	}
	
	public void setTheAirfoilInterface (IAirfoil theAirfoilInterface) {
		this._theAirfoilInterface = theAirfoilInterface;
	}
	
	public List<Double> getClCurve() {
		return _theAirfoilInterface.getClCurve();
	}

	public void setClCurve(List<Double> _clCurve) {
		IAirfoil.Builder.from(_theAirfoilInterface).getClCurve().clear();
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).addAllClCurve(_clCurve).build());
	}

	public List<Double> getCdCurve() {
		return _theAirfoilInterface.getCdCurve();
	}

	public void setCdCurve(List<Double> _cdCurve) {
		IAirfoil.Builder.from(_theAirfoilInterface).getCdCurve().clear();
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).addAllCdCurve(_cdCurve).build());
	}

	public List<Double> getCmCurve() {
		return _theAirfoilInterface.getCmCurve();
	}

	public void setCmCurve(List<Double> _cmCurve) {
		IAirfoil.Builder.from(_theAirfoilInterface).getCmCurve().clear();
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).addAllCmCurve(_cmCurve).build());
	}

	public List<Amount<Angle>> getAlphaForClCurve() {
		return _theAirfoilInterface.getAlphaForClCurve();
	}

	public void setAlphaForClCurve(List<Amount<Angle>> _alphaForClCurve) {
		IAirfoil.Builder.from(_theAirfoilInterface).getAlphaForClCurve().clear();
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).addAllAlphaForClCurve(_alphaForClCurve).build());
	}

	public List<Double> getClForCdCurve() {
		return _theAirfoilInterface.getClForCdCurve();
	}

	public void setClForCdCurve(List<Double> _clForCdCurve) {
		IAirfoil.Builder.from(_theAirfoilInterface).getClForCdCurve().clear();
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).addAllClForCdCurve(_clForCdCurve).build());
	}

	public double getLaminarBucketSemiExtension() {
		return _theAirfoilInterface.getLaminarBucketSemiExtension();
	}

	public void setLaminarBucketSemiExtension(double _laminarBucketSemiExtension) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setLaminarBucketSemiExtension(_laminarBucketSemiExtension).build());
	}

	public double getLaminarBucketDepth() {
		return _theAirfoilInterface.getLaminarBucketDepth();
	}
	
	public void setLaminarBucketDepth(double _laminarBucketDepth) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setLaminarBucketDepth(_laminarBucketDepth).build());
	}

	public List<PVector> getCoordinatesRight() {
		return _coordinatesRight;
	}

	public void setCoordinatesRight(List<PVector> _coordinatesRight) {
		this._coordinatesRight = _coordinatesRight;
	}

	public List<PVector> getCoordinatesLeft() {
		return _coordinatesLeft;
	}

	public void setCoordinatesLeft(List<PVector> _coordinatesLeft) {
		this._coordinatesLeft = _coordinatesLeft;
	}

	public List<Double> getClForCmCurve() {
		return _theAirfoilInterface.getClForCmCurve();
	}

	public void setClForCmCurve(List<Double> _clForCmCurve) {
		IAirfoil.Builder.from(_theAirfoilInterface).getClForCmCurve().clear();
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).addAllClForCmCurve(_clForCmCurve).build());
	}

	public boolean getClCurveFromFile() {
		return _theAirfoilInterface.getClCurveFromFile();
	}

	public void setClCurveFromFile(boolean _clCurveFromFile) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setClCurveFromFile(_clCurveFromFile).build());
	}

	public boolean getCdCurveFromFile() {
		return _theAirfoilInterface.getCdCurveFromFile();
	}

	public void setCdCurveFromFile(boolean _cdCurveFromFile) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setCdCurveFromFile(_cdCurveFromFile).build());
	}

	public boolean getCmCurveFromFile() {
		return _theAirfoilInterface.getCmCurveFromFile();
	}

	public void setCmCurveFromFile(boolean _cmCurveFromFile) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setCmCurveFromFile(_cmCurveFromFile).build());
	}
	
	public String getName() {
		return _theAirfoilInterface.getName();
	}

	public void setName(String _name) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setName(_name).build());
	}

	public AirfoilTypeEnum getType() {
		return _theAirfoilInterface.getType();
	}

	public void setType(AirfoilTypeEnum type) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setType(type).build());
	}

	public AirfoilFamilyEnum getFamily() {
		return _theAirfoilInterface.getFamily();
	}

	public void setFamily(AirfoilFamilyEnum fam) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setFamily(fam).build());
	}

	public double getThicknessToChordRatio() {
		return _theAirfoilInterface.getThicknessToChordRatio();
	}

	public void setThicknessToChordRatio(double tOverC) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setThicknessToChordRatio(tOverC).build());
	}

	public double getRadiusLeadingEdge() {
		return _theAirfoilInterface.getRadiusLeadingEdgeNormalized();
	}

	public void setRadiusLeadingEdge(double rLEOverC) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setRadiusLeadingEdgeNormalized(rLEOverC).build());
	}

	public double[] getXCoords() {
		return _theAirfoilInterface.getXCoordinates();
	}

	public void setXCoords(double[] xCoords) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setXCoordinates(xCoords).build());
	}
	
	public double[] getZCoords() {
		return _theAirfoilInterface.getZCoordinates();
	}

	public void setZCoords(double[] zCoords) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setZCoordinates(zCoords).build());
	}
	
	public Amount<Angle> getAlphaZeroLift() {
		return _theAirfoilInterface.getAlphaZeroLift();
	}

	public void setAlphaZeroLift(Amount<Angle> alpha0l) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setAlphaZeroLift(alpha0l).build());
	}

	public Amount<Angle> getAlphaEndLinearTrait() {
		return _theAirfoilInterface.getAlphaEndLinearTrait();
	}

	public void setAlphaLinearTrait(Amount<Angle> alphaStar) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setAlphaEndLinearTrait(alphaStar).build());
	}

	public Amount<Angle> getAlphaStall() {
		return _theAirfoilInterface.getAlphaStall();
	}

	public void setAlphaStall(Amount<Angle> alphaStall) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setAlphaStall(alphaStall).build());
	}

	public Amount<?> getClAlphaLinearTrait() {
		return _theAirfoilInterface.getClAlphaLinearTrait();
	}

	public void setClAlphaLinearTrait(Amount<?> clApha) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setClAlphaLinearTrait(clApha).build());
	}

	public double getCdMin() {
		return _theAirfoilInterface.getCdMin();
	}

	public void setCdMin(double cdMin) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setCdMin(cdMin).build());
	}

	public double getClAtCdMin() {
		return _theAirfoilInterface.getClAtCdMin();
	}

	public void setClAtCdMin(double clAtCdMin) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setClAtCdMin(clAtCdMin).build());
	}

	public double getClAtAlphaZero() {
		return _theAirfoilInterface.getClAtAlphaZero();
	}

	public void setClAtAlphaZero(double clAtAlphaZero) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setClAtAlphaZero(clAtAlphaZero).build());
	}

	public double getClEndLinearTrait() {
		return _theAirfoilInterface.getClEndLinearTrait();
	}

	public void setClEndLinearTrait(double clEndLinearTrait) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setClEndLinearTrait(clEndLinearTrait).build());
	}

	public double getClMax() {
		return _theAirfoilInterface.getClMax();
	}

	public void setClMax(double clMax) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setClMax(clMax).build());
	}

	public double getKFactorDragPolar() {
		return _theAirfoilInterface.getKFactorDragPolar();
	}

	public void setKFactorDragPolar(double kFactorDragPolar) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setKFactorDragPolar(kFactorDragPolar).build());
	}

	public Amount<?> getCmAlphaQuarterChord() {
		return _theAirfoilInterface.getCmAlphaQuarterChord();
	}

	public void setCmAlphaQuarterChord(Amount<?> cmAlphaQuarterChord) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setCmAlphaQuarterChord(cmAlphaQuarterChord).build());
	}

	public double getXACNormalized() {
		return _theAirfoilInterface.getXACNormalized();
	}

	public void setXACNormalized(double xACAdimensional) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setXACNormalized(xACAdimensional).build());
	}

	public double getCmAC() {
		return _theAirfoilInterface.getCmAC();
	}

	public void setCmAC(double cmAC) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setCmAC(cmAC).build());
	}

	public double getCmACAtStall() {
		return _theAirfoilInterface.getCmACAtStall();
	}

	public void setCmACAtStall(double cmACAtStall) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setCmACAtStall(cmACAtStall).build());
	}

	public double getMachCritical() {
		return _theAirfoilInterface.getCriticalMach();
	}

	public void setMachCritical(double machCritical) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setCriticalMach(machCritical).build());
	}

	public double getXTransitionUpper() {
		return _theAirfoilInterface.getXTransitionUpper();
	}

	public void setXTransitionUpper(double _xTransitionUpper) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setXTransitionUpper(_xTransitionUpper).build());
	}

	public double getXTransitionLower() {
		return _theAirfoilInterface.getXTransitionLower();
	}

	public void setXTransitionLower(double _xTransitionLower) {
		setTheAirfoilInterface(IAirfoil.Builder.from(_theAirfoilInterface).setXTransitionLower(_xTransitionLower).build());
	}
}
