
package sandbox.mr.HighLiftStallPath;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import com.sun.jna.platform.win32.WinUser.INPUT;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.NasaBlackwell;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.DatabaseManager;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class HighLiftStallPathCalc {



	public static void calculateAll(InputOutputTree input,
			String databaseFolderPath,
			String aerodynamicDatabaseFileName,
			String highLiftDatabaseFileName
			) throws InstantiationException, IllegalAccessException{

		// Set databases

		//------------------------------------------------------------------------------------
		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(new AerodynamicDatabaseReader(
				databaseFolderPath,	aerodynamicDatabaseFileName),
				databaseFolderPath);

		HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(new HighLiftDatabaseReader(
				databaseFolderPath, highLiftDatabaseFileName),
				databaseFolderPath);

		// Managing flaps types:
		List<Double> flapTypeIndex = new ArrayList<Double>();
		List<Double> deltaFlapRef = new ArrayList<Double>();


		for(int i=0; i<input.getFlapType().size(); i++) {
			if(input.getFlapType().get(i) == FlapTypeEnum.SINGLE_SLOTTED) {
				flapTypeIndex.add(1.0);
				deltaFlapRef.add(45.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.DOUBLE_SLOTTED) {
				flapTypeIndex.add(2.0);
				deltaFlapRef.add(50.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.PLAIN) {
				flapTypeIndex.add(3.0);
				deltaFlapRef.add(60.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.FOWLER) {
				flapTypeIndex.add(4.0);
				deltaFlapRef.add(40.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.TRIPLE_SLOTTED) {
				flapTypeIndex.add(5.0);
				deltaFlapRef.add(50.0);
			}
		}

		// Calculate mean values 
		
		
		// Define input vector 

		int numberOfPoint = input.getNumberOfPointSemispan();
		double [] yStationInput = new double [input.getNumberOfSections()];
		double [] yStationDimensional = new double [numberOfPoint];


		for (int i=0; i<yStationInput.length; i++){
			yStationInput [i] = input.getyAdimensionalStationInput().get(i);
		}

		//input

		//		MyArray chordInput = new MyArray(input.getNumberOfSections());
		//		MyArray xleInput = new MyArray(input.getNumberOfSections());
		//		MyArray dihedralInput = new MyArray(input.getNumberOfSections());
		//		MyArray twistInput = new MyArray(input.getNumberOfSections());
		//		MyArray alphaStarInput = new MyArray(input.getNumberOfSections());
		//		MyArray alpha0lInput = new MyArray(input.getNumberOfSections());
		//		MyArray clMaxInput = new MyArray(input.getNumberOfSections());


		double [] chordInput = new double [input.getNumberOfSections()];
		double [] xleInput = new double [input.getNumberOfSections()];
		double [] leadingEdgeRadiusInput = new double [input.getNumberOfSections()];
		double [] dihedralInput = new double [input.getNumberOfSections()];
		double [] twistInput = new double [input.getNumberOfSections()];
		double [] maximumTicknessInput = new double [input.getNumberOfSections()];
		double [] alphaStarInput = new double [input.getNumberOfSections()];
		double [] alpha0lInput = new double [input.getNumberOfSections()];
		double [] clMaxInput = new double [input.getNumberOfSections()];
		double [] clAlphaInput = new double [input.getNumberOfSections()];
		double [] clZeroInput = new double [input.getNumberOfSections()];

		double [] etaInFlap = new double [input.getFlapsNumber()];
		double [] etaOutFlap = new double [input.getFlapsNumber()];
		double [] etaInSlat = new double [input.getSlatsNumber()];
		double [] etaOutSlat = new double [input.getSlatsNumber()];

		for (int i =0; i<input.getNumberOfSections(); i++){
			chordInput[i] = input.getChordDistribution().get(i).getEstimatedValue();
			xleInput[i] = input.getxLEDistribution().get(i).getEstimatedValue();
			leadingEdgeRadiusInput [i] = input.getLeadingEdgeRdiusDistribution().get(i).getEstimatedValue();
			dihedralInput[i] = Math.toRadians(input.getDihedralDistribution().get(i).getEstimatedValue());
			maximumTicknessInput[i] = input.getMaximumTickness().get(i);
			twistInput[i] = Math.toRadians(input.getTwistDistribution().get(i).getEstimatedValue());
			alphaStarInput[i] = Math.toRadians(input.getAlphaStarDistribution().get(i).getEstimatedValue());
			alpha0lInput[i] = Math.toRadians(input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue());
			clMaxInput[i] = input.getMaximumliftCoefficientDistribution().get(i);
			clAlphaInput[i] = input.getClalphaDistribution().get(i);
			clZeroInput[i] = input.getClZeroDistribution().get(i);
		}

		for (int i=0; i<input.getFlapsNumber(); i++){
			etaInFlap[i] = input.getEtaInFlap().get(i);
			etaOutFlap [i] = input.getEtaOutFlap().get(i);}

		for (int i=0; i<input.getSlatsNumber(); i++){
			etaInSlat [i] = input.getEtaInSlat().get(i);
			etaOutSlat [i] = input.getEtaOutSlat().get(i);
		}

		//		chordInput = new MyArray(chordInputDouble);
		//		xleInput = new MyArray(xleInputDouble);
		//		dihedralInput = new MyArray(dihedralInputDouble);
		//		twistInput = new MyArray(twistInputDouble);
		//		alphaStarInput = new MyArray(alphaStarInputDouble);
		//		alpha0lInput = new MyArray(alpha0lInputDouble);
		//		clMaxInput = new MyArray(clMaxInputDouble);
		//		


		// Define new exended vectors

		// y Stations

		//define y station actual

		int numberOfElementYStationActual = etaInFlap.length + etaOutFlap.length + etaInSlat.length + etaOutSlat.length +2;
		int n = 0;
		double [] yStationActual = new double [numberOfElementYStationActual];
		System.out.println(Arrays.toString(yStationActual));

		double [] innerOuterWing = {0,1};

		System.arraycopy(innerOuterWing, 0, yStationActual, 0, innerOuterWing.length);
		System.out.println(Arrays.toString(yStationActual));

		n=0+innerOuterWing.length;

		System.arraycopy(etaInFlap, 0, yStationActual, n, etaInFlap.length);
		System.out.println(Arrays.toString(yStationActual));

		n=n + etaInFlap.length;

		System.arraycopy(etaOutFlap, 0, yStationActual, n, etaOutFlap.length);
		System.out.println(Arrays.toString(yStationActual));

		n=n + etaOutFlap.length;

		System.arraycopy(etaInSlat, 0, yStationActual, n, etaInSlat.length);
		System.out.println(Arrays.toString(yStationActual));

		n = n + etaInSlat.length;

		System.arraycopy(etaOutSlat, 0, yStationActual, n, etaOutSlat.length);
		System.out.println(Arrays.toString(yStationActual));

		Arrays.sort(yStationActual);

		System.out.println("ystat" + Arrays.toString(yStationActual));

		System.out.println("\n FLAP ");


		// define yStation Actual Flap and slat

		int numberOfElementYStationActualFlap = etaInFlap.length + etaOutFlap.length +2;
		int m = 0;
		double [] yStationActualFlap = new double [numberOfElementYStationActualFlap];
		System.out.println(Arrays.toString(yStationActualFlap));


		System.arraycopy(innerOuterWing, 0, yStationActualFlap, 0, innerOuterWing.length);
		System.out.println(Arrays.toString(yStationActualFlap));

		m=0+innerOuterWing.length;

		System.arraycopy(etaInFlap, 0, yStationActualFlap, m, etaInFlap.length);
		System.out.println(Arrays.toString(yStationActualFlap));

		m=m + etaInFlap.length;

		System.arraycopy(etaOutFlap, 0, yStationActualFlap, m, etaOutFlap.length);
		System.out.println(Arrays.toString(yStationActualFlap));

		Arrays.sort(yStationActualFlap);

		System.out.println("ysta Flap" + Arrays.toString(yStationActualFlap));


		System.out.println("\n SLAT ");

		int numberOfElementYStationActualSlat =etaInSlat.length + etaOutSlat.length +2;
		int k = 0;
		double [] yStationActualSlat = new double [numberOfElementYStationActualSlat];
		System.out.println(Arrays.toString(yStationActualSlat));

		System.arraycopy(innerOuterWing, 0, yStationActualSlat, 0, innerOuterWing.length);
		System.out.println(Arrays.toString(yStationActualSlat));

		k=0+innerOuterWing.length;

		System.arraycopy(etaInSlat, 0, yStationActualSlat, k, etaInSlat.length);
		System.out.println(Arrays.toString(yStationActualSlat));

		k=k + etaInSlat.length;

		System.arraycopy(etaOutSlat, 0, yStationActualSlat, k, etaOutSlat.length);
		System.out.println(Arrays.toString(yStationActualSlat));

		Arrays.sort(yStationActualSlat);

		System.out.println("ysta Slat" + Arrays.toString(yStationActualSlat));



		// define yTempFlap and Slat

		List<Double> yTempFlap = new ArrayList<Double>();
		double deltaValue = 0.01;

		for ( int i =0; i< yStationActualFlap.length-1 ; i++) {
			if ( (yStationActualFlap[i+1] - yStationActualFlap[i]) > 0.03 ) {

				if ( yStationActualFlap[i] == 0){
					yTempFlap.add(yStationActualFlap[i+1]-deltaValue);			}

				if (yStationActualFlap[i+1] ==1){
					yTempFlap.add(yStationActualFlap[i] + deltaValue);		}

				if (yStationActualFlap[i] != 0 & yStationActualFlap[i+1] !=1){
					//					if (Arrays.asList(etaOutFlap).contains(yStationActualFlap[i+1]) &  Arrays.asList(etaInFlap).contains(yStationActualFlap[i])){}
					if (Arrays.binarySearch(etaOutFlap,yStationActualFlap[i+1])>=0 &  Arrays.binarySearch(etaInFlap,yStationActualFlap[i])>=0){}
					else{
						yTempFlap.add(yStationActualFlap[i] + deltaValue);
						yTempFlap.add(yStationActualFlap[i+1] - deltaValue);
					}
				}
			}
		}

		double [] yTempArrayFlap = new double  [yTempFlap.size()];
		for (int i=0; i<yTempArrayFlap.length; i++)
			yTempArrayFlap[i] = yTempFlap.get(i);

		System.out.println("ytemp Flap" +  Arrays.toString(yTempArrayFlap));


		List<Double> yTempSlat = new ArrayList<Double>();

		for ( int i =0; i< yStationActualSlat.length-1 ; i++) {
			if ( (yStationActualSlat[i+1] - yStationActualSlat[i]) > 0.03 ) {

				if ( yStationActualSlat[i] == 0){
					yTempSlat.add(yStationActualSlat[i+1]- deltaValue);			}

				if (yStationActualSlat[i+1] ==1){
					yTempSlat.add(yStationActualSlat[i] + deltaValue);		}

				if ( yStationActualSlat[i] != 0 & yStationActualSlat[i+1] !=1){
					if (Arrays.binarySearch(etaOutSlat,yStationActualSlat[i+1])>=0 &  Arrays.binarySearch(etaInSlat,yStationActualSlat[i])>=0){}
					else{
						yTempSlat.add(yStationActualSlat[i] + deltaValue);
						yTempSlat.add(yStationActualSlat[i+1] - deltaValue);
					}
				}
			}
		}

		double [] yTempArraySlat = new double  [yTempSlat.size()];
		for (int i=0; i<yTempArraySlat.length; i++)
			yTempArraySlat[i] = yTempSlat.get(i);

		System.out.println("ytemp Slat" +  Arrays.toString(yTempArraySlat));


		// yStation total flap e slat 

		double [] yStationTotalFlap = new double [yStationActualFlap.length + yTempArrayFlap.length];

		System.out.println(Arrays.toString(yStationTotalFlap));
		System.arraycopy(yStationActualFlap, 0, yStationTotalFlap, 0, yStationActualFlap.length);
		System.out.println(Arrays.toString(yStationTotalFlap));

		k=0+yStationActualFlap.length;

		System.arraycopy(yTempArrayFlap, 0, yStationTotalFlap, k, yTempArrayFlap.length);
		System.out.println(Arrays.toString(yStationTotalFlap));

		Arrays.sort(yStationTotalFlap);
		for (int i =0; i<yStationTotalFlap.length-1; i++){
			if (yStationTotalFlap[i+1] == yStationTotalFlap[i]){
				yStationTotalFlap[i+1] = yStationTotalFlap[i+1]+0.001;
			}
		}
		System.out.println("y station Flap with add" + Arrays.toString(yStationTotalFlap));

		//slat
		double [] yStationTotalSlat = new double [yStationActualSlat.length + yTempArraySlat.length];

		System.out.println(Arrays.toString(yStationTotalSlat));
		System.arraycopy(yStationActualSlat, 0, yStationTotalSlat, 0, yStationActualSlat.length);
		System.out.println(Arrays.toString(yStationTotalSlat));

		k=0+yStationActualSlat.length;

		System.arraycopy(yTempArraySlat, 0, yStationTotalSlat, k, yTempArraySlat.length);
		System.out.println(Arrays.toString(yStationTotalSlat));

		Arrays.sort(yStationTotalSlat);

		for (int i =0; i<yStationTotalSlat.length-1; i++){
			if (yStationTotalSlat[i+1] == yStationTotalSlat[i]){
				yStationTotalSlat[i+1] = yStationTotalSlat[i+1]+0.001;
			}
		}
		System.out.println("y station Slat with add" + Arrays.toString(yStationTotalSlat));

		// y Station total

		int numberOfElementYStationTotal = yStationActual.length + yTempArraySlat.length + yTempArrayFlap.length;
		int j = 0;
		double [] yStationTotal = new double [numberOfElementYStationTotal];
		System.out.println(Arrays.toString(yStationTotal));


		System.arraycopy(yStationActual, 0, yStationTotal, 0, yStationActual.length);
		System.out.println(Arrays.toString(yStationTotal));

		j=0+yStationActual.length;

		System.arraycopy(yTempArrayFlap, 0,  yStationTotal, j, yTempArrayFlap.length);
		System.out.println(Arrays.toString(yStationTotal));

		j=j + yTempArrayFlap.length;

		System.arraycopy(yTempArraySlat, 0,  yStationTotal, j, yTempArraySlat.length);
		System.out.println(Arrays.toString(yStationTotal));

		Arrays.sort(yStationTotal);

		for (int i=0; i<yStationTotal.length-3; i++){
			if ( yStationTotal[i] == yStationTotal[i+1] &  yStationTotal[i+1] == yStationTotal[i+2] & yStationTotal[i+2] == yStationTotal[i+3]){
				yStationTotal[i+3] = yStationTotal[i+1] + 0.003;
				yStationTotal[i+2] = yStationTotal[i+1] + 0.002;
				yStationTotal[i+1] = yStationTotal[i+1] + 0.001;
			}
			if ( yStationTotal[i] == yStationTotal[i+1] &  yStationTotal[i+1] == yStationTotal[i+2]){
				yStationTotal[i+2] = yStationTotal[i+1] + 0.002;
				yStationTotal[i+1] = yStationTotal[i+1] + 0.001;
			}
		if ( yStationTotal[i] == yStationTotal[i+1]){
			yStationTotal[i+1] = yStationTotal[i+1] + 0.001;
		}
		}
		System.out.println("Y station Total" + Arrays.toString(yStationTotal));

	// mean values
		
		double [] clAlphaMeanFlap = new double [input.getFlapsNumber()];
		double [] clZeroMeanFlap = new double [input.getFlapsNumber()];
		double [] maxTicknessMeanFlap = new double [input.getFlapsNumber()];
		double [] maxTicknessMeanSlat = new double [input.getSlatsNumber()];
		double [] maxTicknessFlapStations = new double [2*input.getFlapsNumber()];
		double [] clAlphaFlapStations = new double [2*input.getFlapsNumber()];
		double [] clZeroFlapStations = new double [2*input.getFlapsNumber()];
		double [] leRadiusMeanSlat = new double [input.getSlatsNumber()];
		double [] chordMeanSlat = new double [input.getSlatsNumber()];
		double [] leadingEdgeRadiusSlatStations = new double [2*input.getSlatsNumber()];
		double [] maxTicknessSlatStations = new double [2*input.getSlatsNumber()];
		double [] chordSlatStations = new double [2*input.getSlatsNumber()];

		double [] influenceFactor = new double [2];
		
		for ( int i=0; i< input.getFlapsNumber(); i++){
			int kk = i*2;
			
			System.out.println(" ystat " + Arrays.toString(MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput())));
			System.out.println(" cl alpha " + Arrays.toString(MyArrayUtils.convertToDoublePrimitive(input.getClalphaDistribution())));
			
			System.out.println(" y stat " + Arrays.toString(MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput())));
			
			System.out.println(" cl zero " + Arrays.toString(MyArrayUtils.convertToDoublePrimitive(input.getClZeroDistribution()) ));
			
			clAlphaFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClalphaDistribution()),
					etaOutFlap[i]);
			
			clAlphaFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClalphaDistribution()),
					etaInFlap[i]);
			
			clZeroFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClZeroDistribution()),
					etaInFlap[i]);
			
			clZeroFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClZeroDistribution()),
					etaOutFlap[i]);
			
			maxTicknessFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getMaximumTickness()),
					etaInFlap[i]);
			
			maxTicknessFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getMaximumTickness()),
					etaOutFlap[i]);
			
			influenceFactor = MeanAirfoilCalc.meanAirfoilFlap(etaInFlap[i], etaOutFlap[i], input);
			
			clAlphaMeanFlap[i] = clAlphaFlapStations[kk] * influenceFactor[0] + clAlphaFlapStations[kk+1]*influenceFactor[1];
			clZeroMeanFlap[i] = clZeroFlapStations[kk] * influenceFactor[0] + clZeroFlapStations[kk+1]*influenceFactor[1];
			maxTicknessMeanFlap[i] = maxTicknessFlapStations[kk]* influenceFactor[0] + maxTicknessFlapStations[kk+1]*influenceFactor[1];
		}
		System.out.println(" CL ALPHA FLAP STATIONS " + Arrays.toString(clAlphaMeanFlap));
		System.out.println(" CL ZERO MEAN FLAP STATIONS " + Arrays.toString(clZeroMeanFlap));
		System.out.println(" MAX TICKNESS MEAN FLAP STATIONS " + Arrays.toString(maxTicknessMeanFlap));
		
		for ( int i=0; i< input.getSlatsNumber(); i++){
			int kk = i*2;
			
		
						
			leadingEdgeRadiusSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertListOfAmountodoubleArray(input.getLeadingEdgeRdiusDistribution()),
					etaOutSlat[i]);
			
			leadingEdgeRadiusSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertListOfAmountodoubleArray(input.getLeadingEdgeRdiusDistribution()),
					etaInSlat[i]);
			
			chordSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertListOfAmountodoubleArray(input.getChordDistribution()),
					etaInSlat[i]);
			
			chordSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertListOfAmountodoubleArray(input.getChordDistribution()),
					etaOutSlat[i]);


			maxTicknessSlatStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getMaximumTickness()),
					etaInSlat[i]);
			
			maxTicknessSlatStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getMaximumTickness()),
					etaOutSlat[i]);
			
			influenceFactor = MeanAirfoilCalc.meanAirfoilFlap(etaInSlat[i], etaOutSlat[i], input);
			
			leRadiusMeanSlat[i] = leadingEdgeRadiusSlatStations[kk] * influenceFactor[0] + leadingEdgeRadiusSlatStations[kk+1]*influenceFactor[1];
			chordMeanSlat[i] = chordSlatStations[kk] * influenceFactor[0] + chordSlatStations[kk+1]*influenceFactor[1];
			maxTicknessMeanSlat[i] = maxTicknessSlatStations[kk] * influenceFactor[0] + maxTicknessSlatStations[kk+1]*influenceFactor[1];
			
		}

		System.out.println(" LE RADIUS MEAN SLAT STATIONS " + Arrays.toString(leRadiusMeanSlat));
		System.out.println(" CHORD MEAN SLAT STATIONS " + Arrays.toString(chordMeanSlat));
		System.out.println(" MAX TICKNESS MEAN SLAT STATIONS " + Arrays.toString(maxTicknessMeanSlat));
		// chords
		// Base

		System.out.println("\n---------------------------");
		System.out.println("          CHORD            ");
		System.out.println("---------------------------\n");
		
		Double[] chordDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, chordInput, yStationTotal);

		System.out.println("Chord distribution base :" + Arrays.toString(chordDistributionBase));


		// Delta flap

		List<Double> deltaCCfFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCCfFlap.add(
					highLiftDatabaseReader
					.getDeltaCCfVsDeltaFlap(input.getDeltaFlap().get(i).getEstimatedValue(), flapTypeIndex.get(i))
					);

		List<Double> cFirstCFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			cFirstCFlap.add(1+(deltaCCfFlap.get(i).doubleValue()*input.getCfc().get(i).doubleValue()));

		int pos;

		Double [] deltaChordFlap = new Double [yStationActualFlap.length];
		for (int i=0; i<deltaChordFlap.length; i++){
			deltaChordFlap[i] = 0.0;
		}

		for (int i=1; i<input.getFlapsNumber()+1; i++) {
			pos=i*2;
			deltaChordFlap[pos] = cFirstCFlap.get(i-1)-1;
			deltaChordFlap[pos-1] = cFirstCFlap.get(i-1)-1;
		}

		System.out.println(" delta c due to flap with no separation" + Arrays.toString(deltaChordFlap));

		ArrayList<Double> deltaChordFlapAsList  = new ArrayList<Double>(Arrays.asList(deltaChordFlap));
		ArrayList<Double> deltaChordFlapWithSeparator  = new ArrayList<Double>(Arrays.asList(deltaChordFlap));

		deltaChordFlapWithSeparator = createCompleteArray(input, deltaChordFlapAsList, yStationActualFlap, etaInFlap, etaOutFlap);
		
		System.out.println(" delta c due to flap with separation" + deltaChordFlapWithSeparator.toString());



		// Delta Slat

		List<Double> deltaCCfSlat = new ArrayList<Double>();

		for(int i=0; i<input.getSlatsNumber(); i++){
			deltaCCfSlat.add(input.getcExtCSlat().get(i));}

		int posSlat;
		Double [] deltaChordSlat = new Double [yStationActualSlat.length];
		for (int i=0; i<deltaChordSlat.length; i++){
			deltaChordSlat[i] = 0.0;
		}
		for (int i=1; i<input.getSlatsNumber()+1; i++) {
			posSlat=i*2;
			deltaChordSlat[posSlat] = deltaCCfSlat.get(i-1)-1;
			deltaChordSlat[posSlat-1] = deltaCCfSlat.get(i-1)-1;
		}

		System.out.println(" delta c due to slat " + Arrays.toString(deltaChordSlat));


		ArrayList<Double> deltaChordSlatAsList  = new ArrayList<Double>(Arrays.asList(deltaChordSlat));
		ArrayList<Double> deltaChordSlatWithSeparator  = new ArrayList<Double>(Arrays.asList(deltaChordSlat));

		deltaChordSlatWithSeparator = createCompleteArray(input, deltaChordSlatAsList, yStationActualSlat , etaInSlat, etaOutSlat);
		
		System.out.println(" delta c due to slat with separation " + deltaChordSlatWithSeparator.toString());



		// TOTAL

		// interpolation

		double [] deltaChordFlapWithSeparatorArray = new double [deltaChordFlapAsList.size()];
		double [] deltaChordSlatWithSeparatorArray = new double [deltaChordSlatAsList.size()];
		for(int i=0; i<deltaChordFlapWithSeparatorArray.length; i++){
			deltaChordFlapWithSeparatorArray[i] = deltaChordFlapAsList.get(i);
		}
		for(int i=0; i<deltaChordSlatWithSeparatorArray.length; i++){
		deltaChordSlatWithSeparatorArray[i] = deltaChordSlatAsList.get(i);
		}
		
		MyArray deltaChordFlapMyArray = new MyArray(deltaChordFlapWithSeparatorArray);
		MyArray deltaChordFlapTotal = MyArray.createArray(
				deltaChordFlapMyArray.interpolate(
						yStationTotalFlap,
						yStationTotal));
		
		MyArray deltaChordSlatMyArray = new MyArray(deltaChordSlatWithSeparatorArray);
		MyArray deltaChordSlatTotal = MyArray.createArray(
				deltaChordSlatMyArray.interpolate(
						yStationTotalSlat,
						yStationTotal));


		double [] chordDistributionTotal = new double [yStationTotal.length];

		for (int i=0; i<yStationTotal.length; i++){
			chordDistributionTotal [i] = chordDistributionBase[i] + deltaChordFlapTotal.get(i) + deltaChordSlatTotal.get(i);
		}
		
		System.out.println(" chord distribution total " + Arrays.toString(chordDistributionTotal));

		// xle
		
		System.out.println("\n---------------------------");
		System.out.println("           X LE            ");
		System.out.println("---------------------------\n");

		Double[] xLEDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, xleInput, yStationTotal);

		System.out.println("xle distribution base :" + Arrays.toString(xLEDistributionBase));
		
		// The delta in xle is due only of slat. In particular the delta xle is the delta chord due to slat.
		
		double [] deltaXleSlatTotal = new double [yStationTotal.length];
		for (int i = 0 ; i<yStationTotal.length; i++){
			deltaXleSlatTotal[i] =-deltaChordSlatTotal.get(i);
		}
		
		System.out.println(" delta XLE due to slat " + Arrays.toString(deltaXleSlatTotal));
		
		
		double [] xLeDistributionTotal = new double [yStationTotal.length];
		
		for (int i=0; i<yStationTotal.length; i++){
			xLeDistributionTotal [i] = xLEDistributionBase[i] + deltaXleSlatTotal[i];
		}
		
		System.out.println(" X LE distribution total " + Arrays.toString(xLeDistributionTotal));
		

		// alpha zero lift
		
		 //delta cl0 
		
		System.out.println("\n---------------------------");
		System.out.println("          CL0            ");
		System.out.println("---------------------------\n");
		
		Double[] clZeroDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, clZeroInput, yStationTotal);

		System.out.println("Cl zero distribution base :" + Arrays.toString(clZeroDistributionBase));

	
		
		// Delta flap

		List<Double> thetaF = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) 
			thetaF.add(Math.acos((2*input.getCfc().get(i))-1));

		List<Double> alphaDelta = new ArrayList<Double>();
		for(int i=0; i<thetaF.size(); i++)
			alphaDelta.add(1-((thetaF.get(i)-Math.sin(thetaF.get(i)))/Math.PI));
	
		List<Double> etaDeltaFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				etaDeltaFlap.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlapPlain(input.getDeltaFlap().get(i).getEstimatedValue(), input.getCfc().get(i)));
			else
				etaDeltaFlap.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlap(input.getDeltaFlap().get(i).getEstimatedValue(), flapTypeIndex.get(i))
						);
		}
		
		List<Double> deltaCl0First = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCl0First.add(
					alphaDelta.get(i).doubleValue()
					*etaDeltaFlap.get(i).doubleValue()
					*input.getDeltaFlap().get(i).getEstimatedValue()
					*clAlphaMeanFlap[i]
					); 
		
	
		
		List<Double> deltaCl0FlapList = new ArrayList<Double>();
		
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCl0FlapList.add(
					(deltaCl0First.get(i).doubleValue()*cFirstCFlap.get(i).doubleValue())
					+(clZeroMeanFlap[i]*(cFirstCFlap.get(i).doubleValue()-1))
					);
		
		
		
		Double [] deltaCl0Flap = new Double [yStationActualFlap.length];
		for (int i=0; i<yStationActualFlap.length; i++){
			deltaCl0Flap[i] = 0.0;
		}

		for (int i=1; i<input.getFlapsNumber()+1; i++) {
			pos=i*2;
			deltaCl0Flap[pos] = deltaCl0FlapList.get(i-1);
			deltaCl0Flap[pos-1] = deltaCl0FlapList.get(i-1);
		}

		System.out.println(" delta clo due to flap with no separation" + Arrays.toString(deltaCl0Flap));

		ArrayList<Double> deltaCl0FlapAsList  = new ArrayList<Double>(Arrays.asList(deltaCl0Flap));
	
		
		List<Double> deltaCl0FlapWithSeparator = new ArrayList<Double>();
		List<Double> clAlphaFlapWithSeparator = new ArrayList<Double>();
		
		
		
		deltaCl0FlapWithSeparator = createCompleteArray(input, deltaCl0FlapAsList, yStationActualFlap, etaInFlap, etaOutFlap);
		
		System.out.println(" delta cl0 due to flap with separation "+ deltaCl0FlapWithSeparator);
		
		List<Double> cl0FlapWithSeparator = new ArrayList<Double>();
		
		Double [] cl0BaseActual = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClZeroDistribution()),
					yStationTotalFlap);
		for (int i=0 ; i < yStationTotalFlap.length ; i++ ){
			cl0FlapWithSeparator.add(cl0BaseActual[i] + deltaCl0FlapWithSeparator.get(i));
		}
		
		System.out.println(" clo flapstat " + Arrays.toString(cl0BaseActual ) );
		System.out.println(" delta cl0 "  + deltaCl0FlapWithSeparator.toString());
		System.out.println(" cl0 with flap separator " + cl0FlapWithSeparator.toString());
		
		
		
		//---
		
		Double [] clAlphaFlap = new Double [input.getFlapsNumber()*2+2];
		
		clAlphaFlap[0] = 0.0;
		
		for (int i=1; i<input.getFlapsNumber()+1; i++) {
			pos=i*2;
			double clAlpa = clAlphaMeanFlap[i-1] * ( cFirstCFlap.get(i-1)*
					(1-
							((input.getCfc().get(i-1)*(1/cFirstCFlap.get(i-1))*
							Math.pow(input.getDeltaFlap().get(i-1).to(SI.RADIAN).getEstimatedValue(), 2)))));
			clAlphaFlap[pos] =  clAlpa;
			clAlphaFlap[pos-1] = clAlpa;
		}
		
		System.out.println(" cl alpha flap " + Arrays.toString(clAlphaFlap));
		
		ArrayList<Double> cFlapAsList  = new ArrayList<Double>(Arrays.asList(clAlphaFlap));
			
		clAlphaFlapWithSeparator = createCompleteArray(input, cFlapAsList, yStationActualFlap, etaInFlap, etaOutFlap);
		clAlphaFlapWithSeparator.set(clAlphaFlapWithSeparator.size()-1, 0.0);
		
		Double [] clAlphaNoFlap = MyMathUtils.getInterpolatedValue1DLinear( 
				MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
				MyArrayUtils.convertToDoublePrimitive(input.getClalphaDistribution()),
				yStationTotalFlap);
		System.out.println(" cl alpha due to flap with separation "+ clAlphaFlapWithSeparator);
		
		for (int i=0; i<clAlphaFlapWithSeparator.size(); i++){
			if (clAlphaFlapWithSeparator.get(i)==0){
				clAlphaFlapWithSeparator.set(i, clAlphaNoFlap[i]);
			}
		}
		System.out.println(" cl alpha base " + Arrays.toString(clAlphaNoFlap));
		System.out.println(" cl alpha due to flap with separation "+ clAlphaFlapWithSeparator);
		
		
		 // alpha zero lift // DEGREE
		
		double [] alphaZeroLiftFlapWithSepartor = new double [clAlphaFlapWithSeparator.size()];
		for (int i=0 ; i< clAlphaFlapWithSeparator.size(); i++){
			alphaZeroLiftFlapWithSepartor [i]= - (cl0FlapWithSeparator.get(i)/clAlphaFlapWithSeparator.get(i));
		}
		
		System.out.println(" alpha zero lift with sepator " + Arrays.toString(alphaZeroLiftFlapWithSepartor) );
		
		
		// TOTAL

		MyArray alphaZeroLiftMyArray = new MyArray(alphaZeroLiftFlapWithSepartor);
		MyArray cl0Distribution = new MyArray(MyArrayUtils.convertToDoublePrimitive(cl0FlapWithSeparator));
		MyArray clAlphaDistribution = new MyArray(MyArrayUtils.convertToDoublePrimitive(clAlphaFlapWithSeparator));
		
		MyArray alphaZeroLiftTotal = MyArray.createArray(
				alphaZeroLiftMyArray.interpolate(
						yStationTotalFlap,
						yStationTotal));
		
		MyArray cl0DistributionTotal = MyArray.createArray(
				cl0Distribution.interpolate(
						yStationTotalFlap,
						yStationTotal));
		
		MyArray clAlphaDistributionTotal = MyArray.createArray(
				clAlphaDistribution.interpolate(
						yStationTotalFlap,
						yStationTotal));
		
		MyArray alphaZeroLiftClean = new MyArray(MyArrayUtils.convertListOfAmountodoubleArray(input.getAlphaZeroLiftDistribution()));
		MyArray alphaZeroLiftBasic = MyArray.createArray(
				alphaZeroLiftClean.interpolate(
						MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
						yStationTotal));

		System.out.println(" alpha zero lift clean " + alphaZeroLiftBasic);
		System.out.println(" Alpha zero lift total " + alphaZeroLiftTotal);
		
		
		
		 //delta cl alpha
