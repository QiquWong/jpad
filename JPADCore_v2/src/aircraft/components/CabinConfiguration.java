package aircraft.components;

import static java.lang.Math.round;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;

import aircraft.auxiliary.SeatsBlock;
import aircraft.auxiliary.SeatsBlock.CGboarding;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.RelativePositionEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;

/**
 * Define the cabin configuration (full economy, economy + business) and current flight configuration
 * (in terms of number of passengers, number of crew members).
 * 
 * The number of passengers and their location is necessary for estimating 
 * the Aircraft Center of Gravity position.
 * 
 * @author Lorenzo Attanasio, Vittorio Trifari
 *
 */
public class CabinConfiguration implements ICabinConfiguration {

	private String _id;
	private File _cabinConfigurationPath;
	private SeatsBlock _seatsBlockRight,	
					   _seatsBlockLeft,
					   _seatsBlockCenter;

	private Map<MethodEnum, Amount<?>> _massMap;
	private Map<Integer, Amount<Length>> _breaksMap;
	private Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap;

	private List<MethodEnum> _methodsList;
	private List<SeatsBlock> _seatsBlocksList;
	private MyArray _xLoading,
					_yLoading;

	private Double[] _percentDifference;
	private Amount<Mass> _massFurnishingsAndEquipmentReference,
						 _massFurnishingsAndEquipment,
						 _massEstimatedFurnishingsAndEquipment;
	
	private Integer _nPax,
				   _nCrew,
				   _flightCrewNumber,
				   _cabinCrewNumber,
				   _maxPax,
				   _classesNumber;
	
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

