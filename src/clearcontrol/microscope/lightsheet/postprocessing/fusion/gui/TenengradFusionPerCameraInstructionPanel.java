package clearcontrol.microscope.lightsheet.postprocessing.fusion.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.fusion.TenengradFusionPerCameraInstruction;

/**
 * TenengradFusionPerCameraInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class TenengradFusionPerCameraInstructionPanel extends CustomGridPane {
    public TenengradFusionPerCameraInstructionPanel(TenengradFusionPerCameraInstruction instruction) {
        addDoubleField(instruction.getBlurWeightSigmaX(), 0);
        addDoubleField(instruction.getBlurWeightSigmaY(), 1);
        addDoubleField(instruction.getBlurWeightSigmaZ(), 2);
        addDoubleField(instruction.getWeightExponent(), 3);
    }
}
