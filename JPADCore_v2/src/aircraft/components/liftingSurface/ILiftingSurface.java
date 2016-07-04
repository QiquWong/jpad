package aircraft.components.liftingSurface;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.customdata.CenterOfGravity;

public interface ILiftingSurface {

	public String getId();
	public void setId (String name);
	public ComponentEnum getType();
	public void setType (ComponentEnum type);
	
	public LiftingSurfaceCreator getLiftingSurfaceCreator();
	public void setLiftingSurfaceCreator (LiftingSurfaceCreator lsc);
	
	public AerodynamicDatabaseReader getAerodynamicDatabaseReader ();
	public void setAerodynamicDatabaseReader (AerodynamicDatabaseReader aeroDatabaseReader);
	
	public Amount<Length> getXApexConstructionAxes();
	public void setXApexConstructionAxes (Amount<Length> xApexConstructionAxes);
	public Amount<Length> getYApexConstructionAxes();
	public void setYApexConstructionAxes (Amount<Length> yApexConstructionAxes);
	public Amount<Length> getZApexConstructionAxes();
	public void setZApexConstructionAxes (Amount<Length> zApexConstructionAxes);
	public Amount<Angle> getRiggingAngle();
	public void setRiggingAngle (Amount<Angle> iW);
	
	public Amount<Area> getSurface();
	public double getAspectRatio();
	public Amount<Length> getSpan();
	public Amount<Length> getSemiSpan();
	public double getTaperRatio();
	public double getTaperRatioEquivalent(Boolean recalculate);
	public LiftingSurfaceCreator getEquivalentWing(Boolean recalculate);
	public Amount<Length> getChordRootEquivalent(Boolean recalculate);
	public Amount<Length> getChordRoot();
	public Amount<Length> getChordTip();
	public Amount<Angle> getSweepLEEquivalent(Boolean recalculate);
	public Amount<Angle> getSweepHalfChordEquivalent(Boolean recalculate);
	public Amount<Angle> getSweepQuarterChordEquivalent(Boolean recalculate);
	
	public void calculateGeometry(ComponentEnum type, Boolean mirrored);
	public void calculateGeometry(int nSections, ComponentEnum type, Boolean mirrored);
	
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions);
	public void calculateCG(MethodEnum method, ComponentEnum type);
	
	public List<Airfoil> getAirfoilList();
	public void setAirfoilList(List<Airfoil> airfoilList);
	
	public List<Airfoil> populateAirfoilList (
			AerodynamicDatabaseReader aeroDatabaseReader,
			Boolean equivalentWingFlag
			);
	
	public double getChordAtYActual(Double y);
	
	public CenterOfGravity getCG();
	public Amount<Length> getXCG();
	public Amount<Length> getYCG();
	public Amount<Length> getZCG();

	public void setCG(CenterOfGravity cg);
	public void setXCG(Amount<Length> xCG);
	public void setYCG(Amount<Length> yCG);
	public void setZCG(Amount<Length> zCG);
	
	public Double getPositionRelativeToAttachment();
	public void setPositionRelativeToAttachment(Double positionRelativeToAttachment);
	
}

