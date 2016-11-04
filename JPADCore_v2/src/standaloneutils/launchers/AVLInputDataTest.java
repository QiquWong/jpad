package standaloneutils.launchers;

public class AVLInputDataTest {

	public static void main(String[] args) {
		
		AVLAircraft aircraft = new AVLAircraft
				.Builder()
				.appendWing(
						new AVLWing
							.Builder().build())
				.build();
		
		
		System.out.println("The aircraft:");
		System.out.println(aircraft);
	}

}
