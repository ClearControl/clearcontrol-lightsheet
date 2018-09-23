package clearcontrol.devices.lasers.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * Deprecated: Use SwitchLaserOnOffInstruction or SwitchLaserPowerOnOffInstruction
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
@Deprecated
public class LaserOnOffInstruction extends InstructionBase implements
                                   InstructionInterface,
                                   LoggingFeature
{
  LaserDeviceInterface mLaserDevice;
  boolean mTurnOn;

  private Variable<Boolean> mDebugVariable =
                                           new Variable<Boolean>("Debug",
                                                                 true);

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public LaserOnOffInstruction(LaserDeviceInterface pLaserDevice,
                               boolean pTurnOn)
  {
    super("Laser: Turn " + pLaserDevice.getName()
          + " "
          + (pTurnOn ? "ON" : "OFF"));
    mLaserDevice = pLaserDevice;
    mTurnOn = pTurnOn;
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    if (mDebugVariable.get())
    {
      info("Laser " + (mTurnOn ? "On" : "Off"));
      info("Laser operating mode "
           + mLaserDevice.getOperatingModeVariable().get());
      info("Wavelength " + mLaserDevice.getWavelengthInNanoMeter());
      info("Power on before "
           + mLaserDevice.getPowerOnVariable().get());
      info("Laser on before "
           + mLaserDevice.getLaserOnVariable().get());
      info("Current power before "
           + mLaserDevice.getCurrentPowerInMilliWattVariable().get());
      info("Target power before "
           + mLaserDevice.getTargetPowerInMilliWattVariable().get());
    }
    mLaserDevice.setLaserPowerOn(mTurnOn);
    mLaserDevice.setLaserOn(mTurnOn);
    mLaserDevice.setLaserPowerOn(mTurnOn);
    mLaserDevice.setLaserOn(mTurnOn);
    mLaserDevice.getLaserOnVariable().set(mTurnOn);
    mLaserDevice.getPowerOnVariable().set(mTurnOn);

    if (mDebugVariable.get())
    {
      info("Laser " + (mTurnOn ? "On" : "Off"));
      info("Laser operating mode "
           + mLaserDevice.getOperatingModeVariable().get());
      info("Wavelength " + mLaserDevice.getWavelengthInNanoMeter());
      info("Power on after "
           + mLaserDevice.getPowerOnVariable().get());
      info("Laser on after "
           + mLaserDevice.getLaserOnVariable().get());
      info("Current power after "
           + mLaserDevice.getCurrentPowerInMilliWattVariable().get());
      info("Target power after "
           + mLaserDevice.getTargetPowerInMilliWattVariable().get());
    }
    return true;
  }

  @Override
  public LaserOnOffInstruction copy()
  {
    return new LaserOnOffInstruction(mLaserDevice, mTurnOn);
  }

  public Variable<Boolean> getDebugVariable()
  {
    return mDebugVariable;
  }
}
