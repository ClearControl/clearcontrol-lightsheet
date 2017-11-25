package clearcontrol.microscope.lightsheet.configurationstate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public enum ConfigurationState
{
  UNINITIALIZED("LIGHTGREY"),
  SUCCEEDED("LIGHTGREEN"),
  ACCEPTABLE("LIMEGREEN"),
  FAILED("#ff7c4c");

  private String mColor;
  ConfigurationState(String pColor) {
    mColor = pColor;
  }
  public String getColor() {
    return mColor;
  }
}
