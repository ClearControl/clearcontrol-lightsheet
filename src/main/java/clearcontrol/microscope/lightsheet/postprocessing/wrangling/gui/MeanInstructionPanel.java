package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.MeanInstruction;

public class MeanInstructionPanel extends CustomGridPane {
    public MeanInstructionPanel(MeanInstruction instruction) {
        addIntegerField(instruction.getRadiusXY(), 0);
        addIntegerField(instruction.getRadiusZ(), 0);
        addCheckbox(instruction.getRecycleSavedContainers(), 2);
    }
}
