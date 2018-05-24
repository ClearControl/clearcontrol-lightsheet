package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.dmp40;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import dmp40j.DMP40JDevice;

/**
 * DMP40Device
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DMP40Device extends ZernikeModeFactorBasedSpatialPhaseModulatorBase
        implements LoggingFeature {

    DMP40JDevice mDMP40JDevice;
    public DMP40Device(String pSerialNumber) {
        super("DMP40 " + pSerialNumber, 1, 1, 1);

        mDMP40JDevice = new DMP40JDevice(pSerialNumber);
    }

    @Override
    public void zero() {
        mDMP40JDevice.sendFlatMirrorShapeVector();
    }

    @Override
    public long getRelaxationTimeInMilliseconds() {
        return mDMP40JDevice.getRelaxSteps(); // Todo: steps == seconds?
    }

    @Override
    public boolean setZernikeFactors(double[] pZernikeFactors) {
        mDMP40JDevice.setZernikeFactors(pZernikeFactors);
        super.setZernikeFactorsInternal(pZernikeFactors);
        return false;
    }

    @Override
    public boolean start() {
        boolean result = mDMP40JDevice.open();

        if (result) {
            setZernikeFactors(new double[mDMP40JDevice.getNumberOfZernikeFactors()]);
        }
        return result;
    }

    @Override
    public boolean stop() {
        if (mDMP40JDevice.isOpen()) {
            try {
                mDMP40JDevice.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public double getMinZernikeAmplitude() {
        return mDMP40JDevice.getMinZernikeAmplitude();
    }

    public double getMaxZernikeAmplitude() {
        return mDMP40JDevice.getMaxZernikeAmplitude();
    }
}
