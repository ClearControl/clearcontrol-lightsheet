package clearcontrol.devices.stages.kcube.impl;

import aptj.APTJDevice;
import aptj.APTJExeption;
import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.devices.stages.StageDeviceInterface;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;

import java.util.concurrent.TimeUnit;

/**
 *
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class KCubeDevice extends VirtualDevice
                            implements VisualConsoleInterface,
                                       LoggingFeature,
                                       BasicStageInterface
{
  protected long mPollPeriodWhileWaiting = 100;
  protected long mTimeoutWhileWaiting = 1000;
  protected TimeUnit mTimeUnit = TimeUnit.MILLISECONDS;

  private Variable<Double> mPositionVariable = new Variable<Double>("position", 0.0 );

  long mSerialId;
  APTJDevice mKCubeAPTJDevice = null;

  /**
   * The constructor is package private because you are supposed to
   * get new instances from the KCubeFactory.
   *
   * @param pKCubeDevice
   */
  KCubeDevice(APTJDevice pKCubeDevice, String pName)
  {
    super(pName + " (Thorlabs K-cube, " + pKCubeDevice.getSerialNumber() + ") test");
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

    //System.out.println("APTLibrary.APTInit();");
    //APTLibrary.
  //  System.out.println("APTLibrary.InitHWDevice(" + mSerialId + ");");
//    APTLibrary.InitHWDevice(mSerialId);
    //System.out.println("B");
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

  public double getMinPosition() {
    if (mKCubeAPTJDevice != null)   {
      return mKCubeAPTJDevice.getMinPosition();
    }
    return Double.NaN;
  }
  public double getMaxPosition() {
    if (mKCubeAPTJDevice != null)   {
      return mKCubeAPTJDevice.getMaxPosition();
    }
    return Double.NaN;
  }
  public double getCurrentPosition() {
    if (mKCubeAPTJDevice != null)   {
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

  public boolean moveBy(double pStep, boolean pWaitToFinish) {
    double lNewPosition = getCurrentPosition() + pStep;
    if (lNewPosition > getMaxPosition() || lNewPosition < getMinPosition()) {
      warning("The KCube controlled motor " + mSerialId + " cannot be moved to position " + lNewPosition + ", it would be out of [" + getMinPosition() + ", " + getMaxPosition() + "]");
      return false;
    }


    info("Moving KCube " + mSerialId + " by " + pStep);

    return moveTo(pStep, pWaitToFinish);
  }

  @Override public Variable<Double> getPositionVariable()
  {
    return mPositionVariable;
  }

  public boolean moveTo(double pPosition)
  {
    return moveTo(pPosition, false);
  }

  public boolean moveTo(double pPosition, boolean pWaitToFinish) {
    info("Moving KCube " + mSerialId + " to " + pPosition);
    try
    {
      mKCubeAPTJDevice.moveBy(pPosition);
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


}
