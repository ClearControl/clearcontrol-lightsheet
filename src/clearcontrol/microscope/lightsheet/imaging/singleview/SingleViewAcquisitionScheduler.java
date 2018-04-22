package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialAcquisitionScheduler;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;

/**
 * This scheduler acquires a single image stack for a defined camera
 * and light sheet.
 * The image stacks are stored in the DataWarehouse in
 * an SingleLightSheetImageDataContainer with a key like:
 *
 * C0L0
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class SingleViewAcquisitionScheduler extends
                                            SequentialAcquisitionScheduler implements
                                                                                SchedulerInterface,
                                                                                LoggingFeature
{
  int mCameraIndex;
  int mLightSheetIndex;

  public SingleViewAcquisitionScheduler(int pCameraIndex, int pLightSheetIndex) {
    super("Acquisition: Single view C" + pCameraIndex + "L" + pLightSheetIndex + " (heap++)");
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
