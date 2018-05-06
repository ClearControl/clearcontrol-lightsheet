package clearcontrol.microscope.lightsheet.timelapse.schedulers;

import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.timelapse.TimelapseInterface;

/**
 * The TimelapseStopScheduler stops the running time lapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class TimelapseStopScheduler extends SchedulerBase {
    public TimelapseStopScheduler() {
        super("Smart: Stop timelapse");
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        TimelapseInterface lTimelapse = (TimelapseInterface) mMicroscope.getDevice(TimelapseInterface.class, 0);
        if (lTimelapse != null) {
            lTimelapse.stopTimelapse();
        }
        return true;
    }
}
