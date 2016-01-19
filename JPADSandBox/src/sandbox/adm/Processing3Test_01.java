package sandbox.adm;

import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;


public class Processing3Test_01 extends PApplet {


	// The rest is what you would expect in the sketch

	int x, y;

	public void settings(){
		size(400, 400, FX2D);     
	}

	public void setup() {
		// Other setuo code here
	}

	public void mouseMoved(){
		x = mouseX;
		y = mouseY;
	}

	public void draw() {
		background(0);
		ellipse(x, y, 10, 10);
	}    

	// Run this project as Java application and this
	// method will launch the sketch
	public static void main(String[] args) {
		String[] a = {"MAIN"};
		PApplet.runSketch( a, new Processing3Test_01());
	}

}
