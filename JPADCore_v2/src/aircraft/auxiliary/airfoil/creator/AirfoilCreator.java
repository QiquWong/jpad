package aircraft.auxiliary.airfoil.creator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
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

public class AirfoilCreator implements IAirfoilCreator {

	private String _name;
	private AirfoilTypeEnum _type;
	private AirfoilFamilyEnum _family;
	private Double[] _xCoords;
	private Double[] _zCoords;
	private Double _thicknessToChordRatio;
	private Amount<Length> _radiusLeadingEdge;
	private Amount<Angle> _alphaZeroLift;
	private Amount<Angle> _alphaEndLinearTrait;
	private Amount<Angle> _alphaStall;
	private Amount<?> _clAlphaLinearTrait;
	private Amount<?> _cmAlphaQuarterChord;
	private Double _cdMin;
	private Double _clAtCdMin;
	private Double _clAtAlphaZero;
	private Double _clEndLinearTrait;
	private Double _clMax;
	private Double _kFactorDragPolar;
	private Double _laminarBucketSemiExtension;
	private Double _laminarBucketDepth;
	private Double _xACNormalized;
	private Double _cmAC;
	private Double _cmACAtStall;
	private Double _machCritical;
	private Double _xTransitionUpper;
	private Double _xTransitionLower;

	private Boolean _clCurveFromFile;
	private Boolean _cdCurveFromFile;
	private Boolean _cmCurveFromFile;
	
	private List<Double> _clCurve;
	private List<Double> _cdCurve;
	private List<Double> _cmCurve;
	private List<Amount<Angle>> _alphaForClCurve;
	private List<Double> _clForCdCurve;
	private List<Double> _clForCmCurve;
	
	private List<PVector> _coordinatesRight = new ArrayList<PVector>();
	private List<PVector> _coordinatesLeft = new ArrayList<PVector>();
	
	public static class AirfoilBuilder {

		// required parameters
		private String __name;
		private AirfoilTypeEnum __type;
		private AirfoilFamilyEnum __family;

		// optional, set to default values
		private Double __thicknessToChordRatio;
		private Amount<Length> __radiusLeadingEdge;
		private Double[] __xCoords;
		private Double[] __zCoords;

		private Amount<Angle> __alphaZeroLift;
		private Amount<Angle> __alphaEndLinearTrait;
		private Amount<Angle> __alphaStall;
		private Amount<?> __clAlphaLinearTrait;
		private Amount<?> __cmAlphaQuarterChord;
		private Double __cdMin;
		private Double __clAtCdMin;
		private Double __clAtAlphaZero;
		private Double __clEndLinearTrait;
		private Double __clMax;
		private Double __kFactorDragPolar;
		private Double __laminarBucketSemiExtension;
		private Double __laminarBucketDepth;
		private Double __xACNormalized;
		private Double __cmAC;
		private Double __cmACAtStall;
		private Double __machCritical;
		private Double __xTransitionUpper;
		private Double __xTransitionLower;
		
		private Boolean __clCurveFromFile;
		private Boolean __cdCurveFromFile;
		private Boolean __cmCurveFromFile;
		
		private List<Double> __clCurve = new ArrayList<>();
		private List<Double> __cdCurve = new ArrayList<>();
		private List<Double> __cmCurve = new ArrayList<>();
		private List<Amount<Angle>> __alphaForClCurve = new ArrayList<>();
		private List<Double> __clForCdCurve = new ArrayList<>();
		private List<Double> __clForCmCurve = new ArrayList<>();
		

		public AirfoilBuilder name (String name) {
			__name = name;
			return this;
		}
		
		public AirfoilBuilder type(AirfoilTypeEnum type) {
			__type = type;
			return this;
		}

		public AirfoilBuilder family(AirfoilFamilyEnum fam) {
			__family = fam;
			return this;
		}

