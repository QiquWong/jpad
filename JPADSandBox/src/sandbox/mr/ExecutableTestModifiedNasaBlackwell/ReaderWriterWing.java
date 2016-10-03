package sandbox.mr.ExecutableTestModifiedNasaBlackwell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AirfoilFamilyEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.DatabaseManager;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class ReaderWriterWing {
	

	public void importFromXML(String pathToXML, String databaseFolderPath, String aerodynamicDatabaseFileName, InputOutputTree input) throws ParserConfigurationException {


		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");
		
		//------------------------------------------------------------------------------------
		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(new AerodynamicDatabaseReader(
				databaseFolderPath,	aerodynamicDatabaseFileName),
				databaseFolderPath);


		//---------------------------------------------------------------------------------
		// OPERATING CONDITION:
		
		Amount<Length> altitude = reader.getXMLAmountWithUnitByPath("//altitude").to(SI.METER);
		input.setAltitude(altitude);
	
		double machNumber =  Double.parseDouble(reader.getXMLPropertiesByPath("//mach_number").get(0));
		input.setMachNumber(machNumber);
	
		List<String> alphaAnalysisArray = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_array_analysis").get(0));
		for(int i=0; i<alphaAnalysisArray.size(); i++)
			input.getAlphaAnalysisValues().add(Amount.valueOf(Double.valueOf(alphaAnalysisArray.get(i)), NonSI.DEGREE_ANGLE));
		
		int numberOfAlpha =  alphaAnalysisArray.size();
		input.setNumberOfAlpha(numberOfAlpha);
		
		
		//----------------------------------------------------------------------------------
		// GLOBAL
		
		Amount<Area> surface = reader.getXMLAmountWithUnitByPath("//surface").to(SI.SQUARE_METRE);
		input.setSurface(surface);
		
		double aspectRatio = Double.parseDouble(reader.getXMLPropertiesByPath("//aspect_ratio").get(0));
		input.setAspectRatio(aspectRatio);
		
		int numberOfPointSemispan =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_point_semispan").get(0));
		input.setNumberOfPointSemispan(numberOfPointSemispan);
		
		double adimensionalKinkStation = Double.parseDouble(reader.getXMLPropertiesByPath("//adimensional_kink_station ").get(0));
		input.setAdimensionalKinkStation(adimensionalKinkStation);
		
		double meanAirfoilThickness = Double.parseDouble(reader.getXMLPropertiesByPath("//max_thickness_mean_airfoil").get(0));
		input.setMeanThickness(meanAirfoilThickness);
		
		double [] yApp = new double[numberOfPointSemispan];
		yApp = MyArrayUtils.linspace(0, 1, numberOfPointSemispan);
		input.setyStationsAdimensional(yApp);
	
		
		//-------------------------------------------------------------------------------------
		//DISTRIBUTION
		
		int numberOfSection =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_given_sections").get(0));
		input.setNumberOfSections(numberOfSection);
		
		List<String> airfoilFamilyProperty = reader.getXMLPropertiesByPath("//airfoil_family");
		if(airfoilFamilyProperty.get(0).equals("NACA_4_DIGIT"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_4_Digit);
		else if(airfoilFamilyProperty.get(0).equals("NACA_5_DIGIT"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_5_Digit);
		else if(airfoilFamilyProperty.get(0).equals("NACA_63_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_63_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_64_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_64_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_65_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_65_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_66_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_66_Series);
		else if(airfoilFamilyProperty.get(0).equals("BICONVEX"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.BICONVEX);
		else if(airfoilFamilyProperty.get(0).equals("DOUBLE_WEDGE"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.DOUBLE_WEDGE);
		else {
			System.err.println("NO VALID FAMILY TYPE!!");
			return;
			
		}
		
		//recognizing airfoil family
				int airfoilFamilyIndex = 0;
				if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit) 
					airfoilFamilyIndex = 1;
				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_5_Digit)
					airfoilFamilyIndex = 2;
				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_63_Series)
					airfoilFamilyIndex = 3;
				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_64_Series)
					airfoilFamilyIndex = 4;
				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_65_Series)
					airfoilFamilyIndex = 5;
				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_66_Series)
					airfoilFamilyIndex = 6;
				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.BICONVEX)
					airfoilFamilyIndex = 7;
				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.DOUBLE_WEDGE)
					airfoilFamilyIndex = 8;
				
			
		double sharpnessParameterLE = aeroDatabaseReader.getDeltaYvsThickness(input.getMeanThickness(), airfoilFamilyIndex);
									
		List<String> chordDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//chord_distribution").get(0));
		for(int i=0; i<chordDistribution.size(); i++)
			input.getChordDistribution().add(Amount.valueOf(Double.valueOf(chordDistribution.get(i)), SI.METER));
		
		List<String> yAdimensionalStationIput = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//y_adimensional_stations").get(0));
		for(int i=0; i<yAdimensionalStationIput.size(); i++)
			input.getyAdimensionalStationInput().add(Double.valueOf(yAdimensionalStationIput.get(i)));
		
		List<String> xleDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//x_le_distribution").get(0));
		for(int i=0; i<xleDistribution.size(); i++)
			input.getxLEDistribution().add(Amount.valueOf(Double.valueOf(xleDistribution.get(i)), SI.METER));
		
		List<String> twistDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//twist_distribution").get(0));
		for(int i=0; i<twistDistribution.size(); i++)
			input.getTwistDistribution().add(Amount.valueOf(Double.valueOf(twistDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> dihedralDistribution  = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//dihedral_distribution").get(0));
		for(int i=0; i<dihedralDistribution.size(); i++)
			input.getDihedralDistribution().add(Amount.valueOf(Double.valueOf(dihedralDistribution.get(i)), NonSI.DEGREE_ANGLE));
			
		
		// It is possible to accept as input the main point or the complete curves
		
		String inputMethod = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_executable/@method_input");
		
//----1-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		// MAIN POINTS--------------------------------------------------------------------------------------------------------------------
			if(inputMethod.equalsIgnoreCase("MAIN_POINTS")) {
				
				List<String> alphaZeroLiftDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_zero_lift_distribution").get(0));
				for(int i=0; i<alphaZeroLiftDistribution.size(); i++)
					input.getAlphaZeroLiftDistribution().add(Amount.valueOf(Double.valueOf(alphaZeroLiftDistribution.get(i)), NonSI.DEGREE_ANGLE));
				
				List<String> alphaStarDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_star_distribution").get(0));
				for(int i=0; i<alphaStarDistribution.size(); i++)
					input.getAlphaStarDistribution().add(Amount.valueOf(Double.valueOf(alphaStarDistribution.get(i)), NonSI.DEGREE_ANGLE));
				
				List<String> alphaStallDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_stall_distribution").get(0));
				for(int i=0; i<alphaStallDistribution.size(); i++)
					input.getAlphaStallDistribution().add(Amount.valueOf(Double.valueOf(alphaStallDistribution.get(i)), NonSI.DEGREE_ANGLE));
				
				List<String> clMaxDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//maximum_lift_coefficient_distribution").get(0));
				for(int i=0; i<clMaxDistribution.size(); i++)
					input.getMaximumliftCoefficientDistribution().add(Double.valueOf(clMaxDistribution.get(i)));
				
				List<String> clAlphaDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//linear_slope_coefficient").get(0));
				for(int i=0; i<clAlphaDistribution.size(); i++)
					input.getClAlphaDistribution().add(Double.valueOf(clAlphaDistribution.get(i)));

				// WARNINGS
				
				if ( input.getNumberOfSections() != input.getChordDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of chords. ( number of section = " + input.getNumberOfSections()
					 + " ; number of chords = " + input.getChordDistribution().size() + " )");
				}
				
				if ( input.getNumberOfSections() != input.getxLEDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of XLE values. ( number of section = " + input.getNumberOfSections()
					 + " ; number of XLE values = " + input.getxLEDistribution().size()+ " )");
				}
				
				if ( input.getNumberOfSections() != input.getTwistDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of twist angles. ( number of section = " + input.getNumberOfSections()
					 + " ; number of twist angles = " + input.getTwistDistribution().size()+ " )");
				}
				
				if ( input.getNumberOfSections() != input.getDihedralDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of dihedral angles. ( number of section = " + input.getNumberOfSections()
					 + " ; number of dihedral angles = " + input.getDihedralDistribution().size()+ " )");
				}
				
				if ( input.getNumberOfSections() != input.getAlphaZeroLiftDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of zero lift angles. ( number of section = " + input.getNumberOfSections()
					 + " ; number of zero lift angles = " + input.getAlphaZeroLiftDistribution().size()+ " )");
				}
				
				if ( input.getNumberOfSections() != input.getAlphaStarDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of end of linearity angles. ( number of section = " + input.getNumberOfSections()
					 + " ; number of end of linearity angles = " + input.getAlphaStarDistribution().size()+ " )");
				}
					
				if ( input.getNumberOfSections() != input.getMaximumliftCoefficientDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of cl max. ( number of section = " + input.getNumberOfSections()
					 + " ; number of cl max = " + input.getMaximumliftCoefficientDistribution().size()+ " )");
				}
				
				if ( input.getNumberOfSections() != input.getAlphaStallDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of alpha stall. ( number of section = " + input.getNumberOfSections()
					 + " ; number of alpha stall = " + input.getAlphaStallDistribution().size()+ " )");
				}
				
				if ( input.getNumberOfSections() != input.getClAlphaDistribution().size()){
					 System.err.println("WARNING! the number of declared section differs from the number of cl alpha. ( number of section = " + input.getNumberOfSections()
					 + " ; number of cl alpha  = " + input.getClAlphaDistribution().size()+ " )");
				}
							
			// Built of interpolated curve---------------------------------------------------------------------
				
				//alphaArray
				double alphaMin = 0.0;
				double alphaMax = 0.0;
				double[] alphaMinTemp = MyArrayUtils.convertListOfAmountodoubleArray(input.getAlphaZeroLiftDistribution());
				double[] alphaMaxTemp = MyArrayUtils.convertListOfAmountodoubleArray(input.getAlphaStallDistribution());
				alphaMin = MyArrayUtils.getMin(alphaMinTemp);
				alphaMax =  MyArrayUtils.getMin(alphaMaxTemp);
				
				input.setAlphaArrayCompleteCurveAirfoil(MyArrayUtils.linspace(alphaMin-2, alphaMax+5, input.getNumberOfPoint2DCurve()));
				
				// cl
				for (int i=0; i<input.getNumberOfSections(); i++)
					Calculator.calculateClvsAlphaAirfoil(input, i);
			
			}
			
			
//----2-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------			
		  // COMPLETE CURVE-------------------------------------------------------------------------------------------------	
			else if(inputMethod.equalsIgnoreCase("COMPLETE_CURVE")) {
			
				List<String> alphaAirfoilDistribution, clAirfoilDistribution;
				
				for (int i=0; i<input.getNumberOfSections(); i++){
					alphaAirfoilDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//airfoil/alpha").get(i));
					List<Amount<Angle>> alphaAmount = new ArrayList<Amount<Angle>>();
					for(int j=0; j<alphaAirfoilDistribution.size(); j++){
						alphaAmount.add(Amount.valueOf(Double.valueOf(alphaAirfoilDistribution.get(j)), NonSI.DEGREE_ANGLE));
					}
						input.getAlphaAirfoils().add(i,alphaAmount);	
			}
				for (int i=0; i<input.getNumberOfSections(); i++){
					clAirfoilDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//airfoil/lift_coefficient").get(i));
					List<Double> clDouble =  new ArrayList<Double>();
					for(int j=0; j<clAirfoilDistribution.size(); j++){
						clDouble.add((Double.valueOf(clAirfoilDistribution.get(j))));
					}
						input.getClAirfoils().add(i, clDouble);	
				}
				
				for (int k=0; k<input.getNumberOfSections(); k++){
					int num =k+1;
					if (input.getAlphaAirfoils().get(k).size() != input.getClAirfoils().get(k).size())
					System.err.println("WARNING! the number of cl differs from the number of alpha for airfoil " + num);
				}
					
				
				// Built of interpolated curve---------------------------------------------------------------------
				
				int numberOfPoint = input.getNumberOfPoint2DCurve();
				
				double [] alphaArrayCompleteCurve= null;
				alphaArrayCompleteCurve = MyArrayUtils.linspace(-6, 25, 32);
				for (int j=0; j<alphaArrayCompleteCurve.length; j++){	
				input.getAlphaArrayCompleteCurveAirfoil().add(j,alphaArrayCompleteCurve[j]);
				}
				
				System.out.println(" alpha array " + input.getAlphaArrayCompleteCurveAirfoil().size() );
						
				for ( int i=0; i<input.getNumberOfSections(); i++){
					
					double appNegValues = 0.0;
					
					for (int l=0; l<input.getAlphaAirfoils().get(i).size(); l++){
						if (input.getClAirfoils().get(i).get(l)<0){
							appNegValues=input.getClAirfoils().get(i).get(l);
						}
					}
					
					double alphaInitialAirfoilArray, alphaFinalAirfoilArray;
					double [] clArrayCompleteCurve = null;
					double clAlphaTemp = 0;
					
					clAlphaTemp = (input.getClAirfoils().get(i).get(1)-input.getClAirfoils().get(i).get(0))/
							(input.getAlphaAirfoils().get(i).get(1).getEstimatedValue()-input.getAlphaAirfoils().get(i).get(0).getEstimatedValue());
					
					if (appNegValues==0.0){
						
						double alphaZeroLiftTemp = (clAlphaTemp*input.getAlphaAirfoils().get(i).get(0).getEstimatedValue()-input.getClAirfoils().get(i).get(0))/clAlphaTemp;
						
						input.getAlphaAirfoils().get(i).add(0, Amount.valueOf(alphaZeroLiftTemp, NonSI.DEGREE_ANGLE));
						input.getClAirfoils().get(i).add(0, 0.0);
						
					}
				
					// INTERPOLATED VALUE ARRAYS
					
					System.out.println("\n cl airfoil " + Arrays.toString(MyArrayUtils.convertListTodoubleArray(input.getClAirfoils().get(i))));
					
					clArrayCompleteCurve = MyArrayUtils.convertToDoublePrimitive(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(input.getAlphaAirfoils().get(i)), 
									MyArrayUtils.convertListTodoubleArray(input.getClAirfoils().get(i)), alphaArrayCompleteCurve));
					
					input.getClArrayCompleteCurveAirfoil().add(i,clArrayCompleteCurve);
					
					System.out.println(" alpha array " + Arrays.toString(MyArrayUtils.convertListOfAmountodoubleArrayAngle(input.getAlphaAirfoils().get(i))));
					System.out.println(" alpha new " + Arrays.toString(alphaArrayCompleteCurve));
					System.out.println(" cl array  " + Arrays.toString(clArrayCompleteCurve));
				    // find alpha zero lit, alpha star, cl max
					
					// cl max
					
					input.getMaximumliftCoefficientDistribution().add(i, MyArrayUtils.getMax(clArrayCompleteCurve));
					
					// alpha zero lift
					double numTemp = clArrayCompleteCurve[0];
					int pos = 0;
					for (int ii=0; ii<clArrayCompleteCurve.length; ii++){
						if(Math.abs(clArrayCompleteCurve[ii])<Math.abs(numTemp)){
							numTemp = clArrayCompleteCurve[ii];
							pos = ii;		
					}}
					
					input.getAlphaZeroLiftDistribution().add(i, Amount.valueOf(alphaArrayCompleteCurve[pos], NonSI.DEGREE_ANGLE));
				
					// alpha star 
					  // cl0
					  double numTempAlpha = alphaArrayCompleteCurve[0];
					  int position = 0;
					  for (int iii=0; iii<alphaArrayCompleteCurve.length; iii++){
						  if(Math.abs(alphaArrayCompleteCurve[iii])<Math.abs(numTempAlpha)){
							  numTempAlpha = alphaArrayCompleteCurve[iii];
							  position = iii;		
					  }}
					  System.out.println(" cl array " + Arrays.toString(clArrayCompleteCurve));
					  System.out.println(" position " + position);
					  double clZeroArray = clArrayCompleteCurve[position];
					  System.out.println(" alpha zero " + numTempAlpha);
					  System.out.println(" cl zero " + clZeroArray);
					  System.out.println(" position " + position);
					  				  
//					  double clAlphaArray = (clArray[pos + 1]- clArray[pos])/(alphaArray[pos+1] - alphaArray[pos]);
					  double clAlphaArray = clAlphaTemp;
					  
					  System.out.println(" cl alpha " + clAlphaArray);
					  System.out.println(" pos " + pos);
					  int posStar = position;
					  
					  double clLinear;
					  
					  for (int j=position; j<clArrayCompleteCurve.length; j++){
						  clLinear = clAlphaArray*alphaArrayCompleteCurve[j] + clZeroArray;
						  if( Math.abs(clArrayCompleteCurve[j] - clLinear) < 0.01){
							 posStar = posStar+1;	 
						  }
					  }					
					  input.getAlphaStarDistribution().add(i, Amount.valueOf(alphaArrayCompleteCurve[posStar], NonSI.DEGREE_ANGLE));
					
				}
				
//					System.out.println(" cl airfoil 1" + input.getClAirfoils().get(0).toString());
//					System.out.println(" alpha airfoil 1" + input.getAlphaAirfoils().get(0).toString());
//					System.out.println(" cl airfoil 2" + input.getClAirfoils().get(1).toString());
//					System.out.println(" alpha airfoil 2" + input.getAlphaAirfoils().get(1).toString());
//					System.out.println(" cl airfoil 3" + input.getClAirfoils().get(2).toString());
//					System.out.println(" alpha airfoil 3" + input.getAlphaAirfoils().get(2).toString());
										
			}
			
			else if(inputMethod.isEmpty() || !inputMethod.equalsIgnoreCase("MAIN_POINTS") || !inputMethod.equalsIgnoreCase("COMPLETE_CURVE")) {
				System.err.println("No valid input! wing_executable method_input = MAIN_POINTS or COMPLETE_CURVE");
			}
			
//----------------------------------------------------------------------------------------------------------------------------------------------------------//
//                                                                                                                                                          //   
//                                                                                                                                                          //
//----------------------------------------------------------------------------------------------------------------------------------------------------------//
			
	// Bidimensional airfoil curves as matrix
	//
	//  --------------------------> number of point semi span
	//  |   |
	//  | c |
	//  | l	|
	//	|	|
	//	|
	// \/
	// number of point 2d curve (100)

			double [] clLocalCurve = new double [input.getNumberOfPoint2DCurve()];
			
			double [] clInputStation = new double [input.getyAdimensionalStationInput().size()];


			for (int i=0; i<input.getNumberOfPointSemispan(); i++){
				for (int j=0; j<input.getNumberOfPoint2DCurve(); j++){

					for (int k=0; k<input.getyAdimensionalStationInput().size(); k++)	{
						System.out.println(" alpha array " + input.getAlphaArrayCompleteCurveAirfoil().size());
						System.out.println(" cl array " + input.getClArrayCompleteCurveAirfoil().get(k).length);
						clInputStation[k] = MyMathUtils.getInterpolatedValue1DLinear(
								MyArrayUtils.convertToDoublePrimitive(input.getAlphaArrayCompleteCurveAirfoil()),
								input.getClArrayCompleteCurveAirfoil().get(k), 
								input.getAlphaArrayCompleteCurveAirfoil().get(j));
					}

					clLocalCurve[j] = MyMathUtils.getInterpolatedValue1DLinear(
							clInputStation, 
							MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()) ,
							input.getyStationsAdimensional()[i]
							);

					input.getClAirfoilMatrix()[j][i] = clLocalCurve[j];
				}

				clLocalCurve = new double [input.getNumberOfPoint2DCurve()];
			}

			

			
			
	// OTHER VALUES
		double span = Math.sqrt(input.getAspectRatio() * input.getSurface().getEstimatedValue());
		input.setSpan(Amount.valueOf(span, SI.METER));
		input.setSemiSpan(Amount.valueOf(span/2, SI.METER));
		
	// delta alpha	
		
		double tgAngle =input.getxLEDistribution().get(input.getNumberOfSections()-1).getEstimatedValue()/input.getSemiSpan().getEstimatedValue();
		double sweepLE =Math.toDegrees(Math.atan(tgAngle));
		double deltaAlpha = aeroDatabaseReader.getD_Alpha_Vs_LambdaLE_VsDy(sweepLE,sharpnessParameterLE);
		
		input.setDeltaAlpha(deltaAlpha);
		
		
//	 PRINT
//		
		System.out.println("\n\nINPUT DATA\n\n");
		
		System.out.println("Operating Conditions");
		System.out.println("-------------------------------------");
		System.out.println("Altitude : " + input.getAltitude().getEstimatedValue()+ " " + input.getAltitude().getUnit());
		System.out.println("Mach Number : " + input.getMachNumber());
		
		System.out.println("\nAlpha Values");
		System.out.println("-------------------------------------");
		System.out.println("Number of Alpha : " + input.getNumberOfAlpha());
		System.out.println("Alpha Initial : " + input.getAlphaInitial().getEstimatedValue()+ " " + input.getAlphaInitial().getUnit());
		System.out.println("Alpha Final : " + input.getAlphaFinal().getEstimatedValue()+ " " + input.getAlphaFinal().getUnit());
		
		System.out.println("\nWing");
		System.out.println("-------------------------------------");
		System.out.println("Surface : " + input.getSurface().getEstimatedValue()+ " " + input.getSurface().getUnit());
		System.out.println("Aspect Ratio : " + input.getAspectRatio());
		System.out.println("Number of point along semi-span : " + input.getNumberOfPointSemispan());
		System.out.println("Adimensional kink station : " + input.getAdimensionalKinkStation());
		System.out.println("Span : " + input.getSpan().getEstimatedValue()+ " " + input.getSpan().getUnit());
		System.out.println("\nDistribution");
		System.out.println("-------------------------------------");
		System.out.println("Number of given stations : " + input.getNumberOfSections());
		
		System.out.println("\nMean airoil type : " + input.getMeanAirfoilFamily());
        System.out.println("Mean airfoil thickness : " + input.getMeanThickness());
		
		System.out.print("\nChord distribution : [");
		for(int i=0; i<input.getChordDistribution().size(); i++)
			System.out.print("  " +input.getChordDistribution().get(i).getEstimatedValue() + "  ");
			System.out.println("] " + input.getChordDistribution().get(0).getUnit() );
		
		System.out.print("X LE distribution : [");
		for(int i=0; i<input.getxLEDistribution().size(); i++)
			System.out.print("  " +input.getxLEDistribution().get(i).getEstimatedValue()+ " ");
		    System.out.println("] " + input.getxLEDistribution().get(0).getUnit() );
		
		System.out.print("Twist distribution : [");
		for(int i=0; i<input.getTwistDistribution().size(); i++)
			System.out.print("  " +input.getTwistDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getTwistDistribution().get(0).getUnit() );
			
		System.out.print("Dihedral distribution : [");
		for(int i=0; i<input.getDihedralDistribution().size(); i++)
			System.out.print("  " +input.getDihedralDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getDihedralDistribution().get(0).getUnit() );
	
		System.out.print("Alpha zero lift distribution : [");
		for(int i=0; i<input.getAlphaZeroLiftDistribution().size(); i++)
			System.out.print("  " +input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getAlphaZeroLiftDistribution().get(0).getUnit() );
			
		System.out.print("Alpha Star distribution: [");
		for(int i=0; i<input.getAlphaStarDistribution().size(); i++)
			System.out.print("  " +input.getAlphaStarDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getAlphaStarDistribution().get(0).getUnit() );	
			
		System.out.print("Cl max distribution : ");
			System.out.print(input.getMaximumliftCoefficientDistribution());
			
			System.out.println("\n AIRFOIL COMPLETE CURVE ");
			for(int i=0; i<input.getNumberOfSections(); i++)
				System.out.print(Arrays.toString(input.getClArrayCompleteCurveAirfoil().get(i)) + "\n");
			
			System.out.println("\n ALPHAS");
			System.out.print((input.getAlphaArrayCompleteCurveAirfoil()) + "\n");
			
			
//		System.out.print("Alpha Stall distribution: [");
//			for(int i=0; i<input.getAlphaStallDistribution().size(); i++)
//				System.out.print("  " +input.getAlphaStallDistribution().get(i).getEstimatedValue()+ " ");
//				System.out.println("] " + input.getAlphaStallDistribution().get(0).getUnit() );	
					
				
	}
	public static void writeToXML(String filenameWithPathAndExt, InputOutputTree input) {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			defineXmlTree(doc, docBuilder, input);
			
			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void defineXmlTree(Document doc, DocumentBuilder docBuilder, InputOutputTree input) {
		
		org.w3c.dom.Element rootElement = doc.createElement("Wing_aerodynamic_executable");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
		rootElement.appendChild(inputRootElement);

		org.w3c.dom.Element flightConditionsElement = doc.createElement("operating_conditions");
		inputRootElement.appendChild(flightConditionsElement);

		JPADStaticWriteUtils.writeSingleNode("altitude", input.getAltitude(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("mach_number", input.getMachNumber(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("number_of_alpha", input.getNumberOfAlpha(), flightConditionsElement, doc);
		if (input.getNumberOfAlpha()!=0){
		JPADStaticWriteUtils.writeSingleNode("alpha_initial", input.getAlphaInitial(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_final", input.getAlphaFinal(), flightConditionsElement, doc);
		}
				
		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
		inputRootElement.appendChild(wingDataElement);
		
		org.w3c.dom.Element geometryDataElement = doc.createElement("global");
		wingDataElement.appendChild(geometryDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("surface", input.getSurface(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("number_of_point_semispan", input.getNumberOfPointSemispan(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("adimensional_kink_station", input.getAdimensionalKinkStation(), geometryDataElement, doc);
		
		
		org.w3c.dom.Element cleanConfigurationDataElement = doc.createElement("distibution");
		wingDataElement.appendChild(cleanConfigurationDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("number_of_given_sections", input.getNumberOfSections(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("airfoil_family", input.getMeanAirfoilFamily(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("max_thickness_mean_airfoil", input.getMeanThickness(), cleanConfigurationDataElement, doc);
		
		org.w3c.dom.Element childDistribution = doc.createElement("geometry");
		cleanConfigurationDataElement.appendChild(childDistribution);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("y_adimensional_stations", input.getyAdimensionalStationInput(),childDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("chord_distribution", input.getChordDistribution(), childDistribution, doc, input.getChordDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("x_le_distribution", input.getxLEDistribution(), childDistribution, doc, input.getxLEDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("twist_distribution", input.getTwistDistribution(), childDistribution, doc, input.getTwistDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("dihedral_distribution", input.getDihedralDistribution(), childDistribution, doc,  input.getDihedralDistribution().get(0).getUnit().toString());
		
		org.w3c.dom.Element childDistributionNew = doc.createElement("aerodynamics");
		cleanConfigurationDataElement.appendChild(childDistributionNew);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_zero_lift_distribution", input.getAlphaZeroLiftDistribution(), childDistributionNew, doc,  input.getAlphaZeroLiftDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_star_distribution", input.getAlphaStarDistribution(), childDistributionNew, doc,  input.getAlphaStarDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("maximum_lift_coefficient_distribution", input.getMaximumliftCoefficientDistribution(), childDistributionNew, doc);
		
		//--------------------------------------------------------------------------------------
		// OUTPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element outputRootElement = doc.createElement("OUTPUT");
		rootElement.appendChild(outputRootElement);
		
		org.w3c.dom.Element highLiftDevicesEffectsElement = doc.createElement("wing_aerodynamic_characteristics");
		outputRootElement.appendChild(highLiftDevicesEffectsElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_zero_lift", input.getAlphaZeroLift(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha", Amount.valueOf(Double.valueOf(input.getClAlpha()), NonSI.DEGREE_ANGLE.inverse()), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_star", input.getClStar(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star", input.getAlphaStar(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_max", input.getClMax(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_stall", input.getAlphaStall(), highLiftDevicesEffectsElement, doc);
		
		org.w3c.dom.Element highLiftGlobalDataElement = doc.createElement("cL_vs_alpha_curve");
		outputRootElement.appendChild(highLiftGlobalDataElement);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cL_array",input.getcLVsAlphaVector(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_array", input.getAlphaVector(), highLiftGlobalDataElement, doc, "°");
	
		
		org.w3c.dom.Element highLiftCurveDataElement = doc.createElement("distribution");
		outputRootElement.appendChild(highLiftCurveDataElement);
		double [] yAd = MyArrayUtils.linspace(0, 1, input.getNumberOfPointSemispan());
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("eta",yAd, highLiftCurveDataElement, doc);
		
		if (input.getNumberOfAlpha()!=0){
		for (int i=0; i<input.getNumberOfAlpha(); i++){
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_at_alpha_" + input.getAlphaDistributionArray()[i], input.getClVsEtaVectors().get(i), highLiftCurveDataElement, doc);
		}
		}
	}


}
