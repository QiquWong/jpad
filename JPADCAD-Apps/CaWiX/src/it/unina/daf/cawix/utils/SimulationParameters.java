package it.unina.daf.cawix.utils;


public class SimulationParameters {
	private final String simulationType;
	private final boolean isSymmetrical;
	private final boolean executeMesh;
	private final int xPos_Case;
	private final int zPos_Case;
	private final int span_Case;
	private final int sweep_Case;
	private final int dihedral_Case;
	private final int rigging_Case;
	private final int mach_Case;
	private final int alpha_Case;
	
	public static class SimulationParametersBuilder {
		private final String simulationType;
		private final boolean isSymmetrical;
		private final boolean executeMesh;
		private final int xPos_Case;
		private final int zPos_Case;
		private final int span_Case;
		private final int sweep_Case;
		private final int dihedral_Case;
		private final int rigging_Case;
		private final int mach_Case;
		private final int alpha_Case;
		
		public SimulationParametersBuilder(
				String simulationType,
				boolean isSymmetrical,
				boolean executeMesh,
				int xPos_Case,
				int zPos_Case,
				int span_Case,
				int sweep_Case,
				int dihedral_Case,
				int rigging_Case,
				int mach_Case,
				int alpha_Case
				) {
			this.simulationType = simulationType;
			this.isSymmetrical  = isSymmetrical;
			this.executeMesh    = executeMesh;
			this.xPos_Case      = xPos_Case;
			this.zPos_Case      = zPos_Case;
			this.span_Case      = span_Case;
			this.sweep_Case     = sweep_Case;
			this.dihedral_Case  = dihedral_Case;
			this.rigging_Case   = rigging_Case;
			this.mach_Case      = mach_Case;
			this.alpha_Case     = alpha_Case;
		}
		
		public SimulationParameters build() {
			return new SimulationParameters(this);
		}
	}

	public SimulationParameters(SimulationParametersBuilder builder) {
		this.simulationType = builder.simulationType;
		this.isSymmetrical  = builder.isSymmetrical;
		this.executeMesh    = builder.executeMesh;
		this.xPos_Case      = builder.xPos_Case;
		this.zPos_Case      = builder.zPos_Case;
		this.span_Case      = builder.span_Case;
		this.sweep_Case     = builder.sweep_Case;
		this.dihedral_Case  = builder.dihedral_Case;
		this.rigging_Case   = builder.rigging_Case;
		this.mach_Case      = builder.mach_Case;
		this.alpha_Case     = builder.alpha_Case;
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
	
	public int xPos_Case() {
		return this.xPos_Case;
	}

	public int zPos_Case() {
		return this.zPos_Case;
	}

	public int span_Case() {
		return this.span_Case;
	}

	public int sweep_Case() {
		return this.sweep_Case;
	}

	public int dihedral_Case() {
		return this.dihedral_Case;
	}

	public int rigging_Case() {
		return this.rigging_Case;
	}

	public int mach_Case() {
		return this.mach_Case;
	}

	public int alpha_Case() {
		return this.alpha_Case;
	}

}
