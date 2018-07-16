package it.unina.daf.jpadcadsandbox;

import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Fuse;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepPrimAPI_MakeCylinder;
import opencascade.BRepPrimAPI_MakeRevol;
import opencascade.BRepPrimAPI_MakeSphere;
import opencascade.BRepPrimAPI_MakeTorus;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;

public class TestBoolean01mds {

	// Testing OpenCascade boolean operations
	// example taken from https://algotopia.com/contents/opencascade/opencascade_basic
	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		// Generate a sphere
		double sphereRadius = 1.0;
		double sphereAngle = Math.atan(0.5);
		gp_Ax2 sphereOrigin = new gp_Ax2(
				new gp_Pnt(0.0, 0.0, 0.0), 
				new gp_Dir(0.0, 0.0, 1.0));
		
		TopoDS_Shape sphere = new BRepPrimAPI_MakeSphere(
				sphereOrigin, 
				sphereRadius,
			   -sphereAngle,
				sphereAngle
				).Shape();
		
		// Create a first cylinder, in order to drill a hole through the sphere
		double cylinder1Radius = 0.4;
		double cylinder1Height = 2.0;
		gp_Ax2 cylinder1Origin = new gp_Ax2(
				new gp_Pnt(0.0, 0.0, -cylinder1Height/2.0), 
				new gp_Dir(0.0, 0.0, 1.0));
		
		TopoDS_Shape cylinder1 = new BRepPrimAPI_MakeCylinder(
				cylinder1Origin, 
				cylinder1Radius, 
				cylinder1Height
				).Shape();
		
		TopoDS_Shape drilledSphere = new BRepAlgoAPI_Cut(sphere, cylinder1).Shape();
		
		// Create a second cylinder and move it in order to subtract it repeatedly from the drilled sphere
		double cylinder2Radius = 0.25;
		double cylinder2Height = 2.0;
		gp_Ax2 cylinder2Origin = new gp_Ax2(
				new gp_Pnt(0.0, 0.0, -cylinder2Height/2.0), 
				new gp_Dir(0.0, 0.0, 1.0));
		
		TopoDS_Shape cylinder2 = new BRepPrimAPI_MakeCylinder(
				cylinder2Origin, 
				cylinder2Radius, 
				cylinder2Height
				).Shape();
		
		gp_Trsf move = new gp_Trsf();
		TopoDS_Shape drilledShape = drilledSphere;
		TopoDS_Shape movedCylinder;
		double cloneRadius = 1.0;
		
		for(int clone = 0; clone < 8; clone++) {
			double angle = clone*Math.PI/4.0;
			
			move.SetTranslation(new gp_Vec(
					Math.cos(angle)*cloneRadius, 
					Math.sin(angle)*cloneRadius,
					0.0
					));
			
			movedCylinder = new BRepBuilderAPI_Transform(cylinder2, move, 1).Shape();
			drilledShape = new BRepAlgoAPI_Cut(drilledShape, movedCylinder).Shape();
		}
		
		// Create a torus and fuse it with the drilled shape
		double ringRadius = 0.25;
		double torusRadius = 1.0 - ringRadius;
		TopoDS_Shape torus = new BRepPrimAPI_MakeTorus(torusRadius, ringRadius).Shape();
		
		TopoDS_Shape fusedShape = new BRepAlgoAPI_Fuse(drilledShape, torus).Shape();
		
		// Create an hexagonal face in order to generate a solid and subtract it from the drilled shape
		TColgp_Array1OfPnt facePoints = new TColgp_Array1OfPnt(1, 7);
		double faceInnerRadius = 0.6;
		
		facePoints.SetValue(1, new gp_Pnt(faceInnerRadius - 0.05, 0.0, -0.05 ));
		facePoints.SetValue(2, new gp_Pnt(faceInnerRadius - 0.10, 0.0, -0.025));
		facePoints.SetValue(3, new gp_Pnt(faceInnerRadius - 0.10, 0.0,  0.025));
		facePoints.SetValue(4, new gp_Pnt(faceInnerRadius + 0.10, 0.0,  0.025));
		facePoints.SetValue(5, new gp_Pnt(faceInnerRadius + 0.10, 0.0, -0.025));
		facePoints.SetValue(6, new gp_Pnt(faceInnerRadius + 0.05, 0.0, -0.05 ));
		facePoints.SetValue(7, new gp_Pnt(faceInnerRadius - 0.05, 0.0, -0.05 ));
		
		BRepBuilderAPI_MakeWire hexWire = new BRepBuilderAPI_MakeWire();
		for(int i = 1; i < 7; i++) {
			BRepBuilderAPI_MakeEdge hexEdge = new BRepBuilderAPI_MakeEdge(
					facePoints.Value(i), 
					facePoints.Value(i+1)
					);
			hexWire.Add(hexEdge.Edge());
		}
		
		TopoDS_Face hexFace = new BRepBuilderAPI_MakeFace(hexWire.Wire()).Face();
		
		gp_Ax1 revolveAxis = new gp_Ax1(
				new gp_Pnt(0.0, 0.0, 0.0), 
				new gp_Dir(0.0, 0.0, 1.0)
				);
		TopoDS_Shape revolvedShape = new BRepPrimAPI_MakeRevol(hexFace, revolveAxis).Shape();
		
		gp_Trsf move2 = new gp_Trsf();
		move2.SetTranslation(
				new gp_Pnt(0.0, 0.0, 0.0), 
				new gp_Pnt(0.0, 0.0, Math.sin(0.5))
				);
		TopoDS_Shape movedShape = new BRepBuilderAPI_Transform(revolvedShape, move2, 0).Shape();
		
		TopoDS_Shape finalShape = new BRepAlgoAPI_Cut(drilledShape, movedShape).Shape();
		
		// Export the final shape to CAD file
		OCCUtils.initCADShapeFactory();	
		OCCShape exportShapes = (OCCShape) OCCUtils.theFactory.newShape(finalShape);
		
		String fileName = "testBoolean01mds.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}
}
