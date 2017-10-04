package sandbox2.adm.cpacs;

import java.io.File;
import java.util.List;

import com.sun.jna.ptr.IntByReference;

import configuration.MyConfiguration;
import de.dlr.sc.tigl.TiglBSpline;
import de.dlr.sc.tigl.TiglException;
import de.dlr.sc.tigl.TiglNativeInterface;
import standaloneutils.CPACSReader;

public class MyTest_Tigl_02 {

	public static void main(String[] args) {

		System.out.println("TIGL, CPACSReader test ...");

		String cpacsFileFolderName = "CPACS";
		String cpacsFileFolderPath = 
				MyConfiguration.currentDirectoryString + File.separator
				+ cpacsFileFolderName;		
		String cpacsFilePath = cpacsFileFolderPath + File.separator + "D150_AGILE.xml";

		System.out.println("--------------------------------");
		System.out.println("FILE: " + cpacsFilePath);

		try {
			CPACSReader cpacsReader = new CPACSReader(cpacsFilePath);

			System.out.println("--------------------------------");
			System.out.println("Fuselage count: " + cpacsReader.getFuselageCount());
			System.out.println("Fuselage(1) ID: " + cpacsReader.getFuselageID(1));
			System.out.println("Fuselage(1) surface area (m^2): " + cpacsReader.getFuselageSurfaceArea(1));
			System.out.println("Fuselage(1) volume (m^3): " + cpacsReader.getFuselageVolume(1));
			System.out.println("Fuselage(1) length (m): " + cpacsReader.getFuselageLength(1));

			System.out.println("--------------------------------");
			System.out.println("Wing count: " + cpacsReader.getWingCount());

			
			System.out.println("--------------------------------");
			System.out.println("Profile test");
			String profileID = "D150_VAMP_W_SupCritProf1"; // "NACA0012"; //"D150_VAMP_FL1_ProfSupEl1"; // "D150_VAMP_FL1_ProfCirc";
			System.out.println("Profile ID: " + profileID);
			
			// see:
			// http://www.cs.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/B-spline/bspline-curve.html
			// for BSplines
			
			IntByReference bsplineCount = new IntByReference(0);
			TiglNativeInterface
				.tiglProfileGetBSplineCount(
						cpacsReader.getConfig().getCPACSHandle(), profileID, bsplineCount);
			System.out.println("\tBSpline count: " + bsplineCount.getValue());
			
			List<TiglBSpline> splines = cpacsReader.getConfig().getProfileSplines(profileID);
			for (int ispl = 0; ispl < splines.size(); ++ispl) {
				TiglBSpline spl = splines.get(ispl);
				System.out.println("\tDegree: " + spl.degree + ", Control points: " + spl.controlPoints.size() + ", Knots: " + spl.knots.size());
				System.out.println("\tControl point data:");
				System.out.println(spl.controlPoints);
				System.out.println("\tKnots data:");
				System.out.println(spl.knots);
			}
			
			cpacsReader.closeConfig();

			System.out.println("--------------------------------");
		} catch (TiglException e) {
			e.printStackTrace();
		}

	}

}
