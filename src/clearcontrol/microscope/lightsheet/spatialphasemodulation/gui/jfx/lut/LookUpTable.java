package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut;

import javafx.scene.paint.Color;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public interface LookUpTable
{
  /**
   * Define colours depending on a index value
   * @param index value between 0 and 1
   * @return corresponding color
   */
  Color getColor(float index);
}
