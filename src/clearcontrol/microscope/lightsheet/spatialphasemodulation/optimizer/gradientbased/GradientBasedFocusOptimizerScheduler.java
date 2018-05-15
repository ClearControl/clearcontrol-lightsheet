package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.gradientbased;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * GradientBasedFocusOptimizerScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class GradientBasedFocusOptimizerScheduler extends SchedulerBase {

    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;

    final static int DEFOCUS_INDEX = 4;
    private final LightSheetMicroscope mLightSheetMicroscope;

    private BoundedVariable<Double> stepSize = new BoundedVariable<Double>("Defocus step size",0.001, 0.0, Double.MAX_VALUE, 0.0000000001);

    private BoundedVariable<Double> mPositionZ = null;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public GradientBasedFocusOptimizerScheduler(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface) {
        super("Adaptive: Gradient based focus optimizer for " + pSpatialPhaseModulatorDeviceInterface.getName());
        this.mLightSheetMicroscope = pLightSheetMicroscope;
        this.mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;

        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
        mPositionZ = new BoundedVariable<Double>("position Z", (lState.getStackZLowVariable().get().doubleValue() + lState.getStackZHighVariable().get().doubleValue()) / 2, lState.getStackZLowVariable().getMin().doubleValue(), lState.getStackZHighVariable().getMax().doubleValue(), lState.getStackZLowVariable().getGranularity().doubleValue());
    }

    @Override
    public boolean initialize() {
        double[] zernikes = mSpatialPhaseModulatorDeviceInterface.getZernikeFactors();
        zernikes[DEFOCUS_INDEX] = 0; // reset
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        double[] zernikes = mSpatialPhaseModulatorDeviceInterface.getZernikeFactors();

        // decrease one Zernike factor
        double[] zernikesDefocusDecreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesDefocusDecreased, 0, zernikes.length);
        zernikesDefocusDecreased[DEFOCUS_INDEX] -= stepSize.get();
        ZernikeSolution zernikeSolutionDefocusDecrement = new ZernikeSolution(zernikes, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ.get());

        // increase one Zernike factor
        double[] zernikesDefocusIncreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesDefocusIncreased, 0, zernikes.length);
        zernikesDefocusIncreased[DEFOCUS_INDEX] += stepSize.get();
        ZernikeSolution zernikeSolutionDefocusIncrement = new ZernikeSolution(zernikes, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ.get());

        // determine fitness of both solutions
        double defocusDecrementQuality = zernikeSolutionDefocusDecrement.fitness();
        double defocusIncrementQuality = zernikeSolutionDefocusIncrement.fitness();

        // compare quality and set new factor to the mirror
        if (defocusDecrementQuality > defocusIncrementQuality) {
            mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesDefocusDecreased);
        } else {
            mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesDefocusIncreased);
        }

        return false;
    }
}
