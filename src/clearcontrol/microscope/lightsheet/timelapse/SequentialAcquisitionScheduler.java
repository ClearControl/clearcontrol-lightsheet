package clearcontrol.microscope.lightsheet.timelapse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;
import coremem.recycling.RecyclerInterface;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class SequentialAcquisitionScheduler extends AbstractAcquistionScheduler implements
                                                                  SchedulerInterface,
                                                                  LoggingFeature
{


  /**
   * INstanciates a virtual device with a given name
   *
   */
  public SequentialAcquisitionScheduler(RecyclerInterface<StackInterface, StackRequest> pRecycler)
  {
    super("Acquisition: Sequential", pRecycler);
    mChannelName = "sequential";
  }

  public SequentialAcquisitionScheduler(String pName, RecyclerInterface<StackInterface, StackRequest> pRecycler) {
    super(pName, pRecycler);
  }


  @Override public boolean enqueue(long pTimePoint)
  {
    boolean lFastFusionEngineInitialized = false;

    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("" + this + " needs a lightsheet microscope!");
      return false;
    }

    int lNumberOfDetectionArms = mLightSheetMicroscope.getNumberOfDetectionArms();

    int lNumberOfLightSheets = mLightSheetMicroscope.getNumberOfLightSheets();

    HashMap<Integer, LightSheetMicroscopeQueue> lViewToQueueMap = new HashMap<>();

    // preparing queues:
    for (int l = 0; l < lNumberOfLightSheets; l++)
      if (isLightSheetOn(l))
      {
        LightSheetMicroscopeQueue
            lQueueForView =
            getQueueForSingleLightSheet(mCurrentState, l);

        lViewToQueueMap.put(l, lQueueForView);
      }

    // playing the queues in sequence:

    for (int l = 0; l < lNumberOfLightSheets; l++) {
      if (isLightSheetOn(l))
      {
        LightSheetMicroscopeQueue lQueueForView = lViewToQueueMap.get(l);

        for (int c = 0; c < lNumberOfDetectionArms; c++)
          if (isCameraOn(c))
          {

            StackMetaData
                lMetaData =
                lQueueForView.getCameraDeviceQueue(c)
                             .getMetaDataVariable()
                             .get();

            lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                               AcquisitionType.TimelapseSequential);
            lMetaData.addEntry(MetaDataView.Camera, c);
            lMetaData.addEntry(MetaDataView.LightSheet, l);

            if (isFused())
            {
              lMetaData.addEntry(MetaDataFusion.RequestFullFusion,
                                   true);

              lMetaData.addEntry(MetaDataChannel.Channel,  "sequential");
            }
            else
            {
              String lCxLyString = MetaDataView.getCxLyString(lMetaData);
              lMetaData.addEntry(MetaDataChannel.Channel,
                                 lCxLyString);
            }
          }




        try
        {
          mLightSheetMicroscope.playQueueAndWait(lQueueForView,
                                                 mTimelapse.getTimeOut(),
                                                 TimeUnit.SECONDS);

          if (l == lNumberOfLightSheets - 1 || this instanceof SingleViewAcquisitionScheduler) // dirty workaround
          {
            initializeStackSaving(mTimelapse.getCurrentFileStackSinkVariable()
                                            .get());
          }
          handleImageFromCameras(pTimePoint);
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

      }
    }



    return true;
  }

  protected LightSheetMicroscopeQueue getQueueForSingleLightSheet(LightSheetAcquisitionStateInterface<?> pCurrentState,
                                                                  int pLightSheetIndex)
  {
    int lNumberOfDetectionArms =
        mLightSheetMicroscope.getNumberOfDetectionArms();

    @SuppressWarnings("unused")
    int lNumberOfLightSheets =
        mLightSheetMicroscope.getNumberOfLightSheets();

    int lNumberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

    LightSheetMicroscopeQueue lQueue = mLightSheetMicroscope.requestQueue();
    lQueue.clearQueue();


    int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
    int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
    double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

    lQueue.setCenteredROI(lImageWidth, lImageHeight);
    lQueue.setExp(lExposureTimeInSeconds);



    // initial position
    goToInitialPosition(mLightSheetMicroscope,
            lQueue,
            mCurrentState.getStackZLowVariable().get().doubleValue(),
            mCurrentState.getStackZLowVariable().get().doubleValue());


    for (int lImageCounter = 0; lImageCounter
            < lNumberOfImagesToTake; lImageCounter++)
    {
      mCurrentState.applyAcquisitionStateAtStackPlane(lQueue,
              lImageCounter);
      for (int k = 0; k
              < mLightSheetMicroscope.getNumberOfLightSheets(); k++)
      {

        lQueue.setI(k, pLightSheetIndex == k);
      }

      lQueue.addCurrentStateToQueue();
    }
/*
        pCurrentState.getQueue(0,
                               lNumberOfDetectionArms,
                               pLightSheetIndex,
                               pLightSheetIndex + 1,
                               0,
                               lNumberOfLaserLines,
                               lNumberOfEDFSlices);
*/

    // initial position
    goToInitialPosition(mLightSheetMicroscope,
            lQueue,
            mCurrentState.getStackZLowVariable().get().doubleValue(),
            mCurrentState.getStackZLowVariable().get().doubleValue());


    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " W: " + lQueue.getIW(l));
    }
    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " H: " + lQueue.getIH(l));
    }

    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                            mTimelapse.getTimePointCounterVariable().get());


    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);
    lQueue.finalizeQueue();

    return lQueue;
  }

  protected boolean isLightSheetOn(int pLightIndex) {
    return mCurrentState.getLightSheetOnOffVariable(pLightIndex).get();
  }


}
