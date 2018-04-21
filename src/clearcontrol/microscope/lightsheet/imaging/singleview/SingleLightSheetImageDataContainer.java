package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class SingleLightSheetImageDataContainer extends
                                          StackInterfaceContainer
{
  public SingleLightSheetImageDataContainer(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override public boolean isDataComplete()
  {
    for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++) {
      if (! super.containsKey("C" + d + "singlelightsheet")) {
        return false;
      }
    }
    return true;
  }
}

