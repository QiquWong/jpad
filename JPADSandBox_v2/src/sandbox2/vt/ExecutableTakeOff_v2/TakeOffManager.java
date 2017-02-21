package sandbox2.vt.ExecutableTakeOff_v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
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
import org.apache.commons.math3.ode.events.EventHandler.Action;
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

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.Aircraft;
import calculators.performance.TakeOffCalc;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.TakeOffResultsMap;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.PerformanceEnum;
import database.databasefunctions.engine.EngineDatabaseManager;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
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

	public static void executeStandAloneTakeOffCalculator(String outputFolder) throws InstantiationException, IllegalAccessException {
		
		String chartsFolderPath = JPADStaticWriteUtils.createNewFolder(
				outputFolder 
				+ "Charts"
				+ File.separator
				);
		
		TakeOffManager theTakeOffManager = new TakeOffManager();
		
		output = new OutputTree();
			
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// DEFINE THE TAKE-OFF MANAGER AS IN THE PREVIOUS VERSION, BUT USING THE NEW TAKE-OFF CALCULATOR ... //
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		
		TakeOffCalculator theTakeOffCalculator = theTakeOffManager.new TakeOffCalculator(
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
				alphaRed,
				mu,
				muBrake,
				obstacle
				);
				
		theTakeOffCalculator.calculateTakeOffDistanceODE(null, false);
		
		// Distances:
		output.setGroundRoll(theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(0).to(NonSI.FOOT));
		output.setRotation(theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(1).minus(output.getGroundRoll()).to(NonSI.FOOT));
		output.setAirborne(theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(2).minus(output.getRotation()).minus(output.getGroundRoll()).to(NonSI.FOOT));
		output.setTakeOffDistanceAOE(output.getGroundRoll().plus(output.getRotation()).plus(output.getAirborne()).to(NonSI.FOOT));
		output.setTakeOffDistanceFAR25(output.getTakeOffDistanceAOE().times(1.15).to(NonSI.FOOT));
		
		// Duration:
		output.setTakeOffDuration(theTakeOffCalculator.getTakeOffResults().getTime().get(2));
		
		// Velocities:
		output.setVsT0(theTakeOffCalculator.getvSTakeOff().to(NonSI.KNOT));
		output.setvRot(theTakeOffCalculator.getvRot().to(NonSI.KNOT));
		output.setvLO(theTakeOffCalculator.getvLO().to(NonSI.KNOT));
		output.setV2(theTakeOffCalculator.getV2().to(NonSI.KNOT));
		
		if(input.isCharts())
			theTakeOffCalculator.createTakeOffCharts(chartsFolderPath);
		
		if(input.isBalancedFieldLength()) {
			theTakeOffCalculator.calculateBalancedFieldLength();
			output.setBalancedFieldLength(theTakeOffCalculator.getBalancedFieldLength().to(NonSI.FOOT));
			output.setV1(theTakeOffCalculator.getV1().to(NonSI.KNOT));
		}
		
		if(input.isCharts())
			theTakeOffCalculator.createBalancedFieldLengthChart(chartsFolderPath);
		
		System.out.println(theTakeOffCalculator.toString());
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
	
	/********************************************************************************************
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
			JPADStaticWriteUtils.writeSingleNode("net_thrust_array_single_engine", Arrays.toString(input.getNetThrust()), engineDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("mach_array", Arrays.toString(input.getMachArray()), engineDataElement, doc);
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
	
	/********************************************************************************************
	 * This method creates and fills the XLS output file using the results form the OutputTree
	 * object
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param filenameWithPathAndExt
	 * @throws InvalidFormatException
	 * @throws IOException
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
		
		// Arrays always present:
		MyArray timeArray = new MyArray();
		timeArray.setAmountList(output.getTime());
		MyArray groundDistenceArray = new MyArray();
		groundDistenceArray.setAmountList(output.getGroundDistance());
		
		//---------------------------------------------------------------------------------------
		// ACCELERATION:
		Sheet sheetAcceleration = wb.createSheet("Acceleration");
		
		List<String> xlsAccelerationDescription = new ArrayList<String>();
		xlsAccelerationDescription.add("Time");
		xlsAccelerationDescription.add("Space");
		xlsAccelerationDescription.add("Acceleration");
		
		MyArray accelerationArray = new MyArray();
		accelerationArray.setAmountList(output.getAcceleration());
		
		List<MyArray> xlsAccelerationList = new ArrayList<MyArray>();
		xlsAccelerationList.add(timeArray);
		xlsAccelerationList.add(groundDistenceArray);
		xlsAccelerationList.add(accelerationArray);
		
		List<String> xlsAccelerationUnit = new ArrayList<String>();
		xlsAccelerationUnit.add("s");
		xlsAccelerationUnit.add("m");
		xlsAccelerationUnit.add("m/(s^2)");
		
		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetAcceleration,
				xlsAccelerationDescription,
				xlsAccelerationList,
				xlsAccelerationUnit
				);
		
		//---------------------------------------------------------------------------------------
		// TAKE OFF TRAJECTORY:
		Sheet sheetTrajectory = wb.createSheet("Take-off trajectory");
		
		List<String> xlsTrajectoryDescription = new ArrayList<String>();
		xlsTrajectoryDescription.add("Time");
		xlsTrajectoryDescription.add("Space");
		xlsTrajectoryDescription.add("Acceleration");
		
		MyArray trajectoryArray = new MyArray();
		trajectoryArray.setAmountList(output.getVerticalDistance());
		
		List<MyArray> xlsTrajectoryList = new ArrayList<MyArray>();
		xlsTrajectoryList.add(timeArray);
		xlsTrajectoryList.add(groundDistenceArray);
		xlsTrajectoryList.add(trajectoryArray);
		
		List<String> xlsTrajectoryUnit = new ArrayList<String>();
		xlsTrajectoryUnit.add("s");
		xlsTrajectoryUnit.add("m");
		xlsTrajectoryUnit.add("m");
		
		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetTrajectory,
				xlsTrajectoryDescription,
				xlsTrajectoryList,
				xlsTrajectoryUnit
				);
		
		//---------------------------------------------------------------------------------------
		// ANGLES:
		Sheet sheetAngles = wb.createSheet("Angles");

		List<String> xlsAnglesDescription = new ArrayList<String>();
		xlsAnglesDescription.add("Time");
		xlsAnglesDescription.add("Space");
		xlsAnglesDescription.add("Alpha");
		xlsAnglesDescription.add("Gamma");
		xlsAnglesDescription.add("Theta");

		MyArray alphaArray = new MyArray();
		alphaArray.setAmountList(output.getAlpha());
		MyArray gammaArray = new MyArray();
		gammaArray.setAmountList(output.getGamma());
		MyArray thetaArray = new MyArray();
		thetaArray.setAmountList(output.getTheta());

		List<MyArray> xlsAnglesList = new ArrayList<MyArray>();
		xlsAnglesList.add(timeArray);
		xlsAnglesList.add(groundDistenceArray);
		xlsAnglesList.add(alphaArray);
		xlsAnglesList.add(gammaArray);
		xlsAnglesList.add(thetaArray);

		List<String> xlsAnglesUnit = new ArrayList<String>();
		xlsAnglesUnit.add("s");
		xlsAnglesUnit.add("m");
		xlsAnglesUnit.add("deg");
		xlsAnglesUnit.add("deg");
		xlsAnglesUnit.add("deg");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetAngles,
				xlsAnglesDescription,
				xlsAnglesList,
				xlsAnglesUnit
				);

		//---------------------------------------------------------------------------------------
		// ANGULAR VELOCITIES:
		Sheet sheetAngularVelocities = wb.createSheet("Angular velocities");

		List<String> xlsAngularVelocitiesDescription = new ArrayList<String>();
		xlsAngularVelocitiesDescription.add("Time");
		xlsAngularVelocitiesDescription.add("Space");
		xlsAngularVelocitiesDescription.add("Alpha");
		xlsAngularVelocitiesDescription.add("Gamma");
		xlsAngularVelocitiesDescription.add("Theta");

		MyArray alphaDotArray = new MyArray();
		alphaDotArray.setList(output.getAlphaDot());
		MyArray gammaDotArray = new MyArray();
		gammaArray.setList(output.getGammaDot());

		List<MyArray> xlsAngularVelocitiesList = new ArrayList<MyArray>();
		xlsAngularVelocitiesList.add(timeArray);
		xlsAngularVelocitiesList.add(groundDistenceArray);
		xlsAngularVelocitiesList.add(alphaDotArray);
		xlsAngularVelocitiesList.add(gammaDotArray);

		List<String> xlsAngularVelocitiesUnit = new ArrayList<String>();
		xlsAngularVelocitiesUnit.add("s");
		xlsAngularVelocitiesUnit.add("m");
		xlsAngularVelocitiesUnit.add("deg/s");
		xlsAngularVelocitiesUnit.add("deg/s");
		xlsAngularVelocitiesUnit.add("deg/s");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetAngularVelocities,
				xlsAngularVelocitiesDescription,
				xlsAngularVelocitiesList,
				xlsAngularVelocitiesUnit
				);

		//---------------------------------------------------------------------------------------
		// CL:
		Sheet sheetCL = wb.createSheet("CL");

		List<String> xlsCLDescription = new ArrayList<String>();
		xlsCLDescription.add("Time");
		xlsCLDescription.add("Space");
		xlsCLDescription.add("CL");

		MyArray cLArray = new MyArray();
		cLArray.setList(output.getcL());

		List<MyArray> xlsCLList = new ArrayList<MyArray>();
		xlsCLList.add(timeArray);
		xlsCLList.add(groundDistenceArray);
		xlsCLList.add(cLArray);

		List<String> xlsCLUnit = new ArrayList<String>();
		xlsCLUnit.add("s");
		xlsCLUnit.add("m");
		xlsCLUnit.add(" ");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetCL,
				xlsCLDescription,
				xlsCLList,
				xlsCLUnit
				);
		
		//---------------------------------------------------------------------------------------
		// CD:
		Sheet sheetCD = wb.createSheet("CD");

		List<String> xlsCDDescription = new ArrayList<String>();
		xlsCDDescription.add("Time");
		xlsCDDescription.add("Space");
		xlsCDDescription.add("CD");

		MyArray cDArray = new MyArray();
		cDArray.setList(output.getcD());

		List<MyArray> xlsCDList = new ArrayList<MyArray>();
		xlsCDList.add(timeArray);
		xlsCDList.add(groundDistenceArray);
		xlsCDList.add(cDArray);

		List<String> xlsCDUnit = new ArrayList<String>();
		xlsCDUnit.add("s");
		xlsCDUnit.add("m");
		xlsCDUnit.add(" ");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetCD,
				xlsCDDescription,
				xlsCDList,
				xlsCDUnit
				);

		//---------------------------------------------------------------------------------------
		// LOAD FACTOR:
		Sheet sheetLoadFactor = wb.createSheet("Load factor");

		List<String> xlsLoadFactorDescription = new ArrayList<String>();
		xlsLoadFactorDescription.add("Time");
		xlsLoadFactorDescription.add("Space");
		xlsLoadFactorDescription.add("Load factor");

		MyArray loadFactorArray = new MyArray();
		loadFactorArray.setList(output.getLoadFactor());

		List<MyArray> xlsLoadFactorList = new ArrayList<MyArray>();
		xlsLoadFactorList.add(timeArray);
		xlsLoadFactorList.add(groundDistenceArray);
		xlsLoadFactorList.add(loadFactorArray);

		List<String> xlsLoadFactorUnit = new ArrayList<String>();
		xlsLoadFactorUnit.add("s");
		xlsLoadFactorUnit.add("m");
		xlsLoadFactorUnit.add(" ");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetLoadFactor,
				xlsLoadFactorDescription,
				xlsLoadFactorList,
				xlsLoadFactorUnit
				);

		//---------------------------------------------------------------------------------------
		// RATE OF CLIMB:
		Sheet sheetRateOfClimb = wb.createSheet("Rate of climb");

		List<String> xlsRateOfClimbDescription = new ArrayList<String>();
		xlsRateOfClimbDescription.add("Time");
		xlsRateOfClimbDescription.add("Space");
		xlsRateOfClimbDescription.add("Rate of climb");

		MyArray rateOfClimbArray = new MyArray();
		rateOfClimbArray.setAmountList(output.getRateOfClimb());

		List<MyArray> xlsRateOfClimbList = new ArrayList<MyArray>();
		xlsRateOfClimbList.add(timeArray);
		xlsRateOfClimbList.add(groundDistenceArray);
		xlsRateOfClimbList.add(rateOfClimbArray);

		List<String> xlsRateOfClimbUnit = new ArrayList<String>();
		xlsRateOfClimbUnit.add("s");
		xlsRateOfClimbUnit.add("m");
		xlsRateOfClimbUnit.add("m/s");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetRateOfClimb,
				xlsRateOfClimbDescription,
				xlsRateOfClimbList,
				xlsRateOfClimbUnit
				);
		
		//---------------------------------------------------------------------------------------
		// SPEED:
		Sheet sheetSpeed = wb.createSheet("Speed");

		List<String> xlsSpeedDescription = new ArrayList<String>();
		xlsSpeedDescription.add("Time");
		xlsSpeedDescription.add("Space");
		xlsSpeedDescription.add("Speed");

		MyArray speedArray = new MyArray();
		speedArray.setAmountList(output.getSpeed());

		List<MyArray> xlsSpeedList = new ArrayList<MyArray>();
		xlsSpeedList.add(timeArray);
		xlsSpeedList.add(groundDistenceArray);
		xlsSpeedList.add(speedArray);

		List<String> xlsSpeedUnit = new ArrayList<String>();
		xlsSpeedUnit.add("s");
		xlsSpeedUnit.add("m");
		xlsSpeedUnit.add("m/s");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetSpeed,
				xlsSpeedDescription,
				xlsSpeedList,
				xlsSpeedUnit
				);

		//---------------------------------------------------------------------------------------
		// HORIZONTAL FORCES:
		Sheet sheetHorizontalForces = wb.createSheet("Horizontal forces");

		List<String> xlsHorizontalForcesDescription = new ArrayList<String>();
		xlsHorizontalForcesDescription.add("Time");
		xlsHorizontalForcesDescription.add("Space");
		xlsHorizontalForcesDescription.add("Total force");
		xlsHorizontalForcesDescription.add("T*cos(alpha)");
		xlsHorizontalForcesDescription.add("Drag");
		xlsHorizontalForcesDescription.add("Friction");
		xlsHorizontalForcesDescription.add("W*sin(gamma)");

		MyArray totalForceArray = new MyArray();
		totalForceArray.setAmountList(output.getTotalForce());
		MyArray thrustArray = new MyArray();
		thrustArray.setAmountList(output.getThrustHorizontal());
		MyArray dragArray = new MyArray();
		dragArray.setAmountList(output.getDrag());
		MyArray frictionArray = new MyArray();
		frictionArray.setAmountList(output.getFriction());
		MyArray weightHorizontalArray = new MyArray();
		weightHorizontalArray.setAmountList(output.getGamma());
		weightHorizontalArray.times((input.getTakeOffMass().times(AtmosphereCalc.g0).getEstimatedValue()));
		
		List<MyArray> xlsHorizontalForcesList = new ArrayList<MyArray>();
		xlsHorizontalForcesList.add(timeArray);
		xlsHorizontalForcesList.add(groundDistenceArray);
		xlsHorizontalForcesList.add(totalForceArray);
		xlsHorizontalForcesList.add(thrustArray);
		xlsHorizontalForcesList.add(dragArray);
		xlsHorizontalForcesList.add(frictionArray);
		xlsHorizontalForcesList.add(weightHorizontalArray);

		List<String> xlsHorizontalForcesUnit = new ArrayList<String>();
		xlsHorizontalForcesUnit.add("s");
		xlsHorizontalForcesUnit.add("m");
		xlsHorizontalForcesUnit.add("N");
		xlsHorizontalForcesUnit.add("N");
		xlsHorizontalForcesUnit.add("N");
		xlsHorizontalForcesUnit.add("N");
		xlsHorizontalForcesUnit.add("N");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetHorizontalForces,
				xlsHorizontalForcesDescription,
				xlsHorizontalForcesList,
				xlsHorizontalForcesUnit
				);

		//---------------------------------------------------------------------------------------
		// VERTICAL FORCES:
		Sheet sheetVerticalForces = wb.createSheet("Vertical forces");

		List<String> xlsVerticalForcesDescription = new ArrayList<String>();
		xlsVerticalForcesDescription.add("Time");
		xlsVerticalForcesDescription.add("Space");
		xlsVerticalForcesDescription.add("Lift");
		xlsVerticalForcesDescription.add("T*sin(alpha)");
		xlsVerticalForcesDescription.add("W*cos(gamma)");

		MyArray liftArray = new MyArray();
		liftArray.setAmountList(output.getLift());
		MyArray thrustVerticalArray = new MyArray();
		thrustVerticalArray.setAmountList(output.getThrustVertical());
		MyArray weightVerticalArray = new MyArray();
		weightVerticalArray.setAmountList(output.getGamma());
		weightVerticalArray.times((input.getTakeOffMass().times(AtmosphereCalc.g0).getEstimatedValue()));

		List<MyArray> xlsVerticalForcesList = new ArrayList<MyArray>();
		xlsVerticalForcesList.add(timeArray);
		xlsVerticalForcesList.add(groundDistenceArray);
		xlsVerticalForcesList.add(liftArray);
		xlsVerticalForcesList.add(thrustVerticalArray);
		xlsVerticalForcesList.add(weightVerticalArray);

		List<String> xlsVerticalForcesUnit = new ArrayList<String>();
		xlsVerticalForcesUnit.add("s");
		xlsVerticalForcesUnit.add("m");
		xlsVerticalForcesUnit.add("N");
		xlsVerticalForcesUnit.add("N");
		xlsVerticalForcesUnit.add("N");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetVerticalForces,
				xlsVerticalForcesDescription,
				xlsVerticalForcesList,
				xlsVerticalForcesUnit
				);

		//---------------------------------------------------------------------------------------
		// BALANCED FIELD LENGTH:
		Sheet sheetBalancedFieldLength = wb.createSheet("Balanced field length");

		List<String> xlsBalancedFieldLengthDescription = new ArrayList<String>();
		xlsBalancedFieldLengthDescription.add("Speed");
		xlsBalancedFieldLengthDescription.add("Take-off OEI");
		xlsBalancedFieldLengthDescription.add("Aborted take-off");

		MyArray failureSpeedArray = new MyArray(output.getFailureSpeedArray());
		MyArray takeOffOEIArray = new MyArray(output.getContinuedTakeOffArray());
		MyArray abortedTakeOffArray = new MyArray(output.getAbortedTakeOffArray());
		
		List<MyArray> xlsBalancedFieldLengthList = new ArrayList<MyArray>();
		xlsBalancedFieldLengthList.add(failureSpeedArray);
		xlsBalancedFieldLengthList.add(takeOffOEIArray);
		xlsBalancedFieldLengthList.add(abortedTakeOffArray);

		List<String> xlsBalancedFieldLengthUnit = new ArrayList<String>();
		xlsBalancedFieldLengthUnit.add("m/s");
		xlsBalancedFieldLengthUnit.add("m");
		xlsBalancedFieldLengthUnit.add("m");

		JPADStaticWriteUtils.writeAllArraysToXls(
				sheetBalancedFieldLength,
				xlsBalancedFieldLengthDescription,
				xlsBalancedFieldLengthList,
				xlsBalancedFieldLengthUnit
				);
		
		//---------------------------------------------------------------------------------------
		// OUTPUT FILE CREATION:
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
	}
	
	public class TakeOffCalculator {

		//-------------------------------------------------------------------------------------
		// VARIABLE DECLARATION

		private Amount<Duration> dtRot, dtHold,	
		dtRec = Amount.valueOf(3, SI.SECOND),
		tHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tEndHold = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tRot = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
		tEndRot = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tClimb = Amount.valueOf(10000.0, SI.SECOND),  // initialization to an impossible time
		tFaiulre = Amount.valueOf(10000.0, SI.SECOND), // initialization to an impossible time
		tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
		private Amount<Mass> maxTakeOffMass; 
		private Amount<Velocity> vSTakeOff, vRot, vLO, vWind, v1, v2;
		private Amount<Length> altitude, wingToGroundDistance, obstacle, balancedFieldLength;
		private Amount<Angle> alphaGround, iw;
		private List<Double> alphaDot, gammaDot, cL, cD, loadFactor, sfc;
		private List<Amount<Angle>> alpha, theta, gamma;
		private List<Amount<Duration>> time;
		private List<Amount<Velocity>> speed, rateOfClimb;
		private List<Amount<Acceleration>> acceleration;
		private List<Amount<Force>> thrust, thrustHorizontal, thrustVertical, lift, drag, friction, totalForce;
		private List<Amount<Length>> groundDistance, verticalDistance;
		private double kAlphaDot, kcLMax, kRot, phi, cLmaxTO, kGround, alphaDotInitial, 
		alphaRed, cL0, cLground, kFailure;
		private Double vFailure;
		private boolean isAborted;
		
		private double cLalphaFlap;

		// Statistics to be collected at every phase: (initialization of the lists through the builder
		private TakeOffResultsMap takeOffResults = new TakeOffResultsMap();
		// Interpolated function for balanced field length calculation
		MyInterpolatingFunction continuedTakeOffFitted = new MyInterpolatingFunction();
		MyInterpolatingFunction abortedTakeOffFitted = new MyInterpolatingFunction();
		MyInterpolatingFunction mu;
		MyInterpolatingFunction muBrake;
		MyInterpolatingFunction groundIdlePhi;
		
		// integration index
		private double[] failureSpeedArray, continuedTakeOffArray, abortedTakeOffArray,
		failureSpeedArrayFitted, continuedTakeOffArrayFitted, abortedTakeOffArrayFitted;

		//-------------------------------------------------------------------------------------
		// BUILDER:
		
		/**
		 * This builder is an overload of the previous one designed to allow the user 
		 * to perform the take-off distance calculation without doing all flaps analysis.
		 * This may come in handy when only few data are available.
		 */
		@SuppressWarnings("unchecked")
		public TakeOffCalculator() {
			
			// Required data
			this.altitude = input.getAltitude();
			this.maxTakeOffMass = input.getTakeOffMass();
			this.dtRot = input.getDtRotation();
			this.dtHold = input.getDtHold();
			this.kcLMax = input.getkCLmax();
			this.kRot = input.getkRotation();
			this.alphaDotInitial = input.getAlphaDotRotation().to(MyUnits.DEG_PER_SECOND).getEstimatedValue();
			this.kFailure = input.getDragDueToEnigneFailure();
			this.groundIdlePhi = input.getThrottleGroundIdle();
			this.phi = 1.0;
			this.kAlphaDot = input.getkAlphaDot().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			this.mu = input.getMuFunction();
			this.muBrake = input.getMuBrakeFunction();
			this.wingToGroundDistance = input.getWingToGroundDistance();
			this.obstacle = input.getObstacleTakeOff();
			this.vWind = input.getvWind();
			this.alphaGround = input.getAlphaGround();
			this.iw = input.getIw();
			this.cLmaxTO = input.getcLmaxTO();
			this.cLalphaFlap = input.getcLalphaFlap().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
			this.cL0 = input.getcL0TO();
			this.cLground = cL0 + (cLalphaFlap*iw.getEstimatedValue());
			
			// Reference velocities definition
			vSTakeOff = Amount.valueOf(
					SpeedCalc.calculateSpeedStall(
							getAltitude().doubleValue(SI.METER),
							maxTakeOffMass.to(SI.KILOGRAM).times(AtmosphereCalc.g0).getEstimatedValue(),
							input.getWingSurface().doubleValue(SI.SQUARE_METRE),
							cLmaxTO
							),
					SI.METERS_PER_SECOND);
			vRot = vSTakeOff.times(kRot);
			
			System.out.println("\n-----------------------------------------------------------");
			System.out.println("CLmaxTO = " + cLmaxTO);
			System.out.println("CL0 = " + cL0);
			System.out.println("CLground = " + cLground);
			System.out.println("VsTO = " + vSTakeOff);
			System.out.println("VRot = " + vRot);
			System.out.println("-----------------------------------------------------------\n");

			// McCormick interpolated function --> See the excel file into JPAD DOCS
			Amount<Length> wingSpan = Amount.valueOf(
					Math.sqrt(
							input.getWingSurface().to(SI.SQUARE_METRE)
							.times(input.getAspectRatio())
							.getEstimatedValue()
							),
					SI.METER
					);
			double hb = wingToGroundDistance.divide(wingSpan.to(SI.METER).times(Math.PI/4)).getEstimatedValue();
			kGround = - 622.44*(Math.pow(hb, 5)) + 624.46*(Math.pow(hb, 4)) - 255.24*(Math.pow(hb, 3))
					+ 47.105*(Math.pow(hb, 2)) - 0.6378*hb + 0.0055;
			
			// List initialization
			this.time = new ArrayList<Amount<Duration>>();
			this.speed = new ArrayList<Amount<Velocity>>();
			this.thrust = new ArrayList<Amount<Force>>();
			this.thrustHorizontal = new ArrayList<Amount<Force>>();
			this.thrustVertical = new ArrayList<Amount<Force>>();
			this.alpha = new ArrayList<Amount<Angle>>();
			this.alphaDot = new ArrayList<Double>();
			this.gamma = new ArrayList<Amount<Angle>>();
			this.gammaDot = new ArrayList<Double>();
			this.theta = new ArrayList<Amount<Angle>>();
			this.cL = new ArrayList<Double>();
			this.lift = new ArrayList<Amount<Force>>();
			this.loadFactor = new ArrayList<Double>();
			this.cD = new ArrayList<Double>();
			this.drag = new ArrayList<Amount<Force>>();
			this.friction = new ArrayList<Amount<Force>>();
			this.totalForce = new ArrayList<Amount<Force>>();
			this.acceleration = new ArrayList<Amount<Acceleration>>();
			this.rateOfClimb = new ArrayList<Amount<Velocity>>();
			this.groundDistance = new ArrayList<Amount<Length>>();
			this.verticalDistance = new ArrayList<Amount<Length>>();
			this.sfc = new ArrayList<Double>();
			
			takeOffResults.initialize();
		}
		//-------------------------------------------------------------------------------------
		// METHODS:

		/**************************************************************************************
		 * This method is used to initialize all lists in order to perform a new calculation or
		 * to setup the first one
		 *
		 * @author Vittorio Trifari
		 */
		public void initialize() {

			// lists cleaning
			time.clear();
			speed.clear();
			thrust.clear();
			thrustHorizontal.clear();
			thrustVertical.clear();
			alpha.clear();
			alphaDot.clear();
			gamma.clear();
			gammaDot.clear();
			theta.clear();
			cL.clear();
			lift.clear();
			loadFactor.clear();
			cD.clear();
			drag.clear();
			friction.clear();
			totalForce.clear();
			acceleration.clear();
			rateOfClimb.clear();
			groundDistance.clear();
			verticalDistance.clear();
			sfc.clear();
			
			tHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tEndHold = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tRot = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
			tEndRot = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tClimb = Amount.valueOf(10000.0, SI.SECOND);  // initialization to an impossible time
			tFaiulre = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			tRec = Amount.valueOf(10000.0, SI.SECOND); // initialization to an impossible time
			
			vFailure = null;
			isAborted = false;
			
			takeOffResults.initialize();
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

			int i=0;
			double newAlphaRed = 0.0;
			alphaRed = 0.0;
			
			v2 = Amount.valueOf(10000.0, SI.METERS_PER_SECOND); // initialization to an impossible speed
			
			while (Math.abs(((v2.divide(vSTakeOff).getEstimatedValue()) - 1.2)) >= 0.009) {

				if(i >= 1) {
					if(newAlphaRed <= 0.0)
						alphaRed = newAlphaRed;
					else
						break;
				}
				
				initialize();
				
				this.isAborted = isAborted;
				// failure check
				if(vFailure == null)
					this.vFailure = 10000.0; // speed impossible to reach --> no failure!!
				else
					this.vFailure = vFailure;

				FirstOrderIntegrator theIntegrator = new HighamHall54Integrator(
						1e-8,
						1,
						1e-16,
						1e-16
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
							return x[1] - TakeOffCalculator.this.vFailure;
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

						if(t < tRec.getEstimatedValue()) {
							return speed - vRot.getEstimatedValue();
						}
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
						takeOffResults.collectResults(
								time.get(time.size()-1),
								thrust.get(thrust.size()-1),
								thrustHorizontal.get(thrustHorizontal.size()-1),
								thrustVertical.get(thrustVertical.size()-1),
								friction.get(friction.size()-1),
								lift.get(lift.size()-1),
								drag.get(drag.size()-1),
								totalForce.get(totalForce.size()-1),
								loadFactor.get(loadFactor.size()-1),
								speed.get(speed.size()-1),
								rateOfClimb.get(rateOfClimb.size()-1),
								acceleration.get(acceleration.size()-1),
								groundDistance.get(groundDistance.size()-1),
								verticalDistance.get(verticalDistance.size()-1),
								alpha.get(alpha.size()-1),
								alphaDot.get(alphaDot.size()-1),
								gamma.get(gamma.size()-1),
								gammaDot.get(gammaDot.size()-1),
								theta.get(theta.size()-1),
								cL.get(cL.size()-1),
								cD.get(cD.size()-1)
								);
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
						takeOffResults.collectResults(
								time.get(time.size()-1),
								thrust.get(thrust.size()-1),
								thrustHorizontal.get(thrustHorizontal.size()-1),
								thrustVertical.get(thrustVertical.size()-1),
								friction.get(friction.size()-1),
								lift.get(lift.size()-1),
								drag.get(drag.size()-1),
								totalForce.get(totalForce.size()-1),
								loadFactor.get(loadFactor.size()-1),
								speed.get(speed.size()-1),
								rateOfClimb.get(rateOfClimb.size()-1),
								acceleration.get(acceleration.size()-1),
								groundDistance.get(groundDistance.size()-1),
								verticalDistance.get(verticalDistance.size()-1),
								alpha.get(alpha.size()-1),
								alphaDot.get(alphaDot.size()-1),
								gamma.get(gamma.size()-1),
								gammaDot.get(gammaDot.size()-1),
								theta.get(theta.size()-1),
								cL.get(cL.size()-1),
								cD.get(cD.size()-1)
								);
						
						v2 = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
						System.out.println("V2/VsTO = " + v2.divide(vSTakeOff));
						
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
					theIntegrator.addEventHandler(ehCheckObstacle, 1.0, 1e-7, 50);
				}
				else {
					theIntegrator.addEventHandler(ehCheckVRot, 1.0, 1e-3, 20);
					theIntegrator.addEventHandler(ehCheckFailure, 1.0, 1e-3, 20);
					theIntegrator.addEventHandler(ehCheckBrakes, 1.0, 1e-3, 20);
					theIntegrator.addEventHandler(ehCheckStop, 1.0, 1e-7, 50);
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
									(TakeOffCalculator.this.getLoadFactor().get(TakeOffCalculator.this.getLoadFactor().size()-1) > 1) &&
									(TakeOffCalculator.this.getLoadFactor().get(TakeOffCalculator.this.getLoadFactor().size()-2) < 1)) {
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
								takeOffResults.collectResults(
										time.get(time.size()-1),
										thrust.get(thrust.size()-1),
										thrustHorizontal.get(thrustHorizontal.size()-1),
										thrustVertical.get(thrustVertical.size()-1),
										friction.get(friction.size()-1),
										lift.get(lift.size()-1),
										drag.get(drag.size()-1),
										totalForce.get(totalForce.size()-1),
										loadFactor.get(loadFactor.size()-1),
										speed.get(speed.size()-1),
										rateOfClimb.get(rateOfClimb.size()-1),
										acceleration.get(acceleration.size()-1),
										groundDistance.get(groundDistance.size()-1),
										verticalDistance.get(verticalDistance.size()-1),
										alpha.get(alpha.size()-1),
										alphaDot.get(alphaDot.size()-1),
										gamma.get(gamma.size()-1),
										gammaDot.get(gammaDot.size()-1),
										theta.get(theta.size()-1),
										cL.get(cL.size()-1),
										cD.get(cD.size()-1)
										);
								System.out.println("\n---------------------------DONE!-------------------------------");

								tEndRot = Amount.valueOf(t, SI.SECOND);
								vLO = Amount.valueOf(x[1], SI.METERS_PER_SECOND);
							}
							// CHECK IF THE THRESHOLD CL IS REACHED --> FROM THIS POINT ON THE BAR IS LOCKED
							if((t > tEndRot.getEstimatedValue()) && 
									(TakeOffCalculator.this.getcL().get(TakeOffCalculator.this.getcL().size()-1) > kcLMax*cLmaxTO) &&
									((TakeOffCalculator.this.getcL().get(TakeOffCalculator.this.getcL().size()-2) < kcLMax*cLmaxTO))) {
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
									(TakeOffCalculator.this.getLoadFactor().get(TakeOffCalculator.this.getLoadFactor().size()-1) < 1) &&
									(TakeOffCalculator.this.getLoadFactor().get(TakeOffCalculator.this.getLoadFactor().size()-2) > 1)) {
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
						TakeOffCalculator.this.getTime().add(Amount.valueOf(t, SI.SECOND));
						//----------------------------------------------------------------------------------------
						// SPEED:
						TakeOffCalculator.this.getSpeed().add(Amount.valueOf(x[1], SI.METERS_PER_SECOND));
						//----------------------------------------------------------------------------------------
						// THRUST:
						TakeOffCalculator.this.getThrust().add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t),
								SI.NEWTON)
								);
						//----------------------------------------------------------------------------------------
						// THRUST HORIZONTAL:
						TakeOffCalculator.this.getThrustHorizontal().add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.cos(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										),
								SI.NEWTON)
								);
						//----------------------------------------------------------------------------------------
						// THRUST VERTICAL:
						TakeOffCalculator.this.getThrustVertical().add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).thrust(x[1], x[2], t)*Math.sin(
										Amount.valueOf(
												((DynamicsEquationsTakeOff)ode).alpha,
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()
										),
								SI.NEWTON)
								);

						//--------------------------------------------------------------------------------
						// FRICTION:
						if(!isAborted) {
							if(t < tEndRot.getEstimatedValue())
								TakeOffCalculator.this.getFriction().add(Amount.valueOf(
										((DynamicsEquationsTakeOff)ode).mu(x[1])
										*(((DynamicsEquationsTakeOff)ode).weight
												- ((DynamicsEquationsTakeOff)ode).lift(
														x[1],
														((DynamicsEquationsTakeOff)ode).alpha,
														x[2],
														t)),
										SI.NEWTON)
										);
							else
								TakeOffCalculator.this.getFriction().add(Amount.valueOf(0.0, SI.NEWTON));
						}
						else {
							if(t < tRec.getEstimatedValue())
								TakeOffCalculator.this.getFriction().add(Amount.valueOf(
										((DynamicsEquationsTakeOff)ode).mu(x[1])
										*(((DynamicsEquationsTakeOff)ode).weight
												- ((DynamicsEquationsTakeOff)ode).lift(
														x[1],
														((DynamicsEquationsTakeOff)ode).alpha,
														x[2],
														t)),
										SI.NEWTON)
										);
							else
								TakeOffCalculator.this.getFriction().add(Amount.valueOf(
										((DynamicsEquationsTakeOff)ode).muBrake(x[1])
										*(((DynamicsEquationsTakeOff)ode).weight
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
						TakeOffCalculator.this.getLift().add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).lift(
										x[1],
										((DynamicsEquationsTakeOff)ode).alpha,
										x[2],
										t),
								SI.NEWTON)
								);
						//----------------------------------------------------------------------------------------
						// DRAG:
						TakeOffCalculator.this.getDrag().add(Amount.valueOf(
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
							TakeOffCalculator.this.getTotalForce().add(Amount.valueOf(
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
									- ((DynamicsEquationsTakeOff)ode).mu(x[1])
									*(((DynamicsEquationsTakeOff)ode).weight
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
								TakeOffCalculator.this.getTotalForce().add(Amount.valueOf(
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
										- ((DynamicsEquationsTakeOff)ode).mu(x[1])
										*(((DynamicsEquationsTakeOff)ode).weight
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
								TakeOffCalculator.this.getTotalForce().add(Amount.valueOf(
										- ((DynamicsEquationsTakeOff)ode).drag(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t)
										- ((DynamicsEquationsTakeOff)ode).muBrake(x[1])
										*(((DynamicsEquationsTakeOff)ode).weight
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
						TakeOffCalculator.this.getLoadFactor().add(
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
						TakeOffCalculator.this.getRateOfClimb().add(Amount.valueOf(
								x[1]*Math.sin(
										Amount.valueOf(
												x[2],
												NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()),
								SI.METERS_PER_SECOND)
								);
						//----------------------------------------------------------------------------------------
						// ACCELERATION:
						if(!isAborted)
							TakeOffCalculator.this.getAcceleration().add(Amount.valueOf(
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
											- ((DynamicsEquationsTakeOff)ode).mu(x[1])
											*(((DynamicsEquationsTakeOff)ode).weight
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
								TakeOffCalculator.this.getAcceleration().add(Amount.valueOf(
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
												- ((DynamicsEquationsTakeOff)ode).mu(x[1])
												*(((DynamicsEquationsTakeOff)ode).weight
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
								TakeOffCalculator.this.getAcceleration().add(Amount.valueOf(
										(AtmosphereCalc.g0.getEstimatedValue()/((DynamicsEquationsTakeOff)ode).weight)
										*(- ((DynamicsEquationsTakeOff)ode).drag(
												x[1],
												((DynamicsEquationsTakeOff)ode).alpha,
												x[2],
												t)
												- ((DynamicsEquationsTakeOff)ode).muBrake(x[1])
												*(((DynamicsEquationsTakeOff)ode).weight
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
						TakeOffCalculator.this.getGroundDistance().add(Amount.valueOf(
								x[0],
								SI.METER)
								);
						//----------------------------------------------------------------------------------------
						// VERTICAL DISTANCE:
						TakeOffCalculator.this.getVerticalDistance().add(Amount.valueOf(
								x[3],
								SI.METER)
								);
						//----------------------------------------------------------------------------------------
						// ALPHA:
						TakeOffCalculator.this.getAlpha().add(Amount.valueOf(
								((DynamicsEquationsTakeOff)ode).alpha,
								NonSI.DEGREE_ANGLE)
								);
						//----------------------------------------------------------------------------------------
						// GAMMA:
						TakeOffCalculator.this.getGamma().add(Amount.valueOf(
								x[2],
								NonSI.DEGREE_ANGLE)
								);
						//----------------------------------------------------------------------------------------
						// ALPHA DOT:
						TakeOffCalculator.this.getAlphaDot().add(
								((DynamicsEquationsTakeOff)ode).alphaDot(t)
								); 
						//----------------------------------------------------------------------------------------
						// GAMMA DOT:
						if(t <= tEndRot.getEstimatedValue())
							TakeOffCalculator.this.getGammaDot().add(0.0);
						else
							TakeOffCalculator.this.getGammaDot().add(57.3*(AtmosphereCalc.g0.getEstimatedValue()/
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
						TakeOffCalculator.this.getTheta().add(Amount.valueOf(
								x[2] + ((DynamicsEquationsTakeOff)ode).alpha,
								NonSI.DEGREE_ANGLE)
								);
						//----------------------------------------------------------------------------------------
						// CL:				
						TakeOffCalculator.this.getcL().add(
								((DynamicsEquationsTakeOff)ode).cL(
										x[1],
										((DynamicsEquationsTakeOff)ode).alpha,
										x[2],
										t
										)
								);
						//----------------------------------------------------------------------------------------
						// CD:
						TakeOffCalculator.this.getcD().add(
								((DynamicsEquationsTakeOff)ode).cD(
										TakeOffCalculator.this.getcL().get(
												TakeOffCalculator.this.getcL().size()-1)
										)
								);
						//----------------------------------------------------------------------------------------
					}
				};
				theIntegrator.addStepHandler(stepHandler);

				double[] xAt0 = new double[] {0.0, 0.0, 0.0, 0.0}; // initial state
				theIntegrator.integrate(ode, 0.0, xAt0, 100, xAt0); // now xAt0 contains final state

				theIntegrator.clearEventHandlers();
				theIntegrator.clearStepHandlers();

				if(isAborted) {
						break;
				}
				
				//--------------------------------------------------------------------------------
				// NEW ALPHA REDUCTION RATE 
				if(((v2.divide(vSTakeOff).getEstimatedValue()) - 1.2) >= 0.0)
					newAlphaRed = alphaRed + 0.1;
				else
					newAlphaRed = alphaRed - 0.1;
				
				i++;
			}
			
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

			// failure speed array
			failureSpeedArray = MyArrayUtils.linspace(
					vSTakeOff.times(0.5).getEstimatedValue(),
					vRot.getEstimatedValue(),
					4);
			// continued take-off array
			continuedTakeOffArray = new double[failureSpeedArray.length]; 
			// aborted take-off array
			abortedTakeOffArray = new double[failureSpeedArray.length];

			// iterative take-off distance calculation for both conditions
			for(int i=0; i<failureSpeedArray.length; i++) {
				calculateTakeOffDistanceODE(failureSpeedArray[i], false);
				continuedTakeOffArray[i] = getGroundDistance().get(groundDistance.size()-1).getEstimatedValue();
				calculateTakeOffDistanceODE(failureSpeedArray[i], true);
				abortedTakeOffArray[i] = getGroundDistance().get(groundDistance.size()-1).getEstimatedValue();
			}

			MyInterpolatingFunction continuedTakeOffFunction = new MyInterpolatingFunction();
			MyInterpolatingFunction abortedTakeOffFunction = new MyInterpolatingFunction();
			
			continuedTakeOffFunction.interpolateLinear(failureSpeedArray, continuedTakeOffArray);
			abortedTakeOffFunction.interpolateLinear(failureSpeedArray, abortedTakeOffArray);
			
			failureSpeedArrayFitted = MyArrayUtils.linspace(
					vSTakeOff.times(0.5).getEstimatedValue(),
					vRot.getEstimatedValue(),
					1000);
			continuedTakeOffArrayFitted = new double[failureSpeedArrayFitted.length];
			abortedTakeOffArrayFitted = new double[failureSpeedArrayFitted.length];
			
			for(int i=0; i<failureSpeedArrayFitted.length; i++) {
				
				continuedTakeOffArrayFitted[i] = continuedTakeOffFunction.value(failureSpeedArrayFitted[i]);
				abortedTakeOffArrayFitted[i] = abortedTakeOffFunction.value(failureSpeedArrayFitted[i]);
				
			}
			
			// arrays intersection
			double[] intersection = MyArrayUtils.intersectArraysSimple(
					continuedTakeOffArrayFitted,
					abortedTakeOffArrayFitted);
			for(int i=0; i<intersection.length; i++)
				if(intersection[i] != 0.0) {
					balancedFieldLength = Amount.valueOf(intersection[i], SI.METER);
					v1 = Amount.valueOf(failureSpeedArrayFitted[i], SI.METERS_PER_SECOND);
				}
			
		}

		/**************************************************************************************
		 * This method allows users to plot all take-off performance producing several output charts
		 * which have time or ground distance as independent variables.
		 *
		 * @author Vittorio Trifari
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		public void createTakeOffCharts(String takeOffFolderPath) throws InstantiationException, IllegalAccessException {

			System.out.println("\n---------WRITING TAKE-OFF PERFORMANCE CHARTS TO FILE-----------");

			if(!isAborted) {
				//.................................................................................
				// take-off trajectory
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
						MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistance),
						0.0, null, 0.0, null,
						"Ground Distance", "Altitude", "m", "m",
						takeOffFolderPath, "TakeOff_Trajectory_SI)");
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								groundDistance.stream()
								.map(x -> x.to(NonSI.FOOT))
								.collect(Collectors.toList())
								), 
						MyArrayUtils.convertListOfAmountTodoubleArray(
								verticalDistance.stream()
								.map(x -> x.to(NonSI.FOOT))
								.collect(Collectors.toList())
								),
						0.0, null, 0.0, null,
						"Ground Distance", "Altitude", "ft", "ft",
						takeOffFolderPath, "TakeOff_Trajectory_IMPERIAL");

				//.................................................................................
				// vertical distance v.s. time
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(time),
						MyArrayUtils.convertListOfAmountTodoubleArray(verticalDistance),
						0.0, null, 0.0, null,
						"Time", "Altitude", "s", "m",
						takeOffFolderPath, "Altitude_evolution_SI");
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(time),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								verticalDistance.stream()
								.map(x -> x.to(NonSI.FOOT))
								.collect(Collectors.toList())
								),
						0.0, null, 0.0, null,
						"Time", "Altitude", "s", "ft",
						takeOffFolderPath, "Altitude_evolution_IMPERIAL");
				
			}
			
			//.................................................................................
			// speed v.s. time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(speed),
					0.0, null, 0.0, null,
					"Time", "Speed", "s", "m/s",
					takeOffFolderPath, "Speed_evolution_SI");
			
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							speed.stream()
							.map(x -> x.to(NonSI.KNOT))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Time", "Speed", "s", "kn",
					takeOffFolderPath, "Speed_evolution_IMPERIAL");
			
			//.................................................................................
			// speed v.s. ground distance
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
					MyArrayUtils.convertListOfAmountTodoubleArray(speed),
					0.0, null, 0.0, null,
					"Ground Distance", "Speed", "m", "m/s",
					takeOffFolderPath, "Speed_vs_GroundDistance_SI");
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							speed.stream()
							.map(x -> x.to(NonSI.KNOT))
							.collect(Collectors.toList())
							),
					0.0, null, 0.0, null,
					"Ground Distance", "Speed", "ft", "kn",
					takeOffFolderPath, "Speed_vs_GroundDistance_IMPERIAL");

			//.................................................................................
			// acceleration v.s. time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(acceleration),
					0.0, null, null, null,
					"Time", "Acceleration", "s", "m/(s^2)",
					takeOffFolderPath, "Acceleration_evolution_SI");
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							acceleration.stream()
							.map(x -> x.to(MyUnits.FOOT_PER_SQUARE_MINUTE))
							.collect(Collectors.toList())
							),
					0.0, null, null, null,
					"Time", "Acceleration", "s", "ft/(min^2)",
					takeOffFolderPath, "Acceleration_evolution_IMPERIAL");
			
			//.................................................................................
			// acceleration v.s. time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
					MyArrayUtils.convertListOfAmountTodoubleArray(acceleration),
					0.0, null, null, null,
					"Ground Distance", "Acceleration", "m", "m/(s^2)",
					takeOffFolderPath, "Acceleration_vs_GroundDistance_SI");
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertListOfAmountTodoubleArray(
							acceleration.stream()
							.map(x -> x.to(MyUnits.FOOT_PER_SQUARE_MINUTE))
							.collect(Collectors.toList())
							),
					0.0, null, null, null,
					"Ground Distance", "Acceleration", "ft", "ft/(min^2)",
					takeOffFolderPath, "Acceleration_vs_GroundDistance_IMPERIAL");

			//.................................................................................
			// load factor v.s. time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertToDoublePrimitive(loadFactor),
					0.0, null, 0.0, null,
					"Time", "Load Factor", "s", "",
					takeOffFolderPath, "LoadFactor_evolution");

			//.................................................................................
			// load factor v.s. ground distance
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
					MyArrayUtils.convertToDoublePrimitive(loadFactor),
					0.0, null, 0.0, null,
					"Ground distance", "Load Factor", "m", "",
					takeOffFolderPath, "LoadFactor_vs_GroundDistance_SI");
			
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							), 
					MyArrayUtils.convertToDoublePrimitive(loadFactor),
					0.0, null, 0.0, null,
					"Ground distance", "Load Factor", "m", "",
					takeOffFolderPath, "LoadFactor_vs_GroundDistance_IMPERIAL");

			if(!isAborted) {
				//.................................................................................
				// Rate of Climb v.s. Time
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(time),
						MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimb),
						0.0, null, 0.0, null,
						"Time", "Rate of Climb", "s", "m/s",
						takeOffFolderPath, "RateOfClimb_evolution_SI");
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(time),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								rateOfClimb.stream()
								.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
								.collect(Collectors.toList())
								),
						0.0, null, 0.0, null,
						"Time", "Rate of Climb", "s", "ft/min",
						takeOffFolderPath, "RateOfClimb_evolution_IMPERIAL");

				//.................................................................................
				// Rate of Climb v.s. Ground distance
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance), 
						MyArrayUtils.convertListOfAmountTodoubleArray(rateOfClimb),
						0.0, null, 0.0, null,
						"Ground distance", "Rate of Climb", "m", "m/s",
						takeOffFolderPath, "RateOfClimb_vs_GroundDistance_SI");
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								groundDistance.stream()
								.map(x -> x.to(NonSI.FOOT))
								.collect(Collectors.toList())
								), 
						MyArrayUtils.convertListOfAmountTodoubleArray(
								rateOfClimb.stream()
								.map(x -> x.to(MyUnits.FOOT_PER_MINUTE))
								.collect(Collectors.toList())
								),
						0.0, null, 0.0, null,
						"Ground distance", "Rate of Climb", "ft", "ft/min",
						takeOffFolderPath, "RateOfClimb_vs_GroundDistance_IMPERIAL");
			}
			
			//.................................................................................
			// CL v.s. Time
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(time),
					MyArrayUtils.convertToDoublePrimitive(cL),
					0.0, null, 0.0, null,
					"Time", "CL", "s", "",
					takeOffFolderPath, "CL_evolution");

			//.................................................................................
			// CL v.s. Ground distance
			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance),
					MyArrayUtils.convertToDoublePrimitive(cL),
					0.0, null, 0.0, null,
					"Ground distance", "CL", "m", "",
					takeOffFolderPath, "CL_vs_GroundDistance_SI");

			MyChartToFileUtils.plotNoLegend(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(cL),
					0.0, null, 0.0, null,
					"Ground distance", "CL", "ft", "",
					takeOffFolderPath, "CL_vs_GroundDistance_IMPERIAL");
			
			//.................................................................................
			// Horizontal Forces v.s. Time
			double[][] xMatrix1SI = new double[5][totalForce.size()];
			for(int i=0; i<xMatrix1SI.length; i++)
				xMatrix1SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

			double[][] yMatrix1SI = new double[5][totalForce.size()];
			yMatrix1SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForce);
			yMatrix1SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontal);
			yMatrix1SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(drag);
			yMatrix1SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(friction);
			yMatrix1SI[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix1SI, yMatrix1SI,
					0.0, null, null, null,
					"Time", "Horizontal Forces", "s", "N",
					new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
					takeOffFolderPath, "HorizontalForces_evolution_SI");

			double[][] xMatrix1IMPERIAL = new double[5][totalForce.size()];
			for(int i=0; i<xMatrix1IMPERIAL.length; i++)
				xMatrix1IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

			double[][] yMatrix1IMPERIAL = new double[5][totalForce.size()];
			yMatrix1IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					totalForce.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix1IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					thrustHorizontal.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix1IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					drag.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix1IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
					friction.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix1IMPERIAL[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.map(x -> x.times(0.224809))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix1IMPERIAL, yMatrix1IMPERIAL,
					0.0, null, null, null,
					"Time", "Horizontal Forces", "s", "lb",
					new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
					takeOffFolderPath, "HorizontalForces_evolution_IMPERIAL");
			
			//.................................................................................
			// Horizontal Forces v.s. Ground Distance
			double[][] xMatrix2SI = new double[5][totalForce.size()];
			for(int i=0; i<xMatrix2SI.length; i++)
				xMatrix2SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

			double[][] yMatrix2SI = new double[5][totalForce.size()];
			yMatrix2SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(totalForce);
			yMatrix2SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustHorizontal);
			yMatrix2SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(drag);
			yMatrix2SI[3] = MyArrayUtils.convertListOfAmountTodoubleArray(friction);
			yMatrix2SI[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix2SI, yMatrix2SI,
					0.0, null, null, null,
					"Time", "Horizontal Forces", "m", "N",
					new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
					takeOffFolderPath, "HorizontalForces_evolution_SI");

			double[][] xMatrix2IMPERIAL = new double[5][totalForce.size()];
			for(int i=0; i<xMatrix2IMPERIAL.length; i++)
				xMatrix2IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

			double[][] yMatrix2IMPERIAL = new double[5][totalForce.size()];
			yMatrix2IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					totalForce.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix2IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					thrustHorizontal.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix2IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					drag.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix2IMPERIAL[3] = MyArrayUtils.convertListOfAmountTodoubleArray(
					friction.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix2IMPERIAL[4] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.sin(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.map(x -> x.times(0.224809))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix2IMPERIAL, yMatrix2IMPERIAL,
					0.0, null, null, null,
					"Time", "Horizontal Forces", "ft", "lb",
					new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction", "W*sin(gamma)"},
					takeOffFolderPath, "HorizontalForces_evolution_IMPERIAL");

			//.................................................................................
			// Vertical Forces v.s. Time
			double[][] xMatrix3SI = new double[3][totalForce.size()];
			for(int i=0; i<xMatrix3SI.length; i++)
				xMatrix3SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

			double[][] yMatrix3SI = new double[3][totalForce.size()];
			yMatrix3SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(lift);
			yMatrix3SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVertical);
			yMatrix3SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix3SI, yMatrix3SI,
					0.0, null, null, null,
					"Time", "Vertical Forces", "s", "N",
					new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
					takeOffFolderPath, "VerticalForces_evolution");

			double[][] xMatrix3IMPERIAL = new double[3][totalForce.size()];
			for(int i=0; i<xMatrix3IMPERIAL.length; i++)
				xMatrix3IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

			double[][] yMatrix3IMPERIAL = new double[3][totalForce.size()];
			yMatrix3IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					lift.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix3IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					thrustVertical.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix3IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.map(x -> x.times(0.224809))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix3IMPERIAL, yMatrix3IMPERIAL,
					0.0, null, null, null,
					"Time", "Vertical Forces", "s", "lb",
					new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
					takeOffFolderPath, "VerticalForces_evolution_IMPERIAL");
			
			//.................................................................................
			// Vertical Forces v.s. ground distance
			double[][] xMatrix4SI = new double[3][totalForce.size()];
			for(int i=0; i<xMatrix4SI.length; i++)
				xMatrix4SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

			double[][] yMatrix4SI = new double[3][totalForce.size()];
			yMatrix4SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(lift);
			yMatrix4SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(thrustVertical);
			yMatrix4SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix4SI, yMatrix4SI,
					0.0, null, null, null,
					"Ground distance", "Vertical Forces", "m", "N",
					new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
					takeOffFolderPath, "VerticalForces_vs_GroundDistance_SI");

			double[][] xMatrix4IMPERIAL = new double[3][totalForce.size()];
			for(int i=0; i<xMatrix4IMPERIAL.length; i++)
				xMatrix4IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
						groundDistance.stream()
						.map(x -> x.to(NonSI.FOOT))
						.collect(Collectors.toList())
						);

			double[][] yMatrix4IMPERIAL = new double[3][totalForce.size()];
			yMatrix4IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
					lift.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix4IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
					thrustVertical.stream()
					.map(x -> x.to(NonSI.POUND_FORCE))
					.collect(Collectors.toList())
					);
			yMatrix4IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
					gamma.stream()
					.map(x -> Math.cos(x.doubleValue(SI.RADIAN)))
					.map(x -> maxTakeOffMass.times(AtmosphereCalc.g0).times(x))
					.map(x -> x.times(0.224809))
					.collect(Collectors.toList())
					);

			MyChartToFileUtils.plot(
					xMatrix4IMPERIAL, yMatrix4IMPERIAL,
					0.0, null, null, null,
					"Ground distance", "Vertical Forces", "ft", "lb",
					new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
					takeOffFolderPath, "VerticalForces_vs_GroundDistance_IMPERIAL");
			
			if(!isAborted) {
				//.................................................................................
				// Angles v.s. time
				double[][] xMatrix5 = new double[3][totalForce.size()];
				for(int i=0; i<xMatrix5.length; i++)
					xMatrix5[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

				double[][] yMatrix5 = new double[3][totalForce.size()];
				yMatrix5[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						alpha.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix5[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						theta.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix5[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
						gamma.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);

				MyChartToFileUtils.plot(
						xMatrix5, yMatrix5,
						0.0, null, null, null,
						"Time", "Angles", "s", "deg",
						new String[] {"Alpha Body", "Theta", "Gamma"},
						takeOffFolderPath, "Angles_evolution");

				//.................................................................................
				// Angles v.s. Ground Distance
				double[][] xMatrix6SI = new double[3][totalForce.size()];
				for(int i=0; i<xMatrix6SI.length; i++)
					xMatrix6SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

				double[][] yMatrix6SI = new double[3][totalForce.size()];
				yMatrix6SI[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						alpha.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6SI[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						theta.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6SI[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
						gamma.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);

				MyChartToFileUtils.plot(
						xMatrix6SI, yMatrix6SI,
						0.0, null, null, null,
						"Ground Distance", "Angles", "m", "deg",
						new String[] {"Alpha Body", "Theta", "Gamma"},
						takeOffFolderPath, "Angles_vs_GroundDistance_SI");

				double[][] xMatrix6IMPERIAL = new double[3][totalForce.size()];
				for(int i=0; i<xMatrix6IMPERIAL.length; i++)
					xMatrix6IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							);

				double[][] yMatrix6IMPERIAL = new double[3][totalForce.size()];
				yMatrix6IMPERIAL[0] = MyArrayUtils.convertListOfAmountTodoubleArray(
						alpha.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6IMPERIAL[1] = MyArrayUtils.convertListOfAmountTodoubleArray(
						theta.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);
				yMatrix6IMPERIAL[2] = MyArrayUtils.convertListOfAmountTodoubleArray(
						gamma.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList())
						);

				MyChartToFileUtils.plot(
						xMatrix6IMPERIAL, yMatrix6IMPERIAL,
						0.0, null, null, null,
						"Ground Distance", "Angles", "ft", "deg",
						new String[] {"Alpha Body", "Theta", "Gamma"},
						takeOffFolderPath, "Angles_vs_GroundDistance_IMPERIAL");
				
				//.................................................................................
				// Angular velocity v.s. time
				double[][] xMatrix7 = new double[2][totalForce.size()];
				for(int i=0; i<xMatrix7.length; i++)
					xMatrix7[i] = MyArrayUtils.convertListOfAmountTodoubleArray(time);

				double[][] yMatrix7 = new double[2][totalForce.size()];
				yMatrix7[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
				yMatrix7[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);

				MyChartToFileUtils.plot(
						xMatrix7, yMatrix7,
						0.0, null, null, null,
						"Time", "Angular Velocity", "s", "deg/s",
						new String[] {"Alpha_dot", "Gamma_dot"},
						takeOffFolderPath, "AngularVelocity_evolution");

				//.................................................................................
				// Angular velocity v.s. Ground Distance
				double[][] xMatrix8SI = new double[2][totalForce.size()];
				for(int i=0; i<xMatrix8SI.length; i++)
					xMatrix8SI[i] = MyArrayUtils.convertListOfAmountTodoubleArray(groundDistance);

				double[][] yMatrix8SI = new double[2][totalForce.size()];
				yMatrix8SI[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
				yMatrix8SI[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);

				MyChartToFileUtils.plot(
						xMatrix8SI, yMatrix8SI,
						0.0, null, null, null,
						"Ground Distance", "Angular Velocity", "m", "deg/s",
						new String[] {"Alpha_dot", "Gamma_dot"},
						takeOffFolderPath, "AngularVelocity_vs_GroundDistance_SI");
				
				double[][] xMatrix8IMPERIAL = new double[2][totalForce.size()];
				for(int i=0; i<xMatrix8IMPERIAL.length; i++)
					xMatrix8IMPERIAL[i] = MyArrayUtils.convertListOfAmountTodoubleArray(
							groundDistance.stream()
							.map(x -> x.to(NonSI.FOOT))
							.collect(Collectors.toList())
							);

				double[][] yMatrix8SIMPERIAL = new double[2][totalForce.size()];
				yMatrix8SIMPERIAL[0] = MyArrayUtils.convertToDoublePrimitive(alphaDot);
				yMatrix8SIMPERIAL[1] = MyArrayUtils.convertToDoublePrimitive(gammaDot);

				MyChartToFileUtils.plot(
						xMatrix8IMPERIAL, yMatrix8SIMPERIAL,
						0.0, null, null, null,
						"Ground Distance", "Angular Velocity", "ft", "deg/s",
						new String[] {"Alpha_dot", "Gamma_dot"},
						takeOffFolderPath, "AngularVelocity_vs_GroundDistance_SI");
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
		public void createBalancedFieldLengthChart(String takeOffFolderPath) {

			System.out.println("\n-------WRITING BALANCED TAKE-OFF DISTANCE CHART TO FILE--------");

			for(int i=0; i<failureSpeedArrayFitted.length; i++)
				failureSpeedArrayFitted[i] = failureSpeedArrayFitted[i]/vSTakeOff.getEstimatedValue();

			double[][] xArray = new double[][]
					{failureSpeedArrayFitted, failureSpeedArrayFitted};
			double[][] yArraySI = new double[][]
					{continuedTakeOffArrayFitted, abortedTakeOffArrayFitted};

			MyChartToFileUtils.plot(
					xArray, yArraySI,
					null, null, null, null,
					"Vfailure/VsTO", "Distance", "", "m",
					new String[] {"OEI Take-Off", "Aborted Take-Off"},
					takeOffFolderPath, "BalancedTakeOffLength_SI");
			
			double[][] yArrayIMPERIAL = new double[][]	{
				Arrays.stream(continuedTakeOffArrayFitted)
				.map(x -> x*3.28084)
				.toArray(), 
				Arrays.stream(abortedTakeOffArrayFitted)
				.map(x -> x*3.28084)
				.toArray()
				};

			MyChartToFileUtils.plot(
					xArray, yArrayIMPERIAL,
					null, null, null, null,
					"Vfailure/VsTO", "Distance", "", "ft",
					new String[] {"OEI Take-Off", "Aborted Take-Off"},
					takeOffFolderPath, "BalancedTakeOffLength_IMPERIAL");

			System.out.println("\n---------------------------DONE!-------------------------------");
		}

		@Override
		public String toString() {

			MyConfiguration.customizeAmountOutput();

			StringBuilder sb = new StringBuilder();
			sb.append("\tTAKE-OFF RESULTS\n")
			.append("\t-------------------------------------\n")
			.append("\t\tGround roll distance = " + output.getGroundRoll().to(SI.METER) + "\n")
			.append("\t\tRotation distance = " + output.getRotation().to(SI.METER) + "\n")
			.append("\t\tAirborne distance = " + output.getAirborne().to(SI.METER) + "\n")
			.append("\t\tAEO take-off distance = " + output.getTakeOffDistanceAOE().to(SI.METER) + "\n")
			.append("\t\tFAR-25 take-off field length = " + output.getTakeOffDistanceFAR25().to(SI.METER) + "\n");
			if(input.isBalancedFieldLength()) 
				sb.append("\t\tBalanced field length = " + output.getBalancedFieldLength().to(SI.METER) + "\n");
			sb.append("\t\t.....................................\n")
			.append("\t\tGround roll distance = " + output.getGroundRoll().to(NonSI.FOOT) + "\n")
			.append("\t\tRotation distance = " + output.getRotation().to(NonSI.FOOT) + "\n")
			.append("\t\tAirborne distance = " + output.getAirborne().to(NonSI.FOOT) + "\n")
			.append("\t\tAEO take-off distance = " + output.getTakeOffDistanceAOE().to(NonSI.FOOT) + "\n")
			.append("\t\tFAR-25 take-off field length = " + output.getTakeOffDistanceFAR25().to(NonSI.FOOT) + "\n");
			if(input.isBalancedFieldLength()) 
				sb.append("\t\tBalanced field length = " + output.getBalancedFieldLength().to(NonSI.FOOT) + "\n");
			sb.append("\t\t.....................................\n")
			.append("\t\tStall speed take-off (VsTO)= " + output.getVsT0().to(SI.METERS_PER_SECOND) + "\n");
			if(input.isBalancedFieldLength())
				sb.append("\t\tDecision speed (V1) = " + output.getV1().to(SI.METERS_PER_SECOND) + "\n");
			sb.append("\t\tRotation speed (V_Rot) = " + output.getvRot().to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tLift-off speed (V_LO) = " + output.getvLO().to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\tTake-off safety speed (V2) = " + output.getV2().to(SI.METERS_PER_SECOND) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed take-off (VsTO)= " + output.getVsT0().to(NonSI.KNOT) + "\n");
			if(input.isBalancedFieldLength())
				sb.append("\t\tDecision speed (V1) = " + output.getV1().to(NonSI.KNOT) + "\n");
			sb.append("\t\tRotation speed (V_Rot) = " + output.getvRot().to(NonSI.KNOT) + "\n")
			.append("\t\tLift-off speed (V_LO) = " + output.getvLO().to(NonSI.KNOT) + "\n")
			.append("\t\tTake-off safety speed (V2) = " + output.getV2().to(NonSI.KNOT) + "\n")
			.append("\t\t.....................................\n");
			if(input.isBalancedFieldLength())
				sb.append("\t\tV1/VsTO = " + output.getV1().to(SI.METERS_PER_SECOND).divide(output.getVsT0().to(SI.METERS_PER_SECOND)) + "\n");
			sb.append("\t\tV_Rot/VsTO = " + output.getvRot().to(SI.METERS_PER_SECOND).divide(output.getVsT0().to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\tV_LO/VsTO = " + output.getvLO().to(SI.METERS_PER_SECOND).divide(output.getVsT0().to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\tV2/VsTO = " + output.getV2().to(SI.METERS_PER_SECOND).divide(output.getVsT0().to(SI.METERS_PER_SECOND)) + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tTake-off duration = " + output.getTakeOffDuration() + "\n")
			.append("\t-------------------------------------\n")
			;
			
			return sb.toString();
		}
		
		//-------------------------------------------------------------------------------------
		//										NESTED CLASS
		//-------------------------------------------------------------------------------------
		// ODE integration
		// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

		public class DynamicsEquationsTakeOff implements FirstOrderDifferentialEquations {

			double weight, altitude, g0, kAlpha, cD0, deltaCD0, oswald, ar, kGround, vWind, alphaDotInitial;
			MyInterpolatingFunction mu, muBrake;
			
			// visible variables
			public double alpha, gamma;

			public DynamicsEquationsTakeOff() {

				// constants and known values
				weight = maxTakeOffMass.times(AtmosphereCalc.g0).getEstimatedValue();
				g0 = AtmosphereCalc.g0.getEstimatedValue();
				mu = TakeOffCalculator.this.mu;
				muBrake = TakeOffCalculator.this.muBrake;
				kAlpha = TakeOffCalculator.this.kAlphaDot;
				ar = input.getAspectRatio();
				kGround = TakeOffCalculator.this.getkGround();
				vWind = TakeOffCalculator.this.getvWind().getEstimatedValue();
				altitude = TakeOffCalculator.this.getAltitude().getEstimatedValue();
				alphaDotInitial = TakeOffCalculator.this.getAlphaDotInitial();
			

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
								- (mu(speed)*(weight - lift(speed, alpha, gamma, t))));
						xDot[2] = 0.0;
						xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
					}
					else {
						xDot[0] = speed;
						xDot[1] = (g0/weight)*(
								thrust(speed, gamma,t)*Math.cos(Amount.valueOf(alpha, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()) 
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
								- (mu.value(speed)*(weight - lift(speed, alpha, gamma, t))));
						xDot[2] = 0.0;
						xDot[3] = speed*Math.sin(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue());
					}
					else {
						xDot[0] = speed;
						xDot[1] = (g0/weight)*(thrust(speed, gamma, t) - drag(speed, alpha, gamma, t)
								- (muBrake.value(speed)*(weight-lift(speed, alpha, gamma, t))));
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
						theThrust = input.getNetThrust().value(speed)*input.getnEngine();
				}
				else {
					if(input.isEngineModel()) {
						double thrustRatio = 1-(0.00252*speed)+(0.00000434*(Math.pow(speed, 2)));  // simplified thrust model for a turbofan
						theThrust = (input.getnEngine()-1)*input.getT0().getEstimatedValue()*thrustRatio;
					}
					else
						theThrust = input.getNetThrust().value(speed)*(input.getnEngine()-1);
				}

				return theThrust;
			}

			public double cD(double cL) {
				
				return input.getcD0Clean() 
						+ input.getDeltaCD0Flap() 
						+ input.getDeltaCD0LandingGear() 
						+ ((Math.pow(cL, 2)/(Math.PI*input.getAspectRatio()*input.getOswald()))*kGround);
				
			}

			public double drag(double speed, double alpha, double gamma, double time) {

				double cD = 0;
				
				if (time < tRec.getEstimatedValue())
					cD = cD(cL(speed, alpha, gamma, time));
				else
					cD = input.getDragDueToEnigneFailure() + cD(cL(speed, alpha, gamma, time));

				return 	0.5
						*input.getWingSurface().doubleValue(SI.SQUARE_METRE)
						*AtmosphereCalc.getDensity(
								altitude)
						*(Math.pow(speed + (vWind*Math.cos(Amount.valueOf(
								gamma,
								NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())), 2))
						*cD;
			}

			public double cL(double speed, double alpha, double gamma ,double time) {

				if (time < tClimb.getEstimatedValue()) {
					double cL0 = TakeOffCalculator.this.cL0;
					double cLalpha = TakeOffCalculator.this.getcLalphaFlap();
					double alphaWing = alpha + TakeOffCalculator.this.getIw().getEstimatedValue();

					return cL0 + (cLalpha*alphaWing);
					
				}
				else
					return (2*weight*Math.cos(Amount.valueOf(gamma, NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue()))/
							(input.getWingSurface().doubleValue(SI.SQUARE_METRE)*
									AtmosphereCalc.getDensity(
											altitude)*
									Math.pow(speed, 2));
			}

			public double lift(double speed, double alpha, double gamma, double time) {

				double cL = cL(speed, alpha, gamma, time);

				return 	0.5
						*input.getWingSurface().doubleValue(SI.SQUARE_METRE)
						*AtmosphereCalc.getDensity(
								altitude)
						*(Math.pow(speed + (vWind*Math.cos(Amount.valueOf(
								gamma,
								NonSI.DEGREE_ANGLE).to(SI.RADIAN).getEstimatedValue())), 2))
						*cL;
			}

			public double mu(double speed) {
				return mu.value(speed);
			}
			
			public double muBrake(double speed) {
				return muBrake.value(speed);
			}
			
			public double throttleGrounIdle(double speed) {
				return groundIdlePhi.value(speed);
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

				double alpha = TakeOffCalculator.this.getAlphaGround().getEstimatedValue();

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
		//									END NESTED CLASS	
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

		public Amount<Velocity> getvWind() {
			return vWind;
		}

		public void setvWind(Amount<Velocity> vWind) {
			this.vWind = vWind;
		}

		public Amount<Length> getWingToGroundDistance() {
			return wingToGroundDistance;
		}

		public void setWingToGroundDistance(Amount<Length> wingToGroundDistance) {
			this.wingToGroundDistance = wingToGroundDistance;
		}

		public Amount<Length> getObstacle() {
			return obstacle;
		}

		public void setObstacle(Amount<Length> obstacle) {
			this.obstacle = obstacle;
		}

		public Amount<Angle> getAlphaGround() {
			return alphaGround;
		}

		public void setAlphaGround(Amount<Angle> alphaGround) {
			this.alphaGround = alphaGround;
		}

		public Amount<Angle> getIw() {
			return iw;
		}

		public void setIw(Amount<Angle> iw) {
			this.iw = iw;
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

		public double getkAlphaDot() {
			return kAlphaDot;
		}

		public void setkAlphaDot(double kAlphaDot) {
			this.kAlphaDot = kAlphaDot;
		}

		public MyInterpolatingFunction getMu() {
			return mu;
		}

		public void setMu(MyInterpolatingFunction mu) {
			this.mu = mu;
		}

		public MyInterpolatingFunction getMuBrake() {
			return muBrake;
		}

		public void setMuBrake(MyInterpolatingFunction muBrake) {
			this.muBrake = muBrake;
		}

		public double getcLmaxTO() {
			return cLmaxTO;
		}

		public void setcLmaxTO(double cLmaxTO) {
			this.cLmaxTO = cLmaxTO;
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

		public double getcL0() {
			return cL0;
		}

		public void setcL0(double cL0) {
			this.cL0 = cL0;
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

		public TakeOffResultsMap getTakeOffResults() {
			return takeOffResults;
		}

		public void setTakeOffResults(TakeOffResultsMap takeOffResults) {
			this.takeOffResults = takeOffResults;
		}

		public double getkRot() {
			return kRot;
		}

		public double getKclMax() {
			return kcLMax;
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

		public Amount<Velocity> getV1() {
			return v1;
		}

		public void setV1(Amount<Velocity> v1) {
			this.v1 = v1;
		}

		public Amount<Length> getBalancedFieldLength() {
			return balancedFieldLength;
		}

		public void setBalancedFieldLength(Amount<Length> balancedFieldLength) {
			this.balancedFieldLength = balancedFieldLength;
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

		public boolean isAborted() {
			return isAborted;
		}

		public void setAborted(boolean isAborted) {
			this.isAborted = isAborted;
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

		public void setPhi(double phi) {
			this.phi = phi;
		}

		public Amount<Duration> gettRec() {
			return tRec;
		}

		public void settRec(Amount<Duration> tRec) {
			this.tRec = tRec;
		}

		public double getcLalphaFlap() {
			return cLalphaFlap;
		}

		public void setcLalphaFlap(double cLalphaFlap) {
			this.cLalphaFlap = cLalphaFlap;
		}

		public Amount<Mass> getMaxTakeOffMass() {
			return maxTakeOffMass;
		}

		public void setMaxTakeOffMass(Amount<Mass> maxTakeOffMass) {
			this.maxTakeOffMass = maxTakeOffMass;
		}

		public Amount<Length> getAltitude() {
			return altitude;
		}

		public void setAltitude(Amount<Length> altitude) {
			this.altitude = altitude;
		}

		public List<Double> getSfc() {
			return sfc;
		}

		public void setSfc(List<Double> sfc) {
			this.sfc = sfc;
		}

		public MyInterpolatingFunction getGroundIdlePhi() {
			return groundIdlePhi;
		}

		public void setGroundIdlePhi(MyInterpolatingFunction groundIdlePhi) {
			this.groundIdlePhi = groundIdlePhi;
		}

		public Amount<Velocity> getV2() {
			return v2;
		}

		public void setV2(Amount<Velocity> v2) {
			this.v2 = v2;
		}

		public double[] getContinuedTakeOffArrayFitted() {
			return continuedTakeOffArrayFitted;
		}

		public void setContinuedTakeOffArrayFitted(double[] continuedTakeOffArrayFitted) {
			this.continuedTakeOffArrayFitted = continuedTakeOffArrayFitted;
		}

		public double[] getAbortedTakeOffArrayFitted() {
			return abortedTakeOffArrayFitted;
		}

		public void setAbortedTakeOffArrayFitted(double[] abortedTakeOffArrayFitted) {
			this.abortedTakeOffArrayFitted = abortedTakeOffArrayFitted;
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
