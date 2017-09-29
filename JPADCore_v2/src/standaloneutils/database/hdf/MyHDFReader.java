package standaloneutils.database.hdf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import com.sun.org.apache.xerces.internal.xs.StringList;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.callbacks.H5O_iterate_cb;
import ncsa.hdf.hdf5lib.callbacks.H5O_iterate_t;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import ncsa.hdf.hdf5lib.structs.H5O_info_t;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.customdata.MyArray;

public class MyHDFReader {

	private String _fileName = null;
	private H5File _fileH5 = null;
	private H5Group _rootGroup = null;


	// HDF5 Object Types
	enum H5O_type {
		H5O_TYPE_UNKNOWN(-1), // Unknown object type
		H5O_TYPE_GROUP(0), // Object is a group
		H5O_TYPE_DATASET(1), // Object is a dataset
		H5O_TYPE_NAMED_DATATYPE(2), // Object is a named data type
		H5O_TYPE_NTYPES(3); // Number of different object types
		private static final Map<Integer, H5O_type> lookup = new HashMap<Integer, H5O_type>();

		static {
			for (H5O_type s : EnumSet.allOf(H5O_type.class))
				lookup.put(s.getCode(), s);
		}

		private int code;

		H5O_type(int layout_type) {
			this.code = layout_type;
		}

		public int getCode() {
			return this.code;
		}

		public static H5O_type get(int code) {
			return lookup.get(code);
		}
	}

	public MyHDFReader() {}

	public MyHDFReader(String fname) {
		_fileName = fname;
		this.open();
	}

	public void open(){

		_fileH5 = new H5File(_fileName, FileFormat.READ); // throws an exception in case of failure
		_fileH5.setMaxMembers(1000000);
		
		// get the root node
		String DATASETNAME = "/";
		try {
			_rootGroup = (H5Group) _fileH5.get(DATASETNAME);
		} catch (Exception e) {
			
			String interpolaterDatabaseSerializedDirectory = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR) + File.separator + "serializedDatabase" + File.separator; 
			String interpolaterDatabaseSerializedFullName = "";
			File fileDatabase = null;
			
			String fileName = _fileH5.getName();
			if(fileName.contains("Aerodynamic_Database_Ultimate")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory +  
						MyConfiguration.interpolaterAerodynamicDatabaseSerializedName;

				fileDatabase = new File(interpolaterDatabaseSerializedFullName);

				if(!fileDatabase.exists()){
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("HighLiftDatabase")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory +  
						MyConfiguration.interpolaterHighLiftDatabaseSerializedName;

				fileDatabase = new File(interpolaterDatabaseSerializedFullName);

				if(!fileDatabase.exists()){
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("FusDes_database")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory +  
						MyConfiguration.interpolaterFusDesDatabaseSerializedName;

				fileDatabase = new File(interpolaterDatabaseSerializedFullName);

				if(!fileDatabase.exists()){
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("VeDSC_database")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ MyConfiguration.interpolaterVeDSCDatabaseSerializedName; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("FuelFractions")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ MyConfiguration.interpolaterFuelFractionDatabaseSerializedName; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("TurbofanEngineDatabase")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "TurbofanEngineDatabase.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("TurbopropEngineDatabase")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "TurbopropEngineDatabase.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("Normalized_TurboProp_100%Cruise_ISA0")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_100%Cruise_ISA0.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("Normalized_TurboProp_100%Cruise_ISA10")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_100%Cruise_ISA10.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("Normalized_TurboProp_100%Cruise_ISA30")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_100%Cruise_ISA30.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("Normalized_TurboProp_100%Cruise_ISA40")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_100%Cruise_ISA40.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("Normalized_TurboProp_95%Cruise_ISA0")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_95%Cruise_ISA0.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			} 
			else if(fileName.contains("Normalized_TurboProp_95%Cruise_ISA10")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_95%Cruise_ISA10.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_95%Cruise_ISA30")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_95%Cruise_ISA30.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_95%Cruise_ISA40")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_95%Cruise_ISA40.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_75%Cruise_ISA0")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_75%Cruise_ISA0.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_75%Cruise_ISA10")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_75%Cruise_ISA10.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_75%Cruise_ISA30")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_75%Cruise_ISA30.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_75%Cruise_ISA40")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_75%Cruise_ISA40.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_40%Cruise_ISA0")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_40%Cruise_ISA0.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_40%Cruise_ISA10")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_40%Cruise_ISA10.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_40%Cruise_ISA30")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_40%Cruise_ISA30.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
			else if(fileName.contains("Normalized_TurboProp_40%Cruise_ISA40")) {
				interpolaterDatabaseSerializedFullName = interpolaterDatabaseSerializedDirectory 
						+ "Normalized_TurboProp_40%Cruise_ISA40.xml"; 

				fileDatabase = new File(interpolaterDatabaseSerializedFullName); 

				if (!fileDatabase.exists()) {
					e.printStackTrace();
				}
			}
		}

