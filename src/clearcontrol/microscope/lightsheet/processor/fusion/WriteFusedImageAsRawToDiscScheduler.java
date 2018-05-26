package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscScheduler;

/**
 * This instructions writes a fused image to disc. Depending on how the images
 * was fused, it might be stored in different folders.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteFusedImageAsRawToDiscScheduler extends
        WriteStackInterfaceContainerAsRawToDiscScheduler
{
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pChannelName
   */
  public WriteFusedImageAsRawToDiscScheduler(String pChannelName)
  {
    super("IO: Write " + pChannelName + " fused stack to disc", FusedImageDataContainer.class, new String[]{"fused"}, pChannelName);
  }
}
