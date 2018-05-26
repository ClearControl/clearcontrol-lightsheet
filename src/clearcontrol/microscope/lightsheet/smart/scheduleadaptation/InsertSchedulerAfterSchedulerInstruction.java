package clearcontrol.microscope.lightsheet.smart.scheduleadaptation;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

import java.util.ArrayList;

/**
 * The InsertSchedulerAfterSchedulerInstruction inserts a given instructions after any instructions of a certain class.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class InsertSchedulerAfterSchedulerInstruction<T extends SchedulerInterface> extends InstructionBase {
    private final LightSheetMicroscope lightSheetMicroscope;
    private final SchedulerInterface schedulerToInsert;
    private final Class<T> schedulerToInsertBefore;

    public InsertSchedulerAfterSchedulerInstruction(LightSheetMicroscope lightSheetMicroscope, SchedulerInterface schedulerToInsert, Class<T> schedulerToInsertBefore) {
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

        // add myself to the instructions so that I'll be asked again after next imaging sequence
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
