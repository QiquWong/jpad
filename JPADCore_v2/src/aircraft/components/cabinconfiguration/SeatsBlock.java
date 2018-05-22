package aircraft.components.cabinconfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.RelativePositionEnum;

/**
 * A seat block stands for a group of seats which can be separated by empty
 * spaces and where some seats could be missing. Each block is separated
 * from another one by an aisle
 * 
 * @author Lorenzo Attanasio
 * @param pos
 *        LEFT, RIGHT or CENTER
 * 
 * @param xStart
 *        the x coordinate (from fuselage nose) where the seats block starts
 * 
 * @param pitch
 * @param width
 * @param distanceFromWall
 * @param breaksMap
 *        This map contains breaks positions (given in row number) as key
 *        and break length as value. The map is 0-based (this means that the
 *        first row is #0). If break position is 1, this means that the
 *        break is after the first row. If break position is -1 then there
 *        are no breaks.
 * 
 * @param rows
 *        total number of rows
 *        
 * @param columns
 *        total number of columns
 *        
 * @param missingSeatRow
 *        an array which holds the row number of eventually missing seats
 * 		  If no seat is missing then missingSeatRow = -1
 * 
 * @param missingSeatColumn
 *        an array which holds the column number of eventually missing seats.
 *        If no seat is missing then missingSeatColumn = -1 
 * 
 * @param type
 *        FIRST, BUSINESS or ECONOMY class
 *        
 */
public class SeatsBlock {

	//-----------------------------------------------------------------------
	// VARIABLE DECLARATION
	private ISeatBlock _theSeatBlockInterface;
	
	//.......................................................................
	// Derived Input
	private List<Integer> _rowList;
	private List<Integer> _columnList;
	private List<Double> _xList;
	private List<Double> _yList;
	private List<ClassTypeEnum> _classList;

	private Amount<Length> _lenghtOverall;
	private int _seatsNumber = 0;
	private List<Integer> _breakPosition;

	private Amount<Length> _breaksLength = Amount.valueOf(0., SI.METER), _xCGblock;

	//-----------------------------------------------------------------------
	// BUILDER
	public SeatsBlock(ISeatBlock theSeatBlockInterface) {

		this._theSeatBlockInterface = theSeatBlockInterface;

		this._rowList = new ArrayList<Integer>();
		this._columnList = new ArrayList<Integer>();
		this._xList = new ArrayList<Double>();
		this._yList = new ArrayList<Double>();
		this._classList = new ArrayList<ClassTypeEnum>();


		// Number of seats
		if (_theSeatBlockInterface.getMissingSeatRow()[0] == -1) {
			setSeatsNumber(theSeatBlockInterface.getRowsNumber() * theSeatBlockInterface.getColumnsNumber());
		} else {
			setSeatsNumber(theSeatBlockInterface.getRowsNumber() * theSeatBlockInterface.getColumnsNumber() 
					- _theSeatBlockInterface.getMissingSeatRow().length);
		}

		// Overall breaks length
		for (Amount<Length> x : _theSeatBlockInterface.getBreaksMap().values()) {
			_breaksLength = _breaksLength.plus(x);
		}

		// Overall block length
		setLenghtOverall((_theSeatBlockInterface.getPitch()
				.times(_theSeatBlockInterface.getRowsNumber()))
				.plus(_breaksLength));

	}
	
