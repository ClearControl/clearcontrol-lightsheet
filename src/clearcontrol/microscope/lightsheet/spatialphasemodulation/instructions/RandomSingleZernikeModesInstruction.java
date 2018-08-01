package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomSingleZernikeModesInstruction extends InstructionBase implements
        LoggingFeature {

    private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;
    private BoundedVariable<Double>[] mRangeOfZernikeCoefficientsArray;
    private BoundedVariable<Integer> mDigitsAfterDecimal = new BoundedVariable("Number Of Places After decimal",3,0,5);

    private Random mRandom = new Random();


    public RandomSingleZernikeModesInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptive optics: Send randomly chosen single random Zernike modes to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
        mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;

        mRangeOfZernikeCoefficientsArray = new BoundedVariable[mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors().length];

        for(int i = 0; i < mRangeOfZernikeCoefficientsArray.length; i++) {
            //mRangeOfZernikeCoefficientsArray[i] = new BoundedVariable<Double>("Z" + ZernikePolynomials.jNoll(i) + "(" + ZernikePolynomials.getZernikeModeName(i) + ") -min/max", 0.0, 0.0, 5.0, 0.0000001);
            mRangeOfZernikeCoefficientsArray[i] = new BoundedVariable<Double>("Z" + i + " -min/max", 0.0, 0.0, 5.0, 0.0000001);

        }

        mDigitsAfterDecimal.set(3);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        double[] lArray = new double[mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors().length];
        double thousands = Math.pow(10,mDigitsAfterDecimal.get());
        List<Integer> lSelectedZernikeModesList = new ArrayList<Integer>();
        int lSelectedChangingMode = 0;

        for (int i = 0; i < lArray.length; i++) {
            lArray[i] = 0.0;
            double absZernCoeff = mRangeOfZernikeCoefficientsArray[i].get();
            if (absZernCoeff != 0.0) {
                lSelectedZernikeModesList.add(i);
            }
        }

        if(lSelectedZernikeModesList.size()==0){
            info("Mirror Unchanged");
            return true;
        }
        else{
            lSelectedChangingMode = lSelectedZernikeModesList.get(mRandom.nextInt(lSelectedZernikeModesList.size()));
        }
        lArray[lSelectedChangingMode] = Math.round((mRandom.nextDouble() * 2.0 - 1.0) *
                mRangeOfZernikeCoefficientsArray[lSelectedChangingMode].get()*thousands)/thousands;

        mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);
        return true;
    }

    @Override
    public RandomSingleZernikeModesInstruction copy() {
        RandomSingleZernikeModesInstruction copied = new RandomSingleZernikeModesInstruction(mZernikeModeFactorBasedSpatialPhaseModulatorBase);

        for (int i = 0; i < mRangeOfZernikeCoefficientsArray.length; i++) {
            copied.mRangeOfZernikeCoefficientsArray[i] = mRangeOfZernikeCoefficientsArray[i];
        }

        return copied;
    }

    public BoundedVariable<Double> getRangeOfZernikeCoefficientArray(int i) {
        return mRangeOfZernikeCoefficientsArray[i];
    }
    public BoundedVariable<Integer> getNumberOfPlacesAfterDecimal(){
        return mDigitsAfterDecimal;
    }

}

