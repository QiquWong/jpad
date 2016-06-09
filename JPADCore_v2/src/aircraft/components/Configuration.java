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
import org.jscience.physics.amount.AmountFormat;

import aircraft.OperatingConditions;
import aircraft.auxiliary.SeatsBlock;
import aircraft.auxiliary.SeatsBlock.CGboarding;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.ComponentEnum;
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
public class Configuration implements IConfiguration {
	
	private String _id;
	private Fuselage _theFuselage;
	private LiftingSurface _theWing, _theHTail, _theVTail;
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
	private Amount<Mass> _massFurnishingsAndEquipmentReference,
						 _massFurnishingsAndEquipment,
						 _massEstimatedFurnishingsAndEquipment,
						 _mass;

	private Double _nPax, _nCrew, _flightCrewNumber, _cabinCrewNumber, _maxPax, _classesNumber;
	private Integer	_numberOfBreaksEconomyClass, 
					_numberOfBreaksBusinessClass,
					_numberOfBreaksFirstClass;

	private Integer _aislesNumber,
					_numberOfRowsEconomyClass,
					_numberOfRowsBusinessClass,
					_numberOfRowsFirstClass;
	private Integer[] _numberOfColumnsEconomyClass,
					  _numberOfColumnsBusinessClass,
					  _numberOfColumnsFirstClass;

	private Amount<Length> _breakLenght,
						   _pitchFirstClass,
						   _pitchBusinessClass,
						   _pitchEconomyClass,
						   _widthEconomyClass,
						   _widthBusinessClass,
						   _widthFirstClass,
						   _distanceFromWallEconomyClass,
						   _distanceFromWallBusinessClass,
						   _distanceFromWallFirstClass;

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

	////////////////////////////////////
	// TODO:  ADD BUILDER PATTERN     //
	//		  AND OTHER METHOD IN 	  //
	//        ORDER TO READ FROM FILE //
	////////////////////////////////////
	
	/**********************************************************************************************
	 * method that recongnize aircraft name and sets the relative data.
	 * 
	 * @author Vittorio Trifari
	 */
	private void initializeDefaultVariables(AircraftEnum aircraftName) {

		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		
		switch(aircraftName) {
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
		}
	}
	
	@Override
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

	@Override
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
	@Override
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

//				for (int k=0; k < seatsCoGboarding.getCurrentXCoGfrontToRearWindow().size(); k++){
//					_seatsCoGFrontToRearWindow.add(seatsCoGboarding.getCurrentXCoGfrontToRearWindow().get(k));
//					_seatsCoGrearToFrontWindow.add(seatsCoGboarding.getCurrentXCoGrearToFrontWindow().get(k));
//					_seatsCoGFrontToRearAisle.add(seatsCoGboarding.getCurrentXCoGfrontToRearAisle().get(k));
//					_seatsCoGrearToFrontAisle.add(seatsCoGboarding.getCurrentXCoGrearToFrontAisle().get(k));
//		
//					if (_seatsBlockRight.get_columns() > 2 | _seatsBlockLeft.get_columns() > 2 |
//							(_seatsBlockCenter != null && _seatsBlockCenter.get_columns() > 2)) {
//						_seatsCoGFrontToRearOther.add((Amount<Length>) seatsCoGboarding.getCurrentXCoGfrontToRearOther().get(k));
//						_seatsCoGrearToFrontOther.add((Amount<Length>) seatsCoGboarding.getCurrentXCoGrearToFrontOther().get(k));	
//					}
//				}

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


	public Fuselage getFuselage() {
		return _theFuselage;
	}

	@Override
	public LiftingSurface getWing() {
		return _theWing;
	}

	@Override
	public LiftingSurface getHTail() {
		return _theHTail;
	}
	
	@Override
	public LiftingSurface getVTail() {
		return _theVTail;
	}
	
	@Override
	public Nacelle getNacelle() {
		return _theNacelle;
	}

	@Override
	public Double getNPax() {
		return _nPax;
	}

	@Override
	public void setNPax(Double _nPax) {
		this._nPax = _nPax;
	}