	//-----------------------------------------------------------------------
	// METHODS
	/** 
	 * Create a proper data structure to hold a correspondence
	 * between row-column coordinates and x-y coordinates 
	 * 
	 */
	public void add(int row, int column, double x,	double y, ClassTypeEnum type) {

		_rowList.add(row);
		_columnList.add(column);
		_xList.add(x);
		_yList.add(y);
		_classList.add(type);
	}

//	/**
//	 * The method creates an ordered map (_blockMap) based on the data given to the builder.
//	 * This map hold the (x,y) coordinates of each seat. The x coordinate is 0 based, 
//	 * which means that the first seat has x = 0. The method also counts the total
//	 * number of seats in the block.
//	 * 
//	 * @author Lorenzo Attanasio
//	 * 
//	 */
//	public void buildBlockMap() {
//
//		double currentXcoord, currentYcoord;
//		int actualRowNumber;
//
//		// Utility matrix: 0 or 1 is associated to each [row, column] coordinate
//		// if the seat is missing or not
//		if(_theSeatBlockInterface.getRowsNumber() != 0) {
//			_seatsMatrix = MatrixUtils.createRealMatrix(
//					_theSeatBlockInterface.getRowsNumber(),
//					_theSeatBlockInterface.getColumnsNumber()
//					);
//		}
//
//		// Iterate over columns
//		for (int j = 0; j < _theSeatBlockInterface.getColumnsNumber(); j++) {
//
//			currentXcoord = 0.;
//			currentYcoord = 0.;
//			actualRowNumber = _theSeatBlockInterface.getRowsNumber();
//			
//			// Iterate over rows
//			for (int i = 1; i < actualRowNumber; i++) {
//
//				_totalSeats++;
//				_seatsMatrix.setEntry(i, j, 1);
//
//				// If there is a break the next seat coordinate is currentXcoord
//				// + breakLength.
//				if (_breaksMap != null && _breaksMap.containsKey(i)) {
//					currentXcoord = currentXcoord + _breaksMap.get(i).doubleValue(SI.METER);
//				}
//
//				// Check if seat is missing.
//				if (Arrays.asList(_theSeatBlockInterface.getMissingSeatRow()).contains(i)) {
//					currentXcoord = currentXcoord + _theSeatBlockInterface.getPitch().doubleValue(SI.METER);
//					_seatsMatrix.setEntry(i, j, 0);
//					_totalSeats--;
//					actualRowNumber--;
//				}
//
//				if (_theSeatBlockInterface.getPosition() == RelativePositionEnum.LEFT) {
//					currentYcoord = -j * _theSeatBlockInterface.getWidth().doubleValue(SI.METER);
//				} else if (_theSeatBlockInterface.getPosition() == RelativePositionEnum.RIGHT) {
//					currentYcoord = j * _theSeatBlockInterface.getWidth().doubleValue(SI.METER);
//				} else {
//					currentYcoord = (j - _theSeatBlockInterface.getColumnsNumber() / 2) 
//							* _theSeatBlockInterface.getWidth().doubleValue(SI.METER);
//				}
//
//				_minY = 0.0;
//				_maxY = 0.0;
//				
//				// Find the minimum y-coordinate
//				if (currentYcoord < _minY) {
//					_minY = currentYcoord;
//				}
//
//				// Find the maximum y-coordinate
//				if (currentYcoord > _maxY) {
//					_maxY = currentYcoord;
//				}
//
//				_blockXcoordinates.add(currentXcoord);
//				_blockYcoordinates.add(currentYcoord);
//				
//				add(i, j, currentXcoord, currentYcoord, _theSeatBlockInterface.getType());
//
//				currentXcoord = currentXcoord + _theSeatBlockInterface.getPitch().doubleValue(SI.METER);
//			}
//
//			_columnList.add(j);
//
//		}
//	}
//
//	/**
//	 * Calculate the center of gravity of the block given the _blockMap
//	 * 
//	 * @author Lorenzo Attanasio
//	 */
//	public void calculateCG(Aircraft aircraft) {
//
//		buildBlockMap();
//
//		double sum = 0.;
//
//		for (Double x : _blockXcoordinates) {
//			sum = sum + x;
//		}
//
//		_xCGblock = Amount.valueOf(
//				(sum/_totalSeats) +
//				_theSeatBlockInterface.getPitch().doubleValue(SI.METER)/2, 
//				SI.METER);
//
//		calculateTotalMass(aircraft);
//	}
//
//
//	public void calculateTotalMass(Aircraft aircraft) {
//		_totalMass = Amount.valueOf(
//				_totalSeats *
//				aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue(), 
//				SI.KILOGRAM);
//	}
//
//	/** 
//	 * Evaluate center of gravity of all blocks
//	 * 
//	 * @param seatsBlocks
//	 * @return
//	 * 
//	 */
//	public static Amount<Length> calculateTotalCoG(List<SeatsBlock> seatsBlocks) {
//
//		double sumNum = 0., sumDenom = 0.;
//
//		for (SeatsBlock x : seatsBlocks) {
//
//			sumNum = sumNum + 
//					x.get_xCoGblock().getEstimatedValue()*x.get_totalMass().getEstimatedValue();
//
//			sumDenom = sumDenom + 
//					x.get_totalMass().getEstimatedValue();
//		}
//
//		return Amount.valueOf(sumNum/sumDenom, SI.METER);
//
//	}
	
