package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

/**
 * DCTS2DContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DCTS2DContainer extends MeasurementInSpaceContainer {

    public DCTS2DContainer(LightSheetMicroscope pLightSheetMicroscope, double pX, double pY, double pZ, double pDCTS2D) {
        super(pLightSheetMicroscope, pX, pY, pZ, pDCTS2D);
    }
}
