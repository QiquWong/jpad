package standaloneutils.launchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLAircraft {
	
	String getDescription();
	List<AVLWing> getWings();
	List<AVLBody> getBodies();

	/** Builder of AVLAircraft instances. */
	class Builder extends AVLAircraft_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("AIRCRAFT");
			clearWings();
			addAllWings(new ArrayList<AVLWing>());
			clearBodies();
			addAllBodies(new ArrayList<AVLBody>());
			
			
		}
		public Builder appendWing(AVLWing wing) {
			addWings(wing);
			return this;
		}
		public Builder appendBody(AVLBody body) {
			addBodies(body);
			return this;
		}
	}	

}
