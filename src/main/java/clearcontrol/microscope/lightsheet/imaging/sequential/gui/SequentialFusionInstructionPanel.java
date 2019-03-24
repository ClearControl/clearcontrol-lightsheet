package clearcontrol.microscope.lightsheet.imaging.sequential.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialFusionInstruction;

public class SequentialFusionInstructionPanel extends CustomGridPane {
    public SequentialFusionInstructionPanel(SequentialFusionInstruction instruction) {
        addCheckbox(instruction.getRecycleSavedContainers(), 0);
    }
}
