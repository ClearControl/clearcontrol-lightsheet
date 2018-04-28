package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.demo;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;

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
    private final ZernikeSolution mReferenceSolution;
    private ZernikeSolutionFactory mFactory;


    public ZernikeDemoSolutionFactory(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ, int pZernikeCount, ZernikeSolution pReferenceSolution) {

        mFactory = new ZernikeSolutionFactory(pLightSheetMicroscope, pSpatialPhaseModulatorDeviceInterface, pPositionZ, pZernikeCount);
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mLightSheetMicroscope = pLightSheetMicroscope;
        mPositionZ = pPositionZ;
        mReferenceSolution = pReferenceSolution;
    }

    @Override
    public ZernikeDemoSolution random() {
        ZernikeSolution lSolution = mFactory.random();

        return new ZernikeDemoSolution(lSolution.mFactors, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ, mReferenceSolution);
    }


    @Override
    public ZernikeDemoSolution crossover(ZernikeDemoSolution pSolution1, ZernikeDemoSolution pSolution2) {
        ZernikeSolution lSolution = mFactory.crossover(pSolution1, pSolution2);

        return new ZernikeDemoSolution(lSolution.mFactors, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ, mReferenceSolution);
    }
}
