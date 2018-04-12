package clearcontrol.microscope.lightsheet.timelapse;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.processor.fastfusiontasks.ResetFastFusionEngineTask;
import clearcontrol.microscope.lightsheet.processor.fastfusiontasks.SaveImageStackTask;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;
import coremem.recycling.RecyclerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public abstract class AbstractAcquistionScheduler extends SchedulerBase implements
                                                                        SchedulerInterface,
                                                                        LoggingFeature
{

  protected String mImageKeyToSave = "fused";
  protected String mChannelName = "default";

  protected StackInterface mLastAcquiredStack;
  RecyclerInterface<StackInterface, StackRequest> mRecycler;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public AbstractAcquistionScheduler(String pDeviceName, RecyclerInterface<StackInterface, StackRequest> pRecycler)
  {
    super(pDeviceName);
    mRecycler = pRecycler;
  }

  protected LightSheetMicroscope mLightSheetMicroscope;
  protected InterpolatedAcquisitionState mCurrentState;
  protected LightSheetTimelapse mTimelapse;

  @Override public boolean initialize()
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("" + this + " needs a lightsheet microscope!");
      return false;
    }

    mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
    mCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
    mTimelapse = mLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0);

    LightSheetFastFusionProcessor
        lProcessor =
        mLightSheetMicroscope.getDevice(
            LightSheetFastFusionProcessor.class,
            0);
    if (lProcessor != null) {
      lProcessor.initializeEngine();
    }

    return true;
  }

  protected void initializeStackSaving(FileStackSinkInterface pFileStackSinkInterface) {

    LightSheetFastFusionProcessor
        lProcessor =
        mLightSheetMicroscope.getDevice(
            LightSheetFastFusionProcessor.class,
            0);


    if (lProcessor != null)
    {
      lProcessor.reInitializeEngine();
      if (pFileStackSinkInterface != null)
      {
        // The save tasks exists twice in the compute graph. That should be improved in the future.
        // At the moment it only works because C101 is either saved (single acquisition) and
        // afterwards, the second task does not see it anymore because its memory was freed

        lProcessor.getEngine()
                  .addTask(new SaveImageStackTask(mImageKeyToSave,
                                                  mImageKeyToSave + "-saved",
                                                  pFileStackSinkInterface,
                                                  mRecycler,
                                                  mChannelName), true);
        lProcessor.getEngine()
                  .addTask(new SaveImageStackTask(mImageKeyToSave,
                                                  mImageKeyToSave + "-saved",
                                                  pFileStackSinkInterface,
                                                  mRecycler,
                                                  mChannelName), false);
        lProcessor.getEngine().addTask(new ResetFastFusionEngineTask(mImageKeyToSave + "-saved"));
      }
    }
  }

  protected void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope,
                                   LightSheetMicroscopeQueue lQueue,
                                   double lIlluminationZStart,
                                   double lDetectionZZStart)
  {
    double widthBefore = lQueue.getIW(0);

    ((InterpolatedAcquisitionState)lLightsheetMicroscope.getAcquisitionStateManager().getCurrentState()).applyAcquisitionStateAtZ(lQueue, lIlluminationZStart);
    for (int l = 0; l
                    < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
    {
      lQueue.setI(l, false);
      lQueue.setIZ(lIlluminationZStart);
    }
    for (int d = 0; d
                    < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      lQueue.setDZ(d, lDetectionZZStart);
      lQueue.setC(d, false);

    }
    double widthAfter = lQueue.getIW(0);

    if (Math.abs(widthAfter - widthBefore) > 0.1)
    {
      // if the width of the light sheets changed significantly, we
      // need to wait a second until the iris has been moved...
      lQueue.setExp(0.5);
    }
    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();
    lQueue.addVoxelDimMetaData(lLightsheetMicroscope, mCurrentState.getStackZStepVariable().get().doubleValue());
  }

  protected void handleImageFromCameras(long pTimepoint) {
    System.out.print("handleImageFromCameras " +pTimepoint );
    final Object lLock = new Object();

    for (int c = 0; c < mLightSheetMicroscope.getNumberOfDetectionArms(); c ++ ) {
      if (isCameraOn(c)) {
        final int lFinalCameraIndex = c;
        ElapsedTime.measure("Handle camera output (of camera " + c + ") and fuse", () ->
        {
          synchronized (lLock) {
            StackInterface
                    lResultingStack =
                    mLightSheetMicroscope.getCameraStackVariable(lFinalCameraIndex).get();

            LightSheetFastFusionProcessor
                    lProcessor =
                    mLightSheetMicroscope.getDevice(
                            LightSheetFastFusionProcessor.class,
                            0);

            RecyclerInterface<StackInterface, StackRequest>
                    lRecyclerOfProcessor = mRecycler;
//              mLightSheetMicroscope.getStackProcesssingPipeline()
            //                                 .getRecyclerOfProcessor(
            //                                   lProcessor);

            info("sending: " + lResultingStack);
            StackInterface lStackInterface = lProcessor.process(lResultingStack, lRecyclerOfProcessor);
            info("Got back: " + lStackInterface);
            if (lStackInterface != null) {
              mLastAcquiredStack = lStackInterface;
            }
          }
        });
      }
    }
  }

  /*
  Pair<AbstractAcquistionScheduler, Long> mLock = null;
  protected synchronized  void sendToPostProcessingAndSaving(StackInterface pStackInterface, long pTimepoint) {
    LightSheetFastFusionProcessor lProcessor = mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);

    Pair<AbstractAcquistionScheduler, Long> lPotentialNewLock = new Pair<>(this, pTimepoint);
    if (mLock != null && lPotentialNewLock.getKey() == mLock.getKey() && lPotentialNewLock.getValue() == mLock.getValue()) {
      lPotentialNewLock = mLock;
    }

    info("Trying to lock " + lPotentialNewLock);
    try
    {
      if (lProcessor.getMutex().lock(lPotentialNewLock)) {
        mLock = lPotentialNewLock;
        info("Got the lock " + lPotentialNewLock);

        // actually do the postprocessing
        RecyclerInterface<StackInterface, StackRequest>
            lRecyclerOfProcessor =
            mLightSheetMicroscope.getStackProcesssingPipeline()
                                 .getRecyclerOfProcessor(lProcessor);

        // Variable<StackInterface> lPipelineStackVariable = mMicroscope.getPipelineStackVariable();
        //lPipelineStackVariable.get().get

        StackInterface lResultStack = lProcessor.process(pStackInterface, lRecyclerOfProcessor);
        if (lResultStack != null) {
          lProcessor.getMutex().unlock(mLock);
          info("Unlocked " + lPotentialNewLock);
          sendToStackSaving(lResultStack);
        } else
        {
          info("Don't unlock " + lPotentialNewLock);
        }

      }
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }*/
/*
  protected synchronized void sendToStackSaving(StackInterface pStackInterface) {

    Variable<FileStackSinkInterface> lStackSinkVariable = mTimelapse.getCurrentFileStackSinkVariable();

    info("Appending new stack %s to the file sink %s",
         pStackInterface,
         lStackSinkVariable);

    String lChannelInMetaData =
        pStackInterface.getMetaData()
         .getValue(MetaDataChannel.Channel);

    final String lChannel =
        lChannelInMetaData != null ? lChannelInMetaData
                                   : StackSinkSourceInterface.cDefaultChannel;

    ElapsedTime.measureForceOutput("TimeLapse stack saving",
                                   () -> lStackSinkVariable.get()
                                                           .appendStack(lChannel,
                                                                        pStackInterface));


  }
*/
  public StackInterface getLastAcquiredStack()
  {
    return mLastAcquiredStack;
  }


  protected boolean isCameraOn(int pCameraIndex) {
    return mCurrentState.getCameraOnOffVariable(pCameraIndex).get();
  }

  protected boolean isFused() {
    return true;
  }
}