		public AirfoilBuilder thicknessToChordRatio(Double tOverC) {
			__thicknessToChordRatio = tOverC;
			return this;
		}

		public AirfoilBuilder radiusLeadingEdge(Amount<Length> rLE) {
			__radiusLeadingEdge = rLE;
			return this;
		}

		public AirfoilBuilder xCoords(Double[] xCoords) {
			__xCoords = xCoords;
			return this;
		}
		
		public AirfoilBuilder zCoords(Double[] zCoords) {
			__zCoords = zCoords;
			return this;
		}
		
		public AirfoilBuilder alphaZeroLift(Amount<Angle> alpha0l) {
			__alphaZeroLift = alpha0l;
			return this;
		}

		public AirfoilBuilder alphaEndLinearTrait(Amount<Angle> alphaStar) {
			__alphaEndLinearTrait = alphaStar;
			return this;
		}

		public AirfoilBuilder alphaStall(Amount<Angle> alphaStall) {
			__alphaStall = alphaStall;
			return this;
		}

		public AirfoilBuilder clAlphaLinearTrait(Amount<?> clApha) {
			__clAlphaLinearTrait = clApha;
			return this;
		}

		public AirfoilBuilder cdMin(Double cdMin) {
			__cdMin = cdMin;
			return this;
		}

		public AirfoilBuilder clAtCdMin(Double clAtCdMin) {
			__clAtCdMin = clAtCdMin;
			return this;
		}

		public AirfoilBuilder clAtAlphaZero(Double clAtAlphaZero) {
			__clAtAlphaZero = clAtAlphaZero;
			return this;
		}

		public AirfoilBuilder clEndLinearTrait(Double clEndLinearTrait) {
			__clEndLinearTrait = clEndLinearTrait;
			return this;
		}

		public AirfoilBuilder clMax(Double clMax) {
			__clMax = clMax;
			return this;
		}

		public AirfoilBuilder kFactorDragPolar(Double kFactorDragPolar) {
			__kFactorDragPolar = kFactorDragPolar;
			return this;
		}

		public AirfoilBuilder laminarBucketSemiExtension(Double laminarBucketSemiExtension) {
			__laminarBucketSemiExtension = laminarBucketSemiExtension;
			return this;
		}
		
		public AirfoilBuilder laminarBucketDepth(Double laminarBucketDepth) {
			__laminarBucketDepth = laminarBucketDepth;
			return this;
		}
		
		public AirfoilBuilder cmAlphaQuarterChord(Amount<?> cmAlphaQuarterChord) {
			__cmAlphaQuarterChord = cmAlphaQuarterChord;
			return this;
		}

		public AirfoilBuilder xACNormalized(Double xACNormalized) {
			__xACNormalized = xACNormalized;
			return this;
		}

		public AirfoilBuilder cmAC(Double cmAC) {
			__cmAC = cmAC;
			return this;
		}

		public AirfoilBuilder cmACAtStall(Double cmACAtStall) {
			__cmACAtStall = cmACAtStall;
			return this;
		}

		public AirfoilBuilder machCritical(Double machCritical) {
			__machCritical = machCritical;
			return this;
		}

		public AirfoilBuilder xTransitionUpper(Double xTransitionUpper) {
			__xTransitionUpper = xTransitionUpper;
			return this;
		}
		
		public AirfoilBuilder xTransitionLower(Double xTransitionLower) {
			__xTransitionLower = xTransitionLower;
			return this;
		}
		
		public AirfoilBuilder clCurve(List<Double> clCurve) {
			__clCurve = clCurve;
			return this;
		}
		
		public AirfoilBuilder cdCurve(List<Double> cdCurve) {
			__cdCurve = cdCurve;
			return this;
		}
		
		public AirfoilBuilder cmCurve(List<Double> cmCurve) {
			__cmCurve = cmCurve;
			return this;
		}
		
		public AirfoilBuilder clCurveFromFile(Boolean clCurveFromFile) {
			__clCurveFromFile = clCurveFromFile;
			return this;
		}
		
