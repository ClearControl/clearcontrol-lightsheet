package clearcontrol.microscope.lightsheet.timelapse.containers;

import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.containers.MeasurementContainer;

/**
 * InstructionDurationContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class InstructionDurationContainer extends MeasurementContainer {
    private final InstructionInterface mInstructionInterface;

    public InstructionDurationContainer(long pTimePoint, InstructionInterface pInstructionInterface, double pDurationInMilliseconds) {
        super(pTimePoint, pDurationInMilliseconds);
        mInstructionInterface = pInstructionInterface;
    }

    public InstructionInterface getSchedulerInterface() {
        return mInstructionInterface;
    }

    public double getDurationInMilliSeconds() {
        return getMeasurement();
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + mInstructionInterface + "] " + getDurationInMilliSeconds() + " ms";
    }
}