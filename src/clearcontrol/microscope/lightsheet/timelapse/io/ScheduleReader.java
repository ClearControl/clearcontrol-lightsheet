package clearcontrol.microscope.lightsheet.timelapse.io;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class ScheduleReader
{
  private final ArrayList<SchedulerInterface> mSchedulerList;
  private final LightSheetMicroscope mLightSheetMicroscope;
  private final File mSourceFile;

  public ScheduleReader(ArrayList<SchedulerInterface> pSchedulerList, LightSheetMicroscope pLightSheetMicroscope, File pSourceFile) {
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


    String[] lSchedulerNames = sb.toString().split("\n");
    for (String lSchedulerName : lSchedulerNames) {
      mSchedulerList.add(mLightSheetMicroscope.getSchedulerDevice(lSchedulerName.replace("\r", "")));
    }

    return true;
  }
}
