package analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
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
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

/**
 * Manage components weight calculations
 * 
 * @author Lorenzo Attanasio, Vittorio Trifari
 */
public class ACWeightsManager extends ACCalculatorManager implements IACWeightsManager {

	private String _id;
	private static Aircraft _theAircraft;
	
	// Aluminum density
	public static Amount<VolumetricDensity> _materialDensity = 
			Amount.valueOf(2711.0,VolumetricDensity.UNIT);

	// 84 kg assumed for each passenger + 15 kg baggage (EASA 2008.C.06) 
	public static Amount<Mass> _paxSingleMass = Amount.valueOf(99.0, SI.KILOGRAM);
	
	//---------------------------------------------------------------------------------
	// INPUT DATA : 
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _maximumZeroFuelMass;
	private Amount<Mass> _maximumLandingMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _trappedFuelOilMass;
	private Amount<Length> _referenceRange;
	
	//---------------------------------------------------------------------------------
	// OUTPUT DATA : 
	private Amount<Mass> _paxMass;
	private Amount<Mass> _paxMassMax;
	private Amount<Mass> _crewMass;
	private Amount<Mass> _operatingItemMass;
	private Amount<Force> _operatingItemWeight;
	private Amount<Mass> _emptyMass;
	private Amount<Force> _emptyWeight;
	private Amount<Force> _maximumTakeOffWeight;
	private Amount<Force> _maximumZeroFuelWeight;
	private Amount<Force> _operatingEmptyWeight;
	private Amount<Force> _trappedFuelOilWeight;
	private Amount<Force> _maximumLandingWeight;
	private Amount<Force> _manufacturerEmptyWeight;
	private Amount<Mass> _structuralMass;
	private Amount<Mass> _manufacturerEmptyMass;
	private Amount<Mass> _zeroFuelMass;
	private Amount<Mass> _takeOffMass;
	private Amount<Force> _zeroFuelWeight;
	private Amount<Force> _takeOffWeight;

