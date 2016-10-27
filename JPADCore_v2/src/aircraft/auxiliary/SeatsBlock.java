package aircraft.auxiliary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.RelativePositionEnum;

public class SeatsBlock {

	private Amount<Length> _xCoordinate,
						   _lenghtOverall,
						   _width,
						   _pitch,
						   _distanceFromWall;

	private Integer _aisleNumber,
					_breaksNumber,
					_seatsNumber,
					_rowNumber,
					_columns,
					_totalSeats = 0;

	private Integer[] _missingSeatRow;
	private Integer[] _missingSeatColumn;
	private List<Integer> _breakPosition;
	private Aircraft _theAircraft;

	private Map<Integer, Amount<Length>> _breaksMap = new HashMap<Integer, Amount<Length>>();
	private Map<Integer, Integer> _currentSeat = new HashMap<Integer, Integer>();
	private Map<Double, Double> _currentCoG = new HashMap<Double, Double>();
	private Map<Double, Double> _blockMap = new HashMap<Double, Double>();
	private List<Double> _blockXcoordinates = new ArrayList<Double>();
	private List<Double> _blockYcoordinates = new ArrayList<Double>();
	//	private Triplet _seatsMap;
	private Amount<Length> _breaksLength = Amount.valueOf(0., SI.METER), _xCoGblock;
	private RelativePositionEnum _position;
	private List<Amount<Mass>> _currentMass = new ArrayList<Amount<Mass>>(); 
	private Amount<Mass> _totalMass;
	private Double _xCoG = 0., _yCoG = 0.;
	private Double[][] _blockMatrix;
	private RealMatrix _seatsMatrix;
	private RowColumnCoordinate _rowColumnCoordinate = new RowColumnCoordinate();

	private ClassTypeEnum _type;

	private double _minY = 0.;
	private double _maxY = 0.;


	public SeatsBlock() {

	}

	/** 
	 * Create a proper data structure to hold a correspondence
	 * between row-column coordinates and x-y coordinates 
	 * 
	 * @author Lorenzo Attanasio
	 *
	 */
	public class RowColumnCoordinate {

		private List<Integer> _column = new ArrayList<Integer>();
		private List<Integer> _rowList = new ArrayList<Integer>();
		private List<Integer> _columnList = new ArrayList<Integer>();
		private List<Double> _xList = new ArrayList<Double>();
		private List<Double> _yList = new ArrayList<Double>();
		private List<ClassTypeEnum> _classList = new ArrayList<ClassTypeEnum>();

		private RowColumnCoordinate() {

		}

		public void add(
				int row, 
				int column, 
				double x, 
				double y,
				ClassTypeEnum type) {

			_rowList.add(row);
			_columnList.add(column);
			_xList.add(x);
			_yList.add(y);
			_classList.add(type);
		}

		public void setColumn(int j) {
			_column.add(j);
		}
		
		@SuppressWarnings("unused")
		public List<Double> getXList() {
			return _xList;
		}
		
	}

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
	public void createSeatsBlock(
			RelativePositionEnum pos,
			Amount<Length> xStart,
			Amount<Length> pitch,
			Amount<Length> width,
			Amount<Length> distanceFromWall,
			Map<Integer, Amount<Length>> breaksMap,
			Integer rows,
			Integer columns,
			Integer[] missingSeatRow,
			ClassTypeEnum type) {

		_position = pos;

		// x-coordinate (from aircraft nose) of first seat of each block.
		// We need _aisleNumber + 1 coordinates
		_xCoordinate = xStart;

		// Number of rows. It is a fixed parameter of the block: we suppose
		// that the user never deletes an entire row of seats but uses a break
		// to insert an empty space.
		_rowNumber = rows;

		// Missing seat coordinate : [row number , column number]
		_missingSeatRow = missingSeatRow;

		// Pitch: the distance between two consecutive seats farther pillars.
		_pitch = pitch;

		// Seat width
		_width = width;

		// The distance of the nearest seat of the block from the wall
		// This distance is measured at half the height of the seat
		_distanceFromWall = distanceFromWall;

		// Columns are given from the leftmost "line" to the rightmost one,
		// looking the aircraft rear to front.
		_columns = columns;

		// Number of seats
		if (_missingSeatRow[0] == -1) {
			_seatsNumber = rows * columns;
		} else {
			_seatsNumber = rows * columns - _missingSeatRow.length;
		}

		_breaksMap = breaksMap;

		// Overall breaks length
		for (Amount<Length> x : _breaksMap.values()) {
			_breaksLength = _breaksLength.plus(x);
		}

		// Overall block length
		_lenghtOverall = (_pitch.times(_rowNumber)).plus(_breaksLength);

		_type = type;
	}

