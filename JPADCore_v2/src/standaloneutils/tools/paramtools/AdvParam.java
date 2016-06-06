package standaloneutils.tools.paramtools;

import java.net.URL;

import javax.measure.unit.Unit;

/**
*  <p>  An advanced parameter type that includes additional
*       information such as a link to a help web page and
*       an optional graphical symbol rather than a text label.
*  </p>
*
*  <p>  Modified by:  Agostino De Marco    </p>
*
*  @author  Agostino De Marco   Date: December 12, 2015
*  @version December 12, 2015
**/
public class AdvParam extends Param {

	/**
	*  An object containing a method for verifying the
	*  validity of the value input to this parameter.
	**/
//	private ParamVerifier verifier = null;

	/**
	*  URL to the image file to use as a symbol in
	*  place of the text label when displaying
	*  this parameter (like in a ParamInput panel).
	**/
	private URL symbolURL = null;

	
	/**
	*  URL to an image file to use for the
	*  "help/info" button when displaying this parameter
	*  in an AvdParamInput panel.
	**/
	private URL helpIconURL = null;

	/**
	*  Reference to help information for this parameter.
	**/
//	private HelpSystem helpSys = null;

	/**
	*  String describing this parameter.
	**/
	private String desc = null;
	
	
	//-----------------------------------------------------------------------------
	/**
	*  Constructs an advanced parameter object using a
	*  label string and sets the parameter to an initial
	*  value of Float.NaN (undefined).
	*
	*  @param  The label to be attached to this object.
	**/
	public AdvParam( String label ) {
		super( label );
	}

	/**
	*  Constructs an advanced parameter object using a
	*  label string and a float value for the parameter.
	*
	*  @param   label  The label to be attached to this object.
	*  @param   value  The initial value for the parameter to hold.
	**/
	public AdvParam( String label, float value ) {
		super( label, value );
	}

	/**
	*  Constructs an advanced parameter object using a
	*  label string and the units for the parameter while
	*  setting the parameter's initial value to Float.NaN
	*  (undefined).
	*
	*  @param  label    The label to be attached to this object.
	*  @param  theUnits The units associated with this parameter.
	**/
	public AdvParam( String label, Unit theUnits ) {
		super( label, theUnits );
	}

	/**
	*  Constructs an advanced parameter object using a
	*  label string, float value, and units for the parameter.
	*
	*  @param  label    The label to be attached to this object.
	*  @param  value    The initial value for the parameter to hold.
	*  @param  theUnits The units associated with this parameter.
	**/
	public AdvParam( String label, float value, Unit theUnits ) {
		super( label, value, theUnits );
	}

	/**
	*  Constructs an advanced parameter object that takes a
	*  label string and label symbol image path and sets the
	*  parameter to an initial value of Float.NaN (undefined).
	*
	*  @param  label       The label to be attached to this object.
	*  @param  symbolURL   The URL to an image file to use in place of the text
	*                      label string.
	**/
	public AdvParam( String label, URL symbolURL ) {
		super( label );
		this.symbolURL = symbolURL;
	}

	/**
	*  Constructs an advanced parameter object that takes a
	*  label string, label symbol image URL, and the units and
	*  sets parameter to an initial value of Float.NaN (undefined).
	*
	*  @param  label       The label to be attached to this object.
	*  @param  theUnits    The units associated with this parameter.
	*  @param  symbolURL   The URL to an image file to use in place of the text
	*                      label string.
	**/
	public AdvParam( String label, URL symbolURL, Unit theUnits ) {
		super( label, theUnits );
		this.symbolURL = symbolURL;
	}

	/**
	*  Constructs an advanced parameter object that takes a
	*  label string, label symbol image URL, float value,
	*  and units for the parameter.
	*
	*  @param  label       The label to be attached to this object.
	*  @param  symbolURL   The URL to an image file to use in place of the text
	*                      label string.
	*  @param  value       The initial value for the parameter to hold.
	*  @param  theUnits    The units associated with this parameter.
	**/
	public AdvParam( String label, URL symbolURL, float value, Unit theUnits ) {
		super( label, value, theUnits );
		this.symbolURL = symbolURL;
	}

	//-----------------------------------------------------------------------------
	/**
	*  Method to return the URL to the symbol image file that can be
	*  used in place of the string label of this parameter.
	*
	*  @return Returns the URL to the parameter symbol image file.
	**/
	public final URL getSymbolURL() {
		return symbolURL;
	}

	/**
	*  Add help information to this parameter.  Help information
	*  is used by display/input containers to provide the user
	*  access to user assistance information.
	*
	*  @param  buttonImgURL  URL to an image file containing the
	*                        image to be used with the help button.
	*  @param  help          Reference to a class that provides user assistance
	*                        information for this paramter.
	**/
//	public void addHelp( URL buttonImgURL, HelpSystem help ) {
//		helpIconURL = buttonImgURL;
//		helpSys = help;
//	}

	/**
	*  Removes reference to help information used by this parameter.
	**/
//	public void removeHelp() {
//		helpIconURL = null;
//		helpSys = null;
//	}

	/**
	*  Returns true if there is help information available, false if
	*  not.
	*
	*  @return  Returns true if help is avialable, false if it is not.
	**/
//	public boolean isHelpAvail() {
//		return (helpSys != null && helpSys.helpAvailable());
//	}

	/**
	*  Return's a reference to the help information associated with
	*  this paramter.  If there is none available, null is returned.
	*
	*  @return  Returns a reference to the help information for this
	*           parameter.
	**/
//	public final HelpSystem getHelpSystem() {
//		return helpSys;
//	}

	/**
	*  Method that returns the URL to the image file that can be
	*  used for the "help/info" button displayed in an
	*  AdvParamInput panel.
	*
	*  @return Returns the URL to the parameter help/info button image file.
	*          Returns null if no image is available.
	**/
	public final URL getHelpImageURL() {
		return helpIconURL;
	}

	/**
	*  Set the ParamVerifier object (and method) to be used to
	*  verify the validity of values entered for this parameter.   </p>
	*
	*  @param  verifierMethod   The ParamVerifier object to use to
	*                           verify the validity of this parameter.
	**/
//	public void setParamVerifier( ParamVerifier verifierMethod ) {
//		verifier = verifierMethod;
//	}

	/**
	*  Returns a reference to the ParamVerifier associated with this
	*  parameter object.
	*
	*  @return Returns a reference to the ParamVerifier method
	*          associated with this parameter.
	**/
//	public final ParamVerifier getVerifier() {
//		return verifier;
//	}

	/**
	*  Set the description of this parameter.
	**/
	public void setDescription(String description) {
		desc = description;
	}
	
	/**
	*  Return the description of this parameter.  Null is returned
	*  if there is no description.
	**/
	public String getDescription() {
		return desc;
	}
	
	
}