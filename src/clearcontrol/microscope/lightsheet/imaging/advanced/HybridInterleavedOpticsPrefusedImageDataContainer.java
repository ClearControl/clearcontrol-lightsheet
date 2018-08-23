package clearcontrol.microscope.lightsheet.imaging.advanced;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * HybridInterleavedOpticsPrefusedImageDataContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class HybridInterleavedOpticsPrefusedImageDataContainer extends
        StackInterfaceContainer
{
    private final LightSheetMicroscope mLightSheetMicroscope;

    public HybridInterleavedOpticsPrefusedImageDataContainer(LightSheetMicroscope pLightSheetMicroscope) {
        super(pLightSheetMicroscope.getDevice(
                LightSheetTimelapse.class, 0).getTimePointCounterVariable().get());
        mLightSheetMicroscope = pLightSheetMicroscope;
    }

    @Override public boolean isDataComplete()
    {
        for (int d = 0; d < mLightSheetMicroscope.getNumberOfDetectionArms(); d++) {
            if (! super.containsKey("C" + d + "hybrid_interleaved_opticsprefused")) {
                return false;
            }
        }
        return true;
    }
}
