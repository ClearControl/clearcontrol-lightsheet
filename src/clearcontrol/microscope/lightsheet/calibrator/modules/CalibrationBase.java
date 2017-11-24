package clearcontrol.microscope.lightsheet.calibrator.modules;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Base class providing common fields and methods for all calibration modules
 *
 * @author royer
 */
public abstract class CalibrationBase implements
                                      CalibrationModuleInterface,
                                      LoggingFeature
{
  private final CalibrationEngine mCalibrationEngine;
  private final LightSheetMicroscope mLightSheetMicroscope;

  private volatile int mIteration = 0;

  private String mName;

  /**
   * Instantiates a calibration module given a parent calibrator and lightsheet
   * microscope.
   *
   * @param pName
   *          name of the calibrator
   * @param pCalibrationEngine
   *          parent calibrator
   */
  public CalibrationBase(String pName, CalibrationEngine pCalibrationEngine)
  {
    super();
    mName = pName;
    mCalibrationEngine = pCalibrationEngine;
    mLightSheetMicroscope =
                          pCalibrationEngine.getLightSheetMicroscope();
  }

  /**
   * Returns this calibrator's parent lightsheet microscope
   * 
   * @return parent lightsheet microscope
   */
  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  /**
   * Returns this calibration module parent calibration engine
   * 
   * @return parent calibration engine
   */
  public CalibrationEngine getCalibrationEngine()
  {
    return mCalibrationEngine;
  }

  @Override
  public void reset()
  {
    resetState();
    resetIteration();
  }

  /**
   * Returns the iteration counter
   * 
   * @return iteration counter value
   */
  public int getIteration()
  {
    return mIteration;
  }

  /**
   * increment iteration counter
   */
  public void incrementIteration()
  {
    mIteration++;
  }

  /**
   * resets iteration counter
   */
  public void resetIteration()
  {
    mIteration = 0;
  }

  /**
   * Returns the number of lightsheets
   * 
   * @return number of lightsheets
   */
  public int getNumberOfLightSheets()
  {
    return getLightSheetMicroscope().getDeviceLists()
                                    .getNumberOfDevices(LightSheetInterface.class);
  }

  /**
   * Returns the number of detection arms
   * 
   * @return number of detection arms
   */
  public int getNumberOfDetectionArms()
  {
    return getLightSheetMicroscope().getDeviceLists()
                                    .getNumberOfDevices(DetectionArmInterface.class);
  }

  public String getName() {
    return mName;
  }

  private HashMap<Integer, CalibrationState> mCalibrationStates = new HashMap<>();
  private void resetState() {
    mCalibrationStates.clear();
  }

  protected void setCalibrationState(int pLightSheetIndex, CalibrationState pState) {
    if (mCalibrationStates.containsKey(pLightSheetIndex)) {
      mCalibrationStates.remove(pLightSheetIndex);
    }
    mCalibrationStates.put(pLightSheetIndex, pState);

    // call listeners
    for (CalibrationStateChangeListener lCalibrationStateChangeListener : mCalibrationStateChangeListeners) {
      lCalibrationStateChangeListener.execute(this, pLightSheetIndex);
    }
  }

  public CalibrationState getCalibrationState(int pIntLightSheetIndex) {
    if (mCalibrationStates.containsKey(pIntLightSheetIndex)) {
      return mCalibrationStates.get(pIntLightSheetIndex);
    }
    return CalibrationState.NOT_CALIBRATED;
  }


  ArrayList<CalibrationStateChangeListener>
      mCalibrationStateChangeListeners = new ArrayList<>();
  public void addCalibrationStateChangeListener(CalibrationStateChangeListener pCalibrationStateChangeListener) {
    mCalibrationStateChangeListeners.add(pCalibrationStateChangeListener);
  }


}
