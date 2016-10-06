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
import java.util.stream.Collectors;

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
				"$"+KEYWORDS.FLTCON.toString() + " "
					+KEYWORDS.NMACH.toString() + "=1.0" + ", "
					+KEYWORDS.MACH.toString() + "(1)=0.3" + ", "
				);
		content.add(
				KEYWORDS.NALT.toString() + "=1.0" + ", "
					+KEYWORDS.ALT.toString() + "(1)=1.500" + ", "
				);
		List<Double> alphas = Arrays.asList(
				-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0,
				10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0);
		content.add(
				KEYWORDS.NALPHA.toString() + "=20.0" + ", "
					+KEYWORDS.ALSCHD.toString() + "(1)="
					+alphas.stream()
				           .map(number -> String.valueOf(number))
				           .collect(Collectors.joining(", "))
				    +","
				);
		content.add(
				KEYWORDS.GAMMA.toString() + "=0.0" + ", "
					+KEYWORDS.LOOP.toString() + "=2.0" + ", "
				);
		content.add(
				KEYWORDS.RNNUB.toString() + "=20120887.0" 
				+"$"
				);
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Reference Parameters (page 29)");
		content.add("**************************************************************************");
		// TODO
/*

$OPTINS BLREF=93.0,SREF=1329.9,CBARR=14.3$		

 */
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Group II     Synthesis Parameters (page 33)");
		content.add("**************************************************************************");
		// TODO
/*

 $SYNTHS XW=28.3,ZW=-1.4,ALIW=1.0,XCG=41.3,ZCG=0.0,
    XH=76.6,ZH=6.2,
    XV=71.1,ZV=7.6,
    XVF=66.2,ZVF=13.1,
    VERTUP=.TRUE.$
 
 */
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Body Configuration Parameters (page 36)");
		content.add("**************************************************************************");
		// TODO
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
       127.81,108.11,95.68,56.88,28.39,3.64,0.11 
 
 */
		content.add(" ");
		content.add(" ");
		
		content.add("**************************************************************************");
		content.add("* Wing planform variables (page 37-38)");
		content.add("**************************************************************************");
		// TODO
/*

 $WGPLNF CHRDR=23.8,CHRDTP=4.8,CHRDBP=12.4,
    SSPN=46.9,SSPNOP=31.1,SSPNE=40.0,CHSTAT=.25,TWISTA=0.,TYPE=1.,
    SAVSI=29.,SAVSO=26.0,DHDADI=0.,DHDADO=4.$ 
 
 */
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
		// TODO
/*

 $VTPLNF CHRDR=15.9,CHRDTP=4.8,SAVSI=33.,
    SSPN=27.6,SSPNOP=0.,SSPNE=20.7,CHSTAT=.25,TWISTA=0.,TYPE=1.$
 
 */
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
