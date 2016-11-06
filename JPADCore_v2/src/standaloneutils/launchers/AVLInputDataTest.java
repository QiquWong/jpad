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
		
		// assign main input data
		AVLMainInputData inputData = new AVLMainInputData
				.Builder()
				.build();
		
		// assign the aircraft as a collection of wings and bodies
		AVLAircraft aircraft = new AVLAircraft
				.Builder()
				.setDescription("The aircraft - agodemar")
				.appendWing( //----------------------------------------------- wing 1
					new AVLWing
						.Builder()
						.setDescription("Main wing")
						.addSections( //-------------------------------------- wing 1 - section 1
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
						.addSections( //-------------------------------------- wing 1 - section 2
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
				.appendWing( //----------------------------------------------- wing 2
					new AVLWing
						.Builder()
						.setDescription("Horizontal tail")
						.setOrigin(new Double[]{15.0, 0.0, 1.25})
						.addSections( //-------------------------------------- wing 2 - section 1
							new AVLWingSection
								.Builder()
								.setDescription("Root section")
								.setAirfoilCoordFile(
									new File(binDirPath + File.separator 
										+ "ag38.dat"
									)
								)
								.setOrigin(new Double[]{0.0, 0.0, 0.0})
								.setChord(1.2)
								.setTwist(0.0)
								.addControlSurfaces(
									new AVLWingSectionControlSurface
										.Builder()
										.setDescription("Elevator")
										.setGain(1.0)
										.setXHinge(0.6)
										.setHingeVector(new Double[]{0.0, 1.0, 0.0})
										.setSignDuplicate(1.0)
										.build()
								)
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
								.setOrigin(new Double[]{0.0, 3.5, 0.0})
								.setChord(1.2)
								.setTwist(0.0)
								.addControlSurfaces(
										new AVLWingSectionControlSurface
											.Builder()
											.setDescription("Elevator")
											.setGain(1.0)
											.setXHinge(0.6)
											.setHingeVector(new Double[]{0.0, 1.0, 0.0})
											.setSignDuplicate(1.0)
											.build()
								)
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

		sb.append(String.format(Locale.ROOT, "%1$-11s %2$-11s %3$-11s", 
				"#IYsym", "IZsym", "ZSym")
				).append("\n");
		sb.append(String.format(Locale.ROOT, "%1$-11d %2$-11d %3$-11.5g", 
				inputData.getIYsym(), inputData.getIZsym(), inputData.getZsym())
				).append("\n");

		sb.append(String.format(Locale.ROOT, "%1$-11s %2$-11s %3$-11s (meters)", 
				"#Sref", "Cref", "Bref")
				).append("\n");
		sb.append(
			String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g %3$-11.5g", 
				inputData.getSref(), inputData.getCref(), inputData.getBref())
				).append("\n");

		sb.append(String.format(Locale.ROOT, "%1$-11s %2$-11s %3$-11s (meters)", 
				"#Xref", "Yref", "Zref")
				).append("\n");
		sb.append(
			String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g %3$-11.5g", 
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
		
		// wing main data
		sb.append(String.format(Locale.ROOT, "%1$-11s %2$-11s %3$-11s %4$-11s", 
				"#Nchord", "Cspace", "Nspan", "Sspace")
				).append("\n");
		sb.append(
			String.format(Locale.ROOT, "%1$-11d %2$-11.5g %3$-11d %4$-11.5g", 
				wing.getConfiguration().getNChordwise(), wing.getConfiguration().getCSpace(), 
				wing.getConfiguration().getNSpanwise(), wing.getConfiguration().getSSpace())
				).append("\n");

		if (wing.isSymmetric()) {
			sb.append("#\n")
			  .append("# reflect image wing about y=0 plane\n")
			  .append("YDUPLICATE\n");
			sb.append("0.0").append("\n");
		}

		sb.append("#\n")
		  .append("# twist angle bias for whole surface\n")
		  .append("ANGLE\n");
		sb.append(wing.getIncidence()).append("\n");

		sb.append("#\n")
		  .append("# x,y,z bias for whole surface\n")
		  .append("TRANSLATE\n");
		sb.append(
				String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g %3$-11.5g", 
					wing.getOrigin()[0], wing.getOrigin()[1], wing.getOrigin()[2])
					).append("\n");
		
		sb.append("#--------------------------------------------------------------\n")
		  .append("#    Xle         Yle         Zle         chord       angle   [Nspan  Sspace]\n")
		  .append("#\n");
		
		// format sections
		wing.getSections().stream()
			.forEach(section ->
				sb.append(formatAsAVLWingSection(section))
			);
		
		sb.append("#\n")
		  .append("#==============================================================\n")
		  .append("#\n");

		return sb.toString();
	}
	
	public static String formatAsAVLWingSection(AVLWingSection section) {
		StringBuilder sb = new StringBuilder();
		sb.append("SECTION").append("\n");
		sb.append(
				String.format(Locale.ROOT, "     %1$-11.5g %2$-11.5g %3$-11.5g %4$-11.5g %5$-11.5g", 
					section.getOrigin()[0], section.getOrigin()[1], section.getOrigin()[2], 
					section.getChord(), section.getTwist())
					).append("\n");
		sb.append("AFIL").append("\n");
		sb.append(section.getAirfoilCoordFile().getName()).append("\n");

		// format controls
		section.getControlSurfaces().stream()
			.forEach(controlSurface ->
				sb.append(formatAsAVLWingSectionControlSurface(controlSurface))
			);
		
		sb.append("#-----------------------\n");

		return sb.toString();
	}

	public static String formatAsAVLWingSectionControlSurface(AVLWingSectionControlSurface controlSurface) {
		StringBuilder sb = new StringBuilder();
		sb.append("CONTROL").append("\n");
		sb.append(
				String.format(Locale.ROOT, "%1$s  %2$-7.4g %3$-7.4g %4$-7.4g %5$-7.4g %6$-7.4g %7$-7.4g", 
					controlSurface.getDescription().replaceAll(" ", "_"), // replace spaces in description
					controlSurface.getGain(), controlSurface.getXHinge(),
					controlSurface.getHingeVector()[0], controlSurface.getHingeVector()[1], controlSurface.getHingeVector()[2],
					controlSurface.getSignDuplicate()
				)
		  ).append(" | name, gain,  Xhinge,  (X,Y,Z)hvec,  SgnDup\n");
		
		return sb.toString();
	}	
}
