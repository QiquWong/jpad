package sandbox2.atsi;

public class Frazioni {
	int num=0; int den=1;
	
	//introduco il primo costruttore--> Tipo, nome classe (tipo e nome variabili)
	
	public Frazioni(int x_, int y_)
	{
		num=x_;
		
		if (y_!=0)
			den=y_;
		else
			den=1;
	}

	public Frazioni (int x_)
	{
		this(x_,1);
	}
	
	public Frazioni (String f)
	{
		this(Integer.parseInt(f.split("/")[0]),
			 Integer.parseInt(f.split("/")[1]));
	}
	
	//Definisco il metodo che mi permette di leggere in console il valore della frazione
	
	public String ToString()
	{
		return "La frazione è " + num + "/" +den;
	}
	
	//Introduco anche  una function che faccia la somma di num e den
	
	public String Somma()
	{
	  int somma=den+num;
	  
	  return "La somma di numeratore e denominatore è "+somma;
	}

}
