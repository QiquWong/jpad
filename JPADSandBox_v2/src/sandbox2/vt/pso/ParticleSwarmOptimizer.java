package sandbox2.vt.pso;

import java.util.ArrayList;
import java.util.List;

import standaloneutils.MyInterpolatingFunction;

/**
 * This class implements a simple PSO (Particle Swarm Optimization) algorithm.
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
	private Double _designVariablesLowerBound;
	private Double _designVariablesUpperBound;
	private int _maximumNumberOfIteration;
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
    private Double _velocityLowerBound; 
    private Double _velocityUpperBound; 
	
	//..............................................................................
	// OUTPUT	
	private List<Particle> _population;
	private List<Double> _bestCostsFunctionValueOverIterations;
	private Double _bestPosition;
	private Double _globalBestCostsFunctionValue;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------	
	public ParticleSwarmOptimizer(
			int numberOfDesignVariables,
			Double designVariablesLowerBound,
			Double designVariablesUpperBound,
			int maximumNumberOfIteration,
			int particlesNumber,
			Double kappa,
			Double phi1,
			Double phi2
			) {
		
		// Input assignment ...
		this._numberOfDesignVariables = numberOfDesignVariables;
		this._designVariablesLowerBound = designVariablesLowerBound;
		this._designVariablesUpperBound = designVariablesUpperBound;
		this._maximumNumberOfIteration = maximumNumberOfIteration;
		this._particlesNumber = particlesNumber;
		this._kappa = kappa;
		this._phi1 = phi1;
		this._phi2 =  phi2;
		
		//	Constriction Coefficient Function (Clerk & Kennedy, 2002)
		Double phi = phi1 + phi2;
		Double chi = 2*kappa/Math.abs(2-phi-Math.sqrt(Math.pow(phi, 2)-4*phi));
		
		// Derived Input Calculation ...
		this._inertiaCoefficient = chi;
		this._individualAccelerationCoefficient = chi*phi1;
		this._socialAccelerationCoefficient = chi*phi2;
		this._velocityUpperBound = 0.2*(designVariablesUpperBound-designVariablesLowerBound);
		this._velocityLowerBound = - this._velocityUpperBound;
		
		System.out.println("\n\t------------------------------------");
		System.out.println("\tDERIVED INPUT: ");
		System.out.println("\tPhi : " + phi);
		System.out.println("\tChi : " + chi);
		
		System.out.println("\n\tInertia Coefficient (w) : " + _inertiaCoefficient);
		System.out.println("\tIndividual Acceleration Coefficient (c1) : " + _individualAccelerationCoefficient);
		System.out.println("\tSocial Acceleration Coefficient (c2) : " + _socialAccelerationCoefficient);
		
		System.out.println("\n\tVelocity Lower Bound : " + _velocityLowerBound);
		System.out.println("\tVelocity Upper Bound : " + _velocityUpperBound);
		System.out.println("\t------------------------------------\n");
		
		// Output Lists initialization ...
		this._population = new ArrayList<>();
		this._bestCostsFunctionValueOverIterations = new ArrayList<>();
	}

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	public void optimize() {
		
		populationInitialization();
		
		
	}
	
	private void populationInitialization() {
		
		System.out.println("\t------------------------------------");
		System.out.println("\tRunning Paticle Swarm Optimization ... ");
		System.out.println("\t------------------------------------\n");
		System.out.println("\tINTIALIZING PARTICLES POPULATION ... ");
		
		for(int i=1; i<_particlesNumber; i++)
			_population.add(Particle.Builder.build());
		
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

	public Double getDesignVariablesLowerBound() {
		return _designVariablesLowerBound;
	}

	public void setDesignVariablesLowerBound(Double _designVariablesLowerBound) {
		this._designVariablesLowerBound = _designVariablesLowerBound;
	}

	public Double getDesignVariablesUpperBound() {
		return _designVariablesUpperBound;
	}

	public void setDesignVariablesUpperBound(Double _designVariablesUpperBound) {
		this._designVariablesUpperBound = _designVariablesUpperBound;
	}

	public int getMaximumNumberOfIteration() {
		return _maximumNumberOfIteration;
	}

	public void setMaximumNumberOfIteration(int _maximumNumberOfIteration) {
		this._maximumNumberOfIteration = _maximumNumberOfIteration;
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

	public Double getBestPosition() {
		return _bestPosition;
	}

	public void setBestPosition(Double _bestPosition) {
		this._bestPosition = _bestPosition;
	}

	public Double getGlobalBestCostsFunctionValue() {
		return _globalBestCostsFunctionValue;
	}

	public void setGlobalBestCostsFunctionValue(Double _globalBestCostsFunctionValue) {
		this._globalBestCostsFunctionValue = _globalBestCostsFunctionValue;
	}

	public Double getVelocityLowerBound() {
		return _velocityLowerBound;
	}

	public void setVelocityLowerBound(Double _velocityLowerBound) {
		this._velocityLowerBound = _velocityLowerBound;
	}

	public Double getVelocityUpperBound() {
		return _velocityUpperBound;
	}

	public void setVelocityUpperBound(Double _velocityUpperBound) {
		this._velocityUpperBound = _velocityUpperBound;
	}
	
}
