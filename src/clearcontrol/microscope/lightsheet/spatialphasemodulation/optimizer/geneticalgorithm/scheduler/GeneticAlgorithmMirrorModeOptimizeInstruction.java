package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
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
public class GeneticAlgorithmMirrorModeOptimizeInstruction extends InstructionBase implements LoggingFeature {

    private BoundedVariable<Integer> mNumberOfEpochsPerTimePoint = new BoundedVariable<Integer>("Number of epochs per time point",10, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mPopulationSize = new BoundedVariable<Integer>("Population size",10, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mNumberOfMutations = new BoundedVariable<Integer>("NumberOfMutations",1, 0, Integer.MAX_VALUE);

    private BoundedVariable<Double> mPositionZ = null;
    private BoundedVariable<Double> mZernikeRangeFactor = new BoundedVariable<Double>("Zernike range factor", 0.1, 0.0, Double.MAX_VALUE, 0.0000001);
    private LightSheetMicroscope mLightSheetMicroscope = null;

    Population<ZernikeSolution> mPopulation;
    SpatialPhaseModulatorDeviceInterface mMirror;


    /**
     * INstanciates a virtual device with a given name
     *
     */
    public GeneticAlgorithmMirrorModeOptimizeInstruction(SpatialPhaseModulatorDeviceInterface pMirror) {
        super("Adaptive optics: GA Mirror optimizer for " + pMirror);
        mMirror = pMirror;


    }

    @Override
    public boolean initialize() {
        if(!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }

        mLightSheetMicroscope = (LightSheetMicroscope)mMicroscope;
        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
        if (mPositionZ == null) {
            mPositionZ = new BoundedVariable<Double>("position Z", (lState.getStackZLowVariable().get().doubleValue() + lState.getStackZHighVariable().get().doubleValue()) / 2, lState.getStackZLowVariable().getMin().doubleValue(), lState.getStackZHighVariable().getMax().doubleValue(), lState.getStackZLowVariable().getGranularity().doubleValue());
        }
        ZernikeSolutionFactory lFactory = new ZernikeSolutionFactory(mLightSheetMicroscope, mMirror, mPositionZ.get(), 6, mZernikeRangeFactor.get());
        mPopulation = new Population<ZernikeSolution>(lFactory, mPopulationSize.get(), mNumberOfMutations.get());

        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (mLightSheetMicroscope == null) {
            warning("Not initialized");
            return false;
        }



        for (int e = 0; e < mNumberOfEpochsPerTimePoint.get(); e++) {
            mPopulation = mPopulation.runEpoch();
            if (mLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0).getStopSignalVariable().get()) {
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
}
