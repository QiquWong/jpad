package sandbox2.adm;

import sandbox2.pdfu.PDFuTest01;

public class ADMTest01 {

	public static void main(String[] args) {
		
		PDFuTest01 test = new PDFuTest01();
		
		System.out.println("Hello world from PDFuTest02 ...");
		
		// test.a1 = 2.0f;
		System.out.println("a1 = " + test.getA1());
		test.setA1(12.0f);
		System.out.println("a1 = " + test.getA1());

	}

}
