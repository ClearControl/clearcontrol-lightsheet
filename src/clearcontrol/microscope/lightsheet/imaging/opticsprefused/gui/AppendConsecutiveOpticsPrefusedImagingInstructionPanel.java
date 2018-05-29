package clearcontrol.microscope.lightsheet.imaging.opticsprefused.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.AppendConsecutiveOpticsPrefusedImagingInstruction;

/**
 * AppendConsecutiveOpticsPrefusedImagingInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveOpticsPrefusedImagingInstructionPanel extends CustomGridPane {
    public AppendConsecutiveOpticsPrefusedImagingInstructionPanel(AppendConsecutiveOpticsPrefusedImagingInstruction pInstruction) {
        addIntegerField(pInstruction.getNumberOfImages(), 0);
        addDoubleField(pInstruction.getIntervalInSeconds(), 1);
    }
}
