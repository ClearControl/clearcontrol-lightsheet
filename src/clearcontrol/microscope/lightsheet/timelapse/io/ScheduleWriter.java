package clearcontrol.microscope.lightsheet.timelapse.io;

import java.io.*;
import java.util.ArrayList;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;

/**
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
        lOutputStream.write(lScheduler.toString() + "::"
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
        result = result + variableNameToString(lVariable)
                 + "=["
                 + lVariable.get()
                 + "] ";
      }
    }

    return result;
  }

  public static String variableNameToString(Variable lVariable)
  {
    return lVariable.getName()
                    .replace(" ", "_")
                    .replace("[", "_")
                    .replace("]", "_")
                    .replace("=", "_");
  }
}
