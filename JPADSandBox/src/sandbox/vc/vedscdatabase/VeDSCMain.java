package sandbox.vc.vedscdatabase;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import configuration.MyConfiguration;


public class VeDSCMain {

	protected Shell shell;
	private Text text;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("No input file name given. Terminating.");
			return;
		}

		System.out.println("currentDirectoryString --> " + MyConfiguration.currentDirectoryString);

		//
		// NO-GUI mode
		if (!args[0].equals("-g")) {

			String fileNameWithPathAndExt = args[0];
			String databaseName = "VeDSC_database.h5"; 

			File inputFile = new File(fileNameWithPathAndExt);

			if (!inputFile.exists()) {
				System.out.println("Input file " + fileNameWithPathAndExt + " not found! Terminating.");
				return;
			}
			if (inputFile.isDirectory()) {
				System.out.println("Input string " + fileNameWithPathAndExt + " is not a file. Terminating.");
				return;
			}

			System.out.println("Input file found. Running ...");
			String outputFileNameWithPathAndExt = fileNameWithPathAndExt.replace(
					Paths.get(fileNameWithPathAndExt).getFileName().toString(), "") 
					+ "VeDSCout.xml";

			VeDSCDatabaseCalc.executeStandalone(
					databaseName, 
					fileNameWithPathAndExt, 
					outputFileNameWithPathAndExt);

			//			VeDSCDatabaseCalc.writeDefaultFile("VeDSC_database.h5", MyConfiguration.inputDirectory + "VeDSC.xml");

			System.out.println("Done.");
		}

		//
		//		} else {

		//		// GUI mode
		//
		//			try {
		//				Main window = new Main();
		//				window.open();
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		////		}
		//	}
		//
		//
		//	/**
		//	 * Open the window.
		//	 */
		//	public void open() {
		//		Display display = Display.getDefault();
		//		createContents();
		//		shell.open();
		//		shell.layout();
		//		while (!shell.isDisposed()) {
		//			if (!display.readAndDispatch()) {
		//				display.sleep();
		//			}
		//		}
		//	}
		//
		//	/**
		//	 * Create contents of the window.
		//	 */
		//	protected void createContents() {
		//		shell = new Shell();
		//		shell.setSize(450, 300);
		//		shell.setText("SWT Application");
		//		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
	}

}


//System.out.println(get_KFv_vs_bv_over_dfv(5., 2., 0.5));
//System.out.println(get_KVf_vs_zw_over_dfv(5., 2., 0.5));
//System.out.println(get_KWv_vs_zw_over_rf(-1., 6., 0.5));
//System.out.println(get_KWf_vs_zw_over_rf(-1., 6., 0.5));
//System.out.println(get_KHv_vs_zh_over_bv1_high_wing(1., 1.5, 0.5));
//System.out.println(get_KHf_vs_zh_over_bv1_high_wing(1., 1.5, 0.5));

//DatabaseIOmanager<VeDSCDatabaseEnum> ioManager = new DatabaseIOmanager<VeDSCDatabaseEnum>();
//ioManager.addElement(VeDSCDatabaseEnum.Wing_Aspect_Ratio, Amount.valueOf(0., Unit.ONE), "Wing Aspect Ratio. Accepts values in [6,14] range.");
//ioManager.addElement(VeDSCDatabaseEnum.Wing_position, Amount.valueOf(0., Unit.ONE), "Between -1 and +1. Low wing = -1; high wing = 1");
//ioManager.addElement(VeDSCDatabaseEnum.Vertical_Tail_Aspect_Ratio, Amount.valueOf(0., Unit.ONE), "Vertical Tail Aspect Ratio. Accepts values in [1,2] range.");
//ioManager.addElement(VeDSCDatabaseEnum.Vertical_Tail_span, Amount.valueOf(0., SI.METER), "Vertical tail span");
//ioManager.addElement(VeDSCDatabaseEnum.Horizontal_position_over_vertical, Amount.valueOf(0., Unit.ONE), "Relative position of the horizontal tail over the vertical tail span, "
//		+ "computed from a reference line. Must be in [0,1] range"); //TODO
//ioManager.addElement(VeDSCDatabaseEnum.Diameter_at_vertical_MAC, Amount.valueOf(0., SI.METER), "Fuselage diameter at vertical MAC");
//ioManager.addElement(VeDSCDatabaseEnum.Tailcone_shape, Amount.valueOf(0., Unit.ONE), "Fuselage tailcone shape");

