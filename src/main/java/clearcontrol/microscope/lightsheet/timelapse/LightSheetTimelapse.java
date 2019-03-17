package clearcontrol.microscope.lightsheet.timelapse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import clearcontrol.core.concurrent.timing.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.var.combo.enums.TimeUnitEnum;
import clearcontrol.instructions.ExecutableInstructionList;
import clearcontrol.instructions.HasInstructions;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionEngine;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.timelapse.containers.InstructionDurationContainer;
import clearcontrol.instructions.io.ScheduleWriter;
import clearcontrol.microscope.timelapse.TimelapseBase;
import clearcontrol.microscope.timelapse.TimelapseInterface;

/**
 * A LightSheetTimelapse is a list of instructions, which are executed one by
 * one as long as the timelapse is running.
 *
 * @author royer
 * @author haesleinhuepf
 */
public class LightSheetTimelapse<M extends HasInstructions> extends TimelapseBase implements
                                 TimelapseInterface,
                                 LoggingFeature
{

  private static final long cTimeOut = 1000;
  private static final int cMinimumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfLiveStacks = 16;

  private final LightSheetMicroscope mLightSheetMicroscope;

  private ExecutableInstructionList<M> mCurrentProgram;

  private Variable<Integer> mLastExecutedInstructionIndexVariable =
                                                                  new Variable<Integer>("Last executed instructions index",
                                                                                        -1);

  private Variable<String> mDatasetComment = new Variable<String>("Comment", "");

  ArrayList<InstructionInterface> mInitializedInstructionsList;

  private BufferedWriter mLogFileWriter;

  /**
   * @param pLightSheetMicroscope
   *          microscope
   */
  public LightSheetTimelapse(LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pLightSheetMicroscope);
    mCurrentProgram = new ExecutableInstructionList<M>((M)pLightSheetMicroscope);
    mLightSheetMicroscope = pLightSheetMicroscope;

    this.getMaxNumberOfTimePointsVariable().set(99999999L);
    this.getTimelapseTimerVariable().get().getAcquisitionIntervalUnitVariable().set(TimeUnitEnum.Milliseconds);
  }

  @Override
  public void acquire()
  {
    if (getTimePointCounterVariable().get() == 0)
    {
      File lCommentFile = new File(getWorkingDirectory(),
              "comment.txt");

      File lLogFile = new File(getWorkingDirectory(),
                               "scheduleLog.txt");

      lLogFile.getParentFile().mkdir();

      mLightSheetMicroscope.getDataWarehouse().clear();

      try
      {
        BufferedWriter commentWriter = new BufferedWriter(new FileWriter(lCommentFile));
        commentWriter.write(mDatasetComment.get());
        commentWriter.close();

        if (mLogFileWriter != null)
        {
          mLogFileWriter.close();
          mLogFileWriter = null;
        }
        mLogFileWriter = new BufferedWriter(new FileWriter(lLogFile));
        mLogFileWriter.write("Max timepoints      "
                             + getTimePointCounterVariable().get()
                             + "\n");
        mLogFileWriter.write("Folder              "
                             + getWorkingDirectory() + "\n");

        InterpolatedAcquisitionState lState =
                                            (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager()
                                                                                                .getCurrentState();
        mLogFileWriter.write("Min Z               "
                             + lState.getStackZLowVariable().get()
                             + "\n");
        mLogFileWriter.write("Max Z               "
                             + lState.getStackZHighVariable().get()
                             + "\n");
        mLogFileWriter.write("Slice distance      "
                             + lState.getStackZStepVariable() + "\n");

        mLogFileWriter.write("Exposure time (s)   "
                             + lState.getExposureInSecondsVariable()
                                     .get()
                             + "\n");
        mLogFileWriter.write("Image width         "
                             + lState.getImageWidthVariable().get()
                             + "\n");
        mLogFileWriter.write("Image height        "
                             + lState.getImageHeightVariable().get()
                             + "\n");
        mLogFileWriter.write("Slice distance      "
                             + lState.getStackZStepVariable() + "\n");

        mLogFileWriter.write("DataWarehouse items "
                             + mLightSheetMicroscope.getDataWarehouse()
                                                    .keySet()
                                                    .size()
                             + "\n");
        mLogFileWriter.write("Schedule items      "
                             + mCurrentProgram.size() + "\n");

        mLogFileWriter.write(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())
                             + " (time point "
                             + getTimePointCounterVariable().get()
                             + ") "
                             + "Starting log\r\n");
      }
      catch (IOException e)
      {
        e.printStackTrace();
        mLogFileWriter = null;
      }

      File lProgramFile = new File(getWorkingDirectory(),
                                   "program.txt");
      ScheduleWriter writer = new ScheduleWriter(mCurrentProgram,
                                                 lProgramFile);
      writer.write();

      mInitializedInstructionsList =
                                   new ArrayList<InstructionInterface>();

      LightSheetFastFusionProcessor lLightSheetFastFusionProcessor =
                                                                   mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class,
                                                                                                   0);
      LightSheetFastFusionEngine lLightSheetFastFusionEngine =
                                                             lLightSheetFastFusionProcessor.getEngine();
      if (lLightSheetFastFusionEngine != null)
      {
        lLightSheetFastFusionEngine.reset(true);
      }
      mLastExecutedInstructionIndexVariable.set(-1);
    }

    if (getStopSignalVariable().get())
    {
      return;
    }

    try
    {
      LightSheetFastFusionProcessor lLightSheetFastFusionProcessor =
                                                                   mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class,
                                                                                                   0);
      info("Executing timepoint: "
           + getTimePointCounterVariable().get()
           + " data warehouse holds "
           + mLightSheetMicroscope.getDataWarehouse().size()
           + " items");

      mLightSheetMicroscope.useRecycler("3DTimelapse",
                                        cMinimumNumberOfAvailableStacks,
                                        cMaximumNumberOfAvailableStacks,
                                        cMaximumNumberOfLiveStacks);

      // Determine the next instruction
      mLastExecutedInstructionIndexVariable.set(mLastExecutedInstructionIndexVariable.get()
                                                + 1);
      if (mLastExecutedInstructionIndexVariable.get() > mCurrentProgram.size()
                                                        - 1)
      {
        mLastExecutedInstructionIndexVariable.set(0);
      }

      InstructionInterface lNextSchedulerToRun =
                                               mCurrentProgram.get(mLastExecutedInstructionIndexVariable.get());

      // if the instruction wasn't initialized yet, initialize it now!
      if (!mInitializedInstructionsList.contains(lNextSchedulerToRun))
      {
        lNextSchedulerToRun.initialize();
        mInitializedInstructionsList.add(lNextSchedulerToRun);
      }

      log("Starting " + lNextSchedulerToRun);
      double duration = ElapsedTime.measure("instructions execution",
                                            () -> {
                                              lNextSchedulerToRun.enqueue(getTimePointCounterVariable().get());
                                            });
      log("Finished " + lNextSchedulerToRun);

      // store how long the execution took
      InstructionDurationContainer lContainer =
                                              new InstructionDurationContainer(getTimePointCounterVariable().get(),
                                                                               lNextSchedulerToRun,
                                                                               duration);
      mLightSheetMicroscope.getDataWarehouse()
                           .put("duration_"
                                + getTimePointCounterVariable().get(),
                                lContainer);
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  public void log(String pText)
  {
    if (mLogFileWriter != null)
    {
      try
      {
        mLogFileWriter.write(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())
                             + " (time point "
                             + getTimePointCounterVariable().get()
                             + ") "
                             + pText
                             + "\r\n");
        mLogFileWriter.flush();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  public long getTimeOut()
  {
    return cTimeOut;
  }

  /**
   * Deprecated: use getCurrentProgram() instead
   * 
   * @return current program as list of instructions
   */
  @Deprecated
  public ArrayList<InstructionInterface> getListOfActivatedSchedulers()
  {
    return getCurrentProgram();
  }

  /**
   *
   * @return current program as list of instructions
   */
  public ExecutableInstructionList<M> getCurrentProgram()
  {
    return mCurrentProgram;
  }

  /**
   * Use microscope.getInstructions()
   * @param pMustContainStrings
   * @return
   */
  @Deprecated
  public ArrayList<InstructionInterface> getListOfAvailableSchedulers(String... pMustContainStrings)
  {
    ArrayList<InstructionInterface> lListOfAvailabeSchedulers =
                                                              new ArrayList<>();
    for (InstructionInterface lScheduler : mLightSheetMicroscope.getDevices(InstructionInterface.class))
    {
      boolean lNamePatternMatches = true;
      for (String part : pMustContainStrings)
      {
        if (!lScheduler.toString()
                       .toLowerCase()
                       .contains(part.toLowerCase()))
        {
          lNamePatternMatches = false;
          break;
        }
      }
      if (lNamePatternMatches)
      {
        lListOfAvailabeSchedulers.add(lScheduler);
      }
    }

    lListOfAvailabeSchedulers.sort(new Comparator<InstructionInterface>()
    {
      @Override
      public int compare(InstructionInterface o1,
                         InstructionInterface o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });

    return lListOfAvailabeSchedulers;
  }

  public Variable<Integer> getLastExecutedSchedulerIndexVariable()
  {
    return mLastExecutedInstructionIndexVariable;
  }

  public Variable<String> getDatasetComment() {
    return mDatasetComment;
  }
}
