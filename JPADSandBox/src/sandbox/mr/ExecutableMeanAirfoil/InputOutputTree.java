package sandbox.mr.ExecutableMeanAirfoil;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import org.jscience.physics.amount.Amount;

public class InputOutputTree {
	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:

	// INPUT

	private int numberOfSection;
	
	private Amount<Length> wingSpan;
	
	private Amount<Area> wingSurface;

	private List<Amount<Angle>> alphaZeroLiftArray,
								angleOfStallArray,
								alphaStarArray,
								phiTEArray;

	private List<Double> etaStations,
						 clStarArray, 
						 cl0Array,
						 clmaxArray,
						 cdminArray,
						 clAtCdminArray,
						 kFactorDragPolarArray,
						 xacArray,
						 cmACArray,
						 cmACstallArray,
						 maximumThicknessArray,
						 otherValuesArray;

	private List<Amount<Length>> chordsArray,
								 radiusLEArray;

	private List<Amount<?>> clAlphaArray;

	// OUTPUT 

	private List<Double> influenceCoefficients;
	
	private List<Amount<Area>> influenceAreas;
	
	private Amount<Angle> alphaZeroLift,
						  angleOfStall,
						  alphaStar,
						  phiTE; 

	private Double clStar, 
			       cl0,
				   clmax,
			       cdmin,
				   clAtCdmin,
				   kFactorDragPolar,
				   xac,
				   cmAC,
				   cmACstall,
				   maximumThickness,
				   otherValues;
	
	private Amount<Length> chords,
					       radiusLE;

	private Amount<?> clAlpha;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:

	public InputOutputTree() {

		alphaZeroLiftArray = new ArrayList<Amount<Angle>>();
		angleOfStallArray = new ArrayList<Amount<Angle>>();
		alphaStarArray = new ArrayList<Amount<Angle>>();
		phiTEArray = new ArrayList<Amount<Angle>>();
		
		etaStations = new ArrayList<Double>();
		clStarArray = new ArrayList<Double>();
		cl0Array = new ArrayList<Double>();
		clmaxArray = new ArrayList<Double>();
		cdminArray = new ArrayList<Double>();
		clAtCdminArray = new ArrayList<Double>();
		kFactorDragPolarArray = new ArrayList<Double>();
		xacArray = new ArrayList<Double>();
		cmACArray = new ArrayList<Double>();
		cmACstallArray = new ArrayList<Double>();
		maximumThicknessArray = new ArrayList<Double>();
		otherValuesArray = new ArrayList<Double>();

		radiusLEArray = new ArrayList<Amount<Length>>();
		chordsArray = new ArrayList<Amount<Length>>();
		
		clAlphaArray = new ArrayList<Amount<?>>();

		setInfluenceCoefficients(new ArrayList<Double>());
		setInfluenceAreas(new ArrayList<Amount<Area>>());
	}

	//------------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:
	
	public int getNumberOfSection() {
		return numberOfSection;
	}

	public List<Amount<Angle>> getAlphaZeroLiftArray() {
		return alphaZeroLiftArray;
	}

	public List<Amount<Angle>> getAngleOfStallArray() {
		return angleOfStallArray;
	}

	public List<Amount<Angle>> getAlphaStarArray() {
		return alphaStarArray;
	}

	public List<Amount<Angle>> getPhiTEArray() {
		return phiTEArray;
	}

	public List<Double> getClStarArray() {
		return clStarArray;
	}

	public List<Double> getCl0Array() {
		return cl0Array;
	}

	public List<Double> getClmaxArray() {
		return clmaxArray;
	}

	public List<Double> getCdminArray() {
		return cdminArray;
	}

	public List<Double> getClAtCdminArray() {
		return clAtCdminArray;
	}

	public List<Double> getkFactorDragPolarArray() {
		return kFactorDragPolarArray;
	}

	public List<Double> getXacArray() {
		return xacArray;
	}

	public List<Double> getCmACArray() {
		return cmACArray;
	}

	public List<Double> getCmACstallArray() {
		return cmACstallArray;
	}

	public List<Double> getMaximumThicknessArray() {
		return maximumThicknessArray;
	}

	public List<Double> getOtherValuesArray() {
		return otherValuesArray;
	}

	public List<Amount<Length>> getChordsArray() {
		return chordsArray;
	}

	public List<Amount<Length>> getRadiusLEArray() {
		return radiusLEArray;
	}

	public List<Amount<?>> getClAlphaArray() {
		return clAlphaArray;
	}

	public Amount<Angle> getAlphaZeroLift() {
		return alphaZeroLift;
	}

	public Amount<Angle> getAngleOfStall() {
		return angleOfStall;
	}

	public Amount<Angle> getAlphaStar() {
		return alphaStar;
	}

	public Amount<Angle> getPhiTE() {
		return phiTE;
	}

	public Double getClStar() {
		return clStar;
	}

	public Double getCl0() {
		return cl0;
	}

	public Double getClmax() {
		return clmax;
	}

	public Double getCdmin() {
		return cdmin;
	}

	public Double getClAtCdmin() {
		return clAtCdmin;
	}

	public Double getkFactorDragPolar() {
		return kFactorDragPolar;
	}

	public Double getXac() {
		return xac;
	}

	public Double getCmAC() {
		return cmAC;
	}

	public Double getCmACstall() {
		return cmACstall;
	}

	public Double getMaximumThickness() {
		return maximumThickness;
	}

	public Double getOtherValues() {
		return otherValues;
	}

