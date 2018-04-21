package clearcontrol.microscope.lightsheet.imaging.sequential;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.StackInterfaceContainer;

/**
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
