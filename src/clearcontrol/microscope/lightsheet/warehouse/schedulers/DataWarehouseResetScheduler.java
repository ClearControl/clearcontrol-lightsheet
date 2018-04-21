package clearcontrol.microscope.lightsheet.warehouse.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class DataWarehouseResetScheduler extends SchedulerBase implements
                                                               SchedulerInterface,
                                                               LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public DataWarehouseResetScheduler()
  {
    super("Memory: Reset memory (heap=0)");
  }

  @Override public boolean initialize()
  {
    return false;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (mMicroscope instanceof LightSheetMicroscope) {
      ((LightSheetMicroscope) mMicroscope).getDataWarehouse().clear();
    } else {
      warning("I need a LightSheetMicroscope!");
    }
    return false;
  }
}
