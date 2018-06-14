package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.HalfStackMaxProjectionInstruction;
import javafx.scene.control.TextField;


/**
 * HalfStackMaxProjectionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class HalfStackMaxProjectionInstructionPanel extends CustomGridPane {
    public HalfStackMaxProjectionInstructionPanel(HalfStackMaxProjectionInstruction pHalfStackMaxProjectionInstruction) {
        addCheckbox(pHalfStackMaxProjectionInstruction.getViewFront(), 0);

        StringVariableTextField lTextField = new StringVariableTextField(pHalfStackMaxProjectionInstruction.getMustContainStringVariable().getName(), pHalfStackMaxProjectionInstruction.getMustContainStringVariable());
        add(lTextField.getLabel(), 0, 1);
        add(lTextField.getTextField(), 1, 1);

        addIntegerField(pHalfStackMaxProjectionInstruction.getFontSizeVariable(), 3);
    }
}
