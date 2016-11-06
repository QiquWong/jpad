package standaloneutils.launchers;

import java.io.File;
import java.util.Locale;

public class AVLInputDataTest {

	public static void main(String[] args) {
		
		System.out.println("---------------------------- Test AVL input data");

		// Set the AVLROOT environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "AVL" + File.separator 
				+ "bin" 				
				;
		
		AVLMainInputData inputData = new AVLMainInputData
				.Builder()
				.build();
		
		AVLAircraft aircraft = new AVLAircraft
				.Builder()
				.setDescription("The aircraft - agodemar")
				.appendWing(
					new AVLWing
						.Builder()
						.setDescription("Main wing")
						.addSections(
							new AVLWingSection
								.Builder()
								.setDescription("Root section")
								.setAirfoilCoordFile(
									new File(binDirPath + File.separator 
										+ "ag38.dat"
									)
								)
								.setOrigin(new Double[]{0.0, 0.0, 0.0})
								.setChord(3.0)
								.setTwist(0.0)
								.build()
							)
						.addSections(
								new AVLWingSection
									.Builder()
									.setDescription("Tip section")
									.setAirfoilCoordFile(
										new File(binDirPath + File.separator 
											+ "ag38.dat"
										)
									)
									.setOrigin(new Double[]{0.0, 12.0, 0.0})
									.setChord(1.5)
									.setTwist(0.0)
									.build()
								)
						.build()
					)
				.build();

//		System.out.println("The aircraft:");
//		System.out.println(aircraft);
		
		// Format the AVL input according to SUAVE template
		//     see on https://github.com/suavecode/SUAVE
		//     file: SUAVE/regression/scripts/avl_surrogate_files/aircraft.avl
		System.out.println(formatAsAVLInput(inputData, aircraft));
	}

	public static String formatAsAVLInput(AVLMainInputData inputData, AVLAircraft aircraft) {

		StringBuilder sb = new StringBuilder();
		sb.append(aircraft.getDescription()).append("\n");
		
		// format main configuration data
		sb.append(formatAsAVLMainInputData(inputData));
		
		sb.append("#\n")
		  .append("#==============================================================\n")
		  .append("#\n");
		
		// format wings
		aircraft.getWings().stream()
			.forEach( wing ->
				sb.append(formatAsAVLWing(wing))
			);
		
		// format bodies
		// TODO ...
		
		return sb.toString();
	}
	
	public static String formatAsAVLMainInputData(AVLMainInputData inputData) {
		StringBuilder sb = new StringBuilder();
		sb.append("#Mach").append("\n");
		sb.append(inputData.getMach()).append("\n");

		sb.append(String.format(Locale.ROOT, "%1$-10s %2$-10s %3$-10s", 
				"#IYsym", "IZsym", "ZSym")
				).append("\n");
		sb.append(String.format(Locale.ROOT, "%1$-10d %2$-10d %3$-10.1f", 
				inputData.getIYsym(), inputData.getIZsym(), inputData.getZsym())
				).append("\n");

		sb.append(String.format(Locale.ROOT, "%1$-10s %2$-10s %3$-10s (meters)", 
				"#Sref", "Cref", "Bref")
				).append("\n");
		sb.append(
			String.format(Locale.ROOT, "%1$-10.3f %2$-10.3f %3$-10.3f", 
				inputData.getSref(), inputData.getCref(), inputData.getBref())
				).append("\n");

		sb.append(String.format(Locale.ROOT, "%1$-10s %2$-10s %3$-10s (meters)", 
				"#Xref", "Yref", "Zref")
				).append("\n");
		sb.append(
			String.format(Locale.ROOT, "%1$-10.3f %2$-10.3f %3$-10.3f", 
				inputData.getXref(), inputData.getYref(), inputData.getZref())
				).append("\n");

		sb.append("#CD0ref").append("\n");
		sb.append(inputData.getCD0ref()).append("\n");
		
		return sb.toString();
	}
	
	public static String formatAsAVLWing(AVLWing wing) {
		StringBuilder sb = new StringBuilder();
		sb.append("SURFACE").append("\n");
		sb.append(wing.getDescription()).append("\n");
		
		// TODO: wing main data
		sb.append("#Nchord   Cspace   Nspan  Sspace").append("\n");
		sb.append("to-do").append("\n");
		
		// TODO: sections
		// format wings
		wing.getSections().stream()
			.forEach(section ->
				// TODO: sb.append(formatAsAVLSection(section))
				sb.append("section ...").append("\n")
			);

		
		return sb.toString();
	}
	
}
