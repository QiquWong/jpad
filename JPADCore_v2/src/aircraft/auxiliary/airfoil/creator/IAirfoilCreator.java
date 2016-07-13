package aircraft.auxiliary.airfoil.creator;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilTypeEnum;

public interface IAirfoilCreator {
	
	String getID();
	void setID(String id);
	
	AirfoilTypeEnum getType();
	void setType(AirfoilTypeEnum type);
	
	AirfoilEnum getFamily();
	void setFamily(AirfoilEnum fam);

	double[][] getNormalizedCornerPointsXZ();
	void setNormalizedCornerPointsXZ(double[][] xz);
	
	Double getThicknessToChordRatio();
	void setThicknessToChordRatio(Double tOverC);

	Double getCamberRatio();
	void setCamberRatio(Double fOverC);
	
	Amount<Length> getRadiusLeadingEdge();
	void setRadiusLeadingEdge(Amount<Length> rLE);
	
	Double[] getXCoords();
	void setXCoords(Double[] xCoords);
	
	Double[] getZCoords();
	void setZCoords(Double[] zCoords);
	
	Amount<Angle> getAngleAtTrailingEdge();
	void setAngleAtTrailingEdge(Amount<Angle> phiTE);
	
	Amount<Angle> getAlphaZeroLift();
	void setAlphaZeroLift(Amount<Angle> alpha0l);
	
	Amount<Angle> getAlphaEndLinearTrait();
	void setAlphaLinearTrait(Amount<Angle> alphaStar);
	
	Amount<Angle> getAlphaStall();
	void setAlphaStall(Amount<Angle> alphaStall);
	
    Amount<?> getClAlphaLinearTrait();
    void setClAlphaLinearTrait(Amount<?> clApha);
    
    Double getCdMin();
    void setCdMin(Double cdMin); 
    
    Double getClAtCdMin();
    void setClAtCdMin(Double clAtCdMin);
    
    Double getClAtAlphaZero();
    void setClAtAlphaZero(Double clAtAlphaZero);
    
    Double getClEndLinearTrait();
    void setClEndLinearTrait(Double clEndLinearTrait);
    
    Double getClMax();
    void setClMax(Double clMax);
    
    Double getKFactorDragPolar();
    void setKFactorDragPolar(Double kFactorDragPolar);
    
    Double getMExponentDragPolar();
    void setMExponentDragPolar(Double mExponentDragPolar);
    
    Double getCmAlphaQuarterChord();
    void setCmAlphaQuarterChord(Double cmAlphaQuarterChord);
    
    Double getXACNormalized();
    void setXACNormalized(Double xACNormalized);
    
    Double getCmAC();
    void setCmAC(Double cmAC);
    
    Double getCmACAtStall();
    void setCmACAtStall(Double cmACAtStall);
    
    Double getMachCritical();
    void setMachCritical(Double machCritical);
    
	Double getXTransitionUpper();
	void setXTransitionUpper(Double _xTransitionUpper);

	Double getXTransitionLower();
	void setXTransitionLower(Double _xTransitionLower);
	
    public Double calculateThicknessRatioAtXNormalizedStation (Double x, Double tcMaxActual);
    
}
