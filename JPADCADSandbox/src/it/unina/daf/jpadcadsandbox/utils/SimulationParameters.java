package it.unina.daf.jpadcadsandbox.utils;

public class SimulationParameters {
	private final String simulationType;
	private final boolean isSymmetrical;
	private final boolean executeMesh;
	
	public static class SimulationParametersBuilder {
		private final String simulationType;
		private final boolean isSymmetrical;
		private final boolean executeMesh;
		
		public SimulationParametersBuilder(
				String simulationType,
				boolean isSymmetrical,
				boolean executeMesh
				) {
			this.simulationType = simulationType;
			this.isSymmetrical  = isSymmetrical;
			this.executeMesh    = executeMesh;
		}
		
		public SimulationParameters build() {
			return new SimulationParameters(this);
		}
	}

	public SimulationParameters(SimulationParametersBuilder builder) {
		this.simulationType = builder.simulationType;
		this.isSymmetrical  = builder.isSymmetrical;
		this.executeMesh    = builder.executeMesh;
	}

	public String getSimulationType() {
		return this.simulationType;
	}
	
	public boolean isSimulationSymmetrical() {
		return this.isSymmetrical;
	}
	
	public boolean executeMeshOperation() {
		return this.executeMesh;
	}
}
