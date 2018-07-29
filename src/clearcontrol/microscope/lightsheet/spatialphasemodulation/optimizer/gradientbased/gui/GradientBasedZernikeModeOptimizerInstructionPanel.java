package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.gradientbased.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.gradientbased.GradientBasedZernikeModeOptimizerInstruction;

public class GradientBasedZernikeModeOptimizerInstructionPanel extends CustomGridPane {
    public GradientBasedZernikeModeOptimizerInstructionPanel(GradientBasedZernikeModeOptimizerInstruction pInstruction) {
        addDoubleField(pInstruction.getStepSize(), 0 );
    }
}
