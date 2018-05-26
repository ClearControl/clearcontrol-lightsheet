package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsTifToDiscInstruction;

/**
 * WriteFusedImageAsTifToDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class WriteFusedImageAsTifToDiscInstruction extends
        WriteStackInterfaceContainerAsTifToDiscInstruction
{
    /**
     * INstanciates a virtual device with a given name
     *
     * @param pChannelName
     */
    public WriteFusedImageAsTifToDiscInstruction(String pChannelName, LightSheetMicroscope pLightSheetMicroscope)
    {
        super("IO: Write " + pChannelName + " fused stack as TIF to disc", FusedImageDataContainer.class, new String[]{"fused"}, pChannelName, pLightSheetMicroscope);
    }
}