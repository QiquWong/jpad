package sandbox2.gt;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

import aircraft.components.liftingSurface.creator.SpoilerCreator;
import configuration.MyConfiguration;
import de.dlr.sc.tigl.CpacsConfiguration;
import de.dlr.sc.tigl.Tigl;
import de.dlr.sc.tigl.Tigl.WSProjectionResult;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import de.dlr.sc.tigl.TiglBSpline;
import de.dlr.sc.tigl.TiglException;
import de.dlr.sc.tigl.TiglNativeInterface;
import de.dlr.sc.tigl.TiglPoint;
import de.dlr.sc.tigl.TiglSymmetryAxis;
import de.dlr.sc.tigl.TixiNativeInterface;

class ArgumentsCPACSReaderTest1 {
	@Option(name = "-f", aliases = { "--file" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

}

public class CPACSReaderTest1 {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	public static void main(String[] args) {
		System.out.println("TIGL test ...");

		String cpacsFileFolderName = "CPACS";
		String cpacsFileFolderPath = 
				MyConfiguration.currentDirectoryString + File.separator
				+ cpacsFileFolderName;		

		//		URL cpacsFolderURL = MyTest_Tigl_00.class.getResource("/sandbox/adm/" + cpacsFileFolderName);
		//		System.out.println("URL: " + cpacsFolderURL.getPath());
		//		String cpacsFileName = 
		//				cpacsFolderURL.getPath() + File.separator + "D150_AGILE.xml";
		//				// "C:/Users/agodemar/JPAD/jpad/JPADSandBox/bin/it/unina/sandbox/test/adm/cpacsfiles/D150_AGILE.xml"; 

		//		String cpacsFilePath = cpacsFileFolderPath + File.separator + "D150_AGILE.xml";
		//		System.out.println("--------------------------------");
		//		System.out.println("FILE: " + cpacsFilePath);


		ArgumentsCPACSReaderTest1 va = new ArgumentsCPACSReaderTest1();
		CPACSReaderTest1.theCmdLineParser = new CmdLineParser(va);

		try {
			CPACSReaderTest1.theCmdLineParser.parseArgument(args);

			String cpacsFilePath = va.getInputFile().getAbsolutePath();

			System.out.println("--------------------------------");
			System.out.println("TiGL Version: " + Tigl.getVersion());
			try (CpacsConfiguration tigl_s = Tigl.openCPACSConfiguration(
					cpacsFilePath, // cpacsFileName, 
					"")) {
				
				// TixiNativeInterface
				
				// Use a JPAD internal reader to get all non-tigl standard items 
				JPADXmlReader jpadXmlReader = new JPADXmlReader(cpacsFilePath);
				
				//------------------------------------------------------------------------------
				// example: getting a list of nodes
				NodeList wingsNodes = MyXMLReaderUtils.getXMLNodeListByPath(
						jpadXmlReader.getXmlDoc(), 
						"//vehicles/aircraft/model/wings/wing");
				System.out.println("[JPAD] Number of wings: " + wingsNodes.getLength());
				for (int i = 0; i < wingsNodes.getLength(); i++) {
					Node nodeWing  = wingsNodes.item(i); // .getNodeValue();
					Element elementWing = (Element) nodeWing;
		            System.out.println("wing[" + i + "] --> uid: " + elementWing.getAttribute("uID"));
				}
				//------------------------------------------------------------------------------
				
				
				String flagwing = jpadXmlReader.getXMLPropertyByPath(
						"/cpacs/toolspecific/UNINA_modules/input/wings/MainWing/mainWingUID");
				int position = 3;//integer position of the select airfoil 
				// String flagwing = "D150_VAMP_W1";
				List<Double> airfoil = new ArrayList<>();
//				MyArrayUtils
//				convertListOfDoubleToDoubleArray
//				readArrayDoubleFromXML
				airfoil = jpadXmlReader.readArrayDoubleFromXML(
						"cpacs/vehicles/profiles/wingAirfoils/wingAirfoil["+position+"]/pointList/x");//this read airfoil in position specified in position variables
				
				double wingspan = tigl_s.wingGetSpan(flagwing);
				double flagwingPosition = tigl_s.wingGetIndex(flagwing);
				System.out.println("wingspan: " + wingspan);
				System.out.println("flagwingPosition: " + flagwingPosition);
				System.out.println("Airfoil height: "+ airfoil);
				//				String wingUID = tigl_s.wingGetW
				//				i=0
				//				wingUid  =[0]*nWing
				//				wIndex   =[0]*nWing
				//				wSegCount=[0]*nWing
				//				wSurfArea=[0]*nWing
				//				wWettedArea=[0]*nWing
				//				wRefArea =[0]*nWing
				//				wSecCount=[0]*nWing
				//				wSymmetry=[0]*nWing 
				//				wSpan    =[0]*nWing
				//				wMAC     =[[0]]*nWing
				//
				//				wIndexPosition=[[0]]*nWing
				//
				//				wingSectionIndex_inner=[[0]]*nWing
				//
				//
				//						## Load the value
				//				for i in range(0,nWing):
				//				    wingUid[i]  =tigl_s.wingGetUID(i+1)
				//					wIndex[i]   =tigl_s.wingGetIndex(wingUid[i])
				//					wSegCount[i]=tigl_s.wingGetSegmentCount(wIndex[i])
				//					wSurfArea[i]=tigl_s.wingGetSurfaceArea(wIndex[i])#wetted surface of the isolate wing
				//					wRefArea[i] =tigl_s.wingGetReferenceArea(wIndex[i],wSymmetry[i])#half surface
				//					wSecCount[i]=tigl_s.wingGetSectionCount(wIndex[i])
				//					wSymmetry[i]=tigl_s.wingGetSymmetry(wIndex[i])#number of symmetry-axes
				//					wSpan[i]    =tigl_s.wingGetSpan(wingUid[i])
				//					wMAC[i]     =tigl_s.wingGetMAC(wingUid[i])    

				//				mainWingUID=tixi_s.getTextElement('/cpacs/toolspecific/UNINA_modules/input/wings/MainWing/mainWingUID')
				//				for i in range(0,len(wingUid)):
				//					if mainWingUID==wingUid[i]:
				//						   flagUIDWing = i
				//						   print ("UID mainW found")
				//					else : 
				//						 print ("UID mainW don't found wrong name")
				//				 HorizontalTailUID=tixi_s.getTextElement('/cpacs/toolspecific/UNINA_modules/input/wings/HorizTail/HorizTailUID')
				//				 for i in range(0,len(wingUid)):
				//				    if HorizontalTailUID==wingUid[i]:
				//					  print ("UID HT found ")
				//					  flagUIDHT = i
				//				    else : 
				//					  print ("UID HT don't found wrong name")
				//						          
				//						## Check UID VT
				//				VerticalTailUID=tixi_s.getTextElement('/cpacs/toolspecific/UNINA_modules/input/wings/VerticalTail/VerticalTailUID')
				//				for i in range(0,len(wingUid)):
				//					if VerticalTailUID==wingUid[i]:
				//					    print ("UID VT found")
				//						flagUIDVT = i        
				//					else : 
				//					    print ("UID VT don't found wrong name")
				//
				//
				//
				//				#Wing Dimension
				//				WingArea = wRefArea[flagUIDWing]
				//				WingSpan = wSpan[flagUIDWing]
				//				MeanAerodynamicChord_Wing=wMAC[flagUIDWing][0]
				//				RootChord = tixi_s.getDoubleElement('cpacs/vehicles/aircraft/model/wings/wing[%a]/sections/section[%s]/elements/element/transformation/scaling/x'%((flagUIDWing+1), 1))
				//				#HT
				//				HorizontalTailArea = wRefArea[flagUIDHT]
				//				HorizontalTailARM = tixi_s.getDoubleElement('cpacs/toolspecific/WingAirfoilUnina/htailarm')
				//				#VT
				//				VerticalTailArea = wRefArea[flagUIDHT]
				//				VerticalTailARM = tixi_s.getDoubleElement('cpacs/toolspecific/WingAirfoilUnina/vtailarm')
				//

				// get splines from a profile NACA0012
				List<TiglBSpline> splines = tigl_s.getProfileSplines("NACA0012");
				System.out.println("--------------------------------");
				System.out.println("Number of profile splines NACA0012: " + splines.size());
				for (int ispl = 0; ispl < splines.size(); ++ispl) {
					TiglBSpline spl = splines.get(ispl);
					System.out.println("Spline degree, Ctrl pts, Knots");
					System.out.println(spl.degree + " " + spl.controlPoints.size() + " " + spl.knots.size());
				}

				System.out.println("--------------------------------");
				System.out.println("Fuselage count: " + tigl_s.getFuselageCount());

				System.out.println("--------------------------------");
				System.out.println("Wing UID 1");
				String wingUID = tigl_s.wingGetUID(1);

				System.out.println("Number of wing sections (UID 1): " + tigl_s.wingGetSectionCount(1));
				for (int i = 1; i <= tigl_s.wingGetSectionCount(1); ++i) {
					System.out.println(tigl_s.wingGetSectionUID(1, i));
				}

				System.out.println("--------------------------------");
				System.out.println("Wing 1, lower point: (1, 0.5, 0.5)");
				TiglPoint p = tigl_s.wingGetLowerPoint(1, 1, 0.5, 0.5);
				System.out.println(p);

				System.out.println("--------------------------------");
				System.out.println("Wing 1, wingGetSegmentEtaXsi");
				WSProjectionResult res = tigl_s.wingGetSegmentEtaXsi(1, p);
				System.out.println("\neta/xsi: " + res.point.eta + "," + res.point.xsi);
				System.out.println("\nOnTop: " + res.isOnTop);

				// System.out.println(config.wingComponentSegmentGetPoint("WING_CS1", 0.5, 0.5));

				System.out.println("--------------------------------");
				System.out.println("Wing 1, wingGetUpperPointAtDirection");
				Tigl.WGetPointDirectionResult result = 
						tigl_s.wingGetUpperPointAtDirection(1, 1, 0.5, 0.5, new TiglPoint(0, 0, 1));

				System.out.println("\nGetPointDirection point=" + result.point);
				System.out.println("\nGetPointDirection error=" + result.errorDistance);

				// do some exports

				boolean doExport = false;
				if (doExport) {
					System.out.println("--------------------------------");
					System.out.println("Exporting in CAD formats ... \n\n\n");
					tigl_s.exportIGES(cpacsFileFolderPath + File.separator + "test.igs");
					tigl_s.exportSTEP(cpacsFileFolderPath + File.separator + "test.stp");
					tigl_s.exportWingCollada(tigl_s.wingGetUID(1), 
							cpacsFileFolderPath + File.separator + "wing.dae", 0.01);
					System.out.println("\n\n\n");
				}

				// example how to access low level tigl interface
				System.out.println("--------------------------------");
				System.out.println("example how to access low level tigl interface\n");

				IntByReference wingCount = new IntByReference();
				if (TiglNativeInterface.tiglGetWingCount(tigl_s.getCPACSHandle(), wingCount) == 0) {
					System.out.println("wingcount: " + wingCount.getValue());
				}

				// AGODEMAR: fuselage stuff
				IntByReference fuselageCount = new IntByReference();
				if (TiglNativeInterface.tiglGetFuselageCount(tigl_s.getCPACSHandle(), fuselageCount) == 0) {
					System.out.println("fuselageCount: " + fuselageCount.getValue());

					if (fuselageCount.getValue() > 0) {
						DoubleByReference fuselageSurfaceArea = new DoubleByReference();
						DoubleByReference fuselageVolume = new DoubleByReference();
						if (
								TiglNativeInterface
								.tiglFuselageGetSurfaceArea(
										tigl_s.getCPACSHandle(), 1, fuselageSurfaceArea
										) == 0
								) {
							System.out.println("fuselageSurfaceArea: " + fuselageSurfaceArea.getValue());
						}					
						if (
								TiglNativeInterface
								.tiglFuselageGetVolume(
										tigl_s.getCPACSHandle(), 1, fuselageVolume
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
				if (TiglNativeInterface.tiglWingGetMAC(tigl_s.getCPACSHandle(), wingUID, mac, mac_x, mac_y, mac_z) == 0) {
					System.out.println("wing mac: "+ mac.getValue());
				}

				System.out.println("--------------------------------");
				System.out.println("Ref area: " + tigl_s.wingGetReferenceArea(1, TiglSymmetryAxis.TIGL_X_Y_PLANE));
			}
			catch(TiglException err) {
				System.out.println(err.getMessage());
				System.out.println(err.getErrorCode());
				return;
			}

			System.out.println("... end of TIGL test");

		} catch (CmdLineException e) {
			System.err.println("A problem occurred with command line arguments!");
			e.printStackTrace();
		}

	}//end-of-main

}// end-of-class
