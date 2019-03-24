package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.MaximumProjectionInstruction;

public class MaximumProjectionInstructionPanel extends CustomGridPane {
    public MaximumProjectionInstructionPanel(MaximumProjectionInstruction instruction) {
        addCheckbox(instruction.getRecycleSavedContainers(), 0);
    }
}
