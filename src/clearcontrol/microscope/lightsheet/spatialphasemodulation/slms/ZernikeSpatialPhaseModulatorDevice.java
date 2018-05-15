package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms;

import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * Use ZernikeModeFactorBasedSpatialPhaseModulatorBase instead
 */
@Deprecated
public abstract class ZernikeSpatialPhaseModulatorDevice extends
                                                 SpatialPhaseModulatorDeviceBase
                                                 implements
                                                 SpatialPhaseModulatorDeviceInterface
{

  final DenseMatrix64F mZernickeTransformMatrix;

  protected SpatialPhaseModulatorDeviceInterface mDelegatedSpatialPhaseModulatorDeviceInterface;

  public ZernikeSpatialPhaseModulatorDevice(SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface)
  {
    super("Zernicke"
          + pSpatialPhaseModulatorDeviceInterface.getName(),
          pSpatialPhaseModulatorDeviceInterface.getMatrixHeight(),
          pSpatialPhaseModulatorDeviceInterface.getActuatorResolution());
    mDelegatedSpatialPhaseModulatorDeviceInterface =
                                                   pSpatialPhaseModulatorDeviceInterface;

    final int lMatrixWidth =
                           mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixWidth();
    final int lMatrixHeight =
                            mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixHeight();

    mZernickeTransformMatrix =
                             TransformMatrices.computeZernickeTransformMatrix(lMatrixHeight);

    mMatrixVariable =
                    new Variable<DenseMatrix64F>("Matrix",
                                                 new DenseMatrix64F(lMatrixHeight
                                                                    * lMatrixWidth,
                                                                    1))
                    {

                      @Override
                      public DenseMatrix64F setEventHook(DenseMatrix64F pOldValue,
                                                         DenseMatrix64F pNewValue)
                      {
                        final int lMatrixWidth =
                                               mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixWidth();
                        final int lMatrixHeight =
                                                mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixHeight();
                        final DenseMatrix64F lTransformedVector =
                                                                new DenseMatrix64F(lMatrixWidth
                                                                                   * lMatrixHeight,
                                                                                   1);

                        CommonOps.mult(mZernickeTransformMatrix,
                                       pNewValue,
                                       lTransformedVector);

                        mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixReference()
                                                                      .set(lTransformedVector);

                        // System.out.println(lTransformedVector);

                        return super.setEventHook(pOldValue,
                                                  pNewValue);
                      }
                    };

  }

  @Override
  public int getMatrixWidth()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixWidth();
  }

  @Override
  public int getMatrixHeight()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixHeight();
  }

  @Override
  public Variable<Double> getMatrixWidthVariable()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixWidthVariable();
  }

  @Override
  public Variable<Double> getMatrixHeightVariable()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.getMatrixHeightVariable();
  }

  @Override
  public Variable<Double> getActuatorResolutionVariable()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.getActuatorResolutionVariable();
  }

  @Override
  public Variable<Double> getNumberOfActuatorVariable()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.getNumberOfActuatorVariable();
  }

  @Override
  public void zero()
  {
    getMatrixReference().get().zero();
  }

  @Override
  public long getRelaxationTimeInMilliseconds()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.getRelaxationTimeInMilliseconds();
  }

  @Override
  public boolean start()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.start();
  }

  @Override
  public boolean stop()
  {
    return mDelegatedSpatialPhaseModulatorDeviceInterface.stop();
  }

}
