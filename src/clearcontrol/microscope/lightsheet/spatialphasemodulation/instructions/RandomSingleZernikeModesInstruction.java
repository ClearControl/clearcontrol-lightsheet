package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSingleZernikeModesInstruction extends RandomZernikesInstruction implements
        LoggingFeature {

    public RandomSingleZernikeModesInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase) {
        super("Adaptive optics: Send randomly chosen single random Zernike modes to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName(), pZernikeModeFactorBasedSpatialPhaseModulatorBase);
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

}

