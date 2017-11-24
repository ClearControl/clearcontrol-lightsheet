package clearcontrol.microscope.lightsheet.calibrator.modules.impl.gui;

import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationBase;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.stack.metadata.StackMetaData;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class StandardCalibrationModulePanel extends CustomVariablePane
{
  public StandardCalibrationModulePanel(CalibrationModuleInterface pCalibrationModuleInterface) {
    super();
    addTab(pCalibrationModuleInterface.getName());
  }
}
