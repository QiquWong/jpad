package standaloneutils.tools.paramtools;

/**
*  <p>  An abstract class that serves as the base class for various
*       parameter method objects.  Parameter method objects contain a method
*       for calculating a given parameter's float value with a given list
*       of input parameter values.
*  </p>
*
*  <p>  Modified by:  Agostino De Marco  </p>
*
*  @author   Agostino De Marco   Date: December 12, 2015
*  @version  December 15, 2015
**/
public abstract class ParamMethod implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	*  Array to hold labels of input parameters.
	**/
	protected String[] inputLabels;

	/**
	*  Do not allow users of this class to use the default constructor.
	**/
	protected ParamMethod() {
		super();
	}

	/**
	*  The user of this class should supply a routine that calculates
	*  the parameter value with a given set of input values.  If
	*  an exception occures that can't be handled in calc(), it may be
	*  thrown.  It will be intercepted and reported back to the user
	*  by the AutoParamInput class.
	**/
	public abstract float calc( float[] inputs ) throws Exception;

	/**
	*  Returns true if the inputs for this parameter are appropriate
	*  for this method.  Normally, "valid" means that the values for the input
	*  parameters are within a certain range.
	*  The default implementation always returns true.
	**/
	public boolean validInputs(ParamDatabase pdb, String[] inputs) {
		return true;
	}
	
	/**
	*  Returns the number of inputs required by this method (the
	*  number of input parameter labels passed to the constructor when
	*  this object was instantiated).
	*
	*  @return  Returns the number of input values required by this method.
	**/
	public int getNumInputs() {
		if ( inputLabels != null )
			return inputLabels .length;
		
		return 0;
	}

	/**
	*  Returns the label of the input parameter specified by "index".
	*
	*  @param   index  Index into the input label array for the label to be returned.
	*  @return  Returns the label for the input parameter specified.
	*  @exception  ArrayIndexOutOfBoundsException  Thrown if index is < 0 or greater
	*              than the number of parameters available.
	**/
	public String getInputLabel( int index ) throws ArrayIndexOutOfBoundsException {
		return inputLabels[index];
	}

	/**
	*  Returns the array of all the input parameter labels.
	*
	*  @return  The array of all input parameter label strings is returned.
	**/
	public String[] getAllLabels() {
		return inputLabels;
	}

}