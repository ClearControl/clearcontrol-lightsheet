package clearcontrol.microscope.lightsheet.timelapse.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * The MultipleExecutorInstruction can execute a list of instructions.
 *
 * deprecated: Be careful: This code might be changed in the future. Try not to
 * use this class!
 *
 * Author: @haesleinhuepf May 2018
 */
@Deprecated
public class MultipleExecutorInstruction extends InstructionBase
{
  private final InstructionInterface[] schedulersToExecute;

  public MultipleExecutorInstruction(InstructionInterface[] schedulersToExecute)
  {
    super("Smart: Execute several instructions "
          + schedulersToExecute);
    this.schedulersToExecute = schedulersToExecute;
  }

  @Override
  public boolean initialize()
  {
    for (InstructionInterface scheduler : schedulersToExecute)
    {
      scheduler.initialize();
    }
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    for (InstructionInterface scheduler : schedulersToExecute)
    {
      scheduler.enqueue(pTimePoint);
    }
    return false;
  }

  @Override
  public MultipleExecutorInstruction copy()
  {
    return new MultipleExecutorInstruction(schedulersToExecute);
  }
}
