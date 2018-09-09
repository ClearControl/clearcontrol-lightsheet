package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms;

import clearcontrol.core.device.name.NameableInterface;
import clearcontrol.core.device.openclose.OpenCloseDeviceInterface;
import clearcontrol.core.device.startstop.StartStopDeviceInterface;
import clearcontrol.core.variable.Variable;

import org.ejml.data.DenseMatrix64F;

public interface SpatialPhaseModulatorDeviceInterface extends
                                                      OpenCloseDeviceInterface,
                                                      StartStopDeviceInterface,
                                                      NameableInterface
{
  @Deprecated
  int getMatrixWidth();

  @Deprecated
  int getMatrixHeight();

  @Deprecated
  int getActuatorResolution();

  @Deprecated
  Variable<Double> getMatrixWidthVariable();

  @Deprecated
  Variable<Double> getMatrixHeightVariable();

  @Deprecated
  Variable<Double> getActuatorResolutionVariable();

  @Deprecated
  Variable<Double> getNumberOfActuatorVariable();

  @Deprecated
  Variable<DenseMatrix64F> getMatrixReference();

  void zero();

  void setMode(int u, int v, double pValue);

  long getRelaxationTimeInMilliseconds();

  double[] getZernikeFactors();

  boolean setZernikeFactors(double[] pZernikeFactors);

}
