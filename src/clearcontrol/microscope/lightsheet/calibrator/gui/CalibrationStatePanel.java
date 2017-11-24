package clearcontrol.microscope.lightsheet.calibrator.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.swing.JLabelString;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationState;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationStateChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;

import java.awt.*;
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
      add(lLabel, x, 0);
    }
    for (int y = 0; y < pCalibrationEngine.getLightSheetMicroscope().getNumberOfLightSheets(); y++)
    {
      Label lLabel = new Label("L" + y);
      add(lLabel, 0, y);
    }

    for (int x = 0; x < lModuleList.size(); x++) {
      for (int y = 0; y < pCalibrationEngine.getLightSheetMicroscope().getNumberOfLightSheets(); y++)
      {
        JLabelString lLabelString = new JLabelString("", "");
        lModuleList.get(x).addCalibrationStateChangeListener(new CalibrationStateChangeListener()
        {
          @Override public void execute(CalibrationModuleInterface pCalibrationModuleInterface,
                                        int pLightSheetIndex)
          {
            CalibrationState lCalibrationState = pCalibrationModuleInterface.getSuccessOfLastCalibration(pLightSheetIndex);

            lLabelString.getStringVariable().set("" + lCalibrationState);
            lLabelString.setBackground(Color.getColor(lCalibrationState.getColor()));
          }
        });
      }
    }
  }

}
