package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;

import java.text.DecimalFormat;
import java.util.Random;

public class SequentialZernikesScheduler extends SchedulerBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private double mMaxZernCoeff;
    private double mMinZernCoeff;
    private static double[] mArray;
    private double mStepper;
    private double mInitialValue;
    public int mStartingMode;
    private static int mChangingMode;
    public int mEndingMode;
    private static String mDirection;

    public SequentialZernikesScheduler(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase, double pStepper, double pInitialValue, double pMaxZernCoeff, double pMinZernCoeff) {
        super("Adaptation: Sequential mirror modes " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
        mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;
        mMaxZernCoeff = pMaxZernCoeff;
        mMinZernCoeff = pMinZernCoeff;
        mStepper = pStepper;
        mInitialValue = pInitialValue;
        mStartingMode = 3;
        mEndingMode = 3;
    }

    @Override
    public boolean initialize() {
        mArray = mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors();
        for(int i = 0; i < mArray.length; i++) {
            mArray[i] = mInitialValue;
        }
        mChangingMode = mStartingMode;
        mDirection = "positive";
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if(mStepper>(mMaxZernCoeff-mInitialValue)){
            System.out.println("Stepper is too big");
            return false;
        }
        if (mArray[mChangingMode] >= mMaxZernCoeff) {
            mDirection = "negative";
            mArray[mChangingMode] = mInitialValue;
        }
        else if (mArray[mChangingMode] <= mMinZernCoeff) {
            mDirection = "positive";
            mArray[mChangingMode] = mInitialValue;
            mChangingMode++;
        }
        if(mChangingMode <= mEndingMode) {
            switch (mDirection){
                case "positive": {
                    mArray[mChangingMode] = mArray[mChangingMode]+mStepper;
                    break;
                }

                case "negative": {
                    mArray[mChangingMode] = mArray[mChangingMode]-mStepper;
                    break;
                }
            }

        }
        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(mArray);
        return true;

    }
}
