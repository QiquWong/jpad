package standaloneutils.tools.paramtools;

/**
*  <p>  Parameter methods may throw this exception whenever there is
*       a problem.
*  </p>
*
*  <p>  Modified by:  Agostino De Marco  </p>
*
*  @author  Agostino De Marco   Date: December 12, 2015
*  @version December 6, 2015
**/
public class ParamException extends Exception {

	/**
	*  Constructs a ParamException with the specified detail message.
	*  A detail message is a String that describes this particular exception.
	*
	*	@param msg  The String containing a detail message.
	**/
	public ParamException( String msg ) {
		super( msg );
	}

	/**
	* Returns a short description of the ParamException.
	*
	*	@return  A short description of the Exception as a String.
	**/
	public  String toString() {
		return getMessage();
	}

}


