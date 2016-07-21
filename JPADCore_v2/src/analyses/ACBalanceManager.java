package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyXLSUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.CenterOfGravity;

/**
 * Manage the calculations for estimating the aircraft balance.
 * 
 * @author Lorenzo Attanasio, Vittorio Trifari
 *
 */
public class ACBalanceManager extends ACCalculatorManager {

	private String _id;
	private static Aircraft _theAircraft;

	//---------------------------------------------------------------------------------
	// INPUT DATA :
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _maximumZeroFuelMass;
	private Amount<Mass> _operatingEmptyMass;	
	private Amount<Mass> _passengersTotalMass;
	private Amount<Mass> _fuselageMass;
	private Amount<Mass> _wingMass;
	private Amount<Mass> _horizontalTailMass;
	private Amount<Mass> _verticalTailMass;
	private Amount<Mass> _canardMass;
	private List<Amount<Mass>> _nacellesMassList;
	private List<Amount<Mass>> _enginesMassList;
	private Amount<Mass> _landingGearsMass;
	
	//---------------------------------------------------------------------------------
	// OUTPUT DATA : 
	private Double _xCGMeanAtOEM;
	private Double _xCGMaxAftAtOEM;
	private Double _xCGMaxForAtOEM;
	private Double _xCGMeanOEMplusMaxPax;
	private CenterOfGravity _cgStructure;
	private CenterOfGravity _cgStructureAndPower;
	private CenterOfGravity _cgMZFM;
	private List<CenterOfGravity> _cgList;
	private CenterOfGravity _cgMTOM;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACBalanceManagerBuilder {

		// required parameters
		private String __id;
		private Aircraft __theAircraft;

		// optional parameters ... defaults
		// ...
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __maximumZeroFuelMass;
		private Amount<Mass> __operatingEmptyMass;	
		private Amount<Mass> __passengersTotalMass;
		private Amount<Mass> __fuselageMass;
		private Amount<Mass> __wingMass;
		private Amount<Mass> __horizontalTailMass;
		private Amount<Mass> __verticalTailMass;
		private Amount<Mass> __canardMass;
		private List<Amount<Mass>> __nacellesMassList = new ArrayList<Amount<Mass>>();
		private List<Amount<Mass>> __enginesMassList = new ArrayList<Amount<Mass>>();
		private Amount<Mass> __landingGearsMass;
	
		private List<CenterOfGravity> __cgList = new ArrayList<CenterOfGravity>();
		
		public ACBalanceManagerBuilder id (String id) {
			this.__id = id;
			return this;
		}

		public ACBalanceManagerBuilder aircraft (Aircraft theAircraft) {
			this.__theAircraft = theAircraft;
			return this;
		}
		
		public ACBalanceManagerBuilder maximumTakeOffMass (Amount<Mass> maximumTakeOffMass) {
			this.__maximumTakeOffMass = maximumTakeOffMass;
			return this;
		}
		
		public ACBalanceManagerBuilder maximumZeroFuelMass (Amount<Mass> maximumZeroFuelMass) {
			this.__maximumZeroFuelMass = maximumZeroFuelMass;
			return this;
		}
		
		public ACBalanceManagerBuilder operatingEmptyMass (Amount<Mass> operatingEmptyMass) {
			this.__operatingEmptyMass = operatingEmptyMass;
			return this;
		}
		
		public ACBalanceManagerBuilder passengersTotalMass (Amount<Mass> passengersTotalMass) {
			this.__passengersTotalMass = passengersTotalMass;
			return this;
		}
		
		public ACBalanceManagerBuilder fuselageMass (Amount<Mass> fuselageMass) {
			this.__fuselageMass = fuselageMass;
			return this;
		}
		
		public ACBalanceManagerBuilder wingMass (Amount<Mass> wingMass) {
			this.__wingMass = wingMass;
			return this;
		}
		
		public ACBalanceManagerBuilder horizontalTailMass (Amount<Mass> horizontalTailMass) {
			this.__horizontalTailMass = horizontalTailMass;
			return this;
		}
		
		public ACBalanceManagerBuilder verticalTailMass (Amount<Mass> verticalTailMass) {
			this.__verticalTailMass = verticalTailMass;
			return this;
		}
		
		public ACBalanceManagerBuilder canardMass (Amount<Mass> canardMass) {
			this.__canardMass = canardMass;
			return this;
		}
		
		public ACBalanceManagerBuilder nacellesMass (List<Amount<Mass>> nacellesMassList) {
			this.__nacellesMassList = nacellesMassList;
			return this;
		}
		
		public ACBalanceManagerBuilder enginesMass (List<Amount<Mass>> enginesMassList) {
			this.__enginesMassList = enginesMassList;
			return this;
		}
		
		public ACBalanceManagerBuilder landingGearsMass (Amount<Mass> landingGearsMass) {
			this.__landingGearsMass = landingGearsMass;
			return this;
		}
		
		public ACBalanceManager build() {
			return new ACBalanceManager(this);
		}
	}
	
