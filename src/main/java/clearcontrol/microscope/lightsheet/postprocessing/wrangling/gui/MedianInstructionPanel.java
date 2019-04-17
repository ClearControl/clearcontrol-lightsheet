package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.MedianInstruction;

public class MedianInstructionPanel extends CustomGridPane {
    public MedianInstructionPanel(MedianInstruction instruction) {
        addIntegerField(instruction.getRadius(), 0);
        addCheckbox(instruction.getRecycleSavedContainers(), 1);
    }
}
