package standaloneutils.launchers;

import java.util.ArrayList;
import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLWingSection {

	List<AVLWingSectionControlSurface> getControlSurfaces();

	/** Builder of AVLWingSection instances. */
	class Builder extends AVLWingSection_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			clearControlSurfaces();
			addAllControlSurfaces(new ArrayList<AVLWingSectionControlSurface>());
			

		}
	}	

	default void appendControlSurface(AVLWingSectionControlSurface controlSurface) {
		getControlSurfaces().add(controlSurface);
	}
	
}
