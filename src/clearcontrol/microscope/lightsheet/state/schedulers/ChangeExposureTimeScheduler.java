package clearcontrol.microscope.lightsheet.state.schedulers;

import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;

/**
 * ChangeExposureTimeScheduler
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ChangeExposureTimeScheduler extends SchedulerBase {
    double mExposureTimeInSeconds;

    public ChangeExposureTimeScheduler(double pExposureTimeInSeconds) {

        super("Adaptation: Change exposure time to " + pExposureTimeInSeconds + " s");
        mExposureTimeInSeconds = pExposureTimeInSeconds;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        mMicroscope.getAcquisitionStateManager().getCurrentState().getExposureInSecondsVariable().set(mExposureTimeInSeconds);
        return true;
    }
}
