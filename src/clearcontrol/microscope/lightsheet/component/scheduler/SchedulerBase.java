package clearcontrol.microscope.lightsheet.component.scheduler;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.MicroscopeInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public abstract class SchedulerBase extends VirtualDevice implements
                                                          SchedulerInterface
{
  private Variable<Boolean>
      mActiveVariable = new Variable<Boolean>("active", false);

  protected MicroscopeInterface mMicroscope = null;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public SchedulerBase(String pDeviceName)
  {
    super(pDeviceName);
  }

  @Override public Variable<Boolean> getActiveVariable()
  {
    return mActiveVariable;
  }

  public void setMicroscope(MicroscopeInterface pMicroscope) {
    mMicroscope = pMicroscope;
  }
}
