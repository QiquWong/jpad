import opencascade.TColStd_ListOfInteger;
import opencascade.TColStd_ListOfInteger_Iterator;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BSplCLib;
import opencascade.Geom_CartesianPoint;
import opencascade.Geom_Circle;
import opencascade.Geom_Conic;
import opencascade.Geom_Curve;
import opencascade.Message;
import opencascade.Message_Gravity;
import opencascade.Message_Messenger;
import opencascade.Message_Printer;
import opencascade.OSD_MemInfo;
import opencascade.OSD_MemInfo.Counter;
import opencascade.OSD_Protection;
import opencascade.OSD_SingleProtection;
import opencascade.Precision;
import opencascade.Standard_Transient;
import opencascade.Standard_Version;
import opencascade.TColStd_Array1OfInteger;
import opencascade.TColStd_Array1OfReal;
import opencascade.TCollection_AsciiString;
import opencascade.TCollection_ExtendedString;
import opencascade.TColStd_Array1OfTransient;
import opencascade.TColStd_HArray1OfReal;
import opencascade.TColStd_ListOfInteger;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TopExp;
import opencascade.TopoDS_Edge;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_XYZ;
import opencascade.GeomAdaptor_Curve;
import opencascade.GCPnts_AbscissaPoint;

public class Test {

	private static int myNbErr = 0;

	private static boolean CheckError(boolean error, String msg) {
		if (error) {
			myNbErr++;
			System.out.println("*** TEST FAILED: " + msg);
		} else
			System.out.println("*** TEST OK: " + msg);
		return error;
	}

	/**
	 * Test memory deallocation by garbage collector.
	 */
	private static void TestGC() {
		System.out.println("Final test of garbage collector: if it fails, the system may go down!");
		OSD_MemInfo MemInfo = new OSD_MemInfo();
		long VMSize0 = MemInfo.Value(Counter.MemVirtual) / (1024 * 1024);
		System.out.println("Virtual memory occupied at start: " + VMSize0
				+ " Mb");

		// Run multiple allocation - deallocation cycles, each time calling
		// GC.Collect()
		// explicitly; the number of calls is selected so that in case of memory
		// leak
		// it does due to overallocation (~100 Gb of VRAM)
		for (int k = 1; k < 100; k++) {
			// cycle allocating ~1 Gb (not more, otherwise it will die
			// prematurely on 32-bit system)
			for (int i = 1; i < 1000; i++) {
				TColStd_HArray1OfReal anArr = new TColStd_HArray1OfReal(1,
						100000); // ~1 Mb
						anArr.SetValue(1, 10);
			}
			System.gc();
		}
		System.gc();

		MemInfo.Update();
		long VMSize2 = MemInfo.Value(Counter.MemVirtual) / (1024 * 1024);
		System.out.println("Virtual memory occupied after playing with ~100 Gb of memory: "
				+ VMSize2 + " Mb");
		// we expect that at least 300 Mb of memory shall be occupied and then
		// released by GC
		CheckError(VMSize2 - VMSize0 > 10000, // we are Ok in any case if
					// arrived here
				"TestGC: memory deallocation by garbage collector.");
	}

	/**
	 * Test OCCT memory manager for concurrent allocation / deallocation (by
	 * garbage collector)
	 */
	private static void TestMMGR() {
		boolean ok = true;
		try {
			for (int i = 1; i < 100000; i++) {
				TColStd_HArray1OfReal anArr = new TColStd_HArray1OfReal(1, 100);
				anArr.SetValue(1, 10);
			}
		} catch (Exception e) {
			System.out.println("TestMMGR: Exception " + e.getMessage());
			ok = false;
		}
		System.gc();
		CheckError(!ok,
				"TestMMGR: OCCT memory manager in concurrent environment");
	}

