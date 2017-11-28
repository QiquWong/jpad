package sandbox2.mds;

import java.util.ArrayList;
import java.util.List;

public class Test1 {
	
	private double x;
	private double y;
	String id;
	
	public Test1(double x1, double y, String id) {
		x = x1;
		this.y = y;
		this.id = id;
	}

	@Override
	public String toString() { 
	    return "Id: '" + this.id + "', x = " + this.x + ", y = " + this.y;
	}

	/**
	 * Sums two internal variables x and y. 
	 *
	 * @return      x + y
	 */
	public double getSumXY() {
		return this.x + this.y;
	}
	
	public static void main(String[] args) {
		
		System.out.println("Hello world!");
		
		System.out.println("Number of arguments: " + args.length);
		for (int k = 0; k < args.length; k++) {
			System.out.println(k + ": " + args[k]);
		}
		
		Test1 test1 = new Test1(1.8, -0.5, "Hello!");
		Test1 test2 = new Test1(-1.8, -test1.y, test1.id.replaceAll("He", "KU"));

		List<Test1> myList = new ArrayList<>();
		myList.add(test1);
		myList.add(test2);
		myList.add(null);
		myList.add(new Test1(999.1, -2, "V1"));

		
//		myList.clear();
//		myList = null;

		for (int k = 0; k < myList.size(); k++) {
		System.out.println(k + " ==> " + myList.get(k));
	}
		
//		for (Test1 t : myList) {
//			System.out.println(" ==> " + t);
//		}

		
	}

}
