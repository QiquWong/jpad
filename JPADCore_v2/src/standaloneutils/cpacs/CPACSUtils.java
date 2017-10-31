package standaloneutils.cpacs;

/** 
 * Utility functions for CPACS frile manipulation
 * This class cannot be instantiated
 * 
 * @author Agostino De Marco
 * @author Giuseppe Torre
 * 
 */

public final class CPACSUtils {

	/**
	 * Returns a string made up of rows, that displays a 2D array in a tabular format
	 * recognized by JSBSim; element (0,0) of input matrix is unused
	 * 
	 * @param matrix, a 2D array; element (0,0) is unused
	 * @param string, column separator in the tabular output
	 * @return string, tabular display of the input matrix 
	 */
	public static String matrixDoubleToJSBSimTable2D(double[][] matrix, String separator) {

		StringBuffer result = new StringBuffer();

		// iterate over the first dimension
		for (int i = 0; i < matrix.length; i++) {
			// iterate over the second dimension
			for(int j = 0; j < matrix[i].length; j++){
				if(i==0&&j==0) {
					result.append("	");
				}
				else {
					result.append(matrix[i][j]);
					result.append(separator);
				}
			}
			// remove the last separator
			result.setLength(result.length() - separator.length());
			// add a line break.
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Returns a string made up of N rows and 2 columns, that displays a 2D array in a tabular format
	 * recognized by JSBSim
	 * 
	 * @param matrix, a 2D array, Nx2, where N is the number of matrix rows
	 * @param string, column separator in the tabular output
	 * @return string, tabular display of the input matrix 
	 */
	public static String matrixDoubleToJSBSimTableNx2(double[][] matrix) {

		StringBuffer result = new StringBuffer();

		// iterate over the first dimension
		for (int i = 0; i < matrix.length; i++) {
			// iterate over the second dimension
			for(int j = 0; j < matrix[i].length; j++){

				result.append(matrix[i][j]);
				result.append("	");
			}
			// remove the last separator
			// add a line break.
			result.append("\n");
		}
		return result.toString();
	}	


}
