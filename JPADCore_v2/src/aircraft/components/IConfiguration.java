package aircraft.components;

import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.SeatsBlock;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.MyArray;

public interface IConfiguration {

	public void updateConfiguration();
	public void calculateDependentVariables();
	public void buildSimpleLayout(Aircraft aircraft);
	public void calculateMass(Aircraft aircraft,
							  OperatingConditions conditions,
							  MethodEnum method
							  );
	public void calculateMassFurnishings(Aircraft aircraft,
										 OperatingConditions conditions,
										 MethodEnum method
										 );
	
	public Integer getNPax();

	public void setNPax(Integer nPax);

	public Integer getNCrew();

	public void setNCrew(Integer nCrew);

	public Integer getMaxPax();
	
	public void setMaxPax(Integer maxPax);

	public Amount<Mass> getMassFurnishings();

	public void getMassFurnishingsAndEquipment(Amount<Mass> massFurnishings);

	public Amount<Mass> getMassFurnishingsAndEquipmentReference();

	public void setMassFurnishingsAndEquipmentReference(Amount<Mass> massReference);

	public Amount<Mass> getMassEstimatedFurnishingsAndEquipment();

	public void setMassEstimatedFurnishingsAndEquipment(Amount<Mass> massMean);

	public Double[] getPercentDifference();

	public void setPercentDifference(Double[] percentDifference);

	public Integer getClassesNumber();

	public void setClassesNumber(Integer classesNumber);

	public Integer getAislesNumber();

	public void setAislesNumber(Integer aislesNumber); 

	public List<Amount<Length>> getPitch(); 

	public void setPitch(List<Amount<Length>> pitch);

	public List<Amount<Length>> getDistanceFromWall();

	public void setDistanceFromWall(List<Amount<Length>> distanceFromWall);

	public List<Integer> getNumberOfBreaks();

	public void setNumberOfBreaks(List<Integer> numberOfBreaks);

	public List<Integer> getNumberOfRows();

	public void setNumberOfRows(List<Integer> numberOfRows);

	public List<Integer[]> getNumberOfColumns();

	public void setNumberOfColumns(List<Integer[]> numberOfColumns);

	public List<Integer[]> getMissingSeatsRow();

	public void setMissingSeatsRow(List<Integer[]> missingSeatsRow);

	public Amount<Length> getPitchFirstClass();

	public void setPitchFirstClass(Amount<Length> pitchFirstClass);

	public Amount<Length> getPitchBusinessClass();

	public void setPitchBusinessClass(Amount<Length> pitchBusinessClass);

	public Amount<Length> getPitchEconomyClass();

	public void setPitchEconomyClass(Amount<Length> pitchEconomyClass);
		
	public List<SeatsBlock> getSeatsBlocksList();

	public void setSeatsBlocksList(List<SeatsBlock> seatsBlocksList);

	public List<Amount<Length>> getWidth();

	public void setWidth(List<Amount<Length>> width);

	public Integer getNumberOfBreaksEconomyClass();

	public void setNumberOfBreaksEconomyClass(Integer numberOfBreaksEconomyClass);

	public Integer getNumberOfBreaksBusinessClass();

	public void setNumberOfBreaksBusinessClass(Integer numberOfBreaksBusinessClass);

	public Integer getNumberOfBreaksFirstClass();
	
	public void setNumberOfBreaksFirstClass(Integer numberOfBreaksFirstClass);

	public Integer[] getNumberOfColumnsEconomyClass();

	public void setNumberOfColumnsEconomyClass(Integer[] numberOfColumnsEconomyClass);

	public Integer[] getNumberOfColumnsBusinessClass();

	public void setNumberOfColumnsBusinessClass(Integer[] numberOfColumnsBusinessClass);

	public Integer[] getNumberOfColumnsFirstClass();

	public void setNumberOfColumnsFirstClass(Integer[] numberOfColumnsFirstClass);