	private ACBalanceManager(ACBalanceManagerBuilder builder) {
		
		this._id = builder.__id;
		_theAircraft = builder.__theAircraft;
		
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._maximumZeroFuelMass = builder.__maximumZeroFuelMass;
		this._operatingEmptyMass = builder.__operatingEmptyMass;
		this._passengersTotalMass = builder.__passengersTotalMass;
		this._fuselageMass = builder.__fuselageMass;
		this._wingMass = builder.__wingMass;
		this._horizontalTailMass = builder.__horizontalTailMass;
		this._verticalTailMass = builder.__verticalTailMass;
		this._canardMass = builder.__canardMass;
		this._nacellesMassList = builder.__nacellesMassList;
		this._enginesMassList = builder.__enginesMassList;
		this._landingGearsMass = builder.__landingGearsMass;
		
		this._cgList = builder.__cgList;
		
		if(_theAircraft.getFuselage() != null)
			_theAircraft.getFuselage().setMassEstimated(_fuselageMass);
		if(_theAircraft.getWing() != null)
			_theAircraft.getWing().setMassEstimated(_wingMass);
		if(_theAircraft.getHTail() != null)
			_theAircraft.getHTail().setMassEstimated(_horizontalTailMass);
		if(_theAircraft.getVTail() != null)
			_theAircraft.getVTail().setMassEstimated(_verticalTailMass);
		if(_theAircraft.getCanard() != null)
			_theAircraft.getCanard().setMassEstimated(_canardMass);
		if(_theAircraft.getNacelles() != null) {
			
			// TODO : CHECK THIS OUT !!
			
			_theAircraft.getNacelles().initializeWeights(_theAircraft);
			for(int i=0; i<_theAircraft.getNacelles().getNacellesNumber(); i++) 
				_theAircraft.getNacelles().getNacellesList().get(i).getWeights().setMassEstimated(_nacellesMassList.get(i));
		}
		if(_theAircraft.getPowerPlant() != null) {
			for(int i=0; i<_theAircraft.getPowerPlant().getEngineNumber(); i++) 
				_theAircraft.getPowerPlant().getEngineList().get(i).setTotalMass(_enginesMassList.get(i));
		}
		if(_theAircraft.getLandingGears() != null)
			_theAircraft.getLandingGears().setMassEstimated(_landingGearsMass);
		
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================

	/**
	 * ImportFromXML is the only way to create a ACBalanceManger object. In this way the possibility
	 * to create an object with null input data is avoided.
	 * 
	 * @throws IOException 
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public static ACBalanceManager importFromXML (String pathToXML, Aircraft theAircraft) throws IOException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading balance analysis data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		Boolean readFromXLSFlag;
		String readFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@from_xls_file");
		if(readFromXLSString.equalsIgnoreCase("true"))
			readFromXLSFlag = Boolean.TRUE;
		else
			readFromXLSFlag = Boolean.FALSE;

		String fileWeightsXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file");

		Amount<Mass> maximumTakeOffMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> maximumZeroFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> operatingEmptyMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> passengersTotalMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> fuselageMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> wingMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> horizontalTailMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> verticalTailMass = Amount.valueOf(0.0, SI.KILOGRAM);
		Amount<Mass> canardMass = Amount.valueOf(0.0, SI.KILOGRAM);
		List<Amount<Mass>> nacellesMassList = new ArrayList<Amount<Mass>>();
		List<Amount<Mass>> enginesMassList = new ArrayList<Amount<Mass>>();
		Amount<Mass> landingGearsMass = Amount.valueOf(0.0, SI.KILOGRAM);

		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		if(readFromXLSFlag == Boolean.TRUE) {

			System.out.println("OUTPUT --> " + MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR));
			
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
				if (weightsFile.getName().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (weightsFile.getName().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}

				//---------------------------------------------------------------
				// MAXIMUM TAKE-OFF MASS
				Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "GLOBAL RESULTS");
				if(sheetGlobalData != null) {
					Cell maximumTakeOffMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Take-Off Mass").get(0)).getCell(3);
					if(maximumTakeOffMassCell != null)
						maximumTakeOffMass = Amount.valueOf(maximumTakeOffMassCell.getNumericCellValue(), SI.KILOGRAM);
				}

				//---------------------------------------------------------------
				// MAXIMUM ZERO FUEL MASS
				Cell maximumZeroFuelMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Zero Fuel Mass").get(0)).getCell(3);
				if(maximumZeroFuelMassCell != null)
					maximumZeroFuelMass = Amount.valueOf(maximumZeroFuelMassCell.getNumericCellValue(), SI.KILOGRAM);

				//---------------------------------------------------------------
				// OPERATING EMPTY MASS
				Cell operatingEmptyMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Operating Empty Mass").get(0)).getCell(3);
				if(operatingEmptyMassCell != null)
					operatingEmptyMass = Amount.valueOf(operatingEmptyMassCell.getNumericCellValue(), SI.KILOGRAM);

				//---------------------------------------------------------------
				// PASSENGERS TOTAL MASS
				Cell passengersTotalMassCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Maximum Passengers Mass").get(0)).getCell(3);
				if(passengersTotalMassCell != null)
					passengersTotalMass = Amount.valueOf(passengersTotalMassCell.getNumericCellValue(), SI.KILOGRAM);

				//---------------------------------------------------------------
				// FUSELAGE MASS
				Sheet sheetFuselage = MyXLSUtils.findSheet(workbook, "FUSELAGE");
				if(sheetFuselage != null) {
					
					Cell fuselageMassCell = sheetFuselage.getRow(MyXLSUtils.findRowIndex(sheetFuselage, "Estimated Mass ").get(0)).getCell(3);
					if(fuselageMassCell != null)
						fuselageMass = Amount.valueOf(fuselageMassCell.getNumericCellValue(), SI.KILOGRAM);
				}

				//---------------------------------------------------------------
				// WING MASS
				Sheet sheetWing = MyXLSUtils.findSheet(workbook, "WING");
				if(sheetWing != null) {
					Cell wingMassCell = sheetWing.getRow(MyXLSUtils.findRowIndex(sheetWing, "Estimated Mass ").get(0)).getCell(3);
					if(wingMassCell != null)
						wingMass = Amount.valueOf(wingMassCell.getNumericCellValue(), SI.KILOGRAM);
				}

				//---------------------------------------------------------------
				// HORIZONTAL TAIL MASS
				Sheet sheetHTail = MyXLSUtils.findSheet(workbook, "HORIZONTAL TAIL");
				if(sheetHTail != null) {
					Cell hTailMassCell = sheetHTail.getRow(MyXLSUtils.findRowIndex(sheetHTail, "Estimated Mass ").get(0)).getCell(3);
					if(hTailMassCell != null)
						horizontalTailMass = Amount.valueOf(hTailMassCell.getNumericCellValue(), SI.KILOGRAM);
				}

				//---------------------------------------------------------------
				// VERTICAL TAIL MASS
				Sheet sheetVTail = MyXLSUtils.findSheet(workbook, "VERTICAL TAIL");
				if(sheetVTail != null) {
					Cell vTailMassCell = sheetVTail.getRow(MyXLSUtils.findRowIndex(sheetVTail, "Estimated Mass ").get(0)).getCell(3);
					if(vTailMassCell != null)
						verticalTailMass = Amount.valueOf(vTailMassCell.getNumericCellValue(), SI.KILOGRAM);
				}

				//---------------------------------------------------------------
				// CANARD MASS
				Sheet sheetCanard = MyXLSUtils.findSheet(workbook, "CANARD");
				if(sheetCanard != null) {
					Cell canardMassCell = sheetCanard.getRow(MyXLSUtils.findRowIndex(sheetCanard, "Estimated Mass ").get(0)).getCell(3);
					if(canardMassCell != null)
						canardMass = Amount.valueOf(canardMassCell.getNumericCellValue(), SI.KILOGRAM);
				}

				//---------------------------------------------------------------
				// NACELLES MASS
				Sheet sheetNacelles = MyXLSUtils.findSheet(workbook, "NACELLES");
				if(sheetNacelles != null) {
					for(int i=0; i<theAircraft.getNacelles().getNacellesNumber(); i++) {
						Cell nacellesMassCell = sheetNacelles.getRow(MyXLSUtils.findRowIndex(sheetNacelles, "Estimated Mass ").get(i)).getCell(3);
						if(nacellesMassCell != null)
							nacellesMassList.add(Amount.valueOf(nacellesMassCell.getNumericCellValue(), SI.KILOGRAM));
					}
				}

				//---------------------------------------------------------------
				// ENGINES MASS
				Sheet sheetEngines = MyXLSUtils.findSheet(workbook, "ENGINES");
				if(sheetEngines != null) {
					for(int i=0; i<theAircraft.getPowerPlant().getEngineNumber(); i++) {
						Cell enginesMassCell = sheetEngines.getRow(MyXLSUtils.findRowIndex(sheetEngines, "Total Mass ").get(i)).getCell(3);
						if(enginesMassCell != null)
							enginesMassList.add(Amount.valueOf(enginesMassCell.getNumericCellValue(), SI.KILOGRAM));
					}
				}

				//---------------------------------------------------------------
				// LANDING GEARS MASS
				Sheet sheetLandingGears = MyXLSUtils.findSheet(workbook, "LANDING GEARS");
				if(sheetLandingGears != null) {
					Cell landingGearsMassCell = sheetLandingGears.getRow(MyXLSUtils.findRowIndex(sheetLandingGears, "Estimated Mass ").get(0)).getCell(3);
					if(landingGearsMassCell != null)
						landingGearsMass = Amount.valueOf(landingGearsMassCell.getNumericCellValue(), SI.KILOGRAM);
				}

			}
			else {
				System.err.println("FILE '" + weightsFile.getAbsolutePath() + "' NOT FOUND!! \n\treturning...");
				return null;
			}
		}
		else {
		
			//---------------------------------------------------------------
			// MAXIMUM TAKE-OFF MASS
			String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//balance/maximum_take_off_mass");
			if(maximumTakeOffMassProperty != null)
				maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/maximum_take_off_mass");
			else {
				System.err.println("MAXIMUM TAKE-OFF MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// MAXIMUM ZERO FUEL MASS
			String maximumZeroFuelMassProperty = reader.getXMLPropertyByPath("//balance/maximum_zero_fuel_mass");
			if(maximumZeroFuelMassProperty != null)
				maximumZeroFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/maximum_zero_fuel_mass");
			else {
				System.err.println("MAXIMUM ZERO FUEL MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//balance/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/operating_empty_mass");
			else {
				System.err.println("OPERATING EMPTY MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// PASSENGERS TOTAL MASS
			String passengersTotalMassProperty = reader.getXMLPropertyByPath("//balance/passengers_total_mass");
			if(passengersTotalMassProperty != null)
				passengersTotalMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/passengers_total_mass");
			else {
				System.err.println("PASSENGERS TOTAL MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// FUSELAGE MASS
			String fuselageMassProperty = reader.getXMLPropertyByPath("//balance/fuselage_mass");
			if(fuselageMassProperty != null)
				fuselageMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/fuselage_mass");
			else {
				System.err.println("FUSELAGE MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// WING MASS
			String wingMassProperty = reader.getXMLPropertyByPath("//balance/wing_mass");
			if(wingMassProperty != null)
				wingMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/wing_mass");
			else {
				System.err.println("WING MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// HORIZONTAL TAIL MASS
			String horizontalTailMassProperty = reader.getXMLPropertyByPath("//balance/horizontal_tail_mass");
			if(horizontalTailMassProperty != null)
				horizontalTailMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/horizontal_tail_mass");
			else {
				System.err.println("HORIZONTAL TAIL MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// VERTICAL TAIL MASS
			String verticalTailMassProperty = reader.getXMLPropertyByPath("//balance/vertical_tail_mass");
			if(verticalTailMassProperty != null)
				verticalTailMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/vertical_tail_mass");
			else {
				System.err.println("VERTICAL TAIL MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// CANARD MASS
			String canardMassProperty = reader.getXMLPropertyByPath("//balance/canard_mass");
			if(canardMassProperty != null)
				canardMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/canard_mass");
			else {
				System.err.println("CANARD MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// NACELLES MASS
			List<String> nacellesMassProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//balance/nacelles_mass").get(0));
			if(nacellesMassProperty != null)
				for(int i=0; i<nacellesMassProperty.size(); i++)
					nacellesMassList.add(Amount.valueOf(Double.valueOf(nacellesMassProperty.get(i)), SI.KILOGRAM));
			else {
				System.err.println("NACELLES MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// ENGINES MASS
			List<String> enignesMassProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//balance/engines_mass").get(0));
			if(enignesMassProperty != null)
				for(int i=0; i<enignesMassProperty.size(); i++)
					enginesMassList.add(Amount.valueOf(Double.valueOf(enignesMassProperty.get(i)), SI.KILOGRAM));
			else {
				System.err.println("ENGINES MASS REQUIRED !! \n ... returning ");
				return null; 
			}
			
			//---------------------------------------------------------------
			// LANDING GEARS MASS
			String landingGearsMassProperty = reader.getXMLPropertyByPath("//balance/landing_gears_mass");
			if(landingGearsMassProperty != null)
				landingGearsMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//balance/landing_gears_mass");
			else {
				System.err.println("LANDING GEARS MASS REQUIRED !! \n ... returning ");
				return null; 
			}
		}
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the ACBalanceManager object can be created
		 * using the builder pattern.
		 */
		ACBalanceManager theBalance = new ACBalanceManagerBuilder()
				.id(id)
				.aircraft(theAircraft)
				.maximumTakeOffMass(maximumTakeOffMass)
				.maximumZeroFuelMass(maximumZeroFuelMass)
				.operatingEmptyMass(operatingEmptyMass)
				.passengersTotalMass(passengersTotalMass)
				.fuselageMass(fuselageMass)
				.wingMass(wingMass)
				.horizontalTailMass(horizontalTailMass)
				.verticalTailMass(verticalTailMass)
				.canardMass(canardMass)
				.nacellesMass(nacellesMassList)
				.enginesMass(enginesMassList)
				.landingGearsMass(landingGearsMass)
				.build();
		
		return theBalance;
		}
		
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tWeights Analysis\n")
				.append("\t-------------------------------------\n")
				.append("\tXcg structure MAC: " + getCGStructure().getXMAC() + "\n")
				.append("\tXcg structure BRF: " + getCGStructure().getXBRF() + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg structure and engines MAC: " + getCGStructureAndPower().getXMAC() + "\n")
				.append("\tXcg structure and engines BRF: " + getCGStructureAndPower().getXBRF() + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg maximum zero fuel mass MAC: " + getCGMZFM().getXMAC() + "\n")
				.append("\tXcg maximum zero fuel mass BRF: " + getCGMZFM().getXBRF() + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg maximum take-off mass MAC: " + getCGMTOM().getXMAC() + "\n")
				.append("\tXcg maximum take-off mass BRF: " + getCGMTOM().getXBRF() + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg operating empty mass (max aft): " + getXCGMaxAftAtOEM() + "\n")
				.append("\tXcg operating empty mass (mean): " + getXCGMeanAtOEM() + "\n")
				.append("\tXcg operating empty mass (max for): " + getXCGMaxForAtOEM() + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg operating empty mass + maximum passengers mass (mean): " + getXCGMeanOEMplusMaxPax() + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				;
		return sb.toString();
	}
	
	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException {
		
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
		
		//--------------------------------------------------------------------------------
		// GLOBAL ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		Sheet sheet = wb.createSheet("GLOBAL RESULTS");
		List<Object[]> dataListGlobal = new ArrayList<>();
		
		dataListGlobal.add(new Object[] {"Description","Unit","Value"});
		dataListGlobal.add(new Object[] {"Xcg structure MAC"," ", _cgStructure.getXMAC()});
		dataListGlobal.add(new Object[] {"Xcg structure MAC","m", _cgStructure.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Xcg structure and engines MAC","", _cgStructureAndPower.getXMAC()});
		dataListGlobal.add(new Object[] {"Xcg structure and engines BRF","m", _cgStructureAndPower.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Xcg maximum zero fuel mass MAC","",_cgMZFM.getXMAC()});
		dataListGlobal.add(new Object[] {"Xcg maximum zero fuel mass BRF","m",_cgMZFM.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Xcg maximum take-off mass MAC","",_cgMTOM.getXMAC()});
		dataListGlobal.add(new Object[] {"Xcg maximum take-off mass MAC","m",_cgMTOM.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Xcg operating empty mass (max aft)","m",_xCGMaxAftAtOEM});
		dataListGlobal.add(new Object[] {"Xcg operating empty mass (mean)","m",_xCGMeanAtOEM});
		dataListGlobal.add(new Object[] {"Xcg operating empty mass (max for)","m",_xCGMaxForAtOEM});
		dataListGlobal.add(new Object[] {"Xcg operating empty mass + maximum passengers mass (mean)","m", _xCGMeanOEMplusMaxPax});
		
		CellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    Font font = wb.createFont();
	    font.setFontHeightInPoints((short) 20);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleHead.setFont(font);
        
        Row row = sheet.createRow(0);
		Object[] objArr = dataListGlobal.get(0);
		int cellnum = 0;
		for (Object obj : objArr) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellStyle(styleHead);
			if (obj instanceof Date) {
				cell.setCellValue((Date) obj);
			} else if (obj instanceof Boolean) {
				cell.setCellValue((Boolean) obj);
			} else if (obj instanceof String) {
				cell.setCellValue((String) obj);
			} else if (obj instanceof Double) {
				cell.setCellValue((Double) obj);
			}
			sheet.setDefaultColumnWidth(25);
		}
	
		int rownum = 1;
		for (int i = 1; i < dataListGlobal.size(); i++) {
			objArr = dataListGlobal.get(i);
			row = sheet.createRow(rownum++);
			cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}
		}

		//--------------------------------------------------------------------------------
		// FUSELAGE BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getFuselage() != null) {
			Sheet sheetFuselage = wb.createSheet("FUSELAGE");
			List<Object[]> dataListFuselage = new ArrayList<>();
			dataListFuselage.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListFuselage.add(new Object[] {"Xcg LRF","m", _theAircraft.getFuselage().getCG().getXLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Ycg LRF","m", _theAircraft.getFuselage().getCG().getYLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Zcg LRF","m", _theAircraft.getFuselage().getCG().getZLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Xcg BRF","m", _theAircraft.getFuselage().getCG().getXBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Ycg BRF","m", _theAircraft.getFuselage().getCG().getYBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Zcg BRF","m", _theAircraft.getFuselage().getCG().getZBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			int indexFuselage=0;
			for(MethodEnum methods : _theAircraft.getFuselage().getXCGMap().keySet()) {
				if(_theAircraft.getFuselage().getXCGMap().get(methods) != null) 
					dataListFuselage.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getFuselage().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getFuselage().getPercentDifference()[indexFuselage]
							}
							);
			}

			Row rowFuselage = sheetFuselage.createRow(0);
			Object[] objArrFuselage = dataListFuselage.get(0);
			int cellnumFuselage = 0;
			for (Object obj : objArrFuselage) {
				Cell cell = rowFuselage.createCell(cellnumFuselage++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetFuselage.setDefaultColumnWidth(25);
			}

			int rownumFuselage = 1;
			for (int j = 1; j < dataListFuselage.size(); j++) {
				objArrFuselage = dataListFuselage.get(j);
				rowFuselage = sheetFuselage.createRow(rownumFuselage++);
				cellnumFuselage = 0;
				for (Object obj : objArrFuselage) {
					Cell cell = rowFuselage.createCell(cellnumFuselage++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		//--------------------------------------------------------------------------------
		// WING BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getWing() != null) {
			Sheet sheetWing = wb.createSheet("WING");
			List<Object[]> dataListWing = new ArrayList<>();
			dataListWing.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListWing.add(new Object[] {"Xcg LRF","m", _theAircraft.getWing().getCG().getXLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Ycg LRF","m", _theAircraft.getWing().getCG().getYLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Zcg LRF","m", _theAircraft.getWing().getCG().getZLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Xcg BRF","m", _theAircraft.getWing().getCG().getXBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Ycg BRF","m", _theAircraft.getWing().getCG().getYBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Zcg BRF","m", _theAircraft.getWing().getCG().getZBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			int indexWing=0;
			for(MethodEnum methods : _theAircraft.getWing().getXCGMap().keySet()) {
				if(_theAircraft.getWing().getXCGMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getWing().getXCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getWing().getPercentDifferenceXCG()[indexWing]
							}
							);
				if(_theAircraft.getWing().getYCGMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getWing().getYCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getWing().getPercentDifferenceYCG()[indexWing]
							}
							);
				indexWing++;
			}

			Row rowWing = sheetWing.createRow(0);
			Object[] objArrWing = dataListWing.get(0);
			int cellnumWing = 0;
			for (Object obj : objArrWing) {
				Cell cell = rowWing.createCell(cellnumWing++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetWing.setDefaultColumnWidth(25);
			}

			int rownumWing = 1;
			for (int j = 1; j < dataListWing.size(); j++) {
				objArrWing = dataListWing.get(j);
				rowWing = sheetWing.createRow(rownumWing++);
				cellnumWing = 0;
				for (Object obj : objArrWing) {
					Cell cell = rowWing.createCell(cellnumWing++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		//--------------------------------------------------------------------------------
		// HORIZONTAL TAIL BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getHTail() != null) {
			Sheet sheetHTail = wb.createSheet("HORIZONTAL TAIL");
			List<Object[]> dataListHTail = new ArrayList<>();
			dataListHTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListHTail.add(new Object[] {"Xcg LRF","m", _theAircraft.getHTail().getCG().getXLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Ycg LRF","m", _theAircraft.getHTail().getCG().getYLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Zcg LRF","m", _theAircraft.getHTail().getCG().getZLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Xcg BRF","m", _theAircraft.getHTail().getCG().getXBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Ycg BRF","m", _theAircraft.getHTail().getCG().getYBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Zcg BRF","m", _theAircraft.getHTail().getCG().getZBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			int indexHTail=0;
			for(MethodEnum methods : _theAircraft.getHTail().getXCGMap().keySet()) {
				if(_theAircraft.getHTail().getXCGMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getHTail().getXCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getHTail().getPercentDifferenceXCG()[indexHTail]
							}
							);
				if(_theAircraft.getHTail().getYCGMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getHTail().getYCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getHTail().getPercentDifferenceYCG()[indexHTail]
							}
							);
				indexHTail++;
			}

			Row rowHTail = sheetHTail.createRow(0);
			Object[] objArrHTail = dataListHTail.get(0);
			int cellnumHTail = 0;
			for (Object obj : objArrHTail) {
				Cell cell = rowHTail.createCell(cellnumHTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetHTail.setDefaultColumnWidth(25);
			}

			int rownumHTail = 1;
			for (int j = 1; j < dataListHTail.size(); j++) {
				objArrHTail = dataListHTail.get(j);
				rowHTail = sheetHTail.createRow(rownumHTail++);
				cellnumHTail = 0;
				for (Object obj : objArrHTail) {
					Cell cell = rowHTail.createCell(cellnumHTail++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// VERTICAL TAIL BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getVTail() != null) {
			Sheet sheetVTail = wb.createSheet("VERTICAL TAIL");
			List<Object[]> dataListVTail = new ArrayList<>();
			dataListVTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListVTail.add(new Object[] {"Xcg LRF","m", _theAircraft.getVTail().getCG().getXLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Ycg LRF","m", _theAircraft.getVTail().getCG().getYLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Zcg LRF","m", _theAircraft.getVTail().getCG().getZLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Xcg BRF","m", _theAircraft.getVTail().getCG().getXBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Ycg BRF","m", _theAircraft.getVTail().getCG().getYBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Zcg BRF","m", _theAircraft.getVTail().getCG().getZBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			int indexVTail=0;
			for(MethodEnum methods : _theAircraft.getVTail().getXCGMap().keySet()) {
				if(_theAircraft.getVTail().getXCGMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getVTail().getXCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getVTail().getPercentDifferenceXCG()[indexVTail]
							}
							);
				if(_theAircraft.getVTail().getYCGMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getVTail().getYCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getVTail().getPercentDifferenceYCG()[indexVTail]
							}
							);
				indexVTail++;
			}

			Row rowVTail = sheetVTail.createRow(0);
			Object[] objArrVTail = dataListVTail.get(0);
			int cellnumVTail = 0;
			for (Object obj : objArrVTail) {
				Cell cell = rowVTail.createCell(cellnumVTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetVTail.setDefaultColumnWidth(25);
			}

			int rownumVTail = 1;
			for (int j = 1; j < dataListVTail.size(); j++) {
				objArrVTail = dataListVTail.get(j);
				rowVTail = sheetVTail.createRow(rownumVTail++);
				cellnumVTail = 0;
				for (Object obj : objArrVTail) {
					Cell cell = rowVTail.createCell(cellnumVTail++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// CANARD BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getCanard() != null) {
			Sheet sheetCanard = wb.createSheet("CANARD");
			List<Object[]> dataListCanard = new ArrayList<>();
			dataListCanard.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListCanard.add(new Object[] {"Xcg LRF","m", _theAircraft.getCanard().getCG().getXLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Ycg LRF","m", _theAircraft.getCanard().getCG().getYLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Zcg LRF","m", _theAircraft.getCanard().getCG().getZLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Xcg BRF","m", _theAircraft.getCanard().getCG().getXBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Ycg BRF","m", _theAircraft.getCanard().getCG().getYBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Zcg BRF","m", _theAircraft.getCanard().getCG().getZBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			int indexCanard=0;
			for(MethodEnum methods : _theAircraft.getCanard().getXCGMap().keySet()) {
				if(_theAircraft.getCanard().getXCGMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getCanard().getXCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getCanard().getPercentDifferenceXCG()[indexCanard]
							}
							);
				if(_theAircraft.getCanard().getYCGMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"m",
									_theAircraft.getCanard().getYCGMap().get(methods).getEstimatedValue(),
									_theAircraft.getCanard().getPercentDifferenceYCG()[indexCanard]
							}
							);
				indexCanard++;
			}

			Row rowCanard = sheetCanard.createRow(0);
			Object[] objArrCanard = dataListCanard.get(0);
			int cellnumCanard = 0;
			for (Object obj : objArrCanard) {
				Cell cell = rowCanard.createCell(cellnumCanard++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetCanard.setDefaultColumnWidth(25);
			}

			int rownumCanard = 1;
			for (int j = 1; j < dataListCanard.size(); j++) {
				objArrCanard = dataListCanard.get(j);
				rowCanard = sheetCanard.createRow(rownumCanard++);
				cellnumCanard = 0;
				for (Object obj : objArrCanard) {
					Cell cell = rowCanard.createCell(cellnumCanard++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// NACELLES BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getNacelles() != null) {
			Sheet sheetNacelles = wb.createSheet("NACELLES");
			List<Object[]> dataListNacelles = new ArrayList<>();
			dataListNacelles.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListNacelles.add(new Object[] {"Total Xcg LRF","m", _theAircraft.getNacelles().getTotalCG().getXLRF()});
			dataListNacelles.add(new Object[] {"Total Xcg BRF","m", _theAircraft.getNacelles().getTotalCG().getXBRF()});
			dataListNacelles.add(new Object[] {"Total Ycg LRF","m", _theAircraft.getNacelles().getTotalCG().getYLRF()});
			dataListNacelles.add(new Object[] {"Total Ycg BRF","m", _theAircraft.getNacelles().getTotalCG().getYBRF()});
			dataListNacelles.add(new Object[] {"Total Zcg LRF","m", _theAircraft.getNacelles().getTotalCG().getZLRF()});
			dataListNacelles.add(new Object[] {"Total Zcg BRF","m", _theAircraft.getNacelles().getTotalCG().getZBRF()});
			dataListNacelles.add(new Object[] {" "});
			dataListNacelles.add(new Object[] {"BALANCE ESTIMATION FOR EACH NACELLE"});
			dataListNacelles.add(new Object[] {" "});
			for(int iNacelle = 0; iNacelle < _theAircraft.getNacelles().getNacellesNumber(); iNacelle++) {
				dataListNacelles.add(new Object[] {"NACELLE " + (iNacelle+1)});
				dataListNacelles.add(new Object[] {"Xcg LRF","m", _theAircraft.getNacelles().getCGList().get(iNacelle).getXLRF()});
				dataListNacelles.add(new Object[] {"Xcg BRF","m", _theAircraft.getNacelles().getCGList().get(iNacelle).getXBRF()});
				dataListNacelles.add(new Object[] {"Ycg LRF","m", _theAircraft.getNacelles().getCGList().get(iNacelle).getYLRF()});
				dataListNacelles.add(new Object[] {"Ycg BRF","m", _theAircraft.getNacelles().getCGList().get(iNacelle).getYBRF()});
				dataListNacelles.add(new Object[] {"Zcg LRF","m", _theAircraft.getNacelles().getCGList().get(iNacelle).getZLRF()});
				dataListNacelles.add(new Object[] {"Zcg BRF","m", _theAircraft.getNacelles().getCGList().get(iNacelle).getZBRF()});
				dataListNacelles.add(new Object[] {" "});	
			}
			
			Row rowNacelles = sheetNacelles.createRow(0);
			Object[] objArrNacelles = dataListNacelles.get(0);
			int cellnumNacelles = 0;
			for (Object obj : objArrNacelles) {
				Cell cell = rowNacelles.createCell(cellnumNacelles++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetNacelles.setDefaultColumnWidth(25);
			}

			int rownumNacelles = 1;
			for (int j = 1; j < dataListNacelles.size(); j++) {
				objArrNacelles = dataListNacelles.get(j);
				rowNacelles = sheetNacelles.createRow(rownumNacelles++);
				cellnumNacelles = 0;
				for (Object obj : objArrNacelles) {
					Cell cell = rowNacelles.createCell(cellnumNacelles++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// POWER PLANT BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getPowerPlant() != null) {
			Sheet sheetPowerPlant = wb.createSheet("POWER PLANT");
			List<Object[]> dataListPowerPlant = new ArrayList<>();
			dataListPowerPlant.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListPowerPlant.add(new Object[] {"Total Xcg LRF","m", _theAircraft.getPowerPlant().getTotalCG().getXLRF()});
			dataListPowerPlant.add(new Object[] {"Total Xcg BRF","m", _theAircraft.getPowerPlant().getTotalCG().getXBRF()});
			dataListPowerPlant.add(new Object[] {"Total Ycg LRF","m", _theAircraft.getPowerPlant().getTotalCG().getYLRF()});
			dataListPowerPlant.add(new Object[] {"Total Ycg BRF","m", _theAircraft.getPowerPlant().getTotalCG().getYBRF()});
			dataListPowerPlant.add(new Object[] {"Total Zcg LRF","m", _theAircraft.getPowerPlant().getTotalCG().getZLRF()});
			dataListPowerPlant.add(new Object[] {" "});
			dataListPowerPlant.add(new Object[] {"BALANCE ESTIMATION FOR EACH ENGINE"});
			dataListPowerPlant.add(new Object[] {" "});
			for(int iEngines = 0; iEngines < _theAircraft.getPowerPlant().getEngineNumber(); iEngines++) {
				dataListPowerPlant.add(new Object[] {"ENGINE " + (iEngines+1)});
				dataListPowerPlant.add(new Object[] {"Xcg LRF","m", _theAircraft.getPowerPlant().getCGList().get(iEngines).getXLRF()});
				dataListPowerPlant.add(new Object[] {"Xcg BRF","m", _theAircraft.getPowerPlant().getCGList().get(iEngines).getXBRF()});
				dataListPowerPlant.add(new Object[] {"Ycg LRF","m", _theAircraft.getPowerPlant().getCGList().get(iEngines).getYLRF()});
				dataListPowerPlant.add(new Object[] {"Ycg BRF","m", _theAircraft.getPowerPlant().getCGList().get(iEngines).getYBRF()});
				dataListPowerPlant.add(new Object[] {"Zcg LRF","m", _theAircraft.getPowerPlant().getCGList().get(iEngines).getZLRF()});
				dataListPowerPlant.add(new Object[] {"Zcg BRF","m", _theAircraft.getPowerPlant().getCGList().get(iEngines).getZBRF()});
				dataListPowerPlant.add(new Object[] {" "});	
			}
			
			Row rowEngines = sheetPowerPlant.createRow(0);
			Object[] objArrEngines = dataListPowerPlant.get(0);
			int cellnumEngines = 0;
			for (Object obj : objArrEngines) {
				Cell cell = rowEngines.createCell(cellnumEngines++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetPowerPlant.setDefaultColumnWidth(25);
			}

			int rownumEngines = 1;
			for (int j = 1; j < dataListPowerPlant.size(); j++) {
				objArrEngines = dataListPowerPlant.get(j);
				rowEngines = sheetPowerPlant.createRow(rownumEngines++);
				cellnumEngines = 0;
				for (Object obj : objArrEngines) {
					Cell cell = rowEngines.createCell(cellnumEngines++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// LANDING GEARS BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getLandingGears() != null) {
			Sheet sheetLandingGears = wb.createSheet("LANDING GEARS");
			List<Object[]> dataListLandingGears = new ArrayList<>();
			dataListLandingGears.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListLandingGears.add(new Object[] {"Xcg LRF","m", _theAircraft.getLandingGears().getCG().getXLRF()});
			dataListLandingGears.add(new Object[] {"Xcg BRF","m", _theAircraft.getLandingGears().getCG().getXBRF()});
			dataListLandingGears.add(new Object[] {"Ycg LRF","m", _theAircraft.getLandingGears().getCG().getYLRF()});
			dataListLandingGears.add(new Object[] {"Ycg BRF","m", _theAircraft.getLandingGears().getCG().getYBRF()});
			dataListLandingGears.add(new Object[] {"Zcg LRF","m", _theAircraft.getLandingGears().getCG().getZLRF()});
			dataListLandingGears.add(new Object[] {"Zcg BRF","m", _theAircraft.getLandingGears().getCG().getZBRF()});

			Row rowLandingGears = sheetLandingGears.createRow(0);
			Object[] objArrLandingGears = dataListLandingGears.get(0);
			int cellnumLandingGears = 0;
			for (Object obj : objArrLandingGears) {
				Cell cell = rowLandingGears.createCell(cellnumLandingGears++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetLandingGears.setDefaultColumnWidth(25);
			}

			int rownumLandingGears = 1;
			for (int j = 1; j < dataListLandingGears.size(); j++) {
				objArrLandingGears = dataListLandingGears.get(j);
				rowLandingGears = sheetLandingGears.createRow(rownumLandingGears++);
				cellnumLandingGears = 0;
				for (Object obj : objArrLandingGears) {
					Cell cell = rowLandingGears.createCell(cellnumLandingGears++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
	}
	
	public void createBalanceCharts(String balanceOutputFolderPath) {

		new MyChartToFileUtils().createMultiTraceTikz(
				MyArrayUtils.convertListOfAmountTodoubleArray(_theAircraft.getCabinConfiguration().getSeatsCoGFrontToRear()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theAircraft.getCabinConfiguration().getCurrentMassList()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theAircraft.getCabinConfiguration().getSeatsCoGRearToFront()),
				MyArrayUtils.convertListOfAmountTodoubleArray(_theAircraft.getCabinConfiguration().getCurrentMassList()),
				null, null,
				balanceOutputFolderPath,
				"loadingCycle",
				"$X_{cg}$", "Mass",
				"m","kg");
	}
	
	/** 
	 * A first guess value of center of gravity location
	 * of the whole aircraft.
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 */
	public void calculateBalance() {

		_xCGMeanAtOEM = (_theAircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
		setXCGMaxAftAtOEM((_xCGMeanAtOEM*(1-0.1)));	
	}

	/** 
	 * Evaluate center of gravity location
	 * of each component.
	 * 
	 * @param aircraft
	 * @param conditions
	 * @param methodsMap
	 */
	public void calculateBalance(Map<ComponentEnum, List<MethodEnum>> methodsMap){

		if(_theAircraft.getFuselage() != null)
			_theAircraft.getFuselage().calculateCG(_theAircraft);

		if(_theAircraft.getWing() != null)
			_theAircraft.getWing().calculateCGAllMethods(methodsMap, ComponentEnum.WING);
		if(_theAircraft.getHTail() != null)
			_theAircraft.getHTail().calculateCGAllMethods(methodsMap, ComponentEnum.HORIZONTAL_TAIL);
		if(_theAircraft.getVTail() != null)
			_theAircraft.getVTail().calculateCGAllMethods(methodsMap, ComponentEnum.VERTICAL_TAIL);
		if(_theAircraft.getCanard() != null)
			_theAircraft.getCanard().calculateCGAllMethods(methodsMap, ComponentEnum.CANARD);
		
		if(_theAircraft.getNacelles() != null)
			_theAircraft.getNacelles().calculateCG();

		if(_theAircraft.getFuelTank() != null)
			_theAircraft.getFuelTank().calculateCG();

		if(_theAircraft.getLandingGears() != null)
			_theAircraft.getLandingGears().calculateCG(_theAircraft);


		// --- END OF STRUCTURE MASS-----------------------------------
		if(_theAircraft.getPowerPlant() != null)
			_theAircraft.getPowerPlant().calculateCG();

		calculateTotalCG();

		setXCGMeanAtOEM(_theAircraft.getWing().getXApexConstructionAxes().getEstimatedValue() + 
				0.25*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue());
		setXCGMaxAftAtOEM(getXCGMeanAtOEM()*(1-0.1));
		setXCGMaxForAtOEM(getXCGMeanAtOEM()*(1+0.1));
	}

	/**
	 * Evaluate overall CG
	 * 
	 * @param aircraft
	 */
	public void calculateTotalCG() {

		// Structural CG
		_cgStructure = new CenterOfGravity();

		_cgList.add(_theAircraft.getFuselage().getCG());
		_cgList.add(_theAircraft.getWing().getCG());
		_cgList.add(_theAircraft.getHTail().getCG());
		_cgList.add(_theAircraft.getVTail().getCG());
		_cgList.add(_theAircraft.getLandingGears().getCG());
		_cgList.addAll(_theAircraft.getNacelles().getCGList());
		
		System.out.println("\n \nCG COMPONENTS LOCATION IN BRF");
		System.out.println("fuselage --> " + _cgList.get(0).getXBRF());
		System.out.println("wing --> " + _cgList.get(1).getXBRF());
		System.out.println("HTail --> " + _cgList.get(2).getXBRF());
		System.out.println("VTail --> " + _cgList.get(3).getXBRF());
		System.out.println("Landing gear --> " + _cgList.get(4).getXBRF());
		for(int i=0 ;  i<_theAircraft.getNacelles().getCGList().size() ; i++){
		System.out.println("Nacelle  "+  i + " --> " + _cgList.get(i+5).getXBRF());
		}

		List<Amount<Mass>> massStructureList = new ArrayList<Amount<Mass>>();
		if(_fuselageMass != null)
			massStructureList.add(_fuselageMass);
		if(_wingMass != null)
			massStructureList.add(_wingMass);
		if(_horizontalTailMass != null)
			massStructureList.add(_horizontalTailMass);
		if(_verticalTailMass != null)
			massStructureList.add(_verticalTailMass);
		if(_canardMass != null)
			massStructureList.add(_canardMass);
		if(_nacellesMassList != null) 
			massStructureList.addAll(_nacellesMassList);
		if(_landingGearsMass != null)
			massStructureList.add(_landingGearsMass);
		
		double prod = 0., sum = 0.;
		for (int i=0; i < _cgList.size(); i++) {

			prod += _cgList.get(i).getXBRF().getEstimatedValue()*massStructureList.get(i).getEstimatedValue();
			sum += massStructureList.get(i).getEstimatedValue();

		}

		_cgStructure.setXBRF(
				Amount.valueOf(prod/sum, SI.METER));

		_cgStructure.calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// Structure + engines CG
		_cgStructureAndPower = new CenterOfGravity();

		System.out.println("fuel tank --> " + _theAircraft.getFuelTank().getXCG().getEstimatedValue());
		double cgPowerPlantContribute =0.0;
		Amount<Mass> powerPlantMass = Amount.valueOf(0.0, SI.KILOGRAM);
		
		for(int i=0 ; i< _theAircraft.getPowerPlant().getEngineNumber(); i++){
			powerPlantMass = powerPlantMass.plus(_enginesMassList.get(i));
			cgPowerPlantContribute = cgPowerPlantContribute + (_theAircraft.getPowerPlant().getCGList().get(i).getXBRF().getEstimatedValue()*
					_enginesMassList.get(i).getEstimatedValue());
			System.out.println("Engine " + i + " --> " + _theAircraft.getPowerPlant().getCGList().get(i).getXBRF());
		}
		_cgStructureAndPower.setXBRF(
				Amount.valueOf(
						       (cgPowerPlantContribute+
						    		   sum*getCGStructure().getXBRF().getEstimatedValue())/
								(sum + powerPlantMass.getEstimatedValue())
										, SI.METER));

		getCGStructureAndPower().calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MZFW CG location
		_cgMZFM = new CenterOfGravity();

		getCGMZFM().setXBRF(Amount.valueOf(
				(getCGStructureAndPower().getXBRF().getEstimatedValue()*_operatingEmptyMass.getEstimatedValue() + 
						_theAircraft.getCabinConfiguration().getSeatsCoG().getEstimatedValue()*
						_passengersTotalMass.getEstimatedValue()) /
						(_passengersTotalMass.getEstimatedValue() 
								+ _operatingEmptyMass.getEstimatedValue())
						, SI.METER));

		getCGMZFM().calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

		// MTOM CG location
		_cgMTOM = new CenterOfGravity();

		_cgMTOM.setXBRF(Amount.valueOf(
				(_cgMZFM.getXBRF().getEstimatedValue() 
						* _maximumZeroFuelMass.getEstimatedValue()
						+ _theAircraft.getFuelTank().getFuelMass().getEstimatedValue()
						* _theAircraft.getFuelTank().getXCG().getEstimatedValue())
						/ this._maximumTakeOffMass.getEstimatedValue(),
						SI.METER));

		_cgMTOM.calculateCGinMAC(
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX(), 
				Amount.valueOf(0., SI.METER), 
				_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord());

	}

	public Double getXCGMeanAtOEM() {
		return _xCGMeanAtOEM;
	}

	public void setXCGMeanAtOEM(Double _xCGMeanAtOEM) {
		this._xCGMeanAtOEM = _xCGMeanAtOEM;
	}

	public Double getXCGMaxAftAtOEM() {
		return _xCGMaxAftAtOEM;
	}

	public void setXCGMaxAftAtOEM(Double _xCGMaxAftAtOEM) {
		this._xCGMaxAftAtOEM = _xCGMaxAftAtOEM;
	}

	public Double getXCGMaxForAtOEM() {
		return _xCGMaxForAtOEM;
	}

	public void setXCGMaxForAtOEM(Double _xCGMaxForAtOEM) {
		this._xCGMaxForAtOEM = _xCGMaxForAtOEM;
	}

	public Double getXCGMeanOEMplusMaxPax() {
		return _xCGMeanOEMplusMaxPax;
	}

	public void setXCGMeanOEMplusMaxPax(Double _xCGMeanOEMplusMaxPax) {
		this._xCGMeanOEMplusMaxPax = _xCGMeanOEMplusMaxPax;
	}

	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}

	public void setCGList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	public CenterOfGravity getCGStructure() {
		return _cgStructure;
	}

	public void setCGStructure(CenterOfGravity _cgStructure) {
		this._cgStructure = _cgStructure;
	}

	public CenterOfGravity getCGStructureAndPower() {
		return _cgStructureAndPower;
	}

	public void setCGStructureAndPower(CenterOfGravity _cgStructureAndPower) {
		this._cgStructureAndPower = _cgStructureAndPower;
	}

	public CenterOfGravity getCGMZFM() {
		return _cgMZFM;
	}

	public void setCGMZFM(CenterOfGravity _cgMZFM) {
		this._cgMZFM = _cgMZFM;
	}

	public CenterOfGravity getCGMTOM() {
		return _cgMTOM;
	}

	public void setCGMTOM(CenterOfGravity _cgMTOM) {
		this._cgMTOM = _cgMTOM;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}
	
	public static Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public static void setTheAircraft(Aircraft _theAircraft) {
		ACBalanceManager._theAircraft = _theAircraft;
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) {
		this._maximumTakeOffMass = _maximumTakeOffMass;
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

	public Amount<Mass> getPassengersTotalMass() {
		return _passengersTotalMass;
	}

	public void setPassengersTotalMass(Amount<Mass> _passengerTotalMass) {
		this._passengersTotalMass = _passengerTotalMass;
	}

	public Amount<Mass> getFuselageMass() {
		return _fuselageMass;
	}

	public void setFuselageMass(Amount<Mass> _fuselageMass) {
		this._fuselageMass = _fuselageMass;
	}

	public Amount<Mass> getWingMass() {
		return _wingMass;
	}

	public void setWingMass(Amount<Mass> _wingMass) {
		this._wingMass = _wingMass;
	}

	public Amount<Mass> getHorizontalTailMass() {
		return _horizontalTailMass;
	}

	public void setHorizontalTailMass(Amount<Mass> _horizontalTailMass) {
		this._horizontalTailMass = _horizontalTailMass;
	}

	public Amount<Mass> getVerticalTailMass() {
		return _verticalTailMass;
	}

	public void setVerticalTailMass(Amount<Mass> _verticalTailMass) {
		this._verticalTailMass = _verticalTailMass;
	}

	public Amount<Mass> getCanardMass() {
		return _canardMass;
	}

	public void setCanardMass(Amount<Mass> _canardMass) {
		this._canardMass = _canardMass;
	}

	public List<Amount<Mass>> getNacellesMassList() {
		return _nacellesMassList;
	}

	public void setNacellesMassList(List<Amount<Mass>> _nacellesMassList) {
		this._nacellesMassList = _nacellesMassList;
	}

	public List<Amount<Mass>> getEnginesMassList() {
		return _enginesMassList;
	}

	public void setEnginesMassList(List<Amount<Mass>> _enginesMassList) {
		this._enginesMassList = _enginesMassList;
	}

	public Amount<Mass> getLandingGearsMass() {
		return _landingGearsMass;
	}

	public void setLandingGearsMass(Amount<Mass> _landingGearsMass) {
		this._landingGearsMass = _landingGearsMass;
	}
}