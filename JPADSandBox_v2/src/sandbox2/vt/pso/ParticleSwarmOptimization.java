package sandbox2.vt.pso;

/**
 * Example from http://yarpiz.com/440/ytea101-particle-swarm-optimization-pso-in-matlab-video-tutorial
 * Cost function ==> Shpere ==> sum(x_i^2)
 * @author Vittorio Trifari
 *
 */
public class ParticleSwarmOptimization {

	public static void main(String[] args) {

		System.out.println("\t------------------------------------");
		System.out.println("\tParticle Swarm Optimization :: Test ");
		System.out.println("\t------------------------------------\n");
		
		// DATA TO BE PROVIDED
		int numberOfDesignVariables = 10;
		Double designVariablesLowerBound = 10.0;
		Double designVariablesUpperBound = -10.0;
		int maximumNumberOfIteration = 1000;
		int particlesNumber = 50;
		Double kappa = 1.0;
		Double phi1 = 2.05;
		Double phi2 = 2.05;
		
		System.out.println("\t------------------------------------");
		System.out.println("\tINPUT: ");
		System.out.println("\tNumber of Design Variable : " + numberOfDesignVariables);
		System.out.println("\tDesign Variable Lower Bound : " + designVariablesLowerBound);
		System.out.println("\tDesign Variable Upper Bound : " + designVariablesUpperBound);
		System.out.println("\tMaximum Number of Iterations : " + maximumNumberOfIteration);
		System.out.println("\tParticles Number : " + particlesNumber);
		System.out.println("\n\tConstriction Coefficient");
		System.out.println("\t\tKappa : " + kappa);
		System.out.println("\t\tPhi 1 : " + phi1);
		System.out.println("\t\tPhi 2 : " + phi2);
		System.out.println("\t------------------------------------\n");
		
		// CALLING THE PSO OPTIMIZER ...
		ParticleSwarmOptimizer pso = new ParticleSwarmOptimizer(
				numberOfDesignVariables,
				designVariablesLowerBound,
				designVariablesUpperBound,
				maximumNumberOfIteration, 
				particlesNumber, 
				kappa, 
				phi1, 
				phi2
				);
		
		pso.optimize();
	}

}
