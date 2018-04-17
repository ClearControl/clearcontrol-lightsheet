package clearcontrol.microscope.lightsheet.timelapse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;

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

  public SingleViewAcquisitionScheduler(int pCameraIndex, int pLightSheetIndex, RecyclerInterface<StackInterface, StackRequest> pRecycler) {
    super("Acquisition: Single view C" + pCameraIndex + "L" + pLightSheetIndex, pRecycler);
    mCameraIndex = pCameraIndex;
    mLightSheetIndex = pLightSheetIndex;

    mImageKeyToSave = "C" + pCameraIndex + "L" + pLightSheetIndex;
    mChannelName = mImageKeyToSave;
  }


  protected boolean isLightSheetOn(int pLightIndex) {
    return mLightSheetIndex == pLightIndex;
  }

  protected boolean isCameraOn(int pCameraIndex) {
    return mCameraIndex == pCameraIndex;
  }

  protected boolean isFused() {
    return true;
  }

  public int getLightSheetIndex() {
    return mLightSheetIndex;
  }

  public int getCameraIndex() {
    return mCameraIndex;
  }

  @Override
  public StackInterface getLastAcquiredStack()
  {
    return mLastAcquiredStack;
  }
}
