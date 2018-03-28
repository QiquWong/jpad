/*
*   FReaderFactory	-- A factory object that returns FTableReader objects.
*
*   Copyright (C) 2003-2011 by Joseph A. Huwaldt
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
*   
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Library General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*  Or visit:  http://www.gnu.org/licenses/lgpl.html
**/
package standaloneutils.aerotools.tools.tables;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.JOptionPane;

import standaloneutils.aerotools.util.ToStringComparator;

/**
*  This class returns a specific FTableReader object that can read in the specified file.
*  This class implements a pluggable architecture.  A new FTableReader class can be added
*  by simply creating a subclass of FTableReader, creating a FTableReader.properties file
*  that refers to it, and putting that properties file somewhere in the Java search
*  path.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  April 3, 2000
*  @version   October 2, 2011
**/
public final class FTReaderFactory {

	//  Debug flag.
    private static final boolean DEBUG = true;

    /**
	* All class loader resources with this name ("FTableReader.properties") are loaded
	* as .properties definitions and merged together to create a global list of
	* reader handler mappings.
	**/
    private static final String kMappingResName = "FTableReader.properties";
    
	//  An array containing a reference to all the readers that have been found.
    private static final FTableReader[] kAllReaders; // set in <clinit>
    
	//  Locate and load all the readers we can find.
    static {
        FTableReader[] temp = null;
        try {
            temp = loadResourceList(kMappingResName, getClassLoader());
			
        } catch (Exception e) {
            if (DEBUG) {
                System.out.println ("could not load all" +
									" [" + kMappingResName + "] mappings:");
                e.printStackTrace (System.out);
            }
        }
        
        kAllReaders = temp;
    }

