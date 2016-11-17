package standaloneutils.launchers.avl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
					
					Pattern p;
					Matcher m;

					
					String line = scanner.nextLine();
					
					// System.out.println(line);
					
					//----------------------------------------------------
					// Find the reference values
					p = Pattern.compile(".*Sref =\\s.*");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("Sref =", "")
								.replaceAll("Cref =", "")
								.replaceAll("Bref =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ Sref
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Sref", list1);
							// ============================================ Cref
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cref", list2);
							// ============================================ Bref
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("Bref", list3);							
						}
					}
					p = Pattern.compile(".*Xref =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("Xref =", "")
								.replaceAll("Yref =", "")
								.replaceAll("Zref =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ Xref
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Xref", list1);
							// ============================================ Yref
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Yref", list2);
							// ============================================ Zref
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("Zref", list3);							
						}
					}				
					//----------------------------------------------------
					// Find the nominal values
					p = Pattern.compile(".*Alpha =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("Alpha =", "")
								.replaceAll("pb/2V =", "")
								.replaceAll("p\'b/2V =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ Alpha
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Alpha", list1);
							// ============================================ pb/2V
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("pb/2V", list2);
							// ============================================ p'b/2V
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("p\'b/2V", list3);							
						}
					}
					p = Pattern.compile(".*Beta  =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("Beta  =", "")
								.replaceAll("qc/2V =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ Beta
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Beta", list1);
							// ============================================ qc/2V
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("qc/2V", list2);
						}
					}
					p = Pattern.compile(".*Mach  =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("Mach  =", "")
								.replaceAll("rb/2V =", "")
								.replaceAll("r\'b/2V =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ Mach
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Mach", list1);
							// ============================================ rb/2V
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("rb/2V", list2);
							// ============================================ r'b/2V
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("r\'b/2V", list3);							
						}
					}
					p = Pattern.compile(".*CXtot =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CXtot =", "")
								.replaceAll("Cltot =", "")
								.replaceAll("Cl\'tot =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ CXtot
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CXtot", list1);
							// ============================================ Cltot
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cltot", list2);
							// ============================================ Cl'tot
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("Cl\'tot", list3);							
						}
					}
					p = Pattern.compile(".*CYtot =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CYtot =", "")
								.replaceAll("Cmtot =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ CYtot
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CYtot", list1);
							// ============================================ Cmtot
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cmtot", list2);
						}
					}
					p = Pattern.compile(".*CZtot =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CZtot =", "")
								.replaceAll("Cntot =", "")
								.replaceAll("Cn\'tot =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ CZtot
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CZtot", list1);
							// ============================================ Cntot
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cntot", list2);
							// ============================================ Cn'tot
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("Cn\'tot", list3);							
						}
					}
					p = Pattern.compile(".*CLtot =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CLtot =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 1) {
							// ============================================ CLtot
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CLtot", list1);
						}
					}
					p = Pattern.compile(".*CDtot =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CDtot =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 1) {
							// ============================================ CLtot
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CDtot", list1);
						}
					}
					p = Pattern.compile(".*CDvis =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CDvis =", "")
								.replaceAll("CDind =", "");
						// System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ CDvis
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CDvis", list1);
							// ============================================ CDind
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("CDind", list2);
						}
					}
					p = Pattern.compile(".*CLff  =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CLff  =", "")
								.replaceAll("CDff  =", "")
								.replaceAll("\\| Trefftz", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ CLff
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CLff", list1);
							// ============================================ CDff
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("CDff", list2);
						}
					}
					p = Pattern.compile(".*CYff  =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("CYff  =", "")
								.replaceAll("e =", "")
								.replaceAll("\\| Plane", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ CYff
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CYff", list1);
							// ============================================ e
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("e", list2);
						}
					}
					p = Pattern.compile(".*elevator.*=");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("elevator.*=", "")
								;
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 1) {
							// ============================================ elevator
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("elevator", list1);
						}
					}
					p = Pattern.compile(".*rudder.*=");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll("rudder.*=", "")
								;
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 1) {
							// ============================================ elevator
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("rudder", list1);
						}
					}
					//----------------------------------------------------
					// Stability-axis derivatives...
					p = Pattern.compile(".*CLa =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*CLa =", "")
								.replaceAll("CLb =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ CLa
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CLa", list1);
							// ============================================ CLb
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("CLb", list2);
						}
					}
					p = Pattern.compile(".*CYa =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*CYa =", "")
								.replaceAll("CYb =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ CYa
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CYa", list1);
							// ============================================ CYb
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("CYb", list2);
						}
					}
					p = Pattern.compile(".*Cla =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*Cla =", "")
								.replaceAll("Clb =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ Cla
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Cla", list1);
							// ============================================ Clb
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Clb", list2);
						}
					}
					p = Pattern.compile(".*Cma =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*Cma =", "")
								.replaceAll("Cmb =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ Cma
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Cma", list1);
							// ============================================ Cmb
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cmb", list2);
						}
					}
					p = Pattern.compile(".*Cna =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*Cna =", "")
								.replaceAll("Cnb =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 2) {
							// ============================================ Cna
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Cna", list1);
							// ============================================ Cnb
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cnb", list2);
						}
					}
					p = Pattern.compile(".*CLp =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*CLp =", "")
								.replaceAll("CLq =", "")
								.replaceAll("CLr =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ CLp
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CLp", list1);
							// ============================================ CLq
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("CLq", list2);
							// ============================================ CLr
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("CLr", list3);
						}
					}
					p = Pattern.compile(".*CYp =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*CYp =", "")
								.replaceAll("CYq =", "")
								.replaceAll("CYr =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ CYp
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("CYp", list1);
							// ============================================ CYq
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("CYq", list2);
							// ============================================ CYr
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("CYr", list3);
						}
					}
					p = Pattern.compile(".*Clp =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*Clp =", "")
								.replaceAll("Clq =", "")
								.replaceAll("Clr =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ Clp
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Clp", list1);
							// ============================================ Clq
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Clq", list2);
							// ============================================ Clr
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("Clr", list3);
						}
					}
					p = Pattern.compile(".*Cmp =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*Cmp =", "")
								.replaceAll("Cmq =", "")
								.replaceAll("Cmr =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ Cmp
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Cmp", list1);
							// ============================================ Cmq
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cmq", list2);
							// ============================================ Cmr
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("Cmr", list3);
						}
					}
					p = Pattern.compile(".*Cnp =");
					m = p.matcher(line);
					if (m.find()) {
						System.out.println(line);
						String line1 = line
								.replaceAll(".*Cnp =", "")
								.replaceAll("Cnq =", "")
								.replaceAll("Cnr =", "");
						//System.out.println(line1);
						String[] splitString = line1.trim().split("\\s+");
						// System.out.println("N. tokens = " + splitString.length);
						// System.out.println(Arrays.toString(splitString));
						if (splitString.length == 3) {
							// ============================================ Cnp
							Double value = Double.valueOf(splitString[0]);
							List<Number> list1 = new ArrayList<Number>();
							list1.add(value);
							variables.put("Cnp", list1);
							// ============================================ Cnq
							value = Double.valueOf(splitString[1]);
							List<Number> list2 = new ArrayList<Number>();
							list2.add(value);
							variables.put("Cnq", list2);
							// ============================================ Cnr
							value = Double.valueOf(splitString[2]);
							List<Number> list3 = new ArrayList<Number>();
							list3.add(value);
							variables.put("Cnr", list3);
						}
					}
					
					// TODO : parse the rest DC(*)/d(^), with *={L,Y,l,m,n,Dff,e} and ^={elevator,rudder}={d1,d2}
					
					
				} // end-of scanner.hasNextLine()
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
		System.out.println("------ Map of variables ------");
		Map<String, List<Number>> variables = reader.getVariables();
		// Print the map of variables
		variables.forEach((key, value) -> {
		    System.out.println(key + " = " + value);
		});		
		
	}
}
