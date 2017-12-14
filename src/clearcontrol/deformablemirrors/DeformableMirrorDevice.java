package clearcontrol.deformablemirrors;

import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.devices.slm.slms.devices.alpao.AlpaoDMDevice;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class DeformableMirrorDevice extends TaskDevice
                                    implements VisualConsoleInterface
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
}
