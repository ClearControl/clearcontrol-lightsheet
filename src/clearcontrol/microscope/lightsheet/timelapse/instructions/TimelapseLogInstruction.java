package clearcontrol.microscope.lightsheet.timelapse.instructions;

import java.io.File;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleWriter;

/**
 * TimelapseLogInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class TimelapseLogInstruction extends InstructionBase
{
  private final LightSheetTimelapse mTimelapse;

  public TimelapseLogInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Log content of the timelapse schedule to disc");

    mTimelapse = pLightSheetMicroscope.getTimelapse();
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    new ScheduleWriter(mTimelapse.getListOfActivatedSchedulers(),
                       new File(mTimelapse.getWorkingDirectory(),
                                "program" + mTimelapse.getTimePointCounterVariable()
                                                      .get()
                                                                  + ".txt")).write();
    return true;
  }

  @Override
  public TimelapseLogInstruction copy()
  {
    return new TimelapseLogInstruction((LightSheetMicroscope) mTimelapse.getMicroscope());
  }
}
