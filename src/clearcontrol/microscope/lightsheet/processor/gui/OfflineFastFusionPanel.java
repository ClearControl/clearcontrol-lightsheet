package clearcontrol.microscope.lightsheet.processor.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.microscope.lightsheet.processor.OfflineFastFusionEngine;
import eu.hansolo.enzo.simpleindicator.SimpleIndicator;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class OfflineFastFusionPanel extends
                                    CustomGridPane
{

  public OfflineFastFusionPanel(
      OfflineFastFusionEngine pOfflineFastFusionEngine)
  {


    int lRow = 0;
    {
      SimpleIndicator lOfflineFusionStateIndicator = new SimpleIndicator();
      lOfflineFusionStateIndicator.indicatorStyleProperty().set(SimpleIndicator.IndicatorStyle.RED);
      pOfflineFastFusionEngine.getIsRunningVariable().addSetListener((o, n) -> {
        lOfflineFusionStateIndicator.onProperty().set(n);
      });

      lOfflineFusionStateIndicator.setMinSize(50, 50);

      GridPane.setRowSpan(lOfflineFusionStateIndicator, 2);

      add(lOfflineFusionStateIndicator, 0, 0);
    }
    {
      Button lStart = new Button("Start");
      lStart.setAlignment(Pos.CENTER);
      lStart.setMaxWidth(Double.MAX_VALUE);
      lStart.setOnAction((e) -> {
        pOfflineFastFusionEngine.startTask();
      });
      //GridPane.setColumnSpan(lStart, 2);
      GridPane.setHgrow(lStart, Priority.ALWAYS);
      add(lStart, 1, lRow);

      lRow++;
    }

    {
      Button lStop = new Button("Stop");
      lStop.setAlignment(Pos.CENTER);
      lStop.setMaxWidth(Double.MAX_VALUE);
      lStop.setOnAction((e) -> {
        pOfflineFastFusionEngine.stopTask();
      });
      //GridPane.setColumnSpan(lStop, 2);
      GridPane.setHgrow(lStop, Priority.ALWAYS);
      add(lStop, 1, lRow);

      lRow++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      VariableFileChooser
          lRootFolderChooser =
          new VariableFileChooser("Folder:",
                                  pOfflineFastFusionEngine.getRootFolderVariable(),
                                  true);
      GridPane.setColumnSpan(lRootFolderChooser.getLabel(), Integer.valueOf(1));
      GridPane.setColumnSpan(lRootFolderChooser.getTextField(),
                             Integer.valueOf(2));
      GridPane.setColumnSpan(lRootFolderChooser.getButton(), Integer.valueOf(1));
      this.add(lRootFolderChooser.getLabel(), 0, lRow);
      this.add(lRootFolderChooser.getTextField(), 1, lRow);
      this.add(lRootFolderChooser.getButton(), 3, lRow);

      lRow++;
    }


    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      CustomVariablePane lCustomVariablePane = new CustomVariablePane();

      lCustomVariablePane.addTab("Configuration");


      lCustomVariablePane.addNumberTextFieldForVariable(
          pOfflineFastFusionEngine.getFirstTimePointToFuse().getName(),
          pOfflineFastFusionEngine.getFirstTimePointToFuse(),
          pOfflineFastFusionEngine.getFirstTimePointToFuse().getMin(),
          pOfflineFastFusionEngine.getFirstTimePointToFuse().getMax(),
          pOfflineFastFusionEngine.getFirstTimePointToFuse().getGranularity());

      lCustomVariablePane.addNumberTextFieldForVariable(
          pOfflineFastFusionEngine.getLastTimePointToFuse().getName(),
          pOfflineFastFusionEngine.getLastTimePointToFuse(),
          pOfflineFastFusionEngine.getLastTimePointToFuse().getMin(),
          pOfflineFastFusionEngine.getLastTimePointToFuse().getMax(),
          pOfflineFastFusionEngine.getLastTimePointToFuse().getGranularity());

      lCustomVariablePane.addCheckBoxForVariable(
          "Background subtraction", pOfflineFastFusionEngine.getBackgroundSubtractionSwitchVariable());

      lCustomVariablePane.addCheckBoxForVariable(
          "Downscaling by 2 in X/Y", pOfflineFastFusionEngine.getDownscaleSwitchVariable());

      lCustomVariablePane.addCheckBoxForVariable(
          "Registration", pOfflineFastFusionEngine.getRegistrationSwitchVariable() );


      lCustomVariablePane.addTab("Advanced");

      lCustomVariablePane.addNumberTextFieldForVariable(
          "Number of restarts",
          pOfflineFastFusionEngine.getNumberOfRestartsVariable(),
          0,
          Integer.MAX_VALUE,
          1);

      lCustomVariablePane.addNumberTextFieldForVariable(
          "Maximum number of evaluations",
          pOfflineFastFusionEngine.getMaxNumberOfEvaluationsVariable(),
          0,
          Integer.MAX_VALUE,
          1);

      lCustomVariablePane.addNumberTextFieldForVariable(
          "Translation search radius",
          pOfflineFastFusionEngine.getTranslationSearchRadiusVariable(),
          0d,
          1000d,
          1d);

      lCustomVariablePane.addNumberTextFieldForVariable(
          "Rotation search radius",
          pOfflineFastFusionEngine.getRotationSearchRadiusVariable(),
          0d,
          1000d,
          1d);

      lCustomVariablePane.addNumberTextFieldForVariable(
          "Smoothing constant",
          pOfflineFastFusionEngine.getSmoothingConstantVariable(),
          0d,
          1d,
          0.00001d);

      lCustomVariablePane.addCheckBoxForVariable(
          "Do background subtraction",
          pOfflineFastFusionEngine.getBackgroundSubtractionSwitchVariable());

      GridPane.setColumnSpan(lCustomVariablePane, 2);
      add(lCustomVariablePane, 0, lRow);
      lRow++;
    }


  }
}
