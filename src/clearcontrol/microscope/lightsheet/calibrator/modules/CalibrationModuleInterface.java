package clearcontrol.microscope.lightsheet.calibrator.modules;

import clearcontrol.microscope.lightsheet.configurationstate.HasConfigurationState;
import clearcontrol.microscope.lightsheet.configurationstate.HasName;

/**
 * Calibration module interface
 *
 * @author royer
 */
public interface CalibrationModuleInterface extends
                                            HasConfigurationState,
                                            HasName
{

  /**
   * Resets this calbration module
   */
  void reset();

  String getName();

}
