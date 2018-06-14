package clearcontrol.microscope.lightsheet.imaging.opticsprefused.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.AppendConsecutiveHyperDriveImagingInstruction;

/**
 * AppendConsecutiveHyperDriveImagingInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveHyperDriveImagingInstructionPanel extends CustomGridPane {
    public AppendConsecutiveHyperDriveImagingInstructionPanel(AppendConsecutiveHyperDriveImagingInstruction pInstruction) {
        addIntegerField(pInstruction.getNumberOfImages(), 0);
        addDoubleField(pInstruction.getIntervalInSeconds(), 1);
    }
}
