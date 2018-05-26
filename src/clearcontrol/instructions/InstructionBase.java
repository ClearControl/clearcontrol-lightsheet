package clearcontrol.instructions;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.microscope.MicroscopeInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public abstract class InstructionBase extends VirtualDevice implements
                                                          SchedulerInterface
{
  protected MicroscopeInterface mMicroscope = null;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public InstructionBase(String pDeviceName)
  {
    super(pDeviceName);
  }

  public void setMicroscope(MicroscopeInterface pMicroscope) {
    mMicroscope = pMicroscope;
  }
}
