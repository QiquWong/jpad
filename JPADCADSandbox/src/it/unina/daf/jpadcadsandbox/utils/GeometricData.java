package it.unina.daf.jpadcadsandbox.utils;

public class GeometricData {
	private final String cadUnits;
	private final String components;
	private final String componentsNumber;
	private final double fuselageLength;
	private final double meanAerodynamicChord;
	private final double wingSurface;
	private final double wingSpan;
	private final double momentPoleXCoord;

	public static class GeometricDataBuilder {
		private final String cadUnits;
		private final String components;
		private final String componentsNumber;
		private final double fuselageLength;
		private final double meanAerodynamicChord;
		private final double wingSurface;
		private final double wingSpan;
		private final double momentPoleXCoord;

		public GeometricDataBuilder(
				String cadUnits,
				String components,
				String componentsNumber,
				double fuselageLength,
				double meanAerodynamicChord,
				double wingSurface,
				double wingSpan,
				double momentPoleXCoord
				) {
			this.cadUnits             = cadUnits;
			this.components           = components;
			this.componentsNumber     = componentsNumber;
			this.fuselageLength       = fuselageLength;
			this.meanAerodynamicChord = meanAerodynamicChord;
			this.wingSurface          = wingSurface;
			this.wingSpan             = wingSpan;
			this.momentPoleXCoord     = momentPoleXCoord;
		}

		public GeometricData build() {
			return new GeometricData(this);
		}
	}

	public GeometricData(GeometricDataBuilder builder) {
		this.cadUnits             = builder.cadUnits;
		this.components           = builder.components;
		this.componentsNumber     = builder.componentsNumber;
		this.fuselageLength       = builder.fuselageLength;
		this.meanAerodynamicChord = builder.meanAerodynamicChord;
		this.wingSurface          = builder.wingSurface;
		this.wingSpan             = builder.wingSpan;
		this.momentPoleXCoord     = builder.momentPoleXCoord;
	}
	
	public String getCADUnits() {
		return this.cadUnits;
	}
	
	public String getComponents() {
		return this.components;
	}
	
	public String getComponentsNumber() {
		return this.componentsNumber;
	}
	
	public double getFuselageLength() {
		return this.fuselageLength;
	}

	public double getMeanAerodynamicChord() {
		return this.meanAerodynamicChord;
	}

	public double getWingSurface() {
		return this.wingSurface;
	}

	public double getWingSpan() {
		return this.wingSpan;
	}

	public double getMomentPoleXCoord() {
		return this.momentPoleXCoord;
	}

}
