package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;

/**
 * SpotCountContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class SpotCountContainer  extends MeasurementInSpaceContainer {

    public SpotCountContainer(LightSheetMicroscope pLightSheetMicroscope, double pX, double pY, double pZ, double pSpotCount) {
        super(pLightSheetMicroscope, pX, pY, pZ, pSpotCount);
    }
}