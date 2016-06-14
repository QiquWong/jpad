package CGAL_Test;

import CGAL.Kernel.Point_2;
import CGAL.Triangulation_2.Triangulation_2;
import CGAL.Triangulation_2.Triangulation_2_Vertex_circulator;
import CGAL.Triangulation_2.Triangulation_2_Vertex_handle;
import java.util.Vector;

public class CGAL_Triangulation_Test1 {

	public static void main(String arg[]){


//        System.out.println("-----------------------------------------------");
//		String libPath = System.getProperty("java.library.path");
//        System.out.println("java.library.path=" + libPath);
//        String libraryName = "CGAL_Kernel";
//        System.out.println("Trying to load '" + libraryName + "'");
//        System.loadLibrary(libraryName);
//        System.out.println("-----------------------------------------------");
        
        
		Vector<Point_2> points=new Vector<Point_2>(8);
		points.add( new Point_2(1,0) );
		points.add( new Point_2(3,2) );
		points.add( new Point_2(4,5) );
		points.add( new Point_2(9,8) );
		points.add( new Point_2(7,4) );
		points.add( new Point_2(5,2) );
		points.add( new Point_2(6,3) );
		points.add( new Point_2(10,1) );

		Triangulation_2 t=new Triangulation_2();
		t.insert(points.iterator());

		Triangulation_2_Vertex_circulator vc = t.incident_vertices(t.infinite_vertex());

		if (vc.hasNext()) {
			Triangulation_2_Vertex_handle done = vc.next().clone();
			Triangulation_2_Vertex_handle iter=null;
			do {
				iter=vc.next();
				System.out.println(iter.point());
			}while(!iter.equals(done));
		}    
	}
}