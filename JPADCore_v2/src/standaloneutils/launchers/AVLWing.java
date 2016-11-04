package standaloneutils.launchers;

import java.util.ArrayList;
import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLWing {

	String getDescription();
	boolean isSymmetric();
	boolean isVertical();
	Double[] getOrigin();
	Double getSweep();
	Double getDihedral();
	AVLWingConfiguration getConfiguration();
	List<AVLWingSection> getSections();
	
	
	/** Builder of AVLInputData instances. */
	class Builder extends AVLWing_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("WING");
			setSymmetric(true);
			setVertical(false);
			setOrigin(new Double[]{0.0, 0.0, 0.0});
			setSweep(0.0);
			setDihedral(0.0);
			
			getConfigurationBuilder().setNSpanwise(10);
			getConfigurationBuilder().setNChordwise(5);
			getConfigurationBuilder().setSSpace(1.0);
			getConfigurationBuilder().setCSpace(1.0);
			
			clearSections();
			addAllSections(new ArrayList<AVLWingSection>());

		}
		public Builder appendSection(AVLWingSection section) {
			addSections(section);
			return this;
		}
	}
	
	
}
