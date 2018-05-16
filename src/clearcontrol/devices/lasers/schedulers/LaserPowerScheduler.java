package clearcontrol.devices.lasers.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
public class LaserPowerScheduler extends SchedulerBase implements
                                                       SchedulerInterface,
                                                       LoggingFeature
{
  private final LaserDeviceInterface mLaser;
  private final double mLaserPowerInPercent;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLaser laser to control
   * @param pLaserPowerInPercent power to sent in percent
   */
  public LaserPowerScheduler(LaserDeviceInterface pLaser, double pLaserPowerInPercent)
  {
    super("Laser: Set " + pLaser.getName() + " (" + pLaser.getWavelengthInNanoMeter() + "nm) power to " + pLaserPowerInPercent + "%");
    mLaser = pLaser;
    mLaserPowerInPercent = pLaserPowerInPercent;
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    mLaser.setTargetPowerInPercent(mLaserPowerInPercent);
    return true;
  }
}
