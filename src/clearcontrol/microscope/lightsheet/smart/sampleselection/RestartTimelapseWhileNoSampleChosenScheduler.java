package clearcontrol.microscope.lightsheet.smart.sampleselection;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;

/**
 * RestartTimelapseWhileNoSampleChosenScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class RestartTimelapseWhileNoSampleChosenScheduler extends SchedulerBase {

    private final LightSheetMicroscope mLightSheetMicroscope;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public RestartTimelapseWhileNoSampleChosenScheduler(LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Restart timelapse while no sample is chosen");
        mLightSheetMicroscope = pLightSheetMicroscope;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        SpaceTravelScheduler spaceTravelScheduler = mLightSheetMicroscope.getDevice(SpaceTravelScheduler.class, 0);
        if (spaceTravelScheduler.getTravelPathList().size() > 1) {
            mLightSheetMicroscope.getTimelapse().getLastExecutedSchedulerIndexVariable().set(-1);
        }
        return true;
    }
}
