package standaloneutils.launchers.avl;

public class AVLMacro {

	StringBuilder commandSequence = new StringBuilder();

	public AVLMacro() {
	}
	
	public AVLMacro load(String filePath) {
		commandSequence.append("LOAD ").append(filePath).append("\n");
		return this;
	}

	public AVLMacro mass(String filePath) {
		// @masc do nothing when mass file name is null
		if (filePath != null)
			commandSequence.append("MASS ").append(filePath).append("\n");
		return this;
	}
	public AVLMacro mset(Integer value) {
		commandSequence.append("MSET").append("\n")
			.append(value).append("\n");
		return this;
	}
	public AVLMacro plop(String value) {
		commandSequence.append("PLOP").append("\n")
			.append(value).append("\n");
		return this;
	}
	public AVLMacro back() {
		commandSequence.append("\n");
		return this;
	}
	public AVLMacro oper() {
		commandSequence.append("OPER").append("\n");
		return this;
	}
	public AVLMacro c1() {
		commandSequence.append("c1").append("\n");
		return this;
	}
	public AVLMacro velocity(Double value) {
		commandSequence.append("v ").append(value).append("\n");
		return this;
	}
	public AVLMacro lifeCoefficient(Double value) {
		commandSequence.append("cl ").append(value).append("\n");
		return this;
	}
	public AVLMacro runCase() {
		commandSequence.append("x").append("\n");
		return this;
	}
	public AVLMacro stabilityDerivatives(String filePath) {
		// @masc do nothing when stability derivatives file name is null
		if (filePath != null)
			commandSequence.append("st").append("\n")
			.append(filePath).append("\n");
		return this;
	}
	public AVLMacro bodyAxisDerivatives(String filePath) {
		// @masc do nothing when stability body-axis derivatives file name is null
		if (filePath != null)
			commandSequence.append("sb").append("\n")
			.append(filePath).append("\n");
		return this;
	}
	public AVLMacro mode() {
		commandSequence.append("MODE").append("\n");
		return this;
	}
	public AVLMacro newEigenmodeCalculation() {
		commandSequence.append("n").append("\n");
		return this;
	}
	public AVLMacro writeEigenvaluesToFile(String filePath) {
		commandSequence.append("w").append("\n")
			.append(filePath).append("\n");
		return this;
	}
	
	public AVLMacro quit() {
		commandSequence.append("QUIT"); // .append("\n");
		return this;
	}

	public AVLMacro clear() {
		commandSequence.setLength(0);
		return this;
	}
	
	public String getCommands() {
		return commandSequence.toString();
	}
}
