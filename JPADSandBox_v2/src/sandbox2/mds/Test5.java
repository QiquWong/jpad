package sandbox2.mds;

import java.util.Arrays;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Test5 {
	
	public static void main(String[] args) {

		CmdLineParser theCmdLineParser;
		Arguments4MDSTests va;
		
		va = new Arguments4MDSTests();
		theCmdLineParser = new CmdLineParser(va);
		
		try {
			
			theCmdLineParser.parseArgument(args);
			System.out.println("Command line parsed.");
			
			double[] vec1 = Test4.getArrayOfBracketedDoubles(va.getVectorString1());
			System.out.println("Vector 1:");
			for (double d : vec1) {
				System.out.println("double: " + d);					
			}
			
			System.out.println("-----");
			
			if (va.getVectorString2() != null) {
				double[] vec2 = Test4.getArrayOfBracketedDoubles(va.getVectorString2());
				System.out.println("Vector 2:");
				for (double d : vec2) {
					System.out.println("double: " + d);					
				}
			}
			
			System.out.println("-----");
			// parse the rest, just in case...
			
			
		} catch (CmdLineException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println("----------------------------------------");
			System.err.println("Command line arguments: [options...] arguments...");
			theCmdLineParser.printUsage(System.err);
			System.err.println("----------------------------------------");
		}

		
	}

}
