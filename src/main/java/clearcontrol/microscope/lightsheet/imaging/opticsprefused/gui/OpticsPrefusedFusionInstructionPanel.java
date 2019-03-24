package clearcontrol.microscope.lightsheet.imaging.opticsprefused.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedFusionInstruction;

public class OpticsPrefusedFusionInstructionPanel extends CustomGridPane {
    public OpticsPrefusedFusionInstructionPanel(OpticsPrefusedFusionInstruction instruction) {
        addCheckbox(instruction.getRecycleSavedContainers(), 0);
    }
}
