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
    super("Pause " + humanReadableTime(pPauseTimeInMilliseconds));
    mPauseTimeInMilliseconds = pPauseTimeInMilliseconds;
  }

  private static String humanReadableTime(long pPauseTimeInMilliseconds) {
    String lPauseTimeHumanReadable = "";
    if (pPauseTimeInMilliseconds == 0) {}
    else if (pPauseTimeInMilliseconds < 1000) {
      lPauseTimeHumanReadable = "" + pPauseTimeInMilliseconds + " msec";
    } else if (pPauseTimeInMilliseconds < 60000) {
      lPauseTimeHumanReadable = "" + (pPauseTimeInMilliseconds / 1000) + " sec";
    } else if (pPauseTimeInMilliseconds < 3600000) {
      lPauseTimeHumanReadable = "" + (pPauseTimeInMilliseconds / 60000) + " min";
    } else {
      lPauseTimeHumanReadable = "" + (pPauseTimeInMilliseconds / 3600000) + " h";
    }
    return lPauseTimeHumanReadable;
  }

  @Override public boolean initialize()
  {
    return false;
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