	/**
	 * Test access to OCC objects by reference
	 */
	private static void TestReference() {
		gp_Pnt p_ref;
		{
			TColgp_Array1OfPnt anArr = new TColgp_Array1OfPnt(1, 1);
			p_ref = anArr.ChangeValue(1);
			p_ref.SetCoord(1, 2, 3);
			gp_Pnt p_check = anArr.Value(1);
			CheckError(
					p_check.X() != 1 || p_check.Y() != 2 || p_check.Z() != 3,
					"TestReference: accessing gp_Pnt in array by reference");

			double[] x = { 0 }, y = { 0 }, z = { 0 };
			p_check.Coord(x, y, z);
			CheckError(x[0] != 1 || y[0] != 2 || z[0] != 3,
					"TestReference: coordinates returned by reference");
		}
		System.gc();
		CheckError(p_ref == null
				|| p_ref.Distance(new gp_Pnt(1, 2, 3)) > Precision.Confusion(),
				"TestReference: get reference to element of array (gp_Pnt)");

		// check case when reference to handle is returned in C++
		Standard_Transient anItem;
		{
			TColStd_Array1OfTransient anArrT = new TColStd_Array1OfTransient(1,
					1);
			Standard_Transient aPnt = new Geom_CartesianPoint(p_ref);
			anArrT.SetValue(1, aPnt);
			anItem = anArrT.ChangeValue(1);
		}
		System.gc();
		CheckError(
				anItem.IsNull()
				|| anItem.IsKind(Geom_CartesianPoint.TypeOf()) == 0
				|| Geom_CartesianPoint.DownCast(anItem).Pnt()
				.Distance(p_ref) > Precision.Confusion(),
				"TestReference: get element of array (Handle)");

	}

	/**
	 * Test downcasting
	 */
	private static void TestDownCast() {
		gp_Pnt p = new gp_Pnt(1, 2, 3);
		gp_Ax2 ax = new gp_Ax2(p, new gp_Dir(1, 0, 0));
		Geom_Curve c = new Geom_Circle(ax, 10);
		CheckError(!(c instanceof Geom_Conic),
				"TestDownCast: using 'instanceof' on derived type in Java");

		TColStd_Array1OfTransient anArr = new TColStd_Array1OfTransient(1, 1);
		anArr.SetValue(1, c);
		if (anArr.Value(1) instanceof Geom_Conic)
			System.out
			.println("ERROR: Circle in array IS a Curve! Very strange!");
		else
			System.out
			.println("OK: Circle in array of transients IS NOT a Curve in Java");

		CheckError(anArr.Value(1).IsKind(Geom_Circle.TypeOf()) == 0,
				"TestDownCast: using IsKind() on C++ derived type");

	}

	/**
	 * Test passing enumeration as output argument to a function
	 */
	private static void TestEnumOut() {
		OSD_SingleProtection User = OSD_SingleProtection.OSD_None;
		OSD_SingleProtection Sys = OSD_SingleProtection.OSD_None;
		OSD_SingleProtection Group = OSD_SingleProtection.OSD_None;
		OSD_SingleProtection World = OSD_SingleProtection.OSD_None;
		int[] aUser = { User.swigValue() };
		int[] aSys = { Sys.swigValue() };
		int[] aGroup = { Group.swigValue() };
		int[] aWorld = { World.swigValue() };

		OSD_Protection aP = new OSD_Protection();
		aP.Values(aUser, aSys, aGroup, aWorld);

		String os = System.getProperty("os.name");
		if (os.startsWith("Wind"))
			CheckError(
					OSD_SingleProtection.swigToEnum(aUser[0])!= OSD_SingleProtection.OSD_RWXD
					|| OSD_SingleProtection.swigToEnum(aSys[0]) != OSD_SingleProtection.OSD_RWXD
					|| OSD_SingleProtection.swigToEnum(aGroup[0]) != OSD_SingleProtection.OSD_RX
					|| OSD_SingleProtection.swigToEnum(aWorld[0]) != OSD_SingleProtection.OSD_RX,
					"TestEnumOut: passing enumeration as output argument");
		else        
			CheckError(
					OSD_SingleProtection.swigToEnum(aUser[0])!= OSD_SingleProtection.OSD_R
					|| OSD_SingleProtection.swigToEnum(aSys[0]) != OSD_SingleProtection.OSD_RWD
					|| OSD_SingleProtection.swigToEnum(aGroup[0]) != OSD_SingleProtection.OSD_R
					|| OSD_SingleProtection.swigToEnum(aWorld[0]) != OSD_SingleProtection.OSD_R,
					"TestEnumOut: passing enumeration as output argument");

	}

