package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsTifToDiscScheduler;

/**
 * WriteFusedImageAsTifToDiscScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class WriteFusedImageAsTifToDiscScheduler extends
        WriteStackInterfaceContainerAsTifToDiscScheduler
{
    /**
     * INstanciates a virtual device with a given name
     *
     * @param pChannelName
     */
    public WriteFusedImageAsTifToDiscScheduler(String pChannelName)
    {
        super("IO: Write " + pChannelName + " fused stack as TIF to disc", FusedImageDataContainer.class, new String[]{"fused"}, pChannelName);
    }
}