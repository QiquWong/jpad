package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import calculators.aerodynamics.DragCalc;
import calculators.performance.FlightManeuveringEnvelopeCalc;
import calculators.performance.LandingCalc;
import calculators.performance.PayloadRangeCalc;
import calculators.performance.TakeOffCalc;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXLSUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

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
	private Double _currentLiftingCoefficient;
	private Double _currentDragCoefficient;
	private Double _cD0;
	private Double _oswald;
	private Double _oswaldTakeOff;
	private Double _oswaldLanding;
	private Double _cLmaxClean;
	private Amount<?> _cLAlphaClean;
	private Amount<?> _cLAlphaTakeOff;
	private Amount<?> _cLAlphaLanding;
	private Double _deltaCD0TakeOff;
	private Double _deltaCD0Landing;
	private Double _deltaCD0LandingGear;
	private Double _deltaCD0Spoliers;
	private Double _cLmaxTakeOff;
	private Double _cLZeroTakeOff;
	private Double _cLmaxLanding;
	private Double _cLZeroLanding;
	private Double[] _polarCLCruise;
	private Double[] _polarCDCruise;
	private Double[] _polarCLTakeOff;
	private Double[] _polarCDTakeOff;
	private Double[] _polarCLLanding;
	private Double[] _polarCDLanding;	
	//..............................................................................
	// Take-off & Landing
	private Amount<Duration> _dtRotation;
	private Amount<Duration> _dtHold;
	private Amount<Angle> _alphaGround;
	private Amount<Velocity> _windSpeed;
	private Amount<Length> _obstacleTakeOff;
	private Double _mu;
	private Double _muBrake;
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
	// Other data
	private Double _cLmaxInverted;
	//..............................................................................
	// Plot and Task Maps
	private List<PerformanceEnum> _taskList;
	private List<PerformancePlotEnum> _plotList;

	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................
	// Take-Off
	private TakeOffCalc _theTakeOffCalculator;
	private Amount<Length> _takeOffDistanceAOE;
	private Amount<Length> _takeOffDistanceFAR25;
	private Amount<Length> _balancedFieldLength;
	private Amount<Length> _groundRollDistanceTakeOff;
	private Amount<Length> _rotationDistanceTakeOff;
	private Amount<Length> _airborneDistanceTakeOff;
	private Amount<Velocity> _vStallTakeOff;
	private Amount<Velocity> _vRotation;
	private Amount<Velocity> _vLiftOff;
	private Amount<Velocity> _v1;
	private Amount<Velocity> _v2;
	
	//..............................................................................
	// Climb
	// TODO
	
	//..............................................................................
	// Cruise
	// TODO
	
	//..............................................................................
	// Landing
	private LandingCalc _theLandingCalculator;
	private Amount<Length> _landingDistance;
	private Amount<Length> _landingDistanceFAR25;
	private Amount<Length> _groundRollDistanceLanding;
	private Amount<Length> _flareDistanceLanding;
	private Amount<Length> _airborneDistanceLanding;
	private Amount<Velocity> _vStallLanding;
	private Amount<Velocity> _vApproach;
	private Amount<Velocity> _vFlare;
	private Amount<Velocity> _vTouchDown;
	
	//..............................................................................
	// Payload-Range
	// TODO
	
	//..............................................................................
	// Maneuvering and Gust Flight Envelope 
	private FlightManeuveringEnvelopeCalc _theEnvelopeCalculator;
	private Amount<Velocity> _stallSpeedFullFlap;
	private Amount<Velocity> _stallSpeedClean;
	private Amount<Velocity> _stallSpeedInverted;
	private Amount<Velocity> _maneuveringSpeed;
	private Amount<Velocity> _maneuveringFlapSpeed;
	private Amount<Velocity> _maneuveringSpeedInverted;
	private Amount<Velocity> _designFlapSpeed;
	private Double _positiveLoadFactorManeuveringSpeed;
	private Double _positiveLoadFactorCruisingSpeed;
	private Double _positiveLoadFactorDiveSpeed;
	private Double _positiveLoadFactorDesignFlapSpeed;
	private Double _negativeLoadFactorManeuveringSpeedInverted;
	private Double _negativeLoadFactorCruisingSpeed;
	private Double _negativeLoadFactorDiveSpeed;
	private Double _positiveLoadFactorManeuveringSpeedWithGust;
	private Double _positiveLoadFactorCruisingSpeedWithGust;
	private Double _positiveLoadFactorDiveSpeedWithGust;
	private Double _positiveLoadFactorDesignFlapSpeedWithGust;
	private Double _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	private Double _negativeLoadFactorCruisingSpeedWithGust;
	private Double _negativeLoadFactorDiveSpeedWithGust;
	private Double _negativeLoadFactorDesignFlapSpeedWithGust;
	
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
		// Weights
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __maximumLandingMass;
		private Amount<Mass> __maximumZeroFuelMass;
		private Amount<Mass> __operatingEmptyMass;
		private Amount<Mass> __maximumFuelMass;
		//..............................................................................
		// Aerodynamics
		private Double __currentLiftingCoefficient;
		private Double __currentDragCoefficient;
		private Double __cD0;
		private Double __oswald;
		private Double __oswaldTakeOff;
		private Double __oswaldLanding;
		private Double __cLmaxClean;
		private Amount<?> __cLAlphaClean;
		private Amount<?> __cLAlphaTakeOff;
		private Amount<?> __cLAlphaLanding;
		private Double __deltaCD0TakeOff;
		private Double __deltaCD0Landing;
		private Double __deltaCD0LandingGear;
		private Double __deltaCD0Spoliers;
		private Double __cLmaxTakeOff;
		private Double __cLZeroTakeOff;
		private Double __cLmaxLanding;
		private Double __cLZeroLanding;
		private Double[] __polarCLCruise;
		private Double[] __polarCDCruise;
		private Double[] __polarCLTakeOff;
		private Double[] __polarCDTakeOff;
		private Double[] __polarCLLanding;
		private Double[] __polarCDLanding;	
		//..............................................................................
		// Take-off & Landing
		private Amount<Duration> __dtRotation = Amount.valueOf(3.0, SI.SECOND);
		private Amount<Duration> __dtHold = Amount.valueOf(0.5, SI.SECOND);
		private Amount<Angle> __alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		private Amount<Velocity> __windSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		private Amount<Length> __obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		private Double __mu = 0.03;
		private Double __muBrake = 0.4;
		private Double __kRotation = 1.05;
		private Double __kLiftOff = 1.1;
		private Double __kCLmax = 0.9;
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
		private Double __cLmaxInverted = -1.0;
		//..............................................................................
		private List<PerformanceEnum> __taskList = new ArrayList<PerformanceEnum>();
		private List<PerformancePlotEnum> __plotList = new ArrayList<PerformancePlotEnum>();
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
		
		public ACPerformanceCalculatorBuilder currentLiftingCoefficient(Double cL) {
			this.__currentLiftingCoefficient = cL;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder currentDragCoefficient(Double cD) {
			this.__currentDragCoefficient = cD;
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
		
		public ACPerformanceCalculatorBuilder oswaldTakeOff(Double eTO) {
			this.__oswaldTakeOff = eTO;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder oswaldLanding(Double eLND) {
			this.__oswaldLanding = eLND;
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
		
		public ACPerformanceCalculatorBuilder cLAlphaTakeOff(Amount<?> cLAlphaTakeOff) {
			this.__cLAlphaTakeOff = cLAlphaTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder cLAlphaLanding(Amount<?> cLAlphaLanding) {
			this.__cLAlphaLanding = cLAlphaLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCLCruise (Double[] polarCLCruise) {
			this.__polarCLCruise = polarCLCruise;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCDCruise (Double[] polarCDCruise) {
			this.__polarCDCruise = polarCDCruise;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCLTakeOff (Double[] polarCLTakeOff) {
			this.__polarCLTakeOff = polarCLTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCDTakeOff (Double[] polarCDTakeOff) {
			this.__polarCDTakeOff = polarCDTakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCLLanding (Double[] polarCLLanding) {
			this.__polarCLLanding = polarCLLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder polarCDLanding (Double[] polarCDLanding) {
			this.__polarCDLanding = polarCDLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder deltaCD0TakeOff(Double deltaCD0TakeOff) {
			this.__deltaCD0TakeOff = deltaCD0TakeOff;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder deltaCD0Landing(Double deltaCD0Landing) {
			this.__deltaCD0Landing = deltaCD0Landing;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder deltaCD0LandinGear(Double deltaCD0LandingGear) {
			this.__deltaCD0LandingGear = deltaCD0LandingGear;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder deltaCD0Spoilers(Double deltaCD0Spoilers) {
			this.__deltaCD0Spoliers = deltaCD0Spoilers;
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
		
		public ACPerformanceCalculatorBuilder cLZeroLanding(Double cLZeroLanding) {
			this.__cLZeroLanding = cLZeroLanding;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder dtRotation(Amount<Duration> dtRotation) {
			this.__dtRotation = dtRotation;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder dtHold(Amount<Duration> dtHold) {
			this.__dtHold = dtHold;
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
		
		public ACPerformanceCalculatorBuilder taskList(List<PerformanceEnum> taskList) {
			this.__taskList = taskList;
			return this;
		}
		
		public ACPerformanceCalculatorBuilder plotList(List<PerformancePlotEnum> plotList) {
			this.__plotList = plotList;
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
		
		this._currentLiftingCoefficient = builder.__currentLiftingCoefficient;
		this._currentDragCoefficient = builder.__currentDragCoefficient;
		this._cD0 = builder.__cD0;
		this._oswald = builder.__oswald;
		this._oswaldTakeOff = builder.__oswaldTakeOff;
		this._oswaldLanding = builder.__oswaldLanding;
		this._cLmaxClean = builder.__cLmaxClean;
		this._cLAlphaClean = builder.__cLAlphaClean;
		this._cLAlphaTakeOff = builder.__cLAlphaTakeOff;
		this._cLAlphaLanding = builder.__cLAlphaLanding;
		this._deltaCD0TakeOff = builder.__deltaCD0TakeOff;
		this._deltaCD0Landing = builder.__deltaCD0Landing;
		this._deltaCD0LandingGear = builder.__deltaCD0LandingGear;
		this._deltaCD0Spoliers = builder.__deltaCD0Spoliers;
		this._cLmaxTakeOff = builder.__cLmaxTakeOff;
		this._cLZeroTakeOff = builder.__cLZeroTakeOff;
		this._cLmaxLanding = builder.__cLmaxLanding;
		this._cLZeroLanding = builder.__cLZeroLanding;
		this._polarCLCruise = builder.__polarCLCruise;
		this._polarCDCruise = builder.__polarCDCruise;
		this._polarCLTakeOff = builder.__polarCLTakeOff;
		this._polarCDTakeOff = builder.__polarCDTakeOff;
		this._polarCLLanding = builder.__polarCLLanding;
		this._polarCDLanding = builder.__polarCDLanding;
		
		this._dtRotation = builder.__dtRotation;
		this._dtHold = builder.__dtHold;
		this._alphaGround = builder.__alphaGround;
		this._windSpeed = builder.__windSpeed;
		this._obstacleTakeOff = builder.__obstacleTakeOff;
		this._mu = builder.__mu;
		this._muBrake = builder.__muBrake;
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
		
		this._taskList = builder.__taskList;
		this._plotList = builder.__plotList;
		
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

				//...............................................................
				// MAXIMUM TAKE-OFF MASS
				Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "GLOBAL RESULTS");
				if(sheetGlobalData != null) {
					Cell maximumTakeOffMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Take-Off Mass").get(0)).getCell(2);
					if(maximumTakeOffMassCell != null)
						maximumTakeOffMass = Amount.valueOf(maximumTakeOffMassCell.getNumericCellValue(), SI.KILOGRAM);
				}
				//...............................................................
				// MAXIMUM LANDING MASS
				Cell maximumLandingMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Landing Mass").get(0)).getCell(2);
				if(maximumLandingMassCell != null)
					maximumLandingMass = Amount.valueOf(maximumLandingMassCell.getNumericCellValue(), SI.KILOGRAM);
				
				//...............................................................
				// MAXIMUM ZERO FUEL MASS
				Cell maximumZeroFuelMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Zero Fuel Mass").get(0)).getCell(2);
				if(maximumZeroFuelMassCell != null)
					maximumZeroFuelMass = Amount.valueOf(maximumZeroFuelMassCell.getNumericCellValue(), SI.KILOGRAM);

				//...............................................................
				// OPERATING EMPTY MASS
				Cell operatingEmptyMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Operating Empty Mass").get(0)).getCell(2);
				if(operatingEmptyMassCell != null)
					operatingEmptyMass = Amount.valueOf(operatingEmptyMassCell.getNumericCellValue(), SI.KILOGRAM);

				//...............................................................
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
			//...............................................................
			// MAXIMUM TAKE-OFF MASS
			String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_take_off_mass");
			if(maximumTakeOffMassProperty != null)
				maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_take_off_mass");
			
			//...............................................................
			// MAXIMUM LANDING MASS
			String maximumLandingMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_landing_mass");
			if(maximumLandingMassProperty != null)
				maximumLandingMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_landing_mass");
			
			//...............................................................
			// MAXIMUM ZERO FUEL MASS
			String maximumZeroFuelMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_zero_fuel_mass");
			if(maximumZeroFuelMassProperty != null)
				maximumZeroFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_zero_fuel_mass");
			
			//...............................................................
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//performance/weights/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/operating_empty_mass");
			
			//...............................................................
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

		Double currentLiftingCoefficient = null;
		Double currentDragCoefficient = null;
		Double cD0 = null;
		Double oswald = null;
		Double oswaldTakeOff = null;
		Double oswaldLanding = null;
		Double cLmaxClean = null;
		Amount<?> cLAlphaClean = null;
		Amount<?> cLAlphaTakeOff = null;
		Amount<?> cLAlphaLanding = null;
		Double deltaCD0TakeOff = null;
		Double deltaCD0Landing = null;
		Double deltaCD0LandingGears = null;
		Double deltaCD0Spoilers = null;
		Double cLmaxTakeOff = null;
		Double cLZeroTakeOff = null;
		Double cLmaxLanding = null;
		Double cLZeroLanding = null;
		Double[] polarCLCruise = null;
		Double[] polarCDCruise = null;
		Double[] polarCLTakeOff = null;
		Double[] polarCDTakeOff = null;
		Double[] polarCLLanding = null;
		Double[] polarCDLanding = null;
		
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
				
				////////////////////////////////////////////////////////////////////////////
				//																		  //
				// TODO : READ ALL REQUIRED DATA FROM THE AERODYNAMICS XML WHEN AVAILABLE //
				//																		  //
				////////////////////////////////////////////////////////////////////////////
			}
		}
		else {
			
			Boolean parabolicDragPolarFlag = Boolean.FALSE;
			String paraboliDragPolarProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//@parabolicDragPolar");
			if(paraboliDragPolarProperty.equalsIgnoreCase("TRUE"))
				parabolicDragPolarFlag = Boolean.TRUE;
			else if(paraboliDragPolarProperty.equalsIgnoreCase("FALSE"))
				parabolicDragPolarFlag = Boolean.FALSE;
			else {
				System.err.println("ERROR : parabolicDragPolar HAS TO BE TRUE/FALSE !");
				return null;
			}
			
			//...............................................................
			// CURRENT CL
			String currentLiftingCoefficientProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/current_lifting_coefficient");
			if(currentLiftingCoefficientProperty != null)
				currentLiftingCoefficient = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/current_lifting_coefficient"));
			//...............................................................
			// CURRENT CD
			String currentDragCoefficientProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/current_drag_coefficient");
			if(currentDragCoefficientProperty != null)
				currentDragCoefficient = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/current_drag_coefficient"));
			//...............................................................
			// CD0
			String cD0Property = reader.getXMLPropertyByPath("//performance/aerodynamics/cD0");
			if(cD0Property != null)
				cD0 = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cD0"));
			//...............................................................
			// OSWALD
			String oswladProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald");
			if(oswladProperty != null)
				oswald = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald"));
			//...............................................................
			// OSWALD TO
			String oswladTOProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_take_off");
			if(oswladTOProperty != null)
				oswaldTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_take_off"));
			//...............................................................
			// OSWALD LND
			String oswladLNDProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_landing");
			if(oswladLNDProperty != null)
				oswaldLanding = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/oswald_landing"));
			//...............................................................
			// CLmax CLEAN
			String cLmaxCleanProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_clean_configuration");
			if(cLmaxCleanProperty != null)
				cLmaxClean = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_clean_configuration"));
			//...............................................................
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
			//...............................................................
			// CLalpha TAKE-OFF
			String cLAlphaTakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL_alpha_take_off");
			if(cLAlphaTakeOffProperty != null)
				cLAlphaTakeOff = Amount.valueOf(
							Double.valueOf(
									reader.getXMLPropertyByPath(
											"//performance/aerodynamics/cL_alpha_take_off"
											)
									),
									NonSI.DEGREE_ANGLE.inverse()
									);
			//...............................................................
			// CLalpha LANDING
			String cLAlphaLandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL_alpha_landing");
			if(cLAlphaLandingProperty != null)
				cLAlphaLanding = Amount.valueOf(
							Double.valueOf(
									reader.getXMLPropertyByPath(
											"//performance/aerodynamics/cL_alpha_landing"
											)
									),
									NonSI.DEGREE_ANGLE.inverse()
									);
			//...............................................................
			// DeltaCD0 TAKE-OFF
			String deltaCD0TakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_take_off");
			if(deltaCD0TakeOffProperty != null)
				deltaCD0TakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_take_off"));
			//...............................................................
			// DeltaCD0 LANDING
			String deltaCD0LandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing");
			if(deltaCD0LandingProperty != null)
				deltaCD0Landing = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing"));
			//...............................................................
			// DeltaCD0 LANDING GEARS
			String deltaCD0LandingGearsProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing_gears");
			if(deltaCD0LandingGearsProperty != null)
				deltaCD0LandingGears = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_landing_gears"));
			//...............................................................
			// DeltaCD0 SPOILERS
			String deltaCD0SpoilersProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_spoilers");
			if(deltaCD0SpoilersProperty != null)
				deltaCD0Spoilers = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/delta_CD0_spoilers"));
			//...............................................................
			// CLmax TAKE-OFF
			String cLmaxTakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_take_off_configuration");
			if(cLmaxTakeOffProperty != null)
				cLmaxTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_take_off_configuration"));
			//...............................................................
			// CL0 TAKE-OFF
			String cL0TakeOffProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration");
			if(cL0TakeOffProperty != null)
				cLZeroTakeOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration"));
			//...............................................................
			// CLmax LANDING
			String cLmaxLandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_landing_configuration");
			if(cLmaxLandingProperty != null)
				cLmaxLanding = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cLmax_landing_configuration"));
			//...............................................................
			// CL0 LANDING
			String cL0LandingProperty = reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_take_off_configuration");
			if(cL0LandingProperty != null)
				cLZeroLanding = Double.valueOf(reader.getXMLPropertyByPath("//performance/aerodynamics/cL0_landing_configuration"));
			
			if(parabolicDragPolarFlag == Boolean.FALSE) {
				
				//...............................................................
				// POLAR CURVE CRUISE
				List<String> polarCLCruiseProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/cruise/polar_curve_CL");
				if(!polarCLCruiseProperty.isEmpty()) {
					polarCLCruise = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/cruise/polar_curve_CL")
							); 
				}
				List<String> polarCDCruiseProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/cruise/polar_curve_CD");
				if(!polarCDCruiseProperty.isEmpty()) {
					polarCDCruise = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/cruise/polar_curve_CD")
							); 
				}
				//...............................................................
				// POLAR CURVE TAKE-OFF
				List<String> polarCLTakeOffProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/take_off/polar_curve_CL");
				if(!polarCLTakeOffProperty.isEmpty()) {
					polarCLTakeOff = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/take_off/polar_curve_CL")
							); 
				}
				List<String> polarCDTakeOffProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/take_off/polar_curve_CD");
				if(!polarCDTakeOffProperty.isEmpty()) {
					polarCDTakeOff = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/take_off/polar_curve_CD")
							); 
				}
				//...............................................................
				// POLAR CURVE LANDING
				List<String> polarCLLandingProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/landing/polar_curve_CL");
				if(!polarCLLandingProperty.isEmpty()) {
					polarCLLanding = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/landing/polar_curve_CL")
							);
				}
				List<String> polarCDLandingProperty = reader.getXMLPropertiesByPath("//performance/aerodynamics/polar_curves/landing/polar_curve_CD");
				if(!polarCDLandingProperty.isEmpty()) {
					polarCDLanding = MyArrayUtils.convertListOfDoubleToDoubleArray(
							reader.readArrayDoubleFromXML("//performance/aerodynamics/polar_curves/landing/polar_curve_CD")
							); 
				}
			}
			else {
				
				int numberOfPolarPoints = 50;
				
				//...............................................................
				// POLAR CURVE CRUISE
				polarCLCruise = MyArrayUtils.linspaceDouble(-0.2, cLmaxClean+0.2, numberOfPolarPoints);				
				polarCDCruise = new Double[polarCLCruise.length];
				
				//...............................................................
				// POLAR CURVE TAKE-OFF
				polarCLTakeOff = MyArrayUtils.linspaceDouble(-0.2, cLmaxTakeOff+0.2, numberOfPolarPoints);
				polarCDTakeOff = new Double[polarCLTakeOff.length]; 
				
				//...............................................................
				// POLAR CURVE LANDING
				polarCLLanding = MyArrayUtils.linspaceDouble(-0.2, cLmaxLanding+0.2, numberOfPolarPoints);
				polarCDLanding = new Double[polarCLLanding.length];
				
				// building the CD arrays...
				for(int i=0; i<numberOfPolarPoints; i++) {
					polarCDCruise[i] = DragCalc.calculateCDTotal(
							cD0,
							polarCLCruise[i],
							theAircraft.getWing().getAspectRatio(),
							oswald, 
							theOperatingConditions.getMachCruise(),
							DragCalc.calculateCDWaveLockKornCriticalMachKroo(
									polarCLCruise[i],
									theOperatingConditions.getMachCruise(),
									theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
									theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
									theAircraft.getWing().getAirfoilList().get(0).getType()
									)
							);
					polarCDTakeOff[i] = DragCalc.calculateCDTotal(
							cD0 + deltaCD0TakeOff + deltaCD0LandingGears,
							polarCLTakeOff[i],
							theAircraft.getWing().getAspectRatio(),
							oswaldTakeOff, 
							theOperatingConditions.getMachCruise(),
							DragCalc.calculateCDWaveLockKornCriticalMachKroo(
									polarCLTakeOff[i],
									theOperatingConditions.getMachCruise(),
									theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
									theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
									theAircraft.getWing().getAirfoilList().get(0).getType()
									)
							);
					polarCDLanding[i] = DragCalc.calculateCDTotal(
							cD0 + deltaCD0Landing + deltaCD0LandingGears + deltaCD0Spoilers,
							polarCLLanding[i],
							theAircraft.getWing().getAspectRatio(),
							oswaldLanding, 
							theOperatingConditions.getMachCruise(),
							DragCalc.calculateCDWaveLockKornCriticalMachKroo(
									polarCLLanding[i],
									theOperatingConditions.getMachCruise(),
									theAircraft.getWing().getSweepHalfChordEquivalent(false).doubleValue(SI.RADIAN),
									theAircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getThicknessToChordRatio(),
									theAircraft.getWing().getAirfoilList().get(0).getType()
									)
							);
				}
			}
		}
		
		//===========================================================================================
		// READING TAKE-OFF AND LANDING DATA ...	

		// default values
		Amount<Duration> dtRotation = Amount.valueOf(3.0, SI.SECOND);
		Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
		Amount<Angle> alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Velocity> windSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		Amount<Length> obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		Double mu = 0.03;
		Double muBrake = 0.4;
		Double kRotation = 1.05;
		Double kLiftOff = 1.1;
		Double kCLmax = 0.9;
		Double kDragDueToEngineFailure = 1.1;
		Double kAlphaDot = 0.04;
		Double alphaReductionRate = -4.0;
		Amount<Length> obstacleLanding = Amount.valueOf(50, NonSI.FOOT).to(SI.METER);
		Amount<Angle> thetaApproach = Amount.valueOf(3.0, NonSI.DEGREE_ANGLE);
		Double kApproach = 1.3;
		Double kFlare = 1.23;
		Double kTouchDown = 1.15;
		Amount<Duration> freeRollDuration = Amount.valueOf(2.0, SI.SECOND);
		
		//...............................................................
		// dt ROTATION
		String dtRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_rotation");
		if(dtRotationProperty != null)
			dtRotation = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_rotation")), SI.SECOND);				
		
		//...............................................................
		// dt HOLD
		String dtHoldProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_hold");
		if(dtHoldProperty != null)
			dtHold = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_hold")), SI.SECOND);				
		
		//...............................................................
		// ALPHA GROUND
		String alphaGroundProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground");
		if(alphaGroundProperty != null)
			alphaGround = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground")), NonSI.DEGREE_ANGLE);
		
		//...............................................................
		// WIND SPEED
		String windSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway");
		if(windSpeedProperty != null)
			windSpeed = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway")), SI.METERS_PER_SECOND);
		
		//...............................................................
		// OBSTACLE TAKE-OFF
		String obstacleTakeOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off");
		if(obstacleTakeOffProperty != null)
			obstacleTakeOff = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off")), SI.METER);
		
		//...............................................................
		// WHEELS FRICTION COEFFICIENT
		String wheelsFrictionCoefficientProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient");
		if(wheelsFrictionCoefficientProperty != null)
			mu = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient"));
		
		//...............................................................
		// WHEELS FRICTION COEFFICIENT WITH BRAKES
		String wheelsFrictionCoefficientWithBrakesProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes");
		if(wheelsFrictionCoefficientWithBrakesProperty != null)
			muBrake = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes"));
		
		//...............................................................
		// K ROTATION
		String kRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation");
		if(kRotationProperty != null)
			kRotation = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation"));
		
		//...............................................................
		// K LIFT OFF
		String kLiftOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_lift_off");
		if(kLiftOffProperty != null)
			kLiftOff = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_lift_off"));
		
		//...............................................................
		// K CLmax
		String kCLmaxProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_cLmax");
		if(kCLmaxProperty != null)
			kCLmax = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_cLmax"));
		
		//...............................................................
		// K DRAG DUE TO ENGINE FAILURE
		String kDragDueToEngineFailureProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_drag_due_to_engine_failure");
		if(kDragDueToEngineFailureProperty != null)
			kDragDueToEngineFailure = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_drag_due_to_engine_failure"));

		//...............................................................
		// K ALPHA DOT
		String kAlphaDotProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_alpha_dot");
		if(kAlphaDotProperty != null)
			kAlphaDot = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_alpha_dot"));

		//...............................................................
		// ALPHA REDUCTION RATE
		String alphaReductionRateProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_reduction_rate");
		if(alphaReductionRateProperty != null)
			alphaReductionRate = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_reduction_rate"));

		//...............................................................
		// OBSTACLE LANDING
		String obstacleLandingProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_landing");
		if(obstacleLandingProperty != null)
			obstacleLanding = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_landing")), SI.METER);		
		
		//...............................................................
		// THETA APPROACH
		String thetaApproachProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/theta_approach");
		if(thetaApproachProperty != null)
			thetaApproach = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/theta_approach")), NonSI.DEGREE_ANGLE);		
		
		//...............................................................
		// K APPROACH
		String kApproachProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_approach");
		if(kApproachProperty != null)
			kApproach = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_approach"));
		
		//...............................................................
		// K FLARE
		String kFlareProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_flare");
		if(kFlareProperty != null)
			kFlare = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_flare"));
		
		//...............................................................
		// K TOUCH DOWN
		String kTouchDownProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_touch_down");
		if(kTouchDownProperty != null)
			kTouchDown = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_touch_down"));
		
		//...............................................................
		// FREE ROLL DURATION
		String freeRollDurationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/free_roll_duration");
		if(freeRollDurationProperty != null)
			freeRollDuration = Amount.valueOf(Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/free_roll_duration")), SI.SECOND);
		
		//===========================================================================================
		// READING OTHER DATA ...	
		Double cLmaxInverted = -1.0;
		
		//...............................................................
		// CL MAX INVERTED
		String cLmaxInvertedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/cLmax_inverted");
		if(cLmaxInvertedProperty != null)
			cLmaxInverted = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/cLmax_inverted"));
		
		//===========================================================================================
		// READING PLOT LIST ...	
		List<PerformancePlotEnum> plotList = new ArrayList<PerformancePlotEnum>();
		if(theAircraft.getTheAnalysisManager().getPlotPerformance() == Boolean.TRUE) {
			
			//...............................................................
			// TAKE-OFF
			String takeOffSimulationsProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/takeoff/simulations/@perform");
			if (takeOffSimulationsProperty != null) {
				if(takeOffSimulationsProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.TAKE_OFF_SIMULATIONS);
				}
			
			String balancedFieldLengthChartProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/takeoff/balanced_field_length/@perform");
			if (balancedFieldLengthChartProperty != null) {
				if(balancedFieldLengthChartProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.BALANCED_FIELD_LENGTH);
				}
				
			//...............................................................
			// CLIMB 
			
			// TODO : COMPLETE ME !!
			
			//...............................................................
			// CRUISE
			String thrustDragChartProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/cruise/thrust_drag/@perform");
			if (thrustDragChartProperty != null) {
				if(thrustDragChartProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.THRUST_DRAG_CURVES);
				}
			
			String efficiencyChartProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/cruise/efficiency/@perform");
			if (efficiencyChartProperty != null) {
				if(efficiencyChartProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.EFFICIENCY_CURVES);
				}
			
			String sfcChartProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/cruise/sfc/@perform");
			if (sfcChartProperty != null) {
				if(sfcChartProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.SFC_CURVE);
				}
			
			String cruiseGridChartProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/cruise/cruise_grid/@perform");
			if (cruiseGridChartProperty != null) {
				if(cruiseGridChartProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.CRUISE_GRID_CHART);
				}
			
			String cruiseFlightEnvelopeChartProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/cruise/flight_envelope/@perform");
			if (cruiseFlightEnvelopeChartProperty != null) {
				if(cruiseFlightEnvelopeChartProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.CRUISE_FLIGHT_ENVELOPE);
				}
			
			//...............................................................
			// LANDING
			String landingSimulationsProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/landing/simulations/@perform");
			if (landingSimulationsProperty != null) {
				if(landingSimulationsProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.LANDING_SIMULATIONS);
				}
			
			//...............................................................
			// PAYLOAD-RANGE
			String payloadRangeProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/payload_range/@perform");
			if (payloadRangeProperty != null) {
				if(payloadRangeProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.PAYLOAD_RANGE);
				}
			
			//...............................................................
			// V-n DIAGRAM
			String maneuveringFlightAndGustEnvelopeProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//plot/maneuvering_flight_and_gust_envelope/@perform");
			if (maneuveringFlightAndGustEnvelopeProperty != null) {
				if(maneuveringFlightAndGustEnvelopeProperty.equalsIgnoreCase("TRUE")) 
					plotList.add(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM);
				}
		}
		
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
				.currentLiftingCoefficient(currentLiftingCoefficient)
				.currentDragCoefficient(currentDragCoefficient)
				.cD0(cD0)
				.oswald(oswald)
				.oswaldTakeOff(oswaldTakeOff)
				.oswaldLanding(oswaldLanding)
				.cLmaxClean(cLmaxClean)
				.cLAlphaClean(cLAlphaClean)
				.cLAlphaTakeOff(cLAlphaTakeOff)
				.cLAlphaLanding(cLAlphaLanding)
				.deltaCD0TakeOff(deltaCD0TakeOff)
				.deltaCD0Landing(deltaCD0Landing)
				.deltaCD0LandinGear(deltaCD0LandingGears)
				.deltaCD0Spoilers(deltaCD0Spoilers)
				.cLmaxTakeOff(cLmaxTakeOff)
				.cLZeroTakeOff(cLZeroTakeOff)
				.cLmaxLanding(cLmaxLanding)
				.cLZeroLanding(cLZeroLanding)
				.polarCLCruise(polarCLCruise)
				.polarCDCruise(polarCDCruise)
				.polarCLTakeOff(polarCLTakeOff)
				.polarCDTakeOff(polarCDTakeOff)
				.polarCLLanding(polarCLLanding)
				.polarCDLanding(polarCDLanding)
				.dtRotation(dtRotation)
				.dtHold(dtHold)
				.alphaGround(alphaGround)
				.windSpeed(windSpeed)
				.obstacleTakeOff(obstacleTakeOff)
				.mu(mu)
				.muBrake(muBrake)
				.kRotation(kRotation)
				.kLiftOff(kLiftOff)
				.kCLmax(kCLmax)
				.kDragDueToEngineFailure(kDragDueToEngineFailure)
				.kAlphaDot(kAlphaDot)
				.alphaReductionRate(alphaReductionRate)
				.obstacleLanding(obstacleLanding)
				.thetaApproach(thetaApproach)
				.kApproach(kApproach)
				.kFlare(kFlare)
				.kTouchDown(kTouchDown)
				.freeRollDuration(freeRollDuration)
				.cLmaxInverted(cLmaxInverted)
				// TODO : INSERT ALL THE REQUIRED DATA
				.taskList(theAircraft.getTheAnalysisManager().getTaskListPerformance())
				.plotList(plotList)
				.build();
		
		return thePerformanceCalculator;
	}
	
	//////////////////////////////////////////////////
	//								 		   		//
	// TODO:						 		   		//
	// -ADD THE toString AND THE toXLS METHODS  	//
	// -IMPLEMENT THE CALCULATOR AOE AND OEI 		//
	// 	WHICH CALLS FOR THE REQUIRED NESTED CLASSES	//
	// -IMPLEMENT THE INNER CLASSES					//
	//								 		   		//
	//////////////////////////////////////////////////
	
	/**
	 * This method reads the task list, initializes the related calculators inner classes and 
	 * performe the required calculation
	 */
	public void calculatePerformance(String resultsFolderPath) {
		
		String performanceFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "PERFORMANCE"
				+ File.separator
				);
		
		if(_taskList.contains(PerformanceEnum.TAKE_OFF)) {
			
			String takeOffFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "TAKE_OFF"
					+ File.separator
					);
			
			CalcTakeOff calcTakeOff = new CalcTakeOff();
			calcTakeOff.performTakeOffSimulation(takeOffFolderPath);
			
		}
		
		if(_taskList.contains(PerformanceEnum.LANDING)) {
			
			String landingFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "LANDING"
					+ File.separator
					);
			
			CalcLanding calcLanding = new CalcLanding();
			calcLanding.performLandingSimulation(landingFolderPath);
			
		}
		
		if(_taskList.contains(PerformanceEnum.V_n_DIAGRAM)) {
			
			String maneuveringFlightAndGustEnvelopeFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "V-n_DIAGRAM"
					+ File.separator
					);
			
			CalcFlightManeuveringAndGustEnvelope calcEnvelope =  new CalcFlightManeuveringAndGustEnvelope();
			calcEnvelope.fromRegulations(maneuveringFlightAndGustEnvelopeFolderPath);
			
		}
		
		// TODO : CONTINUE WITH OTHER ANALYSES
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tPerformance Analysis\n")
				.append("\t-------------------------------------\n")
				;
		if(_taskList.contains(PerformanceEnum.TAKE_OFF)) {
			
			sb.append("\tTAKE-OFF\n")
			.append("\t-------------------------------------\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceTakeOff + "\n")
			.append("\t\tRotation distance = " + _rotationDistanceTakeOff + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceTakeOff + "\n")
			.append("\t\tAOE take-off distance = " + _takeOffDistanceAOE + "\n")
			.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25 + "\n")
			.append("\t\tBalanced field length = " + _balancedFieldLength + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOff + "\n")
			.append("\t\tDecision speed (V1) = " + _v1 + "\n")
			.append("\t\tRotation speed (V_Rot) = " + _vRotation + "\n")
			.append("\t\tLift-off speed (V_LO) = " + _vLiftOff + "\n")
			.append("\t\tTake-off safety speed (V2) = " + _v2 + "\n")
			.append("\t-------------------------------------\n")
			;
			
		}
		if(_taskList.contains(PerformanceEnum.LANDING)) {
			
			sb.append("\tLANDING\n")
			.append("\t-------------------------------------\n")
			.append("\t\tGround roll distance = " + _groundRollDistanceLanding + "\n")
			.append("\t\tFlare distance = " + _flareDistanceLanding + "\n")
			.append("\t\tAirborne distance = " + _airborneDistanceLanding + "\n")
			.append("\t\tLanding distance = " + _landingDistance + "\n")
			.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25 + "\n")
			.append("\t\t.....................................\n")
			.append("\t\tStall speed landing (VsLND)= " + _vStallLanding + "\n")
			.append("\t\tTouchdown speed (V_TD) = " + _vTouchDown + "\n")
			.append("\t\tFlare speed (V_Flare) = " + _vFlare + "\n")
			.append("\t\tApproach speed (V_Approach) = " + _vApproach + "\n")
			.append("\t-------------------------------------\n")
			;
			
		}
		if(_taskList.contains(PerformanceEnum.V_n_DIAGRAM)) {
			sb.append("\tV-n DIAGRAM\n")
			.append(_theEnvelopeCalculator.toString());
		}
		
		// TODO : COMPLETE ME !
		
		return sb.toString();
	}
	
	
	//............................................................................
	// TAKE-OFF INNER CLASS
	//............................................................................
	public class CalcTakeOff {
		
		public void performTakeOffSimulation(String takeOffFolderPath) {
			
			Amount<Length> wingToGroundDistance = 
					_theAircraft.getFuselage().getHeightFromGround()
					.plus(_theAircraft.getFuselage().getSectionHeight().divide(2))
					.plus(_theAircraft.getWing().getZApexConstructionAxes()
							.plus(_theAircraft.getWing().getSemiSpan()
									.times(Math.sin(
											_theAircraft.getWing()	
											.getLiftingSurfaceCreator()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			double deltaCD0HighLiftAndLandingGears = _deltaCD0TakeOff + _deltaCD0LandingGear;
			
			_theTakeOffCalculator = new TakeOffCalc(
					_theAircraft,
					_theOperatingConditions,
					_maximumTakeOffMass,
					_dtRotation,
					_dtHold,
					_kCLmax,
					_kRotation,
					_kLiftOff,
					_kDragDueToEnigneFailure,
					_theOperatingConditions.getThrottleTakeOff(), // throttle setting
					_kAlphaDot,
					_alphaReductionRate,
					_mu,
					_muBrake,
					wingToGroundDistance,
					_obstacleTakeOff,
					_windSpeed,
					_alphaGround,
					_theAircraft.getWing().getRiggingAngle(),
					_cD0,
					_oswaldTakeOff,
					_cLmaxTakeOff,
					_cLZeroTakeOff,
					_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
					deltaCD0HighLiftAndLandingGears
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theTakeOffCalculator.calculateTakeOffDistanceODE(null, false);

			// Distances:
			_groundRollDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(0);
			_rotationDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(1).minus(_groundRollDistanceTakeOff);
			_airborneDistanceTakeOff = _theTakeOffCalculator.getTakeOffResults().getGroundDistance().get(2).minus(_rotationDistanceTakeOff).minus(_groundRollDistanceTakeOff);
			_takeOffDistanceAOE = _groundRollDistanceTakeOff.plus(_rotationDistanceTakeOff).plus(_airborneDistanceTakeOff);
			_takeOffDistanceFAR25 = _takeOffDistanceAOE.times(1.15);
			
			// Velocities:
			_vStallTakeOff = _theTakeOffCalculator.getvSTakeOff();
			_vRotation = _theTakeOffCalculator.getvRot();
			_vLiftOff = _theTakeOffCalculator.getvLO();
			_v2 = _theTakeOffCalculator.getTakeOffResults().getSpeed().get(2);
			
			if(_plotList.contains(PerformancePlotEnum.TAKE_OFF_SIMULATIONS))
				try {
					_theTakeOffCalculator.createTakeOffCharts(takeOffFolderPath);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			
			//------------------------------------------------------------
			// BALANCED FIELD LENGTH
			_theTakeOffCalculator.calculateBalancedFieldLength();
			
			_v1 = _theTakeOffCalculator.getV1();
			_balancedFieldLength = _theTakeOffCalculator.getBalancedFieldLength();
			
			if(_plotList.contains(PerformancePlotEnum.BALANCED_FIELD_LENGTH))
				_theTakeOffCalculator.createBalancedFieldLengthChart(takeOffFolderPath);
		}
		
	}
	//............................................................................
	// END OF THE TAKE-OFF INNER CLASS
	//............................................................................
	
	//............................................................................
	// LANDING INNER CLASS
	//............................................................................
	public class CalcLanding {
		
		public void performLandingSimulation(String landingFolderPath) {
		
			Amount<Length> wingToGroundDistance = 
					_theAircraft.getFuselage().getHeightFromGround()
					.plus(_theAircraft.getFuselage().getSectionHeight().divide(2))
					.plus(_theAircraft.getWing().getZApexConstructionAxes()
							.plus(_theAircraft.getWing().getSemiSpan()
									.times(Math.sin(
											_theAircraft.getWing()	
											.getLiftingSurfaceCreator()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			double deltaCD0HighLiftLandingGearsAndSpoilers = _deltaCD0Landing + _deltaCD0LandingGear + _deltaCD0Spoliers;
			
			_theLandingCalculator = new LandingCalc(
					_theAircraft, 
					_theOperatingConditions,
					_maximumLandingMass,
					_kApproach,
					_kFlare,
					_kTouchDown,
					_mu,
					_muBrake,
					wingToGroundDistance,
					_obstacleLanding, 
					_windSpeed,
					_alphaGround,
					_theAircraft.getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE),
					_thetaApproach,
					_cD0, 
					_oswaldLanding, 
					_cLmaxLanding,
					_cLZeroLanding,
					_cLAlphaTakeOff.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
					deltaCD0HighLiftLandingGearsAndSpoilers,
					_theOperatingConditions.getReverseThrottleLanding(),
					_freeRollDuration
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theLandingCalculator.calculateLandingDistance();
			
			// Distances:
			_groundRollDistanceLanding = _theLandingCalculator.getsGround();
			_flareDistanceLanding = _theLandingCalculator.getsFlare();
			_airborneDistanceLanding = _theLandingCalculator.getsApproach();
			_landingDistance = _theLandingCalculator.getsTotal();
			_landingDistanceFAR25 = _theLandingCalculator.getsTotal().divide(0.6);
			
			// Velocities:
			_vStallLanding = _theLandingCalculator.getvSLanding();
			_vTouchDown = _theLandingCalculator.getvTD();
			_vFlare = _theLandingCalculator.getvFlare();
			_vApproach = _theLandingCalculator.getvA();
			
			if(_plotList.contains(PerformancePlotEnum.LANDING_SIMULATIONS)) {
				try {
					_theLandingCalculator.createLandingCharts(landingFolderPath);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	//............................................................................
	// END OF THE LANDING INNER CLASS
	//............................................................................
	
	//............................................................................
	// PAYLOAD-RANGE INNER CLASS
	//............................................................................
	public class CalcPayloadRange {
		
		public void fromRangeBreguet() {
			
					
			
		}
		
	}
	//............................................................................
	// END OF THE PAYLOAD-RANGE INNER CLASS
	//............................................................................
	
	//............................................................................
	// FLIGHT MANEUVERING AND GUST ENVELOPE INNER CLASS
	//............................................................................
	public class CalcFlightManeuveringAndGustEnvelope {
		
		public void fromRegulations(String maneuveringEnvelopeFolderPath) {
			
			_theEnvelopeCalculator = new FlightManeuveringEnvelopeCalc(
					_theAircraft.getRegulations(),
					_theAircraft.getTypeVehicle(),
					_cLmaxClean,
					_cLmaxLanding,
					_cLmaxInverted,
					_theAircraft.getTheAnalysisManager().getPositiveLimitLoadFactor(),
					_theAircraft.getTheAnalysisManager().getNegativeLimitLoadFactor(),
					_theAircraft.getTheAnalysisManager().getVMaxCruise(),
					_theAircraft.getTheAnalysisManager().getVDive(),
					_cLAlphaClean.to(SI.RADIAN),
					_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord(),
					_theOperatingConditions.getAltitudeCruise(),
					_maximumTakeOffMass,
					_maximumLandingMass,
					_theAircraft.getWing().getSurface()
					);
			
			_theEnvelopeCalculator.calculateManeuveringEnvelope();
			
			_stallSpeedClean = _theEnvelopeCalculator.getStallSpeedClean();
			_stallSpeedFullFlap = _theEnvelopeCalculator.getStallSpeedFullFlap();
			_stallSpeedInverted = _theEnvelopeCalculator.getStallSpeedInverted();
			_maneuveringSpeed = _theEnvelopeCalculator.getManeuveringSpeed();
			_maneuveringFlapSpeed = _theEnvelopeCalculator.getManeuveringFlapSpeed();
			_maneuveringSpeedInverted = _theEnvelopeCalculator.getManeuveringSpeedInverted();
			_designFlapSpeed = _theEnvelopeCalculator.getDesignFlapSpeed();
			_positiveLoadFactorManeuveringSpeed = _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeed();
			_positiveLoadFactorCruisingSpeed = _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeed();
			_positiveLoadFactorDiveSpeed = _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeed();
			_positiveLoadFactorDesignFlapSpeed = _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeed();
			_negativeLoadFactorManeuveringSpeedInverted = _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInverted();
			_negativeLoadFactorCruisingSpeed = _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeed();
			_negativeLoadFactorDiveSpeed = _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeed();
			_positiveLoadFactorManeuveringSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorManeuveringSpeedWithGust();
			_positiveLoadFactorCruisingSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorCruisingSpeedWithGust();
			_positiveLoadFactorDiveSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorDiveSpeedWithGust();
			_positiveLoadFactorDesignFlapSpeedWithGust = _theEnvelopeCalculator.getPositiveLoadFactorDesignFlapSpeedWithGust();
			_negativeLoadFactorManeuveringSpeedInvertedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorManeuveringSpeedInvertedWithGust();
			_negativeLoadFactorCruisingSpeedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorCruisingSpeedWithGust();
			_negativeLoadFactorDiveSpeedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorDiveSpeedWithGust();
			_negativeLoadFactorDesignFlapSpeedWithGust = _theEnvelopeCalculator.getNegativeLoadFactorDesignFlapSpeedWithGust();
		
			if(_plotList.contains(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM)) 
				_theEnvelopeCalculator.plotManeuveringEnvelope(maneuveringEnvelopeFolderPath);;
			
		}
		
	}
	//............................................................................
	// END OF THE FLIGHT MANEUVERING AND GUST ENVELOPE INNER CLASS
	//............................................................................	
	
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
	public Double getCurrentLiftingCoefficient() {
		return _currentLiftingCoefficient;
	}
	public void setCurrentLiftingCoefficient(Double _currentLiftingCoefficient) {
		this._currentLiftingCoefficient = _currentLiftingCoefficient;
	}
	public Double getCurrentDragCoefficient() {
		return _currentDragCoefficient;
	}
	public void setCurrentDragCoefficient(Double _currentDragCoefficient) {
		this._currentDragCoefficient = _currentDragCoefficient;
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
	public Double getOswaldTakeOff() {
		return _oswaldTakeOff;
	}

	public void setOswaldTakeOff(Double _oswaldTakeOff) {
		this._oswaldTakeOff = _oswaldTakeOff;
	}

	public Double getOswaldLanding() {
		return _oswaldLanding;
	}

	public void setOswaldLanding(Double _oswaldLanding) {
		this._oswaldLanding = _oswaldLanding;
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
	public Amount<?> getCLAlphaTakeOff() {
		return _cLAlphaTakeOff;
	}
	public void setCLAlphaTakeOff(Amount<?> _cLAlphaHighLift) {
		this._cLAlphaTakeOff = _cLAlphaHighLift;
	}
	public Amount<?> getCLAlphaLanding() {
		return _cLAlphaLanding;
	}
	public void setCLAlphaLanding(Amount<?> _cLAlphaLanding) {
		this._cLAlphaLanding = _cLAlphaLanding;
	}
	public Amount<Duration> getDtRotation() {
		return _dtRotation;
	}
	public void setDtRotation(Amount<Duration> _dtRotation) {
		this._dtRotation = _dtRotation;
	}
	public Amount<Duration> getDtHold() {
		return _dtHold;
	}
	public void setDtHold(Amount<Duration> _dtHold) {
		this._dtHold = _dtHold;
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
		return _deltaCD0TakeOff;
	}
	public void setDeltaCD0TakeOff(Double _deltaCD0TakeOff) {
		this._deltaCD0TakeOff = _deltaCD0TakeOff;
	}
	public Double getDeltaCD0Landing() {
		return _deltaCD0Landing;
	}
	public void setDeltaCD0Landing(Double _deltaCD0Landing) {
		this._deltaCD0Landing = _deltaCD0Landing;
	}
	public Double getDeltaCD0LandingGear() {
		return _deltaCD0LandingGear;
	}
	public void setDeltaCD0LandingGear(Double _deltaCD0LandingGear) {
		this._deltaCD0LandingGear = _deltaCD0LandingGear;
	}
	public Double getDeltaCD0Spoliers() {
		return _deltaCD0Spoliers;
	}

	public void setDeltaCD0Spoliers(Double _deltaCD0Spoliers) {
		this._deltaCD0Spoliers = _deltaCD0Spoliers;
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
	public Double getCLZeroLanding() {
		return _cLZeroLanding;
	}
	public void setCLZeroLanding(Double _cLZeroLanding) {
		this._cLZeroLanding = _cLZeroLanding;
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
	public List<PerformanceEnum> getTaskList() {
		return _taskList;
	}
	public void setTaskList(List<PerformanceEnum> _taskList) {
		this._taskList = _taskList;
	}
	public List<PerformancePlotEnum> getPlotList() {
		return _plotList;
	}
	public void setPlotList(List<PerformancePlotEnum> _plotList) {
		this._plotList = _plotList;
	}

	public Amount<Length> getTakeOffDistanceAOE() {
		return _takeOffDistanceAOE;
	}

	public void setTakeOffDistanceAOE(Amount<Length> _takeOffDistanceAOE) {
		this._takeOffDistanceAOE = _takeOffDistanceAOE;
	}

	public Amount<Length> getTakeOffDistanceFAR25() {
		return _takeOffDistanceFAR25;
	}

	public void setTakeOffDistanceFAR25(Amount<Length> _takeOffDistanceFAR25) {
		this._takeOffDistanceFAR25 = _takeOffDistanceFAR25;
	}

	public Amount<Length> getBalancedFieldLength() {
		return _balancedFieldLength;
	}

	public void setBalancedFieldLength(Amount<Length> _balancedFieldLength) {
		this._balancedFieldLength = _balancedFieldLength;
	}

	public Amount<Length> getGroundRollDistanceTakeOff() {
		return _groundRollDistanceTakeOff;
	}

	public void setGroundRollDistanceTakeOff(Amount<Length> _groundRoll) {
		this._groundRollDistanceTakeOff = _groundRoll;
	}

	public Amount<Length> getRotationDistanceTakeOff() {
		return _rotationDistanceTakeOff;
	}

	public void setRotationDistanceTakeOff(Amount<Length> _rotation) {
		this._rotationDistanceTakeOff = _rotation;
	}

	public Amount<Length> getAirborneDistanceTakeOff() {
		return _airborneDistanceTakeOff;
	}

	public void setAirborneDistanceTakeOff(Amount<Length> _airborne) {
		this._airborneDistanceTakeOff = _airborne;
	}

	public Amount<Velocity> getVStallTakeOff() {
		return _vStallTakeOff;
	}

	public void setVStallTakeOff(Amount<Velocity> _vsTO) {
		this._vStallTakeOff = _vsTO;
	}

	public Amount<Velocity> getVRotation() {
		return _vRotation;
	}

	public void setVRotation(Amount<Velocity> _vRotation) {
		this._vRotation = _vRotation;
	}

	public Amount<Velocity> getVLiftOff() {
		return _vLiftOff;
	}

	public void setVLiftOff(Amount<Velocity> _vLiftOff) {
		this._vLiftOff = _vLiftOff;
	}

	public Amount<Velocity> getV1() {
		return _v1;
	}

	public void setV1(Amount<Velocity> _v1) {
		this._v1 = _v1;
	}

	public Amount<Velocity> getV2() {
		return _v2;
	}

	public void setV2(Amount<Velocity> _v2) {
		this._v2 = _v2;
	}

	public Double[] getPolarCLCruise() {
		return _polarCLCruise;
	}

	public void setPolarCLCruise(Double[] _polarCLCruise) {
		this._polarCLCruise = _polarCLCruise;
	}

	public Double[] getPolarCDCruise() {
		return _polarCDCruise;
	}

	public void setPolarCDCruise(Double[] _polarCDCruise) {
		this._polarCDCruise = _polarCDCruise;
	}

	public Double[] getPolarCLTakeOff() {
		return _polarCLTakeOff;
	}

	public void setPolarCLTakeOff(Double[] _polarCLTakeOff) {
		this._polarCLTakeOff = _polarCLTakeOff;
	}

	public Double[] getPolarCDTakeOff() {
		return _polarCDTakeOff;
	}

	public void setPolarCDTakeOff(Double[] _polarCDTakeOff) {
		this._polarCDTakeOff = _polarCDTakeOff;
	}

	public Double[] getPolarCLLanding() {
		return _polarCLLanding;
	}

	public void setPolarCLLanding(Double[] _polarCLLanding) {
		this._polarCLLanding = _polarCLLanding;
	}

	public Double[] getPolarCDLanding() {
		return _polarCDLanding;
	}

	public void setPolarCDLanding(Double[] _polarCDLanding) {
		this._polarCDLanding = _polarCDLanding;
	}

	public Amount<Length> getLandingDistance() {
		return _landingDistance;
	}

	public void setLandingDistance(Amount<Length> _landingDistance) {
		this._landingDistance = _landingDistance;
	}

	public Amount<Length> getLandingDistanceFAR25() {
		return _landingDistanceFAR25;
	}

	public void setLandingDistanceFAR25(Amount<Length> _landingDistanceFAR25) {
		this._landingDistanceFAR25 = _landingDistanceFAR25;
	}

	public Amount<Length> getGroundRollDistanceLanding() {
		return _groundRollDistanceLanding;
	}

	public void setGroundRollDistanceLanding(Amount<Length> _groundRollDistanceLanding) {
		this._groundRollDistanceLanding = _groundRollDistanceLanding;
	}

	public Amount<Length> getFlareDistanceLanding() {
		return _flareDistanceLanding;
	}

	public void setFlareDistanceLanding(Amount<Length> _rotationDistanceLanding) {
		this._flareDistanceLanding = _rotationDistanceLanding;
	}

	public Amount<Length> getAirborneDistanceLanding() {
		return _airborneDistanceLanding;
	}

	public void setAirborneDistanceLanding(Amount<Length> _airborneDistanceLanding) {
		this._airborneDistanceLanding = _airborneDistanceLanding;
	}

	public Amount<Velocity> getVStallLanding() {
		return _vStallLanding;
	}

	public void setVStallLanding(Amount<Velocity> _vStallLanding) {
		this._vStallLanding = _vStallLanding;
	}

	public Amount<Velocity> getVApproach() {
		return _vApproach;
	}

	public void setVApproach(Amount<Velocity> _vApproach) {
		this._vApproach = _vApproach;
	}

	public Amount<Velocity> getVFlare() {
		return _vFlare;
	}

	public void setVFlare(Amount<Velocity> _vFlare) {
		this._vFlare = _vFlare;
	}

	public Amount<Velocity> getVTouchDown() {
		return _vTouchDown;
	}

	public void setVTouchDown(Amount<Velocity> _vTouchDown) {
		this._vTouchDown = _vTouchDown;
	}

	public Amount<Velocity> getStallSpeedFullFlap() {
		return _stallSpeedFullFlap;
	}

	public void setStallSpeedFullFlap(Amount<Velocity> _stallSpeedFullFlap) {
		this._stallSpeedFullFlap = _stallSpeedFullFlap;
	}

	public Amount<Velocity> getStallSpeedClean() {
		return _stallSpeedClean;
	}

	public void setStallSpeedClean(Amount<Velocity> _stallSpeedClean) {
		this._stallSpeedClean = _stallSpeedClean;
	}

	public Amount<Velocity> getStallSpeedInverted() {
		return _stallSpeedInverted;
	}

	public void setStallSpeedInverted(Amount<Velocity> _stallSpeedInverted) {
		this._stallSpeedInverted = _stallSpeedInverted;
	}

	public Amount<Velocity> getManeuveringSpeed() {
		return _maneuveringSpeed;
	}

	public void setManeuveringSpeed(Amount<Velocity> _maneuveringSpeed) {
		this._maneuveringSpeed = _maneuveringSpeed;
	}

	public Amount<Velocity> getManeuveringFlapSpeed() {
		return _maneuveringFlapSpeed;
	}

	public void setManeuveringFlapSpeed(Amount<Velocity> _maneuveringFlapSpeed) {
		this._maneuveringFlapSpeed = _maneuveringFlapSpeed;
	}

	public Amount<Velocity> getManeuveringSpeedInverted() {
		return _maneuveringSpeedInverted;
	}

	public void setManeuveringSpeedInverted(Amount<Velocity> _maneuveringSpeedInverted) {
		this._maneuveringSpeedInverted = _maneuveringSpeedInverted;
	}

	public Amount<Velocity> getDesignFlapSpeed() {
		return _designFlapSpeed;
	}

	public void setDesignFlapSpeed(Amount<Velocity> _designFlapSpeed) {
		this._designFlapSpeed = _designFlapSpeed;
	}

	public Double getPositiveLoadFactorManeuveringSpeed() {
		return _positiveLoadFactorManeuveringSpeed;
	}

	public void setPositiveLoadFactorManeuveringSpeed(Double _positiveLoadFactorManeuveringSpeed) {
		this._positiveLoadFactorManeuveringSpeed = _positiveLoadFactorManeuveringSpeed;
	}

	public Double getPositiveLoadFactorCruisingSpeed() {
		return _positiveLoadFactorCruisingSpeed;
	}

	public void setPositiveLoadFactorCruisingSpeed(Double _positiveLoadFactorCruisingSpeed) {
		this._positiveLoadFactorCruisingSpeed = _positiveLoadFactorCruisingSpeed;
	}

	public Double getPositiveLoadFactorDiveSpeed() {
		return _positiveLoadFactorDiveSpeed;
	}

	public void setPositiveLoadFactorDiveSpeed(Double _positiveLoadFactorDiveSpeed) {
		this._positiveLoadFactorDiveSpeed = _positiveLoadFactorDiveSpeed;
	}

	public Double getPositiveLoadFactorDesignFlapSpeed() {
		return _positiveLoadFactorDesignFlapSpeed;
	}

	public void setPositiveLoadFactorDesignFlapSpeed(Double _positiveLoadFactorDesignFlapSpeed) {
		this._positiveLoadFactorDesignFlapSpeed = _positiveLoadFactorDesignFlapSpeed;
	}

	public Double getNegativeLoadFactorManeuveringSpeedInverted() {
		return _negativeLoadFactorManeuveringSpeedInverted;
	}

	public void setNegativeLoadFactorManeuveringSpeedInverted(Double _negativeLoadFactorManeuveringSpeedInverted) {
		this._negativeLoadFactorManeuveringSpeedInverted = _negativeLoadFactorManeuveringSpeedInverted;
	}

	public Double getNegativeLoadFactorCruisingSpeed() {
		return _negativeLoadFactorCruisingSpeed;
	}

	public void setNegativeLoadFactorCruisingSpeed(Double _negativeLoadFactorCruisingSpeed) {
		this._negativeLoadFactorCruisingSpeed = _negativeLoadFactorCruisingSpeed;
	}

	public Double getNegativeLoadFactorDiveSpeed() {
		return _negativeLoadFactorDiveSpeed;
	}

	public void setNegativeLoadFactorDiveSpeed(Double _negativeLoadFactorDiveSpeed) {
		this._negativeLoadFactorDiveSpeed = _negativeLoadFactorDiveSpeed;
	}

	public Double getPositiveLoadFactorManeuveringSpeedWithGust() {
		return _positiveLoadFactorManeuveringSpeedWithGust;
	}

	public void setPositiveLoadFactorManeuveringSpeedWithGust(Double _positiveLoadFactorManeuveringSpeedWithGust) {
		this._positiveLoadFactorManeuveringSpeedWithGust = _positiveLoadFactorManeuveringSpeedWithGust;
	}

	public Double getPositiveLoadFactorCruisingSpeedWithGust() {
		return _positiveLoadFactorCruisingSpeedWithGust;
	}

	public void setPositiveLoadFactorCruisingSpeedWithGust(Double _positiveLoadFactorCruisingSpeedWithGust) {
		this._positiveLoadFactorCruisingSpeedWithGust = _positiveLoadFactorCruisingSpeedWithGust;
	}

	public Double getPositiveLoadFactorDiveSpeedWithGust() {
		return _positiveLoadFactorDiveSpeedWithGust;
	}

	public void setPositiveLoadFactorDiveSpeedWithGust(Double _positiveLoadFactorDiveSpeedWithGust) {
		this._positiveLoadFactorDiveSpeedWithGust = _positiveLoadFactorDiveSpeedWithGust;
	}

	public Double getPositiveLoadFactorDesignFlapSpeedWithGust() {
		return _positiveLoadFactorDesignFlapSpeedWithGust;
	}

	public void setPositiveLoadFactorDesignFlapSpeedWithGust(Double _positiveLoadFactorDesignFlapSpeedWithGust) {
		this._positiveLoadFactorDesignFlapSpeedWithGust = _positiveLoadFactorDesignFlapSpeedWithGust;
	}

	public Double getNegativeLoadFactorManeuveringSpeedInvertedWithGust() {
		return _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	}

	public void setNegativeLoadFactorManeuveringSpeedInvertedWithGust(
			Double _negativeLoadFactorManeuveringSpeedInvertedWithGust) {
		this._negativeLoadFactorManeuveringSpeedInvertedWithGust = _negativeLoadFactorManeuveringSpeedInvertedWithGust;
	}

	public Double getNegativeLoadFactorCruisingSpeedWithGust() {
		return _negativeLoadFactorCruisingSpeedWithGust;
	}

	public void setNegativeLoadFactorCruisingSpeedWithGust(Double _negativeLoadFactorCruisingSpeedWithGust) {
		this._negativeLoadFactorCruisingSpeedWithGust = _negativeLoadFactorCruisingSpeedWithGust;
	}

	public Double getNegativeLoadFactorDiveSpeedWithGust() {
		return _negativeLoadFactorDiveSpeedWithGust;
	}

	public void setNegativeLoadFactorDiveSpeedWithGust(Double _negativeLoadFactorDiveSpeedWithGust) {
		this._negativeLoadFactorDiveSpeedWithGust = _negativeLoadFactorDiveSpeedWithGust;
	}

	public Double getNegativeLoadFactorDesignFlapSpeedWithGust() {
		return _negativeLoadFactorDesignFlapSpeedWithGust;
	}

	public void setNegativeLoadFactorDesignFlapSpeedWithGust(Double _negativeLoadFactorDesignFlapSpeedWithGust) {
		this._negativeLoadFactorDesignFlapSpeedWithGust = _negativeLoadFactorDesignFlapSpeedWithGust;
	}

	public TakeOffCalc getTheTakeOffCalculator() {
		return _theTakeOffCalculator;
	}

	public void setTheTakeOffCalculator(TakeOffCalc _theTakeOffCalculator) {
		this._theTakeOffCalculator = _theTakeOffCalculator;
	}

	public LandingCalc getTheLandingCalculator() {
		return _theLandingCalculator;
	}

	public void setTheLandingCalculator(LandingCalc _theLandingCalculator) {
		this._theLandingCalculator = _theLandingCalculator;
	}

	public FlightManeuveringEnvelopeCalc getTheEnvelopeCalculator() {
		return _theEnvelopeCalculator;
	}

	public void setTheEnvelopeCalculator(FlightManeuveringEnvelopeCalc _theEnvelopeCalculator) {
		this._theEnvelopeCalculator = _theEnvelopeCalculator;
	}
}