		public AirfoilBuilder cdCurveFromFile(Boolean cdCurveFromFile) {
			__cdCurveFromFile = cdCurveFromFile;
			return this;
		}
		
		public AirfoilBuilder cmCurveFromFile(Boolean cmCurveFromFile) {
			__cmCurveFromFile = cmCurveFromFile;
			return this;
		}
		
		public AirfoilBuilder alphaForClCurve(List<Amount<Angle>> alphaForClCurve) {
			__alphaForClCurve = alphaForClCurve;
			return this;
		}
		
		public AirfoilBuilder clForCdCurve(List<Double> clForCdCurve) {
			__clForCdCurve = clForCdCurve;
			return this;
		}
		
		public AirfoilBuilder clForCmCurve(List<Double> clForCmCurve) {
			__clForCmCurve = clForCmCurve;
			return this;
		}
		
		public AirfoilCreator build() {
			return new AirfoilCreator(this);
		}
	}
	
	private AirfoilCreator(AirfoilBuilder builder) {
		_name = builder.__name;
		_type = builder.__type;
		_family = builder.__family;
		_thicknessToChordRatio = builder.__thicknessToChordRatio;
		_radiusLeadingEdge = builder.__radiusLeadingEdge;
		_xCoords = builder.__xCoords;
		_zCoords = builder.__zCoords;
		_alphaZeroLift = builder.__alphaZeroLift;
		_alphaEndLinearTrait = builder.__alphaEndLinearTrait;
		_alphaStall = builder.__alphaStall;
		_clAlphaLinearTrait = builder.__clAlphaLinearTrait;
		_cdMin = builder.__cdMin;
		_clAtCdMin = builder.__clAtCdMin;
		_clAtAlphaZero = builder.__clAtAlphaZero;
		_clEndLinearTrait = builder.__clEndLinearTrait;
		_clMax = builder.__clMax;
		_kFactorDragPolar = builder.__kFactorDragPolar;
		_laminarBucketSemiExtension = builder.__laminarBucketSemiExtension;
		_laminarBucketDepth = builder.__laminarBucketDepth;
		_cmAlphaQuarterChord = builder.__cmAlphaQuarterChord;
		_xACNormalized = builder.__xACNormalized;
		_cmAC = builder.__cmAC;
		_cmACAtStall = builder.__cmACAtStall;
		_machCritical = builder.__machCritical;
		_xTransitionUpper = builder.__xTransitionUpper;
		_xTransitionLower = builder.__xTransitionLower;
		
		_clCurveFromFile = builder.__clCurveFromFile;
		_cdCurveFromFile = builder.__cdCurveFromFile;
		_cmCurveFromFile = builder.__cmCurveFromFile;
		
		_clCurve = builder.__clCurve;
		_cdCurve = builder.__cdCurve;
		_cmCurve = builder.__cmCurve;
		_alphaForClCurve = builder.__alphaForClCurve;
		_clForCdCurve = builder.__clForCdCurve;
		_clForCmCurve = builder.__clForCmCurve;
		
		_coordinatesLeft = new ArrayList<>();
		_coordinatesRight = new ArrayList<>();
		
	}

	public static AirfoilCreator importFromXML(String pathToXML) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading airfoil data ...");

		Boolean externalClCurveFlag;
		String externalClCurveProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@external_cl_curve");
		if(externalClCurveProperty.equalsIgnoreCase("true"))
			externalClCurveFlag = Boolean.TRUE;
		else
			externalClCurveFlag = Boolean.FALSE;
		
		Boolean externalCdCurveFlag;
		String externalCdCurveProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@external_cd_curve");
		if(externalCdCurveProperty.equalsIgnoreCase("true"))
			externalCdCurveFlag = Boolean.TRUE;
		else
			externalCdCurveFlag = Boolean.FALSE;
		
