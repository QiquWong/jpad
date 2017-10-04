package sandbox2.adm.cpacs;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

import configuration.MyConfiguration;
import de.dlr.sc.tigl.CpacsConfiguration;
import de.dlr.sc.tigl.Tigl;
import de.dlr.sc.tigl.Tigl.WSProjectionResult;
import de.dlr.sc.tigl.TiglBSpline;
import de.dlr.sc.tigl.TiglException;
import de.dlr.sc.tigl.TiglNativeInterface;
import de.dlr.sc.tigl.TiglPoint;
import de.dlr.sc.tigl.TiglSymmetryAxis;

class MyArgumentSystems_00 {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

}

public class MyTest_Tigl_00 {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	
	public static void main(String[] args) {
		System.out.println("TIGL test ...");

		MyArgumentSystems_00 va = new MyArgumentSystems_00();
		MyTest_Tigl_00.theCmdLineParser = new CmdLineParser(va);
		
		try {
			
			MyTest_Tigl_00.theCmdLineParser.parseArgument(args);
			
		} catch (CmdLineException e) {
			e.printStackTrace();
		}
		
		String cpacsFilePath = va.getInputFile().getAbsolutePath();

		System.out.println("--------------------------------");
		System.out.println("FILE: " + cpacsFilePath);
		
		System.out.println("--------------------------------");
		System.out.println("TiGL Version: " + Tigl.getVersion());

		try (CpacsConfiguration config = Tigl.openCPACSConfiguration(
				cpacsFilePath, // cpacsFileName, 
				"")) {

			// get splines from a profile NACA0012
			List<TiglBSpline> splines = config.getProfileSplines("NACA0012");
			System.out.println("--------------------------------");
			System.out.println("Number of profile splines NACA0012: " + splines.size());
			for (int ispl = 0; ispl < splines.size(); ++ispl) {
				TiglBSpline spl = splines.get(ispl);
				System.out.println("Spline degree, Ctrl pts, Knots");
				System.out.println(spl.degree + " " + spl.controlPoints.size() + " " + spl.knots.size());
			}

			System.out.println("--------------------------------");
			System.out.println("Fuselage count: " + config.getFuselageCount());

			System.out.println("--------------------------------");
			System.out.println("Wing UID 1");
			String wingUID = config.wingGetUID(1);

			System.out.println("Number of wing sections (UID 1): " + config.wingGetSectionCount(1));
			for (int i = 1; i <= config.wingGetSectionCount(1); ++i) {
				System.out.println(config.wingGetSectionUID(1, i));
			}

			System.out.println("--------------------------------");
			System.out.println("Wing 1, lower point: (1, 0.5, 0.5)");
			TiglPoint p = config.wingGetLowerPoint(1, 1, 0.5, 0.5);
			System.out.println(p);
			
			System.out.println("--------------------------------");
			System.out.println("Wing 1, wingGetSegmentEtaXsi");
			WSProjectionResult res = config.wingGetSegmentEtaXsi(1, p);
			System.out.println("\neta/xsi: " + res.point.eta + "," + res.point.xsi);
			System.out.println("\nOnTop: " + res.isOnTop);

			// System.out.println(config.wingComponentSegmentGetPoint("WING_CS1", 0.5, 0.5));

			System.out.println("--------------------------------");
			System.out.println("Wing 1, wingGetUpperPointAtDirection");
			Tigl.WGetPointDirectionResult result = 
					config.wingGetUpperPointAtDirection(1, 1, 0.5, 0.5, new TiglPoint(0, 0, 1));
			
			System.out.println("\nGetPointDirection point=" + result.point);
			System.out.println("\nGetPointDirection error=" + result.errorDistance);

			// do some exports
			
			boolean doExport = false;
			if (doExport) {
				System.out.println("--------------------------------");
				System.out.println("Exporting in CAD formats ... \n\n\n");
				
				String cpacsFileFolderPath = va.getInputFile().getParent();
				
				config.exportIGES(cpacsFileFolderPath + File.separator + "test.igs");
				config.exportSTEP(cpacsFileFolderPath + File.separator + "test.stp");
				config.exportWingCollada(config.wingGetUID(1), 
						cpacsFileFolderPath + File.separator + "wing.dae", 0.01);
				System.out.println("\n\n\n");
			}
			
			// example how to access low level tigl interface
			System.out.println("--------------------------------");
			System.out.println("example how to access low level tigl interface\n");

			IntByReference wingCount = new IntByReference();
			if (TiglNativeInterface.tiglGetWingCount(config.getCPACSHandle(), wingCount) == 0) {
				System.out.println("wingcount: " + wingCount.getValue());
			}

			// AGODEMAR: fuselage stuff
			IntByReference fuselageCount = new IntByReference();
			if (TiglNativeInterface.tiglGetFuselageCount(config.getCPACSHandle(), fuselageCount) == 0) {
				System.out.println("fuselageCount: " + fuselageCount.getValue());
				
				if (fuselageCount.getValue() > 0) {
					DoubleByReference fuselageSurfaceArea = new DoubleByReference();
					DoubleByReference fuselageVolume = new DoubleByReference();
					if (
							TiglNativeInterface
								.tiglFuselageGetSurfaceArea(
										config.getCPACSHandle(), 1, fuselageSurfaceArea
										) == 0
						) {
						System.out.println("fuselageSurfaceArea: " + fuselageSurfaceArea.getValue());
					}					
					if (
							TiglNativeInterface
								.tiglFuselageGetVolume(
										config.getCPACSHandle(), 1, fuselageVolume
										) == 0
						) {
						System.out.println("fuselageVolume: " + fuselageVolume.getValue());
					}					
				}
			}
			
			DoubleByReference mac   = new DoubleByReference();
			DoubleByReference mac_x = new DoubleByReference();
			DoubleByReference mac_y = new DoubleByReference();
			DoubleByReference mac_z = new DoubleByReference();
			if (TiglNativeInterface.tiglWingGetMAC(config.getCPACSHandle(), wingUID, mac, mac_x, mac_y, mac_z) == 0) {
				System.out.println("wing mac: "+ mac.getValue());
			}

			System.out.println("--------------------------------");
			System.out.println("Ref area: " + config.wingGetReferenceArea(1, TiglSymmetryAxis.TIGL_X_Y_PLANE));
		}
		catch(TiglException err) {
			System.out.println(err.getMessage());
			System.out.println(err.getErrorCode());
			return;
		}

		System.out.println("... end of TIGL test");
		
	}//end-of-main

}// end-of-class
