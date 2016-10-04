package standaloneutils.launchers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DatcomOutputFileReader extends AbstractOutputFileReader implements IOutputFileReader {

	public DatcomOutputFileReader(String outputFilePath) {
		super(outputFilePath);
	}

	public DatcomOutputFileReader(File outputFile) {
		super(outputFile);
	}
	
	@Override
	public boolean isFileAvailable() {
		return (theFile != null);
	}

	public enum LINE_POSITION {
        MACH_NUMBER(1),
        ALTITUDE(2),
        VELOCITY(3),
        PRESSURE(4),
        TEMPERATURE(5),
        REYNOLDS_NUMBER(6),
        REF_AREA(7)
        ;

        private final int value;

        LINE_POSITION(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }
	
	public enum VARIABLE_NAME {
        MACH_NUMBER("MACH_NUMBER"),
        ALTITUDE("ALTITUDE"),
        VELOCITY("VELOCITY"),
        PRESSURE("PRESSURE"),
        TEMPERATURE("TEMPERATURE"),
        REYNOLDS_NUMBER("REYNOLDS_NUMBER"),
        REF_AREA("REF_AREA")
        ;

        private final String text;

        VARIABLE_NAME(final String newText) {
            this.text = newText;
        }

        @Override
        public String toString() { return text; }
    }

	
	@Override
	public boolean parse() {
		if (theFile != null) { 
			String lenghtUnits = "";
			String derivUnits = "";

			System.out.println("Parsing...\n");
			try (Scanner scanner =  new Scanner(theFile)) {
				// process line-by-line
				while (scanner.hasNextLine()){
					
					// processLine(scanner.nextLine());
					//System.out.println(scanner.nextLine());

					// regular expressions
					// http://www.vogella.com/tutorials/JavaRegularExpressions/article.html
					
					// http://www.journaldev.com/634/regular-expression-in-java-regex-example

					// datcom read
					// https://searchcode.com/codesearch/view/3176213/

					// http://www.dept.aoe.vt.edu/~mason/Mason_f/MRsoft.html
					// http://www.dept.aoe.vt.edu/~mason/Mason_f/747LDs.out

					String line = scanner.nextLine();

					//----------------------------------------------------
					// Find the default dimensions
					if (line
							.matches(".*DIM\\s.*")
							&&
							lenghtUnits.equals("")
							) {
						System.out.println(line);
						String[] splitString = (line.trim().split("\\s+"));
						System.out.println(Arrays.toString(splitString));
						lenghtUnits = splitString[1].toLowerCase();
						System.out.println("Length units: " + lenghtUnits);
					}				

					if (line
							.matches(".*DERIV\\s.*")
							&&
							derivUnits.equals("")
							) {
						System.out.println(line);
						String[] splitString = (line.trim().split("\\s+"));
						System.out.println(Arrays.toString(splitString));
						derivUnits = splitString[1].toLowerCase();
						System.out.println("Derivative units: " + derivUnits);
					}				


					//.matches("\\w.*")
					//.matches("0.*IDEAL ANGLE OF ATTACK =.*")
					//.matches("0.*MACH=.*LIFT-CURVE-SLOPE =.*XAC =.*")

					//----------------------------------------------------
					// Find the block with results
					if (line
							.matches("1.*AUTOMATED STABILITY AND CONTROL METHODS PER APRIL 1976 VERSION OF DATCOM.*")
							) {
						// advance and check if this is a block with results
						line = scanner.nextLine();
						if (line
								.matches(".*CHARACTERISTICS AT ANGLE OF ATTACK AND IN SIDESLIP.*")
								) {
							System.out.println(line);
							// advance 8 lines
							for(int i = 0; i < 8; i++) {
								line = scanner.nextLine();
								System.out.println(line);
							}
							// advance and get row of numbers
							line = scanner.nextLine();
							System.out.println(line);
							String[] splitString = (line.split("\\s+"));
							System.out.println("Values: " + Arrays.toString(splitString));

							// ============================================ Mach number
							
							Double mach = Double.valueOf(splitString[LINE_POSITION.MACH_NUMBER.getValue()]);
							System.out.println("Mach = " + mach);

							List<Number> machList = new ArrayList<Number>();
							machList.add(mach);
							variables.put(VARIABLE_NAME.MACH_NUMBER.toString(), machList);

							// ============================================ Altitude
							
							Double altitude = Double.valueOf(splitString[LINE_POSITION.ALTITUDE.getValue()]);
							System.out.println("Altitude = " + altitude);

							List<Number> altitudeList = new ArrayList<Number>();
							machList.add(altitude);
							variables.put(VARIABLE_NAME.ALTITUDE.toString(), altitudeList);
							
							// TODO: handle -->  ALTITUDE   VELOCITY    PRESSURE    TEMPERATURE

							variables.forEach((key, value) -> {
							    System.out.println("Key : " + key + " Value : " + value);
							});
							
						}
					}
				}
				System.out.println("\n...parsing done");
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} 
		else
			return false;
	}

	
	public static void main(String[] args) {
		// Set the DATCOMROOT environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "Datcom" + File.separator 
				+ "bin" 				
				;
		
		// Assign the input file
		File inputFile = new File(binDirPath + File.separator + "B-737.dcm");
		System.out.println("Input file full path: " + inputFile);
		System.out.println("Input file name: " + inputFile.getName());
		System.out.println("Input file exists? " + inputFile.exists());

		// Assign the output file path string
		String outputFilePath = binDirPath + File.separator + inputFile.getName().replaceFirst(".dcm", ".out");
		
		System.out.println("Output file full path: " + outputFilePath);
		
		// Use DatcomOutputFileReader object
		DatcomOutputFileReader reader = new DatcomOutputFileReader(outputFilePath);
		System.out.println("The Datcom output file is available? " + reader.isFileAvailable());
		System.out.println("The Datcom output file to read: " + reader.getTheFile());
		
		reader.parse();
	}	
}
