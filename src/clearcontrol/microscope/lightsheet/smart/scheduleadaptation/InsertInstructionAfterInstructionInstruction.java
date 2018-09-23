package clearcontrol.microscope.lightsheet.smart.scheduleadaptation;

import java.util.ArrayList;

import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

/**
 * The InsertInstructionAfterInstructionInstruction inserts a given instructions
 * after any instructions of a certain class.
 *
 * Author: @haesleinhuepf 05 2018
 */
public class InsertInstructionAfterInstructionInstruction<T extends InstructionInterface>
                                                         extends
                                                         LightSheetMicroscopeInstructionBase
{
  private final LightSheetMicroscope lightSheetMicroscope;
  private final InstructionInterface instructionToInsert;
  private final Class<T> instructionToInsertBefore;

  public InsertInstructionAfterInstructionInstruction(LightSheetMicroscope lightSheetMicroscope,
                                                      InstructionInterface instructionToInsert,
                                                      Class<T> instructionToInsertBefore)
  {
    super("Smart: Insert " + instructionToInsert
          + " before any "
          + instructionToInsertBefore,
          lightSheetMicroscope);
    this.lightSheetMicroscope = lightSheetMicroscope;
    this.instructionToInsert = instructionToInsert;
    this.instructionToInsertBefore = instructionToInsertBefore;
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {

    LightSheetTimelapse lTimelapse =
                                   lightSheetMicroscope.getTimelapse();

    // add myself to the instructions so that I'll be asked again after next
    // imaging sequence
    ArrayList<InstructionInterface> schedule =
                                             lTimelapse.getCurrentProgram();
    for (int i = (int) pTimePoint; i < schedule.size() - 1; i++)
    {
      InstructionInterface lInstruction = schedule.get(i);
      InstructionInterface lFollowingInstruction =
                                                 schedule.get(i + 1);
      if ((instructionToInsertBefore.isInstance(lInstruction)
           && (lFollowingInstruction != instructionToInsert)))
      {
        schedule.add(i + 1, instructionToInsert.copy());
        i++;
      }
    }

    return true;
  }

  @Override
  public InsertInstructionAfterInstructionInstruction copy()
  {
    return new InsertInstructionAfterInstructionInstruction(getLightSheetMicroscope(),
                                                            instructionToInsert,
                                                            instructionToInsertBefore);
  }
}
