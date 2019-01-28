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
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcadsandbox.utils.CaWiXUtils;
import it.unina.daf.jpadcadsandbox.utils.DataWriter;
import it.unina.daf.jpadcadsandbox.utils.GeometricData;
import it.unina.daf.jpadcadsandbox.utils.GeometricData.GeometricDataBuilder;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions.OperatingConditionsBuilder;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters.SimulationParametersBuilder;
import standaloneutils.MyArrayUtils;
import standaloneutils.aerotools.aero.StdAtmos1976;
import standaloneutils.atmosphere.AtmosphereCalc;

public class CreateCasesGB {

	private String workingFolderPath;
	private String jpadCADFolderPath;
	private String starTempFolderPath;
	private String macroPath;
	private String macroName;
	private String starExePath; 
	private String starOptions; 
	
	private List<Double> xPosCanardPcntVarList = new ArrayList<>();
	private List<Amount<Length>> zPosCanardList = new ArrayList<>();
	private List<Double> spanCanardPcntVarList = new ArrayList<>();
	private List<Amount<Angle>> sweepCanardList = new ArrayList<>();
	private List<Amount<Angle>> dihedralCanardList = new ArrayList<>();
	private List<Amount<Angle>> riggingAngleCanardList = new ArrayList<>();
	private List<Amount<Angle>> alphaList = new ArrayList<>();
	private List<Double> machList = new ArrayList<>();

	private Aircraft theAircraft = null;

	private CaWiXUtils caseManager = null; 
	

	public static void main(String[] args) throws IOException {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		CreateCasesGB theApp = new CreateCasesGB();		

		theApp.setCaseManager(new CaWiXUtils());
		theApp.getCaseManager().importData(args, theApp);	

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
				if(!(theApp.getAircraft().getFuselage() == null))
					aeroMap.get(AeroComponents.FUSELAGE).add(theApp.getAircraft().getFuselage());
				else {
					System.out.println("There's no FUSELAGE component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;

			case WING:
				if(!(theApp.getAircraft().getWing() == null))
					aeroMap.get(AeroComponents.WING).add(theApp.getAircraft().getWing());
				else {
					System.out.println("There's no WING component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;

			case HORIZONTAL:
				if(!(theApp.getAircraft().getHTail() == null))
					aeroMap.get(AeroComponents.HORIZONTAL).add(theApp.getAircraft().getHTail());
				else {
					System.out.println("There's no HORIZONTAL component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;

			case VERTICAL:
				if(!(theApp.getAircraft().getVTail() == null))
					aeroMap.get(AeroComponents.VERTICAL).add(theApp.getAircraft().getVTail());
				else {
					System.out.println("There's no VERTICAL component for the selected aircraft!");
					aeroMap.remove(component);
				}

				break;	

			case CANARD:
				if(!(theApp.getAircraft().getCanard() == null))
					aeroMap.get(AeroComponents.CANARD).add(theApp.getAircraft().getCanard());
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

//		Amount<Length> spanCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
//				.getEquivalentWing().getPanels().get(0).getSpan().times(2);
//
//		Amount<Angle> sweepLECanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
//				.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE);
//
//		Amount<Angle> dihedralCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
//				.getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE);


		Amount<Angle> riggingAngleCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0))
				.getRiggingAngle().to(NonSI.DEGREE_ANGLE);




		//variation factors
		//
		//		double xPosCanardFactor=15.0; //enter a percentage value
		//		//double zPosCanardFactor=15; //enter a percentage value
		//		double spanFactor=15.0;       //enter a percentage value
		//		//double d_sweepFactor=15; //enter an angular value
		//		double d_dihedralFactor=6.0; //enter an angular value
		//		//double d_riggingFactor=15; //enter an angular value
		//
		//		//modified geometric parameters' vectors

		//double[] xPosCanardVector = new double[] {0};//,-xApexCanard.doubleValue(SI.METER)*(xPosCanardFactor/100),
		//+xApexWing.doubleValue(SI.METER)*(xPosCanardFactor/100)};

		double[] xPosCanardVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(theApp.getXPosCanardPcntVarList()));

		//double deltaZPos=Math.abs(zApexWing.doubleValue(SI.METER)-zApexCanard.doubleValue(SI.METER));

		double[] zPosCanardVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfAmountToDoubleArray(theApp.getZPosCanardList()));


		double[] spanCanardVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(theApp.getSpanCanardPcntVarList()));


		double[] sweepCanardVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfAmountToDoubleArray(theApp.getSweepCanardList()));


		double[] dihedralCanardVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfAmountToDoubleArray(theApp.getDihedralCanardList()));

