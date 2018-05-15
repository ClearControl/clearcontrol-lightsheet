package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.fitness;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.iqm.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.imaging.SinglePlaneImager;
import clearcontrol.microscope.lightsheet.imaging.SingleStackImager;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.MirrorModeContainer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.schedulers.AcquisitionStateBackupRestoreScheduler;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;
import clearcontrol.microscope.state.AcquisitionStateInterface;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.stack.StackInterface;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
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
    private DenseMatrix64F mMatrix = null;
    private double[] mFactors = null;
    private final double mPositionZ;

    // Output
    private double mQuality;

    public MirrorModeImageQualityDeterminer(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ, double[] pFactors) {
        mLightSheetMicroscope = pLightSheetMicroscope;
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mFactors = pFactors;
        mPositionZ = pPositionZ;
    }

    public MirrorModeImageQualityDeterminer(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ, DenseMatrix64F pMatrix) {
        mLightSheetMicroscope = pLightSheetMicroscope;
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mMatrix = pMatrix;
        mPositionZ = pPositionZ;
    }


    private MirrorModeContainer getMirrorModeContainer(String key) {
        DataContainerInterface lContainer = mLightSheetMicroscope.getDataWarehouse().get(mSpatialPhaseModulatorDeviceInterface.getName() + "_" + key);
        if (lContainer instanceof MirrorModeContainer) {
            return (MirrorModeContainer) lContainer;
        } else {

            warning("No '" + key + "' mirror matix found! Reading from disc");
            File lMirrorModeDirectory =
                    MachineConfiguration.get()
                            .getFolder("MirrorModes");

            DenseMatrix64F lFlatMirrorMatrix = mSpatialPhaseModulatorDeviceInterface.getMatrixReference().get().copy();

            new DenseMatrix64FReader(new File(lMirrorModeDirectory, mSpatialPhaseModulatorDeviceInterface.getName() + "_flat.json"), lFlatMirrorMatrix).read();

            MirrorModeContainer lNewContainer = new MirrorModeContainer(mLightSheetMicroscope.getTimelapse().getTimePointCounterVariable().get());
            lNewContainer.setMirrorMode(lFlatMirrorMatrix);
            mLightSheetMicroscope.getDataWarehouse().put(mSpatialPhaseModulatorDeviceInterface.getName() + "_" + key, lNewContainer);
            return (MirrorModeContainer) lContainer;
        }
    }

    private void determineQuality()
    {
        if (mMatrix != null ) {

            warning("Quality is determined from a matrix instead of an array of Zernike Factors! This functionality will be removed in the future!");
            DenseMatrix64F lMatrixToTest = mMatrix;

            DataContainerInterface lActuatorInfluenceMatrixContainer = getMirrorModeContainer("actuator_influence");
            if (lActuatorInfluenceMatrixContainer != null) {
                DenseMatrix64F lActuatorInfluenceMatrix = ((MirrorModeContainer) lActuatorInfluenceMatrixContainer).getMirrorMode();
                lMatrixToTest = TransformMatrices.multiplyElementWise(lMatrixToTest, lActuatorInfluenceMatrix);
            } else {
                warning("No actuator influence matrix available! Mirror shape may be wrong");
            }

            DataContainerInterface lFlatMirrorModeContainer = getMirrorModeContainer("flat");
            if (lFlatMirrorModeContainer != null) {
                DenseMatrix64F lFlatMirrorMatrix = ((MirrorModeContainer) lFlatMirrorModeContainer).getMirrorMode();
                lMatrixToTest = TransformMatrices.sum(lMatrixToTest, lFlatMirrorMatrix);
            } else {
                warning("No flat mirror matrix available! Mirror shape may be wrong");
            }

            mSpatialPhaseModulatorDeviceInterface.getMatrixReference().set(lMatrixToTest);
            //backupState();

            InterpolatedAcquisitionState currentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getDevice(AcquisitionStateManager.class, 0).getCurrentState();
            double minZ = currentState.getStackZLowVariable().get().doubleValue();
            double maxZ = currentState.getStackZHighVariable().get().doubleValue();
            double stepZ = currentState.getStackZStepVariable().get().doubleValue();

            SingleViewPlaneImager lImager = new SingleViewPlaneImager(mLightSheetMicroscope, mPositionZ);
            lImager.setImageWidth(currentState.getImageWidthVariable().get().intValue());
            lImager.setImageHeight(currentState.getImageHeightVariable().get().intValue());
            lImager.setExposureTimeInSeconds(currentState.getExposureInSecondsVariable().get().doubleValue());
            lImager.setDetectionArmIndex(0);
            lImager.setLightSheetIndex(0);
            StackInterface lStack = lImager.acquire();

            DiscreteConsinusTransformEntropyPerSliceEstimator lQualityEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
            mQuality = lQualityEstimator.getQualityArray()[0];

            //restoreState();
            currentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getDevice(AcquisitionStateManager.class, 0).getCurrentState();
            currentState.getStackZLowVariable().set(minZ);
            currentState.getStackZHighVariable().set(maxZ);
            currentState.getStackZStepVariable().set(stepZ);

        } else {

            mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(mFactors);
            //backupState();
            InterpolatedAcquisitionState currentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getDevice(AcquisitionStateManager.class, 0).getCurrentState();
            double minZ = currentState.getStackZLowVariable().get().doubleValue();
            //double maxZ = currentState.getStackZHighVariable().get().doubleValue();
            double stepZ = currentState.getStackZStepVariable().get().doubleValue();

            SingleStackImager lImager = new SingleStackImager(mLightSheetMicroscope);
            lImager.setDetectionZ(minZ);
            lImager.setIlluminationZ(minZ);
            lImager.setDetectionZStepDistance(stepZ);
            lImager.setIlluminationZStepDistance(stepZ);
            lImager.setImageWidth(currentState.getImageWidthVariable().get().intValue());
            lImager.setImageHeight(currentState.getImageHeightVariable().get().intValue());
            lImager.setExposureTimeInSeconds(currentState.getExposureInSecondsVariable().get().doubleValue());
            StackInterface lStack = lImager.acquire();

            DiscreteConsinusTransformEntropyPerSliceEstimator lQualityEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
            mQuality = new Mean().evaluate(lQualityEstimator.getQualityArray());

            //restoreState();
            //currentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getDevice(AcquisitionStateManager.class, 0).getCurrentState();
            //currentState.getStackZLowVariable().set(minZ);
            //currentState.getStackZHighVariable().set(maxZ);
            //currentState.getStackZStepVariable().set(stepZ);
        }
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
