package sandbox2.vt.pso;

import java.util.List;
import org.inferred.freebuilder.FreeBuilder;


@FreeBuilder
public interface Particle {

	List<Double> getPosition();
	List<Double> getVelocity();
	Double getCostFunctionValue();
	List<Double> getBestPosition();
	Double getBestCostFunctionValue();
	
	class Builder extends Particle_Builder {
		public Builder() {
			// Set defaults in the builder constructor.
//			setPosition();
//			setVelocity();
//			setCostFunctionValue();
//			setBestPosition();
//			setBestCostFunctionValue();
		}
	}
	
}
