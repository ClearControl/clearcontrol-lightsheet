package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.RandomZernikesInstruction;

/**
 * RandomZernikesInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class RandomZernikesInstructionPanel extends CustomGridPane {
    public RandomZernikesInstructionPanel(RandomZernikesInstruction pInstruction) {
        addDoubleField(pInstruction.getMinimumZernikeFactor(), 0);
        addDoubleField(pInstruction.getMaximumZernikeFactor(), 1);
    }
}