	@Override
	public Double getNCrew() {
		return _nCrew;
	}

	@Override
	public void setNCrew(Double _nCrew) {
		this._nCrew = _nCrew;
	}

	@Override
	public Double getMaxPax() {
		return _maxPax;
	}
	
	@Override
	public void setMaxPax(Double _maxPax) {
		this._maxPax = _maxPax;
	}

	@Override
	public Amount<Mass> getMassFurnishings() {
		return _massFurnishingsAndEquipment;
	}
	
	@Override
	public void getMassFurnishingsAndEquipment(Amount<Mass> _massFurnishings) {
		this._massFurnishingsAndEquipment = _massFurnishings;
	}

	@Override
	public Amount<Mass> getMassFurnishingsAndEquipmentReference() {
		return _massFurnishingsAndEquipmentReference;
	}

	@Override
	public void setMassFurnishingsAndEquipmentReference(Amount<Mass> _massReference) {
		this._massFurnishingsAndEquipmentReference = _massReference;
	}
	
	@Override
	public Amount<Mass> getMassEstimatedFurnishingsAndEquipment() {
		return _massEstimatedFurnishingsAndEquipment;
	}

	@Override
	public void setMassEstimatedFurnishingsAndEquipment(Amount<Mass> _massMean) {
		this._massEstimatedFurnishingsAndEquipment = _massMean;
	}
	
	@Override
	public Double[] getPercentDifference() {
		return _percentDifference;
	}
	
	@Override
	public void setPercentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	@Override
	public Amount<Mass> getMass() {
		return _mass;
	}

	@Override
	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	@Override
	public Double getClassesNumber() {
		return _classesNumber;
	}

	@Override
	public void setClassesNumber(Double _classesNumber) {
		this._classesNumber = _classesNumber;
	}

	@Override
	public Integer getAislesNumber() {
		return _aislesNumber;
	}

	@Override
	public void setAislesNumber(Integer _aislesNumber) {
		this._aislesNumber = _aislesNumber;
	}

	@Override
	public List<Amount<Length>> getPitch() {
		return _pitchList;
	}

	@Override
	public void setPitch(List<Amount<Length>> _pitch) {
		this._pitchList = _pitch;
	}

	@Override
	public List<Amount<Length>> getDistanceFromWall() {
		return _distanceFromWallList;
	}

	@Override
	public void setDistanceFromWall(List<Amount<Length>> _distanceFromWall) {
		this._distanceFromWallList = _distanceFromWall;
	}

	@Override
	public List<Integer> getNumberOfBreaks() {
		return _numberOfBreaksList;
	}

	@Override
	public void setNumberOfBreaks(List<Integer> _numberOfBreaks) {
		this._numberOfBreaksList = _numberOfBreaks;
	}

	@Override
	public List<Integer> getNumberOfRows() {
		return _numberOfRowsList;
	}

	@Override
	public void setNumberOfRows(List<Integer> _numberOfRows) {
		this._numberOfRowsList = _numberOfRows;
	}

	@Override
	public List<Integer[]> getNumberOfColumns() {
		return _numberOfColumnsList;
	}
	
	@Override
	public void setNumberOfColumns(List<Integer[]> _numberOfColumns) {
		this._numberOfColumnsList = _numberOfColumns;
	}

	@Override
	public List<Integer[]> getMissingSeatsRow() {
		return _missingSeatsRowList;
	}

	@Override
	public void setMissingSeatsRow(List<Integer[]> _missingSeatsRow) {
		this._missingSeatsRowList = _missingSeatsRow;
	}

	@Override
	public List<Integer[]> getMissingSeatsColumn() {
		return _missingSeatsColumnList;
	}

	@Override
	public void setMissingSeatsColumn(List<Integer[]> _missingSeatsColumn) {
		this._missingSeatsColumnList = _missingSeatsColumn;
	}

	@Override
	public Amount<Length> getPitchFirstClass() {
		return _pitchFirstClass;
	}

	@Override
	public void setPitchFirstClass(Amount<Length> _pitchFirstClass) {
		this._pitchFirstClass = _pitchFirstClass;
	}

