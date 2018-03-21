package cad.aircraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.unit.SI;

import org.apache.commons.lang3.ArrayUtils;
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
import org.jcae.opencascade.jni.TopAbs_ShapeEnum;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Shape;
import org.jcae.opencascade.jni.TopoDS_Vertex;
import org.jcae.opencascade.jni.TopoDS_Wire;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import jmatrix.Matrix;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;

/**
 * Build the CAD model of the fuselage once given a Fuselage
 * object.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class MyFuselageBuilder {

	Fuselage theFuselage = null;

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

	private BRepOffsetAPI_ThruSections _loft;
	public BRepOffsetAPI_ThruSections get_loft(){return _loft;}

	// operation stuff
	private boolean _makeSolid = false;	 // try to build a solid
	private boolean _closedSpline = false; // treat sections as closed curves
	private boolean _makeRuled = true;	 // try to build a solid

	// discretization parametrs
	private int 
	_nPointsNoseCap      = 5, 
	_nPointsNosePart     = 25, 
	_nPointsCylindricalPart = 4, 
	_nPointsTailPart     = 15, 
	_nPointsTailCap      = 4;

	// Measurements
	private Amount<Area> _wettedArea = Amount.valueOf(0.0, SI.SQUARE_METRE); 

	public MyFuselageBuilder(Aircraft aircraft) {
		initialize(aircraft.getFuselage());
	}

	public MyFuselageBuilder(Fuselage fuselage) {
		initialize(fuselage);
	}

	private void initialize(Fuselage fuselage) {

		theFuselage = fuselage;
		theFuselage.getFuselageCreator().calculateOutlines(
				20, // num. points Nose
				4,  // num. points Cylinder
				10, // num. points Tail
				10, // num. points Upper section
				10  // num. points Lower section
				);

		// prepare a compound/builder pair
		theBuilder = new BRep_Builder();
		theCompound = new TopoDS_Compound();
		theBuilder.makeCompound(theCompound);
		
		// initialize the object that stores measurements
		thePropertyHolder = new GProp_GProps();
		
		// If Eps is argument is absent, precision is quite poor

		if (theFuselage == null) {
			System.out.println("Null fuselage in MyAircraft object!");
		}
	}

	public void buildAndWriteCAD(
			boolean solid, boolean ruled, 
			boolean closedSplines, 
			String path, String fileName) {

		set_makeSolid(solid);
		set_makeRuledLoft(ruled);
		set_closedSplines(closedSplines);
		buildCAD();

		MyAircraftBuilder.write(new File(path), fileName + ".brep", theCompound);
		MyAircraftBuilder.write(new File(path), fileName + ".step", theCompound);
	}

	public TopoDS_Compound buildCAD() {

		System.out.println(
				"Discretization parameters: ["
						+ _nPointsNoseCap + ","
						+ _nPointsNosePart + ","
						+ _nPointsCylindricalPart + "," 
						+ _nPointsTailPart  + ","
						+ _nPointsTailCap + "]"
				);

		buildNoseCap();
		buildNosePart();
		buildCylindricalPart();
		buildTailPart();
		buildTailCap();

		//		doSewParts();

		theCompound = buildFuselageByParts();
//		theCompound = buildFuselageAsUniqueLoft();
		
		return theCompound;

	}

	public void setXwiseDiscretization(Integer ... integers)
	{
		if (integers.length == 0)
		{
			return; // do nothing
		}
		if (integers.length == 1)
		{
			_nPointsNoseCap = (int) integers[0]; 
			return;
		}
		if (integers.length == 2)
		{
			_nPointsNoseCap  = (int) integers[0]; 
			_nPointsNosePart = (int) integers[1]; 
			return;
		}
		if (integers.length == 3)
		{
			_nPointsNoseCap  = (int) integers[0]; 
			_nPointsNosePart = (int) integers[1]; 
			_nPointsCylindricalPart = (int) integers[2]; 
			return;
		}
		if (integers.length == 4)
		{
			_nPointsNoseCap      = (int) integers[0]; 
			_nPointsNosePart     = (int) integers[1]; 
			_nPointsCylindricalPart = (int) integers[2]; 
			_nPointsTailPart     = (int) integers[3]; 
			return;
		}
		if (integers.length >= 5)
		{
			_nPointsNoseCap      = (int) integers[0]; 
			_nPointsNosePart     = (int) integers[1]; 
			_nPointsCylindricalPart = (int) integers[2]; 
			_nPointsTailPart     = (int) integers[3]; 
			_nPointsTailCap      = (int) integers[4]; 
			return;
		}
	}

	public TopoDS_Compound buildFuselageByParts() {

		BRepBuilderAPI_Sewing sewer = new BRepBuilderAPI_Sewing();
		sewer.add(_loft_NoseCap.shape());
		sewer.add(_loft_NosePart.shape());
		sewer.add(_loft_CilyndricalPart.shape());
		sewer.add(_loft_TailPart.shape());
		sewer.add(_loft_TailCap.shape());
		sewer.perform();

		System.out.println(
				"............... N. solids in sewed: " +
						org.jcae.opencascade.Utilities.numberOfShape(sewer.sewedShape(), TopAbs_ShapeEnum.SOLID)
				);

		BRep_Builder builder0 = new BRep_Builder();
		TopoDS_Compound compound0 = new TopoDS_Compound();
		builder0.makeCompound(compound0);
		builder0.add(compound0, sewer.sewedShape());
		return compound0;
	}

	//		this.write(
	//				new File(MyWriteUtils._CurrentDirectory.getAbsolutePath()+"/test"), 
	//				"fused.igs"
	//				);

	//		if (1 == 0)
	//		{
	//			BRepAlgoAPI_Fuse fuse0 = new BRepAlgoAPI_Fuse(
	//					_loft_NosePart.shape(), _loft_CilyndricalPart.shape()
	//					);
	//
	//			BRepAlgoAPI_Fuse fuse = new BRepAlgoAPI_Fuse(
	//					_loft_NoseCap.shape(), fuse0.shape()
	//					);
	//			
	//			TopExp_Explorer exp = new TopExp_Explorer(fuse.shape(), TopAbs_ShapeEnum.SHELL);
	//			TopoDS_Shell shell = (TopoDS_Shell) exp.current();
	//			ShapeFix_Shell fixShell = new ShapeFix_Shell(shell);
	//			try {
	//				fixShell.perform();
	//				System.out.println("............... n: " + fixShell.nbShells());
	//				if (fixShell.nbShells() == 1)
	//				{
	//					TopoDS_Shell aShell = fixShell.shell();
	//					TopoDS_Solid solid = (TopoDS_Solid) new BRepBuilderAPI_MakeSolid(aShell).shape();
	//					
	//					TopoDS_Shell bShell = org.jcae.opencascade.jni.BRepClass3d.outerShell(solid);
	//					
	//					BRep_Builder builder = new BRep_Builder();
	//					TopoDS_Compound compound = new TopoDS_Compound();
	//					builder.makeCompound(compound);
	////					builder.add(compound, fuse.shape());
	//					builder.add(compound, solid);
	////					builder.add(compound, bShell);
	//					this.write(
	//							new File(MyWriteUtils._CurrentDirectory.getAbsolutePath()+"/test"), 
	//							"fused.brep"
	//							);
	//					this.write(
	//							new File(MyWriteUtils._CurrentDirectory.getAbsolutePath()+"/test"), 
	//							"fused.igs"
	//							);
	//					this.write(
	//							new File(MyWriteUtils._CurrentDirectory.getAbsolutePath()+"/test"), 
	//							"fused.step"
	//							);
	//				}
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//			
	//		}// 0 == 1 ?


	/**
	 * Generate a loft by skinning through a point (optional), 
	 * then a list of ALL curves from nose to tail, and a point (optional)
	 * 
	 * @author Agostino
	 * @param point1 an initial point, PVector
	 * @param sections a list of curves (lists of points, PVector)
	 * @param point2 a final point, PVector
	 * @return 
	 * @return
	 */
	private TopoDS_Compound buildFuselageAsUniqueLoft() {

		System.out.println("-----------> Lofting the whole fuselage");

		// The nose point 
		PVector p0 = new PVector(
				(float) 0.0,
				(float) 0.0,
				(float) theFuselage.getFuselageCreator().getNoseTipOffset().doubleValue(SI.METER)
				);

		Double x_C = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_NOSE_CAP)
				.doubleValue(SI.METER);
		Double x_B = 0.5*x_C;
		Double x_A = 0.25*x_C;

		// The final list of X-sections
		List<Double> xStations = new ArrayList<Double>();

		//		xStations.add(x_A);
		//		xStations.add(x_B);
		//		xStations.add(x_C);

		// The Nose-Cap		
		List<Double> xStations00 = Arrays.asList(ArrayUtils.toObject(
				Matrix.linspace(
						0.0, x_C.doubleValue(), 
						_nPointsNoseCap
						).data
				));

		xStations.addAll(
				xStations00.subList(1, xStations00.size())
				);

		// The Nose part
		// x-coord of section 1
		Double x_D = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_CYLINDER_1)
				.doubleValue(SI.METER);

		List<Double> xStations0 = Arrays.asList(ArrayUtils.toObject(
				//				Matrix.linspace(
				//						x_C.doubleValue(), x_D.doubleValue(), 
				//						_nPointsNosePart
				//						).data
				MyArrayUtils.cosineSpace(
						x_C.doubleValue(), x_D.doubleValue(), 
						_nPointsTailPart
						)
				));

		xStations.addAll(
				xStations0.subList(1, xStations0.size())
				);

		// The Cylindrical part
		Double x_E = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_CYLINDER_2)
				.doubleValue(SI.METER);
		List<Double> xStations1 = Arrays.asList(ArrayUtils.toObject(
				//				Matrix.linspace(
				//						x_D.doubleValue(), x_E.doubleValue(), 
				//						_nPointsCylindricalPart
				//						).data
				MyArrayUtils.cosineSpace(
						x_D.doubleValue(), x_E.doubleValue(), 
						_nPointsCylindricalPart
						)
				));
		xStations.addAll(
				xStations1.subList(1, xStations1.size())
				);

		//		System.out.println(
		//				Arrays.asList(ArrayUtils.toObject(
		//						MyUtilities.cosineSpace(
		//								x_D.doubleValue(), x_E.doubleValue(), 
		//								_nPointsCylindricalPart
		//								)
		//						))
		//				);

		// The Tail part
		Double x_F = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_TAIL_CAP)
				.doubleValue(SI.METER);

		List<Double> xStations2 = Arrays.asList(ArrayUtils.toObject(
				//				Matrix.linspace(
				//						x_E.doubleValue(), x_F.doubleValue(), 
				//						_nPointsTailPart
				//						).data
				MyArrayUtils.halfCosine1Space(
						x_E.doubleValue(), x_F.doubleValue(), 
						_nPointsTailPart
						)
				));
		xStations.addAll(
				xStations2.subList(1, xStations2.size())
				);

		// TODO: add Tail Cap ...

		System.out.println(xStations.size());
		System.out.println(xStations);


		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		sections = prepareSections(xStations);

		try {
			_loft = makeLoft(
					p0,			// a point, PVector 
					sections,	// a list of curves (lists of points, PVector)
					null		// a point, PVector
					);
			
			if (_loft.isDone()) {
				theBuilder.add(theCompound, _loft.shape());				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return theCompound;

	}// end-of doBuildFuselage()

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


	private void buildCylindricalPart() 
	{
		System.out.println("-----------> Lofting fuselage cylindrical part");

		// x-coord of section 1
		Double x_B = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_CYLINDER_1)
				.doubleValue(SI.METER);
		Double x_C = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_CYLINDER_2)
				.doubleValue(SI.METER);

		double[] x = Matrix.linspace(
				x_B.doubleValue(), x_C.doubleValue(), 
				_nPointsCylindricalPart
				).data;
		// convert to a proper data structure
		List<Double> xStations = 
				Arrays.asList(ArrayUtils.toObject(x));
		System.out.println(xStations);

		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		sections = prepareSections(xStations);

		_loft_CilyndricalPart = makeLoft(
				null,		// a point, PVector 
				sections,	// a list of curves (lists of points, PVector)
				null		// a point, PVector
				);

		theBuilder.add(theCompound, _loft_CilyndricalPart.shape());

		//		// TODO: check this
		//		BRepGProp.surfaceProperties(loft.shape(), thePropertyHolder);
		//		_wettedArea = _wettedArea.plus(
		//			Amount.valueOf(
		//				thePropertyHolder.mass(), 
		//				SI.SQUARE_METRE)
		//			);

	}// end-of doBuildCylindricalPart

	private void buildTailCap() 
	{
		System.out.println("-----------> Lofting fuselage tail cap part");

		Double x_C = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_TAIL_TIP)
				.doubleValue(SI.METER);
		Double x_A = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_TAIL_CAP)
				.doubleValue(SI.METER);
		Double x_B = x_A + 0.25*(x_C - x_A);
		//		x_C = x_A + 0.90*(x_C - x_A);

		// The final list of X-sections
		List<Double> xStations = new ArrayList<Double>();

		//		xStations.add(x_A);
		//		xStations.add(x_B);
		//		xStations.add(x_C);

		List<Double> xStations0 = Arrays.asList(ArrayUtils.toObject(
				Matrix.linspace(
						x_C.doubleValue(), x_A.doubleValue(), 
						_nPointsTailCap
						).data
				));

		// take out the first x-coordinate
		xStations.addAll(
				xStations0.subList(1, xStations0.size())
				);

		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		sections = prepareSections(xStations);

		System.out.println(xStations);

		PVector p0 = new PVector(
				(float) theFuselage.getFuselageCreator().getFuselageLength().doubleValue(SI.METER),
				(float) 0.0,
				(float) theFuselage.getFuselageCreator().getTailTipOffset().doubleValue(SI.METER)
				);
		System.out.println("P at Tail: " + p0);

		_loft_TailCap 
		= makeLoft(
				p0,		// a point, PVector 
				sections,	// a list of curves (lists of points, PVector)
				null		    // a point, PVector
				);

		theBuilder.add(theCompound, _loft_TailCap.shape());
	}

	private void buildNoseCap() 
	{
		System.out.println("-----------> Lofting fuselage nose cap part");

		//		Double x_C = 
		//			theFuselage
		//				.get_sectionsYZStations()
		//				.get(theFuselage.IDX_SECTION_YZ_NOSE_CAP)
		//				.doubleValue(SI.METER);
		//		Double x_B = 0.5*x_C;
		//		Double x_A = 0.25*x_C;
		//
		//		List<Double> xStations = new ArrayList<Double>();
		//		xStations.add(x_A);
		//		xStations.add(x_B);
		//		xStations.add(x_C);
		////		System.out.println(xStations);
		//		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		//		sections = prepareSections(xStations);

		// The nose point 
		PVector p0 = new PVector(
				(float) 0.0,
				(float) 0.0,
				(float) theFuselage.getFuselageCreator().getNoseTipOffset().doubleValue(SI.METER)
				);

		Double x_C = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_NOSE_CAP)
				.doubleValue(SI.METER);
		Double x_B = 0.5*x_C;
		Double x_A = 0.25*x_C;

		// The final list of X-sections
		List<Double> xStations = new ArrayList<Double>();

		// The Nose-Cap		
		List<Double> xStations0 = Arrays.asList(ArrayUtils.toObject(
				Matrix.linspace(
						0.0, x_C.doubleValue(), 
						_nPointsNoseCap
						).data
				));

		// take out the first x-coordinate
		xStations.addAll(
				xStations0.subList(1, xStations0.size())
				);

		System.out.println(xStations);

		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		sections = prepareSections(xStations);

		_loft_NoseCap = makeLoft(
				p0,		// a point, PVector 
				sections,	// a list of curves (lists of points, PVector)
				null		// a point, PVector
				);

		theBuilder.add(theCompound, _loft_NoseCap.shape());

	}

	private void buildTailPart() 
	{
		System.out.println("-----------> Lofting fuselage tail part");
		System.out.println(
				"Discretization parameters: ["
						+ _nPointsNoseCap + ","
						+ _nPointsNosePart + ","
						+ _nPointsCylindricalPart + "," 
						+ _nPointsTailPart  + ","
						+ _nPointsTailCap + "]"
				);

		// x-coord of first section
		Double x_A = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_CYLINDER_2)
				.doubleValue(SI.METER);
		Double x_B = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_TAIL_CAP)
				.doubleValue(SI.METER);

		double[] x = Matrix.linspace(
				x_A.doubleValue(), x_B.doubleValue(), 
				_nPointsTailPart
				).data;
		// convert to a proper data structure
		List<Double> xStations = 
				Arrays.asList(ArrayUtils.toObject(x));
		System.out.println(xStations);

		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		sections = prepareSections(xStations);

		_loft_TailPart = makeLoft(
				null,		// a point, PVector 
				sections,	// a list of curves (lists of points, PVector)
				null		// a point, PVector
				);

		theBuilder.add(theCompound, _loft_TailPart.shape());

	}

	private void buildNosePart() 
	{
		System.out.println("-----------> Lofting fuselage nose part");

		// x-coord of section 1
		Double x_A = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_NOSE_CAP)
				.doubleValue(SI.METER);
		Double x_B = 
				theFuselage
				.getFuselageCreator()
				.getSectionsYZStations()
				.get(theFuselage.getFuselageCreator().IDX_SECTION_YZ_CYLINDER_1)
				.doubleValue(SI.METER);

		double[] x = Matrix.linspace(
				x_A.doubleValue(), x_B.doubleValue(), 
				_nPointsNosePart
				).data;
		// convert to a proper data structure
		List<Double> xStations = 
				// new ArrayList<Double>();
				Arrays.asList(ArrayUtils.toObject(x));
		System.out.println(xStations);

		List<List<PVector>> sections = new ArrayList<List<PVector>>();
		sections = prepareSections(xStations);

		_loft_NosePart = makeLoft(
				null,		// a point, PVector 
				sections,	// a list of curves (lists of points, PVector)
				null		// a point, PVector
				);

		theBuilder.add(theCompound, _loft_NosePart.shape());

	}

	public List<List<PVector>> prepareSections(List<Double> xStations) {
		
		if (xStations.size() <= 1) return null;

		List<List<PVector>> curves = new ArrayList<List<PVector>>();

		for (Double x : xStations) {
			// Right part of the section (looking from top)
			List<PVector> c = 					
					theFuselage
					.getFuselageCreator()
					.getUniqueValuesYZSideRCurve(
							Amount.valueOf(x, SI.METER)						
							);
			// hack points in XZ plane
			c.get(0).y = (float) 0.0;
			c.get(c.size() - 1).y = (float) 0.0;

			if (_closedSpline) {
				// Right part of the section (looking from top)
				List<PVector> cL  = 					
						theFuselage
						.getFuselageCreator()
						.getUniqueValuesYZSideLCurve(
								Amount.valueOf(x, SI.METER)						
								);
				// take out from left curve first and last point
				c.addAll(cL.subList(1, cL.size() - 1));
				// TODO: check curve consistency, ie n. of points > 2
			}
			curves.add(c);
		}
		return curves;
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

	public TopoDS_Compound getTheCompound() {
		return theCompound;
	}



}// end-of class
