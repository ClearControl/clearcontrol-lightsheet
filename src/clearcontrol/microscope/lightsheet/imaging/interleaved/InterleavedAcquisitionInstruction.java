package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionInstruction;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * This instructions acquires an image stack per camera where every slice
 * is imaged several times for each light sheet. A stack might contain
 * slices like:
 *
 * C0L0Z0
 * C0L1Z0
 * C0L2Z0
 * C0L3Z0
 * C0L0Z1
 * C0L1Z1
 * C0L2Z1
 * C0L3Z1
 * ...
 *
 * The image stacks are stored in the DataWarehouse in a
 * InterleavedImageDataContainer with keys like CXinterleaved with X
 * representing the camera number.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class InterleavedAcquisitionInstruction extends
        AbstractAcquistionInstruction implements
                                                                   SchedulerInterface,
                                                                   LoggingFeature
{

  /**
   * INstanciates a virtual device with a given name
   */
  public InterleavedAcquisitionInstruction()
  {
    super("Acquisition: Interleaved");
    mChannelName = "interleaved";
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("" + this + " needs a lightsheet microscope!");
      return false;
    }
    mCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();

    int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
    int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
    double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

    int lNumberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

    LightSheetMicroscope
        lLightsheetMicroscope =
        (LightSheetMicroscope) mMicroscope;

    // build a queue
    LightSheetMicroscopeQueue
        lQueue =
        lLightsheetMicroscope.requestQueue();

    // initialize queue
    lQueue.clearQueue();
    lQueue.setCenteredROI(lImageWidth, lImageHeight);

    lQueue.setExp(lExposureTimeInSeconds);

    // initial position
    goToInitialPosition(lLightsheetMicroscope,
                        lQueue,
                        mCurrentState.getStackZLowVariable().get().doubleValue(),
                        mCurrentState.getStackZLowVariable().get().doubleValue());

    // --------------------------------------------------------------------
    // build a queue

    for (int lImageCounter = 0; lImageCounter
                                < lNumberOfImagesToTake; lImageCounter++)
    {
      // acuqire an image per light sheet + one more
      for (int l = 0; l
                      < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
      {
        // configure light sheets accordingly
        for (int k = 0; k
                        < lLightsheetMicroscope.getNumberOfLightSheets(); k++)
        {
          mCurrentState.applyAcquisitionStateAtStackPlane(lQueue,
                                                        lImageCounter);

          lQueue.setI(k, k == l);
        }
        lQueue.addCurrentStateToQueue();
      }
    }

    // back to initial position
    goToInitialPosition(lLightsheetMicroscope,
                        lQueue,
                        mCurrentState.getStackZLowVariable().get().doubleValue(),
                        mCurrentState.getStackZLowVariable().get().doubleValue());

    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);

    for (int c = 0; c < lLightsheetMicroscope.getNumberOfDetectionArms(); c++)
    {
      StackMetaData
          lMetaData =
          lQueue.getCameraDeviceQueue(c).getMetaDataVariable().get();

      lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                         AcquisitionType.TimeLapseInterleaved);
      lMetaData.addEntry(MetaDataView.Camera, c);

      lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

      lMetaData.addEntry(MetaDataChannel.Channel,  "interleaved");
    }
    lQueue.addVoxelDimMetaData(lLightsheetMicroscope, mCurrentState.getStackZStepVariable().get().doubleValue());
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                            pTimePoint);

    lQueue.finalizeQueue();

    // acquire!
    boolean lPlayQueueAndWait = false;
    try
    {
      mTimeStampBeforeImaging = System.nanoTime();
      lPlayQueueAndWait = lLightsheetMicroscope.playQueueAndWait(lQueue,
                                                      100 + lQueue
                                                          .getQueueLength(),
                                                                          TimeUnit.SECONDS);

    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    catch (ExecutionException e)
    {
      e.printStackTrace();
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
    }

    if (!lPlayQueueAndWait)
    {
      System.out.print("Error while imaging");
      return false;
    }

    // Store results in the DataWarehouse
    InterleavedImageDataContainer lContainer = new InterleavedImageDataContainer(mLightSheetMicroscope);
    for (int d = 0 ; d < mLightSheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      StackInterface lStack = mLightSheetMicroscope.getCameraStackVariable(
          d).get();

      putStackInContainer("C" + d + "interleaved", lStack, lContainer);
    }
    mLightSheetMicroscope.getDataWarehouse().put("interleaved_raw_" + pTimePoint, lContainer);

    return true;
  }

}
