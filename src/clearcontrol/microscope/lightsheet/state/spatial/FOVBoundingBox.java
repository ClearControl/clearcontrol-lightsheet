package clearcontrol.microscope.lightsheet.state.spatial;

import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;

/**
 * FOVBoundingBox
 *
 * Todo: The FOV bonuding box should be available via DataWarehouse as a kind of persistent container which cannot
 * be recycled.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class FOVBoundingBox extends SpaceTravelInstruction {

    public FOVBoundingBox(LightSheetMicroscope pLightSheetMicroscope) {
        super("FOV", pLightSheetMicroscope);
        initialize();
    }

}
