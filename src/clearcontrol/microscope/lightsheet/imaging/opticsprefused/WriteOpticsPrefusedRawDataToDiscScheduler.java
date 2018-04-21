package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerToDiscScheduler;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteOpticsPrefusedRawDataToDiscScheduler extends
                                                       WriteStackInterfaceContainerToDiscScheduler
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pNumberOfDetectionArms
   */
  public WriteOpticsPrefusedRawDataToDiscScheduler(int pNumberOfDetectionArms)
  {
    super("IO: Write optics prefused raw data to disc", InterleavedImageDataContainer.class, listKeys(pNumberOfDetectionArms), null);
  }

  private static String[] listKeys(int pNumberOfDetectionArms) {
    String[] result = new String[pNumberOfDetectionArms];
    for (int d = 0; d < pNumberOfDetectionArms; d++) {
      result[d] = "C" + d + "opticsprefused";
    }
    return result;
  }
}
