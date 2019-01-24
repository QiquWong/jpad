package it.unina.daf.jpadcadsandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcadsandbox.Test26mds.AeroComponents;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.DataWriter;
import it.unina.daf.jpadcadsandbox.utils.GeometricData;
import it.unina.daf.jpadcadsandbox.utils.GeometricData.GeometricDataBuilder;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions.OperatingConditionsBuilder;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters.SimulationParametersBuilder;
import standaloneutils.aerotools.aero.StdAtmos1976;
import standaloneutils.atmosphere.AtmosphereCalc;

public class CreateCasesGB {

	public static final String workingFolderPath = "C:\\Users\\giord\\Desktop\\STAR_WORKING_FOLDER"; //"   E:\\CASI\\W_C\\STAR_WORKING_FOLDER"
	public static final String jpadCADFolder = "D:\\JPAD\\jpad\\JPADCADSandbox";
	public static final String starTempFolder = "C:\\Users\\giord\\Desktop\\STAR_TEMP_FOLDER";
	public static final String macroPath = " D:\\eclipse\\STAR_MACRO\\src\\macro";
	public static final String macroName = "Create_Sim.java"; // Test_MultipleExecutes_GB
	//public static final String macroName2 = "PostProcess_Sim.java";
	public static final String starExePath = "C:\\Program Files\\CD-adapco\\12.04.011-R8\\STAR-CCM+12.04.011-R8\\star\\bin\\starccm+.exe";
	public static final String starOptions = "-cpubind -power -podkey 2jHU+QkwqexqrAOdVZ6ZzQ -licpath 1999@flex.cd-adapco.com -np 8 -rsh ssh";

