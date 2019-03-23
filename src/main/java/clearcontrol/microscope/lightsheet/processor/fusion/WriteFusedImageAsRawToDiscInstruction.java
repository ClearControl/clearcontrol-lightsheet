package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstructionBase;

/**
 * This instructions writes a fused image to disc. Depending on how the images
 * was fused, it might be stored in different folders.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class WriteFusedImageAsRawToDiscInstruction extends
                                                   WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
  public WriteFusedImageAsRawToDiscInstruction(String pChannelName,
                                               LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write " + pChannelName
          + " fused stack to disc",
          FusedImageDataContainer.class,
          new String[]
    { "fused" }, pChannelName, pLightSheetMicroscope);
  }

  @Override
  public WriteFusedImageAsRawToDiscInstruction copy()
  {
    return new WriteFusedImageAsRawToDiscInstruction(mChannelName,
                                                     getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Write fused image data as raw to disc.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[]{FusedImageDataContainer.class};
  }
}
