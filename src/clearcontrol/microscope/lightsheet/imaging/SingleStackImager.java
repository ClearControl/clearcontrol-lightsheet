package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class SingleStackImager extends ImagerBase
{
  private double mIlluminationZStepDistance = 1;
  private double mDetectionZStepDistance = 1;
  private int mNumberOfRequestedImages = 1;

  public SingleStackImager(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override protected boolean configureQueue(LightSheetMicroscopeQueue pQueue)
  {
    for (int lImageCount = 0; lImageCount < mNumberOfRequestedImages; lImageCount++)
    {
      if (lImageCount == 0) {
        pQueue.setIZ(mLightSheetIndex, mIlluminationZ);
        pQueue.setDZ(mDetectionArmIndex, mDetectionZ);
      }
      else
      {
        pQueue.setIZ(mLightSheetIndex,
                     pQueue.getIZ(mLightSheetIndex) + mIlluminationZStepDistance);
        pQueue.setDZ(mDetectionArmIndex,
                     pQueue.getDZ(mDetectionArmIndex) + mDetectionZStepDistance);
      }
      pQueue.setC(mDetectionArmIndex, true);
      pQueue.addCurrentStateToQueue();
    }
    return true;
  }

  public void setIlluminationZStepDistance(double mIlluminationZStepDistance)
  {
    this.mIlluminationZStepDistance = mIlluminationZStepDistance;
  }

  public void setDetectionZStepDistance(double mDetectionZStepDistance)
  {
    this.mDetectionZStepDistance = mDetectionZStepDistance;
  }

  public void setNumberOfRequestedImages(int mNumberOfRequestedImages)
  {
    this.mNumberOfRequestedImages = mNumberOfRequestedImages;
  }
}
