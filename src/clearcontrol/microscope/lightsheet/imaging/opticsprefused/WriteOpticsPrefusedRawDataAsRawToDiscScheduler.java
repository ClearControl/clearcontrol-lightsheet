package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscScheduler;

/**
 * This instructions writes the raw data from the oldest optics prefused
 * acquisition stored in the DataWarehouse to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteOpticsPrefusedRawDataAsRawToDiscScheduler extends
        WriteStackInterfaceContainerAsRawToDiscScheduler
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pNumberOfDetectionArms
   */
  public WriteOpticsPrefusedRawDataAsRawToDiscScheduler(int pNumberOfDetectionArms)
  {
    super("IO: Write optics prefused raw data to disc", OpticsPrefusedImageDataContainer.class, listKeys(pNumberOfDetectionArms), null);
  }

  private static String[] listKeys(int pNumberOfDetectionArms) {
    String[] result = new String[pNumberOfDetectionArms];
    for (int d = 0; d < pNumberOfDetectionArms; d++) {
      result[d] = "C" + d + "opticsprefused";
    }
    return result;
  }
}
