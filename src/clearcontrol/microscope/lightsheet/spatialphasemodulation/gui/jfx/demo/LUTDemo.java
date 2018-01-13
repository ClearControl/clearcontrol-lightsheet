package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.demo;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;
import org.junit.Test;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class LUTDemo
{
  @Test
  public void testLUTs(){
    LookUpTable lLookUpTable = new BlueCyanGreenYellowOrangeRedLUT();

    for (float i = 0; i <= 1; i+=0.1)
    {
      System.out.println("[" + i + "]=" + lLookUpTable.getColor(i));
    }

  }
}
