package sandbox2.mds;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Arguments4MDSTests {
	@Option(name = "-vec1", aliases = { "--vector-input-1" }, required = true,
			usage = "an input vector of double, in brackets, e.g. [1, 4.5, -7.5]")
	private String vectorString1;

	@Option(name = "-vec2", aliases = { "--vector-input-2" }, required = false,
			usage = "an input vector of double, in brackets, e.g. [1, 4.5, -7.5]")
	private String vectorString2;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();
	
	public String getVectorString1() {
		return vectorString1;
	}

	public String getVectorString2() {
		return vectorString2;
	}
	
}