//
//		Double[] alphaZeroLiftDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, alpha0lInput, yStationTotal);
//
//		System.out.println("alpha zero lift distribution base :" + Arrays.toString(alphaZeroLiftDistributionBase));

		// alpha star
	
		System.out.println("\n---------------------------");
		System.out.println("        ALPHA STAR           ");
		System.out.println("---------------------------\n");

		Double[] alphaStarDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, alphaStarInput, yStationTotal);

		System.out.println("alpha star distribution base :" + Arrays.toString(alphaStarDistributionBase));
		
		Double[] clAlphaDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, clAlphaInput, yStationTotal);

		System.out.println("Cl alpha distribution base :" + Arrays.toString(clAlphaDistributionBase));

	
		double [] clStarDistributionBase = new double [alphaStarDistributionBase.length];
		
		for (int i=0; i<clStarDistributionBase.length; i++){
			clStarDistributionBase[i] = clAlphaDistributionBase[i] * Math.toDegrees(alphaStarDistributionBase[i]) + clZeroDistributionBase[i];
		}
		
		System.out.println("Cl star distribution base :" + Arrays.toString(clStarDistributionBase));
		
		
		double [] alphaStarDistributionTotal = new double [yStationTotal.length];
		
		for (int i =0; i<yStationTotal.length; i++){
			alphaStarDistributionTotal[i] = (clStarDistributionBase[i] -cl0DistributionTotal.get(i) )/ clAlphaDistributionTotal.get(i);
		}
		
		
		System.out.println(" Alpha star distribution total " + Arrays.toString(alphaStarDistributionTotal));
		
		// cl max
		
		System.out.println("\n---------------------------");
		System.out.println("          CL MAX             ");
		System.out.println("---------------------------\n");
		Double[] clMaxDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, clMaxInput, yStationTotal);

		System.out.println("cl max distribution base :" + Arrays.toString(clMaxDistributionBase));

		
		// deltaClmax (flap)
				List<Double> deltaClmaxBase = new ArrayList<Double>();
				for(int i=0; i<input.getFlapsNumber(); i++)
					deltaClmaxBase.add(
							highLiftDatabaseReader
							.getDeltaCLmaxBaseVsTc(
									maxTicknessFlapStations[i],
									flapTypeIndex.get(i)
									)
							);
			
				List<Double> k1 = new ArrayList<Double>();
				for(int i=0; i<input.getFlapsNumber(); i++)
					if (input.getCfc().get(i) <= 0.30)
						k1.add(highLiftDatabaseReader
								.getK1vsFlapChordRatio(input.getCfc().get(i), flapTypeIndex.get(i))
								);
					else if ((input.getCfc().get(i) > 0.30) && ((flapTypeIndex.get(i) == 2) || (flapTypeIndex.get(i) == 4) || (flapTypeIndex.get(i) == 5)))
						k1.add(0.04*(input.getCfc().get(i)*100));
					else if ((input.getCfc().get(i) > 0.30) && ((flapTypeIndex.get(i) == 1) || (flapTypeIndex.get(i) == 3) ))
						k1.add((608.31*Math.pow(input.getCfc().get(i), 5))
								-(626.15*Math.pow(input.getCfc().get(i), 4))
								+(263.4*Math.pow(input.getCfc().get(i), 3))
								-(62.946*Math.pow(input.getCfc().get(i), 2))
								-(10.638*input.getCfc().get(i))
								+0.0064
								);
			
				List<Double> k2 = new ArrayList<Double>();
				for(int i=0; i<input.getFlapsNumber(); i++)
					k2.add(highLiftDatabaseReader
							.getK2VsDeltaFlap(input.getDeltaFlap().get(i).getEstimatedValue(), flapTypeIndex.get(i))
							);
			
				List<Double> k3 = new ArrayList<Double>();
				for(int i=0; i<input.getFlapsNumber(); i++)
					k3.add(highLiftDatabaseReader
							.getK3VsDfDfRef(
									input.getDeltaFlap().get(i).getEstimatedValue(),
									deltaFlapRef.get(i),
									flapTypeIndex.get(i)
									)
							);
			
				 List<Double> deltaClmaxFlapList = new ArrayList<Double>();
				 double deltaCLmaxFlap;
				 
				for(int i=0; i<input.getFlapsNumber(); i++)
					deltaClmaxFlapList.add(k1.get(i).doubleValue()
							*k2.get(i).doubleValue()
							*k3.get(i).doubleValue()
							*deltaClmaxBase.get(i).doubleValue()
							);
				System.out.println(" delta cl max flap list " + deltaClmaxFlapList.toString());
				double deltaClmaxFlapTemp = 0;
				for(int i=0; i<input.getFlapsNumber(); i++)
					deltaClmaxFlapTemp += deltaClmaxFlapList.get(i);
				
				deltaCLmaxFlap= deltaClmaxFlapTemp;
			
				
				
