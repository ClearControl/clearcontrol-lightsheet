package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.SetZernikeModeInstruction;

/**
 * SetZernikeModeInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 07 2018
 */
public class SetZernikeModeInstructionPanel extends CustomGridPane {
    public SetZernikeModeInstructionPanel (SetZernikeModeInstruction pInstruction) {
        BoundedVariable<Double>[] lZernikeFactors = pInstruction.getZernikeFactorVariables();
        for (int i = 0; i < lZernikeFactors.length; i++ ) {
            addDoubleField(lZernikeFactors[i], i);
        }
    }
}
