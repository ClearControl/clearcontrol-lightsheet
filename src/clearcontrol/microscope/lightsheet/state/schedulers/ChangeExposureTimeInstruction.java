package clearcontrol.microscope.lightsheet.state.schedulers;

import clearcontrol.instructions.InstructionBase;

/**
 * ChangeExposureTimeInstruction
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ChangeExposureTimeInstruction extends InstructionBase {
    double mExposureTimeInSeconds;

    public ChangeExposureTimeInstruction(double pExposureTimeInSeconds) {

        super("Adaptation: Change exposure time to " + pExposureTimeInSeconds + " s");
        mExposureTimeInSeconds = pExposureTimeInSeconds;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        mMicroscope.getAcquisitionStateManager().getCurrentState().getExposureInSecondsVariable().set(mExposureTimeInSeconds);
        return true;
    }
}
