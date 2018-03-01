package aircraft.components.liftingSurface.creator;

import java.util.List;

import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.ComponentEnum;

@FreeBuilder
public interface ILiftingSurfaceCreator {

	// GENERAL INFORMATION
	String getId();
	boolean isMirrored();
	boolean getEquivalentWingFlag();
	ComponentEnum getType();

	// GLOBAL DATA
	double getMainSparDimensionlessPosition();
	double getSecondarySparDimensionlessPosition();
	Amount<Length> getRoughness();
	Amount<Length> getWingletHeight();
	
	// EQUIVALENT WING
	IEquivalentWing getEquivalentWing();	
	
	// PANELS AND CONTROL SURFACE LISTS
	List<LiftingSurfacePanelCreator> getPanels();
	List<SymmetricFlapCreator> getSymmetricFlaps();
	List<SlatCreator> getSlats();
	List<AsymmetricFlapCreator> getAsymmetricFlaps();
	List<SpoilerCreator> getSpoilers();
	
	class Builder extends ILiftingSurfaceCreator_Builder {
		public Builder() {
			
		}
	};
	
}
