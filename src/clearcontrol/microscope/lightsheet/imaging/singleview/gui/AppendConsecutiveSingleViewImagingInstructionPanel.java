package clearcontrol.microscope.lightsheet.imaging.singleview.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.singleview.AppendConsecutiveSingleViewImagingInstruction;

/**
 * AppendConsecutiveSingleViewImagingInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveSingleViewImagingInstructionPanel extends CustomGridPane {
    public AppendConsecutiveSingleViewImagingInstructionPanel(AppendConsecutiveSingleViewImagingInstruction pInstruction) {
        addIntegerField(pInstruction.getNumberOfImages(), 0);
        addDoubleField(pInstruction.getIntervalInSeconds(), 1);
        addIntegerField(pInstruction.getLightSheetIndex(), 2);
        addIntegerField(pInstruction.getDetectionArmIndex(), 3);
    }
}
