package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.actuator;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.fitness.MirrorModeImageQualityDeterminer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;
import java.util.Random;

/**
 * ActuatorSolution
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ActuatorSolution implements SolutionInterface {

    final LightSheetMicroscope mLightSheetMicroscope;
    final double mPositionZ;
    final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;

    private static Random sRandom = new Random();

    private DenseMatrix64F mMatrix = null;


    public ActuatorSolution(DenseMatrix64F pMatrix, LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ) {
        mMatrix = pMatrix;
        mLightSheetMicroscope = pLightSheetMicroscope;
        mPositionZ = pPositionZ;
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
    }

    @Override
    public double fitness() {
        return new MirrorModeImageQualityDeterminer(mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ, mMatrix).getFitness();
    }

    @Override
    public void mutate() {
        int randomPositionX = sRandom.nextInt(mMatrix.numCols);
        int randomPositionY = sRandom.nextInt(mMatrix.numRows);
        double randomValue = sRandom.nextDouble() * 2.0 - 1.0;
        mMatrix.set(randomPositionY, randomPositionX, randomValue);
    }

    public DenseMatrix64F getMatrix() {
        return mMatrix;
    }
}
