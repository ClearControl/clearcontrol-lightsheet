package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.fitness;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.iqm.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.stack.StackInterface;
import org.ejml.data.DenseMatrix64F;

/**
 * MirrorModeImageQualityDeterminer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class MirrorModeImageQualityDeterminer {

    // Input
    private final LightSheetMicroscope mLightSheetMicroscope;
    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
    private final DenseMatrix64F mMatrix;
    private final double mPositionZ;

    // Output
    private double mQuality;

    public MirrorModeImageQualityDeterminer(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ, DenseMatrix64F pMatrix) {
        mLightSheetMicroscope = pLightSheetMicroscope;
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mMatrix = pMatrix;
        mPositionZ = pPositionZ;
    }

    private void determineQuality()
    {
        mSpatialPhaseModulatorDeviceInterface.getMatrixReference().set(mMatrix);

        SingleViewPlaneImager lImager = new SingleViewPlaneImager(mLightSheetMicroscope, mPositionZ);
        StackInterface lStack = lImager.acquire();

        DiscreteConsinusTransformEntropyPerSliceEstimator lQualityEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
        mQuality = lQualityEstimator.getQualityArray()[0];
    }

    public double getFitness() {
        determineQuality();
        return mQuality;
    }

}
