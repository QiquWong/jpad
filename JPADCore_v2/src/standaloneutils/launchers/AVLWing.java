package standaloneutils.launchers;

import java.util.ArrayList;
import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLWing {

	String getDescription();
	
	Integer getNSpanwise();
	Integer getNChordwise();
	Double getSSpace();
	Double getCSpace();
	
	boolean isSymmetric();
	boolean isVertical();
	Double[] getOrigin();
	Double[] getScale();
	Double getSweep();
	Double getDihedral();
	Double getIncidence();
	List<AVLWingSection> getSections();
	
	
	/** Builder of AVLInputData instances. */
	class Builder extends AVLWing_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("WING");
			setNSpanwise(10);
			setNChordwise(5);
			setSSpace(1.0);
			setCSpace(1.0);
			setSymmetric(true);
			setVertical(false);
			setOrigin(new Double[]{0.0, 0.0, 0.0});
			setScale(new Double[]{1.0, 1.0, 1.0});
			setSweep(0.0);
			setDihedral(0.0);
			setIncidence(0.0);
			
			clearSections();
			addAllSections(new ArrayList<AVLWingSection>());

		}
		public Builder appendSection(AVLWingSection section) {
			addSections(section);
			return this;
		}
	}
	
	
}
