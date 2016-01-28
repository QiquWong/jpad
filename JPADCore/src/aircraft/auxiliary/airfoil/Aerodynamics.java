package aircraft.auxiliary.airfoil;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;

import aircraft.auxiliary.AuxiliaryComponentCalculator;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLMaxClean;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcLiftDistribution;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilStationEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class Aerodynamics extends AuxiliaryComponentCalculator{

	private String _id = ""; 
	public static int idCounter = 0;
	public static int nAero = 0;
	
	private CalculateClAlpha calculateClAlpha;
	private CalculateMachCr calculateMachCr;
	private CalculateCdWaveDrag calculateCdWaveDrag;
	private double _mach;
	private double _clCurrent, _clCurrentViscid;

	/** The angle of attack of lifting surface chord root */
	private Amount<Angle> _alphaRoot;

	/** Zero lift angle of attack */
	private Amount<Angle> _alphaZeroLift = Amount.valueOf(0,SI.RADIAN); 

	/** End of linear region of lift vs alpha curve */
	private Amount<Angle> _alphaStar = Amount.valueOf(0,SI.RADIAN); // end-of-linearity 

	/** Stall angle */
	private Amount<Angle> _alphaStall = Amount.valueOf(0,SI.RADIAN); 

	/** Current angle of attack */
	private Amount<Angle> _alphaCurrent;

	/** The actual angle of attack of the airfoil */
	private Amount<Angle> _alphaEffective = Amount.valueOf(0,SI.RADIAN);

	private Double _clAtAlpha0;
	private Double _clAlpha; 
	private Double _clStar; 
	private Double _clMax;

	private Double _cdMin;
	private Double _clAtCdMin;
	private Double _kFactorDragPolar;
	private Double _mExponentDragPolar;

	private Double _machCr0;
	private static Map<AirfoilFamilyEnum, Double> _kWaveDragMap = new HashMap<AirfoilFamilyEnum, Double> ();

	private Double _aerodynamicCenterX;
	private Double _cmAC;
	private Double _cmACStall;
	private Double _cmAlphaAC, _cmAlphaLE;

	private Double _reynoldsCruise;
	private Double _reynoldsNumberStall;

	private double _machCr, _cdw = 0., _machCurrent;
	private Geometry geometry;
	private MyAirfoil _theAirfoil;

	public Aerodynamics(MyAirfoil airf, String aircraftName, AirfoilStationEnum station) {
		switch (aircraftName) {
		case "ATR-72":
			switch (station) {
			case ROOT:
				Aerodynamics(airf, "23-018");
				break;
			case KINK:
				Aerodynamics(airf, "23-015") ;
				break;
			case TIP:
				Aerodynamics(airf, "23-012");
				break;	
			}
			break;
		case "B747-100B":
			//TODO implement this 
			switch (station) {
			case ROOT:
				new Aerodynamics(airf, aircraftName, AirfoilStationEnum.ROOT);
				break;
			case KINK:
				new Aerodynamics(airf, aircraftName, AirfoilStationEnum.KINK);
				break;
			case TIP:
				new Aerodynamics(airf, aircraftName, AirfoilStationEnum.TIP);
				break;	
			}
			break;
		}
		
	}

	private void Aerodynamics(MyAirfoil airf, String string) {
		 switch (string) {
		 
		 case "23-018":
			 _id = airf.getId() + "1" + idCounter + "99";
				idCounter++;
			 airf.set_family(AirfoilFamilyEnum.NACA23_018);	
			 _theAirfoil = airf;
			 geometry = airf.getGeometry();

			 _alphaZeroLift = Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN); 
			 _clAlpha = 7.22; 
			 _alphaStar = Amount.valueOf(Math.toRadians(9.5),SI.RADIAN); // end-of-linearity 
			 _clStar = 1.3 ; 
			 _alphaStall = Amount.valueOf(Math.toRadians(16.0),SI.RADIAN); 
			 _clMax = 1.6; //1.8;

			 _cdMin = 0.00675;
			 _clAtCdMin = 0.3;
			 _kFactorDragPolar = 0.075;

			 _aerodynamicCenterX = 0.243;
			 _cmAC = -0.083;
			 _cmACStall = -0.09;
			 _cmAlphaAC = 0. ;
			 _cmAlphaLE = -0.1; //TODO, change this value
			 //_cmAtClMax = 

			 _reynoldsCruise = 8.9e+6;
			 _reynoldsNumberStall = 1e+6;

			 calculateClAlpha = new CalculateClAlpha();
			 calculateMachCr = new CalculateMachCr();
			 calculateCdWaveDrag = new CalculateCdWaveDrag();
			 break;

		 case "23-015":
			 airf.set_family(AirfoilFamilyEnum.NACA23_015);	
			 _id = airf.getId() + "1" + idCounter + "99";
				idCounter++;
				
			 _theAirfoil = airf;
			 geometry = airf.getGeometry();

			 _alphaZeroLift = Amount.valueOf(Math.toRadians(-1.1), SI.RADIAN); 
			 _clAlpha = 7.05; 
			 _alphaStar = Amount.valueOf(Math.toRadians(10),SI.RADIAN); // end-of-linearity 
			 _clStar = 1.2 ; 
			 _alphaStall = Amount.valueOf(Math.toRadians(18.0),SI.RADIAN); 
			 _clMax = 1.72; //1.6;

			 _cdMin = 0.00625;
			 _clAtCdMin = 0.1;
			 _kFactorDragPolar = 0.075;

			 _aerodynamicCenterX = 0.243;
			 _cmAC = -0.0833;
			 _cmACStall = -0.07;
			 _cmAlphaAC = 0. ;
			 _cmAlphaLE = -0.1; //TODO, change this value
			 //_cmAtClMax = 

			 _reynoldsCruise = 8.9e+6;
			 _reynoldsNumberStall = 1e+6;

			 calculateClAlpha = new CalculateClAlpha();
			 calculateMachCr = new CalculateMachCr();
			 calculateCdWaveDrag = new CalculateCdWaveDrag();
			 break;
			 
		 case "23-012":
			 airf.set_family(AirfoilFamilyEnum.NACA23_012);	
			 _id = airf.getId() + "1" + idCounter + "99";
				idCounter++;
				
			 _theAirfoil = airf;
			 geometry = airf.getGeometry();

			 _alphaZeroLift = Amount.valueOf(Math.toRadians(-1.32), SI.RADIAN); 
			 _clAlpha = 6.88; 
			 _alphaStar = Amount.valueOf(Math.toRadians(14),SI.RADIAN); // end-of-linearity 
			 _clStar = 1.6 ; 
			 _alphaStall = Amount.valueOf(Math.toRadians(18),SI.RADIAN); 
			 _clMax = 1.8 ; //1.5;

			 _cdMin = 0.00575;
			 _clAtCdMin = 0.23;
			 _kFactorDragPolar = 0.075;

			 _aerodynamicCenterX = 0.247;
			 _cmAC = -0.083;
			 _cmACStall = -0.09;
			 _cmAlphaAC = 0. ;
			 _cmAlphaLE = -0.1; //TODO, change this value
			 //_cmAtClMax = 

			 _reynoldsCruise = 8.8e+6;
			 _reynoldsNumberStall = 1e+6;

			 calculateClAlpha = new CalculateClAlpha();
			 calculateMachCr = new CalculateMachCr();
			 calculateCdWaveDrag = new CalculateCdWaveDrag();
			 break;	
			 
			 
		 case "65-209": 
			_id = airf.getId() + "1" + idCounter + "99";
			idCounter++;
			
			_theAirfoil = airf;
			geometry = airf.getGeometry();
			
			_alphaZeroLift = Amount.valueOf(Math.toRadians(-1.3), SI.RADIAN); 
			_clAlpha = 5.96; 
			_alphaStar = Amount.valueOf(Math.toRadians(11),SI.RADIAN); // end-of-linearity 
			_clStar = 1.1 ; 
			_alphaStall = Amount.valueOf(Math.toRadians(17.0),SI.RADIAN); 
			_clMax = 1.6;

			_cdMin = 0.025;
			_clAtCdMin = 0.2;
			_kFactorDragPolar = 0.075;

			_aerodynamicCenterX = 0.25;
			_cmAC = -0.07;
			_cmACStall = -0.09;
			_cmAlphaAC = 0. ;
			_cmAlphaLE = -0.1; //TODO, change this value
			//_cmAtClMax = 

			_reynoldsCruise = 1e+7;
			_reynoldsNumberStall = 1e+6;

			calculateClAlpha = new CalculateClAlpha();
			calculateMachCr = new CalculateMachCr();
			calculateCdWaveDrag = new CalculateCdWaveDrag();	
			break;
			
		 case "65-206" :
			 _id = airf.getId() + "1" + idCounter + "99";
			idCounter++;
			
			_theAirfoil = airf;
			geometry = airf.getGeometry();
			
			_alphaZeroLift = Amount.valueOf(Math.toRadians(-1.4), SI.RADIAN); 
			_clAlpha = 6.13; 
			_alphaStar = Amount.valueOf(Math.toRadians(10.0),SI.RADIAN); // end-of-linearity 
			_clStar = _clAlpha * _alphaStar.getEstimatedValue() ; 
			_alphaStall = Amount.valueOf(Math.toRadians(15.0),SI.RADIAN); 
			_clMax = 1.3;

			_cdMin = 0.025;
			_clAtCdMin = 0.2;
			_kFactorDragPolar = 0.075;

			_aerodynamicCenterX = 0.25;
			_cmAC = -0.07;
			_cmACStall = -0.09;
			_cmAlphaAC = 0. ;
			_cmAlphaLE = -0.1; //TODO, change this value
			//_cmAtClMax = 

			_reynoldsCruise = 1e+7;
			_reynoldsNumberStall = 1e+6;

			calculateClAlpha = new CalculateClAlpha();
			calculateMachCr = new CalculateMachCr();
			calculateCdWaveDrag = new CalculateCdWaveDrag();	
			break;
		 }
		 
			 
		}
		
	

	public Aerodynamics(MyAirfoil airf) {
		
		_id = airf.getId() + "1" + idCounter + "99";
		idCounter++;
		
		_theAirfoil = airf;
		geometry = airf.getGeometry();
		
		_alphaZeroLift = Amount.valueOf(Math.toRadians(-2.5), SI.RADIAN); 
		_clAlpha = 6.1; 
		_alphaStar = Amount.valueOf(Math.toRadians(10.0),SI.RADIAN); // end-of-linearity 
		_clStar = 0.8; 
		_alphaStall = Amount.valueOf(Math.toRadians(13.0),SI.RADIAN); 
		_clMax = 1.03;

		_cdMin = 0.025;
		_clAtCdMin = 0.2;
		_kFactorDragPolar = 0.075;

		_aerodynamicCenterX = 0.25;
		_cmAC = -0.07;
		_cmACStall = -0.09;
		_cmAlphaAC = 0. ;
		_cmAlphaLE = -0.1; //TODO, change this value
		//_cmAtClMax = 

		_reynoldsCruise = 1e+7;
		_reynoldsNumberStall = 1e+6;

		calculateClAlpha = new CalculateClAlpha();
		calculateMachCr = new CalculateMachCr();
		calculateCdWaveDrag = new CalculateCdWaveDrag();	
	}
	
