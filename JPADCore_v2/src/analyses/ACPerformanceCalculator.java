package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXLSUtils;
import standaloneutils.MyXMLReaderUtils;

public class ACPerformanceCalculator {

	/*
	 *******************************************************************************
	 * THIS CLASS IS A PROTOTYPE OF THE NEW ACPerformanceManager (WORK IN PROGRESS)
	 * 
	 * @author Vittorio Trifari
	 *******************************************************************************
	 */
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private String _id;
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	//..............................................................................
	// Weights
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _maximumLandingMass;
	private Amount<Mass> _maximumZeroFuelMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _maximumFuelMass;
	//..............................................................................
	// Aerodynamics
	private Double _cD0;
	private Double _oswald;
	private Double _cLmaxClean;
	private Amount<?> _cLAlphaClean;
	private Double _deltaCD0HighLift;
	private Double _deltaCD0LandingGear;
	private Double _cLmaxTakeOff;
	private Double _cLZeroTakeOff;
	private Double _cLmaxLanding;
	//..............................................................................
	// Take-off & Landing
	private Amount<Angle> _alphaGround;
	private Amount<Velocity> _windSpeed;
	private Amount<Length> _obstacleTakeOff;
	private Double _mu;
	private Double _muBrake;
	private Double _throttleSetting;
	private Double _kRotation;
	private Double _kLiftOff;
	private Double _kCLmax;
	private Double _kDragDueToEnigneFailure;
	private Double _kAlphaDot;
	private Double _alphaReductionRate;
	private Amount<Length> _obstacleLanding;
	private Amount<Angle> _thetaApproach;
	private Double _kApproach;
	private Double _kFlare;
	private Double _kTouchDown;
	private Amount<Duration> _freeRollDuration;
	//..............................................................................
	// Flight maneuvering envelope
	private Double _cLmaxInverted;

	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................
	// Take-Off
	// TODO
	
	//..............................................................................
	// Climb
	// TODO
	
	//..............................................................................
	// Cruise
	// TODO
	
	//..............................................................................
	// Landing
	// TODO
	
	//..............................................................................
	// Payload-Range
	// TODO
	
