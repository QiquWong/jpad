package standaloneutils.launchers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AVLInputData {
	
	String getDescription();
	
	Double getMain_Mach();
	Integer getMain_IYsym();
	Integer getMain_IZsym();
	Double getMain_Zsym();

//	Double getWgplnf_CHRDR();
//	Optional<Double> getWgplnf_CHRDBP();

	
	/** Builder of IDatcomInputData instances. */
	class Builder extends AVLInputData_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("(c) Agostino De Marco - X-airplane");
						
			setMain_IYsym(0);
			setMain_IZsym(0);
			setMain_Zsym(0.0);

			// TODO ... the rest
		}
		
		//--------------------------------------------------------
		// CONSTRAINTS
		// 
		// some of them are for convenience and might be removed
		// at some point in time
		//
		//--------------------------------------------------------
		
		@Override
		public Builder setMain_Mach(Double val) {
			// Check single-field (argument) constraints in the setter method.
			checkArgument((val > 0) && (val <= 0.85));
			return super.setMain_Mach(val);
		}
		@Override 
		public AVLInputData build() {
			AVLInputData data = super.build();
			
			// Check cross-field (state) constraints in the build method.
			//checkState(data.getDescription().contains("Agostino De Marco"));
			
			return data;
		}
	}
}