package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.ViewStack2DInstruction;

/**
 * ViewStack2DInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class ViewStack2DInstructionPanel extends CustomGridPane {
    public ViewStack2DInstructionPanel( ViewStack2DInstruction pInstruction ){
        addStringField(pInstruction.getKeyToShowVariable(), 0);
        addIntegerField(pInstruction.getViewerIndexVariable(), 1 );
    }
}
