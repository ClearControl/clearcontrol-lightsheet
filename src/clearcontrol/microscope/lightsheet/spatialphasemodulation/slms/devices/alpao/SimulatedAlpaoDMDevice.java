package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.alpao;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.DeformableMirrorDevice;

import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class SimulatedAlpaoDMDevice extends DeformableMirrorDevice
                                    implements LoggingFeature
{
  private static final int cFullMatrixWidthHeight = 11;
  private static final int cActuatorResolution = 2 << 14;
  private static final int cNumberOfActuators = 96;

  public SimulatedAlpaoDMDevice(int pAlpaoDeviceIndex)
  {
    this(MachineConfiguration.get()
                             .getStringProperty("device.ao.dm.alpao."
                                                + pAlpaoDeviceIndex,
                                                "NULL"));

  }

  public SimulatedAlpaoDMDevice(String pAlpaoSerialName)
  {
    super("ALPAO_" + pAlpaoSerialName,
          cFullMatrixWidthHeight,
          cActuatorResolution);

    mMatrixVariable = new Variable<DenseMatrix64F>("MatrixReference")
    {
      @Override
      public DenseMatrix64F setEventHook(final DenseMatrix64F pOldValue,
                                         final DenseMatrix64F pNewValue)
      {
        // change matrix element value

        return super.setEventHook(pOldValue, pNewValue);
      }

    };

  }

  @Override
  public boolean open()
  {
    final boolean lOpen = true;
    mNumberOfActuatorsVariable =
                               new Variable<Double>("NumberOfActuators",
                                                    (double) cNumberOfActuators);
    return lOpen;

  }

  @Override
  public boolean start()
  {
    zero();
    return true;
  }

  @Override
  public void zero()
  {
    // todo: reset the mirror to flat
  }

  @Override
  public boolean stop()
  {
    return true;
  }

  @Override
  public boolean close()
  {
    return true;
  }

  @Override
  public long getRelaxationTimeInMilliseconds()
  {
    return 1;
  }

}
