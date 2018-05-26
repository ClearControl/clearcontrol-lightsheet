package clearcontrol.microscope.lightsheet.smart.sampleselection;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelInstruction;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstruction;

/**
 * RestartTimelapseWhileNoSampleChosenInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class RestartTimelapseWhileNoSampleChosenInstruction extends LightSheetMicroscopeInstruction {

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public RestartTimelapseWhileNoSampleChosenInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Restart timelapse while no sample is chosen", pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        SpaceTravelInstruction spaceTravelScheduler = getLightSheetMicroscope().getDevice(SpaceTravelInstruction.class, 0);
        if (spaceTravelScheduler.getTravelPathList().size() > 1) {
            getLightSheetMicroscope().getTimelapse().getLastExecutedSchedulerIndexVariable().set(-1);
        }
        return true;
    }
}
