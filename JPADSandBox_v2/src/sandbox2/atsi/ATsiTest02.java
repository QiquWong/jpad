package sandbox2.atsi;

import sandbox2.mds.Test1;

public class ATsiTest02 {

	public static void main(String[] args) {
		
		ATsiTest01 obj = new ATsiTest01();
		
		System.out.println("Hello world from ATsiTest02 ...");
		
		// test.a1 = 2.0f;
		System.out.println("a1 = " + obj.getA1());
		obj.setA1(12.0f);
		System.out.println("a1 = " + obj.getA1());

		System.out.println("===============================================");
		
		System.out.println("a2 = " + obj.a2);
		obj.a2 = 33.0f;
		System.out.println("a2 = " + obj.a2);

		System.out.println("===============================================");

		System.out.println(
				"a1 = " + obj.getA1() 
				+ ", a2 = " + obj.a2
				+ ", n1 = " + obj.getN1()
			);
		System.out.println("a1 + a2 + n1 = " + obj.getSum());
		
		obj.setAll(-99.0f, 208, -11);
		
		System.out.println(
			"a1 = " + obj.getA1() 
			+ ", a2 = " + obj.a2
			+ ", n1 = " + obj.getN1()
		);
		System.out.println("a1 + a2 + n1 = " + obj.getSum());
		
		System.out.println("===============================================");

		obj.setTest1(new Test1(-21.5, 2.5, "In ATsiTest02!"));
		
		System.out.println("ATsiTest02.test1 ==> " + obj.getTest1());
		System.out.println("ATsiTest02.test1.getSum ==> " + obj.getTest1().getSumXY());
		
	}

}
