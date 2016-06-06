package standaloneutils.tools.paramtools;

import java.util.*;
import java.io.*;


/**
*  <p>  A database of Param objects for use in my engineering programs.  </p>
*
*  <p>  This database or collection can be used to store Param objects used
*       by a program.  The database is keyed off the parameter label.
*       Therefore, all parameter labels must be unique or new ones will
*       overwrite old ones of the same name.  Also, once you've placed a
*       parameter in a ParamDatabase, you must not change it's label.
*  </p>
*
*  <p>  Modified by:  Agostino De Marco  </p>
*
*  @author   Agostino De Marco	Date:	December 12, 2015
*  @version  December 12, 2015
**/
public final class ParamDatabase implements Serializable {

	/**
	*  The database is in a Hashtable.
	**/
	private transient Hashtable theDB = new Hashtable();

	//-----------------------------------------------------------------------------
	/**
	*  Default constructor to return a reference to the parameter database.
	**/
	public ParamDatabase() { }

	//-----------------------------------------------------------------------------
	/**
	*  Returns the number of elements contained in the database.
	*
	*  @return  The number of elements contained in the database.
	**/
	public int size() {
		return theDB.size();
	}

	/**
	*  Returns true if the database contains no elements.
	*
	*  @return  Returns true if the database contains no elements, false otherwise.
	**/
	public boolean isEmpty() {
		return theDB.isEmpty();
	}

	/**
	*  Returns true if the database contains an element for the given label.
	*
	*  @param   label   The label for the parameter that we are looking for.
	*  @return  Returns true if the database contains an element for the given label.
	**/
	public boolean containsKey( String label ) {
		return theDB.containsKey( label );
	}

	/**
	*  Gets the parameter associated with the specified label in the
	*  database.
	*
	*  @param   label   The specified parameter label.
	*  @return  The parameter for the label or null if the label is not
	*           defined in the database.
	*  @see     ParamDatabase#put
	**/
	public Param get( String label ) {
		return (Param) (theDB.get( label ));
	}

	/**
	*  Puts the specified parameter into the database, using the specified
	*  label.  The parameter may be retrieved by doing a get() with the
	*  same label.  The label and the parameter cannot be null.
	*
	*  @param      label   The specified label in the database.
	*  @param      value   The parameter to be added to the database.
	*  @return     The old value of the label, or null if it did not have one.
	*  @exception  NullPointerException If the value of the parameter
	*              is equal to null.
	*  @see        ParamDatabase#get
	**/
	public Param put( String label, Param value ) {
		return (Param) (theDB.put( label, value ));
	}

	/**
	*  Puts all the parameters in the specified parameter database into this
	*  database.
	**/
	public void putAll(ParamDatabase pdb) {
		theDB.putAll(pdb.theDB);
	}
	
	/**
	*  Removes the parameter corresponding to the label. Does nothing if the
	*  label is not present.
	*
	*  @param   label  The label that needs to be removed
	*  @return  The value of label, or null if the label was not found.
	**/
	public Param remove( String label ) {
		return (Param) (theDB.remove( label ));
	}

	/**
	*  Clears the database so that it has no more elements in it.
	**/
	public void clear() {
		theDB.clear();
	}

	/**
	*  Returns an enumeration of the elements. Use the Enumeration methods
	*  on the returned object to fetch the elements sequentially.
	*
	*  @return  An enumeration of the elements in the database returned sequentially.
	**/
	public Enumeration elements() {
		return theDB.elements();
	}

	/**
	*  Converts database to a rather lengthy String.
	*
	*  @return  String representation of the entire database.
	**/
	public String toString() {
		return theDB.toString();
	}


	//-----------------------------------------------------------------------------
	/**
	*  During serialization, this will write out each parameter
	*  in this parameter database.
	**/
	private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
	
		// Call the default write object method.
		out.defaultWriteObject();

		// Write out the number of methods we are going to write out.
		int size = theDB.size();
		out.writeInt( size );

		// Now loop over all the methods and write out each one.
		Enumeration myEnum = theDB.elements();
		while ( myEnum.hasMoreElements() ) {
			Param theParam = (Param) myEnum.nextElement();
			out.writeObject( theParam );
		}

	}

	/**
	*  During de-serialization, this will handle the reading in of
	*  the list of methods associated with this parameter.
	**/
	private void readObject( java.io.ObjectInputStream in )
									throws IOException, ClassNotFoundException {
	
		// Call the default read object method.
		in.defaultReadObject();

		// Create a new database.
		theDB = new Hashtable();
		
		// Read in the number of methods in the file.
		int size = in.readInt();

		// Loop over each method and read it in.
		for ( int i = 0; i < size; ++i ) {
			Param theParam = (Param) in.readObject();
			
			// Add the new method to the method list.
			theDB.put( theParam.getLabel(), theParam );
		}

	}

}