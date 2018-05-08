package it.unina.daf.jpadcadsandbox.utils;

public class OperatingConditions {
	private final double angleOfAttack;
	private final double sideslipAngle;
	private final double machNumber;
	private final double reynoldsNumber;
	private final double altitude;
	private final double pressure;
	private final double density;
	private final double temperature;
	private final double speedOfSound;
	private final double dynamicViscosity;
	private final double velocity;
	
	public static class OperatingConditionsBuilder {
		private final double angleOfAttack;
		private final double sideslipAngle;
		private final double machNumber;
		private final double reynoldsNumber;
		private final double altitude;
		private final double pressure;
		private final double density;
		private final double temperature;
		private final double speedOfSound;
		private final double dynamicViscosity;
		private final double velocity;
		
		public OperatingConditionsBuilder(
				double angleOfAttack,
				double sideslipAngle,
				double machNumber,
				double reynoldsNumber,
				double altitude,
				double pressure,
				double density,
				double temperature,
				double speedOfSound,
				double dynamicViscosity,
				double velocity
				) {
			this.angleOfAttack    = angleOfAttack;
			this.sideslipAngle    = sideslipAngle;
			this.machNumber       = machNumber;
			this.reynoldsNumber   = reynoldsNumber;
			this.altitude         = altitude;
			this.pressure         = pressure;
			this.density          = density;
			this.temperature      = temperature;
			this.speedOfSound     = speedOfSound;
			this.dynamicViscosity = dynamicViscosity;
			this.velocity         = velocity;
		}
		
		public OperatingConditions build() {
			return new OperatingConditions(this);
		}
	}

	public OperatingConditions(OperatingConditionsBuilder builder) {
		this.angleOfAttack    = builder.angleOfAttack;
		this.sideslipAngle    = builder.sideslipAngle;
		this.machNumber       = builder.machNumber;
		this.reynoldsNumber   = builder.reynoldsNumber;
		this.altitude         = builder.altitude;
		this.pressure         = builder.pressure;
		this.density          = builder.density;
		this.temperature      = builder.temperature;
		this.speedOfSound     = builder.speedOfSound;
		this.dynamicViscosity = builder.dynamicViscosity;
		this.velocity         = builder.velocity;
	}
	
	public double getAngleOfAttack() {
		return this.angleOfAttack;
	}
	
	public double getSideslipAngle() {
		return this.sideslipAngle;
	}
	
	public double getMachNumber() {
		return this.machNumber;
	}
	
	public double getReynoldsNumber() {
		return this.reynoldsNumber;
	}

	public double getAltitude() {
		return this.altitude;
	}
	
	public double getPressure() {
		return this.pressure;
	}
	
	public double getDensity() {
		return this.density;
	}
	
	public double getTemperature() {
		return this.temperature;
	}
	
	public double getSpeedOfSound() {
		return this.speedOfSound;
	}
	
	public double getDynamicViscosity() {
		return this.dynamicViscosity;
	}
	
	public double getVelocity() {
		return this.velocity;
	}
}
