package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstructionBase;

/**
 * This instructions writes the raw data from the oldest interleaved acquisition
 * stored in the DataWarehouse to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class WriteInterleavedRawDataToDiscInstruction extends
                                                      WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
  public WriteInterleavedRawDataToDiscInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write interleaved raw data to disc",
          InterleavedImageDataContainer.class,
          listKeys(pLightSheetMicroscope.getNumberOfDetectionArms()),
          null,
          pLightSheetMicroscope);
  }

  private static String[] listKeys(int pNumberOfDetectionArms)
  {
    String[] result = new String[pNumberOfDetectionArms];
    for (int d = 0; d < pNumberOfDetectionArms; d++)
    {
      result[d] = "C" + d + "interleaved";
    }
    return result;
  }

  @Override
  public WriteInterleavedRawDataToDiscInstruction copy()
  {
    return new WriteInterleavedRawDataToDiscInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Write raw data from interleaved acquisition to disc.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[]{InterleavedImageDataContainer.class};
  }
}
