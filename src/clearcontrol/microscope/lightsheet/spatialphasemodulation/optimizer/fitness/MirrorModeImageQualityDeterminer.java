package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.fitness;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.iqm.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.MirrorModeContainer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.state.schedulers.AcquisitionStateBackupRestoreScheduler;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;
import clearcontrol.stack.StackInterface;
import org.ejml.data.DenseMatrix64F;

import java.io.File;

/**
 * MirrorModeImageQualityDeterminer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class MirrorModeImageQualityDeterminer implements LoggingFeature {

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
        DenseMatrix64F lMatrixToTest = mMatrix;

        DataContainerInterface lContainer = mLightSheetMicroscope.getDataWarehouse().get(mSpatialPhaseModulatorDeviceInterface.getName() + "_flat");
        if (lContainer instanceof MirrorModeContainer) {
            DenseMatrix64F lFlatMirrorMatrix = ((MirrorModeContainer) lContainer).getMirrorMode();
            lMatrixToTest = TransformMatrices.sum(lMatrixToTest, lFlatMirrorMatrix);
        } else {
            warning("No flat mirror matix found! Reading from disc");
            File lMirrorModeDirectory =
                    MachineConfiguration.get()
                            .getFolder("MirrorModes");

            DenseMatrix64F lFlatMirrorMatrix = mSpatialPhaseModulatorDeviceInterface.getMatrixReference().get().copy();
            new DenseMatrix64FReader(new File(lMirrorModeDirectory, mSpatialPhaseModulatorDeviceInterface.getName() + "_flat.json"), lFlatMirrorMatrix).read();
            lMatrixToTest = TransformMatrices.sum(lMatrixToTest, lFlatMirrorMatrix);

            MirrorModeContainer lNewContainer = new MirrorModeContainer(mLightSheetMicroscope);
            lNewContainer.setMirrorMode(lFlatMirrorMatrix);
            mLightSheetMicroscope.getDataWarehouse().put(mSpatialPhaseModulatorDeviceInterface.getName() + "_flat", lNewContainer);
        }

        mSpatialPhaseModulatorDeviceInterface.getMatrixReference().set(lMatrixToTest);
        backupState();

        SingleViewPlaneImager lImager = new SingleViewPlaneImager(mLightSheetMicroscope, mPositionZ);
        lImager.setImageWidth(1024);
        lImager.setImageHeight(1024);
        lImager.setExposureTimeInSeconds(0.01);
        lImager.setDetectionArmIndex(0);
        lImager.setLightSheetIndex(0);
        StackInterface lStack = lImager.acquire();

        DiscreteConsinusTransformEntropyPerSliceEstimator lQualityEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
        mQuality = lQualityEstimator.getQualityArray()[0];

        restoreState();
    }

    public double getFitness() {
        determineQuality();
        return mQuality;
    }

    private void backupState() {
        for (AcquisitionStateBackupRestoreScheduler lScheduler : mLightSheetMicroscope.getDevices(AcquisitionStateBackupRestoreScheduler.class)) {
            if (lScheduler.isBackup()) {
                lScheduler.enqueue(-1);
            }
        }
    }

    private void restoreState() {
        for (AcquisitionStateBackupRestoreScheduler lScheduler : mLightSheetMicroscope.getDevices(AcquisitionStateBackupRestoreScheduler.class)) {
            if (!lScheduler.isBackup()) {
                lScheduler.enqueue(-1);
            }
        }
    }

}
