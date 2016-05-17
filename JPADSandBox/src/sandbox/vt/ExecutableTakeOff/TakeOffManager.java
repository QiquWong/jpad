package sandbox.vt.ExecutableTakeOff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class TakeOffManager {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION

	static InputTree input;
	static OutputTree output;

	//-------------------------------------------------------------------------------------
	// METHODS:

	/**************************************************************************************
	 * This method is in charge of reading data from a given XML input file and 
	 * put them inside an object of the InputTree class.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param pathToXML
	 * @throws ParserConfigurationException
	 */
	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		input = new InputTree();

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");
		
		//---------------------------------------------------------------------------------
		// CHARTS AND ENGINE MODEL FLAGS:
		//---------------------------------------------------------------------------------
		NodeList nodelistRoot = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//take_off_executable");
		Node nodeRoot  = nodelistRoot.item(0); 
		Element elementRoot = (Element) nodeRoot;

		// charts:		
		if(elementRoot.getAttribute("charts").equalsIgnoreCase("TRUE"))
			input.setCharts(true);
		else
			input.setCharts(false);
		
		System.out.println("\tCHARTS CREATION : " + input.isCharts());
		
		// engine model:
		if(elementRoot.getAttribute("simplified_thrust_model").equalsIgnoreCase("TRUE"))
			input.setEngineModel(true);
		else
			input.setEngineModel(false);

		System.out.println("\tSIMPLIFIED ENGINE MODEL : " + input.isEngineModel() + "\n");

		//---------------------------------------------------------------------------------
		// GROUND CONDITION:
		//---------------------------------------------------------------------------------
		List<String> alphaGroundProperty = reader.getXMLPropertiesByPath("//ground_conditions/alpha");
		input.setAlphaGround(Amount.valueOf(Double.valueOf(alphaGroundProperty.get(0)), NonSI.DEGREE_ANGLE));

		List<String> vWindProperty = reader.getXMLPropertiesByPath("//ground_conditions/wind_speed");
		input.setvWind(Amount.valueOf(Double.valueOf(vWindProperty.get(0)), SI.METERS_PER_SECOND));

		List<String> altitudeProperty = reader.getXMLPropertiesByPath("//ground_conditions/altitude");
		input.setAltitude(Amount.valueOf(Double.valueOf(altitudeProperty.get(0)), SI.METER));

		//---------------------------------------------------------------------------------
		// WEIGHT:	
		//---------------------------------------------------------------------------------------
		List<String> takeOffMassProperty = reader.getXMLPropertiesByPath("//aircraft_data/take_off_mass");
		input.setTakeOffMass(Amount.valueOf(Double.valueOf(takeOffMassProperty.get(0)), SI.KILOGRAM));

		//---------------------------------------------------------------------------------------
		// WING:	
		//---------------------------------------------------------------------------------------
		// Geometry:
		List<String> aspectRatioProperty = reader.getXMLPropertiesByPath("//geometry/aspect_ratio");
		input.setAspectRatio(Double.valueOf(aspectRatioProperty.get(0)));

		List<String> spanProperty = reader.getXMLPropertiesByPath("//geometry/span");
		input.setWingSpan(Amount.valueOf(Double.valueOf(spanProperty.get(0)), SI.METER));

		List<String> surfaceProperty = reader.getXMLPropertiesByPath("//geometry/surface");
		input.setWingSurface(Amount.valueOf(Double.valueOf(surfaceProperty.get(0)), SI.SQUARE_METRE));

		List<String> wingDistanceFromGroundProperty = reader.getXMLPropertiesByPath("//geometry/distance_from_ground");
		input.setWingToGroundDistance(Amount.valueOf(Double.valueOf(wingDistanceFromGroundProperty.get(0)), SI.METER));

		List<String> iWProperty = reader.getXMLPropertiesByPath("//geometry/angle_of_incidence");
		input.setIw(Amount.valueOf(Double.valueOf(iWProperty.get(0)), NonSI.DEGREE_ANGLE));

		//---------------------------------------------------------------------------------------
		// Aerodynamic data:
		List<String> oswaldProperty = reader.getXMLPropertiesByPath("//aerodynamic_data/oswald");
		input.setOswald(Double.valueOf(oswaldProperty.get(0)));

		List<String> cD0CleanProperty = reader.getXMLPropertiesByPath("//aerodynamic_data/cD0_clean");
		input.setcD0Clean(Double.valueOf(cD0CleanProperty.get(0)));

		List<String> deltaCD0FlapProperty = reader.getXMLPropertiesByPath("//aerodynamic_data/delta_cD0_flap");
		input.setDeltaCD0Flap(Double.valueOf(deltaCD0FlapProperty.get(0)));

		List<String> deltaCD0LandingGearProperty = reader.getXMLPropertiesByPath("//aerodynamic_data/delta_cD0_landing_gears");
		input.setDeltaCD0LandingGear(Double.valueOf(deltaCD0LandingGearProperty.get(0)));

		List<String> cLmaxTOProperty = reader.getXMLPropertiesByPath("//aerodynamic_data/cL_max_take_off");
		input.setcLmaxTO(Double.valueOf(cLmaxTOProperty.get(0)));

		List<String> cL0TOProperty = reader.getXMLPropertiesByPath("//aerodynamic_data/cL0_take_off");
		input.setcL0TO(Double.valueOf(cL0TOProperty.get(0)));

		List<String> cLalphaTOProperty = reader.getXMLPropertiesByPath("//aerodynamic_data/cL_alpha_take_off");
		input.setcLalphaFlap(Amount.valueOf(Double.valueOf(cLalphaTOProperty.get(0)), NonSI.DEGREE_ANGLE.inverse()));

		//---------------------------------------------------------------------------------------
		// ENGINE:	
		//---------------------------------------------------------------------------------------
		List<String> t0Property = reader.getXMLPropertiesByPath("//engine/static_thrust");
		input.setT0(Amount.valueOf(Double.valueOf(t0Property.get(0)), SI.NEWTON));

		List<String> nEngineProperty = reader.getXMLPropertiesByPath("//engine/number_of_engines");
		input.setnEngine(Integer.valueOf(nEngineProperty.get(0)));
		
		List<String> machArrayProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//engine/mach_array").get(0));
		input.setMachArray(new double[machArrayProperty.size()]);
		for(int i=0; i<machArrayProperty.size(); i++)
			input.getMachArray()[i] = Double.valueOf(machArrayProperty.get(i));
		
		List<String> netThrustProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//engine/net_thrust_array_single_engine").get(0));
		input.setNetThrust(new double[netThrustProperty.size()]);
		for(int i=0; i<netThrustProperty.size(); i++)
			input.getNetThrust()[i] = Double.valueOf(netThrustProperty.get(i));
			
		//---------------------------------------------------------------------------------------
		// Print data:
		System.out.println("\tAlpha body at ground = " + input.getAlphaGround().getEstimatedValue() + " " + input.getAlphaGround().getUnit());
		System.out.println("\tWind speed = " + input.getvWind().getEstimatedValue() + " " + input.getvWind().getUnit());
		System.out.println("\tField altitude = " + input.getAltitude().getEstimatedValue() + " " + input.getAltitude().getUnit() + "\n");
		System.out.println("\tTake-off mass = " + input.getTakeOffMass().getEstimatedValue() + " " + input.getTakeOffMass().getUnit() + "\n");
		System.out.println("\tAspect Ratio = " + input.getAspectRatio());
		System.out.println("\tSpan = " + input.getWingSpan().getEstimatedValue() + " " + input.getWingSpan().getUnit());
		System.out.println("\tSurface = " + input.getWingSurface().getEstimatedValue() + " " + input.getWingSurface().getUnit());
		System.out.println("\tWing distance from ground = " + input.getWingToGroundDistance() + " " + input.getWingToGroundDistance().getUnit());
		System.out.println("\tWing angle of incidence (iw) = " + input.getIw().getEstimatedValue() + " " + input.getIw().getUnit() + "\n");
		System.out.println("\tOswald = " + input.getOswald());
		System.out.println("\tCD0 clean = " + input.getcD0Clean());
		System.out.println("\tDelta CD0 flap = " + input.getDeltaCD0Flap());
		System.out.println("\tDelta CD0 landing gears = " + input.getDeltaCD0LandingGear());
		System.out.println("\tCLmax take-off = " + input.getcLmaxTO());
		System.out.println("\tCL0 take-off = " + input.getcL0TO());
		System.out.println("\tCLalpha take-off = " + input.getcLalphaFlap().getEstimatedValue() + " " + input.getcLalphaFlap().getUnit() + "\n");
		System.out.println("\tStatic thrust = " + input.getT0().getEstimatedValue() + " " + input.getT0().getUnit());
		System.out.println("\tNumber of engines = " + input.getnEngine());
		System.out.println("\tMach array = " + Arrays.toString(input.getMachArray()));
		System.out.println("\tNet thrust array = " + Arrays.toString(input.getNetThrust()));
	}

	public static void executeStandAloneTakeOffCalculator() throws InstantiationException, IllegalAccessException {
		
		Amount<Duration> dtRot = Amount.valueOf(3, SI.SECOND);
		Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
		double mu = 0.03;
		double muBrake = 0.4;
		double kAlphaDot = 0.04; // [1/deg]
		double kcLMax = 0.85;
		double kRot = 1.05;
		double kLO = 1.1;
		double kFailure = 1.1;

		// PARAMETERS USED TO CONSIDER THE PARABOLIC DRAG POLAR CORRECTION AT HIGH CL
		double k1 = 0.0;
		double k2 = 0.0;

		double phi = 1.0;
		double alphaReductionRate = -4; // [deg/s]
		Amount<Length> obstacle = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		
		TakeOffManager theTakeOffManager = new TakeOffManager();
		
		output = new OutputTree();
		
		TakeOffCalculator theTakeOffCalculator = theTakeOffManager.new TakeOffCalculator(
				output,
				dtRot,
				dtHold,
				kcLMax,
				kRot,
				kLO,
				kFailure,
				k1,
				k2,
				phi,
				kAlphaDot,
				alphaReductionRate,
				mu,
				muBrake,
				obstacle
				);
				
		theTakeOffCalculator.calculateTakeOffDistanceODE(null, false);
		
		if(input.isCharts())
			theTakeOffCalculator.createTakeOffCharts();
		
		theTakeOffCalculator.calculateBalancedFieldLength();
		
		if(input.isCharts())
			theTakeOffCalculator.createBalancedFieldLengthChart();
		
		System.out.println("\n-----------------------------------------------------------");
		System.out.println("			RESULTS				 ");
		System.out.println("-----------------------------------------------------------");
		System.out.println("TAKE-OFF DISTANCE = " + output.getTakeOffDistanceAOE().getEstimatedValue() + " " + output.getTakeOffDistanceAOE().getUnit());
		System.out.println("FAR-25 TAKE-OFF FIELD LENGTH = " + output.getTakeOffDistanceFAR25().getEstimatedValue() + " " + output.getTakeOffDistanceFAR25().getUnit());
		System.out.println("BALANCED FIELD LENGTH = " + output.getBalancedFieldLength().getEstimatedValue() + " " + output.getBalancedFieldLength().getUnit());
		System.out.println("V1 = " + output.getV1().getEstimatedValue() + " " + output.getV1().getUnit());
		System.out.println("V2 = " + output.getV2().getEstimatedValue() + " " + output.getV2().getUnit());
		System.out.println("-----------------------------------------------------------\n");
	}
	
	/*******************************************************************************************
	 * This method is in charge of writing all input data collected inside the object of the 
	 * OutputTree class on a XML file.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param output object of the OutputTree class which holds all output data
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public static void writeAllOutput(OutputTree output, String filenameWithPathAndExt) throws InvalidFormatException, IOException {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			createXml(doc, docBuilder);
			createXls(filenameWithPathAndExt);
			
			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt + ".xml");

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/*******************************************************************************************
	 * This method defines the XML tree structure and fill it with results form the OutputTree
	 * object
	 * 
	 * @author Vittorio Trifari
	 */
	private static void createXml(Document doc, DocumentBuilder docBuilder) {
		
		org.w3c.dom.Element rootElement = doc.createElement("TakeOff_Executable");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
		rootElement.appendChild(inputRootElement);

		org.w3c.dom.Element groundConditionsElement = doc.createElement("ground_conditions");
		inputRootElement.appendChild(groundConditionsElement);

		JPADStaticWriteUtils.writeSingleNode("alpha", input.getAlphaGround(), groundConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("wind_speed", input.getvWind(), groundConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("altitude", input.getAltitude(), groundConditionsElement, doc);
				
		org.w3c.dom.Element aircraftDataElement = doc.createElement("aircraft_data");
		inputRootElement.appendChild(aircraftDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("take_off_mass", input.getTakeOffMass(), aircraftDataElement, doc);
		
		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
		aircraftDataElement.appendChild(wingDataElement);
		
		org.w3c.dom.Element geometryDataElement = doc.createElement("geometry");
		wingDataElement.appendChild(geometryDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("span", input.getWingSpan(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("surface", input.getWingSurface(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("distance_from_ground", input.getWingToGroundDistance(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("angle_of_incidence", input.getIw(), geometryDataElement, doc);
		
		org.w3c.dom.Element aerodynamicDataElement = doc.createElement("aerodynamic_data");
		wingDataElement.appendChild(aerodynamicDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("oswald", input.getOswald(), aerodynamicDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cD0_clean", input.getcD0Clean(), aerodynamicDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_cD0_flap", input.getDeltaCD0Flap(), aerodynamicDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_cD0_landing_gears", input.getDeltaCD0LandingGear(), aerodynamicDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_max_take_off", input.getcLmaxTO(), aerodynamicDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL0_take_off", input.getcL0TO(), aerodynamicDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha_take_off", input.getcLalphaFlap(), aerodynamicDataElement, doc);
				
		org.w3c.dom.Element engineDataElement = doc.createElement("engine");
		aircraftDataElement.appendChild(engineDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("static_thrust", input.getT0(), engineDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("number_of_engines", input.getnEngine(), engineDataElement, doc);
	
		if(!input.isEngineModel()) {
		JPADStaticWriteUtils.writeSingleNode("net_thrust_array_single_engine", input.getNetThrust(), engineDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("mach_array", input.getMachArray(), engineDataElement, doc);
		}
		
		//--------------------------------------------------------------------------------------
		// OUTPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element outputRootElement = doc.createElement("OUTPUT");
		rootElement.appendChild(outputRootElement);
		
		org.w3c.dom.Element distanceElement = doc.createElement("distances");
		outputRootElement.appendChild(distanceElement);
		
		JPADStaticWriteUtils.writeSingleNode("ground_roll_distance", output.getGroundRoll(), distanceElement, doc);
		JPADStaticWriteUtils.writeSingleNode("rotation_distance", output.getRotation(), distanceElement, doc);
		JPADStaticWriteUtils.writeSingleNode("airborne_distance", output.getAirborne(), distanceElement, doc);
		JPADStaticWriteUtils.writeSingleNode("take_off_distance_AOE", output.getTakeOffDistanceAOE(), distanceElement, doc);
		JPADStaticWriteUtils.writeSingleNode("take_off_distance_FAR25", output.getTakeOffDistanceFAR25(), distanceElement, doc);
		JPADStaticWriteUtils.writeSingleNode("balanced_field_length", output.getBalancedFieldLength(), distanceElement, doc);
		
		org.w3c.dom.Element speedElement = doc.createElement("speeds");
		outputRootElement.appendChild(speedElement);
		
		JPADStaticWriteUtils.writeSingleNode("stall_speed_take_off", output.getVsT0(), speedElement, doc);
		JPADStaticWriteUtils.writeSingleNode("decision_speed", output.getV1(), speedElement, doc);
		JPADStaticWriteUtils.writeSingleNode("rotation_speed", output.getvRot(), speedElement, doc);
		JPADStaticWriteUtils.writeSingleNode("lift_off_speed", output.getvLO(), speedElement, doc);
		JPADStaticWriteUtils.writeSingleNode("take_off_safety_speed", output.getV2(), speedElement, doc);
		
	}
	
	/*******************************************************************************************
	 * This method defines the XML tree structure and fill it with results form the OutputTree
	 * object
	 * 
	 * @author Vittorio Trifari
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	private static void createXls(String filenameWithPathAndExt) throws InvalidFormatException, IOException {
		
		Workbook wb;
		File outputFile = new File(filenameWithPathAndExt + ".xlsx");
		if (outputFile.exists()) { 
		   outputFile.delete();		
		   System.out.println("Deleting the old .xls file ...");
		} 
		
		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}
		
		// TODO: CREATE OTHER SHEETS!!
		// FIXME: OUTPUT LISTS READ THE LAST RUN VALUES...THEY HAVE TO READ THE FIRST RUN VALUES!
		
		Sheet sheet = wb.createSheet("Acceleration");
		
		List<String> xlsArraysDescription = new ArrayList<String>();
		xlsArraysDescription.add("Time");
		xlsArraysDescription.add("Space");
		xlsArraysDescription.add("Acceleration");
		
		MyArray timeArray = new MyArray();
		timeArray.setAmountList(output.getTime());
		MyArray distenceArray = new MyArray();
		distenceArray.setAmountList(output.getGroundDistance());
		MyArray accelerationArray = new MyArray();
		accelerationArray.setAmountList(output.getAcceleration());
		
		List<MyArray> xlsArraysList = new ArrayList<MyArray>();
		xlsArraysList.add(timeArray);
		xlsArraysList.add(distenceArray);
		xlsArraysList.add(accelerationArray);
		
		List<String> xlsArraysUnit = new ArrayList<String>();
		xlsArraysUnit.add("s");
		xlsArraysUnit.add("m");
		xlsArraysUnit.add("m/(s^2)");
		
		JPADStaticWriteUtils.writeAllArraysToXls(
				sheet,
				xlsArraysDescription,
				xlsArraysList,
				xlsArraysUnit
				);
		
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
        wb.write(fileOut);
        fileOut.close();
        System.out.println("Your excel file has been generated!");
	}
	
	//---------------------------------------------------------------------------------------------
	// NESTED CLASS IN CHARGE OF THE TAKE-OFF DISTANCE CALCULATION
	//---------------------------------------------------------------------------------------------

	public class TakeOffCalculator {
		
		//-------------------------------------------------------------------------------------
		// VARIABLE DECLARATION
		
		private OutputTree output;
		
		private Amount<Duration> dtRot, dtHold,	
		dtRec = Amount.valueOf(1.5, SI.SECOND),
		tHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tEndHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tRot = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
		tEndRot = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tClimb = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
		tFaiulre = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		private Amount<Velocity> vSTakeOff, vRot, vLO;
		private Amount<Length> obstacle;
		private List<Double> alphaDot, gammaDot, cL, cD, loadFactor;
		private List<Amount<Angle>> alpha, theta, gamma;
		private List<Amount<Duration>> time;
		private List<Amount<Velocity>> speed, rateOfClimb;
		private List<Amount<Acceleration>> acceleration;
		private List<Amount<Force>> thrust, thrustHorizontal, thrustVertical, lift, drag, friction, totalForce;
		private List<Amount<Length>> groundDistance, verticalDistance;
		private double kAlphaDot, kcLMax, kRot, kLO, phi, mu, muBrake, kGround, alphaDotInitial,
		alphaRed, cLground, kFailure, k1, k2;
		private Double vFailure;
		private boolean isAborted;
		
		// Interpolated function for balanced field length calculation
		MyInterpolatingFunction netThrustArrayFitted = new MyInterpolatingFunction();
		MyInterpolatingFunction continuedTakeOffFitted = new MyInterpolatingFunction();
		MyInterpolatingFunction abortedTakeOffFitted = new MyInterpolatingFunction();
		// integration index
		private double[] failureSpeedArray, continuedTakeOffArray, abortedTakeOffArray,
		failureSpeedArrayFitted, continuedTakeOffSplineValues,
		abortedTakeOffSplineValues;
		
		//-------------------------------------------------------------------------------------
		// BUILDER:

		/*******************************************************************************
		 * @author Vittorio Trifari
		 *
		 * @param dtRot time interval of the rotation phase
		 * @param dtHold time interval of the constant CL phase
		 * @param kcLMax percentage of the CLmaxTO not to be surpasses
		 * @param kRot percentage of VstallTO which defines the rotation speed
		 * @param kLO percentage of VstallTO which defines the lift-off speed
		 * @param kFailure parameter which defines the drag increment due to engine failure
		 * @param k1 linear correction factor of the parabolic drag polar at high CL
		 * @param k2 quadratic correction factor of the parabolic drag polar at high CL
		 * @param phi throttle setting
		 * @param kAlphaDot coefficient which defines the decrease of alpha_dot during manouvering
		 * @param alphaRed constant negative pitching angular velocity to be maintained after holding 
		 * the CL constant
		 * @param mu friction coefficient without brakes action
		 * @param muBrake friction coefficient with brakes activated
		 * @param wingToGroundDistance
		 * @param obstacle
		 * @param vWind
		 * @param alphaGround
		 * @param iw
		 * @param cD0
		 * @param oswald
		 * @param cLmaxTO
		 * @param cL0
		 * @param cLalphaFlap
		 * @param deltaCD0FlapLandingGears
		 */
		public TakeOffCalculator(
				OutputTree output,
				Amount<Duration> dtRot,
				Amount<Duration> dtHold,
				double kcLMax,
				double kRot,
				double kLO,
				double kFailure,
				double k1,
				double k2,
				double phi,
				double kAlphaDot,
				double alphaRed,
				double mu,
				double muBrake,
				Amount<Length> obstacle
				) {

			// Required data
			this.dtRot = dtRot;
			this.dtHold = dtHold;
			this.kcLMax = kcLMax;
			this.kRot = kRot;
			this.kLO = kLO;
			this.kFailure = kFailure;
			this.k1 = k1;
			this.k2 = k2;
			this.phi = phi;
			this.kAlphaDot = kAlphaDot;
			this.alphaRed = alphaRed;
			this.mu = mu;
			this.muBrake = muBrake;
			this.obstacle = obstacle;
			this.cLground = input.getcL0TO() + (input.getcLalphaFlap().getEstimatedValue()*input.getIw().getEstimatedValue());
			this.output = output;
			
			this.alphaDot = output.getAlphaDot();
			this.gammaDot = output.getGammaDot();
			this.cL = output.getcL();
			this.cD = output.getcD();
			this.loadFactor = output.getLoadFactor();
			this.alpha = output.getAlpha();
			this.theta = output.getTheta();
			this.gamma = output.getGamma();
			this.time = output.getTime();
			this.speed = output.getSpeed();
			this.rateOfClimb = output.getRateOfClimb();
			this.acceleration = output.getAcceleration();
			this.thrust = output.getThrust();
			this.thrustHorizontal = output.getThrustHorizontal();
			this.thrustVertical = output.getThrustVertical();
			this.lift = output.getLift();
			this.drag = output.getDrag();
			this.friction = output.getFriction();
			this.totalForce = output.getTotalForce();
			this.groundDistance = output.getGroundDistance();
			this.verticalDistance = output.getVerticalDistance();
			
			// Reference velocities definition
			vSTakeOff = Amount.valueOf(
					SpeedCalc.calculateSpeedStall(
							input.getAltitude().getEstimatedValue(),
							input.getTakeOffMass().times(AtmosphereCalc.g0).getEstimatedValue(),
							input.getWingSurface().getEstimatedValue(),
							input.getcLmaxTO()
							),
					SI.METERS_PER_SECOND);
			vRot = vSTakeOff.times(kRot);
			vLO = vSTakeOff.times(kLO);

			System.out.println("\n-----------------------------------------------------------");
			System.out.println("CLmaxTO = " + input.getcLmaxTO());
			System.out.println("CL0 = " + input.getcL0TO());
			System.out.println("CLground = " + cLground);
			System.out.println("CD0 clean = " + input.getcD0Clean());
			System.out.println("Delta CD0 flap + landing gears = " + (input.getDeltaCD0Flap()+input.getDeltaCD0LandingGear()));
			System.out.println("CD0 TakeOff = " + (input.getcD0Clean() + input.getDeltaCD0Flap() + input.getDeltaCD0LandingGear()));
			System.out.println("VsTO = " + vSTakeOff.getEstimatedValue() + " " + vSTakeOff.getUnit());
			System.out.println("VRot = " + vRot.getEstimatedValue() + " " + vRot.getUnit());
			System.out.println("vLO = " + vLO.getEstimatedValue() + " " +vLO.getUnit());
			System.out.println("-----------------------------------------------------------\n");

			output.setVsT0(vSTakeOff);
			output.setvRot(vRot);
			output.setvLO(vLO);
			
			// McCormick interpolated function --> See the excel file into JPAD DOCS
			double hb = input.getWingToGroundDistance().divide(input.getWingSpan().times(Math.PI/4)).getEstimatedValue();
			kGround = - 622.44*(Math.pow(hb, 5)) + 624.46*(Math.pow(hb, 4)) - 255.24*(Math.pow(hb, 3))
					+ 47.105*(Math.pow(hb, 2)) - 0.6378*hb + 0.0055;

			// check on machArray and netThrust length
			if(input.getMachArray().length != input.getNetThrust().length) {
				System.err.println("WARNING!! MACH ARRAY AND NET THRUST DO NOT HAVE THE SAME SIZE\n\n");
				return;
			}
			
			double[] speedArrayInterpolation = new double[input.getMachArray().length];
			// interpolation of the net thrust:
			for (int i=0; i<input.getMachArray().length; i++)
				speedArrayInterpolation[i] = SpeedCalc.calculateTAS(input.getMachArray()[i], input.getAltitude().getEstimatedValue());
				
			netThrustArrayFitted.interpolate(speedArrayInterpolation, input.getNetThrust());
			
		}

		/**************************************************************************************
		 * This method is used to initialize all lists in order to perform a new calculation or
		 * to setup the first one
		 *
		 * @author Vittorio Trifari
		 */
		public void initialize() {

			// lists cleaning
			output.getTime().clear();
			output.getSpeed().clear();
			output.getThrust().clear();
			output.getThrustHorizontal().clear();
			output.getThrustVertical().clear();
			output.getAlpha().clear();
			output.getAlphaDot().clear();
			output.getGamma().clear();
			output.getGammaDot().clear();
			output.getTheta().clear();
			output.getcL().clear();
			output.getLift().clear();
			output.getLoadFactor().clear();
			output.getcD().clear();
			output.getDrag().clear();
			output.getFriction().clear();
			output.getTotalForce().clear();
			output.getAcceleration().clear();
			output.getRateOfClimb().clear();
			output.getGroundDistance().clear();
			output.getVerticalDistance().clear();

			tHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tEndHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tRot = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
			tEndRot = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tClimb = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
			tFaiulre = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time

			alphaDotInitial = 0.0;
			vFailure = null;
			isAborted = false;

		}

		/***************************************************************************************
		 * This method performs the integration of the total take-off distance by solving a set of
		 * ODE with a HighamHall54Integrator. The library used is the Apache Math3. 
		 * 
		 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
		 * 
		 * @author Vittorio Trifari
		 */
		public void calculateTakeOffDistanceODE(Double vFailure, boolean isAborted) {

			System.out.println("---------------------------------------------------");
			System.out.println("CalcTakeOff :: ODE integration\n\n");

			this.isAborted = isAborted;
			// failure check
			if(vFailure == null)
				this.vFailure = 10000.0; // speed impossible to reach --> no failure!!
			else
				this.vFailure = vFailure;

			FirstOrderIntegrator theIntegrator = new HighamHall54Integrator(
					1e-6,
					1,
					1e-17,
					1e-17
					);
			FirstOrderDifferentialEquations ode = new DynamicsEquationsTakeOff();

			EventHandler ehCheckFailure = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					if(t < tRec.getEstimatedValue())
						return x[1] - TakeOffCalculator.this.getvFailure();
					else
						return 10; // a generic positive value used to make the event trigger once
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tFAILURE OCCURRED !!");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tFaiulre = Amount.valueOf(t, SI.SECOND);

					return  Action.CONTINUE;
				}

			};
			EventHandler ehCheckVRot = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					double speed = x[1];

					if(t < tRec.getEstimatedValue())
						return speed - vRot.getEstimatedValue();
					else
						return 10; // a generic positive value used to make the event trigger once
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF GROUND ROLL PHASE");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m"
							);

					tRot = Amount.valueOf(t, SI.SECOND);

					// COLLECTING DATA IN TakeOffResultsMap
					System.out.println("\n\tCOLLECTING DATA AT THE END OF GROUND ROLL PHASE ...");
					
					if(output.getGroundRoll().getEstimatedValue() == 0.0)
						output.setGroundRoll(groundDistance.get(groundDistance.size()-1));
					
					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.CONTINUE;
				}
			};
			EventHandler ehEndConstantCL = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					return t - (tHold.plus(dtHold).getEstimatedValue());
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND BAR HOLDING");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tEndHold = Amount.valueOf(t, SI.SECOND);

					return  Action.CONTINUE;
				}

			};
			EventHandler ehCheckObstacle = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					return x[3] - obstacle.getEstimatedValue();
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND OF AIRBORNE PHASE");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s" + 
									"\n\tx[2] = gamma = " + x[2] + " °" +
									"\n\tx[3] = altitude = " + x[3] + " m"
							);

					// COLLECTING DATA IN TakeOffResultsMap
					System.out.println("\n\tCOLLECTING DATA AT THE END OF AIRBORNE PHASE ...");

					if(output.getAirborne().getEstimatedValue() == 0.0)
						output.setAirborne(groundDistance.get(groundDistance.size()-1).minus(output.getRotation()).minus(output.getGroundRoll()));
					if(output.getTakeOffDistanceAOE().getEstimatedValue() == 0.0)
						output.setTakeOffDistanceAOE(groundDistance.get(groundDistance.size()-1));
					if(output.getTakeOffDistanceFAR25().getEstimatedValue() == 0.0)
						output.setTakeOffDistanceFAR25(output.getTakeOffDistanceAOE().times(1.15));
					if(output.getV2().getEstimatedValue() == 0.0)
						output.setV2(speed.get(speed.size()-1));
					
					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.STOP;
				}
			};
			EventHandler ehCheckBrakes = new EventHandler() {

				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {

					return t - (tFaiulre.plus(dtRec).getEstimatedValue());
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tFAILURE RECOGNITION --> BRAKES ACTIVATED");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println("\n---------------------------DONE!-------------------------------");

					tRec = Amount.valueOf(t, SI.SECOND);

					return  Action.CONTINUE;
				}

			};
			EventHandler ehCheckStop = new EventHandler() {
				@Override
				public void init(double t0, double[] y0, double t) {

				}

				@Override
				public void resetState(double t, double[] y) {

				}

				// Discrete event, switching function
				@Override
				public double g(double t, double[] x) {
					double speed = x[1];
					return speed - 0.0;
				}

				@Override
				public Action eventOccurred(double t, double[] x, boolean increasing) {
					// Handle an event and choose what to do next.
					System.out.println("\n\t\tEND ABORTED TAKE OFF RUN");
					System.out.println("\n\tswitching function changes sign at t = " + t);
					System.out.println(
							"\n\tx[0] = s = " + x[0] + " m" +
									"\n\tx[1] = V = " + x[1] + " m/s"
							);

					System.out.println("\n---------------------------DONE!-------------------------------");
					return  Action.STOP;
				}
			};

			if(!isAborted) {
				theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckFailure, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehEndConstantCL, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckObstacle, 1.0, 1e-3, 20);
			}
			else {
				theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckFailure, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckBrakes, 1.0, 1e-3, 20);
				theIntegrator.addEventHandler(ehCheckStop, 1.0, 1e-6, 20);
		 	}

			// handle detailed info
			StepHandler stepHandler = new StepHandler() {

				public void init(double t0, double[] x0, double t) {
				}

				@Override
				public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {

					double   t = interpolator.getCurrentTime();
					double[] x = interpolator.getInterpolatedState();

					// CHECK TO BE DONE ONLY IF isAborted IS FALSE!!
					if(!isAborted) {
						// CHECK ON LOAD FACTOR --> END ROTATION WHEN n=1
						if((t > tRot.getEstimatedValue()) && (tEndRot.getEstimatedValue() == 10000.0) &&
								(loadFactor.get(loadFactor.size()-1) > 1) &&
								(loadFactor.get(loadFactor.size()-2) < 1)) {
							System.out.println("\n\t\tEND OF ROTATION PHASE");
							System.out.println(
									"\n\tx[0] = s = " + x[0] + " m" +
											"\n\tx[1] = V = " + x[1] + " m/s" + 
											"\n\tx[2] = gamma = " + x[2] + " °" +
											"\n\tx[3] = altitude = " + x[3] + " m"+
											"\n\tt = " + t + " s"
									);
							// COLLECTING DATA IN TakeOffResultsMap
							System.out.println("\n\tCOLLECTING DATA AT THE END OF ROTATION PHASE ...");
							
							if(output.getRotation().getEstimatedValue() == 0.0)
								output.setRotation(groundDistance.get(groundDistance.size()-1).minus(output.getGroundRoll()));
							
							System.out.println("\n---------------------------DONE!-------------------------------");

							tEndRot = Amount.valueOf(t, SI.SECOND);
						}
						// CHECK IF THE THRESHOLD CL IS REACHED --> FROM THIS POINT ON THE BAR IS LOCKED
						if((t > tEndRot.getEstimatedValue()) && 
								(cL.get(cL.size()-1) > kcLMax*input.getcLmaxTO()) &&
								((cL.get(cL.size()-2) < kcLMax*input.getcLmaxTO()))) {
							System.out.println("\n\t\tBEGIN BAR HOLDING");
							System.out.println(
									"\n\tCL = " + ((DynamicsEquationsTakeOff)ode).cL(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t
											) + 
									"\n\tAlpha Body = " + ((DynamicsEquationsTakeOff)ode).alpha + " °" + 
									"\n\tt = " + t + " s"
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tHold = Amount.valueOf(t, SI.SECOND);
						}
						// CHECK ON LOAD FACTOR TO ENSTABLISH WHEN n=1 WHILE DECREASING ALPHA AND CL
						if((t > tEndHold.getEstimatedValue()) && (tClimb.getEstimatedValue() == 10000.0) &&
								(loadFactor.get(loadFactor.size()-1) < 1) &&
								(loadFactor.get(loadFactor.size()-2) > 1)) {
							System.out.println("\n\t\tLOAD FACTOR = 1 IN CLIMB");
							System.out.println( 
									"\n\tt = " + t + " s"
									);
							System.out.println("\n---------------------------DONE!-------------------------------");

							tClimb = Amount.valueOf(t, SI.SECOND);
						}
					}

					// PICKING UP ALL DATA AT EVERY STEP (RECOGNIZING IF THE TAKE-OFF IS CONTINUED OR ABORTED)
					//----------------------------------------------------------------------------------------
					// TIME:
					time.add(Amount.valueOf(t, SI.SECOND));
					//----------------------------------------------------------------------------------------
					// SPEED:
					speed.add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
					//----------------------------------------------------------------------------------------
					// THRUST:
					if(!isAborted)
						thrust.add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t),
								SI.NEWTON)
								);
					else {
						if(t < tRec.getEstimatedValue())
							thrust.add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t),
									SI.NEWTON)
									);
						else
							thrust.add(Amount.valueOf(
									0.0,
									SI.NEWTON)
									);
					}
					//----------------------------------------------------------------------------------------
					// THRUST HORIZONTAL:
					if(!isAborted)
						thrustHorizontal.add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.cos(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										),
								SI.NEWTON)
								);
					else {
						if(t < tRec.getEstimatedValue())
							thrustHorizontal.add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.cos(
											Amount.valueOf(
													((DynamicsEquationsTakeOff)ode).alpha,
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
											),
									SI.NEWTON)
									);
						else
							thrustHorizontal.add(Amount.valueOf(
									0.0,
									SI.NEWTON)
									);
					}
					//----------------------------------------------------------------------------------------
					// THRUST VERTICAL:
					if(!isAborted)
						thrustVertical.add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.sin(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										),
								SI.NEWTON)
								);
					else {
						if(t < tRec.getEstimatedValue())
							thrustVertical.add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.sin(
											Amount.valueOf(
													((DynamicsEquationsTakeOff)ode).alpha,
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
											),
									SI.NEWTON)
									);
						else
							thrustVertical.add(Amount.valueOf(
									0.0,
									SI.NEWTON)
									);
					}
					//--------------------------------------------------------------------------------
					// FRICTION:
					if(!isAborted) {
						if(t < tEndRot.getEstimatedValue())
							friction.add(Amount.valueOf(
									mu*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)),
									SI.NEWTON)
									);
						else
							friction.add(Amount.valueOf(0.0, SI.NEWTON));
					}
					else {
						if(t < tRec.getEstimatedValue())
							friction.add(Amount.valueOf(
									mu*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)),
									SI.NEWTON)
									);
						else
							friction.add(Amount.valueOf(
									muBrake*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)),
									SI.NEWTON)
									);
					}
					//----------------------------------------------------------------------------------------
					// LIFT:
					lift.add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).lift(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// DRAG:
					drag.add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).drag(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t),
							SI.NEWTON)
							);
					//----------------------------------------------------------------------------------------
					// TOTAL FORCE:
					if(!isAborted) {
						totalForce.add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.cos(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										)
								- ((DynamicsEquationsTakeOff)ode).drag(
										x[1],
										((DynamicsEquationsTakeOff)ode).alpha,
										x[2],
										t)
								- mu*(((DynamicsEquationsTakeOff)ode).weight
										- ((DynamicsEquationsTakeOff)ode).lift(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t))
								- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
										Amount.valueOf(
												x[2],
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
								SI.NEWTON)
								);
					}
					else {
						if(t < tRec.getEstimatedValue())
							totalForce.add(Amount.valueOf(
									((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.cos(
											Amount.valueOf(
													((DynamicsEquationsTakeOff)ode).alpha,
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
											)
									- ((DynamicsEquationsTakeOff)ode).drag(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t)
									- mu*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t))
									- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
											Amount.valueOf(
													x[2],
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
									SI.NEWTON)
									);
						else
							totalForce.add(Amount.valueOf(
									- ((DynamicsEquationsTakeOff)ode).drag(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t)
									- muBrake*(((DynamicsEquationsTakeOff)ode).weight
											- ((DynamicsEquationsTakeOff)ode).lift(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t))
									- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
											Amount.valueOf(
													x[2],
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
									SI.NEWTON)
									);
					}
					//----------------------------------------------------------------------------------------
					// LOAD FACTOR:
					loadFactor.add(
							((DynamicsEquationsTakeOff)ode).lift(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t)
							/(((DynamicsEquationsTakeOff)ode).weight*Math.cos(
									Amount.valueOf(
											x[2],
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
							);
					//----------------------------------------------------------------------------------------
					// RATE OF CLIMB:
					rateOfClimb.add(Amount.valueOf(
							x[1]*Math.sin(
									Amount.valueOf(
											x[2],
											NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
							SI.METERS_PER_SECOND)
							);
					//----------------------------------------------------------------------------------------
					// ACCELERATION:
					if(!isAborted)
						acceleration.add(Amount.valueOf(
								(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsTakeOff)ode).weight)
								*(((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.cos(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										)
										- ((DynamicsEquationsTakeOff)ode).drag(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t)
										- mu*(((DynamicsEquationsTakeOff)ode).weight
												- ((DynamicsEquationsTakeOff)ode).lift(
														x[1],
														((DynamicsEquationsTakeOff)ode).alpha,
														x[2],
														t))
										- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
												Amount.valueOf(
														x[2],
														NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())),
								SI.METERS_PER_SQUARE_SECOND)
								);
					else {
						if(t < tRec.getEstimatedValue())
							acceleration.add(Amount.valueOf(
									(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsTakeOff)ode).weight)
									*(((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.cos(
											Amount.valueOf(
													((DynamicsEquationsTakeOff)ode).alpha,
													NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
											)
											- ((DynamicsEquationsTakeOff)ode).drag(
													x[1],
													((DynamicsEquationsTakeOff)ode).alpha,
													x[2],
													t)
											- mu*(((DynamicsEquationsTakeOff)ode).weight
													- ((DynamicsEquationsTakeOff)ode).lift(
															x[1],
															((DynamicsEquationsTakeOff)ode).alpha,
															x[2],
															t))
											- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
													Amount.valueOf(
															x[2],
															NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())),
									SI.METERS_PER_SQUARE_SECOND)
									);
						else
							acceleration.add(Amount.valueOf(
									(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsTakeOff)ode).weight)
									*(- ((DynamicsEquationsTakeOff)ode).drag(
											x[1],
											((DynamicsEquationsTakeOff)ode).alpha,
											x[2],
											t)
											- muBrake*(((DynamicsEquationsTakeOff)ode).weight
													- ((DynamicsEquationsTakeOff)ode).lift(
															x[1],
															((DynamicsEquationsTakeOff)ode).alpha,
															x[2],
															t))
											- ((DynamicsEquationsTakeOff)ode).weight*Math.sin(
													Amount.valueOf(
															x[2],
															NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())),
									SI.METERS_PER_SQUARE_SECOND)
									);
					}
					//----------------------------------------------------------------------------------------
					// GROUND DISTANCE:
					groundDistance.add(Amount.valueOf(
							x[0],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// VERTICAL DISTANCE:
					verticalDistance.add(Amount.valueOf(
							x[3],
							SI.METER)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA:
					alpha.add(Amount.valueOf(
							((DynamicsEquationsTakeOff)ode).alpha,
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// GAMMA:
					gamma.add(Amount.valueOf(
							x[2],
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// ALPHA DOT:
					alphaDot.add(
							((DynamicsEquationsTakeOff)ode).alphaDot(t)
							); 
					//----------------------------------------------------------------------------------------
					// GAMMA DOT:
					if(t <= tEndRot.getEstimatedValue())
						gammaDot.add(0.0);
					else
						gammaDot.add(57.3*(AtmosphereCalc.g0.getEstimatedValue()/
								(((DynamicsEquationsTakeOff)ode).weight*x[1]))*(
										((DynamicsEquationsTakeOff)ode).lift(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t) 
										+ (((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.sin(Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())
												)
										- ((DynamicsEquationsTakeOff)ode).weight*Math.cos(Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).gamma,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
												))
								);
					//----------------------------------------------------------------------------------------
					// THETA:
					theta.add(Amount.valueOf(
							x[2] + ((DynamicsEquationsTakeOff)ode).alpha,
							NonSI.DEGREE_ANGLE)
							);
					//----------------------------------------------------------------------------------------
					// CL:
					cL.add(
							((DynamicsEquationsTakeOff)ode).cL(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t
									)
							);
					//----------------------------------------------------------------------------------------
					// CD:
					cD.add(
							((DynamicsEquationsTakeOff)ode).cD0 
							+ ((DynamicsEquationsTakeOff)ode).deltaCD0 
							+ ((((DynamicsEquationsTakeOff)ode).cL(
									x[1],
									((DynamicsEquationsTakeOff)ode).alpha,
									x[2],
									t
									)
									/(Math.PI
											*((DynamicsEquationsTakeOff)ode).ar
											*((DynamicsEquationsTakeOff)ode).oswald))
									*kGround)
							);
					//----------------------------------------------------------------------------------------
				}
			};
			theIntegrator.addStepHandler(stepHandler);

			double[] xAt0 = new double[] {0.0, 0.0, 0.0, 0.0}; // initial state
			theIntegrator.integrate(ode, 0.0, xAt0, 100, xAt0); // now xAt0 contains final state

			theIntegrator.clearEventHandlers();
			theIntegrator.clearStepHandlers();

			System.out.println("\n---------------------------END!!-------------------------------");
		}

		/**************************************************************************************
		 * This method is used to evaluate the balanced take-off field length. It calculates the
		 * total take-off distance and the aborted take-off distance in case of engine failure at
		 * several failure speeds; after that the points made by the failure speed and the related
		 * distance are interpolated in order to have more fitted curves of aborted and continued
		 * take-off conditions. At this point the intersection of these curves is evaluated giving
		 * back the balanced take-off field length and the related failure speed which is the decision
		 * speed (V1)
		 *
		 * @author Vittorio Trifari
		 */
		public void calculateBalancedFieldLength() {

			final PrintStream originalOut = System.out;
			PrintStream filterStream = new PrintStream(new OutputStream() {
				public void write(int b) {
					// write nothing
				}
			});
			System.setOut(filterStream);

			// failure speed array
			failureSpeedArray = MyArrayUtils.linspace(
					2.0,
					vLO.getEstimatedValue(),
					120);
			// continued take-off array
			continuedTakeOffArray = new double[failureSpeedArray.length];
			// aborted take-off array
			abortedTakeOffArray = new double[failureSpeedArray.length];

			// iterative take-off distance calculation for both conditions
			for(int i=0; i<failureSpeedArray.length; i++) {
				initialize();
				calculateTakeOffDistanceODE(failureSpeedArray[i], false);
				continuedTakeOffArray[i] = getGroundDistance().get(groundDistance.size()-1).getEstimatedValue();
				initialize();
				calculateTakeOffDistanceODE(failureSpeedArray[i], true);
				abortedTakeOffArray[i] = getGroundDistance().get(groundDistance.size()-1).getEstimatedValue();
			}

			// arrays intersection
			double[] intersection = MyArrayUtils.intersectArraysSimple(
					continuedTakeOffArray,
					abortedTakeOffArray);
			for(int i=0; i<intersection.length; i++)
				if(intersection[i] != 0.0) {
					output.setBalancedFieldLength(Amount.valueOf(intersection[i], SI.METER));
					output.setV1(Amount.valueOf(failureSpeedArray[i], SI.METERS_PER_SECOND));
				}

			// write again
			System.setOut(originalOut);
		}

		/**************************************************************************************
		 * This method allows users to plot all take-off performance producing several output charts
		 * which have time or ground distance as independent variables.
		 *
		 * @author Vittorio Trifari
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		public void createTakeOffCharts() throws InstantiationException, IllegalAccessException {

			System.out.println("\n---------WRITING TAKE-OFF PERFORMANCE CHARTS TO FILE-----------");

			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Take-off charts" + File.separator);

			// data setup
			double[] time = new double[getTime().size()];
			for(int i=0; i<time.length; i++)
				time[i] = getTime().get(i).getEstimatedValue();

			double[] verticalDistance = new double[getVerticalDistance().size()];
			for(int i=0; i<verticalDistance.length; i++)
				verticalDistance[i] = getVerticalDistance().get(i).getEstimatedValue();

			double[] groundDistance = new double[getGroundDistance().size()];
			for(int i=0; i<groundDistance.length; i++)
				groundDistance[i] = getGroundDistance().get(i).getEstimatedValue();

			double[] thrust = new double[getThrust().size()];
			for(int i=0; i<thrust.length; i++)
				thrust[i] = getThrust().get(i).getEstimatedValue();

			double[] thrustHorizontal = new double[getThrustHorizontal().size()];
			for(int i=0; i<thrustHorizontal.length; i++)
				thrustHorizontal[i] = getThrustHorizontal().get(i).getEstimatedValue();

			double[] thrustVertical = new double[getThrustVertical().size()];
			for(int i=0; i<thrustVertical.length; i++)
				thrustVertical[i] = getThrustVertical().get(i).getEstimatedValue();

			double[] lift = new double[getLift().size()];
			for(int i=0; i<lift.length; i++)
				lift[i] = getLift().get(i).getEstimatedValue();

			double[] drag = new double[getDrag().size()];
			for(int i=0; i<drag.length; i++)
				drag[i] = getDrag().get(i).getEstimatedValue();

			double[] friction = new double[getFriction().size()];
			for(int i=0; i<friction.length; i++)
				friction[i] = getFriction().get(i).getEstimatedValue();

			double[] totalForce = new double[getTotalForce().size()];
			for(int i=0; i<totalForce.length; i++)
				totalForce[i] = getTotalForce().get(i).getEstimatedValue();

			double[] loadFactor = new double[getLoadFactor().size()];
			for(int i=0; i<loadFactor.length; i++)
				loadFactor[i] = getLoadFactor().get(i);

			double[] acceleration = new double[getAcceleration().size()];
			for(int i=0; i<acceleration.length; i++)
				acceleration[i] = getAcceleration().get(i).getEstimatedValue();

			double[] speed = new double[getSpeed().size()];
			for(int i=0; i<speed.length; i++)
				speed[i] = getSpeed().get(i).getEstimatedValue();

			double[] alpha = new double[getAlpha().size()];
			for(int i=0; i<alpha.length; i++)
				alpha[i] = getAlpha().get(i).getEstimatedValue();

			double[] gamma = new double[getGamma().size()];
			for(int i=0; i<gamma.length; i++)
				gamma[i] = getGamma().get(i).getEstimatedValue();

			double[] theta = new double[getTime().size()];
			for(int i=0; i<theta.length; i++)
				theta[i] = getTheta().get(i).getEstimatedValue();

			double[] alphaDot = new double[getAlphaDot().size()];
			for(int i=0; i<alphaDot.length; i++)
				alphaDot[i] = getAlphaDot().get(i);

			double[] gammaDot = new double[getGammaDot().size()];
			for(int i=0; i<gammaDot.length; i++)
				gammaDot[i] = getGammaDot().get(i);

			double[] cL = new double[getcL().size()];
			for(int i=0; i<cL.length; i++)
				cL[i] = getcL().get(i);

			double[] rateOfClimb = new double[getRateOfClimb().size()];
			for(int i=0; i<rateOfClimb.length; i++)
				rateOfClimb[i] = getRateOfClimb().get(i).getEstimatedValue();

			double[] weightVertical = new double[getTime().size()];
			for(int i=0; i<weightVertical.length; i++)
				weightVertical[i] = input.getTakeOffMass().times(AtmosphereCalc.g0).getEstimatedValue()
				*Math.cos(getGamma().get(i).to(SI.RADIAN).getEstimatedValue());

			double[] weightHorizontal = new double[getTime().size()];
			for(int i=0; i<weightHorizontal.length; i++)
				weightHorizontal[i] = input.getTakeOffMass().times(AtmosphereCalc.g0).getEstimatedValue()
				*Math.sin(getGamma().get(i).to(SI.RADIAN).getEstimatedValue());

			if(!isAborted) {
				// take-off trajectory
				MyChartToFileUtils.plotNoLegend(
						groundDistance, verticalDistance,
						0.0, null, 0.0, null,
						"Ground Distance", "Altitude", "m", "m",
						subfolderPath, "TakeOff_Trajectory");

				// vertical distance v.s. time
				MyChartToFileUtils.plotNoLegend(
						time, verticalDistance,
						0.0, null, 0.0, null,
						"Time", "Altitude", "s", "m",
						subfolderPath, "Altitude_evolution");
			}

			// speed v.s. time
			MyChartToFileUtils.plotNoLegend(
					time, speed,
					0.0, null, 0.0, null,
					"Time", "Speed", "s", "m/s",
					subfolderPath, "Speed_evolution");

			// speed v.s. ground distance
			MyChartToFileUtils.plotNoLegend(
					groundDistance, speed,
					0.0, null, 0.0, null,
					"Ground Distance", "Speed", "m", "m/s",
					subfolderPath, "Speed_vs_GroundDistance");

			// acceleration v.s. time
			MyChartToFileUtils.plotNoLegend(
					time, acceleration,
					0.0, null, null, null,
					"Time", "Acceleration", "s", "m/(s^2)",
					subfolderPath, "Acceleration_evolution");

			// acceleration v.s. time
			MyChartToFileUtils.plotNoLegend(
					groundDistance, acceleration,
					0.0, null, null, null,
					"Ground Distance", "Acceleration", "m", "m/(s^2)",
					subfolderPath, "Acceleration_vs_GroundDistance");

			// load factor v.s. time
			MyChartToFileUtils.plotNoLegend(
					time, loadFactor,
					0.0, null, 0.0, null,
					"Time", "Load Factor", "s", "",
					subfolderPath, "LoadFactor_evolution");

			// load factor v.s. ground distance
			MyChartToFileUtils.plotNoLegend(
					groundDistance, loadFactor,
					0.0, null, 0.0, null,
					"Ground distance", "Load Factor", "m", "",
					subfolderPath, "LoadFactor_vs_GroundDistance");

			if(!isAborted) {
				// Rate of Climb v.s. Time
				MyChartToFileUtils.plotNoLegend(
						time, rateOfClimb,
						0.0, null, 0.0, null,
						"Time", "Rate of Climb", "s", "m/s",
						subfolderPath, "RateOfClimb_evolution");

				// Rate of Climb v.s. Ground distance
				MyChartToFileUtils.plotNoLegend(
						groundDistance, rateOfClimb,
						0.0, null, 0.0, null,
						"Ground distance", "Rate of Climb", "m", "m/s",
						subfolderPath, "RateOfClimb_vs_GroundDistance");
			}

			// CL v.s. Time
			MyChartToFileUtils.plotNoLegend(
					time, cL,
					0.0, null, 0.0, null,
					"Time", "CL", "s", "",
					subfolderPath, "CL_evolution");

			// CL v.s. Ground distance
			MyChartToFileUtils.plotNoLegend(
					groundDistance, cL,
					0.0, null, 0.0, null,
					"Ground distance", "CL", "m", "",
					subfolderPath, "CL_vs_GroundDistance");

			// Horizontal Forces v.s. Time
			double[][] xMatrix1 = new double[5][totalForce.length];
			for(int i=0; i<xMatrix1.length; i++)
				xMatrix1[i] = time;

			double[][] yMatrix1 = new double[5][totalForce.length];
			yMatrix1[0] = totalForce;
			yMatrix1[1] = thrustHorizontal;
			yMatrix1[2] = drag;
			yMatrix1[3] = friction;
			yMatrix1[4] = weightHorizontal;

			MyChartToFileUtils.plot(
					xMatrix1, yMatrix1,
					0.0, null, null, null,
					"Time", "Horizontal Forces", "s", "N",
					new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
					subfolderPath, "HorizontalForces_evolution");

			// Horizontal Forces v.s. Ground Distance
			double[][] xMatrix2 = new double[5][totalForce.length];
			for(int i=0; i<xMatrix2.length; i++)
				xMatrix2[i] = groundDistance;

			double[][] yMatrix2 = new double[5][totalForce.length];
			yMatrix2[0] = totalForce;
			yMatrix2[1] = thrustHorizontal;
			yMatrix2[2] = drag;
			yMatrix2[3] = friction;
			yMatrix2[4] = weightHorizontal;

			MyChartToFileUtils.plot(
					xMatrix2, yMatrix2,
					0.0, null, null, null,
					"Ground Distance", "Horizontal Forces", "m", "N",
					new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
					subfolderPath, "HorizontalForces_vs_GroundDistance");

			// Vertical Forces v.s. Time
			double[][] xMatrix3 = new double[3][totalForce.length];
			for(int i=0; i<xMatrix3.length; i++)
				xMatrix3[i] = time;

			double[][] yMatrix3 = new double[3][totalForce.length];
			yMatrix3[0] = lift;
			yMatrix3[1] = thrustVertical;
			yMatrix3[2] = weightVertical;

			MyChartToFileUtils.plot(
					xMatrix3, yMatrix3,
					0.0, null, null, null,
					"Time", "Vertical Forces", "s", "N",
					new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
					subfolderPath, "VerticalForces_evolution");

			// Vertical Forces v.s. ground distance
			double[][] xMatrix4 = new double[3][totalForce.length];
			for(int i=0; i<xMatrix4.length; i++)
				xMatrix4[i] = groundDistance;

			double[][] yMatrix4 = new double[3][totalForce.length];
			yMatrix4[0] = lift;
			yMatrix4[1] = thrustVertical;
			yMatrix4[2] = weightVertical;

			MyChartToFileUtils.plot(
					xMatrix4, yMatrix4,
					0.0, null, null, null,
					"Ground distance", "Vertical Forces", "m", "N",
					new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
					subfolderPath, "VerticalForces_vs_GroundDistance");

			if(!isAborted) {
				// Angles v.s. time
				double[][] xMatrix5 = new double[3][totalForce.length];
				for(int i=0; i<xMatrix5.length; i++)
					xMatrix5[i] = time;

				double[][] yMatrix5 = new double[3][totalForce.length];
				yMatrix5[0] = alpha;
				yMatrix5[1] = theta;
				yMatrix5[2] = gamma;

				MyChartToFileUtils.plot(
						xMatrix5, yMatrix5,
						0.0, null, null, null,
						"Time", "Angles", "s", "deg",
						new String[] {"Alpha Body", "Theta", "Gamma"},
						subfolderPath, "Angles_evolution");

				// Angles v.s. Ground Distance
				double[][] xMatrix6 = new double[3][totalForce.length];
				for(int i=0; i<xMatrix6.length; i++)
					xMatrix6[i] = groundDistance;

				double[][] yMatrix6 = new double[3][totalForce.length];
				yMatrix6[0] = alpha;
				yMatrix6[1] = theta;
				yMatrix6[2] = gamma;

				MyChartToFileUtils.plot(
						xMatrix6, yMatrix6,
						0.0, null, null, null,
						"Ground Distance", "Angles", "m", "deg",
						new String[] {"Alpha Body", "Theta", "Gamma"},
						subfolderPath, "Angles_vs_GroundDistance");

				// Angular velocity v.s. time
				double[][] xMatrix7 = new double[2][totalForce.length];
				for(int i=0; i<xMatrix7.length; i++)
					xMatrix7[i] = time;

				double[][] yMatrix7 = new double[2][totalForce.length];
				yMatrix7[0] = alphaDot;
				yMatrix7[1] = gammaDot;

				MyChartToFileUtils.plot(
						xMatrix7, yMatrix7,
						0.0, null, null, null,
						"Time", "Angular Velocity", "s", "deg/s",
						new String[] {"Alpha_dot", "Gamma_dot"},
						subfolderPath, "AngularVelocity_evolution");

				// Angular velocity v.s. Ground Distance
				double[][] xMatrix8 = new double[2][totalForce.length];
				for(int i=0; i<xMatrix8.length; i++)
					xMatrix8[i] = groundDistance;

				double[][] yMatrix8 = new double[2][totalForce.length];
				yMatrix8[0] = alphaDot;
				yMatrix8[1] = gammaDot;

				MyChartToFileUtils.plot(
						xMatrix8, yMatrix8,
						0.0, null, null, null,
						"Ground Distance", "Angular Velocity", "m", "deg/s",
						new String[] {"Alpha_dot", "Gamma_dot"},
						subfolderPath, "AngularVelocity_vs_GroundDistance");
			}

			System.out.println("\n---------------------------DONE!-------------------------------");
		}

		/**************************************************************************************
		 * This method allows users to plot the OEI continued take-off distance and the aborted
		 * take-off distance as function of the failure speed compared with the take-off stalling
		 * speed. From the intersection of the two curves it's possible to identify the decision
		 * speed (V1) and the balanced field length.
		 *
		 * @author Vittorio Trifari
		 */
		public void createBalancedFieldLengthChart() {

			System.out.println("\n-------WRITING BALANCED TAKE-OFF DISTANCE CHART TO FILE--------");

			String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
			String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Take-Off charts" + File.separator);

			for(int i=0; i<failureSpeedArray.length; i++)
				failureSpeedArray[i] = failureSpeedArray[i]/vSTakeOff.getEstimatedValue();

			double[][] xArray = new double[][]
					{failureSpeedArray, failureSpeedArray};
					double[][] yArray = new double[][]
							{continuedTakeOffArray, abortedTakeOffArray};

							MyChartToFileUtils.plot(
									xArray, yArray,
									null, null, null, null,
									"Vfailure/VsTO", "Distance", "", "m",
									new String[] {"OEI Take-Off", "Aborted Take-Off"},
									subfolderPath, "BalancedTakeOffLength");

							System.out.println("\n---------------------------DONE!-------------------------------");
		}

		//------------------------------------------------------------------------------------------
		//  NESTED CLASS INSIDE THE TAKE OFF CALCULATOR IN CHARGE OF DESCRIBING THE ODE EQUATIONS
		//------------------------------------------------------------------------------------------
		// ODE integration
		// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

		public class DynamicsEquationsTakeOff implements FirstOrderDifferentialEquations {

			double weight, altitude, g0, mu, kAlpha, cD0, deltaCD0, oswald, ar, k1, k2, kGround, vWind, alphaDotInitial;

			// visible variables
			public double alpha, gamma;

			public DynamicsEquationsTakeOff() {

				// constants and known values
				weight = input.getTakeOffMass().times(AtmosphereCalc.g0).getEstimatedValue();
				g0 = AtmosphereCalc.g0.getEstimatedValue();
				mu = TakeOffCalculator.this.mu;
				kAlpha = TakeOffCalculator.this.kAlphaDot;
				cD0 = input.getcD0Clean();
				deltaCD0 = input.getDeltaCD0Flap()+input.getDeltaCD0LandingGear();
				oswald = input.getOswald();
				ar = input.getAspectRatio();
				k1 = TakeOffCalculator.this.getK1();
				k2 = TakeOffCalculator.this.getK2();
				kGround = TakeOffCalculator.this.getkGround();
				vWind = input.getvWind().getEstimatedValue();
				altitude = input.getAltitude().getEstimatedValue();

				// alpha_dot_initial calculation
				double cLatLiftOff = input.getcLmaxTO()/(Math.pow(kLO, 2));
				double alphaLiftOff = (cLatLiftOff - input.getcL0TO())/input.getcLalphaFlap().getEstimatedValue();
				alphaDotInitial = (((alphaLiftOff - input.getIw().getEstimatedValue()) 
						- input.getAlphaGround().getEstimatedValue())
						/(dtRot.getEstimatedValue())
						);
			}

			@Override
			public int getDimension() {
				return 4;
			}

			@Override
			public void computeDerivatives(double t, double[] x, double[] xDot)
					throws MaxCountExceededException, DimensionMismatchException {

				alpha = alpha(t);
				double speed = x[1];
				gamma = x[2];

				if(!isAborted) {
					if( t < tEndRot.getEstimatedValue()) {
						xDot[0] = speed;
						xDot[1] = (g0/weight)*(thrust(speed, gamma, t) - drag(speed, alpha, gamma, t)
								- (mu*(weight - lift(speed, alpha, gamma, t))));
						xDot[2] = 0.0;
						xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
					}
					else {
						xDot[0] = speed;
						xDot[1] = (g0/weight)*(
								thrust(speed, gamma,t )*Math.cos(Amount.valueOf(alpha, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()) 
								- drag(speed, alpha, gamma, t) 
								- weight*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()));
						xDot[2] = 57.3*(g0/(weight*speed))*(
								lift(speed, alpha, gamma, t) 
								+ (thrust(speed, gamma, t)*Math.sin(Amount.valueOf(alpha, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))
								- weight*Math.cos(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()));
						xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
					}
				}
				else {
					if( t < tRec.getEstimatedValue()) {
						xDot[0] = speed;
						xDot[1] = (g0/weight)*(thrust(speed, gamma, t) - drag(speed, alpha, gamma, t)
								- (mu*(weight - lift(speed, alpha, gamma, t))));
						xDot[2] = 0.0;
						xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
					}
					else {
						xDot[0] = speed;
						xDot[1] = (g0/weight)*(-drag(speed, alpha, gamma, t)
								- (muBrake*(weight-lift(speed, alpha, gamma, t))));
						xDot[2] = 0.0;
						xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
					}
				}
			}

			public double thrust(double speed, double gamma, double time) {

				double theThrust = 0.0;
				
				if (time < tFaiulre.getEstimatedValue()) {
					if(input.isEngineModel()) {
						double thrustRatio = 1-(0.00252*speed)+(0.00000434*(Math.pow(speed, 2)));  // simplified thrust model for a turbofan
						theThrust = input.getnEngine()*input.getT0().getEstimatedValue()*thrustRatio;
					}
					else
						theThrust = netThrustArrayFitted.value(speed)*input.getnEngine();
				}
				else {
					if(input.isEngineModel()) {
						double thrustRatio = 1-(0.00252*speed)+(0.00000434*(Math.pow(speed, 2)));  // simplified thrust model for a turbofan
						theThrust = (input.getnEngine()-1)*input.getT0().getEstimatedValue()*thrustRatio;
					}
					else
						theThrust = netThrustArrayFitted.value(speed)*(input.getnEngine()-1);
				}

				return theThrust;
			}

			public double cD(double cL) {

				double cD = 0.0;

				if(cL < 1.2) {
					cD = cD0 + deltaCD0 + ((Math.pow(cL, 2)/(Math.PI*ar*oswald))*kGround);
				}
				else { 
					cD = cD0 + deltaCD0 + ((Math.pow(cL, 2)/(Math.PI*ar*oswald))*kGround)
							+ (k1*(cL - 1.2)) + (k2*(Math.pow((cL - 1.2), 2))) ;
				}

				return cD;
			}

			public double drag(double speed, double alpha, double gamma, double time) {

				double cD = 0;

				if (time < tRec.getEstimatedValue())
					cD = cD(cL(speed, alpha, gamma, time));
				else
					cD = kFailure*cD(cL(speed, alpha, gamma, time));

				return 	0.5
						*input.getWingSurface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								altitude)
						*(Math.pow(speed + (vWind*Math.cos(Amount.valueOf(
								gamma,
								NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())), 2))
						*cD;
			}

			public double cL(double speed, double alpha, double gamma ,double time) {

				if (time < tClimb.getEstimatedValue()) {
					double cL0 = input.getcL0TO();
					double cLalpha = input.getcLalphaFlap().getEstimatedValue();
					double alphaWing = alpha + input.getIw().getEstimatedValue();

					return cL0 + (cLalpha*alphaWing);
				}
				else
					return (2*weight*Math.cos(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))/
							(input.getWingSurface().getEstimatedValue()*
									AtmosphereCalc.getDensity(
											altitude)*
									Math.pow(speed, 2));
			}

			public double lift(double speed, double alpha, double gamma, double time) {

				double cL = cL(speed, alpha, gamma, time);

				return 	0.5
						*input.getWingSurface().getEstimatedValue()
						*AtmosphereCalc.getDensity(
								altitude)
						*(Math.pow(speed + (vWind*Math.cos(Amount.valueOf(
								gamma,
								NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())), 2))
						*cL;
			}

			public double alphaDot(double time) {

				double alphaDot = 0.0;

				if(isAborted)
					alphaDot = 0.0;
				else {
					if((time > tRot.getEstimatedValue()) && (time < tHold.getEstimatedValue())) {
						alphaDot = alphaDotInitial*(1-(kAlpha*(TakeOffCalculator.this.getAlpha().get(
								TakeOffCalculator.this.getAlpha().size()-1).getEstimatedValue()))
								);
					}
					else if((time > tEndHold.getEstimatedValue()) && (time < tClimb.getEstimatedValue())) {
						alphaDot = alphaRed;
					}
				}
				return alphaDot;
			}

			public double alpha(double time) {

				double alpha = input.getAlphaGround().getEstimatedValue();

				if(time > tRot.getEstimatedValue())
					alpha = TakeOffCalculator.this.getAlpha().get(
							TakeOffCalculator.this.getAlpha().size()-1).getEstimatedValue()
					+(alphaDot(time)*(TakeOffCalculator.this.getTime().get(
							TakeOffCalculator.this.getTime().size()-1).getEstimatedValue()
							- TakeOffCalculator.this.getTime().get(
									TakeOffCalculator.this.getTime().size()-2).getEstimatedValue()));

				return alpha;
			}
		}
		//-------------------------------------------------------------------------------------
		//								END INNER NESTED CLASS	
		//-------------------------------------------------------------------------------------


		//-------------------------------------------------------------------------------------
		// GETTERS AND SETTERS:

		public Amount<Duration> getDtRot() {
			return dtRot;
		}

		public void setDtRot(Amount<Duration> dtRot) {
			this.dtRot = dtRot;
		}

		public Amount<Duration> getDtHold() {
			return dtHold;
		}

		public void setDtHold(Amount<Duration> dtHold) {
			this.dtHold = dtHold;
		}

		public Amount<Duration> getDtRec() {
			return dtRec;
		}

		public void setDtRec(Amount<Duration> dtRec) {
			this.dtRec = dtRec;
		}

		public Amount<Velocity> getvSTakeOff() {
			return vSTakeOff;
		}

		public void setvSTakeOff(Amount<Velocity> vSTakeOff) {
			this.vSTakeOff = vSTakeOff;
		}

		public Amount<Velocity> getvRot() {
			return vRot;
		}

		public void setvRot(Amount<Velocity> vRot) {
			this.vRot = vRot;
		}

		public Amount<Velocity> getvLO() {
			return vLO;
		}

		public void setvLO(Amount<Velocity> vLO) {
			this.vLO = vLO;
		}

		public Amount<Length> getObstacle() {
			return obstacle;
		}

		public void setObstacle(Amount<Length> obstacle) {
			this.obstacle = obstacle;
		}

		public double getkAlphaDot() {
			return kAlphaDot;
		}

		public void setkAlphaDot(double kAlphaDot) {
			this.kAlphaDot = kAlphaDot;
		}

		public double getMu() {
			return mu;
		}

		public void setMu(double mu) {
			this.mu = mu;
		}

		public double getMuBrake() {
			return muBrake;
		}

		public void setMuBrake(double muBrake) {
			this.muBrake = muBrake;
		}

		public double getkGround() {
			return kGround;
		}

		public void setkGround(double kGround) {
			this.kGround = kGround;
		}

		public double getAlphaDotInitial() {
			return alphaDotInitial;
		}

		public void setAlphaDotInitial(double alphaDotInitial) {
			this.alphaDotInitial = alphaDotInitial;
		}

		public double getAlphaRed() {
			return alphaRed;
		}

		public void setAlphaRed(double alphaRed) {
			this.alphaRed = alphaRed;
		}

		public double getcLground() {
			return cLground;
		}

		public void setcLground(double cLground) {
			this.cLground = cLground;
		}

		public double getkFailure() {
			return kFailure;
		}

		public void setkFailure(double kFailure) {
			this.kFailure = kFailure;
		}

		public double getkRot() {
			return kRot;
		}

		public double getkLO() {
			return kLO;
		}

		public double getKclMax() {
			return kcLMax;
		}

		public double getK1() {
			return k1;
		}

		public double getK2() {
			return k2;
		}

		public double getPhi() {
			return phi;
		}

		public MyInterpolatingFunction getContinuedTakeOffFitted() {
			return continuedTakeOffFitted;
		}

		public void setContinuedTakeOffFitted(MyInterpolatingFunction continuedTakeOffFitted) {
			this.continuedTakeOffFitted = continuedTakeOffFitted;
		}

		public MyInterpolatingFunction getAbortedTakeOffFitted() {
			return abortedTakeOffFitted;
		}

		public void setAbortedTakeOffFitted(MyInterpolatingFunction abortedTakeOffFitted) {
			this.abortedTakeOffFitted = abortedTakeOffFitted;
		}

		public double[] getFailureSpeedArray() {
			return failureSpeedArray;
		}

		public void setFailureSpeedArray(double[] failureSpeedArray) {
			this.failureSpeedArray = failureSpeedArray;
		}

		public double[] getContinuedTakeOffArray() {
			return continuedTakeOffArray;
		}

		public void setContinuedTakeOffArray(double[] continuedTakeOffArray) {
			this.continuedTakeOffArray = continuedTakeOffArray;
		}

		public double[] getAbortedTakeOffArray() {
			return abortedTakeOffArray;
		}

		public void setAbortedTakeOffArray(double[] abortedTakeOffArray) {
			this.abortedTakeOffArray = abortedTakeOffArray;
		}

		public double[] getContinuedTakeOffSplineValues() {
			return continuedTakeOffSplineValues;
		}

		public void setContinuedTakeOffSplineValues(double[] continuedTakeOffSplineValues) {
			this.continuedTakeOffSplineValues = continuedTakeOffSplineValues;
		}

		public double[] getAbortedTakeOffSplineValues() {
			return abortedTakeOffSplineValues;
		}

		public void setAbortedTakeOffSplineValues(double[] abortedTakeOffSplineValues) {
			this.abortedTakeOffSplineValues = abortedTakeOffSplineValues;
		}

		public Amount<Duration> gettHold() {
			return tHold;
		}

		public void settHold(Amount<Duration> tHold) {
			this.tHold = tHold;
		}

		public Amount<Duration> gettEndHold() {
			return tEndHold;
		}

		public void settEndHold(Amount<Duration> tEndHold) {
			this.tEndHold = tEndHold;
		}

		public Amount<Duration> gettRot() {
			return tRot;
		}

		public void settRot(Amount<Duration> tRot) {
			this.tRot = tRot;
		}

		public Amount<Duration> gettEndRot() {
			return tEndRot;
		}

		public void settEndRot(Amount<Duration> tEndRot) {
			this.tEndRot = tEndRot;
		}

		public Amount<Duration> gettAlphaCost() {
			return tClimb;
		}

		public void settAlphaCost(Amount<Duration> tAlphaCost) {
			this.tClimb = tAlphaCost;
		}

		public double getKcLMax() {
			return kcLMax;
		}

		public void setKcLMax(double kcLMax) {
			this.kcLMax = kcLMax;
		}

		public Double getvFailure() {
			return vFailure;
		}

		public void setvFailure(Double vFailure) {
			this.vFailure = vFailure;
		}

		public Amount<Duration> gettClimb() {
			return tClimb;
		}

		public Amount<Duration> gettFaiulre() {
			return tFaiulre;
		}

		public void setkRot(double kRot) {
			this.kRot = kRot;
		}

		public void setkLO(double kLO) {
			this.kLO = kLO;
		}

		public void setPhi(double phi) {
			this.phi = phi;
		}

		public void setK1(double k1) {
			this.k1 = k1;
		}

		public void setK2(double k2) {
			this.k2 = k2;
		}

		public Amount<Duration> gettRec() {
			return tRec;
		}

		public void settRec(Amount<Duration> tRec) {
			this.tRec = tRec;
		}

		public boolean isAborted() {
			return isAborted;
		}

		public void setAborted(boolean isAborted) {
			this.isAborted = isAborted;
		}

		public List<Double> getAlphaDot() {
			return alphaDot;
		}

		public void setAlphaDot(List<Double> alphaDot) {
			this.alphaDot = alphaDot;
		}

		public List<Double> getGammaDot() {
			return gammaDot;
		}

		public void setGammaDot(List<Double> gammaDot) {
			this.gammaDot = gammaDot;
		}

		public List<Double> getcL() {
			return cL;
		}

		public void setcL(List<Double> cL) {
			this.cL = cL;
		}

		public List<Double> getcD() {
			return cD;
		}

		public void setcD(List<Double> cD) {
			this.cD = cD;
		}

		public List<Double> getLoadFactor() {
			return loadFactor;
		}

		public void setLoadFactor(List<Double> loadFactor) {
			this.loadFactor = loadFactor;
		}

		public List<Amount<Angle>> getAlpha() {
			return alpha;
		}

		public void setAlpha(List<Amount<Angle>> alpha) {
			this.alpha = alpha;
		}

		public List<Amount<Angle>> getTheta() {
			return theta;
		}

		public void setTheta(List<Amount<Angle>> theta) {
			this.theta = theta;
		}

		public List<Amount<Angle>> getGamma() {
			return gamma;
		}

		public void setGamma(List<Amount<Angle>> gamma) {
			this.gamma = gamma;
		}

		public List<Amount<Duration>> getTime() {
			return time;
		}

		public void setTime(List<Amount<Duration>> time) {
			this.time = time;
		}

		public List<Amount<Velocity>> getSpeed() {
			return speed;
		}

		public void setSpeed(List<Amount<Velocity>> speed) {
			this.speed = speed;
		}

		public List<Amount<Velocity>> getRateOfClimb() {
			return rateOfClimb;
		}

		public void setRateOfClimb(List<Amount<Velocity>> rateOfClimb) {
			this.rateOfClimb = rateOfClimb;
		}

		public List<Amount<Acceleration>> getAcceleration() {
			return acceleration;
		}

		public void setAcceleration(List<Amount<Acceleration>> acceleration) {
			this.acceleration = acceleration;
		}

		public List<Amount<Force>> getThrust() {
			return thrust;
		}

		public void setThrust(List<Amount<Force>> thrust) {
			this.thrust = thrust;
		}

		public List<Amount<Force>> getThrustHorizontal() {
			return thrustHorizontal;
		}

		public void setThrustHorizontal(List<Amount<Force>> thrustHorizontal) {
			this.thrustHorizontal = thrustHorizontal;
		}

		public List<Amount<Force>> getThrustVertical() {
			return thrustVertical;
		}

		public void setThrustVertical(List<Amount<Force>> thrustVertical) {
			this.thrustVertical = thrustVertical;
		}

		public List<Amount<Force>> getLift() {
			return lift;
		}

		public void setLift(List<Amount<Force>> lift) {
			this.lift = lift;
		}

		public List<Amount<Force>> getDrag() {
			return drag;
		}

		public void setDrag(List<Amount<Force>> drag) {
			this.drag = drag;
		}

		public List<Amount<Force>> getFriction() {
			return friction;
		}

		public void setFriction(List<Amount<Force>> friction) {
			this.friction = friction;
		}

		public List<Amount<Force>> getTotalForce() {
			return totalForce;
		}

		public void setTotalForce(List<Amount<Force>> totalForce) {
			this.totalForce = totalForce;
		}

		public List<Amount<Length>> getGroundDistance() {
			return groundDistance;
		}

		public void setGroundDistance(List<Amount<Length>> groundDistance) {
			this.groundDistance = groundDistance;
		}

		public List<Amount<Length>> getVerticalDistance() {
			return verticalDistance;
		}

		public void setVerticalDistance(List<Amount<Length>> verticalDistance) {
			this.verticalDistance = verticalDistance;
		}

		public double[] getFailureSpeedArrayFitted() {
			return failureSpeedArrayFitted;
		}

		public void setFailureSpeedArrayFitted(double[] failureSpeedArrayFitted) {
			this.failureSpeedArrayFitted = failureSpeedArrayFitted;
		}

		public void settClimb(Amount<Duration> tClimb) {
			this.tClimb = tClimb;
		}

		public void settFaiulre(Amount<Duration> tFaiulre) {
			this.tFaiulre = tFaiulre;
		}
	}
	//-------------------------------------------------------------------------------------
	//								END OUTER NESTED CLASS	
	//-------------------------------------------------------------------------------------

	public static InputTree getInput() {
		return input;
	}

	public static OutputTree getOutput() {
		return output;
	}

	public static void setInput(InputTree input) {
		TakeOffManager.input = input;
	}

	public static void setOutput(OutputTree output) {
		TakeOffManager.output = output;
	}
}