	@Override
	public Amount<Length> getPitchBusinessClass() {
		return _pitchBusinessClass;
	}

	@Override
	public void setPitchBusinessClass(Amount<Length> _pitchBusinessClass) {
		this._pitchBusinessClass = _pitchBusinessClass;
	}
	
	@Override
	public Amount<Length> getPitchEconomyClass() {
		return _pitchEconomyClass;
	}

	@Override
	public void setPitchEconomyClass(Amount<Length> _pitchEconomyClass) {
		this._pitchEconomyClass = _pitchEconomyClass;
	}

	@Override
	public List<SeatsBlock> getSeatsBlocksList() {
		return _seatsBlocksList;
	}

	@Override
	public void setSeatsBlocksList(List<SeatsBlock> _seatsBlocksList) {
		this._seatsBlocksList = _seatsBlocksList;
	}

	@Override
	public List<Amount<Length>> getWidth() {
		return _widthList;
	}

	@Override
	public void setWidth(List<Amount<Length>> _width) {
		this._widthList = _width;
	}

	@Override
	public Integer getNumberOfBreaksEconomyClass() {
		return _numberOfBreaksEconomyClass;
	}

	@Override
	public void setNumberOfBreaksEconomyClass(Integer _numberOfBreaksEconomyClass) {
		this._numberOfBreaksEconomyClass = _numberOfBreaksEconomyClass;
	}
	
	@Override
	public Integer getNumberOfBreaksBusinessClass() {
		return _numberOfBreaksBusinessClass;
	}
	
	@Override
	public void setNumberOfBreaksBusinessClass(Integer _numberOfBreaksBusinessClass) {
		this._numberOfBreaksBusinessClass = _numberOfBreaksBusinessClass;
	}
	
	@Override
	public Integer getNumberOfBreaksFirstClass() {
		return _numberOfBreaksFirstClass;
	}
	
	@Override
	public void setNumberOfBreaksFirstClass(Integer _numberOfBreaksFirstClass) {
		this._numberOfBreaksFirstClass = _numberOfBreaksFirstClass;
	}
	
	@Override
	public Integer[] getNumberOfColumnsEconomyClass() {
		return _numberOfColumnsEconomyClass;
	}

	@Override
	public void setNumberOfColumnsEconomyClass(Integer[] _numberOfColumnsEconomyClass) {
		this._numberOfColumnsEconomyClass = _numberOfColumnsEconomyClass;
	}

	@Override
	public Integer[] getNumberOfColumnsBusinessClass() {
		return _numberOfColumnsBusinessClass;
	}

	@Override
	public void setNumberOfColumnsBusinessClass(Integer[] _numberOfColumnsBusinessClass) {
		this._numberOfColumnsBusinessClass = _numberOfColumnsBusinessClass;
	}

	@Override
	public Integer[] getNumberOfColumnsFirstClass() {
		return _numberOfColumnsFirstClass;
	}

	@Override
	public void setNumberOfColumnsFirstClass(Integer[] _numberOfColumnsFirstClass) {
		this._numberOfColumnsFirstClass = _numberOfColumnsFirstClass;
	}

	@Override
	public Integer getNumberOfRowsEconomyClass() {
		return _numberOfRowsEconomyClass;
	}

	@Override
	public void setNumberOfRowsEconomyClass(Integer _numberOfRowsEconomyClass) {
		this._numberOfRowsEconomyClass = _numberOfRowsEconomyClass;
	}

	@Override
	public Integer getNumberOfRowsBusinessClass() {
		return _numberOfRowsBusinessClass;
	}

	@Override
	public void setNumberOfRowsBusinessClass(Integer _numberOfRowsBusinessClass) {
		this._numberOfRowsBusinessClass = _numberOfRowsBusinessClass;
	}

	@Override
	public Integer getNumberOfRowsFirstClass() {
		return _numberOfRowsFirstClass;
	}

	@Override
	public void setNumberOfRowsFirstClass(Integer _numberOfRowsFirstClass) {
		this._numberOfRowsFirstClass = _numberOfRowsFirstClass;
	}
	
