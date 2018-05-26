package clearcontrol.instructions.implementations;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.SchedulerBase;
import clearcontrol.instructions.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class PauseUntilTimeAfterMeasuredTimeScheduler extends
                                                     SchedulerBase implements
                                                                   SchedulerInterface,
                                                                   LoggingFeature
{
  private final String mMeasuredTimeKey;
  long mPauseTimeInMilliseconds = 0;

  public PauseUntilTimeAfterMeasuredTimeScheduler(String pMeasuredTimeKey, long pPauseTimeInMilliseconds)
  {
    super("Timing: Pause " + Utilities.humanReadableTime(
        pPauseTimeInMilliseconds) + " after time t_" + pMeasuredTimeKey + " measurement");
    mPauseTimeInMilliseconds = pPauseTimeInMilliseconds;
    mMeasuredTimeKey = pMeasuredTimeKey;
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (!MeasureTimeScheduler.sMeasuredTime.containsKey(mMeasuredTimeKey)) {
      warning("Time measurement t_" + mMeasuredTimeKey + " does not exist!");
      return false;
    }
      Long measuredTime = MeasureTimeScheduler.sMeasuredTime.get(mMeasuredTimeKey);
      long timeToWait = mPauseTimeInMilliseconds - (System.currentTimeMillis() - measuredTime);
      if (timeToWait > 0)
      {

        try
        {
          Thread.sleep(timeToWait);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    return true;
  }
}
