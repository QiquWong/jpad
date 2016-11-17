package standaloneutils.launchers.avl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import standaloneutils.launchers.AbstractOutputFileReader;
import standaloneutils.launchers.IOutputFileReader;
import standaloneutils.launchers.datcom.DatcomOutputFileReader.LINE_POSITION_PARAMS;
import standaloneutils.launchers.datcom.DatcomOutputFileReader.PARAMS_NAME;

public class AVLOutputStabilityDerivativesFileReader  extends AbstractOutputFileReader implements IOutputFileReader  {

	public AVLOutputStabilityDerivativesFileReader(String outputFilePath) {
		super(outputFilePath);
	}

	public AVLOutputStabilityDerivativesFileReader(File outputFile) {
		super(outputFile);
	}
	
	@Override
	public boolean isFileAvailable() {
		return (theFile != null);
	}
	
	@Override
	public boolean parse() {
		if (theFile != null) { 

			System.out.println("==============================");
			System.out.println("Parsing " + theFile.getAbsolutePath() + "...");
			
			try (Scanner scanner =  new Scanner(theFile)) {
				// process line-by-line
				while (scanner.hasNextLine()){
					String line = scanner.nextLine();
					
					//System.out.println(line);
					
					//----------------------------------------------------
					// Find the default dimensions
					if (line.matches(".*Sref =\\s.*")) {
						
						System.out.println(line);
						
						// String[] splitString = line.trim().split("\\s+");
						StringTokenizer st = new StringTokenizer(line,"'Sref =''Cref =''Bref ='");
//						while(st.hasMoreTokens()){
//				            System.out.println(st.nextToken());
//				        }
						System.out.println("N. tokens = " + st.countTokens());
						if (st.countTokens() == 3) {
							// ============================================ Sref
							Double sref = Double.valueOf(st.nextToken());
							System.out.println("Sref = " + sref);
							List<Number> srefList = new ArrayList<Number>();
							srefList.add(sref);
							variables.put("Sref", srefList);
							// ============================================ Cref

							// ============================================ Bref
							
						}
					}				

				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
		    return true;
		} 
		else // theFile is null
			return false;
	}

	public static void main(String[] args) {
		// Set the AVL environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "AVL" + File.separator 
				+ "bin" 				
				;
		// Assign the input file
		File inputFile = new File(binDirPath + File.separator + "allegro.run");
		System.out.println("Input file full path: " + inputFile);
		System.out.println("Input file name: " + inputFile.getName());
		System.out.println("Input file exists? " + inputFile.exists());

		// Assign the output file path string
		String outputFilePath = binDirPath + File.separator + inputFile.getName().replaceFirst(".run", ".st");
		
		System.out.println("Output file full path: " + outputFilePath);
		
		// Use AVLOutputStabilityDerivativesFileReader object
		AVLOutputStabilityDerivativesFileReader reader = new AVLOutputStabilityDerivativesFileReader(outputFilePath);
		System.out.println("The Datcom output file is available? " + reader.isFileAvailable());
		System.out.println("The Datcom output file to read: " + reader.getTheFile());
		
		// parse the file and build map of variables & values
		reader.parse();
		
		// print the map
		Map<String, List<Number>> variables = reader.getVariables();
		// Print the map of variables
		variables.forEach((key, value) -> {
		    System.out.println(key + " = " + value);
		});		
		
	}
}
