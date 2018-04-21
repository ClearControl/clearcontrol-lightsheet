package clearcontrol.microscope.lightsheet.timelapse.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.DataContainerInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataContainerBase;
import clearcontrol.microscope.lightsheet.warehouse.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class InterleavedImageDataContainer extends
                                           StackInterfaceContainer
{
  public InterleavedImageDataContainer(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override public boolean isDataComplete()
  {
    for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++) {
      if (! super.containsKey("C" + d + "interleaved")) {
        return false;
      }
    }
    return true;
  }
}