	@Override
	public Amount<Length> getWidthEconomyClass() {
		return _widthEconomyClass;
	}
	
	@Override
	public void setWidthEconomyClass(Amount<Length> _widthEconomyClass) {
		this._widthEconomyClass = _widthEconomyClass;
	}

	@Override
	public Amount<Length> getWidthBusinessClass() {
		return _widthBusinessClass;
	}

	@Override
	public void setWidthBusinessClass(Amount<Length> _widthBusinessClass) {
		this._widthBusinessClass = _widthBusinessClass;
	}
	
	@Override
	public Amount<Length> getWidthFirstClass() {
		return _widthFirstClass;
	}

	@Override
	public void setWidthFirstClass(Amount<Length> _widthFirstClass) {
		this._widthFirstClass = _widthFirstClass;
	}

	@Override
	public Amount<Length> getDistanceFromWallEconomyClass() {
		return _distanceFromWallEconomyClass;
	}

	@Override
	public void setDistanceFromWallEconomyClass(Amount<Length> _distanceFromWallEconomyClass) {
		this._distanceFromWallEconomyClass = _distanceFromWallEconomyClass;
	}

	@Override
	public Amount<Length> getDistanceFromWallBusinessClass() {
		return _distanceFromWallBusinessClass;
	}

	@Override
	public void setDistanceFromWallBusinessClass(Amount<Length> _distanceFromWallBusinessClass) {
		this._distanceFromWallBusinessClass = _distanceFromWallBusinessClass;
	}

	@Override
	public Amount<Length> getDistanceFromWallFirstClass() {
		return _distanceFromWallFirstClass;
	}

	@Override
	public void setDistanceFromWallFirstClass(Amount<Length> _distanceFromWallFirstClass) {
		this._distanceFromWallFirstClass = _distanceFromWallFirstClass;
	}
	
	@Override
	public Double[] getLengthOfEachBreakEconomyClass() {
		return _lengthOfEachBreakEconomyClass;
	}
	
	@Override
	public void setLengthOfEachBreakEconomyClass(Double[] _lengthOfEachBreakEconomyClass) {
		this._lengthOfEachBreakEconomyClass = _lengthOfEachBreakEconomyClass;
	}

	@Override
	public Double[] getPitchArr() {
		return _pitchArr;
	}

	@Override
	public void setPitchArr(Double[] _pitchArr) {
		this._pitchArr = _pitchArr;
	}

	@Override
	public Double[] getWidthArr() {
		return _widthArr;
	}
	
	@Override
	public void setWidthArr(Double[] _widthArr) {
		this._widthArr = _widthArr;
	}
	
	@Override
	public Double[] getDistanceFromWallArr() {
		return _distanceFromWallArr;
	}

	@Override
	public void setDistanceFromWallArr(Double[] _distanceFromWallArr) {
		this._distanceFromWallArr = _distanceFromWallArr;
	}

	@Override
	public Integer[] getNumberOfRowsArr() {
		return _numberOfRowsArr;
	}

	@Override
	public void setNumberOfRowsArr(Integer[] _numberOfRowsArr) {
		this._numberOfRowsArr = _numberOfRowsArr;
	}
	
	@Override
	public Double[] getNumberOfBreaksArr() {
		return _numberOfBreaksArr;
	}
	
	@Override
	public void setNumberOfBreaksArr(Double[] _numberOfBreaksArr) {
		this._numberOfBreaksArr = _numberOfBreaksArr;
	}

	@Override
	public Integer[] getNumberOfColumnsArr() {
		return _numberOfColumnsArr;
	}
	
	@Override
	public void setNumberOfColumnsArr(Integer[] _numberOfColumnsArr) {
		this._numberOfColumnsArr = _numberOfColumnsArr;
	}
	
	@Override
	public Double[] getLengthOfEachBreakBusinessClass() {
		return _lengthOfEachBreakBusinessClass;
	}

	@Override
	public void setLengthOfEachBreakBusinessClass(Double[] _lengthOfEachBreakBusinessClass) {
		this._lengthOfEachBreakBusinessClass = _lengthOfEachBreakBusinessClass;
	}
	
