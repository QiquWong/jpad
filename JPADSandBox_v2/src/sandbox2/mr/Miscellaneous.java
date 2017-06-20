package sandbox2.mr;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;


public class Miscellaneous {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Amount<?> clAlpha = Amount.valueOf(0.1047, NonSI.DEGREE_ANGLE.inverse());
		System.out.println("CL ALPHA " + clAlpha);
		System.out.println("CL ALPHA " + clAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue());
		System.out.println("CL ALPHA " + clAlpha.to(SI.RADIAN.inverse()));
	}

}
