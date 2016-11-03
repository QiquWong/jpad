package standaloneutils.launchers;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLWingConfiguration {

	Integer getNSpanwise();
	Integer getNChordwise();
	Double getSSpace();
	Double getCSpace();
	
	/** Builder of AVLWingConfiguration instances. */
	class Builder extends AVLWingConfiguration_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setNSpanwise(10);
			setNChordwise(5);
			setSSpace(1.0);
			setCSpace(1.0);
		}
	}	
	
	
}
