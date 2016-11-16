package standaloneutils.launchers.avl;

import java.util.ArrayList;
import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

import javaslang.Tuple2;
import javaslang.Tuple4;
import javaslang.Tuple6;

@FreeBuilder
public interface AVLMassInputData {
	
	String getDescription();
	Double getLUnit();
	Double getMUnit();
	Double getTUnit();
	Double getGravityAcceleration();
	Double getFluidDensity();
	
	List<
		Tuple2<
			Tuple4<
				Double, // mass
				Double, // x
				Double, // y
				Double  // z
			>,
			Tuple6<
				Double, // Ixx
				Double, // Iyy
				Double, // Izz
				Double, // Ixy
				Double, // Ixz
				Double  // Iyz
			>
		>
	> getMassProperties();
	
	
	/** Builder of AVLMassInputData instances. */
	class Builder extends AVLMassInputData_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("Mass properties - agodemar");
			setLUnit(1.0);
			setMUnit(1.0);
			setTUnit(1.0);
			setGravityAcceleration(9.81);
			setFluidDensity(1.225);
			
			clearMassProperties();
			addAllMassProperties(new ArrayList<>());
		}
	}

}
