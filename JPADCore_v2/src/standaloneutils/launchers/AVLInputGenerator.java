package standaloneutils.launchers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AVLInputGenerator {

	
	public static void writeDataToFile(AVLMainInputData inputData, String filePath) {
		// TODO
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

		content.add("# -Unnamed-"); // insert description

		// TODO

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

	public static String formatAsAVLInput(AVLMainInputData inputData) {

		StringBuilder sb = new StringBuilder();
		sb.append("# -Unnamed-\n");

		// TODO
		// ...
		
		return sb.toString();
	}
	
}

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