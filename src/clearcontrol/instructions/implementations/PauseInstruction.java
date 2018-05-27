package clearcontrol.instructions.implementations;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class PauseInstruction extends InstructionBase implements
        InstructionInterface
{
  long mPauseTimeInMilliseconds = 0;
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public PauseInstruction() {
    this(0);
  }

  public PauseInstruction(long pPauseTimeInMilliseconds)
  {
    super("Timing: Pause " + Utilities.humanReadableTime(pPauseTimeInMilliseconds));
    mPauseTimeInMilliseconds = pPauseTimeInMilliseconds;
  }



  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    try
    {
      Thread.sleep(mPauseTimeInMilliseconds);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    return true;
  }

  @Override
  public PauseInstruction copy() {
    return new PauseInstruction(mPauseTimeInMilliseconds);
  }
}
