package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut;

import javafx.scene.paint.Color;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class BlackGreyWhiteLUT implements LookUpTable
{

  @Override public Color getColor(float index)
  {
    if (index < 0 || index > 1) {
      throw new IllegalArgumentException("Colour index must be between 0 and 1 but was " + index + "!");
    }
    return new Color(index, index, index, 1.0);
  }
}
