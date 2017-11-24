package clearcontrol.microscope.lightsheet.calibrator.modules.impl.gui;

import clearcontrol.microscope.lightsheet.calibrator.modules.impl.CalibrationP;
import clearcontrol.microscope.lightsheet.calibrator.modules.impl.CalibrationZ;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class CalibrationPPanel extends StandardCalibrationModulePanel
{
  public CalibrationPPanel(CalibrationP pCalibrationP) {
    super(pCalibrationP);

    addNumberTextFieldForVariable(pCalibrationP.getNumberOfSamplesVariable().getName(), pCalibrationP.getNumberOfSamplesVariable());
    addNumberTextFieldForVariable(pCalibrationP.getDetectionArmVariable().getName(), pCalibrationP.getDetectionArmVariable());

    addNumberTextFieldForVariable(pCalibrationP.getMaxIterationsVariable().getName(), pCalibrationP.getMaxIterationsVariable());

  }
}
