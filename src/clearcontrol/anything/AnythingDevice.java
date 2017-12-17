package clearcontrol.anything;

import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class AnythingDevice extends TaskDevice
                            implements VisualConsoleInterface
{
  public AnythingDevice()
  {
    super("Anything");
  }

  @Override
  public void run()
  {

  }
}
