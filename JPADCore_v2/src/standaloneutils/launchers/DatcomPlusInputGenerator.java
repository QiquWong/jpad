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
						Arrays.asList(0.3), // list of Mach numbers
						Arrays.asList(1500.0), // list of altitudes
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
				generateBlockWGPLNF(
						23.8, 4.8, 12.4, // CHRDR, CHRDTP, CHRDBP
						46.9, 31.1, 40.0, // SSPN, SSPNOP, SSPNE
						0.25, 0.0, 1, // CHSTAT, TWISTA, TYPE
						29.0, 26.0, 0.0, 4.0 // SAVSI, SAVSO, DHDADI, DHDADO
						)
				);
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Jet Power Effects parameters (page 51)");
		content.add("**************************************************************************");
		// TODO
/*

 $JETPWR AIETLJ=-2.0, AMBSTP=2116.8, AMBTMP=59.7, JEALOC=42.25, 
         JEALOC=58.0, JELLOC=15.9,   JERAD=2.065, JEVLOC=-5.2, 
         JIALOC=34.5, JINLTA=13.4,   NENGSJ=2.0,  THSTCJ=0.0,
         JEANGL=-2.0$ 
 
 */
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Vertical Tail planform variables (page 37-38)");
		content.add("**************************************************************************");
		content.add(
				generateBlockVTPLNF(
						15.9, 4.8, 12.4, // CHRDR, CHRDTP, CHRDBP
						27.6, 0.0, 20.7, // SSPN, SSPNOP, SSPNE
						0.25, 0.0, 1, // CHSTAT, TWISTA, TYPE
						33.0, 33.0 // SAVSI, SAVSO
						)
		);
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Horizontal Tail planform variables (page 37-38)");
		content.add("**************************************************************************");
		// TODO
/*

 $HTPLNF CHRDR=12.4,CHRDTP=4.1,
    SSPN=17.6,SSPNE=15.87,CHSTAT=.25,TWISTA=0.,TYPE=1.,
    SAVSI=31.,DHDADI=9.$
 
 */
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Symetrical Flap Deflection parameters");
		content.add("**************************************************************************");
		// TODO
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
		// TODO
/*

NACA-W-4-0012-25
NACA-H-4-0012-25
NACA-V-4-0012-25
 
 */
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

	$FLTCON NMACH=1.0, MACH(1)=0.3, 
	   NALT=1.0, ALT(1)=1.500, 
	   NALPHA=20.0, ALSCHD(1)=-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0,
	   GAMMA=0.0, LOOP=2.0, 
	   RNNUB=20120887.0$

	 */	
	public static String generateBlockFLTCON(
			List<Double> machList, List<Double> altitudeList, List<Double> alphaList,
			Double gamma, int loop, Double reynoldsPerUnitLength
			) {
		StringBuilder sb = new StringBuilder();
		sb.append("$FLTCON ")
		  .append("NMACH=").append((double) machList.size()).append(", ");
		sb.append("MACH(1)=");
		machList.stream()
				.forEach(mach -> sb.append(mach).append(", "));
		sb.append("\n   NALT=").append((double) altitudeList.size()).append(", ");
		sb.append("ALT(1)=");
		altitudeList.stream()
				.forEach(alt -> sb.append(alt).append(", "));
		sb.append("\n   NALPHA=").append((double) alphaList.size()).append(", ");
		sb.append("ALSCHD(1)=");
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
	
	$OPTINS BLREF=93.0,SREF=1329.9,CBARR=14.3$
			
	*/
	public static String generateBlockOPTINS(Double blref, Double sref, Double cbarr) {
		StringBuilder sb = new StringBuilder();
		sb.append("$OPTINS ")
		  .append("BLREF=").append(blref).append(", ")
		  .append("SREF=").append(sref).append(", ")
		  .append("CBARR=").append(blref)
		  .append("$");
		return sb.toString();
	}

	/*

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
		sb.append("$SYNTHS ")
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
		sb.append("$BODY ");
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

	$WGPLNF CHRDR=23.8,CHRDTP=4.8,CHRDBP=12.4,
	   SSPN=46.9,SSPNOP=31.1,SSPNE=40.0,CHSTAT=.25,TWISTA=0.,TYPE=1.,
	   SAVSI=29.,SAVSO=26.0,DHDADI=0.,DHDADO=4.$ 

	*/
	public static String generateBlockWGPLNF(
			Double chrdr, Double chrdtp, Double chrdbp,
			Double sspn, Double sspnop, Double sspne,
			Double chstat, Double twista, int type,
			Double savsi, Double savso, Double dhdadi, Double dhdado
			) {
		StringBuilder sb = new StringBuilder();
		sb.append("$WGPLNF ")
		  .append("CHRDR=").append(chrdr).append(", ").append("CHRDTP=").append(chrdtp).append(", ").append("CHRDBP=").append(chrdbp).append(", ")
		  .append("\n   SSPN=").append(sspn).append(", ").append("SSPNOP=").append(sspnop).append(", ").append("SSPNE=").append(sspne).append(", ")
		  .append("CHSTAT=").append(chstat).append(", ").append("TWISTA=").append(twista).append(", ").append("TYPE=").append((double)type).append(", ")
		  .append("\n   SAVSI=").append(savsi).append(", ").append("SAVSO=").append(savso).append(", ").append("DHDADI=").append(dhdadi).append(", ").append("DHDADO=").append(dhdado) // no comma
		  .append("$");
		return sb.toString();		
	}
	
	/*

	 $VTPLNF CHRDR=15.9,CHRDTP=4.8,SAVSI=33.,
	    SSPN=27.6,SSPNOP=0.,SSPNE=20.7,CHSTAT=.25,TWISTA=0.,TYPE=1.$
	    
	 or
	 
	 $VTPLNF CHRDR=15.9, CHRDTP=4.8, CHRDBP=12.4, 
        SSPN=27.6, SSPNOP=0.0, SSPNE=20.7, CHSTAT=0.25, TWISTA=0.0, TYPE=1.0, 
        SAVSI=33.0, SAVSO=33.0$
	 
	 */
	public static String generateBlockVTPLNF(
			Double chrdr, Double chrdtp, Double chrdbp,
			Double sspn, Double sspnop, Double sspne,
			Double chstat, Double twista, int type,
			Double savsi, Double savso
			) {
		StringBuilder sb = new StringBuilder();
		sb.append("$VTPLNF ")
		  .append("CHRDR=").append(chrdr).append(", ").append("CHRDTP=").append(chrdtp).append(", ").append("CHRDBP=").append(chrdbp).append(", ")
		  .append("\n   SSPN=").append(sspn).append(", ").append("SSPNOP=").append(sspnop).append(", ").append("SSPNE=").append(sspne).append(", ")
		  .append("CHSTAT=").append(chstat).append(", ").append("TWISTA=").append(twista).append(", ").append("TYPE=").append((double)type).append(", ")
		  .append("\n   SAVSI=").append(savsi).append(", ").append("SAVSO=").append(savso) // no comma
		  .append("$");
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
