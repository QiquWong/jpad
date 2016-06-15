package cad.aircraft;

import java.io.File;

import org.jcae.opencascade.jni.BRepTools;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.IGESControl_Controller;
import org.jcae.opencascade.jni.IGESControl_Writer;
import org.jcae.opencascade.jni.STEPControl_StepModelType;
import org.jcae.opencascade.jni.STEPControl_Writer;
import org.jcae.opencascade.jni.StlAPI_Writer;
import org.jcae.opencascade.jni.TopoDS_Compound;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import writers.JPADStaticWriteUtils;

/**
 * Generate the CAD model of an Aircraft object.
 * Uses the Classes which generate the CAD model of the fuselage
 * and of a generic lifting surface.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class MyAircraftBuilder {
	
	private TopoDS_Compound theCurrentCompound;

	public MyAircraftBuilder() { }
	
	/**
	 * 
	 * @param aircraft
	 * @param path
	 */
	public void buildAndWriteCAD(Aircraft aircraft, String path) {
		theCurrentCompound = buildCAD(aircraft);
		writeToFile(path, aircraft.get_name(), theCurrentCompound);
	}
	
	/**
	 * 
	 * @param aircraft
	 * @return
	 */
	public TopoDS_Compound buildCAD(Aircraft aircraft) {
		
		String currentFolder = JPADStaticWriteUtils.createNewFolder(MyConfiguration.cadDirectory + aircraft.get_name() + File.separator);
		BRep_Builder theBuilder = new BRep_Builder();
		TopoDS_Compound theCompound = new TopoDS_Compound();
		theBuilder.makeCompound(theCompound);
		
		// Export Fuselage CAD
		MyFuselageBuilder fusCADBuilder = new MyFuselageBuilder(aircraft.getFuselage());
		fusCADBuilder.buildAndWriteCAD(false, true, true, currentFolder, aircraft.get_name() + "Fuselage");
		theBuilder.add(theCompound, fusCADBuilder.getTheCompound());
		
		// Export wing CAD
		MyLiftingSurfaceBuilder lsCADBuilder = new MyLiftingSurfaceBuilder(aircraft.getWing());
		lsCADBuilder.buildAndWriteCAD(false, true, false, currentFolder, aircraft.get_name() + "Wing");
		theBuilder.add(theCompound, lsCADBuilder.getTheCompound());
		
		// Export htail CAD
		lsCADBuilder = new MyLiftingSurfaceBuilder(aircraft.getHTail());
		lsCADBuilder.buildAndWriteCAD(false, true, false, currentFolder, aircraft.get_name() + "HTail");
		theBuilder.add(theCompound, lsCADBuilder.getTheCompound());
		
		// Export vtail CAD
		lsCADBuilder = new MyLiftingSurfaceBuilder(aircraft.getVTail());
		lsCADBuilder.buildAndWriteCAD(false, true, false, currentFolder, aircraft.get_name() + "VTail");
		theBuilder.add(theCompound, lsCADBuilder.getTheCompound());
		
		theCurrentCompound = theCompound;
		return theCompound;
	}

	/**
	 * 
	 * @param dir
	 * @param fileName
	 * @param theCompound
	 */
	public static void write(File dir, String fileName, TopoDS_Compound theCompound) {
	
		JPADStaticWriteUtils.createNewFolder(dir.getAbsolutePath());
		File f = new File(dir.getAbsolutePath() + File.separator + fileName);
		if(f.exists()) f.delete();
	
		if (fileName.endsWith(".igs") || fileName.endsWith(".IGS") ||
				fileName.endsWith(".iges") || fileName.endsWith(".IGES")) {
			// write in IGES format
			new IGESControl_Controller().init();
			IGESControl_Writer aWriter = new IGESControl_Writer("MM", 0);
			aWriter.addShape(theCompound);
			aWriter.computeModel();
			aWriter.write(dir.getAbsolutePath()+ File.separator +fileName);
		}
	
		if (fileName.endsWith(".brep") || 
				fileName.endsWith(".BREP") ||
				fileName.endsWith(".BRep")) {
			// write on file in BRep format
			BRepTools.write(theCompound, dir.getAbsolutePath() + File.separator + fileName);
		}
	
		if (fileName.endsWith(".stl") || 
				fileName.endsWith(".STL")) {
			// write on file in STL format
			StlAPI_Writer writer = new StlAPI_Writer();
			writer.write(theCompound, dir.getAbsolutePath()+ File.separator +fileName);
		}
	
		if (fileName.endsWith(".step") || 
				fileName.endsWith(".STEP") ||
				fileName.endsWith(".stp")  ||
				fileName.endsWith(".STP")) {
			// write on file in BRep format
			STEPControl_Writer aWriter = new STEPControl_Writer();
			aWriter.transfer(theCompound, STEPControl_StepModelType.AsIs);
			aWriter.write(dir.getAbsolutePath() + File.separator + fileName);
		}
	}

	/** 
	 * Write CAD to file
	 * 
	 * @param fileName
	 */
	public static void writeToFile(String path, String fileName, TopoDS_Compound theCompound) {
		write(new File(path), fileName + ".brep", theCompound);
		write(new File(path), fileName + ".step", theCompound);
	}

	public TopoDS_Compound getTheCurrentCompound() {
		return theCurrentCompound;
	}

}
