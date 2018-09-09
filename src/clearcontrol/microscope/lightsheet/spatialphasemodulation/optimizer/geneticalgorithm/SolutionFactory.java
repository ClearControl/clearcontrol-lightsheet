package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm;

/**
 * The generic SolutionFactory interface is use in a GeneticAlgorithm to
 * generate new Solutions.
 *
 * Author: @haesleinhuepf 04 2018
 */
public interface SolutionFactory<S extends SolutionInterface>
{
  S random();

  S crossover(S pSolution1, S pSolution2);
}
