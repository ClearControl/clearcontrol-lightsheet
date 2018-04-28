package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.demo;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import org.ejml.data.DenseMatrix64F;

/**
 * ZernikeDemoSolution
 *
 * In this class we override the fitnesses function to make it compare to a reference solution. In that way, the genetic
 * algorithm can be tested without a real microscope.
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ZernikeDemoSolution extends ZernikeSolution {


    DenseMatrix64F mReferenceMatrix;

    public ZernikeDemoSolution(double[] pFactors, LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ, DenseMatrix64F pReference) {
        super(pFactors, pLightSheetMicroscope, pSpatialPhaseModulatorDeviceInterface, pPositionZ);
        mReferenceMatrix = pReference;
    }

    @Override
    public double fitness() {
        double mse = 0;

        DenseMatrix64F lMatrix = getMatrix();
        DenseMatrix64F lReferenceMatrix = mReferenceMatrix;

        for (int x = 0; x < lReferenceMatrix.numCols; x++) {
            for (int y = 0; y < lReferenceMatrix.numRows; y++) {
                mse += Math.pow(lReferenceMatrix.get(y,x) - lMatrix.get(y, x), 2);
            }
        }
        return - mse;
    }
}
