package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms;

import java.io.File;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;

import org.ejml.data.DenseMatrix64F;

/**
 * ZernikeModeFactorBasedSpatialPhaseModulatorBase
 *
 * This base class for deformable mirror devices adds the feature of handing
 * over an array of Zernike factors in Noll order to the device. The first
 * element int the array [0] corresponds to Noll 1
 *
 *
 * Author: @haesleinhuepf 05 2018
 */
public abstract class ZernikeModeFactorBasedSpatialPhaseModulatorBase extends
                                                                      SpatialPhaseModulatorDeviceBase
{

  private double[] mZernikeModeFactors;
  File lMirrorModeDirectory =
                            MachineConfiguration.get()
                                                .getFolder("MirrorModes");
  DenseMatrix64F mFlatMatrix;
  DenseMatrix64F mInfluenceMatrix;

  public ZernikeModeFactorBasedSpatialPhaseModulatorBase(String pDeviceName,
                                                         int pFullMatrixWidthHeight,
                                                         int pActuatorResolution,
                                                         int pNumberOfZernikeFactors)
  {
    super(pDeviceName, pFullMatrixWidthHeight, pActuatorResolution);
    mZernikeModeFactors = new double[pNumberOfZernikeFactors];

    mFlatMatrix =
                new DenseMatrix64FReader(new File(lMirrorModeDirectory,
                                                  getName() + "_flat.json")).getMatrix();
    mInfluenceMatrix =
                     new DenseMatrix64FReader(new File(lMirrorModeDirectory,
                                                       getName() + "_influence.json")).getMatrix();
    if (mInfluenceMatrix != null)
    {
      mInfluenceMatrix =
                       TransformMatrices.transposeMatrix(mInfluenceMatrix);
    }
  }

  /**
   * @return array of Zernike factors in Nolls order. The first element[0]
   *         corresponds to Noll 1
   */
  @Override
  public double[] getZernikeFactors()
  {
    double[] resultArray = new double[mZernikeModeFactors.length];
    System.arraycopy(mZernikeModeFactors,
                     0,
                     resultArray,
                     0,
                     mZernikeModeFactors.length);
    return resultArray;
  }

  /**
   * set Zernike factors in Nolls order. The first element[0] corresponds to
   * Noll 1
   * 
   * @param pZernikeFactors
   *          factors in Noll order
   */
  protected boolean setZernikeFactorsInternal(double[] pZernikeFactors)
  {
    System.arraycopy(pZernikeFactors,
                     0,
                     mZernikeModeFactors,
                     0,
                     Math.min(mZernikeModeFactors.length,
                              pZernikeFactors.length));
    return true;
  }

  /**
   * @return array of Zernike factors in Nolls order. The first element[0]
   *         corresponds to Noll 1
   */
  protected DenseMatrix64F getActuatorPositions(double[] pZernikeFactors)
  {
    DenseMatrix64F lZernikeFactorsMatrix = new DenseMatrix64F(66, 1);
    DenseMatrix64F lZernikeFactors =
                                   TransformMatrices.convert1DDoubleArrayToDense64RowMatrix(pZernikeFactors);

    if (lZernikeFactors.numRows == lZernikeFactors.numRows)
    {
      lZernikeFactorsMatrix = lZernikeFactors;
    }
    else
    {
      for (int y = 0; y < lZernikeFactorsMatrix.numRows; y++)
      {
        if (y < lZernikeFactors.numRows)
        {
          lZernikeFactorsMatrix.set(y, 0, lZernikeFactors.get(y, 0));
        }
        else
        {
          lZernikeFactorsMatrix.set(y, 0, 0);
        }
      }
    }

    DenseMatrix64F lActuators = null;
    if (mInfluenceMatrix != null && mFlatMatrix != null)
    {
      lActuators =
                 TransformMatrices.multiplyMatrix(mInfluenceMatrix,
                                                  lZernikeFactorsMatrix);
      // System.out.println("InfluenceMatrix" + mInfluenceMatrix.toString());
      // System.out.println("ZernikeFactors" +
      // lZernikeFactorsMatrix.toString());
      // System.out.println("Actuators" + lActuators.toString());
      lActuators = TransformMatrices.sum(lActuators, mFlatMatrix);
    }

    return lActuators;
  }
}
