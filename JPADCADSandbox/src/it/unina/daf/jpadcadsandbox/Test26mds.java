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
import it.unina.daf.jpadcadsandbox.utils.DataWriter;
import it.unina.daf.jpadcadsandbox.utils.GeometricData;
import it.unina.daf.jpadcadsandbox.utils.GeometricData.GeometricDataBuilder;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions.OperatingConditionsBuilder;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters.SimulationParametersBuilder;
import standaloneutils.aerotools.aero.StdAtmos1976;
import standaloneutils.atmosphere.AtmosphereCalc;

public class Test26mds {
	
	public static final String workingFolderPath = "C:\\Users\\Mario\\Documents\\Tesi_Magistrale\\Test_Macro_Star";
	public static final String jpadCADFolder = "C:\\Users\\Mario\\JPAD_PROJECT\\jpad\\JPADCADSandbox";
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
		
		// Remove undesired components
//		aeroMap.remove(AeroComponents.FUSELAGE);
		
		// Modify lifting surfaces
		AeroComponents customComponentEnum = AeroComponents.WING;
		
		if(aeroMap.containsKey(customComponentEnum)) {
		
			if(customComponentEnum.equals(AeroComponents.FUSELAGE)) return;
			
			LiftingSurface originalComponent = (LiftingSurface) aeroMap.get(customComponentEnum).get(0);
			LiftingSurface customComponent = null;
			
			switch(customComponentEnum.name()) {
			
			case "WING": 
				customComponent = AircraftUtils.importAircraft(args).getWing();
				break;
				
			case "HORIZONTAL":
				customComponent = AircraftUtils.importAircraft(args).getHTail();
				break;
				
			case "VERTICAL":
				customComponent = AircraftUtils.importAircraft(args).getVTail();
				break;
				
			case "CANARD":
				customComponent = AircraftUtils.importAircraft(args).getCanard();
				break;
				
			default:
				break;
			}
			
			customComponent.adjustDimensions(
			originalComponent
					.getAspectRatio()*1.2,
			originalComponent.getEquivalentWing().getPanels().get(0)
					.getChordRoot().doubleValue(SI.METER)*1.0,
			originalComponent.getEquivalentWing().getPanels().get(0)
					.getChordTip().doubleValue(SI.METER)*1.0, 
//			originalComponent.getEquivalentWing().getPanels().get(0)
//					.getSweepLeadingEdge(),
			Amount.valueOf(25, NonSI.DEGREE_ANGLE).to(SI.RADIAN),
			originalComponent.getEquivalentWing().getPanels().get(0)
					.getDihedral(), 
			originalComponent.getEquivalentWing().getPanels().get(0)
					.getTwistGeometricAtTip(), 
			WingAdjustCriteriaEnum.AR_ROOTCHORD_TIPCHORD
			);
			
			Amount<Angle> sweepLE = originalComponent.getPanels().get(0).getSweepLeadingEdge();
			
			List<Amount<Angle>> sweeps = new ArrayList<>();		
			sweeps.add(Amount.valueOf(0, NonSI.DEGREE_ANGLE));
			sweeps.add(sweepLE.to(NonSI.DEGREE_ANGLE));
			sweeps.add(sweepLE.opposite().to(NonSI.DEGREE_ANGLE));
			
			customComponent.setAirfoilList(originalComponent.getAirfoilList());	
			customComponent.setXApexConstructionAxes(originalComponent.getXApexConstructionAxes().plus(Amount.valueOf(1.0, SI.METER)));
			customComponent.setYApexConstructionAxes(originalComponent.getYApexConstructionAxes());
			customComponent.setZApexConstructionAxes(originalComponent.getZApexConstructionAxes().plus(Amount.valueOf(0.2, SI.METER)));
			
			aeroMap.get(customComponentEnum).remove(0);
			aeroMap.get(customComponentEnum).add(customComponent);
		}
		
		// Define geometric data
		String cadUnits = "mm";
		String aeroComponents = Arrays.toString(aeroMap.keySet().toArray());
		String componentsNumber = Arrays.toString(aeroMap.values().stream().mapToInt(list -> list.size()).toArray());
		
		double fuselageLength = (aeroMap.containsKey(AeroComponents.FUSELAGE)) ? 
				((Fuselage) aeroMap.get(AeroComponents.FUSELAGE).get(0)).getFuselageLength().doubleValue(SI.METER) : 0;
		
		double wingMAC;
		double wingS;
		double wingSpan;
		double momentPoleXCoord;
		if(aeroMap.containsKey(AeroComponents.WING)) {
			LiftingSurface wing = (LiftingSurface) aeroMap.get(AeroComponents.WING).get(0);
			wingMAC = wing.getMeanAerodynamicChord().doubleValue(SI.METER);
			wingS = wing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
			wingSpan = wing.getSpan().doubleValue(SI.METER);
			momentPoleXCoord = wing.getXApexConstructionAxes().doubleValue(SI.METER) + 
	                  		   wing.getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER) + 
	                           wing.getMeanAerodynamicChord().doubleValue(SI.METER)*0.25;
		} else {
			
			return;
		}

		GeometricData geometricData = new GeometricData(
				new GeometricDataBuilder(
						cadUnits, 
						aeroComponents, 
						componentsNumber, 
						fuselageLength, 
						wingMAC, 
						wingS, 
						wingSpan, 
						momentPoleXCoord
						)
				);
		
		// Define operating conditions
		double angleOfAttack = 2.0;
		double sideslipAngle = 0.0;
		double machNumber = 0.64;
		double altitude = 30000; 
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
		
		SimulationParameters simulationParameters = new SimulationParameters(
				new SimulationParametersBuilder(
						simType, 
						symmetricalSim, 
						executeAutomesh
						)
				);

		// Create the data file
		DataWriter writer = new DataWriter(
				operatingConditions, 
				geometricData, 
				simulationParameters			
				);
		
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

		// Clean working folder
		String destFolder = workingFolderPath;
		File directory = new File(destFolder);
		Arrays.asList(directory.listFiles()).forEach(f -> f.delete());
		
		// Copy CAD and data files to working folder
		String stepFolder = jpadCADFolder;
		
		cadNames.forEach(s -> {
			try { 
				Files.copy(		
						Paths.get(stepFolder + "\\" + s + ".step"), 
						Paths.get(destFolder + "\\" + s + ".step"), 
						StandardCopyOption.REPLACE_EXISTING
						);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		writer.write(workingFolderPath + "\\Data.xml");
		
		// Run STARCCM+ simulation using macro.java
		try {
			Runtime runtime = Runtime.getRuntime();
			
			Process runTheMacro = runtime.exec(
					"cmd /c cd\\ && cd " + macroPath + " && dir && " + // change directory
					"\"" + starExePath + "\" " +                       // run the application
					starOptions + " " +                                // set license and settings
					"-new -batch " + macroName						   // start new simulation in batch mode
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
	
	public enum AeroComponents {
		FUSELAGE,
		WING,
		HORIZONTAL,
		VERTICAL,
		CANARD
	}
}
