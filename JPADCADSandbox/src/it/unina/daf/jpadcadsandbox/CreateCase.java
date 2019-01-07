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
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.FileExtension;

public class CreateCase {

	public static final String workingFolderPath = "C:\\Users\\Mario\\Desktop\\CAD_Test";
	public static final String jpadCADFolder = "C:\\Users\\Mario\\JPAD_PROJECT\\jpad\\JPADCADSandbox";
	public static final String starTempFolder = "C:\\Users\\Mario\\Desktop\\Temp_Folder";
	public static final String macroPath = "Users\\Mario\\eclipse-workspace\\STARCCM\\src\\test";
	public static final String macroName = "Test_MultipleExecutes.java";
	public static final String starExePath = "C:\\Program Files\\CD-adapco\\13.04.010-R8\\STAR-CCM+13.04.010-R8\\star\\bin\\starccm+.exe";
	public static final String starOptions = "-cpubind -np 4 -rsh ssh";

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

		// Remove unnecessary components
		aeroMap.remove(AeroComponents.FUSELAGE);
		aeroMap.remove(AeroComponents.HORIZONTAL);
		aeroMap.remove(AeroComponents.VERTICAL);
		//aeroMap.remove(AeroComponents.CANARD);
		
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

		Amount<Length> p_rootChordCanard = ((LiftingSurface) aeroMap.get(AeroComponents.CANARD).get(0)) //p stands for "projected"
				.getChordsBreakPoints().get(0).times(Math.cos(riggingAngleCanard.doubleValue(SI.RADIAN)));

		// Acquiring original Wing data
		Amount<Length> xApexWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getXApexConstructionAxes();

		Amount<Length> zApexWing =  ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getZApexConstructionAxes();

		Amount<Length> spanWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getEquivalentWing().getPanels().get(0).getSpan().times(2);

