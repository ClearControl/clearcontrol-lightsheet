package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut;

import javafx.scene.paint.Color;
import org.junit.Test;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class BlueCyanGreenYellowOrangeRedLUT implements LookUpTable
{
  @Override public Color getColor(float pIndex)
  {
    if (pIndex < 0 || pIndex > 1) {
      throw new IllegalArgumentException("Colour index must be between 0 and 1 but was " + pIndex
                                         + "!");
    }

    float lInnerIndex = (pIndex * 4) % 1;

    double lRed = 0;
    double lBlue = 0;
    double lGreen = 0;

    if (pIndex < 0.25) {
      lBlue = 1;
      lGreen = lInnerIndex;
    } else if (pIndex < 0.5) {
      lBlue = 1.0f - lInnerIndex;
      lGreen = 1;
    } else if (pIndex < 0.75) {
      lRed = lInnerIndex;
      lGreen = 1;
    } else {
      lGreen = 1.0f - lInnerIndex;
      lRed = 1;
    }

    return new Color(lRed, lGreen, lBlue, 1.0);
  }
}
