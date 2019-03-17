package clearcontrol.instructions.io;

import java.io.*;
import java.util.ArrayList;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.MicroscopeBase;

/**
 * The SchedulerWriter writes a given list of instructions to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class ScheduleWriter
{

  private final ArrayList<InstructionInterface> mScheduledList;
  private final File mTargetFile;

  public ScheduleWriter(ArrayList<InstructionInterface> pScheduledList,
                        File pTargetFile)
  {
    mScheduledList = pScheduledList;
    mTargetFile = pTargetFile;
  }

  public boolean write()
  {
    try
    {
      BufferedWriter lOutputStream =
                                   new BufferedWriter(new FileWriter(mTargetFile));
      for (InstructionInterface lScheduler : mScheduledList)
      {
        lOutputStream.write(lScheduler.getName() + "::"
                            + propertiesToString(lScheduler)
                            + "\n");
      }
      lOutputStream.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private String propertiesToString(InstructionInterface pInstruction)
  {

    String result = "";

    if (pInstruction instanceof PropertyIOableInstructionInterface)
    {
      Variable[] lVariableArray =
                                ((PropertyIOableInstructionInterface) pInstruction).getProperties();

      for (Variable lVariable : lVariableArray)
      {
        if (lVariable != null)
        {
          result = result + variableNameToString(lVariable)
                   + "=["
                   + lVariable.get()
                   + "] ";
        }
      }
    }

    return result;
  }

  public static String variableNameToString(Variable lVariable)
  {
    if (lVariable == null)
    {
      return "null";
    }
    return lVariable.getName()
                    .replace(" ", "_")
                    .replace("[", "_")
                    .replace("]", "_")
                    .replace("=", "_");
  }
}
