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
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public PauseScheduler()
  {
    super("Pause");
  }

  @Override public boolean doExperiment(long pTimePoint)
  {
    return true;
  }
}
