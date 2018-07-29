package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;

import java.util.Random;

public class MakeMirrorFlatInstruction extends InstructionBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private BoundedVariable<Double> mMaximumZernikeFactor = new BoundedVariable<Double>("Maximum Zernike factor", 5.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0000001);
    private BoundedVariable<Double> mMinimumZernikeFactor = new BoundedVariable<Double>("Minimum Zernike factor", -5.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0000001);
    private Random mRandom = new Random();

    public MakeMirrorFlatInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptive optics: Send flat mode to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
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
            lArray[i] = 0;
        }
        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);
        return true;
    }

    @Override
    public MakeMirrorFlatInstruction copy() {
        return new MakeMirrorFlatInstruction(mZernikeModeFactorBasedSpatialPhaseModulatorBase);
    }
}