package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm;

/**
 * The SolutionInterface must be implemented by all Solutions in a genetic
 * algorithm to be able to determine the fitness of the soltions and to mutate
 * it.
 *
 * Author: @haesleinhuepf 04 2018
 */
public interface SolutionInterface
{
  double fitness();

  void mutate();
}
