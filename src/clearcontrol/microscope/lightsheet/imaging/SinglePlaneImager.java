package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class SinglePlaneImager extends ImagerBase implements LoggingFeature
{



  public SinglePlaneImager(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override protected boolean configureQueue(LightSheetMicroscopeQueue pQueue)
  {
    if (mInterpolatedAcquisitionState == null)
    {
      pQueue.setIZ(mLightSheetIndex, mIlluminationZ);
    }
    pQueue.setDZ(mDetectionArmIndex, mDetectionZ);

    pQueue.setC(mDetectionArmIndex, true);
    pQueue.addCurrentStateToQueue();
    return true;
  }
}
