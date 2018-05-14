package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms;

/**
 * ZernikeModeFactorBasedSpatialPhaseModulatorBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public abstract class ZernikeModeFactorBasedSpatialPhaseModulatorBase extends SpatialPhaseModulatorDeviceBase {

    private double[] mZernikeModeFactors;

    public ZernikeModeFactorBasedSpatialPhaseModulatorBase(String pDeviceName, int pFullMatrixWidthHeight, int pActuatorResolution, int pNumberOfZernikeFactors) {
        super(pDeviceName, pFullMatrixWidthHeight, pActuatorResolution);
        mZernikeModeFactors = new double[pNumberOfZernikeFactors];
    }


    @Override
    public double[] getZernikeFactors() {
        double[] resultArray = new double[mZernikeModeFactors.length];
        System.arraycopy(mZernikeModeFactors, 0, resultArray, 0, mZernikeModeFactors.length);
        return resultArray;
    }

    protected boolean setZernikeFactorsInternal(double[] pZernikeFactors) {
        System.arraycopy(pZernikeFactors, 0, mZernikeModeFactors, 0, Math.min(mZernikeModeFactors.length, pZernikeFactors.length));
        return true;
    }
}
