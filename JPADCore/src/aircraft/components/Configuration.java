package aircraft.components;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.SeatsBlock;
import aircraft.auxiliary.SeatsBlock.CGboarding;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.RelativePositionEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

/**
 * Define the cabin configuration (full economy, economy + business) and current flight configuration
 * (in terms of number of passengers, number of crew members).
 * 
 * The number of passengers and their location is necessary for estimating 
 * the Aircraft Center of Gravity position.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class Configuration {
	
	private static final String _id = "11";
	private Fuselage _theFuselage;
	private LiftingSurface2Panels _theWing, _theHTail, _theVTail;
	private Nacelle _theNacelle;
	private SeatsBlock _seatsBlockRight, _seatsBlockLeft, _seatsBlockCenter;

	private Map<MethodEnum, Amount<?>> _massMap = new TreeMap<MethodEnum, Amount<?>>();
	private Map<Integer, Amount<Length>> _breaksMap = new HashMap<Integer, Amount<Length>>();
	private Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private Map<ClassTypeEnum, ArrayList<Object>> _seatsMap = new HashMap<ClassTypeEnum, ArrayList<Object>>();

	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private List<SeatsBlock> _seatsBlocksList = new ArrayList<SeatsBlock>();
	private MyArray _xLoading = new MyArray(),  
			_yLoading = new MyArray();

	private Double[] _percentDifference;
	private Amount<Mass> _massFurnishingsAndEquipmentReference, _massFurnishingsAndEquipment, _massEstimatedFurnishingsAndEquipment,
	_mass;

	private Double _nPax, _nCrew, _flightCrewNumber, _cabinCrewNumber, _maxPax, _classesNumber;
	private Integer 
	_numberOfBreaksEconomyClass, 
	_numberOfBreaksBusinessClass,
	_numberOfBreaksFirstClass;

	private Integer _aislesNumber, _numberOfRowsEconomyClass,
	_numberOfRowsBusinessClass, _numberOfRowsFirstClass;
	private Integer[] _numberOfColumnsEconomyClass, _numberOfColumnsBusinessClass,
	_numberOfColumnsFirstClass;

	//	private Integer[] _abreastFirstClass = { 2, 2 },
	//			_abreastBusinessClass = { 2, 2 },
	//			_abreastEconomyClass = { 2, 2 };

	private Amount<Length> _breakLenght, _pitchFirstClass,
	_pitchBusinessClass, _pitchEconomyClass, _widthEconomyClass,
	_widthBusinessClass, _widthFirstClass, _distanceFromWallEconomyClass,
	_distanceFromWallBusinessClass, _distanceFromWallFirstClass;

	private RelativePositionEnum _position;

	/*
	 * Each element in the following lists is relative to a class:
	 * index 0: ECONOMY
	 * index 1: BUSINESS
	 * index 2: FIRST
	 * 
	 * If a class is missing indexes are decreased by 1, e.g. if first class is
	 * missing:
	 * index 0: ECONOMY
	 * index 1: BUSINESS
	 */
	private Amount<Length> _xCoordinateFirstRow;
	private List<Amount<Length>> _pitchList = new ArrayList<Amount<Length>>();
	private List<Amount<Length>> _widthList = new ArrayList<Amount<Length>>();
	private List<Amount<Length>> _distanceFromWallList = new ArrayList<Amount<Length>>();
	private List<Double> _lengthOfEachBreakList = new ArrayList<Double>();
	private List<Integer> _numberOfBreaksList = new ArrayList<Integer>();
	private List<Integer> _numberOfRowsList = new ArrayList<Integer>();
	private List<Integer[]> _numberOfColumnsList = new ArrayList<Integer[]>();
	private List<Integer[]> _missingSeatsRowList = new ArrayList<Integer[]>();
	private List<Integer[]> _missingSeatsColumnList = new ArrayList<Integer[]>();
	private List<ClassTypeEnum> _typeList = new ArrayList<ClassTypeEnum>();
	private List<Map<Integer, Amount<Length>>> _breaksMapList = new ArrayList<Map<Integer, Amount<Length>>>();
	private Amount<Length> _seatsCoG;
	private Double[] _lengthOfEachBreakEconomyClass;
	private Double[] _lengthOfEachBreakBusinessClass;
	private Double[] _lengthOfEachBreakFirstClass;
	private Double[] _pitchArr;
	private Double[] _widthArr;
	private Double[] _distanceFromWallArr;
	private Integer[] _numberOfRowsArr;
	private Double[] _numberOfBreaksArr;
	private Integer[] _numberOfColumnsArr;
	private List<Amount<Length>> _seatsCoGFrontToRearWindow = new ArrayList<Amount<Length>>();
	private List<Amount<Length>> _seatsCoGrearToFrontWindow = new ArrayList<Amount<Length>>();
	private List<Amount<Length>> _seatsCoGFrontToRearAisle = new ArrayList<Amount<Length>>();
	private List<Amount<Length>> _seatsCoGrearToFrontAisle = new ArrayList<Amount<Length>>();
	private List<Amount<Length>> _seatsCoGrearToFrontOther = new ArrayList<Amount<Length>>();
	private List<Amount<Length>> _seatsCoGFrontToRearOther = new ArrayList<Amount<Length>>();
	private List<Amount<Mass>> _currentMassList;
	private List<Amount<Length>> _seatsCoGFrontToRear;
	private List<Amount<Length>> _seatsCoGRearToFront;


	public Configuration() {

		_massFurnishingsAndEquipmentReference = Amount.valueOf(2000., SI.KILOGRAM);
		
		_maxPax = 72.;
		_nPax = 68.;
		_flightCrewNumber = 2.;
		
		// Number of classes
		_classesNumber = 1.;

		// Number of aisles
		_aislesNumber = new Integer(1);

		_position = RelativePositionEnum.RIGHT;
		_xCoordinateFirstRow = Amount.valueOf(6.16, SI.METER);

		_pitchEconomyClass = Amount.valueOf(0.79, SI.METER);
		_pitchBusinessClass = Amount.valueOf(0., SI.METER);
		_pitchFirstClass = Amount.valueOf(0., SI.METER);

		_widthEconomyClass = Amount.valueOf(0.5, SI.METER);
		_widthBusinessClass = Amount.valueOf(0., SI.METER);
		_widthFirstClass = Amount.valueOf(0., SI.METER);

		_distanceFromWallEconomyClass = Amount.valueOf(0.0845, SI.METER);
		_distanceFromWallBusinessClass = Amount.valueOf(0.0845, SI.METER);
		_distanceFromWallFirstClass = Amount.valueOf(0.0845, SI.METER);

		_numberOfBreaksEconomyClass = new Integer(-1);
		_numberOfBreaksBusinessClass = new Integer(-1);
		_numberOfBreaksFirstClass = new Integer(-1);

		_lengthOfEachBreakEconomyClass = new Double[] {0.0, 0.0};
		_lengthOfEachBreakBusinessClass = new Double[] {0.0, 0.0};
		_lengthOfEachBreakFirstClass = new Double[] {0.0, 0.0};
		//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
		//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
		//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);

		_numberOfRowsEconomyClass = new Integer(16);
		_numberOfRowsBusinessClass = new Integer(0);
		_numberOfRowsFirstClass = new Integer(0);

		_numberOfColumnsEconomyClass = new Integer[]{2,2};
		_numberOfColumnsBusinessClass = new Integer[]{0,0};
		_numberOfColumnsFirstClass = new Integer[]{0,0};

		// _missingSeatsRowList.size() must be equal to number of classes
		_missingSeatsRowList.add(new Integer[] { -1});
		_missingSeatsRowList.add(new Integer[] { -1});
		_missingSeatsRowList.add(new Integer[] { -1});

		// _missingSeatsColumnList.size() must be equal to number of classes
		_missingSeatsColumnList.add(new Integer[] { -1});
		_missingSeatsColumnList.add(new Integer[] { -1});
		_missingSeatsColumnList.add(new Integer[] { -1});

		_typeList.add(ClassTypeEnum.ECONOMY);
		_typeList.add(ClassTypeEnum.BUSINESS);
		_typeList.add(ClassTypeEnum.FIRST);

		calculateDependentVariables();
	}

	/**
	 * Overload of the Configuration builder that recongnize aircraft name and sets the 
	 * relative data.
	 * 
	 * @author Vittorio Trifari
	 */
	public Configuration(AircraftEnum aircratName) {

		switch(aircratName) {
		case ATR72:
			_massFurnishingsAndEquipmentReference = Amount.valueOf(2000., SI.KILOGRAM);
			
			_maxPax = 72.;
			_nPax = 68.;
			_flightCrewNumber = 2.;
			
			// Number of classes
			_classesNumber = 1.;

			// Number of aisles
			_aislesNumber = new Integer(1);

			_position = RelativePositionEnum.RIGHT;
			_xCoordinateFirstRow = Amount.valueOf(6.16, SI.METER);

			_pitchEconomyClass = Amount.valueOf(0.79, SI.METER);
			_pitchBusinessClass = Amount.valueOf(0., SI.METER);
			_pitchFirstClass = Amount.valueOf(0., SI.METER);

			_widthEconomyClass = Amount.valueOf(0.5, SI.METER);
			_widthBusinessClass = Amount.valueOf(0., SI.METER);
			_widthFirstClass = Amount.valueOf(0., SI.METER);

			_distanceFromWallEconomyClass = Amount.valueOf(0.0845, SI.METER);
			_distanceFromWallBusinessClass = Amount.valueOf(0.0845, SI.METER);
			_distanceFromWallFirstClass = Amount.valueOf(0.0845, SI.METER);

			_numberOfBreaksEconomyClass = new Integer(-1);
			_numberOfBreaksBusinessClass = new Integer(-1);
			_numberOfBreaksFirstClass = new Integer(-1);

			_lengthOfEachBreakEconomyClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakBusinessClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakFirstClass = new Double[] {0.0, 0.0};
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);

			_numberOfRowsEconomyClass = new Integer(16);
			_numberOfRowsBusinessClass = new Integer(0);
			_numberOfRowsFirstClass = new Integer(0);

			_numberOfColumnsEconomyClass = new Integer[]{2,2};
			_numberOfColumnsBusinessClass = new Integer[]{0,0};
			_numberOfColumnsFirstClass = new Integer[]{0,0};

			// _missingSeatsRowList.size() must be equal to number of classes
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});

			// _missingSeatsColumnList.size() must be equal to number of classes
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});

			_typeList.add(ClassTypeEnum.ECONOMY);
			_typeList.add(ClassTypeEnum.BUSINESS);
			_typeList.add(ClassTypeEnum.FIRST);

			calculateDependentVariables();
			break;
			
		case B747_100B:
			// see Roskam (Part V) Appendix-A pag.152 --> Fixed Equipment Total
			_massFurnishingsAndEquipmentReference = Amount.valueOf(28000., SI.KILOGRAM);
			
			_maxPax = 550.;
			_nPax = 550.;
			_flightCrewNumber = 2.;
			
			// Number of classes
			_classesNumber = 1.;

			// Number of aisles
			_aislesNumber = new Integer(1);

			_position = RelativePositionEnum.RIGHT;
			_xCoordinateFirstRow = Amount.valueOf(7.45, SI.METER);

			_pitchEconomyClass = Amount.valueOf(0.79, SI.METER);
			_pitchBusinessClass = Amount.valueOf(0., SI.METER);
			_pitchFirstClass = Amount.valueOf(0., SI.METER);

			_widthEconomyClass = Amount.valueOf(0.5, SI.METER);
			_widthBusinessClass = Amount.valueOf(0., SI.METER);
			_widthFirstClass = Amount.valueOf(0., SI.METER);

			_distanceFromWallEconomyClass = Amount.valueOf(0.0845, SI.METER);
			_distanceFromWallBusinessClass = Amount.valueOf(0.0845, SI.METER);
			_distanceFromWallFirstClass = Amount.valueOf(0.0845, SI.METER);

			_numberOfBreaksEconomyClass = new Integer(-1);
			_numberOfBreaksBusinessClass = new Integer(-1);
			_numberOfBreaksFirstClass = new Integer(-1);

			_lengthOfEachBreakEconomyClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakBusinessClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakFirstClass = new Double[] {0.0, 0.0};
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);

			_numberOfRowsEconomyClass = new Integer(55);
			_numberOfRowsBusinessClass = new Integer(0);
			_numberOfRowsFirstClass = new Integer(0);

			_numberOfColumnsEconomyClass = new Integer[]{2,2};
			_numberOfColumnsBusinessClass = new Integer[]{0,0};
			_numberOfColumnsFirstClass = new Integer[]{0,0};

			// _missingSeatsRowList.size() must be equal to number of classes
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});

			// _missingSeatsColumnList.size() must be equal to number of classes
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});

			_typeList.add(ClassTypeEnum.ECONOMY);
			_typeList.add(ClassTypeEnum.BUSINESS);
			_typeList.add(ClassTypeEnum.FIRST);

			calculateDependentVariables();
			break;
			
		case AGILE_DC1:
			// see Roskam (Part V) Appendix-A pag.152 --> Fixed Equipment Total
			_massFurnishingsAndEquipmentReference = Amount.valueOf(1853., SI.KILOGRAM);
			
			_maxPax = 90.0;
			_nPax = 90.0;
			_flightCrewNumber = 2.;
			
			// Number of classes
			_classesNumber = 1.;

			// Number of aisles
			_aislesNumber = new Integer(1);

			_position = RelativePositionEnum.RIGHT;
			_xCoordinateFirstRow = Amount.valueOf(7.40, SI.METER);

			_pitchEconomyClass = Amount.valueOf(0.80, SI.METER);
			_pitchBusinessClass = Amount.valueOf(0., SI.METER);
			_pitchFirstClass = Amount.valueOf(0., SI.METER);

			_widthEconomyClass = Amount.valueOf(0.4, SI.METER);
			_widthBusinessClass = Amount.valueOf(0., SI.METER);
			_widthFirstClass = Amount.valueOf(0., SI.METER);

			_distanceFromWallEconomyClass = Amount.valueOf(0.1, SI.METER);
			_distanceFromWallBusinessClass = Amount.valueOf(0.1, SI.METER);
			_distanceFromWallFirstClass = Amount.valueOf(0.1, SI.METER);

			_numberOfBreaksEconomyClass = new Integer(-1);
			_numberOfBreaksBusinessClass = new Integer(-1);
			_numberOfBreaksFirstClass = new Integer(-1);

			_lengthOfEachBreakEconomyClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakBusinessClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakFirstClass = new Double[] {0.0, 0.0};
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);

			_numberOfRowsEconomyClass = new Integer(22);
			_numberOfRowsBusinessClass = new Integer(0);
			_numberOfRowsFirstClass = new Integer(0);

			_numberOfColumnsEconomyClass = new Integer[]{2,2};
			_numberOfColumnsBusinessClass = new Integer[]{0,0};
			_numberOfColumnsFirstClass = new Integer[]{0,0};

			// _missingSeatsRowList.size() must be equal to number of classes
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});

			// _missingSeatsColumnList.size() must be equal to number of classes
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});

			_typeList.add(ClassTypeEnum.ECONOMY);
			_typeList.add(ClassTypeEnum.BUSINESS);
			_typeList.add(ClassTypeEnum.FIRST);

			calculateDependentVariables();
			break;
			
		case IRON:
			// see Roskam (Part V) Appendix-A pag.142 --> Fixed Equipment Total (mean value)
			_massFurnishingsAndEquipmentReference = Amount.valueOf(3742., SI.KILOGRAM);
			
			_maxPax = 130.;
			_nPax = 130.;
			_flightCrewNumber = 3.;
			
			// Number of classes
			_classesNumber = 1.;

			// Number of aisles
			_aislesNumber = new Integer(1);

			_position = RelativePositionEnum.RIGHT;
			// from ADAS deliverable 2.0
			_xCoordinateFirstRow = Amount.valueOf(5.064, SI.METER);

			_pitchEconomyClass = Amount.valueOf(0.80, SI.METER);
			_pitchBusinessClass = Amount.valueOf(0., SI.METER);
			_pitchFirstClass = Amount.valueOf(0., SI.METER);

			_widthEconomyClass = Amount.valueOf(0.47, SI.METER);
			_widthBusinessClass = Amount.valueOf(0., SI.METER);
			_widthFirstClass = Amount.valueOf(0., SI.METER);

			_distanceFromWallEconomyClass = Amount.valueOf(0.1, SI.METER);
			_distanceFromWallBusinessClass = Amount.valueOf(0.1, SI.METER);
			_distanceFromWallFirstClass = Amount.valueOf(0.1, SI.METER);

			_numberOfBreaksEconomyClass = new Integer(-1);
			_numberOfBreaksBusinessClass = new Integer(-1);
			_numberOfBreaksFirstClass = new Integer(-1);

			_lengthOfEachBreakEconomyClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakBusinessClass = new Double[] {0.0, 0.0};
			_lengthOfEachBreakFirstClass = new Double[] {0.0, 0.0};
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);
			//		_lengthOfEachBreak.add(_lengthOfEachBreakEconomyClass);

			_numberOfRowsEconomyClass = new Integer(26);
			_numberOfRowsBusinessClass = new Integer(0);
			_numberOfRowsFirstClass = new Integer(0);

			_numberOfColumnsEconomyClass = new Integer[]{3,2};
			_numberOfColumnsBusinessClass = new Integer[]{0,0};
			_numberOfColumnsFirstClass = new Integer[]{0,0};

			// _missingSeatsRowList.size() must be equal to number of classes
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});
			_missingSeatsRowList.add(new Integer[] { -1});

			// _missingSeatsColumnList.size() must be equal to number of classes
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});
			_missingSeatsColumnList.add(new Integer[] { -1});

			_typeList.add(ClassTypeEnum.ECONOMY);
			_typeList.add(ClassTypeEnum.BUSINESS);
			_typeList.add(ClassTypeEnum.FIRST);

			calculateDependentVariables();
			break;
		}
			
	}
	
	public void updateConfiguration() {

		_pitchList = new ArrayList<Amount<Length>>();
		_pitchList.add(_pitchEconomyClass);
		_pitchList.add(_pitchBusinessClass);
		_pitchList.add(_pitchFirstClass);

		_widthList = new ArrayList<Amount<Length>>();
		_widthList.add(_widthEconomyClass);
		_widthList.add(_widthBusinessClass);
		_widthList.add(_widthFirstClass);

		_distanceFromWallList = new ArrayList<Amount<Length>>();
		_distanceFromWallList.add(_distanceFromWallEconomyClass);
		_distanceFromWallList.add(_distanceFromWallBusinessClass);
		_distanceFromWallList.add(_distanceFromWallFirstClass);

		_numberOfBreaksList = new ArrayList<Integer>();
		_numberOfBreaksList.add(_numberOfBreaksEconomyClass);
		_numberOfBreaksList.add(_numberOfBreaksBusinessClass);
		_numberOfBreaksList.add(_numberOfBreaksFirstClass);

		_numberOfRowsList = new ArrayList<Integer>();
		_numberOfRowsList.add(_numberOfRowsEconomyClass);
		_numberOfRowsList.add(_numberOfRowsBusinessClass);
		_numberOfRowsList.add(_numberOfRowsFirstClass);

		_numberOfColumnsList = new ArrayList<Integer[]>();
		_numberOfColumnsList.add(_numberOfColumnsEconomyClass);
		_numberOfColumnsList.add(_numberOfColumnsBusinessClass);
		_numberOfColumnsList.add(_numberOfColumnsFirstClass);		
	}

	//	public void updateConfiguration() {
	//		
	//		// Indexes as for lists above
	//		_pitchArr = new Double[]{0., 0., 0.79};
	//		_widthArr = new Double[]{0., 0., 0.5};
	//		_distanceFromWallArr = new Double[]{0., 0., 0.0845};
	//		_numberOfBreaksArr = new Double[]{0., 0., 0.};
	//		_numberOfRowsArr = new Integer[]{0, 0, 16};
	//		_numberOfColumnsArr = new Integer[]
	//				{0, 0,
	//				0, 0, 
	//				2, 2};
	//	}

	public void calculateDependentVariables() {

		_cabinCrewNumber = Math.ceil(_nPax / 35);
		_nCrew = _cabinCrewNumber + _flightCrewNumber;
		updateConfiguration();

	}

	/**
	 * Build a simplified cabin layout: the user has to define the x coordinate
	 * at which the layout starts, the number of classes and, for each class,
	 * number of rows and of abreasts, pitch, width, and eventually breaks
	 * (empty spaces between seats). The method does not consider missing seats
	 * and differences between two groups of seats (a group is such when 
	 * separated from another one by an aisle).
	 * 
	 * @param aircraft
	 * 
	 */
	public void buildSimpleLayout(Aircraft aircraft) {

		System.out.println("----- CABIN LAYOUT CREATION STARTED -----");
		updateConfiguration();
		Amount<Length> length = Amount.valueOf(0., SI.METER);

		for (int i = 0; i < _classesNumber; i++) {

			_seatsBlockRight = new SeatsBlock();
			_seatsBlockLeft = new SeatsBlock();

			_breaksMap.put(_numberOfBreaksList.get(i), Amount.valueOf(0., SI.METER));
			_breaksMapList.add(_breaksMap);

			_seatsBlockLeft.createSeatsBlock(
					RelativePositionEnum.LEFT,
					_xCoordinateFirstRow.plus(length),
					_pitchList.get(i),
					_widthList.get(i),
					_distanceFromWallList.get(i),
					_breaksMapList.get(i),
					_numberOfRowsList.get(i),
					_numberOfColumnsList.get(i)[0],
					_missingSeatsRowList.get(i),
					_missingSeatsColumnList.get(i),
					_typeList.get(i));
			_seatsBlockLeft.calculateCoG(aircraft);

			_seatsBlockRight.createSeatsBlock(
					RelativePositionEnum.RIGHT,
					_xCoordinateFirstRow.plus(length),
					_pitchList.get(i),
					_widthList.get(i),
					_distanceFromWallList.get(i),
					_breaksMapList.get(i),
					_numberOfRowsList.get(i),
					_numberOfColumnsList.get(i)[1],
					_missingSeatsRowList.get(i),
					_missingSeatsColumnList.get(i),
					_typeList.get(i));
			_seatsBlockRight.calculateCoG(aircraft);

			if (_aislesNumber > 1) {

				_seatsBlockCenter = new SeatsBlock();
				_seatsBlockCenter.createSeatsBlock(
						RelativePositionEnum.CENTER,
						_xCoordinateFirstRow.plus(length),
						_pitchList.get(i),
						_widthList.get(i),
						_distanceFromWallList.get(i),
						_breaksMapList.get(i),
						_numberOfRowsList.get(i),
						_numberOfColumnsList.get(i)[2],
						_missingSeatsRowList.get(i),
						_missingSeatsColumnList.get(i),
						_typeList.get(i));

				_seatsBlockCenter.calculateCoG(aircraft);
				_seatsBlocksList.add(_seatsBlockLeft);
				_seatsBlocksList.add(_seatsBlockRight);
				_seatsBlocksList.add(_seatsBlockCenter);

			} else {
				_seatsBlocksList.add(_seatsBlockLeft);	
				_seatsBlocksList.add(_seatsBlockRight);
			}

			length = _seatsBlockRight.get_lenghtOverall();

		}

		// CoG variation during boarding procedure in aircraft reference frame
		CGboarding seatsCoGboarding = 
				SeatsBlock.calculateCoGboarding(_seatsBlocksList, aircraft);

		//		for (int k=0; k < seatsCoGboarding.getCurrentXCoGfrontToRearWindow().size(); k++){
		//			_seatsCoGFrontToRearWindow.add(seatsCoGboarding.getCurrentXCoGfrontToRearWindow().get(k));
		//			_seatsCoGrearToFrontWindow.add(seatsCoGboarding.getCurrentXCoGrearToFrontWindow().get(k));
		//			_seatsCoGFrontToRearAisle.add(seatsCoGboarding.getCurrentXCoGfrontToRearAisle().get(k));
		//			_seatsCoGrearToFrontAisle.add(seatsCoGboarding.getCurrentXCoGrearToFrontAisle().get(k));
		//
		//			if (_seatsBlockRight.get_columns() > 2 | _seatsBlockLeft.get_columns() > 2 |
		//					(_seatsBlockCenter != null && _seatsBlockCenter.get_columns() > 2)) {
		//				_seatsCoGFrontToRearOther.add((Amount<Length>) seatsCoGboarding.getCurrentXCoGfrontToRearOther().get(k));
		//				_seatsCoGrearToFrontOther.add((Amount<Length>) seatsCoGboarding.getCurrentXCoGrearToFrontOther().get(k));	
		//			}

		_currentMassList = seatsCoGboarding.getCurrentMassList();
		_seatsCoGFrontToRearWindow = seatsCoGboarding.getCurrentXCoGfrontToRearWindow();
		_seatsCoGrearToFrontWindow = seatsCoGboarding.getCurrentXCoGrearToFrontWindow();
		_seatsCoGFrontToRearAisle = seatsCoGboarding.getCurrentXCoGfrontToRearAisle();
		_seatsCoGrearToFrontAisle = seatsCoGboarding.getCurrentXCoGrearToFrontAisle();

		if (_seatsBlockRight.get_columns() > 2 | _seatsBlockLeft.get_columns() > 2 |
				(_seatsBlockCenter != null && _seatsBlockCenter.get_columns() > 2)) {
			_seatsCoGFrontToRearOther = seatsCoGboarding.getCurrentXCoGfrontToRearOther();
			_seatsCoGrearToFrontOther = seatsCoGboarding.getCurrentXCoGrearToFrontOther();	
		}

		_seatsCoGFrontToRear = seatsCoGboarding.getCurrentXCoGfrontToRear();
		_seatsCoGRearToFront = seatsCoGboarding.getCurrentXCoGrearToFront();
		
		_xLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_seatsCoGFrontToRear));
		_xLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_seatsCoGRearToFront));

		_yLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_currentMassList));
		_yLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_currentMassList));
		
		_seatsCoG = SeatsBlock.calculateTotalCoG(_seatsBlocksList).plus(_xCoordinateFirstRow);
		
		System.out.println("Total CoG: " + _seatsCoG);
		
		System.out.println("----- CABIN LAYOUT CREATION FINISHED -----");

	}
	

	public void calculateMass(Aircraft aircraft, OperatingConditions conditions,
			MethodEnum method) {
		calculateMassFurnishings(aircraft, conditions, method);
	}

	@SuppressWarnings("unchecked")
	public void calculateMassFurnishings(Aircraft aircraft, OperatingConditions conditions,
			MethodEnum method) {
		switch (method) {
		case TORENBEEK_2013: { // page 257 Torenbeek 2013

			_massFurnishingsAndEquipment = Amount.valueOf(
					(12
							* aircraft.get_fuselage().get_len_F().getEstimatedValue()
							* aircraft.get_fuselage().get_equivalentDiameterCylinderGM().getEstimatedValue() 
							* ( 3
									* aircraft.get_fuselage().get_equivalentDiameterCylinderGM().getEstimatedValue() 
									+ 0.5 * aircraft.get_fuselage().get_deckNumber() + 1) + 3500) /
									AtmosphereCalc.g0.getEstimatedValue(),
									SI.KILOGRAM);

			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(
					round(_massFurnishingsAndEquipment.getEstimatedValue()), SI.KILOGRAM));
		}

		default: {} break;

		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference = new Double[_massMap.size()];

		_massEstimatedFurnishingsAndEquipment = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massFurnishingsAndEquipmentReference,
				_massMap,
				_percentDifference,
				1000.).getFilteredMean(), SI.KILOGRAM);
	}


	public Fuselage get_fuselage() {
		return _theFuselage;
	}

	public LiftingSurface2Panels get_wing() {
		return _theWing;
	}

	public LiftingSurface2Panels get_HTail() {
		return _theHTail;
	}

	public LiftingSurface2Panels get_VTail() {
		return _theVTail;
	}

	public Nacelle get_nacelle() {
		return _theNacelle;
	}

	public Double get_nPax() {
		return _nPax;
	}

	public void set_nPax(Double _nPax) {
		this._nPax = _nPax;
	}

	public Double get_nCrew() {
		return _nCrew;
	}

	public void set_nCrew(Double _nCrew) {
		this._nCrew = _nCrew;
	}

	public Double get_maxPax() {
		return _maxPax;
	}
	
	public void set_maxPax(Double _maxPax) {
		this._maxPax = _maxPax;
	}

	public Amount<Mass> get_massFurnishings() {
		return _massFurnishingsAndEquipment;
	}

	public void get_massFurnishingsAndEquipment(Amount<Mass> _massFurnishings) {
		this._massFurnishingsAndEquipment = _massFurnishings;
	}

	public Amount<Mass> get_massFurnishingsAndEquipmentReference() {
		return _massFurnishingsAndEquipmentReference;
	}

	public void set_massFurnishingsAndEquipmentReference(Amount<Mass> _massReference) {
		this._massFurnishingsAndEquipmentReference = _massReference;
	}

	public Amount<Mass> get_massEstimatedFurnishingsAndEquipment() {
		return _massEstimatedFurnishingsAndEquipment;
	}

	public void set_massEstimatedFurnishingsAndEquipment(Amount<Mass> _massMean) {
		this._massEstimatedFurnishingsAndEquipment = _massMean;
	}

	public Double[] get_percentDifference() {
		return _percentDifference;
	}

	public void set_percentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	public Amount<Mass> get_mass() {
		return _mass;
	}

	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	public Double get_classesNumber() {
		return _classesNumber;
	}

	public void set_classesNumber(Double _classesNumber) {
		this._classesNumber = _classesNumber;
	}

	public Integer get_aislesNumber() {
		return _aislesNumber;
	}

	public void set_aislesNumber(Integer _aislesNumber) {
		this._aislesNumber = _aislesNumber;
	}

	public List<Amount<Length>> get_pitch() {
		return _pitchList;
	}

	public void set_pitch(List<Amount<Length>> _pitch) {
		this._pitchList = _pitch;
	}

	public List<Amount<Length>> get_distanceFromWall() {
		return _distanceFromWallList;
	}

	public void set_distanceFromWall(List<Amount<Length>> _distanceFromWall) {
		this._distanceFromWallList = _distanceFromWall;
	}

	public List<Integer> get_numberOfBreaks() {
		return _numberOfBreaksList;
	}

	public void set_numberOfBreaks(List<Integer> _numberOfBreaks) {
		this._numberOfBreaksList = _numberOfBreaks;
	}

	public List<Integer> get_numberOfRows() {
		return _numberOfRowsList;
	}

	public void set_numberOfRows(List<Integer> _numberOfRows) {
		this._numberOfRowsList = _numberOfRows;
	}

	public List<Integer[]> get_numberOfColumns() {
		return _numberOfColumnsList;
	}

	public void set_numberOfColumns(List<Integer[]> _numberOfColumns) {
		this._numberOfColumnsList = _numberOfColumns;
	}

	public List<Integer[]> get_missingSeatsRow() {
		return _missingSeatsRowList;
	}

	public void set_missingSeatsRow(List<Integer[]> _missingSeatsRow) {
		this._missingSeatsRowList = _missingSeatsRow;
	}

	public List<Integer[]> get_missingSeatsColumn() {
		return _missingSeatsColumnList;
	}

	public void set_missingSeatsColumn(List<Integer[]> _missingSeatsColumn) {
		this._missingSeatsColumnList = _missingSeatsColumn;
	}

	public Amount<Length> get_pitchFirstClass() {
		return _pitchFirstClass;
	}

	public void set_pitchFirstClass(Amount<Length> _pitchFirstClass) {
		this._pitchFirstClass = _pitchFirstClass;
	}

	public Amount<Length> get_pitchBusinessClass() {
		return _pitchBusinessClass;
	}

	public void set_pitchBusinessClass(Amount<Length> _pitchBusinessClass) {
		this._pitchBusinessClass = _pitchBusinessClass;
	}

	public Amount<Length> get_pitchEconomyClass() {
		return _pitchEconomyClass;
	}

	public void set_pitchEconomyClass(Amount<Length> _pitchEconomyClass) {
		this._pitchEconomyClass = _pitchEconomyClass;
	}

	public List<SeatsBlock> get_seatsBlocksList() {
		return _seatsBlocksList;
	}

	public void set_seatsBlocksList(List<SeatsBlock> _seatsBlocksList) {
		this._seatsBlocksList = _seatsBlocksList;
	}

	public List<Amount<Length>> get_width() {
		return _widthList;
	}

	public void set_width(List<Amount<Length>> _width) {
		this._widthList = _width;
	}

	public Integer get_numberOfBreaksEconomyClass() {
		return _numberOfBreaksEconomyClass;
	}

	public void set_numberOfBreaksEconomyClass(Integer _numberOfBreaksEconomyClass) {
		this._numberOfBreaksEconomyClass = _numberOfBreaksEconomyClass;
	}

	public Integer get_numberOfBreaksBusinessClass() {
		return _numberOfBreaksBusinessClass;
	}

	public void set_numberOfBreaksBusinessClass(Integer _numberOfBreaksBusinessClass) {
		this._numberOfBreaksBusinessClass = _numberOfBreaksBusinessClass;
	}

	public Integer get_numberOfBreaksFirstClass() {
		return _numberOfBreaksFirstClass;
	}

	public void set_numberOfBreaksFirstClass(Integer _numberOfBreaksFirstClass) {
		this._numberOfBreaksFirstClass = _numberOfBreaksFirstClass;
	}

	public Integer[] get_numberOfColumnsEconomyClass() {
		return _numberOfColumnsEconomyClass;
	}

	public void set_numberOfColumnsEconomyClass(Integer[] _numberOfColumnsEconomyClass) {
		this._numberOfColumnsEconomyClass = _numberOfColumnsEconomyClass;
	}

	public Integer[] get_numberOfColumnsBusinessClass() {
		return _numberOfColumnsBusinessClass;
	}

	public void set_numberOfColumnsBusinessClass(Integer[] _numberOfColumnsBusinessClass) {
		this._numberOfColumnsBusinessClass = _numberOfColumnsBusinessClass;
	}

	public Integer[] get_numberOfColumnsFirstClass() {
		return _numberOfColumnsFirstClass;
	}

	public void set_numberOfColumnsFirstClass(Integer[] _numberOfColumnsFirstClass) {
		this._numberOfColumnsFirstClass = _numberOfColumnsFirstClass;
	}

	public Integer get_numberOfRowsEconomyClass() {
		return _numberOfRowsEconomyClass;
	}

	public void set_numberOfRowsEconomyClass(Integer _numberOfRowsEconomyClass) {
		this._numberOfRowsEconomyClass = _numberOfRowsEconomyClass;
	}

	public Integer get_numberOfRowsBusinessClass() {
		return _numberOfRowsBusinessClass;
	}

	public void set_numberOfRowsBusinessClass(Integer _numberOfRowsBusinessClass) {
		this._numberOfRowsBusinessClass = _numberOfRowsBusinessClass;
	}

	public Integer get_numberOfRowsFirstClass() {
		return _numberOfRowsFirstClass;
	}

	public void set_numberOfRowsFirstClass(Integer _numberOfRowsFirstClass) {
		this._numberOfRowsFirstClass = _numberOfRowsFirstClass;
	}

	public Amount<Length> get_widthEconomyClass() {
		return _widthEconomyClass;
	}

	public void set_widthEconomyClass(Amount<Length> _widthEconomyClass) {
		this._widthEconomyClass = _widthEconomyClass;
	}

	public Amount<Length> get_widthBusinessClass() {
		return _widthBusinessClass;
	}

	public void set_widthBusinessClass(Amount<Length> _widthBusinessClass) {
		this._widthBusinessClass = _widthBusinessClass;
	}

	public Amount<Length> get_widthFirstClass() {
		return _widthFirstClass;
	}

	public void set_widthFirstClass(Amount<Length> _widthFirstClass) {
		this._widthFirstClass = _widthFirstClass;
	}

	public Amount<Length> get_distanceFromWallEconomyClass() {
		return _distanceFromWallEconomyClass;
	}

	public void set_distanceFromWallEconomyClass(Amount<Length> _distanceFromWallEconomyClass) {
		this._distanceFromWallEconomyClass = _distanceFromWallEconomyClass;
	}

	public Amount<Length> get_distanceFromWallBusinessClass() {
		return _distanceFromWallBusinessClass;
	}

	public void set_distanceFromWallBusinessClass(Amount<Length> _distanceFromWallBusinessClass) {
		this._distanceFromWallBusinessClass = _distanceFromWallBusinessClass;
	}

	public Amount<Length> get_distanceFromWallFirstClass() {
		return _distanceFromWallFirstClass;
	}

	public void set_distanceFromWallFirstClass(Amount<Length> _distanceFromWallFirstClass) {
		this._distanceFromWallFirstClass = _distanceFromWallFirstClass;
	}

	public Double[] get_lengthOfEachBreakEconomyClass() {
		return _lengthOfEachBreakEconomyClass;
	}

	public void set_lengthOfEachBreakEconomyClass(Double[] _lengthOfEachBreakEconomyClass) {
		this._lengthOfEachBreakEconomyClass = _lengthOfEachBreakEconomyClass;
	}

	public Double[] get_pitchArr() {
		return _pitchArr;
	}

	public void set_pitchArr(Double[] _pitchArr) {
		this._pitchArr = _pitchArr;
	}

	public Double[] get_widthArr() {
		return _widthArr;
	}

	public void set_widthArr(Double[] _widthArr) {
		this._widthArr = _widthArr;
	}

	public Double[] get_distanceFromWallArr() {
		return _distanceFromWallArr;
	}

	public void set_distanceFromWallArr(Double[] _distanceFromWallArr) {
		this._distanceFromWallArr = _distanceFromWallArr;
	}

	public Integer[] get_numberOfRowsArr() {
		return _numberOfRowsArr;
	}

	public void set_numberOfRowsArr(Integer[] _numberOfRowsArr) {
		this._numberOfRowsArr = _numberOfRowsArr;
	}

	public Double[] get_numberOfBreaksArr() {
		return _numberOfBreaksArr;
	}

	public void set_numberOfBreaksArr(Double[] _numberOfBreaksArr) {
		this._numberOfBreaksArr = _numberOfBreaksArr;
	}

	public Integer[] get_numberOfColumnsArr() {
		return _numberOfColumnsArr;
	}

	public void set_numberOfColumnsArr(Integer[] _numberOfColumnsArr) {
		this._numberOfColumnsArr = _numberOfColumnsArr;
	}

	public Double[] get_lengthOfEachBreakBusinessClass() {
		return _lengthOfEachBreakBusinessClass;
	}

	public void set_lengthOfEachBreakBusinessClass(Double[] _lengthOfEachBreakBusinessClass) {
		this._lengthOfEachBreakBusinessClass = _lengthOfEachBreakBusinessClass;
	}

	public Double[] get_lengthOfEachBreakFirstClass() {
		return _lengthOfEachBreakFirstClass;
	}

	public void set_lengthOfEachBreakFirstClass(Double[] _lengthOfEachBreakFirstClass) {
		this._lengthOfEachBreakFirstClass = _lengthOfEachBreakFirstClass;
	}

	public Amount<Length> get_seatsCoG() {
		return _seatsCoG;
	}

	public Amount<Length> get_xCoordinateFirstRow() {
		return _xCoordinateFirstRow;
	}

	public void set_xCoordinateFirstRow(Amount<Length> _xCoordinateFirstRow) {
		this._xCoordinateFirstRow = _xCoordinateFirstRow;
	}

	public List<Amount<Length>> get_seatsCoGFrontToRearWindow() {
		return _seatsCoGFrontToRearWindow;
	}

	public List<Amount<Length>> get_seatsCoGrearToFrontWindow() {
		return _seatsCoGrearToFrontWindow;
	}

	public List<Amount<Length>> get_seatsCoGFrontToRearAisle() {
		return _seatsCoGFrontToRearAisle;
	}

	public void set_seatsCoGFrontToRearAisle(List<Amount<Length>> _seatsCoGFrontToRearAisle) {
		this._seatsCoGFrontToRearAisle = _seatsCoGFrontToRearAisle;
	}

	public List<Amount<Length>> get_seatsCoGrearToFrontAisle() {
		return _seatsCoGrearToFrontAisle;
	}

	public void set_seatsCoGrearToFrontAisle(List<Amount<Length>> _seatsCoGrearToFrontAisle) {
		this._seatsCoGrearToFrontAisle = _seatsCoGrearToFrontAisle;
	}

	public List<Amount<Length>> get_seatsCoGFrontToRearOther() {
		return _seatsCoGFrontToRearOther;
	}

	public void set_seatsCoGFrontToRearOther(List<Amount<Length>> _seatsCoGFrontToRearOther) {
		this._seatsCoGFrontToRearOther = _seatsCoGFrontToRearOther;
	}

	public List<Amount<Length>> get_seatsCoGrearToFrontOther() {
		return _seatsCoGrearToFrontOther;
	}

	public void set_seatsCoGrearToFrontOther(List<Amount<Length>> _seatsCoGrearToFrontOther) {
		this._seatsCoGrearToFrontOther = _seatsCoGrearToFrontOther;
	}

	public List<Amount<Mass>> get_currentMassList() {
		return _currentMassList;
	}

	public void set_currentMassList(List<Amount<Mass>> _currentMassList) {
		this._currentMassList = _currentMassList;
	}

	public List<Amount<Length>> get_seatsCoGFrontToRear() {
		return _seatsCoGFrontToRear;
	}

	public void set_seatsCoGFrontToRear(List<Amount<Length>> _seatsCoGFrontToRear) {
		this._seatsCoGFrontToRear = _seatsCoGFrontToRear;
	}

	public List<Amount<Length>> get_seatsCoGRearToFront() {
		return _seatsCoGRearToFront;
	}

	public void set_seatsCoGRearToFront(List<Amount<Length>> _seatsCoGRearToFront) {
		this._seatsCoGRearToFront = _seatsCoGRearToFront;
	}

	public static String getId() {
		return _id;
	}

	public MyArray get_xLoading() {
		return _xLoading;
	}

	public MyArray get_yLoading() {
		return _yLoading;
	}

	public Double get_flightCrewNumber() {
		return _flightCrewNumber;
	}

	public void set_flightCrewNumber(Double _flightCrewNumber) {
		this._flightCrewNumber = _flightCrewNumber;
	}

	public Double get_cabinCrewNumber() {
		return _cabinCrewNumber;
	}

}
