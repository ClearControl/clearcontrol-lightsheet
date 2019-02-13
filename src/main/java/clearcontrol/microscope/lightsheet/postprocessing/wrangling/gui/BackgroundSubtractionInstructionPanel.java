package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.BackgroundSubtractionInstruction;

/**
 * BackgroundSubtractionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class BackgroundSubtractionInstructionPanel extends CustomGridPane {
    public BackgroundSubtractionInstructionPanel(BackgroundSubtractionInstruction instruction) {
        addDoubleField(instruction.getBackgroundDeterminationBlurSigmaXY(), 0);
    }
}
