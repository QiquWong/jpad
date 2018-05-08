package aircraft.components.liftingSurface;

import java.util.List;

import javax.annotation.Nullable;
import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.creator.AsymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.IEquivalentWing;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import configuration.enumerations.ComponentEnum;

@FreeBuilder
public interface ILiftingSurface {

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
	@Nullable
	IEquivalentWing getEquivalentWing();	
	
	// PANELS AND CONTROL SURFACE LISTS
	List<LiftingSurfacePanelCreator> getPanels();
	List<SymmetricFlapCreator> getSymmetricFlaps();
	List<SlatCreator> getSlats();
	List<AsymmetricFlapCreator> getAsymmetricFlaps();
	List<SpoilerCreator> getSpoilers();
	
	class Builder extends ILiftingSurface_Builder {
		public Builder() {
			
		}
	};
	
}
