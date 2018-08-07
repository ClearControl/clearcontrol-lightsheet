package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.CenterMaxProjectionInstruction;

/**
 * CenterMaxProjectionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class CenterMaxProjectionInstructionPanel extends CustomGridPane {
    public CenterMaxProjectionInstructionPanel(CenterMaxProjectionInstruction pInstruction) {
        addStringField(pInstruction.getMustContainStringVariable(), 0);
        addIntegerField(pInstruction.getFontSizeVariable(), 1);
        addIntegerField(pInstruction.getStartZPlaneIndex(), 2);
        addIntegerField(pInstruction.getEndZPlaneIndex(), 3);

        addCheckbox(pInstruction.getPrintSequenceNameVariable(), 4);
        addCheckbox(pInstruction.getPrintTimePointVariable(), 5);


    }
}
