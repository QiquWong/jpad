package sandbox2.atsi;

public class Punto {

	
	private int x =0;
	private int y=0;
	
	public Punto(int x_,int y_)
	{
		x=x_;
		setY(y_);
		
	}

	public Punto(int _x)
	{
		this(_x, 0);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	
	
	
}
