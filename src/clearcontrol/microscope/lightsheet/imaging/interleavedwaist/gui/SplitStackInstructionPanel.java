package clearcontrol.microscope.lightsheet.imaging.interleavedwaist.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.interleavedwaist.SplitStackInstruction;

/**
 * SplitStackInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class SplitStackInstructionPanel extends CustomGridPane {
    public SplitStackInstructionPanel(SplitStackInstruction instruction) {
        addIntegerField(instruction.getNumberOfStacks(), 0);
    }
}
