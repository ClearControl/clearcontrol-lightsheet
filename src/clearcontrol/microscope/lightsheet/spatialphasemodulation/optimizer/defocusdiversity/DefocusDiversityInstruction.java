package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.defocusdiversity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionInstruction;
import clearcontrol.microscope.lightsheet.imaging.SingleStackImager;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataOrdinals;

/**
 * Deprecated: This class might be removed in the future as it contains similar
 * functionality as in
 * clearcontrol.microscope.lightsheet.adaptive.instructions.FocusFinderZInstruction
 */
@Deprecated
public class DefocusDiversityInstruction extends
                                         AbstractAcquistionInstruction
{

  private BoundedVariable<Double> mStepSize =
                                            new BoundedVariable<Double>("Defocus step size",
                                                                        5.0,
                                                                        0.0,
                                                                        Double.MAX_VALUE,
                                                                        0.0000000001);
  private BoundedVariable<Integer> mLightsheetIndex;
  private BoundedVariable<Integer> mDetectionArmIndex;

  private LightSheetMicroscope mLightSheetMicroscope;
  private StackInterface mResultImage;

  public DefocusDiversityInstruction(LightSheetMicroscope pLightSheetMicroscope,
                                     double pStepSize,
                                     int pLightSheetIndex,
                                     int pDetectionArmIndex)
  {
    super("Adaptive optics: Defocus Diversity (C" + pDetectionArmIndex
          + "L"
          + pLightSheetIndex
          + ")",
          pLightSheetMicroscope);
    mStepSize.set(pStepSize);
    mLightSheetMicroscope = pLightSheetMicroscope;
    mLightsheetIndex =
                     new BoundedVariable<Integer>("Light sheet index",
                                                  pLightSheetIndex,
                                                  0,
                                                  pLightSheetMicroscope.getNumberOfLightSheets());
    mDetectionArmIndex =
                       new BoundedVariable<Integer>("Detection arm index",
                                                    pDetectionArmIndex,
                                                    0,
                                                    pLightSheetMicroscope.getNumberOfDetectionArms());
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    imager();
    return false;
  }

  @Deprecated // use imager
  public boolean image()
  {

    LightSheetMicroscopeQueue lQueue =
                                     mLightSheetMicroscope.requestQueue();
    lQueue.clearQueue();
    int pImageWidth = mLightSheetMicroscope.getCameraWidth(0);
    int pImageHeight = mLightSheetMicroscope.getCameraHeight(0);

    lQueue.setFullROI();
    lQueue.setCenteredROI(pImageWidth, pImageHeight);

    lQueue.setExp(mLightSheetMicroscope.getExposure(0));

    // reset everything
    for (int i =
               0; i < mLightSheetMicroscope.getNumberOfLightSheets(); i++)
    {
      lQueue.setI(i, false);
    }

    lQueue.setI(0, true);
    lQueue.setIZ(0, 50.0);
    // lQueue.setIH(0,0);
    lQueue.setDZ(0, 50 - mStepSize.get());

    lQueue.setC(0, true);

    lQueue.addCurrentStateToQueue();
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                            mLightSheetMicroscope.getTimelapse()
                                                 .getTimePointCounterVariable()
                                                 .get());

    lQueue.setDZ(0, 50);
    lQueue.addCurrentStateToQueue();
    lQueue.setDZ(0, 50 + mStepSize.get());
    lQueue.addCurrentStateToQueue();

    lQueue.setTransitionTime(0.1);

    lQueue.finalizeQueue();

    info("Acquiring 3 images for defocus diversity");
    mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
    final Boolean lPlayQueueAndWait;
    try
    {
      lPlayQueueAndWait =
                        mLightSheetMicroscope.playQueueAndWaitForStacks(lQueue,
                                                                        100 + lQueue.getQueueLength(),
                                                                        TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (ExecutionException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
      return false;
    }

    if (!lPlayQueueAndWait)
    {
      return false;
    }
    mResultImage = mLightSheetMicroscope.getCameraStackVariable(0)
                                        .get();
    if (mResultImage == null)
    {
      System.out.println("Null Image in Stack");
      return false;
    }
    SequentialImageDataContainer lContainer =
                                            new SequentialImageDataContainer(mLightSheetMicroscope);

    putStackInContainer("C" + 0 + "L" + 0, mResultImage, lContainer);
    getLightSheetMicroscope().getDataWarehouse()
                             .put("sequential_raw_"
                                  + mLightSheetMicroscope.getTimelapse()
                                                         .getTimePointCounterVariable()
                                                         .get(),
                                  lContainer);

    return true;
  }

  public boolean imager()
  {

    InterpolatedAcquisitionState currentState =
                                              (InterpolatedAcquisitionState) mLightSheetMicroscope.getDevice(AcquisitionStateManager.class,
                                                                                                             0)
                                                                                                  .getCurrentState();
    double minZ = currentState.getStackZLowVariable()
                              .get()
                              .doubleValue();
    double maxZ = currentState.getStackZHighVariable()
                              .get()
                              .doubleValue();

    // double maxZ = currentState.getStackZHighVariable().get().doubleValue();
    double stepZ = mStepSize.get().doubleValue();
    int lNumberOfImages = (int) ((maxZ - minZ) / stepZ);
    double illZ = ((maxZ - minZ) / 2) + minZ;

    SingleStackImager lImager =
                              new SingleStackImager(mLightSheetMicroscope);

    lImager.getLightSheetMicroscope()
           .getLightSheet(0)
           .getZVariable()
           .doNotSyncWith(lImager.getLightSheetMicroscope()
                                 .getDetectionArm(0)
                                 .getZVariable());
    lImager.setDetectionArmIndex(mDetectionArmIndex.get());
    lImager.setLightSheetIndex(mLightsheetIndex.get());
    lImager.setIlluminationZ(illZ);
    lImager.setDetectionZ(illZ - ((lNumberOfImages / 2) * stepZ));
    lImager.setDetectionZStepDistance(stepZ);
    lImager.setIlluminationZStepDistance(0);
    lImager.setImageWidth(currentState.getImageWidthVariable()
                                      .get()
                                      .intValue());
    lImager.setImageHeight(currentState.getImageHeightVariable()
                                       .get()
                                       .intValue());
    lImager.setExposureTimeInSeconds(currentState.getExposureInSecondsVariable()
                                                 .get()
                                                 .doubleValue());
    lImager.setNumberOfRequestedImages((lNumberOfImages + 1));
    mResultImage = lImager.acquire();

    if (mResultImage == null)
    {
      System.out.println("Null Image in Stack");
      return false;
    }
    SequentialImageDataContainer lContainer =
                                            new SequentialImageDataContainer(mLightSheetMicroscope);

    putStackInContainer("C" + mDetectionArmIndex.get()
                        + "L"
                        + mLightsheetIndex.get(),
                        mResultImage,
                        lContainer);
    getLightSheetMicroscope().getDataWarehouse()
                             .put("sequential_raw_"
                                  + mLightSheetMicroscope.getTimelapse()
                                                         .getTimePointCounterVariable()
                                                         .get(),
                                  lContainer);

    return true;
  }

  public BoundedVariable<Double> getStepSize()
  {
    return mStepSize;
  }

  public BoundedVariable<Integer> getDetectionArmIndex()
  {
    return mDetectionArmIndex;
  }

  public BoundedVariable<Integer> getLightsheetIndex()
  {
    return mLightsheetIndex;
  }

  @Override
  public DefocusDiversityInstruction copy()
  {
    return new DefocusDiversityInstruction(getLightSheetMicroscope(),
                                           mStepSize.get(),
                                           mLightsheetIndex.get(),
                                           mDetectionArmIndex.get());
  }

}
