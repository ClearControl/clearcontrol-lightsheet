package clearcontrol.microscope.lightsheet.imaging.exposuremodulation;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialAcquisitionScheduler;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataOrdinals;

/**
 * This instructions acquires a single image stack for a defined camera
 * and light sheet. It acquires every slice twice with two different
 * exposure times.
 *
 * The image stacks are stored in the DataWarehouse in
 * an StackInterfaceContainer with a key like:
 *
 * C0L0
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class ExposureModulatedAcquisitionScheduler extends
                                            SequentialAcquisitionScheduler implements
                                                                                SchedulerInterface,
                                                                                LoggingFeature
{
  int mCameraIndex;
  int mLightSheetIndex;

  private BoundedVariable<Double> mLongExposureTimeInSecondsVariable = new BoundedVariable<Double>("long exposure", 0.1, 0.0, Double.MAX_VALUE, 0.0001);
  private BoundedVariable<Double> mShortExposureTimeInSecondsVariable = new BoundedVariable<Double>("short exposure", 0.01, 0.0, Double.MAX_VALUE, 0.0001);


  public ExposureModulatedAcquisitionScheduler(int pCameraIndex, int pLightSheetIndex) {
    super("Acquisition: Exposure modulated C" + pCameraIndex + "L" + pLightSheetIndex);
    mCameraIndex = pCameraIndex;
    mLightSheetIndex = pLightSheetIndex;

    mImageKeyToSave = "C" + pCameraIndex + "L" + pLightSheetIndex + "exposuremodulated";
    mChannelName = mImageKeyToSave;
  }


  protected boolean isLightSheetOn(int pLightIndex) {
    return mLightSheetIndex == pLightIndex;
  }

  protected boolean isCameraOn(int pCameraIndex) {
    return mCameraIndex == pCameraIndex;
  }

  protected boolean isFused() {
    return true;
  }

  public int getLightSheetIndex() {
    return mLightSheetIndex;
  }

  public int getCameraIndex() {
    return mCameraIndex;
  }

  @Override
  public StackInterface getLastAcquiredStack()
  {
    return mLastAcquiredStack;
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

      lQueue.setExp(mShortExposureTimeInSecondsVariable.get());
      lQueue.addCurrentStateToQueue();
      lQueue.setExp(mLongExposureTimeInSecondsVariable.get());
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

/*
    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " W: " + lQueue.getIW(l));
    }
    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " H: " + lQueue.getIH(l));
    }
*/
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
            mTimelapse.getTimePointCounterVariable().get());


    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);
    lQueue.finalizeQueue();

    return lQueue;
  }

  public BoundedVariable<Double> getLongExposureTimeInSecondsVariable() {
    return mLongExposureTimeInSecondsVariable;
  }

  public BoundedVariable<Double> getShortExposureTimeInSecondsVariable() {
    return mShortExposureTimeInSecondsVariable;
  }
}
