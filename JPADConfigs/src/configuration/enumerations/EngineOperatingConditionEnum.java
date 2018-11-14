package configuration.enumerations;

/**
 * Engine operating conditions supported by the application
 * 
 * @author Agostino De Marco
 * 
 */

// see: https://www.mkyong.com/java/java-convert-string-to-enum-object/
	
public enum EngineOperatingConditionEnum {
	TAKE_OFF("TAKE-OFF"),
	APR("APR"),
	CONTINUOUS("CONTINUOUS"),
	CLIMB("CLIMB"),
	CRUISE("CRUISE"),
	DESCENT("DESCENT"),
	FIDL("FILD"),
	GIDL("GIDL"),
	UNKNOWN("UNKNOWN");
	
	private String condition;
	
	EngineOperatingConditionEnum(String cond) { 
		this.condition = cond;
	}
	
	public String condition() {
		return this.condition;
	}
}
