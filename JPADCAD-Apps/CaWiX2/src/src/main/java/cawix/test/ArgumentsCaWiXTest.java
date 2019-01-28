package cawix.test;

import org.kohsuke.args4j.Option;

public class ArgumentsCaWiXTest { // see CompleteAnalysisTest
	
	@Option(name = "-in", aliases = { "--input-number" }, required = true,
			usage = "User provided number")
	private double x;

	public double getX() {
		return x;
	}
	
}
