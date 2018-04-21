package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.FusionScheduler;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class OpticsPrefusedFusionScheduler extends FusionScheduler implements
                                                                   SchedulerInterface,
                                                                   LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pRecycler
   */
  public OpticsPrefusedFusionScheduler(RecyclerInterface<StackInterface, StackRequest> pRecycler)
  {
    super("Fusion: Optics prefused", pRecycler);
  }


  @Override public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse = mLightSheetMicroscope.getDataWarehouse();
    final SequentialImageDataContainer
        lContainer = lDataWarehouse.getOldestContainer(SequentialImageDataContainer.class);
    String[] lInputImageKeys = new String[mLightSheetMicroscope.getNumberOfDetectionArms() * mLightSheetMicroscope.getNumberOfLightSheets()];

    int count = 0;
    for (int d = 0; d < mLightSheetMicroscope.getNumberOfDetectionArms(); d++) {
      lInputImageKeys[count] = "C" + d + "opticsprefused";
      count ++;
    }

    StackInterface lFusedStack = fuseStacks(lContainer, lInputImageKeys);
    if (lFusedStack == null) {
      return false;
    }

    storeFusedContainer(lFusedStack);
    return true;
  }
}
