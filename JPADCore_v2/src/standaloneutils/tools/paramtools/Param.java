package standaloneutils.tools.paramtools;

import java.text.*;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

/**
*  <p>  A generic parameter class for use in engineering JPAD-derived
*       programs.  The Param object provides a package that
*       contains an extended amount of information on a single
*       parameter or value such as the units and an identifying
*       label.  </p>
*
*  <p>  Modified by:  Agostino De Marco    </p>
*
*  @author  Agostino De Marco   Date: December 12, 2015
*  @version December 12, 2015
**/
public class Param extends Object implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	/**
	*  The value of this parameter.
	**/
	private Amount<?> theValue;

	/**
	* Is this parameter defined?
	**/
	private boolean defined;

	/**
	*  String containing a label for this parameter.
	**/
	private String labelStr;

	/**
	*  The number format used when displaying this parameter.
	**/
	private NumberFormat numFormat = null;
	
	
	//-----------------------------------------------------------------------------
	/**
	*  Constructs a parameter object using a label string.
	*  Sets the parameter to an initial value of Float.NaN
	*  (makes parameter undefined).
	*
	*  @param  The label to be attached to this object.
	**/
	public Param( String label ) {
		labelStr = label;
		theValue = Amount.valueOf(Float.NaN, Unit.ONE);
		defined = false;
	}

	/**
	*  Constructs a parameter object using a label string
	*  and a float value for the parameter.
	*
	*  @param   label  The label to be attached to this object.
	*  @param   value  The initial value for the parameter to hold.
	**/
	public Param( String label, float value ) {
		labelStr = label;
		theValue = Amount.valueOf(value, Unit.ONE);
		defined = true;
	}

	/**
	*  Constructs a parameter object using a label string
	*  and the units for the parameter while setting the
	*  parameter's initial value to 1.
	*
	*  @param  label    The label to be attached to this object.
	*  @param  theUnits The units associated with this parameter.
	**/
	public Param( String label, Unit<?> theUnits ) {
		labelStr = label;
		theValue = Amount.valueOf(1.0, theUnits);
		defined = true;
	}

	/**
	*  Constructs a parameter object using a label string,
	*  float value, and units for the parameter.
	*
	*  @param  label    The label to be attached to this object.
	*  @param  value    The initial value for the parameter to hold.
	*  @param  theUnits The units associated with this parameter.
	**/
	public Param( String label, float value, Unit<?> theUnits ) {
		labelStr = label;
		defined = true;
		theValue = Amount.valueOf(value, theUnits);
	}

	//-----------------------------------------------------------------------------
	/**
	*  Method to return the current value of this parameter in the
	*  current units.
	*
	*  @return  The float value of this parameter in the current units is returned.
	**/
	public final Amount<?> getValue() {
		return theValue;
	}

	/**
	*  Method to set the value of this parameter in the current units.
	*  Makes the parameter "defined".
	*
	*  @param   value  The value that the parameter object should be set to.
	**/
	public void setValue( Amount<?> value ) {
		theValue = value;		
	}

	/**
	*  Method to return the string representation of the label of this
	*  parameter.
	*
	*  @return  Returns the string representation of the parameter label.
	**/
	public final String getLabel() {
		return labelStr;
	}

	/**
	*  Method to change the label that is associated with this parameter.
	*
	*  @param   label   The label to be attached to this object.
	**/
	public void setLabel( String label ) {
		labelStr = label;
	}

	/**
	*  Method to return the current units of this parameter.
	*
	*  @return  Returns the units of this parameter.
	**/
	public final Unit<?> getUnits() {
		return theValue.getUnit();
	}

	/**
	*  Set the number format to use when displaying this parameter.
	*  If no number format is set, then a default will be used.
	**/
	public void setNumberFormat(NumberFormat nf) {
		numFormat = nf;
	}
	
	/**
	*  Get the number format used when displaying this parameter.
	*  If no number format has been set, null is returned.
	**/
	public NumberFormat getNumberFormat() {
		return numFormat;
	}
	
	/**
	*  Returns true if parameter is defined, false otherwise.
	*
	*  @return  Returns true if this parameter is defined.
	**/
	public final boolean isDefined() {
		return defined;
	}

	/**
	*  Method to make this parameter undefined.  The value is set to
	*  Float.NaN.
	*
	*  @return  Returns true if this parameter is defined.
	**/
	public void makeUndefined() {
		defined = false;
		theValue = Amount.valueOf(Float.NaN, Unit.ONE);
	}

	/**
	*  Method to return the value of this parameter in current
	*  units as a string.
	*
	*  @return Returns a string representation of the value of this parameter
	*          in the current units.
	**/
	public String toString() {
		String s = null;
		if (numFormat == null)
			s = theValue.toString();
		else
			s = numFormat.format(theValue);
			
		return s;
	}
	
}
