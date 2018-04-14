package clearcontrol.microscope.lightsheet.component.scheduler.implementations;

import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class MeasureTimeScheduler extends SchedulerBase implements
                                                        SchedulerInterface
{
  static HashMap<String, Long> sMeasuredTime = new HashMap<String, Long>();
  private final String mTimeMeasurementKey;

  /**
   *
   * @param pTimeMeasurementKey
   */
  public MeasureTimeScheduler(String pTimeMeasurementKey)
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
