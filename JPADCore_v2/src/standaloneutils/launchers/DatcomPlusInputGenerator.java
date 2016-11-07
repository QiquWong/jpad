package standaloneutils.launchers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

public class DatcomPlusInputGenerator {

	public enum KEYWORDS {
		//----------------------------------------------------------------------------
		CASEID("CASEID"),
		//----------------------------------------------------------------------------
		DIM("DIM"), 
		FT("FT"), MT("MT"),
		//----------------------------------------------------------------------------
		TRIM("TRIM"),
		DAMP("DAMP"),
		//----------------------------------------------------------------------------
		DERIV("DERIV"), 
		RAD("RAD"), DEG("DEG"),
		//----------------------------------------------------------------------------
		PART("PART"),
		//----------------------------------------------------------------------------
		FLTCON("FLTCON"), 
		NMACH("NMACH"), MACH("MACH"), NALT("NALT"), ALT("ALT"),
		NALPHA("NALPHA"), ALSCHD("ALSCHD"), GAMMA("GAMMA"), LOOP("LOOP"),
		RNNUB("RNNUB"),
		//----------------------------------------------------------------------------
		OPTINS("OPTINS"),
		BLREF("BLREF"), SREF("SREF"), CBARR("CBARR"),
		SYNTHS("SYNTHS"),
		XW("XW"), ZW("ZW"), ALIW("ALIW"), XCG("XCG"), ZCG("ZCG"),
		XH("XH"), ZH("ZH"), XV("XV"), XVF("XVF"), VERTUP("VERTUP"),
		//----------------------------------------------------------------------------
		BODY("BODY"),
		NX("NX"), BNOSE("BNOSE"), BTAIL("BTAIL"), BLA("BLA"),
		X("X"), ZU("ZU"), ZL("ZL"), R("R"), S("S"),
		//----------------------------------------------------------------------------
		WGPLNF("WGPLNF"),
		CHRDR("CHRDR"), CHRDTP("CHRDTP"), CHRDBP("CHRDBP"),
		SSPN("SSPN"), SSPNOP("SSPNOP"), SSPNE("SSPNE"), CHSTAT("CHSTAT"), TWISTA("TWISTA"), TYPE("TYPE"),
		SAVSI("SAVSI"), SAVSO("SAVSO"), DHDADI("DHDADI"), DHDADO("DHDADO"),
		//----------------------------------------------------------------------------
		HTPLNF("HTPLNF"),
		//----------------------------------------------------------------------------
		VTPLNF("VTPLNF"),
		//----------------------------------------------------------------------------
		JETPWR("JETPWR"),
		AIETLJ("AIETLJ"), AMBSTP("AMBSTP"), AMBTMP("AMBTMP"), JEALOC("JEALOC"),
		JELLOC("JELLOC"), JERAD("JERAD"), JEVLOC("JEVLOC"), 
		JIALOC("JIALOC"), JINLTA("JINLTA"), NENGSJ("NENGSJ"), THSTCJ("THSTCJ"),
		JEANGL("JEANGL"),
		//----------------------------------------------------------------------------
		PROPWR("PROPWR"),
		AIETLP("AIETLP"), NENGSP("NENGSP"), THSTCP("THSTCP"),
		PHALOC("PHALOC"), PHVLOC("PHVLOC"), PRPRAD("PRPRAD"),
		ENGFCT("ENGFCT"), NOPBPE("NOPBPE"),
		YP("YP"), CROT("CROT"), 
		//----------------------------------------------------------------------------
		SYMFLP("SYMFLP"),
		FTYPE("FTYPE"), NDELTA("NDELTA"), DELTA("DELTA"),
		SPANFI("SPANFI"), SPANFO("SPANFO"), CHRDFI("CHRDFI"), CHRDFO("CHRDFO"),
		NTYPE("NTYPE"), CB("CB"), TC("TC"), PHETE("PHETE"), PHETEP("PHETEP")
		//----------------------------------------------------------------------------
		;

		private final String text;

		KEYWORDS(final String newText) {
			this.text = newText;
		}

		@Override
		public String toString() { return text; }
	}

