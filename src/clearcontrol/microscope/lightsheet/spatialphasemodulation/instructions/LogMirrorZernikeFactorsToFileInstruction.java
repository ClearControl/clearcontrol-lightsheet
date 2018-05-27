package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
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
public class LogMirrorZernikeFactorsToFileInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private final SpatialPhaseModulatorDeviceInterface mMirror;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public LogMirrorZernikeFactorsToFileInstruction(SpatialPhaseModulatorDeviceInterface pMirror, LightSheetMicroscope pLightSheetMicroscope) {
        super("IO: Log Zernike factors of " + pMirror + " to disc", pLightSheetMicroscope);
        mMirror = pMirror;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        File lFolder = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory();

        File lTargetFile = new File(lFolder, mMirror.getName() + "_zernike_factors_" + pTimePoint + ".json");

        DenseMatrix64F lZernikeFactorsVector = TransformMatrices.convert1DDoubleArrayToDense64RowMatrix(mMirror.getZernikeFactors());

        return new DenseMatrix64FWriter(lTargetFile, lZernikeFactorsVector).write();
    }

    @Override
    public LogMirrorZernikeFactorsToFileInstruction copy() {
        return new LogMirrorZernikeFactorsToFileInstruction(mMirror, getLightSheetMicroscope());
    }
}
