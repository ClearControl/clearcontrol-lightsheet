package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;

import org.ejml.data.DenseMatrix64F;

public abstract class SpatialPhaseModulatorDeviceBase extends
                                                      VirtualDevice
                                                      implements
                                                      SpatialPhaseModulatorDeviceInterface
{
  private double[] mZernikeModeFactors = new double[66];

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
  public int getMatrixWidth()
  {
    return mMatrixWidthVariable.get().intValue();
  }

  @Override
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
  public Variable<Double> getMatrixWidthVariable()
  {
    return mMatrixWidthVariable;
  }

  @Override
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

  @Override
  public double[] getZernikeFactors() {
    double[] resultArray = new double[mZernikeModeFactors.length];
    System.arraycopy(mZernikeModeFactors, 0, resultArray, 0, mZernikeModeFactors.length);
    return resultArray;
  }

  protected boolean setZernikeFactorsInternal(double[] pZernikeFactors) {
    System.arraycopy(pZernikeFactors, 0, mZernikeModeFactors, 0, Math.min(mZernikeModeFactors.length, pZernikeFactors.length));
    return true;
  }
}
