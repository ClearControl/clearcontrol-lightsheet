package clearcontrol.microscope.lightsheet.imaging.sequential;

import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerToDiscScheduler;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteSequentialRawDataToDiscScheduler  extends
                                                    WriteStackInterfaceContainerToDiscScheduler
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pNumberOfDetectionArms
   */
  public WriteSequentialRawDataToDiscScheduler(int pNumberOfDetectionArms, int pNumberOfLightSheets)
  {
    super("IO: Write sequential raw data to disc", SequentialImageDataContainer.class, listKeys(pNumberOfDetectionArms, pNumberOfLightSheets), null);
  }

  private static String[] listKeys(int pNumberOfDetectionArms, int pNumberOfLightSheets) {
    String[] result = new String[pNumberOfDetectionArms];
    int count = 0;
    for (int l = 0; l < pNumberOfLightSheets; l++)
    {
      for (int d = 0; d < pNumberOfDetectionArms; d++)
      {
        result[count] = "C" + d + "L" + l;
        count++;
      }
    }
    return result;
  }
}
