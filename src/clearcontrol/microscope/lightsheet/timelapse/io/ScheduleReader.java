package clearcontrol.microscope.lightsheet.timelapse.io;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;

import java.io.*;
import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class ScheduleReader
{
  private final ArrayList<InstructionInterface> mSchedulerList;
  private final LightSheetMicroscope mLightSheetMicroscope;
  private final File mSourceFile;

  public ScheduleReader(ArrayList<InstructionInterface> pSchedulerList, LightSheetMicroscope pLightSheetMicroscope, File pSourceFile) {
    mSchedulerList = pSchedulerList;
    mLightSheetMicroscope = pLightSheetMicroscope;
    mSourceFile = pSourceFile;
  }

  public boolean read()
  {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = null;
    try
    {
      br = new BufferedReader(new FileReader(mSourceFile));
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      return false;
    }
    try {
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }
    finally {
      try
      {
        br.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
        return false;
      }
    }


    String[] lInstructionNames = sb.toString().split("\n");
    for (String lInstructionName : lInstructionNames) {
      InstructionInterface lInstruction = mLightSheetMicroscope.getSchedulerDevice(lInstructionName.replace("\r", ""));
      if (lInstruction != null) {
        mSchedulerList.add(lInstruction);
      } else {
        mSchedulerList.add(new InstructionBase("UNKNOWN INSTRUCTION: " + lInstructionName) {

          @Override
          public boolean initialize() {
            return false;
          }

          @Override
          public boolean enqueue(long pTimePoint) {
            return false;
          }

          @Override
          public InstructionInterface copy() {
            return null;
          }
        });
      }
    }

    return true;
  }
}
