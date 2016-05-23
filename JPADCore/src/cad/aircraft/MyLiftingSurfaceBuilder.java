package cad.aircraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.unit.SI;

import org.jcae.opencascade.jni.BRepBuilderAPI_MakeEdge;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeVertex;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeWire;
import org.jcae.opencascade.jni.BRepBuilderAPI_Sewing;
import org.jcae.opencascade.jni.BRepGProp;
import org.jcae.opencascade.jni.BRepOffsetAPI_ThruSections;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.GProp_GProps;
import org.jcae.opencascade.jni.GeomAPI_Interpolate;
import org.jcae.opencascade.jni.Geom_BSplineCurve;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Shape;
import org.jcae.opencascade.jni.TopoDS_Vertex;
import org.jcae.opencascade.jni.TopoDS_Wire;
import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import processing.core.PVector;

/**
 * Build the CAD model of a generic lifting surface
 * once given a LiftingSurfaceCreator object.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class MyLiftingSurfaceBuilder {

	private LiftingSurface theLiftingSurface;

	// OCC stuff
	BRep_Builder theBuilder = null;
	TopoDS_Compound theCompound = null;
	GProp_GProps thePropertyHolder = null; // store measurements

	TopoDS_Compound theCompoundHealed = null;
	BRep_Builder theBuilderHealed = null;

	private BRepOffsetAPI_ThruSections 
	_loft_NoseCap, 
	_loft_NosePart, 
	_loft_CilyndricalPart,
	_loft_TailPart,
	_loft_TailCap;

	private BRepOffsetAPI_ThruSections _loft, _loftL, _loftR;

	// operation stuff
	private boolean _makeSolid = false;	 // try to build a solid
	private boolean _closedSpline = false; // treat sections as closed curves
	private boolean _makeRuled = true;	 // try to build a solid

	// discretization parametrs
	int 
	_nPointsNoseCap      = 5, 
	_nPointsNosePart     = 25, 
	_nPointsCylindricalPart = 4, 
	_nPointsTailPart     = 15, 
	_nPointsTailCap      = 4;

	// Measurements
	private Amount<Area> _wettedArea = Amount.valueOf(0.0, SI.SQUARE_METRE);

	public MyLiftingSurfaceBuilder(LiftingSurface ls) {

		theLiftingSurface = ls;

		// prepare a compound/builder pair
		theBuilder = new BRep_Builder();
		theCompound = new TopoDS_Compound();
		theBuilder.makeCompound(theCompound);

		// initialize the object that stores measurements
		thePropertyHolder = new GProp_GProps();

		// If Eps is argument is absent, precision is quite poor
		if (theLiftingSurface == null) {
			System.out.println("Null lifting surface object!");
		}

	}// end-of constructor

	//	public void doFuse()
	//	{
	//
	//		BRepBuilderAPI_Sewing sewer = new BRepBuilderAPI_Sewing();
	//		sewer.add(_loft_NoseCap.shape());
	//		sewer.add(_loft_NosePart.shape());
	//		sewer.add(_loft_CilyndricalPart.shape());
	//		sewer.add(_loft_TailPart.shape());
	//		sewer.add(_loft_TailCap.shape());
	//		sewer.perform();
	//
	//		System.out.println(
	//				"............... N. solids in sewed: " +
	//						org.jcae.opencascade.Utilities.numberOfShape(sewer.sewedShape(), TopAbs_ShapeEnum.SOLID)
	//				);
	//
	//		BRep_Builder builder0 = new BRep_Builder();
	//		TopoDS_Compound compound0 = new TopoDS_Compound();
	//		builder0.makeCompound(compound0);
	//		builder0.add(compound0, sewer.sewedShape());
	//
	//	}

	/**
	 * Generate a loft by skinning through a point (optional), 
	 * then a list of ALL curves from nose to tail, and a point (optional)
	 * 
	 * @author Agostino De Marco
	 * @param point1 an initial point, PVector
	 * @param sections a list of curves (lists of points, PVector)
	 * @param point2 a final point, PVector
	 * @return 
	 * @return
	 */
	public TopoDS_Compound buildCAD() {

		System.out.println("-----------> Lofting the whole lifting surface");

		List<List<PVector>> sections = new ArrayList<List<PVector>>();

		int sz = theLiftingSurface.get_theAirfoilsList().size();
		
		PVector p = theLiftingSurface.get_theAirfoilsList()
				.get(theLiftingSurface.get_theAirfoilsList().size()-1).getGeometry()
				.getCentralPoint();
		
		PVector pR = null, pL = new PVector();

		if (theLiftingSurface.isMirrored() == true) {
			for(int i=0; i < theLiftingSurface.get_theAirfoilsList().size(); i++) {
				sections.add(theLiftingSurface.get_theAirfoilsList().get(i).getGeometry().get_coordinatesLeft());
			}

			pL.set(p.x, -p.y, p.z);
			_loftL = this.makeLoft(
					null,			// a point, PVector 
					sections,	// a list of curves (lists of points, PVector)
					pL		// a point, PVector
					);

			sections.clear();

			for (int j = theLiftingSurface.get_theAirfoilsList().size()-1; j >= 0 ; j--) {
				sections.add(theLiftingSurface.get_theAirfoilsList().get(j).getGeometry().get_coordinatesRight());
			}
			
			pR = p;
			_loftR = this.makeLoft(
					pR,			// a point, PVector 
					sections,	// a list of curves (lists of points, PVector)
					null		// a point, PVector
					);

			if (_loftL.isDone() && _loftR.isDone()) {
				theBuilder.add(theCompound, _loftL.shape());
				theBuilder.add(theCompound, _loftR.shape());				
			}

		} else {
			
			for (int j = theLiftingSurface.get_theAirfoilsList().size()-1; j >= 0 ; j--) {
				sections.add(theLiftingSurface.get_theAirfoilsList().get(j).getGeometry().get_coordinatesRight());
			}

			pR = p;
			_loft = this.makeLoft(
					pR,			// a point, PVector 
					sections,	// a list of curves (lists of points, PVector)
					null		// a point, PVector
					);

			if (_loft.isDone()) {
				theBuilder.add(theCompound, _loft.shape());				
			}
		}

		return theCompound;
	}

	/*
	 * @param boolean solid
	 * @param boolean ruled
	 * @param boolean closedSplines
	 * @param String path
	 * @param String fileName
	 * @return void
	 */
	public void buildAndWriteCAD(
			boolean solid, boolean ruled, 
			boolean closedSplines, 
			String path, String fileName) {

		set_makeSolid(solid);
		set_makeRuledLoft(ruled);
		set_closedSplines(closedSplines);
		buildCAD();

		MyAircraftBuilder.write( new File(path), fileName + ".brep", theCompound);
		MyAircraftBuilder.write( new File(path), fileName + ".step", theCompound);
	}

	private void doSewParts() {

		BRepBuilderAPI_Sewing sewer = new BRepBuilderAPI_Sewing();
		sewer.add(_loft_NosePart.shape());
		sewer.add(_loft_CilyndricalPart.shape());
		sewer.perform();

		// prepare a compound/builder pair
		theBuilderHealed = new BRep_Builder();
		theCompoundHealed = new TopoDS_Compound();
		theBuilderHealed.makeCompound(theCompoundHealed);

		theBuilderHealed.add(theCompoundHealed, sewer.sewedShape());

	}

	/**
	 * Generate a loft by skinning through a point (optional), 
	 * then a list of curves, and a point (optional)
	 * 
	 * @author Agostino
	 * @param point1 an initial point, PVector
	 * @param sections a list of curves (lists of points, PVector)
	 * @param point2 a final point, PVector
	 * @return
	 */
	private BRepOffsetAPI_ThruSections makeLoft(
			PVector point1, 
			List<List<PVector>> sections, 
			PVector point2
			)
	{
		if ((sections.size() <= 1) && (point1 == null)) return null;
		if ((sections.size() <= 1) && (point2 == null)) return null;

		BRepOffsetAPI_ThruSections loft = new BRepOffsetAPI_ThruSections(
				_makeSolid, // solid or not
				_makeRuled, // ruled surface or not
				1.0e-06     // precision criterion
				);

		// initial point, optional
		if (point1 != null) {
			TopoDS_Vertex vertex1 = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
					new double[]{ point1.x, point1.y, point1.z }
					).shape();
			loft.addVertex(vertex1);
		}

		int cCounter = 0;
		for (List<PVector> c : sections)
		{
			// build a proper data structure
			double[] points = new double[3*c.size()];
			int kk = 0;
			for (int i=0; i < c.size(); i++)
			{
				points[kk] = c.get(i).x;
				kk++;
				points[kk] = c.get(i).y;
				kk++;
				points[kk] = c.get(i).z;
				kk++;
			}

			GeomAPI_Interpolate repSpline = new GeomAPI_Interpolate(
					points, // list of points
					_closedSpline, // periodic or non-periodic 
					1E-7 // tolerance
					);

			repSpline.Perform();
			Geom_BSplineCurve s = repSpline.Curve();
			TopoDS_Edge spline = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s).shape();

			// Display various other properties
			BRepGProp.linearProperties(spline, thePropertyHolder);
			System.out.println(cCounter + ": spline length = " + thePropertyHolder.mass());

			// make wires
			BRepBuilderAPI_MakeWire wire = new BRepBuilderAPI_MakeWire();
			wire.add(new TopoDS_Shape[]{spline});

			// add wires to loft structure
			loft.addWire((TopoDS_Wire)wire.shape());

			cCounter++;
		}

		// final point, optional
		if (point2 != null)
		{
			TopoDS_Vertex vertex2 = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
					new double[]{ point2.x, point2.y, point2.z }
					).shape();
			loft.addVertex(vertex2);
		}

		loft.build();
		BRepGProp.surfaceProperties(loft.shape(), thePropertyHolder);
		System.out.println("Loft surface area = " + thePropertyHolder.mass());

		// TODO: check this
		// means that each time a loft is created the total wetted area is updated 
		BRepGProp.surfaceProperties(loft.shape(), thePropertyHolder);
		_wettedArea = _wettedArea.plus(
				Amount.valueOf(
						thePropertyHolder.mass(), 
						SI.SQUARE_METRE)
				);

		return loft;
	}

	public boolean is_makeSolid() {
		return _makeSolid;
	}

	public void set_makeSolid(boolean val) {
		this._makeSolid = val;
	}

	public boolean is_closedSplines() {
		return _closedSpline;
	}

	public void set_closedSplines(boolean val) {
		this._closedSpline = val;
	}

	public boolean is_ruledLoft() {
		return _makeRuled;
	}

	public void set_makeRuledLoft(boolean val) {
		this._makeRuled = val;
	}



	public Amount<Area> get_wettedArea() {
		return _wettedArea;
	}

	public int get_nPointsNoseCap() {
		return _nPointsNoseCap;
	}

	public void set_nPointsNoseCap(int _nPointsNoseCap) {
		this._nPointsNoseCap = _nPointsNoseCap;
	}

	public int get_nPointsNosePart() {
		return _nPointsNosePart;
	}

	public void set_nPointsNosePart(int _nPointsNosePart) {
		this._nPointsNosePart = _nPointsNosePart;
	}

	public int get_nPointsCylinderPart() {
		return _nPointsCylindricalPart;
	}

	public void set_nPointsCylinderPart(int _nPointsCylinderPart) {
		this._nPointsCylindricalPart = _nPointsCylinderPart;
	}

	public int get_nPointsTailPart() {
		return _nPointsTailPart;
	}

	public void set_nPointsTailPart(int _nPointsTailPart) {
		this._nPointsTailPart = _nPointsTailPart;
	}

	public int get_nPointsTailCap() {
		return _nPointsTailCap;
	}

	public void set_nPointsTailCap(int _nPointsTailCap) {
		this._nPointsTailCap = _nPointsTailCap;
	}

	public BRepOffsetAPI_ThruSections get_loft(){return _loft;}

	public TopoDS_Compound getTheCompound() {
		return theCompound;
	}


}// end-of class
