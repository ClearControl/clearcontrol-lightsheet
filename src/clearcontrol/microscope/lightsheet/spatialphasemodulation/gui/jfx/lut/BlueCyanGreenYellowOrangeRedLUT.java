package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut;

import javafx.scene.paint.Color;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class BlueCyanGreenYellowOrangeRedLUT implements LookUpTable
{
  @Override public Color getColor(float index)
  {
    if (index < 0 || index > 1) {
      throw new IllegalArgumentException("Colour index must be between 0 and 1 but was " + index + "!");
    }

    float innerIndex = (index * 4) % 1;

    double red = 0;
    double blue = 0;
    double green = 0;

    if (index < 0.25) {
      blue = 1;
      green = innerIndex;
    } else if (index < 0.5) {
      blue = 1.0f - innerIndex;
      green = 1;
    } else if (index < 0.75) {
      red = innerIndex;
      green = 1;
    } else {
      green = 1.0f - innerIndex;
      red = 1;
    }

    return new Color(red, green, blue, 1.0);
  }
}
