package aircraft.auxiliary;

import java.util.Map;

import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.RelativePositionEnum;

@FreeBuilder
public interface ISeatBlock {

	RelativePositionEnum getPosition();
	
	// x-coordinate (from aircraft nose) of first seat of each block.
	Amount<Length> getXStart();
	
	// Pitch: the distance between two consecutive seats farther pillars.
	Amount<Length> getPitch();
	
	// Seat width
	Amount<Length> getWidth();
	
	// The distance of the nearest seat of the block from the wall
	// This distance is measured at half the height of the seat
	Amount<Length> getDistanceFromWall();
	
	Map<Integer, Amount<Length>> getBreaksMap();
	
	// Number of rows. It is a fixed parameter of the block: we suppose
	// that the user never deletes an entire row of seats but uses a break
	// to insert an empty space.
	int getRowsNumber();
	
	// Columns are given from the leftmost "line" to the rightmost one,
	// looking the aircraft rear to front.
	int getColumnsNumber();
	
	// Missing seat coordinate : [row number , column number]
	Integer[] getMissingSeatRow();
	
	// The class of which the seat block is part
	ClassTypeEnum getType();
	
	class Builder extends ISeatBlock_Builder {
		public Builder() {
			
		}
	}
}
