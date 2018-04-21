package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.StackInterfaceContainer;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class OpticsPrefusedImageDataContainer extends
                                              StackInterfaceContainer
{
  public OpticsPrefusedImageDataContainer(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override public boolean isDataComplete()
  {
    for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++) {
      if (! super.containsKey("C" + d + "opticsprefused")) {
        return false;
      }
    }
    return true;
  }
}
