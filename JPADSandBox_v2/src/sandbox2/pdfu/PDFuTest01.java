package sandbox2.pdfu;

public class PDFuTest01 {
	
	private int n1 = 0;
	private float a1 = -10.0f;
	public float a2 = 0.0f;
	
	public int getN1() {
		return n1;
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
	
	public static void main(String[] args) {

	}
	
}
