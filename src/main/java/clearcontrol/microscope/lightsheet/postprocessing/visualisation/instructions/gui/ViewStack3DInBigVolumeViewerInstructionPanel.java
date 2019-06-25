package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.ViewStack3DInBigVolumeViewerInstruction;

/**
 * ViewStack3DInBigVolumeViewerInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2019
 */
public class ViewStack3DInBigVolumeViewerInstructionPanel extends CustomGridPane {
    public ViewStack3DInBigVolumeViewerInstructionPanel(ViewStack3DInBigVolumeViewerInstruction instruction) {
        addDoubleField(instruction.min, 0);
        addDoubleField(instruction.max, 1);
    }
}
