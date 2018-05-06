package clearcontrol.microscope.lightsheet.smart.scheduleadaptation;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionScheduler;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.CountsSpotsScheduler;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

import java.util.ArrayList;

/**
 * The InsertSchedulerBeforeSchedulerScheduler inserts a given scheduler after any scheduler of a certain class.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class InsertSchedulerBeforeSchedulerScheduler<T extends SchedulerInterface> extends SchedulerBase {
    private final LightSheetMicroscope lightSheetMicroscope;
    private final SchedulerInterface schedulerToInsert;
    private final Class<T> schedulerToInsertBefore;

    public InsertSchedulerBeforeSchedulerScheduler(LightSheetMicroscope lightSheetMicroscope, SchedulerInterface schedulerToInsert, Class<T> schedulerToInsertBefore) {
        super("Smart: Insert " + schedulerToInsert + " before any " + schedulerToInsertBefore);
        this.lightSheetMicroscope = lightSheetMicroscope;
        this.schedulerToInsert = schedulerToInsert;
        this.schedulerToInsertBefore = schedulerToInsertBefore;
    }


    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {

        LightSheetTimelapse lTimelapse = ((LightSheetMicroscope) mMicroscope).getTimelapse();

        // add myself to the scheduler so that I'll be asked again after next imaging sequence
        ArrayList<SchedulerInterface> schedule = lTimelapse.getListOfActivatedSchedulers();
        for (int i = (int)pTimePoint; i < schedule.size() - 1; i++) {
            SchedulerInterface lScheduler = schedule.get(i);
            SchedulerInterface lFollowingScheduler = schedule.get(i + 1);
            if ((schedulerToInsertBefore.isInstance(lScheduler) && (lFollowingScheduler != schedulerToInsert))) {
                schedule.add(i + 1, schedulerToInsert);
                i++;
            }
        }


        return true;
    }
}
