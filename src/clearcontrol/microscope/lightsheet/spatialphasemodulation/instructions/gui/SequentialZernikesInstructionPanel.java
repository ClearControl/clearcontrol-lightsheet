package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.SequentialZernikesInstruction;

/**
 * SequentialZernikesInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class SequentialZernikesInstructionPanel extends CustomGridPane {
    public SequentialZernikesInstructionPanel(SequentialZernikesInstruction pInstruction)
    {
        addDoubleField(pInstruction.getMinimumZernikeCoefficientVariable(), 0 );
        addDoubleField(pInstruction.getMaximumZernikeCoefficientVariable(), 1 );
        addDoubleField(pInstruction.getStepperVariable(), 2 );
        addDoubleField(pInstruction.getInitialValueVariable(), 3 );

        addIntegerField(pInstruction.getStartingModeVariable(), 4 );
        addIntegerField(pInstruction.getEndingModeVariable(), 5 );
        addIntegerField(pInstruction.getChangingModeVariable(), 6 );
    }
}
