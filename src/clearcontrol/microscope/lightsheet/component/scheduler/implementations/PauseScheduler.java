package clearcontrol.microscope.lightsheet.component.scheduler.implementations;

import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class PauseScheduler extends SchedulerBase implements
                                                  SchedulerInterface
{
  long mPauseTimeInMilliseconds = 0;
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public PauseScheduler() {
    this(0);
  }

  public PauseScheduler(long pPauseTimeInMilliseconds)
  {
    super("Timing: Pause " + Utilities.humanReadableTime(pPauseTimeInMilliseconds));
    mPauseTimeInMilliseconds = pPauseTimeInMilliseconds;
  }



  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    try
    {
      Thread.sleep(mPauseTimeInMilliseconds);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    return true;
  }
}
