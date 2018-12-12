package it.unina.daf.jpadcadsandbox;

import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Fuse;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.BRepOffsetAPI_MakeThickSolid;
import opencascade.BRepPrimAPI_MakeCylinder;
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GeomAPI_Interpolate;
import opencascade.Geom_BSplineCurve;
import opencascade.Geom_Circle;
import opencascade.Geom_CylindricalSurface;
import opencascade.Geom_Plane;
import opencascade.Geom_Surface;
import opencascade.GC_Root;
import opencascade.Geom2d_BezierCurve;
import opencascade.Geom2d_Curve;
import opencascade.Geom2d_Ellipse;
import opencascade.Geom2d_TrimmedCurve;
import opencascade.Precision;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TColgp_Array1OfPnt2d;
import opencascade.TColgp_Array1OfVec;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TColgp_HArray1OfPnt2d;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.TopoDS_Vertex;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Ax2d;
import opencascade.gp_Ax3;
import opencascade.gp_Dir;
import opencascade.gp_Dir2d;
import opencascade.gp_Pnt;
import opencascade.gp_Pnt2d;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import opencascade.Geom_TrimmedCurve;
import opencascade.Standard_Transient;
import opencascade.Standard_Type;

/* This test is used to illustrate how to use Open CASCADE
 * classes for a 3D object modeling. In this tutorial we will
 * create a model of a simple bottle similar to the one presented here:
 * https://www.opencascade.com/doc/occt-7.0.0/overview/html/occt__tutorial.html
 */

public class Test02as {

	// First of all let's define the main geometric specification
	// of the model.

	 static double myHeight = 70;
	 static double myWidth = 50;
	 static double myThickness = 30;