	public static void writeDataToFile(DatcomInputData inputData, String filePath) {
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
		
		// transfer the Datcom+ input data structure into a formatted string
		String lines[] = 
				DatcomPlusInputGenerator.formatAsDatcomPlusInput(inputData) // format one single string with newlines
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
	
	public static void writeTemplate(String filePath) {
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

		// File content
		List<String> content = new ArrayList<String>();

		content.add("* ");
		content.add("* X-airplane template");
		content.add("* ");

		content.add("**************************************************************************");
		content.add("* List of Command Card");
		content.add("**************************************************************************");
		content.add(KEYWORDS.TRIM.toString());
		content.add(KEYWORDS.DAMP.toString());
		content.add(KEYWORDS.PART.toString());
		content.add(KEYWORDS.DERIV.toString() + " " +KEYWORDS.RAD.toString());
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Flight conditions");
		content.add("**************************************************************************");
		content.add(
				generateBlockFLTCON(
						//Arrays.asList(0.3), // list of Mach numbers
						0.3, // Only one Mach number permitted
						//Arrays.asList(1500.0), // list of altitudes
						1.500, // Only one Altitude permitted
						Arrays.asList( // list of AoA
								-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0,
								10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0),
						0.0, // Gamma
						2, // Loop
						20120887.0 // Reynolds number per unit length
						)
				);
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Reference Parameters (page 29)");
		content.add("**************************************************************************");
		content.add(
				generateBlockOPTINS(
						93.0, // blref
						1329.9, // sref
						14.3 // cbarr
						)
				);
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Group II     Synthesis Parameters (page 33)");
		content.add("**************************************************************************");
		content.add(
				generateBlockSYNTHS(
						28.3, -1.4, // xw, zw
						1.0, // aliw
						41.3, 0.0, // xcg, zcg
						76.6, 6.2, // xh, zh
						71.1, 7.6, // xv, zv
						66.2, 13.1, // xvf, zvf
						true // vertup
						)
				);
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Body Configuration Parameters (page 36)");
		content.add("**************************************************************************");
		content.add(
				generateBlockBODY(
						2.0, 2.0, 20.0, // bnose, btail, bla
						Arrays.asList( // list of x
								0.,1.38,4.83,6.90,8.97,13.8,27.6,55.2,65.6,69.0,75.9,82.8,89.7,90.4),
						Arrays.asList( // list of zu
								.69,2.07,3.45,4.38,5.87,6.90,8.28,8.28,8.28,8.28,7.94,7.59,7.50,6.9),
						Arrays.asList( // list of zl
								-.35,-1.73,-3.45,-3.80,-4.14,-4.49,-4.83,-4.83,-3.45,-2.76,-0.81,1.04,4.14,6.21),
						Arrays.asList( // list of s
								.55,8.23,28.89,44.31,65.06,92.63,127.81,127.81,108.11,95.68,56.88,28.39,3.64,0.11)
						)
				);
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Wing planform variables (page 37-38)");
		content.add("**************************************************************************");
		content.add(
				generateBlockGenericPLNF("WGPLNF",
						23.8, 4.8, 12.4, // CHRDR, CHRDTP, CHRDBP
						46.9, 31.1, 40.0, // SSPN, SSPNOP, SSPNE
						0.25, 0.0, // CHSTAT, TWISTA
						29.0, 26.0, 0.0, 4.0, // SAVSI, SAVSO, DHDADI, DHDADO
						1 // TYPE
						)
				);
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Jet Power Effects parameters (page 51)");
		content.add("**************************************************************************");
		content.add(
				generateBlockJETPWR(
						-2.0, 2116.8, 59.7, // AIETLJ, AMBSTP, AMBTMP
						Arrays.asList(42.25, 58.0), // list of JEALOC
						15.9, 2.065, -5.2, // JELLOC, JERAD, JEVLOC
						34.5, 13.4, // JIALOC, JINLTA
						2, // NENGSJ
						0.0, // THSTCJ
						-2.0 // JEANGL
						)
				);
		content.add(" ");
		content.add(" ");

/*
		content.add("**************************************************************************");
		content.add("* Propeller Power Effects parameters");
		content.add("**************************************************************************");
		content.add(
				generateBlockPROPWR(
						2, 3, // NENGSP, NOPBPE
						0.0, 0.009, // AIETLP, THSTCP, 
						4.5, 4.0, 3.75, // PHALOC, PHVLOC, PRPRAD 
						0.8, 6.0, // ENGFCT, YP
						false // CROT
						));
		content.add(" ");
		content.add(" ");
 */		
		
		content.add("**************************************************************************");
		content.add("* Vertical Tail planform variables (page 37-38)");
		content.add("**************************************************************************");
		content.add(
				generateBlockGenericPLNF("VTPLNF",
						15.9, 4.8, null, // CHRDR, CHRDTP, CHRDBP
						27.6, null, 20.7, // SSPN, SSPNOP, SSPNE
						0.25, 0.0, // CHSTAT, TWISTA
						33.0, 33.0, null, null, // SAVSI, SAVSO, DHDADI, DHDADO
						1 // TYPE
						)
				);
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Horizontal Tail planform variables (page 37-38)");
		content.add("**************************************************************************");
		content.add(
				generateBlockGenericPLNF("HTPLNF",
						12.4, 4.1, null, // CHRDR, CHRDTP, CHRDBP
						17.6, null, 15.87, // SSPN, SSPNOP, SSPNE
						0.25, 0.0, // CHSTAT, TWISTA
						31.0, 31.0, 9.0, 0.0, // SAVSI, SAVSO, DHDADI, DHDADO
						1 // TYPE
						)
				);
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Symetrical Flap Deflection parameters");
		content.add("**************************************************************************");
		content.add(
				generateBlockSYMFLP(
						1, // FTYPE
						Arrays.asList(-40.0, -30.0, -20.0, -10.0, 0.0, 10.0, 20.0, 30.0, 40.0), // list of deltas
						0.0, 14.0, // SPANFI, SPANFO
						1.72, 1.72, // CHRDFI, CHRDFO
						1, // NTYPE
						0.50, 0.44, 0.003, 0.002 // CB, TC, PHETE, PHETEP
						)
				);
		/*

 $SYMFLP FTYPE=1.,NDELTA=9.,DELTA(1)=-40.,-30.,-20.,-10.,
    0.,10.,20.,30.,40.,SPANFI=0.,SPANFO=14.,CHRDFI=1.72,
    CHRDFO=1.72,NTYPE=1.0,CB=.50,TC=.44,PHETE=.003,PHETEP=.002$

		 */
		content.add(" ");
		content.add(" ");

		content.add("**************************************************************************");
		content.add("* Wing Sectional Characteristics Parameters");
		content.add("**************************************************************************");
		content.add("NACA-W-4-0012-25");
		content.add("NACA-H-4-0012-25");
		content.add("NACA-V-4-0012-25");
		content.add(" ");
		content.add(" ");

		content.add(KEYWORDS.CASEID.toString() + " Total: X-airplane");

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

/*

---------------------------------------------------------------------
Flight Conditions
---------------------------------------------------------------------
WT      Vehicle Weight
LOOP    Program Looping Control
           1 = vary altitude and mach together, default
           2 = vary Mach, at fixed altitude
           3 = vary altitude, at fixed Mach
NMACH   Number of Mach numbers or velocities to be run, max of 20
        Note: This parameter, along with NALT, may affect the
        proper setting of the LOOP control parameter.
MACH    Array(20) Values of freestream Mach number
VINF    Array(20) Values of freestream speed (unit: l/t)
NALPHA  Number of angles of attack to be run, max of 20
ALSCHD  Array(20) Values of angles of attack, in ascending order
RNNUB   Array(20) Reynolds number per unit length
        Freestream Reynolds numbers. Each array element must
        correspond to the respective Mach number/freestream
        speed input, use LOOP=1.0
NALT    Number of atmospheric conditions to be run, max of 20
        input as either altitude or pressure and temperature
        Note: This parameter, along with NMACH, may affect the
        proper setting of the LOOP control parameter.
ALT     Array(20) Values of geometric altitude
        Number of altitude and values. Note, Atmospheric conditions
        are input either as altitude or pressure and temperature. (MAX 20)
PINF    Array(20) Values of freestream Static Pressure
TINF    Array(20) Values of freestream Temperature
HYPERS  =.true.  Hypersonic analysis at all Mach numbers > 1.4
STMACH  Upper limit of Mach numbers for subsonic analysis
        (0.6<STMACH<0.99), Default to 0.6 if not input.
TSMACH  Lower limit of Mach number for Supersonic analysis
        (1.01<=TSMACH<=1.4)  Default to 1.4
TR      Drag due to lift transition flag, for regression analysis
        of wing-body configuration.
        = 0.0 for no transition (default)
        = 1.0 for transition strips or full scale flight
GAMMA   Flight path angle

Example:

 $FLTCON NMACH=1.0, MACH(1)=0.3, 
   NALT=1.0, ALT(1)=1.500, 
   NALPHA=20.0, ALSCHD(1)=-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0,
   GAMMA=0.0, LOOP=2.0, 
   RNNUB=20120887.0$

*/	
	public static String generateBlockFLTCON(
			//List<Double> machList,
			Double mach,  // only one Mach number is permitted
			//List<Double> altitudeList,
			Double altitude, //only one Altitude permitted
			List<Double> alphaList,
			Double gamma, int loop, Double reynoldsPerUnitLength
			) {
		StringBuilder sb = new StringBuilder();
		sb.append(" $FLTCON ");
		
//		sb.append("NMACH=").append((double) machList.size()).append(", ");
//		sb.append("MACH(1)=");
//		machList.stream()
//		.forEach(mach -> sb.append(mach).append(", "));
		
		sb.append("NMACH=1.0, ");
		sb.append("MACH(1)=").append(mach).append(", ");
		
//		sb.append("\n   NALT=").append((double) altitudeList.size()).append(", ");
//		sb.append("ALT(1)=");
//		altitudeList.stream()
//		.forEach(alt -> sb.append(alt).append(", "));

		sb.append("\n   NALT=1.0, ");
		sb.append("ALT(1)=").append(altitude).append(", ");
		
		sb.append("\n   NALPHA=").append((double) alphaList.size()).append(", ");
		sb.append("\n   ALSCHD(1)=");
		sb.append(
				WordUtils.wrap( // Apache Commons Lang
						alphaList.stream()
						.map(value -> value.toString())
						.collect(Collectors.joining(", "))
						, 
						65, // wrapLength
						"\n      ", // newLineStr
						false // wrapLongWords 
						)
				).append(",");
		sb.append("\n   GAMMA=").append(gamma).append(", ");
		sb.append("LOOP=").append((double)loop).append(", ");
		sb.append("\n   RNNUB=")
		.append(String.format(Locale.ROOT, "%.1f", reynoldsPerUnitLength))
		.append("$");
		return sb.toString();
	}

/*

---------------------------------------------------------------------
Reference Parameters
---------------------------------------------------------------------
SREF    Reference area value of theoretical wing area used by program
        if not input
CBARR   Longitudinal reference length value of theoritcal wing
        Mean Aerodynamic Chord used by program if not input
BLREF   Lateral reference length value of wing span used by program
ROUGFC  Surface roughness factor, equivalent sand roughness, default
        to 0.16e-3 inches (Natural sheet metal)
        0.02/0.08E-3 - Polished metal or wood
        0.16E-3  - Natural sheet metal
        0.25E-3  - Smooth matte paint, carefully applied
        0.40E-3  - Standard camouflage paint, average application

Example:

 $OPTINS BLREF=93.0,SREF=1329.9,CBARR=14.3$

 */
	public static String generateBlockOPTINS(Double blref, Double sref, Double cbarr) {
		StringBuilder sb = new StringBuilder();
		sb.append(" $OPTINS ")
		.append("BLREF=").append(blref).append(", ")
		.append("SREF=").append(sref).append(", ")
		.append("CBARR=").append(blref)
		.append("$");
		return sb.toString();
	}

/*

---------------------------------------------------------------------
Group II, Synthesis Parameters
---------------------------------------------------------------------
XCG     Longitudinal location of cg (moment ref. center)
ZCG     Vertical location of CG relative to reference plane
XW      Longitudinal location of theoretical wing apex (where
        leading edge would intersect long axis)
ZW      Vertical location of theoretical wing apex relative to
        reference plane
ALIW    Wing root chord incident angle measured from reference plane
XH      Longitudinal location of theoretical horizontal tail apex.
        If HINAX is input, XH and ZH are evaluated at zero incidence.
ZH      Vertical location of theoretical horizontal tail apex
        relative to reference plane. If HINAX is input, XH and ZH
        are evaluated at zero incidence.
ALIH    Horizontal tail root chord incidence angle measured from
        reference plane
XV      Longitudinal location of theoretical vertical tail apex
XVF     Longitudinal location of theoretical ventral fin apex
ZV      Vertical location of theoretical vertical tail apex
        This kinda makes sense only for twin tails that are canted
ZVF     Vertical location of theoretical ventral fin apex
        This kinda makes sense only for twin tails that are canted
SCALE   Vehicle scale factor (multiplier to input dimensions)
VERTUP  Vertical panel above reference plane (default=true)
HINAX   Longitudinal location of horizontal tail hinge axis.
        Required only for all-moveable horizontal tail trim option.

Example:

$SYNTHS XW=28.3,ZW=-1.4,ALIW=1.0,XCG=41.3,ZCG=0.0,
   XH=76.6,ZH=6.2,
   XV=71.1,ZV=7.6,
   XVF=66.2,ZVF=13.1,
   VERTUP=.TRUE.$

 */
	public static String generateBlockSYNTHS(
			Double xw, Double zw,
			Double aliw,
			Double xcg, Double zcg,
			Double xh, Double zh,
			Double xv, Double zv,
			Double xvf, Double zvf,
			boolean vertup
			) {
		StringBuilder sb = new StringBuilder();
		sb.append(" $SYNTHS ")
		.append("XW=").append(xw).append(", ").append("ZW=").append(zw).append(", ")
		.append("ALIW=").append(aliw).append(", ")
		.append("XCG=").append(xcg).append(", ").append("ZCG=").append(zcg).append(", ")
		.append("\n   XH=").append(xh).append(", ").append("ZH=").append(zh).append(", ")
		.append("\n   XV=").append(xv).append(", ").append("ZV=").append(zv).append(", ")
		.append("\n   XVF=").append(xvf).append(", ").append("ZVF=").append(zvf).append(", ")
		.append("\n   VERTUP=").append(".").append(String.valueOf(vertup).toUpperCase()).append(".")
		.append("$");
		return sb.toString();		
	}

/*

---------------------------------------------------------------------
Body Configuration Parameters
---------------------------------------------------------------------
Here is an error message output by DIGDAT concerning body geometry:
IN NAMELIST BODY, ONLY THE FOLLOWING COMBINATIONS OF VARIABLES CAN BE USED
FOR A CIRCULAR BODY, SPECIFY X AND R OR X AND S
FOR AN ELLIPTICAL BODY, SPECIFY X AND R OR X AND S, AND THE VARIABLE ELLIP
FOR OTHER BODY SHAPES X, R, S, AND P MUST ALL BE SPECIFIED

NX      Number of longitudinal body stations at which data is
        specified, max of 20
X       Array(20) Longitudinal distance measured from arbitray location
S       Array(20) Cross sectional area at station. See note above.
P       Array(20) Periphery at station Xi. See note above.
R       Array(20) Planform half width at station Xi. See note above.
ZU      Array(20) Z-coordinate at upper body surface at station Xi
        (positive when above centerline)
        [Only required for subsonic asymmetric bodies]
ZL      Array(20) Z-coordinate at lower body surface at station Xi
        (negative when below centerline)
        [Only required for subsonic asymmetric bodies]
BNOSE   Nosecone type  1.0 = conical (rounded), 2.0 = ogive (sharp point)
        [Not required in subsonic speed regime]
BTAIL   Tailcone type  1.0 = conical, 2.0 = ogive, omit for lbt = 0
        [Not required in subsonic speed regime]
BLN     Length of body nose
        Not required in subsonic speed regime
BLA     Length of cylindrical afterbody segment, =0.0 for nose alone
        or nose-tail configuration
        Not required in subsonic speed regime
DS      Nose bluntness diameter, zero for sharp nosebodies
        [Hypersonic speed regime only]
ITYPE   1.0 = straight wing, no area rule
        2.0 = swept wing, no area rule (default)
        3.0 = swept wing, area rule
METHOD  1.0 = Use existing methods (default)
        2.0 = Use Jorgensen method

Example:

 $BODY NX=14.,
   BNOSE=2.,BTAIL=2.,BLA=20.0,
   X(1)=0.,1.38,4.83,6.90,8.97,13.8,27.6,55.2,
      65.6,69.0,75.9,82.8,89.7,90.4,
   ZU(1)=.69,2.07,3.45,4.38,5.87,6.90,8.28,
      8.28,8.28,8.28,7.94,7.59,7.50,6.9,
   ZL(1)=-.35,-1.73,-3.45,-3.80,-4.14,-4.49,-4.83,
      -4.83,-3.45,-2.76,-0.81,1.04,4.14,6.21,
   S(1)=.55,8.23,28.89,44.31,65.06,92.63,127.81,
      127.81,108.11,95.68,56.88,28.39,3.64,0.11$

 */
	public static String generateBlockBODY(
			Double bnose, Double btail, Double bla,
			List<Double> x, List<Double> zu, List<Double> zl, List<Double> s) {
		StringBuilder sb = new StringBuilder();
		sb.append(" $BODY ");
		sb.append("NX=").append((double) x.size()).append(", ");
		sb.append("\n   X(1)=");
		sb.append(
				WordUtils.wrap( // Apache Commons Lang
						x.stream()
						.map(value -> value.toString())
						.collect(Collectors.joining(", "))
						, 
						60, // wrapLength
						"\n      ", // newLineStr
						false // wrapLongWords 
						)
				).append(",");
		sb.append("\n   ZU(1)=");
		sb.append(
				WordUtils.wrap( // Apache Commons Lang
						zu.stream()
						.map(value -> value.toString())
						.collect(Collectors.joining(", "))
						, 
						60, // wrapLength
						"\n      ", // newLineStr
						false // wrapLongWords 
						)
				).append(",");
		sb.append("\n   ZL(1)=");
		sb.append(
				WordUtils.wrap( // Apache Commons Lang
						zl.stream()
						.map(value -> value.toString())
						.collect(Collectors.joining(", "))
						, 
						60, // wrapLength
						"\n      ", // newLineStr
						false // wrapLongWords 
						)
				).append(",");
		sb.append("\n   S(1)=");
		sb.append(
				WordUtils.wrap( // Apache Commons Lang
						s.stream()
						.map(value -> value.toString())
						.collect(Collectors.joining(", "))
						, 
						60, // wrapLength
						"\n      ", // newLineStr
						false // wrapLongWords 
						)
				); // no comma at the end
		sb.append("$");
		return sb.toString();		
	}

/*

---------------------------------------------------------------------
Wing planform variables
---------------------------------------------------------------------
CHRDR   Chord root
CHRDBP  Chord at breakpoint. Not required for straight
        tapered planform.
CHRDTP  Tip chord
SSPN    Semi-span theoretical panel from theoretical root chord
SSPNE   Semi-span exposed panel, See diagram on pg 37.
SSPNOP  Semi-span outboard panel. Not required for straight
        tapered planform.
SAVSI   Inboard panel sweep angle
SAVSO   Outboard panel sweep angle
CHSTAT  Reference chord station for inboard and outboard panel
        sweep angles, fraction of chord
TWISTA  Twist angle, negative leading edge rotated down (from
        exposed root to tip)
SSPNDD  Semi-span of outboard panel with dihedral
DHDADI  Dihedral angle of inboard panel
DHDADO  Dihedral angle of outboard panel. If DHDADI=DHDADO only
        input DHDADI
TYPE    1.0 - Straight tapered planform
        2.0 - Double delta planform (aspect ratio <= 3)
        3.0 - Cranked planform (aspect ratio > 3)


Example:

 $WGPLNF CHRDR=23.8,CHRDTP=4.8,CHRDBP=12.4,
   SSPN=46.9,SSPNOP=31.1,SSPNE=40.0,CHSTAT=.25,TWISTA=0.,TYPE=1.,
   SAVSI=29.,SAVSO=26.0,DHDADI=0.,DHDADO=4.$ 
	   
or
	
 $VTPLNF CHRDR=15.9,CHRDTP=4.8,SAVSI=33.,
   SSPN=27.6,SSPNOP=0.,SSPNE=20.7,CHSTAT=.25,TWISTA=0.,TYPE=1.$

 $VTPLNF CHRDTP=3.63, SSPNE=8.85,  SSPN=9.42, CHRDR=8.3, 
   SAVSI=32.3,  CHSTAT=0.25, TYPE=1.0$

or

 $HTPLNF CHRDR=4.99, CHRDTP=2.48,
   SSPN=9.42, SSPNE=9.21,
   SAVSI=5.32,
   CHSTAT=0.25, TWISTA=0.0,
   DHDADI=9.2,
   TYPE=1.0$

 */
	public static String generateBlockGenericPLNF(
			String key,
			Double chrdr, Double chrdtp, Double chrdbp,
			Double sspn, Double sspnop, Double sspne,
			Double chstat, Double twista,
			Double savsi, Double savso, Double dhdadi, Double dhdado,
			int type
			) {
		//------------------------------------------
		// TODO ? manage type of wing ?
		
		StringBuilder sb = new StringBuilder();
		// Return an empty string if key is unknown
		if (!(key.equals("WGPLNF") || key.equals("HTPLNF") || key.equals("VTPLNF")))
			return "";
		// print $<key>=
       	sb.append(" $").append(key).append(" ");
       	sb.append("CHRDR=").append(chrdr).append(", ") // Chord root
       	  .append("CHRDTP=").append(chrdtp).append(", "); // Tip chord
       	if (sspnop != null)
       		sb.append("CHRDBP=").append(chrdbp).append(", "); // Chord at breakpoint. 
       	                                                      // Not required for straight tapered planform.
       	
       	sb.append("\n   SSPN=").append(sspn).append(", "); // Semi-span theoretical panel from theoretical root chord
       	
       	if (sspnop != null)
       		sb.append("SSPNOP=").append(sspnop).append(", "); // Semi-span outboard panel.
                                                              // Not required for straight tapered planform
       	
       	sb.append("SSPNE=").append(sspne).append(", "); // Semi-span exposed panel
       	
       	sb.append("CHSTAT=").append(chstat).append(", "); // Reference chord station for inboard and outboard panel
                                                          // sweep angles, fraction of chord
       	
       	sb.append("TWISTA=").append(twista).append(", "); // Twist angle, negative leading edge rotated down 
                                                          // (from exposed root to tip)
       	
       	sb.append("\n   SAVSI=").append(savsi).append(", "); // Inboard panel sweep angle
       	if (sspnop != null)
       		sb.append("SAVSO=").append(savso).append(", "); // Outboard panel sweep angle

       	if (dhdadi != null)
       		sb.append("DHDADI=").append(dhdadi).append(", "); // Dihedral angle of inboard panel
       	if (sspnop != null) {
       		if (dhdado != null)
       			sb.append("DHDADO=").append(dhdado).append(", "); // Dihedral angle of outboard panel. 
       	                                                          // If DHDADI=DHDADO only input DHDADI
       	}
       	
       	sb.append("TYPE=").append((double)type); // TYPE    1.0 - Straight tapered planform
                                                 //         2.0 - Double delta planform (aspect ratio <= 3)
                                                 //         3.0 - Cranked planform (aspect ratio > 3)
   		// no comma       	
       	sb.append("$");
       	return sb.toString();		
	}
	
/*
 
---------------------------------------------------------------------
Jet Power Effects parameters
---------------------------------------------------------------------
AIETLJ  Angle of incidence of engine thrust line, deg
AMBSTP  Ambient static pressure
AMBTMP  Ambient temperature, deg
JEALOC  Axial location of jet engine exit, feet
JEANGL  Jet exit angle
JELLOC  Lateral location of jet engine, ft
JERAD   Radius of jet exit
JESTMP  Jet exit static temperature
JETOTP  Jet exit total pressure
JEVELO  Jet exit velocity
JEVLOC  Vertical location of jet engine exit, feet
JIALOC  Axial location of jet engine inlet, feet
JINLTA  Jet engine inlet area, square feet
NENGSJ  Number of engines (1 or 2)
THSTCJ  Thrust coefficient  2T/(PV^2*Sref)
        Set this to 0 to keep power effects out of coefficients.

Example:

 $JETPWR AIETLJ=-2.0, AMBSTP=2116.8, AMBTMP=59.7, JEALOC=42.25, 
         JEALOC=58.0, JELLOC=15.9,   JERAD=2.065, JEVLOC=-5.2, 
         JIALOC=34.5, JINLTA=13.4,   NENGSJ=2.0,  THSTCJ=0.0,
         JEANGL=-2.0$ 

 */
	public static String generateBlockJETPWR(
			Double aietlj, Double ambstp, Double ambtmp, 
			List<Double> jealocList, // AIETLJ, AMBSTP, AMBTMP, JEALOC
			Double jelloc, Double jerad, Double jevloc, // JELLOC, JERAD, JEVLOC
			Double jialoc, Double jinlta, int nengsj, Double thstcj, // JIALOC, JINLTA, NENGSJ, THSTCJ
			Double jeangl // JEANGL
			) {
		StringBuilder sb = new StringBuilder();
		sb.append(" $JETPWR ");
		sb.append("AIETLJ=").append(aietlj).append(", ");
		sb.append("AMBSTP=").append(ambstp).append(", ");
		sb.append("AMBTMP=").append(ambtmp).append(", ");
		sb.append("\n   ");
		
		sb.append(
				jealocList.stream()
				.map(value -> "JEALOC="+value.toString())
				.collect(Collectors.joining(", "))
				).append(", ");

		
		sb.append("JELLOC=").append(jelloc).append(", ");
		sb.append("JERAD=").append(jerad).append(", ");
		sb.append("JEVLOC=").append(jevloc).append(", ");
		sb.append("\n   ");
		sb.append("JIALOC=").append(jialoc).append(", ");
		sb.append("JINLTA=").append(jinlta).append(", ");
		sb.append("THSTCJ=").append(thstcj).append(", ");
		sb.append("\n   ");
		sb.append("NENGSJ=").append((double)nengsj).append(", ");
		sb.append("\n   ");
		sb.append("JEANGL=").append(jeangl); // no comma
		sb.append("$");
		return sb.toString();		
	}


/*

---------------------------------------------------------------------
Propulsion parameters for Propeller Power Effects
---------------------------------------------------------------------
AIETLP  Angle of incidence of engine thrust axis, deg
NENGSP  Number of engines (1 or 2 only)
THSTCP  Thrust coefficient 2T/PV^2 Sref
PHALOC  Axial location of propeller hub
PHVLOC  Vertical location of propeller hub
PRPRAD  Propeller radius
ENGFCT  Empirical normal force factor
        Not required if blade widths are input.
BWAPR3  Blade width at 0.3 propeller radius
        Not required if empirical normal force factor is input.
BWAPR6  Blade width at 0.6 propeller radius
        Not required if empirical normal force factor is input.
BWAPR9  Blade width at 0.9 propeller radius
        Not required if empirical normal force factor is input.
NOPBPE  Number of propeller blades per engine
BAPR75  Blade angle at 0.75 propeller radius
YP      Lateral location of engine
CROT    .true.  Counter rotation propeller,
        .false. Non counter rotating

Example:

 $PROPWR AIETLP=0.0,NENGSP=2.0,THSTCP=0.0, 
         PHALOC=4.5,PHVLOC=4.0,PRPRAD=3.75, 
         ENGFCT=0.8,NOPBPE=3.0,
         YP=6.0,CROT=.FALSE.$

 */
	public static String generateBlockPROPWR(
			int nengsp, int nopbpe, // NENGSP, NOPBPE
			Double aietlp, Double thstcp, // AIETLP, THSTCP, 
	        Double phaloc, Double phvloc, Double prprad, // PHALOC, PHVLOC, PRPRAD 
	        Double engfct, Double yp, // ENGFCT, YP
	        boolean crot // CROT
	        ) {
		StringBuilder sb = new StringBuilder();
		sb.append(" $PROPWR ");
		sb.append("NENGSP=").append((double)nengsp).append(", ");
		sb.append("AIETLP=").append(aietlp).append(", ");
		sb.append("THSTCP=").append(thstcp).append(", ");
		sb.append("\n   ");
		sb.append("PHALOC=").append(phaloc).append(", ");
		sb.append("PHVLOC=").append(phvloc).append(", ");
		sb.append("PRPRAD=").append(prprad).append(", ");
		sb.append("\n   ");
		sb.append("ENGFCT=").append(engfct).append(", ");
		sb.append("NOPBPE=").append((double)nopbpe).append(", ");
		sb.append("\n   ");
		sb.append("YP=").append(yp).append(", ");
		sb.append("CROT=").append(".").append(String.valueOf(crot).toUpperCase()).append(".");
		// no comma
		sb.append("$");
		return sb.toString();		
	}
	
/*

---------------------------------------------------------------------
Symetrical Flap Deflection parameters
---------------------------------------------------------------------
DATCOM pg 47 states :

 "In general, the eight flap types defined using SYMFLP
  (variable FTYPE) are assumed to be located on the most
  aft lifting surface, either horizontal tail or wing if
  a horizontal tail is not defined."

FTYPE   Flap type
           1.0  Plain flaps
           2.0  Single slotted flaps
           3.0  Fowler flaps
           4.0  Double slotted flaps
           5.0  Split flaps
           6.0  Leading edge flap
           7.0  Leading edge slats
           8.0  Krueger
NDELTA  Number of flap or slat deflection angles, max of 9

DELTA   Flap deflection angles measured streamwise
        (NDELTA values in array)
PHETE   Tangent of airfoil trailine edge angle based on ordinates at
        90 and 99 percent chord
PHETEP  Tangent of airfoil trailing edge angle based on ordinates at
        95 and 99 percent chord
CHRDFI  Flap chord at inboard end of flap, measured parallel to
        longitudinal axis
CHRDFO  Flap chord at outboard end of flap, measured parallel to
        longitudinal axis
SPANFI  Span location of inboard end of flap, measured perpendicular
        to vertical plane of symmetry
SPANFO  Span location of outboard end of flap, measured perpendicular
        to vertical plane of symmetry
CPRMEI  Total wing chord at inboard end of flap (translating devices
        only) measured parallel to longitudinal axis
        (NDELTA values in array)
           Single-slotted, Fowler, Double-slotted, leading-edge
           slats, Krueger flap, jet flap
CPRMEO  Total wing chord at outboard end of flap (translating devices
        only) measured parallel to longitudinal axis
        (NDELTA values in array)
           Single-slotted, Fowler, Double-slotted, leading-edge
           slats, Krueger flap, jet flap
CAPINS                           (double-slotted flaps only)
CAPOUT                           (double-slotted flaps only)
DOSDEF                           (double-slotted flaps only)
DOBCIN                           (double-slotted flaps only)
DOBCOT                           (double-slotted flaps only)
SCLD    Increment in section lift coefficient due to
        deflecting flap to angle DELTA[i]      (optional)
        (NDELTA values in array)
SCMD    Increment in section pitching moment coefficient due to
        deflecting flap to angle DELTA[i]      (optional)
        (NDELTA values in array)
CB      Average chord of the balance    (plain flaps only)
TC      Average thickness of the control at hinge line
                                        (plain flaps only)
NTYPE   Type of nose
           1.0  Round nose flap
           2.0  Elliptic nose flap
           3.0  Sharp nose flap
JETFLP  Type of flap
           1.0  Pure jet flap
           2.0  IBF
           3.0  EBF
CMU     Two-dimensional jet efflux coefficient
DELJET  Jet deflection angle
        (NDELTA values in array)
EFFJET  EBF Effective jet deflection angle
        (NDELTA values in array)

Example:

 $SYMFLP FTYPE=1.,NDELTA=9.,DELTA(1)=-40.,-30.,-20.,-10.,
   0.,10.,20.,30.,40.,SPANFI=0.,SPANFO=14.,CHRDFI=1.72,
   CHRDFO=1.72,NTYPE=1.0,CB=.50,TC=.44,PHETE=.003,PHETEP=.002$


 */
	
	public static String generateBlockSYMFLP(
			int ftype, // FTYPE
			List<Double> deltaList, // DELTA
			Double spanfi, Double spanfo, // SPANFI, SPANFO
			Double chrdfi, Double chrdfo, // CHRDFI, CHRDFO
			int ntype, // NTYPE
			Double cb, Double tc, Double phete, Double phetep // CB, TC, PHETE, PHETEP
			) {
		StringBuilder sb = new StringBuilder();
		sb.append(" $SYMFLP ");
		sb.append("FTYPE=").append((double)ftype).append(", ");
		sb.append("NDELTA=").append((double)deltaList.size()).append(", ");
		sb.append("\n   DELTA(1)=");
		sb.append(
				WordUtils.wrap( // Apache Commons Lang
						deltaList.stream()
						.map(value -> value.toString())
						.collect(Collectors.joining(", "))
						, 
						60, // wrapLength
						"\n      ", // newLineStr
						false // wrapLongWords 
						)
				).append(",");
		sb.append("\n   ");
		sb.append("SPANFI=").append(spanfi).append(", ");
		sb.append("SPANFO=").append(spanfo).append(", ");
		sb.append("CHRDFI=").append(chrdfi).append(", ");
		sb.append("CHRDFO=").append(chrdfo).append(", ");
		sb.append("\n   ");
		sb.append("NTYPE=").append((double)ntype).append(", ");
		sb.append("CB=").append(cb).append(", ");
		sb.append("TC=").append(tc).append(", ");
		sb.append("PHETE=").append(phete).append(", ");
		sb.append("PHETEP=").append(phetep); // no comma
		sb.append("$");
		return sb.toString();		
	}

	public static String formatAsDatcomPlusInput(DatcomInputData inputData) {

		StringBuilder sb = new StringBuilder();
		sb.append("* Description: " + inputData.getDescription()).append("\n\n");
		sb.append("* Engine type: ").append(inputData.getEngineType()).append("\n\n");
		sb.append("*************************************\n");
		sb.append("* List of command card\n");
		sb.append("*************************************\n");
		if(inputData.getCommand_TRIM())
			sb.append("TRIM\n");
		if(inputData.getCommand_DAMP())
			sb.append("DAMP\n");
		if(inputData.getCommand_PART())
			sb.append("PART\n");
		sb.append("DERIV ").append(inputData.getCommand_DERIV()).append("\n");
		sb.append("\n");

		sb.append("*************************************\n");
		sb.append("* Flight Conditions\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockFLTCON(
						inputData.getFltcon_MACH(), 
						inputData.getFltcon_ALT(), 
						inputData.getFltcon_ALSCHD(), 
						inputData.getFltcon_GAMMA(), 
						inputData.getFltcon_LOOP(), 
						inputData.getFltcon_RNNUB()
						)
				);
		sb.append("\n");
		sb.append("\n");

		sb.append("*************************************\n");
		sb.append("* Reference Parameters\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockOPTINS(
						inputData.getOptins_BLREF(), 
						inputData.getOptins_SREF(), 
						inputData.getOptins_CBARR()
						)
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Group II Synthesis Parameters\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockSYNTHS(
						inputData.getSynths_XW(), inputData.getSynths_ZW(), 
						inputData.getSynths_ALIW(), 
						inputData.getSynths_XCG(), inputData.getSynths_ZCG(), 
						inputData.getSynths_XH(), inputData.getSynths_ZH(), 
						inputData.getSynths_XV(), inputData.getSynths_ZV(), 
						inputData.getSynths_XVF(), inputData.getSynths_ZVF(), 
						inputData.getSynths_VERTUP())
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Body Configuration Parameters\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockBODY(
						inputData.getBody_BNOSE(), inputData.getBody_BTAIL(), inputData.getBody_BLA(), 
						inputData.getBody_X(), inputData.getBody_ZU(), inputData.getBody_ZL(), 
						inputData.getBody_S())
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Wing planform variables \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockGenericPLNF(
						"WGPLNF", 
						inputData.getWgplnf_CHRDR(), inputData.getWgplnf_CHRDTP(), 
						inputData.getWgplnf_CHRDBP().isPresent() ? inputData.getWgplnf_CHRDBP().get() : null, 
						inputData.getWgplnf_SSPN(), 
						inputData.getWgplnf_SSPNOP().isPresent() ? inputData.getWgplnf_SSPNOP().get() : null, 
						inputData.getWgplnf_SSPNE(), 
						inputData.getWgplnf_CHSTAT(), inputData.getWgplnf_TWISTA(), 
						inputData.getWgplnf_SAVSI(), 
						inputData.getWgplnf_SAVSO().isPresent() ? inputData.getWgplnf_SAVSO().get() : null,  
						inputData.getWgplnf_DHDADI(), 
						inputData.getWgplnf_DHDADO().isPresent() ? inputData.getWgplnf_DHDADO().get() : null, 
						inputData.getWgplnf_TYPE())
				);
		sb.append("\n");
		sb.append("\n");
		
		if (inputData.getEngineType() == DatcomEngineType.JET) {
			sb.append("*************************************\n");
			sb.append("* Jet Power Effects parameters \n");
			sb.append("*************************************\n");
			sb.append(
					DatcomPlusInputGenerator.generateBlockJETPWR(
							inputData.getJetpwr_AIETLJ().get(), 
							inputData.getJetpwr_AMBSTP().get(), 
							inputData.getJetpwr_AMBTMP().get(), 
							inputData.getJetpwr_JEALOC().get(), 
							inputData.getJetpwr_JELLOC().get(), 
							inputData.getJetpwr_JERAD().get(), 
							inputData.getJetpwr_JEVLOC().get(), 
							inputData.getJetpwr_JIALOC().get(), 
							inputData.getJetpwr_JINLTA().get(), 
							inputData.getJetpwr_NENGSJ().get(), 
							inputData.getJetpwr_THSTCJ().get(), 
							inputData.getJetpwr_JEANGL().get()
							)
					);
			sb.append("\n");
			sb.append("\n");
		}

		if (inputData.getEngineType() == DatcomEngineType.PROP) {
			sb.append("*************************************\n");
			sb.append("* Propeller Power Effects parameters \n");
			sb.append("*************************************\n");
			sb.append(
					DatcomPlusInputGenerator.generateBlockPROPWR(
							inputData.getPropwr_NENGSP().get().intValue(), 
							inputData.getPropwr_NOPBPE().get().intValue(), 
							inputData.getPropwr_AIETLP().get(), 
							inputData.getPropwr_THSTCP().get(), 
							inputData.getPropwr_PHALOC().get(), 
							inputData.getPropwr_PHVLOC().get(), 
							inputData.getPropwr_PRPRAD().get(), 
							inputData.getPropwr_ENGFCT().get(), 
							inputData.getPropwr_YP().get(), 
							inputData.getPropwr_CROT().get().booleanValue())
					);
			sb.append("\n");
			sb.append("\n");
		}
		
		
		sb.append("*************************************\n");
		sb.append("* Vertical Tail planform variables \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockGenericPLNF(
						"VTPLNF", 
						inputData.getVtplnf_CHRDR(), inputData.getVtplnf_CHRDTP(), 
						inputData.getVtplnf_CHRDBP().isPresent() ? inputData.getVtplnf_CHRDBP().get() : null, 
						inputData.getVtplnf_SSPN(), 
						inputData.getVtplnf_SSPNOP().isPresent() ? inputData.getVtplnf_SSPNOP().get() : null, 
						inputData.getVtplnf_SSPNE(), 
						inputData.getVtplnf_CHSTAT(), inputData.getVtplnf_TWISTA(), 
						inputData.getVtplnf_SAVSI(), 
						inputData.getVtplnf_SAVSO().isPresent() ? inputData.getVtplnf_SAVSO().get() : null,  
						inputData.getVtplnf_DHDADI(), 
						inputData.getVtplnf_DHDADO().isPresent() ? inputData.getVtplnf_DHDADO().get() : null, 
						inputData.getVtplnf_TYPE())
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Horizontal Tail planform variables \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockGenericPLNF(
						"HTPLNF", 
						inputData.getHtplnf_CHRDR(), inputData.getHtplnf_CHRDTP(), 
						inputData.getHtplnf_CHRDBP().isPresent() ? inputData.getHtplnf_CHRDBP().get() : null, 
						inputData.getHtplnf_SSPN(), 
						inputData.getHtplnf_SSPNOP().isPresent() ? inputData.getHtplnf_SSPNOP().get() : null, 
						inputData.getHtplnf_SSPNE(), 
						inputData.getHtplnf_CHSTAT(), inputData.getHtplnf_TWISTA(), 
						inputData.getHtplnf_SAVSI(), 
						inputData.getHtplnf_SAVSO().isPresent() ? inputData.getHtplnf_SAVSO().get() : null,  
						inputData.getHtplnf_DHDADI(), 
						inputData.getHtplnf_DHDADO().isPresent() ? inputData.getHtplnf_DHDADO().get() : null, 
						inputData.getHtplnf_TYPE())
				);
		sb.append("\n");
		sb.append("\n");

