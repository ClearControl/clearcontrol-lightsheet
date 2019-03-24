package clearcontrol.microscope.lightsheet.imaging.interleaved.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedFusionInstruction;

public class InterleavedFusionInstructionPanel extends CustomGridPane {
    public InterleavedFusionInstructionPanel(InterleavedFusionInstruction instruction) {
        addCheckbox(instruction.getRecycleSavedContainers(), 0);
    }
}
