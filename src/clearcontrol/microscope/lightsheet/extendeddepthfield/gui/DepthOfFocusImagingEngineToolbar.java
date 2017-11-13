package clearcontrol.microscope.lightsheet.extendeddepthfield.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.cameras.gui.CameraResolutionGrid;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.combo.ClassComboBoxVariable;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.microscope.lightsheet.extendeddepthfield.DepthOfFocusImagingEngine;
import eu.hansolo.enzo.simpleindicator.SimpleIndicator;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * This is the user interface for imaging exetended depth of field
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * October 2017
 */
public class DepthOfFocusImagingEngineToolbar extends
                                                   CustomGridPane
{
  public DepthOfFocusImagingEngineToolbar(
      DepthOfFocusImagingEngine pDepthOfFieldImagingEngine)
  {
    int lRow = 0;

    {
      SimpleIndicator lAcquisitionStateIndicator = new SimpleIndicator();
      lAcquisitionStateIndicator.indicatorStyleProperty().set(SimpleIndicator.IndicatorStyle.RED);
      pDepthOfFieldImagingEngine.getIsRunningVariable().addSetListener((o, n) -> {
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
        pDepthOfFieldImagingEngine.startTask();
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
        pDepthOfFieldImagingEngine.stopTask();
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
      addIntegerField(pDepthOfFieldImagingEngine.getDetectionArmIndex(), lRow);
      lRow++;
    }
    {
      addIntegerField(pDepthOfFieldImagingEngine.getLightSheetMinIndex(), lRow);
      lRow++;
    }
    {
      addIntegerField(pDepthOfFieldImagingEngine.getLightSheetMaxIndex(), lRow);
      lRow++;
    }

    {
      VariableCheckBox lDetectionArmFixed =
          new VariableCheckBox("",
                               pDepthOfFieldImagingEngine.getDetectionArmFixedVariable());

      Label lInterleavedAcquisitionLabel =
          new Label("Detection arm position fixed \n(otherwise: light sheet position fixed)");

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
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfISamples(), lRow);
      lRow++;
    }

    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfDSamples(), lRow);
      lRow++;
    }

    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfPrecisionIncreasingIterations(),
                      lRow);
      lRow++;
    }

    {
      addDoubleField(pDepthOfFieldImagingEngine.getMinimumRange(),
                      lRow);
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
      GridPane.setRowSpan(lGridPane, 5);
      add(lGridPane, 2, 3);

      // setGridLinesVisible(true);

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