	private Amount<Length> _pitchFirstClass,
						   _pitchBusinessClass,
						   _pitchEconomyClass,
						   _widthEconomyClass,
						   _widthBusinessClass,
						   _widthFirstClass,
						   _distanceFromWallEconomyClass,
						   _distanceFromWallBusinessClass,
						   _distanceFromWallFirstClass;

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
	private List<Amount<Length>> _pitchList; 
	private List<Amount<Length>> _widthList; 
	private List<Amount<Length>> _distanceFromWallList;
	private List<Integer> _numberOfBreaksList;
	private List<Integer> _numberOfRowsList;
	private List<Integer[]> _numberOfColumnsList;
	private List<Integer[]> _missingSeatsRowList;
	private List<ClassTypeEnum> _typeList;
	private List<Map<Integer, Amount<Length>>> _breaksMapList;
	private Amount<Length> _seatsCoG;
	private Double[] _pitchArr;
	private Double[] _widthArr;
	private Double[] _distanceFromWallArr;
	private Integer[] _numberOfRowsArr;
	private Double[] _numberOfBreaksArr;
	private Integer[] _numberOfColumnsArr;
	private List<Amount<Length>> _seatsCoGFrontToRearWindow;
	private List<Amount<Length>> _seatsCoGrearToFrontWindow;
	private List<Amount<Length>> _seatsCoGFrontToRearAisle;
	private List<Amount<Length>> _seatsCoGrearToFrontAisle;
	private List<Amount<Length>> _seatsCoGrearToFrontOther;
	private List<Amount<Length>> _seatsCoGFrontToRearOther;
	private List<Amount<Mass>> _currentMassList;
	private List<Amount<Length>> _seatsCoGFrontToRear;
	private List<Amount<Length>> _seatsCoGRearToFront;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ConfigurationBuilder {

		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		private Amount<Mass> __massFurnishingsAndEquipmentReference;
		
		private Integer __nPax,
					    __flightCrewNumber,
					    __maxPax, 
					    __classesNumber,
						__numberOfBreaksEconomyClass, 
						__numberOfBreaksBusinessClass,
						__numberOfBreaksFirstClass,
						__aislesNumber,
						__numberOfRowsEconomyClass,
						__numberOfRowsBusinessClass,
						__numberOfRowsFirstClass;
	
		private Integer[] __numberOfColumnsEconomyClass,
						  __numberOfColumnsBusinessClass,
						  __numberOfColumnsFirstClass;

		private Amount<Length> __pitchFirstClass,
							   __pitchBusinessClass,
							   __pitchEconomyClass,
							   __widthEconomyClass,
							   __widthBusinessClass,
							   __widthFirstClass,
							   __distanceFromWallEconomyClass,
							   __distanceFromWallBusinessClass,
							   __distanceFromWallFirstClass,
							   __xCoordinateFirstRow;

		private List<Integer[]> __missingSeatsRowList = new ArrayList<Integer[]>();
		private List<ClassTypeEnum> __typeList = new ArrayList<ClassTypeEnum>();

		private Map<MethodEnum, Amount<?>> __massMap = new TreeMap<MethodEnum, Amount<?>>();
		private Map<Integer, Amount<Length>> __breaksMap = new HashMap<Integer, Amount<Length>>();
		private Map<AnalysisTypeEnum, List<MethodEnum>> __methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
		private List<MethodEnum> __methodsList = new ArrayList<MethodEnum>();
		private List<SeatsBlock> __seatsBlocksList = new ArrayList<SeatsBlock>();
		private MyArray __xLoading = new MyArray(),  
						__yLoading = new MyArray();
		
		private List<Amount<Length>> __pitchList = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __widthList = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __distanceFromWallList = new ArrayList<Amount<Length>>();
		private List<Integer> __numberOfBreaksList = new ArrayList<Integer>();
		private List<Integer> __numberOfRowsList = new ArrayList<Integer>();
		private List<Integer[]> __numberOfColumnsList = new ArrayList<Integer[]>();
		private List<Map<Integer, Amount<Length>>> __breaksMapList = new ArrayList<Map<Integer, Amount<Length>>>();

		private List<Amount<Length>> __seatsCoGFrontToRearWindow = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __seatsCoGrearToFrontWindow = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __seatsCoGFrontToRearAisle = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __seatsCoGrearToFrontAisle = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __seatsCoGrearToFrontOther = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> __seatsCoGFrontToRearOther = new ArrayList<Amount<Length>>();
		
		public ConfigurationBuilder (String id) {
			this.__id = id;
//			this.initializeDefaultVariables(AircraftEnum.ATR72); // initialize with ATR-72 data
		}
		
		public ConfigurationBuilder (String id, AircraftEnum aircraftName) {
			this.__id = id;
			this.initializeDefaultVariables(aircraftName);
		}

		public ConfigurationBuilder massFurnishingsAndEquipmentReference (Amount<Mass> massFurnishingsAndEquipmentReference) { 
			__massFurnishingsAndEquipmentReference = massFurnishingsAndEquipmentReference;
			return this;
		}
		
		public ConfigurationBuilder nPax (Integer nPax) { 
			__nPax = nPax;
			return this;
		}

		public ConfigurationBuilder flightCrewNumber (Integer flightCrewNumber) { 
			__flightCrewNumber = flightCrewNumber;
			return this;
		}
		
		public ConfigurationBuilder maxPax (Integer maxPax) { 
			__maxPax = maxPax;
			return this;
		}
		
		public ConfigurationBuilder classesNumber (Integer classesNumber) { 
			__classesNumber = classesNumber;
			return this;
		}
		
		public ConfigurationBuilder numberOfBreaksEconomyClass (Integer numberOfBreaksEconomyClass) { 
			__numberOfBreaksEconomyClass = numberOfBreaksEconomyClass;
			return this;
		}
		
		public ConfigurationBuilder numberOfBreaksBusinessClass (Integer numberOfBreaksBusinessClass) { 
			__numberOfBreaksBusinessClass = numberOfBreaksBusinessClass;
			return this;
		}
		
		public ConfigurationBuilder numberOfBreaksFirstClass (Integer numberOfBreaksFirstClass) { 
			__numberOfBreaksFirstClass = numberOfBreaksFirstClass;
			return this;
		}
		
		public ConfigurationBuilder aislesNumber (Integer aislesNumber) { 
			__aislesNumber = aislesNumber;
			return this;
		}
		
		public ConfigurationBuilder numberOfRowsEconomyClass (Integer numberOfRowsEconomyClass) { 
			__numberOfRowsEconomyClass = numberOfRowsEconomyClass;
			return this;
		}
		
		public ConfigurationBuilder numberOfRowsBusinessClass (Integer numberOfRowsBusinessClass) { 
			__numberOfRowsBusinessClass = numberOfRowsBusinessClass;
			return this;
		}
		
		public ConfigurationBuilder numberOfRowsFirstClass (Integer numberOfRowsFirstClass) { 
			__numberOfRowsFirstClass = numberOfRowsFirstClass;
			return this;
		}
		
		public ConfigurationBuilder numberOfColumnsEconomyClass (Integer[] numberOfColumnsEconomyClass) { 
			__numberOfColumnsEconomyClass = numberOfColumnsEconomyClass;
			return this;
		}
		
		public ConfigurationBuilder numberOfColumnsBusinessClass (Integer[] numberOfColumnsBusinessClass) { 
			__numberOfColumnsBusinessClass = numberOfColumnsBusinessClass;
			return this;
		}
		
		public ConfigurationBuilder numberOfColumnsFirstClass (Integer[] numberOfColumnsFirstClass) { 
			__numberOfColumnsFirstClass  = numberOfColumnsFirstClass;
			return this;
		}
		
		public ConfigurationBuilder pitchFirstClass (Amount<Length> pitchFirstClass) { 
			__pitchFirstClass  = pitchFirstClass;
			return this;
		}
		
		public ConfigurationBuilder pitchBusinessClass (Amount<Length> pitchBusinessClass) { 
			__pitchBusinessClass  = pitchBusinessClass;
			return this;
		}
		
		public ConfigurationBuilder pitchEconomyClass (Amount<Length> pitchEconomyClass) { 
			__pitchEconomyClass  = pitchEconomyClass;
			return this;
		}
		
		public ConfigurationBuilder widthEconomyClass (Amount<Length> widthEconomyClass) { 
			__widthEconomyClass  = widthEconomyClass;
			return this;
		}
		
		public ConfigurationBuilder widthBusinessClass (Amount<Length> widthBusinessClass) { 
			__widthBusinessClass  = widthBusinessClass;
			return this;
		}
		
		public ConfigurationBuilder widthFirstClass (Amount<Length> widthFirstClass) { 
			__widthFirstClass  = widthFirstClass;
			return this;
		}
		
		public ConfigurationBuilder distanceFromWallEconomyClass (Amount<Length> distanceFromWallEconomyClass) { 
			__distanceFromWallEconomyClass  = distanceFromWallEconomyClass;
			return this;
		}
		
		public ConfigurationBuilder distanceFromWallBusinessClass (Amount<Length> distanceFromWallBusinessClass) { 
			__distanceFromWallBusinessClass  = distanceFromWallBusinessClass;
			return this;
		}
		
		public ConfigurationBuilder distanceFromWallFirstClass (Amount<Length> distanceFromWallFirstClass) { 
			__distanceFromWallFirstClass  = distanceFromWallFirstClass;
			return this;
		}
		
		public ConfigurationBuilder xCoordinateFirstRow (Amount<Length> xCoordinateFirstRow) { 
			__xCoordinateFirstRow  = xCoordinateFirstRow;
			return this;
		}
		
		public ConfigurationBuilder missingSeatsRowList (List<Integer[]> missingSeatsRowList) { 
			__missingSeatsRowList = missingSeatsRowList;
			return this;
		}
		
		public ConfigurationBuilder typeList (List<ClassTypeEnum> typeList) { 
			__typeList = typeList;
			return this;
		}
		
		public CabinConfiguration build() {
			return new CabinConfiguration(this);
		}
		
		/**********************************************************************************************
		 * method that recongnize aircraft name and sets the relative data.
		 * 
		 * @author Vittorio Trifari
		 */
		@SuppressWarnings("incomplete-switch")
		private void initializeDefaultVariables(AircraftEnum aircraftName) {

			AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

			switch(aircraftName) {
			case ATR72:
				__massFurnishingsAndEquipmentReference = Amount.valueOf(2000., SI.KILOGRAM);

				__maxPax = 72;
				__nPax = 68;
				__flightCrewNumber = 2;

				// Number of classes
				__classesNumber = 1;

				// Number of aisles
				__aislesNumber = new Integer(1);

				__xCoordinateFirstRow = Amount.valueOf(6.16, SI.METER);

				__pitchEconomyClass = Amount.valueOf(0.79, SI.METER);
				__pitchBusinessClass = Amount.valueOf(0., SI.METER);
				__pitchFirstClass = Amount.valueOf(0., SI.METER);

				__widthEconomyClass = Amount.valueOf(0.5, SI.METER);
				__widthBusinessClass = Amount.valueOf(0., SI.METER);
				__widthFirstClass = Amount.valueOf(0., SI.METER);

				__distanceFromWallEconomyClass = Amount.valueOf(0.0845, SI.METER);
				__distanceFromWallBusinessClass = Amount.valueOf(0.0845, SI.METER);
				__distanceFromWallFirstClass = Amount.valueOf(0.0845, SI.METER);

				__numberOfBreaksEconomyClass = new Integer(-1);
				__numberOfBreaksBusinessClass = new Integer(-1);
				__numberOfBreaksFirstClass = new Integer(-1);

				__numberOfRowsEconomyClass = new Integer(16);
				__numberOfRowsBusinessClass = new Integer(0);
				__numberOfRowsFirstClass = new Integer(0);

				__numberOfColumnsEconomyClass = new Integer[]{2,2};
				__numberOfColumnsBusinessClass = new Integer[]{0,0};
				__numberOfColumnsFirstClass = new Integer[]{0,0};

				// _missingSeatsRowList.size() must be equal to number of classes
				__missingSeatsRowList.add(new Integer[] { -1});
				__missingSeatsRowList.add(new Integer[] { -1});
				__missingSeatsRowList.add(new Integer[] { -1});

				__typeList.add(ClassTypeEnum.ECONOMY);
				__typeList.add(ClassTypeEnum.BUSINESS);
				__typeList.add(ClassTypeEnum.FIRST);

				break;

			case B747_100B:
				// see Roskam (Part V) Appendix-A pag.152 --> Fixed Equipment Total
				__massFurnishingsAndEquipmentReference = Amount.valueOf(28000., SI.KILOGRAM);

				__maxPax = 550;
				__nPax = 550;
				__flightCrewNumber = 2;

				// Number of classes
				__classesNumber = 1;

				// Number of aisles
				__aislesNumber = new Integer(1);

				__xCoordinateFirstRow = Amount.valueOf(7.45, SI.METER);

				__pitchEconomyClass = Amount.valueOf(0.79, SI.METER);
				__pitchBusinessClass = Amount.valueOf(0., SI.METER);
				__pitchFirstClass = Amount.valueOf(0., SI.METER);

				__widthEconomyClass = Amount.valueOf(0.5, SI.METER);
				__widthBusinessClass = Amount.valueOf(0., SI.METER);
				__widthFirstClass = Amount.valueOf(0., SI.METER);

				__distanceFromWallEconomyClass = Amount.valueOf(0.0845, SI.METER);
				__distanceFromWallBusinessClass = Amount.valueOf(0.0845, SI.METER);
				__distanceFromWallFirstClass = Amount.valueOf(0.0845, SI.METER);

				__numberOfBreaksEconomyClass = new Integer(-1);
				__numberOfBreaksBusinessClass = new Integer(-1);
				__numberOfBreaksFirstClass = new Integer(-1);

				__numberOfRowsEconomyClass = new Integer(55);
				__numberOfRowsBusinessClass = new Integer(0);
				__numberOfRowsFirstClass = new Integer(0);

				__numberOfColumnsEconomyClass = new Integer[]{2,2};
				__numberOfColumnsBusinessClass = new Integer[]{0,0};
				__numberOfColumnsFirstClass = new Integer[]{0,0};

				// _missingSeatsRowList.size() must be equal to number of classes
				__missingSeatsRowList.add(new Integer[] { -1});
				__missingSeatsRowList.add(new Integer[] { -1});
				__missingSeatsRowList.add(new Integer[] { -1});

				__typeList.add(ClassTypeEnum.ECONOMY);
				__typeList.add(ClassTypeEnum.BUSINESS);
				__typeList.add(ClassTypeEnum.FIRST);

				break;

			case AGILE_DC1:
				// see Roskam (Part V) Appendix-A pag.152 --> Fixed Equipment Total
				__massFurnishingsAndEquipmentReference = Amount.valueOf(1853., SI.KILOGRAM);

				__maxPax = 90;
				__nPax = 90;
				__flightCrewNumber = 2;

				// Number of classes
				__classesNumber = 1;

				// Number of aisles
				__aislesNumber = new Integer(1);

				__xCoordinateFirstRow = Amount.valueOf(7.40, SI.METER);

				__pitchEconomyClass = Amount.valueOf(0.80, SI.METER);
				__pitchBusinessClass = Amount.valueOf(0., SI.METER);
				__pitchFirstClass = Amount.valueOf(0., SI.METER);

				__widthEconomyClass = Amount.valueOf(0.4, SI.METER);
				__widthBusinessClass = Amount.valueOf(0., SI.METER);
				__widthFirstClass = Amount.valueOf(0., SI.METER);

				__distanceFromWallEconomyClass = Amount.valueOf(0.1, SI.METER);
				__distanceFromWallBusinessClass = Amount.valueOf(0.1, SI.METER);
				__distanceFromWallFirstClass = Amount.valueOf(0.1, SI.METER);

				__numberOfBreaksEconomyClass = new Integer(-1);
				__numberOfBreaksBusinessClass = new Integer(-1);
				__numberOfBreaksFirstClass = new Integer(-1);

				__numberOfRowsEconomyClass = new Integer(22);
				__numberOfRowsBusinessClass = new Integer(0);
				__numberOfRowsFirstClass = new Integer(0);

				__numberOfColumnsEconomyClass = new Integer[]{2,2};
				__numberOfColumnsBusinessClass = new Integer[]{0,0};
				__numberOfColumnsFirstClass = new Integer[]{0,0};

				// _missingSeatsRowList.size() must be equal to number of classes
				__missingSeatsRowList.add(new Integer[] { -1});
				__missingSeatsRowList.add(new Integer[] { -1});
				__missingSeatsRowList.add(new Integer[] { -1});

				__typeList.add(ClassTypeEnum.ECONOMY);
				__typeList.add(ClassTypeEnum.BUSINESS);
				__typeList.add(ClassTypeEnum.FIRST);

				break;
			}
		}
	}

