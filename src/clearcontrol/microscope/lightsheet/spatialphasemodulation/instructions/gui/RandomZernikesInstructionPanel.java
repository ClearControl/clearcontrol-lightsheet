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
        for(int i = 0; i<66;i++) {
            addDoubleField(pInstruction.getRangeOfZernikeCoeffArray(i), i);
        }
    }
}
