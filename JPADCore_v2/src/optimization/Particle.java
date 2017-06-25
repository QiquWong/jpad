package optimization;


public class Particle {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Double[] _position;
	private Double[] _velocity;
	private Double _costFunctionValue;
	private Double[] _bestPosition;
	private Double _bestCostFunctionValue;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public Particle(
			Double[] position,
			Double[] velocity,
			Double costFunctionValue,
			Double[] bestPosition,
			Double bestCostFunctionValue
			) {
		this._position = position;
		this._velocity = velocity;
		this._costFunctionValue = costFunctionValue;
		this._bestPosition = bestPosition;
		this._bestCostFunctionValue = bestCostFunctionValue;
	}

	//------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	//------------------------------------------------------------------------------
	public Double[] getPosition() {
		return _position;
	}

	public void setPosition(Double[] _position) {
		this._position = _position;
	}

	public Double[] getVelocity() {
		return _velocity;
	}

	public void setVelocity(Double[] _velocity) {
		this._velocity = _velocity;
	}

	public Double getCostFunctionValue() {
		return _costFunctionValue;
	}

	public void setCostFunctionValue(Double _costFunctionValue) {
		this._costFunctionValue = _costFunctionValue;
	}

	public Double[] getBestPosition() {
		return _bestPosition;
	}

	public void setBestPosition(Double[] _bestPosition) {
		this._bestPosition = _bestPosition;
	}

	public Double getBestCostFunctionValue() {
		return _bestCostFunctionValue;
	}

	public void setBestCostFunctionValue(Double _bestCostFunctionValue) {
		this._bestCostFunctionValue = _bestCostFunctionValue;
	}
	
	
	
}