	private CabinConfiguration (ConfigurationBuilder builder) {
		
		this._id = builder.__id;
		
		this._massFurnishingsAndEquipmentReference = builder.__massFurnishingsAndEquipmentReference;
		
		this._nPax = builder.__nPax;
		this._flightCrewNumber = builder.__flightCrewNumber;
		this._maxPax = builder.__maxPax; 
		this._classesNumber = builder.__classesNumber;
		this._numberOfBreaksEconomyClass = builder.__numberOfBreaksEconomyClass; 
		this._numberOfBreaksBusinessClass = builder.__numberOfBreaksBusinessClass;
		this._numberOfBreaksFirstClass = builder.__numberOfBreaksFirstClass;

		this._aislesNumber = builder.__aislesNumber;
		this._numberOfRowsEconomyClass = builder.__numberOfRowsEconomyClass;
		this._numberOfRowsBusinessClass = builder.__numberOfRowsBusinessClass;
		this._numberOfRowsFirstClass = builder.__numberOfRowsFirstClass;
		this._numberOfColumnsEconomyClass = builder.__numberOfColumnsEconomyClass;
		this._numberOfColumnsBusinessClass = builder.__numberOfColumnsBusinessClass;
		this._numberOfColumnsFirstClass = builder.__numberOfColumnsFirstClass;

		this._pitchFirstClass = builder.__pitchFirstClass;
		this._pitchBusinessClass = builder.__pitchBusinessClass;
		this._pitchEconomyClass = builder.__pitchEconomyClass;
		this._widthEconomyClass = builder.__widthEconomyClass;
		this._widthBusinessClass = builder.__widthBusinessClass;
		this._widthFirstClass = builder.__widthFirstClass;
		this._distanceFromWallEconomyClass = builder.__distanceFromWallEconomyClass;
		this._distanceFromWallBusinessClass = builder.__distanceFromWallBusinessClass;
		this._distanceFromWallFirstClass = builder.__distanceFromWallFirstClass;

		this._xCoordinateFirstRow = builder.__xCoordinateFirstRow;
		
		this._missingSeatsRowList = builder.__missingSeatsRowList;
		this._typeList = builder.__typeList;

		this._massMap = builder.__massMap;
		this._breaksMap = builder.__breaksMap;
		this._methodsMap = builder.__methodsMap;
		this._methodsList = builder.__methodsList;
		this._seatsBlocksList = builder.__seatsBlocksList;
		this._xLoading = builder.__xLoading;
		this._yLoading = builder.__yLoading;
		
		this._pitchList = builder.__pitchList;
		this._widthList = builder.__widthList;
		this._distanceFromWallList = builder.__distanceFromWallList;
		this._numberOfBreaksList = builder.__numberOfBreaksList;
		this._numberOfRowsList = builder.__numberOfRowsList;
		this._numberOfColumnsList = builder.__numberOfColumnsList;
		this._breaksMapList = builder.__breaksMapList;

		this._seatsCoGFrontToRearWindow = builder.__seatsCoGFrontToRearWindow;
		this._seatsCoGrearToFrontWindow = builder.__seatsCoGrearToFrontWindow;
		this._seatsCoGFrontToRearAisle = builder.__seatsCoGFrontToRearAisle;
		this._seatsCoGrearToFrontAisle = builder.__seatsCoGrearToFrontAisle;
		this._seatsCoGrearToFrontOther = builder.__seatsCoGrearToFrontOther;
		this._seatsCoGFrontToRearOther = builder.__seatsCoGFrontToRearOther;
		
		this.calculateDependentVariables();
		
	}
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	@SuppressWarnings("unchecked")
	public static CabinConfiguration importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading configuration data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//------------------------------------------------------------------
		// GLOBAL DATA
		Integer actualPassengerNumber = null;
		Integer maximumPassengerNumber = null; 
		Integer flightCrewNumber = null;
		Integer classesNumber = null; 
		List<ClassTypeEnum> classesType = new ArrayList<>();
		Integer aislesNumber = null; 
		Amount<Length> xCoordinatesFirstRow = null; 
		List<Integer[]> missingSeatsRow = new ArrayList<>();
		
