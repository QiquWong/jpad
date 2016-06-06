package standaloneutils.customdata;

public class MyPoint {
	
	private double x,y,z;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double distance(MyPoint p) {
		return Math.sqrt(Math.pow(x - p.getX(),2) + Math.pow(y - p.getY(),2) + Math.pow(z - p.getZ(),2));
	}
	
}
