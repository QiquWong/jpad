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
import java.util.List;

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
		ALSCHD("ALSCHD"), GAMMA("GAMMA"), LOOP("LOOP"),
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
		
		System.out.println("Writing " + path.toString() + " ...");
		
		// first delete file if exists
		boolean pathExists =
				Files.exists(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
		if (pathExists) {
			try {
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
		content.add("* Navion template");
		content.add("* ");

		content.add(KEYWORDS.TRIM.toString());
		content.add(KEYWORDS.DAMP.toString());
		content.add(KEYWORDS.PART.toString());
		content.add(KEYWORDS.DERIV.toString() + " " +KEYWORDS.RAD.toString());
		
		Charset charset = Charset.forName("utf-8");
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			for (String line : content) {
				writer.write(line, 0, line.length());
				writer.newLine();
			}
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
		File inputFile = new File(binDirPath + File.separator + "navion.dcm");
		System.out.println("Input file full path: " + inputFile);
		System.out.println("Input file name: " + inputFile.getName());
		System.out.println("Input file exists? " + inputFile.exists());

		DatcomPlusInputGenerator.writeTemplate(inputFile.getAbsolutePath());
		
	}

}