		//..................................................................		
		String actualPassengerNumberProperty =  reader.getXMLPropertyByPath("//global_data/actual_passengers_number");
		if(actualPassengerNumberProperty != null) 
			actualPassengerNumber = Integer.valueOf(actualPassengerNumberProperty);
		//..................................................................
		String maximumPassengerNumberProperty =  reader.getXMLPropertyByPath("//global_data/maximum_passengers_number");
		if(actualPassengerNumberProperty != null) 
			maximumPassengerNumber = Integer.valueOf(maximumPassengerNumberProperty);
		//..................................................................
		String flightCrewNumberProperty =  reader.getXMLPropertyByPath("//global_data/flight_crew_number");
		if(flightCrewNumberProperty != null)
			flightCrewNumber = Integer.valueOf(flightCrewNumberProperty);
		//..................................................................
		String classesNumberProperty =  reader.getXMLPropertyByPath("//global_data/classes_number");
		if(classesNumberProperty != null)
			classesNumber = Integer.valueOf(classesNumberProperty);
		//..................................................................
		List<String> classesTypeProperty = reader.getXMLPropertiesByPath("//global_data/classes_type");
		if(!classesTypeProperty.isEmpty()) {
			List<String> classesTypeList = JPADXmlReader.readArrayFromXML(
					reader.getXMLPropertiesByPath("//global_data/classes_type")
					.get(0));
			classesType = new ArrayList<ClassTypeEnum>();
			for(int i=0; i<classesTypeList.size(); i++) {
				if(classesTypeList.get(i).equalsIgnoreCase("ECONOMY"))
					classesType.add(ClassTypeEnum.ECONOMY);
				else if(classesTypeList.get(i).equalsIgnoreCase("BUSINESS"))
					classesType.add(ClassTypeEnum.BUSINESS);
				else if(classesTypeList.get(i).equalsIgnoreCase("FIRST"))
					classesType.add(ClassTypeEnum.FIRST);
				else {
					System.err.println("\n\tERROR: INVALID CLASS TYPE !!\n");
					return null;
				}
			}
		}
		//..................................................................
		String aislesNumberProperty =  reader.getXMLPropertyByPath("//global_data/aisles_number");
		if(aislesNumberProperty != null)
		aislesNumber = Integer.valueOf(aislesNumberProperty);
		//..................................................................
		String xCoordinatesFirstRowProperty =  reader.getXMLPropertyByPath("//global_data/x_coordinates_first_row");
		if(xCoordinatesFirstRowProperty != null)
			xCoordinatesFirstRow = reader.getXMLAmountLengthByPath("//global_data/x_coordinates_first_row");
		//..................................................................
		List<String> missingSeatsRowProperty = reader.getXMLPropertiesByPath("//value");
		if(missingSeatsRowProperty.size() != classesNumber) {
			System.err.println("THE NUMBER OF MISSING SEAT ROW TAGS HAVE TO BE EQUAL TO THE CLASSES NUMBER !!");
			return null;
		}
		if((missingSeatsRowProperty.isEmpty())) {
			for(int i=0; i<classesNumber; i++)
				missingSeatsRow.add(new Integer[] {-1});
		}
		else {
			for(int i=0; i<classesNumber; i++){
				List<String> tempString = JPADXmlReader.readArrayFromXML(
						missingSeatsRowProperty.get(i)
						);
				Integer[] tempInt = new Integer[tempString.size()];
				for(int j=0; j<tempString.size(); j++) {
					tempInt[j] = Integer.valueOf(tempString.get(j));
				}
				missingSeatsRow.add(tempInt);
			}
		}
		if(missingSeatsRow.size() != classesNumber) {
			System.err.println("ERROR : MISSING SEATS ROW LIST MUST HAVE THE SAME SIZE OF CLASSES NUMBER");
			return null;
		}
		
