package sandbox2.atsi;

public class Cerchio {
	
	private int x=0;
	private int y=0;
	private int r=0;
	
	public Cerchio(int x1, int y1, int r1)
	{
		x=x1;
		y=y1;
		r=r1;
	}
	
	public double Perimetro()
	{
		return 2*Math.PI*r;
	}
	
	public double Area()
	{
		return Math.PI*r*r;
	}
	
	public String Testo()
	{
		return "Il cerchio ha origine ("+x+","+y+") e r="+r;
		
	}
	
}
