package clearcontrol.microscope.lightsheet.timelapse.schedulers;

import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

/**
 * MultipleExecutorScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class MultipleExecutorScheduler extends SchedulerBase {
    private final SchedulerInterface[] schedulersToExecute;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public MultipleExecutorScheduler(SchedulerInterface[] schedulersToExecute) {
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
