package clearcontrol.microscope.lightsheet.state.schedulers;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstruction;

/**
 * ChangeExposureTimeInstruction
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ChangeExposureTimeInstruction extends LightSheetMicroscopeInstruction {
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