	/**
	 * Test returning non-const reference to integer or enum (incompatibility:
	 * reference mapped as by value)
	 */
	private static void TestIntRef() {
		TColStd_ListOfInteger aList = new TColStd_ListOfInteger();
		aList.Append(1);
		aList.Append(2);
		CheckError(aList.First() != 1 || aList.Last() != 2,
				"TestIntRef: returning integer by reference - mapped to value");
	}

	/**
	 * Test null handles
	 */
	private static void TestNullHandle() {
		TColStd_Array1OfTransient anArr = new TColStd_Array1OfTransient(1, 2);
		Integer i = new Integer(1);
		gp_Pnt p = new gp_Pnt(1, 2, 3);
		gp_Ax2 ax = new gp_Ax2(p, new gp_Dir(1, 0, 0));
		Geom_Curve c = new Geom_Circle(ax, 10);
		anArr.SetValue(1, c);

		Standard_Transient c1 = anArr.ChangeValue(1);
		Standard_Transient c1x = anArr.ChangeValue(1);
		Standard_Transient c2 = anArr.ChangeValue(2);
		CheckError(c1 == null || c1.IsNull(),
				"TestNullHandle: non-null value of array item");

		if (c2 != null)
			System.out
			.println("OK: Second element might be expected to be null, but Java wrapper is not a null object");
		else
			System.out.println("Wow: second element is Null in Java!? Magic!");
		CheckError(!c2.IsNull(), "TestNullHandle: null value of array item");
	}

	/**
	 * Test arithmetic operators
	 */
	private static void TestXYZOperators() {
		gp_XYZ x = new gp_XYZ(1, 0, 0);
		gp_XYZ y = new gp_XYZ(0, 1, 0);
		gp_XYZ z = x.Crossed(y);
		CheckError(z.X() != 0 || z.Y() != 0 || z.Z() != 1,
				"TestXYZOperators: cross-product of gp_XYZ vectors");
	}

	/**
	 * Test OCC version number
	 */
	private static void TestVersion() {
		Double i = new Double(Standard_Version.Number());
		CheckError(
				Standard_Version.Number() < 6.1
				|| i.toString().equalsIgnoreCase(
						Standard_Version.String()) == false,
						"TestVersion: OCC version number (" + Standard_Version.Number()
						+ ")");

	}

	/**
	 * Test creation of circle
	 */
	private static void TestCreateCircle() {
		double radius = 10;
		gp_Pnt p1 = new gp_Pnt(0, 0, 0);
		gp_Dir cNorm = new gp_Dir(0, 0, 1);
		gp_Ax2 coordSystem = new gp_Ax2(p1, cNorm);

		Geom_Circle circle = new Geom_Circle(coordSystem, radius);
		TopoDS_Edge edge = (new BRepBuilderAPI_MakeEdge(circle)).Edge();

		System.gc();

		CheckError(
				edge.IsNull() == 1 || TopExp.FirstVertex(edge).IsNull() == 1,
				"TestCreateCircle: creation of circular edge");
	}

	/**
	 * Test work with instantiations of template NCollection classes
	 */
	private static void TestNestedIterator() {
		// create list of integers
		TColStd_ListOfInteger aList = new TColStd_ListOfInteger();
		aList.Append(1);
		aList.Append(2);
		aList.Append(3);
		CheckError(aList.Size() != 3,
				"TestNestedIterator: initialization of TColStd_ListOfInteger");

		// use iterator to remove element in the middle
		TColStd_ListOfInteger_Iterator anIt = 
				new TColStd_ListOfInteger_Iterator(aList);
		for (; anIt.More() == 1; anIt.Next()) {
			if (anIt.Value() == 2)
				aList.Remove(anIt);
		}
		CheckError(
				aList.First() != 1 || aList.Last() != 3 || aList.Size() != 2,
				"TestNestedIterator: removal of element from the list using iterator");
	}

	/**
	 * Class inheriting Message_Printer and redefining its method
	 */
	public static class Printer extends Message_Printer
	{
		public Printer ()
		{
			myMessage = "";
		}

