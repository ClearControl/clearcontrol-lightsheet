package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FWriter;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

import java.io.File;

/**
 * LogMirrorModeToFileScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class LogMirrorModeToFileScheduler extends SchedulerBase implements LoggingFeature {

    private final SpatialPhaseModulatorDeviceInterface mMirror;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public LogMirrorModeToFileScheduler(SpatialPhaseModulatorDeviceInterface pMirror) {
        super("State: Log mode of " + pMirror + "to disc");
        mMirror = pMirror;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }

        File lFolder = ((LightSheetMicroscope) mMicroscope).getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory();

        File lTargetFile = new File(lFolder, "mirror" + pTimePoint + ".json");

        return new DenseMatrix64FWriter(lTargetFile, mMirror.getMatrixReference().get()).write();
    }
}
