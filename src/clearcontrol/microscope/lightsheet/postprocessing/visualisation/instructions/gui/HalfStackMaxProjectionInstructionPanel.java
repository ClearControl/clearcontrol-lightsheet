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
        addCheckbox(pHalfStackMaxProjectionInstruction.getPrintSequenceNameVariable(), 1);
        addCheckbox(pHalfStackMaxProjectionInstruction.getPrintTimePointVariable(), 2);

        StringVariableTextField lTextField = new StringVariableTextField(pHalfStackMaxProjectionInstruction.getMustContainStringVariable().getName(), pHalfStackMaxProjectionInstruction.getMustContainStringVariable());
        add(lTextField.getLabel(), 0, 3);
        add(lTextField.getTextField(), 1, 3);

        addIntegerField(pHalfStackMaxProjectionInstruction.getFontSizeVariable(), 4);
        addDoubleField(pHalfStackMaxProjectionInstruction.getScalingVariable(), 5);
    }
}
