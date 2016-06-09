package aircraft.auxiliary.airfoil.creator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class AirfoilCreator implements IAirfoilCreator {

	private String _id;
	private AirfoilTypeEnum _type;
	private AirfoilEnum _family;
	private RealMatrix _NormalizedCornerPointsXZ;
	private Double[] _xCoords;
	private Double[] _zCoords;
	private Amount<Length> _chord;
	private Double _thicknessToChordRatio;
	private Double _camberRatio;
	private Double _radiusLeadingEdgeNormalized;
	private Amount<Angle> _angleAtTrailingEdge;
	private Amount<Angle> _alphaZeroLift;
	private Amount<Angle> _alphaEndLinearTrait;
	private Amount<Angle> _alphaStall;
	private Double _clAlphaLinearTrait;
	private Double _cdMin;
	private Double _clAtCdMin;
	private Double _clAtAlphaZero;
	private Double _clEndLinearTrait;
	private Double _clMax;
	private Double _kFactorDragPolar;
	private Double _mExponentDragPolar;
	private Double _cmAlphaQuarterChord;
	private Double _xACNormalized;
	private Double _cmAC;
	private Double _cmACAtStall;
	private Double _machCritical;

	@Override
	public String getID() {
		return _id;
	}

	@Override
	public void setID(String id) {
		_id = id;
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
	public AirfoilEnum getFamily() {
		return _family;
	}

	@Override
	public void setFamily(AirfoilEnum fam) {
		_family = fam;
	}

	@Override
	public double[][] getNormalizedCornerPointsXZ() {
		return _NormalizedCornerPointsXZ.getData();
	}

	@Override
	public void setNormalizedCornerPointsXZ(double[][] xz) {
		_NormalizedCornerPointsXZ = MatrixUtils.createRealMatrix(xz);
	}

	@Override
	public Amount<Length> getChord() {
		return _chord;
	}

	@Override
	public void setChord(Amount<Length> c) {
		_chord = c;
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
	public Double getCamberRatio() {
		return _camberRatio;
	}

	@Override
	public void setCamberRatio(Double fOverC) {
		_camberRatio = fOverC;
	}

	@Override
	public Double getRadiusLeadingEdgeNormalized() {
		return _radiusLeadingEdgeNormalized;
	}

	@Override
	public void setRadiusLeadingEdgeNormalized(Double rLEOverC) {
		_radiusLeadingEdgeNormalized = rLEOverC;
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
		return _xCoords;
	}

	@Override
	public void setZCoords(Double[] zCoords) {
		this._zCoords = zCoords;
	}
	
	@Override
	public Amount<Angle> getAngleAtTrailingEdge() {
		return _angleAtTrailingEdge;
	}

	@Override
	public void setAngleAtTrailingEdge(Amount<Angle> phiTE) {
		_angleAtTrailingEdge = phiTE;
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
	public Double getClAlphaLinearTrait() {
		return _clAlphaLinearTrait;
	}

	@Override
	public void setClAlphaLinearTrait(Double clApha) {
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
	public Double getMExponentDragPolar() {
		return _mExponentDragPolar;
	}

	@Override
	public void setMExponentDragPolar(Double mExponentDragPolar) {
		_mExponentDragPolar = mExponentDragPolar;
	}

	@Override
	public Double getCmAlphaQuarterChord() {
		return _cmAlphaQuarterChord;
	}

	@Override
	public void setCmAlphaQuarterChord(Double cmAlphaQuarterChord) {
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

	// Builder pattern via a nested public static class

	public static class AirfoilBuilder {

		// required parameters
		private String __id;

		// optional, set to default values
		private AirfoilTypeEnum __type = AirfoilTypeEnum.CONVENTIONAL;
		private AirfoilEnum __family = AirfoilEnum.NACA65_209;

		private RealMatrix __NormalizedCornerPointsXZ = MatrixUtils.createRealMatrix(30, 2);
		private Amount<Length> __chord = Amount.valueOf(1.0,1e-8,SI.METER);
		private Double __thicknessToChordRatio = 0.12;
		private Double __camberRatio = 0.9;
		private Double __radiusLeadingEdgeNormalized = 0.015;
		private Double[] __xCoords;
		private Double[] __zCoords;
		private Amount<Angle> __angleAtTrailingEdge = Amount.valueOf(4.0,1e-8,NonSI.DEGREE_ANGLE);

		private Amount<Angle> __alphaZeroLift = Amount.valueOf(-1.5,1e-8,NonSI.DEGREE_ANGLE);
		private Amount<Angle> __alphaEndLinearTrait = Amount.valueOf(9.0,1e-8,NonSI.DEGREE_ANGLE);;
		private Amount<Angle> __alphaStall = Amount.valueOf(12.0,1e-8,NonSI.DEGREE_ANGLE);;
		private Double __clAlphaLinearTrait = 6.10;
		private Double __cdMin = 0.002;
		private Double __clAtCdMin = 0.10;
		private Double __clAtAlphaZero = 0.09;
		private Double __clEndLinearTrait = 0.8;
		private Double __clMax = 1.3;
		private Double __kFactorDragPolar = 0.10;
		private Double __mExponentDragPolar = 2.0;
		private Double __cmAlphaQuarterChord = -0.50;
		private Double __xACNormalized = 0.30;
		private Double __cmAC = -0.070;
		private Double __cmACAtStall = -0.090;
		private Double __machCritical = 0.7;

		public AirfoilBuilder(String id){
			this.__id = id;
		}

		public AirfoilBuilder type(AirfoilTypeEnum type) {
			__type = type;
			return this;
		}

		public AirfoilBuilder family(AirfoilEnum fam) {
			__family = fam;
			return this;
		}

		// TODO: implement default airfoil construction according to designation. Example: AirfoilNameEnum.NACA2412
		
		public AirfoilBuilder cornerPointsXZNormalized(double[][] xz) {
			__NormalizedCornerPointsXZ = MatrixUtils.createRealMatrix(xz);
			return this;
		}

		public AirfoilBuilder chord(Amount<Length> c) {
			__chord = c;
			return this;
		}

		public AirfoilBuilder thicknessToChordRatio(Double tOverC) {
			__thicknessToChordRatio = tOverC;
			return this;
		}

		public AirfoilBuilder camberRatio(Double fOverC) {
			__camberRatio = fOverC;
			return this;
		}

		public AirfoilBuilder radiusLeadingEdgeNormalized(Double rLE) {
			__radiusLeadingEdgeNormalized = rLE;
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
		
		public AirfoilBuilder angleAtTrailingEdge(Amount<Angle> phiTE) {
			__angleAtTrailingEdge = phiTE;
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

		public AirfoilBuilder clAlphaLinearTrait(Double clApha) {
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

		public AirfoilBuilder mExponentDragPolar(Double mExponentDragPolar) {
			__mExponentDragPolar = mExponentDragPolar;
			return this;
		}

		public AirfoilBuilder cmAlphaQuarterChord(Double cmAlphaQuarterChord) {
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

		public AirfoilCreator build() {
			return new AirfoilCreator(this);
		}

	}

	private AirfoilCreator(AirfoilBuilder builder) {
		_id = builder.__id;
		_type = builder.__type;
		_family = builder.__family;
		_NormalizedCornerPointsXZ = builder.__NormalizedCornerPointsXZ;
		_chord = builder.__chord;
		_thicknessToChordRatio = builder.__thicknessToChordRatio;
		_camberRatio = builder.__camberRatio;
		_radiusLeadingEdgeNormalized = builder.__radiusLeadingEdgeNormalized;
		_xCoords = builder.__xCoords;
		_zCoords = builder.__zCoords;
		_angleAtTrailingEdge = builder.__angleAtTrailingEdge;
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
		_mExponentDragPolar = builder.__mExponentDragPolar;
		_cmAlphaQuarterChord = builder.__cmAlphaQuarterChord;
		_xACNormalized = builder.__xACNormalized;
		_cmAC = builder.__cmAC;
		_cmACAtStall = builder.__cmACAtStall;
		_machCritical = builder.__machCritical;

	}

	public static AirfoilCreator importFromXML(String pathToXML) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading airfoil data ...");

		@SuppressWarnings("unused")
		String family = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/@family");

		String typeS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/@type");
		// check if the airfoil type given in file is a legal enumerated type
		AirfoilTypeEnum type = Arrays.stream(AirfoilTypeEnum.values())
	            .filter(e -> e.toString().equals(typeS))
	            .findFirst()
	            .orElseThrow(() -> new IllegalStateException(String.format("Unsupported airfoil type %s.", typeS)));

		Double thicknessRatio = Double.valueOf(
				MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//airfoil/geometry/thickness_to_chord_ratio_max/text()"));

		Double camberRatio = Double.valueOf(
				MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//airfoil/geometry/camber_ratio/text()"));

		Double radiusLeadingEdgeNormalized = Double.valueOf(
				MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//airfoil/geometry/radius_leading_edge_normalized/text()"));
		
		List<String> xCoordsProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//airfoil/geometry/x_coordinates").get(0));
		Double[] xCoords = new Double[xCoordsProperty.size()];
		for(int i=0; i<xCoordsProperty.size(); i++)
			xCoords[i] = Double.valueOf(xCoordsProperty.get(i));
			
		List<String> zCoordsProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//airfoil/geometry/z_coordinates").get(0));
		Double[] zCoords = new Double[zCoordsProperty.size()];
		for(int i=0; i<zCoordsProperty.size(); i++)
			zCoords[i] = Double.valueOf(zCoordsProperty.get(i));
		
		Amount<Angle> alphaZeroLift = reader.getXMLAmountAngleByPath("//airfoil/aerodynamics/alpha_zero_lift");

		Amount<Angle> alphaEndLinearTrait = reader.getXMLAmountAngleByPath("//airfoil/aerodynamics/alpha_end_linear_trait");

		Amount<Angle> alphaStall = reader.getXMLAmountAngleByPath("//airfoil/aerodynamics/alpha_stall");

		Double clAlphaLinearTrait = Double.valueOf(
				MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//airfoil/aerodynamics/Cl_alpha_linear_trait/text()"));

		Double cDmin = Double.valueOf(
				MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//airfoil/aerodynamics/Cd_min/text()"));

		Double clAtCdMin = Double.valueOf(
				MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//airfoil/aerodynamics/Cl_at_Cdmin/text()"));

		Double clAtAlphaZero = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/Cl_at_alpha_zero/text()"));

		Double clEndLinearTrait = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/Cl_end_linear_trait/text()"));

		Double clMax = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/Cl_max/text()"));

		Double kFactorDragPolar = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/K_factor_drag_polar/text()"));

		Double mExponentDragPolar = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/m_exponent_drag_polar/text()"));

		Double cmAlphaQuarterChord = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/Cm_alpha_quarter_chord/text()"));

		Double xACNormalized = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/x_ac_normalized/text()"));

		Double cmAC = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/Cm_ac/text()"));

		Double cmACAtStall = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/Cm_ac_at_stall/text()"));

		Double machCritical = Double.valueOf(
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//airfoil/aerodynamics/mach_critical/text()"));

		String formedID = "Imported from ";
		Path p = Paths.get(pathToXML);
		String strippedFileName = p.getFileName().toString();
		formedID += strippedFileName;

		// create an Airfoil object with the Builder pattern
		AirfoilCreator airfoil = new AirfoilBuilder(formedID)
				.type(type)
				.thicknessToChordRatio(thicknessRatio)
				.camberRatio(camberRatio)
				.radiusLeadingEdgeNormalized(radiusLeadingEdgeNormalized)
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
				.mExponentDragPolar(mExponentDragPolar)
				.cmAlphaQuarterChord(cmAlphaQuarterChord)
				.xACNormalized(xACNormalized)
				.cmAC(cmAC)
				.cmACAtStall(cmACAtStall)
				.machCritical(machCritical)
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
				.append("\tID: '" + _id + "'\n")
				.append("\tType: " + _type + "\n")
				.append("\tFamily: " + _family + "\n")
				.append("\tc = " + _chord.to(SI.METER) + "\n")
				.append("\tt/c = " + _thicknessToChordRatio + "\n")
				.append("\tf/c = " + _camberRatio + "\n")
				.append("\tr_le/c = " + _radiusLeadingEdgeNormalized + "\n")
				.append("\tx coordinates = " + Arrays.toString(_xCoords) + "\n")
				.append("\tz coordinates = " + Arrays.toString(_zCoords) + "\n")
				.append("\tphi_te = " + _angleAtTrailingEdge.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\talpha_0l = " + _alphaZeroLift.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\talpha_star = " + _alphaEndLinearTrait.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\talpha_stall = " + _alphaStall.to(NonSI.DEGREE_ANGLE) + "\n")
				.append("\tCl_star = " + _clAlphaLinearTrait + "\n")
				.append("\tCd_min = " + _cdMin + "\n")
				.append("\tCl @ Cd_min = " + _clAtCdMin + "\n")
				.append("\tCl0 = " + _clAtAlphaZero + "\n")
				.append("\tCl_star = " + _clEndLinearTrait + "\n")
				.append("\tCl_max = " + _clMax + "\n")
				.append("\tk-factor (drag polar) = " + _kFactorDragPolar + "\n")
				.append("\tm-exponent (drag polar) = " + _mExponentDragPolar + "\n")
				.append("\tCm_alpha wrt c/4 = " + _cmAlphaQuarterChord + "\n")
				.append("\tx_ac/c = " + _xACNormalized + "\n")
				.append("\tCm_ac = " + _cmAC + "\n")
				.append("\tCm_ac @ stall = " + _cmACAtStall + "\n")
				.append("\tM_cr = " + _machCritical)
				;
		return sb.toString();
	}

}
