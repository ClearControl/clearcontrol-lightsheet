package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;

/**
 * This DataContainer is used to store results for fast fusion. The
 * fused image shall be stored with the key "fused"
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class FusedImageDataContainer extends StackInterfaceContainer
{
  public FusedImageDataContainer(long pTimePoint) {
    super(pTimePoint);
  }

  @Override
  public StackInterface put(String key, StackInterface value) {
    return super.put("fused", value);
  }

  @Override public boolean isDataComplete()
  {
    return super.containsKey("fused");
  }
}