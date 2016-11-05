package standaloneutils.launchers;

import java.io.File;

public class AVLInputDataTest {

	public static void main(String[] args) {
		
		System.out.println("------- Test AVL input data");

		// Set the AVLROOT environment variable
		String binDirPath = System.getProperty("user.dir") + File.separator  
				+ "src" + File.separator 
				+ "standaloneutils" + File.separator 
				+ "launchers" + File.separator 
				+ "apps" + File.separator 
				+ "AVL" + File.separator 
				+ "bin" 				
				;
		
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
		
		System.out.println(formatAsAVLInput(aircraft));
	}

	public static String formatAsAVLInput(AVLAircraft aircraft) {

		StringBuilder sb = new StringBuilder();
		sb.append("# Description: " + aircraft.getDescription()).append("\n");
		
		aircraft.getWings().stream()
			.forEach( ac ->
				sb.append("# ...")
			);
		
		return sb.toString();
	}
	
}
