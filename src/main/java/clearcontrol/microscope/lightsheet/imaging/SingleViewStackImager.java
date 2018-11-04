package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.singleview.SingleViewAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import coremem.ContiguousMemoryInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class SingleViewStackImager implements
        LightSheetImagerInterface,
                                   LoggingFeature
{
  private LightSheetMicroscope mLightSheetMicroscope;

  private AcquisitionType mAcquisitionType =
                                           AcquisitionType.TimeLapseInterleaved;
  protected double mMinZ = 0;
  protected double mMaxZ = 0;
  private double mSliceDistance = 2.0;
  private double mExposureTimeInSeconds = 0.01;
  private long mImageHeight;
  private long mImageWidth;

  private int mLightSheetIndex = 0;
  private int mDetectionArmIndex = 0;

  public SingleViewStackImager(LightSheetMicroscope pLightSheetMicroscope)
  {
    mLightSheetMicroscope = pLightSheetMicroscope;
    mMinZ =
          pLightSheetMicroscope.getDetectionArm(0)
                               .getZVariable()
                               .getMin()
                               .doubleValue();
    mMaxZ =
          pLightSheetMicroscope.getDetectionArm(0)
                               .getZVariable()
                               .getMax()
                               .doubleValue();

  }


  private ContiguousMemoryInterface memoryInterface;
  public void setMemoryInterface(ContiguousMemoryInterface memoryInterface) {
    this.memoryInterface = memoryInterface;
  }

  public boolean acquire() {
    StackInterface stackInterface = acquireStack();
    memoryInterface.copyFrom(stackInterface.getContiguousMemory());
    stackInterface.release();

    return true;
  }

  /**
   * Deprecated: use setMemoryInterface() and acquire() instead
   * @return
   */
  @Deprecated
  public StackInterface acquireStack()
  {

    InterpolatedAcquisitionState lCurrentState =
                                               (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager()
                                                                                                   .getCurrentState();
    lCurrentState.getExposureInSecondsVariable()
                 .set(mExposureTimeInSeconds);
    lCurrentState.getStackZLowVariable().set(mMinZ);
    lCurrentState.getStackZHighVariable().set(mMaxZ);
    lCurrentState.getNumberOfZPlanesVariable()
                 .set((mMaxZ - mMinZ) / mSliceDistance + 1);
    lCurrentState.getImageWidthVariable().set(mImageWidth);
    lCurrentState.getImageHeightVariable().set(mImageHeight);

    LightSheetFastFusionProcessor lProcessor =
                                             mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class,
                                                                             0);
    lProcessor.initializeEngine();
    lProcessor.reInitializeEngine();
    lProcessor.getEngine().reset(true);

    AbstractAcquistionInstruction lAcquisitionScheduler = null;
    for (SingleViewAcquisitionInstruction lScheduler : mLightSheetMicroscope.getDevices(SingleViewAcquisitionInstruction.class))
    {
      if (lScheduler.getCameraIndex() == mDetectionArmIndex
          && lScheduler.getLightSheetIndex() == mLightSheetIndex)
      {
        lAcquisitionScheduler = lScheduler;
      }
    }
    if (lAcquisitionScheduler == null)
    {
      warning("No imaging instructions found for L" + mLightSheetIndex
              + "C"
              + mDetectionArmIndex);
      return null;
    }

    lAcquisitionScheduler.initialize();
    lAcquisitionScheduler.enqueue(0);

    StackInterface lStack = // ((StackInterfaceContainer)mLightSheetMicroscope.getDataWarehouse().getOldestContainer(StackInterfaceContainer.class)).get("C"
                            // + mDetectionArmIndex + "L" + mLightSheetIndex);
                          lAcquisitionScheduler.getLastAcquiredStack();
    return lStack;
  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
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

  public void setImageHeight(long pImageHeight)
  {
    this.mImageHeight = pImageHeight;
  }

  public void setImageWidth(long pImageWidth)
  {
    this.mImageWidth = pImageWidth;
  }

  public void setLightSheetIndex(int mLightSheetIndex)
  {
    this.mLightSheetIndex = mLightSheetIndex;
  }

  public void setDetectionArmIndex(int mDetectionArmIndex)
  {
    this.mDetectionArmIndex = mDetectionArmIndex;
  }


  public boolean connect() {
    return true;
  }

  public boolean disconnect() {
    return true;
  }

  public void setBinning(int binning) {

  }

  public double getPixelSizeInMicrons() {
    return getLightSheetMicroscope().getDetectionArm(0).getPixelSizeInMicrometerVariable().get();
  }
}
