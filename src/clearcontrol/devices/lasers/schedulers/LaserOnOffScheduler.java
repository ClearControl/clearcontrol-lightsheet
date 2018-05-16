package clearcontrol.devices.lasers.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class LaserOnOffScheduler extends SchedulerBase implements
                                                       SchedulerInterface,
                                                       LoggingFeature
{
  LaserDeviceInterface mLaserDevice;
  boolean mTurnOn;

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public LaserOnOffScheduler(LaserDeviceInterface pLaserDevice, boolean pTurnOn)
  {
    super("Laser: Turn " + pLaserDevice.getName() + " " + " (" + pLaserDevice.getWavelengthInNanoMeter() + "nm)" + (pTurnOn?"ON":"OFF"));
    mLaserDevice = pLaserDevice;
    mTurnOn = pTurnOn;
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    mLaserDevice.setLaserPowerOn(mTurnOn);
    mLaserDevice.setLaserOn(mTurnOn);
    mLaserDevice.setLaserPowerOn(mTurnOn);
    mLaserDevice.setLaserOn(mTurnOn);
    return true;
  }


}