	//..............................................................................
	// Maneuvering and Gust Flight Envelope 
	// TODO
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACPerformanceCalculatorBuilder {
	
		// required parameters
		private String __id;
		private Aircraft __theAircraft;
		private OperatingConditions __theOperatingConditions;
		
		// optional parameters ... default
		//..............................................................................
		// Wiehgts
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __maximumLandingMass;
		private Amount<Mass> __maximumZeroFuelMass;
		private Amount<Mass> __operatingEmptyMass;
		private Amount<Mass> __maximumFuelMass;
		//..............................................................................
		// Aerodynamics
		private Double __cD0;
		private Double __oswald;
		private Double __cLmaxClean;
		private Amount<?> __cLAlphaClean;
		private Double __deltaCD0HighLift;
		private Double __deltaCD0LandingGear;
		private Double __cLmaxTakeOff;
		private Double __cLZeroTakeOff;
		private Double __cLmaxLanding;
		//..............................................................................
		// Take-off & Landing
		private Amount<Angle> __alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		private Amount<Velocity> __windSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		private Amount<Length> __obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		private Double __mu = 0.03;
		private Double __muBrake = 0.4;
		private Double __kRotation = 1.05;
		private Double __kLiftOff = 1.1;
		private Double __kCLmax = 0.85;
		private Double __kDragDueToEngineFailure = 1.1;
		private Double __kAlphaDot = 0.04;
		private Double __alphaReductionRate = -4.0; //(deg/s)
		private Amount<Length> __obstacleLanding = Amount.valueOf(50, NonSI.FOOT).to(SI.METER);
		private Amount<Angle> __thetaApproach = Amount.valueOf(3.0, NonSI.DEGREE_ANGLE);
		private Double __kApproach = 1.3;
		private Double __kFlare = 1.23;
		private Double __kTouchDown = 1.15;
		private Amount<Duration> __freeRollDuration = Amount.valueOf(2.0, SI.SECOND);
		//..............................................................................
		// Other data
		private Double __throttleSetting = 0.0;
		private Double __cLmaxInverted = -1.0;
		//..............................................................................
		
		public ACPerformanceCalculatorBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder aircraft(Aircraft aircraft) {
			this.__theAircraft = aircraft;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder operatingConditions(OperatingConditions operatingConditions) {
			this.__theOperatingConditions = operatingConditions;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder maximumTakeOffMass(Amount<Mass> maximumTakeOffMass) {
			this.__maximumTakeOffMass = maximumTakeOffMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder maximumLandingMass(Amount<Mass> maximumLandingMass) {
			this.__maximumLandingMass = maximumLandingMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder maximumZeroFuelMass(Amount<Mass> maximumZeroFuelMass) {
			this.__maximumZeroFuelMass = maximumZeroFuelMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder operatingEmptyMass(Amount<Mass> operatingEmptyMass) {
			this.__operatingEmptyMass = operatingEmptyMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder maximumFuelMass(Amount<Mass> maximumFuelMass) {
			this.__maximumFuelMass = maximumFuelMass;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cD0(Double cD0) {
			this.__cD0 = cD0;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder oswald(Double e) {
			this.__oswald = e;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxClean(Double cLmaxClean) {
			this.__cLmaxClean = cLmaxClean;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLAlphaClean(Amount<?> cLAlphaClean) {
			this.__cLAlphaClean = cLAlphaClean;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder deltaCD0HighLift(Double deltaCD0HighLift) {
			this.__deltaCD0HighLift = deltaCD0HighLift;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder deltaCD0LandinGear(Double deltaCD0LandingGear) {
			this.__deltaCD0LandingGear = deltaCD0LandingGear;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxTakeOff(Double cLmaxTakeOff) {
			this.__cLmaxTakeOff = cLmaxTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLZeroTakeOff(Double cLZeroTakeOff) {
			this.__cLZeroTakeOff = cLZeroTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxLanding(Double cLmaxLanding) {
			this.__cLmaxLanding = cLmaxLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder alphaGround(Amount<Angle> alphaGround) {
			this.__alphaGround = alphaGround;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder windSpeed(Amount<Velocity> windSpeed) {
			this.__windSpeed = windSpeed;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder obstacleTakeOff(Amount<Length> obstacleTakeOff) {
			this.__obstacleTakeOff = obstacleTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder obstacleLanding(Amount<Length> obstacleLanding) {
			this.__obstacleLanding = obstacleLanding;
			return this;
		}

		public ACPerformanceCalculatorBuilder mu(Double mu) {
			this.__mu = mu;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder muBrake(Double muBrake) {
			this.__muBrake = muBrake;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder throttleSetting(Double phi) {
			this.__throttleSetting = phi;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kRotation(Double kRot) {
			this.__kRotation = kRot;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kLiftOff(Double kLO) {
			this.__kLiftOff = kLO;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kCLmax(Double kCLmax) {
			this.__kCLmax = kCLmax;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kDragDueToEngineFailure(Double kFailure) {
			this.__kDragDueToEngineFailure = kFailure;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kAlphaDot(Double kAlphaDot) {
			this.__kAlphaDot = kAlphaDot;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder alphaReductionRate(Double alphaRed) {
			this.__alphaReductionRate = alphaRed;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder thetaApproach(Amount<Angle> thetaApproach) {
			this.__thetaApproach = thetaApproach;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kApproach(Double kA) {
			this.__kApproach = kA;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kFlare(Double kFlare) {
			this.__kFlare = kFlare;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder kTouchDown(Double kTD) {
			this.__kTouchDown = kTD;
			return this;
		}

		public ACPerformanceCalculatorBuilder freeRollDuration(Amount<Duration> nFreeRoll) {
			this.__freeRollDuration = nFreeRoll;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLmaxInverted(Double cLmaxInverted) {
			this.__cLmaxInverted = cLmaxInverted;
			return this;
		}
		
		public ACPerformanceCalculator build() {
			return new ACPerformanceCalculator(this);
		}
		
	}
	
	private ACPerformanceCalculator(ACPerformanceCalculatorBuilder builder) {
		
		this._id = builder.__id;
		this._theAircraft = builder.__theAircraft;
		this._theOperatingConditions = builder.__theOperatingConditions;
		
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._maximumLandingMass = builder.__maximumLandingMass;
		this._maximumZeroFuelMass = builder.__maximumZeroFuelMass;
		this._operatingEmptyMass = builder.__operatingEmptyMass;
		this._maximumFuelMass = builder.__maximumFuelMass;
		this._cD0 = builder.__cD0;
		this._oswald = builder.__oswald;
		this._cLmaxClean = builder.__cLmaxClean;
		this._cLAlphaClean = builder.__cLAlphaClean;
		this._deltaCD0HighLift = builder.__deltaCD0HighLift;
		this._deltaCD0LandingGear = builder.__deltaCD0LandingGear;
		this._cLmaxTakeOff = builder.__cLmaxTakeOff;
		this._cLZeroTakeOff = builder.__cLZeroTakeOff;
		this._cLmaxLanding = builder.__cLmaxLanding;
		this._alphaGround = builder.__alphaGround;
		this._windSpeed = builder.__windSpeed;
		this._obstacleTakeOff = builder.__obstacleTakeOff;
		this._mu = builder.__mu;
		this._muBrake = builder.__muBrake;
		this._throttleSetting = builder.__throttleSetting;
		this._kRotation = builder.__kRotation;
		this._kLiftOff = builder.__kLiftOff;
		this._kCLmax = builder.__kCLmax;
		this._kDragDueToEnigneFailure = builder.__kDragDueToEngineFailure;
		this._kAlphaDot = builder.__kAlphaDot;
		this._alphaReductionRate = builder.__alphaReductionRate;
		this._obstacleLanding = builder.__obstacleLanding;
		this._thetaApproach = builder.__thetaApproach;
		this._kApproach = builder.__kApproach;
		this._kFlare = builder.__kFlare;
		this._kTouchDown = builder.__kTouchDown;
		this._freeRollDuration = builder.__freeRollDuration;
		this._cLmaxInverted = builder.__cLmaxInverted;
	}
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	@SuppressWarnings({ "resource", "unchecked" })
	public static ACPerformanceCalculator importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions
			) throws IOException {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading performance analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		//---------------------------------------------------------------------------------------
		// WEIGHTS FROM FILE INSTRUCTION
		String fileWeightsXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_weights");
		Boolean readWeightsFromXLSFlag;
		String readWeightsFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_xls_file");
		if(readWeightsFromXLSString.equalsIgnoreCase("true"))
			readWeightsFromXLSFlag = Boolean.TRUE;
		else
			readWeightsFromXLSFlag = Boolean.FALSE;
		
		//---------------------------------------------------------------------------------------
		// AERODYNAMICS FROM FILE INSTRUCTION
		String fileAerodynamicsXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_aerodynamics");
		Boolean readAerodynamicsFromXLSFlag;
		String readAerodynamicsFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@aerodynamics_from_xls_file");
		if(readAerodynamicsFromXLSString.equalsIgnoreCase("true"))
			readAerodynamicsFromXLSFlag = Boolean.TRUE;
		else
			readAerodynamicsFromXLSFlag = Boolean.FALSE;

		//===========================================================================================
		// READING WEIGHTS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		Amount<Mass> maximumTakeOffMass = null;
		Amount<Mass> maximumLandingMass = null;
		Amount<Mass> maximumZeroFuelMass = null;
		Amount<Mass> operatingEmptyMass = null;
		Amount<Mass> maximumFuelMass = null;
		
		if(readWeightsFromXLSFlag == Boolean.TRUE) {

			File weightsFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "WEIGHTS"
					+ File.separator
					+ fileWeightsXLS);
			if(weightsFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(weightsFile);
				Workbook workbook;
				if (weightsFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (weightsFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}

				//---------------------------------------------------------------
				// MAXIMUM TAKE-OFF MASS
				Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "GLOBAL RESULTS");
				if(sheetGlobalData != null) {
					Cell maximumTakeOffMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Take-Off Mass").get(0)).getCell(2);
					if(maximumTakeOffMassCell != null)
						maximumTakeOffMass = Amount.valueOf(maximumTakeOffMassCell.getNumericCellValue(), SI.KILOGRAM);
				}
				//---------------------------------------------------------------
				// MAXIMUM LANDING MASS
				Cell maximumLandingMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Landing Mass").get(0)).getCell(2);
				if(maximumLandingMassCell != null)
					maximumLandingMass = Amount.valueOf(maximumLandingMassCell.getNumericCellValue(), SI.KILOGRAM);
				
				//---------------------------------------------------------------
				// MAXIMUM ZERO FUEL MASS
				Cell maximumZeroFuelMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Zero Fuel Mass").get(0)).getCell(2);
				if(maximumZeroFuelMassCell != null)
					maximumZeroFuelMass = Amount.valueOf(maximumZeroFuelMassCell.getNumericCellValue(), SI.KILOGRAM);

				//---------------------------------------------------------------
				// OPERATING EMPTY MASS
				Cell operatingEmptyMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Operating Empty Mass").get(0)).getCell(2);
				if(operatingEmptyMassCell != null)
					operatingEmptyMass = Amount.valueOf(operatingEmptyMassCell.getNumericCellValue(), SI.KILOGRAM);

				//---------------------------------------------------------------
				// MAXIMUM FUEL MASS
				Cell maximumFuelMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Fuel Mass").get(0)).getCell(2);
				if(maximumFuelMassCell != null)
					maximumFuelMass = Amount.valueOf(maximumFuelMassCell.getNumericCellValue(), SI.KILOGRAM);
			}
			else {
				System.err.println("FILE '" + weightsFile.getAbsolutePath() + "' NOT FOUND!! \n\treturning...");
				return null;
			}
		}
		else {
			//---------------------------------------------------------------
			// MAXIMUM TAKE-OFF MASS
			String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_take_off_mass");
			if(maximumTakeOffMassProperty != null)
				maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_take_off_mass");
			
			//---------------------------------------------------------------
			// MAXIMUM LANDING MASS
			String maximumLandingMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_landing_mass");
			if(maximumLandingMassProperty != null)
				maximumLandingMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_landing_mass");
			
			//---------------------------------------------------------------
			// MAXIMUM ZERO FUEL MASS
			String maximumZeroFuelMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_zero_fuel_mass");
			if(maximumZeroFuelMassProperty != null)
				maximumZeroFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_zero_fuel_mass");
			
			//---------------------------------------------------------------
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//performance/weights/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/operating_empty_mass");
			
			//---------------------------------------------------------------
			// MAXIMUM FUEL MASS
			String maximumFuelMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_fuel_mass");
			if(maximumFuelMassProperty != null)
				maximumFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_fuel_mass");
		}
		
		//===========================================================================================
		// READING AERODYNAMICS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */

		Double cD0 = null;
		Double oswald = null;
		Double cLmaxClean = null;
		Amount<?> cLAlphaClean = null;
		Double deltaCD0HighLift = null;
		Double deltaCD0LandingGear = null;
		Double cLmaxTakeOff = null;
		Double cLZeroTakeOff = null;
		Double cLmaxLanding = null;
		
		if(readAerodynamicsFromXLSFlag == Boolean.TRUE) {
			
			File aerodynamicsFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "AERODYNAMICS"
					+ File.separator
					+ fileAerodynamicsXLS);
			if(aerodynamicsFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(aerodynamicsFile);
				Workbook workbook;
				if (aerodynamicsFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (aerodynamicsFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}
				
				// TODO : READ ALL REQUIRED DATA FROM THE AERODYNAMICS XML
			}
		}
		else {
			//---------------------------------------------------------------
			// CD0
			String cD0Property = reader.getXMLPropertyByPath("//performance/aerodynamics/cD0");
			if(cD0Property != null)
				cD0 = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cD0"));
			//---------------------------------------------------------------
			// OSWALD
			String oswladProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald");
			if(oswladProperty != null)
				oswald = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald"));
			//---------------------------------------------------------------
			// CLmax CLEAN
			String cLmaxCleanProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_clean_configuration");
			if(cLmaxCleanProperty != null)
				cLmaxClean = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_clean_configuration"));
			//---------------------------------------------------------------
			// CLalpha CLEAN
			String cLAlphaCleanProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL_alpha_clean_configuration");
			if(cLAlphaCleanProperty != null)
				cLAlphaClean = Amount.valueOf(
							Double.valueOf(
									reader.getXMLPropertyByPath(
											"//performance/aerodynamics/cL_alpha_clean_configuration"
											)
									),
									NonSI.DEGREE_ANGLE.inverse()
									);
			//---------------------------------------------------------------
			// DeltaCD0 HIGH LIFT
			String deltaCD0HighLiftProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_high_lift");
			if(deltaCD0HighLiftProperty != null)
				deltaCD0HighLift = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_high_lift"));
			//---------------------------------------------------------------
			// DeltaCD0 LANDING GEARS
			String deltaCD0LandingGearsProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing_gears");
			if(deltaCD0LandingGearsProperty != null)
				deltaCD0LandingGear = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing_gears"));
			//---------------------------------------------------------------
			// CLmax TAKE-OFF
			String cLmaxTakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_take_off_configuration");
			if(cLmaxTakeOffProperty != null)
				cLmaxTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_take_off_configuration"));
			//---------------------------------------------------------------
			// CL0 TAKE-OFF
			String cL0TakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration");
			if(cL0TakeOffProperty != null)
				cLZeroTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration"));
			//---------------------------------------------------------------
			// CLmax LANDING
			String cLmaxLandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_landing_configuration");
			if(cLmaxLandingProperty != null)
				cLmaxLanding = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_landing_configuration"));
		}
		
		//===========================================================================================
		// READING AERODYNAMICS DATA ...	

		Amount<Angle> alphaGround = null;
		Amount<Velocity> windSpeed = null;
		Amount<Length> obstacleTakeOff = null;
		Double mu = null;
		Double muBrake = null;
		Double kRotation = null;
		Double kLiftOff = null;
		Double kCLmax = null;
		Double kDragDueToEngineFailure = null;
		Double kAlphaDot = null;
		Double alphaReductionRate = null;
		Amount<Length> obstacleLanding = null;
		Amount<Angle> thetaApproach = null;
		Double kApproach = null;
		Double kFlare = null;
		Double kTouchDown = null;
		Amount<Duration> freeRollDuration = null;
		
		//---------------------------------------------------------------
		// ALPHA GROUND
		String alphaGroundProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground");
		if(alphaGroundProperty != null)
			alphaGround = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground")), NonSI.DEGREE_ANGLE);
		
		//---------------------------------------------------------------
		// WIND SPEED
		String windSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway");
		if(windSpeedProperty != null)
			windSpeed = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway")), SI.METERS_PER_SECOND);
		
		//---------------------------------------------------------------
		// OBSTACLE TAKE-OFF
		String obstacleTakeOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off");
		if(obstacleTakeOffProperty != null)
			obstacleTakeOff = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off")), SI.METER);
		
		//===========================================================================================
		// BUILDING THE CALCULATOR ...
		ACPerformanceCalculator thePerformanceCalculator = new ACPerformanceCalculatorBuilder()
				.id(id)
				.aircraft(theAircraft)
				.operatingConditions(theOperatingConditions)
				.maximumTakeOffMass(maximumTakeOffMass)
				.maximumLandingMass(maximumLandingMass)
				.maximumZeroFuelMass(maximumZeroFuelMass)
				.operatingEmptyMass(operatingEmptyMass)
				.maximumFuelMass(maximumFuelMass)
				.cD0(cD0)
				.oswald(oswald)
				.cLmaxClean(cLmaxClean)
				.cLAlphaClean(cLAlphaClean)
				.deltaCD0HighLift(deltaCD0HighLift)
				.deltaCD0LandinGear(deltaCD0LandingGear)
				.cLmaxTakeOff(cLmaxTakeOff)
				.cLZeroTakeOff(cLZeroTakeOff)
				.cLmaxLanding(cLmaxLanding)
				// TODO : INSERT ALL THE REQUIRED DATA
				.build();
		
		return thePerformanceCalculator;
	}
	
	//------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------
	public String getId() {
		return _id;
	}
	public void setId(String _id) {
		this._id = _id;
	}
	public Aircraft getTheAircraft() {
		return _theAircraft;
	}
	public void setTheAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}
	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}
	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}
	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}
	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) {
		this._maximumTakeOffMass = _maximumTakeOffMass;
	}
	public Amount<Mass> getMaximumLandingMass() {
		return _maximumLandingMass;
	}
	public void setMaximumLandingMass(Amount<Mass> _maximumLandingMass) {
		this._maximumLandingMass = _maximumLandingMass;
	}
	public Amount<Mass> getMaximumZeroFuelMass() {
		return _maximumZeroFuelMass;
	}
	public void setMaximumZeroFuelMass(Amount<Mass> _maximumZeroFuelMass) {
		this._maximumZeroFuelMass = _maximumZeroFuelMass;
	}
	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}
	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass) {
		this._operatingEmptyMass = _operatingEmptyMass;
	}
	public Amount<Mass> getMaximumFuelMass() {
		return _maximumFuelMass;
	}
	public void setMaximumFuelMass(Amount<Mass> _maximumFuelMass) {
		this._maximumFuelMass = _maximumFuelMass;
	}
	public Double getCD0() {
		return _cD0;
	}
	public void setCD0(Double _cD0) {
		this._cD0 = _cD0;
	}
	public Double getOswald() {
		return _oswald;
	}
	public void setOswald(Double _oswald) {
		this._oswald = _oswald;
	}
	public Double getCLmaxClean() {
		return _cLmaxClean;
	}
	public void setCLmaxClean(Double _cLmaxClean) {
		this._cLmaxClean = _cLmaxClean;
	}
	public Amount<?> getCLAlphaClean() {
		return _cLAlphaClean;
	}
	public void setCLAlphaClean(Amount<?> _cLAlphaClean) {
		this._cLAlphaClean = _cLAlphaClean;
	}
	public Amount<Angle> getAlphaGround() {
		return _alphaGround;
	}
	public void setAlphaGround(Amount<Angle> _alphaGround) {
		this._alphaGround = _alphaGround;
	}
	public Amount<Velocity> getWindSpeed() {
		return _windSpeed;
	}
	public void setWindSpeed(Amount<Velocity> _windSpeed) {
		this._windSpeed = _windSpeed;
	}
	public Amount<Length> getObstacleTakeOff() {
		return _obstacleTakeOff;
	}
	public void setObstacleTakeOff(Amount<Length> _obstacleTakeOff) {
		this._obstacleTakeOff = _obstacleTakeOff;
	}
	public Double getMu() {
		return _mu;
	}
	public void setMu(Double _mu) {
		this._mu = _mu;
	}
	public Double getMuBrake() {
		return _muBrake;
	}
	public void setMuBrake(Double _muBrake) {
		this._muBrake = _muBrake;
	}
	public Double getDeltaCD0HighLift() {
		return _deltaCD0HighLift;
	}
	public void setDeltaCD0HighLift(Double _deltaCD0HighLift) {
		this._deltaCD0HighLift = _deltaCD0HighLift;
	}
	public Double getDeltaCD0LandingGear() {
		return _deltaCD0LandingGear;
	}
	public void setDeltaCD0LandingGear(Double _deltaCD0LandingGear) {
		this._deltaCD0LandingGear = _deltaCD0LandingGear;
	}
	public Double getCLmaxTakeOff() {
		return _cLmaxTakeOff;
	}
	public void setCLmaxTakeOff(Double _cLmaxTakeOff) {
		this._cLmaxTakeOff = _cLmaxTakeOff;
	}
	public Double getCLZeroTakeOff() {
		return _cLZeroTakeOff;
	}
	public void setCLZeroTakeOff(Double _cLZeroTakeOff) {
		this._cLZeroTakeOff = _cLZeroTakeOff;
	}
	public Double getKRotation() {
		return _kRotation;
	}
	public void setKRotation(Double _kRotation) {
		this._kRotation = _kRotation;
	}
	public Double getKLiftOff() {
		return _kLiftOff;
	}
	public void setKLiftOff(Double _kLiftOff) {
		this._kLiftOff = _kLiftOff;
	}
	public Double getKCLmax() {
		return _kCLmax;
	}
	public void setKCLmax(Double _kCLmax) {
		this._kCLmax = _kCLmax;
	}
	public Double getKDragDueToEngineFailure() {
		return _kDragDueToEnigneFailure;
	}
	public void setKDragDueToEngineFailure(Double _kDragDueToFailure) {
		this._kDragDueToEnigneFailure = _kDragDueToFailure;
	}
	public Double getKAlphaDot() {
		return _kAlphaDot;
	}
	public void setKAlphaDot(Double _kAlphaDot) {
		this._kAlphaDot = _kAlphaDot;
	}
	public Double getAlphaReductionRate() {
		return _alphaReductionRate;
	}
	public void setAlphaReductionRate(Double _alphaReductionRate) {
		this._alphaReductionRate = _alphaReductionRate;
	}
	public Double getCLmaxLanding() {
		return _cLmaxLanding;
	}
	public void setCLmaxLanding(Double _cLmaxLanding) {
		this._cLmaxLanding = _cLmaxLanding;
	}
	public Amount<Length> getObstacleLanding() {
		return _obstacleLanding;
	}
	public void setObstacleLanding(Amount<Length> _obstacleLanding) {
		this._obstacleLanding = _obstacleLanding;
	}
	public Amount<Angle> getThetaApproach() {
		return _thetaApproach;
	}
	public void setThetaApproach(Amount<Angle> _thetaApproach) {
		this._thetaApproach = _thetaApproach;
	}
	public Double getKApproach() {
		return _kApproach;
	}
	public void setKApproach(Double _kApproach) {
		this._kApproach = _kApproach;
	}
	public Double getKFlare() {
		return _kFlare;
	}
	public void setKFlare(Double _kFlare) {
		this._kFlare = _kFlare;
	}
	public Double getKTouchDown() {
		return _kTouchDown;
	}
	public void setKTouchDown(Double _kTouchDown) {
		this._kTouchDown = _kTouchDown;
	}
	public Amount<Duration> getFreeRollDuration() {
		return _freeRollDuration;
	}
	public void setFreeRollDuration(Amount<Duration> _freeRollDuration) {
		this._freeRollDuration = _freeRollDuration;
	}
	public Double getCLmaxInverted() {
		return _cLmaxInverted;
	}
	public void setCLmaxInverted(Double _cLmaxInverted) {
		this._cLmaxInverted = _cLmaxInverted;
	}
	public Double getThrottleSetting() {
		return _throttleSetting;
	}
	public void setThrottleSetting(Double _throttleSetting) {
		this._throttleSetting = _throttleSetting;
	}
}
