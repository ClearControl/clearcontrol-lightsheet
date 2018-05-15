package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import org.ejml.data.DenseMatrix64F;

import java.text.DecimalFormat;
import java.util.Random;

public class RandomZernikesScheduler  extends SchedulerBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private BoundedVariable<Double> mMaximumZernikeFactor = new BoundedVariable<Double>("Maximum Zernike factor", 5.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0000001);
    private BoundedVariable<Double> mMinimumZernikeFactor = new BoundedVariable<Double>("Minimum Zernike factor", -5.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0000001);
    private Random mRandom = new Random();

    public RandomZernikesScheduler(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptation: Random Zernike modes for " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
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
}
