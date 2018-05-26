package clearcontrol.microscope.lightsheet.smart.sampleselection;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelInstruction;
import clearcontrol.instructions.InstructionBase;

/**
 * RestartTimelapseWhileNoSampleChosenInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class RestartTimelapseWhileNoSampleChosenInstruction extends InstructionBase {

    private final LightSheetMicroscope mLightSheetMicroscope;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public RestartTimelapseWhileNoSampleChosenInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Restart timelapse while no sample is chosen");
        mLightSheetMicroscope = pLightSheetMicroscope;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        SpaceTravelInstruction spaceTravelScheduler = mLightSheetMicroscope.getDevice(SpaceTravelInstruction.class, 0);
        if (spaceTravelScheduler.getTravelPathList().size() > 1) {
            mLightSheetMicroscope.getTimelapse().getLastExecutedSchedulerIndexVariable().set(-1);
        }
        return true;
    }
}
