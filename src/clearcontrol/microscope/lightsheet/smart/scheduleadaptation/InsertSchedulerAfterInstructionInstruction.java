package clearcontrol.microscope.lightsheet.smart.scheduleadaptation;

import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

import java.util.ArrayList;

/**
 * The InsertSchedulerAfterInstructionInstruction inserts a given instructions after any instructions of a certain class.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class InsertSchedulerAfterInstructionInstruction<T extends InstructionInterface> extends LightSheetMicroscopeInstruction {
    private final LightSheetMicroscope lightSheetMicroscope;
    private final InstructionInterface schedulerToInsert;
    private final Class<T> schedulerToInsertBefore;

    public InsertSchedulerAfterInstructionInstruction(LightSheetMicroscope lightSheetMicroscope, InstructionInterface schedulerToInsert, Class<T> schedulerToInsertBefore) {
        super("Smart: Insert " + schedulerToInsert + " before any " + schedulerToInsertBefore, lightSheetMicroscope);
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

        LightSheetTimelapse lTimelapse = lightSheetMicroscope.getTimelapse();

        // add myself to the instructions so that I'll be asked again after next imaging sequence
        ArrayList<InstructionInterface> schedule = lTimelapse.getListOfActivatedSchedulers();
        for (int i = (int)pTimePoint; i < schedule.size() - 1; i++) {
            InstructionInterface lScheduler = schedule.get(i);
            InstructionInterface lFollowingScheduler = schedule.get(i + 1);
            if ((schedulerToInsertBefore.isInstance(lScheduler) && (lFollowingScheduler != schedulerToInsert))) {
                schedule.add(i + 1, schedulerToInsert);
                i++;
            }
        }


        return true;
    }
}