		Amount<Angle> sweepLEWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE);

		Amount<Angle> dihedralAngleWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE);

		Amount<Angle> riggingAngleWing = ((LiftingSurface) aeroMap.get(AeroComponents.WING).get(0))
				.getRiggingAngle().to(NonSI.DEGREE_ANGLE);

		//variation factors
		double xPosCanardFactor=15; //enter a percentage value
		//double zPosCanardFactor=15; //enter a percentage value
		double spanFactor=15;       //enter a percentage value
		//double d_sweepFactor=15; //enter an angular value
		double d_dihedralFactor=6; //enter an angular value
		//double d_riggingFactor=15; //enter an angular value

		//modified geometric parameters' vectors
		double[] xPosCanardVector = new double[] {0,-xApexCanard.doubleValue(SI.METER)*(xPosCanardFactor/100),
				+xApexWing.doubleValue(SI.METER)*(xPosCanardFactor/100)}; 

		double deltaZPos=Math.abs(zApexWing.doubleValue(SI.METER)-zApexCanard.doubleValue(SI.METER));
		double[] zPosCanardVector = new double[] {0,-deltaZPos,
				-2*deltaZPos}; 

		double[] spanCanardVector = new double[] {0,-spanCanard.doubleValue(SI.METER)*(spanFactor/100), 
				+spanCanard.doubleValue(SI.METER)*(spanFactor/100)};

		//double delta_sweep_=sweepLECanard.doubleValue(NonSI.DEGREE_ANGLE)-sweepLEWing.doubleValue(NonSI.DEGREE_ANGLE);
		double[] sweepCanardVector = new double[] {0,-sweepLECanard.doubleValue(NonSI.DEGREE_ANGLE),
				-2*sweepLECanard.doubleValue(NonSI.DEGREE_ANGLE)};

		double[] dihedralCanardVector = new double[] {0,-d_dihedralFactor,
				+d_dihedralFactor};

		double deltaRiggingAngle=Math.abs(riggingAngleCanard.doubleValue(NonSI.DEGREE_ANGLE)-riggingAngleWing.doubleValue(NonSI.DEGREE_ANGLE));
		double[] riggingAngleCanardVector = new double[] {0,-deltaRiggingAngle,
				-2*deltaRiggingAngle};

		//iterations

		List<String> caseFolderPaths = new ArrayList<>();

		for(int c1=0;c1<xPosCanardVector.length;c1++)
		{
			for(int c2=0;c2<zPosCanardVector.length;c2++)
			{
				for(int c3=0;c3<spanCanardVector.length;c3++)
				{
					for(int c4=0;c4<sweepCanardVector.length;c4++)
					{
						for(int c5=0;c5<dihedralCanardVector.length;c5++)
						{
							for(int c6=0;c6<riggingAngleCanardVector.length;c6++)
							{
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
											.getEquivalentWing().getPanels().get(0).getSpan().times(2).doubleValue(SI.METER);//+spanCanardVector[c3];

									double modSurfaceCanard = originalComponent.getEquivalentWing().getPanels().get(0)
											.getSurfacePlanform().times(2).doubleValue(SI.SQUARE_METRE);

									double modTaperRatioCanard = originalComponent.getEquivalentWing().getPanels().get(0).getTaperRatio(); 

									Amount<Angle> modSweepCanard = originalComponent.getEquivalentWing().getPanels().get(0)
											.getSweepLeadingEdge().plus(Amount.valueOf(sweepCanardVector[c4], NonSI.DEGREE_ANGLE));

									Amount<Angle> modDihedralCanard = originalComponent.getEquivalentWing().getPanels().get(0)
											.getDihedral().plus(Amount.valueOf(dihedralCanardVector[c5], NonSI.DEGREE_ANGLE));

									Amount<Angle> modRiggingCanard = originalComponent.getRiggingAngle()
											.plus(Amount.valueOf(riggingAngleCanardVector[c6], NonSI.DEGREE_ANGLE));

									Amount<Angle> modTipTwistCanard = 
											originalComponent.getEquivalentWing().getPanels().get(0)
											.getTwistGeometricAtTip();

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
									modComponent.setXApexConstructionAxes(originalComponent.getXApexConstructionAxes().
											plus(Amount.valueOf(xPosCanardVector[c1],SI.METER)));
									modComponent.setZApexConstructionAxes(originalComponent.getZApexConstructionAxes().
											plus(Amount.valueOf(zPosCanardVector[c2], SI.METER)));
									modComponent.setRiggingAngle(modRiggingCanard);

									aeroMap.get(modComponentEnum).remove(0);
									aeroMap.get(modComponentEnum).add(modComponent);		

									
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
												AircraftUtils.getAircraftSolidFile(
														AircraftUtils.getFuselageCAD(
																(Fuselage) aeroMap.get(component).get(i), 7, 7, true, true, false), 
														cadName, 
														FileExtension.STEP);
											}
											break;

										case "WING":
											int nWng = aeroMap.get(component).size();				 
											for(int i = 0; i < nWng; i++) {
												String cadName = (nWng > 1) ? ("WING_" + (i + 1)) : "WING";
												cadNames.add(cadName);
												AircraftUtils.getAircraftSolidFile(
														AircraftUtils.getLiftingSurfaceCAD(
																(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.WING, 1e-3, false, true, false), 
														cadName, 
														FileExtension.STEP);
											}
											break;

										case "HORIZONTAL":
											int nHtl = aeroMap.get(component).size();				 
											for(int i = 0; i < nHtl; i++) {
												String cadName = (nHtl > 1) ? ("HORIZONTAL_" + (i + 1)) : "HORIZONTAL";
												cadNames.add(cadName);
												AircraftUtils.getAircraftSolidFile(
														AircraftUtils.getLiftingSurfaceCAD(
																(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false), 
														cadName, 
														FileExtension.STEP);
											}
											break;	

										case "VERTICAL":
											int nVtl = aeroMap.get(component).size();				 
											for(int i = 0; i < nVtl; i++) {
												String cadName = (nVtl > 1) ? ("VERTICAL_" + (i + 1)) : "VERTICAL";
												cadNames.add(cadName);
												AircraftUtils.getAircraftSolidFile(
														AircraftUtils.getLiftingSurfaceCAD(
																(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.VERTICAL_TAIL, 1e-3, false, true, false), 
														cadName, 
														FileExtension.STEP);
											}
											break;	

										case "CANARD":
											int nCnd = aeroMap.get(component).size();				 
											for(int i = 0; i < nCnd; i++) {
												String cadName = (nCnd > 1) ? ("CANARD_" + (i + 1)) : "CANARD";
												cadNames.add(cadName);
												AircraftUtils.getAircraftSolidFile(
														AircraftUtils.getLiftingSurfaceCAD(
																(LiftingSurface) aeroMap.get(component).get(i), ComponentEnum.CANARD, 1e-3, false, true, false), 
														cadName, 
														FileExtension.STEP);
											}
											break;	

										default:

											break;					
										}
									}

									//modified operating conditions' vectors

									//if incomprimibile metti un riconoscimento (tipo)

									double[] machNumberVector=new double[] {0};//,0.4,0.6};// INCOMPRIMIBILE

									double[] alphaVector=new double[] {0};

									//iterations

									for(int countm=0; countm<machNumberVector.length; countm++)
									{
										for(int counta=0; counta<alphaVector.length; counta++) 
										{ 
											// Clean working folders
											String destFolder = workingFolderPath + File.separator + "Case_" + c1 + "_" + c2 + "_" + c3 + "_" + c4+ "_" + c5 + "_" + c6 + "_" + countm + "_" + counta;
											caseFolderPaths.add(destFolder);
											File directory = new File(destFolder);
											directory.mkdir();
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
											
										}
										
									}
									
								}
								System.gc();
							}
							System.gc();
						}
						System.gc();
					}
					System.gc();
				}
				System.gc();
			} 
			System.gc();
		}
		System.gc();
	}


	public enum AeroComponents {
		FUSELAGE,
		WING,
		HORIZONTAL,
		VERTICAL,
		CANARD
	}

}