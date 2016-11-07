package standaloneutils.launchers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.linear.RealMatrix;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLWingSection {

	String getDescription();
	Double[] getOrigin();
	Double getChord();
	Double getTwist();
	Optional<File> getAirfoilCoordFile();
	Optional<RealMatrix> getAirfoilSectionInline();

	
	List<AVLWingSectionControlSurface> getControlSurfaces();

	/** Builder of AVLWingSection instances. */
	class Builder extends AVLWingSection_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("SECTION");
			setOrigin(new Double[]{0.0, 0.0, 0.0});
			setChord(0.0);
			setTwist(0.0);
			clearControlSurfaces();
			addAllControlSurfaces(new ArrayList<AVLWingSectionControlSurface>());
		}
		Builder appendControlSurface(AVLWingSectionControlSurface controlSurface) {
			addControlSurfaces(controlSurface);
			return this;
		}
	}
}
