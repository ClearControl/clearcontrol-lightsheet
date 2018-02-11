package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut;

import javafx.scene.paint.Color;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class BlackGreyWhiteLUT implements LookUpTable
{

  @Override public Color getColor(float pIndex)
  {
    if (pIndex < 0 || pIndex > 1) {
      throw new IllegalArgumentException("Colour index must be between 0 and 1 but was " + pIndex
                                         + "!");
    }
    return new Color(pIndex, pIndex, pIndex, 1.0);
  }
}
