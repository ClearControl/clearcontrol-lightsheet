package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.dmp40;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;
import dmp40j.DMP40JDevice;

import static dmp40j.DMP40J.*;

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

        mDMP40JDevice.setZernikeFactors(convertZernikeArray(pZernikeFactors));
        super.setZernikeFactorsInternal(pZernikeFactors);
        return false;
    }


    /**
     * We assume Zernike factors being entered in Noll-Order
     * @param inputZernikeFactors
     * @return
     */
    private double[] convertZernikeArray(double[] inputZernikeFactors) {
        double[] output = new double[mDMP40JDevice.getNumberOfZernikeFactors()];

        for (int i = 0; i < inputZernikeFactors.length; i++) {
            int jNollZ = ZernikePolynomials.jNoll(i);
            switch (jNollZ) {
                case 4:
                    output[TLDFMX_Z4_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 5:
                    output[TLDFMX_Z5_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 6:
                    output[TLDFMX_Z6_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 7:
                    output[TLDFMX_Z7_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 8:
                    output[TLDFMX_Z8_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 9:
                    output[TLDFMX_Z9_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 10:
                    output[TLDFMX_Z10_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 11:
                    output[TLDFMX_Z11_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 12:
                    output[TLDFMX_Z12_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 13:
                    output[TLDFMX_Z13_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 14:
                    output[TLDFMX_Z14_AMPL_POS] = inputZernikeFactors[i];
                    break;
                case 15:
                    output[TLDFMX_Z15_AMPL_POS] = inputZernikeFactors[i];
                    break;
                default:
                    if (Math.abs(inputZernikeFactors[i]) > 0.0001) {
                        warning("Ignoring Z" + jNollZ + " = " + inputZernikeFactors[i]);
                    }
                    break;
            }
        }
        return output;
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