	public static void main(String[] args) throws IOException {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		Aircraft aircraft = AircraftUtils.importAircraft(args);	

		ConcurrentHashMap<AeroComponents, List<Object>> aeroMap = new ConcurrentHashMap<AeroComponents, List<Object>>();
		aeroMap.put(AeroComponents.FUSELAGE, new ArrayList<Object>());
		aeroMap.put(AeroComponents.WING, new ArrayList<Object>());
		aeroMap.put(AeroComponents.CANARD, new ArrayList<Object>());
		aeroMap.put(AeroComponents.HORIZONTAL, new ArrayList<Object>());
		aeroMap.put(AeroComponents.VERTICAL, new ArrayList<Object>());

		for(Iterator<AeroComponents> comp = aeroMap.keySet().iterator(); comp.hasNext(); ) {

			AeroComponents component = comp.next();
			switch(component) {

			case FUSELAGE:
				if(!(aircraft.getFuselage() == null))
					aeroMap.get(AeroComponents.FUSELAGE).add(aircraft.getFuselage());
				else {
					System.out.println("There's no FUSELAGE component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;

			case WING:
				if(!(aircraft.getWing() == null))
					aeroMap.get(AeroComponents.WING).add(aircraft.getWing());
				else {
					System.out.println("There's no WING component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;

			case HORIZONTAL:
				if(!(aircraft.getHTail() == null))
					aeroMap.get(AeroComponents.HORIZONTAL).add(aircraft.getHTail());
				else {
					System.out.println("There's no HORIZONTAL component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;

			case VERTICAL:
				if(!(aircraft.getVTail() == null))
					aeroMap.get(AeroComponents.VERTICAL).add(aircraft.getVTail());
				else {
					System.out.println("There's no VERTICAL component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;	

			case CANARD:
				if(!(aircraft.getCanard() == null))
					aeroMap.get(AeroComponents.CANARD).add(aircraft.getCanard());
				else {
					System.out.println("There's no CANARD component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;

			default:

				break;
			}
		}

		//TODO// Remove unnecessary components
		aeroMap.remove(AeroComponents.FUSELAGE);
		aeroMap.remove(AeroComponents.HORIZONTAL);
		aeroMap.remove(AeroComponents.VERTICAL);
		//aeroMap.remove(AeroComponents.CANARD);

		// Acquiring original Wing data


		Amount<Length> xApexWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getXApexConstructionAxes().to(SI.METER);

		Amount<Length> zApexWing =  ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getZApexConstructionAxes().to(SI.METER);

		Amount<Length> spanWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getEquivalentWing().getPanels().get(0).getSpan().times(2).to(SI.METER);

		Amount<Angle> sweepLEWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE);

		Amount<Angle> dihedralAngleWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE);

		Amount<Angle> riggingAngleWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getRiggingAngle().to(NonSI.DEGREE_ANGLE);



		// Acquiring original Canard Data


		Amount<Length> xApexCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
				.getXApexConstructionAxes();

		Amount<Length> zApexCanard =  ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
				.getZApexConstructionAxes();

		Amount<Length> spanCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
				.getEquivalentWing().getPanels().get(0).getSpan().times(2);

		Amount<Angle> sweepLECanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
				.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE);

		Amount<Angle> dihedralCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
				.getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE);


		Amount<Angle> riggingAngleCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
				.getRiggingAngle().to(NonSI.DEGREE_ANGLE);




		//variation factors

		double xPosCanardFactor=15.0; //enter a percentage value
		//double zPosCanardFactor=15; //enter a percentage value
		double spanFactor=15.0;       //enter a percentage value
		//double d_sweepFactor=15; //enter an angular value
		double d_dihedralFactor=6.0; //enter an angular value
		//double d_riggingFactor=15; //enter an angular value

		//modified geometric parameters' vectors

		double[] xPosCanardVector = new double[] {0};//,-xApexCanard.doubleValue(SI.METER)*(xPosCanardFactor/100),
		//+xApexWing.doubleValue(SI.METER)*(xPosCanardFactor/100)};



		double deltaZPos=Math.abs(zApexWing.doubleValue(SI.METER)-zApexCanard.doubleValue(SI.METER));
		double[] zPosCanardVector = new double[] {0};//,-deltaZPos,-2*deltaZPos};



		double[] spanCanardVector = new double[] {0};//,-(spanFactor/100),
		//+(spanFactor/100)};



		double[] sweepCanardVector = new double[]   {0};//,-sweepLECanard.doubleValue(NonSI.DEGREE_ANGLE),
		//-2*sweepLECanard.doubleValue(NonSI.DEGREE_ANGLE)};

		double[] dihedralCanardVector = new double[] {0};//,-d_dihedralFactor,
		//+d_dihedralFactor};

		double deltaRiggingAngle=Math.abs(riggingAngleCanard.doubleValue(NonSI.DEGREE_ANGLE)-riggingAngleWing.doubleValue(NonSI.DEGREE_ANGLE));
		double[] riggingAngleCanardVector = new double[] {0};//,-deltaRiggingAngle,
		//-2*deltaRiggingAngle};

		//iterations

		List<String> caseFolderPaths = new ArrayList<>();


		//TODO MACH NUMBER

		double[] machNumberVector=new double[] {0.2};
		//{0.4,0.6};

		for(int countm=0; countm<machNumberVector.length; countm++) {

			for(int c1=0;c1<xPosCanardVector.length;c1++) {

				for(int c2=0;c2<zPosCanardVector.length;c2++) {

					for(int c3=0;c3<spanCanardVector.length;c3++) {

						for(int c4=0;c4<sweepCanardVector.length;c4++) {

							for(int c5=0;c5<dihedralCanardVector.length;c5++) {

								for(int c6=0;c6<riggingAngleCanardVector.length;c6++) {


									// Apply modifications to lifting surfaces

									AeroComponents modComponentEnum = AeroComponents.CANARD;

									if(aeroMap.containsKey(modComponentEnum)) {

										if(modComponentEnum.equals(AeroComponents.FUSELAGE)) return;

										LiftingSurface originalComponent = (LiftingSurface) aeroMap.get(modComponentEnum).get(0);
										LiftingSurface modComponent = null;

										switch(modComponentEnum.name()) {

										case "WING": 
											modComponent = AircraftUtils.importAircraft(args).getWing();
											break;

										case "HORIZONTAL":
											modComponent = AircraftUtils.importAircraft(args).getHTail();
											break;

										case "VERTICAL":
											modComponent = AircraftUtils.importAircraft(args).getVTail();
											break;

										case "CANARD":
											modComponent = AircraftUtils.importAircraft(args).getCanard();
											break;

										default:
											break;
										}


										double modSpanCanard = originalComponent
												.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METER)*(1+spanCanardVector[c3]);

										double modSurfaceCanard = originalComponent.getEquivalentWing().getPanels().get(0)
												.getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);

										double modTaperRatioCanard = originalComponent.getEquivalentWing().getPanels().get(0).getTaperRatio(); 

										Amount<Angle> modSweepCanard = originalComponent.getEquivalentWing().getPanels().get(0)
												.getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE).plus(Amount.valueOf(sweepCanardVector[c4], NonSI.DEGREE_ANGLE));

										Amount<Angle> modDihedralCanard = originalComponent.getEquivalentWing().getPanels().get(0)
												.getDihedral().to(NonSI.DEGREE_ANGLE).plus(Amount.valueOf(dihedralCanardVector[c5], NonSI.DEGREE_ANGLE));




										Amount<Angle> modTipTwistCanard = 
												originalComponent.getEquivalentWing().getPanels().get(0)
												.getTwistGeometricAtTip().to(NonSI.DEGREE_ANGLE);//.plus(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE)); 

										if(c4==2) {
											modDihedralCanard=modDihedralCanard.plus(Amount.valueOf(0.1, NonSI.DEGREE_ANGLE));
											////									        modTipTwistCanard=modTipTwistCanard.plus(Amount.valueOf(1.0, NonSI.DEGREE_ANGLE)); 
										}

										modComponent.adjustDimensions(
												modSpanCanard,
												modSurfaceCanard,
												modTaperRatioCanard,
												modSweepCanard,
												modDihedralCanard,
												modTipTwistCanard,
												WingAdjustCriteriaEnum.SPAN_AREA_TAPER
												);




										modComponent.setAirfoilList(originalComponent.getAirfoilList());	
										modComponent.setXApexConstructionAxes(xApexCanard.
												plus(Amount.valueOf(xPosCanardVector[c1],SI.METER)));
										modComponent.setZApexConstructionAxes(zApexCanard.
												plus(Amount.valueOf(zPosCanardVector[c2], SI.METER)));
										modComponent.setRiggingAngle(riggingAngleCanard.plus(Amount.valueOf(riggingAngleCanardVector[c6], NonSI.DEGREE_ANGLE)));






										aeroMap.get(modComponentEnum).remove(0);
										aeroMap.get(modComponentEnum).add(modComponent);		



										// Define geometric data

										String cadUnits = "mm";
										String aeroComponents = Arrays.toString(aeroMap.keySet().toArray());
										String componentsNumber = Arrays.toString(aeroMap.values().stream().mapToInt(list -> list.size()).toArray());

										double fuselageLength = (aeroMap.containsKey(AeroComponents.FUSELAGE)) ? 
												((Fuselage) aeroMap.get(AeroComponents.FUSELAGE).get(0)).getFuselageLength().doubleValue(SI.METER) : 0;


												double eqWingRootChord;
												double eqWingTaperRatio;
												double wingMAC;
												double wingS;
												double momentPoleXCoord;

												if(aeroMap.containsKey(AeroComponents.WING)) {
													LiftingSurface wing = (LiftingSurface) aeroMap.get(AeroComponents.WING).get(0);		
													eqWingRootChord = wing.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
													eqWingTaperRatio = wing.getEquivalentWing().getPanels().get(0).getTaperRatio();
													wingMAC = wing.getMeanAerodynamicChord().doubleValue(SI.METER);
													wingS = wing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
													momentPoleXCoord = wing.getXApexConstructionAxes().doubleValue(SI.METER) + 
															wing.getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER) + 
															wing.getMeanAerodynamicChord().doubleValue(SI.METER)*0.25;
												}

												else {

													return;
												}

												//											double canardMAC;
												//											double canardS;
												//											double canardMomentPoleXCoord;
												//											if(aeroMap.containsKey(AeroComponents.CANARD)) {
												//												LiftingSurface canard = (LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0);
												//												canardMAC = canard.getMeanAerodynamicChord().doubleValue(SI.METER);
												//												canardS = canard.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
												//												canardMomentPoleXCoord = canard.getXApexConstructionAxes().doubleValue(SI.METER) + 
												//														canard.getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER) + 
												//														canard.getMeanAerodynamicChord().doubleValue(SI.METER)*0.25;}        
												//						
												//
												//
												//											else {
												//
												//												return;
												//											}

												double span_Wing = spanWing.doubleValue(SI.METER);
												double xPosWing = xApexWing.doubleValue(SI.METER);
												double zPosWing = zApexWing.doubleValue(SI.METER);
												double sweepWing = sweepLEWing.doubleValue(NonSI.DEGREE_ANGLE);
												double dihedralWing = dihedralAngleWing.doubleValue(NonSI.DEGREE_ANGLE);
												double riggingWing = riggingAngleWing.doubleValue(NonSI.DEGREE_ANGLE);
												double xPosCanard = modComponent.getXApexConstructionAxes().doubleValue(SI.METER);
												double zPosCanard = modComponent.getZApexConstructionAxes().doubleValue(SI.METER);
												double eqCanardRootChord = modComponent.getEquivalentWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
												double eqCanardTaperRatio = modComponent.getEquivalentWing().getPanels().get(0).getTaperRatio();
												double span_Canard = modSpanCanard;
												double sweepCanard = modSweepCanard.doubleValue(NonSI.DEGREE_ANGLE);
												double dihedral_Canard = modDihedralCanard.doubleValue(NonSI.DEGREE_ANGLE);
												double rigging_Angle_Canard = modComponent.getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE);

												//											System.out.println("span_w "+span_Wing);
												//											System.out.println("sweep_w " +sweepWing);
												//											System.out.println("dihedral_w " +dihedralWing);
												//											System.out.println("rigging_w " +riggingWing);
												//											System.out.println("eqRootChord_w " +eqWingRootChord);
												//											System.out.println("eqTaper_w " +eqWingTaperRatio);
												//											System.out.println("span_c "+span_Canard);
												//											System.out.println("sweep_c " +sweepCanard);
												//											System.out.println("dihedral_c " +dihedral_Canard);
												//											System.out.println("rigging_c " +rigging_Angle_Canard);
												//											System.out.println("eqRootChord_c " +eqCanardRootChord);
												//											System.out.println("eqTaper_c " +eqCanardTaperRatio);








												GeometricData geometricData = new GeometricData(
														new GeometricDataBuilder(
																cadUnits, 
																aeroComponents, 
																componentsNumber, 
																fuselageLength, 
																wingMAC,
																eqWingRootChord,
																eqWingTaperRatio,
																wingS, 
																span_Wing,
																momentPoleXCoord,
																xPosWing,
																zPosWing,
																sweepWing,
																dihedralWing,
																riggingWing,
																xPosCanard,
																zPosCanard,
																eqCanardRootChord,
																eqCanardTaperRatio,
																span_Canard,
																sweepCanard,
																dihedral_Canard,
																rigging_Angle_Canard
																));

												// Create aircraft CAD files

												List<String> cadNames = new ArrayList<>();

												for(Iterator<AeroComponents> comp = aeroMap.keySet().iterator(); comp.hasNext(); ) {
													AeroComponents component = comp.next();

													switch(component.name()) {

													case "FUSELAGE":
														int nFus = aeroMap.get(component).size();				 
														for(int i = 0; i < nFus; i++) {
															String cadName = (nFus > 1) ? ("FUSELAGE_" + (i + 1)) : "FUSELAGE";
															cadNames.add(cadName);
															OCCUtils.write(cadName, FileExtension.STEP, AircraftCADUtils.getFuselageCAD(
																	(Fuselage) aeroMap.get(component).get(i), 7, 7, false, false, true));
															////														AircraftUtils.getAircraftSolidFile(
															//////																AircraftUtils.getFuselageCAD(
															//////																		(Fuselage) aeroMap.get(component).get(i), 7, 7, true, true, false), 
															//////																cadName, 
															//////																FileExtension.STEP);
														}
														break;

													case "WING":
														int nWng = aeroMap.get(component).size();				 
														for(int i = 0; i < nWng; i++) {
															String cadName = (nWng > 1) ? ("WING_" + (i + 1)) : "WING";
															cadNames.add(cadName);
															OCCUtils.write(cadName, FileExtension.STEP,AircraftCADUtils.getLiftingSurfaceCAD(
																	(LiftingSurface) aeroMap.get(component).get(i), WingTipType.ROUNDED, false, false, true));
															//////														AircraftUtils.getAircraftSolidFile(
															//////																AircraftUtils.getLiftingSurfaceCAD(
															//////																		(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.WING, 1e-3, false, true, false), 
															//////																cadName, 
															//////																FileExtension.STEP);
														}
														break;

													case "HORIZONTAL":
														int nHtl = aeroMap.get(component).size();				 
														for(int i = 0; i < nHtl; i++) {
															String cadName = (nHtl > 1) ? ("HORIZONTAL_" + (i + 1)) : "HORIZONTAL";
															cadNames.add(cadName);
															OCCUtils.write(cadName, FileExtension.STEP,AircraftCADUtils.getLiftingSurfaceCAD(
																	(LiftingSurface) aeroMap.get(component).get(i), WingTipType.ROUNDED, false, false, true));
															//////														AircraftUtils.getAircraftSolidFile(
															//////																AircraftUtils.getLiftingSurfaceCAD(
															//////																		(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false), 
															//////																cadName, 
															//////																FileExtension.STEP);
														}
														break;	

													case "VERTICAL":
														int nVtl = aeroMap.get(component).size();				 
														for(int i = 0; i < nVtl; i++) {
															String cadName = (nVtl > 1) ? ("VERTICAL_" + (i + 1)) : "VERTICAL";
															cadNames.add(cadName);
															OCCUtils.write(cadName, FileExtension.STEP,AircraftCADUtils.getLiftingSurfaceCAD(
																	(LiftingSurface) aeroMap.get(component).get(i), WingTipType.ROUNDED, false, false, true));
															//////														AircraftUtils.getAircraftSolidFile(
															//////																AircraftUtils.getLiftingSurfaceCAD(
															//////																		(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.VERTICAL_TAIL, 1e-3, false, true, false), 
															//////																cadName, 
															//////																FileExtension.STEP);
														}
														break;	

													case "CANARD":
														int nCnd = aeroMap.get(component).size();				 
														for(int i = 0; i < nCnd; i++) {
															String cadName = (nCnd > 1) ? ("CANARD_" + (i + 1)) : "CANARD";
															cadNames.add(cadName);
															OCCUtils.write(cadName, FileExtension.STEP,AircraftCADUtils.getLiftingSurfaceCAD(
																	(LiftingSurface) aeroMap.get(component).get(i), WingTipType.ROUNDED, false, false, true));
															////////														AircraftUtils.getAircraftSolidFile(
															////////																AircraftUtils.getLiftingSurfaceCAD(
															////////																		(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.CANARD, 1e-3, false, true, false), 
															////////																cadName, 
															////////																FileExtension.STEP);
														}
														break;	

													default:

														break;					
													}
												}


												//TODO 
												double[] alphaVector=new double[] {-2,0,4};





												//iterations

												for(int counta=0; counta<alphaVector.length; counta++){

													// Define operating conditions

													double angleOfAttack = alphaVector[counta];
													double sideslipAngle = 0.0;
													double machNumber = machNumberVector[countm];
													double altitude = 30000.0; //ft
													double deltaTemperature = 0.0;
													double altitudeM = Amount.valueOf(altitude, NonSI.FOOT).doubleValue(SI.METER);
													double deltaTemperatureM = Amount.valueOf(deltaTemperature, SI.CELSIUS).doubleValue(SI.CELSIUS);

													StdAtmos1976 atmosphere = AtmosphereCalc.getAtmosphere(altitudeM, deltaTemperatureM);
													double pressure = atmosphere.getPressure();
													double density = atmosphere.getDensity()*1000;
													double temperature = atmosphere.getTemperature();
													double speedOfSound = atmosphere.getSpeedOfSound();
													double dynamicViscosity = AtmosphereCalc.getDynamicViscosity(altitudeM, deltaTemperatureM);
													double velocity = speedOfSound*machNumber;
													double reynoldsNumber = density*velocity*wingMAC/dynamicViscosity;

													OperatingConditions operatingConditions = new OperatingConditions(
															new OperatingConditionsBuilder(
																	angleOfAttack, 
																	sideslipAngle, 
																	machNumber, 
																	reynoldsNumber, 
																	altitude, 
																	pressure, 
																	density, 
																	temperature, 
																	speedOfSound, 
																	dynamicViscosity, 
																	velocity
																	)
															);

													// Define simulation parameters

													String simType = "EULER";
													boolean symmetricalSim = true;
													boolean executeAutomesh = false;
													int xPosCase=c1;
													int zPosCase=c2;
													int spanCase=c3;
													int sweepCase=c4;
													int dihedralCase=c5;
													int riggingCase=c6;
													int machCase=countm;
													int alphaCase=counta;


													SimulationParameters simulationParameters = new SimulationParameters(
															new SimulationParametersBuilder(
																	simType, 
																	symmetricalSim, 
																	executeAutomesh,
																	xPosCase,
																	zPosCase,
																	spanCase,
																	sweepCase,
																	dihedralCase,
																	riggingCase,
																	machCase,
																	alphaCase
																	)
															);

													// Create the data file

													DataWriter writer = new DataWriter(
															operatingConditions, 
															geometricData, 
															simulationParameters			
															);


													// Clean working folders 

													String destFolder = workingFolderPath + File.separator + "Case_" + c1 + "_" + c2 + "_" + c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm + "_" +  counta;
													caseFolderPaths.add(destFolder);
													File directory = new File(destFolder);
													directory.mkdir();
													//Arrays.asList(new File (workingFolderPath).listFiles()).forEach(f -> f.delete());
													Arrays.asList(new File(starTempFolder).listFiles()).forEach(f -> f.delete());

													// Copy CAD and data files to working folder

													String stepFolder = jpadCADFolder;

													cadNames.forEach(s -> {
														try { 
															Files.copy(		
																	Paths.get(stepFolder + File.separator + s + ".step"), 
																	Paths.get(destFolder + File.separator + s + ".step"),
																	StandardCopyOption.REPLACE_EXISTING
																	);
															Files.copy(
																	Paths.get(stepFolder + File.separator + s + ".step"), 
																	Paths.get(starTempFolder + File.separator + s + ".step"),
																	StandardCopyOption.REPLACE_EXISTING
																	);
														} 
														catch (IOException e) {
															e.printStackTrace();
														}
													});



													writer.write(destFolder + File.separator + "Data_"+c1+"_"+c2+"_"+c3+"_"+c4+"_"+c5+"_"+c6+"_"+countm+"_"+counta+".xml");
													writer.write(starTempFolder + File.separator + "Data_"+c1+"_"+c2+"_"+c3+"_"+c4+"_"+c5+"_"+c6+"_"+countm+"_"+counta+".xml");

													//TODO												// Run STARCCM+ simulation using macro.java
													try {
														Runtime runtime = Runtime.getRuntime();

														Process runTheMacro = runtime.exec(
																"cmd /c cd\\ && cd " + macroPath + " && dir && " + // change directory
																		"\"" + starExePath + "\" " +               // run the application
																		starOptions + " " +                        // set license and settings
																		"-new -batch " + macroName               // start new simulation in batch mode
																); 

														BufferedReader input = new BufferedReader(new InputStreamReader(runTheMacro.getInputStream()));

														String line = null;		
														while((line = input.readLine()) != null) System.out.println(line);

														int exitVal = runTheMacro.waitFor();
														System.out.println("Exited with error code " + exitVal);
													}

													catch(Exception e) {
														System.out.println(e.toString());
														e.printStackTrace();
													}

												}
									}			
								}				
							}

						}	

					}

				}
			}

		}


		File directory = new File(starTempFolder);

		for(File file: directory.listFiles()) 
			if (!file.isDirectory()) 
				file.delete();



	}


	public enum AeroComponents {
		FUSELAGE,
		WING,
		HORIZONTAL,
		VERTICAL,
		CANARD
	}



}



