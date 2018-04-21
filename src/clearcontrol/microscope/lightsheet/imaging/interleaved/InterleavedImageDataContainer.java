package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

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