	public void createSimpleSeatsBlock(
			RelativePositionEnum pos,
			Amount<Length> xStart,
			Amount<Length> pitch,
			Amount<Length> width,
			Integer rows,
			Integer columns,
			ClassTypeEnum type) {

		createSeatsBlock(
				pos,
				xStart,
				pitch,
				width,
				width.times(0.15),
				null,
				rows,
				columns,
				new Integer[] { -1},
				type);
	}

	/**
	 * The method creates an ordered map (_blockMap) based on the data given in
	 * createSeatsBlock. This map hold the (x,y) coordinates of each seat. The x
	 * coordinate is 0 based, which means that the first seat has x = 0. The
	 * method also counts the total number of seats in the block.
	 * 
	 * @author Lorenzo Attanasio
	 * 
	 */
	public void buildBlockMap() {

		double currentXcoord, currentYcoord;
		int actualRowNumber;

		// Utility matrix: 0 or 1 is associated to each [row, column] coordinate
		// if the seat is missing or not
		if(_rowNumber!=0) {
			_seatsMatrix = MatrixUtils.createRealMatrix(_rowNumber, _columns);
		}

		// Iterate over columns
		for (int j = 0; j < _columns; j++) {

			currentXcoord = 0.;
			currentYcoord = 0.;
			actualRowNumber = _rowNumber;
			
			// Iterate over rows
			for (int i = 1; i < actualRowNumber; i++) {

				_totalSeats++;
				_seatsMatrix.setEntry(i, j, 1);

				// If there is a break the next seat coordinate is currentXcoord
				// + breakLength.
				if (_breaksMap != null && _breaksMap.containsKey(i)) {
					currentXcoord = currentXcoord + _breaksMap.get(i).getEstimatedValue();
				}

				// Check if seat is missing.
				if (Arrays.asList(_missingSeatRow).contains(i)) {
					currentXcoord = currentXcoord + _pitch.getEstimatedValue();
					_seatsMatrix.setEntry(i, j, 0);
					_totalSeats--;
					actualRowNumber--;
				}

				if (_position == RelativePositionEnum.LEFT) {
					currentYcoord = -j * _width.getEstimatedValue();
				} else if (_position == RelativePositionEnum.RIGHT) {
					currentYcoord = j * _width.getEstimatedValue();
				} else {
					currentYcoord = (j - _columns / 2) * _width.getEstimatedValue();
				}

				// Find the minimum y-coordinate
				if (currentYcoord < _minY) {
					_minY = currentYcoord;
				}

				// Find the maximum y-coordinate
				if (currentYcoord > _maxY) {
					_maxY = currentYcoord;
				}

				_blockXcoordinates.add(currentXcoord);
				_blockYcoordinates.add(currentYcoord);
				_rowColumnCoordinate.add(i, j, currentXcoord, currentYcoord, _type);

				currentXcoord = currentXcoord + _pitch.getEstimatedValue();
			}

			_rowColumnCoordinate.setColumn(j);

		}
	}


	/**
	 * Calculate the center of gravity of the block given the _blockMap
	 * 
	 * @author Lorenzo Attanasio
	 */
	public void calculateCoG(Aircraft aircraft) {

		buildBlockMap();

		double sum = 0.;

		for (Double x : _blockXcoordinates) {
			sum = sum + x;
		}

		_xCoGblock = Amount.valueOf(
				(sum/_totalSeats) +
				_pitch.getEstimatedValue()/2, 
				SI.METER);

		calculateTotalMass(aircraft);
	}


	public void calculateTotalMass(Aircraft aircraft) {
		_totalMass = Amount.valueOf(
				_totalSeats *
				aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue(), 
				SI.KILOGRAM);
	}

	/** 
	 * Evaluate center of gravity of all blocks
	 * 
	 * @param seatsBlocks
	 * @return
	 * 
	 */
	public static Amount<Length> calculateTotalCoG(List<SeatsBlock> seatsBlocks) {

		double sumNum = 0., sumDenom = 0.;

		for (SeatsBlock x : seatsBlocks) {

			sumNum = sumNum + 
					x.get_xCoGblock().getEstimatedValue()*x.get_totalMass().getEstimatedValue();

			sumDenom = sumDenom + 
					x.get_totalMass().getEstimatedValue();
		}

		return Amount.valueOf(sumNum/sumDenom, SI.METER);

	}


