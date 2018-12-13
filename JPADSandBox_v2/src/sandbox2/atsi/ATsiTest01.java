package sandbox2.atsi;

import sandbox2.mds.Test1;

public class ATsiTest01 {
	
	private int n1 = 0;
	private float a1 = -10.0f;
	public float a2 = 0.0f;
	
	private sandbox2.mds.Test1 test1 = new Test1(0.0, 0.0, "Pippo!");
	
	public sandbox2.mds.Test1 getTest1() {
		return test1;
	}

	public void setTest1(sandbox2.mds.Test1 test1) {
		this.test1 = test1;
	}

	public int getN1() {
		return this.n1;
	}

	public void setN1(int n1) {
		this.n1 = n1;
	}

	public float getA1() {
		return a1;
	}

	public void setA1(float a1) {
		this.a1 = a1;
	}

	public void setAll(float c1, float c2, int i) {
		this.a1 = c1;
		this.a2 = c2;
		this.n1 = i;
	}
	
	public float getSum() {
		return this.a1 + this.a2 + this.n1;
	}
	
//	public static void main(String[] args) {
//		
//		System.out.println("Hello ATsiTest01 !!!");
//		
//		ATsiTest01 obj = new ATsiTest01();
//		
//		System.out.println("n1 = " + obj.n1);
//
//	}

}
