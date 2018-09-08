package clearcontrol.microscope.lightsheet.imaging.interleavedgao;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionFactory;

import java.util.Random;

/**
 * InterleavedGAOStateSolutionFactory
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class InterleavedGAOStateSolutionFactory implements SolutionFactory<InterleavedGAOStateSolution> {

    InterleavedGAOStateSolution startSolution;

    InterleavedGAOStateSolutionFactory(InterleavedGAOStateSolution startSolution) {
        this.startSolution = startSolution;
    }


    Random random = new Random();

    @Override
    public InterleavedGAOStateSolution random() {
        return new InterleavedGAOStateSolution(startSolution.state, startSolution.stepState);
    }

    @Override
    public InterleavedGAOStateSolution crossover(InterleavedGAOStateSolution pSolution1, InterleavedGAOStateSolution pSolution2) {

        InterleavedGAOStateSolution result = new InterleavedGAOStateSolution(pSolution1.state, startSolution.stepState);
        for (LightSheetDOF key : pSolution2.state.keySet()) {
            if (random.nextBoolean()) {
                result.state.remove(key);
                result.state.put(key, pSolution2.state.get(key));
            }
        }
        return result;
    }
}