	//-----------------------------------------------------------------------
	// GETTERS AND SETTERS
	public ISeatBlock getTheSeatBlockInterface() {
		return _theSeatBlockInterface;
	}

	public void setTheSeatBlockInterface(ISeatBlock _theSeatBlockInterface) {
		this._theSeatBlockInterface = _theSeatBlockInterface;
	}
	
	public RelativePositionEnum getPosition() {
		return _theSeatBlockInterface.getPosition();
	}

	public void setPosition (RelativePositionEnum position) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setPosition(position).build());
	}
	
	public Amount<Length> getXStart() {
		return _theSeatBlockInterface.getXStart();
	}

	public void setXStart (Amount<Length> xStart) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setXStart(xStart).build());
	}
	
	public Amount<Length> getPitch() {
		return _theSeatBlockInterface.getPitch();
	}
	
	public void setPitch (Amount<Length> pitch) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setPitch(pitch).build());
	}
	
	public Amount<Length> getWidth() {
		return _theSeatBlockInterface.getWidth();
	}
	
	public void setWidth (Amount<Length> width) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setWidth(width).build());
	}
	
	public Amount<Length> getDistanceFromWall() {
		return _theSeatBlockInterface.getDistanceFromWall();
	}
	
	public void setDistanceFromWall (Amount<Length> distanceFromWall) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setDistanceFromWall(distanceFromWall).build());
	}
	
	public Map<Integer, Amount<Length>> getBreaksMap() {
		return _theSeatBlockInterface.getBreaksMap();
	}
	
	public void setBreakMap (Map<Integer, Amount<Length>> breakMap) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).putAllBreaksMap(breakMap).build());
	}
	
	public int getRowsNumber() {
		return _theSeatBlockInterface.getRowsNumber();
	}
	
	public void setRowNumber (int rowNumber) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setRowsNumber(rowNumber).build());
	}
	
	public int getColumnsNumber() {
		return _theSeatBlockInterface.getColumnsNumber();
	}
	
	public void setColumnNumber (int columnNumber) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setColumnsNumber(columnNumber).build());
	}
	
	public Integer[] getMissingSeatRow() {
		return _theSeatBlockInterface.getMissingSeatRow();
	}
	
	public void setMissingSeatRow (Integer[] missingSeatRow) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setMissingSeatRow(missingSeatRow).build());
	}
	
	public ClassTypeEnum getType() {
		return _theSeatBlockInterface.getType();
	}
	
	public void setType (ClassTypeEnum type) {
		setTheSeatBlockInterface(ISeatBlock.Builder.from(_theSeatBlockInterface).setType(type).build());
	}

	public List<Integer> getRowList() {
		return _rowList;
	}

	public void setRowList(List<Integer> _rowList) {
		this._rowList = _rowList;
	}

	public List<Integer> getColumnList() {
		return _columnList;
	}

	public void setColumnList(List<Integer> _columnList) {
		this._columnList = _columnList;
	}

	public List<Double> getXList() {
		return _xList;
	}

	public void setXList(List<Double> _xList) {
		this._xList = _xList;
	}

	public List<Double> getYList() {
		return _yList;
	}

	public void setYList(List<Double> _yList) {
		this._yList = _yList;
	}

	public List<ClassTypeEnum> getClassList() {
		return _classList;
	}

	public void setClassList(List<ClassTypeEnum> _classList) {
		this._classList = _classList;
	}

	public Amount<Length> getLenghtOverall() {
		return _lenghtOverall;
	}

	public void setLenghtOverall(Amount<Length> _lenghtOverall) {
		this._lenghtOverall = _lenghtOverall;
	}

	public int getSeatsNumber() {
		return _seatsNumber;
	}

	public void setSeatsNumber(int _seatsNumber) {
		this._seatsNumber = _seatsNumber;
	}

	public List<Integer> getBreakPosition() {
		return _breakPosition;
	}

	public void setBreakPosition(List<Integer> _breakPosition) {
		this._breakPosition = _breakPosition;
	}

	public Amount<Length> getXCGblock() {
		return _xCGblock;
	}

	public void setXCGblock(Amount<Length> _xCGblock) {
		this._xCGblock = _xCGblock;
	}

}