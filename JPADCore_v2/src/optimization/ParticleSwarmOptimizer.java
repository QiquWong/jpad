package optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;


/**
 * This class implements a simple PSO (Particle Swarm Optimization) algorithm for minimizing an objective function.
 * For more information see http://yarpiz.com/440/ytea101-particle-swarm-optimization-pso-in-matlab-video-tutorial
 * 
 * @author Vittorio Trifari
 *
 */
public class ParticleSwarmOptimizer {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA 
	private int _numberOfDesignVariables;
	private Double[] _designVariablesLowerBound;
	private Double[] _designVariablesUpperBound;
	private Double _convergenceThreshold;
	private int _particlesNumber;
	private Double _kappa;   // = 1;
	private Double _phi1;    // = 2.05;
	private Double _phi2;    // = 2.05;

	//..............................................................................
	// DERIVED INPUT	
	private Double _phi;
	private Double _chi;
	private Double _inertiaCoefficient;
	private Double _individualAccelerationCoefficient;
	private Double _socialAccelerationCoefficient;
    private Double[] _velocityLowerBound; 
    private Double[] _velocityUpperBound; 
	
	//..............................................................................
	// OUTPUT	
	private List<Particle> _population;
	private List<Double> _bestCostsFunctionValueOverIterations;
	private Double[] _bestPosition;
	private Double _globalBestCostsFunctionValue;
	private String _outputFolder; 
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------	
	public ParticleSwarmOptimizer(
			int numberOfDesignVariables,
			Double[] designVariablesLowerBound,
			Double[] designVariablesUpperBound,
			Double convergenceThreshold,
			int particlesNumber,
			Double kappa,
			Double phi1,
			Double phi2,
			String outputFolder
			) {
		
		// Preliminary checks ...
		if(designVariablesLowerBound.length != designVariablesUpperBound.length) {
			System.err.println("DESIGN VARABLES UPPER AND LOWER BOUNDS MUST BE OF THE SAME LENGHT");
			System.exit(1);
			
			if(designVariablesLowerBound.length != numberOfDesignVariables) {
				System.err.println("DESIGN VARABLES UPPER AND LOWER BOUNDS MUST HAVE A NUMBER OF ELEMENTS EQUAL TO THE NUMBER OF DESIGN VARIABLES");
				System.exit(1);
			}
			
		}
		
		// Input assignment ...
		this._numberOfDesignVariables = numberOfDesignVariables;
		this._designVariablesLowerBound = designVariablesLowerBound;
		this._designVariablesUpperBound = designVariablesUpperBound;
		this._convergenceThreshold = convergenceThreshold;
		this._particlesNumber = particlesNumber;
		this._kappa = kappa;
		this._phi1 = phi1;
		this._phi2 =  phi2;
		this._outputFolder = outputFolder;
		
		// Arrays initialization ...
		this._velocityLowerBound = new Double[numberOfDesignVariables];
		this._velocityUpperBound = new Double[numberOfDesignVariables];
		
		//	Constriction Coefficient Function (Clerk & Kennedy, 2002)
		Double phi = phi1 + phi2;
		Double chi = 2*kappa/Math.abs(2-phi-Math.sqrt(Math.pow(phi, 2)-4*phi));
		
		// Derived Input Calculation ...
		this._inertiaCoefficient = chi;
		this._individualAccelerationCoefficient = chi*phi1;
		this._socialAccelerationCoefficient = chi*phi2;
			
		for(int i=0; i<_designVariablesLowerBound.length; i++) {
			this._velocityUpperBound[i] = 0.5*(designVariablesUpperBound[i]-designVariablesLowerBound[i]);
			this._velocityLowerBound[i] = - this._velocityUpperBound[i];
		}
		
		System.out.println("\n\t------------------------------------");
		System.out.println("\tDERIVED INPUT: ");
		System.out.println("\tPhi : " + phi);
		System.out.println("\tChi : " + chi);
		
		System.out.println("\n\tInertia Coefficient (w) : " + _inertiaCoefficient);
		System.out.println("\tIndividual Acceleration Coefficient (c1) : " + _individualAccelerationCoefficient);
		System.out.println("\tSocial Acceleration Coefficient (c2) : " + _socialAccelerationCoefficient);
		
		System.out.println("\n\tVelocity Lower Bound : " + Arrays.toString(_velocityLowerBound));
		System.out.println("\tVelocity Upper Bound : " + Arrays.toString(_velocityUpperBound));
		System.out.println("\t------------------------------------\n");
		
		// Output Lists initialization ...
		this._population = new ArrayList<>();
		this._bestCostsFunctionValueOverIterations = new ArrayList<>();
	}

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	public void optimize() {
		
		System.out.println("\n\t------------------------------------");
		System.out.println("\tRUNNING PSO ... ");
		System.out.println("\t\tINITIALIZING RANDOM POPULATION ... ");
		populationInitialization();
		
		System.out.println("\t\tCHECKING THE INITIAL GLOBAL BEST ... ");
			
		_globalBestCostsFunctionValue = Double.POSITIVE_INFINITY;
		_bestPosition = new Double[_numberOfDesignVariables];

		for(int i=0; i<_population.size(); i++)
			if(_population.get(i).getCostFunctionValue() < _globalBestCostsFunctionValue) {
				_globalBestCostsFunctionValue = _population.get(i).getCostFunctionValue();
				_bestPosition = _population.get(i).getPosition();
			}
		
		int iterationIndex = 0;
		
		System.out.println("\t\tBEGINNING PSO ITERATIONS ... ");
		while(_globalBestCostsFunctionValue >= _convergenceThreshold) {  //FIXME: THIS WORKS ONLY IF THE COST FUNCTION HAVE TO BE MINIMIZED TO ZERO... 
			
			// generator used to create random array needed for the velocity update
			Random randomGenerator = new Random();
			
			_population.stream().forEach(p -> {
				
				Double[] newVelocity = new Double[p.getVelocity().length];
				Double[] newPosition = new Double[p.getPosition().length];
				
				for(int j=0; j<_numberOfDesignVariables; j++) {
					
					//==================================================================
					// Update Velocity
					//==================================================================
					double rand1 = randomGenerator.nextDouble();
					double rand2 = randomGenerator.nextDouble();
					newVelocity[j] = 
							(p.getVelocity()[j]*_inertiaCoefficient)
							+ (_individualAccelerationCoefficient*rand1*(p.getBestPosition()[j]-p.getPosition()[j]))
							+ (_socialAccelerationCoefficient*rand2*(_bestPosition[j]-p.getPosition()[j]));
				
					 // Apply Velocity Limits
					if(newVelocity[j] > _velocityUpperBound[j])
						newVelocity[j] = _velocityUpperBound[j];
					if(newVelocity[j] < _velocityLowerBound[j])
						newVelocity[j] = _velocityLowerBound[j];
					
					//==================================================================
					// Update Position
					//==================================================================
					newPosition[j] = p.getPosition()[j] + newVelocity[j];

					// Apply Lower and Upper Bound Limits
					if(newPosition[j] > _designVariablesUpperBound[j])
						newPosition[j] = _designVariablesUpperBound[j];
					if(newPosition[j] < _designVariablesLowerBound[j])
						newPosition[j] = _designVariablesLowerBound[j];
					
				}
				
				p.setVelocity(newVelocity);
				p.setPosition(newPosition);
				
				//==================================================================
				// Evaluation 
				//==================================================================
				p.setCostFunctionValue(CostFunctions.sphere(p.getPosition()));

				// Update Personal Best
				if(p.getCostFunctionValue() < p.getBestCostFunctionValue()) {
					
					p.setBestCostFunctionValue(p.getCostFunctionValue());
					p.setBestPosition(p.getPosition());
					
					// Update Global Best
					if(p.getBestCostFunctionValue() < _globalBestCostsFunctionValue) {
						
						_globalBestCostsFunctionValue = p.getCostFunctionValue();
						_bestPosition = p.getPosition();
						
					}
				}
			});
			
			//==================================================================
			// Output generation 
			//==================================================================
			// Store the Best Cost Value
			_bestCostsFunctionValueOverIterations.add(_globalBestCostsFunctionValue);
			System.out.println("\t\tIteration " + (iterationIndex+1) + " --> Best Cost:  " + _bestCostsFunctionValueOverIterations.get(iterationIndex));
			
			iterationIndex++;
		}
		
		// Cost Minimization chart
		List<Double[]> xList = new ArrayList<>();
		xList.add(MyArrayUtils.linspaceDouble(0.0, iterationIndex, iterationIndex));
		
		List<Double[]> yList = new ArrayList<>();
		yList.add(MyArrayUtils.convertListOfDoubleToDoubleArray(_bestCostsFunctionValueOverIterations));
		
		List<String> legend = new ArrayList<>();
		legend.add("PSO_Best_Cost_Value_Over_Iterations");
		
		try {
			MyChartToFileUtils.plotLogAxisY(
					xList, 
					yList, 
					"Best cost value over iterations", "Iterations", "Best Cost", 
					0.0, (double) iterationIndex, null, null, 
					"", "", 
					false, legend, 
					_outputFolder, "PSO_Best_Cost_Value_Over_Iterations"
					);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}

	private void populationInitialization() {
		
		System.out.println("\t------------------------------------");
		System.out.println("\tRunning Paticle Swarm Optimization ... ");
		System.out.println("\t------------------------------------\n");
		System.out.println("\tINTIALIZING PARTICLES POPULATION ... \n\n");
		
		for(int i=1; i<_particlesNumber; i++) {
			
			Double[] initialPosition = createRandomPositions(_numberOfDesignVariables);
			
			_population.add(
					new Particle(
							initialPosition,
							MyArrayUtils.zeros(_numberOfDesignVariables),
							CostFunctions.sphere(initialPosition), 
							initialPosition,
							CostFunctions.sphere(initialPosition)
							)
					);
			
		}
		
	}
	
	private Double[] createRandomPositions(int numberOfDesignVariables) {
		
		Random randomGenerator = new Random();
		Double[] positions = new Double[numberOfDesignVariables];
		for(int i=0; i<numberOfDesignVariables; i++)
			positions[i] = _designVariablesLowerBound[i]
						   + (_designVariablesUpperBound[i] - _designVariablesLowerBound[i]) 
						   * randomGenerator.nextDouble();
		
		return positions;
		
	}
	
	
	//------------------------------------------------------------------------------
	// GETTERS & GETTERS:
	//------------------------------------------------------------------------------
	
	public int getNumberOfDesignVariables() {
		return _numberOfDesignVariables;
	}

	public void setNumberOfDesignVariables(int _numberOfDesignVariables) {
		this._numberOfDesignVariables = _numberOfDesignVariables;
	}

	public Double[] getDesignVariablesLowerBound() {
		return _designVariablesLowerBound;
	}

	public void setDesignVariablesLowerBound(Double[] _designVariablesLowerBound) {
		this._designVariablesLowerBound = _designVariablesLowerBound;
	}

	public Double[] getDesignVariablesUpperBound() {
		return _designVariablesUpperBound;
	}

	public void setDesignVariablesUpperBound(Double[] _designVariablesUpperBound) {
		this._designVariablesUpperBound = _designVariablesUpperBound;
	}

	public int getParticlesNumber() {
		return _particlesNumber;
	}

	public void setParticlesNumber(int _particlesNumber) {
		this._particlesNumber = _particlesNumber;
	}

	public Double getKappa() {
		return _kappa;
	}

	public void setKappa(Double _kappa) {
		this._kappa = _kappa;
	}

	public Double getPhi1() {
		return _phi1;
	}

	public void setPhi1(Double _phi1) {
		this._phi1 = _phi1;
	}

	public Double getPhi2() {
		return _phi2;
	}

	public void setPhi2(Double _phi2) {
		this._phi2 = _phi2;
	}

	public Double getPhi() {
		return _phi;
	}

	public void setPhi(Double _phi) {
		this._phi = _phi;
	}

	public Double getChi() {
		return _chi;
	}

	public void setChi(Double _chi) {
		this._chi = _chi;
	}

	public Double getInertiaCoefficient() {
		return _inertiaCoefficient;
	}

	public void setInertiaCoefficient(Double _inertiaCoefficient) {
		this._inertiaCoefficient = _inertiaCoefficient;
	}

	public Double getIndividualAccelerationCoefficient() {
		return _individualAccelerationCoefficient;
	}

	public void setIndividualAccelerationCoefficient(Double _individualAccelerationCoefficient) {
		this._individualAccelerationCoefficient = _individualAccelerationCoefficient;
	}

	public Double getSocialAccelerationCoefficient() {
		return _socialAccelerationCoefficient;
	}

	public void setSocialAccelerationCoefficient(Double _socialAccelerationCoefficient) {
		this._socialAccelerationCoefficient = _socialAccelerationCoefficient;
	}

	public List<Particle> getPopulation() {
		return _population;
	}

	public void setPopulation(List<Particle> _population) {
		this._population = _population;
	}

	public List<Double> getBestCostsFunctionValueOverIterations() {
		return _bestCostsFunctionValueOverIterations;
	}

	public void setBestCostsFunctionValueOverIterations(List<Double> _bestCostsFunctionValueOverIterations) {
		this._bestCostsFunctionValueOverIterations = _bestCostsFunctionValueOverIterations;
	}

	public Double[] getBestPosition() {
		return _bestPosition;
	}

	public void setBestPosition(Double[] _bestPosition) {
		this._bestPosition = _bestPosition;
	}

	public Double getGlobalBestCostsFunctionValue() {
		return _globalBestCostsFunctionValue;
	}

	public void setGlobalBestCostsFunctionValue(Double _globalBestCostsFunctionValue) {
		this._globalBestCostsFunctionValue = _globalBestCostsFunctionValue;
	}

	public Double[] getVelocityLowerBound() {
		return _velocityLowerBound;
	}

	public void setVelocityLowerBound(Double[] _velocityLowerBound) {
		this._velocityLowerBound = _velocityLowerBound;
	}

	public Double[] getVelocityUpperBound() {
		return _velocityUpperBound;
	}

	public void setVelocityUpperBound(Double[] _velocityUpperBound) {
		this._velocityUpperBound = _velocityUpperBound;
	}

	public String getOutputFolder() {
		return _outputFolder;
	}

	public void setOutputFolder(String _outputFolder) {
		this._outputFolder = _outputFolder;
	}

	public Double getConvergenceThreshold() {
		return _convergenceThreshold;
	}

	public void setConvergenceThreshold(Double _convergenceThreshold) {
		this._convergenceThreshold = _convergenceThreshold;
	}
	
}
