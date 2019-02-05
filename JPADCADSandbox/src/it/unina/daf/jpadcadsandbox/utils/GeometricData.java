package it.unina.daf.jpadcadsandbox.utils;

public class GeometricData {
	private final String cadUnits;
	private final String components;
	private final String componentsNumber;
	private final double fuselageLength;
	private final double meanAerodynamicChord;
	private final double wingRootChord;
	private final double wingTaperRatio;
	private final double wingSurface;
	private final double wingSpan;
	private final double momentPoleXCoord;
	private final double xPosWing;
	private final double zPosWing;
	private final double sweepWing;
	private final double dihedralWing;
	private final double riggingWing;
	private final double xPosCanard;
	private final double zPosCanard;
	private final double canardRootChord;
	private final double canardTaperRatio;
	private final double spanCanard;
	private final double sweepCanard;
	private final double dihedralCanard;
	private final double riggingCanard;
	
	
	

	public static class GeometricDataBuilder {
		private final String cadUnits;
		private final String components;
		private final String componentsNumber;
		private final double fuselageLength;
		private final double meanAerodynamicChord;
		private final double wingRootChord;
		private final double wingTaperRatio;
		private final double wingSurface;
		private final double wingSpan;
		private final double momentPoleXCoord;
		private final double xPosWing;
		private final double zPosWing;
		private final double sweepWing;
		private final double dihedralWing;
		private final double riggingWing;
		private final double xPosCanard;
		private final double zPosCanard;
		private final double canardRootChord;
		private final double canardTaperRatio;
		private final double spanCanard;
		private final double sweepCanard;
		private final double dihedralCanard;
		private final double riggingCanard;
		
		

		public GeometricDataBuilder(
				String cadUnits,
				String components,
				String componentsNumber,
				double fuselageLength,
				double meanAerodynamicChord,
				double wingRootChord,
				double wingTaperRatio,
				double wingSurface,
				double wingSpan,
				double momentPoleXCoord,
				double xPosWing,
				double zPosWing,
				double sweepWing,
				double dihedralWing,
				double riggingWing,
				double xPosCanard,
				double zPosCanard,
				double canardRootChord,
				double canardTaperRatio,
				double spanCanard,
				double sweepCanard,
				double dihedralCanard,
				double riggingCanard
				
			   
				) {
			this.cadUnits             = cadUnits;
			this.components           = components;
			this.componentsNumber     = componentsNumber;
			this.fuselageLength       = fuselageLength;
			this.meanAerodynamicChord = meanAerodynamicChord;
			this.wingRootChord        = wingRootChord;
			this.wingTaperRatio       = wingTaperRatio;
			this.wingSurface          = wingSurface;
			this.wingSpan             = wingSpan;
			this.momentPoleXCoord     = momentPoleXCoord;
			this.xPosWing             = xPosWing;
			this.zPosWing             = zPosWing;
			this.sweepWing            = sweepWing;
			this.dihedralWing         = dihedralWing;
			this.riggingWing          = riggingWing;
			this.xPosCanard           = xPosCanard;
			this.zPosCanard           = zPosCanard;
			this.canardRootChord      = canardRootChord;
			this.canardTaperRatio     = canardTaperRatio;
			this.spanCanard           = spanCanard;
			this.sweepCanard          = sweepCanard;
			this.dihedralCanard       = dihedralCanard;
			this.riggingCanard        = riggingCanard;
			
			
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
		this.wingRootChord        = builder.wingRootChord;
		this.wingTaperRatio       = builder.wingTaperRatio;
		this.wingSurface          = builder.wingSurface;
		this.wingSpan             = builder.wingSpan;
		this.momentPoleXCoord     = builder.momentPoleXCoord;
		this.xPosWing             = builder.xPosWing; 
		this.zPosWing             = builder.zPosWing; 
		this.sweepWing            = builder.sweepWing; 
		this.dihedralWing         = builder.dihedralWing; 
		this.riggingWing          = builder.riggingWing;
		this.xPosCanard           = builder.xPosCanard; 
		this.zPosCanard           = builder.zPosCanard;
		this.canardRootChord      = builder.canardRootChord;
		this.canardTaperRatio     = builder.canardTaperRatio;
		this.spanCanard           = builder.spanCanard;
		this.sweepCanard          = builder.sweepCanard; 
		this.dihedralCanard       = builder.dihedralCanard; 
		this.riggingCanard        = builder.riggingCanard;
		
		
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
	
	public double getwingRootChord() {
		return this.wingRootChord;
	}
	
	public double getwingTaperRatio() {
		return this.wingTaperRatio;
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

	public double getxPosWing() {
		return this.xPosWing;
	}
	
    public double getzPosWing() {
		return this.zPosWing;
	}
	
	public double getsweepWing() {
		return this.sweepWing;
	}
	
	public double getdihedralWing() {
		return this.dihedralWing;
	}
	
	public double getriggingWing() {
		return this.riggingWing;
	}
	
	
	public double getxPosCanard() {
		return this.xPosCanard;
	}
	
    public double getzPosCanard() {
		return this.zPosCanard;
	}
    
	public double getcanardRootChord() {
		return this.canardRootChord;
	}
	
	public double getcanardTaperRatio() {
		return this.canardTaperRatio;
	}
	
    public double getspanCanard() {
    	return this.spanCanard;
    }
    
    public double getsweepCanard() {
		return this.sweepCanard;
	}
	
	public double getdihedralCanard() {
		return this.dihedralCanard;
	}
	
	public double getriggingCanard() {
		return this.riggingCanard;
	}
	

	

		
	
	
}