	/** 
	 * Evaluate center of gravity variation during boarding procedure
	 * 
	 * @param seatsBlocks
	 * @return
	 * 
	 */
	public static CGboarding calculateCoGboarding(List<SeatsBlock> seatsBlocks, Aircraft aircraft) {

		double sumFtoR = aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF().doubleValue(SI.METER)*
				aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().getEstimatedValue(),

				sumRtoF = aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF().doubleValue(SI.METER)*
				aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().getEstimatedValue();

		double currentMass, mult, emptyColumns = 0.;
		boolean window = false, aisle = false, other = false;

		CGboarding cg = new CGboarding();

		SeatsBlock x = new SeatsBlock();
		currentMass = aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().getEstimatedValue();
		window = false; aisle = false; other = false;

		///////////////////
		// Front to rear
		///////////////////
		for (int k = 0; k < seatsBlocks.size(); k++) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			for (int j=0; j < x._columns; j++) {

				// Check if the seat is near the window
				if ((window == false && aisle == false && other == false) 
						&& ((x._position.equals(RelativePositionEnum.RIGHT) 
								&& x._rowColumnCoordinate._column.get(j).equals(x._columns-1)) |
								(x._position.equals(RelativePositionEnum.LEFT) && x._rowColumnCoordinate._column.get(j).equals(0)))) {

					for (int i = 0; i < x._rowNumber-1; i++) {

						//						System.out.println("FRwindow-----" + currentMass);
						sumFtoR += (x._rowColumnCoordinate._xList.get(i) + x._xCoordinate.getEstimatedValue() + x._pitch.getEstimatedValue()/2)*
								2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

						currentMass += 2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();
						cg.getCurrentMassList().add(Amount.valueOf(currentMass, SI.KILOGRAM));

						cg.getCurrentXCoGfrontToRearWindow().add(
								Amount.valueOf(
										(sumFtoR/currentMass)
										, SI.METER));
					}
					window = true;
					break;
				}

				// Check if the seat is near the aisle
				if ((window == true && other == false) && ((x._position == RelativePositionEnum.RIGHT && x._rowColumnCoordinate._column.get(j).equals(0)) |
						(x._position == RelativePositionEnum.LEFT && x._rowColumnCoordinate._column.get(j).equals(x._columns-1)) |
						(x._position == RelativePositionEnum.CENTER && x._rowColumnCoordinate._column.get(j).equals(0)) |
						(x._position == RelativePositionEnum.CENTER && x._rowColumnCoordinate._column.get(j).equals(x._columns-1)))) {

					// If there are two aisles the loop has to fill 4 columns,
					// otherwise it has to fill 2 columns
					if (aircraft.getCabinConfiguration().getAislesNumber() > 1) {
						mult = 4.;
					} else {
						mult = 2.;
					}

					for (int i = 0; i < x._rowNumber-1; i++) {

						//						System.out.println("FRaisle-----" + currentMass);
						sumFtoR += (x._rowColumnCoordinate._xList.get(i) + x._xCoordinate.getEstimatedValue() + x._pitch.getEstimatedValue()/2)*
								mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

						currentMass += mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();
						cg.getCurrentMassList().add(Amount.valueOf(currentMass, SI.KILOGRAM));

						cg.getCurrentXCoGfrontToRearAisle().add(
								Amount.valueOf(
										(sumFtoR/currentMass)
										, SI.METER));
					}
					aisle = true;
					break;
				}
			}
		}


