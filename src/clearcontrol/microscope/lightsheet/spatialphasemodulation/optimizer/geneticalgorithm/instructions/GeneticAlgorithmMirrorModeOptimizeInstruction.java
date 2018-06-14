package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

/**
 * GeneticAlgorithmMirrorModeOptimizeInstruction
 *
 *
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class GeneticAlgorithmMirrorModeOptimizeInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private BoundedVariable<Integer> mNumberOfEpochsPerTimePoint = new BoundedVariable<Integer>("Number of epochs per time point",10, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mPopulationSize = new BoundedVariable<Integer>("Population size",10, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mNumberOfMutations = new BoundedVariable<Integer>("NumberOfMutations",1, 0, Integer.MAX_VALUE);

    private BoundedVariable<Double> mPositionZ = new BoundedVariable<Double>("position Z", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0001);

    private BoundedVariable<Double> mZernikeRangeFactor = new BoundedVariable<Double>("Zernike range factor", 0.1, 0.0, Double.MAX_VALUE, 0.0000001);

    Population<ZernikeSolution> mPopulation;
    SpatialPhaseModulatorDeviceInterface mMirror;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public GeneticAlgorithmMirrorModeOptimizeInstruction(SpatialPhaseModulatorDeviceInterface pMirror, LightSheetMicroscope pLightSheetMicroscope) {
        super("Adaptive optics: GA Mirror optimizer for " + pMirror, pLightSheetMicroscope);
        mMirror = pMirror;
    }

    @Override
    public boolean initialize() {
        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();
        if (mPositionZ.get() < lState.getStackZLowVariable().getMin().doubleValue() || mPositionZ.get() > lState.getStackZLowVariable().getMax().doubleValue()) {
            mPositionZ.set((lState.getStackZLowVariable().get().doubleValue() + lState.getStackZHighVariable().get().doubleValue()) / 2);
        }
        mPositionZ.setMinMax(lState.getStackZHighVariable().getMin().doubleValue(), lState.getStackZHighVariable().getMax().doubleValue());
        ZernikeSolutionFactory lFactory = new ZernikeSolutionFactory(getLightSheetMicroscope(), mMirror, mPositionZ.get(), 6, mZernikeRangeFactor.get());
        mPopulation = new Population<ZernikeSolution>(lFactory, mPopulationSize.get(), mNumberOfMutations.get());

        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        for (int e = 0; e < mNumberOfEpochsPerTimePoint.get(); e++) {
            mPopulation = mPopulation.runEpoch();
            if (getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getStopSignalVariable().get()) {
                return false;
            }
        }

        mMirror.getMatrixReference().set(mPopulation.best().getMatrix());

        return true;
    }

    public BoundedVariable<Double> getPositionZ() {
        return mPositionZ;
    }

    public BoundedVariable<Double> getZernikeRangeFactor() {
        return mZernikeRangeFactor;
    }

    public BoundedVariable<Integer> getNumberOfEpochsPerTimePoint() {
        return mNumberOfEpochsPerTimePoint;
    }

    public BoundedVariable<Integer> getNumberOfMutations() {
        return mNumberOfMutations;
    }

    public BoundedVariable<Integer> getPopulationSize() {
        return mPopulationSize;
    }

    @Override
    public GeneticAlgorithmMirrorModeOptimizeInstruction copy() {
        GeneticAlgorithmMirrorModeOptimizeInstruction copied = new GeneticAlgorithmMirrorModeOptimizeInstruction(mMirror, getLightSheetMicroscope());
        copied.mNumberOfEpochsPerTimePoint.set(mNumberOfEpochsPerTimePoint.get());
        copied.mNumberOfMutations.set(mNumberOfMutations.get());
        copied.mPopulationSize.set(mPopulationSize.get());
        copied.mZernikeRangeFactor.set(mZernikeRangeFactor.get());
        copied.mPositionZ.set(mPositionZ.get());
        return copied;
    }
}
