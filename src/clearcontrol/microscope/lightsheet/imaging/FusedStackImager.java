package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.timelapse.*;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
public class FusedStackImager implements ImagerInterface, LoggingFeature
{
  private LightSheetMicroscope mLightSheetMicroscope;

  private AcquisitionType mAcquisitionType = AcquisitionType.TimeLapseInterleaved;
  private double mMinZ = 0;
  private double mMaxZ = 0;
  private double mSliceDistance = 2.0;
  private double mExposureTimeInSeconds = 0.01;
  private int mImageHeight;
  private int mImageWidth;

  public FusedStackImager(LightSheetMicroscope pLightSheetMicroscope) {
    mLightSheetMicroscope = pLightSheetMicroscope;
    mMinZ = pLightSheetMicroscope.getDetectionArm(0).getZVariable().getMin().doubleValue();
    mMaxZ = pLightSheetMicroscope.getDetectionArm(0).getZVariable().getMax().doubleValue();

  }

  public StackInterface acquire() {
    InterpolatedAcquisitionState lCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
    LightSheetTimelapse lTimelapse = mLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0);
    lCurrentState.getExposureInSecondsVariable().set(mExposureTimeInSeconds);
    lCurrentState.getStackZLowVariable().set(mMinZ);
    lCurrentState.getStackZHighVariable().set(mMaxZ);
    lCurrentState.getNumberOfZPlanesVariable().set((mMaxZ - mMinZ) / mSliceDistance + 1);
    lCurrentState.getImageWidthVariable().set(mImageWidth);
    lCurrentState.getImageHeightVariable().set(mImageHeight);


    LightSheetFastFusionProcessor
        lProcessor =
        mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
    lProcessor.initializeEngine();
    lProcessor.reInitializeEngine();
    lProcessor.getEngine().reset(true);


    AbstractAcquistionScheduler lAcquisitionScheduler;
    switch (mAcquisitionType) {
    case TimelapseSequential:
      lAcquisitionScheduler = mLightSheetMicroscope.getDevice(SequentialAcquisitionScheduler.class, 0);
      break;
    case TimeLapseOpticallyCameraFused:
      lAcquisitionScheduler = mLightSheetMicroscope.getDevice(OpticsPrefusedAcquisitionScheduler.class, 0);
      break;
    case TimeLapseInterleaved:
    default:
      lAcquisitionScheduler = mLightSheetMicroscope.getDevice(InterleavedAcquisitionScheduler.class, 0);
      break;
    }

    lAcquisitionScheduler.setMicroscope(mLightSheetMicroscope);
    lAcquisitionScheduler.initialize();
    lAcquisitionScheduler.enqueue(0);

    StackInterface lStack = lAcquisitionScheduler.getLastAcquiredStack();
    return lStack;


  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  public void setAcquisitionType(AcquisitionType pAcquisitionType)
  {
    if (pAcquisitionType == AcquisitionType.Interactive || pAcquisitionType == AcquisitionType.TimeLapse) {
      warning("Acquistion type " + pAcquisitionType + " is not supported!");
      return;
    }
    this.mAcquisitionType = pAcquisitionType;
  }

  public void setMinZ(double pMinZ)
  {
    this.mMinZ = pMinZ;
  }

  public void setMaxZ(double pMaxZ)
  {
    this.mMaxZ = pMaxZ;
  }

  public void setSliceDistance(double pSliceDistance)
  {
    this.mSliceDistance = pSliceDistance;
  }

  public void setExposureTimeInSeconds(double pExposureTimeInSeconds)
  {
    this.mExposureTimeInSeconds = pExposureTimeInSeconds;
  }


  public void setImageHeight(int pImageHeight)
  {
    this.mImageHeight = pImageHeight;
  }

  public void setImageWidth(int pImageWidth) {
    this.mImageWidth = pImageWidth;
  }
}
