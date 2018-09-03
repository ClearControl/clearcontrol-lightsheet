package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;

import org.ejml.data.DenseMatrix64F;

public abstract class SpatialPhaseModulatorDeviceBase extends
                                                      VirtualDevice
                                                      implements
                                                      SpatialPhaseModulatorDeviceInterface
{

  protected Variable<Double> mMatrixWidthVariable;
  protected Variable<Double> mMatrixHeightVariable;
  protected Variable<Double> mActuatorResolutionVariable;
  protected Variable<Double> mNumberOfActuatorsVariable;

  protected Variable<DenseMatrix64F> mMatrixVariable;

  public SpatialPhaseModulatorDeviceBase(String pDeviceName,
                                         int pFullMatrixWidthHeight,
                                         int pActuatorResolution)
  {
    super(pDeviceName);

    mMatrixWidthVariable =
                         new Variable<Double>("MatrixWidth",
                                              (double) pFullMatrixWidthHeight);
    mMatrixHeightVariable =
                          new Variable<Double>("MatrixHeight",
                                               (double) pFullMatrixWidthHeight);
    mActuatorResolutionVariable =
                                new Variable<Double>("ActuatorResolution",
                                                     (double) pActuatorResolution);

    mMatrixVariable =
                    new Variable<DenseMatrix64F>("Matrix",
                                                 new DenseMatrix64F(pFullMatrixWidthHeight,
                                                                    pFullMatrixWidthHeight,
                                                                    true,
                                                                    new double[pFullMatrixWidthHeight
                                                                               * pFullMatrixWidthHeight]));
    System.out.println("Matrix SET to " + mMatrixVariable.get());
  }

  @Override
  @Deprecated
  public int getMatrixWidth()
  {
    return mMatrixWidthVariable.get().intValue();
  }

  @Override
  @Deprecated
  public int getMatrixHeight()
  {
    return mMatrixHeightVariable.get().intValue();
  }

  @Override
  public int getActuatorResolution()
  {
    return mActuatorResolutionVariable.get().intValue();
  }

  @Override
  @Deprecated
  public Variable<Double> getMatrixWidthVariable()
  {
    return mMatrixWidthVariable;
  }

  @Override
  @Deprecated
  public Variable<Double> getMatrixHeightVariable()
  {
    return mMatrixHeightVariable;
  }

  @Override
  public Variable<Double> getActuatorResolutionVariable()
  {
    return mActuatorResolutionVariable;
  }

  @Override
  public Variable<Double> getNumberOfActuatorVariable()
  {
    return mNumberOfActuatorsVariable;
  }

  @Override
  @Deprecated
  public Variable<DenseMatrix64F> getMatrixReference()
  {
    return mMatrixVariable;
  }

  @Override
  public void setMode(int pU, int pV, double pValue)
  {
    mMatrixVariable.get().set(pU + getMatrixWidth() * pV, 0, pValue);
    mMatrixVariable.setCurrent();
  }

  @Override
  public abstract void zero();

  @Override
  public abstract long getRelaxationTimeInMilliseconds();

}
