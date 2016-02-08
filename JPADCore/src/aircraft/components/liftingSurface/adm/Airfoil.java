package aircraft.components.liftingSurface.adm;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;
import parser.output.__Test__;

public class Airfoil implements IAirfoil {

	private String _id;
	private AirfoilTypeEnum _type;
	private AirfoilFamilyEnum _family;	
	private RealMatrix _NormalizedCornerPointsXZ;
	private Amount<Length> _chord;
	private Double _thicknessToChordRatio;
	private Double _camberRatio;
	private Amount<Length> _radiusLeadingEdge;
	private Amount<Angle> _angleAtTrailingEdge;
	private Amount<Angle> _alphaZeroLift;
	private Amount<Angle> _alphaLinearTrait;
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
	private Double _xACAdimensional;
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
	public AirfoilFamilyEnum getFamily() {
		return _family;
	}

	@Override
	public void setFamily(AirfoilFamilyEnum fam) {
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
	public Amount<Length> getRadiusLeadingEdge() {
		return _radiusLeadingEdge;
	}

	@Override
	public void setRadiusLeadingEdge(Amount<Length> rLE) {
		_radiusLeadingEdge = rLE;
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
	public Amount<Angle> getAlphaLinearTrait() {
		return _alphaLinearTrait;
	}

	@Override
	public void setAlphaLinearTrait(Amount<Angle> alphaStar) {
		_alphaLinearTrait = alphaStar;
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
	public Double getXACAdimensional() {
		return _xACAdimensional;
	}

	@Override
	public void setXACAdimensional(Double xACAdimensional) {
		_xACAdimensional = xACAdimensional;
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
		private AirfoilFamilyEnum __family = AirfoilFamilyEnum.NACA65_209;	
		private RealMatrix __NormalizedCornerPointsXZ = MatrixUtils.createRealMatrix(30, 2);
		private Amount<Length> __chord = Amount.valueOf(1.0,SI.METER);
		private Double __thicknessToChordRatio = 0.12;
		private Double __camberRatio = 0.9;
		private Amount<Length> __radiusLeadingEdge = Amount.valueOf(0.015,SI.METER);
		private Amount<Angle> __angleAtTrailingEdge = Amount.valueOf(4.0,NonSI.DEGREE_ANGLE);
		private Amount<Angle> __alphaZeroLift = Amount.valueOf(-1.5,NonSI.DEGREE_ANGLE);
		private Amount<Angle> __alphaLinearTrait = Amount.valueOf(9.0,NonSI.DEGREE_ANGLE);;
		private Amount<Angle> __alphaStall = Amount.valueOf(12.0,NonSI.DEGREE_ANGLE);;
		private Double __clAlphaLinearTrait = 6.10;
		private Double __cdMin = 0.002;
		private Double __clAtCdMin = 0.10;
		private Double __clAtAlphaZero = 0.09;
		private Double __clEndLinearTrait = 0.8;
		private Double __clMax = 1.3;
		private Double __kFactorDragPolar = 0.10;
		private Double __mExponentDragPolar = 2.0;
		private Double __cmAlphaQuarterChord = -0.50;
		private Double __xACAdimensional = 0.30;
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

		public AirfoilBuilder family(AirfoilFamilyEnum fam) {
			__family = fam;	
			return this;
		}

		public AirfoilBuilder normalizedCornerPointsXZ(double[][] xz) {
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

		public AirfoilBuilder radiusLeadingEdge(Amount<Length> rLE) {
			__radiusLeadingEdge = rLE;
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

		public AirfoilBuilder alphaLinearTrait(Amount<Angle> alphaStar) {
			__alphaLinearTrait = alphaStar;
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

		public AirfoilBuilder xACAdimensional(Double xACAdimensional) {
			__xACAdimensional = xACAdimensional;
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
		
		// TODO - implement a constructor from an XML node
		
		public Airfoil build() {
			return new Airfoil(this);
		}

	}
	
	private Airfoil(AirfoilBuilder builder) {
		_id = builder.__id;
		_type = builder.__type;
		_family = builder.__family;	
		_NormalizedCornerPointsXZ = builder.__NormalizedCornerPointsXZ;
		_chord = builder.__chord;
		_thicknessToChordRatio = builder.__thicknessToChordRatio;
		_camberRatio = builder.__camberRatio;
		_radiusLeadingEdge = builder.__radiusLeadingEdge;
		_angleAtTrailingEdge = builder.__angleAtTrailingEdge;
		_alphaZeroLift = builder.__alphaZeroLift;
		_alphaLinearTrait = builder.__alphaLinearTrait;
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
		_xACAdimensional = builder.__xACAdimensional;
		_cmAC = builder.__cmAC;
		_cmACAtStall = builder.__cmACAtStall;
		_machCritical = builder.__machCritical;
		
	}
	
	@Override public String toString() {
		return 
				"Airfoil\n"
				+ "ID: '" + _id + "'\n"
				+ "Type: " + _type + "\n" 
				+ "Family: " + _family + "\n"
				+ "c = " + _chord.to(SI.METER).getEstimatedValue() + " m\n"
				+ "t/c = " + _thicknessToChordRatio + "\n"
				+ "f/c = " + _camberRatio + "\n"
				+ "r_le = "	+ _radiusLeadingEdge.to(SI.METER).getEstimatedValue() + " m\n"
				+ "phi_te = " + _angleAtTrailingEdge.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + " deg\n"
				+ "alpha_0l = " + _alphaZeroLift.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + " deg\n"
				+ "alpha_star = " + _alphaLinearTrait.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + " deg\n"
				+ "alpha_stall = " + _alphaStall.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + " deg\n"
				+ "Cl_star = " + _clAlphaLinearTrait + "\n"
				+ "Cd_min = " + _cdMin + "\n"
				+ "Cl @ Cd_min = " + _clAtCdMin + "\n"
				+ "Cl0 = " + _clAtAlphaZero + "\n"
				+ "Cl_star = " + _clEndLinearTrait + "\n"
				+ "Cl_max" + _clMax + "\n"
				+ "k-factor (polar) = " + _kFactorDragPolar + "\n"
				+ "m-exponent (polar) = " + _mExponentDragPolar + "\n"
				+ "Cm_alpha wrt c/4 = " + _cmAlphaQuarterChord + "\n"
				+ "x_ac / c = " + _xACAdimensional + "\n"
				+ "Cm_ac = " + _cmAC + "\n"
				+ "Cm_ac @ stall = " + _cmACAtStall + "\n"
				+ "M_cr = " + _machCritical
				; 
	}
	
	public static void main(String[] args) {
		
		Airfoil a = new AirfoilBuilder("Pippo-0012")
				.alphaZeroLift(Amount.valueOf(-3.0,NonSI.DEGREE_ANGLE))
				.clAlphaLinearTrait(5.9)
				.alphaLinearTrait(Amount.valueOf(-3.0,NonSI.DEGREE_ANGLE))
				.alphaStall(Amount.valueOf(13.0,NonSI.DEGREE_ANGLE))
				.clMax(1.1)
				.build();
		System.out.println(a);
				
	}
}
