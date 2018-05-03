package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.iqm.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.fitness.MirrorModeImageQualityDeterminer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import clearcontrol.stack.StackInterface;
import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;
import java.util.Random;

/**
 * The ZernikeSolution represents a combination of Zernike modes representing a mirror shape. It's fitness function
 * uses a LightSheetMicroscope and a deformable mirror to determine image quality.
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class ZernikeSolution implements SolutionInterface {

    final LightSheetMicroscope mLightSheetMicroscope;
    final double mPositionZ;
    final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
    public double[] mFactors;
    private ArrayList<DenseMatrix64F> mZernikePolynomialList;

    public final int sMaxN = 3;

    private static Random sRandom = new Random();
    private DenseMatrix64F mMatrix = null;

    boolean fitnessInitialized = false;
    double fitness = 0;


    public ZernikeSolution(double[] pFactors, LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface, double pPositionZ) {
        mFactors = pFactors;
        mLightSheetMicroscope = pLightSheetMicroscope;
        mPositionZ = pPositionZ;
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        initializeZernikePolynomialList(pSpatialPhaseModulatorDeviceInterface.getMatrixWidth(), pSpatialPhaseModulatorDeviceInterface.getMatrixHeight());
    }

    @Override
    public double fitness() {

        if (fitnessInitialized) {
            return fitness;
        }

        DenseMatrix64F lMatrix = getMatrix();

        fitness = new MirrorModeImageQualityDeterminer(mLightSheetMicroscope, mSpatialPhaseModulatorDeviceInterface, mPositionZ, lMatrix).getFitness();
        fitnessInitialized = true;
        return fitness;
    }

    public DenseMatrix64F getMatrix() {
        if (mMatrix != null) {
            return mMatrix;
        }

        DenseMatrix64F lTemplate = mZernikePolynomialList.get(0);
        DenseMatrix64F lMatrix = new DenseMatrix64F(lTemplate.numRows, lTemplate.numCols);

        for (int i = 0; i < mFactors.length; i++) {
            DenseMatrix64F lSummand = TransformMatrices.multiply(mZernikePolynomialList.get(i), mFactors[i]);
            lMatrix = TransformMatrices.sum(lMatrix, lSummand);
        }
        mMatrix = lMatrix;
        return lMatrix;
    }

    @Override
    public void mutate() {
        int randomPosition = sRandom.nextInt(mFactors.length);
        double randomValue = sRandom.nextDouble() * 2.0 - 1.0;
        mFactors[randomPosition] = randomValue;
    }

    /**
     * Initialize the list of Zernike modes which can be composed
     * @param pWidth
     * @param pHeight
     */
    private void initializeZernikePolynomialList(int pWidth, int pHeight)
    {
        mZernikePolynomialList = new ArrayList<>();

        for (int n = 0; n <= sMaxN; n++) {
            for (int m = -n; m <= n; m += 2) {
                //System.out.println("m" + m + " n" + n);
                mZernikePolynomialList.add(new ZernikePolynomialsDenseMatrix64F(pWidth, pHeight, m,n));
            }
        }
    }

    public double[] getFactors(){
        return mFactors;
    }

}
