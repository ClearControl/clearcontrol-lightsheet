package clearcontrol.devices.stages.kcube.sim;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.stages.BasicThreeAxesStageInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class SimulatedThreeAxesStageDevice extends VirtualDevice implements
                                                                 BasicThreeAxesStageInterface,
                                                                 LoggingFeature
{
  Variable<Double> mPositionX = new Variable<Double>("X", 0.0);
  Variable<Double> mPositionY = new Variable<Double>("Y", 0.0);
  Variable<Double> mPositionZ = new Variable<Double>("Z", 0.0);
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public SimulatedThreeAxesStageDevice()
  {
    super("Simulated 3 axes stage" );
  }

  @Override public boolean moveXBy(double pDistance,
                                   boolean pWaitToFinish)
  {
    sleep();
    mPositionX.set(mPositionX.get() + pDistance);
    info("New X position: " + mPositionX);
    return true;
  }

  @Override public boolean moveYBy(double pDistance,
                                   boolean pWaitToFinish)
  {
    sleep();
    mPositionY.set(mPositionY.get() + pDistance);
    info("New Y position: " + mPositionY);
    return true;
  }

  @Override public boolean moveZBy(double pDistance,
                                   boolean pWaitToFinish)
  {
    sleep();
    mPositionZ.set(mPositionZ.get() + pDistance);

    info("New Z position: " + mPositionZ);
    return true;
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

  private void sleep() {
    try
    {
      Thread.sleep(500);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }
}
