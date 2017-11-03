package clearcontrol.microscope.lightsheet.extendeddepthfield;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.stack.OffHeapPlanarStack;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * The FocusableImager takes a LightSheetMicroscope and some configuration parameters.
 * Afterwards it allow imaging given lightsheet / detection arm Z positions
 * without programming overhead.
 *
 * Example pseudo code:
 *
 * imager = new FocusableImager(lightSheetMicroscope, ...)
 *
 * imager.addImageRequest(lightSheetZ - 1, detectionArmZ)
 * imager.addImageRequest(lightSheetZ, detectionArmZ)
 * imager.addImageRequest(lightSheetZ + 1, detectionArmZ)
 *
 * stack = imager.execute()
 *
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * October 2017
 */
public class FocusableImager
{
  LightSheetMicroscope mLightSheetMicroscope;

  LightSheetMicroscopeQueue mQueue;

  int mLightSheetIndex;
  int mDetectionArmIndex;

  int mNumberOfExpectedImages = 0;
  double mInitialDetectionZ = 0;

  public FocusableImager(LightSheetMicroscope pLightSheetMicroscope, int pLightSheetIndex, int pDetectionArm, double pExposureTimeInSeconds)
  {
    mLightSheetMicroscope = pLightSheetMicroscope;
    mQueue = mLightSheetMicroscope.requestQueue();
    mLightSheetIndex = pLightSheetIndex;
    mDetectionArmIndex = pDetectionArm;

    mQueue.clearQueue();
    // lQueue.zero();

    mQueue.setFullROI();

    mQueue.setExp(pExposureTimeInSeconds);
  }

  public void setFieldOfView(int pImageWidth, int pImageHeight)
  {
    mQueue.setCenteredROI(pImageWidth, pImageHeight);
  }

  public void setExposureTimeInSeconds(double pExposureTimeInSeconds)
  {
    mQueue.setExp(pExposureTimeInSeconds);
  }

  public void addImageRequest(double illuminationZ, double detectionZ)
  {
    if (mNumberOfExpectedImages == 0)
    {
      mQueue.setI(mLightSheetIndex);
      mQueue.setIX(mLightSheetIndex, 0);
      mQueue.setIY(mLightSheetIndex, 0);

      mInitialDetectionZ = detectionZ;
      mQueue.setDZ(mDetectionArmIndex, detectionZ);
      mQueue.setC(mDetectionArmIndex, false);
      mQueue.addCurrentStateToQueue();
    }

    mQueue.setDZ(mDetectionArmIndex, detectionZ);
    mQueue.setIZ(mLightSheetIndex, illuminationZ); // !!!! vv
    mQueue.setC(mDetectionArmIndex, true);
    mQueue.addCurrentStateToQueue();

    mNumberOfExpectedImages++;
  }

  public OffHeapPlanarStack execute() throws
                                      InterruptedException,
                                      ExecutionException,
                                      TimeoutException
  {
    if (mNumberOfExpectedImages == 0)
    {
      return null;
    }

    System.out.println("imaging... " + mNumberOfExpectedImages + " images...");

    mQueue.setDZ(mDetectionArmIndex, mInitialDetectionZ);
    mQueue.setC(mDetectionArmIndex, false);

    mQueue.addCurrentStateToQueue();

    mQueue.setTransitionTime(0.1);

    mQueue.finalizeQueue();

    mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
    final Boolean
        lPlayQueueAndWait =
        mLightSheetMicroscope.playQueueAndWaitForStacks(mQueue,
                                                        100 + mQueue.getQueueLength(),
                                                        TimeUnit.SECONDS);

    if (!lPlayQueueAndWait) {
      return null;
    }

    OffHeapPlanarStack lResultingStack = (OffHeapPlanarStack) mLightSheetMicroscope.getCameraStackVariable(
        mDetectionArmIndex).get();

    if (lResultingStack.getDepth() != mNumberOfExpectedImages) {
      System.out.println("Warning: number of resulting image does not match the expected number. The stack may be corrupted.");
    }

    System.out.println("imaging done...");
    return lResultingStack;
  }
}