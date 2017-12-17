package clearcontrol.deformablemirrors;

import clearcontrol.core.device.position.PositionDeviceInterface;
import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.slm.slms.devices.alpao.AlpaoDMDevice;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class DeformableMirrorDevice extends TaskDevice
                                    implements PositionDeviceInterface
{
  AlpaoDMDevice mAlpaoDMDevice;

  public DeformableMirrorDevice(int pDeviceIndex)
  {
    super("Deformable mirror");
    mAlpaoDMDevice = new AlpaoDMDevice(pDeviceIndex);
  }

  public AlpaoDMDevice getAlpaoDMDevice()
  {
    return mAlpaoDMDevice;
  }

  @Override
  public void run()
  {

  }

  @Override
  public Variable<Integer> getPositionVariable()
  {
    return null;
  }

  @Override
  public int[] getValidPositions()
  {
    return new int[0];
  }

  @Override
  public void setPositionName(int pPositionIndex,
                              String pPositionName)
  {

  }

  @Override
  public String getPositionName(int pPositionIndex)
  {
    return null;
  }
}
