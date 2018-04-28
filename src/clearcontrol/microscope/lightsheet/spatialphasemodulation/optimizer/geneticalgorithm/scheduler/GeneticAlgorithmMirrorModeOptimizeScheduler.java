package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * GeneticAlgorithmMirrorModeOptimizeScheduler
 *
 *
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class GeneticAlgorithmMirrorModeOptimizeScheduler extends SchedulerBase implements LoggingFeature {

    private BoundedVariable<Integer> mNumberOfEpochsPerTimePoint = new BoundedVariable<Integer>("Number of epochs per time point",10, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mPopulationSize = new BoundedVariable<Integer>("Population size",100, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mNumberOfMutations = new BoundedVariable<Integer>("NumberOfMutations",1, 0, Integer.MAX_VALUE);

    private BoundedVariable<Double> mPositionZ = null;
    private LightSheetMicroscope mLightSheetMicroscope = null;

    Population<ZernikeSolution> mPopulation;
    SpatialPhaseModulatorDeviceInterface mMirror;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public GeneticAlgorithmMirrorModeOptimizeScheduler(SpatialPhaseModulatorDeviceInterface pMirror) {
        super("Adaptation: GA Mirror optimizer for " + pMirror);
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
        mPositionZ = new BoundedVariable<Double>("position Z", (lState.getStackZLowVariable().get().doubleValue() + lState.getStackZHighVariable().get().doubleValue()) / 2, lState.getStackZLowVariable().getMin().doubleValue(), lState.getStackZHighVariable().getMax().doubleValue(), lState.getStackZLowVariable().getGranularity().doubleValue() );

        ZernikeSolutionFactory lFactory = new ZernikeSolutionFactory(mLightSheetMicroscope, mMirror, mPositionZ.get(), 6);
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
        }

        mMirror.getMatrixReference().set(mPopulation.best().getMatrix());

        return true;
    }


}