		//---------------------------------------------------------------
		// DETAILED DATA
		Integer numberOfBreaksEconomyClass = null;
		Integer numberOfBreaksBusinessClass = null;
		Integer numberOfBreaksFirstClass = null;
		Integer numberOfRowsEconomyClass = null;
		Integer numberOfRowsBusinessClass = null;
		Integer numberOfRowsFirstClass = null;
		Integer[] numberOfColumnsEconomyClass = null;
		Integer[] numberOfColumnsBusinessClass = null;
		Integer[] numberOfColumnsFirstClass = null;
		Amount<Length> pitchEconomyClass = null;
		Amount<Length> pitchBusinessClass = null;
		Amount<Length> pitchFirstClass = null;
		Amount<Length> widthEconomyClass = null;
		Amount<Length> widthBusinessClass = null;
		Amount<Length> widthFirstClass = null;
		Amount<Length> distanceFromWallEconomyClass = null;
		Amount<Length> distanceFromWallBusinessClass = null;
		Amount<Length> distanceFromWallFirstClass = null;

		//..................................................................
		String numberOfBreaksEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_breaks_economy_class");
		if(numberOfBreaksEconomyClassProperty != null)
			numberOfBreaksEconomyClass = Integer.valueOf(numberOfBreaksEconomyClassProperty);
		//..................................................................
		String numberOfBreaksBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_breaks_business_class");
		if(numberOfBreaksBusinessClassProperty != null)
			numberOfBreaksBusinessClass = Integer.valueOf(numberOfBreaksBusinessClassProperty);
		//..................................................................
		String numberOfBreaksFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_breaks_first_class");
		if(numberOfBreaksFirstClassProperty != null)
			numberOfBreaksFirstClass = Integer.valueOf(numberOfBreaksFirstClassProperty);
		//..................................................................
		String numberOfRowsEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_rows_economy_class");
		if(numberOfRowsEconomyClassProperty != null)
			numberOfRowsEconomyClass = Integer.valueOf(numberOfRowsEconomyClassProperty);
		//..................................................................
		String numberOfRowsBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_rows_business_class");
		if(numberOfRowsBusinessClassProperty != null)
			numberOfRowsBusinessClass = Integer.valueOf(numberOfRowsBusinessClassProperty);
		//..................................................................
		String numberOfRowsFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_rows_first_class");
		if(numberOfRowsFirstClassProperty != null)
			numberOfRowsFirstClass = Integer.valueOf(numberOfRowsFirstClassProperty);
		//..................................................................
		List<String> numberOfColumnsEconomyClassProperty = reader.getXMLPropertiesByPath("//detailed_data/number_of_columns_economy_class");
		if(!numberOfColumnsEconomyClassProperty.isEmpty()) {
			List<String> numberOfColumnsEconomyClassArray = JPADXmlReader
					.readArrayFromXML(
							reader.getXMLPropertiesByPath(
									"//detailed_data/number_of_columns_economy_class"
									)
							.get(0)
							);
			numberOfColumnsEconomyClass = new Integer[numberOfColumnsEconomyClassArray.size()];
			for(int i=0; i<numberOfColumnsEconomyClassArray.size(); i++)
				numberOfColumnsEconomyClass[i] = Integer.valueOf(
						numberOfColumnsEconomyClassArray.get(i)
						);
		}
		//..................................................................
		List<String> numberOfColumnsBusinessClassProperty = reader.getXMLPropertiesByPath("//detailed_data/number_of_columns_business_class");
		if(!numberOfColumnsBusinessClassProperty.isEmpty()) {
			List<String> numberOfColumnsBusinessClassArray = JPADXmlReader
					.readArrayFromXML(
							reader.getXMLPropertiesByPath(
									"//detailed_data/number_of_columns_business_class"
									)
							.get(0)
							);
			numberOfColumnsBusinessClass = new Integer[numberOfColumnsBusinessClassArray.size()];
			for(int i=0; i<numberOfColumnsBusinessClassArray.size(); i++)
				numberOfColumnsBusinessClass[i] = Integer.valueOf(
						numberOfColumnsBusinessClassArray.get(i)
						);
		}
		//..................................................................
		List<String> numberOfColumnsFirstClassProperty = reader.getXMLPropertiesByPath("//detailed_data/number_of_columns_first_class");
		if(!numberOfColumnsFirstClassProperty.isEmpty()) {
			List<String> numberOfColumnsFirstClassArray = JPADXmlReader
					.readArrayFromXML(
							reader.getXMLPropertiesByPath(
									"//detailed_data/number_of_columns_first_class"
									)
							.get(0)
							);
			numberOfColumnsFirstClass = new Integer[numberOfColumnsFirstClassArray.size()];
			for(int i=0; i<numberOfColumnsFirstClassArray.size(); i++)
				numberOfColumnsFirstClass[i] = Integer.valueOf(
						numberOfColumnsFirstClassArray.get(i)
						);
		}
		//..................................................................
		String pitchEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/pitch_economy_class");
		if(pitchEconomyClassProperty != null)
		pitchEconomyClass = reader.getXMLAmountLengthByPath("//detailed_data/pitch_economy_class");
		//..................................................................
		String pitchBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/pitch_business_class");
		if(pitchBusinessClassProperty != null)
			pitchBusinessClass = reader.getXMLAmountLengthByPath("//detailed_data/pitch_business_class");
		//..................................................................
		String pitchFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/pitch_first_class");
		if(pitchFirstClassProperty != null)
			pitchFirstClass = reader.getXMLAmountLengthByPath("//detailed_data/pitch_first_class");
		//..................................................................
		String widthEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/width_economy_class");
		if(widthEconomyClassProperty != null)
			widthEconomyClass = reader.getXMLAmountLengthByPath("//detailed_data/width_economy_class");
		//..................................................................
		String widthBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/width_business_class");
		if(widthBusinessClassProperty != null)
			widthBusinessClass = reader.getXMLAmountLengthByPath("//detailed_data/width_business_class");
		//..................................................................
		String widthFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/width_first_class");
		if(widthFirstClassProperty != null)
			widthFirstClass = reader.getXMLAmountLengthByPath("//detailed_data/width_first_class");
		//..................................................................
		String distanceFromWallEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/distance_from_wall_economy_class");
		if(distanceFromWallEconomyClassProperty != null)
			distanceFromWallEconomyClass = reader.getXMLAmountLengthByPath("//detailed_data/distance_from_wall_economy_class");
		//..................................................................
		String distanceFromWallBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/distance_from_wall_business_class");
		if(distanceFromWallBusinessClassProperty != null)
			distanceFromWallBusinessClass = reader.getXMLAmountLengthByPath("//detailed_data/distance_from_wall_business_class");
		//..................................................................
		String distanceFromWallFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/distance_from_wall_first_class");
		if(distanceFromWallFirstClassProperty != null)
			distanceFromWallFirstClass = reader.getXMLAmountLengthByPath("//detailed_data/distance_from_wall_first_class");
		
