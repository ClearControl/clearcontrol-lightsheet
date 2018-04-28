package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.actuator.demo;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.sim.SpatialPhaseModulatorDeviceSimulator;
import com.sun.media.imageioimpl.plugins.jpeg2000.DataEntryURLBox;
import org.ejml.data.DenseMatrix64F;

import java.util.Arrays;

/**
 * The ActuatorGeneticAlgorithmDemo sets a certain combination of Zernike modes as ground truth and asks a genetic
 * algorithm to find the same comination with random combinations as starting point.
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ActuatorGeneticAlgorithmDemo {
    public static void main(String... args) {

        // we need a deformable mirror simulator just to tell the deserved matrix size to the SolutionFactory
        SpatialPhaseModulatorDeviceSimulator lDMSimulator = new SpatialPhaseModulatorDeviceSimulator("dm", 11, 11);

        // We want to find this solution by running the optimization algorithm
        double[] lReferenceFactors = {0, 0.1, 0.1, 0, 0, 0};
        ZernikeSolution lReferenceSolution = new ZernikeSolution(lReferenceFactors, null, lDMSimulator, 160);
        //ActuatorDemoSolution lReferenceSolution = new ActuatorDemoSolution(lReferenceMatrix, null, lDMSimulator, 160, null);
        //mReferenceSolution.mReference = mReferenceSolution;
        //System.out.println("Test: " + mReferenceSolution.fitness());
        //if (true) return;

        ActuatorDemoSolutionFactory lSolutionFactory = new ActuatorDemoSolutionFactory(null, lDMSimulator, 160, lReferenceSolution.getMatrix());

        Population<ActuatorDemoSolution> lInitialPopulation = new Population<ActuatorDemoSolution>(lSolutionFactory, 100, 10);

        System.out.println("Initial fitness: " + lInitialPopulation.fitness());

        Population<ActuatorDemoSolution> lPopulation = lInitialPopulation;

        for (int i = 0; i < 2000; i ++) {
            lPopulation = lPopulation.selection();
        }
        //System.out.println("Population [" + i + "] fitness: " + lPopulation.fitness());
        ActuatorDemoSolution lSolution = lPopulation.best();
        System.out.println("Best solution fitness: " + lSolution.fitness());
        System.out.println("Best solution: "  + lSolution.getMatrix());
        System.out.println("Ground truth:  "  + lReferenceSolution.getMatrix());


    }
}
