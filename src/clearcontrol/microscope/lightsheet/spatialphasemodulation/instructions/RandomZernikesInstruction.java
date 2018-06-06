package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import net.imglib2.RandomAccessibleInterval;

import java.util.Random;

public class RandomZernikesInstruction extends InstructionBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private BoundedVariable<Double> mMaximumZernikeFactor = new BoundedVariable<Double>("Maximum Zernike factor", 5.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0000001);
    private BoundedVariable<Double> mMinimumZernikeFactor = new BoundedVariable<Double>("Minimum Zernike factor", -5.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0000001);
    private Random mRandom = new Random();

    public RandomZernikesInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptive optics: Send random Zernike modes to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
        mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        double[] lArray = mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors();

        for (int i = 0; i < lArray.length; i++) {
            double value = (mMinimumZernikeFactor.get() + (mMaximumZernikeFactor.get() - mMinimumZernikeFactor.get()) * mRandom.nextDouble());
            lArray[i] = value;
        }

        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);
        return true;
    }

    @Override
    public RandomZernikesInstruction copy() {
        return new RandomZernikesInstruction(mZernikeModeFactorBasedSpatialPhaseModulatorBase);
    }

    public BoundedVariable<Double> getMaximumZernikeFactor() {
        return mMaximumZernikeFactor;
    }

    public BoundedVariable<Double> getMinimumZernikeFactor() {
        return mMinimumZernikeFactor;
    }
}
