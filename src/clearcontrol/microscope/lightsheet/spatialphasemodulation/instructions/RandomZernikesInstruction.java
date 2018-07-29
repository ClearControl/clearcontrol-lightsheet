package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;

import java.util.Random;

public class RandomZernikesInstruction extends InstructionBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private BoundedVariable<Double>[] mRangeOfZernikeCoefficientsArray;

    private Random mRandom = new Random();

    public RandomZernikesInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptive optics: Send random Zernike modes to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
        mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;

        mRangeOfZernikeCoefficientsArray = new BoundedVariable[mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors().length];

        for(int i = 0; i < mRangeOfZernikeCoefficientsArray.length; i++) {
            mRangeOfZernikeCoefficientsArray[i] = new BoundedVariable<Double>("Z" + ZernikePolynomials.jNoll(i) + "(" + ZernikePolynomials.getZernikeModeName(i) + ") -min/max", 0.0, 0.0, 5.0, 0.0000001);
        }
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        double[] lArray = mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors();

        for (int i = 0; i < lArray.length; i++) {
            double value = (-(mRangeOfZernikeCoefficientsArray[i].get()) + (mRangeOfZernikeCoefficientsArray[i].get() - (-mRangeOfZernikeCoefficientsArray[i].get())) * mRandom.nextDouble());
            lArray[i] = value;
        }

        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);
        return true;
    }

    @Override
    public RandomZernikesInstruction copy() {
        RandomZernikesInstruction copied = new RandomZernikesInstruction(mZernikeModeFactorBasedSpatialPhaseModulatorBase);

        for (int i = 0; i < mRangeOfZernikeCoefficientsArray.length; i++) {
            copied.mRangeOfZernikeCoefficientsArray[i] = mRangeOfZernikeCoefficientsArray[i];
        }

        return copied;
    }

    public BoundedVariable<Double> getRangeOfZernikeCoefficientArray(int i) {
        return mRangeOfZernikeCoefficientsArray[i];
    }

}
