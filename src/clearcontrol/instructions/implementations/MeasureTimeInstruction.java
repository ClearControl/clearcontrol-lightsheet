package clearcontrol.instructions.implementations;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class MeasureTimeInstruction extends InstructionBase implements
        InstructionInterface
{
  static HashMap<String, Long> sMeasuredTime = new HashMap<String, Long>();
  private final String mTimeMeasurementKey;

  /**
   *
   * @param pTimeMeasurementKey
   */
  public MeasureTimeInstruction(String pTimeMeasurementKey)
  {
    super("Timing: Measure time t_" + pTimeMeasurementKey);
    mTimeMeasurementKey = pTimeMeasurementKey;
  }

  @Override public boolean initialize()
  {
    return false;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (sMeasuredTime.containsKey(mTimeMeasurementKey)) {
      sMeasuredTime.remove(mTimeMeasurementKey);
    }
    sMeasuredTime.put(mTimeMeasurementKey, System.currentTimeMillis());
    return false;
  }
}
