package clearcontrol.microscope.lightsheet.warehouse.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * This instructions takes the oldest StackInterfaceContainer from the
 * DataWarehouse and recycles it. Its memory is freed then.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class DropOldestStackInterfaceContainerInstruction extends
        InstructionBase implements
        InstructionInterface,
                                                                      LoggingFeature
{
  Class mContainerClassToDrop;

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public DropOldestStackInterfaceContainerInstruction(Class pContainerClassToDrop)
  {
    super("Memory: Recycle container of type " + pContainerClassToDrop.getSimpleName());
    mContainerClassToDrop = pContainerClassToDrop;
  }

  @Override public boolean initialize()
  {
    return false;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (mMicroscope instanceof LightSheetMicroscope) {
      DataWarehouse lWarehouse = ((LightSheetMicroscope) mMicroscope).getDataWarehouse();
      StackInterfaceContainer lContainer = lWarehouse.getOldestContainer(mContainerClassToDrop);
      lWarehouse.disposeContainer(lContainer);
    } else {
      warning("I need a LightSheetMicroscope!");
    }
    return false;
  }
}
