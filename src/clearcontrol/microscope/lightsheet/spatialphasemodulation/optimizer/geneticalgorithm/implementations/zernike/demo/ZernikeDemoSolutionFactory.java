package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.demo;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import org.ejml.data.DenseMatrix64F;

/**
 * The ZernikeDemoSolutionFactory allows to create ZernikeDemoSolutions which don't need a real microscope to calculate
 * fitness.
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ZernikeDemoSolutionFactory implements SolutionFactory<ZernikeDemoSolution> {
    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
    private final LightSheetMicroscope mLightSheetMicroscope;
    private final double mPositionZ;
    private final DenseMatrix64F mReferenceMatrix;
    private ZernikeSolutionFactory mFactory;


    public ZernikeDemoSolutionFactory(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ, int pZernikeCount, double pRangeFactor, DenseMatrix64F pReferenceMatrix) {

        mFactory = new ZernikeSolutionFactory(pLightSheetMicroscope, pSpatialPhaseModulatorDeviceInterface, pPositionZ, pZernikeCount, pRangeFactor);
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mLightSheetMicroscope = pLightSheetMicroscope;
        mPositionZ = pPositionZ;
        mReferenceMatrix = pReferenceMatrix;
    }

    @Override
    public ZernikeDemoSolution random() {
        ZernikeSolution lSolution = mFactory.random();

        return new ZernikeDemoSolution(lSolution.mFactors, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ, mReferenceMatrix);
    }


    @Override
    public ZernikeDemoSolution crossover(ZernikeDemoSolution pSolution1, ZernikeDemoSolution pSolution2) {
        ZernikeSolution lSolution = mFactory.crossover(pSolution1, pSolution2);

        return new ZernikeDemoSolution(lSolution.mFactors, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ, mReferenceMatrix);
    }
}
