package writers;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface AircraftSaveDirectives {
	String getAircraftFileName();
	String getWingFileName();
	String getHTailFileName();
	String getVTailFileName();
	String getCanardFileName();
	String getFuselageFileName();
	String getCabinConfigurationFileName();
	String getNacelleFileName();
	String getEngineFileName();
	String getLandingGearFileName();
	String getSystemFileName();
	
	class Builder extends AircraftSaveDirectives_Builder {
		// NOTE: pass a string to the Builder object to be appended to all names
		// example: "_1" ==> "aircraft_1.xml", "wing_1.xml" etc.
		public Builder(String... args) {
			String appendToName = "";
			if (args.length > 0)
				appendToName = args[0];
			// Set defaults in the builder constructor.
			setAircraftFileName("aircraft"+ appendToName + ".xml");
			setWingFileName("wing"+ appendToName + ".xml");
			setHTailFileName("htail"+ appendToName + ".xml");
			setVTailFileName("vtail"+ appendToName + ".xml");
			setCanardFileName("canard"+ appendToName + ".xml");
			setFuselageFileName("fuselage"+ appendToName + ".xml");
			setCabinConfigurationFileName("cabin_configuration"+ appendToName + ".xml");
			setNacelleFileName("nacelle"+ appendToName + ".xml");
			setEngineFileName("engine"+ appendToName + ".xml");
			setLandingGearFileName("landing_gear"+ appendToName + ".xml");
			setSystemFileName("system"+ appendToName + ".xml");
		}
	}	

}
