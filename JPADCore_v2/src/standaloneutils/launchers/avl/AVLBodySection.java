package standaloneutils.launchers.avl;

import java.io.File;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLBodySection {

	String getDescription();
	Double[] getOrigin();
	File getBodySectionCoordFile();

	/** Builder of AVLBodySection instances. */
	class Builder extends AVLBodySection_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("SECTION");
			setOrigin(new Double[]{0.0, 0.0, 0.0});
		}
	}
	
}