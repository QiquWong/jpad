package standaloneutils.launchers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.linear.RealMatrix;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLBody {
	/*
	 * A data class defining the parameters of a fuselage or other body modeled
	 * by side and planform projections arranged in a plus (+) shape (when
	 * viewed from the front).
	 */
	
	String getDescription();
	
	Integer getNBody();
	Double getBSpace();
	
	boolean isDuplicated();
	Double getYDupl();
	
	Double[] getOrigin();
	Double[] getScale();
	
	Optional<File> getBodyCoordFile();
	
	Optional<RealMatrix> getBodySectionInline();

	/** Builder of AVLBody instances. */
	class Builder extends AVLBody_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("BODY");
			
			setNBody(15);
			setBSpace(1.0);
			
			setDuplicated(false);
			setYDupl(0.0);
			
			setOrigin(new Double[]{0.0, 0.0, 0.0});
			setScale(new Double[]{1.0, 1.0, 1.0});
			
		}
	}
	

}
