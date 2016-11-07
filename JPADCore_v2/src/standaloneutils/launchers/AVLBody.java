package standaloneutils.launchers;

import java.util.ArrayList;
import java.util.List;

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
	Double[] getOrigin();
	
	List<Double> getLengths();
	Double getLengthTotal();
	Double getLengthNose();
	Double getLengthTail();

	List<Double> getWidths();
	Double getWidthMax();

	List<Double> getHeights();
	Double getHeightMax();
	
	List<AVLBodySection> getSectionsHorizontal();
	List<AVLBodySection> getSectionsVertical();
	
	AVLBodyConfiguration getConfiguration();
	
	/** Builder of AVLBody instances. */
	class Builder extends AVLBody_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("BODY");
			
			setNBody(15);
			setBSpace(1.0);
			
			setDuplicated(false);
			setOrigin(new Double[]{0.0, 0.0, 0.0});
			
			clearLengths();
			addAllLengths(new ArrayList<Double>());
			setLengthTotal(0.0);
			setLengthNose(0.0);
			setLengthTail(0.0);

			clearWidths();
			addAllWidths(new ArrayList<Double>());
			setWidthMax(0.0);

			clearHeights();
			addAllHeights(new ArrayList<Double>());
			setHeightMax(0.0);
			
			clearSectionsHorizontal();
			addAllSectionsHorizontal(new ArrayList<AVLBodySection>());
			clearSectionsVertical();
			addAllSectionsVertical(new ArrayList<AVLBodySection>());

			getConfigurationBuilder().setNSpanwise(10);
			getConfigurationBuilder().setNChordwise(5);
			getConfigurationBuilder().setSSpace(1.0);
			getConfigurationBuilder().setCSpace(1.0);
			
		}
		public Builder appendSection(AVLBodySection section, String orientation) {
			/*
			 * adds a section to the body vertical or horizontal segment
			 */
			if (orientation.toUpperCase().equals("HORIZONTAL"))
				addSectionsHorizontal(section);
			if (orientation.toUpperCase().equals("VERTICAL"))
				addSectionsVertical(section);
			return this;
		}
	}
	

}
