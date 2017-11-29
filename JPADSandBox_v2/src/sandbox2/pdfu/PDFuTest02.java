package sandbox2.pdfu;

import sandbox2.mds.Test1;

public class PDFuTest02 {

	public static void main(String[] args) {
		
		PDFuTest01 test = new PDFuTest01();
		
		System.out.println("Hello world from PDFuTest02 ...");
		
		// test.a1 = 2.0f;
		System.out.println("a1 = " + test.getA1());
		test.setA1(12.0f);
		System.out.println("a1 = " + test.getA1());

		System.out.println("===============================================");
		
		System.out.println("a2 = " + test.a2);
		test.a2 = 33.0f;
		System.out.println("a2 = " + test.a2);

		System.out.println("===============================================");

		System.out.println(
				"a1 = " + test.getA1() 
				+ ", a2 = " + test.a2
				+ ", n1 = " + test.getN1()
			);
		System.out.println("a1 + a2 + n1 = " + test.getSum());
		
		test.setAll(-99.0f, 208, -11);
		
		System.out.println(
			"a1 = " + test.getA1() 
			+ ", a2 = " + test.a2
			+ ", n1 = " + test.getN1()
		);
		System.out.println("a1 + a2 + n1 = " + test.getSum());
		
		System.out.println("===============================================");

		test.setTest1(new Test1(-21.5, 2.5, "In PDFuTest02!"));
		
		System.out.println("PDFuTest02.test1 ==> " + test.getTest1());
		System.out.println("PDFuTest02.test1.getSum ==> " + test.getTest1().getSumXY());
		
	}

}
