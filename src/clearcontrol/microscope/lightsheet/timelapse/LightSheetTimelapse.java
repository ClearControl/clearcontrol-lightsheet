package clearcontrol.microscope.lightsheet.timelapse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import clearcontrol.core.concurrent.timing.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionEngine;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.lightsheet.timelapse.containers.SchedulerDurationContainer;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.microscope.timelapse.TimelapseBase;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;

/**
 * Standard Timelapse implementation
 *
 * @author royer
 */
public class LightSheetTimelapse extends TimelapseBase implements
                                 TimelapseInterface,
                                 LoggingFeature
{

  private static final long cTimeOut = 1000;
  private static final int cMinimumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfLiveStacks = 16;

  private final LightSheetMicroscope mLightSheetMicroscope;

  private final Variable<Boolean> mFuseStacksVariable =
                                                      new Variable<Boolean>("FuseStacks",
                                                                            true);

  private final Variable<Boolean> mFuseStacksPerCameraVariable =
                                                               new Variable<Boolean>("FuseStacksPerCamera",
                                                                                     false);

  private final Variable<Boolean> mInterleavedAcquisitionVariable =
                                                                  new Variable<Boolean>("InterleavedAcquisition",
                                                                                        false);

  private final Variable<Boolean> mExtendedDepthOfFieldAcquisitionVariable =
      new Variable<Boolean>("ExtendedDepthOfFieldAcquisition",
                            false);
  private Variable<Boolean> mLegacyTimelapseAcquisitionVariable  =
      new Variable<Boolean>("LegacyTimelapseAcquisition",
                            false);

  private ArrayList<SchedulerInterface>
      mListOfActivatedSchedulers = new ArrayList<SchedulerInterface>();

  private Variable<Integer> mLastExecutedSchedulerIndexVariable = new Variable<Integer>("Last executed instructions index", -1);

  ArrayList<SchedulerInterface>
          mInitializedSchedulerList;


  private BufferedWriter mLogFileWriter;

  /**
   * @param pLightSheetMicroscope
   *          microscope
   */
  public LightSheetTimelapse(LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pLightSheetMicroscope);
    mLightSheetMicroscope = pLightSheetMicroscope;

    this.getMaxNumberOfTimePointsVariable().set(999999L);


    mExtendedDepthOfFieldAcquisitionVariable.addSetListener(new VariableSetListener<Boolean>()
    {
      @Override public void setEvent(Boolean pCurrentValue,
                                     Boolean pNewValue)
      {
        if (pNewValue) {
          mFuseStacksVariable.set(false);
        }
      }
    });

    mFuseStacksVariable.addSetListener(new VariableSetListener<Boolean>()
    {
      @Override public void setEvent(Boolean pCurrentValue,
                                     Boolean pNewValue)
      {
        if (pNewValue) {
          mExtendedDepthOfFieldAcquisitionVariable.set(false);
        }
      }
    });

    /*
    boolean lFuseStacks = getFuseStacksVariable().get()
                          && n.getMetaData()
                              .hasValue(MetaDataFusion.Fused);
    
    
    /**/

  }

  @Override
  public void acquire()
  {
    if (getTimePointCounterVariable().get() == 0) {

      File lLogFile = new File(getWorkingDirectory(), "scheduleLog.txt");

      lLogFile.getParentFile().mkdir();

      mLightSheetMicroscope.getDataWarehouse().clear();

      try
      {
        if (mLogFileWriter != null) {
          mLogFileWriter.close();
          mLogFileWriter = null;
        }
        mLogFileWriter = new BufferedWriter(new FileWriter(lLogFile));
        mLogFileWriter.write("Max timepoints      " + getTimePointCounterVariable().get() + "\n");
        mLogFileWriter.write("Folder              " + getWorkingDirectory() + "\n");

        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
        mLogFileWriter.write("Min Z               " + lState.getStackZLowVariable().get() + "\n");
        mLogFileWriter.write("Max Z               " + lState.getStackZHighVariable().get() + "\n");
        mLogFileWriter.write("Slice distance      " + lState.getStackZStepVariable() + "\n");

        mLogFileWriter.write("Exposure time (s)   " + lState.getExposureInSecondsVariable().get() + "\n");
        mLogFileWriter.write("Image width         " + lState.getImageWidthVariable().get() + "\n");
        mLogFileWriter.write("Image height        " + lState.getImageHeightVariable().get() + "\n");
        mLogFileWriter.write("Slice distance      " + lState.getStackZStepVariable() + "\n");

        mLogFileWriter.write("DataWarehouse items " + mLightSheetMicroscope.getDataWarehouse().keySet().size() + "\n");
        mLogFileWriter.write("Schedule items      " + mListOfActivatedSchedulers.size() + "\n");

        mLogFileWriter.write(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())+ " (time point " + getTimePointCounterVariable().get() + ") " + "Starting log\r\n");
      }
      catch (IOException e)
      {
        e.printStackTrace();
        mLogFileWriter = null;
      }

      mInitializedSchedulerList = new ArrayList<SchedulerInterface>();


      LightSheetFastFusionProcessor lLightSheetFastFusionProcessor = mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
      LightSheetFastFusionEngine lLightSheetFastFusionEngine = lLightSheetFastFusionProcessor.getEngine();
      if (lLightSheetFastFusionEngine != null)
      {
        lLightSheetFastFusionEngine.reset(true);
      }
      mLastExecutedSchedulerIndexVariable.set(-1);
    }

    if (getStopSignalVariable().get()) {
      return;
    }

    try
    {
      LightSheetFastFusionProcessor lLightSheetFastFusionProcessor = mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
      LightSheetFastFusionEngine lLightSheetFastFusionEngine = lLightSheetFastFusionProcessor.getEngine();

      /*
      if (lLightSheetFastFusionEngine != null) {
        while(lLightSheetFastFusionEngine.getAvailableImagesSlotKeys().size() > 0) {
          if (mLogFileWriter != null) {
            mLogFileWriter.write(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())+ " (time point " + getTimePointCounterVariable().get() + ") " + "Waiting for fastfuse to finish....\r\n");
            if (lLightSheetFastFusionEngine != null)
            {
              mLogFileWriter.write("FastFuse knows about " + lLightSheetFastFusionEngine.getAvailableImagesSlotKeys() + "\r\n");
            }
            mLogFileWriter.flush();
          }


          info("Waiting because fastfuse is still working... " + lLightSheetFastFusionEngine.getAvailableImagesSlotKeys());
          try
          {
            Thread.sleep(1000);
            if (getStopSignalVariable().get()) {
              return;
            }
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }*/


      info("Executing timepoint: "
           + getTimePointCounterVariable().get() + " data warehouse holds " + mLightSheetMicroscope.getDataWarehouse().size() + " items");

      mLightSheetMicroscope.useRecycler("3DTimelapse",
                                        cMinimumNumberOfAvailableStacks,
                                        cMaximumNumberOfAvailableStacks,
                                        cMaximumNumberOfLiveStacks);

      @SuppressWarnings("unchecked")
      AcquisitionStateManager<LightSheetAcquisitionStateInterface<?>> lAcquisitionStateManager =
                                                                                               mLightSheetMicroscope.getDevice(AcquisitionStateManager.class,
                                                                                                                               0);

      LightSheetAcquisitionStateInterface<?> lCurrentState =
                                                           lAcquisitionStateManager.getCurrentState();

      // deprecated: this code block will be removed as soon as
      // timelapse became an own Scheduler
      if (getLegacyTimelapseAcquisitionVariable().get())
      {
        if (getInterleavedAcquisitionVariable().get())
          interleavedAcquisition(lCurrentState);
        else
          sequentialAcquisition(lCurrentState);
      }

      // Run the next scheduled item

      mLastExecutedSchedulerIndexVariable.set(mLastExecutedSchedulerIndexVariable.get() + 1);
      if ( mLastExecutedSchedulerIndexVariable.get() > mListOfActivatedSchedulers.size() - 1) {
        mLastExecutedSchedulerIndexVariable.set(0);
      }


      SchedulerInterface lNextSchedulerToRun = mListOfActivatedSchedulers.get(
              mLastExecutedSchedulerIndexVariable.get());

      if (!mInitializedSchedulerList.contains(lNextSchedulerToRun)) {
        //log( "Initializing " + lNextSchedulerToRun);
        lNextSchedulerToRun.setMicroscope(getMicroscope());
        lNextSchedulerToRun.initialize();
        mInitializedSchedulerList.add(lNextSchedulerToRun);
      }

      log( "Starting " + lNextSchedulerToRun);
      double duration = ElapsedTime.measure("instructions execution", () -> {
                lNextSchedulerToRun.enqueue(getTimePointCounterVariable().get());
      });
      log("Finished " + lNextSchedulerToRun);

      // store how long the execution took
      SchedulerDurationContainer lContainer = new SchedulerDurationContainer(getTimePointCounterVariable().get(), lNextSchedulerToRun, duration);
      mLightSheetMicroscope.getDataWarehouse().put("duration_" + getTimePointCounterVariable().get(), lContainer);

      /*
      ArrayList<SchedulerInterface>
          lSchedulerInterfaceList = getMicroscope().getDevices(SchedulerInterface.class);
      for (SchedulerInterface lSchedulerInterface : lSchedulerInterfaceList)
      {
        if (lSchedulerInterface.getActiveVariable().get()) {
          lSchedulerInterface.setMicroscope(getMicroscope());
          lSchedulerInterface.enqueue(getTimePointCounterVariable().get());
        }
      }
      */


    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  public void log(String pText) {
    if (mLogFileWriter != null) {
      try {
        mLogFileWriter.write(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())+ " (time point " + getTimePointCounterVariable().get() + ") " + pText + "\r\n");
        mLogFileWriter.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  @Deprecated
  private void interleavedAcquisition(LightSheetAcquisitionStateInterface<?> pCurrentState)
  {
    // TODO not supported for now

  }

  /**
   * This function will be deleted as soon as the
   * SequentialAcquisitionInstruction proved to be functional and
   * results in equal images
   * @param pCurrentState
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws TimeoutException
   */
  @Deprecated
  private void sequentialAcquisition(LightSheetAcquisitionStateInterface<?> pCurrentState) throws InterruptedException,
                                                                                           ExecutionException,
                                                                                           TimeoutException
  {

    int lNumberOfDetectionArms = mLightSheetMicroscope.getNumberOfDetectionArms();

    int lNumberOfLightSheets = mLightSheetMicroscope.getNumberOfLightSheets();

    HashMap<Integer, LightSheetMicroscopeQueue> lViewToQueueMap = new HashMap<>();

    // preparing queues:
    for (int l = 0; l < lNumberOfLightSheets; l++)
      if (pCurrentState.getLightSheetOnOffVariable(l).get())
      {
        LightSheetMicroscopeQueue
            lQueueForView =
            getQueueForSingleLightSheet(pCurrentState, l);

        lViewToQueueMap.put(l, lQueueForView);
      }

    // playing the queues in sequence:

    for (int l = 0; l < lNumberOfLightSheets; l++) {
      if (pCurrentState.getLightSheetOnOffVariable(l).get())
      {
        LightSheetMicroscopeQueue lQueueForView = lViewToQueueMap.get(l);

        for (int c = 0; c < lNumberOfDetectionArms; c++)
          if (pCurrentState.getCameraOnOffVariable(c).get())
          {

            StackMetaData
                lMetaData =
                lQueueForView.getCameraDeviceQueue(c)
                             .getMetaDataVariable()
                             .get();

            lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                               AcquisitionType.TimeLapse);
            lMetaData.addEntry(MetaDataView.Camera, c);
            lMetaData.addEntry(MetaDataView.LightSheet, l);

            if (getFuseStacksVariable().get())
            {
              if (getFuseStacksPerCameraVariable().get())
                lMetaData.addEntry(MetaDataFusion.RequestPerCameraFusion,
                                   true);
              else
                lMetaData.addEntry(MetaDataFusion.RequestFullFusion,
                                   true);

            }
            else
            {
              String lCxLyString = MetaDataView.getCxLyString(lMetaData);
              lMetaData.addEntry(MetaDataChannel.Channel,
                                 lCxLyString);
            }
          }

        mLightSheetMicroscope.playQueueAndWait(lQueueForView,
                                               cTimeOut,
                                               TimeUnit.SECONDS);

      }
    }

  }

  /**
   * This function will be removed. see SequentialAcquisitionInstruction
   * @param pCurrentState
   * @param pLightSheetIndex
   * @return
   */
  @Deprecated
  protected LightSheetMicroscopeQueue getQueueForSingleLightSheet(LightSheetAcquisitionStateInterface<?> pCurrentState,
                                                                  int pLightSheetIndex)
  {
    int lNumberOfDetectionArms =
                               mLightSheetMicroscope.getNumberOfDetectionArms();

    @SuppressWarnings("unused")
    int lNumberOfLightSheets =
                             mLightSheetMicroscope.getNumberOfLightSheets();

    int lNumberOfLaserLines =
                            mLightSheetMicroscope.getNumberOfLaserLines();

    int lNumberOfEDFSlices = mExtendedDepthOfFieldAcquisitionVariable.get()?10:0;

    LightSheetMicroscopeQueue lQueue =
                                     pCurrentState.getQueue(0,
                                                            lNumberOfDetectionArms,
                                                            pLightSheetIndex,
                                                            pLightSheetIndex + 1,
                                                            0,
                                                            lNumberOfLaserLines,
                                                            lNumberOfEDFSlices);

    /*for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " W: " + lQueue.getIW(l));
    }
    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " H: " + lQueue.getIH(l));
    }*/

    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                            getTimePointCounterVariable().get());

    return lQueue;
  }

  /**
   * Returns the variable holding the flag interleaved-acquisition
   * 
   * @return variable holding the flag interleaved-acquisition
   */
  public Variable<Boolean> getInterleavedAcquisitionVariable()
  {
    return mInterleavedAcquisitionVariable;
  }

  /**
   * Returns the variable holding the boolean flag that decides whether stacks
   * should or should not be fused.
   * 
   * @return fuse stacks variable
   */
  public Variable<Boolean> getFuseStacksVariable()
  {
    return mFuseStacksVariable;
  }

  public Variable<Boolean> getExtendedDepthOfFieldAcquisitionVariable()
  {
    return mExtendedDepthOfFieldAcquisitionVariable;
  }

  public Variable<Boolean> getLegacyTimelapseAcquisitionVariable()
  {
    return mLegacyTimelapseAcquisitionVariable;
  }

  /**
   * Returns the variable holding the boolean flag that decides whether stacks
   * should or should not be fused.
   * 
   * @return fuse stacks variable
   */
  public Variable<Boolean> getFuseStacksPerCameraVariable()
  {
    return mFuseStacksPerCameraVariable;
  }

  public long getTimeOut() {
    return cTimeOut;
  }

  public ArrayList<SchedulerInterface> getListOfActivatedSchedulers()
  {
    return mListOfActivatedSchedulers;
  }

  public ArrayList<SchedulerInterface> getListOfAvailableSchedulers(String... pMustContainStrings)
  {
    ArrayList<SchedulerInterface> lListOfAvailabeSchedulers = new ArrayList<>();
    for (SchedulerInterface lScheduler : mLightSheetMicroscope.getDevices(SchedulerInterface.class)) {
      boolean lNamePatternMatches = true;
      for (String part : pMustContainStrings) {
        if (!lScheduler.toString().toLowerCase().contains(part.toLowerCase())) {
          lNamePatternMatches = false;
          break;
        }
      }
      if (lNamePatternMatches) {
        lListOfAvailabeSchedulers.add(lScheduler);
      }
    }

    lListOfAvailabeSchedulers.sort(new Comparator<SchedulerInterface>()
    {
      @Override public int compare(SchedulerInterface o1,
                                   SchedulerInterface o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });

    return lListOfAvailabeSchedulers;
  }

  public Variable<Integer> getLastExecutedSchedulerIndexVariable() {
    return mLastExecutedSchedulerIndexVariable;
  }

}
