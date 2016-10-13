package standaloneutils.launchers;

public enum DatcomEngineType {
	JET("JET"),
	PROP("PROP");

	private final String text;
	
	DatcomEngineType(final String newText) {
		this.text = newText;
	}

	@Override
	public String toString() { return text; }
}
