package clearcontrol.microscope.lightsheet.imaging.gafaso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionInterface;

/**
 * The AcquisitionStateSolution represents an acquisition state for optimization
 * with a variable list of degrees of freedom.
 *
 * Author: @haesleinhuepf 09 2018
 */
public class AcquisitionStateSolution implements SolutionInterface
{

  private final static Random random = new Random();

  HashMap<LightSheetDOF, Double> state =
                                       new HashMap<LightSheetDOF, Double>();
  HashMap<LightSheetDOF, Double> stepState =
                                           new HashMap<LightSheetDOF, Double>();

  public AcquisitionStateSolution(HashMap<LightSheetDOF, Double> state,
                                  HashMap<LightSheetDOF, Double> stepState)
  {
    for (LightSheetDOF key : state.keySet())
    {
      this.state.put(key, state.get(key));
    }
    for (LightSheetDOF key : stepState.keySet())
    {
      this.stepState.put(key, stepState.get(key));
    }
  }

  private double fitness = 0;

  public void setFitness(double fitness)
  {
    this.fitness = fitness;
  }

  @Override
  public double fitness()
  {
    return fitness;
  }

  @Override
  public void mutate()
  {
    int index = random.nextInt(state.keySet().size());

    boolean presign = random.nextBoolean();

    Iterator<LightSheetDOF> iterator = state.keySet().iterator();
    for (int i = 0; i < index; i++)
    {
      iterator.next();
    }

    LightSheetDOF key = iterator.next();

    double value = state.get(key);
    if (presign)
    {
      value += stepState.get(key);
    }
    else
    {
      value -= stepState.get(key);
    }
    state.remove(key);
    state.put(key, value);
    fitness = 0;
  }

  @Override
  public boolean isSimilar(SolutionInterface s,
                           double similarityTolerance)
  {
    if (!(s instanceof AcquisitionStateSolution))
    {
      return false;
    }

    for (LightSheetDOF key : state.keySet())
    {
      if (Math.abs(state.get(key)
                   - ((AcquisitionStateSolution) s).state.get(key)) > similarityTolerance)
      {
        return false;
      }
    }

    return true;
  }

  @Override
  public String toString()
  {
    String result = this.getClass().getSimpleName() + " (fit: "
                    + fitness()
                    + "): "
                    + state;
    return result;
  }

}
