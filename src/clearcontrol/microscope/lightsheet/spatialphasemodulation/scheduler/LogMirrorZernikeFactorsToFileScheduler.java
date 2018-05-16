package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FWriter;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import org.ejml.data.DenseMatrix64F;

import java.io.File;

/**
 * The LogMirrorModeToFileScheduler loads JSON files representing Zernike factors from a given folder. Whenever it is
 * called, it sends one Zernike mode to the mirror.
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class LogMirrorZernikeFactorsToFileScheduler extends SchedulerBase implements LoggingFeature {

    private final SpatialPhaseModulatorDeviceInterface mMirror;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public LogMirrorZernikeFactorsToFileScheduler(SpatialPhaseModulatorDeviceInterface pMirror) {
        super("IO: Log Zernike factors of " + pMirror + " to disc");
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

        File lTargetFile = new File(lFolder, mMirror.getName() + "_zernike_factors_" + pTimePoint + ".json");

        DenseMatrix64F lZernikeFactorsVector = TransformMatrices.convert1DDoubleArrayToDense64RowMatrix(mMirror.getZernikeFactors());

        return new DenseMatrix64FWriter(lTargetFile, lZernikeFactorsVector).write();
    }
}
