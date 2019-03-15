package clearcontrol.microscope.lightsheet.timelapse.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.timelapse.instructions.TimelapseStopAfterNIterationsInstruction;

/**
 * TimelapseStopAfterNIterationsInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 03 2019
 */
public class TimelapseStopAfterNIterationsInstructionPanel extends CustomGridPane {
    public TimelapseStopAfterNIterationsInstructionPanel (TimelapseStopAfterNIterationsInstruction instruction) {
        addIntegerField(instruction.getMaximumCount(), 0);
    }
}
