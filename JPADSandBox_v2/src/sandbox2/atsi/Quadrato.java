package sandbox2.atsi;

public class Quadrato extends Rettangolo {

	public Quadrato(int x1, int x2, int lato)
	{
		super(x1,x2,lato,lato);
	}
	
	public double Peri()
	{
		double Peri=(base*2)+(alt*2);
		return Peri;
	}
}
