package clearcontrol.microscope.lightsheet.timelapse.schedulers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleWriter;

import java.io.File;

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
        super("IO: Log content of the timelapse schedule to disc");

        mTimelapse = pLightSheetMicroscope.getTimelapse();
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        new ScheduleWriter(mTimelapse.getListOfActivatedSchedulers(), new File(mTimelapse.getWorkingDirectory(), "program" + mTimelapse.getTimePointCounterVariable().get() + ".txt")).write();
        return true;
    }
}
