
package sandbox.mr.HighLiftStallPath;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

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
		double [] dihedralInput = new double [input.getNumberOfSections()];
		double [] twistInput = new double [input.getNumberOfSections()];
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
			dihedralInput[i] = Math.toRadians(input.getDihedralDistribution().get(i).getEstimatedValue());
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

		System.out.println("Y station Total" + Arrays.toString(yStationTotal));


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

		int p=0;
		for ( int i =0; i< yStationActualFlap.length-1 ; i++) {
			if ( (yStationActualFlap[i+1] - yStationActualFlap[i]) > 0.03 ) {

				if ( yStationActualFlap[i] == 0){
					deltaChordFlapAsList.add(i+1+p,0.0);	
					p=p+1;
				}

				if (yStationActualFlap[i+1] ==1){
					deltaChordFlapAsList.add(i+1+p,0.0);		
					p=p+1;}

				if (yStationActualFlap[i] != 0 & yStationActualFlap[i+1] !=1){
					//					if (Arrays.asList(etaOutFlap).contains(yStationActualFlap[i+1]) &  Arrays.asList(etaInFlap).contains(yStationActualFlap[i])){}
					if (Arrays.binarySearch(etaOutFlap,yStationActualFlap[i+1])>=0 &  Arrays.binarySearch(etaInFlap,yStationActualFlap[i])>=0){}
					else{
						deltaChordFlapAsList.add(i+1+p,0.0);
						deltaChordFlapAsList.add(i+1+p,0.0);
						p=p+2;
					}
				}
			}
		}

		System.out.println(" delta c due to flap with separation" + deltaChordFlapAsList.toString());



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

		int ps=0;
		for ( int i =0; i< yStationActualSlat.length-1 ; i++) {
			if ( (yStationActualSlat[i+1] - yStationActualSlat[i]) > 0.03 ) {

				if ( yStationActualSlat[i] == 0){
					deltaChordSlatAsList.add(i+1+ps,0.0);	
					ps=ps+1;
				}

				if (yStationActualSlat[i+1] ==1){
					deltaChordSlatAsList.add(i+1+ps,0.0);		
					ps=ps+1;}

				if (yStationActualSlat[i] != 0 & yStationActualSlat[i+1] !=1){
					//					if (Arrays.asList(etaOutFlap).contains(yStationActualFlap[i+1]) &  Arrays.asList(etaInFlap).contains(yStationActualFlap[i])){}
					if (Arrays.binarySearch(etaOutSlat,yStationActualSlat[i+1])>=0 &  Arrays.binarySearch(etaInSlat,yStationActualSlat[i])>=0){}
					else{
						deltaChordSlatAsList.add(i+1+ps,0.0);
						deltaChordSlatAsList.add(i+1+ps,0.0);
						ps=ps+2;
					}
				}
			}
		}

		System.out.println(" delta c due to slat with separation" + deltaChordSlatAsList.toString());


		// TOTAL

		// interpolation

		double [] deltaChordFlapWithSeparator = new double [deltaChordFlapAsList.size()];
		double [] deltaChordSlatWithSeparator = new double [deltaChordSlatAsList.size()];
		for(int i=0; i<deltaChordFlapWithSeparator.length; i++){
			deltaChordFlapWithSeparator[i] = deltaChordFlapAsList.get(i);
			deltaChordSlatWithSeparator[i] = deltaChordSlatAsList.get(i);
		}
		
		MyArray deltaChordFlapMyArray = new MyArray(deltaChordFlapWithSeparator);
		MyArray deltaChordFlapTotal = MyArray.createArray(
				deltaChordFlapMyArray.interpolate(
						yStationTotalFlap,
						yStationTotal));
		
		MyArray deltaChordSlatMyArray = new MyArray(deltaChordSlatWithSeparator);
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

		// mean values
		
		double [] clAlphaMean = new double [input.getFlapsNumber()];
		double [] clZeroMean = new double [input.getFlapsNumber()];
		double [] clAlphaFlapStations = new double [2*input.getFlapsNumber()];
		double [] clZeroFlapStations = new double [2*input.getFlapsNumber()];

		double [] influenceFactor = new double [2];
		
		for ( int i=0; i< input.getFlapsNumber(); i++){
			int kk = i*2;
			
			clAlphaFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClalphaDistribution()),
					etaInFlap[i]);
			
			clAlphaFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClalphaDistribution()),
					etaOutFlap[i]);
			
			clZeroFlapStations[kk] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClZeroDistribution()),
					etaInFlap[i]);
			
			clZeroFlapStations[kk+1] = MyMathUtils.getInterpolatedValue1DLinear( 
					MyArrayUtils.convertToDoublePrimitive(input.getyAdimensionalStationInput()),
					MyArrayUtils.convertToDoublePrimitive(input.getClZeroDistribution()),
					etaOutFlap[i]);
			
			influenceFactor = MeanAirfoilCalc.meanAirfoilFlap(etaInFlap[i], etaOutFlap[i], input);
			
			clAlphaMean[i] = clAlphaFlapStations[kk] * influenceFactor[0] + clAlphaFlapStations[kk+1]*influenceFactor[1];
			clZeroMean[i] = clZeroFlapStations[kk] * influenceFactor[0] + clZeroFlapStations[kk+1]*influenceFactor[1];
		}
		
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
					*clAlphaMean[i]
					); 
		
		List<Double> deltaCl0FlapList = new ArrayList<Double>();
		
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCl0FlapList.add(
					(deltaCl0First.get(i).doubleValue()*cFirstCFlap.get(i).doubleValue())
					+(clZeroMean[i]*(cFirstCFlap.get(i).doubleValue()-1))
					);
		
		
		//---