	// this class is not extendible
    private FTReaderFactory() {}
	
	
	/**
	*  Method that attempts to find a FTableReader object that might be able
	*  to read the specified file.  If an appropriate reader can not be found
	*  an IOException is thrown.  If the user cancels the selection dialog
	*  for multiple readers, null is returned.  "pathName" may be the path to a file
	*  or just a file name -- the extension may be used to determine file type.
	**/
	public static FTableReader getReader(InputStream inputStream, String pathName) throws IOException {
		
		//  Get the list of data readers that are available.
		FTableReader[] allReaders = getAllReaders();
		if (allReaders == null || allReaders.length < 1)
			throw new IOException("There are no readers available at all.");
		
		ArrayList<FTableReader> list = new ArrayList<FTableReader>();
		BufferedInputStream input = null;
		
		try {
			//	Wrap input stream in a buffered stream.
			input = new BufferedInputStream(inputStream);
			String name = new File(pathName).getName();

			//  Loop through all the available readers and see if any of them will work.
			int numReaders = allReaders.length;
			for (int i=0; i < numReaders; ++i) {
				//  Mark the current position in the input stream (the start of the stream).
				input.mark(1024000);
				
				FTableReader reader = allReaders[i];
				
				//  Can this reader read the specified input stream?
				int canReadFile = reader.canReadData(name, input);
				
				//  If the reader is certain it can read the data, use it.
				if (canReadFile == FTableReader.YES)  return reader;
				
				//  Otherwise, build a list of maybes.
				if (canReadFile == FTableReader.MAYBE)
					list.add(reader);
				
				//  Return to the start of the stream for the next pass.
				input.reset();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException ("An error occured trying to determine the file's type.");
		}
			
		if (list.size() < 1)
			throw new IOException ("Can not determine the file's type.");

		//  If there is only one reader in the list, try and use it.
		FTableReader selectedReader = null;
		if (list.size() == 1)
			selectedReader = list.get(0);
			
		else {
			//  Ask the user to select which reader they want to try and use.
			Object[] possibleValues = list.toArray();
			selectedReader= (FTableReader)JOptionPane.showInputDialog(null,
						"Choose a format for the file: " + pathName, "Select Format",
						JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		}
		
		return selectedReader;
	}
	
	/**
	*  Method that attempts to find a FTableReader object that might be able
	*  to read the specified file.  If an appropriate reader can not be found
	*  an IOException is thrown.  If the user cancels the selection dialog
	*  for multiple readers, null is returned.
	**/
	public static FTableReader getReader(File theFile) throws IOException {
	
		if (!theFile.exists())
			throw new IOException("Could not find the file " + theFile.getName() + ".");

		//  Get the list of data readers that are available.
		FTableReader[] allReaders = getAllReaders();
		if (allReaders == null || allReaders.length < 1)
			throw new IOException("There are no readers available at all.");
		
		ArrayList<FTableReader> list = new ArrayList<FTableReader>();
		BufferedInputStream input = null;
		
		try {
			//	Open up a stream to the file.
			input = new BufferedInputStream(new FileInputStream(theFile));
			String name = theFile.getName();

			//  Loop through all the available readers and see if any of them will work.
			int numReaders = allReaders.length;
			for (int i=0; i < numReaders; ++i) {
				//  Mark the current position in the input stream (the start of the stream).
				input.mark(1024000);
				
				FTableReader reader = allReaders[i];
				
				//  Can this reader read the specified input stream?
				int canReadFile = reader.canReadData(name, input);
				
				//  If the reader is certain it can read the data, use it.
				if (canReadFile == FTableReader.YES)  return reader;
				
				//  Otherwise, build a list of maybes.
				if (canReadFile == FTableReader.MAYBE)
					list.add(reader);
				
				//  Return to the start of the stream for the next pass.
				input.reset();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException ("An error occured trying to determine the file's type.");
			
		} finally {
			//	Make sure and close the input stream.
			try {
				if (input != null)  input.close();
			} catch (IOException ignore) {}
		}
		
		if (list.size() < 1)
			throw new IOException ("Can not determine the file's type.");

		//  If there is only one reader in the list, try and use it.
		FTableReader selectedReader = null;
		if (list.size() == 1)
			selectedReader = list.get(0);
			
		else {
			//  Ask the user to select which reader they want to try and use.
			Object[] possibleValues = list.toArray();
			selectedReader= (FTableReader)JOptionPane.showInputDialog(null,
						"Choose a format for the file: " + theFile.getName(), "Select Format",
						JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		}
		
		return selectedReader;
	}


    /**
	*  Method that returns a list of all the FTableReader objects found by this
	*  factory during static initialization.
	*  
	*  @return An array of FTableReader objects [can be null if static init failed]
	**/
    public static FTableReader[] getAllReaders()  {
        return kAllReaders;
    }
    
    /*
     * Loads a reader list that is a union of *all* resources named
     * 'resourceName' as seen by 'loader'. Null 'loader' is equivalent to the
     * application loader.
     */
	private static FTableReader[] loadResourceList(final String resourceName, ClassLoader loader) {
        if (loader == null) loader = ClassLoader.getSystemClassLoader();
        
        final HashSet<FTableReader> result = new HashSet<FTableReader>();
        
        try {
            // NOTE: using getResources() here
            final Enumeration<URL> resources = loader.getResources(resourceName);
            
            if (resources != null) {
                // merge all mappings in 'resources':
                
                while (resources.hasMoreElements()) {
                    final URL url = (URL)resources.nextElement();
                    final Properties mapping;
                    
                    InputStream urlIn = null;
                    try {
                        urlIn = url.openStream();
                        
                        mapping = new Properties();
                        mapping.load(urlIn); // load in .properties format
						
                    } catch (IOException ioe) {
                        // ignore this resource and go to the next one
                        continue;
					
                    } finally {
                        if (urlIn != null) try { urlIn.close (); }
                                           catch (Exception ignore) {} 
                    }
                    
                    // load all readers specified in 'mapping':
                     
                    for (Enumeration<?> keys = mapping.propertyNames(); keys.hasMoreElements (); ) {
                        final String format = (String) keys.nextElement();
                        final String implClassName = mapping.getProperty(format);
                        
                        result.add((FTableReader) loadResource(implClassName, loader));
                    }
                }
            }
		
        } catch (IOException ignore) {
            // ignore: an empty result will be returned
        }
        
		//  Convert result Set to an array.
		FTableReader[] resultArr = (FTableReader[])result.toArray();
		
		//  Sort the array using the specified comparator.
		Arrays.sort(resultArr, new ToStringComparator<FTableReader>());
		
		//  Output the sorted array.
        return resultArr;  
    }

    /*
     * Loads and initializes a single resource for a given format name via a
     * given class loader. For simplicity, all errors are converted to
     * RuntimeExceptions.
     */    
    private static Object loadResource(final String className, final ClassLoader loader) {
        if (className == null) throw new IllegalArgumentException ("null input: className");
        if (loader == null) throw new IllegalArgumentException ("null input: loader");
        
        final Class<?> cls;
        final Object reader;
        try {
            cls = Class.forName(className, true, loader);
            reader = cls.newInstance();
			
        } catch (Exception e) {
            throw new RuntimeException ("could not load and instantiate" +
                " [" + className + "]: " + e.getMessage ());
        }
        
        if (! (reader instanceof FTableReader))
            throw new RuntimeException ("not a standaloneutils.mathtools.tables.FTableReader" +
                " implementation: " + cls.getName ());
        
        return reader;
    }
    
    /**
     * This method decides on which class loader is to be used by all resource/class
     * loading in this class. At the very least you should use the current thread's
     * context loader. A better strategy would be to use techniques shown in
     * http://www.javaworld.com/javaworld/javaqa/2003-06/01-qa-0606-load.html 
     */
    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
	
}

