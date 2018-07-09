package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;

import java.util.Random;

/**
 * SetZernikeModeInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 07 2018
 */
public class SetZernikeModeInstruction  extends InstructionBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private BoundedVariable<Double> mZernikeFactors[];

    public SetZernikeModeInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptive optics: Send given Zernike modes to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
        mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;

        double[] zernikeFactors = mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors();

        mZernikeFactors = new BoundedVariable[zernikeFactors.length];

        for (int i = 0; i < zernikeFactors.length; i++) {
            mZernikeFactors[i] = new BoundedVariable<Double>("Z" + ZernikePolynomials.jNoll(i) + ": " + ZernikePolynomials.getZernikeModeName(i), zernikeFactors[i], -5.0, 5.0, 0.0001);
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
            double value = mZernikeFactors[i].get();
            lArray[i] = value;
        }

        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);
        return true;
    }

    @Override
    public SetZernikeModeInstruction copy() {
        return new SetZernikeModeInstruction(mZernikeModeFactorBasedSpatialPhaseModulatorBase);
    }

    public BoundedVariable<Double>[] getZernikeFactorVariables() {
        return mZernikeFactors;
    }
}
