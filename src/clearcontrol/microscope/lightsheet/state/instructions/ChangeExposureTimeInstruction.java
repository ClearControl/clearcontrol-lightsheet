package clearcontrol.microscope.lightsheet.state.instructions;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;

/**
 * ChangeExposureTimeInstruction
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ChangeExposureTimeInstruction extends LightSheetMicroscopeInstructionBase {
    double mExposureTimeInSeconds;

    public ChangeExposureTimeInstruction(double pExposureTimeInSeconds, LightSheetMicroscope pLightSheetMicroscope) {

        super("Adaptation: Change exposure time to " + pExposureTimeInSeconds + " s", pLightSheetMicroscope);
        mExposureTimeInSeconds = pExposureTimeInSeconds;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState().getExposureInSecondsVariable().set(mExposureTimeInSeconds);
        return true;
    }
}
