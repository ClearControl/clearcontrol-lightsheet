package clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.CountsSpotsScheduler;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * SpotDetectionSchedulerPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class SpotDetectionSchedulerPanel extends CustomGridPane {
    public SpotDetectionSchedulerPanel(CountsSpotsScheduler<StackInterfaceContainer> pCountsSpotsScheduler) {

        addDoubleField(pCountsSpotsScheduler.getXYDownsamplingFactor(), 0);
        addDoubleField(pCountsSpotsScheduler.getZDownsamplingFactor(), 1);

        addIntegerField(pCountsSpotsScheduler.getDoGRadius(), 2);
        addDoubleField(pCountsSpotsScheduler.getDoGSigmaMinued(), 3);
        addDoubleField(pCountsSpotsScheduler.getDoGSigmaSubtrahend(), 4);

        addDoubleField(pCountsSpotsScheduler.getBlurSigma(), 5);
        addIntegerField(pCountsSpotsScheduler.getBlurRadius(), 6);

        addDoubleField(pCountsSpotsScheduler.getThreshold(), 7);

        addCheckbox(pCountsSpotsScheduler.getShowIntermediateResults(), 8);
    }
}
