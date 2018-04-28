package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.actuator;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;

/**
 * ActuatorSolutionFactory
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ActuatorSolutionFactory implements SolutionFactory<ActuatorSolution> {

    private final double mPositionZ;
    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
    private final LightSheetMicroscope mLightSheetMicroscope;

    private Random mRandom = new Random();

    public ActuatorSolutionFactory(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ) {
        mLightSheetMicroscope = pLightSheetMicroscope;
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mPositionZ = pPositionZ;
    }


    @Override
    public ActuatorSolution random() {
        DenseMatrix64F lMatrix = new DenseMatrix64F(mSpatialPhaseModulatorDeviceInterface.getMatrixWidth(), mSpatialPhaseModulatorDeviceInterface.getMatrixHeight());

        for (int x = 0; x < lMatrix.numCols; x++) {
            for (int y = 0; y < lMatrix.numRows; y++) {
                double lRandomValue = mRandom.nextDouble() * 2.0 - 1.0;
                lMatrix.set(x, y, lRandomValue);
            }
        }

        return new ActuatorSolution(lMatrix, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ);
    }

    @Override
    public ActuatorSolution crossover(ActuatorSolution pSolution1, ActuatorSolution pSolution2) {
        DenseMatrix64F lMatrix1 = pSolution1.getMatrix();
        DenseMatrix64F lMatrix2 = pSolution2.getMatrix();

        DenseMatrix64F lNewMatrix = lMatrix1.copy();

        int lRandomPositionStartX = mRandom.nextInt(lNewMatrix.numCols);
        int lRandomPositionStartY = mRandom.nextInt(lNewMatrix.numCols);
        int lRandomPositionEndX = mRandom.nextInt(lNewMatrix.numCols);
        int lRandomPositionEndY = mRandom.nextInt(lNewMatrix.numCols);

        if (lRandomPositionEndX < lRandomPositionStartX) {
            int temp = lRandomPositionEndX;
            lRandomPositionEndX = lRandomPositionStartX;
            lRandomPositionStartX = temp;
        }

        if (lRandomPositionEndY < lRandomPositionStartY) {
            int temp = lRandomPositionEndY;
            lRandomPositionEndY = lRandomPositionStartY;
            lRandomPositionStartY = temp;
        }

        for(int x = lRandomPositionStartX; x <= lRandomPositionEndX; x++) {
            for(int y = lRandomPositionStartY; y <= lRandomPositionEndY; y++) {
                lNewMatrix.set(x, y, lMatrix2.get(x, y));
            }
        }

        return new ActuatorSolution(lNewMatrix, mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ);
    }
}
