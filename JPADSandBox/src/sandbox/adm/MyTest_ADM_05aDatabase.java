package sandbox.adm;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;

public class MyTest_ADM_05aDatabase {

	static long[] dims2D = { 20, 10 };
	static long[] dims3D = { 20, 10, 5 };

	public MyTest_ADM_05aDatabase() throws Exception {

		// Create a HDF5 file

		// The name of the file we'll create.
		String fname = "test/test05a.h5";

		//	    // Retrieve an instance of the implementing class for the HDF5 format
		//	    FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
		//
		//	    // If the implementing class wasn't found, it's an error.
		//	    if (fileFormat == null) {
		//	    	System.err.println("Cannot find HDF5 FileFormat.");
		//	    	return;
		//	    }
		//
		//	    // If the implementing class was found, use it to create a new HDF5 file
		//	    // with a specific file name.
		//	    //
		//	    // If the specified file already exists, it is truncated.
		//	    // The default HDF5 file creation and access properties are used.
		//	    //
		//	    H5File testFile = (H5File) fileFormat.createFile(fname, FileFormat.FILE_CREATE_DELETE);
		//
		//	    // Check for error condition and report.
		//	    if (testFile == null) {
		//	    	System.err.println("Failed to create file: " + fname);
		//	    	return;
		//	    }

		System.out.println("Creating file : " + fname);
		
//		createFile(fname);
		createFile2(fname);

		// retrieve an instance of H5File
		FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

		if (fileFormat == null) {
			System.err.println("Cannot find HDF5 FileFormat.");
			return;
		}	    

		// open the file with read-only access
        FileFormat testFile = fileFormat.createInstance(fname, FileFormat.READ);
        
		// open the file and retrieve the file structure
		testFile.open();
		Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();

		printGroup(root, "agodemar");

		// close file resource
		testFile.close();
		
		System.out.println("file " + fname + " written and closed");
		
		/////////////////////////////////////////////////////////////////////////

		System.out.println("Reopening file " + fname);
		
        // open the file and retrieve the file structure
        testFile.open();
        root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();

        // retrieve the dataset "2D 32-bit integer 20x10"
        Dataset dataset = (Dataset) root.getMemberList().get(0);
        int[] dataRead = (int[]) dataset.read();

        // print out the data values
        System.out.println("\n\nOriginal Data Values");
        for (int i = 0; i < 20; i++) {
            System.out.print("\n" + dataRead[i * 10]);
            for (int j = 1; j < 10; j++) {
                System.out.print(", " + dataRead[i * 10 + j]);
            }
        }
        System.out.println("");

	} // end-of constructor

	/**
	 * Recursively print a group and its members.
	 * 
	 * @throws Exception
	 */
	private static void printGroup(Group g, String indent) throws Exception {
		if (g == null) return;

		java.util.List members = g.getMemberList();

		int n = members.size();
		indent += "    ";
		HObject obj = null;
		for (int i = 0; i < n; i++) {
			obj = (HObject) members.get(i);
			System.out.println(indent + obj);
			if (obj instanceof Group) {
				printGroup((Group) obj, indent);
			}
		}
	}

	/**
	 * create the file and add groups and dataset into the file, which is the
	 * same as javaExample.H5DatasetCreate
	 * 
	 * @see javaExample.HDF5DatasetCreate
	 * @throws Exception
	 */
	private static void createFile(String fname) throws Exception {
		// retrieve an instance of H5File
		FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

		if (fileFormat == null) {
			System.err.println("Cannot find HDF5 FileFormat.");
			return;
		}

		// create a new file with a given file name.
		H5File testFile = (H5File) fileFormat.createFile(fname, FileFormat.FILE_CREATE_DELETE);

		if (testFile == null) {
			System.err.println("Failed to create file:" + fname);
			return;
		}

		// open the file and retrieve the root group
		testFile.open();
		Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();

		// create groups at the root
		Group g1 = testFile.createGroup("integer arrays", root);
		Group g2 = testFile.createGroup("float arrays", root);

		// create 2D 32-bit (4 bytes) integer dataset of 20 by 10
		Datatype dtype = testFile.createDatatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);
		Dataset dataset = testFile.createScalarDS("2D 32-bit integer 20x10", g1, dtype, dims2D, null, null, 0, null);

		// create 3D 8-bit (1 byte) unsigned integer dataset of 20 by 10 by 5
		dtype = testFile.createDatatype(Datatype.CLASS_INTEGER, 1, Datatype.NATIVE, Datatype.SIGN_NONE);
		dataset = testFile.createScalarDS("3D 8-bit unsigned integer 20x10x5", g1, dtype, dims3D, null, null, 0, null);

		// create 2D 64-bit (8 bytes) double dataset of 20 by 10
		dtype = testFile.createDatatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, -1);
		dataset = testFile.createScalarDS("2D 64-bit double 20x10", g2, dtype, dims2D, null, null, 0, null);

		// create 3D 32-bit (4 bytes) float dataset of 20 by 10 by 5
		dtype = testFile.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
		dataset = testFile.createScalarDS("3D 32-bit float  20x10x5", g2, dtype, dims3D, null, null, 0, null);

		// close file resource
		testFile.close();
	}

    /**
     * create the file and add groups and dataset into the file, which is the
     * same as javaExample.H5DatasetCreate
     * 
     * @see javaExample.HDF5DatasetCreate
     * @throws Exception
     */
    private static void createFile2(String fname) throws Exception {
        // retrieve an instance of H5File
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null) {
            System.err.println("Cannot find HDF5 FileFormat.");
            return;
        }

        // create a new file with a given file name.
        H5File testFile = (H5File) fileFormat.createFile(fname, FileFormat.FILE_CREATE_DELETE);

        if (testFile == null) {
            System.err.println("Failed to create file:" + fname);
            return;
        }

        // open the file and retrieve the root group
        testFile.open();
        Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();

        // set the data values
        int[] dataIn = new int[20 * 10];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                dataIn[i * 10 + j] = 1000 + i * 100 + j;
            }
        }

        // create 2D 32-bit (4 bytes) integer dataset of 20 by 10
        Datatype dtype = testFile.createDatatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);
        Dataset dataset = testFile
                .createScalarDS("2D 32-bit integer 20x10", root, dtype, dims2D, null, null, 0, dataIn);

        // close file resource
        testFile.close();
    }
	

}
