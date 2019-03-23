package clearcontrol.instructions.implementations;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;

import java.io.File;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) February 2018
 */
public class PauseUntilAFileAppearsInstruction extends LightSheetMicroscopeInstructionBase
                              implements InstructionInterface
{
  BoundedVariable<Integer> mPauseTimeInMilliseconds =
                                                    new BoundedVariable<>("Interval in ms",
                                                                          500,
                                                                          100,
                                                                          Integer.MAX_VALUE);
  private Variable<File> mRootFolderVariable;

  public PauseUntilAFileAppearsInstruction(LightSheetMicroscope lightSheetMicroscope)
  {
    super("Timing: Pause until a new file in a folder appears", lightSheetMicroscope);

    mRootFolderVariable = new Variable("RootFolder",
            new File(System.getProperty("user.home")
                    + "/Desktop"));
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    int numberOfFiles = mRootFolderVariable.get().listFiles().length;

    while (true) {
      if (!getLightSheetMicroscope().getTimelapse().getIsRunningVariable().get()) {
        return false;
      }
      if (mRootFolderVariable.get().listFiles().length > numberOfFiles) {
        return true;
      }
      try {
        Thread.sleep(mPauseTimeInMilliseconds.get());
      } catch (InterruptedException e) {
        e.printStackTrace();
        return false;
      }
    }
  }

  @Override
  public PauseUntilAFileAppearsInstruction copy()
  {
    PauseUntilAFileAppearsInstruction copied = new PauseUntilAFileAppearsInstruction(getLightSheetMicroscope());
    copied.mPauseTimeInMilliseconds.set(mPauseTimeInMilliseconds.get());
    copied.mRootFolderVariable.set(mRootFolderVariable.get());
    return copied;
  }

  @Override
  public String getDescription() {
    return "Pause instruction execution until the number of files in a folder increase.";
  }

  /*
  @Override
  public String toString()
  {
    return "Timing: Pause "
           + Utilities.humanReadableTime(mPauseTimeInMilliseconds.get());
  }*/

  @Override
  public String getName()
  {
    return toString();
  }

  public BoundedVariable<Integer> getPauseTimeInMilliseconds()
  {
    return mPauseTimeInMilliseconds;
  }

  public Variable<File> getRootFolderVariable() {
    return mRootFolderVariable;
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[0];
  }
}
