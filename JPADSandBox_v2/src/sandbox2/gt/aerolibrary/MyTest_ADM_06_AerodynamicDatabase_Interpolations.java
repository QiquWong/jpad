package sandbox2.gt.aerolibrary;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class MyTest_ADM_06_AerodynamicDatabase_Interpolations {

	static double wingSpan ;
	static double wingSpanInner;
	static double wingSpanOuter;
	static double lambdaLEInner ;
	static double lambdaLEOuter ;
	static double gamma ;
	static double gammaOuter ;
	static double epsmaxInner ;
	static double epsmaxOuter ;
	static double chordWingKink ;
	static double thicknessKink ;
	static double alfaZeroLiftWingKink ;
	static double clAlfaWingKink ;
	static double cmAlfaWingKink ;
	static double xACWingKink ;
	static double machKink ;
	static double machRoot ;
	static double machTip ;
	static double twistAngleWingKink ;
	static double aspectRatio ;
	static double chord ;
	static double iw;
	static double chordRootWing;
	static double thicknessRoot ;
	static double alfaZeroLiftCR ;
	static double cLalfaWR;
	static double cMACWR ;
	static double xACFromCR ;
	static double mach ;
	static double altitude;
	static double chordTipWing ;
	static double thicknessTip ;
	static double alfaZeroLiftCT ;
	static double cLalfaWT;
	static double cMACWT ;
	static double xACFromCT ;
	static double TwistAngleWingTip ;
	static double etaInnerAirelons ;
	static double etaOuterAirelons ;
	static double chordAirelons ;
	static double etaInnerFlaps ;
	static double etaOuterFlaps ;
	static double chordFlaps ;
	static double deltaZeroLiftFlaps ;
	static double dBW ;
	static double dB ;
	static double lB ;
	static double sBSide ;
	static double z1 ;
	static double z2 ;
	static double zMax ;
	static double omegaMax ;
	static double rOne ;
	static double hOne ;
	static double nBZero ;
	static double nBOne ;
	static double nBTwo ;	
	static double nBWing ;
	static double spanHT ;
	static double lambdaHt ;
	static double gammaHT ;
	static double etaTMaxHT ;
	static double iHT ;
	static double chordHTRoot ;
	static double ticknessHTRoot ;
	static double alfaZeroLiftHTRoot ;
	static double cLAlfaHT ;
	static double cMACRootHT ;
	static double epsChordRootHt;
	static double chordHTTip ;
	static double ticknessHTTip ;
	static double machHTRoot ;
	static double alfaZeroLiftHTTip ;
	static double machHTTip ;
	static double clAlfaHtTip ;
	static double cMAlfaHTTip ;
	static double epsACHTTip ;
	static double twistAngleHTTip ;
	static double dynamicPressureRatio ;
	static double etaInnerElevator ;
	static double etaOuterElevator ;
	static double chordElevator ;	
	static double spanVT ;
	static double lambdaLEVT ;
	static double gammaVT ;
	static double etaTMaxVT ;
	static double geometricTwistAngleVT ;
	static double dynamicsPressureRatioVT ;
	static double chordVTRoot ;
	static double ticknessVTRoot ;
	static double cLAlfaVTRoot ;
	static double cMACRootVT ;
	static double epsChordRootVt ;
	static double machVTRoot ;
	static double chordVTTip;
	static double ticknessVTTip ;
	static double clAlfaVtTip ;
	static double cMAlfaVTTip ;
	static double epsACVTTip ;
	static double machVTTip ;
	static double twistAngleTipVT;
	static double etaInnerRudder ;
	static double etaOuterRudder ;
	static double chordRudder ;
	static double mass ;
	static double cDZero ;
	static double xcg ;
	static double deltaXLEWNose;
	static double deltaXLEHTNose ;
	static double deltaXLEVTNose ;
	static double deltaZLEWNose ;
	static double deltaZLEHTNose ;
	static double deltaZLEVTNose ;
	static double nOne;
	static double nTwo ;
	static double deltaXCGEng1 ;
	static double deltaXCGEng2 ;
	static double yeng1 ;
	static double yeng2 ;
	static double deltaZEng1 ;
	static double deltaZEng2 ;
	static double dEng ;
	static double downwashEngine ;
	static double cNAlfaEngine ;
	static double semiSpanVT ;

	public MyTest_ADM_06_AerodynamicDatabase_Interpolations() {

		//quiwqwqwqwqwqwwqrwqewqerwqerwewqewewqewewqewqewqewq
		System.out.println("---------------------------------------------------");
		System.out.println("Reading input data file (excel format)");
		String inputFileName = "Data_Input_DC9.xlsx";
		File inputFile = new File (inputFileName) ;

		if (inputFile.exists()){
			System.out.println("File " + inputFileName + " found.");
			for (int sheetNum = 0; sheetNum < 6; sheetNum++){

				readDataFromExcelFile(inputFile,sheetNum);

			}
		}
		else {
			System.out.println("File " + inputFileName + " not found.");			
		}

		System.out.println("---------------------------------------------------");
		System.out.println("Calculating MAC, AeroLibrary.calcMeanChord");

		double TaperRatio = chordTipWing/chordRootWing;
		double density = 0.41271;
		double soundSpeed = 299.5;
		double macw = AeroLibrary.calculateMeanChord(chordRootWing, TaperRatio);
		double xmacw = AeroLibrary.calculateMeanChordLeadingEdge(wingSpan, TaperRatio, lambdaLEInner);
		double xacw = 0.285 ;
		System.out.println("cr = "+ chordRootWing + ", lambda = " + TaperRatio + ", MAC = " + macw + ", XMAC = " + xmacw + ", Xacw = " + xacw);
		System.out.println("---------------------------------------------------\n");

		System.out.println("Calculating  geometric parameters Wing  \n");
		//	    double wingSurface = AeroLibrary.calcWingSurface(wingSpan, chordRootWing, TaperRatio);
		double wingSurface = 86.216;
		double arWing = wingSpan*wingSpan/wingSurface;
		double lambdaquarterWing=AeroLibrary.calculateSweepAngle(lambdaLEInner, 0.25, TaperRatio, arWing);
		//	    double cl = AeroLibrary.calculateLiftCoefficient(mass, density, mach, soundSpeed, wingSurface);
		double cl = 0.448;
		double clAlfaWingTotal = AeroLibrary.calculateCLalfaPolhamus(arWing, lambdaLEInner, mach, TaperRatio);

		System.out.println("cr = " + chordRootWing + "\n lambda = " + TaperRatio + "\n b = "+ wingSpan + "\n S = " + wingSurface + "\n AR = " + arWing + "\n Sweep angle c/4 = " + lambdaquarterWing + "\n CL = "+ cl + "\n CL_Alfa_wing = "+ clAlfaWingTotal);
		System.out.println("---------------------------------------------------\n");


		System.out.println("Calculating geometric parameters Horizontal Tail \n");
		double taperRatioHT = chordHTTip/chordHTRoot;
		double sh = AeroLibrary.calculateWingSurface(spanHT, chordHTRoot, taperRatioHT);
		double arht = spanHT*spanHT/sh;
		double macHT = AeroLibrary.calculateMeanChord(chordHTRoot, taperRatioHT);
		double xmacht = AeroLibrary.calculateMeanChordLeadingEdge(spanHT, taperRatioHT, lambdaHt);
		double lambdaquarterHT = AeroLibrary.calculateSweepAngle( lambdaHt , 0.25 , TaperRatio, arht);
		double xacHTFromHTLE = xmacht + 0.25*macHT;



		double xacht = 4.345;
		double clalfaht = 4.639;    
		double downwash = 0.246;
		double clAlfaHTTotal = AeroLibrary.calculateCLalfaPolhamus(arht, lambdaHt, mach, taperRatioHT);

		System.out.println("cr = " + chordHTRoot + "\n lambda = " + taperRatioHT + "\n b = " + spanHT + "\n S = " + sh + "\n Sweep angle c/4 = " + lambdaquarterHT +  "\n ARHT = " + arht + "\n MAC HT = " + macHT + "\n lift slope gradient = "+ clAlfaHTTotal + "\n distance of horizontal tail aerodynamic center from h-tail apex = "+ xacHTFromHTLE);
		System.out.println("---------------------------------------------------\n");
		System.out.println(" aerodynamic center of horizontal tail from wing nose = "+xacht);
		System.out.println("---------------------------------------------------\n");

		System.out.println(" gravity center from wing nose = "+xcg);
		System.out.println("---------------------------------------------------\n");
		double a = Math.abs(xacht - xcg);
		double b = Math.abs(xacw - xcg);
		System.out.println("\n |Xacw - Xgc| = " + b + "\n |Xach-Xcg| " + a);
		System.out.println("---------------------------------------------------\n");

		//https://bitbucket.org/lorenzoexe/jpad/src
		double clalfah = AeroLibrary.calculateCLAlphaDotHTail(clalfaht, 0.9, wingSurface, sh, xcg, xacht, downwash);
		System.out.println("\n Unsteady lift Coefficient ( horizontal tail contribution) = " + clalfah);
		System.out.println("---------------------------------------------------\n");

		double clqw=AeroLibrary.calculateCLPitchRateWing(arWing, lambdaquarterWing, mach, xacw, xcg, TaperRatio);
		System.out.println("\n Unsteady lift Coefficient ( pitch contribution of wings) = " + clqw);
		System.out.println("---------------------------------------------------\n");

		double clqht=AeroLibrary.calculateCLPitchDotHTail(clalfaht, 0.9, wingSurface, sh, xcg, xacht);
		System.out.println("\n Unsteady lift Coefficient ( pitch contribution of horizontal tail) = " + clqht);
		System.out.println("---------------------------------------------------\n");

		double clq=AeroLibrary.calculateCLPitchRate(clqw, clqht);
		System.out.println("\n Unsteady lift Coefficient ( pitch contribution ) = " + clq);
		System.out.println("---------------------------------------------------\n");

		double cmalfaht=AeroLibrary.calculateCMAlphaDotHTail(clalfaht, 0.9, wingSurface, sh, xcg, xacht, downwash);
		System.out.println("\n Unsteady pitch Coefficient ( alfa contribution of horizontal tail) = " + cmalfaht);
		System.out.println("---------------------------------------------------\n"); 

		double cmqht=AeroLibrary.calculateCMPitchRateHTail(clalfaht, 0.9, wingSurface, sh, xcg, xacht);
		System.out.println("\n Unsteady pitch Coefficient ( pitch contribution of horizontal tail) = " + cmqht);
		System.out.println("---------------------------------------------------\n"); 


		double cmqw=AeroLibrary.calculateCMPitchRateWing( arWing, lambdaLEInner, mach, 0.293, xcg, 5.748, TaperRatio);
		System.out.println("\n Unsteady pitch Coefficient ( pitch contribution of wing) = " + cmqw);
		System.out.println("---------------------------------------------------\n"); 

		double cmq=AeroLibrary.calculateCMpitchDot(cmqw, cmqht);
		System.out.println("\n Unsteady pitch Coefficient  = " + cmq);
		System.out.println("---------------------------------------------------\n"); 
		System.out.println("---------------------------------------------------\n"); 

		System.out.println("Calculating geometric parameters Vertical Tail \n");
		double taperRatioVT = chordVTTip/chordVTRoot;
		double sVT= AeroLibrary.calculateWingSurface(spanVT, chordVTRoot, taperRatioVT);
		double arVT = spanVT*spanVT/sVT;

		double lambdaquarterVT = AeroLibrary.calculateSweepAngle( lambdaLEVT , 0.25 , taperRatioVT, arht);
		double macVT = AeroLibrary.calculateMeanChord(chordVTRoot, taperRatioVT);


		double arVTEffective = AeroLibrary.calculateEffectiveAspectRatioVerticalTail(semiSpanVT, rOne, taperRatioVT, -4.87, xacHTFromHTLE, macVT, sh, sVT, arVT);

		System.out.println("cr = " + chordVTRoot + "\n lambdaVT = " + taperRatioVT + "\n b = " + spanVT + "\n S = " + sVT + "\n Sweep angle c/4 = " + lambdaquarterVT +  "\n ARVT = " + arVT +"\n ARVT Effective = " + arVTEffective + "\n MAC VT = " + macVT);
		System.out.println("---------------------------------------------------\n");

		double clAlfaVT = AeroLibrary.calculateCLalfaPolhamus(arVTEffective, lambdaLEVT, mach, taperRatioVT);
		System.out.println("\n CL_alfa VT  = " + clAlfaVT);


		double cYBetaWing=AeroLibrary.calculateCYBetaWing(gamma);
		System.out.println("\n CYBeta wing contribution  = " + cYBetaWing);
		System.out.println("---------------------------------------------------\n"); 

		double kint = 0.7 ;
		double spv = 10.676;

		double cYBetaBody=AeroLibrary.calculateCYBetaBody(kint, spv, wingSurface);
		System.out.println("\n CYBeta Body contribution  = " + cYBetaBody);
		System.out.println("---------------------------------------------------\n"); 

		double cYBetaHT=AeroLibrary.calculateCYBetaHT(wingSurface, sh,lambdaquarterWing,0.98 ,3.7,arWing,gammaHT );
		System.out.println("\n CYBeta Horizontal Tail contribution  = " + cYBetaHT);
		System.out.println("---------------------------------------------------\n"); 

		double cYBetaVT = AeroLibrary.calculateCYBetaVT(clAlfaVT, wingSurface, sVT,lambdaquarterWing, 3.7, 0.98, spanVT,rOne, arWing);	
		System.out.println("\n CYBeta Vertical Tail contribution  = " + cYBetaVT);
		System.out.println("---------------------------------------------------\n"); 

		double cYDeltaRudder = AeroLibrary.calculateCYDeltaRudder(clAlfaVT, dynamicsPressureRatioVT, sVT,wingSurface, chordRudder,macVT, etaInnerRudder,etaOuterRudder,taperRatioVT);
		System.out.println("\n CYDelta Rudder  = " + cYDeltaRudder);
		System.out.println("---------------------------------------------------\n"); 

		//        double cEpsWing =-(3.4)* Math.pow(10,-5 );
		//        System.out.println("\n CEpsWing  = " + cEpsWing);
		//        double cAR =(0.2)* Math.pow(10,-3 );
		//        System.out.println("\n cAR  = " + cAR);
		double lambdameanWing = AeroLibrary.calculateSweepAngle(lambdaLEInner, 0.50, TaperRatio, arWing);
		System.out.println("\n lambdamean  = " + lambdameanWing);
		double cSW = (-1.7)*Math.pow(10, -3);
		double cLBetaWingBody = AeroLibrary.calculateCLBetaWingBody(TwistAngleWingTip, lambdaquarterWing, cl, 18.5, TaperRatio,lambdameanWing, gamma, mach, arWing, wingSpan,0.98, dB);           
		System.out.println("\n CL_Beta WB  = " + cLBetaWingBody);
		System.out.println("---------------------------------------------------\n"); 
		/*
        double cEpsHT =(-2.6)* Math.pow(10,-5 );//4.46
        System.out.println("\n CEpsWing  = " + cEpsWing);
        double cARHT =(-1)* Math.pow(10,-3 );//4.42
        System.out.println("\n cAR  = " + cAR);
        double lambdameanHT = AeroLibrary.calculateSweepAngle(lambdaHt, 0.50, taperRatioHT, arht);
        System.out.println("\n lambdamean  = " + lambdamean);
        double cSHT = (-0.9)*Math.pow(10, -3);//4.39
        double cDHT = (-1.6)*Math.pow(10, -4);//4.43
        double cLBetaHT = AeroLibrary.calculateCLBetaHorizontalTail(twistAngleHTTip, lambdaquarterHT, cEpsHT, 0.10, cARHT, cSHT, 1.11, kF, cDHT, gammaht, kMGamma, arht, sb, sht, bht, sw, bw, etah, zWing, db)
		 */

		double cLBetaVT = AeroLibrary.calculateCLBetaVerticalTail(clAlfaVT, wingSurface, sVT,lambdaquarterWing, 3.7, 0.98, spanVT,rOne, arWing, 3.68, 10.49, wingSpan, 0.0349);
		System.out.println("\n CL_Beta VT  = " + cLBetaVT);
		System.out.println("---------------------------------------------------\n"); 

		//        double lambdabeta = AeroLibrary.calculateLambdaBeta(lambdaLEInner, mach, TaperRatio, arWing, 0.25);
		//        
		//        double beta = AeroLibrary.calculateCompressibilityFactor(mach);
		//        
		//        double kappa = AeroLibrary.calculateKappaDeltaAirelons(arWing, lambdaLEInner,mach, TaperRatio);

		double reynolds = Math.pow(1.6554, 8);

		//        System.out.println("\n lambda beta  = " + lambdabeta + " beta = " + beta + " K = " + kappa);
		//        System.out.println("---------------------------------------------------\n"); 

		double cLDeltaAirelons = AeroLibrary.calculateClAirelons(arWing, lambdaLEInner, mach, TaperRatio, lambdaquarterWing, etaInnerAirelons,etaOuterAirelons, chordAirelons,macw);
		System.out.println("\n CL_DeltaAirelons  = " + cLDeltaAirelons  );
		System.out.println("---------------------------------------------------\n"); 

		double clDeltaRudder = AeroLibrary.calculateCLRudder(clAlfaVT,dynamicsPressureRatioVT,sVT,wingSurface,chordRudder,macVT,etaInnerRudder,etaOuterRudder,taperRatioVT,wingSpan ,10.49 ,3.68,0.0349);
		System.out.println("\n CL_DeltaRudder  = " + clDeltaRudder  );
		System.out.println("---------------------------------------------------\n"); 

		double cYRollPitch = AeroLibrary.calculateCYRollPitch(clAlfaVT, wingSurface,sVT,lambdaquarterWing, 3.7, 0.98, spanVT,rOne, arWing, 3.68, 10.49, wingSpan,  0.0349);
		System.out.println("\n CY Roll Pitch  = " + cYRollPitch);
		System.out.println("---------------------------------------------------\n");
		double clRollPitchWingBody = AeroLibrary.calculateCLRollRateWing(arWing, lambdaLEInner, mach, TaperRatio);
		System.out.println("\n CL_WingRollPitch  = " + clRollPitchWingBody  );
		System.out.println("---------------------------------------------------\n"); 

		double clRollPitchHT = AeroLibrary.calculateCLHTRollPitch(arht, lambdaHt, mach, taperRatioHT, -0.32, sh, wingSurface, spanHT, wingSpan);
		System.out.println("\n CL_HTRollPitch  = " + clRollPitchHT  );
		System.out.println("---------------------------------------------------\n"); 

		double clRollPitchVT = AeroLibrary.calculateCLRollPitchVT(clAlfaVT, wingSurface, sVT, lambdaquarterVT, 3.7, 0.98, spanVT,rOne,arWing, 3.68, wingSpan);
		System.out.println("\n CL_RollPitchVT  = " + clRollPitchVT  );
		System.out.println("---------------------------------------------------\n"); 

		double cyYawPitchVT = AeroLibrary.calculateCYYawRateVT(clAlfaVT, wingSurface, sVT, lambdaquarterVT, 3.7, 0.98, spanVT, cYRollPitch, rOne, 3.68, 10.49, 0.0349, wingSpan);
		System.out.println("\n Cy_YawPitchVT  = " + cyYawPitchVT  );
		System.out.println("---------------------------------------------------\n"); 

		double cLYawRateTotal = AeroLibrary.calculateCLYawRateTotal(clAlfaVT, wingSurface, sVT, lambdaquarterWing, 3.7, 0.98, spanVT,rOne, arWing, gamma, wingSpan, 0.0349, 10.49, 3.68, mach, TwistAngleWingTip, TaperRatio, cl);
		System.out.println("\n CL_YawRateTotal  = " + cLYawRateTotal  );
		System.out.println("---------------------------------------------------\n"); 

		double cNBetaBody = AeroLibrary.calculateCNBetaBody(reynolds, sBSide, wingSurface, lB, wingSpan,15.91,z1,z2,zMax,omegaMax);
		System.out.println("\n CN_BetaBody  = " + cNBetaBody  );
		System.out.println("---------------------------------------------------\n"); 

		double cNBetaVT = AeroLibrary.calculateCNVerticalTail(clAlfaVT, wingSurface, sVT, lambdaLEVT, 3.7, 0.98, spanVT,rOne, arWing,3.68, 10.49, 0.0349, wingSpan);
		System.out.println("\n CN_BetaVT  = " + cNBetaVT  );
		System.out.println("---------------------------------------------------\n"); 

		double cNDeltaAirelons = AeroLibrary.calculateCNAirelons(arWing,etaInnerAirelons,etaOuterAirelons, cl, arWing, lambdaLEInner,mach, TaperRatio, lambdaquarterWing, etaInnerAirelons,etaOuterAirelons, chordAirelons,macw);
		System.out.println("\n CN_DeltaAirelons  = " + cNDeltaAirelons  );
		System.out.println("---------------------------------------------------\n"); 

		double cNDeltaRudder = AeroLibrary.calculateCNRudder(clAlfaVT, dynamicsPressureRatioVT, sVT, wingSurface, chordRudder,macVT,etaInnerRudder,etaOuterRudder,taperRatioVT, 3.68, 10.49, 0.0349, wingSpan);
		System.out.println("\n CN_DeltaRudder  = " + cNDeltaRudder  );
		System.out.println("---------------------------------------------------\n"); 

		double cNRollRateWB = AeroLibrary.calculateCNRollRateWB(arWing, lambdaquarterWing, xcg, xacw, mach, cl,TaperRatio,TwistAngleWingTip*57.3);
		System.out.println("\n CN_RollRateWB  = " + cNRollRateWB  );
		System.out.println("---------------------------------------------------\n");

		double cNRollRateVT = AeroLibrary.calculateCNRollRateVerticalTail(clAlfaVT, wingSurface, sVT, lambdaquarterVT, 3.7, 0.98, spanVT,rOne, arWing, wingSpan,  0.0349, 10.49,3.68);
		System.out.println("\n CN_RollRateVT  = " + cNRollRateVT  );
		System.out.println("---------------------------------------------------\n");

		double cNYawRateTotal = AeroLibrary.calculateCNYawRateTotal(TaperRatio, cl,0.602,0.3,lambdameanWing,0, clAlfaVT, wingSurface, sVT, lambdaquarterVT, 3.7, 0.98, spanVT,rOne, arWing, wingSpan, 0.0349, 10.49,3.68); 
		System.out.println("\n CN_YawRateTotal  = " + cNYawRateTotal  );
		System.out.println("---------------------------------------------------\n");

		double prova = AeroLibrary.calculateAilerons(4, 0.5, 40, 0.3, 0.7);
		System.out.println("\n provaa = "+prova);
		System.out.println("---------------------------------------------------\n");

		double prova1 = AeroLibrary.calculateCNYawRateWingBody(1, 0.2, 5, 60, 0.6, 0.4, 0.2);
		System.out.println("\n prova1 = "+prova1);



	}


	public static void readDataFromExcelFile(File file, int sheetNumber) {
		try {
			System.out.println("Input file: " + file.getAbsolutePath());
			Workbook wb = WorkbookFactory.create(file);
			Sheet ws = wb.getSheetAt(sheetNumber);
			int rowNum = ws.getLastRowNum() + 1;

			if(sheetNumber == 0){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n Flight and Geometric Aircraft Data ");
				System.out.println("---------------------------------------------------\n");

			}

			else if (sheetNumber == 1){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n Wing Data ");
				System.out.println("---------------------------------------------------\n");
			}

			else if (sheetNumber == 2){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n Fuselage Data ");
				System.out.println("---------------------------------------------------\n");
			}

			else if (sheetNumber == 3){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n Horizontal Tail Data ");
				System.out.println("---------------------------------------------------\n");
			}

			else if (sheetNumber == 4){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n Vertical Tail Data ");
				System.out.println("---------------------------------------------------\n");
			}


			else if (sheetNumber == 5){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n Engine Data ");
				System.out.println("---------------------------------------------------\n");
			}

			System.out.println("Number of rows: " + rowNum);

			for (int i = 0 ; i < rowNum ; i++) {
				Row row = ws.getRow(i);
				int colNum = ws.getRow(0).getLastCellNum();
				for (int j = 0 ; j < colNum ; j++) {

					Cell cell = row.getCell(j);
					String value = cellToString(cell);
					switch (sheetNumber){
					// First sheet, Sheet n. 1
					case 0:
						if ((i == 1) && (j == 1)) {
							altitude = Double.parseDouble(value);
							System.out.println("FlightAltitude = " + altitude);
						}
						//-------------------------------------------------------------------------------------------
						// chord
						if ((i == 2) && (j == 1)) {
							mach = Double.parseDouble(value);
							System.out.println("Mach number = " + mach);

						}

						// Aircraft weigth
						if ((i == 3) && (j == 1)) {
							mass = Double.parseDouble(value);
							System.out.println("Aircraft weight = " + mass);
						}

						//-------------------------------------------------------------------------------------------
						// Aircraft drag coefficient at zero lift
						if ((i == 4) && (j == 1)) {
							cDZero = Double.parseDouble(value);
							System.out.println("Aircraft drag coefficient at zero lift = " + cDZero);

						}

						//-------------------------------------------------------------------------------------------
						// Center of gravity position in percent of mean aerodynamic chord
						if ((i == 5) && (j == 1)) {
							xcg = Double.parseDouble(value);
							System.out.println("Center of gravity position in percent of mean aerodynamic chord = " + xcg);

						}

						//-------------------------------------------------------------------------------------------
						// Longitudinal distance of wing leading edge from fuselage nose
						if ((i == 6) && (j == 1)) {
							deltaXLEHTNose = Double.parseDouble(value);
							System.out.println("Longitudinal distance of wing leading edge from fuselage nose  = " + deltaXLEHTNose);

						}

						//-------------------------------------------------------------------------------------------
						// Longitudinal distance of horizontal tail leading edge from fuselage nose
						if ((i == 7) && (j == 1)) {
							deltaXLEHTNose = Double.parseDouble(value);
							System.out.println("Longitudinal distance of horizontal tail leading edge from fuselage nose  = " + deltaXLEHTNose);

						}	      

						//-------------------------------------------------------------------------------------------
						//  Longitudinal distance of vertical tail leading edge from fuselage nose
						if ((i == 8) && (j == 1)) {
							deltaXLEVTNose = Double.parseDouble(value);
							System.out.println("Longitudinal distance of vertical tail leading edge from fuselage nose  = " + deltaXLEVTNose);

						}

						//-------------------------------------------------------------------------------------------
						// Vertical distance of wing leading edge from fuselage reference line
						if ((i == 9) && (j == 1)) {
							deltaZLEWNose = Double.parseDouble(value);
							System.out.println("Vertical distance of wing leading edge from fuselage reference line  = " + deltaZLEWNose);

						}

						//-------------------------------------------------------------------------------------------
						// Vertical distance of horizontal tail leading edge from wing leading edge
						if ((i == 10) && (j == 1)) {
							deltaZLEHTNose = Double.parseDouble(value);
							System.out.println("Vertical distance of horizontal tail leading edge from wing leading edge = " + deltaZLEHTNose);

						}

						//-------------------------------------------------------------------------------------------
						//  Vertical distance of vertical tail leading edge from fuselage nose
						if ((i == 11) && (j == 1)) {
							deltaZLEVTNose = Double.parseDouble(value);
							System.out.println("Vertical distance of vertical tail leading edge from fuselage nose  = " + deltaZLEVTNose);

						}

						break;

						// Sheet n. 2
					case 1:


						//-------------------------------------------------------------------------------------------
						// WingSpanTotal
						if ((i == 1) && (j == 1)) {
							wingSpan = Double.parseDouble(value);
							System.out.println("Wing Span = " + wingSpan);
						}
						//-------------------------------------------------------------------------------------------
						// WingSpan Inner panel for cranked wing
						if ((i == 2) && (j == 1)) {
							wingSpanInner = Double.parseDouble(value);
							//digital datcom,openvspaero.org,
							wingSpanOuter = wingSpan - wingSpanInner;

							System.out.println("Wing Span inner panel = " + wingSpanInner + "\n Wing Span outer panel = "+ wingSpanOuter);
						}
						//-------------------------------------------------------------------------------------------
						// Sweep Angle LE inner panel 
						if ((i == 3) && (j == 1)) {
							lambdaLEInner = Double.parseDouble(value);
							System.out.println("Sweep Angle Leaving Edge Wing Inner panel = " + lambdaLEInner);

						}
						//-------------------------------------------------------------------------------------------
						// Sweep Angle LE outer panel 
						if ((i == 4) && (j == 1)) {
							lambdaLEOuter = Double.parseDouble(value);
							System.out.println("Sweep Angle Leaving Edge Wing outer panel = " + lambdaLEOuter);

						}
						//-------------------------------------------------------------------------------------------
						// Diedhral Angle Wing inner angle 
						if ((i == 5) && (j == 1)) {
							gamma = Double.parseDouble(value);
							System.out.println("Diehdral Angle Wing inner panel = " + gamma);

						}
						//-------------------------------------------------------------------------------------------
						// Diedhral Angle Wing outer angle 
						if ((i == 6) && (j == 1)) {
							gammaOuter = Double.parseDouble(value);
							System.out.println("Diehdral Angle Wing outer panel = " + gammaOuter);

						}
						//-------------------------------------------------------------------------------------------
						// Section's mean perc. position of max thickness of inner panel
						if ((i == 7) && (j == 1)) {
							epsmaxInner = Double.parseDouble(value);
							System.out.println("Section's mean perc. position of max thickness of inner panel  = " + epsmaxInner);

						}
						//-------------------------------------------------------------------------------------------
						// Section's mean perc. position of max thickness of outer panel
						if ((i == 8) && (j == 1)) {
							epsmaxOuter = Double.parseDouble(value);
							System.out.println("Section's mean perc. position of max thickness of outer panel  = " + epsmaxOuter);

						}

						//-------------------------------------------------------------------------------------------
						// wing rigging angle
						if ((i == 9) && (j == 1)) {
							iw = Double.parseDouble(value);
							System.out.println("wing rigging angle  = " + iw);

						}	      

						//-------------------------------------------------------------------------------------------
						//  Root chord wing
						if ((i == 10) && (j == 1)) {
							chordRootWing = Double.parseDouble(value);
							System.out.println("Root chord wing (in meters)  = " + chordRootWing);

						}

						//-------------------------------------------------------------------------------------------
						// Maximum thickness root chord wing
						if ((i == 11) && (j == 1)) {
							thicknessRoot = Double.parseDouble(value);
							System.out.println("Maximum thickness root chord wing (in meters)  = " + thicknessRoot);

						}

						//-------------------------------------------------------------------------------------------
						//  Alfa Zero lift chord root wing 
						if ((i == 12) && (j == 1)) {
							alfaZeroLiftCR = Double.parseDouble(value);
							System.out.println("Zero lift angle at root chord  = " + alfaZeroLiftCR);

						}

						//-------------------------------------------------------------------------------------------
						//  Lift-curve slope at root
						if ((i == 13) && (j == 1)) {
							cLalfaWR = Double.parseDouble(value);
							System.out.println("Lift-curve slope at root  = " + cLalfaWR);

						}

						//-------------------------------------------------------------------------------------------
						//  Pitching moment coefficient at wing root chord evaluated at the condition of zero angle of attack
						if ((i == 14) && (j == 1)) {
							cMACWR = Double.parseDouble(value);
							System.out.println("Pitching moment coefficient at wing root chord evaluated at the condition of zero angle of attack  = " + cMACWR);

						}

						//-------------------------------------------------------------------------------------------
						// Section's aerodynamic center at root chord
						if ((i == 15) && (j == 1)) {
							xACFromCR = Double.parseDouble(value);
							System.out.println("Section's aerodynamic center at root chord  = " + xACFromCR);

						}
						//-------------------------------------------------------------------------------------------
						// Critical Mach number at root chord
						if ((i == 16) && (j == 1)) {
							machRoot = Double.parseDouble(value);
							System.out.println("Critical Mach number at root chord  = " + machRoot);

						}
						//-------------------------------------------------------------------------------------------
						// Wing chord at kink between inner and outer panel
						if ((i == 17) && (j == 1)) {
							chordWingKink = Double.parseDouble(value);
							System.out.println("Wing chord at kink between inner and outer panel  = " + chordWingKink);

						}
						//-------------------------------------------------------------------------------------------
						// Relative thickness at kink between inner and outer panel
						if ((i == 18) && (j == 1)) {
							thicknessKink = Double.parseDouble(value);
							System.out.println("Relative thickness at kink between inner and outer panel  = " + thicknessKink);

						}
						//-------------------------------------------------------------------------------------------
						// Alpha zero lift angle at kink between inner and outer panel
						if ((i == 19) && (j == 1)) {
							alfaZeroLiftWingKink = Double.parseDouble(value);
							System.out.println("Alpha zero lift angle at kink between inner and outer panel  = " + alfaZeroLiftWingKink);

						}
						//-------------------------------------------------------------------------------------------
						// Lift-curve slope at kink between inner and outer panel
						if ((i == 20) && (j == 1)) {
							clAlfaWingKink = Double.parseDouble(value);
							System.out.println("Lift-curve slope at kink between inner and outer panel  = " + clAlfaWingKink);

						}
						//-------------------------------------------------------------------------------------------
						// Pitching moment coefficient at kink between inner and outer panel evaluated at the condition of zero angle of attack
						if ((i == 21) && (j == 1)) {
							cmAlfaWingKink = Double.parseDouble(value);
							System.out.println("Pitching moment coefficient at kink between inner and outer panel evaluated at the condition of zero angle of attack  = " + cmAlfaWingKink);

						}
						//-------------------------------------------------------------------------------------------
						// Section's aerodynamic center at chord between inner and outer panel
						if ((i == 22) && (j == 1)) {
							xACWingKink = Double.parseDouble(value);
							System.out.println("Section's aerodynamic center at chord between inner and outer panel  = " + xACWingKink);

						}
						//-------------------------------------------------------------------------------------------
						// Critical Mach number at chord between inner and outer panel
						if ((i == 23) && (j == 1)) {
							machKink = Double.parseDouble(value);
							System.out.println("Critical Mach number at chord between inner and outer panel  = " + machKink);

						}
						//-------------------------------------------------------------------------------------------
						// Inner panel twist angle
						if ((i == 24) && (j == 1)) {
							twistAngleWingKink = Double.parseDouble(value);
							System.out.println("Inner panel twist angle  = " + twistAngleWingKink);

						}
						//-------------------------------------------------------------------------------------------
						// chord tip wing
						if ((i == 25) && (j == 1)) {
							chordTipWing = Double.parseDouble(value);
							System.out.println("chord tip wing  = " + chordTipWing);

						}

						//-------------------------------------------------------------------------------------------
						// Maximum thickness root chord wing
						if ((i == 26) && (j == 1)) {
							thicknessTip = Double.parseDouble(value);
							System.out.println("Maximum thickness tip chord wing (in meters)  = " + thicknessTip);

						}

						//-------------------------------------------------------------------------------------------
						//  Alfa Zero lift chord tip wing 
						if ((i == 27) && (j == 1)) {
							alfaZeroLiftCT = Double.parseDouble(value);
							System.out.println("Zero lift angle at tip  = " + alfaZeroLiftCT);

						}

						//-------------------------------------------------------------------------------------------
						//  Lift-curve slope at tip
						if ((i == 28) && (j == 1)) {
							cLalfaWT = Double.parseDouble(value);
							System.out.println("Lift-curve slope at tip  = " + cLalfaWT);

						}

						//-------------------------------------------------------------------------------------------
						//  Pitching moment coefficient at wing tip chord evaluated at the condition of zero angle of attack
						if ((i == 29) && (j == 1)) {
							cMACWT = Double.parseDouble(value);
							System.out.println("Pitching moment coefficient at wing tip chord evaluated at the condition of zero angle of attack  = " + cMACWT);

						}

						//-------------------------------------------------------------------------------------------
						// Section's aerodynamic center at tip chord
						if ((i == 30) && (j == 1)) {
							xACFromCT = Double.parseDouble(value);
							System.out.println("Section's aerodynamic center at tip chord  = " + xACFromCT);

						}
						//-------------------------------------------------------------------------------------------
						// Critical Mach number at outer panel tip chord
						if ((i == 31) && (j == 1)) {
							machTip = Double.parseDouble(value);
							System.out.println("Critical Mach number at outer panel tip chord  = " + machTip);

						}
						//-------------------------------------------------------------------------------------------
						// twist angle tip
						if ((i == 32) && (j == 1)) {
							TwistAngleWingTip = Double.parseDouble(value);
							System.out.println("Twist Angle at tip chord  = " + TwistAngleWingTip);

						}

						//-------------------------------------------------------------------------------------------
						// Aileron inner station over wing semi-span
						if ((i == 33) && (j == 1)) {
							etaInnerAirelons = Double.parseDouble(value);
							System.out.println("Aileron inner station over wing semi-span  = " + etaInnerAirelons);

						}

						//-------------------------------------------------------------------------------------------
						// Aileron outer station over wing semi-span
						if ((i == 34) && (j == 1)) {
							etaOuterAirelons = Double.parseDouble(value);
							System.out.println("Aileron outer station over wing semi-span  = " + etaOuterAirelons);

						}

						//-------------------------------------------------------------------------------------------
						// Airelons chord 
						if ((i == 35) && (j == 1)) {
							chordAirelons = Double.parseDouble(value);
							System.out.println("Airelons chord   = " + chordAirelons);

						}

						//-------------------------------------------------------------------------------------------
						// Flap inner station over wing semi-span
						if ((i == 36) && (j == 1)) {
							etaInnerFlaps = Double.parseDouble(value);
							System.out.println("Flaps inner station over wing semi-span  = " + etaInnerFlaps);

						}

						//-------------------------------------------------------------------------------------------
						// Flap outer station over wing semi-span
						if ((i == 37) && (j == 1)) {
							etaOuterFlaps = Double.parseDouble(value);
							System.out.println("Flaps outer station over wing semi-span  = " + etaOuterFlaps);

						}

						//-------------------------------------------------------------------------------------------
						// Flap chord 
						if ((i == 38) && (j == 1)) {
							chordFlaps = Double.parseDouble(value);
							System.out.println("Flap chord   = " + chordFlaps);

						}

						//-------------------------------------------------------------------------------------------
						// Zero lift angle decrease due to flaps deflection
						if ((i == 39) && (j == 1)) {
							deltaZeroLiftFlaps = Double.parseDouble(value);
							System.out.println("Zero lift angle decrease due to flaps deflection   = " + deltaZeroLiftFlaps);

						}
						//-------------------------------------------------------------------------------------------
						// TO DO: put code here to read values from file
						// ...

						break;

						// Sheet n. 3
					case 2:

						//-------------------------------------------------------------------------------------------
						// Fuselage diameter at wing leading edge
						if ((i == 1) && (j == 1)) {
							dBW = Double.parseDouble(value);
							System.out.println("Fuselage diameter at wing leading edge = " + dBW);
						}
						//-------------------------------------------------------------------------------------------
						// Mean fuselage diameter
						if ((i == 2) && (j == 1)) {
							dB = Double.parseDouble(value);
							System.out.println("Mean fuselage diameter = " + dB);

						}

						//-------------------------------------------------------------------------------------------
						// Fuselage length
						if ((i == 3) && (j == 1)) {
							lB = Double.parseDouble(value);
							System.out.println("Fuselage length = " + lB);

						}

						//-------------------------------------------------------------------------------------------
						// Fuselage side area
						if ((i == 4) && (j == 1)) {
							sBSide = Double.parseDouble(value);
							System.out.println("Fuselage side area  = " + sBSide);

						}

						//-------------------------------------------------------------------------------------------
						// Fuselage vertical width at 0.25 fuselage length
						if ((i == 5) && (j == 1)) {
							z1 = Double.parseDouble(value);
							System.out.println("Fuselage vertical width at 0.25 fuselage length  = " + z1);

						}	      

						//-------------------------------------------------------------------------------------------
						//  Fuselage vertical width at 0.75 fuselage length
						if ((i == 6) && (j == 1)) {
							z2 = Double.parseDouble(value);
							System.out.println("Fuselage vertical width at 0.75 fuselage length  = " + z2);

						}

						//-------------------------------------------------------------------------------------------
						// Max fuselage vertical width
						if ((i == 7) && (j == 1)) {
							zMax = Double.parseDouble(value);
							System.out.println("Max fuselage vertical width  = " + zMax);

						}

						//-------------------------------------------------------------------------------------------
						// Max fuselage vertical width from top view
						if ((i == 8) && (j == 1)) {
							omegaMax = Double.parseDouble(value);
							System.out.println("Max fuselage vertical width from top view = " + omegaMax);

						}

						//-------------------------------------------------------------------------------------------
						//  Fuselage height at quarter vertical tail root chord
						if ((i == 9) && (j == 1)) {
							rOne = Double.parseDouble(value);
							System.out.println("Fuselage height at quarter vertical tail root chord  = " + rOne);

						}

						//-------------------------------------------------------------------------------------------
						//  Fuselage height at vertical tail root chord
						if ((i == 10) && (j == 1)) {
							hOne = Double.parseDouble(value);
							System.out.println("Fuselage height at vertical tail root chord  = " + hOne);

						}
						//-------------------------------------------------------------------------------------------
						// Number of divisions +1 when computing C_M0_B
						if ((i == 11) && (j == 1)) {
							nBZero = Double.parseDouble(value);
							System.out.println("Number of divisions +1 when computing C_M0_B = " + nBZero);

						}

						//-------------------------------------------------------------------------------------------
						//  Number of divisions +1 when computing C_M_Alpha_B, from Nose to wing LE
						if ((i == 12) && (j == 1)) {
							nBOne = Double.parseDouble(value);
							System.out.println("Number of divisions +1 when computing C_M_Alpha_B, from Nose to wing LE  = " + nBOne);

						}

						//-------------------------------------------------------------------------------------------
						//  Number of divisions +1 when computing C_M_Alpha_B, from wing TE to tail
						if ((i == 13) && (j == 1)) {
							nBTwo = Double.parseDouble(value);
							System.out.println("Number of divisions +1 when computing C_M_Alpha_B, from wing TE to tail  = " + nBTwo);

						}

						//-------------------------------------------------------------------------------------------
						//  Number of division +1 when computing delta_x_ac_B, from wing LE to wing TE (must be at least equal to 3)
						if ((i == 14) && (j == 1)) {
							nBWing = Double.parseDouble(value);
							System.out.println("Number of division +1 when computing delta_x_ac_B, from wing LE to wing TE (must be at least equal to 3)  = " + nBWing);

						}


						break;

						// Sheet n. 4
					case 3:

						//-------------------------------------------------------------------------------------------
						// Horizontal tail span
						if ((i == 1) && (j == 1)) {
							spanHT = Double.parseDouble(value);
							System.out.println("Horizontal tail span = " + spanHT);
						}
						//-------------------------------------------------------------------------------------------
						//Horizontal tail leading edge sweep angle
						if ((i == 2) && (j == 1)) {
							lambdaHt = Double.parseDouble(value);
							System.out.println("Horizontal tail leading edge sweep angle = " + lambdaHt);

						}

						//-------------------------------------------------------------------------------------------
						// Horizontal tail dihedrical angle
						if ((i == 3) && (j == 1)) {
							gammaHT = Double.parseDouble(value);
							System.out.println("Horizontal tail dihedrical angle = " + gammaHT);

						}

						//-------------------------------------------------------------------------------------------
						// Section's mean perc. position of max thickness HT
						if ((i == 4) && (j == 1)) {
							etaTMaxHT = Double.parseDouble(value);
							System.out.println("Section's mean perc. position of max thickness VT  = " + etaTMaxHT);

						}

						//-------------------------------------------------------------------------------------------
						// Incidence angle
						if ((i == 5) && (j == 1)) {
							iHT = Double.parseDouble(value);
							System.out.println("Incidence angle  = " + iHT);

						}	      

						//-------------------------------------------------------------------------------------------
						//  Horizontal Tail root chord
						if ((i == 6) && (j == 1)) {
							chordHTRoot = Double.parseDouble(value);
							System.out.println("Horizontal Tail root chord  = " + chordHTRoot);

						}

						//-------------------------------------------------------------------------------------------
						// Relative thickness at root chord
						if ((i == 7) && (j == 1)) {
							ticknessHTRoot = Double.parseDouble(value);
							System.out.println("Relative thickness at root chord  = " + ticknessHTRoot);

						}

						//-------------------------------------------------------------------------------------------
						// Alpha zero lift angle at root chord
						if ((i == 8) && (j == 1)) {
							alfaZeroLiftHTRoot = Double.parseDouble(value);
							System.out.println("Alpha zero lift angle at root chord = " + alfaZeroLiftHTRoot);

						}

						//-------------------------------------------------------------------------------------------
						//  Lift-curve slope at root chord
						if ((i == 9) && (j == 1)) {
							cLAlfaHT = Double.parseDouble(value);
							System.out.println("Lift-curve slope at root chord  = " + cLAlfaHT);

						}

						//-------------------------------------------------------------------------------------------
						//  Pitching moment coefficient at root chord evaluated at the condition of zero angle of attack
						if ((i == 10) && (j == 1)) {
							cMACRootHT = Double.parseDouble(value);
							System.out.println("Pitching moment coefficient at root chord evaluated at the condition of zero angle of attack  = " + cMACRootHT);

						}
						//-------------------------------------------------------------------------------------------
						// Section's aerodynamic center at root chord
						if ((i == 11) && (j == 1)) {
							epsChordRootHt = Double.parseDouble(value);
							System.out.println("Section's aerodynamic center at root chord = " + epsChordRootHt);

						}

						//-------------------------------------------------------------------------------------------
						//Critical Mach number at root chord

						if ((i == 12) && (j == 1)) {
							machHTRoot = Double.parseDouble(value);
							System.out.println("Critical Mach number at root chord  = " + machHTRoot);

						}
						//-------------------------------------------------------------------------------------------
						// Horizontal Tail tip chord
						if ((i == 13) && (j == 1)) {
							chordHTTip = Double.parseDouble(value);
							System.out.println("Horizontal Tail tip chord  = " + chordHTTip);

						}
						//-------------------------------------------------------------------------------------------
						// Relative thickness at tip chord
						if ((i == 14) && (j == 1)) {
							ticknessHTTip = Double.parseDouble(value);
							System.out.println("Relative thickness at tip chord = " + ticknessHTTip);

						}
						//-------------------------------------------------------------------------------------------
						//  Alpha zero lift angle at tip chord
						if ((i == 15) && (j == 1)) {
							alfaZeroLiftHTTip = Double.parseDouble(value);
							System.out.println("Alpha zero lift angle at tip chord  = " + alfaZeroLiftHTTip);

						}
						//-------------------------------------------------------------------------------------------
						// Lift-curve slope at tip chord
						if ((i == 16) && (j == 1)) {
							clAlfaHtTip = Double.parseDouble(value);
							System.out.println("Lift-curve slope at tip chord = " + clAlfaHtTip);

						}
						//-------------------------------------------------------------------------------------------
						//  Pitching moment coefficient at tip chord evaluated at the condition of zero angle of attack
						if ((i == 17) && (j == 1)) {
							cMAlfaHTTip = Double.parseDouble(value);
							System.out.println("Pitching moment coefficient at tip chord evaluated at the condition of zero angle of attack  = " + cMAlfaHTTip);

						}
						//-------------------------------------------------------------------------------------------
						// Section's aerodynamic center at tip chord
						if ((i == 18) && (j == 1)) {
							epsACHTTip = Double.parseDouble(value);
							System.out.println("Section's aerodynamic center at tip chord = " + epsACHTTip);

						}
						//-------------------------------------------------------------------------------------------
						//Critical Mach number at tip chord
						if ((i == 19) && (j == 1)) {
							machHTTip = Double.parseDouble(value);
							System.out.println("Critical Mach number at tip chord  = " + machHTTip);

						}
						//-------------------------------------------------------------------------------------------
						// Twist angle at Horizontal Tail tip
						if ((i == 20) && (j == 1)) {
							twistAngleHTTip = Double.parseDouble(value);
							System.out.println("Twist angle at Horizontal Tail tip = " + twistAngleHTTip);

						}
						//-------------------------------------------------------------------------------------------
						// Dynamic pressure ratio
						if ((i == 21) && (j == 1)) {
							dynamicPressureRatio = Double.parseDouble(value);
							System.out.println("Dynamic pressure ratio = " + dynamicPressureRatio);

						}
						//-------------------------------------------------------------------------------------------
						//Elevator inner station
						if ((i == 22) && (j == 1)) {
							etaInnerElevator = Double.parseDouble(value);
							System.out.println("Elevator inner station  = " + etaInnerElevator);
						}//-------------------------------------------------------------------------------------------
						//  Elevator outer station
						if ((i == 23) && (j == 1)) {
							etaOuterElevator = Double.parseDouble(value);
							System.out.println("Elevator outer station = " + etaOuterElevator);
						}
						//-------------------------------------------------------------------------------------------
						// Elevator chord
						if ((i == 24) && (j == 1)) {
							chordElevator = Double.parseDouble(value);
							System.out.println("Elevator chord = " + chordElevator);

						}


						break;

						// Sheet n. 5
					case 4:

						//-------------------------------------------------------------------------------------------
						// Semi-Vertical tail span
						if ((i == 1) && (j == 1)) {
							semiSpanVT = Double.parseDouble(value);
							System.out.println("Semi-Vertical tail span = " + semiSpanVT);
							spanVT = 2*semiSpanVT;
						}
						//-------------------------------------------------------------------------------------------
						//Vertical tail leading edge sweep angle
						if ((i == 2) && (j == 1)) {
							lambdaLEVT = Double.parseDouble(value);
							System.out.println("Vertical tail leading edge sweep angle = " + lambdaLEVT);

						}

						//-------------------------------------------------------------------------------------------
						// Vertical tail dihedrical angle
						if ((i == 3) && (j == 1)) {
							gammaVT = Double.parseDouble(value);
							System.out.println("Vertical tail dihedrical angle = " + gammaVT);

						}

						//-------------------------------------------------------------------------------------------
						// Section's mean perc. position of max thickness VT
						if ((i == 4) && (j == 1)) {
							etaTMaxVT = Double.parseDouble(value);
							System.out.println("Section's mean perc. position of max thickness VT  = " + etaTMaxVT);

						}

						//-------------------------------------------------------------------------------------------
						//Geometric Twist Angle VT
						if ((i == 5) && (j == 1)) {
							geometricTwistAngleVT = Double.parseDouble(value);
							System.out.println("Geometric Twist Angle VT  = " + geometricTwistAngleVT);

						}	      

						//-------------------------------------------------------------------------------------------
						//  Dynamic pressure ratio
						if ((i == 6) && (j == 1)) {
							dynamicsPressureRatioVT = Double.parseDouble(value);
							System.out.println("Dynamic pressure ratio  = " + dynamicsPressureRatioVT);

						}

						//-------------------------------------------------------------------------------------------
						// Vertical tail root chord
						if ((i == 7) && (j == 1)) {
							chordVTRoot = Double.parseDouble(value);
							System.out.println("Vertical tail root chord = " + chordVTRoot);

						}

						//-------------------------------------------------------------------------------------------
						// Relative thickness at root chord
						if ((i == 8) && (j == 1)) {
							ticknessVTRoot = Double.parseDouble(value);
							System.out.println("Relative thickness at root chord = " + ticknessVTRoot);

						}

						//-------------------------------------------------------------------------------------------
						// Lift-curve slope at root chord
						if ((i == 9) && (j == 1)) {
							cLAlfaVTRoot = Double.parseDouble(value);
							System.out.println("Lift-curve slope at root chord = " + cLAlfaVTRoot);

						}

						//-------------------------------------------------------------------------------------------
						//  Pitching moment coefficient at root chord evaluated at the condition of zero angle of attack
						if ((i == 10) && (j == 1)) {
							cMACRootVT = Double.parseDouble(value);
							System.out.println("Pitching moment coefficient at root chord evaluated at the condition of zero angle of attack  = " + cMACRootVT);

						}
						//-------------------------------------------------------------------------------------------
						// Section's aerodynamic center at root chord
						if ((i == 11) && (j == 1)) {
							epsChordRootVt = Double.parseDouble(value);
							System.out.println("Section's aerodynamic center at root chord = " + epsChordRootVt);

						}

						//-------------------------------------------------------------------------------------------
						//Critical Mach number at root chord

						if ((i == 12) && (j == 1)) {
							machVTRoot = Double.parseDouble(value);
							System.out.println("Critical Mach number at root chord  = " + machVTRoot);

						}
						//-------------------------------------------------------------------------------------------
						// Vertical Tail tip chord
						if ((i == 13) && (j == 1)) {
							chordVTTip = Double.parseDouble(value);
							System.out.println("Horizontal Tail tip chord  = " + chordVTTip);

						}



						//-------------------------------------------------------------------------------------------
						// Relative thickness at tip chord
						if ((i == 14) && (j == 1)) {
							ticknessVTTip = Double.parseDouble(value);
							System.out.println("Relative thickness at tip chord = " + ticknessVTTip);

						}
						//-------------------------------------------------------------------------------------------
						//Lift-curve slope at tip chord
						if ((i == 15) && (j == 1)) {
							clAlfaVtTip = Double.parseDouble(value);
							System.out.println("Lift-curve slope at tip chord  = " + clAlfaVtTip);

						}
						//-------------------------------------------------------------------------------------------
						// Pitching moment coefficient at tip chord evaluated at the condition of zero angle of attack
						if ((i == 16) && (j == 1)) {
							cMAlfaVTTip = Double.parseDouble(value);
							System.out.println("Pitching moment coefficient at tip chord evaluated at the condition of zero angle of attack = " + cMAlfaVTTip);

						}
						//-------------------------------------------------------------------------------------------
						//  Section's aerodynamic center at tip chord
						if ((i == 17) && (j == 1)) {
							epsACVTTip = Double.parseDouble(value);
							System.out.println("Section's aerodynamic center at tip chord  = " + epsACVTTip);

						}
						//-------------------------------------------------------------------------------------------
						// Critical Mach number at tip chord
						if ((i == 18) && (j == 1)) {
							machVTTip = Double.parseDouble(value);
							System.out.println("Critical Mach number at tip chord = " + machVTTip);

						}

						//-------------------------------------------------------------------------------------------
						// Rudder inner station
						if ((i == 19) && (j == 1)) {
							etaInnerRudder = Double.parseDouble(value);
							System.out.println("Rudder inner station = " + etaInnerRudder);

						}


						//-------------------------------------------------------------------------------------------
						// Rudder outer station
						if ((i == 20) && (j == 1)) {
							etaOuterRudder = Double.parseDouble(value);
							System.out.println("Rudder outer station = " + etaOuterRudder);

						}


						//-------------------------------------------------------------------------------------------
						// Rudder chord
						if ((i == 21) && (j == 1)) {
							chordRudder = Double.parseDouble(value);
							System.out.println("Rudder chord = " + chordRudder);

						}


						break;

						//6°foglio
					case 5:		
						//-------------------------------------------------------------------------------------------
						//Number of engines from the first set
						if ((i == 1) && (j == 1)) {
							nOne = Double.parseDouble(value);
							System.out.println("Number of engines from the first set = " + nOne);
						}
						//-------------------------------------------------------------------------------------------
						// Number of engines from the second set
						if ((i == 2) && (j == 1)) {
							nTwo = Double.parseDouble(value);
							System.out.println("Number of engines from the second set = " + nTwo);

						}

						// Longitudinal distance of first set of engines from the aircraft center of gravity 
						if ((i == 3) && (j == 1)) {
							deltaXCGEng1 = Double.parseDouble(value);
							System.out.println("Longitudinal distance of first set of engines from the aircraft center of gravity  = " + deltaXCGEng1);
						}

						//-------------------------------------------------------------------------------------------
						// Longitudinal distance of second set of engines from the aircraft center of gravity 
						if ((i == 4) && (j == 1)) {
							deltaXCGEng2 = Double.parseDouble(value);
							System.out.println("Longitudinal distance of second set of engines from the aircraft center of gravity  = " + deltaXCGEng2);

						}

						//-------------------------------------------------------------------------------------------
						// Lateral distance of first set of engines
						if ((i == 5) && (j == 1)) {
							yeng1 = Double.parseDouble(value);
							System.out.println("Lateral distance of first set of engines = " + yeng1);

						}

						//-------------------------------------------------------------------------------------------
						// Lateral distance of second set of engines
						if ((i == 6) && (j == 1)) {
							yeng2 = Double.parseDouble(value);
							System.out.println("Lateral distance of second set of engines  = " + yeng2);

						}

						//-------------------------------------------------------------------------------------------
						// Vertical distance of first set of engines from the aircraft center of gravity (
						if ((i == 7) && (j == 1)) {
							deltaZEng1 = Double.parseDouble(value);
							System.out.println("Vertical distance of first set of engines from the aircraft center of gravity (can be< or > 0)  = " + deltaZEng1);

						}	      

						//-------------------------------------------------------------------------------------------
						//  Vertical distance of second set of engines from the aircraft center of gravity 
						if ((i == 8) && (j == 1)) {
							deltaZEng2 = Double.parseDouble(value);
							System.out.println("Vertical distance of second set of engines from the aircraft center of gravity (can be < or >0)  = " + deltaZEng2);

						}

						//-------------------------------------------------------------------------------------------
						// Engines diameter
						if ((i == 9) && (j == 1)) {
							dEng = Double.parseDouble(value);
							System.out.println("Engines diameter  = " + dEng);

						}

						//-------------------------------------------------------------------------------------------
						// Upwash gradient at engines' inlet
						if ((i == 10) && (j == 1)) {
							downwashEngine = Double.parseDouble(value);
							System.out.println("Upwash gradient at engines' inlet = " + downwashEngine);

						}

						//-------------------------------------------------------------------------------------------
						// Engines normal-to-disk force coefficient derivative. 
						if ((i == 11) && (j == 1)) {
							cNAlfaEngine = Double.parseDouble(value);
							System.out.println("Engines normal-to-disk force coefficient derivative  = " + cNAlfaEngine);

						}

						break;



					}
				}	
			}
		} 
		catch(Exception ioe) {
			ioe.printStackTrace();
		}

	}

	public static String cellToString(Cell cell) {  
		int type;
		Object result = null;
		type = cell.getCellType();

		switch (type) {

		case Cell.CELL_TYPE_NUMERIC: // numeric value in Excel
		case Cell.CELL_TYPE_FORMULA: // precomputed value based on formula
			result = cell.getNumericCellValue();
			break;
		case Cell.CELL_TYPE_STRING: // String Value in Excel 
			result = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BLANK:
			result = "";
		case Cell.CELL_TYPE_BOOLEAN: //boolean value 
			result = cell.getBooleanCellValue();
			break;
		case Cell.CELL_TYPE_ERROR:
		default:  
			throw new RuntimeException("There is no support for this type of cell");                        
		}

		if (result == null)
			return "";
		else
			return result.toString();
	}

	public static void main(String[] args) throws IOException {
		System.out.println("MyTest_ADM_06_AerodynamicDatabase_Interpolations :: main ");
		/*
		 * test Giuseppe Torre's functions 
		 */
		try {
			System.out.println("Try MyTest_ADM_06 ...");
			MyTest_ADM_06_AerodynamicDatabase_Interpolations myTest_ADM_06 = new MyTest_ADM_06_AerodynamicDatabase_Interpolations(); 
		}
		catch (Exception exc) {
			System.out.println(exc);
			System.out.println("Test ADM 06 went wrong!");
		}
		
		System.out.println("... end of Aerodynamic Database interpolation test");
		System.out.println("---------------------------");
		
	}
	
}
