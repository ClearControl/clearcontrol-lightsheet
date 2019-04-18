package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.MeanInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.TopHatInstruction;

public class TopHatInstructionPanel extends CustomGridPane {
    public TopHatInstructionPanel(TopHatInstruction instruction) {
        addIntegerField(instruction.getRadiusXY(), 0);
        addIntegerField(instruction.getRadiusZ(), 1);
        addCheckbox(instruction.getRecycleSavedContainers(), 2);
    }
}
