package clearcontrol.microscope.lightsheet.postprocessing.measurements.instructions.gui;

import autopilot.measures.FocusMeasures;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.instructions.MeasureImageQualityInstruction;

import java.util.HashMap;

public class MeasureImageQualityInstructionPanel extends CustomGridPane {
    public MeasureImageQualityInstructionPanel(MeasureImageQualityInstruction pInstruction) {
        StringVariableTextField lTextField = new StringVariableTextField(pInstruction.getKeyMustContainString().getName(), pInstruction.getKeyMustContainString());
        add(lTextField.getLabel(), 0, 0);
        add(lTextField.getTextField(), 1, 0);

        HashMap<FocusMeasures.FocusMeasure, Variable<Boolean>> featureMap = pInstruction.getSelectedFeaturesMap();
        int i = 1;
        for (FocusMeasures.FocusMeasure focusMeasure : featureMap.keySet()) {
            addCheckbox(featureMap.get(focusMeasure), i);
            i++;
        }

    }
}
