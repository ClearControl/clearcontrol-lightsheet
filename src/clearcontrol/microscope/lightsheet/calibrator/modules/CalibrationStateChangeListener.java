package clearcontrol.microscope.lightsheet.calibrator.modules;

import java.util.EventListener;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public abstract class CalibrationStateChangeListener implements
                                                     EventListener
{
  public abstract void execute(CalibrationModuleInterface pCalibrationModuleInterface, int pLightSheetIndex);
}
