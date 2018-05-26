package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstruction;

/**
 * This instructions writes the raw data from the single view
 * acquisition stored in the DataWarehouse to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteSingleLightSheetImageAsRawToDiscInstruction extends
        WriteStackInterfaceContainerAsRawToDiscInstruction
{
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public WriteSingleLightSheetImageAsRawToDiscInstruction(int pDetectionArmIndex, int pLightSheetIndex, LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write C" + pDetectionArmIndex + "L" + pLightSheetIndex + " raw data to disc", StackInterfaceContainer.class, new String[] {"C" + pDetectionArmIndex + "L" + pLightSheetIndex}, null, pLightSheetMicroscope);
  }
}

