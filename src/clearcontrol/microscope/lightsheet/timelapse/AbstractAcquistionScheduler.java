package clearcontrol.microscope.lightsheet.timelapse;

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

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public AbstractAcquistionScheduler(String pDeviceName)
  {
    super(pDeviceName);
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

  protected void initializeStackSaving(FileStackSinkInterface pFileStackSinkInterface, String pChannel) {

    LightSheetFastFusionProcessor
        lProcessor =
        mLightSheetMicroscope.getDevice(
            LightSheetFastFusionProcessor.class,
            0);

    RecyclerInterface<StackInterface, StackRequest>
        lRecyclerOfProcessor =
        mLightSheetMicroscope.getStackProcesssingPipeline()
                             .getRecyclerOfProcessor(
                                 lProcessor);

    if (lProcessor != null) {
      lProcessor.reInitializeEngine();
      lProcessor.getEngine().addTask(new SaveImageStackTask("fused", "fused-saved", pFileStackSinkInterface, lRecyclerOfProcessor, pChannel));
      lProcessor.getEngine().addTask(new ResetFastFusionEngineTask("fused-saved"));
    }
  }

  protected void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope,
                                   LightSheetMicroscopeQueue lQueue,
                                   double lLightsheetWidth,
                                   double lLightsheetHeight,
                                   double lLightsheetX,
                                   double lLightsheetY,
                                   double lIlluminationZStart,
                                   double lDetectionZZStart)
  {

    for (int l = 0; l
                    < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
    {
      lQueue.setI(l, false);
      lQueue.setIW(l, lLightsheetWidth);
      lQueue.setIH(l, lLightsheetHeight);
      lQueue.setIX(l, lLightsheetX);
      lQueue.setIY(l, lLightsheetY);

      lQueue.setIZ(lIlluminationZStart);
    }
    for (int d = 0; d
                    < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      lQueue.setDZ(d, lDetectionZZStart);
      lQueue.setC(d, false);

    } lQueue.addCurrentStateToQueue();
  }

  protected void handleImageFromCameras(long pTimepoint) {

    final Object lLock = new Object();

    for (int c = 0; c < mLightSheetMicroscope.getNumberOfDetectionArms(); c ++ )
    {
      final int lFinalCameraIndex = c;
      new Runnable()
      {
        @Override public void run()
        {
          synchronized (lLock)
          {
            StackInterface
                lResultingStack =
                mLightSheetMicroscope.getCameraStackVariable(lFinalCameraIndex).get();

            LightSheetFastFusionProcessor
                lProcessor =
                mLightSheetMicroscope.getDevice(
                    LightSheetFastFusionProcessor.class,
                    0);

            RecyclerInterface<StackInterface, StackRequest>
                lRecyclerOfProcessor =
                mLightSheetMicroscope.getStackProcesssingPipeline()
                                     .getRecyclerOfProcessor(
                                         lProcessor);

            lProcessor.process(lResultingStack, lRecyclerOfProcessor);
          }
        }
      }.run();
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
}
