package clearcontrol.microscope.lightsheet.calibrator.modules.impl.gui;

import clearcontrol.microscope.lightsheet.calibrator.modules.impl.CalibrationZ;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class CalibrationZPanel extends StandardCalibrationModulePanel
{
  public CalibrationZPanel(CalibrationZ pCalibrationZ) {
    super(pCalibrationZ);

    addNumberTextFieldForVariable(pCalibrationZ.getNumberOfISamples().getName(), pCalibrationZ.getNumberOfISamples());
    addNumberTextFieldForVariable(pCalibrationZ.getNumberOfDSamples().getName(), pCalibrationZ.getNumberOfDSamples());


  }
}