		//---------------------------------------------------------------
		//REFERENCE MASS
		Amount<Mass> massFurnishingsAndEquipment = null;
		
		String massFurnishingsAndEquipmentProperty = reader.getXMLPropertyByPath("//reference_masses/mass_furnishings_and_equipment");
		if(massFurnishingsAndEquipmentProperty != null)
			massFurnishingsAndEquipment = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//reference_masses/mass_furnishings_and_equipment");
		else
			massFurnishingsAndEquipment = Amount.valueOf(0.0, SI.KILOGRAM);
		
		CabinConfiguration aircraftConfiguration = new ConfigurationBuilder(id)
				.nPax(actualPassengerNumber)
				.maxPax(maximumPassengerNumber)
				.flightCrewNumber(flightCrewNumber)
				.classesNumber(classesNumber)
				.typeList(classesType)
				.aislesNumber(aislesNumber)
				.xCoordinateFirstRow(xCoordinatesFirstRow)
				.missingSeatsRowList(missingSeatsRow)
				.numberOfBreaksEconomyClass(numberOfBreaksEconomyClass)
				.numberOfBreaksBusinessClass(numberOfBreaksBusinessClass)
				.numberOfBreaksFirstClass(numberOfBreaksFirstClass)
				.numberOfRowsEconomyClass(numberOfRowsEconomyClass)
				.numberOfRowsBusinessClass(numberOfRowsBusinessClass)
				.numberOfRowsFirstClass(numberOfRowsFirstClass)
				.numberOfColumnsEconomyClass(numberOfColumnsEconomyClass)
				.numberOfColumnsBusinessClass(numberOfColumnsBusinessClass)
				.numberOfColumnsFirstClass(numberOfColumnsFirstClass)
				.pitchEconomyClass(pitchEconomyClass)
				.pitchBusinessClass(pitchBusinessClass)
				.pitchFirstClass(pitchFirstClass)
				.widthEconomyClass(widthEconomyClass)
				.widthBusinessClass(widthBusinessClass)
				.widthFirstClass(widthFirstClass)
				.distanceFromWallEconomyClass(distanceFromWallEconomyClass)
				.distanceFromWallBusinessClass(distanceFromWallBusinessClass)
				.distanceFromWallFirstClass(distanceFromWallFirstClass)
				.massFurnishingsAndEquipmentReference(massFurnishingsAndEquipment)
				.build();
		
