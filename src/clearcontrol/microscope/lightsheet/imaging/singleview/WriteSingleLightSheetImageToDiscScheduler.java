package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerToDiscScheduler;

/**
 * This scheduler writes the raw data from the single view
 * acquisition stored in the DataWarehouse to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteSingleLightSheetImageToDiscScheduler extends
                                                       WriteStackInterfaceContainerToDiscScheduler
{
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public WriteSingleLightSheetImageToDiscScheduler(int pDetectionArmIndex, int pLightSheetIndex)
  {
    super("IO: Write C" + pDetectionArmIndex + "L" + pLightSheetIndex + " raw data to disc", SequentialImageDataContainer.class, new String[] {"C" + pDetectionArmIndex + "L" + pLightSheetIndex}, null);
  }
}

