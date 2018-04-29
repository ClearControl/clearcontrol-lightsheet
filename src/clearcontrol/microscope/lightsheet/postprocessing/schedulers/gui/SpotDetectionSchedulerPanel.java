package clearcontrol.microscope.lightsheet.postprocessing.schedulers.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.schedulers.SpotDetectionScheduler;
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
    public SpotDetectionSchedulerPanel(SpotDetectionScheduler<StackInterfaceContainer> pSpotDetectionScheduler) {

        addDoubleField(pSpotDetectionScheduler.getXYDownsamplingFactor(), 0);
        addDoubleField(pSpotDetectionScheduler.getZDownsamplingFactor(), 1);

        addIntegerField(pSpotDetectionScheduler.getDoGRadius(), 2);
        addDoubleField(pSpotDetectionScheduler.getDoGSigmaMinued(), 3);
        addDoubleField(pSpotDetectionScheduler.getDoGSigmaSubtrahend(), 4);

        addDoubleField(pSpotDetectionScheduler.getBlurSigma(), 5);
        addIntegerField(pSpotDetectionScheduler.getBlurRadius(), 6);

        addDoubleField(pSpotDetectionScheduler.getThreshold(), 7);

        addCheckbox(pSpotDetectionScheduler.getShowIntermediateResults(), 8);
    }
}
