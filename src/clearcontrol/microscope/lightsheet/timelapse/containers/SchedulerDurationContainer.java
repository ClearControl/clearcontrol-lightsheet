package clearcontrol.microscope.lightsheet.timelapse.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.postprocessing.containers.MeasurementContainer;
import clearcontrol.microscope.lightsheet.postprocessing.containers.MeasurementInSpaceContainer;

/**
 * SchedulerDurationContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class SchedulerDurationContainer extends MeasurementContainer {
    private final SchedulerInterface mSchedulerInterface;

    public SchedulerDurationContainer(long pTimePoint, SchedulerInterface pSchedulerInterface, double pDurationInMilliseconds) {
        super(pTimePoint, pDurationInMilliseconds);
        mSchedulerInterface = pSchedulerInterface;
    }

    public SchedulerInterface getSchedulerInterface() {
        return mSchedulerInterface;
    }

    public double getDurationInMilliSeconds() {
        return getMeasurement();
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + mSchedulerInterface + "] " + getDurationInMilliSeconds() + " ms";
    }
}