		public void Send(TCollection_ExtendedString  theString, Message_Gravity theGravity, long theToPutEol)
		{
			myMessage += theString;
		}
		public void Send(String theString, Message_Gravity theGravity, long theToPutEol)
		{
			myMessage += theString;
		}
		public void Send(TCollection_AsciiString theString, Message_Gravity theGravity, long theToPutEol)
		{
			myMessage += theString;
		}

		public String myMessage;
	};

	/**
	 * Test subclassing of C++ class with SWIG directors
	 */
	private static void TestPrinter() {
		Printer aPrinter = new Printer();
		Message_Messenger aMess = Message.DefaultMessenger();
		aMess.AddPrinter(aPrinter);

		String aTestString = "test string";
		aMess.Send(aTestString);
		aMess.RemovePrinter (aPrinter);

		CheckError(! aPrinter.myMessage.equals (aTestString),
				"TestPrinter: message capturing");
	}
	
	/*
	 * Testing Spline Curve lib
	 */
	private static void TestBSplCLib() {
		System.out.println("-----------------------------------------------");
		System.out.println("TestBSplCLib");

		TColgp_HArray1OfPnt points1 = new TColgp_HArray1OfPnt(1, 4);
		points1.SetValue(1, new gp_Pnt( 0,  0, 0));
		points1.SetValue(2, new gp_Pnt( 0, 10, 5));
		points1.SetValue(3, new gp_Pnt( 0, 15,-5));
		points1.SetValue(4, new gp_Pnt( 0, 20, 0));
		long isPeriodic = 0;
		
		TColStd_Array1OfInteger mults = new TColStd_Array1OfInteger(1,4);
		mults.SetValue(1, 1);
		mults.SetValue(2, 2);
		mults.SetValue(3, 1);
		mults.SetValue(4, 1);
		int m_degree = 3;
		int nFlatKnots = BSplCLib.KnotSequenceLength(mults, m_degree, 0);
		TColStd_Array1OfReal flatKnots = new TColStd_Array1OfReal(1, nFlatKnots);
		//knotSequence
		TColStd_Array1OfReal knots = new TColStd_Array1OfReal(1, nFlatKnots);
		BSplCLib.KnotSequence(knots, mults, flatKnots);
		System.out.println("-----------------------------------------------");
		for(int i = 1; i <= mults.Length(); i++) {
			System.out.println("Mult: " + mults.Value(i));
		}
		for(int i = 1; i <= knots.Length(); i++) {
			System.out.println("Knot: " + knots.Value(i));
		}
		System.out.println("-----------------------------------------------");		
	}
	
	/*
	 * Testing Spline Curve lib
	 */
	private static void TestGCPnts() {
		System.out.println("-----------------------------------------------");
		System.out.println("TestGCPnts");

		// create a circle
		double radius = 10;
		gp_Pnt p1 = new gp_Pnt(0, 0, 0);
		gp_Dir cNorm = new gp_Dir(0, 0, 1);
		gp_Ax2 coordSystem = new gp_Ax2(p1, cNorm);

		Geom_Circle circle = new Geom_Circle(coordSystem, radius);
		double umin = circle.FirstParameter();
		double umax = circle.LastParameter();
		GeomAdaptor_Curve adaptorCurve = new GeomAdaptor_Curve(circle, umin, umax);
		double len = GCPnts_AbscissaPoint.Length(adaptorCurve, umin, umax);
		System.out.println("Circle length: " + len);
		System.out.println("-----------------------------------------------");
	}
	
	public static void main(String[] args) {
		// run tests in sequence
		TestIntRef();
		TestEnumOut();
		TestVersion();
		TestDownCast();
		TestReference();
		TestNullHandle();
		TestXYZOperators();
		TestCreateCircle();
		TestNestedIterator();
		TestMMGR();
		TestPrinter();
		TestBSplCLib();
		TestGCPnts();

		// this test should be last one -- if it fails, the system will go down
		TestGC();

		if (myNbErr > 0)
			System.out.println("*** Total number of tests FAILED: " + myNbErr);
		else
			System.out.println("*** ALL TESTS COMPLETED");
		
	}

}
