package cawix.test;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class TestArgs4j {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		CmdLineParser theCmdLineParser;
		ArgumentsCaWiXTest va;
		
		va = new ArgumentsCaWiXTest();
		theCmdLineParser = new CmdLineParser(va);
		
		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			System.out.println(args.length);
			theCmdLineParser.parseArgument(args);
			
			double a = va.getX();
			System.out.println("Test x = " + a);
			
			
		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
		}	
		

	}

}
