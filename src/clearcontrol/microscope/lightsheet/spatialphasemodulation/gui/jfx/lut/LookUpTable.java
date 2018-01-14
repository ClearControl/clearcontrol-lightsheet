package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut;

import javafx.scene.paint.Color;

/**
 * A lookup table defines how a matrix of numbers is displayed as
 * image in RGB on screen.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public interface LookUpTable
{
  /**
   * Define colours depending on a index value
   * @param pIndex value between 0 and 1
   * @return corresponding color
   */
  Color getColor(float pIndex);
}