		Boolean externalCmCurveFlag;
		String externalCmCurveProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@external_cm_curve");
		if(externalCmCurveProperty.equalsIgnoreCase("true"))
			externalCmCurveFlag = Boolean.TRUE;
		else
			externalCmCurveFlag = Boolean.FALSE;
		
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
		
		Double thicknessRatio = null;
		Amount<Length> radiusLeadingEdge = null;
		Double[] xCoords = null;
		Double[] zCoords = null;
		Amount<Angle> alphaZeroLift = null;
		Amount<Angle> alphaEndLinearTrait = null;
		Amount<Angle> alphaStall = null;
		Amount<?> clAlphaLinearTrait = null;
		Double clAtAlphaZero = null;
		Double clEndLinearTrait = null;
		Double clMax = null;
		Double cDmin = null;
		Double clAtCdMin = null;
		Double kFactorDragPolar = null;
		Double laminarBucketSemiExtension = 0.0;
		Double laminarBucketDepth = 0.0;
		Amount<?> cmAlphaQuarterChord = null;
		Double cmAC = null;
		Double cmACAtStall = null;
		List<Double> clCurve = new ArrayList<>();
		List<Double> cdCurve = new ArrayList<>();
		List<Double> cmCurve = new ArrayList<>();
		List<Amount<Angle>> alphaForClCurve = new ArrayList<>();
		List<Double> clForCdCurve = new ArrayList<>();
		List<Double> clForCmCurve = new ArrayList<>();
		Double xACNormalized = null;
		Double machCritical = null;
		Double xTransitionUpper = null;
		Double xTransitionLower = null;
		
		String thicknessRatioProperty = reader.getXMLPropertyByPath("//geometry/thickness_to_chord_ratio_max");
		if(thicknessRatioProperty != null)
			thicknessRatio = Double.valueOf(reader.getXMLPropertyByPath("//geometry/thickness_to_chord_ratio_max"));
		
		String radiusLeadingEdgeProperty = reader.getXMLPropertyByPath("//geometry/radius_leading_edge_normalized");
		if(radiusLeadingEdgeProperty != null)
			radiusLeadingEdge = reader.getXMLAmountLengthByPath("//airfoil/geometry/radius_leading_edge_normalized");
		
		List<String> xCoordsProperty = reader.getXMLPropertiesByPath("//geometry/x_coordinates");
		if(!xCoordsProperty.isEmpty()) 
			xCoords = MyArrayUtils.convertListOfDoubleToDoubleArray(
					JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/x_coordinates").get(0)).stream()
					.map(x -> Double.valueOf(x))
					.collect(Collectors.toList())
					);
			
