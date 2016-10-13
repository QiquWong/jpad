package standaloneutils.launchers;

public class DatcomInputData {

	public static void main(String[] args) {
		IDatcomInputData inputData = new IDatcomInputData
				.Builder()
				.buildPartial(); // Skips validation
				// .build(); // validate for all fields to be set
		System.out.println("--- Test DatcomInputData ---\n");
		System.out.println(inputData);
		System.out.println(inputData.getDescription());

	}

}
