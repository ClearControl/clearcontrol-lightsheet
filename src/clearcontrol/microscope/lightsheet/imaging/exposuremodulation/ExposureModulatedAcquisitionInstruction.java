package clearcontrol.microscope.lightsheet.imaging.exposuremodulation;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialAcquisitionInstruction;
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
public class ExposureModulatedAcquisitionInstruction extends
        SequentialAcquisitionInstruction implements
        InstructionInterface,
                                                                                LoggingFeature
{
  int mCameraIndex;
  int mLightSheetIndex;

  private BoundedVariable<Double> mLongExposureTimeInSecondsVariable = new BoundedVariable<Double>("long exposure", 0.1, 0.0, Double.MAX_VALUE, 0.0001);
  private BoundedVariable<Double> mShortExposureTimeInSecondsVariable = new BoundedVariable<Double>("short exposure", 0.01, 0.0, Double.MAX_VALUE, 0.0001);
  private BoundedVariable<Integer> mNumberOfRepeats = new BoundedVariable<Integer>("repeats", 1, 1, Integer.MAX_VALUE);


  public ExposureModulatedAcquisitionInstruction(int pCameraIndex, int pLightSheetIndex, LightSheetMicroscope pLightSheetMicroscope) {
    super("Acquisition: Exposure modulated C" + pCameraIndex + "L" + pLightSheetIndex, pLightSheetMicroscope);
    mCameraIndex = pCameraIndex;
    mLightSheetIndex = pLightSheetIndex;

    mImageKeyToSave = "C" + pCameraIndex + "L" + pLightSheetIndex + "exposuremodulated";
    mChannelName.set(mImageKeyToSave);
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
            getLightSheetMicroscope().getNumberOfDetectionArms();

    @SuppressWarnings("unused")
    int lNumberOfLightSheets =
            getLightSheetMicroscope().getNumberOfLightSheets();

    int lNumberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

    LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();
    lQueue.clearQueue();


    int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
    int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
    double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

    lQueue.setCenteredROI(lImageWidth, lImageHeight);
    lQueue.setExp(lExposureTimeInSeconds);



    // initial position
    goToInitialPosition(getLightSheetMicroscope(),
            lQueue,
            mCurrentState.getStackZLowVariable().get().doubleValue(),
            mCurrentState.getStackZLowVariable().get().doubleValue());


    for (int lImageCounter = 0; lImageCounter
            < lNumberOfImagesToTake; lImageCounter++)
    {
      mCurrentState.applyAcquisitionStateAtStackPlane(lQueue,
              lImageCounter);
      for (int k = 0; k
              < getLightSheetMicroscope().getNumberOfLightSheets(); k++)
      {

        lQueue.setI(k, pLightSheetIndex == k);
      }

      lQueue.setExp(mShortExposureTimeInSecondsVariable.get());
      for (int i = 0; i < mNumberOfRepeats.get(); i++) {
        lQueue.addCurrentStateToQueue();
      }
      lQueue.setExp(mLongExposureTimeInSecondsVariable.get());
      for (int i = 0; i < mNumberOfRepeats.get(); i++) {
        lQueue.addCurrentStateToQueue();
      }
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
    goToInitialPosition(getLightSheetMicroscope(),
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

  public BoundedVariable<Integer> getNumberOfRepeats() {
    return mNumberOfRepeats;
  }

  @Override
  public ExposureModulatedAcquisitionInstruction copy() {
    ExposureModulatedAcquisitionInstruction copied = new ExposureModulatedAcquisitionInstruction(mCameraIndex, mLightSheetIndex, getLightSheetMicroscope());
    copied.mLongExposureTimeInSecondsVariable.set(mLongExposureTimeInSecondsVariable.get());
    copied.mShortExposureTimeInSecondsVariable.set(mLongExposureTimeInSecondsVariable.get());
    return copied;
  }

}
