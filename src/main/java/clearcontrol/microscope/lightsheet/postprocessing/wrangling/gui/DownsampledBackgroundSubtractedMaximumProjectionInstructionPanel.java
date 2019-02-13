package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.BackgroundSubtractionInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.DownsampledBackgroundSubtractedMaximumProjectionInstruction;

/**
 * BackgroundSubtractionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class DownsampledBackgroundSubtractedMaximumProjectionInstructionPanel extends CustomGridPane {
    public DownsampledBackgroundSubtractedMaximumProjectionInstructionPanel(DownsampledBackgroundSubtractedMaximumProjectionInstruction instruction) {
        addDoubleField(instruction.getBackgroundDeterminationBlurSigmaXY(), 0);
    }
}