		List<String> zCoordsProperty = reader.getXMLPropertiesByPath("//geometry/z_coordinates");
		if(!zCoordsProperty.isEmpty()) 
			zCoords = MyArrayUtils.convertListOfDoubleToDoubleArray(
					JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/z_coordinates").get(0)).stream()
					.map(x -> Double.valueOf(x))
					.collect(Collectors.toList())
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
		if(externalCdCurveFlag == Boolean.TRUE) {
			
			List<String> cdCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cd_curve");
			if(!cdCurveProperty.isEmpty()) 
				cdCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cd_curve");
			
			List<String> clForCdCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cl_for_Cd_curve");
			if(!clForCdCurveProperty.isEmpty()) 
				clForCdCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cl_for_Cd_curve");
		}
		if(externalCmCurveFlag == Boolean.TRUE) {
			
			List<String> cmCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cm_curve");
			if(!cmCurveProperty.isEmpty()) 
				cmCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cm_curve");
			
			List<String> clForCmCurveProperty = reader.getXMLPropertiesByPath("//aerodynamics/airfoil_curves/Cl_for_Cm_curve");
			if(!clForCmCurveProperty.isEmpty()) 
				clForCmCurve = reader.readArrayDoubleFromXML("//aerodynamics/airfoil_curves/Cl_for_Cm_curve"); 
			
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
		AirfoilCreator airfoil = new AirfoilBuilder()
				.name(name)
				.type(type)
				.family(family)
				.thicknessToChordRatio(thicknessRatio)
				.radiusLeadingEdge(radiusLeadingEdge)
				.xCoords(xCoords)
				.zCoords(zCoords)
				.alphaZeroLift(alphaZeroLift)
				.alphaEndLinearTrait(alphaEndLinearTrait)
				.alphaStall(alphaStall)
				.clAlphaLinearTrait(clAlphaLinearTrait)
				.cdMin(cDmin)
				.clAtCdMin(clAtCdMin)
				.clAtAlphaZero(clAtAlphaZero)
				.clEndLinearTrait(clEndLinearTrait)
				.clMax(clMax)
				.kFactorDragPolar(kFactorDragPolar)
				.laminarBucketSemiExtension(laminarBucketSemiExtension)
				.laminarBucketDepth(laminarBucketDepth)
				.cmAlphaQuarterChord(cmAlphaQuarterChord)
				.xACNormalized(xACNormalized)
				.cmAC(cmAC)
				.cmACAtStall(cmACAtStall)
				.machCritical(machCritical)
				.xTransitionUpper(xTransitionUpper)
				.xTransitionLower(xTransitionLower)
				.clCurveFromFile(externalClCurveFlag)
				.cdCurveFromFile(externalCdCurveFlag)
				.cmCurveFromFile(externalCmCurveFlag)
				.clCurve(clCurve)
				.cdCurve(cdCurve)
				.cmCurve(cmCurve)
				.alphaForClCurve(alphaForClCurve)
				.clForCdCurve(clForCdCurve)
				.clForCmCurve(clForCmCurve)
				.build();

		return airfoil;

	}

	
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tAirfoil\n")
				.append("\t-------------------------------------\n")
				.append("\tName: " + _name + "\n")
				.append("\tType: " + _type + "\n")
				.append("\tt/c = " + _thicknessToChordRatio + "\n")
				.append("\tr_le/c = " + _radiusLeadingEdge + "\n")
				.append("\tx coordinates = " + Arrays.toString(_xCoords) + "\n")
				.append("\tz coordinates = " + Arrays.toString(_zCoords) + "\n")
				.append("\talpha_0l = " + _alphaZeroLift.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\talpha_star = " + _alphaEndLinearTrait.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\talpha_stall = " + _alphaStall.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tCl_alpha = " + _clAlphaLinearTrait + "\n")
				.append("\tCl0 = " + _clAtAlphaZero + "\n")
				.append("\tCl_star = " + _clEndLinearTrait + "\n")
				.append("\tCl_max = " + _clMax + "\n")
				.append("\tAlpha for Cl curve (deg) = " + _alphaForClCurve + "\n")
				.append("\tCl curve (deg) = " + _clCurve + "\n")
				.append("\tCd_min = " + _cdMin + "\n")
				.append("\tCl @ Cd_min = " + _clAtCdMin + "\n")
				.append("\tk-factor (drag polar) = " + _kFactorDragPolar + "\n")
				.append("\tCl for Cd curve (deg) = " + _clForCdCurve + "\n")
				.append("\tCd curve (deg) = " + _cdCurve + "\n")
				.append("\tCm_alpha = " + _cmAlphaQuarterChord + "\n")
				.append("\tCm_ac = " + _cmAC + "\n")
				.append("\tCm_ac @ stall = " + _cmACAtStall + "\n")
				.append("\tCl for Cm curve (deg) = " + _clForCmCurve + "\n")
				.append("\tCm curve (deg) = " + _cmCurve + "\n")
				.append("\tx_ac/c = " + _xACNormalized + "\n")
				.append("\tM_cr = " + _machCritical)
				.append("\tTransition point upper side = " + _xTransitionUpper + "\n")
				.append("\tTransition point lower side = " + _xTransitionLower + "\n")
				;
		return sb.toString();
	}

	public List<Double> getClCurve() {
		return _clCurve;
	}

	public void setClCurve(List<Double> _clCurve) {
		this._clCurve = _clCurve;
	}

	public List<Double> getCdCurve() {
		return _cdCurve;
	}

	public void setCdCurve(List<Double> _cdCurve) {
		this._cdCurve = _cdCurve;
	}

	public List<Double> getCmCurve() {
		return _cmCurve;
	}

	public void setCmCurve(List<Double> _cmCurve) {
		this._cmCurve = _cmCurve;
	}

	public List<Amount<Angle>> getAlphaForClCurve() {
		return _alphaForClCurve;
	}

	public void setAlphaForClCurve(List<Amount<Angle>> _alphaForClCurve) {
		this._alphaForClCurve = _alphaForClCurve;
	}

	public List<Double> getClForCdCurve() {
		return _clForCdCurve;
	}

	public void setClForCdCurve(List<Double> _clForCdCurve) {
		this._clForCdCurve = _clForCdCurve;
	}

	public Double getLaminarBucketSemiExtension() {
		return _laminarBucketSemiExtension;
	}

	public Double getLaminarBucketDepth() {
		return _laminarBucketDepth;
	}

	public void setLaminarBucketSemiExtension(Double _laminarBucketSemiExtension) {
		this._laminarBucketSemiExtension = _laminarBucketSemiExtension;
	}

	public void setLaminarBucketDepth(Double _laminarBucketDepth) {
		this._laminarBucketDepth = _laminarBucketDepth;
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
		return _clForCmCurve;
	}

	public void setClForCmCurve(List<Double> _clForCmCurve) {
		this._clForCmCurve = _clForCmCurve;
	}

	public Boolean getClCurveFromFile() {
		return _clCurveFromFile;
	}

	public void setClCurveFromFile(Boolean _clCurveFromFile) {
		this._clCurveFromFile = _clCurveFromFile;
	}

	public Boolean getCdCurveFromFile() {
		return _cdCurveFromFile;
	}

	public void setCdCurveFromFile(Boolean _cdCurveFromFile) {
		this._cdCurveFromFile = _cdCurveFromFile;
	}

	public Boolean getCmCurveFromFile() {
		return _cmCurveFromFile;
	}

	public void setCmCurveFromFile(Boolean _cmCurveFromFile) {
		this._cmCurveFromFile = _cmCurveFromFile;
	}
	
	/**
	 * @return the _name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param _name the _name to set
	 */
	public void setName(String _name) {
		this._name = _name;
	}

	@Override
	public AirfoilTypeEnum getType() {
		return _type;
	}

	@Override
	public void setType(AirfoilTypeEnum type) {
		_type = type;
	}

	@Override
	public AirfoilFamilyEnum getFamily() {
		return _family;
	}

	@Override
	public void setFamily(AirfoilFamilyEnum fam) {
		_family = fam;
	}

	@Override
	public Double getThicknessToChordRatio() {
		return _thicknessToChordRatio;
	}

	@Override
	public void setThicknessToChordRatio(Double tOverC) {
		_thicknessToChordRatio = tOverC;
	}

	@Override
	public Amount<Length> getRadiusLeadingEdge() {
		return _radiusLeadingEdge;
	}

	@Override
	public void setRadiusLeadingEdge(Amount<Length> rLEOverC) {
		_radiusLeadingEdge = rLEOverC;
	}

	@Override
	public Double[] getXCoords() {
		return _xCoords;
	}

	@Override
	public void setXCoords(Double[] xCoords) {
		this._xCoords = xCoords;
	}
	
	@Override
	public Double[] getZCoords() {
		return _zCoords;
	}

	@Override
	public void setZCoords(Double[] zCoords) {
		this._zCoords = zCoords;
	}
	
	@Override
	public Amount<Angle> getAlphaZeroLift() {
		return _alphaZeroLift;
	}

	@Override
	public void setAlphaZeroLift(Amount<Angle> alpha0l) {
		_alphaZeroLift = alpha0l;
	}

	@Override
	public Amount<Angle> getAlphaEndLinearTrait() {
		return _alphaEndLinearTrait;
	}

	@Override
	public void setAlphaLinearTrait(Amount<Angle> alphaStar) {
		_alphaEndLinearTrait = alphaStar;
	}

	@Override
	public Amount<Angle> getAlphaStall() {
		return _alphaStall;
	}

	@Override
	public void setAlphaStall(Amount<Angle> alphaStall) {
		_alphaStall = alphaStall;
	}

	@Override
	public Amount<?> getClAlphaLinearTrait() {
		return _clAlphaLinearTrait;
	}

	@Override
	public void setClAlphaLinearTrait(Amount<?> clApha) {
		_clAlphaLinearTrait = clApha;
	}

	@Override
	public Double getCdMin() {
		return _cdMin;
	}

	@Override
	public void setCdMin(Double cdMin) {
		_cdMin = cdMin;
	}

	@Override
	public Double getClAtCdMin() {
		return _clAtCdMin;
	}

	@Override
	public void setClAtCdMin(Double clAtCdMin) {
		_clAtCdMin = clAtCdMin;
	}

	@Override
	public Double getClAtAlphaZero() {
		return _clAtAlphaZero;
	}

	@Override
	public void setClAtAlphaZero(Double clAtAlphaZero) {
		_clAtAlphaZero = clAtAlphaZero;
	}

	@Override
	public Double getClEndLinearTrait() {
		return _clEndLinearTrait;
	}

	@Override
	public void setClEndLinearTrait(Double clEndLinearTrait) {
		_clEndLinearTrait = clEndLinearTrait;
	}

	@Override
	public Double getClMax() {
		return _clMax;
	}

	@Override
	public void setClMax(Double clMax) {
		_clMax = clMax;
	}

	@Override
	public Double getKFactorDragPolar() {
		return _kFactorDragPolar;
	}

	@Override
	public void setKFactorDragPolar(Double kFactorDragPolar) {
		_kFactorDragPolar = kFactorDragPolar;
	}

	@Override
	public Amount<?> getCmAlphaQuarterChord() {
		return _cmAlphaQuarterChord;
	}

	@Override
	public void setCmAlphaQuarterChord(Amount<?> cmAlphaQuarterChord) {
		_cmAlphaQuarterChord = cmAlphaQuarterChord;
	}

	@Override
	public Double getXACNormalized() {
		return _xACNormalized;
	}

	@Override
	public void setXACNormalized(Double xACAdimensional) {
		_xACNormalized = xACAdimensional;
	}

	@Override
	public Double getCmAC() {
		return _cmAC;
	}

	@Override
	public void setCmAC(Double cmAC) {
		_cmAC = cmAC;
	}

	@Override
	public Double getCmACAtStall() {
		return _cmACAtStall;
	}

	@Override
	public void setCmACAtStall(Double cmACAtStall) {
		_cmACAtStall = cmACAtStall;
	}

	@Override
	public Double getMachCritical() {
		return _machCritical;
	}

	@Override
	public void setMachCritical(Double machCritical) {
		_machCritical = machCritical;
	}

	@Override
	public Double getXTransitionUpper() {
		return _xTransitionUpper;
	}

	@Override
	public void setXTransitionUpper(Double _xTransitionUpper) {
		this._xTransitionUpper = _xTransitionUpper;
	}

	@Override
	public Double getXTransitionLower() {
		return _xTransitionLower;
	}

	@Override
	public void setXTransitionLower(Double _xTransitionLower) {
		this._xTransitionLower = _xTransitionLower;
	}
}
