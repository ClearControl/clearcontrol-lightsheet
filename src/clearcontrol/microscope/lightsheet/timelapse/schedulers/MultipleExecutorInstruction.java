package clearcontrol.microscope.lightsheet.timelapse.schedulers;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.SchedulerInterface;

/**
 * MultipleExecutorInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class MultipleExecutorInstruction extends InstructionBase {
    private final SchedulerInterface[] schedulersToExecute;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public MultipleExecutorInstruction(SchedulerInterface[] schedulersToExecute) {
        super("Smart: Execute several schedulers " + schedulersToExecute);
        this.schedulersToExecute = schedulersToExecute;
    }

    @Override
    public boolean initialize() {
        for (SchedulerInterface scheduler : schedulersToExecute) {
            scheduler.setMicroscope(mMicroscope);
            scheduler.initialize();
        }
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        for (SchedulerInterface scheduler : schedulersToExecute) {
            scheduler.enqueue(pTimePoint);
        }
        return false;
    }
}