	private List<Amount<Mass>> _maximumTakeOffMassList;
	private List<Amount<Mass>> _massStructureList;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACWeightsManagerBuilder {
		
		// required parameters
		private String __id;
		private Aircraft __theAircraft;
		
		// optional parameters ... defaults
		// ...
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __maximumZeroFuelMass;
		private Amount<Mass> __maximumLandingMass;
		private Amount<Mass> __operatingEmptyMass;
		private Amount<Mass> __trappedFuelOilMass;
		private Amount<Length> __referenceRange;

		private List<Amount<Mass>> __maximumTakeOffMassList = new ArrayList<Amount<Mass>>();
		private List<Amount<Mass>> __massStructureList = new ArrayList<Amount<Mass>>();
		
		public ACWeightsManagerBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public ACWeightsManagerBuilder aircraft(Aircraft theAircraft) {
			this.__theAircraft = theAircraft;
			return this;
		}
		
		public ACWeightsManagerBuilder maximumTakeOffMass(Amount<Mass> maximumTakeOffMass) {
			this.__maximumTakeOffMass = maximumTakeOffMass;
			return this;
		}
		
		public ACWeightsManagerBuilder maximumZeroFuelMass(Amount<Mass> maximumZeroFuelMass) {
			this.__maximumZeroFuelMass = maximumZeroFuelMass;
			return this;
		}
		
		public ACWeightsManagerBuilder maximumLandingMass(Amount<Mass> maximumLandingMass) {
			this.__maximumLandingMass = maximumLandingMass;
			return this;
		}
		
		public ACWeightsManagerBuilder operatingEmptyMass(Amount<Mass> operatingEmptyMass) {
			this.__operatingEmptyMass = operatingEmptyMass;
			return this;
		}
		
		public ACWeightsManagerBuilder trappedFuelOilMass(Amount<Mass> trappedFuelOilMass) {
			this.__trappedFuelOilMass = trappedFuelOilMass;
			return this;
		}
		
		public ACWeightsManagerBuilder referenceRange(Amount<Length> referenceRange) {
			this.__referenceRange = referenceRange;
			return this;
		}
		
		public ACWeightsManagerBuilder (String id, Aircraft theAircraft) {
			this.__id = id;
			this.__theAircraft = theAircraft;
			this.initializeDefaultData(AircraftEnum.ATR72);
		}
		
		public ACWeightsManagerBuilder (String id, Aircraft theAircraft, AircraftEnum aircraftName) {
			this.__id = id;
			this.__theAircraft = theAircraft;
			this.initializeDefaultData(aircraftName);
		}
		
		private void initializeDefaultData(AircraftEnum aircraftName) {

			switch(aircraftName) {
			case ATR72:
				__maximumTakeOffMass = Amount.valueOf(23063.5789, SI.KILOGRAM); // ATR72 MTOM, REPORT_ATR72
				__maximumZeroFuelMass = Amount.valueOf(20063.5789, SI.KILOGRAM); // ATR72 MZFM, REPORT_ATR72
				__maximumLandingMass = Amount.valueOf(20757.2210, SI.KILOGRAM);
				__operatingEmptyMass = Amount.valueOf(12935.5789, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0.0, SI.KILOGRAM);
				__referenceRange = Amount.valueOf(1528.0, SI.KILOMETER);
				break;
				
			case B747_100B:
				__maximumTakeOffMass = Amount.valueOf(354991.5060, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__maximumZeroFuelMass = Amount.valueOf(207581.9860, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__maximumLandingMass = Amount.valueOf(319517.5554, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__operatingEmptyMass = Amount.valueOf(153131.9860, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0.005*(__maximumTakeOffMass.getEstimatedValue()), SI.KILOGRAM);
				__referenceRange = Amount.valueOf(9800., SI.KILOMETER);
				break;
				
			case AGILE_DC1:
				__maximumTakeOffMass = Amount.valueOf(36336, SI.KILOGRAM); // ADAS project
				__maximumZeroFuelMass = Amount.valueOf(29716, SI.KILOGRAM); // 
				__maximumLandingMass = Amount.valueOf(32702.4, SI.KILOGRAM);
				__operatingEmptyMass = Amount.valueOf(20529, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0., SI.KILOGRAM);
				__referenceRange = Amount.valueOf(3500., SI.KILOMETER);
				break;
			}
		}
		
		public ACWeightsManager build() {
			return new ACWeightsManager(this);
		}
	}
	
	private ACWeightsManager(ACWeightsManagerBuilder builder) {
		
		this._id = builder.__id;
		ACWeightsManager._theAircraft = builder.__theAircraft;
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._maximumZeroFuelMass = builder.__maximumZeroFuelMass;
		this._maximumLandingMass = builder.__maximumLandingMass;
		this._operatingEmptyMass = builder.__operatingEmptyMass;
		this._trappedFuelOilMass = builder.__trappedFuelOilMass;
		this._referenceRange = builder.__referenceRange;
		
		this._maximumTakeOffMassList = builder.__maximumTakeOffMassList;
		this._massStructureList = builder.__massStructureList;
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	@SuppressWarnings("unchecked")
	public static ACWeightsManager importFromXML (String pathToXML, Aircraft theAircraft) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading weights analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		// MAXIMUM TAKE-OFF MASS
		Amount<Mass> maximumTakeOffMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//weights/maximum_take_off_mass");
		if(maximumTakeOffMassProperty != null)
			maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/maximum_take_off_mass");
		else {
			System.err.println("MAXIMUM TAKE-OFF MASS REQUIRED !! \n ... returning ");
			return null; 
		}
			
		//---------------------------------------------------------------
		// MAXIMUM LANDING MASS
		Amount<Mass> maximumLandingMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumLandingMassProperty = reader.getXMLPropertyByPath("//weights/maximum_landing_mass");
		if(maximumLandingMassProperty != null)
			maximumLandingMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/maximum_landing_mass");
		else {
			System.err.println("MAXIMUM LANDING MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// MAXIMUM ZERO FUEL MASS
		Amount<Mass> maximumZeroFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumZeroFuelMassProperty = reader.getXMLPropertyByPath("//weights/maximum_zero_fuel_mass");
		if(maximumZeroFuelMassProperty != null)
			maximumZeroFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/maximum_zero_fuel_mass");
		else {
			System.err.println("MAXIMUM ZERO FUEL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// OPERATING EMPTY MASS
		Amount<Mass> operatingEmptyMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//weights/operating_empty_mass");
		if(operatingEmptyMassProperty != null)
			operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/operating_empty_mass");
		else {
			System.err.println("MAXIMUM ZERO FUEL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// TRAPPED FUEL OIL MASS
		Amount<Mass> trappedFuelOilMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String trappedFuelOilMassProperty = reader.getXMLPropertyByPath("//weights/trapped_fuel_oil_mass");
		if(trappedFuelOilMassProperty != null)
			trappedFuelOilMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/trapped_fuel_oil_mass");
		else {
			System.err.println("TRAPPED FUEL OIL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// REFERENCE RANGE
		Amount<Length> referenceRange = Amount.valueOf(0.0, SI.KILOMETER);
		String referenceRangeProperty = reader.getXMLPropertyByPath("//weights/reference_range");
		if(referenceRangeProperty != null)
			referenceRange = (Amount<Length>) reader.getXMLAmountLengthByPath("//weights/reference_range").to(SI.KILOMETER);
		else {
			System.err.println("REFERENCE RANGE REQUIRED !! \n ... returning ");
			return null; 
		}
		
		ACWeightsManager theWeigths = new ACWeightsManagerBuilder(id, theAircraft)
				.maximumTakeOffMass(maximumTakeOffMass)
				.maximumLandingMass(maximumLandingMass)
				.maximumZeroFuelMass(maximumZeroFuelMass)
				.operatingEmptyMass(operatingEmptyMass)
				.trappedFuelOilMass(trappedFuelOilMass)
				.referenceRange(referenceRange)
				.build()
				;
		
		return theWeigths;

	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tWeights Analysis\n")
				.append("\t-------------------------------------\n")
				.append("\tReference Range: " + _referenceRange + "\n")
				.append("\tMaterial Density (Alluminuim): " + _materialDensity + "\n")
				.append("\tPax Single Mass: " + _paxSingleMass + "\n")
				.append("\t-------------------------------------\n")
				.append("\tMaximum Take-Off Mass: " + _maximumTakeOffMass + "\n")
				.append("\tMaximum Take-Off Weight: " + _maximumTakeOffWeight + "\n")
				.append("\t·····································\n")
				.append("\tTake-Off Mass: " + _takeOffMass + "\n")
				.append("\tTake-Off Weight: " + _takeOffWeight + "\n")
				.append("\t·····································\n")
				.append("\tMaximum Landing Mass: " + _maximumLandingMass + "\n")
				.append("\tMaximum Landing Weight: " + _maximumLandingWeight + "\n")
				.append("\t·····································\n")
				.append("\tMaximum Passenger Mass: " + _paxMassMax + "\n")
				.append("\tMaximum Passenger Weight: " + _paxMassMax.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")			
				.append("\t·····································\n")
				.append("\tPassenger Mass: " + _paxMass + "\n")
				.append("\tPassenger Weight: " + _paxMass.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")			
				.append("\t·····································\n")
				.append("\tCrew Mass: " + _crewMass + "\n")
				.append("\tCrew Weight: " + _crewMass.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")			
				.append("\t·····································\n")
				.append("\tFuel Mass: " + _theAircraft.getFuelTank().getFuelMass() + "\n")
				.append("\tFuel Weight: " + _theAircraft.getFuelTank().getFuelWeight() + "\n")			
				.append("\t·····································\n")
				.append("\tMaximum Zero Fuel Mass: " + _maximumZeroFuelMass + "\n")
				.append("\tMaximum Zero Fuel Weight: " + _maximumZeroFuelWeight + "\n")			
				.append("\t·····································\n")
				.append("\tZero Fuel Mass: " + _zeroFuelMass + "\n")
				.append("\tZero Fuel Weight: " + _zeroFuelWeight + "\n")
				.append("\t·····································\n")
				.append("\tOperating Empty Mass: " + _operatingEmptyMass + "\n")
				.append("\tOperating Empty Weight: " + _operatingEmptyWeight + "\n")
				.append("\t·····································\n")
				.append("\tEmpty Mass: " + _emptyMass + "\n")
				.append("\tEmpty Weight: " + _emptyWeight + "\n")
				.append("\t·····································\n")
				.append("\tManufacturer Empty Mass: " + _manufacturerEmptyMass + "\n")
				.append("\tManufacturer Empty Weight: " + _manufacturerEmptyWeight + "\n")
				.append("\t·····································\n")
				.append("\tStructural Mass: " + _structuralMass + "\n")
				.append("\tStructural Mass: " + _structuralMass.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")
				.append("\t·····································\n")
				.append("\tOperating Item Mass: " + _operatingItemMass + "\n")
				.append("\tOperating Item Weight: " + _operatingItemWeight + "\n")
				.append("\t·····································\n")
				.append("\tTrapped Fuel Oil Mass: " + _trappedFuelOilMass + "\n")
				.append("\tTrapped Fuel Oil Weight: " + _trappedFuelOilWeight + "\n")
				.append("\t·····································\n")
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
		dataListGlobal.add(new Object[] {"Reference Range","nmi",_referenceRange.doubleValue(NonSI.NAUTICAL_MILE)});
		dataListGlobal.add(new Object[] {"Material density","kg/m³",_materialDensity.getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Single passenger Mass","kg",_paxSingleMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Maximum Take-Off Mass","kg",_maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Take-Off Weight","N",_maximumTakeOffWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Take-Off Mass","kg",_takeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Take-Off Weight","N",_takeOffWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Maximum Landing Mass","kg",_maximumLandingMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Landing Weight","N",_maximumLandingWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Maximum Passengers Mass","kg",_paxMassMax.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Passengers Weight","N",(_paxMassMax.times(AtmosphereCalc.g0)).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Fuel Mass","kg",_theAircraft.getFuelTank().getFuelMass().doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Fuel Weight","N",_theAircraft.getFuelTank().getFuelWeight().doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Crew Mass","kg",_crewMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Crew Weight","N",_crewMass.times(AtmosphereCalc.g0).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Maximum Zero Fuel Mass","kg",_maximumZeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Zero Fuel Weight","N",_maximumZeroFuelWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Zero Fuel Mass","kg",_zeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Zero Fuel Weight","N",_zeroFuelWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Operating Empty Mass","kg",_operatingEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Empty Weight","N",_operatingEmptyWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Empty Mass","kg",_emptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Empty Weight","N",_emptyWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Manufacturer Empty Mass","kg",_manufacturerEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Manufacturer Empty Weight","N",_manufacturerEmptyMass.times(AtmosphereCalc.g0).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Operating Item Mass","kg",_operatingItemMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Item Weight","N",_operatingItemWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Trapped Fuel Oil Mass","kg",_trappedFuelOilMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Trapped Fuel Oil Weight","N",_trappedFuelOilWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Operating Empty Mass","kg",_operatingEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Empty Weight","N",_operatingEmptyWeight.doubleValue(SI.NEWTON)});
		
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
		// FUSELAGE WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getFuselage() != null) {
			Sheet sheetFuselage = wb.createSheet("FUSELAGE");
			List<Object[]> dataListFuselage = new ArrayList<>();
			dataListFuselage.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListFuselage.add(new Object[] {"Reference Mass","kg", _theAircraft.getFuselage().getReferenceMass().getEstimatedValue()});
			dataListFuselage.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getFuselage().getMassCorrectionFactor()});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexFuselage=0;
			for(MethodEnum methods : _theAircraft.getFuselage().getMassMap().keySet()) {
				if(_theAircraft.getFuselage().getMassMap().get(methods) != null) 
					dataListFuselage.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getFuselage().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getFuselage().getPercentDifference()[indexFuselage]
							}
							);
				indexFuselage++;
			}
			dataListFuselage.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getFuselage().getMassEstimated().getEstimatedValue()});

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
		// WING WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getWing() != null) {
			Sheet sheetWing = wb.createSheet("WING");
			List<Object[]> dataListWing = new ArrayList<>();
			dataListWing.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListWing.add(new Object[] {"Reference Mass","kg", _theAircraft.getWing().getMassReference().getEstimatedValue()});
			dataListWing.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getWing().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListWing.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getWing().getMassCorrectionFactor()});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexWing=0;
			for(MethodEnum methods : _theAircraft.getWing().getMassMap().keySet()) {
				if(_theAircraft.getWing().getMassMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getWing().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getWing().getPercentDifference()[indexWing]
							}
							);
				indexWing++;
			}
			dataListWing.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getWing().getMassEstimated().getEstimatedValue()});

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
		// HORIZONTAL TAIL WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getHTail() != null) {
			Sheet sheetHTail = wb.createSheet("HORIZONTAL TAIL");
			List<Object[]> dataListHTail = new ArrayList<>();
			dataListHTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListHTail.add(new Object[] {"Reference Mass","kg", _theAircraft.getHTail().getMassReference().getEstimatedValue()});
			dataListHTail.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getHTail().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListHTail.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getHTail().getMassCorrectionFactor()});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexHTail=0;
			for(MethodEnum methods : _theAircraft.getHTail().getMassMap().keySet()) {
				if(_theAircraft.getHTail().getMassMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getHTail().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getHTail().getPercentDifference()[indexHTail]
							}
							);
				indexHTail++;
			}
			dataListHTail.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getHTail().getMassEstimated().getEstimatedValue()});

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
		// VERTICAL TAIL WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getVTail() != null) {
			Sheet sheetVTail = wb.createSheet("VERTICAL TAIL");
			List<Object[]> dataListVTail = new ArrayList<>();
			dataListVTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListVTail.add(new Object[] {"Reference Mass","kg", _theAircraft.getVTail().getMassReference().getEstimatedValue()});
			dataListVTail.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getVTail().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListVTail.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getVTail().getMassCorrectionFactor()});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexVTail=0;
			for(MethodEnum methods : _theAircraft.getVTail().getMassMap().keySet()) {
				if(_theAircraft.getVTail().getMassMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getVTail().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getVTail().getPercentDifference()[indexVTail]
							}
							);
				indexVTail++;
			}
			dataListVTail.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getVTail().getMassEstimated().getEstimatedValue()});

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
		// CANARD WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getCanard() != null) {
			Sheet sheetCanard = wb.createSheet("CANARD");
			List<Object[]> dataListCanard = new ArrayList<>();
			dataListCanard.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListCanard.add(new Object[] {"Reference Mass","kg", _theAircraft.getCanard().getMassReference().getEstimatedValue()});
			dataListCanard.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getCanard().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListCanard.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getCanard().getMassCorrectionFactor()});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexCanard=0;
			for(MethodEnum methods : _theAircraft.getCanard().getMassMap().keySet()) {
				if(_theAircraft.getCanard().getMassMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getCanard().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getCanard().getPercentDifference()[indexCanard]
							}
							);
				indexCanard++;
			}
			dataListCanard.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getCanard().getMassEstimated().getEstimatedValue()});

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
		// NACELLES WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getNacelles() != null) {
			Sheet sheetNacelles = wb.createSheet("NACELLES");
			List<Object[]> dataListNacelles = new ArrayList<>();
			dataListNacelles.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListNacelles.add(new Object[] {"Total Reference Mass","kg", _theAircraft.getNacelles().getMassReference().getEstimatedValue()});
			dataListNacelles.add(new Object[] {"Total mass estimated","kg",_theAircraft.getNacelles().getTotalMass().getEstimatedValue(),_theAircraft.getNacelles().getPercentTotalDifference()});
			dataListNacelles.add(new Object[] {" "});
			dataListNacelles.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON FOR EACH NACELLE"});
			dataListNacelles.add(new Object[] {" "});
			for(int iNacelle = 0; iNacelle < _theAircraft.getNacelles().getNacellesNumber(); iNacelle++) {
				dataListNacelles.add(new Object[] {"NACELLE " + (iNacelle+1)});
				dataListNacelles.add(new Object[] {"Reference Mass","kg", _theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassReference().getEstimatedValue()});
				int indexNacelles=0;
				for(MethodEnum methods : _theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassMap().keySet()) {
					if(_theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassMap().get(methods) != null) 
						dataListNacelles.add(
								new Object[] {
										methods.toString(),
										"Kg",
										_theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassMap().get(methods).getEstimatedValue(),
										_theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getPercentDifference()[indexNacelles]
								}
								);
					indexNacelles++;
				}
				dataListNacelles.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassEstimated().getEstimatedValue()});
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
		// POWER PLANT WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getPowerPlant() != null) {
			Sheet sheetPowerPlant = wb.createSheet("POWER PLANT");
			List<Object[]> dataListPowerPlant = new ArrayList<>();
			dataListPowerPlant.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListPowerPlant.add(new Object[] {"Total Reference Mass","kg", _theAircraft.getPowerPlant().getDryMassPublicDomainTotal().getEstimatedValue()});
			dataListPowerPlant.add(new Object[] {"Total mass estimated","kg",_theAircraft.getPowerPlant().getTotalMass().getEstimatedValue(),_theAircraft.getNacelles().getPercentTotalDifference()});
			dataListPowerPlant.add(new Object[] {" "});
			dataListPowerPlant.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON FOR EACH ENGINE"});
			dataListPowerPlant.add(new Object[] {" "});
			for(int iEngine = 0; iEngine < _theAircraft.getPowerPlant().getEngineNumber(); iEngine++) {
				dataListPowerPlant.add(new Object[] {"ENGINE " + (iEngine+1)});
				dataListPowerPlant.add(new Object[] {"Reference Mass","kg", _theAircraft.getPowerPlant().getEngineList().get(iEngine).getTheWeights().getDryMassPublicDomain().getEstimatedValue()});
				dataListPowerPlant.add(new Object[] {"Total Mass","kg", _theAircraft.getPowerPlant().getEngineList().get(iEngine).getTotalMass().getEstimatedValue()});			
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
		// LANDING GEARS WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getLandingGears() != null) {
			Sheet sheetLandingGears = wb.createSheet("LANDING GEARS");
			List<Object[]> dataListLandingGears = new ArrayList<>();
			dataListLandingGears.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListLandingGears.add(new Object[] {"Reference Mass","kg", _theAircraft.getLandingGears().getReferenceMass().getEstimatedValue()});
			dataListLandingGears.add(new Object[] {"Overall Mass","kg", _theAircraft.getLandingGears().getOverallMass().getEstimatedValue()});
			dataListLandingGears.add(new Object[] {" "});
			dataListLandingGears.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexLandingGears=0;
			for(MethodEnum methods : _theAircraft.getLandingGears().getMassMap().keySet()) {
				if(_theAircraft.getLandingGears().getMassMap().get(methods) != null) 
					dataListLandingGears.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theAircraft.getLandingGears().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getLandingGears().getPercentDifference()[indexLandingGears]
							}
							);
				indexLandingGears++;
			}
			dataListLandingGears.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getLandingGears().getMassEstimated().getEstimatedValue()});

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
		// SYSTEMS WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getSystems() != null) {
			Sheet sheetSystems = wb.createSheet("SYSTEMS");
			List<Object[]> dataListSystems = new ArrayList<>();
			dataListSystems.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListSystems.add(new Object[] {"Reference Mass","kg", _theAircraft.getSystems().getReferenceMass().getEstimatedValue()});
			dataListSystems.add(new Object[] {"Overall Mass","kg", _theAircraft.getSystems().getOverallMass().getEstimatedValue()});
			dataListSystems.add(new Object[] {" "});
			dataListSystems.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexSystems=0;
			for(MethodEnum methods : _theAircraft.getSystems().getMassMap().keySet()) {
				if(_theAircraft.getSystems().getMassMap().get(methods) != null) 
					dataListSystems.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theAircraft.getSystems().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getSystems().getPercentDifference()[indexSystems]
							}
							);
				indexSystems++;
			}
			dataListSystems.add(new Object[] {"Estimated Mass ","Kg", _theAircraft.getSystems().getMeanMass().getEstimatedValue()});

			Row rowSystems = sheetSystems.createRow(0);
			Object[] objArrSystems = dataListSystems.get(0);
			int cellnumSystems = 0;
			for (Object obj : objArrSystems) {
				Cell cell = rowSystems.createCell(cellnumSystems++);
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
				sheetSystems.setDefaultColumnWidth(25);
			}

			int rownumSystems = 1;
			for (int j = 1; j < dataListSystems.size(); j++) {
				objArrSystems = dataListSystems.get(j);
				rowSystems = sheetSystems.createRow(rownumSystems++);
				cellnumSystems = 0;
				for (Object obj : objArrSystems) {
					Cell cell = rowSystems.createCell(cellnumSystems++);
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

	public void calculateDependentVariables(Aircraft aircraft) {

		// Passengers and crew mass
		// 76.5 kg for each crew member + baggage
		_paxMass = _paxSingleMass.times(aircraft.getCabinConfiguration().getNPax());
		_crewMass = Amount.valueOf(aircraft.getCabinConfiguration().getNCrew() * 76.5145485, SI.KILOGRAM); 

		// Passengers and crew mass
		_paxMassMax = _paxSingleMass.times(aircraft.getCabinConfiguration().getMaxPax());

		// Operating items mass
		if (_referenceRange.getEstimatedValue() < 2000) { 
			_operatingItemMass = Amount.valueOf(8.617*aircraft.getCabinConfiguration().getNPax(), SI.KILOGRAM);
			_operatingItemWeight = _operatingItemMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		} else {
			_operatingItemMass = Amount.valueOf(14.97*aircraft.getCabinConfiguration().getNPax(), SI.KILOGRAM);
			_operatingItemWeight = _operatingItemMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		}

		_emptyMass =_operatingEmptyMass.minus(_crewMass).minus(_trappedFuelOilMass);
		_emptyWeight = _emptyMass.times(AtmosphereCalc.g0).to(SI.NEWTON);

		_maximumTakeOffWeight = _maximumTakeOffMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_maximumZeroFuelWeight = _maximumZeroFuelMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_operatingEmptyWeight = _operatingEmptyMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_trappedFuelOilWeight = _trappedFuelOilMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_maximumLandingWeight = _maximumLandingMass.times(AtmosphereCalc.g0).to(SI.NEWTON);

	}

	/** 
	 * Calculate mass of selected configuration. When comparing some/all available methods 
	 * for the selected component the iterative procedure is done using the first selected method.
	 * 
	 * @param aircraft
	 * @param methodsMap
	 */
	public void calculateAllMasses(Aircraft aircraft, 
			Map <ComponentEnum, List<MethodEnum>> methodsMap) {

		System.out.println("\n-----------------------------------------------");
		System.out.println("----- WEIGHT ESTIMATION PROCEDURE STARTED -----");
		System.out.println("-----------------------------------------------\n");
		calculateFirstGuessMTOM(aircraft);

		aircraft.getFuelTank().calculateFuelMass();

		int i=0;
		_maximumTakeOffMassList.add(Amount.valueOf(0.0, SI.KILOGRAM));

		Amount<Mass> sum = Amount.valueOf(0., SI.KILOGRAM);

		// Evaluate MTOM 5 times and then take the mean value to avoid
		// an infinite loop due to MTOM estimate oscillation 
		while (i < 5) {

			_maximumTakeOffMassList.add(_maximumTakeOffMass);

			aircraft.getTheAnalysisManager().getTheWeights().calculateDependentVariables(aircraft);

			//////////////////////////////////////////////////////////////////
			// Evaluate weights with more than one method for each component
			//////////////////////////////////////////////////////////////////

			// --- STRUCTURE MASS-----------------------------------

			calculateStructuralMass(aircraft, methodsMap);

			// --- END OF STRUCTURE MASS-----------------------------------

			aircraft.getPowerPlant().calculateMass();

			// --- END OF POWER PLANT MASS-----------------------------------

			calculateManufacturerEmptyMass(aircraft);

			// --- END OF MANUFACTURER EMPTY MASS-----------------------------------

			aircraft.getTheAnalysisManager().getTheWeights().setOperatingEmptyMass(
					aircraft.getTheAnalysisManager().getTheWeights().getManufacturerEmptyMass().plus(
							aircraft.getTheAnalysisManager().getTheWeights().getOperatingItemMass()).plus(
									aircraft.getTheAnalysisManager().getTheWeights().getCrewMass()));

			// --- END OF OPERATING EMPTY MASS-----------------------------------

			// Zero fuel mass
			aircraft.getTheAnalysisManager().getTheWeights().setZeroFuelMass(
					aircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().plus(
							_paxMass));

			aircraft.getTheAnalysisManager().getTheWeights().setZeroFuelWeight(
					aircraft.getTheAnalysisManager().getTheWeights().getZeroFuelMass().times(
							AtmosphereCalc.g0).to(SI.NEWTON));

			// Maximum zero fuel mass
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumZeroFuelMass(
					aircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().plus(
							_paxMassMax));

			// --- END ZERO FUEL MASS-----------------------------------

			// Take-off mass
			aircraft.getTheAnalysisManager().getTheWeights().setTakeOffMass(
					aircraft.getTheAnalysisManager().getTheWeights().getZeroFuelMass().plus(
							aircraft.getFuelTank().getFuelMass()));

			aircraft.getTheAnalysisManager().getTheWeights().setTakeOffWeight(
					aircraft.getTheAnalysisManager().getTheWeights().getTakeOffMass().times(
							AtmosphereCalc.g0).to(SI.NEWTON));

			// Maximum take-off mass
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumTakeOffMass(
					aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().plus(
							aircraft.getFuelTank().getFuelMass()));

			// Maximum landing mass
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumLandingMass(_maximumTakeOffMass.times(0.9));

			System.out.println("Iteration " + (i+1) + 
					", Structure mass: " + aircraft.getTheAnalysisManager().getTheWeights().getStructuralMass() + 
					" , Maximum Take-Off Mass: " + _maximumTakeOffMass);

			sum = sum.plus(_maximumTakeOffMass);
			i++;
			_maximumTakeOffMass = sum.divide(i);

			aircraft.getTheAnalysisManager().getTheWeights().calculateDependentVariables(aircraft);
		}

		_massStructureList.add(aircraft.getFuselage().getMassEstimated());
		_massStructureList.add(aircraft.getWing().getMassEstimated());
		_massStructureList.add(aircraft.getHTail().getMassEstimated());
		_massStructureList.add(aircraft.getVTail().getMassEstimated());
		_massStructureList.addAll(aircraft.getNacelles().getMassList());
		_massStructureList.add(aircraft.getLandingGears().getMassEstimated());

		System.out.println("\n-----------------------------------------------");
		System.out.println("--- WEIGHT ESTIMATION PROCEDURE COMPLETED -----");
		System.out.println("-----------------------------------------------\n");

	}

	public void calculateStructuralMass(
			Aircraft aircraft, 
			Map <ComponentEnum, List<MethodEnum>> methodsMap) {

		if(aircraft.getFuselage() != null)
			aircraft.getFuselage().calculateMass(aircraft);

		if(aircraft.getWing() != null)
			aircraft.getWing().calculateMass(aircraft);
		if(aircraft.getHTail() != null)
			aircraft.getHTail().calculateMass(aircraft);
		if(aircraft.getVTail() != null)
			aircraft.getVTail().calculateMass(aircraft);
		if(aircraft.getCanard() != null)
			aircraft.getCanard().calculateMass(aircraft);
		
		if(aircraft.getNacelles() != null)
			aircraft.getNacelles().calculateMass(aircraft);

		if(aircraft.getLandingGears() != null)
			aircraft.getLandingGears().calculateMass(aircraft);

		if(aircraft.getSystems() != null)
			aircraft.getSystems().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);

		aircraft.getTheAnalysisManager().getTheWeights().setStructuralMass(
				aircraft.getFuselage().getMassEstimated().plus(
						aircraft.getWing().getMassEstimated()).plus(
								aircraft.getHTail().getMassEstimated()).plus(
										aircraft.getVTail().getMassEstimated()).plus(
												aircraft.getNacelles().getTotalMass()).plus(
														aircraft.getLandingGears().getMassEstimated()));

	}

	public void calculateManufacturerEmptyMass(Aircraft aircraft) {
		if(aircraft.getSystems() != null)
			aircraft.getSystems().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		if(aircraft.getCabinConfiguration() != null)
			aircraft.getCabinConfiguration().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		aircraft.getTheAnalysisManager().getTheWeights().setManufacturerEmptyMass(
				aircraft.getPowerPlant().getTotalMass().plus(
						aircraft.getTheAnalysisManager().getTheWeights().getStructuralMass()).plus(
								aircraft.getSystems().getOverallMass()).plus(
										aircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment()));
	}


	public void calculateFirstGuessMTOM(Aircraft aircraft) {

		if(aircraft.getFuselage() != null)
			aircraft.getFuselage().setMass(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.15));
		if(aircraft.getWing() != null)
			aircraft.getWing().setMassReference(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.1));
		if(aircraft.getHTail() != null)
			aircraft.getHTail().setMassReference(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.015));
		if(aircraft.getVTail() != null)
			aircraft.getVTail().setMassReference(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.015));
		if(aircraft.getPowerPlant() != null)
			aircraft.getPowerPlant().setTotalMass(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.05));
		if(aircraft.getNacelles() != null)
			aircraft.getNacelles().setTotalMass(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.015));
		if(aircraft.getFuelTank() != null)
			aircraft.getFuelTank().setFuelMass(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.015));
		if(aircraft.getLandingGears() != null)
			aircraft.getLandingGears().setMass(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.04));
		if(aircraft.getSystems() != null)
			aircraft.getSystems().setOverallMass(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().times(.04));

		aircraft.getTheAnalysisManager().getTheWeights().setStructuralMass(
				aircraft.getFuselage().getMass().plus(
						aircraft.getWing().getMassReference()).plus(
								aircraft.getHTail().getMassReference()).plus(
										aircraft.getVTail().getMassReference()).plus(
												aircraft.getNacelles().getTotalMass()).plus(
														aircraft.getLandingGears().getOverallMass()));

		System.out.println("First guess value:" + aircraft.getTheAnalysisManager().getTheWeights().getStructuralMass().getEstimatedValue());
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> _MTOM) {
		this._maximumTakeOffMass = _MTOM;
	}

	public Amount<Mass> getMaximumZeroFuelMass() {
		return _maximumZeroFuelMass;
	}

	public Amount<Force> getMaximumTakeOffWeight() {
		return _maximumTakeOffWeight;
	}

	public Amount<Force> getMaximumZeroFuelWeight() {
		return _maximumZeroFuelWeight;
	}

	public Amount<Mass> getPaxMass() {
		return _paxMass;
	}

	public Amount<Mass> getCrewMass() {
		return _crewMass;
	}

	public Amount<Mass> getEmptyMass() {
		return _emptyMass;
	}

	public void setEmptyMass(Amount<Mass> _emptyMass) {
		this._emptyMass = _emptyMass;
	}

	public Amount<Force> getEmptyWeight() {
		return _emptyWeight;
	}

	public void setEmptyWeight(Amount<Force> _emptyWeight) {
		this._emptyWeight = _emptyWeight;
	}

	public Amount<Mass> getStructuralMass() {
		return _structuralMass;
	}

	public void setStructuralMass(Amount<Mass> _structureMass) {
		this._structuralMass = _structureMass;
	}

	public Amount<Force> getStructuralWeight() {
		return _structuralMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
	}

	public Amount<VolumetricDensity> getMaterialDensity() {
		return _materialDensity;
	}

	public void setMaterialDensity(Amount<VolumetricDensity> _materialDensity) {
		ACWeightsManager._materialDensity = _materialDensity;
	}

	public Amount<Force> getMaximumLandingWeight() {
		return _maximumLandingWeight;
	}

	public void setMaximumLandingWeight(Amount<Force> _MLW) {
		this._maximumLandingWeight = _MLW;
	}

	public Amount<Mass> getMaximumLangingMass() {
		return _maximumLandingMass;
	}

	public void setMaximumLandingMass(Amount<Mass> _MLM) {
		this._maximumLandingMass = _MLM;
	}

	public Amount<Mass> getOperatingItemMass() {
		return _operatingItemMass;
	}

	public void setOperatingItemMass(Amount<Mass> _OIM) {
		this._operatingItemMass = _OIM;
	}

	public Amount<Force> getOperatingItemWeight() {
		return _operatingItemWeight;
	}

	public void setOperatingItemWeight(Amount<Force> _operatingItemWeight) {
		this._operatingItemWeight = _operatingItemWeight;
	}

	public Amount<Mass> getManufacturerEmptyMass() {
		return _manufacturerEmptyMass;
	}

	public void setManufacturerEmptyMass(Amount<Mass> _manufacturerEmptyMass) {
		this._manufacturerEmptyMass = _manufacturerEmptyMass;
	}

	public Amount<Force> getManufacturerEmptyWeight() {
		return _manufacturerEmptyWeight;
	}

	public void setManufacturerEmptyWeight(Amount<Force> _manufacturerEmptyWeight) {
		this._manufacturerEmptyWeight = _manufacturerEmptyWeight;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> _OEM) {
		this._operatingEmptyMass = _OEM;
	}

	public Amount<Force> getOperatingEmptyWeight() {
		return _operatingEmptyWeight;
	}

	public void setOperatingEmptyWeight(Amount<Force> _operatingEmptyWeight) {
		this._operatingEmptyWeight = _operatingEmptyWeight;
	}

	public Amount<Mass> getTrappedFuelOilMass() {
		return _trappedFuelOilMass;
	}

	public void setTrappedFuelOilMass(Amount<Mass> _trappedFuelOilMass) {
		this._trappedFuelOilMass = _trappedFuelOilMass;
	}

	public Amount<Force> getTrappedFuelOilWeight() {
		return _trappedFuelOilWeight;
	}

	public void setTrappedFuelOilWeight(Amount<Force> _trappedFuelOilWeight) {
		this._trappedFuelOilWeight = _trappedFuelOilWeight;
	}

	public Amount<Mass> getZeroFuelMass() {
		return _zeroFuelMass;
	}

	public void setZeroFuelMass(Amount<Mass> _ZFM) {
		this._zeroFuelMass = _ZFM;
	}

	public Amount<Force> getZeroFuelWeight() {
		return _zeroFuelWeight;
	}

	public void setZeroFuelWeight(Amount<Force> _zeroFuelWeight) {
		this._zeroFuelWeight = _zeroFuelWeight;
	}

	public Amount<Mass> getPaxMassMax() {
		return _paxMassMax;
	}

	public void setMaximumZeroFuelMass(Amount<Mass> _MZFM) {
		this._maximumZeroFuelMass = _MZFM;
	}

	public Amount<Mass> getTakeOffMass() {
		return _takeOffMass;
	}

	public void setTakeOffMass(Amount<Mass> _TOM) {
		this._takeOffMass = _TOM;
	}

	public Amount<Force> getTakeOffWeight() {
		return _takeOffWeight;
	}

	public void setTakeOffWeight(Amount<Force> _takeOffWeight) {
		this._takeOffWeight = _takeOffWeight;
	}

	public Amount<Mass> getPaxSingleMass() {
		return _paxSingleMass;
	}

	public void setPaxSingleMass(Amount<Mass> _paxSingleMass) {
		ACWeightsManager._paxSingleMass = _paxSingleMass;
	}

	public List<Amount<Mass>> getMassStructureList() {
		return _massStructureList;
	}

	public void setMassStructureList(List<Amount<Mass>> _massStructureList) {
		this._massStructureList = _massStructureList;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		ACWeightsManager._theAircraft = _theAircraft;
	}

	public Amount<Length> getRange() {
		return _referenceRange;
	}

	public void setRange(Amount<Length> _range) {
		this._referenceRange = _range;
	}
}