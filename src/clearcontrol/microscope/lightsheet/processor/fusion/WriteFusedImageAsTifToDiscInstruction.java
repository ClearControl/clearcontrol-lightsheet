package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsTifToDiscInstruction;

/**
 * WriteFusedImageAsTifToDiscInstruction
 * <p>
 * <p>
 * <p>
 * Deprecated: Use WriteStackInterfaceContainerAsTifToDiscInstruction directly instead
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
@Deprecated
public class WriteFusedImageAsTifToDiscInstruction extends
        WriteStackInterfaceContainerAsTifToDiscInstruction
{
    public WriteFusedImageAsTifToDiscInstruction(String pChannelName, LightSheetMicroscope pLightSheetMicroscope)
    {
        super("IO: Write " + pChannelName + " fused stack as TIF to disc", FusedImageDataContainer.class, new String[]{"fused"}, pChannelName, pLightSheetMicroscope);
    }

    @Override
    public WriteFusedImageAsTifToDiscInstruction copy() {
        return new WriteFusedImageAsTifToDiscInstruction(mChannelName, getLightSheetMicroscope());
    }
}