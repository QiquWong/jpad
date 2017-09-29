package database.databasefunctions;

import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class FuelFractionDatabaseReader extends DatabaseReader {

	private double[][] _fuelFractionTable;
	
	public FuelFractionDatabaseReader(String databaseFolderPath, String databaseFileName) 
			throws HDF5LibraryException, NullPointerException {
		
		super(databaseFolderPath, databaseFileName);
		
		_fuelFractionTable = database.getDataset2DFloatByName("FuelFractions_Roskam");
	}

	public double[][] getFuelFractionTable() {
		return _fuelFractionTable;
	}
	
	public void setFuelFractionTable(double[][] dataset) {
		this._fuelFractionTable = dataset;
	}
}
