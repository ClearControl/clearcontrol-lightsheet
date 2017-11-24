package clearcontrol.microscope.lightsheet.calibrator.modules;

import javafx.scene.paint.Color;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public enum CalibrationState
{
  NOT_CALIBRATED("YELLOW"),
  SUCCEEDED("LIGHTGREEN"),
  ACCEPTABLE("LIMEGREEN"),
  FAILED("#ff7c4c");

  private String mColor;
  CalibrationState(String pColor) {
    mColor = pColor;
  }
  public String getColor() {
    return mColor;
  }
}
