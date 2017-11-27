package clearcontrol.microscope.lightsheet.calibrator.modules.impl.gui;

import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.gui.jfx.var.rangeslider.VariableRangeSlider;
import clearcontrol.microscope.lightsheet.calibrator.modules.impl.CalibrationA;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class CalibrationAPanel extends StandardCalibrationModulePanel
{

  /**
   * Instantiates an calibration A panel
   *
   * @param pCalibrationA
   *          calibration A module
   */
  public CalibrationAPanel(CalibrationA pCalibrationA) {
    super(pCalibrationA);

    addNumberTextFieldForVariable(pCalibrationA.getAngleOptimisationRangeWidthVariable().getName(), pCalibrationA.getAngleOptimisationRangeWidthVariable());
    addNumberTextFieldForVariable(pCalibrationA.getNumberOfAnglesVariable().getName(), pCalibrationA.getNumberOfAnglesVariable());
    addNumberTextFieldForVariable(pCalibrationA.getNumberOfRepeatsVariable().getName(), pCalibrationA.getNumberOfRepeatsVariable());
    addNumberTextFieldForVariable(pCalibrationA.getLightSheetWidthWhileImaging().getName(), pCalibrationA.getLightSheetWidthWhileImaging());
    addNumberTextFieldForVariable(pCalibrationA.getExposureTimeInSecondsVariable().getName(), pCalibrationA.getExposureTimeInSecondsVariable());
    addNumberTextFieldForVariable(pCalibrationA.getMaxIterationsVariable().getName(), pCalibrationA.getMaxIterationsVariable());
    addNumberTextFieldForVariable(pCalibrationA.getStoppingConditionErrorThreshold().getName(), pCalibrationA.getStoppingConditionErrorThreshold());

  }
}
