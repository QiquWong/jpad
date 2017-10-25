package sandbox2.adm.cpacs;

import java.io.File;
import java.util.List;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import configuration.MyConfiguration;
import de.dlr.sc.tigl.CpacsConfiguration;
import de.dlr.sc.tigl.Tigl;
import de.dlr.sc.tigl.TiglBSpline;
import de.dlr.sc.tigl.TiglException;
import de.dlr.sc.tigl.TiglNativeInterface;

public class MyTest_Tigl_01 {

	public static void main(String[] args) {
		System.out.println("TIGL test - Fuselage manipulation ...");


		// Try with Tixi library
		//		sandbox.adm.TIXIInterface tixiInterface = 
		//				new sandbox.adm.TIXIInterface();


		String cpacsFileFolderName = "CPACS";
		String cpacsFileFolderPath = 
				MyConfiguration.currentDirectoryString + File.separator
				+ cpacsFileFolderName;		

		//	URL cpacsFolderURL = MyTest_Tigl_00.class.getResource("/sandbox/adm/" + cpacsFileFolderName);
		//	System.out.println("URL: " + cpacsFolderURL.getPath());
		//	String cpacsFileName = 
		//			cpacsFolderURL.getPath() + File.separator + "D150_AGILE.xml";
		//			// "C:/Users/agodemar/JPAD/jpad/JPADSandBox/bin/it/unina/sandbox/test/adm/cpacsfiles/D150_AGILE.xml"; 

		String cpacsFilePath = cpacsFileFolderPath + File.separator + "D150_AGILE.xml";
		System.out.println("--------------------------------");
		System.out.println("FILE: " + cpacsFilePath);

		System.out.println("--------------------------------");
		//System.out.println("TiGL Version: " + Tigl.getVersion());

		try (CpacsConfiguration config = Tigl.openCPACSConfiguration(
				cpacsFilePath, // cpacsFileName, 
				"")) {

			//			// get splines from a profile NACA0012
			//			List<TiglBSpline> splines = config.getProfileSplines("NACA0012");
			//			System.out.println("--------------------------------");
			//			System.out.println("Number of profile splines NACA0012: " + splines.size());
			//			for (int ispl = 0; ispl < splines.size(); ++ispl) {
			//				TiglBSpline spl = splines.get(ispl);
			//				System.out.println("Spline degree, Ctrl pts, Knots");
			//				System.out.println(spl.degree + " " + spl.controlPoints.size() + " " + spl.knots.size());
			//			}
			//			System.out.println("--------------------------------");

			// System.out.println("Fuselage count: " + config.getFuselageCount());

			IntByReference fuselageCount = new IntByReference();
			if (TiglNativeInterface.tiglGetFuselageCount(config.getCPACSHandle(), fuselageCount) == 0) {
				System.out.println("fuselageCount: " + fuselageCount.getValue());

				if (fuselageCount.getValue() > 0) {

					// List<TiglBSpline> splines = config.getProfileSplines("NACA0012");

					IntByReference fuselageSectionCount = new IntByReference();
					if (TiglNativeInterface
							.tiglFuselageGetSectionCount(
									config.getCPACSHandle(), 
									1,
									fuselageSectionCount) == 0) {
						if (fuselageSectionCount.getValue() > 0) {
							// List<TiglBSpline> splines = config.getProfileSplines("NACA0012");
							// Loop over sections
							for (int kSection = 0; kSection < fuselageSectionCount.getValue(); kSection++) {
								System.out.println("Fuselage section: " + (kSection+1));
								String fuselageSectionUID = config.fuselageGetSectionUID(1, kSection+1);
								System.out.println("\tSection UID: " + fuselageSectionUID);

								//								PointerByReference profileNamePtr = new PointerByReference();
								//								String profileName;
								//								if (TiglNativeInterface.tiglFuselageGetProfileName(
								//										config.getCPACSHandle(), 1, kSection, 
								//										1, // The index of an element on the section 
								//										profileNamePtr) == 0) {
								//									profileName = profileNamePtr.getValue().getString(0);
								//									System.out.println("\tProfile name (element 1): " + profileName);
								//								}

								//								tixiInterface .tixiCheckElement();



								//								IntByReference curveCount = new IntByReference(0);
								//								if (TiglNativeInterface.tiglProfileGetBSplineCount(
								//										config.getCPACSHandle(), 
								//										fuselageSectionUID, 
								//										curveCount) == 0) {
								//									System.out.println("\tProfile count: " + curveCount);
								//								}

								//								List<TiglBSpline> splines = config.getProfileSplines(fuselageSectionUID);
								//								System.out.println("\tspline count: " + splines.size());

							}
						}
					}


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
		}
		catch(TiglException err) {
			System.out.println(err.getMessage());
			System.out.println(err.getErrorCode());
			return;
		}

		System.out.println("... end of TIGL test");

	}//end-of-main

}// end-of-class
