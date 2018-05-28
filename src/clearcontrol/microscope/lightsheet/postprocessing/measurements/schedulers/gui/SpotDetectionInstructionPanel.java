package clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.CountsSpotsInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * SpotDetectionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class SpotDetectionInstructionPanel extends CustomGridPane {
    public SpotDetectionInstructionPanel(CountsSpotsInstruction<StackInterfaceContainer> pCountsSpotsInstruction) {

        addDoubleField(pCountsSpotsInstruction.getXYDownsamplingFactor(), 0);
        addDoubleField(pCountsSpotsInstruction.getZDownsamplingFactor(), 1);

        addIntegerField(pCountsSpotsInstruction.getDoGRadius(), 2);
        addDoubleField(pCountsSpotsInstruction.getDoGSigmaMinued(), 3);
        addDoubleField(pCountsSpotsInstruction.getDoGSigmaSubtrahend(), 4);

        addDoubleField(pCountsSpotsInstruction.getBlurSigma(), 5);
        addIntegerField(pCountsSpotsInstruction.getBlurRadius(), 6);

        addDoubleField(pCountsSpotsInstruction.getThreshold(), 7);

        addCheckbox(pCountsSpotsInstruction.getShowIntermediateResults(), 8);
    }
}