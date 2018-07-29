package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;

import java.util.Random;

public class RandomZernikesInstruction extends InstructionBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private BoundedVariable<Double>[] mRangeOfZernikeCoeffArray;



    private Random mRandom = new Random();

    public RandomZernikesInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptive optics: Send random Zernike modes to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
        mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;

        mRangeOfZernikeCoeffArray = new BoundedVariable[mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors().length];
        for(int i = 0; i < mRangeOfZernikeCoeffArray.length; i++) {
            mRangeOfZernikeCoeffArray[i] = new BoundedVariable<Double>("Zernike Coeff Pos/Neg Range", 0.0, 0.0, 5.0, 0.0000001);
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
            double value = (-(mRangeOfZernikeCoeffArray[i].get()) + (mRangeOfZernikeCoeffArray[i].get() - (-mRangeOfZernikeCoeffArray[i].get())) * mRandom.nextDouble());
            lArray[i] = value;
        }

        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);
        return true;
    }

    @Override
    public RandomZernikesInstruction copy() {
        return new RandomZernikesInstruction(mZernikeModeFactorBasedSpatialPhaseModulatorBase);
    }

    public BoundedVariable<Double> getRangeOfZernikeCoeffArray(int i) {
        return mRangeOfZernikeCoeffArray[i];
    }

}
