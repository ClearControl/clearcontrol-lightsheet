package clearcontrol.microscope.lightsheet.timelapse.schedulers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleWriter;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
        new ScheduleWriter(mTimelapse.getListOfActivatedSchedulers(), new File(mTimelapse.getWorkingDirectory(), "program" + mTimelapse.getTimePointCounterVariable().get() + ".txt")).write();
        return true;
    }
}
