package sandbox2.pdfu;

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
		
	}

}
