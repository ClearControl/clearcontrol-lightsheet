package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsTifToDiscInstructionBase;

/**
 * WriteFusedImageAsTifToDiscInstructionBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class WriteFusedImageAsTifToDiscInstructionBase extends
                                                       WriteStackInterfaceContainerAsTifToDiscInstructionBase
{
  public WriteFusedImageAsTifToDiscInstructionBase(String pChannelName,
                                                   LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write " + pChannelName
          + " fused stack as TIF to disc",
          FusedImageDataContainer.class,
          new String[]
    { "fused" }, pChannelName, pLightSheetMicroscope);
  }

  @Override
  public WriteFusedImageAsTifToDiscInstructionBase copy()
  {
    return new WriteFusedImageAsTifToDiscInstructionBase(mChannelName,
                                                         getLightSheetMicroscope());
  }
}