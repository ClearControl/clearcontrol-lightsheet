package clearcontrol.devices.stages.kcube.impl;

import java.util.concurrent.TimeUnit;

import aptj.APTJDevice;
import aptj.APTJExeption;
import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;

/**
 *
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class KCubeDevice extends VirtualDevice implements
                         VisualConsoleInterface,
                         LoggingFeature,
                         BasicStageInterface
{
  protected long mPollPeriodWhileWaiting = 100;
  protected long mTimeoutWhileWaiting = 1000;
  protected TimeUnit mTimeUnit = TimeUnit.MILLISECONDS;

  private Variable<Double> mPositionVariable =
                                             new Variable<Double>("position",
                                                                  0.0);

  long mSerialId;
  APTJDevice mKCubeAPTJDevice = null;

  /**
   * The constructor is package private because you are supposed to get new
   * instances from the KCubeFactory.
   *
   * @param pKCubeDevice
   */
  KCubeDevice(APTJDevice pKCubeDevice, String pName)
  {
    super(pName + " (Thorlabs K-cube, "
          + pKCubeDevice.getSerialNumber()
          + ")");
    mKCubeAPTJDevice = pKCubeDevice;
    mSerialId = mKCubeAPTJDevice.getSerialNumber();
    try
    {
      mPositionVariable.set(mKCubeAPTJDevice.getCurrentPosition());
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();
    }

    // System.out.println("APTLibrary.APTInit();");
    // APTLibrary.
    // System.out.println("APTLibrary.InitHWDevice(" + mSerialId + ");");
    // APTLibrary.InitHWDevice(mSerialId);
    // System.out.println("B");
    /*
    try
    {
      mKCubeAPTJDevice = mAPTJLibrary.createDeviceFromSerialNumber(mSerialId);
    
      System.out.println(mKCubeAPTJDevice.getCurrentPosition());
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();
    }*/
  }

  public double getMinPosition()
  {
    if (mKCubeAPTJDevice != null)
    {
      return mKCubeAPTJDevice.getMinPosition();
    }
    return Double.NaN;
  }

  public double getMaxPosition()
  {
    if (mKCubeAPTJDevice != null)
    {
      return mKCubeAPTJDevice.getMaxPosition();
    }
    return Double.NaN;
  }

  public double getCurrentPosition()
  {
    if (mKCubeAPTJDevice != null)
    {
      try
      {
        return mKCubeAPTJDevice.getCurrentPosition();
      }
      catch (APTJExeption aptjExeption)
      {
        aptjExeption.printStackTrace();
      }
    }
    return Double.NaN;
  }

  public boolean moveBy(double pStep, boolean pWaitToFinish)
  {
    double lNewPosition = getCurrentPosition() + pStep;
    /*if (lNewPosition > getMaxPosition() || lNewPosition < getMinPosition()) {
      warning("The KCube controlled motor " + mSerialId + " cannot be moved to position " + lNewPosition + ", it would be out of [" + getMinPosition() + ", " + getMaxPosition() + "]");
      return false;
    }*/
    info("Moving KCube " + mSerialId + " by " + pStep);
    try
    {
      mKCubeAPTJDevice.moveBy(pStep);
      if (pWaitToFinish)
      {
        mKCubeAPTJDevice.waitWhileMoving(mPollPeriodWhileWaiting,
                                         mTimeoutWhileWaiting,
                                         mTimeUnit);
        mPositionVariable.set(mKCubeAPTJDevice.getCurrentPosition());
      }
      return true;
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();
    }
    return false;

  }

  @Override
  public Variable<Double> getPositionVariable()
  {
    mPositionVariable.set(getCurrentPosition());
    return mPositionVariable;
  }
}
