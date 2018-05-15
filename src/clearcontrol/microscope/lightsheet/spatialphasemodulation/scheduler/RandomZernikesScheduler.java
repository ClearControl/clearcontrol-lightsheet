package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import org.ejml.data.DenseMatrix64F;

import java.text.DecimalFormat;
import java.util.Random;

public class RandomZernikesScheduler  extends SchedulerBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private double mMaxZrnCoeff;
    private double mMinZernCoeff;

    public RandomZernikesScheduler(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptation: Random Zernike modes for " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
        mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;
        mMaxZrnCoeff = 5;
        mMinZernCoeff = -5;

    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        Random rand = new Random();
        DecimalFormat df = new DecimalFormat("#.##");
        double[] lArray = new double[66];

        for (int i = 0; i < 66; i++) {
            double value = Double.parseDouble(df.format(mMinZernCoeff + (mMaxZrnCoeff - mMinZernCoeff) * rand.nextDouble()));
            lArray[i] = value;
        }

        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);
        return true;
    }
}
