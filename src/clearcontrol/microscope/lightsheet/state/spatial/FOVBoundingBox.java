package clearcontrol.microscope.lightsheet.state.spatial;

import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.state.spatial.PositionListContainer;

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


    BasicStageInterface mStageX = null;
    BasicStageInterface mStageY = null;
    BasicStageInterface mStageZ = null;

    public FOVBoundingBox(LightSheetMicroscope pLightSheetMicroscope) {
        super("FOV");
        setMicroscope(pLightSheetMicroscope);
        initialize();
    }



}
