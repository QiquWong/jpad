package aircraft.components.liftingSurface;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.enumerations.ComponentEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;

public interface ILiftingSurface {

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
	public Amount<Angle> getDihedralEquivalent(Boolean recalculate);
	public LiftingSurfaceCreator getLiftingSurfaceCreator();
	
	public void calculateGeometry(ComponentEnum type);
	public void calculateGeometry(int nSections, ComponentEnum type);
	
	public List<Airfoil> getAirfoilList();
	public void setAirfoilList(List<Airfoil> airfoilList);
	
	public List<Airfoil> populateAirfoilList (
			AerodynamicDatabaseReader aeroDatabaseReader,
			Boolean equivalentWingFlag
			);
	
	public double getChordAtYActual(Double y);
	
}

