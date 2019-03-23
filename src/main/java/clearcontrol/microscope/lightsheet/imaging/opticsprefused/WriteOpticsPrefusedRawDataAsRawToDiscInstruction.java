package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstructionBase;

/**
 * This instructions writes the raw data from the oldest optics prefused
 * acquisition stored in the DataWarehouse to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class WriteOpticsPrefusedRawDataAsRawToDiscInstruction extends
                                                              WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
  public WriteOpticsPrefusedRawDataAsRawToDiscInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write optics prefused raw data to disc",
          OpticsPrefusedImageDataContainer.class,
          listKeys(pLightSheetMicroscope.getNumberOfDetectionArms()),
          null,
          pLightSheetMicroscope);
  }

  private static String[] listKeys(int pNumberOfDetectionArms)
  {
    String[] result = new String[pNumberOfDetectionArms];
    for (int d = 0; d < pNumberOfDetectionArms; d++)
    {
      result[d] = "C" + d + "opticsprefused";
    }
    return result;
  }

  @Override
  public WriteOpticsPrefusedRawDataAsRawToDiscInstruction copy()
  {
    return new WriteOpticsPrefusedRawDataAsRawToDiscInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Write raw data from optics-prefused acquisition to disc.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[]{OpticsPrefusedImageDataContainer.class};
  }
}
