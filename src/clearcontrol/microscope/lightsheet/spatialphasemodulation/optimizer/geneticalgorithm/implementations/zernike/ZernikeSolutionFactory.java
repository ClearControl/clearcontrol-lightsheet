package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;

import java.util.Arrays;
import java.util.Random;

/**
 * The ZernikeSolutionFactory generates potential solutions for finding a good mirror shape
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ZernikeSolutionFactory implements SolutionFactory<ZernikeSolution> {

    private final LightSheetMicroscope mLightSheetMicroscope;
    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
    private final double mPositionZ;
    private final int mZernikeCount;
    private final double sBound = 1.0;

    private Random mRandom = new Random();

    public ZernikeSolutionFactory(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ, int pZernikeCount) {
      mLightSheetMicroscope = pLightSheetMicroscope;
      mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
      mPositionZ = pPositionZ;
      mZernikeCount = pZernikeCount;
    }


    @Override
    public ZernikeSolution random() {
        double[] lFactors = new double[mZernikeCount];
        for (int i = 0; i < lFactors.length; i++){
            lFactors[i] = mRandom.nextDouble() * sBound * 2 - sBound;
        }

        return new ZernikeSolution(lFactors, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ);
    }

    @Override
    public ZernikeSolution crossover(ZernikeSolution pSolution1, ZernikeSolution pSolution2) {
        double[] lFactors1 = pSolution1.getFactors();
        double[] lFactors2 = pSolution2.getFactors();

        double[] lNewFactors = new double[lFactors1.length];

        int lRandomPosition = mRandom.nextInt(lNewFactors.length + 1);
        for (int i = 0; i < lRandomPosition; i++) {
            lNewFactors[i] = lFactors1[i];
        }
        for (int i = lRandomPosition; i < lNewFactors.length; i++) {
            lNewFactors[i] = lFactors2[i];
        }

        return new ZernikeSolution(lNewFactors, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ);
    }
}
