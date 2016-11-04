package standaloneutils.launchers;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLBodyConfiguration {

	Integer getNSpanwise();
	Integer getNChordwise();
	Double getSSpace();
	Double getCSpace();
	
	/** Builder of AVLBodyConfiguration instances. */
	class Builder extends AVLBodyConfiguration_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setNSpanwise(10);
			setNChordwise(5);
			setSSpace(1.0);
			setCSpace(1.0);
		}
	}	

}