	 public static void main(String[] args) {

		 /* Building the profile:
		  * To create the bottle's profile we will use a mirror transformation therefore
		  * we just need to create halves of profile's points.
		  */
		 
		 gp_Pnt p1 = new gp_Pnt(-myWidth/2,0,0);
		 gp_Pnt p2 = new gp_Pnt(-myWidth/2,-myThickness/4,0);
		 gp_Pnt p3 = new gp_Pnt(0,-myThickness/2,0);
		 gp_Pnt p4 = new gp_Pnt(myWidth/2,-myThickness/4,0);
		 gp_Pnt p5 = new gp_Pnt(myWidth/2,0,0);

		 // Edges creation
		 TopoDS_Edge edge1 = new BRepBuilderAPI_MakeEdge(p1,p2).Edge();
		 TopoDS_Edge edge3 = new BRepBuilderAPI_MakeEdge(p4,p5).Edge();
		 // The last edge is obtained by using a BSpline interpolation
		 TColgp_HArray1OfPnt points = new TColgp_HArray1OfPnt(1, 3);
		 points.SetValue(1, p2);
		 points.SetValue(2, p3);
		 points.SetValue(3, p4);
		 // Creating a periodic 3D curve
		 GeomAPI_Interpolate aperiodicCurve = new GeomAPI_Interpolate(points,0, Precision.Confusion());
		 aperiodicCurve.Perform();
		 Geom_BSplineCurve interpCurve = aperiodicCurve.Curve();
		 TopoDS_Edge edge2 = new BRepBuilderAPI_MakeEdge(interpCurve).Edge();				

		 // Wires creation	
		 TopoDS_Wire wire = new BRepBuilderAPI_MakeWire(edge1,edge2,edge3).Wire();
		 
		 // Mirror transformation
		 gp_Pnt aPoint = new gp_Pnt(0,0,0);
		 gp_Dir aDir = new gp_Dir(1,0,0);
		 gp_Ax1 axis = new gp_Ax1(aPoint,aDir);
		 gp_Trsf mirror = new gp_Trsf();
		 mirror.SetMirror(axis);

		 // Final wire
		 TopoDS_Wire wire2 = TopoDS.ToWire(new BRepBuilderAPI_Transform(wire,mirror).Shape());
		 BRepBuilderAPI_MakeWire mkWire = new BRepBuilderAPI_MakeWire();
		 mkWire.Add(wire);
		 mkWire.Add(wire2);
		 TopoDS_Wire finalWire = TopoDS.ToWire(mkWire.Shape());

		 /* Building the body:
		  * To create the body of the bottle we will use BRepPrimAPI_MakePrism class
		  * that generates a solid if a face is given as input. Then we proceed by applying
		  * fillets on all the edges of the body using a while loop.
		  */
		 
		 TopoDS_Shape face = new BRepBuilderAPI_MakeFace(finalWire).Face();
		 TopoDS_Shape solid = new BRepPrimAPI_MakePrism(face,new gp_Vec(0,0,myHeight)).Shape();
		 
		 BRepFilletAPI_MakeFillet fillets = new BRepFilletAPI_MakeFillet(solid);
		 TopExp_Explorer anEdgeExplorer = new TopExp_Explorer(solid,TopAbs_ShapeEnum.TopAbs_EDGE);

		 while(anEdgeExplorer.More()>0) {			
			 TopoDS_Edge anEdge = TopoDS.ToEdge(anEdgeExplorer.Current());
			 fillets.Add(myThickness/12, anEdge);
			 anEdgeExplorer.Next();			
		 }

		 TopoDS_Shape solid2 = fillets.Shape();

		 // Adding the neck

		 gp_Pnt pNeck = new gp_Pnt(0,0,myHeight);
		 gp_Dir dNeck = new gp_Dir(new gp_Vec(0,0,1));
		 gp_Ax2 nAxis = new gp_Ax2(pNeck,dNeck);

		 double radius = myThickness/4;
		 double nHeight = myHeight/10;

		 TopoDS_Shape neck = new BRepPrimAPI_MakeCylinder(nAxis,radius,nHeight).Shape();
		 
		 TopoDS_Shape solid3 = new BRepAlgoAPI_Fuse(solid2,neck).Shape();
		 
		 /* Creating a Hollowed Solid:
		  * Now that we have a solid we just need to create a hollowed solid. The easiest way 
		  * to do that is by using a Boolean cutting operation with a second body. The second body
		  * is defined by scaling and translating the original profile and reapiting all the operations
		  * previously done on the first body.  
		  */
		 
		 // Scale transformation
		 gp_Trsf scale1 = new gp_Trsf();
		 scale1.SetScale(new gp_Pnt(0, 0,0),0.9);
		 TopoDS_Wire finalWire2 = TopoDS.ToWire(new BRepBuilderAPI_Transform(finalWire,scale1).Shape());
		 BRepBuilderAPI_MakeWire mkWire3 = new BRepBuilderAPI_MakeWire();
		 mkWire3.Add(finalWire2);
		 TopoDS_Wire finalWire3 = TopoDS.ToWire(mkWire3.Shape());
		 // Translation transformation
		 gp_Trsf trasl1 = new gp_Trsf();
		 trasl1.SetTranslation(new gp_Pnt(0,0,0),new gp_Pnt(0,0,3));
		 TopoDS_Wire finalWire4 = TopoDS.ToWire(new BRepBuilderAPI_Transform(finalWire3,trasl1).Shape());
		 BRepBuilderAPI_MakeWire mkWire4 = new BRepBuilderAPI_MakeWire();
		 mkWire4.Add(finalWire4);
		 TopoDS_Wire finalWire5 = TopoDS.ToWire(mkWire4.Shape());

		 // Body creation
		 TopoDS_Face faceSub = new BRepBuilderAPI_MakeFace(finalWire5).Face();
		 TopoDS_Shape solidSub = new BRepPrimAPI_MakePrism(faceSub,new gp_Vec(0,0,(myHeight-6))).Shape();

		 // Adding the neck

		 gp_Pnt pNeck2 = new gp_Pnt(0,0,(myHeight-6));
		 gp_Dir dNeck2 = new gp_Dir(new gp_Vec(0,0,1));
		 gp_Ax2 nAxis2 = new gp_Ax2(pNeck2,dNeck2);

		 double radius2 = radius*0.95;
		 double nHeight2 = nHeight*4;

		 TopoDS_Shape neckSub = new BRepPrimAPI_MakeCylinder(nAxis2,radius2,nHeight2).Shape();

		 // Boolean fuse operation
		 TopoDS_Shape solid3Sub = new BRepAlgoAPI_Fuse(solidSub,neckSub).Shape();

		 // Final Boolean cut
		 TopoDS_Shape finalSolid = new BRepAlgoAPI_Cut(solid3, solid3Sub).Shape();



		 BRep_Builder builder = new BRep_Builder();
		 TopoDS_Compound compound = new TopoDS_Compound();
		 builder.MakeCompound(compound);
		 builder.Add(compound,finalSolid);

		 //Write to a file
		 String fileName = "test02as.brep";
		 BRepTools.Write(compound, fileName);

		 System.out.println("Output written on file: " + fileName);




		 // Hallow solid creation
		 //		TopoDS_Shape faceToRemove = new TopoDS_Shape();
		 //		double zMax = -1;
		 //
		 //		for(TopExp_Explorer aFaceExplorer = new TopExp_Explorer(solid3,TopAbs_ShapeEnum.TopAbs_FACE); aFaceExplorer.More()>0; aFaceExplorer.Next())
		 //		{
		 //			TopoDS_Face aFace = TopoDS.ToFace(aFaceExplorer.Current());
		 //
		 //			Geom_Surface aSurface = BRep_Tool.Surface(aFace);
		 //
		 //			if(aSurface.DynamicType() == Geom_Plane.get_type_descriptor())
		 //
		 //			{	
		 //				Geom_Plane plane;			
		 //				plane = Geom_Plane.DownCast(aSurface);
		 //				gp_Pnt point = plane.Location();
		 //				double z = point.Z();
		 //				if (z > zMax)
		 //				{
		 //					zMax = z;
		 //					faceToRemove = aFace;
		 //				}
		 //
		 //			}
		 //		}
		 //		
		 //		TopTools_ListOfShape facesToRemove = new TopTools_ListOfShape();
		 //		facesToRemove.Append(faceToRemove);
		 //		System.out.println(facesToRemove.Size());
		 //		
		 //		double tolerance = 0.001;
		 //		double wallThickness = -myThickness/50;
		 //		// Qui da problemi!!
		 //		BRepOffsetAPI_MakeThickSolid solid4 = new BRepOffsetAPI_MakeThickSolid(solid3,facesToRemove,wallThickness,tolerance);
		 //		// 
		 //		TopoDS_Shape solid5 = solid4.Shape();

		 // Threading creation
		 //		gp_Ax3 axisCy = new gp_Ax3(nAxis);
		 //		Geom_CylindricalSurface cylinder1 = new Geom_CylindricalSurface(axisCy,radius*0.99);
		 //		Geom_CylindricalSurface cylinder2 = new Geom_CylindricalSurface(axisCy,radius*1.05);
		 //		
		 //		gp_Pnt2d p2D = new gp_Pnt2d(2*Math.PI,nHeight/2);
		 //		gp_Dir2d d2D = new gp_Dir2d(2*Math.PI,nHeight/4);
		 //		gp_Ax2d axis2D = new gp_Ax2d(p2D,d2D);
		 //		
		 //		double aMajor = 2*Math.PI;
		 //		double aMinor = nHeight/10;
		 //		Geom2d_Ellipse ellipse1 = new Geom2d_Ellipse(axis2D,aMajor,aMinor);
		 //		Geom2d_Ellipse ellipse2 = new Geom2d_Ellipse(axis2D,aMajor,aMinor/4);
		 //		
		 //		Geom2d_TrimmedCurve arc1 = new Geom2d_TrimmedCurve(ellipse1,0,Math.PI);
		 //		Geom2d_TrimmedCurve arc2 = new Geom2d_TrimmedCurve(ellipse2,0,Math.PI);
		 //		
		 //		gp_Pnt2d ellipsePnt1 = new gp_Pnt2d();
		 //		ellipse1.D0(0, ellipsePnt1);
		 //		System.out.println(ellipsePnt1.Coord(1) + "  "+ ellipsePnt1.Coord(2));
		 //		gp_Pnt2d ellipsePnt2 = new gp_Pnt2d();
		 //		ellipse2.D0(0, ellipsePnt2);
		 //		gp_Pnt ell1 = new gp_Pnt(ellipsePnt1.X(),ellipsePnt1.Y(),0);
		 //		gp_Pnt ell2 = new gp_Pnt(ellipsePnt2.X(),ellipsePnt2.Y(),0);
		 //		
		 //		
		 //		
		 //		TopoDS_Edge anEdge1 = new BRepBuilderAPI_MakeEdge(ell1,ell2).Edge();
		 //		TopoDS_Vertex aVertex1 = new BRepBuilderAPI_MakeVertex(ell1).Vertex();
		 //		TopoDS_Vertex aVertex2 = new BRepBuilderAPI_MakeVertex(ell2).Vertex();
		 //		
		 //		TColgp_Array1OfPnt2d points2d = new TColgp_Array1OfPnt2d(1, 2);
		 //		points2d.SetValue(1, ellipsePnt1);
		 //		points2d.SetValue(2, ellipsePnt2);
		 //		
		 //		TopoDS_Edge anEdge1OnSurf2 = new BRepBuilderAPI_MakeEdge(arc2, cylinder2).Edge();
		 //		TopoDS_Edge anEdge1OnSurf1 = new BRepBuilderAPI_MakeEdge(arc1, cylinder1).Edge();
		 //		TopoDS_Edge anEdge2 = new BRepBuilderAPI_MakeEdge(aVertex1,aVertex2).Edge();
		 //		
		 //		TopoDS_Wire threadingWire1 = new BRepBuilderAPI_MakeWire(anEdge1OnSurf1,anEdge2).Wire();
		 //		TopoDS_Wire threadingWire2 = new BRepBuilderAPI_MakeWire(anEdge1OnSurf2,anEdge2).Wire();


		 //		BRep_Builder builder = new BRep_Builder();
		 //		TopoDS_Compound compound = new TopoDS_Compound();
		 //		builder.MakeCompound(compound);
		 //		builder.Add(compound, solid5);
		 //		
		 //		//Write to a file
		 //		String fileName = "test02as.brep";
		 //		BRepTools.Write(compound, fileName);
		 //		
		 //		System.out.println("Output written on file: " + fileName);




	 }

}
