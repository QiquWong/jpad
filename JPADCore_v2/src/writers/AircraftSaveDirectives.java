package writers;

import java.util.List;

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
	List<String> getWingAirfoilFileNames();
	List<String> getHTailAirfoilFileNames();
	List<String> getVTailAirfoilFileNames();
	List<String> getCanardAirfoilFileNames();
	
	class Builder extends AircraftSaveDirectives_Builder {
		// NOTE: pass a string to the Builder object to be appended to all names
		// example: "_1" ==> "aircraft_1.xml", "wing_1.xml" etc.
		public Builder(String... args) {
			String appendToName = "";
			if (args.length > 0)
				appendToName = args[0];
			// Set defaults in the builder constructor.
			setAircraftFileName("aircraft"+ appendToName);
			setWingFileName("wing"+ appendToName);
			setHTailFileName("htail"+ appendToName);
			setVTailFileName("vtail"+ appendToName);
			setCanardFileName("canard"+ appendToName);
			setFuselageFileName("fuselage"+ appendToName);
			setCabinConfigurationFileName("cabin_configuration"+ appendToName);
			setNacelleFileName("nacelle"+ appendToName + ".xml");
			setEngineFileName("engine"+ appendToName + ".xml");
			setLandingGearFileName("landing_gear"+ appendToName);
		}
	}	

}
