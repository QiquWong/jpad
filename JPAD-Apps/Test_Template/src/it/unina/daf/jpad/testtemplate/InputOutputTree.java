package it.unina.daf.jpad.testtemplate;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;

public class InputOutputTree {
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// INPUT

	private Amount<Angle> alpha, sweepLE;

	private Amount<Length> altitude;

	private Amount<Area> surface;

	private double  machNumber,	aspectRatio;

	private int numberOfPointSemispan;

	private List<Double> testInputList;

	// OUTPUT 

	private Amount<Length> span, semiSpan;

	private Amount<Angle> alphaZeroLift;

	int numberOfAlphaCL = 50;

	private double cLZero;

	private List<Double> testOutputList;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:

	public InputOutputTree() {

		alpha = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		sweepLE = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		altitude = Amount.valueOf(0.0, SI.METER);

		surface = Amount.valueOf(0.0, SI.SQUARE_METRE);

		machNumber = 0.0;
		aspectRatio = 0.0;

		numberOfPointSemispan = 0;

		testInputList = new ArrayList<>();
	}

	public void importFromXML(
			String pathToXML, 
			String databaseFolderPath, String aerodynamicDatabaseFileName
			) throws ParserConfigurationException {
		
		System.out.println("########### pippo!");
		
	}

	public void buildOutput(){
		
		testOutputList = new ArrayList<>();
		
	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public Amount<Angle> getAlpha() {
		return alpha;
	}

	public Amount<Angle> getSweepLE() {
		return sweepLE;
	}

	public void setSweepLE(Amount<Angle> sweepLE) {
		this.sweepLE = sweepLE;
	}

	public Amount<Length> getAltitude() {
		return altitude;
	}

	public void setAltitude(Amount<Length> altitude) {
		this.altitude = altitude;
	}

	public Amount<Area> getSurface() {
		return surface;
	}

	public void setSurface(Amount<Area> surface) {
		this.surface = surface;
	}

	public double getMachNumber() {
		return machNumber;
	}

	public void setMachNumber(double machNumber) {
		this.machNumber = machNumber;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public int getNumberOfPointSemispan() {
		return numberOfPointSemispan;
	}


	public void setNumberOfPointSemispan(int numberOfPointSemispan) {
		this.numberOfPointSemispan = numberOfPointSemispan;
	}

	public Amount<Length> getSpan() {
		return span;
	}

	public void setSpan(Amount<Length> span) {
		this.span = span;
	}

	public Amount<Length> getSemiSpan() {
		return semiSpan;
	}

	public void setSemiSpan(Amount<Length> semiSpan) {
		this.semiSpan = semiSpan;
	}

	public Amount<Angle> getAlphaZeroLift() {
		return alphaZeroLift;
	}

	public void setAlphaZeroLift(Amount<Angle> alphaZeroLift) {
		this.alphaZeroLift = alphaZeroLift;
	}

	public int getNumberOfAlphaCL() {
		return numberOfAlphaCL;
	}

	public void setNumberOfAlphaCL(int numberOfAlphaCL) {
		this.numberOfAlphaCL = numberOfAlphaCL;
	}

	public List<Double> getTestInputList() {
		return testInputList;
	}


	public void setgetTestInputList(List<Double> list) {
		this.testInputList = list;
	}

	public double getcLZero() {
		return cLZero;
	}

	public void setcLZero(double cLZero) {
		this.cLZero = cLZero;
	}

}