//				//---------------------------------------------------------------
//				// deltaClmax (slat)
				
				List<Double> deltaClmaxSlatList = new ArrayList<Double>();
				
				if(input.getSlatsNumber() > 0.0) {
			
					List<Double> dCldDelta = new ArrayList<Double>();
					for(int i=0; i<input.getSlatsNumber(); i++)
						dCldDelta.add(highLiftDatabaseReader
								.getDCldDeltaVsCsC(input.getCsc().get(i))
								);
			
					List<Double> etaMaxSlat = new ArrayList<Double>();
					for(int i=0; i<input.getSlatsNumber(); i++)
						etaMaxSlat.add(highLiftDatabaseReader
								.getEtaMaxVsLEradiusTicknessRatio(
										leRadiusMeanSlat[i]/(chordMeanSlat[i]),
										maxTicknessMeanSlat[i])
								);
			
					List<Double> etaDeltaSlat = new ArrayList<Double>();
					for(int i=0; i<input.getSlatsNumber(); i++)
						etaDeltaSlat.add(
								highLiftDatabaseReader
								.getEtaDeltaVsDeltaSlat(input.getDeltaSlat().get(i).getEstimatedValue())
								);
			
					
					
					System.out.println(" ETA MAX --> " + etaMaxSlat.toString());
					
					System.out.println(" ETA DELTA SLAT --> " + etaDeltaSlat.toString());
					
					System.out.println(" d cl --> " + dCldDelta.toString());
					
					for(int i=0; i<input.getSlatsNumber(); i++)
						deltaClmaxSlatList.add(
								dCldDelta.get(i).doubleValue()
								*etaMaxSlat.get(i).doubleValue()
								*etaDeltaSlat.get(i).doubleValue()
								*input.getDeltaSlat().get(i).getEstimatedValue()
								*input.getcExtCSlat().get(i)
								);
			
					System.out.println(" delta cl max slat list " + deltaClmaxSlatList.toString());	
				}
				
				
				
				pos=0;

				Double [] deltaClMaxFlap = new Double [yStationActualFlap.length];
				for (int i=0; i<deltaClMaxFlap.length; i++){
					deltaClMaxFlap[i] = 0.0;
				}

				for (int i=1; i<input.getFlapsNumber()+1; i++) {
					pos=i*2;
					deltaClMaxFlap[pos] = deltaClmaxFlapList.get(i-1);
					deltaClMaxFlap[pos-1] = deltaClmaxFlapList.get(i-1);
				}

				System.out.println(" delta cl max flap with no separation" + Arrays.toString(deltaClMaxFlap));

				ArrayList<Double> deltaclMaxFlapAsList  = new ArrayList<Double>(Arrays.asList(deltaClMaxFlap));
				ArrayList<Double> deltaclmaxFlapWithSeparator  = new ArrayList<Double>(Arrays.asList(deltaClMaxFlap));

				deltaclmaxFlapWithSeparator = createCompleteArray(input, deltaclMaxFlapAsList, yStationActualFlap, etaInFlap, etaOutFlap);
				
				System.out.println(" delta c due to flap with separation" + deltaclmaxFlapWithSeparator.toString());
				
				
				pos=0;

				Double [] deltaClMaxSlat = new Double [yStationActualSlat.length];
				for (int i=0; i<deltaClMaxSlat.length; i++){
					deltaClMaxSlat[i] = 0.0;
				}

				for (int i=1; i<input.getSlatsNumber()+1; i++) {
					pos=i*2;
					deltaClMaxSlat[pos] = deltaClmaxSlatList.get(i-1);
					deltaClMaxSlat[pos-1] = deltaClmaxSlatList.get(i-1);
				}

				System.out.println(" delta cl max slat with no separation" + Arrays.toString(deltaClMaxSlat));

				ArrayList<Double> deltaclMaxSlatAsList  = new ArrayList<Double>(Arrays.asList(deltaClMaxSlat));
				ArrayList<Double> deltaclmaxSlatWithSeparator  = new ArrayList<Double>(Arrays.asList(deltaClMaxSlat));

				deltaclmaxSlatWithSeparator = createCompleteArray(input, deltaclMaxSlatAsList, yStationActualSlat, etaInSlat, etaOutSlat);
				
				System.out.println(" delta cl max due to slat with separation" + deltaclmaxSlatWithSeparator.toString());

				// TOTAL

				double [] deltaclMaxFlapWithSeparatorArray = new double [deltaclMaxFlapAsList.size()];
				double [] deltaclMaxSlatWithSeparatorArray = new double [deltaclMaxSlatAsList.size()];
				for(int i=0; i<deltaclMaxFlapWithSeparatorArray.length; i++){
					deltaclMaxFlapWithSeparatorArray[i] = deltaclMaxFlapAsList.get(i);
								}
				for(int i=0; i<deltaclMaxSlatWithSeparatorArray.length; i++){
				deltaclMaxSlatWithSeparatorArray[i] = deltaclMaxSlatAsList.get(i);
				}
				
				MyArray deltaclMaxFlapMyArray = new MyArray(deltaclMaxFlapWithSeparatorArray);
				MyArray deltaclMaxFlapTotal = MyArray.createArray(
						deltaclMaxFlapMyArray.interpolate(
								yStationTotalFlap,
								yStationTotal));
				
				MyArray deltaclMaxSlatMyArray = new MyArray(deltaclMaxSlatWithSeparatorArray);
				MyArray deltaclMaxSlatTotal = MyArray.createArray(
						deltaclMaxSlatMyArray.interpolate(
								yStationTotalSlat,
								yStationTotal));
				
				System.out.println("\n\n delta cl max slat array  " + Arrays.toString(deltaclMaxSlatTotal.toArray()));
				System.out.println(" y station total   " + Arrays.toString(yStationTotal));
				
				System.out.println(" delta cl max flap array  " + Arrays.toString(deltaclMaxFlapTotal.toArray()));
				System.out.println(" y station total   " + Arrays.toString(yStationTotal));



				double [] clMaxDistributionTotal = new double [yStationTotal.length];

				for (int i=0; i<yStationTotal.length; i++){
					clMaxDistributionTotal [i] = clMaxDistributionBase[i] + deltaclMaxFlapTotal.get(i) + deltaclMaxSlatTotal.get(i);
				}
				
				System.out.println(" cl max distribution total " + Arrays.toString(clMaxDistributionTotal));
				
				
				
		//Calculate flapped curve
				
				Double [] dihedralDistributionTotal = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
						dihedralInput,yStationTotal);
				
				Double [] twistDistributionTotal = MyMathUtils.getInterpolatedValue1DLinear( 
						MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
						twistInput,
						yStationTotal);
				
				System.out.println(" dihedral total " + Arrays.toString(dihedralDistributionTotal));
				System.out.println(" dihedral total " + Arrays.toString(twistDistributionTotal));
				double vortexSemiSpanToSemiSpanRatio = (1./(2*input.getNumberOfPointSemispan()));
				
				
				
		// Nasa Blackwell input
				
				int nPointsSemispanWise = (int) (1./(2*vortexSemiSpanToSemiSpanRatio));
				double [] yStationNasaBlackwellND = new double [nPointsSemispanWise];
				double [] yStationNasaBlackwellDimensional = new double [nPointsSemispanWise];
				
				yStationNasaBlackwellND = MyArrayUtils.linspace(0, 1, nPointsSemispanWise);
				yStationNasaBlackwellDimensional = MyArrayUtils.linspace(0, input.getSemiSpan().getEstimatedValue(), nPointsSemispanWise);
				
				MyArray chordsVsYActual = new MyArray(numberOfPoint);
				MyArray xLEvsYActual = new MyArray(numberOfPoint);
				MyArray dihedralActual = new MyArray(numberOfPoint);
				MyArray twistActual = new MyArray(numberOfPoint);
				MyArray alphaStarActual = new MyArray(numberOfPoint);
				MyArray alpha0lActual = new MyArray(numberOfPoint);
				MyArray clMaxActual = new MyArray(numberOfPoint);
				
				MyArray chordsVsYIn = new MyArray(chordDistributionTotal);
				MyArray xLEvsYIn = new MyArray(xLeDistributionTotal);
				MyArray dihedralIn = new MyArray(dihedralDistributionTotal);
				MyArray twistIn  = new MyArray(twistDistributionTotal);
				MyArray alphaStarIn = new MyArray(alphaStarDistributionTotal);
				MyArray alpha0lIn = new MyArray(alphaZeroLiftTotal);
				MyArray clMaxIn = new MyArray(clMaxDistributionTotal);


				xLEvsYActual = MyArray.createArray(
						xLEvsYIn.interpolate(
								yStationTotal,
								yStationNasaBlackwellND));

				chordsVsYActual = MyArray.createArray(
						chordsVsYIn.interpolate(
								yStationTotal,
								yStationNasaBlackwellND));

				dihedralActual = MyArray.createArray(
						dihedralIn.interpolate(
								yStationTotal,
								yStationNasaBlackwellND));
				
				twistActual = MyArray.createArray(
						twistIn.interpolate(
								yStationTotal,
								yStationNasaBlackwellND));
				
				double [] twistDegree = new double [twistDistributionTotal.length ];
				for (int i=0; i<twistDegree.length; i++){
					twistDegree[i] = Math.toDegrees(twistDistributionTotal[i]);
				}
				
				alphaStarActual = MyArray.createArray(
						alphaStarIn.interpolate(
								yStationTotal,
								yStationNasaBlackwellND));

				alpha0lActual = MyArray.createArray(
						alpha0lIn.interpolate(
								yStationTotal,
								yStationNasaBlackwellND));
				
				double [] alphaZeroLiftActual = new double [alpha0lActual.size() ];
				for (int i=0; i<alpha0lActual.size(); i++){
					alphaZeroLiftActual[i] = Math.toRadians(alpha0lActual.get(i));
				}

				clMaxActual = MyArray.createArray(
						clMaxIn.interpolate(
								yStationTotal,
								yStationNasaBlackwellND));
				
				double [] clMaxDistributionTotalNPoint = new double [yStationNasaBlackwellND.length];

				clMaxDistributionTotalNPoint = MyArrayUtils.convertToDoublePrimitive(
						MyMathUtils.getInterpolatedValue1DLinear(
								yStationTotal,
								clMaxDistributionTotal, 
								yStationNasaBlackwellND));

				
				// alpha star
				
				System.out.println("\n---------------------------");
				System.out.println("        FINAL VALUES           ");
				System.out.println("---------------------------\n");

				System.out.println("\n         CLEAN             ");
				System.out.println("-----------------------------");
				
				System.out.println("Ystation --> " + Arrays.toString(yStationInput));
				System.out.println("Chords -->"+ Arrays.toString(chordInput));
				System.out.println("X le -->"+ Arrays.toString(xleInput));
				System.out.println("Dihedral -->"+ Arrays.toString(dihedralInput));
				System.out.println("Twist -->"+ Arrays.toString(twistInput));
				System.out.println("Alpha zero lift -->"+ Arrays.toString(alpha0lInput));
				System.out.println("Alpha Star -->"+ Arrays.toString(alphaStarInput));
				System.out.println("Cl max -->"+ Arrays.toString(clMaxInput));
				
				System.out.println("\n   CLEAN FLAP SECTION      ");
				System.out.println("-----------------------------");
				
				System.out.println("Ystation --> " + Arrays.toString(yStationTotal));
				System.out.println("Chords -->"+ Arrays.toString(chordDistributionBase));
				System.out.println("X le -->"+ Arrays.toString(xLEDistributionBase));
				System.out.println("Alpha zero lift -->"+ alphaZeroLiftBasic.toString());
				System.out.println("Alpha Star -->"+ Arrays.toString(alphaStarDistributionBase));
				System.out.println("Cl max -->"+ Arrays.toString(clMaxDistributionBase));

				
				System.out.println("\n         FLAPPED             ");
				System.out.println("-----------------------------");
				
				System.out.println("Ystation --> "+ Arrays.toString(yStationTotal));
				System.out.println("Chords -->"+ Arrays.toString(chordDistributionTotal));
				System.out.println("X le -->"+ Arrays.toString(xLeDistributionTotal));
				System.out.println("Dihedral -->"+ Arrays.toString(dihedralDistributionTotal));
				System.out.println("Twist -->"+ Arrays.toString(twistDegree));
				System.out.println("Alpha zero lift -->"+ alphaZeroLiftTotal.toString());
				System.out.println("Alpha Star -->"+ Arrays.toString(alphaStarDistributionTotal));
				System.out.println("Cl max -->"+ Arrays.toString(clMaxDistributionTotal));
	
				
				System.out.println("\n         FLAPPED 100 section            ");
				System.out.println("-----------------------------");
				
				System.out.println("Ystation --> "+ Arrays.toString(yStationNasaBlackwellDimensional));
				System.out.println("Chords -->"+ chordsVsYActual.toString());
				System.out.println("X le -->" + xLEvsYActual.toString());
				System.out.println("Dihedral -->" + dihedralActual.toString());
				System.out.println("Twist -->" + twistActual.toString());
				System.out.println("Alpha zero lift -->" + Arrays.toString(alphaZeroLiftActual));
				System.out.println("Cl max -->" + clMaxActual.toString());

				System.out.println(" Mach " + input.getMachNumber());
				System.out.println(" Altitude " + input.getAltitude());
				
				
				NasaBlackwell theNasaBlackwellCalculator = new  NasaBlackwell(
						input.getSemiSpan().getEstimatedValue(), 
						input.getSurface().getEstimatedValue(),
						yStationNasaBlackwellDimensional,
						chordsVsYActual.toArray(),
						xLEvsYActual.toArray(),
						dihedralActual.toArray(),
						twistActual.toArray(),
						alphaZeroLiftActual,
						vortexSemiSpanToSemiSpanRatio,
						0.0,
						input.getMachNumber(),
						input.getAltitude().getEstimatedValue());
				
				Amount<Angle> alphaFirst = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
				Amount<Angle> alphaSecond = Amount.valueOf(Math.toRadians(4.0), SI.RADIAN);

				Amount<Angle> alphaThird = Amount.valueOf(Math.toRadians(3.0), SI.RADIAN);
				
				System.out.println("\n\ny STATION ND " + Arrays.toString(yStationNasaBlackwellND));
				theNasaBlackwellCalculator.calculate(alphaThird);
				double [] clOutput = theNasaBlackwellCalculator.get_clTotalDistribution().toArray();
				
				System.out.println( " cl array at alpha 3 " + Arrays.toString(clOutput));
				
				theNasaBlackwellCalculator.calculate(alphaFirst);
				double [] clDistribution = theNasaBlackwellCalculator.get_clTotalDistribution().toArray();
				double cLFirst = theNasaBlackwellCalculator.get_cLEvaluated();//MyMathUtils.integrate1DTrapezoidLinear(yStationActual, clDistribution, 0, 1);
				//System.out.println("\n\n cL alpha 2 " + cLFirst);

				theNasaBlackwellCalculator.calculate(alphaSecond);
				double cLSecond = theNasaBlackwellCalculator.get_cLEvaluated();
				//System.out.println(" cL alpha 4 " + cLSecond);

				double cLAlpha = (cLSecond - cLFirst)/(alphaSecond.getEstimatedValue()-alphaFirst.getEstimatedValue()); // 1/rad
				input.setClAlpha(Math.toRadians(cLAlpha));

				System.out.println(" \n ");
				//		System.out.println(" cL ALPHA " + cLAlpha);


				Amount<Angle> alphaZero = Amount.valueOf(0.0, SI.RADIAN);

				theNasaBlackwellCalculator.calculate(alphaZero);
				double cLZero = theNasaBlackwellCalculator.get_cLEvaluated();
				input.setcLZero(cLZero);
				//		System.out.println(" cl zero " + cLZero);

				double alphaZeroLift = -(cLZero)/cLAlpha;
				input.setAlphaZeroLift(Amount.valueOf(Math.toDegrees(alphaZeroLift), NonSI.DEGREE_ANGLE));

				//		System.out.println(" alpha zero lift (deg) " + Math.toDegrees(alphaZeroLift));


				// alpha Star

				double rootChord = chordInput[0];
				double kinkChord = MyMathUtils.getInterpolatedValue1DLinear(yStationTotal, chordDistributionTotal,
						input.getAdimensionalKinkStation());
				double tipChord = chordInput[chordInput.length-1];

				double alphaStarRoot= alphaStarDistributionTotal[0];
				double alphaStarKink = MyMathUtils.getInterpolatedValue1DLinear(yStationTotal, alphaStarDistributionTotal,
						input.getAdimensionalKinkStation());
				double alphaStarTip = alphaStarDistributionTotal[alphaStarDistributionTotal.length-1];

				double dimensionalKinkStation = input.getAdimensionalKinkStation()*input.getSemiSpan().getEstimatedValue();
				double dimensionalOverKink = input.getSemiSpan().getEstimatedValue() - dimensionalKinkStation;

				double influenceAreaRoot = rootChord * dimensionalKinkStation/2;
				double influenceAreaKink = (kinkChord * dimensionalKinkStation/2) + (kinkChord * dimensionalOverKink/2);
				double influenceAreaTip = tipChord * dimensionalOverKink/2;

				double kRoot = 2*influenceAreaRoot/input.getSurface().getEstimatedValue();
				double kKink = 2*influenceAreaKink/input.getSurface().getEstimatedValue();
				double kTip = 2*influenceAreaTip/input.getSurface().getEstimatedValue();


				double alphaStar =  alphaStarRoot * kRoot + alphaStarKink * kKink + alphaStarTip * kTip;

				input.setAlphaStar(Amount.valueOf(alphaStar, NonSI.DEGREE_ANGLE));
				//		System.out.println(" alpha star (deg) " + Math.toDegrees(alphaStar));

				Amount<Angle> alphaStarAmount = Amount.valueOf(alphaStar, SI.RADIAN);

				theNasaBlackwellCalculator.calculate(alphaStarAmount);
				double cLStar = theNasaBlackwellCalculator.get_cLEvaluated();
				input.setClStar(cLStar);

				//		System.out.println(" cL star " + cLStar);

				// cl Max

				double cLMax = LiftCalc.calculateCLMax(clMaxDistributionTotalNPoint,
						input.getSemiSpan().getEstimatedValue(), 
						input.getSurface().getEstimatedValue(),
						yStationNasaBlackwellDimensional,
						chordsVsYActual.toArray(),
						xLEvsYActual.toArray(),
						dihedralActual.toArray(),
						twistActual.toArray(),
						alphaZeroLiftActual,
						vortexSemiSpanToSemiSpanRatio,
						0.0,
						input.getMachNumber(),
						input.getAltitude().getEstimatedValue());
				
				
				input.setClMax(cLMax);
				//		System.out.println(" cl max " + cLMax);

				// alpha stall
				double alphaMax = ((cLMax-cLZero)/Math.toRadians(cLAlpha));
				double alphaStall = alphaMax + input.getDeltaAlpha();

				input.setAlphaStall(Amount.valueOf(alphaStall, NonSI.DEGREE_ANGLE));
				//		System.out.println(" alpha Stall (deg) = " +  alphaStall);



				// RESULTS

				System.out.println(" \n-----------WING RESULTS-------------- ");
				System.out.println(" Alpha stall = " + input.getAlphaStall().getEstimatedValue() + " " + input.getAlphaStall().getUnit());
				System.out.println(" Alpha star = " + input.getAlphaStar().getEstimatedValue() + " " + input.getAlphaStar().getUnit());
				System.out.println(" CL max = " + input.getClMax());
				System.out.println(" CL star = " + input.getClStar());
				System.out.println(" CL alpha = " + input.getClAlpha() + " (1/deg)");




		// alpha0L

		// cL alpha new

		// alpha star

		// FLAPPED STALL PATH

		// Print results

		// plot

	
	String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);


	
	List<Double[]> yVector = new ArrayList<Double[]>();
	List<Double[]> clVector = new ArrayList<Double[]>();
	List<String> legend  = new ArrayList<>(); 
	Double [] yStationDouble = new Double [nPointsSemispanWise];
	Double [] clMaxDouble = new Double [clMaxActual.size()];
	Double [] clMaxArrayDouble = new Double [clMaxActual.size()];
	
	for (int i=0; i< yStationNasaBlackwellND.length; i++){
		yStationDouble[i] = yStationNasaBlackwellND[i];
	}
	for (int i=0; i<2; i++){
		yVector.add(i, yStationDouble);
	}

	legend.add(0,"$c_l$ max airfoils ");
	legend.add(1, "$c_l$ distribution at $\\alpha$ " + alphaMax);
	
	theNasaBlackwellCalculator.calculate(Amount.valueOf(Math.toRadians(alphaMax), SI.RADIAN));
	double [] clMaxArray =theNasaBlackwellCalculator.get_clTotalDistribution().toArray();

	for (int i =0; i<clMaxActual.size(); i++){
		clMaxDouble [i] =clMaxActual.get(i);
		clMaxArrayDouble[i] = clMaxArray[i];
	}
	
	clVector.add(clMaxDouble);
	clVector.add(clMaxArrayDouble);

	System.out.println(" \n-----------WRITING CHART TO FILE . STALL PATH-------------- ");
	
	MyChartToFileUtils.plotJFreeChart(
			yVector, 
			clVector,
			"CL vs alpha",
			"$\\eta$", 
			"$C_l$",
			null, null, null, null,
			"",
			"",
			true,
			legend,
			JPADStaticWriteUtils.createNewFolder(folderPath + "HighLift_StallPath_Charts" + File.separator),
			"Stall_path");

	System.out.println(" \n-------------------DONE----------------------- \n");
	}
	
	public static ArrayList<Double> createCompleteArray (InputOutputTree input, List<Double> outputAsList, double [] yStation, double [] etaIn, double [] etaOut){
		
		int position;

		
				int pp=0;
				for ( int i =0; i< yStation.length-1 ; i++) {
					if ( (yStation[i+1] - yStation[i]) > 0.03 ) {
		
						if ( yStation[i] == 0){
							outputAsList.add(i+1+pp,0.0);	
							pp=pp+1;
						}
		
						if (yStation[i+1] ==1){
							outputAsList.add(i+1+pp,0.0);		
							pp=pp+1;}
		
						if (yStation[i] != 0 & yStation[i+1] !=1){
							//					if (Arrays.asList(etaOutFlap).contains(yStationActualFlap[i+1]) &  Arrays.asList(etaInFlap).contains(yStationActualFlap[i])){}
							if (Arrays.binarySearch(etaOut,yStation[i+1])>=0 &  Arrays.binarySearch(etaIn,yStation[i])>=0){}
							else{
								outputAsList.add(i+1+pp,0.0);
								outputAsList.add(i+1+pp,0.0);
								pp=pp+2;
							}
						}
					}
				}
				return (ArrayList<Double>) outputAsList;
	}
	
	
	public static double[] calculateCLArraymodifiedStallPath(MyArray alphaArray, LiftingSurface2Panels theLiftingSurface){


		// VARIABLE DECLARATION
		Amount<Angle> alphaActual;
		double qValue, cLWingActual = 0;
		double [] clNasaBlackwell = new double [alphaArray.size()];

		List<Airfoil> airfoilList = new ArrayList<Airfoil>();

		LSAerodynamicsManager theLSManager = theLiftingSurface.getAerodynamics();
		LSAerodynamicsManager.CalcLiftDistribution calculateLiftDistribution = theLSManager.getCalculateLiftDistribution();

		int nPointSemiSpan = theLSManager.get_nPointsSemispanWise();
		double [] yArray = MyArrayUtils.linspace(0., theLiftingSurface.get_span().getEstimatedValue()/2, nPointSemiSpan);
		double [] yArrayND = MyArrayUtils.linspace(0., 1, nPointSemiSpan);
		double [] cLDistributionInviscid = new double [nPointSemiSpan];
		double [] alphaLocalAirfoil = new double [nPointSemiSpan];
		double [] clDisributionReal = new double [nPointSemiSpan];

		double [] cLWingArray = new double [alphaArray.size()];


		for (int j=0 ; j<nPointSemiSpan; j++){
			airfoilList.add(j,theLSManager.calculateIntermediateAirfoil(
					theLiftingSurface, yArray[j]) );
			airfoilList.get(j).getAerodynamics().calculateClvsAlpha();}


		// iterations
		for (int ii=0; ii<alphaArray.size(); ii++){
			alphaActual = Amount.valueOf(alphaArray.get(ii),SI.RADIAN);

			calculateLiftDistribution.getNasaBlackwell().calculate(alphaActual);
			clNasaBlackwell = calculateLiftDistribution.getNasaBlackwell().get_clTotalDistribution().toArray();
			clNasaBlackwell[clNasaBlackwell.length-1] = 0;

			for (int i=0 ; i<nPointSemiSpan ;  i++){
				cLDistributionInviscid[i] = clNasaBlackwell[i];
				//			System.out.println( " cl local " + cLLocal);
				qValue = airfoilList.get(i).getAerodynamics().calculateClAtAlphaInterp(0.0);
				//			System.out.println(" qValue " + qValue );
				alphaLocalAirfoil[i] = (cLDistributionInviscid[i]-qValue)/airfoilList.get(i).getAerodynamics().get_clAlpha();
				//			System.out.println(" alpha local airfoil " + alphaLocalAirfoil);
				clDisributionReal[i] = airfoilList.get(i).getAerodynamics().calculateClAtAlpha(
						//alphaLocal.getEstimatedValue()+
						alphaLocalAirfoil[i]);
				//					airfoilList.get(i).getGeometry().get_twist().getEstimatedValue());
			}
			cLWingActual = MyMathUtils.integrate1DSimpsonSpline(yArrayND, clDisributionReal);

			cLWingArray[ii] = cLWingActual;
		}

		return cLWingArray;
	}
	
	
	// MODIFIED STALL PATH
	
	// Need to define the vector useful to the method.
	// Clmax
	// alphaStall
	// alphaStar
	// ClStar
	// ClZero
	// Cl
	
	
}