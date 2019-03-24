package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.DownsampleByHalfMedianInstruction;

public class DownsampleByHalfMedianInstructionPanel extends CustomGridPane {
    public DownsampleByHalfMedianInstructionPanel (DownsampleByHalfMedianInstruction instruction) {
        addCheckbox(instruction.getRecycleSavedContainers(), 0);
    }
}