	public Integer getNumberOfRowsEconomyClass();

	public void setNumberOfRowsEconomyClass(Integer numberOfRowsEconomyClass);

	public Integer getNumberOfRowsBusinessClass();

	public void setNumberOfRowsBusinessClass(Integer numberOfRowsBusinessClass);

	public Integer getNumberOfRowsFirstClass();

	public void setNumberOfRowsFirstClass(Integer numberOfRowsFirstClass);

	public Amount<Length> getWidthEconomyClass();

	public void setWidthEconomyClass(Amount<Length> widthEconomyClass);

	public Amount<Length> getWidthBusinessClass();

	public void setWidthBusinessClass(Amount<Length> widthBusinessClass);

	public Amount<Length> getWidthFirstClass();

	public void setWidthFirstClass(Amount<Length> widthFirstClass);

	public Amount<Length> getDistanceFromWallEconomyClass();

	public void setDistanceFromWallEconomyClass(Amount<Length> distanceFromWallEconomyClass);

	public Amount<Length> getDistanceFromWallBusinessClass();

	public void setDistanceFromWallBusinessClass(Amount<Length> distanceFromWallBusinessClass);

	public Amount<Length> getDistanceFromWallFirstClass();

	public void setDistanceFromWallFirstClass(Amount<Length> distanceFromWallFirstClass);

	public Double[] getPitchArr();

	public void setPitchArr(Double[] pitchArr);

	public Double[] getWidthArr();

	public void setWidthArr(Double[] widthArr);

	public Double[] getDistanceFromWallArr();

	public void setDistanceFromWallArr(Double[] distanceFromWallArr);

	public Integer[] getNumberOfRowsArr();

	public void setNumberOfRowsArr(Integer[] numberOfRowsArr);

	public Double[] getNumberOfBreaksArr();

	public void setNumberOfBreaksArr(Double[] numberOfBreaksArr);

	public Integer[] getNumberOfColumnsArr();

	public void setNumberOfColumnsArr(Integer[] numberOfColumnsArr);

	public Amount<Length> getSeatsCoG();
	
	public Amount<Length> getXCoordinateFirstRow();

	public void setXCoordinateFirstRow(Amount<Length> xCoordinateFirstRow);

	public List<Amount<Length>> getSeatsCoGFrontToRearWindow();

	public List<Amount<Length>> getSeatsCoGrearToFrontWindow();

	public List<Amount<Length>> getSeatsCoGFrontToRearAisle();

	public void setSeatsCoGFrontToRearAisle(List<Amount<Length>> seatsCoGFrontToRearAisle);

	public List<Amount<Length>> getSeatsCoGrearToFrontAisle();

	public void setSeatsCoGrearToFrontAisle(List<Amount<Length>> seatsCoGrearToFrontAisle);

	public List<Amount<Length>> getSeatsCoGFrontToRearOther();

	public void setSeatsCoGFrontToRearOther(List<Amount<Length>> seatsCoGFrontToRearOther);

	public List<Amount<Length>> getSeatsCoGrearToFrontOther();

	public void setSeatsCoGrearToFrontOther(List<Amount<Length>> seatsCoGrearToFrontOther);

	public List<Amount<Mass>> getCurrentMassList();

	public void setCurrentMassList(List<Amount<Mass>> currentMassList);

	public List<Amount<Length>> getSeatsCoGFrontToRear();

	public void setSeatsCoGFrontToRear(List<Amount<Length>> seatsCoGFrontToRear);

	public List<Amount<Length>> getSeatsCoGRearToFront();

	public void setSeatsCoGRearToFront(List<Amount<Length>> seatsCoGRearToFront);

	public String getId();

	public MyArray getXLoading();

	public MyArray getYLoading(); 

	public Integer getFlightCrewNumber();

	public void setFlightCrewNumber(Integer flightCrewNumber);

	public Integer getCabinCrewNumber();
	
}
