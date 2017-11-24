package clearcontrol.microscope.lightsheet.calibrator.modules;

/**
 * Calibration module interface
 *
 * @author royer
 */
public interface CalibrationModuleInterface
{

  /**
   * Resets this calbration module
   */
  void reset();

  String getName();


  CalibrationState getSuccessOfLastCalibration(int pIntLightSheetIndex);

  void addCalibrationStateChangeListener(CalibrationStateChangeListener pCalibrationStateChangeListener);

}
