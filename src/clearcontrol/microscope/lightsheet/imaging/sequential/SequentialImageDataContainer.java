package clearcontrol.microscope.lightsheet.imaging.sequential;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * This container contains the raw images resulting from sequential
 * acquisition. For example for a microscope with two cameras, the
 * stack have these keys:
 *
 * C0L0
 * C1L0
 * C0L1
 * C1L1
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class SequentialImageDataContainer  extends
                                           StackInterfaceContainer
{
  public SequentialImageDataContainer(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override public boolean isDataComplete()
  {
    for (int l = 0; l < getLightSheetMicroscope().getNumberOfDetectionArms(); l++)
    {
      for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++)
      {
        if (!super.containsKey("C" + d + "L" + l ))
        {
          return false;
        }
      }
    }
    return true;
  }
}
