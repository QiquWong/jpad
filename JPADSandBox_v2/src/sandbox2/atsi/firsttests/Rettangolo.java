package sandbox2.atsi.firsttests;

public class Rettangolo {
	
	private Punto p1 = null;
	protected int base=0; 
	protected int alt=0;
	
	

	//Primo costruttore
	
	public Rettangolo(int x1,int x2, int b, int h)
	{
		
		super();
		Punto p1= new Punto(x1,x2);
		base=b;
		alt=h;	
	}
	
	public Rettangolo()
	{
		this(0,1,2,1);
	}
	
	public double Area()
	{
		double area=base*alt;
		return area;
		
	}
	
	public double Peri()
	{
		double Peri=(base*2)+(alt*2);
		return Peri;
		
	}
	
	

}
