package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerToDiscScheduler;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteInterleavedRawDataToDiscScheduler extends
                                                    WriteStackInterfaceContainerToDiscScheduler
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pNumberOfDetectionArms
   */
  public WriteInterleavedRawDataToDiscScheduler(int pNumberOfDetectionArms)
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