		return aircraftConfiguration;
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

		_cabinCrewNumber = (int) Math.ceil(_nPax.doubleValue()/35);
		_nCrew = _cabinCrewNumber + _flightCrewNumber;
		updateConfiguration();

	}

	/**
	 * Build a simplified cabin layout: the user has to define the x coordinate
	 * at which the layout starts, the number of classes and, for each class,
	 * number of rows and of abreast, pitch, width, and eventually breaks
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
						_typeList.get(i));

				_seatsBlockCenter.calculateCoG(aircraft);
				_seatsBlocksList.add(_seatsBlockLeft);
				_seatsBlocksList.add(_seatsBlockRight);
				_seatsBlocksList.add(_seatsBlockCenter);

			} else {
				_seatsBlocksList.add(_seatsBlockLeft);	
				_seatsBlocksList.add(_seatsBlockRight);
			}

			length = _seatsBlockRight.getLenghtOverall();

		}

		// CoG variation during boarding procedure in aircraft reference frame
		CGboarding seatsCoGboarding = 
				SeatsBlock.calculateCoGboarding(_seatsBlocksList, aircraft);

		_currentMassList = new ArrayList<>();
		_currentMassList.add(aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass());
		_currentMassList.addAll(seatsCoGboarding.getCurrentMassList());
		
		_seatsCoGFrontToRearWindow = seatsCoGboarding.getCurrentXCoGfrontToRearWindow();
		_seatsCoGrearToFrontWindow = seatsCoGboarding.getCurrentXCoGrearToFrontWindow();
		_seatsCoGFrontToRearAisle = seatsCoGboarding.getCurrentXCoGfrontToRearAisle();
		_seatsCoGrearToFrontAisle = seatsCoGboarding.getCurrentXCoGrearToFrontAisle();

		if (_seatsBlockRight.get_columns() > 2 | _seatsBlockLeft.get_columns() > 2 |
				(_seatsBlockCenter != null && _seatsBlockCenter.get_columns() > 2)) {
			_seatsCoGFrontToRearOther = seatsCoGboarding.getCurrentXCoGfrontToRearOther();
			_seatsCoGrearToFrontOther = seatsCoGboarding.getCurrentXCoGrearToFrontOther();	
		}

		_seatsCoGFrontToRear = new ArrayList<>();
		_seatsCoGFrontToRear.add(aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF());
		_seatsCoGFrontToRear.addAll(seatsCoGboarding.getCurrentXCoGfrontToRear());
		
		_seatsCoGRearToFront = new ArrayList<>();
		_seatsCoGRearToFront.add(aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF());
		_seatsCoGRearToFront.addAll(seatsCoGboarding.getCurrentXCoGrearToFront());

		_xLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_seatsCoGFrontToRear));
		_xLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_seatsCoGRearToFront));

		_yLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_currentMassList));
		_yLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_currentMassList));

		_seatsCoG = SeatsBlock.calculateTotalCoG(_seatsBlocksList).plus(_xCoordinateFirstRow);

		System.out.println("Total CoG: " + _seatsCoG);

		System.out.println("----- CABIN LAYOUT CREATION FINISHED -----");

	}

	@Override
	public void calculateMass(Aircraft aircraft, MethodEnum method) {
		calculateMassFurnishings(aircraft, method);
	}

	@Override
	public void calculateMassFurnishings(Aircraft aircraft,	MethodEnum method) {
		switch (method) {
		case TORENBEEK_2013: { // page 257 Torenbeek 2013

			_massFurnishingsAndEquipment = Amount.valueOf(
					(12
							* aircraft.getFuselage().getFuselageCreator().getLenF().getEstimatedValue()
							* aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue() 
							* ( 3
									* aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue() 
									+ 0.5 * aircraft.getFuselage().getDeckNumber() + 1) + 3500) /
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

		_massEstimatedFurnishingsAndEquipment = _massFurnishingsAndEquipment;
	}
	
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tConfiguration\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "'\n")
				.append("\t.....................................\n")
				.append("\tActual number of passengers: " + _nPax + "\n")
				.append("\tMaximum number of passengers: " + _maxPax + "\n")
				.append("\tFlight crew number: " + _flightCrewNumber + "\n")
				.append("\tClasses number: " + _classesNumber + "\n")
				.append("\tClasses type: " + _typeList + "\n")
				.append("\tAisles number: " + _aislesNumber + "\n")
				.append("\tX coordinates first row: " + _xCoordinateFirstRow + "\n");

		if((_missingSeatsRowList.size() == 1) && (_missingSeatsRowList.get(0).equals(-1)))
			sb.append("\tMissing seats each row: " + 0 + "\n");
		else {
			sb.append("\tMissing seats each row: \n");
			for (int i=0; i<_missingSeatsRowList.size(); i++)
				sb.append("\t\t" + Arrays.toString(_missingSeatsRowList.get(i)) + " ");
		}
		
		sb.append("\n\t.....................................\n")
		.append("\tNumber of breaks economy class: " + _numberOfBreaksEconomyClass + "\n")
		.append("\tNumber of breaks business class: " + _numberOfBreaksBusinessClass + "\n")
		.append("\tNumber of breaks first class: " + _numberOfBreaksFirstClass + "\n")
		.append("\tNumber of rows economy class: " + _numberOfRowsEconomyClass + "\n")
		.append("\tNumber of rows business class: " + _numberOfRowsBusinessClass + "\n")
		.append("\tNumber of rows first class: " + _numberOfRowsFirstClass + "\n")
		.append("\tNumber of columns economy class: " + Arrays.toString(_numberOfColumnsEconomyClass) + "\n")
		.append("\tNumber of columns business class: " + Arrays.toString(_numberOfColumnsBusinessClass) + "\n")
		.append("\tNumber of columns first class: " + Arrays.toString(_numberOfColumnsFirstClass) + "\n")
		.append("\tPitch economy class: " + _pitchEconomyClass + "\n")
		.append("\tPitch business class: " + _pitchBusinessClass + "\n")
		.append("\tPitch first class: " + _pitchFirstClass + "\n")
		.append("\tWidth economy class: " + _widthEconomyClass + "\n")
		.append("\tWidth business class: " + _widthBusinessClass + "\n")
		.append("\tWidth first class: " + _widthFirstClass + "\n")
		.append("\tDistance from wall economy class: " + _distanceFromWallEconomyClass + "\n")
		.append("\tDistance from wall business class: " + _distanceFromWallBusinessClass + "\n")
		.append("\tDistance from wall first class: " + _distanceFromWallFirstClass + "\n")
		.append("\t.....................................\n")
		.append("\tReference mass of furnishings and equipments: " + _massFurnishingsAndEquipmentReference + "\n")
		.append("\t.....................................\n");

		// TODO: ADD ANALYSIS RESULTS AND DERIVED DATA
		
		return sb.toString();
	}

	@Override
	public Integer getNPax() {
		return _nPax;
	}

	@Override
	public void setNPax(Integer _nPax) {
		this._nPax = _nPax;
	}

	@Override
	public Integer getNCrew() {
		return _nCrew;
	}

	@Override
	public void setNCrew(Integer _nCrew) {
		this._nCrew = _nCrew;
	}

	@Override
	public Integer getMaxPax() {
		return _maxPax;
	}
	
	@Override
	public void setMaxPax(Integer _maxPax) {
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
	public Integer getClassesNumber() {
		return _classesNumber;
	}

	@Override
	public void setClassesNumber(Integer _classesNumber) {
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
	public Integer getFlightCrewNumber() {
		return _flightCrewNumber;
	}

	@Override
	public void setFlightCrewNumber(Integer _flightCrewNumber) {
		this._flightCrewNumber = _flightCrewNumber;
	}
	
	@Override
	public Integer getCabinCrewNumber() {
		return _cabinCrewNumber;
	}

	@Override
	public File getCabinConfigurationPath() {
		return _cabinConfigurationPath;
	}
	
	@Override
	public void setCabinConfigurationPath(File _cabinConfigurationPath) {
		this._cabinConfigurationPath = _cabinConfigurationPath;
	}

	public List<ClassTypeEnum> getTypeList() {
		return _typeList;
	}

	public void setTypeList(List<ClassTypeEnum> _typeList) {
		this._typeList = _typeList;
	}

}