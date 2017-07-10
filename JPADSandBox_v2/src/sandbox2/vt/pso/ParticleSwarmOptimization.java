package sandbox2.vt.pso;

import java.io.File;
import java.util.Arrays;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import optimization.ParticleSwarmOptimizer;
import writers.JPADStaticWriteUtils;

/**
 * Example from http://yarpiz.com/440/ytea101-particle-swarm-optimization-pso-in-matlab-video-tutorial
 * Cost function ==> Shpere ==> sum(x_i^2)
 * @author Vittorio Trifari
 *
 */
public class ParticleSwarmOptimization {

	public static void main(String[] args) {

		System.out.println("\t------------------------------------");
		System.out.println("\tParticle Swarm Optimization :: START ");
		System.out.println("\t------------------------------------\n");
		
		long startTime = System.currentTimeMillis();     
		
		// SETUP OUTPUT FOLDER
		MyConfiguration.initWorkingDirectoryTree();
		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR); 
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Particle_Swarm_Optimization" + File.separator);
		
		// DATA TO BE PROVIDED
		int numberOfDesignVariables = 10;
		Double[] designVariablesLowerBound = new Double[] {10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0};
		Double[] designVariablesUpperBound = new Double[] {-10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0};
//		Double[] designVariablesUpperBound = new Double[] {5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0};
		Double convergenceThreshold = 1e-3; // threshold used to compare particles position during each iteration
		int particlesNumber = 10;
		Double kappa = 1.0;
		Double phi1 = 2.05;
		Double phi2 = 2.05;
		
		System.out.println("\t------------------------------------");
		System.out.println("\tINPUT: ");
		System.out.println("\tNumber of Design Variable : " + numberOfDesignVariables);
		System.out.println("\tDesign Variable Lower Bound : " + Arrays.toString(designVariablesLowerBound));
		System.out.println("\tDesign Variable Upper Bound : " + Arrays.toString(designVariablesUpperBound));
		System.out.println("\tConvergence Threshold : " + convergenceThreshold);
		System.out.println("\tParticles Number : " + particlesNumber);
		System.out.println("\n\tConstriction Coefficient");
		System.out.println("\t\tKappa : " + kappa);
		System.out.println("\t\tPhi 1 : " + phi1);
		System.out.println("\t\tPhi 2 : " + phi2);
		System.out.println("\t------------------------------------\n");
		
		// CALLING THE PSO OPTIMIZER ...
		ParticleSwarmOptimizer pso = new ParticleSwarmOptimizer(
				numberOfDesignVariables,
				designVariablesUpperBound,
				designVariablesLowerBound,
				convergenceThreshold,
				particlesNumber, 
				kappa, 
				phi1, 
				phi2,
				subfolderPath,
				null,
				null
				);
		
		pso.optimize();
		
		System.out.println("\n\n\t------------------------------------");
		System.out.println("\tParticle Swarm Optimization :: END ");
		System.out.println("\t------------------------------------\n");
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("\t TIME ESTIMATED = " + (estimatedTime/1000) + " seconds");
		
	}

}
