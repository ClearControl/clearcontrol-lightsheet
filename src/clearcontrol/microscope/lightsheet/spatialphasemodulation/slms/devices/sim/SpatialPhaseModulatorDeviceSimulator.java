package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.sim;

import clearcontrol.core.device.sim.SimulationDeviceInterface;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceBase;

import org.ejml.data.DenseMatrix64F;

public class SpatialPhaseModulatorDeviceSimulator extends
                                                  SpatialPhaseModulatorDeviceBase
                                                  implements
                                                  LoggingFeature,
                                                  SimulationDeviceInterface
{

  public SpatialPhaseModulatorDeviceSimulator(String pDeviceName,
                                              int pFullMatrixWidthHeight,
                                              int pActuatorResolution)
  {
    super(pDeviceName, pFullMatrixWidthHeight, pActuatorResolution);
    DenseMatrix64F lMatrix = null;
    if (mMatrixVariable != null)
    {
      lMatrix = mMatrixVariable.get();
    }
    mMatrixVariable = new Variable<DenseMatrix64F>("MatrixReference",
                                                   lMatrix)
    {
      @Override
      public DenseMatrix64F setEventHook(final DenseMatrix64F pOldValue,
                                         final DenseMatrix64F pNewValue)
      {
        if (isSimLogging())
          info("Device: %s received new data: %s",
               getName(),
               pNewValue);

        return super.setEventHook(pOldValue, pNewValue);
      }

    };
  }

  @Override
  public void zero()
  {

  }

  @Override
  public void setMode(int pU, int pV, double pValue)
  {

  }

  @Override
  public long getRelaxationTimeInMilliseconds()
  {
    return 1;
  }


  @Override
  public boolean setZernikeFactors(double[] pZernikeFactors) {
    info("Sending factors to simulated mirror: " + pZernikeFactors);
    setZernikeFactorsInternal(pZernikeFactors);
    return true;
  }

  @Override
  public boolean start()
  {
    return true;
  }

  @Override
  public boolean stop()
  {
    return true;
  }

}