		//double deltaRiggingAngle=Math.abs(riggingAngleCanard.doubleValue(NonSI.DEGREE_ANGLE)-riggingAngleWing.doubleValue(NonSI.DEGREE_ANGLE));

		double[] riggingAngleCanardVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfAmountToDoubleArray(theApp.getRiggingAngleCanardList()));

		double[] machNumberVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(theApp.getMachList()));
		
		double[] alphaVector = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfAmountToDoubleArray(theApp.getAlphaList()));
		
		
		//iterations


		for(int countm = 0; countm < machNumberVector.length; countm++) {

			for(int c1 = 0; c1 < xPosCanardVector.length; c1++) {

				for(int c2 = 0; c2 < zPosCanardVector.length; c2++) {

					for(int c3 = 0; c3 < spanCanardVector.length; c3++) {

						for(int c4 = 0; c4 < sweepCanardVector.length; c4++) {

							for(int c5 = 0; c5 < dihedralCanardVector.length; c5++) {

								for(int c6 = 0; c6 < riggingAngleCanardVector.length; c6++) {


									// Apply modifications to lifting surfaces

									AeroComponents modComponentEnum = AeroComponents.CANARD;

									if(aeroMap.containsKey(modComponentEnum)) {

										if(modComponentEnum.equals(AeroComponents.FUSELAGE)) return;

										LiftingSurface originalComponent = (LiftingSurface) aeroMap.get(modComponentEnum).get(0);
										LiftingSurface modComponent = null;

										switch(modComponentEnum.name()) {

										case "WING": 
											modComponent = theApp.getCaseManager().importAircraft(
													theApp.getCaseManager().getVa()).getWing();
											break;

										case "HORIZONTAL":
											modComponent = theApp.getCaseManager().importAircraft(
													theApp.getCaseManager().getVa()).getHTail();
											break;

										case "VERTICAL":
											modComponent = theApp.getCaseManager().importAircraft(
													theApp.getCaseManager().getVa()).getVTail();
											break;

										case "CANARD":
											modComponent = theApp.getCaseManager().importAircraft(
													theApp.getCaseManager().getVa()).getCanard();
											break;

										default:
											break;
										}


										double modSpanCanard = originalComponent
												.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METER)*(1 + spanCanardVector[c3]);

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

										if(2 == c4) {
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

//													case "WING":
//														int nWng = aeroMap.get(component).size();				 
//														for(int i = 0; i < nWng; i++) {
//															String cadName = (nWng > 1) ? ("WING_" + (i + 1)) : "WING";
//															cadNames.add(cadName);
//															OCCUtils.write(cadName, FileExtension.STEP,AircraftCADUtils.getLiftingSurfaceCAD(
//																	(LiftingSurface) aeroMap.get(component).get(i), WingTipType.ROUNDED, false, false, true));
//															//////														AircraftUtils.getAircraftSolidFile(
//															//////																AircraftUtils.getLiftingSurfaceCAD(
//															//////																		(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.WING, 1e-3, false, true, false), 
//															//////																cadName, 
//															//////																FileExtension.STEP);
//														}
//														break;

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

												//iterations

												for(int counta=0; counta < alphaVector.length; counta++){

													// Define operating conditions

													double angleOfAttack = alphaVector[counta];
													double sideslipAngle = 0.0;
													double machNumber = machNumberVector[countm];
													//TODO		
													double altitude = 0.0; //ft
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
													List<String> caseFolderPaths = new ArrayList<>();
													String destFolder = theApp.getWorkingFolderPath()+"//CANARD_ALONE"+File.separator + "Case_" + c1 + "_" + c2 + "_" + c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm + "_" +  counta;
													caseFolderPaths.add(destFolder);
													File directory = new File(destFolder);
													directory.mkdir();
													//Arrays.asList(new File (theApp.workingFolderPath).listFiles()).forEach(f -> f.delete());
													Arrays.asList(new File(theApp.getStarTempFolderPath()).listFiles()).forEach(f -> f.delete());

													// Copy CAD and data files to working folder

													String stepFolder = theApp.getJPADCADFolderPath();

													cadNames.forEach(s -> {
														try { 
															Files.copy(		
																	Paths.get(stepFolder + File.separator + s + ".step"), 
																	Paths.get(destFolder + File.separator + s + ".step"),
																	StandardCopyOption.REPLACE_EXISTING
																	);
															Files.copy(
																	Paths.get(stepFolder + File.separator + s + ".step"), 
																	Paths.get(theApp.getStarTempFolderPath() + File.separator + s + ".step"),
																	StandardCopyOption.REPLACE_EXISTING
																	);
														} 
														catch (IOException e) {
															e.printStackTrace();
														}
													});



													writer.write(destFolder + File.separator + "Data_"+c1+"_"+c2+"_"+c3+"_"+c4+"_"+c5+"_"+c6+"_"+countm+"_"+counta+".xml");
													writer.write(theApp.getStarTempFolderPath() + File.separator + "Data_"+c1+"_"+c2+"_"+c3+"_"+c4+"_"+c5+"_"+c6+"_"+countm+"_"+counta+".xml");

													//TODO												// Run STARCCM+ simulation using macro.java
													try {
														Runtime runtime = Runtime.getRuntime();

														Process runTheMacro = runtime.exec(
																"cmd /c cd\\ && cd " + theApp.getMacroPath() + " && dir && " + // change directory
																		"\"" + theApp.getStarExePath() + "\" " +               // run the application
																		theApp.getStarOptions() + " " +                        // set license and settings
																		"-new -batch " + theApp.getMacroName()               // start new simulation in batch mode
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


		File directory = new File(theApp.getStarTempFolderPath());

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


	public void setWorkingFolderPath(String workingFolderPath) {
		this.workingFolderPath = workingFolderPath;
	}

	public void setStarTempFolderPath(String starTempFolderPath) {
		this.starTempFolderPath = starTempFolderPath;
	}


	public void setMacroPath(String macroPath) {
		this.macroPath = macroPath;
	}


	public void setMacroName(String macroName) {
		this.macroName = macroName;
	}


	public void setStarExePath(String starExePath) {
		this.starExePath = starExePath; 
	}


	public void setStarOptions(String starOptions) {
		this.starOptions = starOptions; 	
	}


	public void setAlphaList(List<Amount<Angle>> alphaList) {
		this.alphaList = alphaList;	
	}


	public void setMachList(List<Double> machList) {
		this.machList = machList;	
	}


	public void setRiggingAngleCanardList(List<Amount<Angle>> riggingAngleCanardList) {
		this.riggingAngleCanardList = riggingAngleCanardList;	
	}


	public void setDihedralCanardList(List<Amount<Angle>> dihedralCanardList) {
		this.dihedralCanardList = dihedralCanardList;	
	}


	public void setSweepCanardList(List<Amount<Angle>> sweepCanardList) {
		this.sweepCanardList = sweepCanardList;
	}


	public void setSpanCanardPcntVarList(List<Double> spanCanardPcntVarList) {
		this.spanCanardPcntVarList = spanCanardPcntVarList;
	}


	public void setZPosCanardList(List<Amount<Length>> zPosCanardList) {
		this.zPosCanardList = zPosCanardList;
	}


	public void setXPosCanardPcntVarList(List<Double> xPosCanardPcntVarList) {
		this.xPosCanardPcntVarList = xPosCanardPcntVarList;
	}

	public void setAircraft(Aircraft aircraft) {
		this.theAircraft = aircraft;
	}


	public String getJPADCADFolderPath() {
		return this.jpadCADFolderPath;
	}


	public void setJPADCADFolderPath(String jpadCADFolderPath) {
		this.jpadCADFolderPath = jpadCADFolderPath;
	}


	public List<Double> getXPosCanardPcntVarList() {
		return this.xPosCanardPcntVarList;
	}


	public void setxPosCanardPcntVarList(List<Double> xPosCanardPcntVarList) {
		this.xPosCanardPcntVarList = xPosCanardPcntVarList;
	}


	public List<Amount<Length>> getZPosCanardList() {
		return this.zPosCanardList;
	}


	public void setzPosCanardList(List<Amount<Length>> zPosCanardList) {
		this.zPosCanardList = zPosCanardList;
	}


	public Aircraft getAircraft() {
		return this.theAircraft;
	}

	public String getWorkingFolderPath() {
		return this.workingFolderPath;
	}


	public String getStarTempFolderPath() {
		return this.starTempFolderPath;
	}


	public String getMacroPath() {
		return this.macroPath;
	}


	public String getMacroName() {
		return this.macroName;
	}


	public String getStarExePath() {
		return this.starExePath;
	}


	public String getStarOptions() {
		return this.starOptions;
	}


	public List<Double> getSpanCanardPcntVarList() {
		return this.spanCanardPcntVarList;
	}


	public List<Amount<Angle>> getSweepCanardList() {
		return this.sweepCanardList;
	}


	public List<Amount<Angle>> getDihedralCanardList() {
		return this.dihedralCanardList;
	}


	public List<Amount<Angle>> getRiggingAngleCanardList() {
		return this.riggingAngleCanardList;
	}


	public List<Amount<Angle>> getAlphaList() {
		return this.alphaList;
	}


	public List<Double> getMachList() {
		return this.machList;
	}

	public CaWiXUtils getCaseManager() {
		return caseManager;
	}

	public void setCaseManager(CaWiXUtils caseManager) {
		this.caseManager = caseManager;
	}



}