	@Override
	public Double[] getLengthOfEachBreakFirstClass() {
		return _lengthOfEachBreakFirstClass;
	}

	@Override
	public void setLengthOfEachBreakFirstClass(Double[] _lengthOfEachBreakFirstClass) {
		this._lengthOfEachBreakFirstClass = _lengthOfEachBreakFirstClass;
	}

	@Override
	public Amount<Length> getSeatsCoG() {
		return _seatsCoG;
	}

	@Override
	public Amount<Length> getXCoordinateFirstRow() {
		return _xCoordinateFirstRow;
	}

	@Override
	public void setXCoordinateFirstRow(Amount<Length> _xCoordinateFirstRow) {
		this._xCoordinateFirstRow = _xCoordinateFirstRow;
	}

	@Override
	public List<Amount<Length>> getSeatsCoGFrontToRearWindow() {
		return _seatsCoGFrontToRearWindow;
	}
	
	@Override
	public List<Amount<Length>> getSeatsCoGrearToFrontWindow() {
		return _seatsCoGrearToFrontWindow;
	}
	
	@Override
	public List<Amount<Length>> getSeatsCoGFrontToRearAisle() {
		return _seatsCoGFrontToRearAisle;
	}

	@Override
	public void setSeatsCoGFrontToRearAisle(List<Amount<Length>> _seatsCoGFrontToRearAisle) {
		this._seatsCoGFrontToRearAisle = _seatsCoGFrontToRearAisle;
	}

	@Override
	public List<Amount<Length>> getSeatsCoGrearToFrontAisle() {
		return _seatsCoGrearToFrontAisle;
	}

	@Override
	public void setSeatsCoGrearToFrontAisle(List<Amount<Length>> _seatsCoGrearToFrontAisle) {
		this._seatsCoGrearToFrontAisle = _seatsCoGrearToFrontAisle;
	}

	@Override
	public List<Amount<Length>> getSeatsCoGFrontToRearOther() {
		return _seatsCoGFrontToRearOther;
	}

	@Override
	public void setSeatsCoGFrontToRearOther(List<Amount<Length>> _seatsCoGFrontToRearOther) {
		this._seatsCoGFrontToRearOther = _seatsCoGFrontToRearOther;
	}

	@Override
	public List<Amount<Length>> getSeatsCoGrearToFrontOther() {
		return _seatsCoGrearToFrontOther;
	}

	@Override
	public void setSeatsCoGrearToFrontOther(List<Amount<Length>> _seatsCoGrearToFrontOther) {
		this._seatsCoGrearToFrontOther = _seatsCoGrearToFrontOther;
	}

	@Override
	public List<Amount<Mass>> getCurrentMassList() {
		return _currentMassList;
	}

	@Override
	public void setCurrentMassList(List<Amount<Mass>> _currentMassList) {
		this._currentMassList = _currentMassList;
	}

	@Override
	public List<Amount<Length>> getSeatsCoGFrontToRear() {
		return _seatsCoGFrontToRear;
	}

	@Override
	public void setSeatsCoGFrontToRear(List<Amount<Length>> _seatsCoGFrontToRear) {
		this._seatsCoGFrontToRear = _seatsCoGFrontToRear;
	}

	@Override
	public List<Amount<Length>> getSeatsCoGRearToFront() {
		return _seatsCoGRearToFront;
	}

	@Override
	public void setSeatsCoGRearToFront(List<Amount<Length>> _seatsCoGRearToFront) {
		this._seatsCoGRearToFront = _seatsCoGRearToFront;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public MyArray getXLoading() {
		return _xLoading;
	}

	@Override
	public MyArray getYLoading() {
		return _yLoading;
	}

	@Override
	public Double getFlightCrewNumber() {
		return _flightCrewNumber;
	}

	@Override
	public void setFlightCrewNumber(Double _flightCrewNumber) {
		this._flightCrewNumber = _flightCrewNumber;
	}
	
	@Override
	public Double getCabinCrewNumber() {
		return _cabinCrewNumber;
	}

}
