package clearcontrol.instructions.implementations;

import java.util.HashMap;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class MeasureTimeInstruction extends InstructionBase
                                    implements InstructionInterface
{
  static HashMap<String, Long> sMeasuredTime =
                                             new HashMap<String, Long>();
  private final Variable<String> mMeasuredTimeKeyVariable =
                                                          new Variable<String>("Time measurement key",
                                                                               "_");

  /**
   *
   * @param pTimeMeasurementKey
   */
  public MeasureTimeInstruction(String pTimeMeasurementKey)
  {
    super("Timing: Measure time t_" + pTimeMeasurementKey);
    mMeasuredTimeKeyVariable.set(pTimeMeasurementKey);
  }

  @Override
  public boolean initialize()
  {
    return false;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    if (sMeasuredTime.containsKey(mMeasuredTimeKeyVariable.get()))
    {
      sMeasuredTime.remove(mMeasuredTimeKeyVariable.get());
    }
    sMeasuredTime.put(mMeasuredTimeKeyVariable.get(),
                      System.currentTimeMillis());
    return false;
  }

  @Override
  public MeasureTimeInstruction copy()
  {
    return new MeasureTimeInstruction(mMeasuredTimeKeyVariable.get());
  }

  @Override
  public String getDescription() {
    return "Measures the current time and stores it under the name " + mMeasuredTimeKeyVariable.get() + " in a list.";
  }

  public Variable<String> getMeasuredTimeKeyVariable()
  {
    return mMeasuredTimeKeyVariable;
  }
}
