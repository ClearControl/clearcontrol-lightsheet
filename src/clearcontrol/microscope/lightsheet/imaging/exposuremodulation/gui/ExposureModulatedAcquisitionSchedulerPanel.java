package clearcontrol.microscope.lightsheet.imaging.exposuremodulation.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.exposuremodulation.ExposureModulatedAcquisitionScheduler;

/**
 * ExposureModulatedAcquisitionSchedulerPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ExposureModulatedAcquisitionSchedulerPanel extends CustomGridPane {
    public ExposureModulatedAcquisitionSchedulerPanel(ExposureModulatedAcquisitionScheduler pExposureModulatedAcquisitionScheduler)
    {
        addDoubleField(pExposureModulatedAcquisitionScheduler.getShortExposureTimeInSecondsVariable(), 0);
        addDoubleField(pExposureModulatedAcquisitionScheduler.getLongExposureTimeInSecondsVariable(), 1);
    }
}
