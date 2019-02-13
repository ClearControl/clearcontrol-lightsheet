package clearcontrol.devices.stages.kcube.sim;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.stages.BasicStageInterface;

/**
 * SimulatedBasicStageDevice
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 04 2018
 */
public class SimulatedBasicStageDevice extends VirtualDevice
                                       implements BasicStageInterface
{

  Variable<Double> mPosition = new Variable<Double>("X", 0.0);

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName
   *          device name
   */
  public SimulatedBasicStageDevice(String pDeviceName)
  {
    super("Stage " + pDeviceName);
  }

  @Override
  public boolean moveBy(double pDistance, boolean pWaitToFinish)
  {
    mPosition.set(mPosition.get() + pDistance);
    return true;
  }

  @Override
  public Variable<Double> getPositionVariable()
  {
    return mPosition;
  }
}
