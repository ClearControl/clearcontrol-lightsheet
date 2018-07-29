package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.gradientbased;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * The GradientBasedZernikeModeOptimizerInstruction allows optimizing the deformable mirror. It optimizes a single Zernike
 * factor in the array taken from the mirror device. Was successfully tested on defocus (Z4). It is recommend to run
 * it only on images where there is enough content (single beads are on enough)
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class GradientBasedZernikeModeOptimizerInstruction extends LightSheetMicroscopeInstructionBase {

    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;

    private final int mZernikeFactorIndexToOptimize;

    private BoundedVariable<Double> stepSize = new BoundedVariable<Double>("Defocus step size",0.05, 0.0, Double.MAX_VALUE, 0.0000000001);

    private BoundedVariable<Double> mPositionZ = null;


    public GradientBasedZernikeModeOptimizerInstruction(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, int pZernikeFactorIndexToOptimize) {
        super("Adaptive optics: Gradient based Z" + ZernikePolynomials.jNoll(pZernikeFactorIndexToOptimize) + "(" + ZernikePolynomials.getZernikeModeName(pZernikeFactorIndexToOptimize) + ")" + " optimizer for " + pSpatialPhaseModulatorDeviceInterface.getName(), pLightSheetMicroscope);
        this.mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mZernikeFactorIndexToOptimize = pZernikeFactorIndexToOptimize;
    }

    @Override
    public boolean initialize() {
        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();
        mPositionZ = new BoundedVariable<Double>("position Z", (lState.getStackZLowVariable().get().doubleValue() + lState.getStackZHighVariable().get().doubleValue()) / 2, lState.getStackZLowVariable().getMin().doubleValue(), lState.getStackZHighVariable().getMax().doubleValue(), lState.getStackZLowVariable().getGranularity().doubleValue());

        double[] zernikes = mSpatialPhaseModulatorDeviceInterface.getZernikeFactors();
        zernikes[mZernikeFactorIndexToOptimize] = 0; // reset
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        double[] zernikes = mSpatialPhaseModulatorDeviceInterface.getZernikeFactors();

        // decrease one Zernike factor
        double[] zernikesFactorDecreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesFactorDecreased, 0, zernikes.length);
        zernikesFactorDecreased[mZernikeFactorIndexToOptimize] -= stepSize.get();
        ZernikeSolution zernikeSolutionFactorDecrement = new ZernikeSolution(zernikesFactorDecreased, getLightSheetMicroscope(), mSpatialPhaseModulatorDeviceInterface, mPositionZ.get());

        // increase one Zernike factor
        double[] zernikesFactorIncreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesFactorIncreased, 0, zernikes.length);
        zernikesFactorIncreased[mZernikeFactorIndexToOptimize] += stepSize.get();
        ZernikeSolution zernikeSolutionFactorIncrement = new ZernikeSolution(zernikesFactorIncreased, getLightSheetMicroscope(), mSpatialPhaseModulatorDeviceInterface, mPositionZ.get());

        // determine fitness of both solutions
        double factorDecrementQuality = zernikeSolutionFactorDecrement.fitness();
        double factorIncrementQuality = zernikeSolutionFactorIncrement.fitness();

        // compare quality and set new factor to the mirror
        if (factorDecrementQuality > factorIncrementQuality) {
            mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesFactorDecreased);
        } else {
            mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesFactorIncreased);
        }

        return false;
    }

    @Override
    public GradientBasedZernikeModeOptimizerInstruction copy() {
        return new GradientBasedZernikeModeOptimizerInstruction(getLightSheetMicroscope(), mSpatialPhaseModulatorDeviceInterface, mZernikeFactorIndexToOptimize);
    }

    public BoundedVariable<Double> getStepSize() {
        return stepSize;
    }
}
