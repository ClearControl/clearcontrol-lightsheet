package clearcontrol.microscope.lightsheet.state.spatial;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelScheduler;

/**
 * FOVBoundingBox
 *
 * Todo: The FOV bonuding box should be available via DataWarehouse as a kind of persistent container which cannot
 * be recycled.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class FOVBoundingBox extends SpaceTravelScheduler {

    public FOVBoundingBox(LightSheetMicroscope pLightSheetMicroscope) {
        super("FOV");
        setMicroscope(pLightSheetMicroscope);
        initialize();
    }

}
