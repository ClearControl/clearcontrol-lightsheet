package clearcontrol.microscope.lightsheet.imaging.exposuremodulation.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.exposuremodulation.ExposureModulatedAcquisitionInstruction;

/**
 * ExposureModulatedAcquisitionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ExposureModulatedAcquisitionInstructionPanel extends CustomGridPane {
    public ExposureModulatedAcquisitionInstructionPanel(ExposureModulatedAcquisitionInstruction pExposureModulatedAcquisitionScheduler)
    {
        addDoubleField(pExposureModulatedAcquisitionScheduler.getShortExposureTimeInSecondsVariable(), 0);
        addDoubleField(pExposureModulatedAcquisitionScheduler.getLongExposureTimeInSecondsVariable(), 1);
    }
}
