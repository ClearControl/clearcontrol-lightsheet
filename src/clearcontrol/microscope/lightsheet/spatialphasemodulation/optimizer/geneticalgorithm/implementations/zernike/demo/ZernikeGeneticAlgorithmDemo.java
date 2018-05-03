package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.demo;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.sim.SpatialPhaseModulatorDeviceSimulator;

import java.util.Arrays;

/**
 * The ZernikeGeneticAlgorithmDemo sets a certain combination of Zernike modes as ground truth and asks a genetic
 * algorithm to find the same comination with random combinations as starting point.
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ZernikeGeneticAlgorithmDemo {
    public static void main(String... args) {

        // we need a deformable mirror simulator just to tell the deserved matrix size to the SolutionFactory
        SpatialPhaseModulatorDeviceSimulator lDMSimulator = new SpatialPhaseModulatorDeviceSimulator("dm", 11, 11);

        // We want to find this solution by running the optimization algorithm
        double[] lReferenceFactors = {1.0, -0.5, 0.25, 0.125, - 0.0625, 0};
        ZernikeDemoSolution lReferenceSolution = new ZernikeDemoSolution(lReferenceFactors, null, lDMSimulator, 160, null);
        //mReferenceSolution.mReferenceMatrix = mReferenceSolution;
        //System.out.println("Test: " + mReferenceSolution.fitness());
        //if (true) return;

        ZernikeDemoSolutionFactory lSolutionFactory = new ZernikeDemoSolutionFactory(null, lDMSimulator, 160, 6, 1.0, lReferenceSolution.getMatrix());

        Population<ZernikeDemoSolution> lInitialPopulation = new Population<ZernikeDemoSolution>(lSolutionFactory, 100, 1);

        System.out.println("Initial fitness: " + lInitialPopulation.fitness());

        Population<ZernikeDemoSolution> lPopulation = lInitialPopulation;

        for (int i = 0; i < 200; i ++) {
            lPopulation = lPopulation.runEpoch();
        }
        //System.out.println("Population [" + i + "] fitness: " + lPopulation.fitness());
        ZernikeDemoSolution lSolution = lPopulation.best();
        System.out.println("Best solution fitness: " + lSolution.fitness());
        System.out.println("Best solution: "  + Arrays.toString(lSolution.getFactors()));
        System.out.println("Ground truth:  "  + Arrays.toString(lReferenceSolution.getFactors()));


    }
}
