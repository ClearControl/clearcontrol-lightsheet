package clearcontrol.devices.stages.kcube.impl;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.stages.BasicThreeAxesStageInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class KCubeThreeAxesStageDevice extends VirtualDevice implements
                                                             BasicThreeAxesStageInterface
{
  private Variable<Double> mPositionX;
  private Variable<Double> mPositionY;
  private Variable<Double> mPositionZ;

  private KCubeDevice mXStepMotor;
  private KCubeDevice mYStepMotor;
  private KCubeDevice mZStepMotor;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public KCubeThreeAxesStageDevice(String pDeviceName, KCubeDevice pXStepMotorDevice, KCubeDevice pYStepMotorDevice, KCubeDevice pZStepMotorDevice)
  {
    super(pDeviceName);

    mXStepMotor = pXStepMotorDevice;
    mYStepMotor = pYStepMotorDevice;
    mZStepMotor = pZStepMotorDevice;


    mPositionX = new Variable<Double>("positionX", pXStepMotorDevice.getCurrentPosition());
    mPositionY = new Variable<Double>("positionY", pYStepMotorDevice.getCurrentPosition());
    mPositionZ = new Variable<Double>("positionZ", pZStepMotorDevice.getCurrentPosition());

  }

  public boolean moveXBy(double pDistance, boolean pWaitUntilMotionFinished) {
    boolean lResult = mXStepMotor.moveBy(pDistance, pWaitUntilMotionFinished);
    if (pWaitUntilMotionFinished) {
      refreshVariables();
    }
    return lResult;
  }

  public boolean moveYBy(double pDistance, boolean pWaitUntilMotionFinished) {
    boolean lResult = mYStepMotor.moveBy(pDistance, pWaitUntilMotionFinished);
    if (pWaitUntilMotionFinished) {
      refreshVariables();
    }
    return lResult;
  }

  public boolean moveZBy(double pDistance, boolean pWaitUntilMotionFinished) {
    boolean lResult = mZStepMotor.moveBy(pDistance, pWaitUntilMotionFinished);
    if (pWaitUntilMotionFinished) {
      refreshVariables();
    }
    return lResult;
  }

  @Override public Variable<Double> getXPositionVariable()
  {
    return mPositionX;
  }

  @Override public Variable<Double> getYPositionVariable()
  {
    return mPositionY;
  }

  @Override public Variable<Double> getZPositionVariable()
  {
    return mPositionZ;
  }

  public void refreshVariables() {
    mPositionX.set(mXStepMotor.getCurrentPosition());
    mPositionY.set(mYStepMotor.getCurrentPosition());
    mPositionZ.set(mZStepMotor.getCurrentPosition());
  }




}