public Aerodynamics(MyAirfoil airf, String name) {
	
	 switch (name) {
	 
	 case "23-018":
		 _id = airf.getId() + "1" + idCounter + "99";
			idCounter++;
			
		 _theAirfoil = airf;
		 geometry = airf.getGeometry();

		 _alphaZeroLift = Amount.valueOf(Math.toRadians(-1.2), SI.RADIAN); 
		 _clAlpha = 7.22; 
		 _alphaStar = Amount.valueOf(Math.toRadians(9.5),SI.RADIAN); // end-of-linearity 
		 _clStar = 1.3 ; 
		 _alphaStall = Amount.valueOf(Math.toRadians(16.0),SI.RADIAN); 
		 _clMax = 1.6; //1.8;

		 _cdMin = 0.00675;
		 _clAtCdMin = 0.3;
		 _kFactorDragPolar = 0.075;

		 _aerodynamicCenterX = 0.243;
		 _cmAC = -0.083;
		 _cmACStall = -0.09;
		 _cmAlphaAC = 0. ;
		 _cmAlphaLE = -0.1; //TODO, change this value
		 //_cmAtClMax = 

		 _reynoldsCruise = 8.9e+6;
		 _reynoldsNumberStall = 1e+6;

		 calculateClAlpha = new CalculateClAlpha();
		 calculateMachCr = new CalculateMachCr();
		 calculateCdWaveDrag = new CalculateCdWaveDrag();
		 break;

	 case "23-015":
		 _id = airf.getId() + "1" + idCounter + "99";
			idCounter++;
			
		 _theAirfoil = airf;
		 geometry = airf.getGeometry();

		 _alphaZeroLift = Amount.valueOf(Math.toRadians(-1.1), SI.RADIAN); 
		 _clAlpha = 7.05; 
		 _alphaStar = Amount.valueOf(Math.toRadians(10),SI.RADIAN); // end-of-linearity 
		 _clStar = 1.2 ; 
		 _alphaStall = Amount.valueOf(Math.toRadians(18.0),SI.RADIAN); 
		 _clMax = 1.72; //1.6;

		 _cdMin = 0.00625;
		 _clAtCdMin = 0.1;
		 _kFactorDragPolar = 0.075;

		 _aerodynamicCenterX = 0.243;
		 _cmAC = -0.0833;
		 _cmACStall = -0.07;
		 _cmAlphaAC = 0. ;
		 _cmAlphaLE = -0.1; //TODO, change this value
		 //_cmAtClMax = 

		 _reynoldsCruise = 8.9e+6;
		 _reynoldsNumberStall = 1e+6;

		 calculateClAlpha = new CalculateClAlpha();
		 calculateMachCr = new CalculateMachCr();
		 calculateCdWaveDrag = new CalculateCdWaveDrag();
		 break;
		 
	 case "23-012":
		 _id = airf.getId() + "1" + idCounter + "99";
			idCounter++;
			
		 _theAirfoil = airf;
		 geometry = airf.getGeometry();

		 _alphaZeroLift = Amount.valueOf(Math.toRadians(-1.32), SI.RADIAN); 
		 _clAlpha = 6.88; 
		 _alphaStar = Amount.valueOf(Math.toRadians(14),SI.RADIAN); // end-of-linearity 
		 _clStar = 1.6 ; 
		 _alphaStall = Amount.valueOf(Math.toRadians(18),SI.RADIAN); 
		 _clMax = 1.8 ; //1.5;

		 _cdMin = 0.00575;
		 _clAtCdMin = 0.23;
		 _kFactorDragPolar = 0.075;

		 _aerodynamicCenterX = 0.247;
		 _cmAC = -0.083;
		 _cmACStall = -0.09;
		 _cmAlphaAC = 0. ;
		 _cmAlphaLE = -0.1; //TODO, change this value
		 //_cmAtClMax = 

		 _reynoldsCruise = 8.8e+6;
		 _reynoldsNumberStall = 1e+6;

		 calculateClAlpha = new CalculateClAlpha();
		 calculateMachCr = new CalculateMachCr();
		 calculateCdWaveDrag = new CalculateCdWaveDrag();
		 break;	
		 
		 
	 case "65-209": 
		_id = airf.getId() + "1" + idCounter + "99";
		idCounter++;
		
		_theAirfoil = airf;
		geometry = airf.getGeometry();
		
		_alphaZeroLift = Amount.valueOf(Math.toRadians(-1.3), SI.RADIAN); 
		_clAlpha = 5.96; 
		_alphaStar = Amount.valueOf(Math.toRadians(11),SI.RADIAN); // end-of-linearity 
		_clStar = 1.1 ; 
		_alphaStall = Amount.valueOf(Math.toRadians(17.0),SI.RADIAN); 
		_clMax = 1.6;

		_cdMin = 0.025;
		_clAtCdMin = 0.2;
		_kFactorDragPolar = 0.075;

		_aerodynamicCenterX = 0.25;
		_cmAC = -0.07;
		_cmACStall = -0.09;
		_cmAlphaAC = 0. ;
		_cmAlphaLE = -0.1; //TODO, change this value
		//_cmAtClMax = 

		_reynoldsCruise = 1e+7;
		_reynoldsNumberStall = 1e+6;

		calculateClAlpha = new CalculateClAlpha();
		calculateMachCr = new CalculateMachCr();
		calculateCdWaveDrag = new CalculateCdWaveDrag();	
		break;
		
	 case "65-206" :
		 _id = airf.getId() + "1" + idCounter + "99";
		idCounter++;
		
		_theAirfoil = airf;
		geometry = airf.getGeometry();
		
		_alphaZeroLift = Amount.valueOf(Math.toRadians(-1.4), SI.RADIAN); 
		_clAlpha = 6.13; 
		_alphaStar = Amount.valueOf(Math.toRadians(10.0),SI.RADIAN); // end-of-linearity 
		_clStar = _clAlpha * _alphaStar.getEstimatedValue() ; 
		_alphaStall = Amount.valueOf(Math.toRadians(15.0),SI.RADIAN); 
		_clMax = 1.3;

		_cdMin = 0.025;
		_clAtCdMin = 0.2;
		_kFactorDragPolar = 0.075;

		_aerodynamicCenterX = 0.25;
		_cmAC = -0.07;
		_cmACStall = -0.09;
		_cmAlphaAC = 0. ;
		_cmAlphaLE = -0.1; //TODO, change this value
		//_cmAtClMax = 

		_reynoldsCruise = 1e+7;
		_reynoldsNumberStall = 1e+6;

		calculateClAlpha = new CalculateClAlpha();
		calculateMachCr = new CalculateMachCr();
		calculateCdWaveDrag = new CalculateCdWaveDrag();	
		break;
	 }
	 
		 
	}

	public void calculateAll(){

	}

	public double calculateClAtAlphaLinear(Double alpha) {
		_clCurrent = alpha/_clAlpha;
		return _clCurrent;
	}
	
	/**
	 * This function calculates Cl at alpha of an airfoil. It calculates both in the linear trait and non linear using a cubic 
	 * interpolation
	 * 
	 * @author Manuela Ruocco
	 * @param double alpha: angle of attack in radians
	 */  


	public  double calculateClAtAlpha (double alpha){
		double q = _clStar - _clAlpha * _alphaStar.getEstimatedValue();
		if ( alpha < _alphaStar.getEstimatedValue() ) {
			_clCurrentViscid = _clAlpha* alpha + q ;
		}
		else {
			double[][] matrixData = { {Math.pow(_alphaStall.getEstimatedValue(), 3),
				Math.pow(_alphaStall.getEstimatedValue(), 2), _alphaStall.getEstimatedValue(),1.0},
					{3* Math.pow(_alphaStall.getEstimatedValue(), 2), 2*_alphaStall.getEstimatedValue(), 1.0, 0.0},
					{3* Math.pow(_alphaStar.getEstimatedValue(), 2), 2*_alphaStar.getEstimatedValue(), 1.0, 0.0},
					{Math.pow(_alphaStar.getEstimatedValue(), 3), Math.pow(_alphaStar.getEstimatedValue(), 2),
						_alphaStar.getEstimatedValue(),1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
			double [] vector = {_clMax, 0,_clAlpha, _clStar};
			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);
			double a = solSystem[0];
			double b = solSystem[1];
			double c = solSystem[2];
			double d = solSystem[3];

			_clCurrentViscid = a * Math.pow(alpha,3) + b * Math.pow(alpha, 2) + c * alpha +d;  		
		}
		return _clCurrentViscid;
	}

	public void plotClvsAlpha(){

		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("STARTING PLOT AIRFOIL DRAG POLAR");
		System.out.println("-----------------------------------------------------");

		MyArray alphaArray = new MyArray();
		int _numberOfAlpha = 40;
		double [] clArray = new double [_numberOfAlpha];
		double [] alphaArrayDeg = new double [_numberOfAlpha];

		Amount<Angle> alphaActualAmount;

		Amount<Angle> alphaStart = Amount.valueOf(toRadians(-6.), SI.RADIAN);
		Amount<Angle> alphaEnd = Amount.valueOf(
				toRadians(get_alphaStall()
						.to(NonSI.DEGREE_ANGLE)
						.getEstimatedValue()+2), SI.RADIAN);

		alphaArray.setDouble(MyArrayUtils.linspace(
				alphaStart.getEstimatedValue(), 
				alphaEnd.getEstimatedValue(), 
				_numberOfAlpha));

		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Cl vs Alpha Airfoil" + File.separator);

		for (int i=0 ; i<_numberOfAlpha ; i++){
			alphaActualAmount = Amount.valueOf( alphaArray.get(i), SI.RADIAN); 
			clArray[i] = calculateClAtAlpha(alphaActualAmount.getEstimatedValue());
			alphaArrayDeg [i] = alphaActualAmount.to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		}

		MyChartToFileUtils.plotNoLegend
		(alphaArrayDeg , clArray,null , null  ,
				null ,null , "alpha", "CL", "deg" , "", subfolderPath, "CLalphaAirfoil");


		System.out.println("-----------------------------------------------------");	
		System.out.println("\n DONE");
		System.out.println("-----------------------------------------------------");
	}


	/**
	 * Evaluate Cl at effective AoA (i.e., taking into account
	 * airfoil twist) 
	 */
	public void calculateClAtAlphaEffective() {
		calculateClAtAlphaLinear(calculateAlphaEffective());
	}

	
	/**
	 * Evaluate effective AoA (i.e., taking into account
	 * airfoil twist) 
	 */
	public double calculateAlphaEffective() {
		_alphaEffective = Amount.valueOf(
				geometry.get_twist().getEstimatedValue() + _alphaRoot.getEstimatedValue()
				, SI.RADIAN);

		return _alphaEffective.getEstimatedValue();
	}

	public class CalculateClAlpha {
		
		
		

	}

	/**
	 * Evaluate Cd using a parabolic polar curve
	 * 
	 * @author Manuela Ruocco
	 */
	
	public double calculateCdAtAlpha(Amount<Angle> alpha) {
		double cdAirfoil;
		double clAirfoil = calculateClAtAlpha(alpha.getEstimatedValue());
		cdAirfoil = get_cdMin() + Math.pow(( clAirfoil - get_clAtCdMin()), 2)*get_kFactorDragPolar();
		return cdAirfoil;

	}
	
	/**
	 * Evaluate Cd using a parabolic polar curve
	 * 
	 * @author Manuela Ruocco
	 */
	
	public static double calculateCd(double clAirfoil, Double cdMin, Double  clAtCdMin, double kFactorDragPolar){
		double cdAirfoil;
		cdAirfoil = cdMin +Math.pow(( clAirfoil - clAtCdMin), 2)*kFactorDragPolar;
		return cdAirfoil;
	}

	
	/**
	 * This function plots the airfoil drag polar using a parabolic approssimation. 
	 * 
	 * @author Manuela Ruocco
	 * 
	 */  
	public void plotPolar(){
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("STARTING PLOT AIRFOIL DRAG POLAR");
		System.out.println("-----------------------------------------------------");
		
		MyArray alphaArray = new MyArray();
		int _numberOfAlpha = 40;
		double [] cdArrayPolar = new double [_numberOfAlpha];
		double [] clArrayPolar = new double [_numberOfAlpha];
		Amount<Angle> alphaActualAmount;

		Amount<Angle> alphaStart = Amount.valueOf(toRadians(-6.), SI.RADIAN);
		Amount<Angle> alphaEnd = Amount.valueOf(toRadians(12.), SI.RADIAN);

		alphaArray.setDouble(MyArrayUtils.linspace(
				alphaStart.getEstimatedValue(), 
				alphaEnd.getEstimatedValue(), 
				_numberOfAlpha));

		String folderPathPolar = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		String subfolderPathPolar = JPADStaticWriteUtils.createNewFolder(folderPathPolar + "Polar_Airfoil" + File.separator);
		
		for (int i=0 ; i<_numberOfAlpha ; i++){
			alphaActualAmount = Amount.valueOf( alphaArray.get(i), SI.RADIAN); 
			clArrayPolar[i] = calculateClAtAlpha(alphaActualAmount.getEstimatedValue());
			cdArrayPolar[i] = calculateCdAtAlpha(alphaActualAmount);
		
		}
		
		MyChartToFileUtils.plotNoLegend
		( cdArrayPolar,clArrayPolar ,null ,null ,
				null , null, "Cd", "Cl", "" , "", subfolderPathPolar,
				"Polar_Airfoil ");
		
		System.out.println("\n \n-----------------------------------------------------");
		System.out.println("DONE");
		System.out.println("-----------------------------------------------------");
	}


	public class CalculateMachCr {

		private double mCr0PerkinsAndHage() {
			// Page 407 Sforza
			_machCr0 = 0.89 - 1.3*(geometry.get_maximumThicknessOverChord());
			return _machCr0; 
		}

		private double mCr0Shevell() {
			// Page 409 Sforza
			_machCr0 = 0.9 - geometry.get_maximumThicknessOverChord();
			return _machCr0; 
		}

		public double perkinsAndHage() {
			if (_kWaveDragMap.containsKey(_theAirfoil.get_family())) {
				_machCr = mCr0PerkinsAndHage() - _kWaveDragMap.get(_theAirfoil.get_family())*_clCurrent;
			} else _machCr = 0.;
			return _machCr;
		}

		public double shevell() {
			_machCr = mCr0Shevell() - (0.17 + 0.016)*_clCurrent;
			return _machCr;
		}

		public double korn() {
			double k;
			if (_theAirfoil.get_type().equals(AirfoilTypeEnum.CONVENTIONAL)) k = 0.87;
			else k = 0.95; 

			_machCr = (k - 0.108) - geometry.get_maximumThicknessOverChord() - 0.1*_clCurrent;
			return _machCr;
		}

		public void allMethods() {
			perkinsAndHage();
			shevell();
			korn();
		}
	}


	public class CalculateCdWaveDrag {

		public double perkinsAndHage() {

			// Difference between current mach number and critical mach number 
			double diff = _machCurrent - calculateMachCr.perkinsAndHage();

			if (diff > 0)
				_cdw = 9.5*Math.pow((diff), 2.8) + 0.00193;
			return _cdw;
		}

		/** Page 410 Sforza */
		public double lockShevell() {

			double diff = _machCurrent - calculateMachCr.shevell();

			if (diff > 0)
				_cdw = 20*Math.pow((diff),4);
			return _cdw;
		}

		public double lockKorn() {

			double diff = _machCurrent - calculateMachCr.korn();

			if (diff > 0)
				_cdw = 20*Math.pow((diff),4);
			return _cdw;
		}

		public void allMethods() {
			perkinsAndHage();
			lockShevell();
			lockKorn();
		}
	}


	public CalculateClAlpha getCalculateClAlpha() {
		return calculateClAlpha;
	}


	public CalculateMachCr getCalculateMachCr() {
		return calculateMachCr;
	}


	public CalculateCdWaveDrag getCalculateCdWaveDrag() {
		return calculateCdWaveDrag;
	}

	public double get_mach() {
		return _mach;
	}

	public void set_mach(double _mach) {
		this._mach = _mach;
	}

	public double get_clCurrent() {
		return _clCurrent;
	}

	public void set_clCurrent(double _clCurrent) {
		this._clCurrent = _clCurrent;
	}

	public Amount<Angle> get_alphaRoot() {
		return _alphaRoot;
	}

	public void set_alphaRoot(Amount<Angle> _alphaRoot) {
		this._alphaRoot = _alphaRoot;
	}

	public Amount<Angle> get_alphaZeroLift() {
		return _alphaZeroLift;
	}

	public void set_alphaZeroLift(Amount<Angle> _alphaZeroLift) {
		this._alphaZeroLift = _alphaZeroLift;
	}

	public Amount<Angle> get_alphaStar() {
		return _alphaStar;
	}

	public void set_alphaStar(Amount<Angle> _alphaStar) {
		this._alphaStar = _alphaStar;
	}

	public Amount<Angle> get_alphaStall() {
		return _alphaStall;
	}

	public void set_alphaStall(Amount<Angle> _alphaStall) {
		this._alphaStall = _alphaStall;
	}

	public Amount<Angle> get_alphaCurrent() {
		return _alphaCurrent;
	}

	public void set_alphaCurrent(Amount<Angle> _alphaCurrent) {
		this._alphaCurrent = _alphaCurrent;
	}

	public Amount<Angle> get_alphaEffective() {
		return _alphaEffective;
	}

	public void set_alphaEffective(Amount<Angle> _alphaEffective) {
		this._alphaEffective = _alphaEffective;
	}

	public Double get_clAtAlpha0() {
		return _clAtAlpha0;
	}

	public void set_clAtAlpha0(Double _clAtAlpha0) {
		this._clAtAlpha0 = _clAtAlpha0;
	}

	public Double get_clAlpha() {
		return _clAlpha;
	}

	public void set_clAlpha(Double _clAlpha) {
		this._clAlpha = _clAlpha;
	}

	public Double get_clStar() {
		return _clStar;
	}

	public void set_clStar(Double _clStar) {
		this._clStar = _clStar;
	}

	public Double get_clMax() {
		return _clMax;
	}

	public void set_clMax(Double _clMax) {
		this._clMax = _clMax;
	}

	public Double get_cdMin() {
		return _cdMin;
	}

	public void set_cdMin(Double _cdMin) {
		this._cdMin = _cdMin;
	}

	public Double get_clAtCdMin() {
		return _clAtCdMin;
	}

	public void set_clAtCdMin(Double _clAtCdMin) {
		this._clAtCdMin = _clAtCdMin;
	}

	public Double get_kFactorDragPolar() {
		return _kFactorDragPolar;
	}

	public void set_kFactorDragPolar(Double _kFactorDragPolar) {
		this._kFactorDragPolar = _kFactorDragPolar;
	}

	public Double get_mExponentDragPolar() {
		return _mExponentDragPolar;
	}

	public void set_mExponentDragPolar(Double _mExponentDragPolar) {
		this._mExponentDragPolar = _mExponentDragPolar;
	}

	public Double get_machCr0() {
		return _machCr0;
	}

	public void set_machCr0(Double _machCr0) {
		this._machCr0 = _machCr0;
	}

	public Double get_aerodynamicCenterX() {
		return _aerodynamicCenterX;
	}

	public void set_aerodynamicCenterX(Double _aerodynamicCenterX) {
		this._aerodynamicCenterX = _aerodynamicCenterX;
	}

	public Double get_cmAC() {
		return _cmAC;
	}

	public void set_cmAC(Double _cmAC) {
		this._cmAC = _cmAC;
	}

	public Double get_cmACStall() {
		return _cmACStall;
	}

	public void set_cmACStall(Double _cmACStall) {
		this._cmACStall = _cmACStall;
	}

	public Double get_cmAlphaAC() {
		return _cmAlphaAC;
	}

	public void set_cmAlphaAC(Double _cmAlphaAC) {
		this._cmAlphaAC = _cmAlphaAC;
	}

	public Double get_cmAlphaLE() {
		return _cmAlphaLE;
	}

	public void set_cmAlphaLE(Double _cmAlphaLE) {
		this._cmAlphaLE = _cmAlphaLE;
	}

	public Double get_reynoldsCruise() {
		return _reynoldsCruise;
	}

	public void set_reynoldsCruise(Double _reynoldsCruise) {
		this._reynoldsCruise = _reynoldsCruise;
	}

	public Double get_reynoldsNumberStall() {
		return _reynoldsNumberStall;
	}

	public void set_reynoldsNumberStall(Double _reynoldsNumberStall) {
		this._reynoldsNumberStall = _reynoldsNumberStall;
	}

	public double get_machCr() {
		return _machCr;
	}

	public void set_machCr(double _machCr) {
		this._machCr = _machCr;
	}

	public double get_cdw() {
		return _cdw;
	}

	public void set_cdw(double _cdw) {
		this._cdw = _cdw;
	}

	public double get_machCurrent() {
		return _machCurrent;
	}

	public void set_machCurrent(double _machCurrent) {
		this._machCurrent = _machCurrent;
	}

	public static Map<AirfoilFamilyEnum, Double> get_kWaveDragMap() {
		return _kWaveDragMap;
	}

	@Override
	public String getId() {
		return _id;
	}
	
	public String getIdNew() {
		String id = _theAirfoil.getId() + "aero" + nAero;
		nAero++;
		return id;
	}
	
}
