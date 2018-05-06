package clearcontrol.microscope.lightsheet.timelapse.schedulers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

/**
 * TimelapseLogScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class TimelapseLogScheduler  extends SchedulerBase {
    private final LightSheetTimelapse mTimelapse;

    public TimelapseLogScheduler(LightSheetMicroscope pLightSheetMicroscope) {
        super("State: Log content of the timelapse schedule");

        mTimelapse = pLightSheetMicroscope.getTimelapse();
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        int i = 0;
        mTimelapse.log("Timelapse schedule contains:");
        for (SchedulerInterface scheduler : mTimelapse.getListOfActivatedSchedulers()) {
            mTimelapse.log("[" + i + "] \t" + scheduler);
            i++;
        }
        return false;
    }
}
