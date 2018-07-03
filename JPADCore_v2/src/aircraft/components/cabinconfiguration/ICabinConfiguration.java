package aircraft.components.cabinconfiguration;

import java.util.List;

import javax.annotation.Nullable;
import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.ClassTypeEnum;

@FreeBuilder
public interface ICabinConfiguration {

	String getId();
	int getActualPassengerNumber();
	int getMaximumPassengerNumber();
	int getFlightCrewNumber();
	int getClassesNumber();
	List<ClassTypeEnum> getClassesType();
	int getAislesNumber();
	Amount<Length> getXCoordinatesFirstRow();
//	List<Integer[]> getMissingSeatsRow();
	int getNumberOfBreaksEconomyClass();
	int getNumberOfBreaksBusinessClass();
	int getNumberOfBreaksFirstClass();
	int getNumberOfRowsEconomyClass();
	int getNumberOfRowsBusinessClass();
	int getNumberOfRowsFirstClass();
	
	@Nullable
	Integer[] getNumberOfColumnsEconomyClass();
	@Nullable
	Integer[] getNumberOfColumnsBusinessClass();
	@Nullable
	Integer[] getNumberOfColumnsFirstClass();
	
	Amount<Length> getPitchEconomyClass();
	Amount<Length> getPitchBusinessClass();
	Amount<Length> getPitchFirstClass();
	Amount<Length> getWidthEconomyClass();
	Amount<Length> getWidthBusinessClass();
	Amount<Length> getWidthFirstClass();
	Amount<Length> getDistanceFromWallEconomyClass();
	Amount<Length> getDistanceFromWallBusinessClass();
	Amount<Length> getDistanceFromWallFirstClass();

	class Builder extends ICabinConfiguration_Builder {
		public Builder() {
			
		}
	}
}