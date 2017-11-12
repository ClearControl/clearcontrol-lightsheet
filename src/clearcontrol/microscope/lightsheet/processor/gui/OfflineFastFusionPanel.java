package clearcontrol.microscope.lightsheet.processor.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.processor.OfflineFastFusionProcessor;
import eu.hansolo.enzo.simpleindicator.SimpleIndicator;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
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
      OfflineFastFusionProcessor pOfflineFastFusionProcessor)
  {

    int lRow = 0;
    {
      SimpleIndicator lAcquisitionStateIndicator = new SimpleIndicator();
      lAcquisitionStateIndicator.indicatorStyleProperty().set(SimpleIndicator.IndicatorStyle.RED);
      pOfflineFastFusionProcessor.getIsRunningVariable().addSetListener((o, n) -> {
        lAcquisitionStateIndicator.onProperty().set(n);
      });

      lAcquisitionStateIndicator.setMinSize(50, 50);

      GridPane.setRowSpan(lAcquisitionStateIndicator, 2);

      add(lAcquisitionStateIndicator, 0, 0);
    }
    {
      Button lStart = new Button("Start");
      lStart.setAlignment(Pos.CENTER);
      lStart.setMaxWidth(Double.MAX_VALUE);
      lStart.setOnAction((e) -> {
        pOfflineFastFusionProcessor.startTask();
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
        pOfflineFastFusionProcessor.stopTask();
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
                                  pOfflineFastFusionProcessor.getRootFolderVariable(),
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
      StringVariableTextField
          lPostFixTextField =
          new StringVariableTextField("Name:",
                                      pOfflineFastFusionProcessor.getDataSetNamePostfixVariable());
      /*ClassComboBoxVariable
          lStackSinkComboBox = new ClassComboBoxVariable(pTimelapseInterface.getCurrentFileStackSinkTypeVariable(), pTimelapseInterface.getFileStackSinkTypeList(), 100);*/
      GridPane.setColumnSpan(lPostFixTextField.getLabel(), Integer.valueOf(1));
      GridPane.setColumnSpan(lPostFixTextField.getTextField(), Integer.valueOf(2));
      /*GridPane.setColumnSpan(lStackSinkComboBox, Integer.valueOf(1));*/
      this.add(lPostFixTextField.getLabel(), 0, lRow);
      this.add(lPostFixTextField.getTextField(), 1, lRow);
      /*this.add(lStackSinkComboBox, 3, lRow);*/

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
      Label lInterleavedAcquisitionLabel =
          new Label("Fusion/Registration parameters (important parameters on top)");

      GridPane.setHalignment(lInterleavedAcquisitionLabel,
                             HPos.RIGHT);
      GridPane.setColumnSpan(lInterleavedAcquisitionLabel,
                             3);

      add(lInterleavedAcquisitionLabel, 0, lRow);
      lRow++;
    }



    {
      VariableCheckBox lDetectionArmFixed =
          new VariableCheckBox("",
                               pOfflineFastFusionProcessor.getBackgroundSubtractionSwitchVariable());

      Label lInterleavedAcquisitionLabel =
          new Label("Background subtraction");

      GridPane.setHalignment(lDetectionArmFixed.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lDetectionArmFixed.getCheckBox(),
                             1);
      GridPane.setColumnSpan(lInterleavedAcquisitionLabel, 3);

      add(lInterleavedAcquisitionLabel, 0, lRow);
      add(lDetectionArmFixed.getCheckBox(), 1, lRow);
      lRow++;
    }

    {
      VariableCheckBox lDetectionArmFixed =
          new VariableCheckBox("",
                               pOfflineFastFusionProcessor.getDownscaleSwitchVariable());

      Label lInterleavedAcquisitionLabel =
          new Label("Downscaling");

      GridPane.setHalignment(lDetectionArmFixed.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lDetectionArmFixed.getCheckBox(),
                             1);
      GridPane.setColumnSpan(lInterleavedAcquisitionLabel, 3);

      add(lInterleavedAcquisitionLabel, 0, lRow);
      add(lDetectionArmFixed.getCheckBox(), 1, lRow);
      lRow++;
    }

    {
      VariableCheckBox lRegistrationSwitch =
          new VariableCheckBox("",
                               pOfflineFastFusionProcessor.getRegistrationSwitchVariable());

      Label lRegistrationLabel =
          new Label("Registration");

      GridPane.setHalignment(lRegistrationSwitch.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lRegistrationSwitch.getCheckBox(),
                             1);
      GridPane.setColumnSpan(lRegistrationLabel, 3);

      add(lRegistrationLabel, 0, lRow);
      add(lRegistrationSwitch.getCheckBox(), 1, lRow);
      lRow++;
    }



  }
}
