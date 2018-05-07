package clearcontrol.microscope.lightsheet.warehouse.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

/**
 * This class contains some convenience methods for working with
 * DataContainers.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public abstract class DataContainerBase implements
                                        DataContainerInterface
{
  private long mTimePoint;

  protected DataContainerBase(long pTimepoint) {
    mTimePoint = pTimepoint;
  }

  @Override public long getTimepoint()
  {
    return mTimePoint;
  }
}
