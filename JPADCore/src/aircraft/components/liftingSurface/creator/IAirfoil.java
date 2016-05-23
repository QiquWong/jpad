package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilTypeEnum;

public interface IAirfoil {
	
	String getID();
	void setID(String id);
	
	AirfoilTypeEnum getType();
	void setType(AirfoilTypeEnum type);
	
	AirfoilEnum getFamily();
	void setFamily(AirfoilEnum fam);

	double[][] getNormalizedCornerPointsXZ();
	void setNormalizedCornerPointsXZ(double[][] xz);
	
	Amount<Length> getChord();
	void setChord(Amount<Length> c);
	
	Double getThicknessToChordRatio();
	void setThicknessToChordRatio(Double tOverC);

	Double getCamberRatio();
	void setCamberRatio(Double fOverC);
	
	Double getRadiusLeadingEdgeNormalized();
	void setRadiusLeadingEdgeNormalized(Double rLE);
	
	Amount<Angle> getAngleAtTrailingEdge();
	void setAngleAtTrailingEdge(Amount<Angle> phiTE);
	
	Amount<Angle> getAlphaZeroLift();
	void setAlphaZeroLift(Amount<Angle> alpha0l);
	
	Amount<Angle> getAlphaLinearTrait();
	void setAlphaLinearTrait(Amount<Angle> alphaStar);
	
	Amount<Angle> getAlphaStall();
	void setAlphaStall(Amount<Angle> alphaStall);
	
    Double getClAlphaLinearTrait();
    void setClAlphaLinearTrait(Double clApha);
    
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
	
}