//DatabaseFileWriter<VeDSCDatabaseEnum> veDSCdatabaseWriter = new DatabaseFileWriter<VeDSCDatabaseEnum>(VeDSCdatabaseFileName, MyConfiguration.outputDirectory + "VeDSC.xml", ioManager);
//veDSCdatabaseWriter.writeDocument();

//DatabaseFileReader veDSCdatabaseReader = new DatabaseFileReader(MyConfiguration.outputDirectory + "VeDSC.xml", ioManager.getTagList());
//List<Amount> valueList = veDSCdatabaseReader.readDatabase();
//ioManager.setValueList(valueList);
//System.out.println(ioManager.getValue(VeDSCDatabaseEnum.Wing_Aspect_Ratio));
//System.out.println(ioManager.getValue(VeDSCDatabaseEnum.Vertical_Tail_span));

//double wingSpan = 0.;
//double sweepV_c2 = 25;              // Vertical tail sweep angle (deg)
//double cl_alpha_2D = 0.11;          // Vertical tail airfoil lift curve slope (deg^-1)
//double mach = 0.4;                  // Mach number
//
//double bv = 5.0;                    // Vertical tail span (m or ft)
//double dfv = 2.0;                   // Fuselage height in the region of vertical tail (m or ft)
//double tailconeShape = 0.5;         // Fuselage tailcone shape [0, 1]
//double wingPosition = 1;            // Wing vertical position on fuselage [-1, 1]
//double arWing = 12;                 // Wing aspect ratio [6, 14]
//double horizPosOverVertical = 1.0;  // Horizontal tail position on the vertical tail [0, 1], 0 is body-mounted
//double arVertical = 1.5;            // Vertical tail aspect ratio [1, 2]
//double sVertical = Math.pow(bv, 2)/arVertical;              // Vertical tail area (m^2 or ft^2)
//double sWing = Math.pow(wingSpan, 2)/arWing;                // Wing area (m^2 or ft^2)
//
//double cL_alpha_v = LiftCalc.calculateCLalpha_v(arVertical, cl_alpha_2D, sweepV_c2, mach);
//System.out.println("CN_beta_v " + MomentCalc.calcCNbetaVerticalTail(cL_alpha_v, kFv, kWv, kHv, sVertical, sWing));
// 
//double kFv = veDSCDatabaseReader.get_KFv_vs_bv_over_dfv(bv, dfv, tailconeShape);
//double kVf = veDSCDatabaseReader.get_KVf_vs_zw_over_dfv(bv, dfv, tailconeShape);
//double kWv = veDSCDatabaseReader.get_KWv_vs_zw_over_rf(wingPosition, arWing, tailconeShape);
//double kWf = veDSCDatabaseReader.get_KWf_vs_zw_over_rf(wingPosition, arWing, tailconeShape);
//double kHv = veDSCDatabaseReader.get_KHv_vs_zh_over_bv1(horizPosOverVertical, arVertical, tailconeShape, wingPosition);
//double kHf = veDSCDatabaseReader.get_KHf_vs_zh_over_bv1(horizPosOverVertical, arVertical, tailconeShape, wingPosition);
// 
//System.out.println("KFv = " + kFv);
//System.out.println("KVf = " + kVf);
//System.out.println("KWv = " + kWv);
//System.out.println("KWf = " + kWf);
//System.out.println("KHv = " + kHv);
//System.out.println("KHf = " + kHf);