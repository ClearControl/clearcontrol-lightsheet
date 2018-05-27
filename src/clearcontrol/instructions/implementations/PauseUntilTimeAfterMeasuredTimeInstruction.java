package clearcontrol.instructions.implementations;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class PauseUntilTimeAfterMeasuredTimeInstruction extends
        InstructionBase implements
        InstructionInterface,
                                                                   LoggingFeature
{
  private final String mMeasuredTimeKey;
  long mPauseTimeInMilliseconds = 0;

  public PauseUntilTimeAfterMeasuredTimeInstruction(String pMeasuredTimeKey, long pPauseTimeInMilliseconds)
  {
    super("Timing: Pause " + Utilities.humanReadableTime(
        pPauseTimeInMilliseconds) + " after time t_" + pMeasuredTimeKey + " measurement");
    mPauseTimeInMilliseconds = pPauseTimeInMilliseconds;
    mMeasuredTimeKey = pMeasuredTimeKey;
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (!MeasureTimeInstruction.sMeasuredTime.containsKey(mMeasuredTimeKey)) {
      warning("Time measurement t_" + mMeasuredTimeKey + " does not exist!");
      return false;
    }
      Long measuredTime = MeasureTimeInstruction.sMeasuredTime.get(mMeasuredTimeKey);
      long timeToWait = mPauseTimeInMilliseconds - (System.currentTimeMillis() - measuredTime);
      if (timeToWait > 0)
      {

        try
        {
          Thread.sleep(timeToWait);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    return true;
  }

  @Override
  public PauseUntilTimeAfterMeasuredTimeInstruction copy() {
    return new PauseUntilTimeAfterMeasuredTimeInstruction(mMeasuredTimeKey, mPauseTimeInMilliseconds);
  }
}
