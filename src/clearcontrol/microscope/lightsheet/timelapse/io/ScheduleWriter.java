package clearcontrol.microscope.lightsheet.timelapse.io;

import clearcontrol.instructions.InstructionInterface;

import java.io.*;
import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class ScheduleWriter
{

  private final ArrayList<InstructionInterface> mScheduledList;
  private final File mTargetFile;

  public ScheduleWriter(ArrayList<InstructionInterface> pScheduledList, File pTargetFile) {
    mScheduledList = pScheduledList;
    mTargetFile = pTargetFile;
  }

  public boolean write()
  {
    try
    {
      BufferedWriter lOutputStream = new BufferedWriter(new FileWriter(mTargetFile));
      for (InstructionInterface lScheduler : mScheduledList) {
        lOutputStream.write(lScheduler.toString() + "\n");
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
}
