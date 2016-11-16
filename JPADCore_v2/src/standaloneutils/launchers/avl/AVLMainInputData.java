package standaloneutils.launchers.avl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLMainInputData {
	
	String getDescription();
	
	Double getMach();
	Integer getIYsym();
	Integer getIZsym();
	Double getZsym();
	
	Double getSref();
	Double getCref();
	Double getBref();

	Double getXref();
	Double getYref();
	Double getZref();

	Double getCD0ref();
	
//	Double getWgplnf_CHRDR();
//	Optional<Double> getWgplnf_CHRDBP();

	
	/** Builder of AVLInputData instances. */
	class Builder extends AVLMainInputData_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("(c) Agostino De Marco - X-airplane");
			setMach(0.3);
			setIYsym(0);
			setIZsym(0);
			setZsym(0.0);
			setSref(124.862);
			setCref(4.235);
			setBref(35.66);
			setXref(18.288);
			setYref(0.0);
			setZref(0.0);
			setCD0ref(0.020);
			
		}
		
		//--------------------------------------------------------
		// CONSTRAINTS
		// 
		// some of them are for convenience and might be removed
		// at some point in time
		//
		//--------------------------------------------------------
		
		@Override
		public Builder setMach(Double val) {
			// Check single-field (argument) constraints in the setter method.
			checkArgument((val >= 0) && (val <= 0.85));
			return super.setMach(val);
		}
		
		// TODO: more constraints on Sref, Cref, Bref etc as appropriate
		
		@Override 
		public AVLMainInputData build() {
			AVLMainInputData data = super.build();
			
			// Check cross-field (state) constraints in the build method.
			//checkState(data.getDescription().contains("Agostino De Marco"));
			
			return data;
		}
	}
}