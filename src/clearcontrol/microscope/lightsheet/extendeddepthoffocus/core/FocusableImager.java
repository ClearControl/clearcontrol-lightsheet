package clearcontrol.microscope.lightsheet.extendeddepthoffocus.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.lightsheet.si.BinaryStructuredIlluminationPattern;
import clearcontrol.stack.OffHeapPlanarStack;

/**
 * The FocusableImager takes a LightSheetMicroscope and some configuration
 * parameters. Afterwards it allow imaging given lightsheet / detection arm Z
 * positions without programming overhead.
 * <p>
 * Example pseudo code:
 * <p>
 * imager = new FocusableImager(lightSheetMicroscope, ...)
 * <p>
 * imager.addImageRequest(lightSheetZ - 1, detectionArmZ)
 * imager.addImageRequest(lightSheetZ, detectionArmZ)
 * imager.addImageRequest(lightSheetZ + 1, detectionArmZ)
 * <p>
 * stack = imager.configurationStateOfLightSheetChanged()
 * <p>
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) October 2017
 */
@Deprecated
public class FocusableImager implements LoggingFeature
{
  LightSheetMicroscope mLightSheetMicroscope;

  LightSheetMicroscopeQueue mQueue;

  int mLightSheetMinIndex;
  int mLightSheetMaxIndex;

  int mDetectionArmIndex;

  int mNumberOfExpectedImages = 0;
  double mInitialDetectionZ = 0;

  public FocusableImager(LightSheetMicroscope pLightSheetMicroscope,
                         int pLightSheetMinIndex,
                         int pLightSheetMaxIndex,
                         int pDetectionArm,
                         double pExposureTimeInSeconds)
  {
    mLightSheetMicroscope = pLightSheetMicroscope;
    mQueue = mLightSheetMicroscope.requestQueue();
    mLightSheetMinIndex = pLightSheetMinIndex;
    mLightSheetMaxIndex = pLightSheetMaxIndex;
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

      for (int i = 0; i <= 3; i++)
      {
        mQueue.setI(i, false);
      }
      for (int i = mLightSheetMinIndex; i <= mLightSheetMaxIndex; i++)
      {
        mQueue.setI(i, true);
        mQueue.setIX(i, 0);
        mQueue.setIY(i, 0);
        mQueue.setIPattern(i,
                           0,
                           new BinaryStructuredIlluminationPattern());
        mQueue.setIPattern(i,
                           0,
                           new BinaryStructuredIlluminationPattern());
      }

      mInitialDetectionZ = detectionZ;
      mQueue.setDZ(mDetectionArmIndex, detectionZ);
      mQueue.setC(mDetectionArmIndex, false);

      mQueue.addCurrentStateToQueue();
    }

    mQueue.setDZ(mDetectionArmIndex, detectionZ);

    for (int i = mLightSheetMinIndex; i <= mLightSheetMaxIndex; i++)
    {
      mQueue.setIZ(i, illuminationZ);
    }
    mQueue.setC(mDetectionArmIndex, true);
    mQueue.addCurrentStateToQueue();

    mNumberOfExpectedImages++;
  }

  public OffHeapPlanarStack execute() throws InterruptedException,
                                      ExecutionException,
                                      TimeoutException
  {
    if (mNumberOfExpectedImages == 0)
    {
      return null;
    }

    info("imaging... " + mNumberOfExpectedImages + " images...");

    mQueue.setDZ(mDetectionArmIndex, mInitialDetectionZ);
    mQueue.setC(mDetectionArmIndex, false);

    mQueue.addCurrentStateToQueue();

    mQueue.setTransitionTime(0.1);

    mQueue.finalizeQueue();

    mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
    final Boolean lPlayQueueAndWait =
                                    mLightSheetMicroscope.playQueueAndWaitForStacks(mQueue,
                                                                                    100 + mQueue.getQueueLength(),
                                                                                    TimeUnit.SECONDS);

    if (!lPlayQueueAndWait)
    {
      return null;
    }

    OffHeapPlanarStack lResultingStack =
                                       (OffHeapPlanarStack) mLightSheetMicroscope.getCameraStackVariable(mDetectionArmIndex)
                                                                                 .get();

    if (lResultingStack.getDepth() != mNumberOfExpectedImages)
    {
      warning("Warning: number of resulting image does not match the expected number. The stack may be corrupted.");
    }

    info("imaging done...");
    return lResultingStack;
  }

  public int getDetectionArmIndex()
  {
    return mDetectionArmIndex;
  }
}