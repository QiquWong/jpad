package database.databasefunctions;

import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class FuelFractionDatabaseReader extends DatabaseReader {

	public FuelFractionDatabaseReader(String databaseFolderPath, String databaseFileName) {
		
		super(databaseFolderPath, databaseFileName);
	}

	public double[][] getFuelFractionTable(String datasetFileName)
			throws HDF5LibraryException, NullPointerException{
		
		double[][] fuelFractionTable= database.getDataset2DFloatByName(datasetFileName);
		return fuelFractionTable;
	}
}
