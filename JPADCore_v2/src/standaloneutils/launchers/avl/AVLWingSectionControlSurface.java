package standaloneutils.launchers.avl;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLWingSectionControlSurface {

	String getDescription();
	Double getGain();
	Double getXHinge();
	Double[] getHingeVector();
	/*
	 * 1.0 or -1.0 - the sign of the duplicate control on the mirror wing.
	 *				 Use 1.0 for a mirrored control surface, like an elevator. 
	 *				 Use -1.0 for an aileron.
	 */
	Double getSignDuplicate();
	
	/** Builder of AVLWingSectionControlSurface instances. */
	class Builder extends AVLWingSectionControlSurface_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("CONTROL_SURFACE");
			setGain(0.0);
			setXHinge(0.0);
			setHingeVector(new Double[]{0.0, 0.0, 0.0});
			setSignDuplicate(1.0);
			
		}
	}	
}
