package clearcontrol.microscope.lightsheet.component.lightsheet.demo;

import clearcontrol.devices.signalgen.staves.StaveInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.si.BinaryStructuredIlluminationPattern;
import clearcontrol.microscope.lightsheet.component.lightsheet.si.StructuredIlluminationPatternInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) January 2018
 */
public class StructuredIlluminationDemo
{
  public static void main(String... args)
  {
    StructuredIlluminationPatternInterface lStructuredIlluminationPatternInterface =
                                                                                   /*  new ClosureStructuredIlluminationPattern(new SteppingFunction()
                                                                                     {
                                                                                       @Override public float function(int pIndex)
                                                                                       {
                                                                                         return pIndex%2;
                                                                                       }
                                                                                     }, 10);*/

                                                                                   new BinaryStructuredIlluminationPattern();

    StaveInterface stave =
                         lStructuredIlluminationPatternInterface.getStave(0.1);

    for (float d = 0; d < 1; d += 0.05)
    {
      System.out.println("si[" + d + "] = " + stave.getValue(d));
    }
  }
}
