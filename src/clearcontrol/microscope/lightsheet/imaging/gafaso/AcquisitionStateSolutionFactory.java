package clearcontrol.microscope.lightsheet.imaging.gafaso;

import java.util.Random;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionFactory;

/**
 * The AcquisitionStateSolutionFactory generates acquisition states. "Random"
 * initialisation is not given. Initialisation happens by setting the whole
 * population to default.
 *
 * This implementation is needed for a genetic algorithm
 *
 * Author: @haesleinhuepf 09 2018
 */
public class AcquisitionStateSolutionFactory implements
                                             SolutionFactory<AcquisitionStateSolution>
{

  AcquisitionStateSolution startSolution;

  AcquisitionStateSolutionFactory(AcquisitionStateSolution startSolution)
  {
    this.startSolution = startSolution;
  }

  Random random = new Random();

  @Override
  public AcquisitionStateSolution random()
  {
    return new AcquisitionStateSolution(startSolution.state,
                                        startSolution.stepState);
  }

  @Override
  public AcquisitionStateSolution crossover(AcquisitionStateSolution pSolution1,
                                            AcquisitionStateSolution pSolution2)
  {

    AcquisitionStateSolution result =
                                    new AcquisitionStateSolution(pSolution1.state,
                                                                 startSolution.stepState);
    for (LightSheetDOF key : pSolution2.state.keySet())
    {
      if (random.nextBoolean())
      {
        result.state.remove(key);
        result.state.put(key, pSolution2.state.get(key));
      }
    }
    return result;
  }
}
