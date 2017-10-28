package clearcontrol.microscope.lightsheet.extendeddepthfield.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.extendeddepthfield.DepthOfFocusImagingEngine;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class DepthOfFieldFocusImagingEngineToolbar extends
                                                   CustomGridPane
{
  public DepthOfFieldFocusImagingEngineToolbar(DepthOfFocusImagingEngine pDepthOfFieldImagingEngine) {
    int lRow = 0;

    {
      Button lStart = new Button("Start");
      lStart.setAlignment(Pos.CENTER);
      lStart.setMaxWidth(Double.MAX_VALUE);
      lStart.setOnAction((e) -> {
        pDepthOfFieldImagingEngine.startTask();
      });
      GridPane.setColumnSpan(lStart, 2);
      GridPane.setHgrow(lStart, Priority.ALWAYS);
      add(lStart, 0, lRow);

      lRow++;
    }
  }
}