	public Amount<Length> getChords() {
		return chords;
	}

	public Amount<Length> getRadiusLE() {
		return radiusLE;
	}

	public Amount<?> getClAlpha() {
		return clAlpha;
	}

	public void setNumberOfSection(int numberOfSection) {
		this.numberOfSection = numberOfSection;
	}

	public void setAlphaZeroLiftArray(List<Amount<Angle>> alphaZeroLiftArray) {
		this.alphaZeroLiftArray = alphaZeroLiftArray;
	}

	public void setAngleOfStallArray(List<Amount<Angle>> angleOfStallArray) {
		this.angleOfStallArray = angleOfStallArray;
	}

	public void setAlphaStarArray(List<Amount<Angle>> alphaStarArray) {
		this.alphaStarArray = alphaStarArray;
	}

	public void setPhiTEArray(List<Amount<Angle>> phiTEArray) {
		this.phiTEArray = phiTEArray;
	}

	public void setClStarArray(List<Double> clStarArray) {
		this.clStarArray = clStarArray;
	}

	public void setCl0Array(List<Double> cl0Array) {
		this.cl0Array = cl0Array;
	}

	public void setClmaxArray(List<Double> clmaxArray) {
		this.clmaxArray = clmaxArray;
	}

	public void setCdminArray(List<Double> cdminArray) {
		this.cdminArray = cdminArray;
	}

	public void setClAtCdminArray(List<Double> clAtCdminArray) {
		this.clAtCdminArray = clAtCdminArray;
	}

	public void setkFactorDragPolarArray(List<Double> kFactorDragPolarArray) {
		this.kFactorDragPolarArray = kFactorDragPolarArray;
	}

	public void setXacArray(List<Double> xacArray) {
		this.xacArray = xacArray;
	}

	public void setCmACArray(List<Double> cmACArray) {
		this.cmACArray = cmACArray;
	}

	public void setCmACstallArray(List<Double> cmACstallArray) {
		this.cmACstallArray = cmACstallArray;
	}

	public void setMaximumThicknessArray(List<Double> maximumThicknessArray) {
		this.maximumThicknessArray = maximumThicknessArray;
	}

	public void setOtherValuesArray(List<Double> otherValuesArray) {
		this.otherValuesArray = otherValuesArray;
	}

	public void setChordsArray(List<Amount<Length>> chordsArray) {
		this.chordsArray = chordsArray;
	}

	public void setRadiusLEArray(List<Amount<Length>> radiusLEArray) {
		this.radiusLEArray = radiusLEArray;
	}

	public void setClAlphaArray(List<Amount<?>> clAlphaArray) {
		this.clAlphaArray = clAlphaArray;
	}

	public void setAlphaZeroLift(Amount<Angle> alphaZeroLift) {
		this.alphaZeroLift = alphaZeroLift;
	}

	public void setAngleOfStall(Amount<Angle> angleOfStall) {
		this.angleOfStall = angleOfStall;
	}

	public void setAlphaStar(Amount<Angle> alphaStar) {
		this.alphaStar = alphaStar;
	}

	public void setPhiTE(Amount<Angle> phiTE) {
		this.phiTE = phiTE;
	}

	public void setClStar(Double clStar) {
		this.clStar = clStar;
	}

	public void setCl0(Double cl0) {
		this.cl0 = cl0;
	}

	public void setClmax(Double clmax) {
		this.clmax = clmax;
	}

	public void setCdmin(Double cdmin) {
		this.cdmin = cdmin;
	}

	public void setClAtCdmin(Double clAtCdmin) {
		this.clAtCdmin = clAtCdmin;
	}

	public void setkFactorDragPolar(Double kFactorDragPolar) {
		this.kFactorDragPolar = kFactorDragPolar;
	}

	public void setXac(Double xac) {
		this.xac = xac;
	}

	public void setCmAC(Double cmAC) {
		this.cmAC = cmAC;
	}

	public void setCmACstall(Double cmACstall) {
		this.cmACstall = cmACstall;
	}

	public void setMaximumThickness(Double maximumThickness) {
		this.maximumThickness = maximumThickness;
	}

	public void setOtherValues(Double otherValues) {
		this.otherValues = otherValues;
	}

	public void setChords(Amount<Length> chords) {
		this.chords = chords;
	}

	public void setRadiusLE(Amount<Length> radiusLE) {
		this.radiusLE = radiusLE;
	}

	public void setClAlpha(Amount<?> clAlpha) {
		this.clAlpha = clAlpha;
	}

	public List<Double> getEtaStations() {
		return etaStations;
	}

	public void setEtaStations(List<Double> etaStations) {
		this.etaStations = etaStations;
	}

	public Amount<Length> getWingSpan() {
		return wingSpan;
	}

	public void setWingSpan(Amount<Length> wingSpan) {
		this.wingSpan = wingSpan;
	}

	public Amount<Area> getWingSurface() {
		return wingSurface;
	}

	public void setWingSurface(Amount<Area> wingSurface) {
		this.wingSurface = wingSurface;
	}

	public List<Double> getInfluenceCoefficients() {
		return influenceCoefficients;
	}

	public void setInfluenceCoefficients(List<Double> influenceCoefficients) {
		this.influenceCoefficients = influenceCoefficients;
	}

	public List<Amount<Area>> getInfluenceAreas() {
		return influenceAreas;
	}

	public void setInfluenceAreas(List<Amount<Area>> influenceAreas) {
		this.influenceAreas = influenceAreas;
	}
	
}