		sb.append("*************************************\n");
		sb.append("* Symetrical Flap Deflection parameters \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockSYMFLP(
						inputData.getSymflp_FTYPE().get(), 
						inputData.getSymflp_DELTA().get(), 
						inputData.getSymflp_SPANFI().get(), 
						inputData.getSymflp_SPANFO().get(), 
						inputData.getSymflp_CHRDFI().get(), 
						inputData.getSymflp_CHRDFO().get(), 
						inputData.getSymflp_NTYPE().get(), 
						inputData.getSymflp_CB().get(), 
						inputData.getSymflp_TC().get(), 
						inputData.getSymflp_PHETE().get(), 
						inputData.getSymflp_PHETEP().get())
				);
		sb.append("\n");
		sb.append("\n");
		
		// TODO: handle this via the DatcomInputData
		sb.append("*************************************\n");
		sb.append("* Wing Sectional Characteristics Parameters\n");
		sb.append("*************************************\n");
		sb.append("NACA-W-4-0012-25\n");
		sb.append("NACA-H-4-0012-25\n");
		sb.append("NACA-V-4-0012-25\n");
		sb.append("\n");

		sb.append("CASEID Total: X-airplane");
		
		return sb.toString();
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
		File inputFile = new File(binDirPath + File.separator + "X-airplane.dcm");
		System.out.println("Input file full path: " + inputFile);
		System.out.println("Input file name: " + inputFile.getName());
		System.out.println("Input file exists? " + inputFile.exists());

		DatcomPlusInputGenerator.writeTemplate(inputFile.getAbsolutePath());

	}

}

