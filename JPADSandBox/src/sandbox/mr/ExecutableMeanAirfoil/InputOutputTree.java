package sandbox.mr.ExecutableMeanAirfoil;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;

public class InputOutputTree {
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// INPUT

	private int numberOfSection;

	private List<Amount> alphaZeroLift, angleOfStall;

	private List<Double> maximumThickness, liftCoefficientSlope, endOfLinearityCl,
	maximumLiftCoefficient, aerodynamicCenter, otherValues, clZero, leadingEdgeRdius;

	// OUTPUT 
	
	private Amount<Angle> alphaZeroLiftMean, angleOfStallMean;

	private double maximumThicknessMean, liftCoefficientSlopeMean, endOfLinearityClMean,
	maximumLiftCoefficientMean, aerodynamicCenterMean, otherValuesMean;

	//------------------------------------------------------------------------------------------
	// BUILDER:

	public InputOutputTree() {
		
		alphaZeroLift = new ArrayList<Amount>();
		angleOfStall = new ArrayList<Amount>();
		
		maximumThickness = new ArrayList<Double>();
		liftCoefficientSlope = new ArrayList<Double>();
		endOfLinearityCl = new ArrayList<Double>();
		maximumLiftCoefficient = new ArrayList<Double>();
		aerodynamicCenter = new ArrayList<Double>();
		otherValues = new ArrayList<Double>();
		clZero = new ArrayList<Double>();
		leadingEdgeRdius = new ArrayList<Double>();

		alphaZeroLiftMean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		angleOfStallMean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

	}


	public void buildOutput(){}


	public int getNumberOfSection() {
		return numberOfSection;
	}


	public void setNumberOfSection(int numberOfSection) {
		this.numberOfSection = numberOfSection;
	}


	public List<Amount> getAlphaZeroLift() {
		return alphaZeroLift;
	}


	public void setAlphaZeroLift(List<Amount> alphaZeroLift) {
		this.alphaZeroLift = alphaZeroLift;
	}


	public List<Amount> getAngleOfStall() {
		return angleOfStall;
	}


	public void setAngleOfStall(List<Amount> angleOfStall) {
		this.angleOfStall = angleOfStall;
	}


	public List<Double> getMaximumThickness() {
		return maximumThickness;
	}


	public void setMaximumThickness(List<Double> maximumThickness) {
		this.maximumThickness = maximumThickness;
	}


	public List<Double> getLiftCoefficientSlope() {
		return liftCoefficientSlope;
	}


	public void setLiftCoefficientSlope(List<Double> liftCoefficientSlope) {
		this.liftCoefficientSlope = liftCoefficientSlope;
	}


	public List<Double> getEndOfLinearityCl() {
		return endOfLinearityCl;
	}


	public void setEndOfLinearityCl(List<Double> endOfLinearityCl) {
		this.endOfLinearityCl = endOfLinearityCl;
	}


	public List<Double> getMaximumLiftCoefficient() {
		return maximumLiftCoefficient;
	}


	public void setMaximumLiftCoefficient(List<Double> maximumLiftCoefficient) {
		this.maximumLiftCoefficient = maximumLiftCoefficient;
	}


	public List<Double> getAerodynamicCenter() {
		return aerodynamicCenter;
	}


	public void setAerodynamicCenter(List<Double> aerodynamicCenter) {
		this.aerodynamicCenter = aerodynamicCenter;
	}


	public List<Double> getOtherValues() {
		return otherValues;
	}


	public void setOtherValues(List<Double> otherValues) {
		this.otherValues = otherValues;
	}


	public Amount<Angle> getAlphaZeroLiftMean() {
		return alphaZeroLiftMean;
	}


	public void setAlphaZeroLiftMean(Amount<Angle> alphaZeroLiftMean) {
		this.alphaZeroLiftMean = alphaZeroLiftMean;
	}


	public Amount<Angle> getAngleOfStallMean() {
		return angleOfStallMean;
	}


	public void setAngleOfStallMean(Amount<Angle> angleOfStallMean) {
		this.angleOfStallMean = angleOfStallMean;
	}


	public double getMaximumThicknessMean() {
		return maximumThicknessMean;
	}


	public void setMaximumThicknessMean(double maximumThicknessMean) {
		this.maximumThicknessMean = maximumThicknessMean;
	}


	public double getLiftCoefficientSlopeMean() {
		return liftCoefficientSlopeMean;
	}


	public void setLiftCoefficientSlopeMean(double liftCoefficientSlopeMean) {
		this.liftCoefficientSlopeMean = liftCoefficientSlopeMean;
	}


	public double getEndOfLinearityClMean() {
		return endOfLinearityClMean;
	}


	public void setEndOfLinearityClMean(double endOfLinearityClMean) {
		this.endOfLinearityClMean = endOfLinearityClMean;
	}


	public double getMaximumLiftCoefficientMean() {
		return maximumLiftCoefficientMean;
	}


	public void setMaximumLiftCoefficientMean(double maximumLiftCoefficientMean) {
		this.maximumLiftCoefficientMean = maximumLiftCoefficientMean;
	}


	public double getAerodynamicCenterMean() {
		return aerodynamicCenterMean;
	}


	public void setAerodynamicCenterMean(double aerodynamicCenterMean) {
		this.aerodynamicCenterMean = aerodynamicCenterMean;
	}


	public double getOtherValuesMean() {
		return otherValuesMean;
	}


	public void setOtherValuesMean(double otherValuesMean) {
		this.otherValuesMean = otherValuesMean;
	}


	public List<Double> getClZero() {
		return clZero;
	}


	public void setClZero(List<Double> clZero) {
		this.clZero = clZero;
	}


	public List<Double> getLeadingEdgeRdius() {
		return leadingEdgeRdius;
	}


	public void setLeadingEdgeRdius(List<Double> leadingEdgeRdius) {
		this.leadingEdgeRdius = leadingEdgeRdius;
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:



}
