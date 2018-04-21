package clearcontrol.microscope.lightsheet.warehouse.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class DropOldestStackInterfaceContainerScheduler extends
                                                        SchedulerBase implements
                                                                      SchedulerInterface,
                                                                      LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public DropOldestStackInterfaceContainerScheduler()
  {
    super("Memory: Recycle memory (heap--)");
  }

  @Override public boolean initialize()
  {
    return false;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (mMicroscope instanceof LightSheetMicroscope) {
      DataWarehouse lWarehouse = ((LightSheetMicroscope) mMicroscope).getDataWarehouse();
      StackInterfaceContainer lContainer = lWarehouse.getOldestContainer(StackInterfaceContainer.class);
      lWarehouse.disposeContainer(lContainer);
    } else {
      warning("I need a LightSheetMicroscope!");
    }
    return false;
  }
}