/*

---------------------------------------------------------------------
Flight Conditions
---------------------------------------------------------------------
WT      Vehicle Weight
LOOP    Program Looping Control
           1 = vary altitude and mach together, default
           2 = vary Mach, at fixed altitude
           3 = vary altitude, at fixed Mach
NMACH   Number of Mach numbers or velocities to be run, max of 20
        Note: This parameter, along with NALT, may affect the
        proper setting of the LOOP control parameter.
MACH    Array(20) Values of freestream Mach number
VINF    Array(20) Values of freestream speed (unit: l/t)
NALPHA  Number of angles of attack to be run, max of 20
ALSCHD  Array(20) Values of angles of attack, in ascending order
RNNUB   Array(20) Reynolds number per unit length
        Freestream Reynolds numbers. Each array element must
        correspond to the respective Mach number/freestream
        speed input, use LOOP=1.0
NALT    Number of atmospheric conditions to be run, max of 20
        input as either altitude or pressure and temperature
        Note: This parameter, along with NMACH, may affect the
        proper setting of the LOOP control parameter.
ALT     Array(20) Values of geometric altitude
        Number of altitude and values. Note, Atmospheric conditions
        are input either as altitude or pressure and temperature. (MAX 20)
PINF    Array(20) Values of freestream Static Pressure
TINF    Array(20) Values of freestream Temperature
HYPERS  =.true.  Hypersonic analysis at all Mach numbers > 1.4
STMACH  Upper limit of Mach numbers for subsonic analysis
        (0.6<STMACH<0.99), Default to 0.6 if not input.
TSMACH  Lower limit of Mach number for Supersonic analysis
        (1.01<=TSMACH<=1.4)  Default to 1.4
TR      Drag due to lift transition flag, for regression analysis
        of wing-body configuration.
        = 0.0 for no transition (default)
        = 1.0 for transition strips or full scale flight
GAMMA   Flight path angle

---------------------------------------------------------------------
Reference Parameters
---------------------------------------------------------------------
SREF    Reference area value of theoretical wing area used by program
        if not input
CBARR   Longitudinal reference length value of theoritcal wing
        Mean Aerodynamic Chord used by program if not input
BLREF   Lateral reference length value of wing span used by program
ROUGFC  Surface roughness factor, equivalent sand roughness, default
        to 0.16e-3 inches (Natural sheet metal)
        0.02/0.08E-3 - Polished metal or wood
        0.16E-3  - Natural sheet metal
        0.25E-3  - Smooth matte paint, carefully applied
        0.40E-3  - Standard camouflage paint, average application

---------------------------------------------------------------------
Group II, Synthesis Parameters
---------------------------------------------------------------------
XCG     Longitudinal location of cg (moment ref. center)
ZCG     Vertical location of CG relative to reference plane
XW      Longitudinal location of theoretical wing apex (where
        leading edge would intersect long axis)
ZW      Vertical location of theoretical wing apex relative to
        reference plane
ALIW    Wing root chord incident angle measured from reference plane
XH      Longitudinal location of theoretical horizontal tail apex.
        If HINAX is input, XH and ZH are evaluated at zero incidence.
ZH      Vertical location of theoretical horizontal tail apex
        relative to reference plane. If HINAX is input, XH and ZH
        are evaluated at zero incidence.
ALIH    Horizontal tail root chord incidence angle measured from
        reference plane
XV      Longitudinal location of theoretical vertical tail apex
XVF     Longitudinal location of theoretical ventral fin apex
ZV      Vertical location of theoretical vertical tail apex
        This kinda makes sense only for twin tails that are canted
ZVF     Vertical location of theoretical ventral fin apex
        This kinda makes sense only for twin tails that are canted
SCALE   Vehicle scale factor (multiplier to input dimensions)
VERTUP  Vertical panel above reference plane (default=true)
HINAX   Longitudinal location of horizontal tail hinge axis.
        Required only for all-moveable horizontal tail trim option.

---------------------------------------------------------------------
Body Configuration Parameters
---------------------------------------------------------------------
Here is an error message output by DIGDAT concerning body geometry:
IN NAMELIST BODY, ONLY THE FOLLOWING COMBINATIONS OF VARIABLES CAN BE USED
FOR A CIRCULAR BODY, SPECIFY X AND R OR X AND S
FOR AN ELLIPTICAL BODY, SPECIFY X AND R OR X AND S, AND THE VARIABLE ELLIP
FOR OTHER BODY SHAPES X, R, S, AND P MUST ALL BE SPECIFIED

NX      Number of longitudinal body stations at which data is
        specified, max of 20
X       Array(20) Longitudinal distance measured from arbitray location
S       Array(20) Cross sectional area at station. See note above.
P       Array(20) Periphery at station Xi. See note above.
R       Array(20) Planform half width at station Xi. See note above.
ZU      Array(20) Z-coordinate at upper body surface at station Xi
        (positive when above centerline)
        [Only required for subsonic asymmetric bodies]
ZL      Array(20) Z-coordinate at lower body surface at station Xi
        (negative when below centerline)
        [Only required for subsonic asymmetric bodies]
BNOSE   Nosecone type  1.0 = conical (rounded), 2.0 = ogive (sharp point)
        [Not required in subsonic speed regime]
BTAIL   Tailcone type  1.0 = conical, 2.0 = ogive, omit for lbt = 0
        [Not required in subsonic speed regime]
BLN     Length of body nose
        Not required in subsonic speed regime
BLA     Length of cylindrical afterbody segment, =0.0 for nose alone
        or nose-tail configuration
        Not required in subsonic speed regime
DS      Nose bluntness diameter, zero for sharp nosebodies
        [Hypersonic speed regime only]
ITYPE   1.0 = straight wing, no area rule
        2.0 = swept wing, no area rule (default)
        3.0 = swept wing, area rule
METHOD  1.0 = Use existing methods (default)
        2.0 = Use Jorgensen method

---------------------------------------------------------------------
Wing planform variables
---------------------------------------------------------------------
CHRDR   Chord root
CHRDBP  Chord at breakpoint. Not required for straight
        tapered planform.
CHRDTP  Tip chord
SSPN    Semi-span theoretical panel from theoretical root chord
SSPNE   Semi-span exposed panel, See diagram on pg 37.
SSPNOP  Semi-span outboard panel. Not required for straight
        tapered planform.
SAVSI   Inboard panel sweep angle
SAVSO   Outboard panel sweep angle
CHSTAT  Reference chord station for inboard and outboard panel
        sweep angles, fraction of chord
TWISTA  Twist angle, negative leading edge rotated down (from
        exposed root to tip)
SSPNDD  Semi-span of outboard panel with dihedral
DHDADI  Dihedral angle of inboard panel
DHDADO  Dihedral angle of outboard panel. If DHDADI=DHDADO only
        input DHDADI
TYPE    1.0 - Straight tapered planform
        2.0 - Double delta planform (aspect ratio <= 3)
        3.0 - Cranked planform (aspect ratio > 3)

---------------------------------------------------------------------
Wing Sectional Characteristics Parameters
---------------------------------------------------------------------
The section aerodynamic characteristics for these surfaces are
input using either the sectional characteristics namelists WGSCHR,
HTSCHR, VTSCHR and VFSCHR and/or the NACA control cards. Airfoil
characteristics are assummed constant for each panel of the planform.

To avoid having to input all the airfoil sectional characteristics,
you can specify the NACA airfoil designation. Starts in Column 1.

NACA x y zzzzzz

 where:
    column 1-4   NACA
           5     any deliminator
           6     W, H, V, or F  Planform for which the airfoil
                                designation applies:  Wing, Horizontal
                                tail, Vertical tail, or Ventral fin.
           7     any deliminator
           8     1,4,5,6,S      Type of airfoil section: 1-series,
                                4-digit, 5-digit, 6-series, or Supersonic
           9     any deliminator
          10-80  Designation, columns are free format, blanks are ignored

 TOVC    Maximum airfoil section thickness fraction of chord
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 DELTAY  Difference between airfoil ordinates at 6% and 15% chord,
         percent chord (% correct ???)
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 XOVC    Chord location of maximum airfoil thickness, fraction of chord
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 CLI     Airfoil section design lift coefficient
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 ALPHAI  Angle of attack at section design lift coefficient, deg
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 CLALPA  Airfoil section lift curve slope dCl/dAlpha, per deg (array 20)
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 CLMAX   Airfoil section maximum lift cofficient (array 20)
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 CMO     Section zero lift pitching moment coefficient
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 LERI    Airfoil leading edge radius, fraction of chord
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 LERO    RLE for outboard panel, fraction of chord
         [Required input].
         Not required for straight tapered planforms.
 CAMBER  Cambered airfoil flag flag
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 TOVCO   t/c for outboard panel
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
         Not required for straight tapered planforms.
 XOVCO   (x/c)max for outboard panel
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
         Not required for straight tapered planforms.
 CMOT    Cmo for outboard panel
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
         Not required for straight tapered planforms.
 CLMAXL  Airfoil maximum lift coefficient at mach = 0.0
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 CLAMO   Airfoil section lift curve slope at Mach=0.0, per deg
         [Not required for subsonic speed regime. Required input
         for transonic speed regime, user supplied or computed if
         NACA card supplied]
  TCEFF  Planform effective thickness ratio, fraction of chord
         [Not required for subsonic speed regime. Required input
         for transonic speed regime, user supplied or computed if
         NACA card supplied]
 KSHARP  Wave-drag factor for sharp-nosed airfoil section, not
         input for round-nosed airfoils
         [Not required for subsonic speed regime. Required input
         for transonic speed regime, user supplied or computed if
         NACA card supplied]
 SLOPE   Airfoil surface slope at 0,20,40,60,80 and 100% chord, deg.
         Positive when the tangent intersects the chord plane forward
         of the reference chord point
         [Not required for subsonic speed regime. Required input
         for transonic speed regime, user supplied or computed if
         NACA card supplied]
 ARCL    Aspect ratio classification (see table 9, pg 41)
         [Optional input]
 XAC     Section Aerodynamic Center, fraction of chord
         [Optional input, computed by airfoil section module if airfoil
         defined with NACA card or section coordinates]
 DWASH   Subsonic downwash method flag
         = 1.0  use DATCOM method 1
         = 2.0  use DATCOM method 2
         = 3.0  use DATCOM method 3
         Supersonic, use DATCOM method 2
         [Optional input]
         See figure 9 on page 41.
 YCM     Airfoil maximum camber, fraction of chord
         [Required input, user supplied or computed by airfoil
         section module if airfoil defined with NACA card or
         section coordinates]
 CLD     Conical camber design lift coefficient for M=1.0 design
         see NACA RM A55G19 (default to 0.0)
         [Required input]
 TYPEIN  Type of airfoil section coordinates input for airfoil
         section module
         = 1.0  upper and lower surface coordinates (YUPPER and YLOWER)
         = 2.0 Mean line and thickness distribution (MEAN and THICK)
         [Optional input]
 NPTS    Number of section points input, max = 50.0
         [Optional input]
 XCORD   Abscissas of inputs points, TYPEIN=1.0 or 2.0, XCORD(1)=0.0
         XCORD(NPTS)= 1.0 required.
         [Optional input]
 YUPPER  Ordinates of upper surface, TYPEIN=1.0, fraction of chord, and
         requires YUPPER(1)=0.0 and YUPPER(NPTS)=0.0
         [Optional input]
 YLOWER  Ordinates of lower surface, TYPEIN=1.0, fraction of chord,
         and requires YLOWER(1)=0.0 and YLOWER(NPTS)=0.0
         [Optional input]
 MEAN    Ordinates of mean line, TYPEIN=2.0, fraction of chord, and
         requires MEAN(1)=0.0 and MEAN(NPTS)=0.0
         [Optional input]
 THICK   Thickness distribution, TYPEIN=2.0, fraction of chord, and
         requires THICK(1)=0.0 and THICK(NPTS)=0.0
         [Optional input]

---------------------------------------------------------------------
Ground effects parameters
---------------------------------------------------------------------
NGH     Number of ground heights to be run, maximum of 10.
GRDHT   Values of ground heights. Ground heights equal altitude
        of reference plane relative to ground. Ground effect output
        may be obtained at a maximum of ten different ground heights.
        According to the DATCOM, the ground effects become neglible
        when the ground height exceeds the wing span. Through
        testing, there is a minimal effect up to twice the wing
        span, so to keep our tables smooth, let's make the last
        point 1.5b, and the output adds a point at 2b of 0.0. The
        smallest value should NOT be 0.0, which would be the wing
        sitting on the ground. It should be the height of the wing
        with the aircraft sitting on the ground.

---------------------------------------------------------------------
Symetrical Flap Deflection parameters
---------------------------------------------------------------------
DATCOM pg 47 states :

 "In general, the eight flap types defined using SYMFLP
  (variable FTYPE) are assumed to be located on the most
  aft lifting surface, either horizontal tail or wing if
  a horizontal tail is not defined."

FTYPE   Flap type
           1.0  Plain flaps
           2.0  Single slotted flaps
           3.0  Fowler flaps
           4.0  Double slotted flaps
           5.0  Split flaps
           6.0  Leading edge flap
           7.0  Leading edge slats
           8.0  Krueger
NDELTA  Number of flap or slat deflection angles, max of 9

DELTA   Flap deflection angles measured streamwise
        (NDELTA values in array)
PHETE   Tangent of airfoil trailine edge angle based on ordinates at
        90 and 99 percent chord
PHETEP  Tangent of airfoil trailing edge angle based on ordinates at
        95 and 99 percent chord
CHRDFI  Flap chord at inboard end of flap, measured parallel to
        longitudinal axis
CHRDFO  Flap chord at outboard end of flap, measured parallel to
        longitudinal axis
SPANFI  Span location of inboard end of flap, measured perpendicular
        to vertical plane of symmetry
SPANFO  Span location of outboard end of flap, measured perpendicular
        to vertical plane of symmetry
CPRMEI  Total wing chord at inboard end of flap (translating devices
        only) measured parallel to longitudinal axis
        (NDELTA values in array)
           Single-slotted, Fowler, Double-slotted, leading-edge
           slats, Krueger flap, jet flap
CPRMEO  Total wing chord at outboard end of flap (translating devices
        only) measured parallel to longitudinal axis
        (NDELTA values in array)
           Single-slotted, Fowler, Double-slotted, leading-edge
           slats, Krueger flap, jet flap
CAPINS                           (double-slotted flaps only)
CAPOUT                           (double-slotted flaps only)
DOSDEF                           (double-slotted flaps only)
DOBCIN                           (double-slotted flaps only)
DOBCOT                           (double-slotted flaps only)
SCLD    Increment in section lift coefficient due to
        deflecting flap to angle DELTA[i]      (optional)
        (NDELTA values in array)
SCMD    Increment in section pitching moment coefficient due to
        deflecting flap to angle DELTA[i]      (optional)
        (NDELTA values in array)
CB      Average chord of the balance    (plain flaps only)
TC      Average thickness of the control at hinge line
                                        (plain flaps only)
NTYPE   Type of nose
           1.0  Round nose flap
           2.0  Elliptic nose flap
           3.0  Sharp nose flap
JETFLP  Type of flap
           1.0  Pure jet flap
           2.0  IBF
           3.0  EBF
CMU     Two-dimensional jet efflux coefficient
DELJET  Jet deflection angle
        (NDELTA values in array)
EFFJET  EBF Effective jet deflection angle
        (NDELTA values in array)

---------------------------------------------------------------------
Asymmetrical Control Deflection parameters : Ailerons
---------------------------------------------------------------------
STYPE   Type
           1.0  Flap spoiler on wing
           2.0  Plug spoiler on wing
           3.0  Spoiler-slot-deflection on wing
           4.0  Plain flap aileron
           5.0  Differentially deflected all moveable horizontal tail
NDELTA  Number of control deflection angles, required for all controls,
        max of 9
DELTAL  Defelction angle for left hand plain flap aileron or left
        hand panel all moveable horizontal tail, measured in
        vertical plane of symmetry
DELTAR  Defelction angle for right hand plain flap aileron or right
        hand panel all moveable horizontal tail, measured in
        vertical plane of symmetry
SPANFI  Span location of inboard end of flap or spoiler control,
        measured perpendicular to vertical plane of symmetry
SPANFO  Span location of outboard end of flap or spoiler control,
        measured perpendicular to vertical plane of symmetry
PHETE   Tangent of airfoil trailing edge angle based on ordinates
        at x/c - 0.90 and 0.99
CHRDFI  Aileron chord at inboard end of plain flap aileron,
        measured parallel to longitudinal axis
CHRDFO  Aileron chord at outboard end of plain flap aileron,
        measured parallel to longitudinal axis
DELTAD  Projected height of deflector, spoiler-slot-deflector
        control, fraction of chord
DELTAS  Projected height of spoiler, flap spoiler, plug spoiler and
        spoiler-slot-deflector control; fraction of chord
XSOC    Distance from wing leading edge to spoiler lip measured
        parallel to streamwise wng chord, flap and plug spoilers,
        fraction of chord
XSPRME  Distance from wing leading edge to spoiler hinge line
        measured parallel to streamwise chord, flap spoiler,
        plug spoiler and spoiler-slot-deflector control, fraction
        of chord
HSOC    Projected height of spoiler measured from and normal to
        airfoil mean line, flap spoiler, plug spoiler and spoiler-
        slot-reflector, fraction of chord

---------------------------------------------------------------------
Propulsion parameters for Propeller Power Effects
---------------------------------------------------------------------
AIETLP  Angle of incidence of engine thrust axis, deg
NENGSP  Number of engines (1 or 2 only)
THSTCP  Thrust coefficient 2T/PV^2 Sref
PHALOC  Axial location of propeller hub
PHVLOC  Vertical location of propeller hub
PRPRAD  Propeller radius
ENGFCT  Empirical normal force factor
        Not required if blade widths are input.
BWAPR3  Blade width at 0.3 propeller radius
        Not required if empirical normal force factor is input.
BWAPR6  Blade width at 0.6 propeller radius
        Not required if empirical normal force factor is input.
BWAPR9  Blade width at 0.9 propeller radius
        Not required if empirical normal force factor is input.
NOPBPE  Number of propeller blades per engine
BAPR75  Blade angle at 0.75 propeller radius
YP      Lateral location of engine
CROT    .true.  Counter rotation propeller,
        .false. Non counter rotating

---------------------------------------------------------------------
Jet Power Effects parameters
---------------------------------------------------------------------
AIETLJ  Angle of incidence of engine thrust line, deg
AMBSTP  Ambient static pressure
AMBTMP  Ambient temperature, deg
JEALOC  Axial location of jet engine exit, feet
JEANGL  Jet exit angle
JELLOC  Lateral location of jet engine, ft
JERAD   Radius of jet exit
JESTMP  Jet exit static temperature
JETOTP  Jet exit total pressure
JEVELO  Jet exit velocity
JEVLOC  Vertical location of jet engine exit, feet
JIALOC  Axial location of jet engine inlet, feet
JINLTA  Jet engine inlet area, square feet
NENGSJ  Number of engines (1 or 2)
THSTCJ  Thrust coefficient  2T/(PV^2*Sref)
        Set this to 0 to keep power effects out of coefficients.

*/