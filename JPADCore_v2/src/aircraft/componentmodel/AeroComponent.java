package aircraft.componentmodel;

import configuration.enumerations.ComponentEnum;


public abstract class AeroComponent extends Component {
	
	public final String id = null;
	private ComponentEnum _type;
	private boolean _mirrored = true;

	
	public AeroComponent(String name, ComponentEnum type) {
		super(name);
		this._type = type;
	}

	public AeroComponent(String name, String description, ComponentEnum type) {
		super(name,description);
		this._type = type;
	}

	public AeroComponent(
			String name, 
			String description, 
			double x, double y, double z, 
			ComponentEnum type) {
		super("", name, description, x, y, z);
		this._type = type;
	}

	public ComponentEnum getType() {
		return _type;
	}
	public void setType(ComponentEnum type) {
		this._type = type;
	}

	public boolean isMirrored() {
		return _mirrored;
	}

	public void setMirrored(boolean mirrored) {
		this._mirrored = mirrored;
	}

	public static String getId() {
		return null;
	}

}

