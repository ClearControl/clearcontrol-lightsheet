package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.stack.StackInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
public interface ImagerInterface
{
  void setExposureTimeInSeconds(double pExposureTimeInSeconds);

  LightSheetMicroscope getLightSheetMicroscope();

  void setImageWidth(int pImageWidth);
  void setImageHeight(int pImageHeight);

  StackInterface acquire();
}
