package clearcontrol.microscope.lightsheet.timelapse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class SingleViewAcquisitionScheduler extends SequentialAcquisitionScheduler implements
                                                                                SchedulerInterface,
                                                                                LoggingFeature
{
  int mCameraIndex;
  int mLightSheetIndex;

  public SingleViewAcquisitionScheduler(int pCameraIndex, int pLightSheetIndex) {
    super("Acquisition: Single view C" + pCameraIndex + "L" + pLightSheetIndex);
  }


  protected boolean isLightSheetOn(int pLightIndex) {
    return mLightSheetIndex == pLightIndex;
  }

  protected boolean isCameraOn(int pCameraIndex) {
    return mCameraIndex == pCameraIndex;
  }

  protected boolean isFused() {
    return false;
  }



}
