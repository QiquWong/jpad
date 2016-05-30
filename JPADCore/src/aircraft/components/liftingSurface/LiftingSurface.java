package aircraft.components.liftingSurface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.componentmodel.AeroComponent;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.enumerations.ComponentEnum;

public class LiftingSurface extends AeroComponent implements ILiftingSurface{

	private String _id = null;
	private ComponentEnum _type;
	private Amount<Length> _xApexConstructionAxes = null; 
	private Amount<Length> _yApexConstructionAxes = null; 
	private Amount<Length> _zApexConstructionAxes = null;

	private LiftingSurfaceCreator _liftingSurfaceCreator;

	//	private Amount<Area> _surface = null;
	//	private Double _aspectRatio = null; 
	//	private Double _taperRatioEquivalent = null;
	//	private Double _taperRatioActual = null; 
	//	private Double _taperRatioOpt = null; // (oswald factor)
	//	private Amount<Angle> _sweepQuarterChordEq = null; 
	//	private Amount<Angle> _sweepHalfChordEq = null; 
	//	private Amount<Angle> _dihedralMean = null;

	//	private double deltaFactorDrag;

	//================================================
	// Builder pattern via a nested public static class
	public static class LiftingSurfaceBuilder {

		private String __id = null;
		private ComponentEnum __type;
		private Amount<Length> __xApexConstructionAxes = null; 
		private Amount<Length> __yApexConstructionAxes = null; 
		private Amount<Length> __zApexConstructionAxes = null;
		private LiftingSurfaceCreator __liftingSurfaceCreator;

		public LiftingSurfaceBuilder(String id, ComponentEnum type) {
			// required parameter
			this.__id = id;
			this.__type = type;

			// optional parameters ...

		}

		public LiftingSurfaceBuilder liftingSurfaceCreator(LiftingSurfaceCreator lsc) {
			this.__liftingSurfaceCreator = lsc;
			return this;
		}
		
		public LiftingSurface build() {
			return new LiftingSurface(this);
		}

	}

	private LiftingSurface(LiftingSurfaceBuilder builder) {
		super(builder.__id, builder.__type);
		this._id = builder.__id; 
		this._type = builder.__type;
		this._xApexConstructionAxes = builder.__xApexConstructionAxes; 
		this._yApexConstructionAxes = builder.__yApexConstructionAxes; 
		this._zApexConstructionAxes = builder.__zApexConstructionAxes;
		this._liftingSurfaceCreator = builder.__liftingSurfaceCreator;
	}

	@Override
	public Amount<Area> getSurface() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getAspectRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Amount<Length> getSpan() {
		return _liftingSurfaceCreator.getSpan();
	}

	@Override
	public Amount<Length> getSemiSpan() {
		return _liftingSurfaceCreator.getSemiSpan();
	}

	@Override
	public double getTaperRatio() {
		return _liftingSurfaceCreator.getTaperRatio();
	}

	@Override
	public double getTaperRatioEquivalent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LiftingSurface getEquivalentWing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getChordRootEquivalent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getChordRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Length> getChordTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Angle> getSweepLEEquivalent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Angle> getSweepHalfChordEquivalent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Angle> getSweepQuarterChordEquivalent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDihedralEquivalent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LiftingSurfaceCreator getLiftingSurfaceCreator() {
		return _liftingSurfaceCreator;
	}

	@Override
	public void calculateGeometry() {
		_liftingSurfaceCreator.calculateGeometry();
	}

	@Override
	public void calculateGeometry(int nSections) {
		_liftingSurfaceCreator.calculateGeometry(nSections);
	}


	//==============================================================
	// MAIN: 
	//==============================================================
	public static void main(String[] args) {

		// TODO: check out this as an alternative
		// https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/

		System.out.println("--------------");
		System.out.println("Generic Lifting Surface Test");
		System.out.println("--------------");

		class MyArgument {
			@Option(name = "-i", aliases = { "--input" }, required = true,
					usage = "my input file")
			private File _inputFile;

			@Option(name = "-da", aliases = { "--dir-airfoils" }, required = true,
					usage = "airfoil directory path")
			private File _airfoilDirectory;

			// receives other command line parameters than options
			@Argument
			public List<String> arguments = new ArrayList<String>();

			public File getInputFile() {
				return _inputFile;
			}

			public File getAirfoilDirectory() {
				return _airfoilDirectory;
			}

		}

		MyArgument va = new MyArgument();
		CmdLineParser theCmdLineParser = new CmdLineParser(va);

		// populate the wing static object in the class
		// before launching the JavaFX application thread (launch --> start ...)
		try {
			theCmdLineParser.parseArgument(args);
			String pathToXML = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToXML);

			String dirAirfoil = va.getAirfoilDirectory().getCanonicalPath();
			System.out.println("AIRFOILS ===> " + dirAirfoil);

			System.out.println("--------------");

			// This wing static object is available in the scope of
			// the Application.start method

			// define LiftingSurface ...
			LiftingSurface theWing = new LiftingSurfaceBuilder("MyWing", ComponentEnum.WING)
					.liftingSurfaceCreator(LiftingSurfaceCreator.importFromXML(pathToXML, dirAirfoil))
					.build();
			
			//====================================================================================
			// THIS SEQUENCE READS A WING AND CREATES THE RELATED OBJECT : 
//			theWing = LiftingSurfaceCreator.importFromXML(pathToXML, dirAirfoil);

			//====================================================================================
			// THIS SEQUENCE CREATES A WING OBJECT WITH DEFAULT DATA :
//			theWing = new LiftingSurfaceCreator
//					.LiftingSurfaceCreatorBuilder("Test ATR72 wing", AircraftEnum.ATR72)
//					.build();
			//====================================================================================

			theWing.calculateGeometry(40);

			System.out.println("The wing ...");
			System.out.println(theWing);
			System.out.println("Details on panel discretization ...");
			theWing
				.getLiftingSurfaceCreator()
				.reportPanelsToSpanwiseDiscretizedVariables();

		} catch (CmdLineException | IOException e) {
			System.err.println("Error: " + e.getMessage());
			theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    
	}

	public String get_id() {
		return _id;
	}

	public ComponentEnum get_type() {
		return _type;
	}

	public Amount<Length> get_xApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public Amount<Length> get_yApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public Amount<Length> get_zApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public LiftingSurfaceCreator get_liftingSurfaceCreator() {
		return _liftingSurfaceCreator;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public void set_type(ComponentEnum _type) {
		this._type = _type;
	}

	public void set_xApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public void set_yApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public void set_zApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public void set_liftingSurfaceCreator(LiftingSurfaceCreator _liftingSurfaceCreator) {
		this._liftingSurfaceCreator = _liftingSurfaceCreator;
	}
}

