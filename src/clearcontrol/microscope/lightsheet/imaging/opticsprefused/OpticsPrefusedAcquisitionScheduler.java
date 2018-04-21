package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionScheduler;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;
import coremem.recycling.RecyclerInterface;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class OpticsPrefusedAcquisitionScheduler extends
                                                AbstractAcquistionScheduler implements
                                                                                       SchedulerInterface,
                                                                                       LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public OpticsPrefusedAcquisitionScheduler()
  {
    super("Acquisition: optics-prefused (heap++)");
    mChannelName = "opticsprefused";
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("" + this + " needs a lightsheet microscope!");
      return false;
    }

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
      // acuqire an image with all light sheets on
      mCurrentState.applyAcquisitionStateAtStackPlane(lQueue,
                                                      lImageCounter);

      for (int k = 0; k
                      < lLightsheetMicroscope.getNumberOfLightSheets(); k++)
      {

        lQueue.setI(k, true);
      }
      lQueue.addCurrentStateToQueue();

    }

    // back to initial position
    goToInitialPosition(lLightsheetMicroscope,
                        lQueue,
                        mCurrentState.getStackZLowVariable().get().doubleValue(),
                        mCurrentState.getStackZLowVariable().get().doubleValue());

    //lQueue.setTransitionTime(0.1);
    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);

    for (int c = 0; c < lLightsheetMicroscope.getNumberOfDetectionArms(); c++)
    {
      StackMetaData
          lMetaData =
          lQueue.getCameraDeviceQueue(c).getMetaDataVariable().get();

      lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                         AcquisitionType.TimeLapseOpticallyCameraFused);
      lMetaData.addEntry(MetaDataView.Camera, c);

      lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

      lMetaData.addEntry(MetaDataChannel.Channel, "opticsprefused");
    }
    lQueue.addVoxelDimMetaData(lLightsheetMicroscope, mCurrentState.getStackZStepVariable().get().doubleValue());
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                            pTimePoint);

    lQueue.finalizeQueue();

    // acquire!
    boolean lPlayQueueAndWait = false;
    try
    {
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

    OpticsPrefusedImageDataContainer
        lContainer = new OpticsPrefusedImageDataContainer(mLightSheetMicroscope);
    for (int d = 0 ; d < mLightSheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      /*lContainer.put("C" + d + "opticsprefused",
                     mLightSheetMicroscope.getCameraStackVariable(d).get());*/
      StackInterface lStack = mLightSheetMicroscope.getCameraStackVariable(
          d).get();

      putStackInContainer("C" + d + "opticsprefused", lStack, lContainer);

    }


    mLightSheetMicroscope.getDataWarehouse().put("opticsprefused_raw_" + pTimePoint, lContainer);

    //initializeStackSaving(mTimelapse.getCurrentFileStackSinkVariable().get());
    //handleImageFromCameras(pTimePoint);

    return true;
  }
}
