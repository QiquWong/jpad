package standaloneutils.launchers.avl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class AVLInputGenerator {

	public static void writeDataToAVLFile(AVLMainInputData inputData, AVLAircraft aircraft, String filePath) {
		
		Path path = Paths.get(filePath);

		System.out.println("Writing " + path + " ...");

		// first delete file if exists
		boolean pathExists =
				Files.exists(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
		if (pathExists) {
			try {
				System.out.println("File already exists. Overwriting ...");
				Files.delete(path);
			} catch (IOException e) {
				// Some sort of failure, such as permissions.
				e.printStackTrace();
			}
		}

		try {
			// Create the empty file with default permissions, etc.
			Files.createFile(path);
		} catch (IOException e) {
			// Some sort of failure, such as permissions.
			// System.err.format("createFile error: %s%n", e);
			e.printStackTrace();
		}
		
		// transfer the AVL input data structures into a formatted string
		String lines[] = 
				AVLInputGenerator.formatAsAVLInput(inputData, aircraft) // format one single string with newlines
				.split("\\r?\\n");
		List<String> content = Arrays.stream(lines).collect(Collectors.toList()); // split in a list of strings
		
		// write out the content

		System.out.println("======================================");

		Charset charset = Charset.forName("utf-8");
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			for (String line : content) {
				System.out.println(line);
				writer.write(line, 0, line.length());
				writer.newLine();
			}
			System.out.println("======================================");
			System.out.println("... done.");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeTemplateAVLFile(String filePath) {
		Path path = Paths.get(filePath);

		System.out.println("Writing " + path + " ...");

		// first delete file if exists
		boolean pathExists =
				Files.exists(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
		if (pathExists) {
			try {
				System.out.println("File already exists. Overwriting ...");
				Files.delete(path);
			} catch (IOException e) {
				// Some sort of failure, such as permissions.
				e.printStackTrace();
			}
		}

		try {
			// Create the empty file with default permissions, etc.
			Files.createFile(path);
		} catch (IOException e) {
			// Some sort of failure, such as permissions.
			// System.err.format("createFile error: %s%n", e);
			e.printStackTrace();
		}

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
								/*
								.setAirfoilCoordFile(
									new File(binDirPath + File.separator 
										+ "ag38.dat"
									)
								)
								*/
								.setAirfoilSectionInline(
									/*
									 * Inline section coordinates formatted as airfoil section: 
									 *    This is useful when the real airfoil shape is known.
									 *    Such a 2D array would be filled programmatically and 
									 *    the AFIL/<airfoil-section>.dat couple would not be 
									 *    required (no auxiliary file to write).
									 */
									getAG38AirfoilSection()
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
								/*
								.setAirfoilCoordFile(
									new File(binDirPath + File.separator 
										+ "ag38.dat"
									)
								)
								*/
								.setAirfoilSectionInline(
									/*
									 * Inline section coordinates formatted as airfoil section: 
									 *    This is useful when the real airfoil shape is known.
									 *    Such a 2D array would be filled programmatically and 
									 *    the AFIL/<airfoil-section>.dat couple would not be 
									 *    required (no auxiliary file to write).
									 */
									getAG38AirfoilSection()
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
								.setAirfoilSectionInline(
									/*
									 * Inline section coordinates formatted as airfoil section: 
									 *    This is useful when the real airfoil shape is known.
									 *    Such a 2D array would be filled programmatically and 
									 *    the AFIL/<airfoil-section>.dat couple would not be 
									 *    required (no auxiliary file to write).
									 */
									getAG38AirfoilSection()
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
								.setAirfoilSectionInline(
									/*
									 * Inline section coordinates formatted as airfoil section: 
									 *    This is useful when the real airfoil shape is known.
									 *    Such a 2D array would be filled programmatically and 
									 *    the AFIL/<airfoil-section>.dat couple would not be 
									 *    required (no auxiliary file to write).
									 */
									getAG38AirfoilSection()
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
				.appendBody( //----------------------------------------------- body 1
					new AVLBody
						.Builder()
						.setDescription("theFuselage")
						/*
						.setBodyCoordFile(
							new File(binDirPath + File.separator 
								+ "sub.dat"
							)
						)
						*/
						.setBodySectionInline(
							/*
							 * Inline body-section coordinates formatted as airfoil section: 
							 *    x --> X-coordinate of the section parallel to YZ-plane
							 *    y --> radius of the equivalent circular section, 
							 *          i.e. a circle of the same area of body's real section 
							 *          
							 *    This is useful when the real fuselage shape is known and equivalent sections
							 *    are calculated on the fly. Such a 2D array would be filled programmatically
							 *    and the BFIL/<body-section>.dat couple would not be required (no auxiliary file 
							 *    to write).
							 */
							MatrixUtils.createRealMatrix(
									new double[][]{
										{1.0, 0.000},
										{0.9, 0.010},
										{0.8, 0.015},
										{0.5, 0.020},
										{0.2, 0.015},
										{0.1, 0.010},
										{0.0, 0.000},
										{0.1,-0.010},
										{0.2,-0.015},
										{0.5,-0.020},
										{0.8,-0.015},
										{0.9,-0.010},
										{1.0, 0.000}
									}
							)
						)
						.build()
					)
				// -------------------------------------- build the aircraft, finally
				.build();
		
		// transfer the AVL input data structures into a formatted string
		String lines[] = 
				AVLInputGenerator.formatAsAVLInput(inputData, aircraft) // format one single string with newlines
				.split("\\r?\\n");
		List<String> content = Arrays.stream(lines).collect(Collectors.toList()); // split in a list of strings
		
		// write out the content

		System.out.println("======================================");

		Charset charset = Charset.forName("utf-8");
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			for (String line : content) {
				System.out.println(line);
				writer.write(line, 0, line.length());
				writer.newLine();
			}
			System.out.println("======================================");
			System.out.println("... done.");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeDataToMassFile(AVLMassInputData massData, String filePath) {
		
		Path path = Paths.get(filePath);

		System.out.println("Writing " + path + " ...");

		// first delete file if exists
		boolean pathExists =
				Files.exists(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
		if (pathExists) {
			try {
				System.out.println("File already exists. Overwriting ...");
				Files.delete(path);
			} catch (IOException e) {
				// Some sort of failure, such as permissions.
				e.printStackTrace();
			}
		}

		try {
			// Create the empty file with default permissions, etc.
			Files.createFile(path);
		} catch (IOException e) {
			// Some sort of failure, such as permissions.
			// System.err.format("createFile error: %s%n", e);
			e.printStackTrace();
		}
		
		// transfer the AVL-Mass input data structures into a formatted string
		String lines[] = 
				AVLInputGenerator.formatAsMassInput(massData) // format one single string with newlines
				.split("\\r?\\n");
		List<String> content = Arrays.stream(lines).collect(Collectors.toList()); // split in a list of strings
		
		// write out the content

		System.out.println("======================================");

		Charset charset = Charset.forName("utf-8");
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			for (String line : content) {
				System.out.println(line);
				writer.write(line, 0, line.length());
				writer.newLine();
			}
			System.out.println("======================================");
			System.out.println("... done.");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeDataToRunFile(AVLMacro avlMacro, String filePath) {
		Path path = Paths.get(filePath);

		System.out.println("Writing " + path + " ...");

		// first delete file if exists
		boolean pathExists =
				Files.exists(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
		if (pathExists) {
			try {
				System.out.println("File already exists. Overwriting ...");
				Files.delete(path);
			} catch (IOException e) {
				// Some sort of failure, such as permissions.
				e.printStackTrace();
			}
		}

		try {
			// Create the empty file with default permissions, etc.
			Files.createFile(path);
		} catch (IOException e) {
			// Some sort of failure, such as permissions.
			// System.err.format("createFile error: %s%n", e);
			e.printStackTrace();
		}
		
		// transfer the AVL macro input data structures into a formatted string
		String lines[] = 
				avlMacro.format() // format one single string with newlines
				.split("\\r?\\n");
		List<String> content = Arrays.stream(lines).collect(Collectors.toList()); // split in a list of strings
		
		// write out the content

		System.out.println("======================================");

		Charset charset = Charset.forName("utf-8");
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			for (String line : content) {
				System.out.println(line);
				writer.write(line, 0, line.length());
				writer.newLine();
			}
			System.out.println("======================================");
			System.out.println("... done.");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* From file: avl_doc.txt - Surface and Body data

	Surface and Body data
	- - - - - - - - - - -
	 
	  SURFACE
	      COMPONENT (or INDEX)
	      YDUPLICATE
	      SCALE
	      TRANSLATE
	      ANGLE
	      NOWAKE
	      NOALBE
	      NOLOAD

	      SECTION

	      SECTION
	          NACA

	      SECTION
	          AIRFOIL
	          CLAF
	          CDCL

	      SECTION
	          AFILE
	          CONTROL
	          CONTROL

	  BODY
	      YDUPLICATE
	      SCALE
	      TRANSLATE
	      BFILE


	  SURFACE
	      YDUPLICATE

	      SECTION

	      SECTION

	  SURFACE
	     .
	     .
	      etc.
	 	
	*/
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
		aircraft.getBodies().stream()
		.forEach( body ->
			sb.append(formatAsAVLBody(body))
		);
		
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

	/* From file: avl_doc.txt - Surface-definition keywords and data formats

	Surface-definition keywords and data formats
	- - - - - - - - - - - - - - - - - - - - - - -

	*****

	SURFACE              | (keyword)
	Main Wing            | surface name string
	12   1.0  20  -1.5   | Nchord  Cspace   [ Nspan Sspace ]

	The SURFACE keyword declares that a surface is being defined until 
	the next SURFACE or BODY keyword, or the end of file is reached.  
	A surface does not really have any significance to the underlying 
	AVL vortex lattice solver, which only recognizes the overall 
	collection of all the individual horseshoe vortices.  SURFACE 
	is provided only as a configuration-defining device, and also 
	as a means of defining individual surface forces.  This is 
	necessary for structural load calculations, for example.

	  Nchord =  number of chordwise horseshoe vortices placed on the surface
	  Cspace =  chordwise vortex spacing parameter (described later)

	  Nspan  =  number of spanwise horseshoe vortices placed on the surface [optional]
	  Sspace =  spanwise vortex spacing parameter (described later)         [optional]

	If Nspan and Sspace are omitted (i.e. only Nchord and Cspace are present on line),
	then the Nspan and Sspace parameters will be expected for each section interval,
	as described later.


	*****

	COMPONENT       | (keyword) or INDEX 
	3               | Lcomp

	This optional keywords COMPONENT (or INDEX for backward compatibility)
	allows multiple input SURFACEs to be grouped together into a composite 
	virtual surface, by assigning each of the constituent surfaces the same 
	Lcomp value.  Application examples are:
	- A wing component made up of a wing SURFACE and a winglet SURFACE
	- A T-tail component made up of horizontal and vertical tail SURFACEs.

	A common Lcomp value instructs AVL to _not_ use a finite-core model
	for the influence of a horseshoe vortex and a control point which lies
	on the same component, as this would seriously corrupt the calculation.

	If each COMPONENT is specified via only a single SURFACE block,
	then the COMPONENT (or INDEX) declaration is unnecessary.


	*****

	YDUPLICATE      | (keyword)
	0.0             | Ydupl

	The YDUPLICATE keyword is a convenient shorthand device for creating 
	another surface which is a geometric mirror image of the one 
	being defined.  The duplicated surface is _not_ assumed to be 
	an aerodynamic image or anti-image, but is truly independent.  
	A typical application would be for cases which have geometric 
	symmetry, but not aerodynamic symmetry, such as a wing in yaw.  
	Defining the right wing together with YDUPLICATE will conveniently 
	create the entire wing.

	The YDUPLICATE keyword can _only_ be used if iYsym = 0 is specified.
	Otherwise, the duplicated real surface will be identical to the
	implied aerodynamic image surface, and velocities will be computed
	directly on the line-vortex segments of the images.  This will 
	almost certainly produce an arithmetic fault.

	The duplicated surface gets the same Lcomp value as the parent surface,
	so they are considered to be the same COMPONENT.  There is no significant 
	effect on the results if they are in reality two physically-separate surfaces.


	  Ydupl =  Y position of X-Z plane about which the current surface is 
	           reflected to make the duplicate geometric-image surface.


	*****

	SCALE            |  (keyword)
	1.0  1.0  0.8    | Xscale  Yscale  Zscale

	The SCALE allows convenient rescaling for the entire surface.
	The scaling is applied before the TRANSLATE operation described below. 

	  Xscale,Yscale,Zscale  =  scaling factors applied to all x,y,z coordinates
	                           (chords are also scaled by Xscale)


	*****

	TRANSLATE         |  (keyword)
	10.0  0.0  0.5    | dX  dY  dZ

	The TRANSLATE keyword allows convenient relocation of the entire 
	surface without the need to change the Xle,Yle,Zle locations 
	for all the defining sections.  A body can be translated without
	the need to modify the body shape coordinates.

	  dX,dY,dZ =  offset added on to all X,Y,Z values in this surface.

	*****

	ANGLE       |  (keyword)
	2.0         | dAinc

	The ANGLE keyword allows convenient changing of the incidence angle 
	of the entire surface without the need to change the Ainc values 
	for all the defining sections.  The rotation is performed about
	the spanwise axis projected onto the y-z plane.

	  dAinc =  offset added on to the Ainc values for all the defining sections
	           in this surface

	*****

	NOWAKE     |  (keyword)

	The NOWAKE keyword specifies that this surface is to NOT shed a wake,
	so that its strips will not have their Kutta conditions imposed.
	Such a surface will have a near-zero net lift, but it will still 
	generate a nonzero moment.

	*****

	NOALBE    |  (keyword)

	The NOALBE keyword specifies that this surface is unaffected by
	freestream direction changes specified by the alpha,beta angles
	and p,q,r rotation rates.  This surface then reacts to only to
	the perturbation velocities of all the horseshoe vortices and 
	sources and doublets in the flow.
	This allows the SURFACE/NOALBE object to model fixed surfaces such 
	as a ground plane, wind tunnel walls, or a nearby other aircraft 
	which is at a fixed flight condition.

	*****

	NOLOAD    |  (keyword)

	The NOLOAD keyword specifies that the force and moment on this surface
	is to NOT be included in the overall forces and moments of the configuration.
	This is typically used together with NOALBE, since the force on a ground
	plane or wind tunnel walls certainly is not to be considered as part
	of the aircraft force of interest.

	*****
	The following keyword declarations would be used in envisioned applications.

	1) Non-lifting fuselage modeled by its side-view and top-view profiles.
	This will capture the moment of the fuselage reasonably well.
	NOWAKE

	2) Another nearby aircraft, with both aircraft maneuvering together.
	This would be for trim calculation in formation flight.
	NOALBE
	NOLOAD

	3) Another nearby aircraft, with only the primary aircraft maneuvering.
	This would be for a flight-dynamics analysis in formation flight.
	NOLOAD

	4) Nearby wind tunnel walls or ground plane.
	NOALBE
	NOLOAD

	*/
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
				wing.getNChordwise(), wing.getCSpace(), 
				wing.getNSpanwise(), wing.getSSpace())
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
		  .append("# x,y,z scale factors for whole surface\n")
		  .append("SCALE\n");
		sb.append(
				String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g %3$-11.5g", 
					wing.getScale()[0], wing.getScale()[1], wing.getScale()[2])
					).append("\n");
		
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

	/* From file: avl_doc.txt - Section keywords and data formats

	SECTION                             |  (keyword)
	0.0 5.0 0.2   0.50  1.50   5 -2.0   | Xle Yle Zle   Chord Ainc   [ Nspan Sspace ]

	The SECTION keyword defines an airfoil-section camber line at some 
	spanwise location on the surface.

	  Xle,Yle,Zle =  airfoil's leading edge location
	  Chord       =  the airfoil's chord  (trailing edge is at Xle+Chord,Yle,Zle)
	  Ainc        =  incidence angle, taken as a rotation (+ by RH rule) about 
	                 the surface's spanwise axis projected onto the Y-Z plane.  
	  Nspan       =  number of spanwise vortices until the next section [ optional ]
	  Sspace      =  controls the spanwise spacing of the vortices      [ optional ]


	Nspan and Sspace are used here only if the overall Nspan and Sspace 
	for the whole surface is not specified after the SURFACE keyword.
	The Nspan and Sspace for the last section in the surface are always ignored.

	Note that Ainc is used only to modify the flow tangency boundary 
	condition on the airfoil camber line, and does not rotate the geometry 
	of the airfoil section itself.  This approximation is consistent with 
	linearized airfoil theory.

	The local chord and incidence angle are linearly interpolated between
	defining sections.  Obviously, at least two sections (root and tip)
	must be specified for each surface.

	The default airfoil camber line shape is a flat plate.  The NACA, AIRFOIL,
	and AFIL keywords, described below, are available to define non-flat
	camber lines.  If one of these is used, it must immediately follow 
	the data line of the SECTION keyword.

	All the sections in the surface must be defined in order across the span.


	*****

	NACA                      |    (keyword)
	4300                      | section NACA camberline

	The NACA keyword sets the camber line to the NACA 4-digit shape specified

	*****

	AIRFOIL   X1  X2          |(keyword)   [ optional x/c range ]
	1.0   0.0                 | x/c(1)  y/c(1)
	0.98  0.002               | x/c(2)  y/c(2)
	 .     .                  |  .       .
	 .     .                  |  .       .
	 .     .                  |  .       .
	1.0  -0.01                | x/c(N)  y/c(N)


	The AIRFOIL keyword declares that the airfoil definition is input
	as a set of x/c, y/c pairs.

	  x/c,y/c =  airfoil coordinates 

	The x/c, y/c coordinates run from TE, to LE, back to the TE again 
	in either direction.  These corrdinates are splined, and the slope 
	of the camber y(x) function is obtained from the middle y/c values 
	between top and bottom.  The number of points N is deterimined 
	when a line without two readable numbers is encountered.

	If present, the optional X1 X2 parameters indicate that only the 
	x/c range X1..X2 from the coordinates is to be assigned to the surface.
	If the surface is a 20%-chord flap, for example, then X1 X2
	would be 0.80 1.00.  This allows the camber shape to be easily 
	assigned to any number of surfaces in piecewise manner.


	*****

	AFILE      X1  X2         | (keyword)   [ optional x/c range ]
	filename                  | filename string

	The AFILE keyword is essentially the same as AIRFOIL, except
	that the x/c,y/c pairs are generated from a standard (XFOIL-type)
	set of airfoil coordinates contained in the file "filename".  
	The first line of this file is assumed to contain a string
	with the name of the airfoil (as written out with XFOIL's SAVE
	command).   If the path/filename has embedded blanks
	double quotes should be used to delimit the string.

	The optional X1 X2 parameters are used as in AIRFOIL.


	*****

	*/
	public static String formatAsAVLWingSection(AVLWingSection section) {
		StringBuilder sb = new StringBuilder();
		sb.append("SECTION").append("\n");
		sb.append(
				String.format(Locale.ROOT, "     %1$-11.5g %2$-11.5g %3$-11.5g %4$-11.5g %5$-11.5g", 
					section.getOrigin()[0], section.getOrigin()[1], section.getOrigin()[2], 
					section.getChord(), section.getTwist())
					).append("\n");

		// sb.append("AFIL").append("\n");
		// sb.append(section.getAirfoilCoordFile().getName()).append("\n");

		// in case a body-section file is given
		if (section.getAirfoilCoordFile().isPresent()) {
			sb.append("AFIL").append("\n");
			sb.append(section.getAirfoilCoordFile().get().getName()).append("\n");			
		} 

		// in case a body-section is given inline
		if (section.getAirfoilSectionInline().isPresent()) {
			sb.append("AIRFOIL").append("\n");
			Arrays.asList(section.getAirfoilSectionInline().get().getData()).stream()
				.forEach(pair -> 
					sb.append(
						String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g", pair[0], pair[1])
					).append("\n")
				);
		} 

		// format controls
		section.getControlSurfaces().stream()
			.forEach(controlSurface ->
				sb.append(formatAsAVLWingSectionControlSurface(controlSurface))
			);
		
		sb.append("#-----------------------\n");

		return sb.toString();
	}

	/* From file: avl_doc.txt - Control keywords and data formats

	CONTROL                              | (keyword)
	elevator  1.0  0.6   0. 1. 0.   1.0  | name, gain,  Xhinge,  XYZhvec,  SgnDup



	The CONTROL keyword declares that a hinge deflection at this section
	is to be governed by one or more control variables.  An arbitrary
	number of control variables can be used, limited only by the array
	limit NDMAX.

	The data line quantities are...

	 name     name of control variable
	 gain     control deflection gain, units:  degrees deflection / control variable
	 Xhinge   x/c location of hinge.  
	           If positive, control surface extent is Xhinge..1  (TE surface)
	           If negative, control surface extent is 0..-Xhinge (LE surface)
	 XYZhvec  vector giving hinge axis about which surface rotates 
	           + deflection is + rotation about hinge by righthand rule
	           Specifying XYZhvec = 0. 0. 0. puts the hinge vector along the hinge
	 SgnDup   sign of deflection for duplicated surface
	           An elevator would have SgnDup = +1
	           An aileron  would have SgnDup = -1


	Control derivatives will be generated for all control variables 
	which are declared.


	More than one variable can contribute to the motion at a section.
	For example, for the successive declarations

	CONTROL                         
	aileron  1.0  0.7  0. 1. 0.  -1.0

	CONTROL                         
	flap     0.3  0.7  0. 1. 0.   1.0

	the overall deflection will be

	 control_surface_deflection  =  1.0 * aileron  +  0.3 * flap


	The same control variable can be used on more than one surface.
	For example the wing sections might have

	CONTROL                         
	flap     0.3   0.7  0. 1. 0.   1.0

	and the horizontal tail sections might have

	CONTROL                         
	flap     0.03  0.5  0. 1. 0.   1.0

	with the latter simulating 10:1 flap -> elevator mixing.


	A partial-span control surface is specified by declaring
	CONTROL data only at the sections where the control surface
	exists, including the two end sections.  For example,
	the following wing defined with three sections (i.e. two panels)
	has a flap over the inner panel, and an aileron over the 
	outer panel.

	SECTION
	0.0  0.0  0.0   2.0   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	flap     1.0   0.80   0. 0. 0.   1   | name, gain,  Xhinge,  XYZhvec,  SgnDup

	SECTION
	0.0  8.0  0.0   2.0   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	flap     1.0   0.80   0. 0. 0.   1   | name, gain,  Xhinge,  XYZhvec,  SgnDup
	CONTROL                         
	aileron  1.0   0.85   0. 0. 0.  -1   | name, gain,  Xhinge,  XYZhvec,  SgnDup

	SECTION
	0.2 12.0  0.0   1.5   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	aileron  1.0   0.85   0. 0. 0.  -1   | name, gain,  Xhinge,  XYZhvec,  SgnDup


	The control gain for a control surface does not need to be equal
	at each section.  Spanwise stations between sections receive a gain
	which is linearly interpolated from the two bounding sections.
	This allows specification of flexible-surface control systems.
	For example, the following surface definition models wing warping
	which is linear from root to tip.  Note that the "hinge" is at x/c=0.0, 
	so that the entire chord rotates in response to the aileron deflection.

	SECTION
	0.0  0.0  0.0   2.0   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	aileron  0.0   0.     0. 0. 0.  -1   | name, gain,  Xhinge,  XYZhvec,  SgnDup

	SECTION
	0.2 12.0  0.0   1.5   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	aileron  1.0   0.     0. 0. 0.  -1   | name, gain,  Xhinge,  XYZhvec,  SgnDup



	Non-symmetric control effects, such as Aileron Differential, can be specified
	by a non-unity SgnDup magnitude.  For example, 

	SECTION
	0.0  6.0  0.0   2.0   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	aileron  1.0   0.7    0. 0. 0.  -2.0   | name, gain,  Xhinge,  XYZhvec,  SgnDup

	SECTION
	0.0 10.0  0.0   2.0   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	aileron  1.0   0.7    0. 0. 0.  -2.0   | name, gain,  Xhinge,  XYZhvec,  SgnDup

	will result in the duplicated aileron having a deflection opposite and 
	2.0 times larger than the defined aileron.  Note that this will have 
	the proper effect only in one direction.  In the example above, the 
	two aileron surfaces deflect as follows:

	  Right control surface:   1.0*aileron         =  1.0*aileron
	  Left  control surface:   1.0*aileron*(-2.0)  = -2.0*aileron

	which is the usual way Aileron Differential is implemented if "aileron" is positive.
	To get the same effect with a negative "aileron" control change, 
	the definitions would have to be as follows.

	SECTION
	0.0  6.0  0.0   2.0   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	aileron  2.0   0.7    0. 0. 0.  -0.5   | name, gain,  Xhinge,  XYZhvec,  SgnDup

	SECTION
	0.0 10.0  0.0   2.0   0.0   | Xle Yle Zle   Chord Ainc
	CONTROL                         
	aileron  2.0   0.7    0. 0. 0.  -0.5   | name, gain,  Xhinge,  XYZhvec,  SgnDup

	This then gives:

	  Right control surface:   2.0*aileron         = -2.0*(-aileron)
	  Left  control surface:   2.0*aileron*(-0.5)  =  1.0*(-aileron)

	which is the correct mirror image of the previous case if "aileron" is negative.

	*/
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
		  ).append(" ! name, gain,  Xhinge,  (X,Y,Z)hvec,  SgnDup\n");
		
		return sb.toString();
	}
		
	/* From file: avl_doc.txt - Body-definition keywords and data formats

	Body-definition keywords and data formats
	- - - - - - - - - - - - - - - - - - - - -

	*****

	BODY                 | (keyword)
	Fuselage             | body name string
	15   1.0             | Nbody  Bspace

	The BODY keyword declares that a body is being defined until
	the next SURFACE or BODY keyword, or the end of file is reached.  
	A body is modeled with a source+doublet line along its axis,
	in accordance with slender-body theory.

	  Nbody  =  number of source-line nodes
	  Bspace =  lengthwise node spacing parameter (described later)

	*****

	YDUPLICATE      | (keyword)
	0.0             | Ydupl

	Same function as for a surface, described earlier.

	*****

	SCALE            |  (keyword)
	1.0  1.0  0.8    | Xscale  Yscale  Zscale

	Same function as for a surface, described earlier.

	*****

	TRANSLATE         |  (keyword)
	10.0  0.0  0.5    | dX  dY  dZ

	Same function as for a surface, described earlier.

	*****
		
	*/
	public static String formatAsAVLBody(AVLBody body) {
		StringBuilder sb = new StringBuilder();
		sb.append("BODY").append("\n");
		sb.append(body.getDescription()).append("\n");
	
		// wing main data
		sb.append(String.format(Locale.ROOT, "%1$-11s %2$-11s", 
				"#Nbody", "Bspace")
				).append("\n");
		sb.append(
			String.format(Locale.ROOT, "%1$-11d %2$-11.5g", 
				body.getNBody(), body.getBSpace())
				).append("\n");

		if (body.isDuplicated()) {
			sb.append("YDUPLICATE\n");
			sb.append(body.getYDupl()).append("\n");
		}

		sb.append("#\n")
		  .append("# x,y,z scale factors for whole body\n")
		  .append("SCALE\n");
		sb.append(
				String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g %3$-11.5g", 
					body.getScale()[0], body.getScale()[1], body.getScale()[2])
					).append("\n");
		
		sb.append("#\n")
		  .append("# x,y,z bias for whole body\n")
		  .append("TRANSLATE\n");
		sb.append(
				String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g %3$-11.5g", 
					body.getOrigin()[0], body.getOrigin()[1], body.getOrigin()[2])
					).append("\n");

		// in case a body-section file is given
		if (body.getBodyCoordFile().isPresent()) {
			sb.append("BFIL").append("\n");
			sb.append(body.getBodyCoordFile().get().getName()).append("\n");			
		} 

		// in case a body-section is given inline
		if (body.getBodySectionInline().isPresent()) {
			sb.append("AIRFOIL").append("\n");
			Arrays.asList(body.getBodySectionInline().get().getData()).stream()
				.forEach(pair -> 
					sb.append(
						String.format(Locale.ROOT, "%1$-11.5g %2$-11.5g", pair[0], pair[1])
					).append("\n")
				);
		} 
		
		sb.append("#\n")
		  .append("#==============================================================\n")
		  .append("#\n");
		
		return sb.toString();
	}

	public static String formatAsMassInput(AVLMassInputData massData) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("#").append("\n");
		sb.append("#  xyz is location of item's own CG").append("\n");
		sb.append("#  Ixx.. are item's inertias about item's own CG").append("\n");
		sb.append("#").append("\n");
		sb.append("#  x back, y right, z up").append("\n");
		sb.append("#").append("\n");
		sb.append("#  x,y,z system here must have origin").append("\n");
		sb.append("#  at same location as AVL input file").append("\n");
		sb.append("#").append("\n");
		
		// Unit data
		sb.append(
			String.format(Locale.ROOT, "Lunit = %1$.5g m", massData.getLUnit())).append("\n");
		sb.append(
			String.format(Locale.ROOT, "Munit = %1$.5g kg", massData.getMUnit())).append("\n");
		sb.append(
				String.format(Locale.ROOT, "Tunit = %1$.5g s", massData.getTUnit())).append("\n");
		sb.append("#").append("\n");
		sb.append("#  mass   x     y     z      Ixx     Iyy     Izz   [ Ixy  Ixz  Iyz ]").append("\n");
		
		sb.append("#").append("\n");

		massData.getMassProperties().stream()
			.forEach(t -> {
				sb.append(
						String.format(
							Locale.ROOT, 
							"%1$11.3f %2$11.5f %3$11.5f %4$11.5f %5$14.3f %6$14.3f %7$14.3f %8$10.3f %9$10.3f %10$10.3f", 
							t._1._1, t._1._2, t._1._3, t._1._4,
							t._2._1, t._2._2, t._2._3, t._2._4, t._2._5, t._2._6
								)
						).append("\n");
			});
	
		return sb.toString();
	}

	// See file: ag38.dat
	static RealMatrix getAG38AirfoilSection() {
		return MatrixUtils.createRealMatrix(
			new double[][]{
				{0.999999 ,  0.004704},
				{0.993621 ,  0.005696},
				{0.983682 ,  0.007241},
				{0.972066 ,  0.009047},
				{0.959633 ,  0.010981},
				{0.946835 ,  0.012971},
				{0.933981 ,  0.014969},
				{0.921234 ,  0.016952},
				{0.908447 ,  0.018940},
				{0.895764 ,  0.020914},
				{0.882852 ,  0.022914},
				{0.870677 ,  0.024564},
				{0.857810 ,  0.026064},
				{0.844779 ,  0.027481},
				{0.831613 ,  0.028913},
				{0.818263 ,  0.030364},
				{0.804818 ,  0.031827},
				{0.791388 ,  0.033286},
				{0.777951 ,  0.034747},
				{0.764549 ,  0.036205},
				{0.751106 ,  0.037667},
				{0.737702 ,  0.039124},
				{0.724342 ,  0.040577},
				{0.711051 ,  0.042022},
				{0.697928 ,  0.043448},
				{0.685186 ,  0.044836},
				{0.672293 ,  0.046199},
				{0.658783 ,  0.047537},
				{0.647516 ,  0.048556},
				{0.635011 ,  0.049622},
				{0.622485 ,  0.050624},
				{0.609531 ,  0.051627},
				{0.596533 ,  0.052634},
				{0.583312 ,  0.053658},
				{0.569976 ,  0.054692},
				{0.556633 ,  0.055727},
				{0.543315 ,  0.056758},
				{0.530000 ,  0.057791},
				{0.516728 ,  0.058820},
				{0.503472 ,  0.059846},
				{0.490290 ,  0.060869},
				{0.477236 ,  0.061880},
				{0.464539 ,  0.062866},
				{0.452148 ,  0.063819},
				{0.439870 ,  0.064696},
				{0.427640 ,  0.065505},
				{0.415386 ,  0.066252},
				{0.403250 ,  0.066927},
				{0.391172 ,  0.067542},
				{0.379063 ,  0.068100},
				{0.366729 ,  0.068612},
				{0.354113 ,  0.069073},
				{0.341333 ,  0.069479},
				{0.328488 ,  0.069821},
				{0.315654 ,  0.070095},
				{0.302897 ,  0.070294},
				{0.290182 ,  0.070419},
				{0.277486 ,  0.070467},
				{0.264739 ,  0.070440},
				{0.251950 ,  0.070331},
				{0.239044 ,  0.070136},
				{0.226021 ,  0.069844},
				{0.212912 ,  0.069446},
				{0.199800 ,  0.068938},
				{0.186742 ,  0.068312},
				{0.173780 ,  0.067562},
				{0.160907 ,  0.066679},
				{0.148128 ,  0.065652},
				{0.135369 ,  0.064462},
				{0.122667 ,  0.063091},
				{0.110044 ,  0.061522},
				{0.097563 ,  0.059738},
				{0.086115 ,  0.057868},
				{0.075071 ,  0.055815},
				{0.064507 ,  0.053584},
				{0.054572 ,  0.051199},
				{0.045308 ,  0.048667},
				{0.036786 ,  0.046008},
				{0.029161 ,  0.043281},
				{0.022616 ,  0.040585},
				{0.017243 ,  0.038018},
				{0.012940 ,  0.035620},
				{0.009579 ,  0.033433},
				{0.007013 ,  0.031471},
				{0.005102 ,  0.029742},
				{0.003685 ,  0.028228},
				{0.002589 ,  0.026840},
				{0.001700 ,  0.025486},
				{0.000978 ,  0.024106},
				{0.000451 ,  0.022746},
				{0.000094 ,  0.021225},
				{0.000009 ,  0.019745},
				{0.000172 ,  0.018622},
				{0.000581 ,  0.017457},
				{0.001293 ,  0.016280},
				{0.002383 ,  0.015135},
				{0.003836 ,  0.014081},
				{0.005649 ,  0.013115},
				{0.007886 ,  0.012203},
				{0.010612 ,  0.011326},
				{0.013931 ,  0.010463},
				{0.018027 ,  0.009582},
				{0.023174 ,  0.008665},
				{0.029676 ,  0.007708},
				{0.037756 ,  0.006738},
				{0.047349 ,  0.005792},
				{0.058120 ,  0.004930},
				{0.069666 ,  0.004170},
				{0.081678 ,  0.003507},
				{0.093975 ,  0.002937},
				{0.106461 ,  0.002451},
				{0.119072 ,  0.002040},
				{0.131774 ,  0.001694},
				{0.144550 ,  0.001399},
				{0.157380 ,  0.001147},
				{0.170260 ,  0.000929},
				{0.183177 ,  0.000744},
				{0.196137 ,  0.000585},
				{0.209128 ,  0.000450},
				{0.222152 ,  0.000335},
				{0.235196 ,  0.000245},
				{0.248262 ,  0.000172},
				{0.261351 ,  0.000117},
				{0.274460 ,  0.000077},
				{0.287589 ,  0.000040},
				{0.300735 ,  0.000018},
				{0.313898 ,  0.000004},
				{0.327086 , -0.000005},
				{0.340287 , -0.000003},
				{0.353486 , -0.000003},
				{0.366689 , -0.000003},
				{0.379895 , -0.000001},
				{0.393109 , -0.000003},
				{0.406322 , -0.000002},
				{0.419532 ,  0.000001},
				{0.432737 , -0.000003},
				{0.445943 ,  0.000001},
				{0.459157 ,  0.000002},
				{0.472370 , -0.000001},
				{0.485577 ,  0.000002},
				{0.498782 ,  0.000001},
				{0.511992 ,  0.000000},
				{0.525205 ,  0.000001},
				{0.538418 , -0.000001},
				{0.551625 , -0.000001},
				{0.564826 ,  0.000001},
				{0.578028 , -0.000001},
				{0.591236 , -0.000002},
				{0.604443 ,  0.000000},
				{0.617649 ,  0.000000},
				{0.630860 , -0.000001},
				{0.644076 ,  0.000000},
				{0.657286 ,  0.000000},
				{0.670495 ,  0.000002},
				{0.683700 ,  0.000000},
				{0.696909 ,  0.000001},
				{0.710122 , -0.000001},
				{0.723334 ,  0.000000},
				{0.736542 , -0.000002},
				{0.749746 , -0.000002},
				{0.762958 , -0.000001},
				{0.776172 ,  0.000000},
				{0.789383 ,  0.000000},
				{0.802589 ,  0.000001},
				{0.815788 ,  0.000001},
				{0.828992 ,  0.000001},
				{0.842199 ,  0.000002},
				{0.855403 , -0.000002},
				{0.868602 , -0.000002},
				{0.881798 , -0.000002},
				{0.895001 , -0.000002},
				{0.908208 , -0.000001},
				{0.921420 ,  0.000000},
				{0.934616 ,  0.000001},
				{0.947766 ,  0.000002},
				{0.960776 ,  0.000000},
				{0.973384 ,  0.000000},
				{0.984990 ,  0.000000},
				{0.994806 ,  0.000000},
				{1.000000 ,  0.000001}
			}
		);		
	}

} // end-of-class AVLInputGenerator

/*

Allegro-lite 2M
0.0                      Mach
0     0     0.0          iYsym  iZsym  Zsym
530.0 6.6  78.6          Sref   Cref   Bref   reference area, chord, span
3.250 0.0   0.5          Xref   Yref   Zref   moment reference location (arb.)
0.020                    CDoref
#
#==============================================================
#
SURFACE
WING
7  1.0  20  -2.0  !  Nchord   Cspace   Nspan  Sspace
#
# reflect image wing about y=0 plane
YDUPLICATE
     0.00000 
#
# twist angle bias for whole surface
ANGLE
     0.00000    
#
# x,y,z bias for whole surface
TRANSLATE
    0.00000     0.00000     0.00000
#--------------------------------------------------------------
#    Xle         Yle         Zle         chord       angle   Nspan  Sspace
SECTION
     0.00000     0.00000     0.00000     8.0         1.490   5      0.25
AFIL
ag35.dat
#-----------------------
SECTION
     0.500       15.0        0.0         7.5         1.380   7     -2.60
AFIL
ag36.dat
#-----------------------
SECTION
     1.875       31.0        3.30        6.0         1.220   8     -2.25
AFIL
ag37.dat
#-----------------------
SECTION
     3.625       39.3        7.00        4.0         0.940   1      0
AFIL
ag38.dat
#
#==============================================================
#
SURFACE
Horizontal tail
5  1.0  7  -1.5  ! Nchord   Cspace
#
YDUPLICATE
     0.00000
ANGLE
     0.0000
TRANSLATE
    27.50000     0.00000     1.25000
#--------------------------------------------------------------
SECTION
     0.00000     0.00000     0.00000     3.5         0.000   7  -1.5

CONTROL
elevator  1.0  0.0  0.0 1.0 0.0  1.0
#-----------------------
SECTION
     1.15        9.0         0.00000     1.8         0.000   1   0

CONTROL
elevator  1.0  0.0  0.0 1.0 0.0  1.0
#
#==============================================================
#
SURFACE
Vertical tail
6  1.0  10  0.5  ! Nchord   Cspace
TRANSLATE
    33.00000     0.00000     0.00000
#--------------------------------------------------------------
SECTION
    -1.28   0.00000    -2.00000     3.20000     0.000   3   1.5

CONTROL
rudder  1.0  0.4   0.0 0.0 1.0   1.0
#-----------------------
SECTION
    -1.68   0.00000     0.00000     4.20000     0.000   2   0.5

CONTROL
rudder  1.0  0.4   0.0 0.0 1.0   1.0
#-----------------------
SECTION
    -1.5388 0.00000     1.25000     3.847       0.000   8  -1.5

CONTROL
rudder  1.0  0.4   0.0 0.0 1.0   1.0
#-----------------------
SECTION
    -0.72   0.00000     8.50        1.80000     0.000   1   0

CONTROL
rudder  1.0  0.4   0.0 0.0 1.0   1.0

#==============================================================


*/