//		for(int i=0; i<input.getFlapsNumber(); i++)
//			deltaCCfFlap.add(
//					highLiftDatabaseReader
//					.getDeltaCCfVsDeltaFlap(input.getDeltaFlap().get(i).getEstimatedValue(), flapTypeIndex.get(i))
//					);
//
//		List<Double> cFirstCFlap = new ArrayList<Double>();
//		
//		for(int i=0; i<input.getFlapsNumber(); i++)
//			cFirstCFlap.add(1+(deltaCCfFlap.get(i).doubleValue()*input.getCfc().get(i).doubleValue()));
//
//		int pos;
//
//		Double [] deltaChordFlap = new Double [yStationActualFlap.length];
//		for (int i=0; i<deltaChordFlap.length; i++){
//			deltaChordFlap[i] = 0.0;
//		}
//
//		for (int i=1; i<input.getFlapsNumber()+1; i++) {
//			pos=i*2;
//			deltaChordFlap[pos] = cFirstCFlap.get(i-1)-1;
//			deltaChordFlap[pos-1] = cFirstCFlap.get(i-1)-1;
//		}
//
//		System.out.println(" delta c due to flap with no separation" + Arrays.toString(deltaChordFlap));
//
//		ArrayList<Double> deltaChordFlapAsList  = new ArrayList<Double>(Arrays.asList(deltaChordFlap));
//
//		int p=0;
//		for ( int i =0; i< yStationActualFlap.length-1 ; i++) {
//			if ( (yStationActualFlap[i+1] - yStationActualFlap[i]) > 0.03 ) {
//
//				if ( yStationActualFlap[i] == 0){
//					deltaChordFlapAsList.add(i+1+p,0.0);	
//					p=p+1;
//				}
//
//				if (yStationActualFlap[i+1] ==1){
//					deltaChordFlapAsList.add(i+1+p,0.0);		
//					p=p+1;}
//
//				if (yStationActualFlap[i] != 0 & yStationActualFlap[i+1] !=1){
//					//					if (Arrays.asList(etaOutFlap).contains(yStationActualFlap[i+1]) &  Arrays.asList(etaInFlap).contains(yStationActualFlap[i])){}
//					if (Arrays.binarySearch(etaOutFlap,yStationActualFlap[i+1])>=0 &  Arrays.binarySearch(etaInFlap,yStationActualFlap[i])>=0){}
//					else{
//						deltaChordFlapAsList.add(i+1+p,0.0);
//						deltaChordFlapAsList.add(i+1+p,0.0);
//						p=p+2;
//					}
//				}
//			}
//		}
//
//		System.out.println(" delta c due to flap with separation" + deltaChordFlapAsList.toString());
//
//
//
//		// Delta Slat
//
//		List<Double> deltaCCfSlat = new ArrayList<Double>();
//
//		for(int i=0; i<input.getSlatsNumber(); i++){
//			deltaCCfSlat.add(input.getcExtCSlat().get(i));}
//
//		int posSlat;
//		Double [] deltaChordSlat = new Double [yStationActualSlat.length];
//		for (int i=0; i<deltaChordSlat.length; i++){
//			deltaChordSlat[i] = 0.0;
//		}
//		for (int i=1; i<input.getSlatsNumber()+1; i++) {
//			posSlat=i*2;
//			deltaChordSlat[posSlat] = deltaCCfSlat.get(i-1)-1;
//			deltaChordSlat[posSlat-1] = deltaCCfSlat.get(i-1)-1;
//		}
//
//		System.out.println(" delta c due to slat " + Arrays.toString(deltaChordSlat));
//
//
//		ArrayList<Double> deltaChordSlatAsList  = new ArrayList<Double>(Arrays.asList(deltaChordSlat));
//
//		int ps=0;
//		for ( int i =0; i< yStationActualSlat.length-1 ; i++) {
//			if ( (yStationActualSlat[i+1] - yStationActualSlat[i]) > 0.03 ) {
//
//				if ( yStationActualSlat[i] == 0){
//					deltaChordSlatAsList.add(i+1+ps,0.0);	
//					ps=ps+1;
//				}
//
//				if (yStationActualSlat[i+1] ==1){
//					deltaChordSlatAsList.add(i+1+ps,0.0);		
//					ps=ps+1;}
//
//				if (yStationActualSlat[i] != 0 & yStationActualSlat[i+1] !=1){
//					//					if (Arrays.asList(etaOutFlap).contains(yStationActualFlap[i+1]) &  Arrays.asList(etaInFlap).contains(yStationActualFlap[i])){}
//					if (Arrays.binarySearch(etaOutSlat,yStationActualSlat[i+1])>=0 &  Arrays.binarySearch(etaInSlat,yStationActualSlat[i])>=0){}
//					else{
//						deltaChordSlatAsList.add(i+1+ps,0.0);
//						deltaChordSlatAsList.add(i+1+ps,0.0);
//						ps=ps+2;
//					}
//				}
//			}
//		}
//
//		System.out.println(" delta c due to slat with separation" + deltaChordSlatAsList.toString());
//
//
//		// TOTAL
//
//		// interpolation
//
//		double [] deltaChordFlapWithSeparator = new double [deltaChordFlapAsList.size()];
//		double [] deltaChordSlatWithSeparator = new double [deltaChordSlatAsList.size()];
//		for(int i=0; i<deltaChordFlapWithSeparator.length; i++){
//			deltaChordFlapWithSeparator[i] = deltaChordFlapAsList.get(i);
//			deltaChordSlatWithSeparator[i] = deltaChordSlatAsList.get(i);
//		}
//		
//		MyArray deltaChordFlapMyArray = new MyArray(deltaChordFlapWithSeparator);
//		MyArray deltaChordFlapTotal = MyArray.createArray(
//				deltaChordFlapMyArray.interpolate(
//						yStationTotalFlap,
//						yStationTotal));
//		
//		MyArray deltaChordSlatMyArray = new MyArray(deltaChordSlatWithSeparator);
//		MyArray deltaChordSlatTotal = MyArray.createArray(
//				deltaChordSlatMyArray.interpolate(
//						yStationTotalSlat,
//						yStationTotal));
//
//
//		double [] chordDistributionTotal = new double [yStationTotal.length];
//
//		for (int i=0; i<yStationTotal.length; i++){
//			chordDistributionTotal [i] = chordDistributionBase[i] + deltaChordFlapTotal.get(i) + deltaChordSlatTotal.get(i);
//		}
//		
//		System.out.println(" chord distribution total " + Arrays.toString(chordDistributionTotal));
		
		 //delta cl alpha

		Double[] alphaZeroLiftDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, alpha0lInput, yStationTotal);

		System.out.println("alpha zero lift distribution base :" + Arrays.toString(alphaZeroLiftDistributionBase));

		// alpha star

		Double[] alphaStarDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, alphaStarInput, yStationTotal);

		System.out.println("alpha star distribution base :" + Arrays.toString(alphaStarDistributionBase));

		// cl max
		Double[] clMaxDistributionBase = MyMathUtils.getInterpolatedValue1DLinear(yStationInput, clMaxInput, yStationTotal);

		System.out.println("cl max distribution base :" + Arrays.toString(clMaxDistributionBase));


		//Calculate flapped curve

		// alpha0L

		// cL alpha new

		// alpha star

		// FLAPPED STALL PATH

		// Print results

		// plot
	}
}