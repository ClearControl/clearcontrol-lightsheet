package clearcontrol.microscope.lightsheet.extendeddepthfield.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.cameras.gui.CameraResolutionGrid;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.combo.ClassComboBoxVariable;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.microscope.lightsheet.extendeddepthfield.DepthOfFocusImagingEngine;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class DepthOfFieldFocusImagingEngineToolbar extends
                                                   CustomGridPane
{
  public DepthOfFieldFocusImagingEngineToolbar(
      DepthOfFocusImagingEngine pDepthOfFieldImagingEngine)
  {
    int lRow = 0;

    {
      addIntegerField(pDepthOfFieldImagingEngine.getDetectionArmIndex(), lRow);
      lRow++;
    }

    {
      addIntegerField(pDepthOfFieldImagingEngine.getLightSheetIndex(), lRow);
      lRow++;
    }

    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfISamples(), lRow);
      lRow++;
    }

    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfDSamples(), lRow);
      lRow++;
    }

    {
      addDoubleField(pDepthOfFieldImagingEngine.getExposureVariable(), lRow);
      lRow++;
    }

    {
      VariableFileChooser
          lRootFolderChooser =
          new VariableFileChooser("Folder:",
                                  pDepthOfFieldImagingEngine.getRootFolderVariable(),
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
          lPostFixTextField = new StringVariableTextField("Name:", pDepthOfFieldImagingEngine.getDataSetNamePostfixVariable());
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

      CameraResolutionGrid.ButtonEventHandler lButtonHandler =
          (w,
           h) -> {
            return event -> {
              pDepthOfFieldImagingEngine.getLightSheetMicroscope()
                                     .setCameraWidthHeight(w,
                                                           h);
            };
          };

      final int lMaxCameraWidth =
          pDepthOfFieldImagingEngine.getLightSheetMicroscope()
                                 .getDevice(StackCameraDeviceInterface.class,
                                            0)
                                 .getMaxWidthVariable()
                                 .get()
                                 .intValue();
      final int lMaxCameraHeight =
          pDepthOfFieldImagingEngine.getLightSheetMicroscope()
                                 .getDevice(StackCameraDeviceInterface.class,
                                            0)
                                 .getMaxHeightVariable()
                                 .get()
                                 .intValue();

      CameraResolutionGrid lGridPane =
          new CameraResolutionGrid(lButtonHandler,
                                   lMaxCameraWidth,
                                   lMaxCameraHeight);
      lGridPane.setAlignment(Pos.BASELINE_CENTER);
      GridPane.setHalignment(lGridPane, HPos.CENTER);
      GridPane.setHgrow(lGridPane, Priority.ALWAYS);
      GridPane.setColumnSpan(lGridPane, 3);
      add(lGridPane, 0, lRow);

      // setGridLinesVisible(true);

      lRow++;
    }
//
//    {
//      TextField
//          lCalibrationDataNameTextField =
//          new TextField(pCalibrationEngine.getCalibrationDataNameVariable()
//                                          .get());
//      lCalibrationDataNameTextField.setMaxWidth(Double.MAX_VALUE);
//      lCalibrationDataNameTextField.textProperty()
//                                   .addListener((obs, o, n) -> {
//                                     String lName = n.trim();
//                                     if (!lName.isEmpty())
//                                       pCalibrationEngine.getCalibrationDataNameVariable()
//                                                         .set(lName);
//
//                                   });
//      GridPane.setColumnSpan(lCalibrationDataNameTextField, 3);
//      GridPane.setFillWidth(lCalibrationDataNameTextField, true);
//      GridPane.setHgrow(lCalibrationDataNameTextField,
//                        Priority.ALWAYS);
//      add(lCalibrationDataNameTextField, 0, lRow);
//
//      lRow++;
//    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, lRow);
      lRow++;
    }

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

  private void addIntegerField(BoundedVariable<Integer> variable, int lRow) {
    NumberVariableTextField<Integer>
        lField =
        new NumberVariableTextField<Integer>(variable.getName(),
                                            variable,
                                            variable.getMin(),
                                            variable.getMax(),
                                            variable.getGranularity());
    this.add(lField.getLabel(), 0, lRow);
    this.add(lField.getTextField(), 1, lRow);

  }
  private void addDoubleField(BoundedVariable<Double> variable, int lRow) {
    NumberVariableTextField<Double>
        lField =
        new NumberVariableTextField<Double>(variable.getName(),
            variable,
            variable.getMin(),
            variable.getMax(),
            variable.getGranularity());
    this.add(lField.getLabel(), 0, lRow);
    this.add(lField.getTextField(), 1, lRow);
  }
}
