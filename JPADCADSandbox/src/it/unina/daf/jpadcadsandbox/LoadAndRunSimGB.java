package it.unina.daf.jpadcadsandbox;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadAndRunSimGB {

	public static final String macroPath = "D:\\eclipse\\STAR_MACRO\\src\\macro";
	public static final String macroName = "Run_Sim.java"; // Test_MultipleExecutes_GB
	public static final String starExePath = "C:\\Program Files\\CD-adapco\\12.04.011-R8\\STAR-CCM+12.04.011-R8\\star\\bin\\starccm+.exe";
	public static final String starOptions = "-cpubind -power -podkey 2jHU+QkwqexqrAOdVZ6ZzQ -licpath 1999@flex.cd-adapco.com -np 8 -rsh ssh";


	public static void main(String[] args) throws IOException {

		int n_cases=3;

		for(int countm=0; countm<n_cases-2; countm++) {
			for(int c1=0; c1<n_cases-2; c1++) {
				for(int c2=0; c2<n_cases-2; c2++) {
					for(int c3=0; c3<n_cases-2; c3++) {
						for(int c4=0; c4<n_cases-2; c4++) {
							for(int c5=0; c5<n_cases-2; c5++) {
								for(int c6=0; c6<n_cases-2; c6++) {
									for(int counta=0; counta<n_cases-2; counta++) {


										//String simName = "CANARD_WING_"+ c1 +"_"+ c2 +"_"+ c3 +"_"+ c4 +"_"+ c5 +"_"+ c6 +"_"+ countm +"_"+ counta+".sim";
										String simName = "WING_"+ c1 +"_"+ c2 +"_"+ c3 +"_"+ c4 +"_"+ c5 +"_"+ c6 +"_"+ countm +"_"+ counta+".sim";
										
										try {
											Runtime runtime = Runtime.getRuntime();

											Process runTheMacro = runtime.exec(
													"cmd /c cd\\ && cd " + macroPath + " && dir && " + // change directory
															"\"" + starExePath + "\" " +               // run the application
															starOptions + " " +                        // set license and settings
															simName+" -batch " + macroName            // load simulation in batch mode
													); 
											
										 

											BufferedReader input = new BufferedReader(new InputStreamReader(runTheMacro.getInputStream()));

											String line = null;		
											while((line = input.readLine()) != null) System.out.println(line);

											int exitVal = runTheMacro.waitFor();
											System.out.println("Exited with error code " + exitVal);
										}

										catch(Exception e) {
											System.out.println(e.toString());
											e.printStackTrace();
										}


										try
										{ 
											Files.deleteIfExists(Paths.get(macroPath+"\\"+simName));
										} 
										catch(NoSuchFileException e) 
										{ 
											System.out.println("No such file/directory exists"); 
										}


									}
								}
							}
						}
					}
				}
			}
		}
	}
}





