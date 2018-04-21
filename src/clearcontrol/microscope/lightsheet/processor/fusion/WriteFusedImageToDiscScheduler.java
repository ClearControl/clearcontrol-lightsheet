package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerToDiscScheduler;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteFusedImageToDiscScheduler extends
                                            WriteStackInterfaceContainerToDiscScheduler
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pChannelName
   */
  public WriteFusedImageToDiscScheduler(String pChannelName)
  {
    super("IO: Write " + pChannelName + " fused stack to disc", FusedImageDataContainer.class, new String[]{"fused"}, pChannelName);
  }
}