		for(int k = 0; k < seatsBlocks.size(); k++) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			if (x._columns > 2) {

				/*
				 * Total number of columns still empty for a single block. 
				 * There are two possible cases:
				 * 1) the block is on the side, so two columns have been
				 * taken (window and aisle);
				 * 2) the block is a central one. Still two columns have
				 * been taken which are the ones near the aisle
				 */
				emptyColumns = emptyColumns + x._columns - 2;

				for (int i = 0; i < x._rowNumber-1; i++) {

					sumFtoR += (x._rowColumnCoordinate._xList.get(i) + x._xCoordinate.getEstimatedValue() + x._pitch.getEstimatedValue()/2)*
							emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

					currentMass += emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();
					cg.getCurrentMassList().add(Amount.valueOf(currentMass, SI.KILOGRAM));

					cg.getCurrentXCoGfrontToRearOther().add(
							Amount.valueOf(
									(sumFtoR/currentMass)
									, SI.METER));
				}
				break;
			}
		}

		currentMass = aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass().getEstimatedValue();
		window = false; aisle = false; other = false;

		///////////////////
		// Rear to front
		///////////////////
		for (int k = seatsBlocks.size()-1; k >= 0; k--) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			for (int j=0; j < x._columns; j++) {

				// Check if the seat is near the window
				if ((window == false && aisle == false && other == false) && ((x._position.equals(RelativePositionEnum.RIGHT) && x._rowColumnCoordinate._column.get(j).equals(x._columns-1)) |
						(x._position.equals(RelativePositionEnum.LEFT) && x._rowColumnCoordinate._column.get(j).equals(0)))) {

					for (int i = x._rowNumber-2; i >= 0; i--) {

						sumRtoF += (x._rowColumnCoordinate._xList.get(i) + x._xCoordinate.getEstimatedValue() + x._pitch.getEstimatedValue()/2)*
								2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

						//						System.out.println("RFwindow-----" + currentMass);
						currentMass += 2*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

						cg.getCurrentXCoGrearToFrontWindow().add(
								Amount.valueOf(
										(sumRtoF/currentMass)
										, SI.METER));
					}
					window = true;
					break;
				}

				// Check if the seat is near the aisle
				if ((window == true && other == false) && ((x._position == RelativePositionEnum.RIGHT && x._rowColumnCoordinate._column.get(j).equals(0)) |
						(x._position == RelativePositionEnum.LEFT && x._rowColumnCoordinate._column.get(j).equals(x._columns-1)) |
						(x._position == RelativePositionEnum.CENTER && x._rowColumnCoordinate._column.get(j).equals(0)) |
						(x._position == RelativePositionEnum.CENTER && x._rowColumnCoordinate._column.get(j).equals(x._columns-1)))) {

					// If there are the aisles the loop has to fill 4 columns,
					// otherwise it has to fill 2 columns
					if (aircraft.getCabinConfiguration().getAislesNumber() > 1) {
						mult = 4.;
					} else {
						mult = 2.;
					}

					for (int i = x._rowNumber-2; i >= 0; i--){

						//						System.out.println("RFailse-----" + currentMass);
						sumRtoF += ((x._rowColumnCoordinate._xList.get(i) + x._xCoordinate.getEstimatedValue() + x._pitch.getEstimatedValue()/2)*
								mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue());

						currentMass += mult*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

						cg.currentXCoGrearToFrontAisle.add(
								Amount.valueOf(
										(sumRtoF/currentMass)
										, SI.METER));
					}
					aisle = true;
					break;
				}
			}
		}

		for (int k = seatsBlocks.size()-1; k >= 0; k--) {

			x = seatsBlocks.get(k);
			emptyColumns = 0.;

			if (x._columns > 2) {

				/*
				 * Total number of columns still empty for a single block. 
				 * There are two possible cases:
				 * 1) the block is on the side, so two columns have been
				 * taken (window and aisle);
				 * 2) the block is a central one. Still two columns have
				 * been taken which are the ones near the aisle
				 */
				emptyColumns = x._columns - 2;

				for (int i = x._rowNumber - 2; i >= 0 ; i--) {

					sumRtoF += (x._rowColumnCoordinate._xList.get(i) + x._xCoordinate.getEstimatedValue() + x._pitch.getEstimatedValue()/2)*
							emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

					currentMass += emptyColumns*aircraft.getTheAnalysisManager().getTheBalance().getPassengersSingleMass().getEstimatedValue();

					cg.currentXCoGrearToFrontOther.add(
							Amount.valueOf(
									(sumRtoF/currentMass)
									, SI.METER));
				}
				break;
			}
		}

		return cg;

	}

	public static class CGboarding {

		private List<Amount<Mass>> currentMassList = new ArrayList<Amount<Mass>>();
		private List<Amount<Length>> currentXCoGfrontToRearWindow = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> currentXCoGrearToFrontWindow = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> currentXCoGfrontToRearAisle = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> currentXCoGrearToFrontAisle = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> currentXCoGfrontToRearOther = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> currentXCoGrearToFrontOther = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> currentXCoGfrontToRear = new ArrayList<Amount<Length>>();
		private List<Amount<Length>> currentXCoGrearToFront = new ArrayList<Amount<Length>>();

		public CGboarding() { }

		public List<Amount<Mass>> getCurrentMassList() {
			return currentMassList;
		}

		public void setCurrentMassList(List<Amount<Mass>> currentMassList) {
			this.currentMassList = currentMassList;
		}

		public List<Amount<Length>> getCurrentXCoGfrontToRearWindow() {
			return currentXCoGfrontToRearWindow;
		}

		public void setCurrentXCoGfrontToRearWindow(List<Amount<Length>> currentXCoGfrontToRearWindow) {
			this.currentXCoGfrontToRearWindow = currentXCoGfrontToRearWindow;
		}

		public List<Amount<Length>> getCurrentXCoGrearToFrontWindow() {
			return currentXCoGrearToFrontWindow;
		}

		public void setCurrentXCoGrearToFrontWindow(List<Amount<Length>> currentXCoGrearToFrontWindow) {
			this.currentXCoGrearToFrontWindow = currentXCoGrearToFrontWindow;
		}

		public List<Amount<Length>> getCurrentXCoGfrontToRearAisle() {
			return currentXCoGfrontToRearAisle;
		}

		public void setCurrentXCoGfrontToRearAisle(List<Amount<Length>> currentXCoGfrontToRearAisle) {
			this.currentXCoGfrontToRearAisle = currentXCoGfrontToRearAisle;
		}

		public List<Amount<Length>> getCurrentXCoGrearToFrontAisle() {
			return currentXCoGrearToFrontAisle;
		}

		public void setCurrentXCoGrearToFrontAisle(List<Amount<Length>> currentXCoGrearToFrontAisle) {
			this.currentXCoGrearToFrontAisle = currentXCoGrearToFrontAisle;
		}

		public List<Amount<Length>> getCurrentXCoGfrontToRearOther() {
			return currentXCoGfrontToRearOther;
		}

		public void setCurrentXCoGfrontToRearOther(List<Amount<Length>> currentXCoGfrontToRearOther) {
			this.currentXCoGfrontToRearOther = currentXCoGfrontToRearOther;
		}

		public List<Amount<Length>> getCurrentXCoGrearToFrontOther() {
			return currentXCoGrearToFrontOther;
		}

		public void setCurrentXCoGrearToFrontOther(List<Amount<Length>> currentXCoGrearToFrontOther) {
			this.currentXCoGrearToFrontOther = currentXCoGrearToFrontOther;
		}

		public List<Amount<Length>> getCurrentXCoGfrontToRear() {

			currentXCoGfrontToRear.addAll(currentXCoGfrontToRearWindow);
			currentXCoGfrontToRear.addAll(currentXCoGfrontToRearAisle);
			currentXCoGfrontToRear.addAll(currentXCoGfrontToRearOther);
			return currentXCoGfrontToRear;
		}

		public void setCurrentXCoGfrontToRear(List<Amount<Length>> currentXCoGComplete) {
			this.currentXCoGfrontToRear = currentXCoGComplete;
		}

		public List<Amount<Length>> getCurrentXCoGrearToFront() {

			currentXCoGrearToFront.addAll(currentXCoGrearToFrontWindow);
			currentXCoGrearToFront.addAll(currentXCoGrearToFrontAisle);
			currentXCoGrearToFront.addAll(currentXCoGrearToFrontOther);
			return currentXCoGrearToFront;
		}

		public void setCurrentXCoGrearToFront(List<Amount<Length>> currentXCoGrearToFront) {
			this.currentXCoGrearToFront = currentXCoGrearToFront;
		}

	}

	public Map<Double, Double> get_currentCoG() {
		return _currentCoG;
	}

	public int get_totalSeats() {
		return _totalSeats;
	}

	public Amount<Mass> get_totalMass() {
		return _totalMass;
	}

	public Amount<Length> get_xCoGblock() {
		return _xCoGblock;
	}

	public Amount<Length> get_lenghtOverall() {
		return _lenghtOverall;
	}

	public ClassTypeEnum get_type() {
		return _type;
	}

	public void set_type(ClassTypeEnum _type) {
		this._type = _type;
	}

	public List<Double> get_blockXcoordinates() {
		return _blockXcoordinates;
	}

	public List<Double> get_blockYcoordinates() {
		return _blockYcoordinates;
	}

	public RowColumnCoordinate get_rowColumnCoordinate() {
		return _rowColumnCoordinate;
	}

	public int get_columns() {
		return _columns;
	}

	public void set_columns(int _columns) {
		this._columns = _columns;
	}

}