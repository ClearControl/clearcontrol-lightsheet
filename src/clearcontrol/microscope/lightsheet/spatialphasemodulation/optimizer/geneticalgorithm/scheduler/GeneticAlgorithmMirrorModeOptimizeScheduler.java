package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
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

    private BoundedVariable<Double> mPositionZ = null;
    private LightSheetMicroscope mLightSheetMicroscope = null;

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pDeviceName device name
     */
    public GeneticAlgorithmMirrorModeOptimizeScheduler(String pDeviceName) {
        super(pDeviceName);
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

        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (mLightSheetMicroscope == null) {
            warning("Not initialized");
            return false;
        }

        // WIP



        return true;
    }


}
