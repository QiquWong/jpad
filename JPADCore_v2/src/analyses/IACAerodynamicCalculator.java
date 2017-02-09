package analyses;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface IACAerodynamicCalculator {

	// TODO: Fill with all the getters ...
	
	/** Builder of ACAErodynamicCalculator instances. */
	class Builder extends IACAerodynamicCalculator_Builder {
		public Builder() {
			// Set defaults in the builder constructor.
			// ... eventually
		}
	}
	
}
