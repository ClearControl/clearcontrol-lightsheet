package clearcontrol.microscope.lightsheet.calibrator.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationState;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationStateChangeListener;
import javafx.scene.control.Label;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class CalibrationStatePanel extends CustomGridPane
{


  public CalibrationStatePanel(CalibrationEngine pCalibrationEngine) {

    ArrayList<CalibrationModuleInterface> lModuleList = pCalibrationEngine.getModuleList();

    for (int x = 0; x < lModuleList.size(); x++) {
      Label lLabel = new Label(lModuleList.get(x).getName());
      add(lLabel, x + 1, 0);
    }
    for (int y = 0; y < pCalibrationEngine.getLightSheetMicroscope().getNumberOfLightSheets(); y++)
    {
      Label lLabel = new Label("L" + y);
      add(lLabel, 0, y + 1);
    }

    for (int x = 0; x < lModuleList.size(); x++) {
      for (int y = 0; y < pCalibrationEngine.getLightSheetMicroscope().getNumberOfLightSheets(); y++)
      {
        CalibrationStateLabel lCalibrationStateLabel = new CalibrationStateLabel("", "");
        lModuleList.get(x).addCalibrationStateChangeListener(new CalibrationStateChangeListener()
        {
          @Override public void execute(CalibrationModuleInterface pCalibrationModuleInterface,
                                        int pLightSheetIndex)
          {
            CalibrationState lCalibrationState = pCalibrationModuleInterface.getCalibrationState(pLightSheetIndex);

            lCalibrationStateLabel.getStringVariable().set("" + lCalibrationState);
            lCalibrationStateLabel.setStyle("-fx-border-color:white; -fx-font-color:white; -fx-background-color: " + lCalibrationState.getColor().toLowerCase() + ";");
          }
        });
        add(lCalibrationStateLabel, x+1, y+1);
      }
    }
  }

}