		//		System.out.println("Root group: " + _rootGroup.getName());

	} // end-of open()

	public void close(){
		if (_fileH5.getFID() >= 0)
		{
			try {
				_fileH5.close();
			} catch (HDF5Exception e) {
				e.printStackTrace();
			}
			_fileName = null;
			_rootGroup = null;			
		}
	}

	public H5Group getRootGroup() {
		return _rootGroup;
	}

	/////////////////////////////////////////////////////

	private class MyIterateData_FindGroupByName {
		public String link_name = null;
		public int link_type = -1;
		public int link_id = -1;

		public boolean groupFound = false;
		public H5Group group = null;

		MyIterateData_FindGroupByName(String name, int type, int id) {
			this.link_name = name;
			this.link_type = type;
			this.link_id   = id;
		}
	}

	private class H5O_MyIterData_FindGroupByName implements H5O_iterate_t {
		// a List containig data gathered at each call of callback function
		public ArrayList<MyIterateData_FindGroupByName> iterdata = new ArrayList<MyIterateData_FindGroupByName>();
		// When search is successful, the group id and info are stored 
		// in last element of this list

		// a function returning search success
		public boolean isGroupFound()
		{
			if (iterdata.size() == 0)
				return false;

			// check the last iteration only
			boolean retValue = iterdata.get(iterdata.size() - 1).groupFound;
			return retValue;
		}
	}

	private class H5O_IterCallback_FindGroupByName implements H5O_iterate_cb {

		public String theNameWeSearch = null;
		public int idGroupFound = -1;

		public H5O_IterCallback_FindGroupByName(String theName)
		{
			super();
			theNameWeSearch = theName;
		}

		public int callback(int group_id, String name, H5O_info_t info, H5O_iterate_t op_data) {

			if ( ((H5O_MyIterData_FindGroupByName)op_data).isGroupFound() )
			{
				// there's non need to search further
				// return straight away
				//        		System.out.println("------- search stopped");
				// the group id and info are stored in last element of 
				// ((H5O_MyIterData_FindGroupByName)op_data).iterdata
				return 0;
			}

			MyIterateData_FindGroupByName id = new MyIterateData_FindGroupByName(name, info.type, group_id);
			((H5O_MyIterData_FindGroupByName)op_data).iterdata.add(id);

			//			System.out.print("/"); /* Print root group in object path */

			//Check if the current object is the root group, and if not print the full path name and type.

			if (name.charAt(0) == '.')         /* Root group, do not print '.' */
			{
				//				System.out.println("  (Group)");
			}
			else if(info.type == HDF5Constants.H5O_TYPE_GROUP )
			{
				//				System.out.println(name + "  (Group)" );

				// set flag in list of data
				if (name.equals(theNameWeSearch))
				{
					int sz = ((H5O_MyIterData_FindGroupByName)op_data).iterdata.size();

					// set flag
					(((H5O_MyIterData_FindGroupByName)op_data)
							.iterdata.get(sz - 1)
							).groupFound = true;

					idGroupFound = group_id;

					//					System.out.println(
					//							"---- FOUND ----> " + name + "\n" +
					//							"---- ID    ----> " + group_id + "\n" +
					//							"---- Type  ----> " + info.type                    				
					//							);

				}
			}
			else if(info.type == HDF5Constants.H5O_TYPE_DATASET)
			{
				//				System.out.println(name + "  (Dataset)");
			}
			else if (info.type == HDF5Constants.H5O_TYPE_NAMED_DATATYPE )
			{
				//				System.out.println(name + "  (Datatype)");
			} else {
				//				System.out.println(name + "  (Unknown)");
			}

			return 0;
		}
	}

	public int findGroupByName(String groupFullName) throws HDF5LibraryException, NullPointerException{
		if (_fileH5 == null || _rootGroup == null)
			return -1;

		if (groupFullName == null || groupFullName.length() == 0)
			return -1;

		// recursively iterate

		// some useful variables
		HObject obj = null;
		int o_id = -1;
		int objType;
		H5O_info_t info;

		// check start from root
		o_id = _rootGroup.open();
		info = H5.H5Oget_info(o_id);
		objType = info.type;

		H5O_iterate_t iter_data = new H5O_MyIterData_FindGroupByName();
		H5O_iterate_cb iter_cb = new H5O_IterCallback_FindGroupByName(groupFullName);

		// recursive visit
		H5.H5Ovisit(
				_fileH5.getFID(), 
				HDF5Constants.H5_INDEX_NAME, 
				HDF5Constants.H5_ITER_NATIVE, 
				iter_cb, 
				iter_data
				);

		//        System.out.println(
		//        		"#############################" 
		//        	);			
		// loop on data used in the visit loop
		//		int sz = ((H5O_MyIterData_FindGroupByName)iter_data).iterdata.size();
		//		for (int k = 0; k < sz; k++)
		//		if (sz > 0)
		//		{
		//			int k = sz -1;
		//	        System.out.println(
		//	        		"-------->" + k 
		//	        	);			
		//	        System.out.println(
		//	        		"-------->" + 
		//	        		(((H5O_MyIterData_FindDatasetByName)iter_data).iterdata).get(k).groupFound
		//	        	);			
		//	        System.out.println(
		//	        		"-------->" + 
		//	        		(((H5O_MyIterData_FindDatasetByName)iter_data).iterdata).get(k).link_name
		//	        	);			
		//		}

		return ((H5O_IterCallback_FindGroupByName)iter_cb).idGroupFound;
	}

	/////////////////////////////////////////////////////

	private class MyIterateData_FindDatasetByName {
		public String link_name = null;
		public int link_type = -1;
		public int link_id = -1;

		public boolean datasetFound = false;

		MyIterateData_FindDatasetByName(String name, int type, int id) {
			this.link_name = name;
			this.link_type = type;
			this.link_id   = id;
		}
	}

	private class H5O_MyIterData_FindDatasetByName implements H5O_iterate_t {
		// a List containig data gathered at each call of callback function
		public ArrayList<MyIterateData_FindDatasetByName> iterdata = new ArrayList<MyIterateData_FindDatasetByName>();
		// When search is successful, the group id and info are stored 
		// in last element of this list

		// a function returning search success
		public boolean isDatasetFound()
		{
			if (iterdata.size() == 0)
				return false;

			// check the last iteration only
			boolean retValue = iterdata.get(iterdata.size() - 1).datasetFound;
			return retValue;
		}
	}

	private class H5O_IterCallback_FindDatasetByName implements H5O_iterate_cb {

		public String theNameWeSearch = null;
		public int idDatasetFound = -1;
		public float [] dataset = null;
		public int rank = -1;
		public long[] dims_out;


		public H5O_IterCallback_FindDatasetByName(String theName)
		{
			super();
			theNameWeSearch = theName;
		}

		public int callback(int dset_id, String name, H5O_info_t info, H5O_iterate_t op_data) {

			if ( ((H5O_MyIterData_FindDatasetByName)op_data).isDatasetFound() )
			{
				// there's non need to search further
				// return straight away
				//				System.out.println("------- search stopped");
				// the group id and info are stored in last element of 
				// ((H5O_MyIterData_FindDatasetByName)op_data).iterdata
				return 0;
			}

			MyIterateData_FindDatasetByName id = new MyIterateData_FindDatasetByName(name, info.type, dset_id);
			((H5O_MyIterData_FindDatasetByName)op_data).iterdata.add(id);

			//			System.out.print("/"); /* Print root group in object path */

			//Check if the current object is the root group, and if not print the full path name and type.

			if (name.charAt(0) == '.')         /* Root group, do not print '.' */
			{
				//				System.out.println("  (Group)");
			}
			else if(info.type == HDF5Constants.H5O_TYPE_GROUP )
			{
				//				System.out.println(name + "  (Group)" );
			}
			else if(info.type == HDF5Constants.H5O_TYPE_DATASET)
			{
				// set flag in list of data
				if (
						StringUtils.equals(name, theNameWeSearch) // APACHE COMMONS STRINGUTILS
						//						name.equals(theNameWeSearch)
						)
				{
					int sz = ((H5O_MyIterData_FindDatasetByName)op_data).iterdata.size();

					// set flag
					(((H5O_MyIterData_FindDatasetByName)op_data)
							.iterdata.get(sz - 1)
							).datasetFound = true;

					idDatasetFound = dset_id;

					try {

						int id0 = H5.H5Dopen(dset_id, name, HDF5Constants.H5P_DEFAULT);
						//						System.out.println("_________________________________id0: " + id0);

						long storageSize = H5.H5Dget_storage_size(id0);
						//						System.out.println("_________________________________Storage: " + storageSize);

						dataset = new float[(int) storageSize/4]; // NOTE: 1 float = 4 bytes
						int res = H5.H5Dread_float(
								id0, 
								HDF5Constants.H5T_NATIVE_FLOAT, // mem_type_id 
								HDF5Constants.H5S_ALL, // mem_space_id
								HDF5Constants.H5S_ALL, // file_space_id 
								HDF5Constants.H5P_DEFAULT, // xfer_plist_id
								dataset // buffer
								);

						//						System.out.println(
						//								"_________________________________buff: " + Arrays.toString(buff));

						int dataspace = H5.H5Dget_space(id0);
						rank = H5.H5Sget_simple_extent_ndims(dataspace);

						//						System.out.println(
						//								"---- rank ----> " + rank
						//						);

						if (rank > 0) {
							dims_out = new long[rank];
							int status_n = H5.H5Sget_simple_extent_dims(dataspace, dims_out, null);

							//							System.out.println(
							//									"---- dims_out ----> " + Arrays.toString(dims_out)
							//							);
						}

					} catch (HDF5LibraryException | NullPointerException e) {
						e.printStackTrace();
					}

					//					System.out.println(
					//							"---- FOUND ----> " + name + "\n" +
					//							"---- ID    ----> " + dset_id + "\n" +
					//							"---- Type  ----> " + info.type                    				
					//							);
				}
			}
			else if (info.type == HDF5Constants.H5O_TYPE_NAMED_DATATYPE )
			{
				//				System.out.println(name + "  (Datatype)");
			}
			else
			{
				//				System.out.println(name + "  (Unknown)");
			}

			return 0;
		}
	}

	public int getIdDatasetByName(String datasetFullName) throws HDF5LibraryException, NullPointerException {

		if (_fileH5 == null || _rootGroup == null)
			return -1;

		if (datasetFullName == null || datasetFullName.length() == 0)
			return -1;

		// recursively iterate

		// some useful variables
		//		HObject obj = null;
		int o_id = -1;
		int objType;
		H5O_info_t info;

		// check start from root
		o_id = _rootGroup.open();
		info = H5.H5Oget_info(o_id);
		objType = info.type;

		H5O_iterate_t iter_data = new H5O_MyIterData_FindDatasetByName();
		H5O_iterate_cb iter_cb = new H5O_IterCallback_FindDatasetByName(datasetFullName);

		// recursive visit
		H5.H5Ovisit(
				_fileH5.getFID(), 
				HDF5Constants.H5_INDEX_NAME, 
				HDF5Constants.H5_ITER_NATIVE, 
				iter_cb, 
				iter_data
				);

		float [] dset = ((H5O_IterCallback_FindDatasetByName)iter_cb).dataset;

		int result = ((H5O_IterCallback_FindDatasetByName)iter_cb).idDatasetFound;

		//		System.out.println(
		//				"getIdDatasetByName :: dataset id " + result);		
		return result;
	}

	public double[] getDataset1DFloatByName(String datasetFullName) throws HDF5LibraryException, NullPointerException {

		if (_fileH5 == null || _rootGroup == null)
			return null;

		if (datasetFullName == null || datasetFullName.length() == 0)
			return null;

		// recursively iterate

		// some useful variables
		HObject obj = null;
		int o_id = -1;
		int objType;
		H5O_info_t info;

		// check start from root
		o_id = _rootGroup.open();
		info = H5.H5Oget_info(o_id);
		objType = info.type;

		H5O_iterate_t iter_data = new H5O_MyIterData_FindDatasetByName();
		H5O_iterate_cb iter_cb = new H5O_IterCallback_FindDatasetByName(datasetFullName);

		// recursive visit
		H5.H5Ovisit(
				_fileH5.getFID(), 
				HDF5Constants.H5_INDEX_NAME, 
				HDF5Constants.H5_ITER_NATIVE, 
				iter_cb, 
				iter_data
				);

		if ( ((H5O_IterCallback_FindDatasetByName)iter_cb).dataset != null ) {
			// convert from float[] to double[]
			int sz = (int) ((H5O_IterCallback_FindDatasetByName)iter_cb).dims_out[0];

			if (sz == 0 ) return null;

			double[] result = new double[sz];
			for (int i=0; i<sz; i++) {
				result[i] = (double) ((H5O_IterCallback_FindDatasetByName)iter_cb).dataset[i];
			}
			//			return ((H5O_IterCallback_FindDatasetByName)iter_cb).dataset;
			return result;
		} else
			return null;

	}

	public double[][] getDataset2DFloatByName(String datasetFullName) throws HDF5LibraryException, NullPointerException {

		if (_fileH5 == null || _rootGroup == null)
			return null;

		if (datasetFullName == null || datasetFullName.length() == 0)
			return null;

		// recursively iterate

		// some useful variables
		//		HObject obj = null;
		int o_id = -1;
		int objType;
		H5O_info_t info;

		// check start from root
		o_id = _rootGroup.open();
		info = H5.H5Oget_info(o_id);
		objType = info.type;

		H5O_iterate_t iter_data = new H5O_MyIterData_FindDatasetByName();
		H5O_iterate_cb iter_cb = new H5O_IterCallback_FindDatasetByName(datasetFullName);

		// recursive visit
		H5.H5Ovisit(
				_fileH5.getFID(), 
				HDF5Constants.H5_INDEX_NAME, 
				HDF5Constants.H5_ITER_NATIVE, 
				iter_cb, 
				iter_data
				);

		float [] dset = ((H5O_IterCallback_FindDatasetByName)iter_cb).dataset;

		//		System.out.println(
		//				"getDataset2DFloatByName :: dataset size " + dset.length + "\n"
		//				+ Arrays.toString(dset)
		//				);		
		//		System.out.println(
		//				"getDataset2DFloatByName :: dataset size = "
		//				+ ((H5O_IterCallback_FindDatasetByName)iter_cb).rank
		//				);

		if (
				((H5O_IterCallback_FindDatasetByName)iter_cb).rank == 2
				) {

			int nRows = (int) ((H5O_IterCallback_FindDatasetByName)iter_cb).dims_out[0];
			int nCols = (int) ((H5O_IterCallback_FindDatasetByName)iter_cb).dims_out[1];

			//			System.out.println("2D dataset !!!");
			//			System.out.println(
			//					"Dimensions (rows x columns): "
			//					+ nRows
			//					+ " x "
			//					+ nCols
			//			);

			// From dset, a 1D array, to result, a 2D array
			// http://stackoverflow.com/questions/1817631/iterating-one-dimension-array-as-two-dimension-array
			// http://www.kosbie.net/cmu/fall-08/15-100/handouts/notes-two-dimensional-arrays.html

			double[][] result = new double[nRows][nCols];
			for(int index = 0; index < nRows*nCols; index++) {
				int column = index % nCols;
				int row = (index - column) / nCols;
				result[row][column] = dset[index];
				// System.out.println(row + ", " + column + ": " + dset[index]);
			}			
			return result;
		} else {
			// return a null array float[][]
			// return Collections.emptyList().toArray(new double[0][0]);
			return null;
		}
	}

	/**
	 * 
	 * @param datasetFullName
	 * @return
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public double[][][] getDataset3DFloatByName(String datasetFullName) throws HDF5LibraryException, NullPointerException {

		if (_fileH5 == null || _rootGroup == null)
			return null;

		if (datasetFullName == null || datasetFullName.length() == 0)
			return null;

		// recursively iterate

		// some useful variables
		//		HObject obj = null;
		int o_id = -1;
		int objType;
		H5O_info_t info;

		// check start from root
		o_id = _rootGroup.open();
		info = H5.H5Oget_info(o_id);
		objType = info.type;

		H5O_iterate_t iter_data = new H5O_MyIterData_FindDatasetByName();
		H5O_iterate_cb iter_cb = new H5O_IterCallback_FindDatasetByName(datasetFullName);

		// recursive visit
		H5.H5Ovisit(
				_fileH5.getFID(), 
				HDF5Constants.H5_INDEX_NAME, 
				HDF5Constants.H5_ITER_NATIVE, 
				iter_cb, 
				iter_data
				);

		float [] dset = ((H5O_IterCallback_FindDatasetByName)iter_cb).dataset;

		//		System.out.println(
		//				"getDataset2DFloatByName :: dataset size " + dset.length + "\n"
		//				+ Arrays.toString(dset)
		//				);		
		//		System.out.println(
		//				"getDataset2DFloatByName :: dataset size = "
		//				+ ((H5O_IterCallback_FindDatasetByName)iter_cb).rank
		//				);

		if (((H5O_IterCallback_FindDatasetByName)iter_cb).rank == 3) {

			int nRows  = (int) ((H5O_IterCallback_FindDatasetByName)iter_cb).dims_out[0];
			int nCols  = (int) ((H5O_IterCallback_FindDatasetByName)iter_cb).dims_out[1];
			int nPages = (int) ((H5O_IterCallback_FindDatasetByName)iter_cb).dims_out[2];

			//			System.out.println("3D dataset !!!");
			//			System.out.println(
			//					"Dimensions (rows x columns x pages): "
			//					+ nRows
			//					+ " x "
			//					+ nCols
			//					+ " x "
			//					+ nPages
			//			);

			// From dset, a 1D array, to result, a 3D array
			// http://stackoverflow.com/questions/1817631/iterating-one-dimension-array-as-two-dimension-array
			// http://www.kosbie.net/cmu/fall-08/15-100/handouts/notes-two-dimensional-arrays.html

			double[][][] result = new double[nPages][nRows][nCols];

			int index = -1;
			for(int i = 0; i < nRows; i++) {
				for(int j = 0; j < nCols; j++) {
					for(int k = 0; k < nPages; k++) {
						index++;
						result[k][i][j] = dset[index];
						// System.out.println(i + "," + j + "," + k + ": " + dset[index]);
					}
				}
			}
			//			System.out.println("##################");
			//			for(int k = 0; k < nPages; k++) {
			//				for(int j = 0; j < nCols; j++) {
			//					for(int i = 0; i < nRows; i++) {
			//						System.out.println(i + "," + j + "," + k + ": " + result[i][j][k]);
			//					}
			//				}
			//			}			

			return result;

		} else {
			// return a null array float[][]
			// return Collections.emptyList().toArray(new double[0][0]);
			return null;
		}
	}

	/**
	 * 
	 * @param groupFullName
	 * @return
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public int getDataRankByGroupName(String groupFullName) throws HDF5LibraryException, NullPointerException {

		String datasetFullName = groupFullName + "/data";

		if (_fileH5 == null || _rootGroup == null)
			return -1;

		if (datasetFullName == null || datasetFullName.length() == 0)
			return -1;

		// recursively iterate

		// some useful variables
		//		HObject obj = null;
		int o_id = -1;
		int objType;
		H5O_info_t info;

		// check start from root
		o_id = _rootGroup.open();
		info = H5.H5Oget_info(o_id);
		objType = info.type;

		H5O_iterate_t iter_data = new H5O_MyIterData_FindDatasetByName();
		H5O_iterate_cb iter_cb = new H5O_IterCallback_FindDatasetByName(datasetFullName);

		// recursive visit
		H5.H5Ovisit(
				_fileH5.getFID(), 
				HDF5Constants.H5_INDEX_NAME, 
				HDF5Constants.H5_ITER_NATIVE, 
				iter_cb, 
				iter_data
				);

		//		float [] dset = ((H5O_IterCallback_FindDatasetByName)iter_cb).dataset;

		//		System.out.println(
		//				"getDataset2DFloatByName :: dataset size " + dset.length + "\n"
		//				+ Arrays.toString(dset)
		//				);		
		//		System.out.println(
		//				"getDataset2DFloatByName :: dataset size = "
		//				+ ((H5O_IterCallback_FindDatasetByName)iter_cb).rank
		//				);

		return ((H5O_IterCallback_FindDatasetByName)iter_cb).rank;
	}

	/**
	 * 
	 * @param groupFullName
	 * @return
	 * @throws HDF5LibraryException
	 * @throws NullPointerException
	 */
	public List<Double[]> getVarsByGroupName(String groupFullName) throws HDF5LibraryException, NullPointerException {

		List<Double[]> result = new ArrayList<Double[]>();

		if (_fileH5 == null || _rootGroup == null)
			return null;

		if (groupFullName == null || groupFullName.length() == 0)
			return null;

		List<String> datasetVarnames = new ArrayList<String>();
		datasetVarnames.add("var_0");
		datasetVarnames.add("var_1");
		datasetVarnames.add("var_2");

		for (String varName : datasetVarnames) {

			String var1DFullName = groupFullName + "/" + varName;
			double[] var = null;
			try {
				var = getDataset1DFloatByName(var1DFullName);
			} catch (HDF5LibraryException | NullPointerException e) {
				e.printStackTrace();
			}
			if (var != null) {
				result.add(ArrayUtils.toObject(var));
			}			
		}
		return result;
	}

	/**
	 * 
	 * @param group1DFullName
	 * @param v0
	 * @return
	 */
	public Double interpolate1DFromDataset(String group1DFullName, double v0) {
		return interpolate1DFromDatasetFunction(group1DFullName).value(v0); 
	}

	/**
	 * 
	 * @param group1DFullName
	 * @param v0
	 * @return
	 */
	public MyInterpolatingFunction interpolate1DFromDatasetFunction(String group1DFullName) {

		String dataset1DFullName = group1DFullName + "/data";
		double[] dset1D = null;

		try {
			dset1D = getDataset1DFloatByName(dataset1DFullName);
		} catch (HDF5LibraryException | NullPointerException e) {
			e.printStackTrace();
		} 

		if (dset1D != null) {
			String var01DFullName = group1DFullName + "/var_0";
			double[] var01D = null;
			try {
				var01D = getDataset1DFloatByName(var01DFullName);
			} catch (HDF5LibraryException | NullPointerException e) {
				e.printStackTrace();
			}

			if (var01D != null) {
				MyInterpolatingFunction f = new MyInterpolatingFunction();
				f.interpolateLinear(var01D, dset1D);
				return f;

			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param group2DFullName
	 * @param v0 (y-wise)
	 * @param v1 (x-wise)
	 * @return f(x,y)
	 */
	public Double interpolate2DFromDataset(String group2DFullName, double v0, double v1) {
		return interpolate2DFromDatasetFunction(group2DFullName).value(v1, v0);
	}

	/**
	 * 
	 * @param group2DFullName
	 * @param v0 (y-wise)
	 * @param v1 (x-wise)
	 * @return a spline which represents f(x,y)
	 */
	public MyInterpolatingFunction interpolate2DFromDatasetFunction(String group2DFullName) {

		String dataset2DFullName = group2DFullName + "/data";

		double[][] dset2D = null;
		try {
			dset2D = getDataset2DFloatByName(dataset2DFullName);
		} catch (HDF5LibraryException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (dset2D == null) {
			return null;
		} else {
			String var0_2DFullName = group2DFullName + "/var_0";
			String var1_2DFullName = group2DFullName + "/var_1";
			double[] var0_2D = null;
			double[] var1_2D = null;
			try {
				var0_2D = getDataset1DFloatByName(var0_2DFullName);
				var1_2D = getDataset1DFloatByName(var1_2DFullName);
			} catch (HDF5LibraryException | NullPointerException e) {
				e.printStackTrace();
			} 

			
			MyInterpolatingFunction f = new MyInterpolatingFunction();
			f.interpolateBilinear(var1_2D, var0_2D, dset2D);
			return f;
		}
	}

	// G. Torre
	public MyInterpolatingFunction interpolate2DFromDatasetFunction(String group2DFullName, String dataName, String var0Name, String var1Name) {

		String dataset2DFullName = group2DFullName + "/" + dataName;

		double[][] dset2D = null;
		try {
			dset2D = getDataset2DFloatByName(dataset2DFullName);
		} catch (HDF5LibraryException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (dset2D == null) {
			return null;
		} else {
			String var0_2DFullName = group2DFullName + "/" + var0Name;
			String var1_2DFullName = group2DFullName + "/" + var1Name;
			double[] var0_2D = null;
			double[] var1_2D = null;
			try {
				var0_2D = getDataset1DFloatByName(var0_2DFullName);
				var1_2D = getDataset1DFloatByName(var1_2DFullName);
			} catch (HDF5LibraryException | NullPointerException e) {
				e.printStackTrace();
			} 

			MyInterpolatingFunction f = new MyInterpolatingFunction();
			f.interpolateBilinear(var1_2D, var0_2D, dset2D);
			return f;
		}
	}
	/**
	 * 
	 * @param group3DFullName
	 * @param v0 (z-wise)
	 * @param v1 (y-wise)
	 * @param v2 (x-wise)
	 * @return a spline which represents f(x,y,z)
	 */
	public Double interpolate3DFromDataset(String group3DFullName, double v0, double v1, double v2) {
		return interpolate3DFromDatasetFunction(group3DFullName).value(v2, v1, v0);
	}

	/**
	 * 
	 * @param group3DFullName
	 * @return
	 */
	public MyInterpolatingFunction interpolate3DFromDatasetFunction(String group3DFullName) {

		String dataset3DFullName = group3DFullName + "/data";

		double[][][] dset3D = null;
		try {
			dset3D = getDataset3DFloatByName(dataset3DFullName);
		} catch (HDF5LibraryException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (dset3D == null) {
			return null;

		} else {
			double[] var0 = null;
			double[] var1 = null;
			double[] var2 = null;

			try {
				var0 = getDataset1DFloatByName(group3DFullName + "/var_0");
				var1 = getDataset1DFloatByName(group3DFullName + "/var_1");
				var2 = getDataset1DFloatByName(group3DFullName + "/var_2");

			} catch (HDF5LibraryException | NullPointerException e) {
				e.printStackTrace();
			}

			MyInterpolatingFunction f = new MyInterpolatingFunction();
			f.interpolateTrilinear(var0, var2, var1, dset3D);
			return f;
		}
	}	

	// G. Torre
	/**
	 * In this function we give as input non only the group name but also database and variables name so we could interpolate in a multi-diagram schemes.
	 * @param group3DFullName
	 * @param dataName database used for interpolation (first or second passage)
	 * @param var0Name variables 0 names
	 * @param var1Name variables 1 names
	 * @param var2Name variables 2 names
	 * all input parameters are string
	 * @return 
	 */
	public MyInterpolatingFunction interpolate3DFromDatasetFunction(String group3DFullName,String dataName, String var0Name, String var1Name, String var2Name) {
		String dataset3DFullName = group3DFullName + "/" + dataName;
		double[][][] dset3D = null;
		try {
			dset3D = getDataset3DFloatByName(dataset3DFullName);
		} catch (HDF5LibraryException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (dset3D == null) {
			return null;
		} else {
			String var0_3DFullName = group3DFullName + "/" + var0Name;
			String var1_3DFullName = group3DFullName + "/" + var1Name;
			String var2_3DFullName = group3DFullName + "/" + var2Name;
			double[] var0_3D = null;
			double[] var1_3D = null;
			double[] var2_3D = null;
			try {
				var0_3D = getDataset1DFloatByName(var0_3DFullName);
				var1_3D = getDataset1DFloatByName(var1_3DFullName);
				var2_3D = getDataset1DFloatByName(var2_3DFullName);
			} catch (HDF5LibraryException | NullPointerException e) {
				e.printStackTrace();
			}
			MyInterpolatingFunction f = new MyInterpolatingFunction();
			f.interpolate(var2_3D, var1_3D, var0_3D, dset3D);
			return f;
		}
	}

	// G. Torre
	/**
	 * In this function we give as input group name but also database and variables name,and a list of string.What we do in this function
	 * is similar to the previous function,but we store the interpolating function in an list that we used later
	 * @param group3DFullName
	 * @param dataNamesString this is a list of string that contain all database names for the schemes
	 * @param var0Name variables 0 names
	 * @param var1Name variables 1 names
	 * @param var2Name variables 2 names
	 * @return a list of interpolating function used in the 4D interpolate
	 */
	public List<MyInterpolatingFunction> interpolate3DFromDatasetFunctionFor4DInterpolation(
			String group3DFullName,
			List<String> dataNamesString, 
			String var1Name, String var2Name, String var3Name ) {
		int n = dataNamesString.size();
		List<MyInterpolatingFunction> listArray =  new ArrayList<MyInterpolatingFunction>();
		for (int i = 0 ; i< n; i++ ){
			String dataName = dataNamesString.get(i);
			String dataset3DFullName = group3DFullName + "/" + dataName;
			double[][][] dset3D = null;
			try {
				dset3D = getDataset3DFloatByName(dataset3DFullName);
			} catch (HDF5LibraryException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			if (dset3D == null) {
				return null;
			} else {
				String var3_3DFullName = group3DFullName + "/" + var3Name;
				String var1_3DFullName = group3DFullName + "/" + var1Name;
				String var2_3DFullName = group3DFullName + "/" + var2Name;
				double[] var3_3D = null;
				double[] var1_3D = null;
				double[] var2_3D = null;
				try {
					var1_3D = getDataset1DFloatByName(var1_3DFullName);
					var2_3D = getDataset1DFloatByName(var2_3DFullName);
					var3_3D = getDataset1DFloatByName(var3_3DFullName);
				} catch (HDF5LibraryException | NullPointerException e) {
					e.printStackTrace();
				}
				MyInterpolatingFunction f = new MyInterpolatingFunction();
				f.interpolate(var3_3D, var2_3D, var1_3D, dset3D);
				listArray.add(f); 
			}
		}
		return listArray;
	}

	// G. Torre
	/**
	 * @param group4DFullName names that identify database used
	 * @param dataNamesString this is a list of string that contain all database names for the schemes
	 * @param var0Name variables 0 names
	 * @param var1Name variables 1 names
	 * @param var2Name variables 2 names
	 * @param v3 variables 3 value
	 * @param v2 variables 2 value
	 * @param v1 variables 1 value
	 * @param v0 variables 0 value
	 * @return value from database a 4D database
	 */
	public double interpolate4DFromDatasetFunction(
			String group4DFullName,
			List<String> dataNamesString, String var0Name, 
			String var1Name, String var2Name, String var3Name,double v3, double v2, double v1,double v0  ){ // mettere
		int n = dataNamesString.size();
		List<MyInterpolatingFunction> f =  new ArrayList<MyInterpolatingFunction>();
		f = interpolate3DFromDatasetFunctionFor4DInterpolation(group4DFullName, dataNamesString, var1Name, var2Name, var3Name);
		double[] dset1D = new double[n];
		for  (int i = 0 ; i<n ; i++){
			/*
			 * In this for, we obtain for all extract from a list of the interpolation function (created from var3,var1,var2) 
			 *  with this function we find the value of the result for the value of var0 contained in the string var0Name
			 *  the value that  obtained was stored in an array that we use as a database
			 */
			MyInterpolatingFunction s = f.get(i);
			double a  = s.value(v3, v2, v1);
			dset1D [i]=a;
		}
		String var0_3DFullName = group4DFullName + "/" + var0Name;
		double[] var0_4D = null;
		try {
			var0_4D = getDataset1DFloatByName(var0_3DFullName);
			if (var0_4D != null) {
				/*
				 * In this script we created an interpolation with var0 array obtained from var0Name and the database built in the for
				 */
				MyInterpolatingFunction t = new MyInterpolatingFunction();
				t.interpolate(var0_4D, dset1D);
				double value = t.value(v0);
				return value;
			}	
		}
		catch (HDF5LibraryException | NullPointerException e) {
			e.printStackTrace();
		}
		/*
		 * if the function return this value there are errors
		 */
		return -999999999;
	}

	// G. Torre
	/**
	 * 
	 * @param group3DFullName group name
	 * @param data0NamesString this is a string that contain database names for the schemes
	 * @param data1NamesString this is a string that contain  database names for the schemes
	 * @param var_0_0Name var0 for 1st interpolation
	 * @param var_0_1_Name var1 for 1st interpolation
	 * @param var0_2_Name var2 for 1st interpolation
	 * @param var1_0Name var0 for 2nd interpolation
	 * @param var1_1_Name var1 for 2nd interpolation
	 * @param var0_0 var0_0 value
	 * @param var0_1 var0_1 value
	 * @param var0_2 var0_2 value
	 * @param var1_0 var1_0 value
	 * @return
	 */
	public double interpolate2DFrom3DDatasetFunction(
			String group3DFullName,
			String data0NamesString, String data1NamesString, String var_0_0Name, 
			String var_0_1_Name, String var0_2_Name, String var1_0Name,String var1_1_Name,
			double var0_0, double var0_1, double var0_2, double var1_0){


		String firstDataset3DFullName =  group3DFullName + "/" + data0NamesString;

		String secondDataset3DFullName =  group3DFullName + "/" + data1NamesString;

		double[][][] data0set3D = null;
		double[][][] data1set3D = null;
		double[] var0_2_D = null;
		double[] var0_1_3D = null;
		double[] var_0_0_3D = null;
		double[] var1_0_3D = null;
		double[][][] var1_1_3D = null;
		double[] var1_1_1D = new double [4] ;
		/*
		 * we convert all 3D array in a list of 2D array
		 */
		List<double[][]> dataset_0_2DList =  new ArrayList<double[][]>();//database for 1st interpolation
		List<double[][]> dataset_1_2DList =  new ArrayList<double[][]>();//database for 2nd interpolation
		List<double[][]> var_1_1_2DList =  new ArrayList<double[][]>();//var1_1 list

		String var0_0_3DFullName = group3DFullName + "/" + var_0_0Name;
		String var0_1_3DFullName = group3DFullName + "/" + var_0_1_Name;
		String var0_2_3DFullName = group3DFullName + "/" + var0_2_Name;
		String var1_0_3DFullName = group3DFullName + "/" + var1_0Name;
		String var1_1_3DFullName = group3DFullName + "/" + var1_1_Name;
		try {
			data0set3D = getDataset3DFloatByName(firstDataset3DFullName);
			data1set3D = getDataset3DFloatByName(secondDataset3DFullName);
			var0_1_3D = getDataset1DFloatByName(var0_1_3DFullName);
			var0_2_D = getDataset1DFloatByName(var0_2_3DFullName);
			var_0_0_3D = getDataset1DFloatByName(var0_0_3DFullName);
			var1_0_3D = getDataset1DFloatByName(var1_0_3DFullName);
			var1_1_3D = getDataset3DFloatByName(var1_1_3DFullName);
		} catch (HDF5LibraryException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		dataset_0_2DList  = standaloneutils.MyMathUtils.extractAll2DArraysFrom3DArray(data0set3D);
		dataset_1_2DList  = standaloneutils.MyMathUtils.extractAll2DArraysFrom3DArray(data1set3D);
		var_1_1_2DList = standaloneutils.MyMathUtils.extractAll2DArraysFrom3DArray(var1_1_3D);
		int n = dataset_0_2DList.size();
		double[] dset1D = new double[n];
		for  (int i = 0 ; i<n ; i++){
			/*
			 * with this for we created a 1D database used to find the final value
			 */
			double[][] dataset_0_2D = dataset_0_2DList.get(i);
			double[][] var_1_1_2D = var_1_1_2DList.get(i);
			double[][] dataset_1_2D = dataset_1_2DList.get(i);
			int nRows = var_1_1_2D.length;
			for(int s = 0; s<nRows ;s++){
				/*
				 * from the 2D var1_1 data extract a 1D array contained var1_1 vector used in the interpolation
				 */
				var1_1_1D[s] = var_1_1_2D [s][0]; 
			}
			MyInterpolatingFunction f = new MyInterpolatingFunction();
			f.interpolate(var0_2_D, var0_1_3D, dataset_0_2D);					
			MyInterpolatingFunction f1 = new MyInterpolatingFunction();
			f1.interpolate(var0_2_D, var0_1_3D,var_0_0_3D, data0set3D);
			double var_1_1  = f.value(var0_2, var0_1);
			//			        System.out.println("---------------------------------------------------\n");
			//					System.out.println("Var1_1 = "+var_1_1);	
			//					System.out.println("---------------------------------------------------------");
			if (var_0_0_3D != null || var1_0_3D != null) {
				MyInterpolatingFunction re = new MyInterpolatingFunction();		
				re.interpolate(var1_1_1D, var1_0_3D, dataset_1_2D);
				double value2 = re.value(var_1_1,var1_0);
				dset1D[i] = value2;//value that we added in the 1D Dataset
				//					System.out.println("\n dset = "+value2);
			}
		}
		if (var_0_0_3D != null) {
			MyInterpolatingFunction f2 = new MyInterpolatingFunction();
			f2.interpolate(var_0_0_3D, dset1D);
			double value3 = f2.value(var0_0);
			return value3;
		}
		/*
		 * if the function return this value there are errors
		 */
		return -999999999;

	}


	private void test(String fname, String groupName) {

		int nRows = 0, nCols = 0, nPages = 0, n4 = 0, index = 0;

		// retrieve an instance of H5File
		FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

		if (fileFormat == null) {
			System.err.println("Cannot find HDF5 FileFormat.");
			return;
		}

		// open the file with read access
		FileFormat testFile = null;
		try {
			testFile = fileFormat.createInstance(fname, FileFormat.READ);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (testFile == null) {
			System.err.println("Failed to open file: " + fname);
			return;
		}

		// open the file and retrieve the file structure
		try {
			testFile.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();

		// retrieve the dataset "2D 32-bit integer 20x10"
		Dataset dataset = (Dataset) root.getMemberList().get(0);
		for (int i=0; i<root.getMemberList().size(); i++) {
			if (root.getMemberList().get(i).equals(groupName)) { 
				dataset = (Dataset) root.getMemberList().get(i);
				nRows = (int) dataset.getDims()[0];
				nCols = (int) dataset.getDims()[1];
				nPages = (int) dataset.getDims()[2];
				n4 = (int) dataset.getDims()[3];
			}
		}

		double[] dataRead = new double[0];
		try {
			dataRead = (double[]) dataset.read();
		} catch (OutOfMemoryError | Exception e) {
			e.printStackTrace();
		}

		double[][][] result = new double[nRows][nCols][nPages];

		for (int i=0; i< dataset.getDims().length; i++) {

		}

		for(int i = 0; i < nRows; i++) {
			for(int j = 0; j < nCols; j++) {
				for(int k = 0; k < nPages; k++) {
					index++;
					result[i][j][k] = dataRead[index];
				}
			}
		}

		// print out the data values
		System.out.println("\n\nOriginal Data Values");
		for (int i = 0; i < 20; i++) {
			System.out.print("\n" + dataRead[i * 10]);
			for (int j = 1; j < 10; j++) {
				System.out.print(", " + dataRead[i * 10 + j]);
			}
		}

		// close file resource
		try {
			testFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getDatabaseAbsolutePath() {
		return _fileH5.getAbsolutePath();
	}


} // end of class