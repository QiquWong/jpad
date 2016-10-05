package standaloneutils.launchers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DatcomOutputFileReader extends AbstractOutputFileReader implements IOutputFileReader {

	private int nAlphas = 0;
	
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

	public enum LINE_POSITION_PARAMS {
        MACH_NUMBER(1),
        ALTITUDE(2),
        VELOCITY(3),
        PRESSURE(4),
        TEMPERATURE(5),
        REYNOLDS_NUMBER_PER_UNITLENGTH(6),
        REF_AREA(7),
        REF_LENGTH_LONG(8),
        REF_LENGTH_LAT(9),
        MOMENT_REF_HORIZ(10),
        MOMENT_REF_VERT(11)
        ;

        private final int value;

        LINE_POSITION_PARAMS(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    }
	
	public enum PARAMS_NAME {
        MACH_NUMBER("MACH_NUMBER"),
        ALTITUDE("ALTITUDE"),
        VELOCITY("VELOCITY"),
        PRESSURE("PRESSURE"),
        TEMPERATURE("TEMPERATURE"),
        REYNOLDS_NUMBER_PER_UNITLENGTH("REYNOLDS_NUMBER_PER_UNITLENGTH"),
        REF_AREA("REF_AREA"),
        REF_LENGTH_LONG("REF_LENGTH_LONG"),
        REF_LENGTH_LAT("REF_LENGTH_LAT"),
        MOMENT_REF_HORIZ("MOMENT_REF_HORIZ"),
        MOMENT_REF_VERT("MOMENT_REF_VERT")
        ;

        private final String text;

        PARAMS_NAME(final String newText) {
            this.text = newText;
        }

        @Override
        public String toString() { return text; }
    }

	public enum AERO_COEFF_NAME {
        ALPHA("ALPHA"),
        CD("CD"),
        CL("CL"),
        CM("CM"),
        CN("CN"),
        CA("CA"),
        XCP("XCP"),
        CLA("CLA"),
        CMA("CMA"),
        CYB("CYB"),
        CNB("CNB"),
        CLB("CLB")
        ;

        private final String text;

        AERO_COEFF_NAME(final String newText) {
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
						String[] splitString = (line.trim().split("\\s+"));
						lenghtUnits = splitString[1].toLowerCase();
						System.out.println("Length units: " + lenghtUnits);
					}				

					if (line
							.matches(".*DERIV\\s.*")
							&&
							derivUnits.equals("")
							) {
						String[] splitString = (line.trim().split("\\s+"));
						derivUnits = splitString[1].toLowerCase();
						System.out.println("Deriv units: " + derivUnits);
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

							// advance 8 lines
							for(int i = 0; i < 8; i++) {
								line = scanner.nextLine();
							}
							
							// advance and get row of numbers
							line = scanner.nextLine();
							String[] splitString0 = (line.split("\\s+"));

							// ============================================ Mach number
							
							Double mach = Double.valueOf(splitString0[LINE_POSITION_PARAMS.MACH_NUMBER.getValue()]);
							//System.out.println("Mach = " + mach);

							List<Number> machList = new ArrayList<Number>();
							machList.add(mach);
							variables.put(PARAMS_NAME.MACH_NUMBER.toString(), machList);

							// ============================================ Altitude
							
							Double altitude = Double.valueOf(splitString0[LINE_POSITION_PARAMS.ALTITUDE.getValue()]);
							//System.out.println("Altitude = " + altitude);

							List<Number> altitudeList = new ArrayList<Number>();
							altitudeList.add(altitude);
							variables.put(PARAMS_NAME.ALTITUDE.toString(), altitudeList);

							// ============================================ Velocity
							
							Double velocity = Double.valueOf(splitString0[LINE_POSITION_PARAMS.VELOCITY.getValue()]);
							//System.out.println("Velocity = " + velocity);

							List<Number> velocityList = new ArrayList<Number>();
							velocityList.add(velocity);
							variables.put(PARAMS_NAME.VELOCITY.toString(), velocityList);

							// ============================================ Pressure
							
							Double pressure = Double.valueOf(splitString0[LINE_POSITION_PARAMS.PRESSURE.getValue()]);
							//System.out.println("Pressure = " + pressure);

							List<Number> pressureList = new ArrayList<Number>();
							pressureList.add(pressure);
							variables.put(PARAMS_NAME.PRESSURE.toString(), pressureList);
							
							// ============================================ Temperature
							
							Double temperature = Double.valueOf(splitString0[LINE_POSITION_PARAMS.TEMPERATURE.getValue()]);
							//System.out.println("Temperature = " + temperature);

							List<Number> temperatureList = new ArrayList<Number>();
							temperatureList.add(temperature);
							variables.put(PARAMS_NAME.TEMPERATURE.toString(), temperatureList);

							// ============================================ Reynolds per unit length
							
							Double reynoldsPerUnitLength = Double.valueOf(splitString0[LINE_POSITION_PARAMS.REYNOLDS_NUMBER_PER_UNITLENGTH.getValue()]);
							//System.out.println("Reynolds per unit length = " + reynoldsPerUnitLength);

							List<Number> reynoldsPerUnitLengthList = new ArrayList<Number>();
							reynoldsPerUnitLengthList.add(reynoldsPerUnitLength);
							variables.put(PARAMS_NAME.REYNOLDS_NUMBER_PER_UNITLENGTH.toString(), reynoldsPerUnitLengthList);
							
							// ============================================ Reference Area
							
							Double referenceArea = Double.valueOf(splitString0[LINE_POSITION_PARAMS.REF_AREA.getValue()]);
							//System.out.println("Reference area = " + referenceArea);

							List<Number> referenceAreaList = new ArrayList<Number>();
							referenceAreaList.add(referenceArea);
							variables.put(PARAMS_NAME.REF_AREA.toString(), referenceAreaList);

							// ============================================ Reference length lon.
							
							Double referenceLengthLong = Double.valueOf(splitString0[LINE_POSITION_PARAMS.REF_LENGTH_LONG.getValue()]);
							//System.out.println("Reference length long. = " + referenceLengthLong);

							List<Number> referenceLengthLongList = new ArrayList<Number>();
							referenceLengthLongList.add(referenceLengthLong);
							variables.put(PARAMS_NAME.REF_LENGTH_LONG.toString(), referenceLengthLongList);
							
							// ============================================ Reference length lat.
							
							Double referenceLengthLat = Double.valueOf(splitString0[LINE_POSITION_PARAMS.REF_LENGTH_LAT.getValue()]);
							//System.out.println("Reference length lat. = " + referenceLengthLat);

							List<Number> referenceLengthLatList = new ArrayList<Number>();
							referenceLengthLatList.add(referenceLengthLat);
							variables.put(PARAMS_NAME.REF_LENGTH_LAT.toString(), referenceLengthLatList);

							// ============================================ Moment ref. horiz.
							
							Double momentReferenceHoriz = Double.valueOf(splitString0[LINE_POSITION_PARAMS.MOMENT_REF_HORIZ.getValue()]);
							//System.out.println("Moment reference horiz. = " + momentReferenceHoriz);

							List<Number> momentReferenceHorizList = new ArrayList<Number>();
							momentReferenceHorizList.add(momentReferenceHoriz);
							variables.put(PARAMS_NAME.MOMENT_REF_HORIZ.toString(), momentReferenceHorizList);
							
							// ============================================ Moment ref. vert.
							
							Double momentReferenceVert = Double.valueOf(splitString0[LINE_POSITION_PARAMS.MOMENT_REF_VERT.getValue()]);
							//System.out.println("Moment reference vert. = " + momentReferenceVert);

							List<Number> momentReferenceVertList = new ArrayList<Number>();
							momentReferenceVertList.add(momentReferenceVert);
							variables.put(PARAMS_NAME.MOMENT_REF_VERT.toString(), momentReferenceVertList);
							
							// Advance 3 more lines
							for(int i = 0; i < 3; i++) {
								line = scanner.nextLine();
							}
							
							// advance and get lines until a '0' is found as first character
							List<String> alphaAeroCoeffLines1 = new ArrayList<>();
							line = scanner.nextLine();
							while (!line
									.matches("^0")
									//.matches("^0.*ALPHA.*Q/QINF.*EPSLON.*D(EPSLON)/D(ALPHA).*") // not working
									) {
								alphaAeroCoeffLines1.add(line);
								line = scanner.nextLine();
							}
							alphaAeroCoeffLines1.remove(alphaAeroCoeffLines1.size()-1); // gotta do this - TODO check
							
							// assign number of alpha's
							nAlphas = alphaAeroCoeffLines1.size();
							
							// get more rows until a '0' is found in first column
							List<String> alphaAeroCoeffLines2 = new ArrayList<>();
							for(int k=0; k<nAlphas; k++) {
								line = scanner.nextLine();
								alphaAeroCoeffLines2.add(line);
							} 

							System.out.println("==============================");
							
							// Parse the stored lines and build lists of coefficients vs list of alpha's 
							System.out.println("Parse the stored lines and build lists of coefficients vs list of alpha's");

							List<Number> alphaList = new ArrayList<Number>();
							List<Number> cdList = new ArrayList<Number>();
							List<Number> clList = new ArrayList<Number>();
							List<Number> cmList = new ArrayList<Number>();
							List<Number> cnList = new ArrayList<Number>();
							List<Number> caList = new ArrayList<Number>();
							List<Number> xcpList = new ArrayList<Number>();
							List<Number> claList = new ArrayList<Number>();
							List<Number> cmaList = new ArrayList<Number>();
							List<Number> cybList = new ArrayList<Number>();
							List<Number> cnbList = new ArrayList<Number>();
							List<Number> clbList = new ArrayList<Number>();

							// parse first line first
							line = alphaAeroCoeffLines1.get(0);
							String[] splitString1a = (line.trim().split("\\s+"));

							if (!splitString1a[0].trim().equals("NA"))
								alphaList.add(Double.valueOf(splitString1a[0].trim()));
							if (!splitString1a[1].trim().equals("NA"))
								cdList.add(Double.valueOf(splitString1a[1]));
							if (!splitString1a[2].trim().equals("NA"))
								clList.add(Double.valueOf(splitString1a[2]));
							if (!splitString1a[3].trim().equals("NA"))
								cmList.add(Double.valueOf(splitString1a[3]));
							if (!splitString1a[4].trim().equals("NA"))
								cnList.add(Double.valueOf(splitString1a[4]));
							if (!splitString1a[5].trim().equals("NA"))
								caList.add(Double.valueOf(splitString1a[5]));
							if (!splitString1a[6].trim().equals("NA"))
								xcpList.add(Double.valueOf(splitString1a[6]));
							if (!splitString1a[7].trim().equals("NA"))
								claList.add(Double.valueOf(splitString1a[7]));
							if (!splitString1a[8].trim().equals("NA"))
								cmaList.add(Double.valueOf(splitString1a[8]));
							if (!splitString1a[9].trim().equals("NA"))
								cybList.add(Double.valueOf(splitString1a[9]));
							if (!splitString1a[10].trim().equals("NA"))
								cnbList.add(Double.valueOf(splitString1a[10]));
							if (!splitString1a[11].trim().equals("NA"))
								clbList.add(Double.valueOf(splitString1a[11]));
							
							// parse the remaining lines
							for (int k=1; k<nAlphas; k++) {
								line = alphaAeroCoeffLines1.get(k);
								String[] splitString1b = (line.trim().split("\\s+"));
								if (!splitString1b[0].trim().equals("NA"))
									alphaList.add(Double.valueOf(splitString1b[0].trim()));
								if (!splitString1b[1].trim().equals("NA"))
									cdList.add(Double.valueOf(splitString1b[1]));
								if (!splitString1b[2].trim().equals("NA"))
									clList.add(Double.valueOf(splitString1b[2]));
								if (!splitString1b[3].trim().equals("NA"))
									cmList.add(Double.valueOf(splitString1b[3]));
								if (!splitString1b[4].trim().equals("NA"))
									cnList.add(Double.valueOf(splitString1b[4]));
								if (!splitString1b[5].trim().equals("NA"))
									caList.add(Double.valueOf(splitString1b[5]));
								if (!splitString1b[6].trim().equals("NA"))
									xcpList.add(Double.valueOf(splitString1b[6]));
								if (!splitString1b[7].trim().equals("NA"))
									claList.add(Double.valueOf(splitString1b[7]));
								if (!splitString1b[8].trim().equals("NA"))
									cmaList.add(Double.valueOf(splitString1b[8]));
								if (!splitString1b[9].trim().equals("NA"))
									clbList.add(Double.valueOf(splitString1b[9]));
							}
							
							// put parsed values into the map of variables
							variables.put(AERO_COEFF_NAME.ALPHA.toString(), alphaList);
							variables.put(AERO_COEFF_NAME.CD.toString(), cdList);
							variables.put(AERO_COEFF_NAME.CL.toString(), clList);
							variables.put(AERO_COEFF_NAME.CM.toString(), cmList);
							variables.put(AERO_COEFF_NAME.CN.toString(), cnList);
							variables.put(AERO_COEFF_NAME.CA.toString(), caList);
							variables.put(AERO_COEFF_NAME.XCP.toString(), xcpList);
							variables.put(AERO_COEFF_NAME.CLA.toString(), claList);
							variables.put(AERO_COEFF_NAME.CMA.toString(), cmaList);
							variables.put(AERO_COEFF_NAME.CYB.toString(), cybList);
							variables.put(AERO_COEFF_NAME.CNB.toString(), cnbList);
							variables.put(AERO_COEFF_NAME.CLB.toString(), clbList);
							
//							System.out.println("Alpha: " + alphaList);
//							System.out.println("CD: " + cdList);
//							System.out.println("CL: " + clList);
//							System.out.println("CM: " + cmList);
//							System.out.println("CN: " + cnList);
//							System.out.println("CA: " + caList);
//							System.out.println("XCP: " + xcpList);
//							System.out.println("CLA: " + claList);
//							System.out.println("CMA: " + cmaList);
//							System.out.println("CYB: " + cybList);
//							System.out.println("CNB: " + cnbList);
//							System.out.println("CLB: " + clbList);
							
							// Print the map of variables
							variables.forEach((key, value) -> {
							    System.out.println(key + " = " + value);
							});
							
							System.out.println("==============================");
							System.out.println("Scanning more lines...");
							line = scanner.nextLine();
							System.out.println(line);
							
							// TODO: parse the rest of the file...
							
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
