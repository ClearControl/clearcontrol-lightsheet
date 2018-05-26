package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstruction;

/**
 * This instructions writes the raw data from the oldest interleaved
 * acquisition stored in the DataWarehouse to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteInterleavedRawDataToDiscInstruction extends
        WriteStackInterfaceContainerAsRawToDiscInstruction
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pNumberOfDetectionArms
   */
  public WriteInterleavedRawDataToDiscInstruction(int pNumberOfDetectionArms)
  {
    super("IO: Write interleaved raw data to disc", InterleavedImageDataContainer.class, listKeys(pNumberOfDetectionArms), null);
  }

  private static String[] listKeys(int pNumberOfDetectionArms) {
    String[] result = new String[pNumberOfDetectionArms];
    for (int d = 0; d < pNumberOfDetectionArms; d++) {
      result[d] = "C" + d + "interleaved";
    }
    return result;
  }
}
