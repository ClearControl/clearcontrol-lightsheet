package clearcontrol.microscope.lightsheet.warehouse.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public abstract class DataContainerBase implements
                                        DataContainerInterface
{
  private long mTimePoint;
  private LightSheetMicroscope mLightSheetMicroscope;

  protected DataContainerBase(LightSheetMicroscope pLightSheetMicroscope)
  {
    this(pLightSheetMicroscope, pLightSheetMicroscope.getDevice(
        LightSheetTimelapse.class, 0).getTimePointCounterVariable().get());
  }

  protected DataContainerBase(LightSheetMicroscope pLightSheetMicroscope, long pTimepoint) {
    mTimePoint = pTimepoint;
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  @Override public long getTimepoint()
  {
    return mTimePoint;
  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }
}
